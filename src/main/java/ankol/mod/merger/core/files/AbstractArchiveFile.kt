package ankol.mod.merger.core.files

/**
 * 表示一个压缩包中的文件条目，记录文件名、在压缩包中的路径和来源压缩包链
 * @param fileName 文件名（不带路径）
 * @param fileEntryName  文件名，在压缩包中的相对路径
 * @param archiveFileNames 文件来自哪个mod包（如果是嵌套压缩包的话，使用 mod.zip -> mod.pak 这样的名字显示）
 * @author Ankol
 */
abstract class AbstractArchiveFile(
    var fileName: String,
    var fileEntryName: String,
    var archiveFileNames: MutableList<String> = mutableListOf()
) {
    /**
     * 获取文件内容
     */
    abstract fun getContent(): String

    fun getFullArchiveFileName(): String {
        return archiveFileNames.joinToString(" -> ")
    }

    /**
     * 获取最外层的压缩包文件名，通常也是MOD名称
     *
     * @return 最外层压缩包文件名
     */
    fun getFirstArchiveFileName(): String {
        return if (archiveFileNames.isNotEmpty()) {
            archiveFileNames[0]
        } else {
            ""
        }
    }
}
