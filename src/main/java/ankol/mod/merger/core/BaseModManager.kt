package ankol.mod.merger.core

import ankol.mod.merger.core.filetrees.PathFileTree
import ankol.mod.merger.tools.ColorPrinter
import ankol.mod.merger.tools.Localizations
import ankol.mod.merger.tools.Tools
import ankol.mod.merger.tools.Tools.getEntryFileName
import ankol.mod.merger.tools.logger
import org.apache.commons.compress.archivers.zip.ZipFile
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.security.DigestInputStream
import java.security.MessageDigest
import java.util.concurrent.ConcurrentHashMap
import java.util.function.Function
import kotlin.io.path.name

/**
 * 基准MOD管理器
 * 负责基准MOD相关操作
 *
 * @author Ankol
 */
class BaseModManager(
    private val tempDir: Path,
    private val baseModPath: Path
) {
    private val log = logger()

    /**
     * 文件名 → 标准路径的映射
     * 键：文件名（小写）
     * 值：在基准MOD中的相对路径
     */
    var indexedBaseModFileMap: MutableMap<String, PathFileTree> = mutableMapOf()

    /**
     * 基准MOD是否已加载
     */
    var isLoaded = false

    private val baseTreeCache = ConcurrentHashMap<String, ParsedResult<*>?>()

    /**
     * 文件内容缓存（key 为 pak 内 entry 全路径）
     */
    private val baseFileContentCache = ConcurrentHashMap<String, CachedBaseFile>()

    private data class CachedBaseFile(
        val content: String,
        val hash: String,
        val isEmpty: Boolean
    )

    /**
     * 复用的 ZipFile 连接，避免频繁打开关闭
     */
    private lateinit var zipFileConnection: ZipFile

    //初始化逻辑
    init {
        load()
    }

    /**
     * 加载基准MOD
     */
    fun load() {
        if (isLoaded) {
            ColorPrinter.warning(Localizations.t("BASE_MOD_ALREADY_LOADED"))
            return
        }
        try {
            val startTime = System.currentTimeMillis()
            zipFileConnection = openZipConnection()
            this.indexedBaseModFileMap = indexBasePack(zipFileConnection)
            isLoaded = true
            val timetake = System.currentTimeMillis() - startTime
            ColorPrinter.success(Localizations.t("BASE_MOD_INDEXED_FILES", baseModPath.fileName, indexedBaseModFileMap.size, timetake))
        } catch (e: Exception) {
            log.error("Load base mod $baseModPath failed. Reason: ${e.message}", e)
            if (this::zipFileConnection.isInitialized) {
                zipFileConnection.close()
            }
        }
    }

    /**
     * 校验基准MOD路径并建立Zip连接
     */
    private fun openZipConnection(): ZipFile {
        if (!Files.exists(baseModPath)) {
            throw IOException(Localizations.t("BASE_MOD_FILE_NOT_FOUND", baseModPath))
        }
        if (Files.isDirectory(baseModPath)) {
            throw IOException(Localizations.t("TOOLS_PATH_IS_DIRECTORY", baseModPath))
        }
        if (!baseModPath.name.endsWith(".pak", ignoreCase = true)) {
            throw IOException(Localizations.t("TOOLS_FILE_MUST_BE_PAK"))
        }
        if (!Files.isReadable(baseModPath)) {
            throw IOException("Base MOD file is not readable: $baseModPath")
        }
        return ZipFile.builder().setPath(baseModPath).get()
    }

    /**
     * 索引基准data0.pak，为里面的文件建立一个映射表
     */
    private fun indexBasePack(zipFile: ZipFile): MutableMap<String, PathFileTree> {
        val pakIndexMap = HashMap<String, PathFileTree>()
        val entries = zipFile.entries
        while (entries.hasMoreElements()) {
            val zipEntry = entries.nextElement()
            val entryName = zipEntry.name //这里获取的文件在zip里的完整路径 eg: scripts/inventory/fury_config.scr
            val fileName = getEntryFileName(entryName)
            if (fileName in pakIndexMap) {
                ColorPrinter.warning(
                    Localizations.t(
                        "TOOLS_SAME_FILE_NAME_WARNING",
                        fileName,
                        entryName,
                        pakIndexMap[fileName]?.fileEntryName
                    )
                )
            }
            pakIndexMap[fileName] = PathFileTree(fileName, entryName, mutableListOf(baseModPath.fileName.toString()))
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
        val pathFileTree = indexedBaseModFileMap[fileName] ?: return null

        val fileEntryName = pathFileTree.fileEntryName
        val cached = baseFileContentCache[fileEntryName]
        if (cached != null) {
            return if (cached.isEmpty) null else cached.content
        }

        val extracted = extractFileFromPak(fileEntryName)
        pathFileTree.fileHash = extracted.hash
        baseFileContentCache[fileEntryName] = extracted
        if (extracted.isEmpty) {
            return null
        }
        return extracted.content
    }

    /**
     * 从PAK文件中提取指定文件并在内存中缓存内容与哈希
     */
    private fun extractFileFromPak(fileEntryName: String): CachedBaseFile {
        val digest = MessageDigest.getInstance("SHA-256")
        val entry = zipFileConnection.getEntry(fileEntryName) ?: throw IOException(Localizations.t("BASE_MOD_FILE_NOT_FOUND", fileEntryName))
        if (entry.size == 0L) {
            return CachedBaseFile("", "", true)
        }

        val content = zipFileConnection.getInputStream(entry).use { zin ->
            DigestInputStream(zin, digest).use { din ->
                val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                val output = ByteArrayOutputStream()
                while (true) {
                    val read = din.read(buffer)
                    if (read == -1) {
                        break
                    }
                    output.write(buffer, 0, read)
                }
                output.toString(Charsets.UTF_8)
            }
        }
        val fileHash = Tools.bytesToHex(digest.digest())
        return CachedBaseFile(content, fileHash, false)
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
        val correctPath = pathFileTree.fileEntryName
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
        return indexedBaseModFileMap[fileName]?.fileEntryName
    }

    /**
     * 从基准MOD获得解析后的语法树节点，带缓存机制
     *
     * @param fileEntryName 文件在压缩包中的全路径
     * @param function      解析语法树使用的函数
     * @return 解析结果，如果文件不存在返回null
     */
    @Suppress("UNCHECKED_CAST")
    fun <T : BaseTreeNode> parseForm(
        fileEntryName: String,
        function: Function<String, ParsedResult<T>>
    ): ParsedResult<T>? {
        val normalizedFileName = getEntryFileName(fileEntryName)
        val canonicalEntryName = indexedBaseModFileMap[normalizedFileName]?.fileEntryName ?: fileEntryName
        return baseTreeCache.computeIfAbsent(canonicalEntryName) {
            val content = extractFileContent(canonicalEntryName) ?: return@computeIfAbsent null
            function.apply(content)
        } as ParsedResult<T>?
    }

    /**
     * 清理内存缓存并关闭 ZipFile 连接
     * 建议在合并完成后调用此方法释放资源
     */
    @Synchronized
    fun close() {
        // 关闭 ZipFile 连接
        try {
            if (this::zipFileConnection.isInitialized) {
                zipFileConnection.close()
            }
        } catch (e: IOException) {
            ColorPrinter.warning("Failed to close ZipFile connection: " + e.message)
        }

        baseFileContentCache.clear()
        baseTreeCache.clear()
    }
}
