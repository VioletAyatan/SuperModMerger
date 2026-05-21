package ankol.mod.merger.core

import ankol.mod.merger.core.filetrees.MemoryFileTree
import ankol.mod.merger.core.filetrees.PathFileTree
import ankol.mod.merger.domain.MergerContext
import ankol.mod.merger.domain.MergingModInfo
import ankol.mod.merger.mergers.MergerFactory
import ankol.mod.merger.tools.*
import ankol.mod.merger.tools.Localizations.t
import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.readText
import kotlin.io.path.writeText

/**
 * File Merger Engine - Core logic for executing mod merging operations
 * @param mergeableMods List of mods to merge (.pak file paths)
 * @param outputPath Final output .pak file path
 * @param basePakDirPath Base mod file path (can be null)
 * @author Ankol
 */
class FileMergerEngine(
    private val mergeableMods: List<MergingModInfo>,
    private val outputPath: Path,
    private val basePakDirPath: Path
) {
    private val log = logger()

    /**
     * Runtime temporary file storage directory
     */
    private val tempDir = Path(Tools.tempDir, "SuperModMergerTemp")

    private val baseModManager = BaseModManager(basePakDirPath)
    private val modExtractor = ModExtractor(mergeableMods, tempDir, baseModManager)

    // Statistics
    private var mergedCount = 0 // Successfully merged files (no conflicts)
    private var totalProcessed = 0 // Total files processed
    private var pathCorrectionCount = 0 // Number of path corrections

    /**
     * Execute merge operation
     */
    fun merge() {
        // Print initial information
        ColorPrinter.cyan(t("ENGINE_TITLE"))
        if (mergeableMods.isEmpty()) {
            ColorPrinter.error(t("ENGINE_NO_MODS_FOUND"))
            return
        }
        ColorPrinter.cyan(t("ENGINE_FOUND_MODS_TO_MERGE", mergeableMods.size))
        for ((index, modInfo) in mergeableMods.withIndex()) {
            ColorPrinter.cyan("${index + 1}. ${modInfo.modName}")
        }
        // Start merge
        try {
            // Clean up temporary directory to prevent residual files
            Tools.deleteRecursively(tempDir)
            // Extract all mod files
            val groupedFiles = modExtractor.extractAllMods()
            pathCorrectionCount += groupedFiles.size
            val mergedDir = tempDir.resolve("merged")
            mergedDir.createDirectories()
            // Start merging files
            mergeAllFiles(groupedFiles, mergedDir)
            // Merge complete, package the result
            ColorPrinter.cyan(t("ENGINE_CREATING_MERGED_PAK"))
            PakManager.createPak(mergedDir, outputPath)
            ColorPrinter.success(t("ENGINE_MERGED_PAK_CREATED", outputPath))
            // Print statistics
            printStatistics()
        } catch (e: Exception) {
            log.error(e.message, e)
            throw e
        } finally {
            baseModManager.close()
            cleanupTempDir()
        }
    }

    /**
     * Process all files (merge or copy)
     */
    private fun mergeAllFiles(groupedModFiles: Collection<ModExtractor.GroupedModFile>, mergedDir: Path) {
        ColorPrinter.cyan(t("ENGINE_PROCESSING_FILES"))
        val globalFixActive = GlobalMergingStrategy.activeMode == GlobalMergingStrategy.GLOBAL_FIX_MODE
        if (globalFixActive) {
            ColorPrinter.debug(t("ENGINE_GLOBAL_FIX_ENABLED"))
        }
        for ((fileEntryName, fileSources) in groupedModFiles) {
            totalProcessed++
            try {
                // Single file processing
                if (fileSources.size == 1) {
                    if (globalFixActive) {
                        mergeSingleFile(fileEntryName, fileSources.first(), mergedDir) // Enable this for stress testing
                    } else {
                        Tools.zeroCopy(fileSources.first().safeGetFilePath(), mergedDir.resolve(fileEntryName))
                    }
                } else {
                    // Exists in multiple mods, need to merge
                    mergeFiles(fileEntryName, fileSources, mergedDir)
                }
            } catch (e: Exception) {
                log.error(e.message, e)
            }
        }
    }

    /**
     * Process single file (may need to compare with base mod if available)
     *
     * @param relPath         Relative file path
     * @param fileCurrent     File source
     * @param mergedOutputDir Merged output directory
     */
    private fun mergeSingleFile(relPath: String, fileCurrent: PathFileTree, mergedOutputDir: Path) {
        try {
            if (baseModManager.isLoaded) {
                val vanillaFileContent = baseModManager.extractFileContent(relPath)
                if (vanillaFileContent != null) {
                    val context = MergerContext(baseModManager)
                    val merger = MergerFactory.getMerger(relPath, context)

                    if (merger != null) {
                        val fileName = Tools.getEntryFileName(relPath)
                        val fileBase = MemoryFileTree(fileName, relPath, mutableListOf("data0.pak"), vanillaFileContent)

                        context.configure(
                            relPath,
                            "data0.pak",
                            fileCurrent.getFirstArchiveFileName(),
                            true
                        )

                        val mergeResult = merger.merge(fileBase, fileCurrent)
                        val mergedContent = mergeResult.mergedContent

                        // Write merge result
                        val targetPath = mergedOutputDir.resolve(relPath)
                        targetPath.parent.createDirectories()
                        targetPath.writeText(mergedContent)

                        this.mergedCount++
                        ColorPrinter.success(t("ENGINE_MERGE_SUCCESS", context.mergingFileName))
                        return
                    }
                }
            }
            Tools.zeroCopy(fileCurrent.safeGetFilePath(), mergedOutputDir.resolve(relPath))
        } catch (e: Exception) {
            Tools.zeroCopy(fileCurrent.safeGetFilePath(), mergedOutputDir.resolve(relPath))
            log.error("Processing file '${relPath}' error. Reason: ${e.message}. Fallback to original file: ${fileCurrent.fileName}", e)
            ErrorReporter.addErrorReport(relPath, t("ERROR_FILE_MERGE_FAILED", relPath, e.message))
        }
    }

    /**
     * Merge multiple files with the same name
     * Perform sequential merge for mods
     *
     * @param relPath     Relative file path of the current merge
     * @param fileSources Sources of files with the same name to be merged
     * @param mergedDir   Merged output directory
     */
    private fun mergeFiles(relPath: String, fileSources: MutableList<PathFileTree>, mergedDir: Path) {
        // First check if file contents (hash values) and sizes are the same
        if (areAllFilesIdentical(fileSources)) {
            // All files are identical, use the first one directly
            Tools.zeroCopy(fileSources.first().safeGetFilePath(), mergedDir.resolve(relPath))
            return
        }

        val context = MergerContext(baseModManager)
        val merger = MergerFactory.getMerger(relPath, context) // Get the appropriate merger

        // Unsupported file types, let user choose which file to use
        if (merger == null) {
            chooseWhichAssetToUse(relPath, fileSources, mergedDir)
            return
        }

        try {
            var accumulatedContent = ""
            // Support merge, start processing merge logic
            ColorPrinter.cyan(t("ENGINE_MERGING_FILE", relPath, fileSources.size))

            var vanillaFileContent: String? = null
            if (baseModManager.isLoaded) {
                vanillaFileContent = baseModManager.extractFileContent(relPath)
            }
            val fileName = Tools.getEntryFileName(relPath)

            // Sequential merge: use data0.pak as reference if exists, then sequentially merge each mod
            for ((i, fileCurrent) in fileSources.withIndex()) {
                val currentModPath = fileCurrent.safeGetFilePath()
                val currentModName = fileCurrent.getFirstArchiveFileName()

                // First mod: if data0.pak reference file exists, use it as base to merge with the first mod
                if (i == 0) {
                    if (vanillaFileContent != null) {
                        val fileBase = MemoryFileTree(fileName, relPath, mutableListOf("data0.pak"), vanillaFileContent)

                        context.configure(
                            relPath,
                            "data0.pak",
                            currentModName,
                            true
                        )

                        val result = merger.merge(fileBase, fileCurrent)
                        accumulatedContent = result.mergedContent
                    } else {
                        // No data0.pak reference file, use the first mod's content directly
                        accumulatedContent = currentModPath.readText()
                    }
                } else {
                    // Subsequent mods, merge with current merge result
                    val previousSource = fileSources[i - 1]
                    val previousModName = previousSource.getFirstArchiveFileName()

                    // Perform merge - using real MOD archive name
                    val fileBase = MemoryFileTree(fileName, relPath, mutableListOf(previousModName), accumulatedContent)

                    context.configure(
                        relPath,
                        previousModName,
                        currentModName,
                        false
                    )

                    val result = merger.merge(fileBase, fileCurrent)
                    accumulatedContent = result.mergedContent
                }
            }

            // Write final merge result
            val targetPath = mergedDir.resolve(relPath)
            targetPath.parent.createDirectories()
            targetPath.writeText(accumulatedContent)

            this.mergedCount++
            ColorPrinter.success(t("ENGINE_MERGE_SUCCESS", context.mergingFileName))
        } catch (e: Exception) {
            // TODO: Adjust the strategy for merge failures here, currently using the last mod's version when failing
            val lastSource: PathFileTree = fileSources.last()
            Tools.zeroCopy(lastSource.safeGetFilePath(), mergedDir.resolve(relPath))
            log.error("Failed to merge file '{}': {}", relPath, e.message)
            ErrorReporter.addErrorReport(relPath, t("ERROR_FILE_MERGE_FAILED", relPath, e.message))
        }
    }


    /**
     * Unsupported file types, let user choose which version to use
     */
    private fun chooseWhichAssetToUse(
        relPath: String,
        fileSources: MutableList<PathFileTree>,
        mergedDir: Path
    ) {
        ColorPrinter.warning("\n${t("ASSET_NOT_SUPPORT_FILE_EXTENSION", relPath)}")
        ColorPrinter.warning(t("ASSET_CHOSE_WHICH_VERSION_TO_USE"))
        for ((i, fileTree) in fileSources.withIndex()) {
            ColorPrinter.cyan("{}. {}", i + 1, fileTree.getFirstArchiveFileName())
        }
        while (true) {
            val input = readln()
            try {
                val choice = input.toInt()
                if (choice >= 1 && choice <= fileSources.size) {
                    val chosenSource = fileSources[choice - 1]
                    ColorPrinter.cyan(t("ASSET_USER_CHOSE_COMPLETE", chosenSource.getFirstArchiveFileName()))
                    Tools.zeroCopy(chosenSource.safeGetFilePath(), mergedDir.resolve(relPath))
                    return
                }
            } catch (_: Exception) {
                ColorPrinter.warning(t("ASSET_INVALID_INPUT_PLEASE_ENTER_NUMBER", 1, fileSources.size))
            }
        }
    }

    /**
     * Check if multiple files have identical content
     */
    private fun areAllFilesIdentical(fileSources: MutableList<PathFileTree>): Boolean {
        if (fileSources.size <= 1) {
            return true
        }
        val first = fileSources.first()
        for (i in 1 until fileSources.size) {
            if (!PakManager.areFilesIdentical(first, fileSources[i])) {
                return false
            }
        }
        return true
    }

    /**
     * Print merge statistics
     */
    private fun printStatistics() {
        ColorPrinter.cyan("\n{}", "=".repeat(75))
        ColorPrinter.cyan(t("ENGINE_STATISTICS_TITLE"))
        ColorPrinter.cyan(t("ENGINE_TOTAL_FILES_PROCESSED", totalProcessed))
        ColorPrinter.success(t("ENGINE_MERGED_NO_CONFLICTS", mergedCount))
        if (pathCorrectionCount > 0) {
            ColorPrinter.success(t("ENGINE_PATH_CORRECTIONS_APPLIED", pathCorrectionCount))
        }
        ErrorReporter.printErrors()
        ColorPrinter.cyan("{}", "=".repeat(75))
    }

    /**
     * Clean up temporary files
     */
    private fun cleanupTempDir() {
        Tools.deleteRecursively(tempDir)
    }
}
