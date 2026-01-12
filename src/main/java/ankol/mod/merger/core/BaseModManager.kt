package ankol.mod.merger.core

import ankol.mod.merger.core.filetrees.PathFileTree
import ankol.mod.merger.tools.ColorPrinter
import ankol.mod.merger.tools.Localizations
import ankol.mod.merger.tools.Tools
import ankol.mod.merger.tools.Tools.getEntryFileName
import ankol.mod.merger.tools.Tools.indexPakFile
import ankol.mod.merger.tools.Tools.tempDir
import org.apache.commons.compress.archivers.zip.ZipFile
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.security.DigestInputStream
import java.security.MessageDigest
import java.util.*
import java.util.function.Function
import kotlin.io.path.Path
import kotlin.io.path.createDirectories

/**
 * 基准MOD分析器 - 负责加载和分析基准MOD（原版文件）
 *
 * @param baseModPath 基准MOD文件路径
 * @author Ankol
 */
class BaseModManager(
    private val baseModPath: Path
) {
    /**
     * 文件名 → 标准路径的映射
     * 键：文件名（小写）
     * 值：在基准MOD中的相对路径
     */
    var indexedBaseModFileMap: MutableMap<String, PathFileTree>

    /**
     * 基准MOD是否已加载
     */
    var loaded = false

    /**
     * 临时文件缓存目录
     */
    private val cacheDir: Path

    /**
     * 已提取文件的缓存映射：相对路径 → 临时文件路径
     */
    private val extractedFileCache: MutableMap<String, Path>
    private val baseTreeCache = HashMap<String, ParsedResult<*>?>()

    /**
     * 复用的 ZipFile 连接，避免频繁打开关闭
     */
    private lateinit var zipFileConnection: ZipFile

    //初始化逻辑
    init {
        this.indexedBaseModFileMap = LinkedHashMap()
        this.extractedFileCache = LinkedHashMap()
        // 创建缓存目录：temp/BaseModCache_时间戳
        this.cacheDir = Path(tempDir, "BaseModCache_" + System.currentTimeMillis())
        try {
            Files.createDirectories(this.cacheDir)
        } catch (e: IOException) {
            ColorPrinter.warning("Failed to create base mod cache directory: " + e.message)
        }
    }

    /**
     * 加载基准MOD
     */
    fun load() {
        if (loaded) {
            ColorPrinter.warning(Localizations.t("BASE_MOD_ALREADY_LOADED"))
            return
        }
        if (!Files.exists(baseModPath)) {
            throw IOException(Localizations.t("BASE_MOD_FILE_NOT_FOUND", baseModPath))
        }

        try {
            val startTime = System.currentTimeMillis()
            this.indexedBaseModFileMap = indexPakFile(baseModPath) //这里构建的索引MAP里还没有真正解压出来文件
            zipFileConnection = ZipFile.builder().setPath(baseModPath).get() //建立ZipFile连接，使用完成后关闭

            loaded = true
            val timetake = System.currentTimeMillis() - startTime
            ColorPrinter.success(Localizations.t("BASE_MOD_INDEXED_FILES", indexedBaseModFileMap.size, baseModPath.fileName, timetake))
        } catch (e: Exception) {
            throw RuntimeException(e)
        }
    }

    /**
     * 从基准MOD中提取指定文件的内容（带缓存优化）
     * 
     * @param relPath 文件在基准MOD中的相对路径
     * @return 文件内容，如果文件不存在返回null
     */
    fun extractFileContent(relPath: String): String? {
        if (!loaded) {
            return null
        }

        // 规范化路径（统一使用小写文件名查找）
        val fileName = getEntryFileName(relPath).lowercase(Locale.getDefault())
        val pathFileTree = indexedBaseModFileMap[fileName] ?: return null

        val fullPathName = pathFileTree.fullPathName
        if (fullPathName != null) {
            return Files.readString(fullPathName, Charsets.UTF_8)
        }

        //没有初始化内容，从压缩包里提取出来
        val fileEntryName = pathFileTree.fileEntryName
        val (filePath, fileHash) = extractFileFromPak(fileEntryName) //todo 这里可以考虑改为读取的内容就保存到内存里，不要每次都去硬盘读了
        pathFileTree.fullPathName = filePath
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
        if (!loaded) {
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
        if (!loaded) {
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
        return baseTreeCache.computeIfAbsent(fileEntryName) {
            val content = extractFileContent(fileEntryName) ?: return@computeIfAbsent null
            function.apply(content)
        } as ParsedResult<T>?
    }

    /**
     * 清理临时文件缓存并关闭 ZipFile 连接
     * 建议在合并完成后调用此方法释放资源
     */
    fun close() {
        // 关闭 ZipFile 连接
        try {
            zipFileConnection.close()
        } catch (e: IOException) {
            ColorPrinter.warning("Failed to close ZipFile connection: " + e.message)
        }

        // 清理临时文件缓存
        Tools.deleteRecursively(cacheDir)

        // 清空缓存
        extractedFileCache.clear()
        baseTreeCache.clear()
    }

    /**
     * 从PAK文件中提取指定文件的内容
     * @return 包含了文件路径和hash的Pair
     */
    private fun extractFileFromPak(fileEntryName: String): Pair<Path, String> {
        val zipFile = zipFileConnection
        val digest = MessageDigest.getInstance("SHA-256")
        val entry = zipFile.getEntry(fileEntryName)
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
