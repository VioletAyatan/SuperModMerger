package ankol.mod.merger.tools;

import ankol.mod.merger.exception.BusinessException;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import lombok.Getter;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipFile;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.stream.Stream;

public abstract class Tools {
    @Getter
    private static final String userDir = System.getProperty("user.dir");
    @Getter
    private static final String tempDir = System.getProperty("java.io.tmpdir");

    // 复用十六进制表，避免 String.format 带来的巨大开销
    private static final char[] HEX_ARRAY = "0123456789abcdef".toCharArray();

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
                throw new BusinessException(Localizations.t("TOOLS_MODS_DIR_NOT_EXIST", defaultPath));
            }
        } else {
            if (FileUtil.exists(meringModDir, true)) {
                return meringModDir;
            } else {
                throw new BusinessException(Localizations.t("TOOLS_MODS_DIR_NOT_EXIST", meringModDir));
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
            throw new BusinessException("MOD合并目录mods在当前目录不存在，请创建一个mods目录");
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
            throw new BusinessException(Localizations.t("TOOLS_FILE_NOT_EXIST", file.getAbsolutePath()));
        }
        if (file.isDirectory()) {
            throw new BusinessException(Localizations.t("TOOLS_PATH_IS_DIRECTORY", file.getAbsolutePath()));
        }
        if (!StrUtil.endWithAny(file.getName(), ".pak")) {
            throw new BusinessException(Localizations.t("TOOLS_FILE_MUST_BE_PAK"));
        }
        Map<String, FileTree> pakIndexMap = new HashMap<>();
        try (ZipFile zipFile = ZipFile.builder().setFile(file).get()) {
            Enumeration<ZipArchiveEntry> entries = zipFile.getEntries();
            while (entries.hasMoreElements()) {
                ZipArchiveEntry zipEntry = entries.nextElement();
                String entryName = zipEntry.getName();
                String fileName = getEntryFileName(entryName);
                if (pakIndexMap.containsKey(fileName)) {
                    ColorPrinter.warning(Localizations.t("TOOLS_SAME_FILE_NAME_WARNING", fileName, entryName, pakIndexMap.get(fileName).getFullPathName()));
                }
                pakIndexMap.put(fileName, new FileTree(fileName, entryName, file.getName()));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return pakIndexMap;
    }

    public static String getEntryFileName(String entryName) {
        return entryName.substring(entryName.lastIndexOf("/") + 1);
    }


    /**
     * 计算字符串内容的 SHA-256 哈希值
     *
     * @param content 要计算哈希的内容
     * @return 哈希值的十六进制字符串
     */
    public static String computeHash(String content) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(content.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(hash);
        } catch (NoSuchAlgorithmException e) {
            return String.valueOf(content.hashCode());
        }
    }

    private static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int i = 0; i < bytes.length; i++) {
            int v = bytes[i] & 0xFF;
            hexChars[i * 2] = HEX_ARRAY[v >>> 4];
            hexChars[i * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }
        return new String(hexChars);
    }
}
