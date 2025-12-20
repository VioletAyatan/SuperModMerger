package ankol.mod.merger.merger.scr.news.node;

import lombok.Data;

import java.util.LinkedHashMap;
import java.util.Map;

@Data
public class ScrContainerNode extends ScrNode {
    /**
     * 子节点映射，key 是节点签名，value 是节点对象
     */
    private Map<String, ScrNode> children = new LinkedHashMap<>();

    /**
     * 构造函数
     *
     * @param signature
     * @param start
     * @param stop
     * @param text
     */
    public ScrContainerNode(String signature, int start, int stop, int line, String text) {
        super(signature, start, stop, line, text);
    }

    public void addChild(ScrNode node) {
        children.put(node.getSignature(), node);
    }

}
