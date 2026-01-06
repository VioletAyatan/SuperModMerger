package ankol.mod.merger.merger.scr;

import ankol.mod.merger.antlr.scr.TechlandScriptBaseVisitor;
import ankol.mod.merger.antlr.scr.TechlandScriptParser;
import ankol.mod.merger.core.BaseTreeNode;
import ankol.mod.merger.merger.scr.node.ScrContainerScriptNode;
import ankol.mod.merger.merger.scr.node.ScrFunCallScriptNode;
import ankol.mod.merger.merger.scr.node.ScrLeafScriptNode;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.misc.Interval;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.util.*;

public class TechlandScrFileVisitor extends TechlandScriptBaseVisitor<BaseTreeNode> {
    //=========================关键字=========================
    public static final String FUN_CALL = "funCall";
    public static final String FUN_BLOCK = "funBlock";
    public static final String SUB_FUN = "sub";
    public static final String VARIABLE = "variable";
    public static final String USE = "use";
    public static final String IMPORT = "import";
    public static final String EXPORT = "export";
    private static final String DIRECTIVE = "directive";
    private static final String MACRO = "macro";

    //检测重复函数的东西
    private final Map<String, Set<String>> repeatableFunctions = new HashMap<>();
    private String currentFunBlockSignature = "EMPTY"; //标记当前处理到哪个函数块了，重复函数签名生成仅限自己对应的函数块内

    private ScrContainerScriptNode containerNode;

    /**
     * Token流引用（用于按需提取源文本）
     */
    private final CommonTokenStream tokenStream;

    public TechlandScrFileVisitor(CommonTokenStream tokenStream) {
        this.tokenStream = tokenStream;
    }

    /**
     * 获取context的起始token索引
     */
    private int getStartTokenIndex(ParserRuleContext ctx) {
        return ctx.start.getTokenIndex();
    }

    /**
     * 获取context的结束token索引
     */
    private int getStopTokenIndex(ParserRuleContext ctx) {
        return ctx.stop.getTokenIndex();
    }

    /**
     * 根文件
     */
    @Override
    public BaseTreeNode visitFile(TechlandScriptParser.FileContext ctx) {
        ScrContainerScriptNode rootNode = new ScrContainerScriptNode("ROOT",
                getStartTokenIndex(ctx),
                getStopTokenIndex(ctx),
                ctx.getStart().getLine(),
                tokenStream
        );
        this.containerNode = rootNode;
        for (TechlandScriptParser.DefinitionContext defCtx : ctx.definition()) {
            BaseTreeNode childNode = visit(defCtx);
            if (childNode != null) {
                rootNode.addChild(childNode);
            }
        }
        return rootNode;
    }

    //===========================导入/导出===========================
    @Override
    public BaseTreeNode visitImportDecl(TechlandScriptParser.ImportDeclContext ctx) {
        // Import 签名示例: "import:data/scripts/inputs.scr"
        String path = ctx.String().getText();
        String signature = IMPORT + ":" + path;
        return new ScrLeafScriptNode(
                signature,
                getStartTokenIndex(ctx),
                getStopTokenIndex(ctx),
                ctx.start.getLine(),
                tokenStream
        );
    }

    @Override
    public BaseTreeNode visitExportDecl(TechlandScriptParser.ExportDeclContext ctx) {
        // Export 签名示例: "export:EJumpMaintainedSpeedSource_MoveController"
        // 这样 Mod 修改同一个变量时，能通过签名找到并覆盖它
        String name = ctx.Id().getText();
        String signature = EXPORT + ":" + name;
        return new ScrLeafScriptNode(
                signature,
                getStartTokenIndex(ctx),
                getStopTokenIndex(ctx),
                ctx.start.getLine(),
                tokenStream
        );
    }

    //===========================函数块===========================
    @Override
    public BaseTreeNode visitSubDecl(TechlandScriptParser.SubDeclContext ctx) {
        // Sub 签名示例: "sub:main"
        String name = ctx.Id().getText();
        String signature = SUB_FUN + ":" + name;
        ScrContainerScriptNode subNode = new ScrContainerScriptNode(
                signature,
                getStartTokenIndex(ctx),
                getStopTokenIndex(ctx),
                ctx.start.getLine(),
                tokenStream
        );
        this.containerNode = subNode;
        // 注意：subDecl 包含 paramList 和 functionBlock
        visitFunctionBlockContent(subNode, ctx.functionBlock());
        return subNode;
    }

    @Override
    public BaseTreeNode visitFuntionBlockDecl(TechlandScriptParser.FuntionBlockDeclContext ctx) {
        String funcName = ctx.Id().getText();
        String rawParams = (ctx.valueList() != null) ? getFullText(ctx.valueList()) : "";
        String signature = FUN_BLOCK + ":" + funcName;
        String cleanParams = rawParams.replaceAll("\\s+", "");
        if (!cleanParams.isEmpty()) {
            signature += ":" + cleanParams;
        }
        ScrContainerScriptNode blockNode = new ScrContainerScriptNode(
                signature,
                getStartTokenIndex(ctx),
                getStopTokenIndex(ctx),
                ctx.start.getLine(),
                tokenStream
        );
        this.containerNode = blockNode;
        this.currentFunBlockSignature = signature;
        visitFunctionBlockContent(blockNode, ctx.functionBlock());
        return blockNode;
    }

    private void visitFunctionBlockContent(ScrContainerScriptNode parent, TechlandScriptParser.FunctionBlockContext ctx) {
        if (ctx == null || ctx.statements() == null) {
            return;
        }
        for (TechlandScriptParser.StatementsContext statement : ctx.statements()) {
            // visit(stmt) 会调用 visitStatements，然后再分发到 visitFuntionCallDecl 等
            BaseTreeNode child = visit(statement);
            if (child != null) {
                parent.addChild(child);
            }
        }
    }

