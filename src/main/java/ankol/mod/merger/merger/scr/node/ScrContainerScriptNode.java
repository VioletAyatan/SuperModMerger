package ankol.mod.merger.merger.scr.node;

import lombok.Data;

import java.util.LinkedHashMap;
import java.util.Map;

@Data
public class ScrContainerScriptNode extends ScrScriptNode {
    /**
     * 子节点映射，key 是节点签名，value 是节点对象
     */
    private Map<String, ScrScriptNode> children = new LinkedHashMap<>();

    /**
     * 构造函数
     *
     * @param signature
     * @param start
     * @param stop
     * @param text
     */
    public ScrContainerScriptNode(String signature, int start, int stop, int line, String text) {
        super(signature, start, stop, line, text);
    }

    public void addChild(ScrScriptNode node) {
        children.put(node.getSignature(), node);
    }

}
