package ankol.mod.merger.merger;

/**
 * 合并结果内部类 - 存储单个文件的合并结果
 * <p>
 * 字段说明：
 * - mergedContent: 合并后的脚本内容
 * - hasConflicts: 是否包含被处理的冲突（不包括被跳过的）
 * - conflicts: 被处理的冲突列表
 *
 * @param mergedContent 合并后的脚本文本内容
 * @param hasConflicts  是否存在冲突（被处理的冲突）
 */
public record MergeResult(String mergedContent, boolean hasConflicts) {
}