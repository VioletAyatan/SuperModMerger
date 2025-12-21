package ankol.mod.merger.core;

import ankol.mod.merger.merger.MergeResult;
import ankol.mod.merger.tools.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 模组合并引擎 - 负责执行模组合并的核心逻辑
 *
 * @author Ankol
 */
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
     * 文件来源信息 - 记录文件路径及其来源的MOD压缩包名字
     * 用于在合并时准确识别冲突来自哪个MOD
     *
     * @param filePath      文件实际路径
     * @param sourceModName 文件来源的MOD压缩包名字（如 "data2.pak"）
     */
    private record FileSource(Path filePath, String sourceModName) {
        @Override
        public String toString() {
            return sourceModName + ": " + filePath;
        }
    }


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
        this.baseModAnalyzer = baseModPath != null ? new BaseModAnalyzer(baseModPath) : null;
        this.pathCorrectionStrategy = new PathCorrectionStrategy();
    }

    /**
     * 构造函数 - 初始化合并引擎（不使用基准MOD）
     *
     * @param modsToMerge 要合并的 mod 列表（.pak 文件路径）
     * @param outputPath  最终输出的 .pak 文件路径
     */
    public ModMergerEngine(List<Path> modsToMerge, Path outputPath) {
        this(modsToMerge, outputPath, null);
    }

    /**
     * 执行合并操作
     */
    public void merge() throws IOException {
        ColorPrinter.info("====== Techland Mod Merger ======");

        if (modsToMerge.isEmpty()) {
            ColorPrinter.error("❌ No mods found to merge!");
            return;
        }

        ColorPrinter.info("Found {} mod(s) to merge:", modsToMerge.size());
        for (int i = 0; i < modsToMerge.size(); i++) {
            ColorPrinter.info("{}. {}", (i + 1), modsToMerge.get(i).getFileName());
        }

        try {
            if (baseModAnalyzer != null) {
                baseModAnalyzer.load();
                baseModAnalyzer.printAnalysisReport();
            }
            // 把所有文件先解压到临时文件夹，生成映射路径（包含来源信息）
            Map<String, List<FileSource>> filesByPath = extractAllMods();

            // 3. 处理路径修正（如果有基准MOD）
            if (baseModAnalyzer != null && baseModAnalyzer.isLoaded()) {
                processPathCorrection(filesByPath);
            }

            // 5. 输出目录（临时）
            Path mergedDir = tempDir.resolve("merged");
            Files.createDirectories(mergedDir);

            // 6. 开始合并文件
            processFiles(filesByPath, mergedDir);

            // 7. 合并完成，打包
            ColorPrinter.info("📦 Creating merged PAK file...");
            PakManager.createPak(mergedDir, outputPath);
            ColorPrinter.success("✅ Merged PAK created: {}", outputPath);

            // 8. 打印统计信息
            printStatistics();
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            // 清理临时文件
            cleanupTempDir();
        }
    }

    /**
     * 处理路径修正 - 根据基准MOD修正待合并MOD中的错误路径
     */
    private void processPathCorrection(Map<String, List<FileSource>> filesByPath) {
        ColorPrinter.info("\n🔍 Checking for path mismatches with base MOD...");

        // 查找所有需要修正的路径
        Map<String, String> mismatches = new HashMap<>();
        for (String path : filesByPath.keySet()) {
            if (baseModAnalyzer.hasPathConflict(path)) {
                String suggestedPath = baseModAnalyzer.getSuggestedPath(path);
                mismatches.put(path, suggestedPath);
            }
        }

        if (mismatches.isEmpty()) {
            ColorPrinter.success("✓ No path mismatches found");
            return;
        }

        // 发现路径冲突，提示用户选择修正策略
        ColorPrinter.warning("\nFound {} path mismatches with base MOD", mismatches.size());
        ColorPrinter.warning("These files exist in mods but with different paths than base MOD:");

        for (var entry : mismatches.entrySet()) {
            ColorPrinter.warning("  ├─ Current: {}", entry.getKey());
            ColorPrinter.success("  └─ Suggested: {}", entry.getValue());
        }

        // 询问用户选择修正策略
        ColorPrinter.info("\n📋 Select path correction strategy:");
        ColorPrinter.success("  1. {} (recommended)", PathCorrectionStrategy.Strategy.SMART_CORRECT.getDescription());
        ColorPrinter.info("  2. {}", PathCorrectionStrategy.Strategy.KEEP_ORIGINAL.getDescription());

        // 优化：使用全局Scanner避免资源泄漏
        while (true) {
            ColorPrinter.info("Please enter your choice (1 or 2):");
            String input = SYSTEM_SCANNER.next().trim();

            try {
                if (pathCorrectionStrategy.selectByCode(Integer.parseInt(input))) {
                    ColorPrinter.success("✓ Strategy selected: {}", pathCorrectionStrategy.getSelectedStrategy().getDescription());
                    break;
                }
            } catch (NumberFormatException e) {
                // 继续循环
            }
            ColorPrinter.warning("❌ Invalid choice. Please enter 1 or 2");
        }

        // 应用路径修正
        if (pathCorrectionStrategy.getSelectedStrategy() == PathCorrectionStrategy.Strategy.SMART_CORRECT) {
            ColorPrinter.info("\n🔧Applying smart path correction...");
            for (var entry : mismatches.entrySet()) {
                String originalPath = entry.getKey();
                String correctedPath = entry.getValue();

                List<FileSource> sources = filesByPath.remove(originalPath);
                filesByPath.put(correctedPath, sources);
                pathCorrectionCount++;

                ColorPrinter.success("  ├─ {} → {}", originalPath, correctedPath);
            }
            ColorPrinter.success("✓ Corrected {} paths", pathCorrectionCount);
        } else {
            ColorPrinter.info("ℹ️ Keeping original paths from mods");
        }
    }

    /**
     * 提取文件名的工具方法（优化：避免重复代码）
     */
    private static String extractFileName(String path) {
        int lastSlash = path.lastIndexOf("/");
        return (lastSlash >= 0 ? path.substring(lastSlash + 1) : path).toLowerCase();
    }

    /**
     * 从所有 mod 中提取文件，按相对路径分组
     */
    private Map<String, List<FileSource>> extractAllMods() {
        Map<String, List<FileSource>> filesByName = new ConcurrentHashMap<>(); // 优化：使用线程安全集合

        AtomicInteger index = new AtomicInteger(0);
        //并发提取所有MOD文件
        modsToMerge.parallelStream().forEach((modPath) -> {
            try {
                String modFileName = modPath.getFileName().toString(); //文件真实名称（用作来源标识）
                String modTempDirName = "Mod" + (index.getAndIncrement() + 1);               // 临时目录名（如 Mod1）
                Path modTempDir = tempDir.resolve(modTempDirName);

                ColorPrinter.info("Extracting {}...", modFileName);
                Map<String, FileSourceInfo> extractedFiles = PakManager.extractPak(modPath, modTempDir);
                // 按文件路径分组，并记录来源MOD名字
                for (Map.Entry<String, FileSourceInfo> entry : extractedFiles.entrySet()) {
                    String relPath = entry.getKey();
                    FileSourceInfo sourceInfo = entry.getValue();

                    // 构建完整的来源信息：记录真实的MOD压缩包名称
                    String sourceChainString = sourceInfo.getSourceChainString();

                    // 创建FileSource，记录文件和其来源MOD
                    FileSource fileSource = new FileSource(sourceInfo.getFilePath(), modFileName);
                    filesByName.computeIfAbsent(relPath, k -> new ArrayList<>()).add(fileSource);

                    // 如果是嵌套来源，输出详细日志
                    if (sourceInfo.isFromNestedArchive()) {
                        ColorPrinter.info("  └─ Nested: {} (from: {} → {})", relPath, modFileName, sourceChainString);
                    }
                }
                ColorPrinter.success("✓ Extracted {} files", extractedFiles.size());
            } catch (IOException e) {
                throw new CompletionException("Failed to extract mod: " + modPath.getFileName(), e);
            }
        });
        return filesByName;
    }

    /**
     * 处理所有文件（合并或复制）
     */
    private void processFiles(Map<String, List<FileSource>> filesByName, Path mergedDir) {
        ColorPrinter.info("🔄 Processing files...");

        for (Map.Entry<String, List<FileSource>> entry : filesByName.entrySet()) {
            String relPath = entry.getKey();
            List<FileSource> fileSources = entry.getValue();
            totalProcessed++;
            try {
                if (fileSources.size() == 1) {
                    copyFile(relPath, fileSources.getFirst().filePath, mergedDir);
                } else {
                    // 在多个 mod 中存在，需要合并
                    mergeFiles(relPath, fileSources, mergedDir);
                }
            } catch (Exception e) {
                ColorPrinter.error("❌ ERROR processing {}: {}", relPath, e.getMessage());
            }
        }
    }

    /**
     * 复制单个文件
     */
    private void copyFile(String relPath, Path sourcePath, Path mergedDir) throws IOException {
        Path targetPath = mergedDir.resolve(relPath);
        Files.createDirectories(targetPath.getParent());
        Files.copy(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
    }

    /**
     * 合并多个同名文件
     * <p>
     * 优化：支持合并 N 个文件（不仅仅是 2 个）
     * 采用顺序合并策略：
     * 1. Mod1 + Mod2 → 中间结果
     * 2. 中间结果 + Mod3 → 最终结果
     * ...依此类推
     * <p>
     * 这样可以处理任意数量的 mod 合并场景。
     *
     * @param relPath     相对路径
     * @param fileSources 同名文件的来源信息列表（包含文件路径和来源MOD名字）
     * @param mergedDir   合并输出目录
     */
    private void mergeFiles(String relPath, List<FileSource> fileSources, Path mergedDir) throws IOException {
        // 检查所有文件是否相同
        if (areAllFilesIdentical(fileSources)) {
            // 所有文件都相同，直接复制第一个
            copyFile(relPath, fileSources.getFirst().filePath, mergedDir);
            return;
        }

        // 获取合并器
        MergerContext context = new MergerContext();
        Optional<IFileMerger> mergerOptional = MergerFactory.getMerger(relPath, context);

        //不支持冲突检测的文件类型，直接让用户选择使用哪个mod的版本
        if (mergerOptional.isEmpty()) {
            // 优化：使用全局Scanner避免资源泄漏
            ColorPrinter.warning("\n" + Localizations.t("ASSET_NOT_SUPPORT_FILE_EXTENSION", relPath));
            ColorPrinter.warning(Localizations.t("ASSET_CHOSE_WHICH_VERSION_TO_USE"));
            for (int i = 0; i < fileSources.size(); i++) {
                FileSource fileSource = fileSources.get(i);
                ColorPrinter.info("{}. {}", i + 1, fileSource.sourceModName);
            }
            while (true) {
                String input = SYSTEM_SCANNER.next();
                if (input.matches("\\d+")) {
                    int choice = Integer.parseInt(input);
                    if (choice >= 1 && choice <= fileSources.size()) {
                        FileSource chosenSource = fileSources.get(choice - 1);
                        ColorPrinter.info(Localizations.t("ASSET_USER_CHOSE_COMPLTED", chosenSource.sourceModName));
                        copyFile(relPath, chosenSource.filePath, mergedDir);
                        return;
                    }
                }
                ColorPrinter.warning(Localizations.t("ASSET_INVALID_INPUT_PLEASE_ENTER_NUMBER", 1, fileSources.size()));
            }
        }

        // 智能合并脚本文件
        ColorPrinter.info("🔀Merging: {} ({} mods)", relPath, fileSources.size());

        try {
            IFileMerger merger = mergerOptional.get();
            String mergedContent = null;

            // 顺序合并：FileSource[0] + FileSource[1] + FileSource[2] + ...
            for (int i = 0; i < fileSources.size(); i++) {
                FileSource currentSource = fileSources.get(i);
                Path currentModPath = currentSource.filePath;
                String currentModName = currentSource.sourceModName;

                if (i == 0) {
                    // 第一个 mod，直接读取作为基准
                    mergedContent = Files.readString(currentModPath);
                } else {
                    // 后续的 mod，与当前合并结果合并
                    FileSource previousSource = fileSources.get(i - 1);
                    String previousModName = previousSource.sourceModName;

                    Path tempBaseFile = Files.createTempFile("merge_base_", ".tmp");
                    try {
                        Files.writeString(tempBaseFile, mergedContent);
                        // 执行合并 - 使用真实的MOD压缩包名字
                        FileTree fileBase = new FileTree(previousModName, tempBaseFile.toString());
                        FileTree fileCurrent = new FileTree(currentModName, currentModPath.toString());

                        context.setFileName(relPath);
                        context.setMod1Name(previousModName);
                        context.setMod2Name(currentModName);

                        MergeResult result = merger.merge(fileBase, fileCurrent);
                        mergedContent = result.mergedContent();
                    } finally {
                        // 确保临时文件被删除
                        Files.deleteIfExists(tempBaseFile);
                    }
                }
            }

            // 写入最终合并结果
            Path targetPath = mergedDir.resolve(relPath);
            Files.createDirectories(targetPath.getParent());
            Files.writeString(targetPath, mergedContent);

            this.mergedCount++;
            ColorPrinter.success("✓ Merged successfully");
        } catch (Exception e) {
            ColorPrinter.error("❌ Merge failed: {}", e.getMessage());
            e.printStackTrace();
            // 失败时使用最后一个 mod 的版本
            FileSource lastSource = fileSources.getLast();
            copyFile(relPath, lastSource.filePath, mergedDir);
        }
    }

    /**
     * 检查多个文件是否内容相同
     */
    private boolean areAllFilesIdentical(List<FileSource> fileSources) throws IOException {
        if (fileSources.size() <= 1) {
            return true;
        }
        Path first = fileSources.getFirst().filePath;
        for (int i = 1; i < fileSources.size(); i++) {
            if (!PakManager.areFilesIdentical(first, fileSources.get(i).filePath)) {
                return false;
            }
        }
        return true;
    }

    /**
     * 打印合并统计信息
     */
    private void printStatistics() {
        ColorPrinter.info("\n{}", "=".repeat(50));
        ColorPrinter.info("📊 Merge Statistics:");
        ColorPrinter.info("Total files processed: {}", totalProcessed);
        ColorPrinter.success("Merged (no conflicts): {}", mergedCount);
        ColorPrinter.info("{}", "=".repeat(50));
    }

    /**
     * 清理临时文件
     */
    private void cleanupTempDir() {
        try {
            if (Files.exists(tempDir)) {
                Files.walk(tempDir)
                        .sorted(Comparator.reverseOrder())
                        .forEach(path -> {
                            try {
                                Files.delete(path);
                            } catch (IOException e) {
                                // 忽略删除错误
                            }
                        });
            }
        } catch (Exception e) {
            ColorPrinter.warning("Warning: Failed to clean temp directory: {}", e.getMessage());
        }
    }
}