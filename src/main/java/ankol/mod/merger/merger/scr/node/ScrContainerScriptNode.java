package ankol.mod.merger.merger.scr.node;


import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.LinkedHashMap;
import java.util.Map;

@Getter
@Setter
@ToString
public class ScrContainerScriptNode extends ScrScriptNode {
    /**
     * 子节点映射，key 是节点签名，value 是节点对象
     */
    private final Map<String, ScrScriptNode> children = new LinkedHashMap<>();

    /**
     * 构造函数
     *
     * @param signature
     * @param startTokenIndex
     * @param stopTokenIndex
     * @param line
     * @param text
     */
    public ScrContainerScriptNode(String signature, int startTokenIndex, int stopTokenIndex, int line, String text) {
        super(signature.trim(), startTokenIndex, stopTokenIndex, line, text);
    }

    public void addChild(ScrScriptNode node) {
        children.put(node.getSignature(), node);
    }

}
