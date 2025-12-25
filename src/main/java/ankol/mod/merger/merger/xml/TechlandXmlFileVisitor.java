package ankol.mod.merger.merger.xml;

import ankol.mod.merger.antlr.xml.TechlandXMLParser;
import ankol.mod.merger.antlr.xml.TechlandXMLParserBaseVisitor;
import ankol.mod.merger.merger.xml.node.XmlContainerNode;
import ankol.mod.merger.merger.xml.node.XmlLeafNode;
import ankol.mod.merger.merger.xml.node.XmlNode;
import cn.hutool.core.collection.CollUtil;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.misc.Interval;

import java.util.Comparator;
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
     * 优先级属性名称列表 - 用于识别元素的主要标识符
     */
    private static final String[] PRIORITY_ATTRIBUTE_NAMES = {
            "id", "uid", "name", "key", "Name", "type", "property_id", "class"
    };

    /**
     * 从attribute列表中提取识别性属性
     * 返回格式: "attrName=attrValue" 或 null
     */
    private String extractIdentifyingAttribute(List<TechlandXMLParser.AttributeContext> attributes) {
        if (CollUtil.isEmpty(attributes)) {
            return null;
        }

        // 1. 先尝试按优先级查找属性
        for (String priorityName : PRIORITY_ATTRIBUTE_NAMES) {
            for (TechlandXMLParser.AttributeContext attr : attributes) {
                String attrName = attr.Name().getText();
                if (priorityName.equals(attrName)) {
                    String value = attr.STRING().getText();
                    // 移除引号
                    value = value.replaceAll("^\"|\"$|^'|'$", "");
                    return attrName + "=" + value;
                }
            }
        }

        // 如果没有找到优先级属性，使用所有属性组合
        // 按属性名排序以保证一致性
        StringBuilder sb = new StringBuilder();
        attributes.stream()
                .sorted(Comparator.comparing(a -> a.Name().getText()))
                .forEach(attr -> {
                    String attrName = attr.Name().getText();
                    String value = attr.STRING().getText();
                    value = value.replaceAll("^\"|\"$|^'|'$", "");
                    if (!sb.isEmpty()) {
                        sb.append(",");
                    }
                    sb.append(attrName).append("=").append(value);
                });

        return !sb.isEmpty() ? sb.toString() : null;
    }

    /**
     * 构建element签名
     */
    private String buildSignature(String tagName, String identifyingAttr, int index) {
        if (identifyingAttr != null && !identifyingAttr.isEmpty()) {
            return ELEMENT + ":" + tagName + ":" + identifyingAttr;
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
        List<TechlandXMLParser.AttributeContext> attributes = ctx.attribute();
        String identifyingAttr = extractIdentifyingAttribute(attributes);
        String signature = buildSignature(tagName, identifyingAttr, index);

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
