package ankol.mod.merger.core;

import org.antlr.v4.runtime.misc.Interval;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;

public class DiffResult {

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

    /**
     * 获取树节点所在的行号
     */
    public static int getLineNumber(ParseTree tree) {
        if (tree == null) return -1;
        TerminalNode firstTerminal = findFirstTerminal(tree);
        return firstTerminal != null ? firstTerminal.getSymbol().getLine() : -1;
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
}
