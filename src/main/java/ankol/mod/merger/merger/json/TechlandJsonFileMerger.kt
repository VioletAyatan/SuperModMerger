package ankol.mod.merger.merger.json

import ankol.mod.merger.antlr.json.JSONLexer
import ankol.mod.merger.antlr.json.JSONParser
import ankol.mod.merger.constants.UserChoice
import ankol.mod.merger.core.AbstractFileMerger
import ankol.mod.merger.core.BaseTreeNode
import ankol.mod.merger.core.ConflictResolver.resolveConflict
import ankol.mod.merger.core.MergerContext
import ankol.mod.merger.core.ParsedResult
import ankol.mod.merger.core.filetrees.AbstractFileTree
import ankol.mod.merger.exception.BusinessException
import ankol.mod.merger.merger.ConflictRecord
import ankol.mod.merger.merger.MergeResult
import ankol.mod.merger.merger.json.node.JsonArrayNode
import ankol.mod.merger.merger.json.node.JsonContainerNode
import ankol.mod.merger.merger.json.node.JsonPairNode
import ankol.mod.merger.tools.logger
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import org.antlr.v4.runtime.TokenStreamRewriter

/**
 * JSON文件合并器
 *
 * @author Ankol
 */
class TechlandJsonFileMerger(context: MergerContext) : AbstractFileMerger(context) {
    private val log = logger()

    /**
     * 冲突项列表
     */
    private val conflicts = ArrayList<ConflictRecord>()

    /**
     * 新增节点记录
     */
    private data class NewNodeRecord(
        val parentContainer: BaseTreeNode,
        val previousSibling: BaseTreeNode?,
        val newNode: BaseTreeNode
    )

    /**
     * 新增节点列表
     */
    private val newNodes = ArrayList<NewNodeRecord>()

    /**
     * 原始基准MOD对应文件的语法树
     */
    private var originalBaseModRoot: BaseTreeNode? = null

    override fun merge(file1: AbstractFileTree, file2: AbstractFileTree): MergeResult {
        try {
            //解析基准文件
            val parsedResult = context.baseModManager.parseForm(file1.fileEntryName) { parseContent(it) }
            if (parsedResult != null) {
                originalBaseModRoot = parsedResult.astNode
            }
            val baseResult = parseFile(file1)
            val modResult = parseFile(file2)
            val baseRoot = baseResult.astNode!!
            val modRoot = modResult.astNode!!
            //深度对比
            reduceCompare(originalBaseModRoot, baseRoot, modRoot)
            //冲突解决
            if (context.isFirstModMergeWithBaseMod && conflicts.isNotEmpty()) {
                for (record in conflicts) {
                    record.userChoice = UserChoice.MERGE_MOD
                }
            } else if (conflicts.isNotEmpty()) {
                resolveConflict(conflicts)
            }
            return MergeResult(getMergedContent(baseResult), conflicts.isNotEmpty())
        } catch (e: Exception) {
            log.error("Error during JSON file merge: ${file1.fileName} Reason: ${e.message}", e)
            throw BusinessException("文件${file1.fileName}合并失败")
        } finally {
            // 清理状态，准备下一个文件合并
            conflicts.clear()
            newNodes.clear()
            originalBaseModRoot = null
        }
    }

    private fun reduceCompare(
        originalNode: BaseTreeNode?,
        baseNode: BaseTreeNode,
        modNode: BaseTreeNode
    ) {
        when (baseNode) {
            //对象节点
            is JsonContainerNode if modNode is JsonContainerNode -> {
                compareContainers(originalNode as? JsonContainerNode, baseNode, modNode)
            }
            //数组节点
            is JsonArrayNode if modNode is JsonArrayNode -> {
                compareArrays(originalNode as? JsonArrayNode, baseNode, modNode)
            }
            //叶子节点
            else -> {
                compareLeafNodes(originalNode, baseNode, modNode)
            }
        }
    }

