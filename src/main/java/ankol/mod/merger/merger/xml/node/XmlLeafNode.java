package ankol.mod.merger.merger.xml.node;

/**
 * XML leaf node - represents XML tags without child elements
 * Example: <effect id="..." change="..." /> self-closing element
 *
 * @author Ankol
 */
public class XmlLeafNode extends XmlNode {

    /**
     * Constructor
     *
     * @param signature        node signature (unique identifier)
     * @param startTokenIndex  starting token index in TokenStream
     * @param stopTokenIndex   ending token index in TokenStream
     * @param line             line number
     * @param sourceText       original source text
     */
    public XmlLeafNode(String signature, int startTokenIndex, int stopTokenIndex, int line, String sourceText) {
        super(signature, startTokenIndex, stopTokenIndex, line, sourceText);
    }
}

