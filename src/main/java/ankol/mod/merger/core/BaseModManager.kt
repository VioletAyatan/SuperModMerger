package ankol.mod.merger.core

import ankol.mod.merger.core.filetrees.BaseFile
import ankol.mod.merger.domain.ParsedResult
import ankol.mod.merger.exception.ExitProcessException
import ankol.mod.merger.tools.ColorPrinter
import ankol.mod.merger.tools.Localizations.t
import ankol.mod.merger.tools.SoftLruCache
import ankol.mod.merger.tools.Tools.getEntryFileName
import ankol.mod.merger.tools.logger
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry
import org.apache.commons.compress.archivers.zip.ZipFile
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.lang.AutoCloseable
import java.nio.file.Path
import java.util.function.Function
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
        val isEmpty: Boolean
    )

    /**
     * Reuse zip-connection to avoid repeatedly opening and closing the same file, which is costly.
     * Keep it open until the merge is complete, then close it in the close() method.
     */
    private lateinit var zipFileConnections: Map<String, ZipFile>

    init {
        load()
    }

    /**
     * Load the base MOD
     */
    fun load() {
        if (isLoaded) {
            ColorPrinter.warning(t("BASE_MOD_ALREADY_LOADED"))
            return
        }
        val startTime = System.currentTimeMillis()
        this.zipFileConnections = openZipConnection()
        this.indexedBaseModFileMap = indexBasePack(zipFileConnections)
        this.isLoaded = true
        val timetake = System.currentTimeMillis() - startTime
        ColorPrinter.success(t("BASE_MOD_INDEXED_FILES", basePakDirPath.fileName, indexedBaseModFileMap.size, timetake))
    }

    /**
     * Validate the base MOD path and establish Zip connections
     */
    private fun openZipConnection(): MutableMap<String, ZipFile> {
        if (!basePakDirPath.exists()) {
            throw IOException(t("BASE_MOD_FILE_NOT_FOUND", basePakDirPath))
        }
        val connectionMap = mutableMapOf<String, ZipFile>()
        for (pakName in basePakSet) {
            try {
                val resolvePath = basePakDirPath.resolve(pakName)
                if (resolvePath.exists()) {
                    val zipFile = ZipFile.builder().setPath(resolvePath).get()
                    connectionMap[pakName] = zipFile
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
    private fun indexBasePack(zipConnections: Map<String, ZipFile>): MutableMap<String, BaseFile> {
        val pakIndexMap = HashMap<String, BaseFile>()
        zipConnections.forEach { (pakName, zipFile) ->
            zipFile.entries.asSequence().forEach { archiveEntry: ZipArchiveEntry ->
                if (!archiveEntry.isDirectory) {
                    val archiveEntryName = archiveEntry.name
                    val fileName = getEntryFileName(archiveEntryName, "/")
                    if (fileName in pakIndexMap) {
                        ColorPrinter.warning(
                            t(
                                "TOOLS_SAME_FILE_NAME_WARNING",
                                fileName,
                                archiveEntryName,
                                pakIndexMap[fileName]?.archiveEntryName
                            )
                        )
                    }
                    pakIndexMap[fileName] = BaseFile(fileName, archiveEntryName, pakName)
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
    @Synchronized
    fun extractFileContent(entryFilePath: String): String? {
        if (!isLoaded) {
            return null
        }
        val fileName = getEntryFileName(entryFilePath)
        val baseFile = indexedBaseModFileMap[fileName] ?: return null

        val fileEntryName = baseFile.archiveEntryName
        val cached = fileContentCache.get(fileEntryName)
        if (cached != null) {
            return if (cached.isEmpty) null else cached.content
        }

        val extracted = extractFileFromPak(baseFile.basePakName, fileEntryName)
        fileContentCache.put(fileEntryName, extracted)
        if (extracted.isEmpty) {
            return null
        }
        return extracted.content
    }

    /**
     * Extract the specified file from the PAK file and cache the content and hash in memory
     */
    private fun extractFileFromPak(basePakName: String, archiveEntryName: String): CachedBaseFile {
        val zipFile = zipFileConnections[basePakName] ?: throw IOException(t("BASE_MOD_FILE_NOT_FOUND", basePakName))
        val entry = zipFile.getEntry(archiveEntryName)
        if (entry.size == 0L) {
            return CachedBaseFile("", true)
        }

        val content = zipFile.getInputStream(entry).use { zin ->
            val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
            val output = ByteArrayOutputStream()
            while (true) {
                val read = zin.read(buffer)
                if (read == -1) {
                    break
                }
                output.write(buffer, 0, read)
            }
            output.toString(Charsets.UTF_8)
        }
        return CachedBaseFile(content, false)
    }

    /**
     * Check whether the file path in the MOD is correct
     *
     * @param filePath MOD file path
     */
    fun hasPathConflict(filePath: String): Boolean {
        if (!isLoaded) {
            return false
        }
        val fileName = getEntryFileName(filePath)
        val pathFileTree = indexedBaseModFileMap[fileName] ?: return false
        // Sometimes files not belonging to the mod are added to the pak. If found empty, it's not a file supported by the original mod.
        val correctPath = pathFileTree.archiveEntryName
        return !correctPath.equals(filePath, ignoreCase = true)
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
        parseFunction: Function<String, ParsedResult<T>>
    ): ParsedResult<T>? {
        val entryFileName = getEntryFileName(fileEntryName)
        val canonicalEntryName = indexedBaseModFileMap[entryFileName]?.archiveEntryName ?: fileEntryName

        val cached = astTreeCache.get(canonicalEntryName)
        if (cached != null) {
            return cached as ParsedResult<T>
        }
        val content = extractFileContent(canonicalEntryName) ?: return null
        val result = parseFunction.apply(content)
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
            ColorPrinter.warning("Failed to close ZipFile connection: " + e.message)
        }
        fileContentCache.clear()
        astTreeCache.clear()
    }
}
