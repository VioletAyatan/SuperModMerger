package ankol.mod.merger.merger.xml.node;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * XML container node - represents XML tags with child elements
 * Example: <skill>...</skill>
 *
 * @author Ankol
 */
@Getter
@Setter
@ToString
public class XmlContainerNode extends XmlNode {
    /**
     * Child node mapping, key is node signature, value is node object
     */
    private final Map<String, XmlNode> children = new LinkedHashMap<>();

    /**
     * Constructor
     *
     * @param signature        node signature (unique identifier)
     * @param startTokenIndex  starting token index in TokenStream
     * @param stopTokenIndex   ending token index in TokenStream
     * @param line             line number
     * @param sourceText       original source text
     */
    public XmlContainerNode(String signature, int startTokenIndex, int stopTokenIndex, int line, String sourceText) {
        super(signature.trim(), startTokenIndex, stopTokenIndex, line, sourceText);
    }

    /**
     * Add child node
     */
    public void addChild(XmlNode node) {
        children.put(node.getSignature(), node);
    }

    /**
     * Get all child nodes
     */
    public Map<String, XmlNode> getChildren() {
        return children;
    }
}

