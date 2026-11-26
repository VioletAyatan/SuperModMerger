package ankol.mod.merger.mergers.scr.node

import ankol.mod.merger.constants.ScrNodeType
import ankol.mod.merger.core.BaseTreeNode
import ankol.mod.merger.tools.logger
import org.antlr.v4.runtime.TokenStream

/**
 * SCR容器节点，标识语法树中含有子节点的节点
 *
 * 对应到SCR语法中即为包含其他语句的代码块，如函数体、条件语句体等
 * @author Ankol
 */
class ScrContainerNode(
    signature: String,
    startTokenIndex: Int,
    stopTokenIndex: Int,
    line: Int,
    tokenStream: TokenStream,
    override val nodeType: ScrNodeType,
    override val name: String = "",
    /**
     * 参数列表
     */
    val arguments: List<String> = emptyList(),
    /**
     * Token index of the ')' that closes the header of a function block, e.g. Foo("a", 1) { ... }; -1 if there is none
     */
    val headerStopTokenIndex: Int = -1,
) : AbstractScrNode(signature, startTokenIndex, stopTokenIndex, line, tokenStream) {
    private val log = logger()

    /** 只有函数块带参数列表，其余容器（sub/if/else 等）不参与参数个数匹配 */
    override val argCount: Int?
        get() = if (nodeType == ScrNodeType.FUN_BLOCK) arguments.size else null

    /**
     * 子节点映射，key 是节点签名，value 是节点对象
     */
    val childrens: MutableMap<String, BaseTreeNode> = LinkedHashMap()

    /**
     * 添加子节点
     */
    fun addChild(node: BaseTreeNode) {
        if (childrens.contains(node.signature)) {
            log.debug("Repeatable siginature detected: [${node.signature}] Line: ${node.line} SourceText: ${node.sourceText}.")
        }
        childrens[node.signature] = node
    }

    /**
     * The header of a function block (from its name to the closing ')'), as a node of its own, or null if it has none
     */
    fun headerNode(): ScrLeafNode? =
        if (headerStopTokenIndex < 0) null
        else ScrLeafNode(signature, startTokenIndex, headerStopTokenIndex, line, tokenStream, nodeType, name)

    override fun printTree(indent: String) {
        super.printTree(indent)
        for ((_, child) in childrens) {
            child.printTree("$indent  ")
        }
    }
}
