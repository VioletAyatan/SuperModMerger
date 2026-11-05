package ankol.mod.merger.tools

import ankol.mod.merger.core.files.ExtractedArchiveFile
import ankol.mod.merger.tools.Localizations.t
import ankol.mod.merger.tools.Tools.getEntryFileName
import net.sf.sevenzipjbinding.*
import java.io.OutputStream
import java.nio.file.Path
import java.util.zip.CRC32C
import kotlin.io.path.createDirectories
import kotlin.io.path.deleteIfExists
import kotlin.io.path.nameWithoutExtension
import kotlin.io.path.outputStream

class ExtractArchiveCallback(
    val inArchive: IInArchive,
    val pakPath: Path,
    val archiveNames: MutableList<String>,
    val archiveFileMap: MutableMap<String, ExtractedArchiveFile>,
    val outputDir: Path,
    val nestedArchives: MutableList<Pair<String, Path>>
) : IArchiveExtractCallback {
    private val log = logger()

    private lateinit var fileOutputPath: Path
    private lateinit var outputStream: OutputStream
    private lateinit var fileEntryName: String
    private lateinit var fileName: String

    private var skipped = false
    private val crc32 = CRC32C()

    /**
     * 被 [ArchivePathGuard] 拒绝的不安全条目
     */
    var unsafeEntryError: SecurityException? = null
        private set

    override fun getStream(
        index: Int,
        extractAskMode: ExtractAskMode
    ): ISequentialOutStream? {
        skipped = false
        val rawEntryName = inArchive.getProperty(index, PropID.PATH) as String
        val isFolder = inArchive.getProperty(index, PropID.IS_FOLDER) as Boolean

        if (isFolder) {
            skipped = true
            return null
        }

        val pakName = pakPath.fileName.nameWithoutExtension
        val resolvedEntry = try {
            ArchivePathGuard.resolve(outputDir, normalize(rawEntryName, pakName))
        } catch (e: SecurityException) {
            unsafeEntryError = e
            throw e
        }
        fileEntryName = resolvedEntry.relativeEntryName
        fileName = getEntryFileName(fileEntryName)

        fileOutputPath = resolvedEntry.outputPath
        fileOutputPath.parent.createDirectories()
        outputStream = fileOutputPath.outputStream()

        return ISequentialOutStream { data ->
            outputStream.write(data)
            crc32.update(data)
            data.size
        }
    }

    override fun setOperationResult(result: ExtractOperationResult) {
        if (skipped) return

        val fileHash = crc32.value.toString()
        close()

        if (result != ExtractOperationResult.OK) {
            fileOutputPath.deleteIfExists()
            log.warn("Failed to extract entry '{}' from '{}': {}", fileEntryName, pakPath, result)
            ErrorReporter.addErrorReport(
                archiveNames.joinToString(" -> "),
                fileEntryName,
                t("ERROR_EXTRACT_ENTRY_FAILED", result)
            )
            return
        }

        if (isArchiveFile(fileName)) {
            nestedArchives.add(fileName to fileOutputPath)
        } else {
            archiveFileMap[fileEntryName] = ExtractedArchiveFile(
                fileName,
                fileEntryName,
                archiveNames,
                fileHash,
                fileOutputPath
            )
        }
    }

    override fun prepareOperation(extractAskMode: ExtractAskMode) = Unit

    override fun setTotal(total: Long) = Unit

    override fun setCompleted(complete: Long) = Unit

    private fun close() {
        outputStream.close()
        crc32.reset()
    }

    /**
     * 规范化条目路径：去掉开头与压缩包同名的那一层目录（两种分隔符都识别）。
     * 部分 RAR 包解压出的条目会带上压缩包名作为前缀，不去掉会导致路径与基准包对不上。
     * @param entryName 压缩包条目名称
     * @param pakName pak名称
     */
    private fun normalize(entryName: String, pakName: String): String {
        if (entryName.length > pakName.length + 1
            && entryName.startsWith(pakName)
            && (entryName[pakName.length] == '/' || entryName[pakName.length] == '\\')
        ) {
            return entryName.substring(pakName.length + 1)
        }
        return entryName
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
}
