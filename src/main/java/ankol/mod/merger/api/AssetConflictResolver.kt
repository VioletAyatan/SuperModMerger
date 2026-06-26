package ankol.mod.merger.api

import ankol.mod.merger.core.filetrees.PathFileTree
import java.nio.file.Path

/**
 * 资源冲突解决接口
 * 当有多个 Mod 包含相同路径资源、但非可合并文件类型时，让用户选择使用哪个版本
 */
fun interface AssetConflictResolver {
    fun chooseAsset(
        relPath: String,
        sources: MutableList<PathFileTree>,
        mergedDir: Path
    )
}
