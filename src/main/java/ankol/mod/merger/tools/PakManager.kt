package ankol.mod.merger.tools

import ankol.mod.merger.core.filetrees.PathFileTree
import ankol.mod.merger.tools.Localizations.t
import ankol.mod.merger.tools.Tools.bytesToHex
import ankol.mod.merger.tools.Tools.getEntryFileName
import net.sf.sevenzipjbinding.ExtractOperationResult
import net.sf.sevenzipjbinding.SevenZip
import net.sf.sevenzipjbinding.impl.RandomAccessFileInStream
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.RandomAccessFile
import java.nio.file.Files
import java.nio.file.Path
import java.security.MessageDigest
import java.util.concurrent.atomic.AtomicInteger
import kotlin.io.path.createDirectories
import kotlin.io.path.isRegularFile
import kotlin.io.path.nameWithoutExtension

/**
 * .pak file management utility
 *
 * @author Ankol
 */
object PakManager {
    private val NESTED_COUNTER = AtomicInteger(0)

    init {
        SevenZip.initSevenZipFromPlatformJAR()
    }

    /**
     * Extract all files from a .pak file to a temporary directory (supports recursive extraction of nested archives)
     *
     * If the archive contains .pak, .zip, .7z, or .rar files, they will be recursively extracted.
     * This allows handling cases like "zip containing pak" and other nesting situations.
     *
     * The returned mapping contains file source information, allowing tracking of the nesting chain.
     *
     * @param pakPath Path to the pak file
     * @param tempDir Temporary extraction directory
     * @return File mapping (relative path -> FileSourceInfo), including source chain information
     */
    fun extractPak(archiveName: String, pakPath: Path, tempDir: Path): MutableMap<String, PathFileTree> {
        tempDir.createDirectories()
        val fileTreeMap = hashMapOf<String, PathFileTree>()
        val archiveNames = mutableListOf(archiveName)
        extractArchiveRecursive(pakPath, tempDir, fileTreeMap, archiveNames)
        return fileTreeMap
    }

    /**
     * Recursively extract archives using sevenzipjbinding (supports ZIP, 7z, RAR and nested archives)
     *
     * When encountering .pak, .zip, .7z, or .rar files, they will be recursively extracted, and the source chain will be recorded.
     * @param pakPath Archive path
     * @param outputDir   Output directory
     * @param fileTreeMap File mapping, including source information
     * @param archiveNames Current archive names (for building source chain)
     */
    private fun extractArchiveRecursive(
        pakPath: Path,
        outputDir: Path,
        fileTreeMap: MutableMap<String, PathFileTree>,
        archiveNames: MutableList<String>
    ) {
        val normalizedOutputDir = outputDir.normalize()
        val archiveName = pakPath.fileName.nameWithoutExtension
        RandomAccessFileInStream(RandomAccessFile(pakPath.toFile(), "r")).use { inStream ->
            SevenZip.openInArchive(null, inStream).use { archive ->
                val digest = MessageDigest.getInstance("SHA-256")
                for (item in archive.simpleInterface.archiveItems) {
                    if (item.isFolder) continue

                    val entryName = item.path.replaceFirst("${archiveName}${File.separator}", "")
                    val fileName = getEntryFileName(entryName, File.separator)
                    val outputPath = outputDir.resolve(entryName).normalize()

                    // Path traversal protection
                    require(outputPath.startsWith(normalizedOutputDir)) { "Path traversal detected in archive entry: $entryName" }
                    outputPath.parent.createDirectories()

                    // Extract file and calculate hash simultaneously
                    FileOutputStream(outputPath.toFile()).use { fos ->
                        val result = item.extractSlow { data ->
                            fos.write(data)
                            digest.update(data)
                            data.size
                        }
                        require(result == ExtractOperationResult.OK) { "Failed to extract entry: $entryName, result: $result" }
                    }

                    val hash = bytesToHex(digest.digest())

                    // Handle nested archives
                    if (isArchiveFile(fileName)) {
                        handleNestedArchive(fileName, outputPath, outputDir, fileTreeMap, archiveNames)
                    } else {
                        addFileToTree(fileName, entryName, archiveNames, hash, outputPath, fileTreeMap)
                    }
                }
            }
        }
    }

    /**
     * Handle nested archives
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
        extractArchiveRecursive(outputPath, nestedTempDir, fileTreeMap, archiveNames)
    }

    /**
     * Add file to the file tree mapping
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
     * Determine whether the file is a supported archive format
     *
     * @param fileName File name
     * @return Whether it is an archive file
     */
    private fun isArchiveFile(fileName: String): Boolean =
        fileName.endsWith(".pak", ignoreCase = true) ||
                fileName.endsWith(".zip", ignoreCase = true) ||
                fileName.endsWith(".7z", ignoreCase = true) ||
                fileName.endsWith(".rar", ignoreCase = true)

    /**
     * Package merged files into a .pak file
     *
     * @param sourceDir Source directory (contains all files to be packaged)
     * @param pakPath   Output pak file path
     */
    fun createPak(sourceDir: Path, pakPath: Path) {
        Files.createDirectories(pakPath.parent)

        ZipArchiveOutputStream(pakPath.toFile()).use { zipOut ->
            Files.walk(sourceDir).use { pathStream ->
                pathStream
                    .filter { it.isRegularFile() }
                    .forEach { file: Path ->
                        try {
                            // Calculate relative path, use forward slash as path separator (ZIP standard)
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
     * Determine whether two files are identical in content
     *
     * @param file1 First file
     * @param file2 Second file
     * @return Whether the contents of the two files are identical
     * @throws IOException If the file is unreadable
     */
    fun areFilesIdentical(file1: PathFileTree, file2: PathFileTree): Boolean {
        return Files.size(file1.safeGetFilePath()) == Files.size(file2.safeGetFilePath())
                && file1.fileHash == file2.fileHash
    }
}
