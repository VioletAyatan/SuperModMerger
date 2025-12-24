package ankol.mod.merger.merger.xml;

import ankol.mod.merger.antlr.xml.TechlandXMLParser;
import ankol.mod.merger.antlr.xml.TechlandXMLParserBaseVisitor;
import ankol.mod.merger.merger.xml.node.XmlNode;
import org.antlr.v4.runtime.RuleContext;

import java.util.List;
import java.util.stream.Collectors;

public class TechlandXmlFileVisitor extends TechlandXMLParserBaseVisitor<XmlNode> {
    public static final String ELEMENT = "element";

    @Override
    public XmlNode visitDocument(TechlandXMLParser.DocumentContext ctx) {
        return super.visitDocument(ctx);
    }

    @Override
    public XmlNode visitElement(TechlandXMLParser.ElementContext ctx) {
        var signature = ELEMENT + ":" + ctx.Name().getFirst().getText();
        List<TechlandXMLParser.AttributeContext> attribute = ctx.attribute();
        if (attribute != null) {
            String attributeSignature = attribute.stream().map(RuleContext::getText).collect(Collectors.joining(":"));
            signature += ":" + attributeSignature;
        } else {
            System.out.println("attribute = " + attribute);
        }
        TechlandXMLParser.ContentContext content = ctx.content();
        if (content != null) {
            List<TechlandXMLParser.ElementContext> element = content.element();
        } else {
            System.out.println("signature = " + signature);
        }
        return super.visitElement(ctx);
    }
}
