package ankol.mod.merger.tools

import ankol.mod.merger.core.filetrees.PathFileTree
import ankol.mod.merger.tools.Localizations.t
import ankol.mod.merger.tools.Tools.bytesToHex
import ankol.mod.merger.tools.Tools.getEntryFileName
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
import java.security.DigestInputStream
import java.security.DigestOutputStream
import java.security.MessageDigest
import java.util.concurrent.atomic.AtomicInteger
import kotlin.io.path.createDirectories
import kotlin.io.path.createFile
import kotlin.io.path.isRegularFile

/**
 * .pak文件管理工具
 * 
 * @author Ankol
 */
object PakManager {
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
        tempDir.createDirectories()
        val archiveName = pakPath.fileName.toString()
        val fileTreeMap = hashMapOf<String, PathFileTree>()
        val archiveNames = mutableListOf(archiveName)
        when {
            archiveName.endsWith(".7z") -> {
                extract7zRecursive(pakPath, tempDir, fileTreeMap, archiveNames)
            }

            Strings.CI.endsWithAny(archiveName, ".zip", ".pak") -> {
                extractZipRecursive(pakPath, tempDir, fileTreeMap, archiveNames)
            }

            else -> {
                throw IllegalArgumentException("Invalid archive name: $archiveName")
            }
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
    private fun extractZipRecursive(
        archivePath: Path,
        outputDir: Path,
        fileTreeMap: MutableMap<String, PathFileTree>,
        archiveNames: MutableList<String>
    ) {
        ZipFile.builder()
            .setPath(archivePath)
            .setCharset(StandardCharsets.UTF_8)
            .get()
            .use { zipFile ->
                val digest = MessageDigest.getInstance("SHA-256")
                zipFile.entries.asSequence()
                    .filterNot { it.isDirectory }
                    .forEach { entry ->
                        val entryName = entry.name
                        val fileName = getEntryFileName(entryName)
                        val outputPath = outputDir.resolve(entryName)
                        outputPath.parent?.createDirectories()
                        // 解压文件
                        when (entry.size) {
                            0L -> outputPath.createFile()
                            else -> zipFile.getInputStream(entry).use { zin ->
                                DigestInputStream(zin, digest).use { din -> Files.copy(din, outputPath) }
                            }
                        }
                        val hash = bytesToHex(digest.digest())
                        // 处理嵌套压缩包
                        if (isArchiveFile(fileName)) {
                            handleNestedArchive(fileName, outputPath, outputDir, fileTreeMap, archiveNames)
                        } else {
                            addFileToTree(fileName, entryName, archiveNames, hash, outputPath, fileTreeMap)
                        }
                    }
            }
    }

    /**
     * 递归解压 7Z 格式压缩包（支持嵌套）
     *
     * 当遇到 .pak、.zip、.7z 或 .rar 文件时，会递归解压，并记录来源链
     * @param archivePath 压缩包路径
     * @param outputDir   输出目录
     * @param fileTreeMap 文件映射表，包含来源信息
     * @param archiveNames 当前压缩包名称（用于构建来源链）
     */
    private fun extract7zRecursive(
        archivePath: Path,
        outputDir: Path,
        fileTreeMap: MutableMap<String, PathFileTree>,
        archiveNames: MutableList<String>
    ) {
        SevenZFile.builder()
            .setPath(archivePath)
            .setCharset(StandardCharsets.UTF_8)
            .get()
            .use { sevenZFile ->
                val digest = MessageDigest.getInstance("SHA-256")
                generateSequence { sevenZFile.nextEntry }
                    .filterNot { it.isDirectory }
                    .forEach { entry ->
                        val entryName = entry.name
                        val fileName = getEntryFileName(entryName)
                        val outputPath = outputDir.resolve(entryName)
                        outputPath.parent?.createDirectories()

                        // 写入文件内容
                        when (entry.size) {
                            0L -> Files.createFile(outputPath)
                            else -> Files.newOutputStream(outputPath).use { output ->
                                DigestOutputStream(output, digest).use { dos ->
                                    val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                                    while (sevenZFile.read(buffer) != -1) {
                                        //写入文件内容，顺便计算哈希
                                        dos.write(buffer, 0, buffer.size)
                                    }
                                }
                            }
                        }

                        val hash = bytesToHex(digest.digest())

                        // 处理嵌套压缩包
                        if (isArchiveFile(fileName)) {
                            handleNestedArchive(fileName, outputPath, outputDir, fileTreeMap, archiveNames)
                        } else {
                            addFileToTree(fileName, entryName, archiveNames, hash, outputPath, fileTreeMap)
                        }
                    }
            }
    }

    /**
     * 处理嵌套压缩包
     */
    private fun handleNestedArchive(
        fileName: String,
        outputPath: Path,
        outputDir: Path,
        fileTreeMap: MutableMap<String, PathFileTree>,
        archiveNames: MutableList<String>
    ) {
        val sanitizedFileName = fileName.replace("[^a-zA-Z0-9._-]".toRegex(), "_")
        val nestedTempDir = outputDir.resolve(
            "_nested_${System.currentTimeMillis()}_${NESTED_COUNTER.getAndIncrement()}_$sanitizedFileName"
        )
        nestedTempDir.createDirectories()
        archiveNames.add(fileName)
        when {
            fileName.endsWith(".7z") -> {
                extract7zRecursive(outputPath, nestedTempDir, fileTreeMap, archiveNames)
            }

            Strings.CI.endsWithAny(fileName, ".zip", ".pak") -> {
                extractZipRecursive(outputPath, nestedTempDir, fileTreeMap, archiveNames)
            }

            else -> {
                throw IllegalArgumentException("Invalid archive name: $fileName")
            }
        }
    }

    /**
     * 将文件添加到文件树映射中
     */
    private fun addFileToTree(
        fileName: String,
        entryName: String,
        archiveNames: MutableList<String>,
        hash: String,
        outputPath: Path,
        fileTreeMap: MutableMap<String, PathFileTree>
    ) {
        val current = PathFileTree(fileName, entryName, archiveNames, hash, outputPath)

        fileTreeMap[entryName]?.let { existing ->
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
        fileTreeMap[entryName] = current
    }

    /**
     * 判断文件是否是支持的压缩包格式
     * 
     * @param fileName 文件名
     * @return 是否是压缩包文件
     */
    private fun isArchiveFile(fileName: String): Boolean =
        fileName.endsWith(".pak", ignoreCase = true) ||
                fileName.endsWith(".zip", ignoreCase = true) ||
                fileName.endsWith(".7z", ignoreCase = true) ||
                fileName.endsWith(".rar", ignoreCase = true)

    /**
     * 将合并后的文件打包成 .pak 文件
     * 
     * @param sourceDir 源目录（包含所有要打包的文件）
     * @param pakPath   输出 pak 文件路径
     */
    fun createPak(sourceDir: Path, pakPath: Path) {
        Files.createDirectories(pakPath.parent)

        ZipArchiveOutputStream(pakPath.toFile()).use { zipOut ->
            Files.walk(sourceDir).use { pathStream ->
                pathStream
                    .filter { it.isRegularFile() }
                    .forEach { file: Path ->
                        try {
                            // 计算相对路径，使用正斜杠作为路径分隔符（ZIP 标准）
                            val entryName = sourceDir.relativize(file)
                                .toString()
                                .replace(File.separator, "/")

                            ZipArchiveEntry(entryName).also { entry: ZipArchiveEntry ->
                                zipOut.putArchiveEntry(entry)
                                Files.copy(file, zipOut)
                                zipOut.closeArchiveEntry()
                            }
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
    fun areFilesIdentical(file1: PathFileTree, file2: PathFileTree): Boolean {
        return Files.size(file1.safeGetFullPathName()) == Files.size(file2.safeGetFullPathName())
                && file1.fileHash == file2.fileHash
    }
}
