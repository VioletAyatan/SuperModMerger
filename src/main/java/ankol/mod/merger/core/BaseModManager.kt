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
 * 基准MOD管理器
 * 负责基准MOD相关操作
 *
 * @author Ankol
 */
class BaseModManager(private val basePakDirPath: Path) : AutoCloseable {
    private val log = logger()

    /**
     * 文件名 → 标准路径的映射
     * 键：文件名（小写）
     * 值：在基准MOD中的相对路径
     */
    var indexedBaseModFileMap: MutableMap<String, BaseFile> = mutableMapOf()

    /**
     * 基准MOD是否已加载
     */
    var isLoaded = false

    private val basePakSet = setOf("data0.pak", "data1.pak", "databt.mpak")

    /** AST树缓存 **/
    private val astTreeCache = SoftLruCache<String, ParsedResult<*>>(2048)

    /** 文件内容缓存 **/
    private val fileContentCache = SoftLruCache<String, CachedBaseFile>(2048)

    private data class CachedBaseFile(
        val content: String,
        val isEmpty: Boolean
    )

    /**
     * Reuse zip-connection to avoid repeatedly opening and closing the same file, which is costly.
     * Keep it open until the merged is compleate, then close it in close() method.
     */
    private lateinit var zipFileConnections: Map<String, ZipFile>

    //初始化逻辑
    init {
        load()
    }

    /**
     * 加载基准MOD
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
     * 校验基准MOD路径并建立Zip连接
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
     * 索引游戏基准PAK，为里面的文件建立一个映射表，方便后续操作
     */
    private fun indexBasePack(zipConnections: Map<String, ZipFile>): MutableMap<String, BaseFile> {
        val pakIndexMap = HashMap<String, BaseFile>()
        zipConnections.forEach { (pakName, zipFile) ->
            zipFile.entries.asSequence().forEach { archiveEntry: ZipArchiveEntry ->
                if (!archiveEntry.isDirectory) {
                    val archiveEntryName = archiveEntry.name
                    val fileName = getEntryFileName(archiveEntryName)
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
     * 从基准MOD中提取指定文件的内容（带缓存优化）
     *
     * @param entryFilePath 文件在基准MOD中的相对路径
     * @return 文件内容，如果文件不存在返回null
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
     * 从PAK文件中提取指定文件并在内存中缓存内容与哈希
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
     * 判断MOD里的文件路径是否正确
     *
     * @param filePath mod文件路径
     */
    fun hasPathConflict(filePath: String): Boolean {
        if (!isLoaded) {
            return false
        }
        val fileName = getEntryFileName(filePath)
        val pathFileTree = indexedBaseModFileMap[fileName] ?: return false
        //有时会有一些不属于mod的文件被加入到pak中，这里查到空后说明不是原版mod支持修改的文件.
        val correctPath = pathFileTree.archiveEntryName
        return !correctPath.equals(filePath, ignoreCase = true)
    }

    /**
     * 获取建议的修正路径
     *
     * @param filePath 待检查的文件相对路径
     * @return 如果存在同名文件，返回基准MOD中的正确路径；否则返回null
     */
    fun getSuggestedPath(filePath: String): String? {
        if (!isLoaded) {
            return null
        }
        val fileName = getEntryFileName(filePath)
        return indexedBaseModFileMap[fileName]?.archiveEntryName
    }

    /**
     * 从基准MOD获得解析后的语法树节点，带缓存机制
     *
     * @param fileEntryName 文件在压缩包中的全路径
     * @param parseFunction      解析语法树使用的函数
     * @return 解析结果，如果文件不存在返回null
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
     * 清理内存缓存并关闭 ZipFile 连接
     * 建议在合并完成后调用此方法释放资源
     */
    override fun close() {
        // 关闭 ZipFile 连接
        try {
            zipFileConnections.forEach { (_, zipFile) -> zipFile.close() }
        } catch (e: IOException) {
            ColorPrinter.warning("Failed to close ZipFile connection: " + e.message)
        }
        fileContentCache.clear()
        astTreeCache.clear()
    }
}