    /**
     * 比较容器节点（JSON对象）
     */
    private fun compareContainers(
        originalContainer: JsonContainerNode?,
        baseContainer: JsonContainerNode,
        modContainer: JsonContainerNode
    ) {
        var previousSiblingInBase: BaseTreeNode? = null

        for ((signature, modChild) in modContainer.childerns) {
            val originalChild = originalContainer?.childerns?.get(signature)
            val baseChild = baseContainer.childerns[signature]

            //不存在，新增
            if (baseChild == null) {
                newNodes.add(NewNodeRecord(baseContainer, previousSiblingInBase, modChild))
            } else {
                previousSiblingInBase = baseChild

                if (modChild is JsonPairNode && baseChild is JsonPairNode) {
                    val modValue = modChild.value
                    val baseValue = baseChild.value

                    if (modValue != null && baseValue != null) {
                        when (baseValue) {
                            // 对象节点对比
                            is JsonContainerNode if modValue is JsonContainerNode -> {
                                reduceCompare((originalChild as? JsonPairNode)?.value, baseValue, modValue)
                            }
                            // 数组节点对比
                            is JsonArrayNode if modValue is JsonArrayNode -> {
                                reduceCompare((originalChild as? JsonPairNode)?.value, baseValue, modValue)
                            }
                            // 叶子节点，直接比较文本
                            else -> {
                                if (baseValue.sourceText != modValue.sourceText) {
                                    // 检查是否与原始基准MOD相同
                                    val originalValue = (originalChild as? JsonPairNode)?.value
                                    if (originalValue == null || originalValue.sourceText != modValue.sourceText) {
                                        // 发生冲突，记录完整的PairNode（包含key和value）
                                        conflicts.add(
                                            ConflictRecord(
                                                context.fileName,
                                                context.mod1Name,
                                                context.mod2Name,
                                                baseChild.signature,
                                                baseChild,  // 完整的PairNode
                                                modChild    // 完整的PairNode
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }
                } else {
                    reduceCompare(originalChild, baseChild, modChild)
                }
            }
        }
    }

    /**
     * 比较数组节点
     */
    private fun compareArrays(
        originalArray: JsonArrayNode?,
        baseArray: JsonArrayNode,
        modArray: JsonArrayNode
    ) {
        val baseElements = baseArray.getElements()
        val modElements = modArray.getElements()

        // 简单策略：如果数组长度或内容不同，视为冲突
        if (baseElements.size != modElements.size) {
            if (!isNodeSameAsOriginalBaseMod(originalArray, modArray)) {
                conflicts.add(
                    ConflictRecord(
                        context.fileName,
                        context.mod1Name,
                        context.mod2Name,
                        baseArray.signature,
                        baseArray,
                        modArray
                    )
                )
            }
        } else {
            // 逐个比较数组元素
            for (i in baseElements.indices) {
                val originalElement = originalArray?.getElements()?.getOrNull(i)
                reduceCompare(originalElement, baseElements[i], modElements[i])
            }
        }
    }

    /**
     * 比较叶子节点
     */
    private fun compareLeafNodes(
        originalNode: BaseTreeNode?,
        baseNode: BaseTreeNode,
        modNode: BaseTreeNode
    ) {
        val baseText = baseNode.sourceText
        val modText = modNode.sourceText

        if (baseText != modText) {
            // 不相同，检查是否跟基准mod的一样，不一样视为冲突
            if (!isNodeSameAsOriginalBaseMod(originalNode, modNode)) {
                conflicts.add(
                    ConflictRecord(
                        context.fileName,
                        context.mod1Name,
                        context.mod2Name,
                        baseNode.signature,
                        baseNode,
                        modNode
                    )
                )
            }
        }
    }

    /**
     * 检查节点是否与原始基准MOD相同
     */
    private fun isNodeSameAsOriginalBaseMod(originalNode: BaseTreeNode?, modNode: BaseTreeNode): Boolean {
        if (originalNode == null) {
            return false
        }
        return originalNode.sourceText == modNode.sourceText
    }

    /**
     * 获取合并后的内容
     */
    private fun getMergedContent(baseResult: ParsedResult<BaseTreeNode>): String {
        val rewriter = TokenStreamRewriter(baseResult.tokenStream)

        // 处理冲突节点
        for (conflictRecord in conflicts) {
            if (conflictRecord.userChoice == UserChoice.MERGE_MOD) {
                val baseNode = conflictRecord.baseNode
                val modNode = conflictRecord.modNode
                // 替换为mod节点的内容
                rewriter.replace(
                    baseNode.startTokenIndex,
                    baseNode.stopTokenIndex,
                    modNode.sourceText
                )
            }
        }

        // 处理新增节点
        for (record in newNodes) {
            val parentContainer = record.parentContainer
            val newNode = record.newNode
            val previousSibling = record.previousSibling

            if (previousSibling != null) {
                // 在前一个兄弟节点后插入
                val insertText = buildInsertText(newNode, false)
                rewriter.insertAfter(previousSibling.stopTokenIndex, insertText)
            } else {
                // 作为第一个子节点插入
                val insertText = buildInsertText(newNode, true)
                when (parentContainer) {
                    is JsonContainerNode -> {
                        // 在对象的左花括号后插入
                        rewriter.insertAfter(parentContainer.startTokenIndex, insertText)
                    }
                    is JsonArrayNode -> {
                        // 在数组的左方括号后插入
                        rewriter.insertAfter(parentContainer.startTokenIndex, insertText)
                    }
                }
            }
        }

        return rewriter.text
    }

    /**
     * 构建插入文本
     */
    private fun buildInsertText(
        newNode: BaseTreeNode,
        isFirstChild: Boolean
    ): String {
        val nodeText = newNode.sourceText

        return when {
            isFirstChild -> {
                // 第一个子节点，添加换行和缩进
                "\n    $nodeText"
            }
            else -> {
                // 后续子节点，添加逗号、换行和缩进
                ",\n    $nodeText"
            }
        }
    }

    private fun parseFile(fileTree: AbstractFileTree): ParsedResult<BaseTreeNode> {
        return parseContent(fileTree.getContent())
    }

    private fun parseContent(content: String): ParsedResult<BaseTreeNode> {
        val charStream = CharStreams.fromString(content)
        val lexer = JSONLexer(charStream)
        val tokenStream = CommonTokenStream(lexer)
        val parser = JSONParser(tokenStream)
        val jsonContext = parser.json()

        // 使用Visitor转换为节点树
        val visitor = TechlandJsonFileVisitor(tokenStream)
        val astNode = visitor.visit(jsonContext)

        return ParsedResult(astNode, tokenStream)
    }
}

