package ankol.mod.merger.merger;

import ankol.mod.merger.merger.scr.ScrTreeComparator.DiffResult;

import java.util.ArrayList;
import java.util.List;

/**
 * 合并结果内部类 - 存储单个文件的合并结果
 * <p>
 * 字段说明：
 * - mergedContent: 合并后的脚本内容
 * - hasConflicts: 是否包含被处理的冲突（不包括被跳过的）
 * - conflicts: 被处理的冲突列表
 */
public class MergeResult {
    /**
     * 合并后的脚本文本内容
     */
    public String mergedContent;
    /**
     * 是否存在冲突（被处理的冲突）
     */
    public boolean hasConflicts;
    /**
     * 冲突列表
     */
    public List<DiffResult> conflicts = new ArrayList<>();

    public MergeResult(String mergedContent, boolean hasConflicts) {
        this.mergedContent = mergedContent;
        this.hasConflicts = hasConflicts;
    }
}