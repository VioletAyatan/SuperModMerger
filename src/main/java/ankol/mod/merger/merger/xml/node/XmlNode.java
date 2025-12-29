package ankol.mod.merger.merger.xml.node;

import ankol.mod.merger.core.BaseTreeNode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Map;

@Setter
@Getter
@ToString
public abstract class XmlNode extends BaseTreeNode {
    protected Map<String, String> attributes;

    public XmlNode(String signature, int startTokenIndex, int stopTokenIndex, int line, String sourceText, Map<String, String> attributes) {
        super(signature, startTokenIndex, stopTokenIndex, line, sourceText);
        this.attributes = attributes;
    }
}
