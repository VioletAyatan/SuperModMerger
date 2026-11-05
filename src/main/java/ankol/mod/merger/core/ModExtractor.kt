package ankol.mod.merger.core

import ankol.mod.merger.core.files.ExtractedArchiveFile
import ankol.mod.merger.domain.MergingModInfo
import ankol.mod.merger.tools.ConsoleColorPrinter
import ankol.mod.merger.tools.ErrorReporter
import ankol.mod.merger.tools.Localizations.t
import ankol.mod.merger.tools.PakManager
import ankol.mod.merger.tools.logger
import org.apache.commons.lang3.Strings
import java.nio.file.Path

class ModExtractor(
    private val mergeableMods: List<MergingModInfo>,
    private val extractTempDir: Path,
    private val baseModManager: BaseModManager
) {
    private val log = logger()

    data class GroupedModFile(
        /**
         * FileFullName in Zip-pak eg: /scripts/inventory/inventory.scr
         */
        val fileEntryName: String,
        /**
         * Grouped files list with the same name from different mod paks
         */
        val archiveFiles: MutableList<ExtractedArchiveFile>
    )

    /**
     * 解压结果
     *
     * @param groupedFiles 按相对路径分组后的文件
     * @param pathCorrectionCount 被修正到基准MOD路径的文件数
     */
    data class ExtractionResult(
        val groupedFiles: Collection<GroupedModFile>,
        val pathCorrectionCount: Int,
    )

    /**
     * Extract files from all mods and group them by relative file path
     * @return [ExtractionResult]
     */
    fun extractAllMods(): ExtractionResult {
        val extractedMods = mergeableMods.withIndex().toList()
            .parallelStream().map { (index, mod) ->
                try {
                    val archiveName = mod.modName
                    val modTempDir = extractTempDir.resolve("${archiveName}${index}")
                    return@map mod to PakManager.extractPak(archiveName, mod.modPath, modTempDir)
                } catch (e: Exception) {
                    log.error(t("ENGINE_EXTRACT_FAILED", mod.modName), e)
                    ErrorReporter.addErrorReport(mod.modName, t("ERROR_EXTRA_MOD_FAILED", e.message, mod.modPath))
                    return@map null
                }
            }.toList()

        val filesByPath = LinkedHashMap<String, GroupedModFile>()
        var correctionCount = 0
        for (extracted in extractedMods) {
            val (mod, extractedFiles) = extracted ?: continue
            correctionCount += groupExtractedFilesByPath(mod.modName, extractedFiles, filesByPath)
        }
        return ExtractionResult(filesByPath.values, correctionCount)
    }

    /**
     * Single pass processing: filter, correct paths, and group by relative path
     */
    private fun groupExtractedFilesByPath(
        modFileName: String,
        extractedFiles: Map<String, ExtractedArchiveFile>,
        filesByPath: MutableMap<String, GroupedModFile>
    ): Int {
        val correctionsFileMap = LinkedHashMap<String, String>()

        for ((fileEntryName, sourceInfo) in extractedFiles) {
            if (shouldSkipFile(fileEntryName, sourceInfo)) {
                continue
            }

            var targetPath = fileEntryName
            if (baseModManager.isLoaded && baseModManager.hasPathConflict(fileEntryName)) {
                val suggestedPath = baseModManager.getSuggestedPath(fileEntryName)
                if (suggestedPath != null) {
                    correctionsFileMap[fileEntryName] = suggestedPath
                    targetPath = suggestedPath
                }
            }

            filesByPath.getOrPut(targetPath) { GroupedModFile(targetPath, ArrayList()) }
                .archiveFiles.add(sourceInfo)
        }

        if (correctionsFileMap.isNotEmpty()) {
            ConsoleColorPrinter.cyan(t("ENGINE_PATH_CORRECTIONS_FOR_MOD", modFileName))
            for ((wrongPath, correctPath) in correctionsFileMap) {
                ConsoleColorPrinter.success(t("ENGINE_PATH_CORRECTION_ITEM", wrongPath, correctPath))
            }
        }

        return correctionsFileMap.size
    }

    private fun shouldSkipFile(fileEntryName: String, sourceInfo: ExtractedArchiveFile): Boolean {
        if (Strings.CI.endsWithAny(fileEntryName, ".txt", ".md")) {
            log.warn("Unsupported text file: ${fileEntryName}, Marking to removal.")
            return true
        }
        if (Strings.CI.endsWithAny(fileEntryName, ".dll", ".asi", ".exe")) {
            log.warn("Unsupported dll/asi file: ${fileEntryName}, Please handle it yourself after merging.")
            ErrorReporter.addErrorReport(
                sourceInfo.getFirstArchiveFileName(),
                fileEntryName,
                t("ERROR_NOT_SUPPORT_DLL")
            )
            return true
        }
        if (Strings.CI.endsWithAny(fileEntryName, ".rpack", ".dds")) {
            log.warn("Unsupported rpak file: ${fileEntryName}, Marking to removal.")
            ErrorReporter.addErrorReport(
                sourceInfo.getFirstArchiveFileName(),
                fileEntryName,
                t("ERROR_NOT_SUPPORT_RPACK")
            )
            return true
        }
        return false
    }

}


