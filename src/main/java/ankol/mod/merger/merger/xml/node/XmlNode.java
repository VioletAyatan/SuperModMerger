package ankol.mod.merger.merger.xml.node;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public abstract class XmlNode {
    protected String signature;
    protected int startTokenIndex;
    protected int stopTokenIndex;
    protected int line; //行号
    protected String sourceText;

    public XmlNode(String signature, int startTokenIndex, int stopTokenIndex, int line, String sourceText) {
        this.signature = signature;
        this.startTokenIndex = startTokenIndex;
        this.stopTokenIndex = stopTokenIndex;
        this.line = line;
        this.sourceText = sourceText;
    }
}
