package ankol.mod.merger.merger.scr.node

import ankol.mod.merger.core.BaseTreeNode
import ankol.mod.merger.tools.logger
import org.antlr.v4.runtime.TokenStream

/**
 * SCR容器节点，标识语法树中含有子节点的节点
 *
 * 对应到SCR语法中即为包含其他语句的代码块，如函数体、条件语句体等
 * @author Ankol
 */
class ScrContainerScriptNode(
    signature: String,
    startTokenIndex: Int,
    stopTokenIndex: Int,
    line: Int,
    tokenStream: TokenStream
) : BaseTreeNode(signature, startTokenIndex, stopTokenIndex, line, tokenStream) {
    private val log = logger()

    /**
     * 子节点映射，key 是节点签名，value 是节点对象
     */
    val childrens: MutableMap<String, BaseTreeNode> = LinkedHashMap()

    /**
     * 添加子节点
     */
    fun addChild(node: BaseTreeNode) {
        if (childrens.contains(node.signature)) {
            log.warn("Repeatable siginature [${node.signature}] deteclted! Currently Node is ${node}.")
        }
        childrens[node.signature] = node
    }

    override fun printTree(indent: String) {
        super.printTree(indent)
        for ((_, child) in childrens) {
            child.printTree("$indent  ")
        }
    }
}
