package ankol.mod.merger.merger.xml

import ankol.mod.merger.antlr.xml.TechlandXMLLexer
import ankol.mod.merger.antlr.xml.TechlandXMLParser
import ankol.mod.merger.core.AbstractFileMerger
import ankol.mod.merger.core.ConflictResolver.resolveConflict
import ankol.mod.merger.core.MergerContext
import ankol.mod.merger.core.ParsedResult
import ankol.mod.merger.core.filetrees.AbstractFileTree
import ankol.mod.merger.exception.BusinessException
import ankol.mod.merger.merger.ConflictRecord
import ankol.mod.merger.merger.MergeResult
import ankol.mod.merger.merger.xml.node.XmlContainerNode
import ankol.mod.merger.merger.xml.node.XmlNode
import ankol.mod.merger.tools.logger
import org.antlr.v4.runtime.CharStream
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import org.antlr.v4.runtime.TokenStreamRewriter

/**
 * XML文件合并器
 * 
 * @author Ankol
 */
class TechlandXmlFileMerger(context: MergerContext) : AbstractFileMerger(context) {
    private val log = logger()

    /**
     * 冲突项列表
     */
    private val conflicts = ArrayList<ConflictRecord>()

    /**
     * 新增节点记录
     */
    @JvmRecord
    private data class NewNodeRecord(
        val parentContainer: XmlContainerNode,
        val previousSibling: XmlNode?,
        val newNode: XmlNode
    )

    /**
     * 新增节点列表
     */
    private val newNodes = ArrayList<NewNodeRecord>()

    /**
     * 原始基准MOD对应文件的语法树
     */
    private var originalBaseModRoot: XmlContainerNode? = null

    override fun merge(file1: AbstractFileTree, file2: AbstractFileTree): MergeResult {
        try {
            val parsedResult = context.baseModManager
                .parseForm(file1.getFileEntryName()) { parseContent(it) }
            // 解析原始基准MOD文件（如果存在）
            if (parsedResult != null) {
                originalBaseModRoot = parsedResult.astNode
            }
            // 解析base和mod文件
            val baseResult = parseFile(file1)
            val modResult = parseFile(file2)
            val baseRoot = baseResult.astNode
            val modRoot = modResult.astNode
            // 递归对比，找到冲突项
            reduceCompare(originalBaseModRoot, baseRoot, modRoot)
            // 第一个mod与原版文件的对比，直接通过，不提示冲突
            if (context.isFirstModMergeWithBaseMod && !conflicts.isEmpty()) {
                for (record in conflicts) {
                    record.userChoice = 2 // 自动选择第一个mod的版本
                }
            } else if (!conflicts.isEmpty()) {
                // 正常情况下，提示用户解决冲突
                resolveConflict(conflicts)
            }
            return MergeResult(getMergedContent(baseResult), !conflicts.isEmpty())
        } catch (e: Exception) {
            log.error("Error during XML file merge: ${file1.getFileName()} Reason: ${e.message}", e)
            throw BusinessException("文件" + file1.getFileName() + "合并失败")
        } finally {
            // 清理状态，准备下一个文件合并
            conflicts.clear()
            newNodes.clear()
            originalBaseModRoot = null
        }
    }

    /**
     * 递归对比树节点
     */
    private fun reduceCompare(
        originalContainer: XmlContainerNode?,
        baseContainer: XmlContainerNode,
        modContainer: XmlContainerNode
    ) {
        // 遍历Mod的所有子节点
        var previousSiblingInBase: XmlNode? = null // 追踪前一个兄弟节点
        for ((signature, modNode) in modContainer.children) {
            try {
                var originalNode: XmlNode? = null

                if (originalContainer != null) {
                    originalNode = originalContainer.children[signature]
                }

                val baseNode = baseContainer.children[signature]

                if (baseNode == null) {
                    // Base中不存在这个节点 - 新增节点，需要添加到合并结果中
                    // 记录前一个兄弟节点，用于确定插入位置
                    newNodes.add(NewNodeRecord(baseContainer, previousSiblingInBase, modNode))
                } else {
                    // 更新前一个兄弟节点
                    previousSiblingInBase = baseNode
                    //容器节点，继续递归对比
                    if (baseNode is XmlContainerNode && modNode is XmlContainerNode) {
                        reduceCompare(originalNode as XmlContainerNode?, baseNode, modNode)
                    }
                    //叶子节点，对比属性
                    else if (baseNode !is XmlContainerNode && modNode !is XmlContainerNode) {
                        // 使用规范化文本进行对比，避免空格、换行等格式差异
                        val baseAttr = baseNode.getAttributes()
                        val modAttr = modNode.getAttributes()
                        if (baseAttr != modAttr) {
                            // 不相同，检查是否跟基准mod的一样，不一样视为冲突
                            if (!isNodeSameAsOriginalBaseMod(originalNode, modNode)) {
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
                    } else {
                        // 一个是容器，一个不是容器，这种情况下认为是冲突
                        if (!isNodeSameAsOriginalBaseMod(originalNode, modNode)) {
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
            } catch (e: Exception) {
                System.err.println("Error in processing XML node with signature: '${signature}'")
            }
        }
    }

    /**
     * 获取合并后的内容
     */
    private fun getMergedContent(baseResult: ParsedResult<*>): String? {
        val rewriter = TokenStreamRewriter(baseResult.tokenStream)
        // 处理冲突节点的替换
        for (record in conflicts) {
            if (record.userChoice == 2) { // 用户选择了 Mod
                val baseNode = record.baseNode
                val modNode = record.modNode
                rewriter.replace(
                    baseNode.startTokenIndex,
                    baseNode.stopTokenIndex,
                    modNode.sourceText
                )
            }
        }

        // 处理新增节点的插入
        for (record in newNodes) {
            val previousSibling = record.previousSibling
            val newNode = record.newNode

            val insertPosition = if (previousSibling != null) {
                // 如果有前一个兄弟节点，在其后面插入
                // 使用stopTokenIndex + 1
                previousSibling.stopTokenIndex + 1
            } else {
                // 如果没有前一个兄弟节点（即这是第一个子节点），在父容器的结束标签前面插入
                record.parentContainer.stopTokenIndex
            }
            rewriter.insertBefore(insertPosition, "\n" + newNode.sourceText)
        }
        return rewriter.text
    }

    /**
     * 检查节点是否与原始基准MOD中的对应节点内容相同
     */
    private fun isNodeSameAsOriginalBaseMod(originalNode: XmlNode?, modNode: XmlNode): Boolean {
        // 如果没有原始基准MOD，则认为不相同
        if (originalBaseModRoot == null) {
            return false
        }
        if (originalNode == null) {
            // 原始基准MOD中不存在这个节点
            return false
        }
        return modNode.getAttributes() == originalNode.getAttributes()
    }

    /**
     * 将XML文件解析成语法树
     */
    private fun parseFile(filePath: AbstractFileTree): ParsedResult<XmlContainerNode> {
        return parseContent(filePath.getContent())
    }

    /**
     * 解析字符串内容为ParseResult
     */
    private fun parseContent(content: String): ParsedResult<XmlContainerNode> {
        val input: CharStream = CharStreams.fromString(content)
        val lexer = TechlandXMLLexer(input)
        val tokens = CommonTokenStream(lexer)
        val parser = TechlandXMLParser(tokens)
        val visitor = TechlandXmlFileVisitor(tokens)
        val root = visitor.visitDocument(parser.document())
        val containerRoot = root as XmlContainerNode?
        return ParsedResult(containerRoot, tokens)
    }
}