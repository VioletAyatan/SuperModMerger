package ankol.mod.merger.merger.json.node

import ankol.mod.merger.core.BaseTreeNode
import ankol.mod.merger.tools.logger
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
    var childrens: MutableMap<String, BaseTreeNode> = mutableMapOf()
) : BaseTreeNode(signature, startTokenIndex, stopTokenIndex, lineNumber, tokenStream) {
    private val log = logger()

    /**
     * 添加子节点
     */
    fun addChildern(node: BaseTreeNode) {
        if (childrens.contains(node.signature)) {
            log.warn("Repeatable siginature [${node.signature}] deteclted! Currently Node is ${node}.")
        }
        childrens[node.signature] = node
    }

    override fun printTree(indent: String) {
        super.printTree(indent)
        for (child in childrens.values) {
            child.printTree("$indent  ")
        }
    }
}