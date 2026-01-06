package ankol.mod.merger.merger.xml.node;

import ankol.mod.merger.core.BaseTreeNode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.antlr.v4.runtime.CommonTokenStream;

import java.util.Map;

@Setter
@Getter
@ToString
public abstract class XmlNode extends BaseTreeNode {
    protected Map<String, String> attributes;

    public XmlNode(String signature, int startTokenIndex, int stopTokenIndex, int line, CommonTokenStream tokenStream, Map<String, String> attributes) {
        super(signature, startTokenIndex, stopTokenIndex, line, tokenStream);
        this.attributes = attributes;
    }
}
