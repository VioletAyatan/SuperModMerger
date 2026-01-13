package ankol.mod.merger.merger.xml.node

import org.antlr.v4.runtime.CommonTokenStream

/**
 * XML容器节点，标识可以有子节点的容器
 * 例如：<skills> ... </skills>
 * 
 * @author Ankol
 */
class XmlContainerNode(
    signature: String,
    startTokenIndex: Int,
    stopTokenIndex: Int,
    line: Int,
    tokenStream: CommonTokenStream,
    attributes: MutableMap<String, String>
) : XmlNode(signature, startTokenIndex, stopTokenIndex, line, tokenStream, attributes) {

    val childrens: MutableMap<String, XmlNode> = LinkedHashMap()

    /**
     * 添加子节点
     */
    fun addChild(node: XmlNode) {
        childrens[node.signature] = node
    }

    override fun printTree(indent: String) {
        super.printTree(indent)
        for ((_, child) in childrens) {
            child.printTree("$indent  ")
        }
    }
}
