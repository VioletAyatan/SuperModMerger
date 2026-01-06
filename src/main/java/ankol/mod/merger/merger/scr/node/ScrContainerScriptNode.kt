package ankol.mod.merger.merger.scr.node

import ankol.mod.merger.core.BaseTreeNode
import org.antlr.v4.runtime.CommonTokenStream

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
    tokenStream: CommonTokenStream
) : BaseTreeNode(signature, startTokenIndex, stopTokenIndex, line, tokenStream) {
    /**
     * 子节点映射，key 是节点签名，value 是节点对象
     */
    val childrens = LinkedHashMap<String, BaseTreeNode>()

    /**
     * 添加子节点
     */
    fun addChild(node: BaseTreeNode) {
        childrens[node.signature] = node
    }
}
