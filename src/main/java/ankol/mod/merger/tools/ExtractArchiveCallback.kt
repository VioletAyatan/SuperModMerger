package ankol.mod.merger.tools

import ankol.mod.merger.core.filetrees.PathFileTree
import ankol.mod.merger.tools.Tools.getEntryFileName
import net.sf.sevenzipjbinding.*
import java.io.File
import java.nio.file.Path
import java.util.zip.CRC32C
import kotlin.io.path.nameWithoutExtension

class ExtractArchiveCallback(
    val inArchive: IInArchive,
    val pakPath: Path,
    val outputDir: Path
) : IArchiveExtractCallback {
    /**
     * Calculate CRC32C checksum for the extracted file.
     */
    private val crc32c = CRC32C()

    override fun getStream(
        index: Int,
        extractAskMode: ExtractAskMode
    ): ISequentialOutStream? {
        val entryFileName = inArchive.getProperty(index, PropID.PATH) as String?
        val isFolder = inArchive.getProperty(index, PropID.IS_FOLDER) as Boolean

        if (!isFolder || entryFileName == null) {
            return null
        }

        val pakName = pakPath.fileName.nameWithoutExtension
        entryFileName.replaceFirst("${pakName}${File.separator}", "") //for RAR file, the name will pick some pakname prefix. Need to remove!
        val fileName = getEntryFileName(entryFileName)

        return object : ISequentialOutStream {
            override fun write(data: ByteArray): Int {
                return 0
            }
        }
    }

    override fun prepareOperation(extractAskMode: ExtractAskMode) {
        TODO("Not yet implemented")
    }

    override fun setOperationResult(extractOperationResult: ExtractOperationResult) {
        TODO("Not yet implemented")
    }

    override fun setTotal(total: Long) = Unit

    override fun setCompleted(complete: Long) = Unit

    fun getPathTree(): List<PathFileTree> {
        return emptyList()
    }
}
