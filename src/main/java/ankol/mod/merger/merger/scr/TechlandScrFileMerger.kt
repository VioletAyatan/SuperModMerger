package ankol.mod.merger.merger.scr

import ankol.mod.merger.antlr.scr.TechlandScriptLexer
import ankol.mod.merger.antlr.scr.TechlandScriptParser
import ankol.mod.merger.constants.UserChoice
import ankol.mod.merger.core.*
import ankol.mod.merger.core.filetrees.AbstractFileTree
import ankol.mod.merger.exception.BusinessException
import ankol.mod.merger.merger.ConflictRecord
import ankol.mod.merger.merger.ConflictType
import ankol.mod.merger.merger.MergeResult
import ankol.mod.merger.merger.scr.node.ScrContainerScriptNode
import ankol.mod.merger.merger.scr.node.ScrFunCallScriptNode
import ankol.mod.merger.tools.logger
import lombok.extern.slf4j.Slf4j
import org.antlr.v4.runtime.CharStream
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import org.antlr.v4.runtime.TokenStreamRewriter

@Slf4j
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
    private var originalBaseModRoot: ScrContainerScriptNode? = null

    override fun merge(file1: AbstractFileTree, file2: AbstractFileTree): MergeResult {
        try {
            val parsedResult = context.baseModManager.parseForm(file1.fileEntryName) { parseContent(it) }
            // 解析基准MOD文件（如果存在）
            if (parsedResult != null) {
                originalBaseModRoot = parsedResult.astNode
            }
            // 解析base和mod文件，保留TokenStream
            val baseResult = parseFile(file1)
            val modResult = parseFile(file2)
            val baseRoot: ScrContainerScriptNode = baseResult.astNode!!
            val modRoot: ScrContainerScriptNode = modResult.astNode!!

            //开始递归对比
            reduceCompare(originalBaseModRoot, baseRoot, modRoot)

            //第一个mod与原版文件的对比
            if (context.isFirstModMergeWithBaseMod && !conflicts.isEmpty()) {
                for (record in conflicts) {
                    if (record.conflictType == ConflictType.REMOVAL) {
                        // 删除类型冲突：MOD缺少原版节点，应该保留原版（补回缺失的代码）
                        // 因为这很可能是MOD过期导致的缺失，而非故意删除
                        record.userChoice = UserChoice.BASE_MOD
                    } else {
                        // 普通修改冲突：使用MOD修改的版本
                        record.userChoice = UserChoice.MERGE_MOD
                    }
                }
            } else if (!conflicts.isEmpty()) {
                // 正常情况下，提示用户解决冲突
                ConflictResolver.resolveConflict(conflicts)
            }

            return MergeResult(getMergedContent(baseResult), !conflicts.isEmpty())
        } catch (e: Exception) {
            log.error("Error during SCR file merge: ${file1.fileName} Reason: ${e.message}", e)
            throw BusinessException("文件${file1.fileName}合并失败")
        } finally {
            //清理状态，准备下一个文件合并
            conflicts.clear()
            insertOperations.clear()
            originalBaseModRoot = null
        }
    }

    private fun reduceCompare(
        originalContainer: ScrContainerScriptNode?,
        baseContainer: ScrContainerScriptNode,
        modContainer: ScrContainerScriptNode
    ) {
        // 遍历 Mod 的所有子节点
        for ((signature, modNode) in modContainer.childrens) {
            try {
                var originalNode: BaseTreeNode? = null
                if (originalContainer != null) {
                    originalNode = originalContainer.childrens[signature]
                }
                val baseNode = baseContainer.childrens[signature]

                if (baseNode == null) {
                    // 新增 Base 没有这个节点 -> 插入
                    handleInsertion(baseContainer, modNode)
                } else {
                    // [存在] 检查是否冲突
                    if (baseNode is ScrContainerScriptNode && modNode is ScrContainerScriptNode) {
                        // 容器节点，递归进入内部对比
                        reduceCompare(originalNode as ScrContainerScriptNode?, baseNode, modNode)
                    } else if (baseNode is ScrFunCallScriptNode && modNode is ScrFunCallScriptNode) {
                        if (baseNode.arguments != modNode.arguments) {
                            //两者内容不同，检查mod节点内容与原版是否相同
                            if (!isNodeSameAsOriginalNode(originalNode, modNode)) {
                                //检查base节点是否与原版相同
                                if (isNodeSameAsOriginalNode(originalNode, baseNode)
                                    && GlobalMergingStrategy.autoMergingCodeLine
                                ) {
                                    //base节点与原版一致，说明base节点未变动，使用mod的内容（开启了智能合并的情况下）
                                    val record = ConflictRecord(
                                        context.fileName,
                                        context.mod1Name,
                                        context.mod2Name,
                                        signature,
                                        baseNode,
                                        modNode
                                    )
                                    record.userChoice = UserChoice.MERGE_MOD
                                    conflicts.add(record)
                                } else {
                                    //真正的冲突，记录
                                    conflicts.add(
                                        ConflictRecord(
                                            context.fileName,
                                            context.mod1Name,
                                            context.mod2Name,
                                            signature,
                                            baseNode,
                                            modNode
                                        )
                                    )
                                }
                            }
                        }
                    } else {
                        val baseText = baseNode.sourceText
                        val modText = modNode.sourceText
                        //内容不一致
                        if (!equalsTrimmed(baseText, modText)) {
                            // 检查modNode是否与原始基准MOD相同
                            if (!isNodeSameAsOriginalNode(originalNode, modNode)) {
                                //对比基准节点与base节点，相同直接用mod的
                                if (isNodeSameAsOriginalNode(originalNode, baseNode)) {
                                    conflicts.add(
                                        ConflictRecord(
                                            context.fileName,
                                            context.mod1Name,
                                            context.mod2Name,
                                            signature,
                                            baseNode,
                                            modNode,
                                            userChoice = UserChoice.MERGE_MOD
                                        )
                                    )
                                } else {
                                    conflicts.add(
                                        ConflictRecord(
                                            context.fileName,
                                            context.mod1Name,
                                            context.mod2Name,
                                            signature,
                                            baseNode,
                                            modNode
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

        // 检测被MOD删除的节点（base有，但mod没有）
        detectRemovedNodes(originalContainer, baseContainer, modContainer)
    }

    /**
     * 检测被MOD删除/注释的节点
     *
     * 判断逻辑：
     * - 如果节点在base中存在，但在mod中不存在
     * - 且该节点在原版(original)中也存在，说明MOD故意删除了这个节点
     * - 需要提示用户选择是保留(使用base)还是删除(使用mod的删除操作)
     */
    private fun detectRemovedNodes(
        originalContainer: ScrContainerScriptNode?,
        baseContainer: ScrContainerScriptNode,
        modContainer: ScrContainerScriptNode
    ) {
        for ((signature, baseNode) in baseContainer.childrens) {
            val modNode = modContainer.childrens[signature]

            // base有，但mod没有 -> 可能是删除
            if (modNode == null) {
                // 检查原版是否有这个节点
                val originalNode = originalContainer?.childrens?.get(signature)

                if (originalNode != null) {
                    // 原版有这个节点，MOD也应该有但却没有
                    // 这说明MOD故意删除了这个节点，需要提示用户
                    conflicts.add(
                        ConflictRecord(
                            context.fileName,
                            context.mod1Name,
                            context.mod2Name,
                            signature,
                            baseNode,
                            null, // modNode为null表示删除
                            conflictType = ConflictType.REMOVAL
                        )
                    )
                }
                // 如果原版也没有这个节点，说明是base MOD新增的，mod没有是正常的（过期MOD）
                // 这种情况不需要特殊处理，base的内容会保留
            }
        }
    }

    private fun getMergedContent(baseResult: ParsedResult<ScrContainerScriptNode>): String {
        val rewriter = TokenStreamRewriter(baseResult.tokenStream)
        // 处理冲突节点的替换
        for (record in conflicts) {
            if (record.conflictType == ConflictType.REMOVAL) {
                // 删除类型的冲突
                if (record.userChoice == UserChoice.MERGE_MOD) {
                    // 用户选择使用MOD的版本（即删除该节点）
                    val baseNode = record.baseNode
                    rewriter.delete(baseNode.startTokenIndex, baseNode.stopTokenIndex)
                }
                // 如果选择 BASE_MOD，则保留原内容，不做任何操作
            } else if (record.userChoice == UserChoice.MERGE_MOD) {
                // 普通修改冲突：用户选择了 Mod
                val baseNode = record.baseNode
                val modNode = record.modNode
                if (modNode != null) {
                    rewriter.replace(
                        baseNode.startTokenIndex,
                        baseNode.stopTokenIndex,
                        modNode.sourceText
                    )
                }
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

    private fun isNodeSameAsOriginalNode(originalNode: BaseTreeNode?, modNode: BaseTreeNode): Boolean {
        // 如果没有原始基准MOD，则认为不相同
        if (originalBaseModRoot == null) {
            return false
        }

        if (originalNode == null) {
            // 原始基准MOD中不存在这个节点，说明是新增的
            return false
        }

        // 对比节点内容
        return if (modNode is ScrFunCallScriptNode && originalNode is ScrFunCallScriptNode) {
            // 函数调用节点，对比参数
            modNode.arguments == originalNode.arguments
        } else {
            equalsTrimmed(modNode.sourceText, originalNode.sourceText)
        }
    }

    private fun handleInsertion(baseContainer: ScrContainerScriptNode, modNode: BaseTreeNode) {
        // 根据节点签名确定节点类型
        val nodeType = when {
            modNode.signature.startsWith("import:") -> NodeType.IMPORT
            modNode.signature.startsWith("sub:") -> NodeType.SUB
            else -> NodeType.OTHER
        }

        var newContent: String

        // 选择合适的插入位置
        val insertPos = when (nodeType) {
            NodeType.IMPORT -> {
                // import语句需要插入在文件最顶上
                newContent = "\n${modNode.sourceText}"
                findInsertPositionForImport(baseContainer)
            }

            NodeType.SUB -> {
                // sub 插入到import语句后，但在其他节点前
                newContent = "${modNode.sourceText}\n"
                findInsertPositionForSub(baseContainer)
            }

            NodeType.OTHER -> {
                // 其他节点直接插在容器的 '}' 之前
                newContent = "\n   ${modNode.sourceText}"
                baseContainer.stopTokenIndex
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

    private fun parseFile(fileTree: AbstractFileTree): ParsedResult<ScrContainerScriptNode> {
        return parseContent(fileTree.getContent())
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
