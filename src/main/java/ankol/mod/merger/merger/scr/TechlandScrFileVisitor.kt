package ankol.mod.merger.merger.scr

import ankol.mod.merger.antlr.scr.TechlandScriptBaseVisitor
import ankol.mod.merger.antlr.scr.TechlandScriptParser.*
import ankol.mod.merger.core.BaseTreeNode
import ankol.mod.merger.merger.scr.node.ScrContainerScriptNode
import ankol.mod.merger.merger.scr.node.ScrFunCallScriptNode
import ankol.mod.merger.merger.scr.node.ScrLeafScriptNode
import org.antlr.v4.runtime.CommonTokenStream
import org.antlr.v4.runtime.ParserRuleContext
import org.antlr.v4.runtime.misc.Interval

class TechlandScrFileVisitor(private val tokenStream: CommonTokenStream) : TechlandScriptBaseVisitor<BaseTreeNode>() {
    //检测重复函数的东西
    private val repeatableFunctions: MutableMap<String, MutableSet<String>> = HashMap()
    private var currentFunBlockSignature = "EMPTY" //标记当前处理到哪个函数块了，重复函数签名生成仅限自己对应的函数块内
    private var containerNode: ScrContainerScriptNode? = null

    companion object {
        private const val FUN_CALL: String = "funCall"
        private const val FUN_BLOCK: String = "funBlock"
        private const val SUB_FUN: String = "sub"
        private const val VARIABLE: String = "variable"
        private const val USE: String = "use"
        private const val IMPORT: String = "import"
        private const val EXPORT: String = "export"
        private const val DIRECTIVE = "directive"
        private const val MACRO = "macro"
    }

    /**
     * 根文件
     */
    override fun visitFile(ctx: FileContext): BaseTreeNode {
        val rootNode = ScrContainerScriptNode(
            "ROOT",
            getStartTokenIndex(ctx),
            getStopTokenIndex(ctx),
            ctx.getStart().line,
            tokenStream
        )
        this.containerNode = rootNode
        for (defCtx in ctx.definition()) {
            val childNode = visit(defCtx)
            if (childNode != null) {
                rootNode.addChild(childNode)
            }
        }
        return rootNode
    }

    //===========================导入/导出===========================
    override fun visitImportDecl(ctx: ImportDeclContext): BaseTreeNode {
        // Import 签名示例: "import:data/scripts/inputs.scr"
        val path = ctx.String().text
        val signature = "$IMPORT:$path"
        return ScrLeafScriptNode(
            signature,
            getStartTokenIndex(ctx),
            getStopTokenIndex(ctx),
            ctx.start.line,
            tokenStream
        )
    }

    override fun visitExportDecl(ctx: ExportDeclContext): BaseTreeNode {
        // Export 签名示例: "export:EJumpMaintainedSpeedSource_MoveController"
        // 这样 Mod 修改同一个变量时，能通过签名找到并覆盖它
        val name = ctx.Id().getText()
        val signature = "$EXPORT:$name"
        return ScrLeafScriptNode(
            signature,
            getStartTokenIndex(ctx),
            getStopTokenIndex(ctx),
            ctx.start.line,
            tokenStream
        )
    }

    //===========================函数块===========================
    override fun visitSubDecl(ctx: SubDeclContext): BaseTreeNode {
        // Sub 签名示例: "sub:main"
        val name = ctx.Id().text
        val signature = "$SUB_FUN:$name"
        val subNode = ScrContainerScriptNode(
            signature,
            getStartTokenIndex(ctx),
            getStopTokenIndex(ctx),
            ctx.start.line,
            tokenStream
        )
        this.containerNode = subNode
        // 注意：subDecl 包含 paramList 和 functionBlock
        visitFunctionBlockContent(subNode, ctx.functionBlock())
        return subNode
    }

    override fun visitFuntionBlockDecl(ctx: FuntionBlockDeclContext): BaseTreeNode {
        val funcName = ctx.Id().text
        val rawParams = if (ctx.valueList() != null) getFullText(ctx.valueList()) else ""
        var signature = "$FUN_BLOCK:$funcName"
        val cleanParams = rawParams.replace("\\s+".toRegex(), "")
        if (!cleanParams.isEmpty()) {
            signature += ":$cleanParams"
        }
        val blockNode = ScrContainerScriptNode(
            signature,
            getStartTokenIndex(ctx),
            getStopTokenIndex(ctx),
            ctx.start.line,
            tokenStream
        )
        this.containerNode = blockNode
        this.currentFunBlockSignature = signature
        visitFunctionBlockContent(blockNode, ctx.functionBlock())
        return blockNode
    }

    private fun visitFunctionBlockContent(parent: ScrContainerScriptNode, ctx: FunctionBlockContext?) {
        if (ctx == null || ctx.statements() == null) {
            return
        }
        for (statement in ctx.statements()) {
            // visit(stmt) 会调用 visitStatements，然后再分发到 visitFuntionCallDecl 等
            val child = visit(statement)
            if (child != null) {
                parent.addChild(child)
            }
        }
    }

