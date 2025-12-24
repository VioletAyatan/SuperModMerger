package ankol.mod.merger.core;

import ankol.mod.merger.tools.ColorPrinter;
import ankol.mod.merger.tools.FileTree;
import ankol.mod.merger.tools.Localizations;
import ankol.mod.merger.tools.Tools;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * 基准MOD分析器 - 负责加载和分析基准MOD（原版文件）
 * <p>
 * 优化：只构建文件名→路径映射，不解压文件，按需提取
 * <p>
 * 基准MOD是游戏的原版文件（如 data0.pak），用于：
 * 1. 建立"正确路径映射"（文件名 → 标准路径）
 * 2. 检测待合并MOD中的错误路径
 * 3. 提供路径修正建议
 * 4. 按需提取文件进行对比
 *
 * @author Ankol
 */
@Slf4j
public class BaseModAnalyzer {

    /**
     * 基准MOD文件路径
     */
    private final Path baseModPath;

    /**
     * 文件名 → 标准路径的映射
     * 键：文件名（小写）
     * 值：在基准MOD中的相对路径
     */
    @Getter
    private Map<String, FileTree> indexedBaseModFileMap;

    /**
     * 所有文件的相对路径集合（从基准MOD中提取）
     */
    @Getter
    private Set<String> baseModFilePaths;

    /**
     * 基准MOD是否已加载
     */
    @Getter
    private boolean loaded = false;

    /**
     * 临时文件缓存目录
     */
    private final Path cacheDir;

    /**
     * 已提取文件的缓存映射：相对路径 → 临时文件路径
     */
    private final Map<String, Path> extractedFileCache;

    /**
     * 构造函数
     *
     * @param baseModPath 基准MOD文件路径
     */
    public BaseModAnalyzer(Path baseModPath) {
        this.baseModPath = baseModPath;
        this.indexedBaseModFileMap = new LinkedHashMap<>();
        this.baseModFilePaths = new LinkedHashSet<>();
        this.extractedFileCache = new LinkedHashMap<>();
        // 创建缓存目录：temp/BaseModCache_时间戳
        this.cacheDir = Path.of(Tools.getTempDir(), "BaseModCache_" + System.currentTimeMillis());
        try {
            Files.createDirectories(this.cacheDir);
        } catch (IOException e) {
            ColorPrinter.warning("Failed to create base mod cache directory: " + e.getMessage());
        }
    }

    /**
     * 加载基准MOD（优化：只读取条目，不解压文件）
     *
     * @throws IOException 如果基准MOD文件不存在或无法读取
     */
    public void load() throws IOException {
        if (loaded) {
            ColorPrinter.warning(Localizations.t("BASE_MOD_ALREADY_LOADED"));
            return;
        }
        if (!Files.exists(baseModPath)) {
            throw new IOException(Localizations.t("BASE_MOD_FILE_NOT_FOUND", baseModPath));
        }

        try {
            long startTime = System.currentTimeMillis();
            this.indexedBaseModFileMap = Tools.indexPakFile(baseModPath.toFile());
            loaded = true;
            long elapsed = System.currentTimeMillis() - startTime;
            ColorPrinter.success(Localizations.t("BASE_MOD_INDEXED_FILES",
                    indexedBaseModFileMap.size(),
                    baseModPath.getFileName(),
                    elapsed
            ));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 从基准MOD中提取指定文件的内容（带缓存优化）
     *
     * @param relPath 文件在基准MOD中的相对路径
     * @return 文件内容，如果文件不存在返回null
     */
    public String extractFileContent(String relPath) throws IOException {
        if (!loaded) {
            return null;
        }

        // 规范化路径（统一使用小写文件名查找）
        String fileName = extractFileName(relPath).toLowerCase();
        FileTree fileTree = indexedBaseModFileMap.get(fileName);

        if (fileTree == null) {
            return null;
        }

        String fullPath = fileTree.getFullPathName();

        // 检查缓存中是否已存在
        Path cachedFile = extractedFileCache.get(fullPath);
        if (cachedFile != null && Files.exists(cachedFile)) {
            // 从缓存读取
            return Files.readString(cachedFile);
        }

        // 缓存未命中，从基准MOD的pak文件中提取文件内容
        String content = Tools.extractFileFromPak(baseModPath.toFile(), fullPath);

        if (content != null) {
            // 保存到临时文件缓存
            try {
                // 使用安全的文件名（替换路径分隔符）
                String safeFileName = fullPath.replace("/", "_").replace("\\", "_");
                Path tempFile = cacheDir.resolve(safeFileName);

                // 确保父目录存在
                Files.createDirectories(tempFile.getParent());

                // 写入缓存
                Files.writeString(tempFile, content);
                extractedFileCache.put(fullPath, tempFile);
            } catch (IOException e) {
                // 缓存失败不影响正常流程，只记录警告
                ColorPrinter.warning("Failed to cache extracted file: " + fullPath + " - " + e.getMessage());
            }
        }

        return content;
    }

    /**
     * 判断MOD里的文件路径是否正确
     *
     * @param filePath mod文件路径
     */
    public boolean hasPathConflict(String filePath) {
        if (!loaded) {
            return false;
        }
        String fileName = extractFileName(filePath);
        FileTree fileTree = indexedBaseModFileMap.get(fileName);
        if (fileTree == null) {
            log.warn("File not found in base mod: {}-{}", filePath, fileName);
            return false;
        }
        String correctPath = fileTree.getFullPathName();
        return correctPath != null && !correctPath.equalsIgnoreCase(filePath);
    }

    /**
     * 获取建议的修正路径
     *
     * @param filePath 待检查的文件相对路径
     * @return 如果存在同名文件，返回基准MOD中的正确路径；否则返回null
     */
    public String getSuggestedPath(String filePath) {
        if (!loaded) {
            return null;
        }
        String fileName = extractFileName(filePath);
        return indexedBaseModFileMap.get(fileName).getFullPathName();
    }

    /**
     * 提取文件名的工具方法
     */
    private static String extractFileName(String path) {
        int lastSlash = path.lastIndexOf("/");
        return (lastSlash >= 0 ? path.substring(lastSlash + 1) : path).toLowerCase();
    }

    /**
     * 清理临时文件缓存
     * 建议在合并完成后调用此方法释放磁盘空间
     */
    public void clearCache() {
        if (!Files.exists(cacheDir)) {
            return;
        }

        try (var stream = Files.walk(cacheDir)) {
            stream.sorted(java.util.Comparator.reverseOrder())
                    .forEach(path -> {
                        try {
                            Files.delete(path);
                        } catch (IOException e) {
                            // 忽略删除错误
                        }
                    });
            extractedFileCache.clear();
        } catch (IOException e) {
            ColorPrinter.warning("Failed to clear base mod cache: " + e.getMessage());
        }
    }

    /**
     * 获取缓存统计信息
     *
     * @return 缓存文件数量
     */
    public int getCacheSize() {
        return extractedFileCache.size();
    }
}
