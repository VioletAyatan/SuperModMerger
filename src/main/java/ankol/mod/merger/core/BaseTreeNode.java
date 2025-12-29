package ankol.mod.merger.core;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 基础树节点
 *
 * @author Ankol
 */
@Getter
@Setter
@ToString
public abstract class BaseTreeNode {
    /**
     * 当前节点签名（确保在同一树层级下保持唯一，方便进行多文件对比）
     */
    protected String signature;
    /**
     * 当前节点起始TOKEN索引
     */
    protected int startTokenIndex;
    /**
     * 当前节点结束TOKEN索引
     */
    protected int stopTokenIndex;
    /**
     * 当前行号
     */
    protected int lineNumber;
    /**
     * 保存 Mod 文件里的原始文本（用于替换 Base 时直接搬运，带注释）
     */
    @JsonIgnore
    protected String sourceText;

    /**
     * 构造函数
     *
     * @param signature       签名
     * @param startTokenIndex token起始索引
     * @param stopTokenIndex  token结束索引
     * @param lineNumber      行号
     * @param sourceText      源文本，用于执行替换
     */
    public BaseTreeNode(String signature, int startTokenIndex, int stopTokenIndex, int lineNumber, String sourceText) {
        this.signature = signature;
        this.startTokenIndex = startTokenIndex;
        this.stopTokenIndex = stopTokenIndex;
        this.lineNumber = lineNumber;
        this.sourceText = sourceText;
    }
}
