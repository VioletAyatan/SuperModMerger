package ankol.mod.merger.core;

import ankol.mod.merger.merger.MergeResult;
import ankol.mod.merger.merger.MergerFactory;
import ankol.mod.merger.tools.*;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

/**
 * 模组合并引擎 - 负责执行模组合并的核心逻辑
 *
 * @author Ankol
 */
@Slf4j
public class ModMergerEngine {

    private final List<Path> modsToMerge;
    private final Path outputPath;
    private final Path tempDir;

    // 基准MOD相关
    private final BaseModAnalyzer baseModAnalyzer;
    private final PathCorrectionStrategy pathCorrectionStrategy;

    // 统计信息
    private int mergedCount = 0;      // 成功合并（无冲突）的文件数
    private int totalProcessed = 0;   // 处理的文件总数
    private int pathCorrectionCount = 0;  // 修正的路径数

    // 全局Scanner（避免重复创建）
    private static final Scanner SYSTEM_SCANNER = new Scanner(System.in);


    /**
     * 构造函数 - 初始化合并引擎（带基准MOD）
     *
     * @param modsToMerge 要合并的 mod 列表（.pak 文件路径）
     * @param outputPath  最终输出的 .pak 文件路径
     * @param baseModPath 基准MOD文件路径（可为null）
     */
    public ModMergerEngine(List<Path> modsToMerge, Path outputPath, Path baseModPath) {
        this.modsToMerge = modsToMerge;
        this.outputPath = outputPath;
        this.tempDir = Path.of(Tools.getTempDir(), "ModMerger_" + System.currentTimeMillis());
        this.baseModAnalyzer = new BaseModAnalyzer(baseModPath);
        this.pathCorrectionStrategy = new PathCorrectionStrategy();
    }

    /**
     * 执行合并操作
     */
    public void merge() {
        ColorPrinter.info(Localizations.t("ENGINE_TITLE"));

        if (modsToMerge.isEmpty()) {
            ColorPrinter.error(Localizations.t("ENGINE_NO_MODS_FOUND"));
            return;
        }

        ColorPrinter.info(Localizations.t("ENGINE_FOUND_MODS_TO_MERGE", modsToMerge.size()));
        for (int i = 0; i < modsToMerge.size(); i++) {
            ColorPrinter.info(Localizations.t("ENGINE_MOD_LIST_ITEM", (i + 1), modsToMerge.get(i).getFileName()));
        }

        try {
            //初始化基准mod
            baseModAnalyzer.load();
            // 如果有基准MOD，先确定路径修正策略
            /*if (baseModAnalyzer.isLoaded()) {
                selectPathCorrectionStrategy();
            }*/
            // 在提取过程中对每个mod分别进行路径修正
            Map<String, List<FileTree>> filesByPath = extractAllMods();
            // 5. 输出目录（临时）
            Path mergedDir = tempDir.resolve("merged");
            Files.createDirectories(mergedDir);
            // 6. 开始合并文件
            processFiles(filesByPath, mergedDir);
            // 7. 合并完成，打包
            ColorPrinter.info(Localizations.t("ENGINE_CREATING_MERGED_PAK"));
            PakManager.createPak(mergedDir, outputPath);
            ColorPrinter.success(Localizations.t("ENGINE_MERGED_PAK_CREATED", outputPath));
            // 8. 打印统计信息
            printStatistics();
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            // 清理基准MOD缓存
            baseModAnalyzer.clearCache();
            // 清理临时文件
            cleanupTempDir();
        }
    }

