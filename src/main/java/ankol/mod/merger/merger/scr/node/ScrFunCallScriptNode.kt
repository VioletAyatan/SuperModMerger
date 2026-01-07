package ankol.mod.merger.merger.scr.node

import ankol.mod.merger.core.BaseTreeNode
import org.antlr.v4.runtime.CommonTokenStream

/**
 * 函数调用节点
 */
class ScrFunCallScriptNode(
    signature: String,
    startTokenIndex: Int,
    stopTokenIndex: Int,
    line: Int,
    tokenStream: CommonTokenStream,
    val functionName: String,
    val arguments: List<String>
) : BaseTreeNode(signature, startTokenIndex, stopTokenIndex, line, tokenStream)
