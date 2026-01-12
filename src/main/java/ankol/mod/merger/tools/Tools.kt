package ankol.mod.merger.tools

import ankol.mod.merger.core.filetrees.PathFileTree
import ankol.mod.merger.exception.BusinessException
import org.apache.commons.compress.archivers.zip.ZipFile
import java.io.IOException
import java.nio.channels.FileChannel
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import kotlin.io.path.*

object Tools {
    @JvmStatic
    val userDir: String = System.getProperty("user.dir")

    @JvmStatic
    val tempDir: String = System.getProperty("java.io.tmpdir")

    private val strFormatRegex = Regex("\\{}")

    private val HEX_ARRAY = "0123456789abcdef".toCharArray()

    /**
     * 获取待合并的MOD所在目录
     * 这个工具默认配置的是在mods目录下
     * 
     * @param meringModDir mod合并目录地址，可用于修改默认合并目录
     * @return 待合并的MOD目录路径
     */
    fun getMergingModDir(meringModDir: Path? = null): Path {
        return if (meringModDir == null) {
            val defaultPath = Path(userDir, "mods")
            if (defaultPath.exists()) {
                defaultPath
            } else {
                throw BusinessException(Localizations.t("TOOLS_MODS_DIR_NOT_EXIST", defaultPath))
            }
        } else {
            if (meringModDir.exists()) {
                meringModDir
            } else {
                throw BusinessException(Localizations.t("TOOLS_MODS_DIR_NOT_EXIST", meringModDir))
            }
        }
    }

    /**
     * 扫描指定目录中的所有文件，按扩展名过滤
     * 
     * @param mergedDirPath        目录路径
     * @param extensions 要查找的扩展名（如 ".pak", ".zip"）
     * @return 匹配的文件列表
     * @throws IOException 如果目录不存在或无法访问
     */
    fun scanFiles(mergedDirPath: Path, vararg extensions: String): MutableList<Path> {
        val results = ArrayList<Path>()
        if (!mergedDirPath.exists()) {
            throw BusinessException(Localizations.t("TOOLS_MODS_DIR_NOT_EXIST"))
        }
        mergedDirPath.walk().forEach { file: Path ->
            if (file.isRegularFile()) {
                val filename = file.fileName.toString()
                for (ext in extensions) {
                    if (filename.endsWith(ext)) {
                        results.add(file)
                    }
                }
            }
        }
        return results
    }

    /**
     * 索引基准MOD，建立一个索引MAP，方便后续进行文件路径修正和对比使用
     * @param filePath 基准MOD路径
     */
    @JvmStatic
    fun indexPakFile(filePath: Path): MutableMap<String, PathFileTree> {
        if (filePath.notExists()) {
            throw BusinessException(Localizations.t("TOOLS_FILE_NOT_EXIST", filePath.absolutePathString()))
        }
        if (filePath.isDirectory()) {
            throw BusinessException(Localizations.t("TOOLS_PATH_IS_DIRECTORY", filePath.absolutePathString()))
        }
        if (!filePath.fileName.toString().endsWith(".pak")) {
            throw BusinessException(Localizations.t("TOOLS_FILE_MUST_BE_PAK"))
        }
        val pakIndexMap = HashMap<String, PathFileTree>()
        try {
            ZipFile.builder().setPath(filePath).get().use { zipFile ->
                val entries = zipFile.entries
                while (entries.hasMoreElements()) {
                    val zipEntry = entries.nextElement()
                    val entryName = zipEntry.name
                    val fileName = getEntryFileName(entryName)
                    //重复文件的识别
                    if (fileName in pakIndexMap) {
                        ColorPrinter.warning(
                            Localizations.t(
                                "TOOLS_SAME_FILE_NAME_WARNING",
                                fileName,
                                entryName,
                                pakIndexMap[fileName]?.fullPathName
                            )
                        )
                    }
                    pakIndexMap[fileName] = PathFileTree(fileName, entryName, mutableListOf(filePath.fileName.toString()))
                }
            }
        } catch (e: IOException) {
            throw RuntimeException(e)
        }
        return pakIndexMap
    }

    @JvmStatic
    fun getEntryFileName(entryName: String): String {
        return entryName.substring(entryName.lastIndexOf("/") + 1)
    }

    /**
     * 递归删除指定路径及其下的所有文件和目录
     * @param path 要删除的路径
     */
    @JvmStatic
    fun deleteRecursively(path: Path) {
        if (path.notExists()) {
            return
        }
        if (path.isDirectory()) {
            path.listDirectoryEntries().forEach { child ->
                deleteRecursively(child)
            }
        }
        path.deleteIfExists()
    }

    /**
     * 拷贝文件，使用零拷贝方式提高效率
     * @param sourcePath 源文件路径
     * @param targetPath 目标文件路径
     * @param createParentDirs 是否自动创建父目录，默认为 true
     */
    fun zeroCopy(sourcePath: Path, targetPath: Path, createParentDirs: Boolean = true) {
        if (createParentDirs) {
            targetPath.parent?.createDirectories()
        }
        FileChannel.open(sourcePath).use { sourceChannel ->
            FileChannel.open(targetPath, StandardOpenOption.CREATE, StandardOpenOption.WRITE).use { targetChannel ->
                var position = 0L
                val size = sourceChannel.size()
                while (position < size) {
                    // transferTo 可能无法一次传输所有数据，需要循环处理
                    position += sourceChannel.transferTo(position, size - position, targetChannel)
                }
            }
        }
    }

    /**
     * 格式化字符串，将 {} 占位符替换为参数值
     * @param template 模板字符串，如 "Hello {} World {}"
     * @param args 参数列表
     * @return 格式化后的字符串
     */
    @JvmStatic
    fun format(template: String, vararg args: Any?): String {
        if (args.isEmpty()) return template
        var index = 0
        return template.replace(strFormatRegex) {
            if (index < args.size) args[index++]?.toString() ?: "null" else "{}"
        }
    }

    /**
     * 将字节数组转换为十六进制字符串
     *
     * @param bytes 字节数组
     * @return 十六进制字符串
     */
    fun bytesToHex(bytes: ByteArray): String {
        val hexChars = CharArray(bytes.size * 2)
        for (i in bytes.indices) {
            val v = bytes[i].toInt() and 0xFF
            hexChars[i * 2] = HEX_ARRAY[v ushr 4]
            hexChars[i * 2 + 1] = HEX_ARRAY[v and 0x0F]
        }
        return String(hexChars)
    }
}
