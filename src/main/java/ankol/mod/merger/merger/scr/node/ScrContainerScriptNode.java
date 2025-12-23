package ankol.mod.merger.merger.scr.node;


import java.util.LinkedHashMap;
import java.util.Map;

public class ScrContainerScriptNode extends ScrScriptNode {
    /**
     * 子节点映射，key 是节点签名，value 是节点对象
     */
    private final Map<String, ScrScriptNode> children = new LinkedHashMap<>();

    /**
     * 构造函数
     *
     * @param signature
     * @param start
     * @param stop
     * @param text
     */
    public ScrContainerScriptNode(String signature, int start, int stop, int line, String text) {
        super(signature.trim(), start, stop, line, text);
    }

    public void addChild(ScrScriptNode node) {
        children.put(node.getSignature(), node);
    }

    public Map<String, ScrScriptNode> getChildren() {
        return children;
    }

    @Override
    public String toString() {
        return "ScrContainerScriptNode{" +
                "children=" + children +
                ", signature='" + signature + '\'' +
                ", startIndex=" + startIndex +
                ", stopIndex=" + stopIndex +
                ", line=" + line +
                ", sourceText='" + sourceText + '\'' +
                '}';
    }
}
