package ankol.mod.merger.merger.scr.node;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public abstract class ScrScriptNode {
    /**
     * node签名，用于实现节点唯一标识
     */
    protected String signature;
    // 记录该节点在TokenStream中的起止位置（Token索引）
    protected int startTokenIndex;
    protected int stopTokenIndex;
    protected int line; //行号
    // 保存 Mod 文件里的原始文本（用于替换 Base 时直接搬运，带注释）
    @JsonIgnore
    protected String sourceText;

    public ScrScriptNode(String signature, int startTokenIndex, int stopTokenIndex, int line, String sourceText) {
        this.signature = signature;
        this.startTokenIndex = startTokenIndex;
        this.stopTokenIndex = stopTokenIndex;
        this.line = line;
        this.sourceText = sourceText;
    }
}
