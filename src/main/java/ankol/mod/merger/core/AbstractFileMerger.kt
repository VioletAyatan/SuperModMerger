package ankol.mod.merger.core

import ankol.mod.merger.core.filetrees.AbstractFileTree
import ankol.mod.merger.merger.MergeResult

/**
 * 文件合并器通用接口
 * 
 * @param context 当前执行合并逻辑的上下文信息
 * 定义了所有文件类型合并器必须实现的方法。
 * 每个实现类负责处理一种特定的文件类型（如.scr, .xml等）。
 */
abstract class AbstractFileMerger(var context: MergerContext) {
    /**
     * 合并两个文件。
     * 
     * @param file1 第一个文件（来自Mod1）的路径。
     * @param file2 第二个文件（来自Mod2）的路径。
     * @return 一个包含合并后内容和冲突信息的 [MergeResult] 对象。
     */
    abstract fun merge(file1: AbstractFileTree, file2: AbstractFileTree): MergeResult
}