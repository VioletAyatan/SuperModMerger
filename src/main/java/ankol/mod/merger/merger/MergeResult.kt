package ankol.mod.merger.merger

/**
 * 合并结果内部类 - 存储单个文件的合并结果
 * 
 * @param mergedContent 合并后的脚本文本内容
 * @param hasConflicts  是否存在冲突（被处理的冲突）
 */
data class MergeResult(val mergedContent: String, val hasConflicts: Boolean)