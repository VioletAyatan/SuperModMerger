package ankol.mod.merger.merger.xml.node;

/**
 * XML叶子节点
 * @author Ankol
 */
public class XmlLeafNode extends XmlNode {


    public XmlLeafNode(String signature, int startTokenIndex, int stopTokenIndex, int line, String sourceText) {
        super(signature, startTokenIndex, stopTokenIndex, line, sourceText);
    }
}

