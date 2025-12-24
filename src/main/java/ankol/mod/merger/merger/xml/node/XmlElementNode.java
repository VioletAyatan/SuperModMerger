package ankol.mod.merger.merger.xml.node;

/**
 * XML element节点
 *
 * @author Ankol
 */
public class XmlElementNode extends XmlNode {

    public XmlElementNode(String signature, int startTokenIndex, int stopTokenIndex, int line, String sourceText) {
        super(signature, startTokenIndex, stopTokenIndex, line, sourceText);
    }
}
