package ankol.mod.merger.merger.json.node

import ankol.mod.merger.core.BaseTreeNode
import org.antlr.v4.runtime.TokenStream

/**
 * JSON容器节点
 */
class JsonContainerNode(
    signature: String,
    startTokenIndex: Int,
    stopTokenIndex: Int,
    lineNumber: Int,
    tokenStream: TokenStream,
    var childerns: MutableMap<String, BaseTreeNode> = mutableMapOf()
) : BaseTreeNode(signature, startTokenIndex, stopTokenIndex, lineNumber, tokenStream) {
    /**
     * 添加子节点
     */
    fun addChildern(node: BaseTreeNode) {
        childerns[node.signature] = node
    }

    override fun printTree(indent: String) {
        super.printTree(indent)
        for (child in childerns.values) {
            child.printTree("$indent  ")
        }
    }
}