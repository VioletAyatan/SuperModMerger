package ankol.mod.merger.merger.scr.news;

import ankol.mod.merger.antlr4.scr.TechlandScriptBaseVisitor;
import ankol.mod.merger.antlr4.scr.TechlandScriptParser;
import ankol.mod.merger.merger.scr.news.node.ScrContainerNode;
import ankol.mod.merger.merger.scr.news.node.ScrFunCallNode;
import ankol.mod.merger.merger.scr.news.node.ScrLeafNode;
import ankol.mod.merger.merger.scr.news.node.ScrNode;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.misc.Interval;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.util.*;

public class ScrModelVisitor extends TechlandScriptBaseVisitor<ScrNode> {
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

    private final Set<String> repeatableFunctions = new HashSet<>();

    private ScrContainerNode containerNode;

    @Override
    public ScrNode visitFile(TechlandScriptParser.FileContext ctx) {
        ScrContainerNode rootNode = new ScrContainerNode("ROOT",
                ctx.start.getStartIndex(),
                ctx.stop.getStopIndex(),
                ctx.getStart().getLine(),
                getFullText(ctx)
        );
        this.containerNode = rootNode;
        for (TechlandScriptParser.DefinitionContext defCtx : ctx.definition()) {
            ScrNode childNode = visit(defCtx);
            if (childNode != null) {
                rootNode.addChild(childNode);
            }
        }
        return rootNode;
    }

    @Override
    public ScrNode visitImportDecl(TechlandScriptParser.ImportDeclContext ctx) {
        // Import 签名示例: "import:data/scripts/inputs.scr"
        // 这样可以防止同一个文件被 import 两次
        String path = ctx.String().getText();
        String signature = IMPORT + ":" + path;
        return new ScrLeafNode(
                signature,
                ctx.start.getStartIndex(),
                ctx.stop.getStopIndex(),
                ctx.start.getLine(),
                getFullText(ctx)
        );
    }

    @Override
    public ScrNode visitExportDecl(TechlandScriptParser.ExportDeclContext ctx) {
        // Export 签名示例: "export:EJumpMaintainedSpeedSource_MoveController"
        // 这样 Mod 修改同一个变量时，能通过签名找到并覆盖它
        String name = ctx.Id().getText();
        String signature = EXPORT + ":" + name;
        return new ScrLeafNode(
                signature,
                ctx.start.getStartIndex(),
                ctx.stop.getStopIndex(),
                ctx.start.getLine(),
                getFullText(ctx)
        );
    }

    @Override
    public ScrNode visitSubDecl(TechlandScriptParser.SubDeclContext ctx) {
        // Sub 签名示例: "sub:main"
        String name = ctx.Id().getText();
        String signature = SUB_FUN + ":" + name;

        // 这里的 getFullText 获取的是 "sub main() { ... }" 整个一大块字符串
        ScrContainerNode subNode = new ScrContainerNode(
                signature,
                ctx.start.getStartIndex(),
                ctx.stop.getStopIndex(),
                ctx.start.getLine(),
                getFullText(ctx)
        );
        this.containerNode = subNode;
        // 注意：subDecl 包含 paramList 和 functionBlock
        visitFunctionBlockContent(subNode, ctx.functionBlock());
        return subNode;
    }

    /**
     * 处理嵌套块，例如: Jump("Normal") { ... }
     * 这是 Techland 脚本中最关键的结构。
     */
    @Override
    public ScrNode visitFuntionBlockDecl(TechlandScriptParser.FuntionBlockDeclContext ctx) {
        String funcName = ctx.Id().getText();
        // 提取参数字符串，用于区分不同的块。
        String rawParams = (ctx.valueList() != null) ? getFullText(ctx.valueList()) : "";
        String cleanParams = rawParams.replaceAll("\\s+", "");
        // 签名示例: "block:Jump:"Normal""
        String signature = FUN_BLOCK + ":" + funcName + ":" + cleanParams;
        ScrContainerNode blockNode = new ScrContainerNode(
                signature,
                ctx.start.getStartIndex(),
                ctx.stop.getStopIndex(),
                ctx.start.getLine(),
                getFullText(ctx)
        );
        this.containerNode = blockNode;
        // 递归处理块内部的语句
        visitFunctionBlockContent(blockNode, ctx.functionBlock());
        return blockNode;
    }

