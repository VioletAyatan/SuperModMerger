package ankol.mod.merger.merger;

import ankol.mod.merger.antlr4.scr.TechlandScriptParser;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;

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

        public DiffResult(ParseTree tree1, ParseTree tree2, String ruleName, String description) {
            this.tree1 = tree1;
            this.tree2 = tree2;
            this.ruleName = ruleName;
            this.description = description;
            this.lineNumber1 = getLineNumber(tree1);
            this.lineNumber2 = getLineNumber(tree2);
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
        if (tree1 instanceof TechlandScriptParser.FuntionBlockDeclContext && tree2 instanceof TechlandScriptParser.FuntionBlockDeclContext) {
            TechlandScriptParser.FuntionBlockDeclContext block1 = (TechlandScriptParser.FuntionBlockDeclContext) tree1;
            TechlandScriptParser.FuntionBlockDeclContext block2 = (TechlandScriptParser.FuntionBlockDeclContext) tree2;

            // 比较块内部的语句
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
        // 索引内部所有语句 (主要是函数调用)
        Map<String, TechlandScriptParser.FuntionCallDeclContext> calls1 = indexBy(
                block1.statements().stream().map(TechlandScriptParser.StatementsContext::funtionCallDecl).collect(Collectors.toList()),
                c -> c.Id().getText()
        );
        Map<String, TechlandScriptParser.FuntionCallDeclContext> calls2 = indexBy(
                block2.statements().stream().map(TechlandScriptParser.StatementsContext::funtionCallDecl).collect(Collectors.toList()),
                c -> c.Id().getText()
        );

        return compareMaps(calls1, calls2);
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
