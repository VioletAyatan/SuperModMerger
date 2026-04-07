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
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

/**
 * 模组提取器
 * @author Ankol
 */
class ModExtrator(
    private val mergeableMods: List<MergingModInfo>,
    private val tempDir: Path,
    private val baseModManager: BaseModManager
) {
    private val log = logger()

    data class ExtractionResult(
        /**
         * 按文件名分组后的mod文件Map
         */
        val filesByPath: MutableMap<String, MutableList<PathFileTree>>,
        /**
         * 修正多少文件路径
         */
        val correctionCount: Int
    )

    /**
     * 从所有 mod 中提取文件，按相对路径分组
     */
    fun extractAllMods(): ExtractionResult {
        val filesByPath = ConcurrentHashMap<String, MutableList<PathFileTree>>()
        val index = AtomicInteger(0)
        val correctionCounter = AtomicInteger(0)

        mergeableMods.parallelStream().forEach { mod: MergingModInfo ->
            try {
                val archiveName = mod.modName
                val modTempDir = tempDir.resolve("${archiveName}${index.getAndIncrement()}")

                var extractedFiles = PakManager.extractPak(archiveName, mod.modPath, modTempDir) //解压压缩包
                extractedFiles = filterFiles(extractedFiles) //过滤掉不支持的文件
                val correctionFileMap = correctPathsForMod(archiveName, extractedFiles) //路径修正

                correctionCounter.addAndGet(correctionFileMap.size) //计数器增加

                //按文件路径分组后返回filesByPath
                for ((fileRelPath, fileSource) in correctionFileMap) {
                    filesByPath.computeIfAbsent(fileRelPath) { Collections.synchronizedList(arrayListOf()) }
                        .add(fileSource)
                }
            } catch (e: Exception) {
                log.error(t("ENGINE_EXTRACT_FAILED", mod.modName), e)
                ErrorReporter.addErrorReport(mod.modName, t("ERROR_EXTRA_MOD_FAILED", e.message, mod.modPath))
            }
        }

        return ExtractionResult(filesByPath, correctionCounter.get())
    }

    /**
     * 过滤掉一些不支持合并的文件
     */
    private fun filterFiles(extractedFiles: MutableMap<String, PathFileTree>): MutableMap<String, PathFileTree> {
        return extractedFiles.filter { predicate: Map.Entry<String, PathFileTree> ->
            val fileEntryName = predicate.key
            val sourceInfo = predicate.value

            if (Strings.CI.endsWithAny(fileEntryName, ".txt", ".md")) {
                log.warn("Unsupported text file: {}, Marking to removal.", fileEntryName)
                return@filter false
            } else if (Strings.CI.endsWithAny(fileEntryName, ".dll", ".asi")) {
                log.warn("Unsupported dll/asi file: {}, Please handle it yourself after merging.", fileEntryName)
                ErrorReporter.addErrorReport(
                    sourceInfo.getFirstArchiveFileName(),
                    t("ERROR_NOT_SUPPORT_DLL", fileEntryName)
                )
                return@filter false
            } else if (Strings.CI.endsWithAny(fileEntryName, ".rpack")) {
                log.warn("Unsupported rpak file: {}, Marking to removal.", fileEntryName)
                ErrorReporter.addErrorReport(
                    sourceInfo.getFirstArchiveFileName(),
                    t("ERROR_NOT_SUPPORT_RPACK", fileEntryName)
                )
                return@filter false
            }

            return@filter true
        }.toMutableMap()
    }

    /**
     * 对单个 MOD 的文件路径进行修正
     */
    fun correctPathsForMod(
        modFileName: String,
        extractedFiles: MutableMap<String, PathFileTree>
    ): Map<String, PathFileTree> {
        if (!baseModManager.isLoaded) {
            return mutableMapOf()
        }

        val corrections = LinkedHashMap<String, String>()
        val correctedFiles = LinkedHashMap<String, PathFileTree>()

        for ((fileEntryName, sourceInfo) in extractedFiles) {
            if (baseModManager.hasPathConflict(fileEntryName)) {
                val suggestedPath = baseModManager.getSuggestedPath(fileEntryName)
                if (suggestedPath != null) {
                    corrections[fileEntryName] = suggestedPath
                    correctedFiles[suggestedPath] = sourceInfo
                } else {
                    correctedFiles[fileEntryName] = sourceInfo
                }
            } else {
                correctedFiles[fileEntryName] = sourceInfo
            }
        }

        if (corrections.isNotEmpty()) {
            ColorPrinter.cyan(t("ENGINE_PATH_CORRECTIONS_FOR_MOD", modFileName))
            for (entry in corrections.entries) {
                ColorPrinter.success(t("ENGINE_PATH_CORRECTION_ITEM", entry.key, entry.value))
            }
        }

        return correctedFiles
    }
}


