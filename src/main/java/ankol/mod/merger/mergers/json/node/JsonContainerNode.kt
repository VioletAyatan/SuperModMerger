package ankol.mod.merger.mergers.json.node

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
    line: Int,
    tokenStream: TokenStream,
    var childrens: MutableMap<String, BaseTreeNode> = mutableMapOf()
) : BaseTreeNode(signature, startTokenIndex, stopTokenIndex, line, tokenStream) {
    private val log = logger()

    /**
     * 添加子节点
     */
    fun addChildern(node: BaseTreeNode) {
        if (childrens.contains(node.signature)) {
            log.debug("Repeatable siginature detected: [${node.signature}] Line: ${node.line} SourceText: ${node.sourceText}.")
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