    /**
     * 选择路径修正策略（在提取文件前）
     */
    private void selectPathCorrectionStrategy() {
        ColorPrinter.info(Localizations.t("ENGINE_SELECT_PATH_CORRECTION_STRATEGY"));
        ColorPrinter.success(Localizations.t("ENGINE_STRATEGY_OPTION_1", PathCorrectionStrategy.Strategy.SMART_CORRECT.getDescription()));
        ColorPrinter.info(Localizations.t("ENGINE_STRATEGY_OPTION_2", PathCorrectionStrategy.Strategy.KEEP_ORIGINAL.getDescription()));
        // 优化：使用全局Scanner避免资源泄漏
        while (true) {
            ColorPrinter.info(Localizations.t("ENGINE_INPUT_CHOICE_PROMPT"));
            String input = SYSTEM_SCANNER.next().trim();
            try {
                if (pathCorrectionStrategy.selectByCode(Integer.parseInt(input))) {
                    ColorPrinter.success(Localizations.t("ENGINE_STRATEGY_SELECTED", pathCorrectionStrategy.getSelectedStrategy().getDescription()));
                    break;
                }
            } catch (NumberFormatException e) {
                // 继续循环
            }
            ColorPrinter.warning(Localizations.t("ENGINE_INVALID_CHOICE"));
        }
    }

    /**
     * 对单个MOD的文件路径进行修正
     *
     * @param modFileName    MOD文件名
     * @param extractedFiles 提取的文件映射（相对路径 -> FileSourceInfo）
     * @return 修正后的文件映射
     */
    private Map<String, FileTree> correctPathsForMod(String modFileName, Map<String, FileTree> extractedFiles) {
        if (!baseModAnalyzer.isLoaded() ||
                pathCorrectionStrategy.getSelectedStrategy() != PathCorrectionStrategy.Strategy.SMART_CORRECT
        ) {
            return extractedFiles;
        }

        Map<String, FileTree> correctedFiles = new LinkedHashMap<>();
        Map<String, String> corrections = new LinkedHashMap<>();

        HashSet<String> markToRemoved = new HashSet<>();
        // 查找需要修正的路径
        for (Map.Entry<String, FileTree> entry : extractedFiles.entrySet()) {
            String fileEntryName = entry.getKey();
            FileTree sourceInfo = entry.getValue();
            try {
                if (baseModAnalyzer.hasPathConflict(fileEntryName)) {
                    String suggestedPath = baseModAnalyzer.getSuggestedPath(fileEntryName);
                    corrections.put(fileEntryName, suggestedPath);
                    correctedFiles.put(suggestedPath, sourceInfo);
                } else {
                    correctedFiles.put(fileEntryName, sourceInfo);
                }
            } catch (NoSuchFileException e) {
                markToRemoved.add(fileEntryName);
                log.warn("File '{}' from mod '{}' does not exist in base mod, marking for removal.", fileEntryName, modFileName);
            }
        }
        markToRemoved.forEach(extractedFiles::remove); //移除不存在于基准MOD中的文件

        // 如果有路径被修正，输出日志
        if (!corrections.isEmpty()) {
            ColorPrinter.info(Localizations.t("ENGINE_PATH_CORRECTIONS_FOR_MOD", modFileName));
            for (Map.Entry<String, String> entry : corrections.entrySet()) {
                ColorPrinter.success(Localizations.t("ENGINE_PATH_CORRECTION_ITEM", entry.getKey(), entry.getValue()));
                pathCorrectionCount++;
            }
        }

        return correctedFiles;
    }

    /**
     * 从所有 mod 中提取文件，按相对路径分组
     * 在提取过程中对每个mod分别进行路径修正，避免不同mod的同名文件冲突
     */
    private Map<String, List<FileTree>> extractAllMods() {
        Map<String, List<FileTree>> filesByName = new ConcurrentHashMap<>(); // 优化：使用线程安全集合
        AtomicInteger index = new AtomicInteger(0);
        //并发提取所有文件
        modsToMerge.parallelStream().forEach((modPath) -> {
            try {
                String archiveName = modPath.getFileName().toString(); //解压的压缩包真实名称
                Path modTempDir = tempDir.resolve(archiveName + index.getAndIncrement()); //生成临时目录名字

                Map<String, FileTree> extractedFiles = PakManager.extractPak(modPath, modTempDir);
                Map<String, FileTree> correctedFiles = correctPathsForMod(archiveName, extractedFiles);

                // 按文件路径分组，并记录来源MOD名字
                for (Map.Entry<String, FileTree> entry : correctedFiles.entrySet()) {
                    String relPath = entry.getKey();
                    FileTree sourceInfo = entry.getValue();
                    // 创建FileSource，记录文件和其来源MOD
                    filesByName.computeIfAbsent(relPath, k -> new ArrayList<>()).add(sourceInfo);
                }
                ColorPrinter.success(Localizations.t("ENGINE_EXTRACTED_FILES", correctedFiles.size()));
            } catch (IOException e) {
                throw new CompletionException(Localizations.t("ENGINE_EXTRACT_FAILED", modPath.getFileName()), e);
            }
        });
        return filesByName;
    }

