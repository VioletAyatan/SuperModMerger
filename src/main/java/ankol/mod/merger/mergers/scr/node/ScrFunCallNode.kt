package ankol.mod.merger.mergers.scr.node

import ankol.mod.merger.constants.ScrNodeType
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
    val arguments: List<String>,
    override val nodeType: ScrNodeType = ScrNodeType.FUN_CALL,
) : AbstractScrNode(signature, startTokenIndex, stopTokenIndex, line, tokenStream) {
    override val name: String get() = functionName
    override val argCount: Int get() = arguments.size
}
