package ankol.mod.merger.domain

import java.nio.file.Path

data class MergingModInfo(
    val modName: String,
    val modPath: Path,
)
