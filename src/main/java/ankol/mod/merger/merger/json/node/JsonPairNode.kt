package ankol.mod.merger.merger.json.node

import ankol.mod.merger.core.BaseTreeNode
import org.antlr.v4.runtime.TokenStream

/**
 * JSON键值对节点
 */
class JsonPairNode(
    signature: String,
    startTokenIndex: Int,
    stopTokenIndex: Int,
    lineNumber: Int,
    tokenStream: TokenStream
) : BaseTreeNode(signature, startTokenIndex, stopTokenIndex, lineNumber, tokenStream) {

    var value: BaseTreeNode? = null
}

