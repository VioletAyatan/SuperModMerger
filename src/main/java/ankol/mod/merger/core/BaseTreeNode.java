package ankol.mod.merger.core;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.misc.Interval;

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
     * Token流引用
     */
    @JsonIgnore
    protected transient CommonTokenStream tokenStream;

    /**
     * 构造函数
     *
     * @param signature       签名
     * @param startTokenIndex token起始索引
     * @param stopTokenIndex  token结束索引
     * @param lineNumber      行号
     * @param tokenStream     token流（用于按需提取源文本）
     */
    public BaseTreeNode(String signature, int startTokenIndex, int stopTokenIndex, int lineNumber, CommonTokenStream tokenStream) {
        this.signature = signature;
        this.startTokenIndex = startTokenIndex;
        this.stopTokenIndex = stopTokenIndex;
        this.lineNumber = lineNumber;
        this.tokenStream = tokenStream;
    }

    /**
     * 获取源文本
     * 从原始TokenStream里获取
     */
    public String getSourceText() {
        int startIndex = tokenStream.get(startTokenIndex).getStartIndex();
        int stopIndex = tokenStream.get(stopTokenIndex).getStopIndex();
        return tokenStream.getTokenSource().getInputStream().getText(new Interval(startIndex, stopIndex));
    }
}
