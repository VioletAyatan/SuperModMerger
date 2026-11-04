package ankol.mod.merger.api.console

import ankol.mod.merger.api.AssetConflictResolver
import ankol.mod.merger.core.files.ExtractedArchiveFile
import ankol.mod.merger.tools.ConsoleColorPrinter
import ankol.mod.merger.tools.Localizations.t
import ankol.mod.merger.tools.Tools
import java.nio.file.Path

/**
 * 控制台实现的资源冲突解决
 * 保留原有的 readln() 交互行为
 */
object ConsoleAssetConflictResolver : AssetConflictResolver {

    override fun chooseAsset(relPath: String, sources: MutableList<ExtractedArchiveFile>, mergedDir: Path) {
        ConsoleColorPrinter.warning("\n${t("ASSET_NOT_SUPPORT_FILE_EXTENSION", relPath)}")
        ConsoleColorPrinter.warning(t("ASSET_CHOSE_WHICH_VERSION_TO_USE"))
        for ((i, archiveFile) in sources.withIndex()) {
            ConsoleColorPrinter.cyan("{}. {}", i + 1, archiveFile.getFirstArchiveFileName())
        }
        while (true) {
            val input = readln()
            try {
                val choice = input.toInt()
                if (choice >= 1 && choice <= sources.size) {
                    val chosenSource = sources[choice - 1]
                    ConsoleColorPrinter.cyan(t("ASSET_USER_CHOSE_COMPLETE", chosenSource.getFirstArchiveFileName()))
                    Tools.zeroCopy(chosenSource.safeGetFilePath(), mergedDir.resolve(relPath))
                    return
                }
            } catch (_: Exception) {
                ConsoleColorPrinter.warning(t("ASSET_INVALID_INPUT_PLEASE_ENTER_NUMBER", 1, sources.size))
            }
        }
    }
}
