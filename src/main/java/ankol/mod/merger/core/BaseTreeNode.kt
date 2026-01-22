package ankol.mod.merger.core

import org.antlr.v4.runtime.TokenStream
import org.antlr.v4.runtime.misc.Interval

/**
 * 基础树节点
 * @param signature 当前节点签名（确保在同一树层级下保持唯一，方便进行多文件对比）
 * @param startTokenIndex 当前节点起始TOKEN索引
 * @param stopTokenIndex 当前节点结束TOKEN索引
 * @param lineNumber 当前行号
 * @param tokenStream Token流引用
 * @author Ankol
 */
abstract class BaseTreeNode(
    var signature: String,
    val startTokenIndex: Int,
    val stopTokenIndex: Int,
    val lineNumber: Int,
    @field:Transient
    val tokenStream: TokenStream
) {
    private val _sourceText: String by lazy {
        val startIndex = tokenStream.get(startTokenIndex).startIndex
        val stopIndex = tokenStream.get(stopTokenIndex).stopIndex
//        calcCount++
        return@lazy tokenStream.tokenSource.inputStream.getText(Interval(startIndex, stopIndex))
    }

    val sourceText: String
        get() {
            return _sourceText
        }

    /**
     * 打印树节点
     */
    open fun printTree(indent: String = "") {
        println("${indent}Node: $signature")
    }
}
