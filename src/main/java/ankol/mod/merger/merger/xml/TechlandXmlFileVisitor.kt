package ankol.mod.merger.merger.xml

import ankol.mod.merger.antlr.xml.TechlandXMLParser
import ankol.mod.merger.antlr.xml.TechlandXMLParser.DocumentContext
import ankol.mod.merger.antlr.xml.TechlandXMLParser.ElementContext
import ankol.mod.merger.antlr.xml.TechlandXMLParserBaseVisitor
import ankol.mod.merger.merger.xml.node.XmlContainerNode
import ankol.mod.merger.merger.xml.node.XmlLeafNode
import ankol.mod.merger.merger.xml.node.XmlNode
import cn.hutool.core.collection.CollUtil
import org.antlr.v4.runtime.CommonTokenStream
import org.antlr.v4.runtime.ParserRuleContext
import java.util.function.Function

/**
 * XML文件访问器 - 用于从ANTLR解析树构建XML AST树
 *
 * @param tokenStream Token流引用（用于按需提取源文本）
 * @author Ankol
 */
class TechlandXmlFileVisitor(val tokenStream: CommonTokenStream) : TechlandXMLParserBaseVisitor<XmlNode>() {
    companion object {
        const val ELEMENT: String = "element"

        /**
         * 优先级属性名称列表 - 用于识别元素的主要标识符
         */
        private val PRIORITY_ATTRIBUTE_NAMES = arrayOf<String?>(
            "id", "uid", "name", "key", "Name", "type", "property_id", "class"
        )
    }

    /**
     * 访问document - 创建根节点
     */
    override fun visitDocument(ctx: DocumentContext): XmlNode {
        val rootNode = XmlContainerNode(
            "ROOT",
            getStartTokenIndex(ctx),
            getStopTokenIndex(ctx),
            ctx.start.line,
            tokenStream,
            mutableMapOf()
        )

        val elements = ctx.element()
        var index = 0
        for (eleCtx in elements) {
            rootNode.addChild(visitElement(eleCtx, index++))
        }
        return rootNode
    }

    /**
     * 访问element - 创建element节点
     */
    override fun visitElement(ctx: ElementContext): XmlNode {
        return visitElement(ctx, 0)
    }

    /**
     * 获取context的起始token索引
     */
    private fun getStartTokenIndex(ctx: ParserRuleContext): Int {
        return ctx.start.tokenIndex
    }

    /**
     * 获取context的结束token索引
     */
    private fun getStopTokenIndex(ctx: ParserRuleContext): Int {
        return ctx.stop.tokenIndex
    }

    /**
     * 从attribute列表中提取识别性属性
     * 返回格式: "attrName=attrValue" 或 null
     */
    private fun extractIdentifyingAttribute(attributes: MutableList<TechlandXMLParser.AttributeContext>): String? {
        if (CollUtil.isEmpty(attributes)) {
            return null
        }

        // 1. 先尝试按优先级查找属性
        for (priorityName in PRIORITY_ATTRIBUTE_NAMES) {
            for (attr in attributes) {
                val attrName = attr.Name().text
                if (priorityName == attrName) {
                    var value = attr.STRING().text
                    // 移除引号
                    value = value.replace("^\"|\"$|^'|'$".toRegex(), "")
                    return "$attrName=$value"
                }
            }
        }

        // 如果没有找到优先级属性，使用所有属性组合
        // 按属性名排序以保证一致性
        val sb = StringBuilder()
        attributes.stream()
            .sorted(Comparator.comparing<TechlandXMLParser.AttributeContext, String>(Function { a: TechlandXMLParser.AttributeContext ->
                a.Name().text
            }))
            .forEach { attr: TechlandXMLParser.AttributeContext ->
                val attrName = attr.Name().text
                var value = attr.STRING().text
                value = value.replace("^\"|\"$|^'|'$".toRegex(), "")
                if (!sb.isEmpty()) {
                    sb.append(",")
                }
                sb.append(attrName).append("=").append(value)
            }
        return if (!sb.isEmpty()) sb.toString() else null
    }

    /**
     * 构建element签名
     */
    private fun buildSignature(tagName: String, identifyingAttr: String?, index: Int): String {
        return if (!identifyingAttr.isNullOrEmpty()) {
            "$ELEMENT:$tagName:$identifyingAttr"
        } else {
            "$ELEMENT:$tagName:$index"
        }
    }

    /**
     * 内部方法：访问element并指定索引
     */
    private fun visitElement(ctx: ElementContext, index: Int): XmlNode {
        val tagName: String = ctx.Name().first().text
        val attributes = ctx.attribute()

        var identifyingAttr: String? = null
        val cleanAttributes: MutableMap<String, String> = LinkedHashMap()
        if (attributes != null) {
            for (attrCtx in ctx.attribute()) {
                val attrKey = attrCtx.Name().text
                val attrValue = attrCtx.STRING().text // 这里拿到的是带引号的原文，如 "  5  "
                // 清洗值：去掉引号，去掉首尾空格
                val cleanValue = normalizeValue(attrValue)
                cleanAttributes[attrKey] = cleanValue
            }
            identifyingAttr = extractIdentifyingAttribute(attributes)
        }

        val signature = buildSignature(tagName, identifyingAttr, index)
        val content = ctx.content()

        // 判断是否为容器节点（有子元素）
        if (content != null && !content.element().isEmpty()) {
            val containerNode = XmlContainerNode(
                signature,
                getStartTokenIndex(ctx),
                getStopTokenIndex(ctx),
                ctx.start.line,
                tokenStream,
                cleanAttributes
            )

            // 添加子元素
            val childElements = content.element()
            var childIndex = 0
            for (childCtx in childElements) {
                val childNode = visitElement(childCtx, childIndex++)
                containerNode.addChild(childNode)
            }
            return containerNode
        } else {
            // 叶子节点（没有子元素或为自闭合标签）
            return XmlLeafNode(
                signature,
                getStartTokenIndex(ctx),
                getStopTokenIndex(ctx),
                ctx.start.line,
                tokenStream,
                cleanAttributes
            )
        }
    }

    /**
     * 标准化文本
     */
    private fun normalizeValue(raw: String?): String {
        if (raw == null) return ""
        var value = raw.trim()
        // 去掉首尾引号
        if (value.length >= 2) {
            val first = value[0]
            val last = value[value.length - 1]
            if ((first == '"' && last == '"') || (first == '\'' && last == '\'')) {
                value = value.substring(1, value.length - 1)
            }
        }
        // 再次去空格 (应对 id="  Value  " 的情况)
        return value.trim()
    }
}
