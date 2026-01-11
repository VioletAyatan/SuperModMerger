package ankol.mod.merger.merger.json

import ankol.mod.merger.antlr.json.JSONBaseVisitor
import ankol.mod.merger.antlr.json.JSONParser
import ankol.mod.merger.core.BaseTreeNode
import ankol.mod.merger.merger.json.node.JsonContainerNode
import org.antlr.v4.runtime.TokenStream

/**
 * 处理JSON格式文件转成node树
 * @author ankol
 */
class TechlandJsonFileVisitor(val tokenStream: TokenStream) : JSONBaseVisitor<BaseTreeNode>() {

    /**
     * 根节点
     */
    override fun visitJson(ctx: JSONParser.JsonContext): BaseTreeNode {
        val rootNode = JsonContainerNode(
            getDefaultSignature(ctx),
            ctx.start.tokenIndex,
            ctx.stop.tokenIndex,
            ctx.start.line,
            tokenStream
        )
        //obj遍历
        ctx.value().obj()?.let {
            for (pairContext in it.pair()) {
                rootNode.addChildern(visit(pairContext))
            }
        }
        //array遍历
        ctx.value().arr()?.let {
            for (context in it.value()) {
                rootNode.addChildern(visit(context))
            }
        }
        return rootNode
    }

    override fun visitObj(ctx: JSONParser.ObjContext?): BaseTreeNode {
        return super.visitObj(ctx)
    }

    override fun visitPair(ctx: JSONParser.PairContext?): BaseTreeNode? {
        return super.visitPair(ctx)
    }

    override fun visitArr(ctx: JSONParser.ArrContext?): BaseTreeNode? {
        return super.visitArr(ctx)
    }

    private fun getDefaultSignature(ctx: JSONParser.JsonContext): String {
        val value = ctx.value()
        return if (value.NUMBER() != null) {
            value.NUMBER().text
        } else if (value.STRING() != null) {
            value.STRING().text
        } else if (value.obj() != null) {
            "ROOT"
        } else if (value.arr() != null) {
            "ROOT"
        } else {
            value.text
        }
    }
}