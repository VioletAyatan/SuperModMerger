package ankol.mod.merger.tools

import ankol.mod.merger.exception.BusinessException
import ankol.mod.merger.tools.Localizations.t
import java.nio.channels.FileChannel
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import kotlin.io.path.*

object Tools {
    val userDir: String = System.getProperty("user.dir")

    val tempDir: String = System.getProperty("java.io.tmpdir")

    private val strFormatRegex = Regex("\\{}")

    private val HEX_ARRAY = "0123456789abcdef".toCharArray()

    /**
     * Get the directory containing the mods to be merged
     * This tool is configured by default to be under the mods directory
     *
     * @param meringModDir mod merging directory path, can be used to override the default merge directory
     * @return Path to the directory containing mods to be merged
     */
    fun getMergingModDir(meringModDir: Path? = null): Path {
        return if (meringModDir == null) {
            val defaultPath = Path(userDir, "mods")
            if (defaultPath.exists()) {
                defaultPath
            } else {
                throw BusinessException(t("TOOLS_DEFAULT_MODS_DIR_NOT_EXIST"))
            }
        } else {
            if (meringModDir.exists()) {
                meringModDir
            } else {
                throw BusinessException(t("TOOLS_MODS_DIR_NOT_EXIST", meringModDir))
            }
        }
    }

    /**
     * Scan all files in the specified directory, filter by extension
     *
     * @param mergedDirPath        Directory path
     * @param extensions Extensions to search for (e.g. ".pak", ".zip")
     * @return List of matching files
     */
    fun scanFiles(mergedDirPath: Path, vararg extensions: String): MutableList<Path> {
        val results = ArrayList<Path>()
        if (!mergedDirPath.exists()) {
            throw BusinessException(t("TOOLS_DEFAULT_MODS_DIR_NOT_EXIST"))
        }
        mergedDirPath.walk(PathWalkOption.FOLLOW_LINKS)
            .filter { it.isRegularFile() }
            .forEach { file: Path ->
                val filename = file.fileName.toString()
                for (ext in extensions) {
                    if (filename.endsWith(ext)) {
                        results.add(file)
                    }
                }
            }
        return results
    }

    /**
     * Get the file name in zipEntry, remove '/', return the lowercase file name
     * @return entry file name
     */
    fun getEntryFileName(entryName: String): String {
        return entryName.substring(entryName.lastIndexOf("/") + 1).lowercase()
    }

    /**
     * Recursively delete the specified path and all files and directories under it
     * @param path Path to delete
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
     * Copy file using zero-copy method for efficiency
     * @param sourcePath Source file path
     * @param targetPath Target file path
     * @param createParentDirs Whether to automatically create parent directories, default is true
     */
    fun zeroCopy(sourcePath: Path, targetPath: Path, createParentDirs: Boolean = true) {
        if (createParentDirs) {
            targetPath.parent?.createDirectories()
        }
        FileChannel.open(sourcePath).use { sourceChannel ->
            FileChannel.open(targetPath, StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING).use { targetChannel ->
                var position = 0L
                val size = sourceChannel.size()
                while (position < size) {
                    // transferTo may not transfer all data at once, need to loop
                    position += sourceChannel.transferTo(position, size - position, targetChannel)
                }
            }
        }
    }

    /**
     * Format string, replace {} placeholders with parameter values
     * @param template Template string, e.g. "Hello {} World {}"
     * @param args Parameter list
     * @return Formatted string
     */
    @JvmStatic
    fun format(template: String, vararg args: Any?): String {
        if (args.isEmpty()) return template
        var index = 0
        return template.replace(strFormatRegex) {
            if (index < args.size) args[index++].toString() else "{}"
        }
    }

    /**
     * Convert byte array to hexadecimal string
     *
     * @param bytes Byte array
     * @return Hexadecimal string
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
