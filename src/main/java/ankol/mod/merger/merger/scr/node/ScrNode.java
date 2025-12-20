package ankol.mod.merger.merger.scr.node;

import lombok.Data;

@Data
public abstract class ScrNode {
    protected String signature;

    // 新增：记录该节点在源文件字符流中的起止位置
    protected int startIndex;
    protected int stopIndex;
    protected int line; //行号

    // 新增：保存 Mod 文件里的原始文本（用于替换 Base 时直接搬运，带注释）
    protected String sourceText;

    public ScrNode(String signature, int startIndex, int stopIndex, int line, String sourceText) {
        this.signature = signature;
        this.startIndex = startIndex;
        this.stopIndex = stopIndex;
        this.line = line;
        this.sourceText = sourceText;
    }
}
