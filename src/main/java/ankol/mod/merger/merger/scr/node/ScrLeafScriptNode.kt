package ankol.mod.merger.merger.scr.node

import ankol.mod.merger.core.BaseTreeNode
import org.antlr.v4.runtime.CommonTokenStream

/**
 * 叶子节点
 */
class ScrLeafScriptNode(
    signature: String,
    startTokenIndex: Int,
    stopTokenIndex: Int,
    line: Int,
    tokenStream: CommonTokenStream
) : BaseTreeNode(signature, startTokenIndex, stopTokenIndex, line, tokenStream)
