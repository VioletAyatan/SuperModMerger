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
     * Get the file name in archiveEntry, remove 'separator', return the lowercase file name
     * @param entryName Archive full-path file name
     * @return entry file name
     */
    fun getEntryFileName(entryName: String): String {
        return entryName.substringAfterLast('/').lowercase()
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
            FileChannel.open(
                targetPath,
                StandardOpenOption.CREATE,
                StandardOpenOption.WRITE,
                StandardOpenOption.TRUNCATE_EXISTING
            ).use { targetChannel ->
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

}
