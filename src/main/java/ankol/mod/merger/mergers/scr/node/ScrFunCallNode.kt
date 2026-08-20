package ankol.mod.merger.mergers.scr.node

import ankol.mod.merger.core.BaseTreeNode
import org.antlr.v4.runtime.TokenStream

/**
 * 函数调用节点
 */
class ScrFunCallNode(
    signature: String,
    startTokenIndex: Int,
    stopTokenIndex: Int,
    line: Int,
    tokenStream: TokenStream,
    val functionName: String,
    val arguments: List<String>
) : BaseTreeNode(signature, startTokenIndex, stopTokenIndex, line, tokenStream)
