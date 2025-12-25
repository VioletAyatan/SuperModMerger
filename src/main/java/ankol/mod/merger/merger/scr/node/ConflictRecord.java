package ankol.mod.merger.merger.scr.node;

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
    private final ScrScriptNode baseNode;
    private final ScrScriptNode modNode;
    /**
     * 用户选择
     */
    private Integer userChoice;
}
