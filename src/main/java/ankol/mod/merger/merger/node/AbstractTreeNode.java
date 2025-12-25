package ankol.mod.merger.merger.node;

public abstract class AbstractTreeNode {
    /**
     * 当前树节点起始TOKEN索引
     * Antlr解析后的AST树节点
     */
    protected int startTokenIndex;
    /**
     * 当前树节点结束TOKEN索引
     * Antlr解析后的AST树节点
     */
    protected int endTokenIndex;
    /**
     * 当前代码行号
     */
    protected int lineNumber;
}
