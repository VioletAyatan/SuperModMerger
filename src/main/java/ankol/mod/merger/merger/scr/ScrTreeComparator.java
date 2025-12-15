package ankol.mod.merger.merger.scr;

import ankol.mod.merger.antlr4.scr.TechlandScriptParser;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.antlr.v4.runtime.misc.Interval;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 语法树对比器 - 比较两个脚本的语法树，找出差异部分
 */
public class ScrTreeComparator {

    public static class DiffResult {
        public final ParseTree tree1;
        public final ParseTree tree2;
        public final String ruleName;
        public final String description;
        public final int lineNumber1;
        public final int lineNumber2;
        public final int startToken1;
        public final int endToken1;
        public final int startToken2;
        public final int endToken2;

        public DiffResult(ParseTree tree1, ParseTree tree2, String ruleName, String description) {
            this.tree1 = tree1;
            this.tree2 = tree2;
            this.ruleName = ruleName;
            this.description = description;
            this.lineNumber1 = getLineNumber(tree1);
            this.lineNumber2 = getLineNumber(tree2);

            Interval i1 = tree1 != null ? tree1.getSourceInterval() : null;
            Interval i2 = tree2 != null ? tree2.getSourceInterval() : null;
            if (i1 != null) {
                this.startToken1 = i1.a;
                this.endToken1 = i1.b;
            } else {
                this.startToken1 = -1;
                this.endToken1 = -1;
            }
            if (i2 != null) {
                this.startToken2 = i2.a;
                this.endToken2 = i2.b;
            } else {
                this.startToken2 = -1;
                this.endToken2 = -1;
            }
        }

        @Override
        public String toString() {
            String text1 = tree1 != null ? tree1.getText() : "null";
            String text2 = tree2 != null ? tree2.getText() : "null";
            return "[" + ruleName + "] " + description + "\n  Mod1 (Line " + lineNumber1 + "): " + text1 + "\n  Mod2 (Line " + lineNumber2 + "): " + text2;
        }
    }

    public static List<DiffResult> compareFiles(TechlandScriptParser.FileContext file1, TechlandScriptParser.FileContext file2) {
        return compareTrees(file1, file2);
    }

    private static List<DiffResult> compareTrees(ParseTree tree1, ParseTree tree2) {
        List<DiffResult> diffs = new ArrayList<>();

        // 1. 如果是函数块声明 (如 Item {...})，则进行深度比较
        if (tree1 instanceof TechlandScriptParser.FuntionBlockDeclContext block1 && tree2 instanceof TechlandScriptParser.FuntionBlockDeclContext block2) {
            return compareFunctionBlocks(block1.functionBlock(), block2.functionBlock());
        }
        // 2. 如果是文件根节点，则比较文件内的所有顶层定义
        if (tree1 instanceof TechlandScriptParser.FileContext && tree2 instanceof TechlandScriptParser.FileContext) {
            Map<String, TechlandScriptParser.DefinitionContext> defs1 = indexBy(
                    ((TechlandScriptParser.FileContext) tree1).definition(),
                    ScrTreeComparator::extractDefinitionKey
            );
            Map<String, TechlandScriptParser.DefinitionContext> defs2 = indexBy(
                    ((TechlandScriptParser.FileContext) tree2).definition(),
                    ScrTreeComparator::extractDefinitionKey
            );
            return compareMaps(defs1, defs2);
        }

        // 3. 如果是函数调用声明，则进行参数级别比较
        if (tree1 instanceof TechlandScriptParser.FuntionCallDeclContext call1 && tree2 instanceof TechlandScriptParser.FuntionCallDeclContext call2) {
            // 如果文本完全相等，则没有差异
            if (treeEquals(tree1, tree2)) return diffs;

            // 比较参数列表
            TechlandScriptParser.ValueListContext v1 = call1.valueList();
            TechlandScriptParser.ValueListContext v2 = call2.valueList();
            List<TechlandScriptParser.ExpressionContext> exprs1 = v1 != null ? v1.expression() : new ArrayList<>();
            List<TechlandScriptParser.ExpressionContext> exprs2 = v2 != null ? v2.expression() : new ArrayList<>();

            int min = Math.min(exprs1.size(), exprs2.size());
            for (int i = 0; i < min; i++) {
                if (!treeEquals(exprs1.get(i), exprs2.get(i))) {
                    diffs.add(new DiffResult(exprs1.get(i), exprs2.get(i), "Expression", "Parameter value changed at index " + i));
                }
            }
            if (exprs1.size() < exprs2.size()) {
                for (int i = min; i < exprs2.size(); i++) {
                    diffs.add(new DiffResult(null, exprs2.get(i), "Expression", "Parameter added in Mod2 at index " + i));
                }
            } else if (exprs1.size() > exprs2.size()) {
                for (int i = min; i < exprs1.size(); i++) {
                    diffs.add(new DiffResult(exprs1.get(i), null, "Expression", "Parameter removed in Mod2 at index " + i));
                }
            }

            // 如果没有发现参数级别差异，则报告为整体修改
            if (diffs.isEmpty()) {
                diffs.add(new DiffResult(tree1, tree2, tree1.getClass().getSimpleName(), "Content differs"));
            }

            return diffs;
        }

        // 3. 默认情况：如果内容不相等，则直接报告差异
        if (!treeEquals(tree1, tree2)) {
            diffs.add(new DiffResult(tree1, tree2, tree1.getClass().getSimpleName(), "Content differs"));
        }

        return diffs;
    }

