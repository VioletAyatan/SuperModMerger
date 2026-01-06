package ankol.mod.merger.merger.scr.node;


import ankol.mod.merger.core.BaseTreeNode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.antlr.v4.runtime.CommonTokenStream;

import java.util.LinkedHashMap;
import java.util.Map;

@Getter
@Setter
@ToString
public class ScrContainerScriptNode extends BaseTreeNode {
    /**
     * 子节点映射，key 是节点签名，value 是节点对象
     */
    private final Map<String, BaseTreeNode> children = new LinkedHashMap<>();

    public ScrContainerScriptNode(String signature, int startTokenIndex, int stopTokenIndex, int line, CommonTokenStream tokenStream) {
        super(signature.trim(), startTokenIndex, stopTokenIndex, line, tokenStream);
    }

    public void addChild(BaseTreeNode node) {
        children.put(node.getSignature(), node);
    }

}