    /**
     * 函数调用的处理
     */
    override fun visitFuntionCallDecl(ctx: FuntionCallDeclContext): BaseTreeNode {
        val funcName = ctx.Id().text
        val valueList = getValueList(ctx.valueList())
        val argsList = valueList.map { it.text }
        //提取函数签名，对于特殊的重复函数需要特殊处理
        var signature = "$FUN_CALL:$funcName"
        val signatures = repeatableFunctions.getOrDefault(currentFunBlockSignature, HashSet())
        if (signatures.contains(signature)) {
            signature = signature + ":" + argsList.first()
        } else {
            val children: MutableMap<String, BaseTreeNode> = containerNode!!.childrens
            //发现重复的函数调用，重新生成signature
            if (children.containsKey(signature)) {
                val funCallNode = children[signature] as ScrFunCallScriptNode
                val newSignature = funCallNode.signature + ":" + funCallNode.arguments.first()
                funCallNode.signature = newSignature
                children.remove(signature)
                children[newSignature] = funCallNode
                signatures.add("$FUN_CALL:$funcName") //标记这个函数为可重复函数，后续生成签名时需要特殊处理
                signature = newSignature
            }
        }
        repeatableFunctions[currentFunBlockSignature] = signatures
        return ScrFunCallScriptNode(
            signature,
            getStartTokenIndex(ctx),
            getStopTokenIndex(ctx),
            ctx.start.line,
            tokenStream,
            funcName,
            argsList
        )
    }

    override fun visitStatements(ctx: StatementsContext): BaseTreeNode? {
        if (ctx.funtionCallDecl() != null) {
            return visit(ctx.funtionCallDecl())
        }
        if (ctx.funtionBlockDecl() != null) {
            return visit(ctx.funtionBlockDecl())
        }
        if (ctx.variableDecl() != null) {
            return visit(ctx.variableDecl())
        }
        if (ctx.useDecl() != null) {
            return visit(ctx.useDecl())
        }
        // 如果有 externDecl 或其他未处理的类型，会返回 null，意味着我们在合并时忽略它们（或需要补充处理逻辑）
        if (ctx.externDecl() != null) {
            return visit(ctx.externDecl())
        }
        return null
    }

    override fun visitDirectiveCall(ctx: DirectiveCallContext): BaseTreeNode {
        // 处理预处理指令调用，例如: !define MAX_SPEED 10
        val directiveName = ctx.Id().text
        val valueList = ctx.valueList()
        var signature: String = "$DIRECTIVE:$directiveName:"
        if (valueList != null) {
            signature += ":" + valueList.getText()
        }
        return ScrLeafScriptNode(
            signature,
            getStartTokenIndex(ctx),
            getStopTokenIndex(ctx),
            ctx.start.line,
            tokenStream
        )
    }

    override fun visitMacroDecl(ctx: MacroDeclContext): BaseTreeNode {
        val macroId = ctx.MacroId() //Like: $Police_parking_dead_zone
        val signature: String = MACRO + ":" + macroId.text
        return ScrLeafScriptNode(
            signature,
            getStartTokenIndex(ctx),
            getStopTokenIndex(ctx),
            ctx.start.line,
            tokenStream
        )
    }

    override fun visitVariableDecl(ctx: VariableDeclContext): BaseTreeNode {
        // 局部变量声明，如: float val = 1.0;
        // 签名示例: "variable:flot:val"
        val name = ctx.type().toString() + ":" + ctx.Id().text
        return ScrLeafScriptNode(
            "$VARIABLE:$name",
            getStartTokenIndex(ctx),
            getStopTokenIndex(ctx),
            ctx.start.line,
            tokenStream
        )
    }

    override fun visitUseDecl(ctx: UseDeclContext): BaseTreeNode {
        // use 语句，例如: use Input();
        // use 语句通常是可以重复的（追加模式），所以把参数也放进签名里
        val name = ctx.Id().text
        val params = if (ctx.valueList() != null) getFullText(ctx.valueList()) else ""
        val cleanParams = params.replace("\\s+".toRegex(), "")

        return ScrLeafScriptNode(
            "$USE:$name:$cleanParams",
            getStartTokenIndex(ctx),
            getStopTokenIndex(ctx),
            ctx.start.line,
            tokenStream
        )
    }

    /**
     * 获取context的起始token索引
     */
    private fun getStartTokenIndex(ctx: ParserRuleContext): Int {
        return ctx.start.tokenIndex
    }

    /**
     * 获取context的结束token索引
     */
    private fun getStopTokenIndex(ctx: ParserRuleContext): Int {
        return ctx.stop.tokenIndex
    }

    /**
     * 关键工具方法：获取 Context 对应的原始文本（包含空格、注释等）
     */
    private fun getFullText(ctx: ParserRuleContext): String {
        if (ctx.start == null || ctx.stop == null) return ""
        val a = ctx.start.startIndex
        val b = ctx.stop.stopIndex
        // 这里的 input 是 CharStream，能拿到最原始的字符流
        return ctx.start.inputStream.getText(Interval(a, b))
    }

    private fun getValueList(context: ValueListContext): MutableList<ExpressionContext> {
        val valueList = ArrayList<ExpressionContext>()
        valueList.addAll(context.expression())
        return valueList
    }
}

