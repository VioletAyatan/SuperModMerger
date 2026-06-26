package ankol.mod.merger.core

import ankol.mod.merger.core.filetrees.BaseFile
import ankol.mod.merger.domain.ParsedResult
import ankol.mod.merger.exception.ExitProcessException
import ankol.mod.merger.tools.ConsoleColorPrinter
import ankol.mod.merger.tools.Localizations.t
import ankol.mod.merger.tools.SoftLruCache
import ankol.mod.merger.tools.Tools.getEntryFileName
import ankol.mod.merger.tools.logger
import net.sf.sevenzipjbinding.IInArchive
import net.sf.sevenzipjbinding.SevenZip
import net.sf.sevenzipjbinding.impl.RandomAccessFileInStream
import net.sf.sevenzipjbinding.simple.ISimpleInArchiveItem
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.RandomAccessFile
import java.lang.AutoCloseable
import java.nio.file.Path
import kotlin.io.path.exists

/**
 * Base MOD Manager
 * Responsible for operations related to the base MOD
 *
 * @author Ankol
 */
class BaseModManager(private val basePakDirPath: Path) : AutoCloseable {
    private val log = logger()

    /**
     * Mapping from file name to standard path
     * Key: file name (lowercase)
     * Value: relative path in the base MOD
     */
    var indexedBaseModFileMap: MutableMap<String, BaseFile> = mutableMapOf()

    /**
     * Whether the base MOD has been loaded
     */
    var isLoaded = false

    private val basePakSet = setOf("data0.pak", "data1.pak", "databt.mpak")

    /** AST tree cache **/
    private val astTreeCache = SoftLruCache<String, ParsedResult<*>>(2048)

    /** File content cache **/
    private val fileContentCache = SoftLruCache<String, CachedBaseFile>(2048)

    private data class CachedBaseFile(
        val content: String,
    )

    /**
     * Reuse zip-connection to avoid repeatedly opening and closing the same file, which is costly.
     * Keep it open until the merge is complete, then close it in the close() method.
     */
    private lateinit var zipFileConnections: Map<String, IInArchive>

    init {
        load()
    }

    /**
     * Load the base MOD
     */
    fun load() {
        if (isLoaded) {
            ConsoleColorPrinter.warning(t("BASE_MOD_ALREADY_LOADED"))
            return
        }
        val startTime = System.currentTimeMillis()
        this.zipFileConnections = openArchiveConnection()
        this.indexedBaseModFileMap = indexBasePack(zipFileConnections)
        this.isLoaded = true
        val timetake = System.currentTimeMillis() - startTime
        ConsoleColorPrinter.success(t("BASE_MOD_INDEXED_FILES", basePakDirPath.fileName, indexedBaseModFileMap.size, timetake))
    }

    /**
     * Validate the base MOD path and establish Zip connections
     */
    private fun openArchiveConnection(): MutableMap<String, IInArchive> {
        if (!basePakDirPath.exists()) {
            throw IOException(t("BASE_MOD_FILE_NOT_FOUND", basePakDirPath))
        }
        val connectionMap = mutableMapOf<String, IInArchive>()
        for (pakName in basePakSet) {
            try {
                val resolvePath = basePakDirPath.resolve(pakName)
                if (resolvePath.exists()) {
                    //Establish archive connection
                    val inArchive = SevenZip.openInArchive(
                        null,
                        RandomAccessFileInStream(RandomAccessFile(resolvePath.toFile(), "r"))
                    )
                    connectionMap[pakName] = inArchive
                }
            } catch (e: RuntimeException) {
                log.error("Failed to create zip file $pakName. Reason: ${e.message}", e)
            }
        }
        if (connectionMap.isEmpty()) {
            throw ExitProcessException(1, t("BASE_MOD_NO_VALID_PAK_FOUND", basePakDirPath))
        }
        return connectionMap
    }

    /**
     * Index the base game PAK, create a mapping table for the files inside for subsequent operations
     */
    private fun indexBasePack(archiveConnection: Map<String, IInArchive>): MutableMap<String, BaseFile> {
        val pakIndexMap = HashMap<String, BaseFile>()
        archiveConnection.forEach { (pakName, inArchive) ->
            inArchive.simpleInterface.archiveItems.forEach { archiveEntry: ISimpleInArchiveItem ->
                if (!archiveEntry.isFolder) {
                    val archiveEntryName = archiveEntry.path
                    val fileName = getEntryFileName(archiveEntryName)
                    if (fileName in pakIndexMap) {
                        ConsoleColorPrinter.warning(
                            t(
                                "TOOLS_SAME_FILE_NAME_WARNING",
                                fileName,
                                archiveEntryName,
                                pakIndexMap[fileName]?.archiveEntryName
                            )
                        )
                    }
                    pakIndexMap[fileName] = BaseFile(fileName, archiveEntryName, archiveEntry.itemIndex, pakName)
                }
            }
        }
        return pakIndexMap
    }

