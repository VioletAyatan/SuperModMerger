package ankol.mod.merger.merger.xml;

import ankol.mod.merger.antlr.xml.TechlandXMLParser;
import ankol.mod.merger.antlr.xml.TechlandXMLParserBaseVisitor;
import ankol.mod.merger.merger.xml.node.XmlElementNode;
import ankol.mod.merger.merger.xml.node.XmlNode;

public class TechlandXmlFileVisitor extends TechlandXMLParserBaseVisitor<XmlNode> {
    public static final String ELEMENT = "element";

    @Override
    public XmlNode visitDocument(TechlandXMLParser.DocumentContext ctx) {
        return super.visitDocument(ctx);
    }

    @Override
    public XmlNode visitElement(TechlandXMLParser.ElementContext ctx) {
        var signature = ELEMENT + ctx.Name().getFirst().getText();

        return super.visitElement(ctx);
    }
}
