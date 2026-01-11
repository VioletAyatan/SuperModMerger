package ankol.mod.merger.merger.json.node

import ankol.mod.merger.core.BaseTreeNode
import org.antlr.v4.runtime.TokenStream

/**
 * JSON键值对节点 - 表示对象中的一个属性
 */
class JsonPairNode(
    signature: String,
    startTokenIndex: Int,
    stopTokenIndex: Int,
    lineNumber: Int,
    tokenStream: TokenStream
) : BaseTreeNode(signature, startTokenIndex, stopTokenIndex, lineNumber, tokenStream) {

    var value: BaseTreeNode? = null

    /**
     * 设置值节点
     */
    fun setValue(node: BaseTreeNode) {
        value = node
    }

    /**
     * 获取值节点
     */
    fun getValue(): BaseTreeNode? = value
}

