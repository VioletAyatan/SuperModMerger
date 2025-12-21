package ankol.mod.merger.core;

import ankol.mod.merger.tools.ColorPrinter;
import ankol.mod.merger.tools.PakManager;
import lombok.Getter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 基准MOD分析器 - 负责加载和分析基准MOD（原版文件）
 * <p>
 * 基准MOD是游戏的原版文件（如 data0.pak），用于：
 * 1. 建立"正确路径映射"（文件名 → 标准路径）
 * 2. 检测待合并MOD中的错误路径
 * 3. 提供路径修正建议
 *
 * @author Ankol
 */
public class BaseModAnalyzer {

    /**
     * 基准MOD文件路径
     */
    private final Path baseModPath;

    /**
     * 临时解压目录
     */
    private final Path tempDir;

    /**
     * 文件名 → 标准路径的映射
     * 键：文件名（如 "config.xml"）
     * 值：在基准MOD中的相对路径（如 "scripts/config/config.xml"）
     */
    @Getter
    private Map<String, String> fileNameToPathMap;

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
     * 构造函数
     *
     * @param baseModPath 基准MOD文件路径
     */
    public BaseModAnalyzer(Path baseModPath) {
        this.baseModPath = baseModPath;
        this.tempDir = Path.of(System.getProperty("java.io.tmpdir"), "BaseModAnalyzer_" + System.currentTimeMillis());
        this.fileNameToPathMap = new LinkedHashMap<>();
        this.baseModFilePaths = new LinkedHashSet<>();
    }

    /**
     * 加载基准MOD
     *
     * @throws IOException 如果基准MOD文件不存在或无法读取
     */
    public void load() throws IOException {
        if (loaded) {
            ColorPrinter.warning("⚠️ Base MOD already loaded, skipping...");
            return;
        }

        if (!Files.exists(baseModPath)) {
            throw new IOException("Base MOD file not found: " + baseModPath);
        }

        ColorPrinter.info("📖 Loading base MOD: {}", baseModPath.getFileName());

        try {
            // 解压基准MOD
            var extractedFiles = PakManager.extractPak(baseModPath, tempDir);

            // 构建文件名 → 路径映射
            for (var entry : extractedFiles.entrySet()) {
                String relPath = entry.getKey();
                baseModFilePaths.add(relPath);

                // 提取文件名
                String fileName = relPath.substring(relPath.lastIndexOf("/") + 1).toLowerCase();
                fileNameToPathMap.put(fileName, relPath);
            }

            loaded = true;
            ColorPrinter.success("✓ Loaded {} files from {}", extractedFiles.size(), baseModPath.getFileName());
        } finally {
            // 清理临时文件
            cleanup();
        }
    }

    /**
     * 检查是否存在路径冲突
     *
     * @param filePath 待检查的文件相对路径
     * @return 如果在基准MOD中有同名文件但路径不同，返回true
     */
    public boolean hasPathConflict(String filePath) {
        if (!loaded) {
            return false;
        }

        String fileName = filePath.substring(filePath.lastIndexOf("/") + 1).toLowerCase();
        String correctPath = fileNameToPathMap.get(fileName);

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

        String fileName = filePath.substring(filePath.lastIndexOf("/") + 1).toLowerCase();
        return fileNameToPathMap.get(fileName);
    }

    /**
     * 检查文件是否在基准MOD中存在
     *
     * @param filePath 文件相对路径
     * @return 如果文件在基准MOD中存在，返回true
     */
    public boolean existsInBaseMod(String filePath) {
        if (!loaded) {
            return false;
        }
        return baseModFilePaths.contains(filePath.toLowerCase());
    }

    /**
     * 获取所有需要修正的文件（同名但路径不同）
     *
     * @param filePaths 待检查的文件路径集合
     * @return 需要修正的文件列表，格式：原始路径 -> 建议路径
     */
    public Map<String, String> findPathMismatches(Collection<String> filePaths) {
        if (!loaded) {
            return new HashMap<>();
        }

        return filePaths.stream()
                .filter(this::hasPathConflict)
                .collect(Collectors.toMap(
                        path -> path,
                        this::getSuggestedPath,
                        (a, b) -> a,
                        LinkedHashMap::new
                ));
    }

    /**
     * 清理临时文件
     */
    private void cleanup() {
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
            ColorPrinter.warning("Warning: Failed to clean base mod analyzer temp directory: {}", e.getMessage());
        }
    }
}

