package ankol.mod.merger.tools;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import lombok.Getter;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Stream;

public abstract class Tools {
    @Getter
    private static final String userDir = System.getProperty("user.dir");
    @Getter
    private static final String tempDir = System.getProperty("java.io.tmpdir");

    /**
     * 获取待合并的MOD所在目录
     * 这个工具默认配置的是在mods目录下
     *
     * @param meringModDir mod合并目录地址
     * @return 待合并的MOD目录路径
     */
    public static Path getMergingModDir(Path meringModDir) {
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
            throw new IllegalArgumentException("MOD合并目录mods在当前目录不存在，请创建一个mods目录");
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


    public static Map<String, FileTree> indexPakFile(File file) {
        if (!file.exists()) {
            throw new IllegalArgumentException("文件不存在: " + file.getAbsolutePath());
        }
        if (file.isDirectory()) {
            throw new IllegalArgumentException("提供的路径是一个目录，而不是文件: " + file.getAbsolutePath());
        }
        if (!StrUtil.endWithAny(file.getName(), ".pak")) {
            throw new IllegalArgumentException("文件必须为.pak类型");
        }
        Map<String, FileTree> pakIndexMap = new HashMap<>();
        try (ZipFile zipFile = ZipFile.builder().setFile(file).get()) {
            Enumeration<ZipArchiveEntry> entries = zipFile.getEntries();
            while (entries.hasMoreElements()) {
                ZipArchiveEntry zipEntry = entries.nextElement();
                String entryName = zipEntry.getName();
                String fileName = getEntryFileName(entryName);
                if (pakIndexMap.containsKey(fileName)) {
                    ColorPrinter.warning("检测到相同的文件名：{}但路径不一致：[{}] [{}]", fileName, entryName, pakIndexMap.get(fileName).getFullPathName());
                }
                pakIndexMap.put(fileName, new FileTree(fileName, entryName));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return pakIndexMap;
    }

    public static String getEntryFileName(String entryName) {
        return entryName.substring(entryName.lastIndexOf("/") + 1);
    }
}
