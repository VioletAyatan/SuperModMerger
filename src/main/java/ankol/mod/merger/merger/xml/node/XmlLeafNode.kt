package ankol.mod.merger.merger.xml.node

import org.antlr.v4.runtime.CommonTokenStream

/**
 * XML叶子节点
 * 
 * @author Ankol
 */
class XmlLeafNode(
    signature: String,
    startTokenIndex: Int,
    stopTokenIndex: Int,
    line: Int,
    tokenStream: CommonTokenStream,
    attributes: MutableMap<String, String>
) : XmlNode(signature, startTokenIndex, stopTokenIndex, line, tokenStream, attributes)

