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

/**
 * 模组提取器
 * @author Ankol
 */
class ModExtractor(
    private val mergeableMods: List<MergingModInfo>,
    private val tempDir: Path,
    private val baseModManager: BaseModManager
) {
    private val log = logger()

    /**
     * 从所有 mod 中提取文件，按相对路径分组
     * @return [Map] key为文件相对路径名，value为分组后的文件
     */
    fun extractAllMods(): ConcurrentHashMap<String, MutableList<PathFileTree>> {
        val filesByPath = ConcurrentHashMap<String, MutableList<PathFileTree>>()
        val index = AtomicInteger(0)
        val correctionCounter = AtomicInteger(0)

        mergeableMods.parallelStream().forEach { mod: MergingModInfo ->
            try {
                val archiveName = mod.modName
                val modTempDir = tempDir.resolve("${archiveName}${index.getAndIncrement()}")
                val extractedFiles = PakManager.extractPak(archiveName, mod.modPath, modTempDir) //解压压缩包
                val correctedCount = processExtractedFiles(archiveName, extractedFiles, filesByPath)
                correctionCounter.addAndGet(correctedCount)
            } catch (e: Exception) {
                log.error(t("ENGINE_EXTRACT_FAILED", mod.modName), e)
                ErrorReporter.addErrorReport(mod.modName, t("ERROR_EXTRA_MOD_FAILED", e.message, mod.modPath))
            }
        }

        return filesByPath
    }

    /**
     * 单次遍历完成：过滤、路径修正、按路径分组
     */
    private fun processExtractedFiles(
        modFileName: String,
        extractedFiles: Map<String, PathFileTree>,
        filesByPath: MutableMap<String, MutableList<PathFileTree>>
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

            filesByPath.computeIfAbsent(targetPath) { CopyOnWriteArrayList() }.add(sourceInfo)
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


