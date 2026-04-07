package ankol.mod.merger.merger.scr

import ankol.mod.merger.antlr.scr.TechlandScriptLexer
import ankol.mod.merger.antlr.scr.TechlandScriptParser
import ankol.mod.merger.constants.UserChoice
import ankol.mod.merger.core.BaseTreeNode
import ankol.mod.merger.core.ConflictResolver
import ankol.mod.merger.domain.MergerContext
import ankol.mod.merger.domain.ParsedResult
import ankol.mod.merger.core.filetrees.AbstractFileTree
import ankol.mod.merger.exception.BusinessException
import ankol.mod.merger.merger.AbstractFileMerger
import ankol.mod.merger.merger.ConflictRecord
import ankol.mod.merger.domain.MergeResult
import ankol.mod.merger.merger.scr.node.ScrContainerScriptNode
import ankol.mod.merger.merger.scr.node.ScrFunCallScriptNode
import ankol.mod.merger.tools.logger
import org.antlr.v4.runtime.CharStream
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import org.antlr.v4.runtime.TokenStreamRewriter

class TechlandScrFileMerger(context: MergerContext) : AbstractFileMerger(context) {
    private val log = logger()

    /**
     * 标记冲突项的容器
     */
    private val conflicts = ArrayList<ConflictRecord>()

    /**
     * 插入操作记录
     */
    private data class InsertOperation(val tokenIndex: Int, val content: String, val nodeType: NodeType = NodeType.OTHER)

    private val insertOperations = ArrayList<InsertOperation>()

    /**
     * 节点类型，用于确定插入位置的优先级
     */
    private enum class NodeType {
        IMPORT,  // import 语句 - 最高优先级，放在文件最前
        SUB,     // sub 函数声明 - 次高优先级，放在 import 之后
        OTHER    // 其他声明 - 最低优先级
    }

    /**
     * 基准MOD（data0.pak）对应文件的语法树，用于三方对比
     */
    private var vanillaRootNode: ScrContainerScriptNode? = null

    override fun merge(accumulatedFile: AbstractFileTree, incomingModFile: AbstractFileTree): MergeResult {
        try {
            val parsedResult = context.baseModManager.parseForm(accumulatedFile.fileEntryName) { parseContent(it) }
            // 解析基准MOD文件（如果存在）
            if (parsedResult != null) {
                vanillaRootNode = parsedResult.astNode
            }
            // 解析base和mod文件，保留TokenStream
            val accumulatedResult = parseContent(accumulatedFile.getContent())
            val incomingModResult = parseContent(incomingModFile.getContent())
            val accumulatedRoot: ScrContainerScriptNode = accumulatedResult.astNode
            val incomingModRoot: ScrContainerScriptNode = incomingModResult.astNode

//            accumulatedRoot.printTree()
//            incomingModRoot.printTree()

            deepCompare(vanillaRootNode, accumulatedRoot, incomingModRoot)

            //第一个mod与原版文件的对比
            if (context.isFirstMerge && !conflicts.isEmpty()) {
                for (record in conflicts) {
                    record.userChoice = UserChoice.MERGE_MOD
                }
            } else if (!conflicts.isEmpty()) {
                // 正常情况下，提示用户解决冲突
                ConflictResolver.resolveConflict(conflicts, context)
            }

            return MergeResult(getMergedContent(accumulatedResult), context.mergedHistory)
        } catch (e: Exception) {
            log.error("Error during SCR file merge: ${accumulatedFile.fileName} Reason: ${e.message}", e)
            throw BusinessException("文件${accumulatedFile.fileName}合并失败")
        } finally {
            //清理状态，准备下一个文件合并
            conflicts.clear()
            insertOperations.clear()
            vanillaRootNode = null
        }
    }

