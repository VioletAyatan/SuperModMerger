package ankol.mod.merger.tools;

import ankol.mod.merger.core.filetrees.PathFileTree;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.archivers.sevenz.SevenZArchiveEntry;
import org.apache.commons.compress.archivers.sevenz.SevenZFile;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.apache.commons.lang3.Strings;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

/**
 * .pak文件管理工具
 *
 * @author Ankol
 */
@Slf4j
public class PakManager {
    // 解压用的缓冲区
    private static final int BUFFER_SIZE = 65536;
    // 十六进制字符数组，用于快速转换
    private static final char[] HEX_ARRAY = "0123456789abcdef".toCharArray();
    // 嵌套解压计数器，确保目录名唯一性
    private static final AtomicInteger NESTED_COUNTER = new AtomicInteger(0);

    /**
     * 从 .pak 文件中提取所有文件到临时目录（支持递归解压嵌套压缩包）
     * <p>
     * 如果压缩包中包含 .pak、.zip、.7z 或 .rar 文件，会递归解压它们
     * 这样可以处理诸如 "zip里套pak" 这样的嵌套情况
     * <p>
     * 返回的映射包含文件来源信息，可以追踪嵌套链
     *
     * @param pakPath pak文件路径
     * @param tempDir 临时解压目录
     * @return 文件映射表 (相对路径 -> FileSourceInfo)，包含来源链信息
     */
    public static Map<String, PathFileTree> extractPak(Path pakPath, Path tempDir) throws IOException {
        Files.createDirectories(tempDir);
        String archiveName = pakPath.getFileName().toString();
        HashMap<String, PathFileTree> fileTreeMap = new HashMap<>(20);
        // 根据文件扩展名判断格式
        List<String> archiveNameChina = new ArrayList<>();
        archiveNameChina.add(archiveName);
        if (Strings.CI.endsWith(archiveName, ".7z")) {
            extract7zRecursive(pakPath, tempDir, fileTreeMap, archiveNameChina);
        } else {
            extractZipRecursive(pakPath, tempDir, fileTreeMap, archiveNameChina);
        }
        return fileTreeMap;
    }

    /**
     * 递归解压ZIP格式压缩包
     *
     * @param archivePath 压缩包路径
     * @param outputDir   输出目录
     * @param fileTreeMap 文件树映射表
     * @param archiveName 当前压缩包名称（用于构建来源链）
     */
    private static void extractZipRecursive(Path archivePath, Path outputDir, HashMap<String, PathFileTree> fileTreeMap, List<String> archiveName) throws IOException {
        try (ZipFile zipFile = ZipFile.builder().setPath(archivePath).setCharset(StandardCharsets.UTF_8).get()) {
            Enumeration<ZipArchiveEntry> entries = zipFile.getEntries();
            while (entries.hasMoreElements()) {
                ZipArchiveEntry entry = entries.nextElement();

                if (entry.isDirectory()) continue;

                String entryName = entry.getName();
                String fileName = Tools.getEntryFileName(entryName);
                //创建临时文件目录
                Path outputPath = outputDir.resolve(entryName);
                Files.createDirectories(outputPath.getParent());

                // entry size等于0，创建空文件
                if (entry.getSize() == 0) {
                    Files.createFile(outputPath);
                } else {
                    // 从 ZIP 中读取文件内容并写入
                    try (InputStream input = zipFile.getInputStream(entry)) {
                        Files.copy(input, outputPath);
                    }
                }
                //处理嵌套压缩包
                if (isArchiveFile(fileName)) {
                    String sanitizedFileName = fileName.replaceAll("[^a-zA-Z0-9._-]", "_");
                    Path nestedTempDir = outputDir.resolve(String.format("_nested_%d_%d_%s",
                            System.currentTimeMillis(),
                            NESTED_COUNTER.getAndIncrement(),
                            sanitizedFileName));
                    Files.createDirectories(nestedTempDir);
                    // 递归解压，根据文件类型选择解压方法，缓存 toLowerCase 结果
                    String lowerFileName = fileName.toLowerCase();
                    archiveName.add(fileName);
                    if (lowerFileName.endsWith(".7z")) {
                        extract7zRecursive(outputPath, nestedTempDir, fileTreeMap, archiveName);
                    } else {
                        extractZipRecursive(outputPath, nestedTempDir, fileTreeMap, archiveName);
                    }
                }
                // 创建文件来源信息，记录来源链
                else {
                    PathFileTree current = new PathFileTree(fileName, entryName, archiveName, outputPath);
                    // 检查是否已有相同路径的文件
                    if (fileTreeMap.containsKey(entryName)) {
                        PathFileTree existing = fileTreeMap.get(entryName);
                        ColorPrinter.warning(Localizations.t("PAK_MANAGER_DUPLICATE_FILE_DETECTED",
                                existing.getArchiveFileNames(),
                                current.getFileEntryName(),
                                existing.getFileEntryName())
                        );
                        ColorPrinter.success(Localizations.t("PAK_MANAGER_USE_NEW_PATH", current.getFileEntryName()));
                    }
                    fileTreeMap.put(entryName, current);
                }
            }
        }
    }

