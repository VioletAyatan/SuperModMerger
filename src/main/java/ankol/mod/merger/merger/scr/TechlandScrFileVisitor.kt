package ankol.mod.merger.merger.scr

import ankol.mod.merger.antlr.scr.TechlandScriptBaseVisitor
import ankol.mod.merger.antlr.scr.TechlandScriptParser.*
import ankol.mod.merger.core.BaseTreeNode
import ankol.mod.merger.merger.scr.node.ScrContainerScriptNode
import ankol.mod.merger.merger.scr.node.ScrFunCallScriptNode
import ankol.mod.merger.merger.scr.node.ScrLeafScriptNode
import org.antlr.v4.runtime.ParserRuleContext
import org.antlr.v4.runtime.TokenStream
import org.antlr.v4.runtime.misc.Interval

class TechlandScrFileVisitor(private val tokenStream: TokenStream) : TechlandScriptBaseVisitor<BaseTreeNode>() {
    //检测重复函数的东西
    private val repeatableFunctions: MutableMap<String, MutableSet<String>> = HashMap()
    private var currentFunBlockSignature = "ROOT" //标记当前处理到哪个函数块了，重复函数签名生成仅限自己对应的函数块内
    private var containerNode: ScrContainerScriptNode? = null

    companion object {
        private const val FUN_CALL: String = "funCall"
        private const val METHOD_REFERENCE: String = "methodReference"
        private const val FUN_BLOCK: String = "funBlock"
        private const val SUB_FUN: String = "sub"
        private const val VARIABLE: String = "variable"
        private const val USE: String = "use"
        private const val IMPORT: String = "import"
        private const val EXPORT: String = "export"
        private const val DIRECTIVE = "directive"
        private const val MACRO = "macro"
        private const val EXTERN = "extern"
        private const val IF = "if"
        private const val ELSE_IF = "elseif"
        private const val ELSE = "else"
    }

    /**
     * 文件根节点
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

    /**
     * 导入声明
     */
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

    /**
     * 导出声明
     */
    override fun visitExportDecl(ctx: ExportDeclContext): BaseTreeNode {
        // Export 签名示例: "export:EJumpMaintainedSpeedSource_MoveController"
        // 这样 Mod 修改同一个变量时，能通过签名找到并覆盖它
        val name = ctx.Id().text
        val signature = "$EXPORT:$name"
        return ScrLeafScriptNode(
            signature,
            getStartTokenIndex(ctx),
            getStopTokenIndex(ctx),
            ctx.start.line,
            tokenStream
        )
    }

    /**
     * sub函数块
     */
    override fun visitSubDecl(ctx: SubDeclContext): BaseTreeNode {
        val previousContainer = this.containerNode //这是标记上一层的容器，以便恢复

        val signature = generateFunctionBlockSignature("$SUB_FUN:${ctx.Id().text}")
        val subNode = ScrContainerScriptNode(
            signature,
            getStartTokenIndex(ctx),
            getStopTokenIndex(ctx),
            ctx.start.line,
            tokenStream
        )
        this.containerNode = subNode
        // 注意：subDecl 包含 paramList 和 functionBlock
        this.currentFunBlockSignature = signature
        visitFunctionBlockContent(subNode, ctx.functionBlock())

        this.containerNode = previousContainer //恢复容器节点
        return subNode
    }

    /**
     * 函数块声明
     */
    override fun visitFuntionBlockDecl(ctx: FuntionBlockDeclContext): BaseTreeNode {
        val previousContainer = this.containerNode //这是标记上一层的容器，以便恢复

        val funcName = ctx.Id().text
        var signature = "$FUN_BLOCK:$funcName"
        if (ctx.valueList() != null) {
            signature += ":${getFullText(ctx.valueList())}"
        }

        this.currentFunBlockSignature = signature
        signature = generateFunctionBlockSignature(signature)
        val funBlockContainer = ScrContainerScriptNode(
            signature,
            getStartTokenIndex(ctx),
            getStopTokenIndex(ctx),
            ctx.start.line,
            tokenStream
        )
        this.containerNode = funBlockContainer
        visitFunctionBlockContent(funBlockContainer, ctx.functionBlock())

        this.containerNode = previousContainer //恢复容器节点
        return funBlockContainer
    }

