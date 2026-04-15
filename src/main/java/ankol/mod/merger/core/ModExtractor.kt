package ankol.mod.merger.core

import ankol.mod.merger.core.filetrees.PathFileTree
import ankol.mod.merger.domain.MergingModInfo
import ankol.mod.merger.tools.ColorPrinter
import ankol.mod.merger.tools.ErrorReporter
import ankol.mod.merger.tools.Localizations.t
import ankol.mod.merger.tools.PakManager
import ankol.mod.merger.tools.logger
import org.apache.commons.lang3.Strings
import java.nio.file.Path
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.atomic.AtomicInteger

class ModExtractor(
    private val mergeableMods: List<MergingModInfo>,
    private val tempDir: Path,
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
        val fileTreePaths: MutableList<PathFileTree>
    )

    /**
     * Extract files from all mods and group them by relative file path
     * @return [Map] key is the relative file path, value is the grouped files
     */
    fun extractAllMods(): Collection<GroupedModFile> {
        val filesByPath = ConcurrentHashMap<String, GroupedModFile>()
        val index = AtomicInteger(0)
        val correctionCounter = AtomicInteger(0)

        mergeableMods.parallelStream().forEach { mod: MergingModInfo ->
            try {
                val archiveName = mod.modName
                val modTempDir = tempDir.resolve("${archiveName}${index.getAndIncrement()}")
                val extractedFiles = PakManager.extractPak(archiveName, mod.modPath, modTempDir) // Extract compressed archive
                val correctedCount = groupExtractedFilesByPath(archiveName, extractedFiles, filesByPath)
                correctionCounter.addAndGet(correctedCount)
            } catch (e: Exception) {
                log.error(t("ENGINE_EXTRACT_FAILED", mod.modName), e)
                ErrorReporter.addErrorReport(mod.modName, t("ERROR_EXTRA_MOD_FAILED", e.message, mod.modPath))
            }
        }

        return filesByPath.values
    }

    /**
     * Single pass processing: filter, correct paths, and group by relative path
     */
    private fun groupExtractedFilesByPath(
        modFileName: String,
        extractedFiles: Map<String, PathFileTree>,
        filesByPath: ConcurrentHashMap<String, GroupedModFile>
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

            filesByPath.computeIfAbsent(targetPath) { GroupedModFile(targetPath, CopyOnWriteArrayList()) }
                .fileTreePaths.add(sourceInfo)
        }

        if (correctionsFileMap.isNotEmpty()) {
            ColorPrinter.cyan(t("ENGINE_PATH_CORRECTIONS_FOR_MOD", modFileName))
            for ((wrongPath, correctPath) in correctionsFileMap) {
                ColorPrinter.success(t("ENGINE_PATH_CORRECTION_ITEM", wrongPath, correctPath))
            }
        }

        return correctionsFileMap.size
    }

    private fun shouldSkipFile(fileEntryName: String, sourceInfo: PathFileTree): Boolean {
        if (Strings.CI.endsWithAny(fileEntryName, ".txt", ".md")) {
            log.warn("Unsupported text file: ${fileEntryName}, Marking to removal.")
            return true
        }
        if (Strings.CI.endsWithAny(fileEntryName, ".dll", ".asi")) {
            log.warn("Unsupported dll/asi file: ${fileEntryName}, Please handle it yourself after merging.")
            ErrorReporter.addErrorReport(sourceInfo.getFirstArchiveFileName(), t("ERROR_NOT_SUPPORT_DLL", fileEntryName))
            return true
        }
        if (Strings.CI.endsWithAny(fileEntryName, ".rpack")) {
            log.warn("Unsupported rpak file: ${fileEntryName}, Marking to removal.")
            ErrorReporter.addErrorReport(sourceInfo.getFirstArchiveFileName(), t("ERROR_NOT_SUPPORT_RPACK", fileEntryName))
            return true
        }
        return false
    }

}


