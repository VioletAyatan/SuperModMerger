package ankol.mod.merger.merger

import ankol.mod.merger.core.MergerContext

/**
 * 合并结果内部类 - 存储单个文件的合并结果
 *
 * @param mergedContent 合并后的脚本文本内容
 */
data class MergeResult(val mergedContent: String, val mergedHistory: MergerContext.MergedHistory) {
}
