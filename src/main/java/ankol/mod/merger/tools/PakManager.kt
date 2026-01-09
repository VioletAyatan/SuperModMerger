package ankol.mod.merger.tools

import ankol.mod.merger.core.filetrees.PathFileTree
import ankol.mod.merger.tools.Localizations.t
import ankol.mod.merger.tools.Tools.getEntryFileName
import org.apache.commons.compress.archivers.sevenz.SevenZArchiveEntry
import org.apache.commons.compress.archivers.sevenz.SevenZFile
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream
import org.apache.commons.compress.archivers.zip.ZipFile
import org.apache.commons.lang3.Strings
import java.io.File
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.*
import java.util.concurrent.atomic.AtomicInteger

/**
 * .pak文件管理工具
 * 
 * @author Ankol
 */
object PakManager {
    private val log = logger()

    // 解压用的缓冲区
    private const val BUFFER_SIZE = 65536

    // 十六进制字符数组，用于快速转换
    private val HEX_ARRAY = "0123456789abcdef".toCharArray()

    // 嵌套解压计数器，确保目录名唯一性
    private val NESTED_COUNTER = AtomicInteger(0)

    /**
     * 从 .pak 文件中提取所有文件到临时目录（支持递归解压嵌套压缩包）
     * 
     * 如果压缩包中包含 .pak、.zip、.7z 或 .rar 文件，会递归解压它们
     * 这样可以处理诸如 "zip里套pak" 这样的嵌套情况
     * 
     * 返回的映射包含文件来源信息，可以追踪嵌套链
     * 
     * @param pakPath pak文件路径
     * @param tempDir 临时解压目录
     * @return 文件映射表 (相对路径 -> FileSourceInfo)，包含来源链信息
     */
    fun extractPak(pakPath: Path, tempDir: Path): MutableMap<String, PathFileTree> {
        Files.createDirectories(tempDir)
        val archiveName = pakPath.fileName.toString()
        val fileTreeMap = HashMap<String, PathFileTree>(20)
        // 根据文件扩展名判断格式
        val archiveNameChina = mutableListOf<String>()
        archiveNameChina.add(archiveName)
        if (Strings.CI.endsWith(archiveName, ".7z")) {
            extract7zRecursive(pakPath, tempDir, fileTreeMap, archiveNameChina)
        } else {
            extractZipRecursive(pakPath, tempDir, fileTreeMap, archiveNameChina)
        }
        return fileTreeMap
    }

    /**
     * 递归解压ZIP格式压缩包
     * 
     * @param archivePath 压缩包路径
     * @param outputDir   输出目录
     * @param fileTreeMap 文件树映射表
     * @param archiveNames 当前压缩包名称（用于构建来源链）
     */
    private fun extractZipRecursive(archivePath: Path, outputDir: Path, fileTreeMap: HashMap<String, PathFileTree>, archiveNames: MutableList<String>) {
        ZipFile.builder().setPath(archivePath).setCharset(StandardCharsets.UTF_8).get().use { zipFile ->
            val entries = zipFile.getEntries()
            while (entries.hasMoreElements()) {
                val entry = entries.nextElement()

                if (entry.isDirectory()) continue

                val entryName = entry.getName()
                val fileName = getEntryFileName(entryName)
                //创建临时文件目录
                val outputPath = outputDir.resolve(entryName)
                Files.createDirectories(outputPath.getParent())

                // entry size等于0，创建空文件
                if (entry.getSize() == 0L) {
                    Files.createFile(outputPath)
                } else {
                    // 从 ZIP 中读取文件内容并写入
                    zipFile.getInputStream(entry).use { input ->
                        Files.copy(input, outputPath)
                    }
                }
                //处理嵌套压缩包
                if (isArchiveFile(fileName)) {
                    val sanitizedFileName = fileName.replace("[^a-zA-Z0-9._-]".toRegex(), "_")
                    val nestedTempDir = outputDir.resolve(
                        String.format(
                            "_nested_%d_%d_%s",
                            System.currentTimeMillis(),
                            NESTED_COUNTER.getAndIncrement(),
                            sanitizedFileName
                        )
                    )
                    Files.createDirectories(nestedTempDir)
                    // 递归解压，根据文件类型选择解压方法，缓存 toLowerCase 结果
                    val lowerFileName = fileName.lowercase(Locale.getDefault())
                    archiveNames.add(fileName)
                    if (lowerFileName.endsWith(".7z")) {
                        extract7zRecursive(outputPath, nestedTempDir, fileTreeMap, archiveNames)
                    } else {
                        extractZipRecursive(outputPath, nestedTempDir, fileTreeMap, archiveNames)
                    }
                } else {
                    val current = PathFileTree(fileName, entryName, archiveNames, outputPath)
                    // 检查是否已有相同路径的文件
                    if (fileTreeMap.containsKey(entryName)) {
                        val existing: PathFileTree = fileTreeMap.get(entryName)!!
                        ColorPrinter.warning(
                            t(
                                "PAK_MANAGER_DUPLICATE_FILE_DETECTED",
                                existing.archiveFileNames,
                                current.fileEntryName,
                                existing.fileEntryName
                            )
                        )
                        ColorPrinter.success(t("PAK_MANAGER_USE_NEW_PATH", current.fileEntryName))
                    }
                    fileTreeMap.put(entryName, current)
                }
            }
        }
    }

