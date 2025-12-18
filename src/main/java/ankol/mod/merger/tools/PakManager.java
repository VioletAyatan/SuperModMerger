package ankol.mod.merger.tools;

import cn.hutool.core.util.StrUtil;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

/**
 * .pak 文件管理工具 - 处理 .pak 文件的打开、读取和写入
 * .pak 文件本质上是 ZIP 压缩包，因此使用 ZIP 相关的 API 处理
 */
public class PakManager {

    /**
     * 从 .pak 文件中提取所有文件到临时目录
     *
     * @param pakPath pak文件路径
     * @param tempDir 临时解压目录
     * @return 文件映射表 (相对路径 -> 实际文件路径)
     */
    public static Map<String, Path> extractPak(Path pakPath, Path tempDir) throws IOException {
        Files.createDirectories(tempDir);
        Map<String, Path> fileMap = new HashMap<>();

        try (ZipFile zipFile = new ZipFile(pakPath.toFile())) {
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();

                if (entry.isDirectory()) continue;

                // 创建文件的完整路径
                Path outputPath = tempDir.resolve(entry.getName());
                Files.createDirectories(outputPath.getParent());

                // 从 ZIP 中读取文件内容并写入
                try (InputStream input = zipFile.getInputStream(entry)) {
                    Files.copy(input, outputPath);
                }

                // 记录映射关系
                fileMap.put(entry.getName(), outputPath);
            }
        }

        return fileMap;
    }

    /**
     * 将合并后的文件打包成 .pak 文件
     *
     * @param sourceDir 源目录（包含所有要打包的文件）
     * @param pakPath   输出 pak 文件路径
     */
    public static void createPak(Path sourceDir, Path pakPath) throws IOException {
        Files.createDirectories(pakPath.getParent());

        try (ZipOutputStream zipOut = new ZipOutputStream(Files.newOutputStream(pakPath))) {
            // 遍历源目录中的所有文件
            Files.walk(sourceDir)
                    .filter(Files::isRegularFile)
                    .forEach(file -> {
                        try {
                            // 计算相对路径
                            String entryName = sourceDir.relativize(file).toString();
                            // 使用正斜杠作为路径分隔符（ZIP 标准）
                            entryName = entryName.replace(File.separator, "/");

                            ZipEntry entry = new ZipEntry(entryName);
                            zipOut.putNextEntry(entry);

                            // 写入文件内容
                            Files.copy(file, zipOut);

                            zipOut.closeEntry();
                        } catch (IOException e) {
                            throw new RuntimeException("Failed to add file to PAK: " + file, e);
                        }
                    });
        }
    }

    /**
     * 获取 .pak 文件中的所有文件列表
     *
     * @param pakPath pak文件路径
     * @return 文件列表
     */
    public static List<String> listPakContents(Path pakPath) throws IOException {
        List<String> contents = new ArrayList<>();

        try (ZipFile zipFile = new ZipFile(pakPath.toFile())) {
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                if (!entry.isDirectory()) {
                    contents.add(entry.getName());
                }
            }
        }

        return contents;
    }

    /**
     * 判断两个文件在内容上是否相同
     * <p>
     * 先判断字节大小是否一致，不一致肯定内容不同
     * 一致则读取内容进行对比
     */
    public static boolean areFilesIdentical(Path file1, Path file2) throws IOException {
        if (Files.size(file1) != Files.size(file2)) {
            return false;
        }
        return Files.readString(file1).equals(Files.readString(file2));
    }

    /**
     * 判断是否为脚本文件（需要进行智能合并）
     */
    public static boolean isScriptFile(String filename) {
        String lower = filename.toLowerCase();
        return StrUtil.endWithAny(lower, ".scr", ".def", ".loot", ".ppfx", ".ares", ".mpcloth");
    }

    /**
     * 判断是否为 XML 文件
     */
    public static boolean isXmlFile(String filename) {
        return filename.toLowerCase().endsWith(".xml");
    }
}

