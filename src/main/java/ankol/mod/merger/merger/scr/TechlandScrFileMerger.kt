package ankol.mod.merger.merger.scr

import ankol.mod.merger.antlr.scr.TechlandScriptLexer
import ankol.mod.merger.antlr.scr.TechlandScriptParser
import ankol.mod.merger.core.*
import ankol.mod.merger.core.filetrees.AbstractFileTree
import ankol.mod.merger.exception.BusinessException
import ankol.mod.merger.merger.ConflictRecord
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
    private data class InsertOperation(val tokenIndex: Int, val content: String)

    private val insertOperations = ArrayList<InsertOperation>()

    /**
     * 基准MOD（data0.pak）对应文件的语法树，用于三方对比
     */
    private var originalBaseModRoot: ScrContainerScriptNode? = null

    override fun merge(file1: AbstractFileTree, file2: AbstractFileTree): MergeResult {
        try {
            val parsedResult = context.baseModManager
                .parseForm(file1.getFileEntryName()) { parseContent(it) }
            // 解析基准MOD文件（如果存在）
            if (parsedResult != null) {
                originalBaseModRoot = parsedResult.astNode
            }
            // 解析base和mod文件，保留TokenStream
            val baseResult = parseFile(file1)
            val modResult = parseFile(file2)
            val baseRoot: ScrContainerScriptNode = baseResult.astNode!!
            val modRoot: ScrContainerScriptNode = modResult.astNode!!
            // 递归对比，找到冲突项
            reduceCompare(originalBaseModRoot, baseRoot, modRoot)
            //第一个mod与原版文件的对比，直接使用MOD修改的版本，不提示冲突
            if (context.isFirstModMergeWithBaseMod && !conflicts.isEmpty()) {
                for (record in conflicts) {
                    record.userChoice = 2 // 自动选择第一个mod的版本
                }
            } else if (!conflicts.isEmpty()) {
                // 正常情况下，提示用户解决冲突
                ConflictResolver.resolveConflict(conflicts)
            }
            return MergeResult(getMergedContent(baseResult), !conflicts.isEmpty())
        } catch (e: Exception) {
            log.error("Error during SCR file merge: ${file1.getFileName()} Reason: ${e.message}", e)
            throw BusinessException("文件" + file1.getFileName() + "合并失败")
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
                        //先检查当前待合并节点内容是否相同
                        if (baseNode.arguments != modNode.arguments) {
                            //两者内容不同，检查mod节点内容与原版是否相同
                            if (!isNodeSameAsOriginalNode(originalNode, modNode)) {
                                //当前节点与原版一致，说明此处节点未变动，使用mod的内容（开启了智能合并的情况下）
                                if (isNodeSameAsOriginalNode(originalNode, baseNode)
                                    && GlobalMergingStrategy.autoMergingCodeLine
                                ) {
                                    val record = ConflictRecord(
                                        context.fileName,
                                        context.mod1Name,
                                        context.mod2Name,
                                        signature,
                                        baseNode,
                                        modNode
                                    )
                                    record.userChoice = 2
                                    conflicts.add(record)
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
                    } else {
                        val baseText = baseNode.sourceText
                        val modText = modNode.sourceText
                        //内容不一致
                        if (!equalsTrimmed(baseText, modText)) {
                            // 检查modNode是否与原始基准MOD相同
                            if (!isNodeSameAsOriginalNode(originalNode, modNode)) {
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
            } catch (e: Exception) {
                log.error("Error in processing scr node with signature: '${signature}'", e)
            }
        }
    }

    private fun getMergedContent(baseResult: ParsedResult<ScrContainerScriptNode>): String? {
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
        for (op in insertOperations) {
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
        if (modNode is ScrFunCallScriptNode && originalNode is ScrFunCallScriptNode) {
            // 函数调用节点，对比参数
            return modNode.arguments == originalNode.arguments
        } else {
            return equalsTrimmed(modNode.sourceText, originalNode.sourceText)
        }
    }

    private fun handleInsertion(baseContainer: ScrContainerScriptNode, modNode: BaseTreeNode) {
        // 插入位置：Base 容器的 '}' 之前
        val insertPos = baseContainer.stopTokenIndex
        val newContent = "\n    " + modNode.sourceText
        insertOperations.add(InsertOperation(insertPos, newContent))
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
