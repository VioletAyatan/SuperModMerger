package ankol.mod.merger.core;

import lombok.Data;

@Data
public class MergerContext {
    /**
     * 当前正在合并的文件名
     */
    private String fileName;
    /**
     * 基准MOD名称
     */
    private String mod1Name;
    /**
     *
     */
    private String mod2Name;
}
