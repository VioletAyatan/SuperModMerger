package ankol.mod.merger.merger.scr.node;

import ankol.mod.merger.core.BaseTreeNode;
import org.antlr.v4.runtime.CommonTokenStream;

/**
 * 叶子节点
 */
public class ScrLeafScriptNode extends BaseTreeNode {

    public ScrLeafScriptNode(String signature, int startTokenIndex, int stopTokenIndex, int line, CommonTokenStream tokenStream) {
        super(signature, startTokenIndex, stopTokenIndex, line, tokenStream);
    }
}
