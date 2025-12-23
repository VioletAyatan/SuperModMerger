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
     * 待合并MOD名称
     */
    private String mod2Name;
    /**
     * 基准MOD（data0.pak）中对应文件的内容，用于三方对比
     * 如果基准MOD中不存在该文件，则为null
     */
    private String originalBaseModContent;
}
