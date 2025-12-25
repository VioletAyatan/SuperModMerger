package ankol.mod.merger.merger.xml.node;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Map;

/**
 * XML叶子节点
 *
 * @author Ankol
 */
@Getter
@Setter
@ToString
public class XmlLeafNode extends XmlNode {

    public XmlLeafNode(String signature, int startTokenIndex, int stopTokenIndex, int line, String sourceText, Map<String, String> attributes) {
        super(signature, startTokenIndex, stopTokenIndex, line, sourceText, attributes);
    }
}

