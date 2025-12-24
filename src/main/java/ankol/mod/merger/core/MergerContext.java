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
    /**
     * 是否是第一个MOD与data0.pak的合并
     * 当为true时，第一个MOD相对于data0.pak的修改应该被自动接受，不提示冲突
     */
    private boolean isFirstModMergeWithBaseMod = false;
}