    /**
     * 提取 helper：遍历 functionBlock 里的 statements 并添加到父节点
     */
    private void visitFunctionBlockContent(ScrContainerNode parent, TechlandScriptParser.FunctionBlockContext ctx) {
        if (ctx == null || ctx.statements() == null) {
            return;
        }
        for (TechlandScriptParser.StatementsContext statement : ctx.statements()) {
            // visit(stmt) 会调用 visitStatements，然后再分发到 visitFuntionCallDecl 等
            ScrNode child = visit(statement);
            if (child != null) {
                parent.addChild(child);
            }
        }
    }

    /**
     * 处理简单的函数调用/属性设置，例如: Height(1.0);
     */
    @Override
    public ScrNode visitFuntionCallDecl(TechlandScriptParser.FuntionCallDeclContext ctx) {
        String funcName = ctx.Id().getText();
        List<TechlandScriptParser.ExpressionContext> valueList = getValueList(ctx.valueList());
        ArrayList<String> argsList = new ArrayList<>();

        //提取参数列表
        for (TechlandScriptParser.ExpressionContext expressionContext : valueList) {
            argsList.add(expressionContext.getText());
        }
        //提取函数签名，对于特殊的重复函数需要特殊处理
        String signature = FUN_CALL + ":" + funcName;
        if (repeatableFunctions.contains(signature)) {
            signature = signature + ":" + argsList.getFirst();
        } else {
            Map<String, ScrNode> children = containerNode.getChildren();
            //发现重复的函数调用，重新生成signature
            if (children.containsKey(signature)) {
                ScrFunCallNode funCallNode = (ScrFunCallNode) children.get(signature);
                String newSignature = funCallNode.getSignature() + ":" + funCallNode.getArguments().getFirst();
                funCallNode.setSignature(newSignature);
                children.remove(signature);
                children.put(newSignature, funCallNode);
                repeatableFunctions.add(FUN_CALL + ":" + funcName); //标记这个函数为可重复函数，后续生成签名时需要特殊处理
                signature = newSignature;
            }
        }
        return new ScrFunCallNode(
                signature,
                ctx.start.getStartIndex(),
                ctx.stop.getStopIndex(),
                ctx.start.getLine(),
                getFullText(ctx),
                funcName,
                argsList
        );
    }

    /**
     * ANTLR 默认的 visitStatements 只是返回子节点的执行结果。
     * 我们需要确保它能把结果传回来。
     */
    @Override
    public ScrNode visitStatements(TechlandScriptParser.StatementsContext ctx) {
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
    public ScrNode visitDirectiveCall(TechlandScriptParser.DirectiveCallContext ctx) {
        // 处理预处理指令调用，例如: #define MAX_SPEED 10
        String directiveName = ctx.Id().getText();
        String signature = DIRECTIVE + ":" + directiveName;
        if (containerNode.getChildren().containsKey(signature)) {
            //发现重复的函数调用，重新生成signature
            ScrFunCallNode funCallNode = (ScrFunCallNode) containerNode.getChildren().get(signature);
            funCallNode.setSignature(funCallNode.getSignature() + ":" + funCallNode.getArguments().getFirst());
        }
        return new ScrLeafNode(
                signature,
                ctx.start.getStartIndex(),
                ctx.stop.getStopIndex(),
                ctx.start.getLine(),
                getFullText(ctx)
        );
    }

    @Override
    public ScrNode visitMacroDecl(TechlandScriptParser.MacroDeclContext ctx) {
        TerminalNode macroId = ctx.MacroId();
        String signature = MACRO + ":" + macroId.getText();
        return new ScrLeafNode(signature,
                ctx.start.getStartIndex(),
                ctx.stop.getStopIndex(),
                ctx.start.getLine(),
                getFullText(ctx)
        );
    }

    @Override
    public ScrNode visitVariableDecl(TechlandScriptParser.VariableDeclContext ctx) {
        // 局部变量声明，如: float val = 1.0;
        // 签名示例: "var:val"
        String name = ctx.Id().getText();
        return new ScrLeafNode(
                VARIABLE + ":" + name,
                ctx.start.getStartIndex(),
                ctx.stop.getStopIndex(),
                ctx.start.getLine(),
                getFullText(ctx)
        );
    }

    @Override
    public ScrNode visitUseDecl(TechlandScriptParser.UseDeclContext ctx) {
        // use 语句，例如: use Input();
        // use 语句通常是可以重复的（追加模式），所以把参数也放进签名里
        String name = ctx.Id().getText();
        String params = (ctx.valueList() != null) ? getFullText(ctx.valueList()) : "";
        String cleanParams = params.replaceAll("\\s+", "");

        return new ScrLeafNode(
                USE + ":" + name + ":" + cleanParams,
                ctx.start.getStartIndex(),
                ctx.stop.getStopIndex(),
                ctx.start.getLine(),
                getFullText(ctx)
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