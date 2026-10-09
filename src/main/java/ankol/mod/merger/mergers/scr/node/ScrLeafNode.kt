package ankol.mod.merger.mergers.scr.node

import ankol.mod.merger.constants.ScrNodeType
import org.antlr.v4.runtime.TokenStream

/**
 * 叶子节点
 */
class ScrLeafNode(
    signature: String,
    startTokenIndex: Int,
    stopTokenIndex: Int,
    line: Int,
    tokenStream: TokenStream,
    override val nodeType: ScrNodeType,
    override val name: String,
) : AbstractScrNode(signature, startTokenIndex, stopTokenIndex, line, tokenStream) {
    override val argCount: Int? get() = null
}
