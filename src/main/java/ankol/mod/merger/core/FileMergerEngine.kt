package ankol.mod.merger.core

import ankol.mod.merger.core.filetrees.MemoryFileTree
import ankol.mod.merger.core.filetrees.PathFileTree
import ankol.mod.merger.merger.MergerFactory
import ankol.mod.merger.tools.*
import ankol.mod.merger.tools.Tools.getEntryFileName
import org.apache.commons.lang3.Strings
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.util.*
import java.util.concurrent.CompletionException
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger
import kotlin.io.path.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.readText
import kotlin.io.path.writeText

/**
 * 模组合并引擎 - 负责执行模组合并的核心逻辑
 * @param modsToMerge 要合并的 mod 列表（.pak 文件路径）
 * @param outputPath 最终输出的 .pak 文件路径
 * @param baseModPath 基准MOD文件路径（可为null）
 * @author Ankol
 */
class FileMergerEngine(
    private val modsToMerge: List<Path>,
    private val outputPath: Path,
    private val baseModPath: Path,
    private val argParser: SimpleArgParser
) {
    private val log = logger()

    /**
     * 运行时临时文件存储目录
     */
    private val tempDir = Path(Tools.tempDir, "SuperModMergerTemp")

    /**
     * 基准MOD管理器
     */
    private val baseModManager: BaseModManager = BaseModManager(tempDir, baseModPath)

    // 统计信息
    private var mergedCount = 0 // 成功合并（无冲突）的文件数
    private var totalProcessed = 0 // 处理的文件总数
    private var pathCorrectionCount = 0 // 修正的路径数

    /**
     * 执行合并操作
     */
    fun merge() {
        //打印初始信息
        ColorPrinter.info(Localizations.t("ENGINE_TITLE"))
        if (modsToMerge.isEmpty()) {
            ColorPrinter.error(Localizations.t("ENGINE_NO_MODS_FOUND"))
            return
        }
        ColorPrinter.info(Localizations.t("ENGINE_FOUND_MODS_TO_MERGE", modsToMerge.size))
        for ((i, modPath) in modsToMerge.withIndex()) {
            ColorPrinter.info(Localizations.t("ENGINE_MOD_LIST_ITEM", (i + 1), modPath.fileName))
        }
        //开始合并
        try {
            Tools.deleteRecursively(tempDir) //先清理掉旧的目录
            // 在提取过程中对每个mod分别进行路径修正
            val filesByPath = extractAllMods()
            // 输出目录（临时）
            val mergedDir = tempDir.resolve("merged")
            Files.createDirectories(mergedDir)
            // 开始合并文件
            processFiles(filesByPath, mergedDir)
            // 合并完成，打包
            ColorPrinter.info(Localizations.t("ENGINE_CREATING_MERGED_PAK"))
            PakManager.createPak(mergedDir, outputPath)
            ColorPrinter.success(Localizations.t("ENGINE_MERGED_PAK_CREATED", outputPath))
            // 打印统计信息
            printStatistics()
        } catch (e: Exception) {
            throw RuntimeException(e)
        } finally {
            baseModManager.close()
            cleanupTempDir()
        }
    }

    /**
     * 对单个MOD的文件路径进行修正
     * 
     * @param modFileName    MOD文件名
     * @param extractedFiles 提取的文件映射（相对路径 -> FileSourceInfo）
     * @return 修正后的文件映射
     */
    private fun correctPathsForMod(
        modFileName: String,
        extractedFiles: MutableMap<String, PathFileTree>
    ): MutableMap<String, PathFileTree> {
        //如果没有基准MOD或者合并策略指定了不修正路径
        if (!baseModManager.loaded || !GlobalMergingStrategy.autoFixPath) {
            return extractedFiles
        }

        val corrections = LinkedHashMap<String, String>() //记录修正的路径（原路径 -> 新路径）
        val correctedFiles = LinkedHashMap<String, PathFileTree>() //修正后的文件路径

        val markToRemoved = HashSet<String>()
        // 查找需要修正的路径
        for ((fileEntryName, sourceInfo) in extractedFiles) {
            if (baseModManager.hasPathConflict(fileEntryName)) {
                val suggestedPath: String = baseModManager.getSuggestedPath(fileEntryName)!!
                corrections[fileEntryName] = suggestedPath
                correctedFiles[suggestedPath] = sourceInfo
            }
            //文本类型的文件，直接标记为删除，这些往往是mod作者自己添加的描述文件
            else if (Strings.CI.endsWithAny(fileEntryName, ".txt", ".md")) {
                markToRemoved.add(fileEntryName)
                log.warn("Unsupported text file: {}, Marking to removal.", fileEntryName)
            } else {
                correctedFiles[fileEntryName] = sourceInfo
            }
        }
        markToRemoved.forEach { removeFile: String -> extractedFiles.remove(removeFile) } //移除不存在于基准MOD中的文件

        // 如果有路径被修正，输出日志
        if (!corrections.isEmpty()) {
            ColorPrinter.info(Localizations.t("ENGINE_PATH_CORRECTIONS_FOR_MOD", modFileName))
            for (entry in corrections.entries) {
                ColorPrinter.success(Localizations.t("ENGINE_PATH_CORRECTION_ITEM", entry.key, entry.value))
                pathCorrectionCount++
            }
        }

        return correctedFiles
    }

    /**
     * 从所有 mod 中提取文件，按相对路径分组
     * 在提取过程中对每个mod分别进行路径修正，避免不同mod的同名文件冲突
     */
    private fun extractAllMods(): MutableMap<String, MutableList<PathFileTree>> {
        val filesByPath = ConcurrentHashMap<String, MutableList<PathFileTree>>()
        val index = AtomicInteger(0)
        modsToMerge.parallelStream().forEach { modPath: Path ->
            try {
                val archiveName = modPath.fileName.toString() // 解压的压缩包真实名称
                val modTempDir: Path = tempDir.resolve(archiveName + index.getAndIncrement()) // 生成临时目录名字

                val extractedFiles = PakManager.extractPak(modPath, modTempDir)
                val correctedFiles = correctPathsForMod(archiveName, extractedFiles)
                // 按文件路径分组，并记录来源MOD名字
                for ((fileRelPath, fileSource) in correctedFiles) {
                    filesByPath.computeIfAbsent(fileRelPath) { Collections.synchronizedList<PathFileTree>(ArrayList()) }
                        .add(fileSource)
                }
                ColorPrinter.success(Localizations.t("ENGINE_EXTRACTED_FILES", correctedFiles.size))
            } catch (e: IOException) {
                throw CompletionException(Localizations.t("ENGINE_EXTRACT_FAILED", modPath.fileName), e)
            }
        }
        return filesByPath
    }

    /**
     * 处理所有文件（合并或复制）
     */
    private fun processFiles(filesByName: Map<String, MutableList<PathFileTree>>, mergedDir: Path) {
        ColorPrinter.info(Localizations.t("ENGINE_PROCESSING_FILES"))
        for ((relPath, fileSources) in filesByName) {
            totalProcessed++
            try {
                //单个文件处理
                if (fileSources.size == 1) {
                    val hasFix = argParser.hasOption("f")
                    if (!hasFix) {
                        Tools.zeroCopy(fileSources.first().safeGetFullPathName(), mergedDir.resolve(relPath))
                    } else {
                        processSingleFile(relPath, fileSources.first(), mergedDir) //做压力测试的时候把这个打开
                    }
                } else {
                    // 在多个 mod 中存在，需要合并
                    mergeFiles(relPath, fileSources, mergedDir)
                }
            } catch (e: Exception) {
                ColorPrinter.error(Localizations.t("ENGINE_PROCESSING_ERROR", relPath, e.message))
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
        if (baseModManager.loaded) {
            try {
                val originalBaseModContent = baseModManager.extractFileContent(relPath)
                // 基准mod中存在该文件，需要进行对比合并
                if (originalBaseModContent != null) {
                    val context = MergerContext().also { it.baseModManager = baseModManager }
                    val mergerOptional = MergerFactory.getMerger(relPath, context)

                    // 如果支持合并，进行对比合并
                    if (mergerOptional.isPresent) {
                        val merger = mergerOptional.get()
                        val fileName = getEntryFileName(relPath)

                        val fileBase = MemoryFileTree(fileName, relPath, mutableListOf("data0.pak"), originalBaseModContent)

                        context.fileName = relPath
                        context.mod1Name = "data0.pak"
                        context.mod2Name = fileCurrent.getFirstArchiveFileName()
                        context.isFirstModMergeWithBaseMod = true // 标记为与data0.pak的合并

                        val result = merger.merge(fileBase, fileCurrent)
                        val mergedContent = result.mergedContent

                        // 写入合并结果
                        val targetPath = mergedOutputDir.resolve(relPath)
                        targetPath.parent.createDirectories()
                        targetPath.writeText(mergedContent)

                        this.mergedCount++
                        ColorPrinter.success(Localizations.t("ENGINE_MERGE_SUCCESS", context.fileName))
                        return
                    }
                }
            } catch (e: Exception) {
                ColorPrinter.error("Processing file '${relPath}' error, Reason: ${e.message}", e)
            }
        }
        // 没有基准mod，或者基准mod中不存在该文件，或者不支持合并，直接复制
        Tools.zeroCopy(fileCurrent.safeGetFullPathName(), mergedOutputDir.resolve(relPath))
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
            Tools.zeroCopy(fileSources.first().safeGetFullPathName(), mergedDir.resolve(relPath))
            return
        }

        val context = MergerContext()
        context.baseModManager = baseModManager
        val mergerOptional = MergerFactory.getMerger(relPath, context) //获取合并器

        //不支持合并的文件类型，直接让用户选择用哪个文件
        if (mergerOptional.isEmpty) {
            choiseWhichAssetToUse(relPath, fileSources, mergedDir)
            return
        }

        try {
            // 支持合并，开始处理合并逻辑
            ColorPrinter.info(Localizations.t("ENGINE_MERGING_FILE", relPath, fileSources.size))
            val merger = mergerOptional.get()
            var baseMergedContent = "" //基准文本内容

            var originalBaseModContent: String? = null
            if (baseModManager.loaded) {
                originalBaseModContent = baseModManager.extractFileContent(relPath)
            }
            val fileName = getEntryFileName(relPath)

            // 顺序合并：使用data0.pak作为基准（如果存在），然后依次合并各个mod
            for ((i, fileCurrent) in fileSources.withIndex()) {
                val currentModPath = fileCurrent.safeGetFullPathName()
                val currentModName = fileCurrent.getFirstArchiveFileName()

                // 第一个 mod：如果有data0.pak基准文件，使用它作为base与第一个mod合并
                if (i == 0) {
                    if (originalBaseModContent != null) {
                        val fileBase = MemoryFileTree(fileName, relPath, mutableListOf("data0.pak"), originalBaseModContent)

                        context.fileName = relPath
                        context.mod1Name = "data0.pak"
                        context.mod2Name = currentModName
                        context.isFirstModMergeWithBaseMod = true // 标记为第一个mod与data0.pak的合并

                        val result = merger.merge(fileBase, fileCurrent)
                        baseMergedContent = result.mergedContent
                    } else {
                        // 没有data0.pak基准文件，直接使用第一个mod的内容
                        baseMergedContent = currentModPath.readText()
                    }
                } else {
                    // 后续的 mod，与当前合并结果合并
                    val previousSource = fileSources[i - 1]
                    val previousModName = previousSource.getFirstArchiveFileName()

                    // 执行合并 - 使用真实的MOD压缩包名字
                    val fileBase = MemoryFileTree(fileName, relPath, mutableListOf("data0.pak"), baseMergedContent)

                    context.fileName = relPath
                    context.mod1Name = previousModName
                    context.mod2Name = currentModName
                    context.isFirstModMergeWithBaseMod = false // 后续合并正常处理冲突

                    val result = merger.merge(fileBase, fileCurrent)
                    baseMergedContent = result.mergedContent
                }
            }

            // 写入最终合并结果
            val targetPath = mergedDir.resolve(relPath)
            targetPath.parent.createDirectories()
            targetPath.writeText(baseMergedContent)

            this.mergedCount++
            ColorPrinter.success(Localizations.t("ENGINE_MERGE_SUCCESS", context.fileName))
        } catch (e: Exception) {
            ColorPrinter.error(Localizations.t("ENGINE_MERGE_FAILED", e.message))
            log.error("Failed to merge file '{}': {}", relPath, e.message)
            // todo 这里合并失败的策略还得再调整下，现在是失败时使用最后一个 mod 的版本
            val lastSource: PathFileTree = fileSources.last()
            Tools.zeroCopy(lastSource.safeGetFullPathName(), mergedDir.resolve(relPath))
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
        ColorPrinter.warning("\n${Localizations.t("ASSET_NOT_SUPPORT_FILE_EXTENSION", relPath)}")
        ColorPrinter.warning(Localizations.t("ASSET_CHOSE_WHICH_VERSION_TO_USE"))
        for ((i, fileTree) in fileSources.withIndex()) {
            ColorPrinter.info("{}. {}", i + 1, fileTree.getFirstArchiveFileName())
        }
        while (true) {
            val input = readln()
            if (input.matches("\\d+".toRegex())) {
                val choice = input.toInt()
                if (choice >= 1 && choice <= fileSources.size) {
                    val chosenSource = fileSources[choice - 1]
                    ColorPrinter.info(
                        Localizations.t(
                            "ASSET_USER_CHOSE_COMPLETE",
                            chosenSource.getFirstArchiveFileName(),
                        )
                    )
                    Tools.zeroCopy(chosenSource.safeGetFullPathName(), mergedDir.resolve(relPath))
                    return
                }
            }
            ColorPrinter.warning(Localizations.t("ASSET_INVALID_INPUT_PLEASE_ENTER_NUMBER", 1, fileSources.size))
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
        ColorPrinter.info("\n{}", "=".repeat(75))
        ColorPrinter.info(Localizations.t("ENGINE_STATISTICS_TITLE"))
        ColorPrinter.info(Localizations.t("ENGINE_TOTAL_FILES_PROCESSED", totalProcessed))
        ColorPrinter.success(Localizations.t("ENGINE_MERGED_NO_CONFLICTS", mergedCount))
        if (pathCorrectionCount > 0) {
            ColorPrinter.success(Localizations.t("ENGINE_PATH_CORRECTIONS_APPLIED", pathCorrectionCount))
        }
        ColorPrinter.info("{}", "=".repeat(75))
    }

    /**
     * 清理临时文件
     */
    private fun cleanupTempDir() {
        Tools.deleteRecursively(tempDir)
    }
}