package ankol.mod.merger.core

import org.antlr.v4.runtime.TokenStream
import org.antlr.v4.runtime.misc.Interval

/**
 * Base tree node
 * @param signature Signature of the current node (should be unique at the same tree level, for multi-file comparison)
 * @param startTokenIndex Start TOKEN index of the current node
 * @param stopTokenIndex End TOKEN index of the current node
 * @param lineNumber Current line number
 * @param tokenStream Token stream reference
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
        return@lazy tokenStream.tokenSource.inputStream.getText(Interval(startIndex, stopIndex))
    }

    val sourceText: String
        get() {
            return _sourceText
        }

    /**
     * Print tree node
     */
    open fun printTree(indent: String = "") {
        println("${indent}Node: $signature")
    }
}
