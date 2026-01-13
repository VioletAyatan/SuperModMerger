package ankol.mod.merger.merger.json.node

import ankol.mod.merger.core.BaseTreeNode
import org.antlr.v4.runtime.TokenStream

/**
 * JSON数组节点
 */
class JsonArrayNode(
    signature: String,
    startTokenIndex: Int,
    stopTokenIndex: Int,
    lineNumber: Int,
    tokenStream: TokenStream
) : BaseTreeNode(signature, startTokenIndex, stopTokenIndex, lineNumber, tokenStream) {

    private val children: MutableList<BaseTreeNode> = mutableListOf()

    /**
     * 添加数组元素
     */
    fun addElement(node: BaseTreeNode) {
        children.add(node)
    }

    /**
     * 获取所有数组元素
     */
    @Suppress("unused")
    fun getElements(): List<BaseTreeNode> = children

    override fun printTree(indent: String) {
        super.printTree(indent)
        for (child in children) {
            child.printTree("$indent  ")
        }
    }
}
