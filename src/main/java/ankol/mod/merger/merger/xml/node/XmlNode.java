package ankol.mod.merger.merger.xml.node;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Map;

@Setter
@Getter
@ToString
public abstract class XmlNode {
    /**
     * 节点签名
     * <p>
     * 用于多文件对比时快速查找相同的节点，在语法树同层级，节点签名必定要保持唯一！！
     */
    protected final String signature;
    /**
     * 节点对应的代码行起始TOKEN索引
     */
    protected final int startTokenIndex;
    /**
     * 节点对应的代码行结束TOKEN索引
     */
    protected final int stopTokenIndex;
    /**
     * 行号
     */
    protected final int line;
    /**
     * 当前节点对应的源码文本
     */
    protected final String sourceText;

    protected Map<String, String> attributes;

    public XmlNode(String signature, int startTokenIndex, int stopTokenIndex, int line, String sourceText, Map<String, String> attributes) {
        this.signature = signature;
        this.startTokenIndex = startTokenIndex;
        this.stopTokenIndex = stopTokenIndex;
        this.line = line;
        this.sourceText = sourceText;
        this.attributes = attributes;
    }

    public boolean isSemanticallyEqual(XmlNode other) {
        if (other == null) {
            return false;
        }
        if (!this.signature.equals(other.getSignature())) {
            return false;
        }
        return this.attributes.equals(other.getAttributes());
    }
}
