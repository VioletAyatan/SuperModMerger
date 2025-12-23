package ankol.mod.merger.tools;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.compress.archivers.zip.ZipFile;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

/**
 * .pak 文件管理工具 - 处理.pak文件的打开、读取和写入
 * <p>
 * .pak 文件本质上是ZIP压缩包，因此使用ZIP相关的API处理
 *
 * @author Ankol
 */
public class PakManager {

    /**
     * 从 .pak 文件中提取所有文件到临时目录（支持递归解压嵌套压缩包）
     * <p>
     * 如果压缩包中包含 .pak 或 .zip 文件，会递归解压它们
     * 这样可以处理诸如 "zip里套pak" 这样的嵌套情况
     * <p>
     * 返回的映射包含文件来源信息，可以追踪嵌套链
     *
     * @param pakPath pak文件路径
     * @param tempDir 临时解压目录
     * @return 文件映射表 (相对路径 -> FileSourceInfo)，包含来源链信息
     */
    public static Map<String, FileSourceInfo> extractPak(Path pakPath, Path tempDir) throws IOException {
        Files.createDirectories(tempDir);
        Map<String, FileSourceInfo> fileMap = new HashMap<>();
        String archiveName = pakPath.getFileName().toString();
        extractPakRecursive(pakPath, tempDir, fileMap, archiveName);
        return fileMap;
    }

    /**
     * 递归解压压缩包（支持嵌套）
     * <p>
     * 当遇到 .pak 或 .zip 文件时，会递归解压，并记录来源链
     * 例如：如果 mymod.zip 中包含 data3.pak，来源链为 ["mymod.zip", "data3.pak"]
     *
     * @param archivePath 压缩包路径
     * @param outputDir   输出目录
     * @param fileMap     文件映射表，包含来源信息
     * @param archiveName 当前压缩包名称（用于构建来源链）
     */
    private static void extractPakRecursive(Path archivePath, Path outputDir, Map<String, FileSourceInfo> fileMap, String archiveName) throws IOException {
        try (ZipFile zipFile = ZipFile.builder().setFile(archivePath.toFile()).get()) {
            Enumeration<ZipArchiveEntry> entries = zipFile.getEntries();
            while (entries.hasMoreElements()) {
                ZipArchiveEntry entry = entries.nextElement();

                if (entry.isDirectory()) continue;

                String entryName = entry.getName();
                String fileName = Tools.getEntryFileName(entryName);
                Path outputPath = outputDir.resolve(entryName);
                Files.createDirectories(outputPath.getParent());

                // 优化：直接检查size而不是先复制空流
                if (entry.getSize() == 0) {
                    Files.createFile(outputPath);
                } else {
                    // 从 ZIP 中读取文件内容并写入
                    try (InputStream input = zipFile.getInputStream(entry)) {
                        Files.copy(input, outputPath);
                    }
                }

                // 检查是否是嵌套的压缩包（.pak 或 .zip）
                if (fileName.endsWith(".pak") || fileName.endsWith(".zip")) {
                    // 创建嵌套压缩包的临时解压目录
                    Path nestedTempDir = outputDir.resolve("_nested_" + System.currentTimeMillis() + "_" + fileName);
                    Files.createDirectories(nestedTempDir);
                    // 递归解压，传递嵌套的文件名
                    extractPakRecursive(outputPath, nestedTempDir, fileMap, fileName);
                } else {
                    // 创建文件来源信息，记录来源链
                    FileSourceInfo sourceInfo = new FileSourceInfo(outputPath, entryName);
                    sourceInfo.addSource(archiveName);

                    // 检查是否已有相同路径的文件（来自不同来源）
                    if (fileMap.containsKey(entryName)) {
                        FileSourceInfo existing = fileMap.get(entryName);
                        ColorPrinter.warning(Localizations.t("PAK_MANAGER_DUPLICATE_FILE_DETECTED",
                                existing.getSourceChainString(),
                                sourceInfo.getFileEnterName(),
                                existing.getFileEnterName())
                        );
                        ColorPrinter.success(" --> 使用新路径：" + sourceInfo.getFileEnterName());
                        fileMap.put(entryName, sourceInfo);
                    } else {
                        fileMap.put(entryName, sourceInfo);
                    }
                }
            }
        }
    }

    /**
     * 将合并后的文件打包成 .pak 文件
     *
     * @param sourceDir 源目录（包含所有要打包的文件）
     * @param pakPath   输出 pak 文件路径
     */
    public static void createPak(Path sourceDir, Path pakPath) throws IOException {
        Files.createDirectories(pakPath.getParent());

        try (ZipArchiveOutputStream zipOut = new ZipArchiveOutputStream(pakPath.toFile())) {
            // 遍历源目录中的所有文件
            try (Stream<Path> pathStream = Files.walk(sourceDir)) {
                pathStream.filter(Files::isRegularFile)
                        .forEach(file -> {
                            try {
                                // 计算相对路径
                                String entryName = sourceDir.relativize(file).toString();
                                // 使用正斜杠作为路径分隔符（ZIP 标准）
                                entryName = entryName.replace(File.separator, "/");

                                ZipArchiveEntry entry = new ZipArchiveEntry(entryName);
                                zipOut.putArchiveEntry(entry);

                                // 写入文件内容
                                Files.copy(file, zipOut);

                                zipOut.closeArchiveEntry();
                            } catch (IOException e) {
                                throw new RuntimeException("Failed to add file to PAK: " + file, e);
                            }
                        });
            }
        }
    }


    /**
     * 判断两个文件在内容上是否相同
     *
     * @param file1 第一个文件
     * @param file2 第二个文件
     * @return 两个文件内容是否相同
     * @throws IOException 如果文件不可读
     */
    public static boolean areFilesIdentical(Path file1, Path file2) throws IOException {
        // 快速判断：文件大小不同，肯定内容不同
        if (Files.size(file1) != Files.size(file2)) {
            return false;
        } else {
            // 文件大小相同的情况下，对比文件HASH值，更快速且节省内存
            return getFileHash(file1).equals(getFileHash(file2));
        }
    }

    /**
     * 计算文件的 SHA-256 哈希值（流式处理）
     * <p>
     * 相比一次性读取文件到内存的方式，这个方法使用 8KB 缓冲区逐块处理，
     * 即使对于 1GB 的文件也只占用恒定的内存。
     *
     * @param file 要计算哈希的文件
     * @return 十六进制格式的哈希值
     * @throws IOException 如果文件不可读
     */
    private static String getFileHash(Path file) throws IOException {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] buffer = new byte[8192]; // 8KB 缓冲区
            int bytesRead;
            try (InputStream fis = Files.newInputStream(file)) {
                while ((bytesRead = fis.read(buffer)) != -1) {
                    digest.update(buffer, 0, bytesRead);
                }
            }
            return bytesToHex(digest.digest());
        } catch (NoSuchAlgorithmException e) {
            // SHA-256 在所有 Java 实现中都应该可用
            throw new IOException("SHA-256 algorithm not available", e);
        }
    }

    /**
     * 将字节数组转换为十六进制字符串
     */
    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}

