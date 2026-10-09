package ankol.mod.merger.mergers.scr

import ankol.mod.merger.antlr.scr.TechlandScriptLexer
import ankol.mod.merger.antlr.scr.TechlandScriptParser
import ankol.mod.merger.constants.UserChoice
import ankol.mod.merger.core.BaseTreeNode
import ankol.mod.merger.core.filetrees.AbstractFileTree
import ankol.mod.merger.domain.MergeContext
import ankol.mod.merger.domain.MergeResult
import ankol.mod.merger.domain.ParsedResult
import ankol.mod.merger.exception.BusinessException
import ankol.mod.merger.mergers.AbstractFileMerger
import ankol.mod.merger.mergers.ConflictRecord
import ankol.mod.merger.mergers.DeletionRecord
import ankol.mod.merger.mergers.scr.node.AbstractScrNode
import ankol.mod.merger.mergers.scr.node.ScrContainerNode
import ankol.mod.merger.mergers.scr.node.ScrFunCallNode
import ankol.mod.merger.constants.ScrNodeType
import ankol.mod.merger.rules.DeletionTarget
import ankol.mod.merger.rules.RuleAction
import ankol.mod.merger.tools.ConsoleColorPrinter
import ankol.mod.merger.tools.Localizations.t
import ankol.mod.merger.tools.logger
import org.antlr.v4.runtime.Token
import org.antlr.v4.runtime.TokenStream
import org.antlr.v4.runtime.TokenStreamRewriter

class TechlandScrFileMerger(context: MergeContext) : AbstractFileMerger(context) {
    private val log = logger()

    /**
     * 标记冲突项的容器
     */
    private val conflicts = arrayListOf<ConflictRecord>()

    private val insertOperations = arrayListOf<InsertOperation>()

    private val deletionRecords = arrayListOf<DeletionRecord>()

    /** 规则为 ASK、等待用户确认的删除 */
    private val pendingDeletions = arrayListOf<PendingDeletion>()

    private data class PendingDeletion(val record: DeletionRecord, val nodeKey: String)

    /**
     * 插入操作记录
     */
    private data class InsertOperation(
        val tokenIndex: Int,
        val content: String,
        val previousSibling: BaseTreeNode?,
        val nodeType: NodeType = NodeType.DEFAULT
    )

    /**
     * 节点类型，用于确定插入位置的优先级
     */
    private enum class NodeType {
        IMPORT,  // import 语句 - 最高优先级，放在文件最前
        SUB,     // sub 函数声明 - 次高优先级，放在 import 之后
        DEFAULT    // 默认，无优先级
    }

    /**
     * 基准MOD（data0.pak）对应文件的语法树，用于三方对比
     */
    private var vanillaRootNode: ScrContainerNode? = null

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
            val accumulatedRoot = accumulatedResult.astNode
            val incomingModRoot = incomingModResult.astNode

            deepCompare(vanillaRootNode, accumulatedRoot, incomingModRoot, accumulatedRoot.signature)

            //第一个mod与原版文件的对比
            if (context.isFirstMerge && conflicts.isNotEmpty()) {
                for (record in conflicts) {
                    record.userChoice = UserChoice.MERGE_MOD
                }
            } else if (conflicts.isNotEmpty()) {
                // 正常情况下，提示用户解决冲突
                context.conflictResolver.resolveConflict(conflicts)
            }

            // 规则为 ASK 的删除交给用户确认
            resolvePendingDeletions()

