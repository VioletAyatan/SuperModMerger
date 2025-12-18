package ankol.mod.merger.merger.scr;

import ankol.mod.merger.antlr4.scr.TechlandScriptParser;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.antlr.v4.runtime.misc.Interval;

import java.util.*;

/**
 * 脚本语法树对比器 - 从根节点完整遍历和对比两个脚本的AST语法树
 * <p>
 * 对比策略:
 * 1. 从 FileContext (根节点) 开始递归遍历
 * 2. 顶层定义按 key 进行分组对比（导出、子函数、变量等）
 * 3. 对于相同的顶层定义，继续递归对比其内容
 * 4. 对于函数块内的语句，按类型和内容分组对比
 * 5. 检测到参数不同时标记为冲突
 */
public class ScrTreeComparator {

    /**
     * 容器，记录diff代码
     */
    public static class DiffResult {
        public final ParseTree tree1;        // Mod1 中的节点
        public final ParseTree tree2;        // Mod2 中的节点
        public final String ruleName;        // 规则名称
        public final String description;     // 冲突描述
        public final int lineNumber1;        // Mod1 行号
        public final int lineNumber2;        // Mod2 行号
        public final int startToken1;
        public final int endToken1;
        public final int startToken2;
        public final int endToken2;
        public final DiffType diffType;      // 差异类型

        public enum DiffType {
            ADDED,       // Mod2 中新增
            REMOVED,     // Mod1 中被删除
            MODIFIED,    // 内容被修改
            CONFLICT     // 参数冲突
        }

        public DiffResult(ParseTree tree1, ParseTree tree2, String ruleName, String description, DiffType diffType) {
            this.tree1 = tree1;
            this.tree2 = tree2;
            this.ruleName = ruleName;
            this.description = description;
            this.diffType = diffType;
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
            String text1 = tree1 != null ? tree1.getText().substring(0, Math.min(50, tree1.getText().length())) : "null";
            String text2 = tree2 != null ? tree2.getText().substring(0, Math.min(50, tree2.getText().length())) : "null";
            return String.format("[%s] %s (Type: %s)\n  Mod1 (Line %d): %s\n  Mod2 (Line %d): %s",
                    ruleName, description, diffType, lineNumber1, text1, lineNumber2, text2);
        }
    }

    /**
     * 从文件根节点开始对比两个脚本
     */
    public static List<DiffResult> compareFiles(TechlandScriptParser.FileContext file1, TechlandScriptParser.FileContext file2) {
        List<DiffResult> allDiffs = new ArrayList<>();

        // 获取两个文件的顶层定义
        List<TechlandScriptParser.DefinitionContext> defs1 = file1.definition();
        List<TechlandScriptParser.DefinitionContext> defs2 = file2.definition();

        // 按定义 key 分组
        Map<String, TechlandScriptParser.DefinitionContext> defMap1 = indexDefinitionsBy(defs1);
        Map<String, TechlandScriptParser.DefinitionContext> defMap2 = indexDefinitionsBy(defs2);

        // 对比所有定义
        Set<String> allKeys = new LinkedHashSet<>();
        allKeys.addAll(defMap1.keySet());
        allKeys.addAll(defMap2.keySet());

        for (String key : allKeys) {
            TechlandScriptParser.DefinitionContext def1 = defMap1.get(key);
            TechlandScriptParser.DefinitionContext def2 = defMap2.get(key);

            if (def1 != null && def2 == null) {
                // Mod1 中有，Mod2 中没有 - 被删除
                allDiffs.add(new DiffResult(def1, null, def1.getClass().getSimpleName(),
                        "Removed in Mod2: " + key, DiffResult.DiffType.REMOVED));
            } else if (def1 == null && def2 != null) {
                // Mod2 中有，Mod1 中没有 - 新增
                allDiffs.add(new DiffResult(null, def2, def2.getClass().getSimpleName(),
                        "Added in Mod2: " + key, DiffResult.DiffType.ADDED));
            } else {
                // 两个都有 - 递归对比
                List<DiffResult> subDiffs = compareDefinitions(def1, def2);
                allDiffs.addAll(subDiffs);
            }
        }

        return allDiffs;
    }

