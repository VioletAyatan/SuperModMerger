package ankol.mod.merger.core;

import ankol.mod.merger.tools.ColorPrinter;
import lombok.Getter;

import java.nio.file.Path;
import java.util.*;

/**
 * 文件合并追踪器 - 用于追踪按文件名分组的文件
 * <p>
 * 游戏加载时只看文件名，不看路径。因此这个追踪器会将所有相同名字的文件
 * （无论来自哪个MOD或哪个路径）分组在一起，以便进行智能合并。
 *
 * @author Ankol
 */
public class FileMergeTracker {

    /**
     * 文件名 → 文件信息列表的映射
     * 键：文件名（小写，用于不区分大小写的比对）
     * 值：文件信息列表（相同名字但可能来自不同路径和MOD）
     */
    private final Map<String, List<FileMergeInfo>> filesByName;

    /**
     * 文件合并信息内部类
     */
    @Getter
    public static class FileMergeInfo {
        /**
         * 文件名（小写）
         */
        private final String fileName;

        /**
         * 相对路径
         */
        private final String relativePath;

        /**
         * 实际文件路径
         */
        private final Path actualPath;

        /**
         * 来源MOD名字
         */
        private final String sourceModName;

        /**
         * 来源链（用于嵌套压缩包）
         */
        private final String sourceChain;

        public FileMergeInfo(String relativePath, Path actualPath, String sourceModName, String sourceChain) {
            this.relativePath = relativePath;
            this.actualPath = actualPath;
            this.sourceModName = sourceModName;
            this.sourceChain = sourceChain;
            // 提取文件名，使用小写用于不区分大小写的比对
            this.fileName = relativePath.substring(relativePath.lastIndexOf("/") + 1).toLowerCase();
        }

        /**
         * 判断两个文件是否是同一文件（相同的文件名）
         */
        public boolean isSameFile(FileMergeInfo other) {
            return this.fileName.equals(other.fileName);
        }

        @Override
        public String toString() {
            return String.format("%s [from: %s, path: %s]", fileName, sourceChain, relativePath);
        }
    }

    public FileMergeTracker() {
        this.filesByName = new LinkedHashMap<>();
    }

    /**
     * 添加文件到追踪器
     *
     * @param relativePath 文件相对路径
     * @param actualPath   文件实际路径
     * @param sourceModName 来源MOD名字
     * @param sourceChain   来源链
     */
    public void addFile(String relativePath, Path actualPath, String sourceModName, String sourceChain) {
        FileMergeInfo info = new FileMergeInfo(relativePath, actualPath, sourceModName, sourceChain);
        filesByName.computeIfAbsent(info.fileName, k -> new ArrayList<>()).add(info);
    }

    /**
     * 获取具有相同文件名的所有文件
     *
     * @param fileName 文件名（小写）
     * @return 相同文件名的文件列表
     */
    public List<FileMergeInfo> getFilesWithName(String fileName) {
        return filesByName.getOrDefault(fileName.toLowerCase(), new ArrayList<>());
    }

    /**
     * 获取所有具有重复名字的文件分组（大小 > 1）
     *
     * @return 重复文件分组列表
     */
    public List<Map.Entry<String, List<FileMergeInfo>>> getDuplicateGroups() {
        return filesByName.entrySet().stream()
                .filter(entry -> entry.getValue().size() > 1)
                .toList();
    }

    /**
     * 获取所有唯一名字的文件（大小 == 1）
     *
     * @return 唯一文件分组列表
     */
    public List<Map.Entry<String, List<FileMergeInfo>>> getUniqueFiles() {
        return filesByName.entrySet().stream()
                .filter(entry -> entry.getValue().size() == 1)
                .toList();
    }

    /**
     * 打印追踪报告
     */
    public void printReport() {
        ColorPrinter.info("\n{}", "=".repeat(60));
        ColorPrinter.info("📊 File Merge Tracking Report:");
        ColorPrinter.info("   Total unique file names: {}", filesByName.size());

        long totalFiles = filesByName.values().stream().mapToLong(List::size).sum();
        ColorPrinter.info("   Total files: {}", totalFiles);

        var duplicates = getDuplicateGroups();
        ColorPrinter.warning("   ⚠️ Files with same name (requires merge): {}", duplicates.size());

        var unique = getUniqueFiles();
        ColorPrinter.success("   ✓ Unique files (no merge needed): {}", unique.size());

        // 如果有重复，打印详细信息
        if (!duplicates.isEmpty()) {
            ColorPrinter.info("\n   Duplicate file groups:");
            for (var entry : duplicates) {
                ColorPrinter.warning("   ├─ {} ({} files)", entry.getKey(), entry.getValue().size());
                for (var file : entry.getValue()) {
                    ColorPrinter.info("   │  ├─ {} (from: {})", file.getRelativePath(), file.getSourceChain());
                }
            }
        }

        ColorPrinter.info("{}", "=".repeat(60));
    }

    /**
     * 获取所有追踪的文件
     *
     * @return 所有文件的映射表
     */
    public Map<String, List<FileMergeInfo>> getAllFiles() {
        return new LinkedHashMap<>(filesByName);
    }

    /**
     * 获取追踪的文件总数
     *
     * @return 文件总数
     */
    public int getTotalFileCount() {
        return (int) filesByName.values().stream().mapToLong(List::size).sum();
    }

    /**
     * 清空追踪器
     */
    public void clear() {
        filesByName.clear();
    }
}

