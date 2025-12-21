package ankol.mod.merger.core;

import ankol.mod.merger.merger.MergeResult;
import ankol.mod.merger.tools.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
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
    private final FileMergeTracker fileMergeTracker;

    // 统计信息
    private int mergedCount = 0;      // 成功合并（无冲突）的文件数
    private int conflictCount = 0;    // 包含冲突的文件数
    private int copiedCount = 0;      // 直接复制的文件数（不可解析）
    private int totalProcessed = 0;   // 处理的文件总数
    private boolean hasAnyConflict = false;
    private int pathCorrectionCount = 0;  // 修正的路径数

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
        this.fileMergeTracker = new FileMergeTracker();
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
            //异步执行解压任务
            CompletableFuture<Void> task1 = CompletableFuture.runAsync(() -> {
                try {
                    if (baseModAnalyzer != null) {
                        baseModAnalyzer.load();
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
            // 把所有文件先解压到临时文件夹，生成映射路径（包含来源信息）
            CompletableFuture<Map<String, List<FileSource>>> task2 = CompletableFuture.supplyAsync(this::extractAllMods);
            CompletableFuture<Void> allOf = CompletableFuture.allOf(task1, task2);
            allOf.join();

            Map<String, List<FileSource>> filesByPath = task2.get(); //执行完成后获取结果

            // 3. 处理路径修正（如果有基准MOD）
            if (baseModAnalyzer != null && baseModAnalyzer.isLoaded()) {
                processPathCorrection(filesByPath);
            }

            // 4. 按文件名（而不是路径）分组，支持相同名字的文件合并
            reGroupFilesByName(filesByPath);

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
        } catch (ExecutionException | InterruptedException e) {
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
        ColorPrinter.warning("\n⚠️ Found {} path mismatches with base MOD", mismatches.size());
        ColorPrinter.warning("These files exist in mods but with different paths than base MOD:");

        for (var entry : mismatches.entrySet()) {
            ColorPrinter.warning("  ├─ Current: {}", entry.getKey());
            ColorPrinter.warning("  └─ Suggested: {}", entry.getValue());
        }

        // 询问用户选择修正策略
        ColorPrinter.info("\n📋 Select path correction strategy:");
        ColorPrinter.info("  1. {} (recommended)", PathCorrectionStrategy.Strategy.SMART_CORRECT.getDescription());
        ColorPrinter.info("  2. {}", PathCorrectionStrategy.Strategy.KEEP_ORIGINAL.getDescription());

        Scanner scanner = new Scanner(System.in);
        while (true) {
            ColorPrinter.info("Please enter your choice (1 or 2):");
            String input = scanner.next().trim();

            if (pathCorrectionStrategy.selectByCode(Integer.parseInt(input))) {
                ColorPrinter.success("✓ Strategy selected: {}", pathCorrectionStrategy.getSelectedStrategy().getDescription());
                break;
            } else {
                ColorPrinter.warning("❌ Invalid choice. Please enter 1 or 2");
            }
        }

        // 应用路径修正
        if (pathCorrectionStrategy.getSelectedStrategy() == PathCorrectionStrategy.Strategy.SMART_CORRECT) {
            ColorPrinter.info("\n🔧 Applying smart path correction...");
            for (var entry : mismatches.entrySet()) {
                String originalPath = entry.getKey();
                String correctedPath = entry.getValue();

                List<FileSource> sources = filesByPath.remove(originalPath);
                filesByPath.put(correctedPath, sources);
                pathCorrectionCount++;

                ColorPrinter.info("  ├─ {} → {}", originalPath, correctedPath);
            }
            ColorPrinter.success("✓ Corrected {} paths", pathCorrectionCount);
        } else {
            ColorPrinter.info("ℹ️ Keeping original paths from mods");
        }
    }

    /**
     * 重新按文件名（而不是路径）分组文件
     * 这样可以处理相同文件名但来自不同路径的情况
     */
    private void reGroupFilesByName(Map<String, List<FileSource>> filesByPath) {
        ColorPrinter.info("\n📊 Grouping files by name (for merge detection)...");

        for (var entry : filesByPath.entrySet()) {
            String path = entry.getKey();
            List<FileSource> sources = entry.getValue();

            for (FileSource source : sources) {
                // 提取文件名
                String fileName = path.substring(path.lastIndexOf("/") + 1).toLowerCase();

                // 添加到FileMergeTracker
                fileMergeTracker.addFile(path, source.filePath, source.sourceModName, source.sourceModName);
            }
        }

        fileMergeTracker.printReport();
    }

    /**
     * 从所有 mod 中提取文件，按相对路径分组
     */
    private Map<String, List<FileSource>> extractAllMods() {
        Map<String, List<FileSource>> filesByName = new LinkedHashMap<>();

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
                    // 如果是嵌套的，来源链为 "outer.zip -> inner.pak"，但我们记录modFileName作为源
                    String sourceChainString = sourceInfo.getSourceChainString();

                    // 创建FileSource，记录文件和其来源MOD（包括嵌套链）
                    FileSource fileSource = new FileSource(sourceInfo.getFilePath(), modFileName);
                    filesByName.computeIfAbsent(relPath, k -> new ArrayList<>()).add(fileSource);

                    // 如果是嵌套来源，输出详细日志
                    if (sourceInfo.isFromNestedArchive()) {
                        ColorPrinter.info("  └─ Nested: {} (from: {} → {})", relPath, modFileName, sourceChainString);
                    }
                }
                ColorPrinter.success("✓ Extracted {} files", extractedFiles.size());
            } catch (IOException e) {
                throw new RuntimeException(e);
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
        copiedCount++;
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
            Scanner scanner = new Scanner(System.in);
            // 不支持智能合并，使用最后一个 mod 的版本
            ColorPrinter.warning("\n" + Localizations.t("ASSET_NOT_SUPPORT_FILE_EXTENSION", relPath));
            ColorPrinter.warning(Localizations.t("ASSET_CHOSE_WHICH_VERSION_TO_USE"));
            for (int i = 0; i < fileSources.size(); i++) {
                FileSource fileSource = fileSources.get(i);
                ColorPrinter.info("{}. {}", i + 1, fileSource.sourceModName);
            }
            while (true) {
                String input = scanner.next();
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

                    // 创建临时文件存储前面的合并结果
                    Path tempBaseFile = Files.createTempFile("merge_base_", ".tmp");
                    Files.writeString(tempBaseFile, mergedContent);

                    try {
                        // 执行合并 - 使用真实的MOD压缩包名字
                        FileTree fileBase = new FileTree(previousModName, tempBaseFile.toString());
                        FileTree fileCurrent = new FileTree(currentModName, currentModPath.toString());

                        context.setFileName(relPath);
                        context.setMod1Name(previousModName);
                        context.setMod2Name(currentModName);

                        MergeResult result = merger.merge(fileBase, fileCurrent);
                        mergedContent = result.mergedContent;
                    } finally {
                        // 清理临时文件
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
        ColorPrinter.info("   Total files processed: {}", totalProcessed);
        ColorPrinter.success("✓  Merged (no conflicts): {}", mergedCount);
        ColorPrinter.warning("⚠️ Merged (with conflicts): {}", conflictCount);
        ColorPrinter.info("📄 Copied: {}", copiedCount);
        if (pathCorrectionCount > 0) {
            ColorPrinter.info("🔧 Path corrections applied: {}", pathCorrectionCount);
        }
        ColorPrinter.info("{}", "=".repeat(50));
        if (hasAnyConflict) {
            ColorPrinter.warning("\n⚠️ WARNING: Some conflicts were resolved.");
            ColorPrinter.warning("   Please review the merged files carefully!");
        } else {
            ColorPrinter.success("\n✅ Merge completed successfully with no conflicts!");
        }
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