    /**
     * 对比两个定义节点
     */
    private static List<DiffResult> compareDefinitions(TechlandScriptParser.DefinitionContext def1, TechlandScriptParser.DefinitionContext def2) {
        List<DiffResult> diffs = new ArrayList<>();

        // 如果是子函数声明
        if (def1.subDecl() != null && def2.subDecl() != null) {
            TechlandScriptParser.SubDeclContext sub1 = def1.subDecl();
            TechlandScriptParser.SubDeclContext sub2 = def2.subDecl();

            // 对比函数块
            List<DiffResult> blockDiffs = compareFunctionBlocks(sub1.functionBlock(), sub2.functionBlock());
            diffs.addAll(blockDiffs);
        }
        // 如果是函数块声明 (如 Item, Jump 等)
        else if (def1.funtionBlockDecl() != null && def2.funtionBlockDecl() != null) {
            TechlandScriptParser.FuntionBlockDeclContext block1 = def1.funtionBlockDecl();
            TechlandScriptParser.FuntionBlockDeclContext block2 = def2.funtionBlockDecl();

            // 先对比函数名和参数
            if (!treeEquals(block1.Id(), block2.Id())) {
                diffs.add(new DiffResult(block1, block2, "FunctionName",
                        "Function name differs", DiffResult.DiffType.CONFLICT));
            }

            // 对比参数列表
            TechlandScriptParser.ValueListContext params1 = block1.valueList();
            TechlandScriptParser.ValueListContext params2 = block2.valueList();
            List<DiffResult> paramDiffs = compareParameterLists(params1, params2, "Function parameters");
            diffs.addAll(paramDiffs);

            // 对比函数块内容
            List<DiffResult> blockContentDiffs = compareFunctionBlocks(block1.functionBlock(), block2.functionBlock());
            diffs.addAll(blockContentDiffs);
        }
        // 其他定义类型的对比
        else if (!treeEquals(def1, def2)) {
            diffs.add(new DiffResult(def1, def2, def1.getClass().getSimpleName(),
                    "Definition content differs", DiffResult.DiffType.MODIFIED));
        }

        return diffs;
    }

    /**
     * 对比两个函数块内的语句
     */
    private static List<DiffResult> compareFunctionBlocks(TechlandScriptParser.FunctionBlockContext block1, TechlandScriptParser.FunctionBlockContext block2) {
        List<DiffResult> diffs = new ArrayList<>();

        // 获取两个块内的所有语句
        List<TechlandScriptParser.StatementsContext> stmts1 = block1.statements();
        List<TechlandScriptParser.StatementsContext> stmts2 = block2.statements();

        // 按函数调用分组
        Map<String, TechlandScriptParser.StatementsContext> stmtMap1 = indexStatementsBy(stmts1);
        Map<String, TechlandScriptParser.StatementsContext> stmtMap2 = indexStatementsBy(stmts2);

        // 获取所有出现过的 key
        Set<String> allKeys = new LinkedHashSet<>();
        allKeys.addAll(stmtMap1.keySet());
        allKeys.addAll(stmtMap2.keySet());

        for (String key : allKeys) {
            TechlandScriptParser.StatementsContext stmt1 = stmtMap1.get(key);
            TechlandScriptParser.StatementsContext stmt2 = stmtMap2.get(key);

            if (stmt1 != null && stmt2 == null) {
                // 在 Mod1 中存在，但在 Mod2 中被删除
                diffs.add(new DiffResult(stmt1, null, "Statement",
                        "Statement removed in Mod2: " + key, DiffResult.DiffType.REMOVED));
            } else if (stmt1 == null && stmt2 != null) {
                // 在 Mod2 中新增
                diffs.add(new DiffResult(null, stmt2, "Statement",
                        "Statement added in Mod2: " + key, DiffResult.DiffType.ADDED));
            } else {
                // 两个都有 - 比较内容
                List<DiffResult> stmtDiffs = compareStatements(stmt1, stmt2);
                diffs.addAll(stmtDiffs);
            }
        }

        return diffs;
    }