    /**
     * Extract the content of the specified file from the base MOD (with cache optimization)
     *
     * @param entryFilePath Relative path of the file in the base MOD
     * @return File content, or null if the file does not exist
     */
    fun extractFileContent(entryFilePath: String): String? {
        if (!isLoaded) {
            return null
        }
        val fileName = getEntryFileName(entryFilePath)
        val baseFile = indexedBaseModFileMap[fileName] ?: return null

        val fileEntryName = baseFile.archiveEntryName
        val cached = fileContentCache.get(fileEntryName)
        if (cached != null) {
            return cached.content.ifEmpty { null }
        }

        val extracted = extractFileFromPak(baseFile.basePakName, baseFile)
        fileContentCache.put(fileEntryName, extracted)
        if (extracted.content.isEmpty()) {
            return null
        }
        return extracted.content
    }

    /**
     * Extract the specified file from the PAK file and cache the content and hash in memory
     */
    private fun extractFileFromPak(basePakName: String, entryBaseFile: BaseFile): CachedBaseFile {
        val inArchive = zipFileConnections[basePakName] ?: throw IOException(t("BASE_MOD_FILE_NOT_FOUND", basePakName))
        var content = ""
        ByteArrayOutputStream().use { os ->
            inArchive.extractSlow(entryBaseFile.archiveEntryIndex) { data ->
                os.write(data)
                data.size
            }
            content = os.toString(Charsets.UTF_8)
        }

        return CachedBaseFile(content)
    }

    /**
     * Check whether the file path in the MOD is correct
     *
     * @param fileEntryName MOD file path
     */
    fun hasPathConflict(fileEntryName: String): Boolean {
        if (!isLoaded) {
            return false
        }
        val fileName = getEntryFileName(fileEntryName)
        val pathFileTree = indexedBaseModFileMap[fileName] ?: return false
        // Sometimes files not belonging to the mod are added to the pak. If found empty, it's not a file supported by the original mod.
        val correctPath = pathFileTree.archiveEntryName
        return !correctPath.equals(fileEntryName, ignoreCase = true)
    }

    /**
     * Get the suggested corrected path
     *
     * @param filePath Relative path of the file to check
     * @return If a file with the same name exists, return the correct path in the base MOD; otherwise return null
     */
    fun getSuggestedPath(filePath: String): String? {
        if (!isLoaded) {
            return null
        }
        val fileName = getEntryFileName(filePath)
        return indexedBaseModFileMap[fileName]?.archiveEntryName
    }

    /**
     * Get the parsed syntax tree node from the base MOD, with cache mechanism
     *
     * @param fileEntryName Full path of the file in the archive
     * @param parseFunction Function used to parse the syntax tree
     * @return Parse result, or null if the file does not exist
     */
    @Suppress("UNCHECKED_CAST")
    fun <T : BaseTreeNode> parseForm(
        fileEntryName: String,
        parseFunction: (String) -> ParsedResult<T>
    ): ParsedResult<T>? {
        val entryFileName = getEntryFileName(fileEntryName)
        val canonicalEntryName = indexedBaseModFileMap[entryFileName]?.archiveEntryName ?: fileEntryName

        val cached = astTreeCache.get(canonicalEntryName)
        if (cached != null) {
            return cached as ParsedResult<T>
        }
        val content = extractFileContent(canonicalEntryName) ?: return null
        val result = parseFunction(content)
        astTreeCache.put(canonicalEntryName, result)
        return result
    }

    /**
     * Clear memory cache and close ZipFile connections
     * It is recommended to call this method to release resources after merging is complete
     */
    override fun close() {
        // Close ZipFile connections
        try {
            zipFileConnections.forEach { (_, zipFile) -> zipFile.close() }
        } catch (e: IOException) {
            ConsoleColorPrinter.warning("Failed to close ZipFile connection: " + e.message)
        }
        fileContentCache.clear()
        astTreeCache.clear()
    }
}