    /**
     * 处理所有文件（合并或复制）
     */
    private void processFiles(Map<String, List<FileTree>> filesByName, Path mergedDir) {
        ColorPrinter.info(Localizations.t("ENGINE_PROCESSING_FILES"));
        for (Map.Entry<String, List<FileTree>> entry : filesByName.entrySet()) {
            String relPath = entry.getKey();
            List<FileTree> fileSources = entry.getValue();
            totalProcessed++;
            try {
                //todo 这里未来可以添加一个自动修正旧版本的mod的功能，因为我合并的逻辑是从基准mod里取得原文件，肯定是最新的，刚好能把一些过期mod没有的参数补上
                //todo 但是对于性能的消耗也会增加很多，文件越多消耗时间越久，后期看下可以做个可选开关
                if (fileSources.size() == 1) {
                    // 即使只有一个mod文件，也需要与基准mod对比（如果基准mod存在）
                    copyFile(relPath, fileSources.getFirst().getFullPathName(), mergedDir);
//                    processSingleFile(relPath, fileSources.getFirst(), mergedDir);
                } else {
                    // 在多个 mod 中存在，需要合并
                    mergeFiles(relPath, fileSources, mergedDir);
                }
            } catch (Exception e) {
                ColorPrinter.error(Localizations.t("ENGINE_PROCESSING_ERROR", relPath, e.getMessage()));
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
    private void processSingleFile(String relPath, FileTree fileCurrent, Path mergedOutputDir) throws IOException {
        // 如果基准mod存在，尝试与基准mod对比
        if (baseModAnalyzer.isLoaded()) {
            try {
                String originalBaseModContent = baseModAnalyzer.extractFileContent(relPath);
                // 基准mod中存在该文件，需要进行对比合并
                if (originalBaseModContent != null) {
                    MergerContext context = new MergerContext();
                    Optional<FileMerger> mergerOptional = MergerFactory.getMerger(relPath, context);

                    // 如果支持合并，进行对比合并
                    if (mergerOptional.isPresent()) {
                        FileMerger merger = mergerOptional.get();
                        String fileName = Tools.getEntryFileName(relPath);

                        Path tempBaseFile = Files.createTempFile("merge_base_data0_", ".tmp");
                        try {
                            Files.writeString(tempBaseFile, originalBaseModContent);

                            FileTree fileBase = new FileTree(fileName, relPath, "data0.pak", tempBaseFile);

                            context.setFileName(relPath);
                            context.setMod1Name("data0.pak");
                            context.setMod2Name(fileCurrent.getArchiveFileName());
                            context.setOriginalBaseModContent(originalBaseModContent);
                            context.setFirstModMergeWithBaseMod(true); // 标记为与data0.pak的合并

                            MergeResult result = merger.merge(fileBase, fileCurrent);
                            String mergedContent = result.mergedContent();

                            // 写入合并结果
                            Path targetPath = mergedOutputDir.resolve(relPath);
                            Files.createDirectories(targetPath.getParent());
                            Files.writeString(targetPath, mergedContent);

                            this.mergedCount++;
                            ColorPrinter.success(Localizations.t("ENGINE_MERGE_SUCCESS"));
                            return;
                        } finally {
                            Files.deleteIfExists(tempBaseFile);
                        }
                    }
                }
            } catch (NoSuchFileException e) {
                // 基准mod中不存在该文件，直接复制
                log.debug("File '{}' not found in base mod, copying directly", relPath);
            } catch (Exception e) {
                ColorPrinter.warning("Failed to merge '{}' with base mod: {}, copying original file", relPath, e.getMessage());
            }
        }
        // 没有基准mod，或者基准mod中不存在该文件，或者不支持合并，直接复制
        copyFile(relPath, fileCurrent.getFullPathName(), mergedOutputDir);
    }

    /**
     * 复制单个文件
     */
    private void copyFile(String relPath, Path sourcePath, Path mergedOutputDir) throws IOException {
        Path targetPath = mergedOutputDir.resolve(relPath);
        Files.createDirectories(targetPath.getParent());
        Files.copy(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
    }

    /**
     * 合并多个同名文件
     * 对MOD进行顺序合并
     *
     * @param relPath     相对路径
     * @param fileSources 待合并的同名文件的来源
     * @param mergedDir   合并输出目录
     */
    private void mergeFiles(String relPath, List<FileTree> fileSources, Path mergedDir) throws IOException {
        // 先简单的判断一下文件内容（计算hash值）、大小是否相同，不同肯定不一样
        if (areAllFilesIdentical(fileSources)) {
            // 文件都一样，直接使用第一个
            copyFile(relPath, fileSources.getFirst().getFullPathName(), mergedDir);
            return;
        }

        MergerContext context = new MergerContext();
        Optional<FileMerger> mergerOptional = MergerFactory.getMerger(relPath, context);

        //不支持进行冲突对比的文本，让用户选择使用哪个版本
        if (mergerOptional.isEmpty()) {
            ColorPrinter.warning("\n" + Localizations.t("ASSET_NOT_SUPPORT_FILE_EXTENSION", relPath));
            ColorPrinter.warning(Localizations.t("ASSET_CHOSE_WHICH_VERSION_TO_USE"));
            for (int i = 0; i < fileSources.size(); i++) {
                FileTree fileTree = fileSources.get(i);
                ColorPrinter.info("{}. {}", i + 1, fileTree.getArchiveFileName());
            }
            while (true) {
                String input = SYSTEM_SCANNER.next();
                if (input.matches("\\d+")) {
                    int choice = Integer.parseInt(input);
                    if (choice >= 1 && choice <= fileSources.size()) {
                        FileTree chosenSource = fileSources.get(choice - 1);
                        ColorPrinter.info(Localizations.t("ASSET_USER_CHOSE_COMPLETE", chosenSource.getArchiveFileName()));
                        copyFile(relPath, chosenSource.getFullPathName(), mergedDir);
                        return;
                    }
                }
                ColorPrinter.warning(Localizations.t("ASSET_INVALID_INPUT_PLEASE_ENTER_NUMBER", 1, fileSources.size()));
            }
        }

        try {
            // 支持合并，开始处理合并逻辑
            ColorPrinter.info(Localizations.t("ENGINE_MERGING_FILE", relPath, fileSources.size()));
            FileMerger merger = mergerOptional.get();
            String baseMergedContent = ""; //基准文本内容

            String originalBaseModContent = null;
            if (baseModAnalyzer.isLoaded()) {
                originalBaseModContent = baseModAnalyzer.extractFileContent(relPath);
            }
            String fileName = Tools.getEntryFileName(relPath);

            // 顺序合并：使用data0.pak作为基准（如果存在），然后依次合并各个mod
            for (int i = 0; i < fileSources.size(); i++) {
                FileTree fileCurrent = fileSources.get(i); //当前处理的合并文件
                Path currentModPath = fileCurrent.getFullPathName();
                String currentModName = fileCurrent.getArchiveFileName();

                if (i == 0) {
                    // 第一个 mod：如果有data0.pak基准文件，使用它作为base与第一个mod合并
                    if (originalBaseModContent != null) {
                        Path tempBaseFile = Files.createTempFile("merge_base_data0_", ".tmp");
                        try {
                            Files.writeString(tempBaseFile, originalBaseModContent);
                            FileTree fileBase = new FileTree(fileName, relPath, "data0.pak", tempBaseFile);

                            context.setFileName(relPath);
                            context.setMod1Name("data0.pak");
                            context.setMod2Name(currentModName);
                            context.setOriginalBaseModContent(originalBaseModContent);
                            context.setFirstModMergeWithBaseMod(true); // 标记为第一个mod与data0.pak的合并

                            MergeResult result = merger.merge(fileBase, fileCurrent);
                            baseMergedContent = result.mergedContent();
                        } finally {
                            Files.deleteIfExists(tempBaseFile);
                        }
                    } else {
                        // 没有data0.pak基准文件，直接使用第一个mod的内容
                        baseMergedContent = Files.readString(currentModPath);
                    }
                } else {
                    // 后续的 mod，与当前合并结果合并
                    FileTree previousSource = fileSources.get(i - 1);
                    String previousModName = previousSource.getArchiveFileName();

                    Path tempBaseFile = Files.createTempFile("merge_base_", ".tmp");
                    try {
                        Files.writeString(tempBaseFile, baseMergedContent);
                        // 执行合并 - 使用真实的MOD压缩包名字
                        FileTree fileBase = new FileTree(fileName, relPath, "data0.pak", tempBaseFile);

                        context.setFileName(relPath);
                        context.setMod1Name(previousModName);
                        context.setMod2Name(currentModName);
                        context.setOriginalBaseModContent(originalBaseModContent); // 设置基准MOD文件内容
                        context.setFirstModMergeWithBaseMod(false); // 后续合并正常处理冲突

                        MergeResult result = merger.merge(fileBase, fileCurrent);
                        baseMergedContent = result.mergedContent();
                    } finally {
                        // 确保临时文件被删除
                        Files.deleteIfExists(tempBaseFile);
                    }
                }
            }

            // 写入最终合并结果
            Path targetPath = mergedDir.resolve(relPath);
            Files.createDirectories(targetPath.getParent());
            Files.writeString(targetPath, baseMergedContent);

            this.mergedCount++;
            ColorPrinter.success(Localizations.t("ENGINE_MERGE_SUCCESS"));
        } catch (Exception e) {
            ColorPrinter.error(Localizations.t("ENGINE_MERGE_FAILED", e.getMessage()));
            log.error("Failed to merge file '{}': {}", relPath, e.getMessage());
            // 失败时使用最后一个 mod 的版本
            FileTree lastSource = fileSources.getLast();
            copyFile(relPath, lastSource.getFullPathName(), mergedDir);
        }
    }

    /**
     * 检查多个文件是否内容相同
     */
    private boolean areAllFilesIdentical(List<FileTree> fileSources) throws IOException {
        if (fileSources.size() <= 1) {
            return true;
        }
        Path first = fileSources.getFirst().getFullPathName();
        for (int i = 1; i < fileSources.size(); i++) {
            if (!PakManager.areFilesIdentical(first, fileSources.get(i).getFullPathName())) {
                return false;
            }
        }
        return true;
    }

    /**
     * 打印合并统计信息
     */
    private void printStatistics() {
        ColorPrinter.info("\n{}", "=".repeat(75));
        ColorPrinter.info(Localizations.t("ENGINE_STATISTICS_TITLE"));
        ColorPrinter.info(Localizations.t("ENGINE_TOTAL_FILES_PROCESSED", totalProcessed));
        ColorPrinter.success(Localizations.t("ENGINE_MERGED_NO_CONFLICTS", mergedCount));
        if (pathCorrectionCount > 0) {
            ColorPrinter.success(Localizations.t("ENGINE_PATH_CORRECTIONS_APPLIED", pathCorrectionCount));
        }
        ColorPrinter.info("{}", "=".repeat(75));
    }

    /**
     * 清理临时文件
     */
    private void cleanupTempDir() {
        if (Files.exists(tempDir)) {
            try (Stream<Path> pathStream = Files.walk(tempDir)) {
                pathStream.sorted(Comparator.reverseOrder())
                        .forEach(path -> {
                            try {
                                Files.delete(path);
                            } catch (IOException e) {
                                // 忽略删除错误
                            }
                        });
            } catch (Exception e) {
                ColorPrinter.warning(Localizations.t("ENGINE_CLEANUP_FAILED", e.getMessage()));
            }
        }
    }
}