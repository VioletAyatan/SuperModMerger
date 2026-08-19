package ankol.mod.merger.mergers.xml

import ankol.mod.merger.antlr.xml.TechlandXMLLexer
import ankol.mod.merger.antlr.xml.TechlandXMLParser
import ankol.mod.merger.constants.UserChoice
import ankol.mod.merger.core.filetrees.AbstractFileTree
import ankol.mod.merger.domain.MergeResult
import ankol.mod.merger.domain.MergeContext
import ankol.mod.merger.domain.ParsedResult
import ankol.mod.merger.exception.BusinessException
import ankol.mod.merger.mergers.AbstractFileMerger
import ankol.mod.merger.mergers.ConflictRecord
import ankol.mod.merger.mergers.xml.node.XmlContainerNode
import ankol.mod.merger.mergers.xml.node.XmlLeafNode
import ankol.mod.merger.mergers.xml.node.XmlNode
import ankol.mod.merger.tools.logger
import org.antlr.v4.runtime.TokenStreamRewriter

/**
 * XML文件合并器
 *
 * @author Ankol
 */
class TechlandXmlFileMerger(context: MergeContext) : AbstractFileMerger(context) {
    private val log = logger()

    /**
     * 冲突项列表
     */
    private val conflicts = arrayListOf<ConflictRecord>()

    /**
     * 新增节点记录
     */
    private data class XmlNewNode(
        val parentContainer: XmlContainerNode,
        val previousSibling: XmlNode?,
        val newNode: XmlNode
    )

    /**
     * 新增节点列表
     */
    private val newNodes = arrayListOf<XmlNewNode>()

    /**
     * 原始基准MOD对应文件的语法树
     */
    private var vanillaRootNode: XmlContainerNode? = null

    override fun merge(accumulatedFile: AbstractFileTree, incomingModFile: AbstractFileTree): MergeResult {
        try {
            val parsedResult = context.baseModManager.parseForm(accumulatedFile.fileEntryName) { parseContent(it) }
            // 解析原始基准MOD文件（如果存在）
            if (parsedResult != null) {
                vanillaRootNode = parsedResult.astNode
            }
            // 解析base和mod文件
            val baseResult = parseContent(accumulatedFile.getContent())
            val modResult = parseContent(incomingModFile.getContent())
            val baseRoot = baseResult.astNode
            val modRoot = modResult.astNode

            deepCompare(vanillaRootNode, baseRoot, modRoot)

            // 第一个mod与原版文件的对比
            if (context.isFirstMerge && !conflicts.isEmpty()) {
                for (record in conflicts) {
                    record.userChoice = UserChoice.MERGE_MOD
                }
            } else if (!conflicts.isEmpty()) {
                context.conflictResolver.resolveConflict(conflicts)
            }

            return MergeResult(getMergedContent(baseResult), context.mergedHistory)
        } catch (e: Exception) {
            log.error("Error during XML file merge: ${accumulatedFile.fileName} Reason: ${e.message}", e)
            throw BusinessException("文件${accumulatedFile.fileName}合并失败")
        } finally {
            // 清理状态，准备下一个文件合并
            conflicts.clear()
            newNodes.clear()
            vanillaRootNode = null
        }
    }