    /**
     * 函数调用
     */
    override fun visitFuntionCallDecl(ctx: FuntionCallDeclContext): BaseTreeNode {
        //生成签名
        val funcName = ctx.Id().text
        val valueList = getValueList(ctx.valueList())
        val argsList = valueList.map { it.text }
        var signature = "$FUN_CALL:$funcName"
        //检测重复签名的处理逻辑
        val signatures = repeatableFunctions.getOrDefault(currentFunBlockSignature, HashSet())
        val children: MutableMap<String, BaseTreeNode> = containerNode!!.childrens

        if (signatures.contains(signature)) {
            // 已标记为可重复函数，根据参数数量采用不同策略
            signature = generateFunctionCallSignature(signature, argsList)
        } else if (children.containsKey(signature)) {
            // 发现重复的函数调用，需要开始特殊处理
            val lastNode = children[signature] as ScrFunCallScriptNode
            signatures.add("$FUN_CALL:$funcName") //标记这个函数为可重复函数

            // 重新生成已存在节点的签名
            val lastNewSignature = generateFunctionCallSignature(signature, lastNode.arguments)
            lastNode.signature = lastNewSignature
            children.remove(signature)
            children[lastNewSignature] = lastNode

            // 当前处理的签名也要重新生成
            signature = generateFunctionCallSignature(signature, argsList)
        }
        repeatableFunctions[currentFunBlockSignature] = signatures
        return ScrFunCallScriptNode(
            signature,
            getStartTokenIndex(ctx),
            getStopTokenIndex(ctx),
            ctx.start.line,
            tokenStream,
            argsList
        )
    }

    /**
     * 方法引用
     */
    override fun visitMethodReferenceFunCallDecl(ctx: MethodReferenceFunCallDeclContext): BaseTreeNode {
        val referanceName = ctx.Id(0).text
        val funName = ctx.Id(1).text
        val signature = "${METHOD_REFERENCE}:${referanceName}:${funName}"

        return ScrFunCallScriptNode(
            signature,
            getStartTokenIndex(ctx),
            getStopTokenIndex(ctx),
            ctx.start.line,
            tokenStream,
            getValueList(ctx.valueList()).map { it.text }
        )
    }

