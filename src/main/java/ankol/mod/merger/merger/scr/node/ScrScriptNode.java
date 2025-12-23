package ankol.mod.merger.merger.scr.node;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

@Data
public abstract class ScrScriptNode {
    protected String signature;

    // 新增：记录该节点在源文件字符流中的起止位置
    protected int startIndex;
    protected int stopIndex;
    protected int line; //行号

    // 新增：保存 Mod 文件里的原始文本（用于替换 Base 时直接搬运，带注释）
    @JsonIgnore
    protected String sourceText;

    public ScrScriptNode(String signature, int startIndex, int stopIndex, int line, String sourceText) {
        this.signature = signature;
        this.startIndex = startIndex;
        this.stopIndex = stopIndex;
        this.line = line;
        this.sourceText = sourceText;
    }
}
