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
    private final String fileName;

    private final String baseModName;
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
}
