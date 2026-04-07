package ankol.mod.merger.core

import ankol.mod.merger.core.filetrees.PathFileTree
import ankol.mod.merger.tools.ColorPrinter
import ankol.mod.merger.tools.Localizations
import ankol.mod.merger.tools.Tools
import ankol.mod.merger.tools.Tools.getEntryFileName
import org.apache.commons.compress.archivers.zip.ZipFile
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.security.DigestInputStream
import java.security.MessageDigest
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.function.Function
import kotlin.io.path.createDirectories

/**
 * 基准MOD管理器
 * 负责基准MOD相关操作
 *
 * @author Ankol
 */
class BaseModManager(
    tempDir: Path,
    private val baseModPath: Path
) {
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

    /**
     * 临时文件缓存目录
     */
    private val cacheDir: Path = tempDir.resolve("BaseModCache_" + System.currentTimeMillis())

    private val baseTreeCache = ConcurrentHashMap<String, ParsedResult<*>?>()

    /**
     * 复用的 ZipFile 连接，避免频繁打开关闭
     */
    private lateinit var zipFileConnection: ZipFile

    //初始化逻辑
    init {
        try {
            load()
            cacheDir.createDirectories()
        } catch (e: IOException) {
            ColorPrinter.warning("Failed to create base mod cache directory: " + e.message)
        }
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
            zipFileConnection = openZipConnection() //建立ZipFile连接
            this.indexedBaseModFileMap = indexPakFile(zipFileConnection)
            isLoaded = true
            val timetake = System.currentTimeMillis() - startTime
            ColorPrinter.success(Localizations.t("BASE_MOD_INDEXED_FILES", baseModPath.fileName, indexedBaseModFileMap.size, timetake))
        } catch (e: Exception) {
            zipFileConnection.close()
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
        if (!baseModPath.fileName.toString().endsWith(".pak", ignoreCase = true)) {
            throw IOException(Localizations.t("TOOLS_FILE_MUST_BE_PAK"))
        }
        if (!Files.isReadable(baseModPath)) {
            throw IOException("Base MOD file is not readable: $baseModPath")
        }
        return ZipFile.builder().setPath(baseModPath).get()
    }

    /**
     * 使用已建立的Zip连接构建基准MOD文件索引
     */
    private fun indexPakFile(zipFile: ZipFile): MutableMap<String, PathFileTree> {
        val pakIndexMap = HashMap<String, PathFileTree>()
        val entries = zipFile.entries
        while (entries.hasMoreElements()) {
            val zipEntry = entries.nextElement()
            val entryName = zipEntry.name
            val fileName = getEntryFileName(entryName)
            val normalizedFileName = fileName.lowercase(Locale.getDefault())
            if (normalizedFileName in pakIndexMap) {
                ColorPrinter.warning(
                    Localizations.t(
                        "TOOLS_SAME_FILE_NAME_WARNING",
                        fileName,
                        entryName,
                        pakIndexMap[normalizedFileName]?.fileEntryName
                    )
                )
            }
            pakIndexMap[normalizedFileName] =
                PathFileTree(fileName, entryName, mutableListOf(baseModPath.fileName.toString()))
        }
        return pakIndexMap
    }

    /**
     * 从基准MOD中提取指定文件的内容（带缓存优化）
     *
     * @param relPath 文件在基准MOD中的相对路径
     * @return 文件内容，如果文件不存在返回null
     */
    @Synchronized
    fun extractFileContent(relPath: String): String? {
        if (!isLoaded) {
            return null
        }

        // 规范化路径（统一使用小写文件名查找）
        val fileName = getEntryFileName(relPath).lowercase(Locale.getDefault())
        val pathFileTree = indexedBaseModFileMap[fileName] ?: return null

        val fullPathName = pathFileTree.filePath
        if (fullPathName != null) {
            return Files.readString(fullPathName, Charsets.UTF_8)
        }

        //没有初始化内容，从压缩包里提取出来
        val fileEntryName = pathFileTree.fileEntryName
        val (filePath, fileHash) = extractFileFromPak(fileEntryName) //todo 这里可以考虑改为读取的内容就保存到内存里，不要每次都去硬盘读了
        pathFileTree.filePath = filePath
        pathFileTree.fileHash = fileHash
        //返回的hash值为空，说明文件大小为0
        if (fileHash.isEmpty()) {
            return null
        }
        return Files.readString(filePath)
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
        val fileName = getEntryFileName(filePath).lowercase(Locale.getDefault())
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
        val fileName = getEntryFileName(filePath).lowercase(Locale.getDefault())
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
        return baseTreeCache.computeIfAbsent(fileEntryName) {
            val content = extractFileContent(fileEntryName) ?: return@computeIfAbsent null
            function.apply(content)
        } as ParsedResult<T>?
    }

    /**
     * 清理临时文件缓存并关闭 ZipFile 连接
     * 建议在合并完成后调用此方法释放资源
     */
    @Synchronized
    fun close() {
        // 关闭 ZipFile 连接
        try {
            zipFileConnection?.close()
        } catch (e: IOException) {
            ColorPrinter.warning("Failed to close ZipFile connection: " + e.message)
        }

        // 清理临时文件缓存
        Tools.deleteRecursively(cacheDir)
        baseTreeCache.clear()
    }

    /**
     * 从PAK文件中提取指定文件的内容
     * @return 包含了文件路径和hash的Pair
     */
    private fun extractFileFromPak(fileEntryName: String): Pair<Path, String> {
        val zipFile = requireNotNull(zipFileConnection) { "Base mod archive connection is not available." }
        val digest = MessageDigest.getInstance("SHA-256")
        val entry = zipFile.getEntry(fileEntryName) ?: throw IOException(Localizations.t("BASE_MOD_FILE_NOT_FOUND", fileEntryName))
        val outputPath = cacheDir.resolve(fileEntryName)
        outputPath.parent.createDirectories()
        //大小为0
        if (entry.size == 0L) {
            Files.createFile(outputPath)
            return Pair(outputPath, "")
        } else {
            zipFile.getInputStream(entry).use { zin ->
                DigestInputStream(zin, digest).use { din ->
                    Files.copy(din, outputPath)
                }
            }
        }
        val fileHash = Tools.bytesToHex(digest.digest())
        return Pair(outputPath, fileHash)
    }
}
