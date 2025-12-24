package ankol.mod.merger.merger.scr.node;

/**
 * 叶子节点
 */
public class ScrLeafScriptNode extends ScrScriptNode {

    public ScrLeafScriptNode(String signature, int startTokenIndex, int stopTokenIndex, int line, String text) {
        super(signature, startTokenIndex, stopTokenIndex, line, text);
    }
}
