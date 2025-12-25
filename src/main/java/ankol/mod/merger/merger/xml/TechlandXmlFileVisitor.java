package ankol.mod.merger.merger.xml;

import ankol.mod.merger.antlr.xml.TechlandXMLParser;
import ankol.mod.merger.antlr.xml.TechlandXMLParserBaseVisitor;
import ankol.mod.merger.merger.xml.node.XmlContainerNode;
import ankol.mod.merger.merger.xml.node.XmlLeafNode;
import ankol.mod.merger.merger.xml.node.XmlNode;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.misc.Interval;

import java.util.List;

/**
 * XML文件访问器 - 用于从ANTLR解析树构建XML AST树
 *
 * @author Ankol
 */
public class TechlandXmlFileVisitor extends TechlandXMLParserBaseVisitor<XmlNode> {
    public static final String ELEMENT = "element";

    /**
     * 获取context的起始token索引
     */
    private int getStartTokenIndex(ParserRuleContext ctx) {
        return ctx.start.getTokenIndex();
    }

    /**
     * 获取context的结束token索引
     */
    private int getStopTokenIndex(ParserRuleContext ctx) {
        return ctx.stop.getTokenIndex();
    }

    /**
     * 获取context的完整原始文本
     */
    private String getFullText(ParserRuleContext ctx) {
        int startIdx = ctx.start.getStartIndex();
        int stopIdx = ctx.stop.getStopIndex();
        String sourceInterval = ctx.start.getInputStream().getText(Interval.of(startIdx, stopIdx));
        return sourceInterval == null ? ctx.getText() : sourceInterval;
    }

    /**
     * 从attribute列表中提取id值
     */
    private String extractIdAttribute(List<TechlandXMLParser.AttributeContext> attributes) {
        if (attributes == null) {
            return null;
        }
        for (TechlandXMLParser.AttributeContext attr : attributes) {
            String name = attr.Name().getText();
            if ("id".equals(name)) {
                String value = attr.STRING().getText();
                // 移除引号
                return value.replaceAll("\"", "").replaceAll("'", "");
            }
        }
        return null;
    }

    /**
     * 构建element签名
     * 格式: element:tagName:idValue (如果有id属性)
     * 或: element:tagName:index (如果没有id属性，使用位置索引)
     */
    private String buildSignature(String tagName, String idValue, int index) {
        if (idValue != null && !idValue.isEmpty()) {
            return ELEMENT + ":" + tagName + ":" + idValue;
        } else {
            return ELEMENT + ":" + tagName + ":" + index;
        }
    }

    /**
     * 访问document - 创建根节点
     */
    @Override
    public XmlNode visitDocument(TechlandXMLParser.DocumentContext ctx) {
        XmlContainerNode rootNode = new XmlContainerNode("ROOT",
                getStartTokenIndex(ctx),
                getStopTokenIndex(ctx),
                ctx.start.getLine(),
                getFullText(ctx)
        );

        List<TechlandXMLParser.ElementContext> elements = ctx.element();
        int index = 0;
        for (TechlandXMLParser.ElementContext eleCtx : elements) {
            rootNode.addChild(visitElement(eleCtx, index++));
        }
        return rootNode;
    }

    /**
     * 访问element - 创建element节点
     */
    @Override
    public XmlNode visitElement(TechlandXMLParser.ElementContext ctx) {
        return visitElement(ctx, 0);
    }

    /**
     * 内部方法：访问element并指定索引
     */
    private XmlNode visitElement(TechlandXMLParser.ElementContext ctx, int index) {
        String tagName = ctx.Name().getFirst().getText();
        List<TechlandXMLParser.AttributeContext> attributes = ctx.attribute(); //xml属性 <Skill id="xxx" cat="xxx"> 这种
        String idValue = extractIdAttribute(attributes);
        String signature = buildSignature(tagName, idValue, index);

        TechlandXMLParser.ContentContext content = ctx.content();

        // 判断是否为容器节点（有子元素）
        if (content != null && !content.element().isEmpty()) {
            XmlContainerNode containerNode = new XmlContainerNode(
                    signature,
                    getStartTokenIndex(ctx),
                    getStopTokenIndex(ctx),
                    ctx.start.getLine(),
                    getFullText(ctx)
            );

            // 添加子元素
            List<TechlandXMLParser.ElementContext> childElements = content.element();
            int childIndex = 0;
            for (TechlandXMLParser.ElementContext childCtx : childElements) {
                XmlNode childNode = visitElement(childCtx, childIndex++);
                containerNode.addChild(childNode);
            }
            return containerNode;
        } else {
            // 叶子节点（没有子元素或为自闭合标签）
            return new XmlLeafNode(
                    signature,
                    getStartTokenIndex(ctx),
                    getStopTokenIndex(ctx),
                    ctx.start.getLine(),
                    getFullText(ctx)
            );
        }
    }
}