    /**
     * 递归解压 7Z 格式压缩包（支持嵌套）
     * <p>
     * 当遇到 .pak、.zip、.7z 或 .rar 文件时，会递归解压，并记录来源链
     *
     * @param archivePath 压缩包路径
     * @param outputDir   输出目录
     * @param fileTreeMap 文件映射表，包含来源信息
     * @param archiveName 当前压缩包名称（用于构建来源链）
     */
    private static void extract7zRecursive(Path archivePath, Path outputDir, HashMap<String, PathFileTree> fileTreeMap, List<String> archiveName) throws IOException {
        try (SevenZFile sevenZFile = SevenZFile.builder().setPath(archivePath)
                .setCharset(StandardCharsets.UTF_8)
                .get()
        ) {
            SevenZArchiveEntry entry;
            while ((entry = sevenZFile.getNextEntry()) != null) {
                if (entry.isDirectory()) continue;

                String entryName = entry.getName();
                String fileName = Tools.getEntryFileName(entryName);
                Path outputPath = outputDir.resolve(entryName);
                Files.createDirectories(outputPath.getParent());

                // 检查文件大小
                if (entry.getSize() == 0) {
                    Files.createFile(outputPath);
                } else {
                    // 从 7Z 中读取文件内容并写入，使用统一的缓冲区大小
                    try (var output = Files.newOutputStream(outputPath)) {
                        byte[] buffer = new byte[BUFFER_SIZE];
                        int bytesRead;
                        while ((bytesRead = sevenZFile.read(buffer)) != -1) {
                            output.write(buffer, 0, bytesRead);
                        }
                    }
                }

                // 检查是否是嵌套的压缩包（.pak、.zip 或 .7z）
                if (isArchiveFile(fileName)) {
                    // 创建嵌套压缩包的临时解压目录，使用原子计数器确保唯一性
                    String sanitizedFileName = fileName.replaceAll("[^a-zA-Z0-9._-]", "_");
                    Path nestedTempDir = outputDir.resolve(String.format("_nested_%d_%d_%s",
                            System.currentTimeMillis(),
                            NESTED_COUNTER.getAndIncrement(),
                            sanitizedFileName));
                    Files.createDirectories(nestedTempDir);
                    // 递归解压，根据文件类型选择解压方法，缓存 toLowerCase 结果
                    String lowerFileName = fileName.toLowerCase();
                    archiveName.add(fileName);
                    if (lowerFileName.endsWith(".7z")) {
                        extract7zRecursive(outputPath, nestedTempDir, fileTreeMap, archiveName);
                    } else {
                        extractZipRecursive(outputPath, nestedTempDir, fileTreeMap, archiveName);
                    }
                } else {
                    // 创建文件来源信息，记录来源链
                    PathFileTree current = new PathFileTree(fileName, entryName, archiveName, outputPath);

                    // 检查是否已有相同路径的文件（来自不同来源）
                    if (fileTreeMap.containsKey(entryName)) {
                        PathFileTree existing = fileTreeMap.get(entryName);
                        ColorPrinter.warning(Localizations.t("PAK_MANAGER_DUPLICATE_FILE_DETECTED",
                                existing.getArchiveFileNames(),
                                current.getFileEntryName(),
                                existing.getFileEntryName())
                        );
                        ColorPrinter.success(Localizations.t("PAK_MANAGER_USE_NEW_PATH", current.getFileEntryName()));
                    }
                    fileTreeMap.put(entryName, current);
                }
            }
        }
    }

    /**
     * 判断文件是否是支持的压缩包格式
     *
     * @param fileName 文件名
     * @return 是否是压缩包文件
     */
    private static boolean isArchiveFile(String fileName) {
        String lowerName = fileName.toLowerCase();
        return Strings.CI.endsWithAny(lowerName, ".pak", ".zip", ".7z", ".rar");
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
                                throw new RuntimeException(Localizations.t("PAK_MANAGER_FAILED_TO_ADD_FILE", file), e);
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
        // 快速判断，文件大小不同，肯定内容不同
        if (Files.size(file1) != Files.size(file2)) {
            return false;
        } else {
            //计算文件hash进行对比，更快且内存占用低
            return getFileHash(file1).equals(getFileHash(file2));
        }
    }

    /**
     * 计算文件的 SHA-256 哈希值（流式处理）
     * <p>
     * 使用 64KB 缓冲区逐块处理，即使对于 1GB 的文件也只占用恒定的内存。
     *
     * @param file 要计算哈希的文件
     * @return 十六进制格式的哈希值
     * @throws IOException 如果文件不可读
     */
    private static String getFileHash(Path file) throws IOException {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] buffer = new byte[BUFFER_SIZE];
            int bytesRead;
            try (InputStream fis = Files.newInputStream(file)) {
                while ((bytesRead = fis.read(buffer)) != -1) {
                    digest.update(buffer, 0, bytesRead);
                }
            }
            return bytesToHex(digest.digest());
        } catch (NoSuchAlgorithmException e) {
            throw new IOException("SHA-256 algorithm not available", e);
        }
    }

    /**
     * 将字节数组转换为十六进制字符串
     *
     * @param bytes 字节数组
     * @return 十六进制字符串
     */
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