    /**
     * 比较两个函数块内部的语句。
     */
    private static List<DiffResult> compareFunctionBlocks(TechlandScriptParser.FunctionBlockContext block1, TechlandScriptParser.FunctionBlockContext block2) {
        List<DiffResult> diffs = new ArrayList<>();

        // 索引内部所有语句 (主要是函数调用)
        Map<String, TechlandScriptParser.FuntionCallDeclContext> calls1 = indexBy(
                block1.statements().stream().map(TechlandScriptParser.StatementsContext::funtionCallDecl).collect(Collectors.toList()),
                c -> {
                    if (c == null) return "";
                    int paramCount = c.valueList() != null ? c.valueList().expression().size() : 0;
                    return c.Id().getText() + "|" + paramCount;
                }
        );
        Map<String, TechlandScriptParser.FuntionCallDeclContext> calls2 = indexBy(
                block2.statements().stream().map(TechlandScriptParser.StatementsContext::funtionCallDecl).collect(Collectors.toList()),
                c -> {
                    if (c == null) return "";
                    int paramCount = c.valueList() != null ? c.valueList().expression().size() : 0;
                    return c.Id().getText() + "|" + paramCount;
                }
        );

        // 对比两个 map
        for (Map.Entry<String, TechlandScriptParser.FuntionCallDeclContext> entry : calls1.entrySet()) {
            String key = entry.getKey();
            TechlandScriptParser.FuntionCallDeclContext node1 = entry.getValue();

            if (!calls2.containsKey(key)) {
                diffs.add(new DiffResult(node1, null, node1.getClass().getSimpleName(), "Removed in Mod2: " + key));
            } else {
                TechlandScriptParser.FuntionCallDeclContext node2 = calls2.get(key);
                // 递归比较函数调用（参数级别）
                diffs.addAll(compareTrees(node1, node2));
            }
        }

        for (Map.Entry<String, TechlandScriptParser.FuntionCallDeclContext> entry : calls2.entrySet()) {
            String key = entry.getKey();
            if (!calls1.containsKey(key)) {
                TechlandScriptParser.FuntionCallDeclContext node2 = entry.getValue();
                diffs.add(new DiffResult(null, node2, node2.getClass().getSimpleName(), "Added in Mod2: " + key));
            }
        }

        return diffs;
    }

    /**
     * 通用的Map比较逻辑。
     */
    private static <T extends ParseTree> List<DiffResult> compareMaps(Map<String, T> map1, Map<String, T> map2) {
        List<DiffResult> diffs = new ArrayList<>();

        for (Map.Entry<String, T> entry : map1.entrySet()) {
            String key = entry.getKey();
            T node1 = entry.getValue();

            if (!map2.containsKey(key)) {
                diffs.add(new DiffResult(node1, null, node1.getClass().getSimpleName(), "Removed in Mod2: " + key));
            } else {
                T node2 = map2.get(key);
                // 递归比较
                diffs.addAll(compareTrees(node1, node2));
            }
        }

        for (Map.Entry<String, T> entry : map2.entrySet()) {
            String key = entry.getKey();
            if (!map1.containsKey(key)) {
                T node2 = entry.getValue();
                diffs.add(new DiffResult(null, node2, node2.getClass().getSimpleName(), "Added in Mod2: " + key));
            }
        }

        return diffs;
    }

    private static <T extends ParseTree> Map<String, T> indexBy(List<T> nodes, Function<T, String> keyExtractor) {
        Map<String, T> map = new LinkedHashMap<>();
        if (nodes == null) return map;
        for (T node : nodes) {
            if (node != null) {
                map.put(keyExtractor.apply(node), node);
            }
        }
        return map;
    }

    private static String extractDefinitionKey(TechlandScriptParser.DefinitionContext def) {
        if (def.importDecl() != null) return "import:" + def.importDecl().getText();
        if (def.exportDecl() != null) return "export:" + def.exportDecl().Id().getText();
        if (def.subDecl() != null) return "sub:" + def.subDecl().Id().getText();
        if (def.macroDecl() != null) return "macro:" + def.macroDecl().MacroId().getText();
        if (def.variableDecl() != null) return "var:" + def.variableDecl().Id().getText();
        if (def.directiveCall() != null) return "directive:" + def.directiveCall().Id().getText();
        if (def.funtionCallDecl() != null) return "call:" + def.funtionCallDecl().Id().getText();
        if (def.funtionBlockDecl() != null) return "block:" + def.funtionBlockDecl().Id().getText();
        return "unknown:" + def.getText();
    }

    public static boolean treeEquals(ParseTree tree1, ParseTree tree2) {
        if (tree1 == null && tree2 == null) return true;
        if (tree1 == null || tree2 == null) return false;
        return tree1.getText().trim().equals(tree2.getText().trim());
    }

    private static TerminalNode findFirstTerminal(ParseTree tree) {
        if (tree instanceof TerminalNode) return (TerminalNode) tree;
        for (int i = 0; i < tree.getChildCount(); i++) {
            TerminalNode found = findFirstTerminal(tree.getChild(i));
            if (found != null) return found;
        }
        return null;
    }

    public static int getLineNumber(ParseTree tree) {
        if (tree == null) return -1;
        TerminalNode firstTerminal = findFirstTerminal(tree);
        return firstTerminal != null ? firstTerminal.getSymbol().getLine() : -1;
    }
}
