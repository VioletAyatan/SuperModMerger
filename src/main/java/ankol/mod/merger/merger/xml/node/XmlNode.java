package ankol.mod.merger.merger.xml.node;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public abstract class XmlNode {
    /**
     * 节点签名
     * <p>
     * 用于多文件对比时快速查找相同的节点，在语法树同层级，节点签名必定要保持唯一！！
     */
    protected String signature;
    /**
     * 节点对应的代码行起始TOKEN索引
     */
    protected int startTokenIndex;
    /**
     * 节点对应的代码行结束TOKEN索引
     */
    protected int stopTokenIndex;
    /**
     * 行号
     */
    protected int line;
    /**
     * 当前节点对应的源码文本
     */
    protected String sourceText;

    public XmlNode(String signature, int startTokenIndex, int stopTokenIndex, int line, String sourceText) {
        this.signature = signature;
        this.startTokenIndex = startTokenIndex;
        this.stopTokenIndex = stopTokenIndex;
        this.line = line;
        this.sourceText = sourceText;
    }
}