            return MergeResult(getMergedContent(accumulatedResult), context.mergedHistory)
        } catch (e: Exception) {
            log.error("Error during SCR file merge: ${accumulatedFile.fileName} Reason: ${e.message}", e)
            throw BusinessException("文件${accumulatedFile.fileName}合并失败")
        } finally {
            //清理状态，准备下一个文件合并
            conflicts.clear()
            insertOperations.clear()
            deletionRecords.clear()
            pendingDeletions.clear()
            vanillaRootNode = null
        }
    }

    private fun deepCompare(
        vanillaContainer: ScrContainerNode?,
        accumulatedContainer: ScrContainerNode,
        incomingModContainer: ScrContainerNode,
        containerPath: String
    ) {
        // 重复的同名函数调用按内容对齐，其余节点仍按签名匹配
        val aligned = ScrRepeatedNodeAligner.align(vanillaContainer, accumulatedContainer, incomingModContainer)
        val vanillaChildren = aligned.vanilla
        val accumulatedChildren = aligned.accumulated

        var previousSiblingInBase: BaseTreeNode? = null // 追踪前一个兄弟节点
        // 遍历 Mod 的所有子节点
        for ((signature, incomingModNode) in aligned.incoming) {
            try {
                val vanillaNode = vanillaChildren?.get(signature)
                val accumulatedNode = accumulatedChildren[signature]

                if (accumulatedNode == null) {
                    if (!isDeletedBefore(containerPath, signature, incomingModNode, aligned.alignedFunctions)) {
                        handleInsertion(accumulatedContainer, incomingModNode, previousSiblingInBase)
                    } else {
                        log.debug("Skipping restoration of previously deleted SCR node: ${containerPath}/${signature}")
                    }
                } else {
                    previousSiblingInBase = accumulatedNode // 更新前一个兄弟节点
                    //容器节点，递归对比
                    if (accumulatedNode is ScrContainerNode && incomingModNode is ScrContainerNode) {
                        deepCompare(
                            vanillaNode as ScrContainerNode?,
                            accumulatedNode,
                            incomingModNode,
                            buildNodeKey(containerPath, signature)
                        )
                    }
                    //函数调用节点的对比处理
                    else if (accumulatedNode is ScrFunCallNode && incomingModNode is ScrFunCallNode) {
                        if (accumulatedNode.arguments != incomingModNode.arguments) {
                            if (!isNodeSameAsOriginalNode(vanillaNode, incomingModNode)) {
                                if (isNodeSameAsOriginalNode(vanillaNode, accumulatedNode)) {
                                    context.mergedHistory.markSignture(
                                        "${incomingModContainer.signature}-${signature}",
                                        context.mergeModName
                                    )
                                    //base节点与原版一致，自动合并
                                    val record = ConflictRecord(
                                        context.currentFileName,
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
                                    val modName =
                                        context.mergedHistory.getModNameFromSignature("${incomingModContainer.signature}-${signature}")
                                    conflicts.add(
                                        ConflictRecord(
                                            context.currentFileName,
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
                                            context.currentFileName,
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
                                            context.currentFileName,
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
        //删除节点检测
        detectDeletions(aligned, containerPath)
    }

    /**
     * 检测新 MOD 中缺失的节点，并按删除规则决定真删除、自动补回还是询问用户。
     */
    private fun detectDeletions(aligned: ScrRepeatedNodeAligner.AlignedChildren, containerPath: String) {
        val vanillaChildren = aligned.vanilla ?: return // 没有原版文件时无法区分删除和缺失，全部保留
        for ((signature, accumulatedNode) in aligned.accumulated) {
            if (aligned.incoming.containsKey(signature)) continue
            val vanillaNode = vanillaChildren[signature] ?: continue
            if (accumulatedNode !is AbstractScrNode) continue

            val action = context.deletionPolicy.decide(
                DeletionTarget(
                    filePath = context.currentFileName,
                    nodeType = accumulatedNode.nodeType,
                    name = accumulatedNode.name,
                    argCount = accumulatedNode.argCount,
                )
            )
            val nodeKey = historyKey(containerPath, signature, accumulatedNode, aligned.alignedFunctions)
            when (action) {
                RuleAction.RESTORE -> {
                    context.restoredNodeCount++
                    log.debug(
                        "Restoring SCR node missing from mod: file={}, node={}, line={}, mod={}",
                        context.currentFileName, signature, accumulatedNode.line, context.mergeModName
                    )
                }

                RuleAction.DELETE, RuleAction.ASK -> {
                    val record = DeletionRecord(
                        fileName = context.currentFileName,
                        deletingModName = context.mergeModName,
                        previousModName = context.accumulatedModName,
                        signature = signature,
                        accumulatedNode = accumulatedNode,
                        isModifyDeleteConflict = isAccumulatedModifiedFromVanilla(vanillaNode, accumulatedNode),
                        userChoice = if (action == RuleAction.DELETE) UserChoice.MERGE_MOD else null
                    )
                    if (action == RuleAction.DELETE) {
                        applyDeletion(record, nodeKey)
                    } else {
                        pendingDeletions.add(PendingDeletion(record, nodeKey))
                    }
                }
            }
        }
    }

    /**
     * 统一询问规则为 ASK 的删除，用户选择删除的才真正删除
     */
    private fun resolvePendingDeletions() {
        if (pendingDeletions.isEmpty()) return
        context.conflictResolver.resolveDeletionConflicts(pendingDeletions.mapTo(mutableListOf()) { it.record })
        for ((record, nodeKey) in pendingDeletions) {
            if (record.userChoice == UserChoice.MERGE_MOD) {
                applyDeletion(record, nodeKey)
            } else {
                context.restoredNodeCount++
            }
        }
    }

    private fun applyDeletion(record: DeletionRecord, nodeKey: String) {
        deletionRecords.add(record)
        context.deletedNodeCount++
        context.mergedHistory.markDeleted(nodeKey, record.deletingModName)
        val node = record.accumulatedNode
        if (record.isModifyDeleteConflict) {
            ConsoleColorPrinter.warning(t("DELETION_MODIFY_CONFLICT_WARNING", record.previousModName))
        }
        ConsoleColorPrinter.warning(
            t("DELETION_AUTO_DELETED", record.deletingModName, node.line, node.sourceText.trim())
        )
        log.info(
            "Applying SCR deletion: file={}, node={}, line={}, mod={}",
            record.fileName, record.signature, node.line, record.deletingModName
        )
    }

    /**
     * 判断节点是否已被之前的 MOD 删除（避免后续 MOD 把它补回来）
     */
    private fun isDeletedBefore(
        containerPath: String,
        signature: String,
        node: BaseTreeNode,
        alignedFunctions: Set<String>
    ): Boolean {
        val history = context.mergedHistory
        return history.isDeleted(historyKey(containerPath, signature, node, alignedFunctions))
                || history.isDeleted(buildNodeKey(containerPath, node.signature))
    }

    /**
     * 删除历史使用的节点键。按内容对齐的重复函数调用，其对齐键随累积结果变化，因此改用「函数名 + 参数」作为稳定的键。
     */
    private fun historyKey(
        containerPath: String,
        signature: String,
        node: BaseTreeNode,
        alignedFunctions: Set<String>
    ): String {
        if (node is ScrFunCallNode && node.nodeType == ScrNodeType.FUN_CALL && node.functionName in alignedFunctions) {
            return buildNodeKey(
                containerPath,
                "${TechlandScrFileVisitor.FUN_CALL}:${node.functionName}(${node.arguments.joinToString(",")})"
            )
        }
        return buildNodeKey(containerPath, signature)
    }

    private fun buildNodeKey(containerPath: String, signature: String): String = "$containerPath/$signature"

    /**
     * 判断 accumulated 节点相对于 vanilla 是否被修改过。
     *
     * 对容器节点优先做结构预检（子节点签名集合 + 参数列表），命中即提前返回，
     * 避免对整个容器文本做 O(N) 的 equalsTrimmed 比较。
     * 非容器节点或结构相同时回退到 isNodeSameAsOriginalNode 的文本比较。
     */
    private fun isAccumulatedModifiedFromVanilla(vanillaNode: BaseTreeNode, accumulatedNode: BaseTreeNode): Boolean {
        if (vanillaNode is ScrContainerNode && accumulatedNode is ScrContainerNode) {
            if (vanillaNode.childrens.keys != accumulatedNode.childrens.keys) return true
            if (vanillaNode.arguments != accumulatedNode.arguments) return true
        }
        return !isNodeSameAsOriginalNode(vanillaNode, accumulatedNode)
    }

    private fun getMergedContent(accumulatedResult: ParsedResult<ScrContainerNode>): String {
        val rewriter = TokenStreamRewriter(accumulatedResult.tokenStream)
        // 处理冲突节点的替换
        for (record in conflicts) {
            if (record.userChoice == UserChoice.MERGE_MOD) {
                // 普通修改冲突：用户选择了 Mod
                val accumulatedNode = record.baseNode
                val incomingModNode = record.modNode
                rewriter.replace(
                    accumulatedNode.startTokenIndex,
                    accumulatedNode.stopTokenIndex,
                    incomingModNode.sourceText
                )
            }
        }

        // 对插入操作按照优先级和位置排序
        // 优先级：IMPORT > SUB > OTHER
        // 同一优先级内按照 tokenIndex 升序排序（从前往后插入）
        val sortedOperations = insertOperations.sortedWith(compareBy<InsertOperation> { op ->
            when (op.nodeType) {
                NodeType.IMPORT -> 0
                NodeType.SUB -> 1
                NodeType.DEFAULT -> 2
            }
        }.thenBy { it.tokenIndex })

        for (op in sortedOperations) {
            if (op.previousSibling == null) {
                rewriter.insertBefore(op.tokenIndex, op.content)
            } else {
                rewriter.insertAfter(op.previousSibling.stopTokenIndex, "  ${op.content}")
            }
        }

        if (deletionRecords.isNotEmpty()) {
            val tokenStream = accumulatedResult.tokenStream
            // 所有插入操作实际落点的 token 下标，删除范围不能吞掉这些插入
            val insertIndices = sortedOperations.mapTo(HashSet()) { op ->
                op.previousSibling?.let { it.stopTokenIndex + 1 } ?: op.tokenIndex
            }
            for (record in deletionRecords) {
                if (record.userChoice != UserChoice.MERGE_MOD) continue
                val node = record.accumulatedNode
                val from = deletionStartIndex(tokenStream, node, insertIndices)
                rewriter.delete(from, node.stopTokenIndex)
            }
        }

        // 获取重写后的文本
        return rewriter.text
    }

    /**
     * 删除节点时连同它前面的换行缩进一起删掉，避免留下空行。
     * 只有前一个 token 是包含换行的空白，并且删除范围内（不含起点）没有插入操作时才扩展。
     */
    private fun deletionStartIndex(tokenStream: TokenStream, node: BaseTreeNode, insertIndices: Set<Int>): Int {
        val start = node.startTokenIndex
        val previous = start - 1
        if (previous < 0) return start
        val token = tokenStream.get(previous)
        if (token.channel != Token.HIDDEN_CHANNEL) return start
        val text = token.text ?: return start
        if (text.isBlank() && text.contains('\n') && (previous + 1..node.stopTokenIndex).none { it in insertIndices }) {
            return previous
        }
        return start
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
        return if (incomingModNode is ScrFunCallNode && originalNode is ScrFunCallNode) {
            // 函数调用节点，对比参数
            incomingModNode.arguments == originalNode.arguments
        } else {
            equalsTrimmed(incomingModNode.sourceText, originalNode.sourceText)
        }
    }

    private fun handleInsertion(
        accumulatedContainer: ScrContainerNode,
        incomingModNode: BaseTreeNode,
        previousSiblingInBase: BaseTreeNode?
    ) {
        // 根据节点签名确定节点类型
        val nodeType = when {
            incomingModNode.signature.startsWith(TechlandScrFileVisitor.IMPORT) -> NodeType.IMPORT
            incomingModNode.signature.startsWith(TechlandScrFileVisitor.SUB_FUN) -> NodeType.SUB
            else -> NodeType.DEFAULT
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

            NodeType.DEFAULT -> {
                // 其他节点直接插在容器的 '}' 之前
                newContent = "\n   ${incomingModNode.sourceText}"
                accumulatedContainer.stopTokenIndex
            }
        }

        insertOperations.add(InsertOperation(insertPos, newContent, previousSiblingInBase, nodeType))
    }

    /**
     * 找到import语句的插入位置：在所有现有import之后，或者在第一个非import节点之前
     */
    private fun findInsertPositionForImport(container: ScrContainerNode): Int {
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
    private fun findInsertPositionForSub(container: ScrContainerNode): Int {
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

    private fun parseContent(content: String): ParsedResult<ScrContainerNode> {
        return parseContentWithTemplate(
            content = content,
            lexerFactory = ::TechlandScriptLexer,
            parserFactory = ::TechlandScriptParser,
            parseTreeFactory = { it.file() },
            astBuilder = { tokenStream, parseTree ->
                val visitor = TechlandScrFileVisitor(tokenStream)
                visitor.visitFile(parseTree) as ScrContainerNode
            }
        )
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