    /**
     * 对比两个语句
     */
    private static List<DiffResult> compareStatements(TechlandScriptParser.StatementsContext stmt1, TechlandScriptParser.StatementsContext stmt2) {
        List<DiffResult> diffs = new ArrayList<>();

        // 如果都是函数块声明
        if (stmt1.funtionBlockDecl() != null && stmt2.funtionBlockDecl() != null) {
            TechlandScriptParser.FuntionBlockDeclContext block1 = stmt1.funtionBlockDecl();
            TechlandScriptParser.FuntionBlockDeclContext block2 = stmt2.funtionBlockDecl();

            // 检查函数名是否相同
            if (!treeEquals(block1.Id(), block2.Id())) {
                diffs.add(new DiffResult(block1, block2, "BlockName",
                        "Block name differs", DiffResult.DiffType.CONFLICT));
                return diffs;
            }

            // 对比参数列表
            TechlandScriptParser.ValueListContext params1 = block1.valueList();
            TechlandScriptParser.ValueListContext params2 = block2.valueList();
            List<DiffResult> paramDiffs = compareParameterLists(params1, params2, "Block parameters");
            diffs.addAll(paramDiffs);

            // 递归对比块内容
            List<DiffResult> blockDiffs = compareFunctionBlocks(block1.functionBlock(), block2.functionBlock());
            diffs.addAll(blockDiffs);
        }
        // 如果都是函数调用
        else if (stmt1.funtionCallDecl() != null && stmt2.funtionCallDecl() != null) {
            TechlandScriptParser.FuntionCallDeclContext call1 = stmt1.funtionCallDecl();
            TechlandScriptParser.FuntionCallDeclContext call2 = stmt2.funtionCallDecl();

            // 检查函数名是否相同
            if (!treeEquals(call1.Id(), call2.Id())) {
                diffs.add(new DiffResult(call1, call2, "FunctionCall",
                        "Function name differs", DiffResult.DiffType.CONFLICT));
                return diffs;
            }

            // 对比参数列表
            TechlandScriptParser.ValueListContext params1 = call1.valueList();
            TechlandScriptParser.ValueListContext params2 = call2.valueList();
            List<DiffResult> paramDiffs = compareParameterLists(params1, params2,
                    "Function call parameters: " + call1.Id().getText());
            diffs.addAll(paramDiffs);
        }
        // 其他语句类型
        else if (!treeEquals(stmt1, stmt2)) {
            diffs.add(new DiffResult(stmt1, stmt2, "Statement",
                    "Statement content differs", DiffResult.DiffType.MODIFIED));
        }

        return diffs;
    }

    /**
     * 对比参数列表
     */
    private static List<DiffResult> compareParameterLists(TechlandScriptParser.ValueListContext params1, TechlandScriptParser.ValueListContext params2, String context) {
        List<DiffResult> diffs = new ArrayList<>();

        List<TechlandScriptParser.ExpressionContext> exprs1 = params1 != null ? params1.expression() : new ArrayList<>();
        List<TechlandScriptParser.ExpressionContext> exprs2 = params2 != null ? params2.expression() : new ArrayList<>();

        int min = Math.min(exprs1.size(), exprs2.size());

        // 对比相同位置的参数
        for (int i = 0; i < min; i++) {
            TechlandScriptParser.ExpressionContext expr1 = exprs1.get(i);
            TechlandScriptParser.ExpressionContext expr2 = exprs2.get(i);

            if (!treeEquals(expr1, expr2)) {
                diffs.add(new DiffResult(expr1, expr2, "Expression",
                        context + " parameter #" + (i + 1) + " differs", DiffResult.DiffType.CONFLICT));
            }
        }

        // 处理参数数量差异
        if (exprs1.size() < exprs2.size()) {
            // Mod2 有更多参数
            for (int i = min; i < exprs2.size(); i++) {
                diffs.add(new DiffResult(null, exprs2.get(i), "Expression",
                        context + " parameter #" + (i + 1) + " added in Mod2", DiffResult.DiffType.ADDED));
            }
        } else if (exprs1.size() > exprs2.size()) {
            // Mod1 有更多参数
            for (int i = min; i < exprs1.size(); i++) {
                diffs.add(new DiffResult(exprs1.get(i), null, "Expression",
                        context + " parameter #" + (i + 1) + " removed in Mod2", DiffResult.DiffType.REMOVED));
            }
        }

        return diffs;
    }

    /**
     * 按定义分组索引（从根节点遍历所有定义）
     */
    private static Map<String, TechlandScriptParser.DefinitionContext> indexDefinitionsBy(List<TechlandScriptParser.DefinitionContext> defs) {
        Map<String, TechlandScriptParser.DefinitionContext> map = new LinkedHashMap<>();
        if (defs == null) return map;

        for (TechlandScriptParser.DefinitionContext def : defs) {
            if (def != null) {
                String key = extractDefinitionKey(def);
                // 同一个 key 可能出现多次，保留最后一个
                map.put(key, def);
            }
        }
        return map;
    }

