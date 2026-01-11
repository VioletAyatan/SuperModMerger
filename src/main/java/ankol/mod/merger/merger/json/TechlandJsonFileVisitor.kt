package ankol.mod.merger.merger.json

import ankol.mod.merger.antlr.json.JSONBaseVisitor
import ankol.mod.merger.antlr.json.JSONParser
import ankol.mod.merger.core.BaseTreeNode
import ankol.mod.merger.merger.json.node.JsonArrayNode
import ankol.mod.merger.merger.json.node.JsonContainerNode
import ankol.mod.merger.merger.json.node.JsonLeafNode
import ankol.mod.merger.merger.json.node.JsonPairNode
import org.antlr.v4.runtime.ParserRuleContext
import org.antlr.v4.runtime.TokenStream

/**
 * 处理JSON格式文件转成node树
 * @author ankol
 */
class TechlandJsonFileVisitor(val tokenStream: TokenStream) : JSONBaseVisitor<BaseTreeNode>() {

    companion object {
        private const val OBJECT = "OBJECT"
        private const val ARRAY = "ARRAY"
    }

    /**
     * 根节点 - 访问JSON文档入口
     */
    override fun visitJson(ctx: JSONParser.JsonContext): BaseTreeNode {
        return visit(ctx.value())
    }

    /**
     * 访问对象节点 - 创建JsonContainerNode
     */
    override fun visitObj(ctx: JSONParser.ObjContext): BaseTreeNode {
        val objNode = JsonContainerNode(
            OBJECT,
            getStartTokenIndex(ctx),
            getStopTokenIndex(ctx),
            ctx.start.line,
            tokenStream
        )

        // 遍历所有键值对
        ctx.pair()?.forEach { pairContext ->
            val pairNode = visit(pairContext)
            objNode.addChildern(pairNode)
        }

        return objNode
    }

    /**
     * 访问键值对节点 - 创建JsonPairNode
     */
    override fun visitPair(ctx: JSONParser.PairContext): BaseTreeNode {
        // 提取key（去掉引号）
        val key = ctx.STRING().text.removeSurrounding("\"")

        val pairNode = JsonPairNode(
            "pair:$key",
            getStartTokenIndex(ctx),
            getStopTokenIndex(ctx),
            ctx.start.line,
            tokenStream
        )

        // 访问value并设置
        val valueNode = visit(ctx.value())
        pairNode.setValue(valueNode)

        return pairNode
    }

    /**
     * 访问数组节点 - 创建JsonArrayNode
     */
    override fun visitArr(ctx: JSONParser.ArrContext): BaseTreeNode {
        val arrNode = JsonArrayNode(
            ARRAY,
            getStartTokenIndex(ctx),
            getStopTokenIndex(ctx),
            ctx.start.line,
            tokenStream
        )

        // 遍历所有数组元素
        ctx.value()?.forEach { valueContext ->
            val elementNode = visit(valueContext)
            arrNode.addElement(elementNode)
        }

        return arrNode
    }

    /**
     * 访问值节点 - 根据类型返回对应节点
     */
    override fun visitValue(ctx: JSONParser.ValueContext): BaseTreeNode {
        return when {
            // 对象类型
            ctx.obj() != null -> visit(ctx.obj())
            // 数组类型
            ctx.arr() != null -> visit(ctx.arr())
            // 字符串类型
            ctx.STRING() != null -> createLeafNode(ctx, ctx.STRING().text)
            // 数字类型
            ctx.NUMBER() != null -> createLeafNode(ctx, ctx.NUMBER().text)
            // 布尔值和null
            else -> createLeafNode(ctx, ctx.text)
        }
    }

    /**
     * 创建叶子节点
     */
    private fun createLeafNode(ctx: JSONParser.ValueContext, value: String): BaseTreeNode {
        return JsonLeafNode(
            "value:$value",
            getStartTokenIndex(ctx),
            getStopTokenIndex(ctx),
            ctx.start.line,
            tokenStream
        )
    }

    /**
     * 获取起始token索引
     */
    private fun getStartTokenIndex(ctx: ParserRuleContext): Int {
        return ctx.start.tokenIndex
    }

    /**
     * 获取结束token索引
     */
    private fun getStopTokenIndex(ctx: ParserRuleContext): Int {
        return ctx.stop.tokenIndex
    }
}