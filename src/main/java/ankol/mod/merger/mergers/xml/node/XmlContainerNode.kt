package ankol.mod.merger.mergers.xml.node

import ankol.mod.merger.tools.logger
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
    private val log = logger()
    val childrens: MutableMap<String, XmlNode> = LinkedHashMap()

    /**
     * 添加子节点
     */
    fun addChild(node: XmlNode) {
        // Siblings with the same signature (e.g. the 31 <level id="1"> blocks in Dying Light 1's default_levels.xml) get #2, #3...
        var key = node.signature
        var occurrence = 2
        while (childrens.contains(key)) {
            key = "${node.signature}#${occurrence++}"
        }
        if (key != node.signature) {
            log.debug("Repeatable siginature detected: [${node.signature}] stored as [$key] Line: ${node.line} SourceText: ${node.sourceText}.")
        }
        childrens[key] = node
    }

    override fun printTree(indent: String) {
        super.printTree(indent)
        for ((_, child) in childrens) {
            child.printTree("$indent  ")
        }
    }
}