    /**
     * 按语句分组索引
     */
    private static Map<String, TechlandScriptParser.StatementsContext> indexStatementsBy(List<TechlandScriptParser.StatementsContext> stmts) {
        Map<String, TechlandScriptParser.StatementsContext> map = new LinkedHashMap<>();
        if (stmts == null) return map;

        for (TechlandScriptParser.StatementsContext stmt : stmts) {
            if (stmt != null) {
                String key = extractStatementKey(stmt);
                // 同一个 key 可能出现多次，保留最后一个
                map.put(key, stmt);
            }
        }
        return map;
    }

    /**
     * 提取定义的唯一标识 key
     */
    private static String extractDefinitionKey(TechlandScriptParser.DefinitionContext def) {
        if (def.importDecl() != null) {
            return "import:" + def.importDecl().getText();
        }
        if (def.exportDecl() != null) {
            return "export:" + def.exportDecl().Id().getText();
        }
        if (def.subDecl() != null) {
            return "sub:" + def.subDecl().Id().getText();
        }
        if (def.macroDecl() != null) {
            return "macro:" + def.macroDecl().MacroId().getText();
        }
        if (def.variableDecl() != null) {
            return "var:" + def.variableDecl().Id().getText();
        }
        if (def.directiveCall() != null) {
            return "directive:" + def.directiveCall().Id().getText();
        }
        if (def.funtionCallDecl() != null) {
            return "call:" + def.funtionCallDecl().Id().getText();
        }
        if (def.funtionBlockDecl() != null) {
            // 使用函数名+参数个数作为 key（同名函数可能有不同参数）
            TechlandScriptParser.FuntionBlockDeclContext block = def.funtionBlockDecl();
            int paramCount = block.valueList() != null ? block.valueList().expression().size() : 0;
            return "block:" + block.Id().getText() + "|" + paramCount;
        }
        return "unknown:" + def.getText();
    }

    /**
     * 提取语句的唯一标识 key
     */
    private static String extractStatementKey(TechlandScriptParser.StatementsContext stmt) {
        if (stmt.funtionBlockDecl() != null) {
            // 函数块：使用函数名+参数个数
            TechlandScriptParser.FuntionBlockDeclContext block = stmt.funtionBlockDecl();
            int paramCount = block.valueList() != null ? block.valueList().expression().size() : 0;
            return "block:" + block.Id().getText() + "|" + paramCount;
        }
        if (stmt.funtionCallDecl() != null) {
            // 函数调用：使用函数名+参数个数
            TechlandScriptParser.FuntionCallDeclContext call = stmt.funtionCallDecl();
            int paramCount = call.valueList() != null ? call.valueList().expression().size() : 0;
            return "call:" + call.Id().getText() + "|" + paramCount;
        }
        if (stmt.variableDecl() != null) {
            return "var:" + stmt.variableDecl().Id().getText();
        }
        if (stmt.useDecl() != null) {
            return "use:" + stmt.useDecl().Id().getText();
        }
        return "stmt:" + stmt.getText().substring(0, Math.min(20, stmt.getText().length()));
    }

    /**
     * 比较两个树节点的文本内容是否相等
     */
    public static boolean treeEquals(ParseTree tree1, ParseTree tree2) {
        if (tree1 == null && tree2 == null) return true;
        if (tree1 == null || tree2 == null) return false;
        return tree1.getText().trim().equals(tree2.getText().trim());
    }

    /**
     * 获取树节点的行号
     */
    private static TerminalNode findFirstTerminal(ParseTree tree) {
        if (tree instanceof TerminalNode) return (TerminalNode) tree;
        for (int i = 0; i < tree.getChildCount(); i++) {
            TerminalNode found = findFirstTerminal(tree.getChild(i));
            if (found != null) return found;
        }
        return null;
    }

    /**
     * 获取树节点所在的行号
     */
    public static int getLineNumber(ParseTree tree) {
        if (tree == null) return -1;
        TerminalNode firstTerminal = findFirstTerminal(tree);
        return firstTerminal != null ? firstTerminal.getSymbol().getLine() : -1;
    }
}