    /**
     * 预处理指令调用
     */
    override fun visitDirectiveCall(ctx: DirectiveCallContext): BaseTreeNode {
        // 处理预处理指令调用，例如: !define MAX_SPEED 10
        val directiveName = ctx.Id().text
        val valueList = ctx.valueList()
        var signature = "$DIRECTIVE:$directiveName:"
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

    /**
     * 宏声明
     */
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

    /**
     * 变量声明
     */
    override fun visitVariableDecl(ctx: VariableDeclContext): BaseTreeNode {
        val signature = generateFunctionBlockSignature("${VARIABLE}:${ctx.type().text}:${ctx.Id().text}")
        return ScrLeafScriptNode(
            signature,
            getStartTokenIndex(ctx),
            getStopTokenIndex(ctx),
            ctx.start.line,
            tokenStream
        )
    }

    /**
     * extern声明
     */
    override fun visitExternDecl(ctx: ExternDeclContext): BaseTreeNode {
        val signature = generateFunctionBlockSignature("${EXTERN}:${ctx.type().text}:${ctx.Id().text}")
        return ScrLeafScriptNode(
            signature,
            getStartTokenIndex(ctx),
            getStopTokenIndex(ctx),
            ctx.start.line,
            tokenStream
        )
    }

    /**
     * use语句
     */
    override fun visitUseDecl(ctx: UseDeclContext): BaseTreeNode {
        val name = ctx.Id().text
        var signature = "$USE:$name"
        if (ctx.valueList() != null) {
            signature += ":${getFullText(ctx.valueList())}"
        }

        return ScrLeafScriptNode(
            signature,
            getStartTokenIndex(ctx),
            getStopTokenIndex(ctx),
            ctx.start.line,
            tokenStream
        )
    }

    /**
     * 逻辑控制语句
     */
    override fun visitLogicControlDecl(ctx: LogicControlDeclContext): BaseTreeNode {
        val previousContainer = this.containerNode

        // 处理 if 语句
        var ifSignature = IF
        val signatures = repeatableFunctions.getOrDefault(currentFunBlockSignature, HashSet())

        // 检查是否已标记为可重复，如果是则需要计算索引
        if (signatures.contains(IF)) {
            // 已标记为可重复，计算当前是第几个 if
            val children = containerNode!!.childrens
            var ifIndex = 0
            for (key in children.keys) {
                if (key.startsWith("$IF:")) {
                    ifIndex++
                }
            }
            ifSignature = "$IF:$ifIndex"
        } else {
            val children: MutableMap<String, BaseTreeNode> = containerNode!!.childrens
            // 发现重复的if语句，重新生成signature（使用索引）
            if (children.containsKey(IF)) {
                val existingIfNode = children[IF]!!
                val existingIfSignature = "$IF:0" // 第一个 if 使用索引 0

                // 更新已存在的节点签名
                existingIfNode.signature = existingIfSignature
                children.remove(IF)
                children[existingIfSignature] = existingIfNode

                signatures.add(IF) // 标记if为可重复语句
                ifSignature = "$IF:1" // 当前这个是第二个，使用索引 1
            }
        }
        repeatableFunctions[currentFunBlockSignature] = signatures

        val ifNode = ScrContainerScriptNode(
            ifSignature,
            getStartTokenIndex(ctx),
            getStopTokenIndex(ctx),
            ctx.start.line,
            tokenStream
        )

        // 切换容器到 ifNode，处理 if 块内的语句
        this.containerNode = ifNode
        visitFunctionBlockContent(ifNode, ctx.functionBlock())

        // 处理 else if 子句 - 作为 if 节点的子节点（也使用索引）
        for ((index, elseIfCtx) in ctx.elseIfClause().withIndex()) {
            var elseIfSignature = ELSE_IF

            // 对于 else if，也使用延迟唯一化（基于索引）
            if (signatures.contains(ELSE_IF)) {
                // 已标记为可重复，计算当前是第几个 else if
                val children = ifNode.childrens
                var elseIfIndex = 0
                for (key in children.keys) {
                    if (key.startsWith("$ELSE_IF:")) {
                        elseIfIndex++
                    }
                }
                elseIfSignature = "$ELSE_IF:$elseIfIndex"
            } else if (index > 0) {
                // 第二个及以后的 else if，触发延迟唯一化
                val children = ifNode.childrens
                if (children.containsKey(ELSE_IF)) {
                    val existingElseIfNode = children[ELSE_IF]!!
                    val existingElseIfSignature = "$ELSE_IF:0"

                    existingElseIfNode.signature = existingElseIfSignature
                    children.remove(ELSE_IF)
                    children[existingElseIfSignature] = existingElseIfNode

                    signatures.add(ELSE_IF)
                    elseIfSignature = "$ELSE_IF:$index"
                }
            }

            val elseIfNode = ScrContainerScriptNode(
                elseIfSignature,
                getStartTokenIndex(elseIfCtx),
                getStopTokenIndex(elseIfCtx),
                elseIfCtx.start.line,
                tokenStream
            )

            // 处理 else if 块内的语句
            this.containerNode = elseIfNode
            visitFunctionBlockContent(elseIfNode, elseIfCtx.functionBlock())

            // 将 else if 节点添加为 if 节点的子节点
            ifNode.addChild(elseIfNode)
        }
        repeatableFunctions[currentFunBlockSignature] = signatures

        // 处理 else 子句 - 作为 if 节点的子节点，签名固定为 "else"
        val elseCtx = ctx.elseClause()
        if (elseCtx != null) {
            val elseNode = ScrContainerScriptNode(
                ELSE,
                getStartTokenIndex(elseCtx),
                getStopTokenIndex(elseCtx),
                elseCtx.start.line,
                tokenStream
            )

            // 处理 else 块内的语句
            this.containerNode = elseNode
            visitFunctionBlockContent(elseNode, elseCtx.functionBlock())

            // 将 else 节点添加为 if 节点的子节点
            ifNode.addChild(elseNode)
        }
        // 恢复容器节点
        this.containerNode = previousContainer
        // 返回 if 节点
        return ifNode
    }

    /**
     * 为函数块生成为唯一签名（使用subDecl、funBloackDecl）
     * 使用ID+参数+索引作为唯一签名
     * @return 签名
     */
    private fun generateFunctionBlockSignature(baseSignature: String): String {
        var _signature = baseSignature
        val repeatebleSignatures = repeatableFunctions.getOrDefault(currentFunBlockSignature, HashSet())
        val childrens = containerNode!!.childrens
        if (repeatebleSignatures.contains(_signature)) {
            var index = 0
            for (key in childrens.keys) {
                //先提取出sub:xxx:1中的索引1，然后+1生成当前自己的索引
                //1 确保 key 属于这个基础签名族（如都是 sub:imports: 开头）
                //2 移除前缀，提取冒号后面的部分（如 "0"、"1"、"123"）
                //3 验证提取的部分全是数字，排除掉 sub:imports:abc 这样的非标准索引化签名
                if (key.startsWith("$_signature:") && key.removePrefix("$_signature:").all { it.isDigit() }) {
                    index++
                }
            }
            _signature = "$_signature:$index"
        } else if (childrens.containsKey(_signature)) {
            val lastNode = childrens[_signature]!!
            repeatebleSignatures.add(_signature) //标记这个sub为可重复函数
            // 重新生成已存在节点的签名
            val lastNewSignature = "$_signature:0"
            lastNode.signature = lastNewSignature
            childrens.remove(_signature)
            childrens[lastNewSignature] = lastNode
            // 当前处理的签名也要重新生成
            _signature = "$_signature:1"
        }
        return _signature
    }

    /**
     * 为可重复函数生成签名
     * 规则：
     * 1. 参数数量 <= 1：使用索引标记 (funCall:funcName:0, funCall:funcName:1, ...)
     * 2. 参数数量 > 1：先用第一个参数加入签名 (funCall:funcName:param1)
     *    如果仍有重复，则追加索引标记 (funCall:funcName:param1:0, funCall:funcName:param1:1, ...)
     */
    private fun generateFunctionCallSignature(baseSignature: String, argsList: List<String>): String {
        if (argsList.size <= 1) {
            // 参数数量 <= 1，直接使用索引
            val children = containerNode!!.childrens
            var index = 0
            for (key in children.keys) {
                if (key.startsWith("$baseSignature:") && key.removePrefix("$baseSignature:").all { it.isDigit() }) {
                    index++
                }
            }
            return "$baseSignature:$index"
        } else {
            // 参数数量 > 1，先用第一个参数加入签名
            val signatureWithParam = "$baseSignature:${argsList.first()}"
            val children = containerNode!!.childrens

            if (children.containsKey(signatureWithParam)) {
                // 仍然有重复，需要追加索引
                var index = 0
                for (key in children.keys) {
                    if (key.startsWith("$signatureWithParam:") && key.removePrefix("$signatureWithParam:").all { it.isDigit() }) {
                        index++
                    }
                }
                return "$signatureWithParam:$index"
            }
            return signatureWithParam
        }
    }

    /**
     * 处理函数块内容
     */
    private fun visitFunctionBlockContent(parent: ScrContainerScriptNode, ctx: FunctionBlockContext?) {
        if (ctx == null || ctx.statements() == null) {
            return
        }
        for (statement in ctx.statements()) {
            val child = visit(statement)
            if (child != null) {
                parent.addChild(child)
            }
        }
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
        return ctx.start.inputStream.getText(Interval(a, b)).replace("\\s".toRegex(), "")
    }

    private fun getValueList(context: ValueListContext?): MutableList<ExpressionContext> {
        val valueList = ArrayList<ExpressionContext>()
        if (context != null) {
            valueList.addAll(context.expression())
        }
        return valueList
    }
}
