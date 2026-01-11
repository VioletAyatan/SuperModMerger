package ankol.mod.merger.merger.json.node

import ankol.mod.merger.core.BaseTreeNode
import org.antlr.v4.runtime.TokenStream

/**
 * JSON叶子节点
 * @author Ankol
 */
class JsonLeafNode(signature: String, startTokenIndex: Int, stopTokenIndex: Int, lineNumber: Int, tokenStream: TokenStream) :
    BaseTreeNode(signature, startTokenIndex, stopTokenIndex, lineNumber, tokenStream)