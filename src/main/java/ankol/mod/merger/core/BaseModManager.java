package ankol.mod.merger.core;

import ankol.mod.merger.core.filetrees.PathFileTree;
import ankol.mod.merger.tools.ColorPrinter;
import ankol.mod.merger.tools.Localizations;
import ankol.mod.merger.tools.Tools;
import cn.hutool.cache.Cache;
import cn.hutool.cache.CacheUtil;
import cn.hutool.core.io.IoUtil;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * 基准MOD分析器 - 负责加载和分析基准MOD（原版文件）
 *
 * @author Ankol
 */
@Slf4j
public class BaseModManager {

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
    private Map<String, PathFileTree> indexedBaseModFileMap;

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
    @SuppressWarnings("rawtypes")
    private final Cache<String, ParsedResult> BASE_TREE_CACHE = CacheUtil.newFIFOCache(100, 30 * 1000);

    /**
     * 构造函数
     *
     * @param baseModPath 基准MOD文件路径
     */
    public BaseModManager(Path baseModPath) {
        this.baseModPath = baseModPath;
        this.indexedBaseModFileMap = new LinkedHashMap<>();
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
            this.indexedBaseModFileMap = Tools.indexPakFile(baseModPath); //这里构建的索引MAP里还没有真正解压出来文件
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
        String fileName = Tools.getEntryFileName(relPath).toLowerCase();
        PathFileTree pathFileTree = indexedBaseModFileMap.get(fileName);
        if (pathFileTree == null) {
            return null;
        }

        String fileEntryName = pathFileTree.getFileEntryName();
        Path cachedFile = extractedFileCache.get(fileEntryName);
        if (cachedFile != null && Files.exists(cachedFile)) {
            return Files.readString(cachedFile);
        }

        String content = extractFileFromPak(baseModPath, fileEntryName);
        if (content != null) {
            try {
                String safeFileName = fileEntryName.replace("/", "_").replace("\\", "_");
                Path tempFile = cacheDir.resolve(safeFileName);
                Files.createDirectories(tempFile.getParent());
                Files.writeString(tempFile, content);
                extractedFileCache.put(fileEntryName, tempFile);
            } catch (IOException e) {
                ColorPrinter.warning("Failed to cache extracted file: " + fileEntryName + " - " + e.getMessage());
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
        String fileName = Tools.getEntryFileName(filePath);
        PathFileTree pathFileTree = indexedBaseModFileMap.get(fileName);
        //有时会有一些不属于mod的文件被加入到pak中，这里查到空后说明不是原版mod支持修改的文件.
        if (pathFileTree == null) {
            return false;
        }
        String correctPath = pathFileTree.getFileEntryName();
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
        String fileName = Tools.getEntryFileName(filePath);
        return indexedBaseModFileMap.get(fileName).getFileEntryName();
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
     * 从基准MOD获得解析后的语法树节点，带缓存机制
     *
     * @param fileEntryName 文件在压缩包中的全路径
     * @param function      解析语法树使用的函数
     * @return 解析结果，如果文件不存在返回null
     */
    @SuppressWarnings("unchecked")
    public <T extends BaseTreeNode> ParsedResult<T> parseForm(String fileEntryName, Function<String, ParsedResult<T>> function) {
        return BASE_TREE_CACHE.get(fileEntryName,
                () -> {
                    String content = extractFileContent(fileEntryName);
                    if (content == null) {
                        return null;
                    }
                    return function.apply(content);
                });
    }

    /**
     * 从PAK文件中提取指定文件的内容
     *
     * @param pakFile       PAK文件
     * @param fileEntryName 文件在PAK中的相对路径
     * @return 文件内容，如果文件不存在返回null
     */
    public String extractFileFromPak(Path pakFile, String fileEntryName) throws IOException {
        try (ZipFile zipFile = ZipFile.builder().setPath(pakFile).get()) {
            ZipArchiveEntry entry = zipFile.getEntry(fileEntryName);
            if (entry.getSize() == 0) {
                return null;
            }

            try (var inputStream = zipFile.getInputStream(entry)) {
                return IoUtil.read(inputStream, StandardCharsets.UTF_8);
            }
        }
    }
}
