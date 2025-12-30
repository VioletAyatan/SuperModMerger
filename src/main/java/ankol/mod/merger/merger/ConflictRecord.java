package ankol.mod.merger.merger;

import ankol.mod.merger.core.BaseTreeNode;
import lombok.Data;
import lombok.RequiredArgsConstructor;

/**
 * 冲突记录
 *
 * @author Ankol
 */
@Data
@RequiredArgsConstructor
public class ConflictRecord {
    /**
     * 冲突的文件名
     */
    private final String fileName;
    /**
     * 基础模组名称
     */
    private final String baseModName;
    /**
     * 合并模组名称
     */
    private final String mergeModName;

    /**
     * 冲突的签名
     */
    private final String signature;
    private final BaseTreeNode baseNode;
    private final BaseTreeNode modNode;
    /**
     * 用户选择
     */
    private Integer userChoice;

    public ConflictRecord(String fileName, String baseModName, String mergeModName, String signature, BaseTreeNode baseNode, BaseTreeNode modNode, Integer userChoice) {
        this.fileName = fileName;
        this.baseModName = baseModName;
        this.mergeModName = mergeModName;
        this.signature = signature;
        this.baseNode = baseNode;
        this.modNode = modNode;
        this.userChoice = userChoice;
    }
}