    private fun deepCompare(
        vanillaContainer: ScrContainerScriptNode?,
        accumulatedContainer: ScrContainerScriptNode,
        incomingModContainer: ScrContainerScriptNode
    ) {
        // 遍历 Mod 的所有子节点
        for ((signature, incomingModNode) in incomingModContainer.childrens) {
            try {
                var vanillaNode: BaseTreeNode? = null
                if (vanillaContainer != null) {
                    vanillaNode = vanillaContainer.childrens[signature]
                }
                val accumulatedNode = accumulatedContainer.childrens[signature]

                if (accumulatedNode == null) {
                    handleInsertion(accumulatedContainer, incomingModNode)
                } else {
                    //容器节点，递归对比
                    if (accumulatedNode is ScrContainerScriptNode && incomingModNode is ScrContainerScriptNode) {
                        deepCompare(vanillaNode as ScrContainerScriptNode?, accumulatedNode, incomingModNode)
                    }
                    //function call节点的对比逻辑
                    else if (accumulatedNode is ScrFunCallScriptNode && incomingModNode is ScrFunCallScriptNode) {
                        if (accumulatedNode.arguments != incomingModNode.arguments) {
                            if (!isNodeSameAsOriginalNode(vanillaNode, incomingModNode)) {
                                if (isNodeSameAsOriginalNode(vanillaNode, accumulatedNode)) {
                                    context.mergedHistory.markSignture("${incomingModContainer.signature}-${signature}", context.mergeModName)
                                    //base节点与原版一致，自动合并
                                    val record = ConflictRecord(
                                        context.mergingFileName,
                                        context.accumulatedModName,
                                        context.mergeModName,
                                        signature,
                                        accumulatedNode,
                                        incomingModNode
                                    )
                                    record.userChoice = UserChoice.MERGE_MOD
                                    conflicts.add(record)
                                } else {
                                    //标记真正的冲突 todo MergedHistory存在作用域混乱的问题，还需改良
                                    val modName = context.mergedHistory.getModNameFromSignature("${incomingModContainer.signature}-${signature}")
                                    conflicts.add(
                                        ConflictRecord(
                                            context.mergingFileName,
                                            modName ?: context.accumulatedModName,
                                            context.mergeModName,
                                            signature,
                                            accumulatedNode,
                                            incomingModNode
                                        )
                                    )
                                }
                            }
                        }
                    }
                    //其他类型节点直接比较文本内容
                    else {
                        val accumulatedText = accumulatedNode.sourceText
                        val incomingModText = incomingModNode.sourceText
                        //内容不一致
                        if (!equalsTrimmed(accumulatedText, incomingModText)) {
                            if (!isNodeSameAsOriginalNode(vanillaNode, incomingModNode)) {
                                if (isNodeSameAsOriginalNode(vanillaNode, accumulatedNode)) {
                                    context.mergedHistory.markSignture(incomingModNode.signature, context.mergeModName)
                                    conflicts.add(
                                        ConflictRecord(
                                            context.mergingFileName,
                                            context.accumulatedModName,
                                            context.mergeModName,
                                            signature,
                                            accumulatedNode,
                                            incomingModNode,
                                            UserChoice.MERGE_MOD
                                        )
                                    )
                                } else {
                                    conflicts.add(
                                        ConflictRecord(
                                            context.mergingFileName,
                                            context.accumulatedModName,
                                            context.mergeModName,
                                            signature,
                                            accumulatedNode,
                                            incomingModNode
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                log.error("Error in processing scr node with signature: '${signature}'", e)
            }
        }
    }

    private fun getMergedContent(accumulatedResult: ParsedResult<ScrContainerScriptNode>): String {
        val rewriter = TokenStreamRewriter(accumulatedResult.tokenStream)
        // 处理冲突节点的替换
        for (record in conflicts) {
            if (record.userChoice == UserChoice.MERGE_MOD) {
                // 普通修改冲突：用户选择了 Mod
                val accumulatedNode = record.baseNode
                val incomingModNode = record.modNode
                rewriter.replace(accumulatedNode.startTokenIndex, accumulatedNode.stopTokenIndex, incomingModNode.sourceText)
            }
        }

        // 对插入操作按照优先级和位置排序
        // 优先级：IMPORT > SUB > OTHER
        // 同一优先级内按照 tokenIndex 升序排序（从前往后插入）
        val sortedOperations = insertOperations.sortedWith(compareBy<InsertOperation> { op ->
            when (op.nodeType) {
                NodeType.IMPORT -> 0
                NodeType.SUB -> 1
                NodeType.OTHER -> 2
            }
        }.thenBy { it.tokenIndex })

        for (op in sortedOperations) {
            rewriter.insertBefore(op.tokenIndex, op.content)
        }

        // 获取重写后的文本
        return rewriter.text
    }

    private fun isNodeSameAsOriginalNode(originalNode: BaseTreeNode?, incomingModNode: BaseTreeNode): Boolean {
        // 如果没有原始基准MOD，则认为不相同
        if (vanillaRootNode == null) {
            return false
        }

        if (originalNode == null) {
            // 原始基准MOD中不存在这个节点，说明是新增的
            return false
        }

        // 对比节点内容
        return if (incomingModNode is ScrFunCallScriptNode && originalNode is ScrFunCallScriptNode) {
            // 函数调用节点，对比参数
            incomingModNode.arguments == originalNode.arguments
        } else {
            equalsTrimmed(incomingModNode.sourceText, originalNode.sourceText)
        }
    }

    private fun handleInsertion(accumulatedContainer: ScrContainerScriptNode, incomingModNode: BaseTreeNode) {
        // 根据节点签名确定节点类型
        val nodeType = when {
            incomingModNode.signature.startsWith("import:") -> NodeType.IMPORT
            incomingModNode.signature.startsWith("sub:") -> NodeType.SUB
            else -> NodeType.OTHER
        }

        var newContent: String

        // 选择合适的插入位置
        val insertPos = when (nodeType) {
            NodeType.IMPORT -> {
                // import语句需要插入在文件最顶上
                newContent = "\n${incomingModNode.sourceText}"
                findInsertPositionForImport(accumulatedContainer)
            }

            NodeType.SUB -> {
                // sub 插入到import语句后，但在其他节点前
                newContent = "${incomingModNode.sourceText}\n"
                findInsertPositionForSub(accumulatedContainer)
            }

            NodeType.OTHER -> {
                // 其他节点直接插在容器的 '}' 之前
                newContent = "\n   ${incomingModNode.sourceText}"
                accumulatedContainer.stopTokenIndex
            }
        }

        insertOperations.add(InsertOperation(insertPos, newContent, nodeType))
    }

    /**
     * 找到import语句的插入位置：在所有现有import之后，或者在第一个非import节点之前
     */
    private fun findInsertPositionForImport(container: ScrContainerScriptNode): Int {
        var lastImportStopIndex: Int? = null

        for ((_, node) in container.childrens) {
            if (node.signature.startsWith("import:")) {
                lastImportStopIndex = node.stopTokenIndex
            } else {
                // 遇到第一个非import节点，如果有import就插在最后import之后，否则插在该节点之前
                return lastImportStopIndex?.let { it + 1 } ?: node.startTokenIndex
            }
        }

        // 所有节点都是import，或者没有节点，返回容器结束位置
        return lastImportStopIndex?.let { it + 1 } ?: container.stopTokenIndex
    }

    /**
     * 找到sub函数的插入位置：在所有import和sub之后，但在其他节点之前
     */
    private fun findInsertPositionForSub(container: ScrContainerScriptNode): Int {
        var lastSubOrImportStopIndex: Int? = null

        for ((_, node) in container.childrens) {
            val isSub = node.signature.startsWith("sub:")
            val isImport = node.signature.startsWith("import:")

            if (isSub || isImport) {
                lastSubOrImportStopIndex = node.startTokenIndex
            } else {
                // 遇到第一个既不是import也不是sub的节点
                return lastSubOrImportStopIndex?.let { it + 1 } ?: node.startTokenIndex
            }
        }

        // 所有节点都是import或sub，返回容器结束位置
        return lastSubOrImportStopIndex ?: container.stopTokenIndex
    }

    private fun parseContent(content: String): ParsedResult<ScrContainerScriptNode> {
        val input: CharStream = CharStreams.fromString(content)
        val lexer = TechlandScriptLexer(input)
        val tokens = CommonTokenStream(lexer)
        val parser = TechlandScriptParser(tokens)
        val visitor = TechlandScrFileVisitor(tokens)
        // 注意：visitFile 返回的一定是我们定义的 ROOT Container
        val ast = visitor.visitFile(parser.file()) as ScrContainerScriptNode
        return ParsedResult(ast, tokens)
    }

    private fun equalsTrimmed(a: String?, b: String?): Boolean {
        // 快路径：同一引用或 equals（完全一致时直接返回，避免扫描）
        if (a == b) {
            return true
        }
        if (a == null || b == null) {
            return false
        }

        var ai = 0
        var bi = 0
        val alen = a.length
        val blen = b.length

        while (true) {
            // 跳过 a 的所有空白
            while (ai < alen && Character.isWhitespace(a[ai])) {
                ai++
            }
            // 跳过 b 的所有空白
            while (bi < blen && Character.isWhitespace(b[bi])) {
                bi++
            }

            // 两边都到尾了 -> 相等
            if (ai >= alen || bi >= blen) {
                // 需要确保剩余部分也都是空白
                while (ai < alen && Character.isWhitespace(a[ai])) ai++
                while (bi < blen && Character.isWhitespace(b[bi])) bi++
                return ai >= alen && bi >= blen
            }

            // 比较当前非空白字符
            if (a[ai] != b[bi]) {
                return false
            }
            ai++
            bi++
        }
    }
}
