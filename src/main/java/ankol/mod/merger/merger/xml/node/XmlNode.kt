package ankol.mod.merger.merger.xml.node

import ankol.mod.merger.core.BaseTreeNode
import org.antlr.v4.runtime.CommonTokenStream

/**
 * XML基础节点
 * @author Ankol
 */
abstract class XmlNode(
    signature: String,
    startTokenIndex: Int,
    stopTokenIndex: Int,
    line: Int,
    tokenStream: CommonTokenStream,
    var attributes: MutableMap<String, String>
) : BaseTreeNode(signature, startTokenIndex, stopTokenIndex, line, tokenStream)