    /**
     * 函数调用的处理
     */
    @Override
    public BaseTreeNode visitFuntionCallDecl(TechlandScriptParser.FuntionCallDeclContext ctx) {
        String funcName = ctx.Id().getText();
        List<TechlandScriptParser.ExpressionContext> valueList = getValueList(ctx.valueList());
        ArrayList<String> argsList = new ArrayList<>();

        //提取参数列表
        for (TechlandScriptParser.ExpressionContext expressionContext : valueList) {
            argsList.add(expressionContext.getText());
        }
        //提取函数签名，对于特殊的重复函数需要特殊处理
        String signature = FUN_CALL + ":" + funcName;
        Set<String> signatures = repeatableFunctions.getOrDefault(currentFunBlockSignature, new HashSet<>());
        if (signatures.contains(signature)) {
            signature = signature + ":" + argsList.getFirst();
        } else {
            Map<String, BaseTreeNode> children = containerNode.getChildrens();
            //发现重复的函数调用，重新生成signature
            if (children.containsKey(signature)) {
                ScrFunCallScriptNode funCallNode = (ScrFunCallScriptNode) children.get(signature);
                String newSignature = funCallNode.getSignature() + ":" + funCallNode.getArguments().getFirst();
                funCallNode.setSignature(newSignature);
                children.remove(signature);
                children.put(newSignature, funCallNode);
                signatures.add(FUN_CALL + ":" + funcName); //标记这个函数为可重复函数，后续生成签名时需要特殊处理
                signature = newSignature;
            }
        }
        repeatableFunctions.put(currentFunBlockSignature, signatures);
        return new ScrFunCallScriptNode(
                signature,
                getStartTokenIndex(ctx),
                getStopTokenIndex(ctx),
                ctx.start.getLine(),
                tokenStream,
                funcName,
                argsList
        );
    }

    @Override
    public BaseTreeNode visitStatements(TechlandScriptParser.StatementsContext ctx) {
        if (ctx.funtionCallDecl() != null) {
            return visit(ctx.funtionCallDecl());
        }
        if (ctx.funtionBlockDecl() != null) {
            return visit(ctx.funtionBlockDecl());
        }
        if (ctx.variableDecl() != null) {
            return visit(ctx.variableDecl());
        }
        if (ctx.useDecl() != null) {
            return visit(ctx.useDecl());
        }
        // 如果有 externDecl 或其他未处理的类型，会返回 null，意味着我们在合并时忽略它们（或需要补充处理逻辑）
        if (ctx.externDecl() != null) {
            return visit(ctx.externDecl());
        }
        return null;
    }

    @Override
    public BaseTreeNode visitDirectiveCall(TechlandScriptParser.DirectiveCallContext ctx) {
        // 处理预处理指令调用，例如: !define MAX_SPEED 10
        String directiveName = ctx.Id().getText();
        TechlandScriptParser.ValueListContext valueList = ctx.valueList();
        String signature = DIRECTIVE + ":" + directiveName + ":";
        if (valueList != null) {
            signature += ":" + valueList.getText();
        }
        return new ScrLeafScriptNode(
                signature,
                getStartTokenIndex(ctx),
                getStopTokenIndex(ctx),
                ctx.start.getLine(),
                tokenStream
        );
    }

    @Override
    public BaseTreeNode visitMacroDecl(TechlandScriptParser.MacroDeclContext ctx) {
        TerminalNode macroId = ctx.MacroId(); //Like: $Police_parking_dead_zone
        String signature = MACRO + ":" + macroId.getText();
        return new ScrLeafScriptNode(signature,
                getStartTokenIndex(ctx),
                getStopTokenIndex(ctx),
                ctx.start.getLine(),
                tokenStream
        );
    }

    @Override
    public BaseTreeNode visitVariableDecl(TechlandScriptParser.VariableDeclContext ctx) {
        // 局部变量声明，如: float val = 1.0;
        // 签名示例: "variable:flot:val"
        String name = ctx.type() + ":" + ctx.Id().getText();
        return new ScrLeafScriptNode(
                VARIABLE + ":" + name,
                getStartTokenIndex(ctx),
                getStopTokenIndex(ctx),
                ctx.start.getLine(),
                tokenStream
        );
    }

    @Override
    public BaseTreeNode visitUseDecl(TechlandScriptParser.UseDeclContext ctx) {
        // use 语句，例如: use Input();
        // use 语句通常是可以重复的（追加模式），所以把参数也放进签名里
        String name = ctx.Id().getText();
        String params = (ctx.valueList() != null) ? getFullText(ctx.valueList()) : "";
        String cleanParams = params.replaceAll("\\s+", "");

        return new ScrLeafScriptNode(
                USE + ":" + name + ":" + cleanParams,
                getStartTokenIndex(ctx),
                getStopTokenIndex(ctx),
                ctx.start.getLine(),
                tokenStream
        );
    }

    /**
     * 关键工具方法：获取 Context 对应的原始文本（包含空格、注释等）
     */
    private String getFullText(ParserRuleContext ctx) {
        if (ctx.start == null || ctx.stop == null) return "";
        int a = ctx.start.getStartIndex();
        int b = ctx.stop.getStopIndex();
        // 这里的 input 是 CharStream，能拿到最原始的字符流
        return ctx.start.getInputStream().getText(new Interval(a, b));
    }

    private List<TechlandScriptParser.ExpressionContext> getValueList(TechlandScriptParser.ValueListContext context) {
        ArrayList<TechlandScriptParser.ExpressionContext> valueList = new ArrayList<>();
        if (context == null) {
            return valueList;
        }
        valueList.addAll(context.expression());
        return valueList;
    }
}

