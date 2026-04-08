package ankol.mod.merger.core

import ankol.mod.merger.core.filetrees.MemoryFileTree
import ankol.mod.merger.core.filetrees.PathFileTree
import ankol.mod.merger.domain.MergerContext
import ankol.mod.merger.domain.MergingModInfo
import ankol.mod.merger.merger.MergerFactory
import ankol.mod.merger.tools.*
import ankol.mod.merger.tools.Localizations.t
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.readText
import kotlin.io.path.writeText

/**
 * 模组合并引擎 - 负责执行模组合并的核心逻辑
 * @param mergeableMods 要合并的 mod 列表（.pak 文件路径）
 * @param outputPath 最终输出的 .pak 文件路径
 * @param baseModPath 基准MOD文件路径（可为null）
 * @author Ankol
 */
class FileMergerEngine(
    private val mergeableMods: List<MergingModInfo>,
    private val outputPath: Path,
    private val baseModPath: Path
) {
    private val log = logger()

    /**
     * 运行时临时文件存储目录
     */
    private val tempDir = Path(Tools.tempDir, "SuperModMergerTemp")

    /**
     * 基准MOD管理器
     */
    private val baseModManager = BaseModManager(baseModPath)

    /**
     * MOD提取器
     */
    private val modExtrator = ModExtrator(mergeableMods, tempDir, baseModManager)

    // 统计信息
    private var mergedCount = 0 // 成功合并（无冲突）的文件数
    private var totalProcessed = 0 // 处理的文件总数
    private var pathCorrectionCount = 0 // 修正的路径数

    /**
     * 执行合并操作
     */
    fun merge() {
        //打印初始信息
        ColorPrinter.cyan(t("ENGINE_TITLE"))
        if (mergeableMods.isEmpty()) {
            ColorPrinter.error(t("ENGINE_NO_MODS_FOUND"))
            return
        }
        ColorPrinter.cyan(t("ENGINE_FOUND_MODS_TO_MERGE", mergeableMods.size))
        for ((index, modInfo) in mergeableMods.withIndex()) {
            ColorPrinter.cyan("${index + 1}. ${modInfo.modName}")
        }
        //开始合并
        try {
            Tools.deleteRecursively(tempDir) //先清理掉旧的目录
            // 在提取过程中对每个mod分别进行路径修正
            val extractionResult = modExtrator.extractAllMods()
            pathCorrectionCount += extractionResult.correctionCount
            // 输出目录（临时）
            val mergedDir = tempDir.resolve("merged")
            Files.createDirectories(mergedDir)
            // 开始合并文件
            processFiles(extractionResult.filesByPath, mergedDir)
            // 合并完成，打包
            ColorPrinter.cyan(t("ENGINE_CREATING_MERGED_PAK"))
            PakManager.createPak(mergedDir, outputPath)
            ColorPrinter.success(t("ENGINE_MERGED_PAK_CREATED", outputPath))
            // 打印统计信息
            printStatistics()
        } catch (e: Exception) {
            log.error(e.message, e)
        } finally {
            baseModManager.close()
            cleanupTempDir()
        }
    }

    /**
     * 处理所有文件（合并或复制）
     */
    private fun processFiles(filesByName: Map<String, MutableList<PathFileTree>>, mergedDir: Path) {
        ColorPrinter.cyan(t("ENGINE_PROCESSING_FILES"))
        val globalFixActive = GlobalMergingStrategy.activeMode == GlobalMergingStrategy.GLOBAL_FIX_MODE
        if (globalFixActive) {
            ColorPrinter.debug(t("ENGINE_GLOBAL_FIX_ENABLED"))
        }
        for ((relPath, fileSources) in filesByName) {
            totalProcessed++
            try {
                //单个文件处理
                if (fileSources.size == 1) {
                    if (globalFixActive) {
                        processSingleFile(relPath, fileSources.first(), mergedDir) //做压力测试的时候把这个打开
                    } else {
                        Tools.zeroCopy(fileSources.first().safegetFilePath(), mergedDir.resolve(relPath))
                    }
                } else {
                    // 在多个 mod 中存在，需要合并
                    mergeFiles(relPath, fileSources, mergedDir)
                }
            } catch (e: Exception) {
                ColorPrinter.error(t("ENGINE_PROCESSING_ERROR", relPath, e.message))
            }
        }
    }

    /**
     * 处理单个文件（可能需要与基准mod对比）
     *
     * @param relPath         相对路径
     * @param fileCurrent     文件来源
     * @param mergedOutputDir 合并输出目录
     */
    private fun processSingleFile(relPath: String, fileCurrent: PathFileTree, mergedOutputDir: Path) {
        // 如果基准mod存在，尝试与基准mod对比
        if (baseModManager.isLoaded) {
            try {
                val vanillaFileContent = baseModManager.extractFileContent(relPath)
                // 基准mod中存在该文件，需要进行对比合并
                if (vanillaFileContent != null) {
                    val context = MergerContext(baseModManager)
                    val merger = MergerFactory.getMerger(relPath, context)

                    // 如果支持合并，进行对比合并
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

                        // 写入合并结果
                        val targetPath = mergedOutputDir.resolve(relPath)
                        targetPath.parent.createDirectories()
                        targetPath.writeText(mergedContent)

                        this.mergedCount++
                        ColorPrinter.success(t("ENGINE_MERGE_SUCCESS", context.mergingFileName))
                        return
                    }
                }
                Tools.zeroCopy(fileCurrent.safegetFilePath(), mergedOutputDir.resolve(relPath))
            } catch (e: Exception) {
                ColorPrinter.error("Processing file '${relPath}' error, Reason: ${e.message}", e)
            }
        }
    }

    /**
     * 合并多个同名文件
     * 对MOD进行顺序合并
     *
     * @param relPath     当前合并的文件相对路径
     * @param fileSources 待合并的同名文件的来源
     * @param mergedDir   合并输出目录
     */
    private fun mergeFiles(relPath: String, fileSources: MutableList<PathFileTree>, mergedDir: Path) {
        // 先简单的判断一下文件内容（计算hash值）、大小是否相同，不同肯定不一样
        if (areAllFilesIdentical(fileSources)) {
            // 文件都一样，直接使用第一个
            Tools.zeroCopy(fileSources.first().safegetFilePath(), mergedDir.resolve(relPath))
            return
        }

        val context = MergerContext(baseModManager)
        val merger = MergerFactory.getMerger(relPath, context) //获取合并器

        //不支持合并的文件类型，直接让用户选择用哪个文件
        if (merger == null) {
            choiseWhichAssetToUse(relPath, fileSources, mergedDir)
            return
        }

        try {
            var accumulatedContent = ""
            // 支持合并，开始处理合并逻辑
            ColorPrinter.cyan(t("ENGINE_MERGING_FILE", relPath, fileSources.size))

            var vanillaFileContent: String? = null
            if (baseModManager.isLoaded) {
                vanillaFileContent = baseModManager.extractFileContent(relPath)
            }
            val fileName = Tools.getEntryFileName(relPath)

            // 顺序合并：使用data0.pak作为基准（如果存在），然后依次合并各个mod
            for ((i, fileCurrent) in fileSources.withIndex()) {
                val currentModPath = fileCurrent.safegetFilePath()
                val currentModName = fileCurrent.getFirstArchiveFileName()

                // 第一个 mod：如果有data0.pak基准文件，使用它作为base与第一个mod合并
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
                        // 没有data0.pak基准文件，直接使用第一个mod的内容
                        accumulatedContent = currentModPath.readText()
                    }
                } else {
                    // 后续的 mod，与当前合并结果合并
                    val previousSource = fileSources[i - 1]
                    val previousModName = previousSource.getFirstArchiveFileName()

                    // 执行合并 - 使用真实的MOD压缩包名字
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

            // 写入最终合并结果
            val targetPath = mergedDir.resolve(relPath)
            targetPath.parent.createDirectories()
            targetPath.writeText(accumulatedContent)

            this.mergedCount++
            ColorPrinter.success(t("ENGINE_MERGE_SUCCESS", context.mergingFileName))
        } catch (e: Exception) {
            ColorPrinter.error(t("ENGINE_MERGE_FAILED", e.message))
            log.error("Failed to merge file '{}': {}", relPath, e.message)
            // todo 这里合并失败的策略还得再调整下，现在是失败时使用最后一个 mod 的版本
            val lastSource: PathFileTree = fileSources.last()
            Tools.zeroCopy(lastSource.safegetFilePath(), mergedDir.resolve(relPath))
        }
    }


    /**
     * 不支持合并的文件类型，让用户选择使用哪个版本
     */
    private fun choiseWhichAssetToUse(
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
                    Tools.zeroCopy(chosenSource.safegetFilePath(), mergedDir.resolve(relPath))
                    return
                }
            } catch (_: Exception) {
                ColorPrinter.warning(t("ASSET_INVALID_INPUT_PLEASE_ENTER_NUMBER", 1, fileSources.size))
            }
        }
    }

    /**
     * 检查多个文件是否内容相同
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
     * 打印合并统计信息
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
     * 清理临时文件
     */
    private fun cleanupTempDir() {
        Tools.deleteRecursively(tempDir)
    }
}
