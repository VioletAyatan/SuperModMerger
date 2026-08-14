package ankol.mod.merger.tools

import ankol.mod.merger.core.filetrees.PathFileTree
import ankol.mod.merger.tools.Tools.getEntryFileName
import net.sf.sevenzipjbinding.*
import java.io.File
import java.io.OutputStream
import java.nio.file.Path
import java.util.zip.CRC32C
import kotlin.io.path.createDirectories
import kotlin.io.path.nameWithoutExtension
import kotlin.io.path.outputStream

class ExtractArchiveCallback(
    val inArchive: IInArchive,
    val pakPath: Path,
    val archiveNames: MutableList<String>,
    val fileTreeMap: MutableMap<String, PathFileTree>,
    val outputDir: Path,
    val nestedArchives: MutableList<Pair<String, Path>>
) : IArchiveExtractCallback {

    private lateinit var fileOutputPath: Path
    private lateinit var outputStream: OutputStream
    private lateinit var fileEntryName: String
    private lateinit var fileName: String

    private var skipped = false
    private val crc32 = CRC32C()

    override fun getStream(
        index: Int,
        extractAskMode: ExtractAskMode
    ): ISequentialOutStream? {
        skipped = false
        fileEntryName = inArchive.getProperty(index, PropID.PATH) as String
        val isFolder = inArchive.getProperty(index, PropID.IS_FOLDER) as Boolean

        if (isFolder) {
            skipped = true
            return null
        }

        val pakName = pakPath.fileName.nameWithoutExtension
        fileEntryName = fileEntryName.replaceFirst("${pakName}${File.separator}", "")
        val resolvedEntry = ArchivePathGuard.resolve(outputDir, fileEntryName)
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

        if (isArchiveFile(fileName)) {
            nestedArchives.add(fileName to fileOutputPath)
        } else {
            fileTreeMap[fileEntryName] = PathFileTree(
                fileName,
                fileEntryName,
                archiveNames,
                crc32.value.toString(),
                fileOutputPath
            )
        }
        close()
    }

    override fun prepareOperation(extractAskMode: ExtractAskMode) = Unit

    override fun setTotal(total: Long) = Unit

    override fun setCompleted(complete: Long) = Unit

    private fun close() {
        outputStream.close()
        crc32.reset()
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
