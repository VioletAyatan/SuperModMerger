package ankol.mod.merger.merger.scr.node;

import ankol.mod.merger.core.BaseTreeNode;

/**
 * 叶子节点
 */
public class ScrLeafScriptNode extends BaseTreeNode {

    public ScrLeafScriptNode(String signature, int startTokenIndex, int stopTokenIndex, int line, String text) {
        super(signature, startTokenIndex, stopTokenIndex, line, text);
    }
}