    /**
     * 递归解压 7Z 格式压缩包（支持嵌套）
     * 
     * 
     * 当遇到 .pak、.zip、.7z 或 .rar 文件时，会递归解压，并记录来源链
     * 
     * @param archivePath 压缩包路径
     * @param outputDir   输出目录
     * @param fileTreeMap 文件映射表，包含来源信息
     * @param archiveNames 当前压缩包名称（用于构建来源链）
     */
    private fun extract7zRecursive(archivePath: Path, outputDir: Path, fileTreeMap: HashMap<String, PathFileTree>, archiveNames: MutableList<String>) {
        SevenZFile.builder().setPath(archivePath).setCharset(StandardCharsets.UTF_8).get().use { sevenZFile ->
                var entry: SevenZArchiveEntry?
                while ((sevenZFile.getNextEntry().also { entry = it }) != null) {
                    if (entry!!.isDirectory) continue

                    val entryName = entry.name
                    val fileName = getEntryFileName(entryName)
                    val outputPath = outputDir.resolve(entryName)
                    Files.createDirectories(outputPath.parent)

                    // 检查文件大小
                    if (entry.size == 0L) {
                        Files.createFile(outputPath)
                    } else {
                        // 从 7Z 中读取文件内容并写入，使用统一的缓冲区大小
                        Files.newOutputStream(outputPath).use { output ->
                            val buffer = ByteArray(BUFFER_SIZE)
                            var bytesRead: Int
                            while ((sevenZFile.read(buffer).also { bytesRead = it }) != -1) {
                                output.write(buffer, 0, bytesRead)
                            }
                        }
                    }

                    // 检查是否是嵌套的压缩包（.pak、.zip 或 .7z）
                    if (isArchiveFile(fileName)) {
                        // 创建嵌套压缩包的临时解压目录，使用原子计数器确保唯一性
                        val sanitizedFileName = fileName.replace("[^a-zA-Z0-9._-]".toRegex(), "_")
                        val nestedTempDir = outputDir.resolve(
                            String.format(
                                "_nested_%d_%d_%s",
                                System.currentTimeMillis(),
                                NESTED_COUNTER.getAndIncrement(),
                                sanitizedFileName
                            )
                        )
                        Files.createDirectories(nestedTempDir)
                        // 递归解压，根据文件类型选择解压方法，缓存 toLowerCase 结果
                        val lowerFileName = fileName.lowercase(Locale.getDefault())
                        archiveNames.add(fileName)
                        if (lowerFileName.endsWith(".7z")) {
                            extract7zRecursive(outputPath, nestedTempDir, fileTreeMap, archiveNames)
                        } else {
                            extractZipRecursive(outputPath, nestedTempDir, fileTreeMap, archiveNames)
                        }
                    } else {
                        // 创建文件来源信息，记录来源链
                        val current = PathFileTree(fileName, entryName, archiveNames, outputPath)

                        // 检查是否已有相同路径的文件（来自不同来源）
                        if (fileTreeMap.containsKey(entryName)) {
                            val existing: PathFileTree = fileTreeMap.get(entryName)!!
                            ColorPrinter.warning(
                                t(
                                    "PAK_MANAGER_DUPLICATE_FILE_DETECTED",
                                    existing.archiveFileNames,
                                    current.fileEntryName,
                                    existing.fileEntryName
                                )
                            )
                            ColorPrinter.success(t("PAK_MANAGER_USE_NEW_PATH", current.fileEntryName))
                        }
                        fileTreeMap.put(entryName, current)
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
    private fun isArchiveFile(fileName: String): Boolean {
        val lowerName = fileName.lowercase(Locale.getDefault())
        return Strings.CI.endsWithAny(lowerName, ".pak", ".zip", ".7z", ".rar")
    }

    /**
     * 将合并后的文件打包成 .pak 文件
     * 
     * @param sourceDir 源目录（包含所有要打包的文件）
     * @param pakPath   输出 pak 文件路径
     */
    @Throws(IOException::class)
    fun createPak(sourceDir: Path, pakPath: Path) {
        Files.createDirectories(pakPath.getParent())

        ZipArchiveOutputStream(pakPath.toFile()).use { zipOut ->
            Files.walk(sourceDir).use { pathStream ->
                pathStream.filter { path: Path? -> Files.isRegularFile(path) }
                    .forEach { file: Path? ->
                        try {
                            // 计算相对路径
                            var entryName = sourceDir.relativize(file).toString()
                            // 使用正斜杠作为路径分隔符（ZIP 标准）
                            entryName = entryName.replace(File.separator, "/")

                            val entry = ZipArchiveEntry(entryName)
                            zipOut.putArchiveEntry(entry)

                            // 写入文件内容
                            Files.copy(file, zipOut)

                            zipOut.closeArchiveEntry()
                        } catch (e: IOException) {
                            throw RuntimeException(t("PAK_MANAGER_FAILED_TO_ADD_FILE", file), e)
                        }
                    }
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
    @Throws(IOException::class)
    fun areFilesIdentical(file1: Path, file2: Path): Boolean {
        // 快速判断，文件大小不同，肯定内容不同
        if (Files.size(file1) != Files.size(file2)) {
            return false
        } else {
            //计算文件hash进行对比，更快且内存占用低
            return getFileHash(file1) == getFileHash(file2)
        }
    }

    /**
     * 计算文件的 SHA-256 哈希值（流式处理）
     * 
     * 
     * 使用 64KB 缓冲区逐块处理，即使对于 1GB 的文件也只占用恒定的内存。
     * 
     * @param file 要计算哈希的文件
     * @return 十六进制格式的哈希值
     * @throws IOException 如果文件不可读
     */
    @Throws(IOException::class)
    private fun getFileHash(file: Path): String {
        try {
            val digest = MessageDigest.getInstance("SHA-256")
            val buffer = ByteArray(BUFFER_SIZE)
            var bytesRead: Int
            Files.newInputStream(file).use { fis ->
                while ((fis.read(buffer).also { bytesRead = it }) != -1) {
                    digest.update(buffer, 0, bytesRead)
                }
            }
            return bytesToHex(digest.digest())
        } catch (e: NoSuchAlgorithmException) {
            throw IOException("SHA-256 algorithm not available", e)
        }
    }

    /**
     * 将字节数组转换为十六进制字符串
     * 
     * @param bytes 字节数组
     * @return 十六进制字符串
     */
    private fun bytesToHex(bytes: ByteArray): String {
        val hexChars = CharArray(bytes.size * 2)
        for (i in bytes.indices) {
            val v = bytes[i].toInt() and 0xFF
            hexChars[i * 2] = HEX_ARRAY[v ushr 4]
            hexChars[i * 2 + 1] = HEX_ARRAY[v and 0x0F]
        }
        return String(hexChars)
    }
}
