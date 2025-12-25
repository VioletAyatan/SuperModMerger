package ankol.mod.merger.merger.xml.node;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * XML容器节点，标识可以有子节点的容器
 * 例如：<skills> ... </skills>
 *
 * @author Ankol
 */
@Getter
@Setter
@ToString
public class XmlContainerNode extends XmlNode {

    private final Map<String, XmlNode> children = new LinkedHashMap<>();

    public XmlContainerNode(String signature, int startTokenIndex, int stopTokenIndex, int line, String sourceText) {
        super(signature.trim(), startTokenIndex, stopTokenIndex, line, sourceText);
    }

    /**
     * 添加子节点
     */
    public void addChild(XmlNode node) {
        children.put(node.getSignature(), node);
    }
}

