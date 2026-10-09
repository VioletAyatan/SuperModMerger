package ankol.mod.merger.mergers.scr.node

import ankol.mod.merger.constants.ScrNodeType
import ankol.mod.merger.core.BaseTreeNode
import org.antlr.v4.runtime.TokenStream

/**
 * SCR 语法树节点基类，在 [BaseTreeNode] 基础上补充节点类型、名称等描述信息，
 * 供删除规则等按类型、名称匹配节点使用。
 */
abstract class AbstractScrNode(
    signature: String,
    startTokenIndex: Int,
    stopTokenIndex: Int,
    line: Int,
    tokenStream: TokenStream,
) : BaseTreeNode(signature, startTokenIndex, stopTokenIndex, line, tokenStream) {
    /** 节点类型 */
    abstract val nodeType: ScrNodeType

    /** 节点名称（函数名、变量名、import 路径等），没有名称的结构节点（if/else 等）为空串 */
    abstract val name: String

    /** 参数个数，没有参数概念的节点返回 null */
    abstract val argCount: Int?
}
