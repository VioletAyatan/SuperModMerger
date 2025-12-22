package ankol.mod.merger.core;

import ankol.mod.merger.tools.ColorPrinter;
import ankol.mod.merger.tools.FileTree;
import ankol.mod.merger.tools.Tools;
import lombok.Getter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

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
    private Map<String, FileTree> fileNameToPathMap;

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
        this.fileNameToPathMap = new LinkedHashMap<>();
        this.baseModFilePaths = new LinkedHashSet<>();
    }

    /**
     * 加载基准MOD（优化：只读取条目，不解压文件）
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

        try {
            long startTime = System.currentTimeMillis();
            this.fileNameToPathMap = Tools.indexPakFile(baseModPath.toFile());
            loaded = true;
            long elapsed = System.currentTimeMillis() - startTime;
            ColorPrinter.success("✓ Indexed {} files from {} in {}ms (on-demand extraction)",
                    fileNameToPathMap.size(),
                    baseModPath.getFileName(),
                    elapsed
            );
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
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
        String correctPath = fileNameToPathMap.get(fileName).getFullPathName();

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
        return fileNameToPathMap.get(fileName).getFullPathName();
    }

    /**
     * 提取文件名的工具方法
     */
    private static String extractFileName(String path) {
        int lastSlash = path.lastIndexOf("/");
        return (lastSlash >= 0 ? path.substring(lastSlash + 1) : path).toLowerCase();
    }
}

