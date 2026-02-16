package ankol.mod.merger.domain

import java.nio.file.Path

data class ModPath(
    val modName: String,
    val modPath: Path,
)
