package ankol.mod.merger.core;

import ankol.mod.merger.tools.ColorPrinter;
import lombok.Getter;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.stream.Collectors;

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

        ColorPrinter.info("📖 Loading base MOD: {}", baseModPath.getFileName());
        long startTime = System.currentTimeMillis();
        // 优化：只读取ZIP条目，不解压文件
        try (ZipFile zipFile = ZipFile.builder().setFile(baseModPath.toFile()).get()) {
            Enumeration<ZipArchiveEntry> entries = zipFile.getEntries();
            int fileCount = 0;

            while (entries.hasMoreElements()) {
                ZipArchiveEntry entry = entries.nextElement();
                if (entry.isDirectory()) continue;

                String relPath = entry.getName();
                baseModFilePaths.add(relPath.toLowerCase());
                // 提取文件名并建立映射
                String fileName = extractFileName(relPath);
                // 如果有重名文件，保留第一个（虽然原版data0.pak中不会出现重复的文件名）
                fileNameToPathMap.putIfAbsent(fileName, relPath);
                fileCount++;
            }

            loaded = true;
            long elapsed = System.currentTimeMillis() - startTime;
            ColorPrinter.success("✓ Indexed {} files from {} in {}ms (on-demand extraction)",
                    fileCount, baseModPath.getFileName(), elapsed);
        }
    }

    /**
     * 从基准MOD中提取指定文件的内容（按需提取）
     *
     * @param relPath 文件的相对路径
     * @return 文件内容的输入流，如果文件不存在返回null
     * @throws IOException 读取错误
     */
    public InputStream extractFile(String relPath) throws IOException {
        if (!loaded) {
            throw new IllegalStateException("Base MOD not loaded yet");
        }

        if (!baseModFilePaths.contains(relPath.toLowerCase())) {
            return null;
        }

        try (ZipFile zipFile = ZipFile.builder().setFile(baseModPath.toFile()).get()) {
            ZipArchiveEntry entry = zipFile.getEntry(relPath);
            if (entry == null) {
                return null;
            }

            // 读取整个文件到内存（因为ZipFile会在close时关闭）
            try (InputStream inputStream = zipFile.getInputStream(entry)) {
                byte[] bytes = inputStream.readAllBytes();
                return new ByteArrayInputStream(bytes);
            }
        }
    }

    /**
     * 从基准MOD中提取指定文件到临时文件（按需提取）
     *
     * @param relPath 文件的相对路径
     * @return 临时文件路径，如果文件不存在返回null
     * @throws IOException 读取错误
     */
    public Path extractFileToTemp(String relPath) throws IOException {
        if (!loaded) {
            throw new IllegalStateException("Base MOD not loaded yet");
        }

        if (!baseModFilePaths.contains(relPath.toLowerCase())) {
            return null;
        }

        try (ZipFile zipFile = ZipFile.builder().setFile(baseModPath.toFile()).get()) {
            ZipArchiveEntry entry = zipFile.getEntry(relPath);
            if (entry == null) {
                return null;
            }

            // 创建临时文件
            String fileName = extractFileName(relPath);
            Path tempFile = Files.createTempFile("baseMod_" + fileName + "_", ".tmp");

            try (InputStream input = zipFile.getInputStream(entry)) {
                Files.copy(input, tempFile, StandardCopyOption.REPLACE_EXISTING);
            }

            ColorPrinter.debug("📤 Extracted base file: {} → {}", relPath, tempFile.getFileName());
            return tempFile;
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
        String fileName = extractFileName(filePath);
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
     * 打印基准MOD的分析报告
     */
    public void printAnalysisReport() {
        if (!loaded) {
            ColorPrinter.warning("⚠️ Base MOD not loaded");
            return;
        }

        ColorPrinter.info("\n{}", "=".repeat(50));
        ColorPrinter.info("📊 Base MOD Analysis Report:");
        ColorPrinter.info("   Total files: {}", baseModFilePaths.size());
        ColorPrinter.info("   Unique file names: {}", fileNameToPathMap.size());
        ColorPrinter.info("   Storage: Index only (on-demand extraction)");
        ColorPrinter.info("{}", "=".repeat(50));
    }

    /**
     * 提取文件名的工具方法
     */
    private static String extractFileName(String path) {
        int lastSlash = path.lastIndexOf("/");
        return (lastSlash >= 0 ? path.substring(lastSlash + 1) : path).toLowerCase();
    }
}

