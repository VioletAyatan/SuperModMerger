package ankol.mod.merger.tools;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import lombok.Getter;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public abstract class Tools {
    @Getter
    private static final String userDir = System.getProperty("user.dir");
    @Getter
    private static final String tempDir = System.getProperty("java.io.tmpdir");

    /**
     * 获取待合并的MOD所在目录
     * 这个工具默认配置的是在mods目录下
     *
     * @param argumentsParser 命令行参数配置，命令行传入的参数可以覆盖默认配置
     * @return 待合并的MOD目录路径
     */
    public static Path getMergingModDir(SimpleArgumentsParser argumentsParser) {
        Path meringModDir = null;
        if (argumentsParser != null) {
            meringModDir = argumentsParser.meringModDir;
        }
        if (meringModDir == null) {
            Path defaultPath = Path.of(userDir + File.separator + "mods");
            if (FileUtil.exists(defaultPath, true)) {
                return defaultPath;
            } else {
                throw new IllegalArgumentException("错误，合并目录[" + defaultPath + "]不存在");
            }
        } else {
            if (FileUtil.exists(meringModDir, true)) {
                return meringModDir;
            } else {
                throw new IllegalArgumentException("错误，合并目录[" + meringModDir + "]不存在");
            }
        }
    }

    /**
     * 获取待合并的MOD所在目录
     * 这个工具默认配置的是在mods目录下
     *
     * @return 待合并的MOD目录路径
     */
    public static Path getMergingModDir() {
        return getMergingModDir(null);
    }

    /**
     * 扫描指定目录中的所有文件，按扩展名过滤
     *
     * @param dir        目录路径
     * @param extensions 要查找的扩展名（如 ".pak", ".zip"）
     * @return 匹配的文件列表
     * @throws IOException 如果目录不存在或无法访问
     */
    public static List<Path> scanFiles(Path dir, String... extensions) throws IOException {
        List<Path> results = new ArrayList<>();
        if (!Files.exists(dir)) {
            throw new IllegalArgumentException("目录不存在: " + dir);
        }
        try (Stream<Path> pathStream = Files.walk(dir)) {
            pathStream.filter(Files::isRegularFile)
                    .filter(file -> {
                        String filename = file.getFileName().toString();
                        for (String ext : extensions) {
                            if (filename.endsWith(ext)) {
                                return true;
                            }
                        }
                        return false;
                    })
                    .forEach(results::add);
        }
        return results;
    }

    /**
     * 从 ZIP 文件中提取所有 PAK 文件到临时目录
     * <p>
     * 如果 ZIP 包含嵌套的 PAK 文件，会将其提取出来作为独立的 mod
     *
     * @param zipPath       ZIP 文件路径
     * @param tempExpandDir 临时展开目录
     * @return 提取出的 PAK 文件列表
     */
    private static List<Path> extractNestedPaksFromZip(Path zipPath, Path tempExpandDir) throws IOException {
        List<Path> extractedPaks = new ArrayList<>();
        Path zipTempDir = tempExpandDir.resolve("zip_" + System.currentTimeMillis());
        Files.createDirectories(zipTempDir);

        try (ZipFile zipFile = new ZipFile(zipPath.toFile())) {
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                if (entry.isDirectory()) continue;
                String entryName = entry.getName().toLowerCase();
                // 只关心 PAK 文件
                if (entryName.endsWith(".pak")) {
                    Path outputPath = zipTempDir.resolve(entry.getName());
                    Files.createDirectories(outputPath.getParent());

                    try (InputStream input = zipFile.getInputStream(entry)) {
                        Files.copy(input, outputPath);
                    }

                    extractedPaks.add(outputPath);
                    ColorPrinter.info("Extracted: {}", entry.getName());
                }
            }
        }

        return extractedPaks;
    }

    /**
     * 构建文件树
     *
     * @param path 文件路径，可以是文件夹或者zip文件
     * @return 文件树MAP key是文件名
     */
    public static Map<String, FileTree> buildFileTree(Path path) {
        File file = path.toFile();
        if (!file.exists()) {
            throw new IllegalArgumentException("非法参数，路径" + path + "不存在");
        }
        if (file.isDirectory()) {
            throw new IllegalArgumentException("不支持传入文件夹");
        } else if (StrUtil.endWithAny(file.getName(), ".zip", ".pak")) {
            return buildFileTreeFromZip(file);
        } else {
            throw new RuntimeException("不支持的文件格式" + file.getName());
        }
    }

    private static Map<String, FileTree> buildFileTreeFromZip(File file) {
        Map<String, FileTree> fileTreeMap = new HashMap<>();
        try (ZipFile zipFile = new ZipFile(file)) {
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry zipEntry = entries.nextElement();
                String entryName = zipEntry.getName();
                String fileName = getEntryFileName(entryName);
                if (!fileTreeMap.containsKey(fileName)) {
                    fileTreeMap.put(fileName, new FileTree(fileName, entryName));
                } else {
                    ColorPrinter.warning("检测到相同的文件名：{}但路径不一致：[{}] [{}]", fileName, entryName, fileTreeMap.get(fileName).getFullPathName());
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return fileTreeMap;
    }

    private static String getEntryFileName(String entryName) {
        if (StrUtil.isNotBlank(entryName)) {
            return entryName.substring(entryName.lastIndexOf("/") + 1);
        }
        return null;
    }

    static void main() {
        //原版文件中是不会出现文件名重名的情况的
        Map<String, FileTree> fileTreeMap = buildFileTree(Path.of("D:\\SteamLibrary\\steamapps\\common\\Dying Light The Beast\\ph_ft\\source\\data0.pak"));
        System.out.println("fileTreeMap = " + fileTreeMap);
    }
}
