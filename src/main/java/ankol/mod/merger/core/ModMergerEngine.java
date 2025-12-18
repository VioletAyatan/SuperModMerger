package ankol.mod.merger.core;

import ankol.mod.merger.merger.MergeResult;
import ankol.mod.merger.tools.FileTree;
import ankol.mod.merger.tools.PakManager;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.*;

/**
 * 模组合并引擎 - 负责执行模组合并的核心逻辑
 *
 * @author Ankol
 */
public class ModMergerEngine {

    private final List<Path> modsToMerge;
    private final Path outputPath;
    private final Path tempDir;

    // 统计信息
    private int mergedCount = 0;      // 成功合并（无冲突）的文件数
    private int conflictCount = 0;    // 包含冲突的文件数
    private int copiedCount = 0;      // 直接复制的文件数（不可解析）
    private int totalProcessed = 0;   // 处理的文件总数
    private boolean hasAnyConflict = false;

    /**
     * 构造函数 - 初始化合并引擎
     *
     * @param modsToMerge 要合并的 mod 列表（.pak 文件路径）
     * @param outputPath  最终输出的 .pak 文件路径
     */
    public ModMergerEngine(List<Path> modsToMerge, Path outputPath) {
        this.modsToMerge = modsToMerge;
        this.outputPath = outputPath;
        this.tempDir = Path.of(System.getProperty("java.io.tmpdir"), "ModMerger_" + System.currentTimeMillis());
    }

    /**
     * 执行合并操作
     */
    public void merge() throws IOException {
        System.out.println("====== Techland Mod Merger ======\n");

        if (modsToMerge.isEmpty()) {
            System.out.println("❌ No mods found to merge!");
            return;
        }

        System.out.println("📦 Found " + modsToMerge.size() + " mod(s) to merge:");
        for (int i = 0; i < modsToMerge.size(); i++) {
            System.out.println("  " + (i + 1) + ". " + modsToMerge.get(i).getFileName());
        }
        System.out.println();

        try {
            //从所有 mod 中提取文件
            Map<String, List<Path>> filesByName = extractAllMods();
            //创建输出目录
            Path mergedDir = tempDir.resolve("merged");
            Files.createDirectories(mergedDir);
            //开始合并文件
            processFiles(filesByName, mergedDir);
            //合并完成，打包
            System.out.println("\n📦 Creating merged PAK file...");
            PakManager.createPak(mergedDir, outputPath);
            System.out.println("✅ Merged PAK created: " + outputPath);

            // 5. 打印统计信息
            printStatistics();

        } finally {
            // 清理临时文件
            cleanupTempDir();
        }
    }

    /**
     * 从所有 mod 中提取文件，按文件名分组
     */
    private Map<String, List<Path>> extractAllMods() throws IOException {
        Map<String, List<Path>> filesByName = new LinkedHashMap<>();

        for (int i = 0; i < modsToMerge.size(); i++) {
            Path modPath = modsToMerge.get(i);
            String modName = "Mod" + (i + 1);
            Path modTempDir = tempDir.resolve(modName);

            System.out.println("📂 Extracting " + modPath.getFileName() + "...");
            Map<String, Path> extractedFiles = PakManager.extractPak(modPath, modTempDir);

            // 按文件名分组
            for (Map.Entry<String, Path> entry : extractedFiles.entrySet()) {
                String relPath = entry.getKey();
                Path filePath = entry.getValue();
                filesByName.computeIfAbsent(relPath, k -> new ArrayList<>()).add(filePath);
            }

            System.out.println("   ✓ Extracted " + extractedFiles.size() + " files");
        }

        return filesByName;
    }

    /**
     * 处理所有文件（合并或复制）
     */
    private void processFiles(Map<String, List<Path>> filesByName, Path mergedDir) throws IOException {
        System.out.println("\n🔄 Processing files...");

        for (Map.Entry<String, List<Path>> entry : filesByName.entrySet()) {
            String relPath = entry.getKey();
            List<Path> filePaths = entry.getValue();
            totalProcessed++;
            try {
                if (filePaths.size() == 1) {
                    // 只在一个 mod 中存在，直接复制
                    copyFile(relPath, filePaths.getFirst(), mergedDir);
                } else {
                    // 在多个 mod 中存在，需要合并
                    mergeFiles(relPath, filePaths, mergedDir);
                }
            } catch (Exception e) {
                System.err.println("❌ ERROR processing " + relPath + ": " + e.getMessage());
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
     */
    private void mergeFiles(String relPath, List<Path> filePaths, Path mergedDir) throws IOException {
        // 检查所有文件是否相同
        if (areAllFilesIdentical(filePaths)) {
            // 所有文件都相同，直接复制第一个
            copyFile(relPath, filePaths.getFirst(), mergedDir);
            return;
        }

        // 根据脚本名称获取合并器
        Optional<IFileMerger> merger = MergerFactory.getMerger(relPath);

        if (merger.isPresent()) {
            // 智能合并脚本文件
            System.out.println("🔀Merging: " + relPath);
            try {
                // 创建 FileTree 对象（保持向后兼容）
                FileTree file1 = new FileTree(
                        filePaths.get(0).getFileName().toString(),
                        filePaths.get(0).toString()
                );
                FileTree file2 = new FileTree(
                        filePaths.get(1).getFileName().toString(),
                        filePaths.get(1).toString()
                );

                // 执行合并
                MergeResult result = merger.get().merge(file1, file2);

                // 写入合并结果
                Path targetPath = mergedDir.resolve(relPath);
                Files.createDirectories(targetPath.getParent());
                Files.writeString(targetPath, result.mergedContent);

                if (result.hasConflicts) {
                    hasAnyConflict = true;
                    conflictCount++;
                    System.out.println("⚠️" + result.conflicts.size() + " conflict(s) resolved");
                } else {
                    mergedCount++;
                    System.out.println("✓Merged successfully");
                }
            } catch (Exception e) {
                System.err.println("❌Merge failed: " + e.getMessage());
                e.printStackTrace();
                // 失败时使用最后一个 mod 的版本
                copyFile(relPath, filePaths.get(filePaths.size() - 1), mergedDir);
            }
        } else {
            // 不支持智能合并，使用最后一个 mod 的版本
            System.out.println("📄Copying (non-mergeable): " + relPath + " (using last mod)");
            copyFile(relPath, filePaths.getLast(), mergedDir);
        }
    }

    /**
     * 检查多个文件是否内容相同
     */
    private boolean areAllFilesIdentical(List<Path> filePaths) throws IOException {
        if (filePaths.size() <= 1) return true;
        Path first = filePaths.getFirst();
        for (int i = 1; i < filePaths.size(); i++) {
            if (!PakManager.areFilesIdentical(first, filePaths.get(i))) {
                return false;
            }
        }
        return true;
    }

    /**
     * 打印合并统计信息
     */
    private void printStatistics() {
        System.out.println("\n" + "=".repeat(50));
        System.out.println("📊 Merge Statistics:");
        System.out.println("  Total files processed: " + totalProcessed);
        System.out.println("  ✓ Merged (no conflicts): " + mergedCount);
        System.out.println("  ⚠️  Merged (with conflicts): " + conflictCount);
        System.out.println("  📄 Copied: " + copiedCount);
        System.out.println("=".repeat(50));

        if (hasAnyConflict) {
            System.out.println("\n⚠️  WARNING: Some conflicts were resolved.");
            System.out.println("   Please review the merged files carefully!");
        } else {
            System.out.println("\n✅ Merge completed successfully with no conflicts!");
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
            System.err.println("Warning: Failed to clean temp directory: " + e.getMessage());
        }
    }
}