    /**
     * 递归对比树节点
     */
    private fun deepCompare(
        vanillaContainer: XmlContainerNode?,
        accumulatedContainer: XmlContainerNode,
        incomingModContainer: XmlContainerNode
    ) {
        // 遍历Mod的所有子节点
        var previousSiblingInBase: XmlNode? = null // 追踪前一个兄弟节点
        for ((signature, modNode) in incomingModContainer.childrens) {
            try {
                var vanillaNode: XmlNode? = null

                if (vanillaContainer != null) {
                    vanillaNode = vanillaContainer.childrens[signature]
                }

                val accumulatedNode = accumulatedContainer.childrens[signature]

                if (accumulatedNode == null) {
                    //新增节点，记录插入操作
                    newNodes.add(XmlNewNode(accumulatedContainer, previousSiblingInBase, modNode))
                } else {
                    // 更新前一个兄弟节点
                    previousSiblingInBase = accumulatedNode
                    //容器节点，继续递归对比
                    if (accumulatedNode is XmlContainerNode && modNode is XmlContainerNode) {
                        deepCompare(vanillaNode as XmlContainerNode?, accumulatedNode, modNode)
                    }
                    //叶子节点，对比属性
                    else if (accumulatedNode is XmlLeafNode && modNode is XmlLeafNode) {
                        val baseAttr = accumulatedNode.attributes
                        val modAttr = modNode.attributes
                        if (baseAttr != modAttr) {
                            // 属性不同，检查modNode与原版是否相同
                            if (!isNodeSameAsOriginalBaseMod(vanillaNode, modNode)) {
                                if (isNodeSameAsOriginalBaseMod(vanillaNode, accumulatedNode)) {
                                    context.mergedHistory.markSignture("${incomingModContainer.signature}-${signature}", context.mergeModName)
                                    //base节点与原版一致，自动合并
                                    val record = ConflictRecord(
                                        context.currentFileName,
                                        context.accumulatedModName,
                                        context.mergeModName,
                                        signature,
                                        accumulatedNode,
                                        modNode
                                    )
                                    record.userChoice = UserChoice.MERGE_MOD
                                    conflicts.add(record)
                                } else {
                                    val modName = context.mergedHistory.getModNameFromSignature("${incomingModContainer.signature}-${signature}")
                                    //真正的冲突，记录变更
                                    conflicts.add(
                                        ConflictRecord(
                                            context.currentFileName,
                                            modName ?: context.accumulatedModName,
                                            context.mergeModName,
                                            signature,
                                            accumulatedNode,
                                            modNode
                                        )
                                    )
                                }
                            }
                        }
                    } else {
                        // 一个是容器，一个不是容器，这种情况下认为是冲突
                        if (!isNodeSameAsOriginalBaseMod(vanillaNode, modNode)) {
                            conflicts.add(
                                ConflictRecord(
                                    context.currentFileName,
                                    context.accumulatedModName,
                                    context.mergeModName,
                                    signature,
                                    accumulatedNode,
                                    modNode
                                )
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                log.error("Error in processing XML node with signature: '${signature}'", e)
            }
        }
    }

    /**
     * 获取合并后的内容
     */
    private fun getMergedContent(baseResult: ParsedResult<*>): String {
        val rewriter = TokenStreamRewriter(baseResult.tokenStream)
        // 处理冲突节点的替换
        for (record in conflicts) {
            if (record.userChoice == UserChoice.MERGE_MOD) {
                // 普通修改冲突：用户选择了 Mod
                val baseNode = record.baseNode
                val modNode = record.modNode
                rewriter.replace(baseNode.startTokenIndex, baseNode.stopTokenIndex, modNode.sourceText)
            }
        }

        // 处理新增节点的插入
        for (record in newNodes) {
            val previousSibling = record.previousSibling
            val newNode = record.newNode
            //有兄弟节点，插在兄弟节点后面
            if (previousSibling != null) {
                rewriter.insertBefore(previousSibling.stopTokenIndex + 1, "\n${newNode.sourceText}")
            } else {
                // xml容器的结束标签是>，然而实际需要插入在<之前，这里需要继续搜索前面的token索引
                //todo 这里的逻辑后面看看还有没有更好的解决方法
                var stopTokenIndex = record.parentContainer.stopTokenIndex
                while (record.parentContainer.tokenStream.get(stopTokenIndex).type != TechlandXMLLexer.OPEN) {
                    stopTokenIndex--
                }
                rewriter.insertBefore(stopTokenIndex, "${newNode.sourceText}\n")
            }
        }
        return rewriter.text
    }

    /**
     * 检查节点是否与原始基准MOD中的对应节点内容相同
     */
    private fun isNodeSameAsOriginalBaseMod(originalNode: XmlNode?, modNode: XmlNode): Boolean {
        // 如果没有原始基准MOD，则认为不相同
        if (vanillaRootNode == null) {
            return false
        }
        if (originalNode == null) {
            // 原始基准MOD中不存在这个节点
            return false
        }
        return modNode.attributes == originalNode.attributes
    }

    /**
     * 解析字符串内容为ParseResult
     */
    private fun parseContent(content: String): ParsedResult<XmlContainerNode> {
        return parseContentWithTemplate(
            content = content,
            lexerFactory = ::TechlandXMLLexer,
            parserFactory = ::TechlandXMLParser,
            parseTreeFactory = { it.document() },
            astBuilder = { tokenStream, parseTree ->
                val visitor = TechlandXmlFileVisitor(tokenStream)
                visitor.visitDocument(parseTree) as XmlContainerNode
            }
        )
    }
}
