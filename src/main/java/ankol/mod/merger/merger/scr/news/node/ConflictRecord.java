package ankol.mod.merger.merger.scr.news.node;

import lombok.Data;

/**
 * 冲突记录
 */
@Data
public class ConflictRecord {
    /**
     * 冲突文件名
     */
    private final String fileName;
    /**
     * 签名
     */
    private final String signature;
    private final ScrNode baseNode;
    private final ScrNode modNode;

    public ConflictRecord(String fileName, String signature, ScrNode baseNode, ScrNode modNode) {
        this.fileName = fileName;
        this.signature = signature;
        this.baseNode = baseNode;
        this.modNode = modNode;
    }
}
