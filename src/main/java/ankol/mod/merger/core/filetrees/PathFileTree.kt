package ankol.mod.merger.core.filetrees

import java.nio.file.Files
import java.nio.file.Path

/**
 * 路径文件树，当文件是在某文件路径时的表示
 * @param fileHash 文件hash值
 * @author Ankol
 */
class PathFileTree(
    fileName: String,
    fileEntryName: String,
    archiveFileName: MutableList<String>,
    var fileHash: String? = null,
    /**
     * 解压出来后的文件路径
     */
    var fullPathName: Path? = null
) : AbstractFileTree(fileName, fileEntryName, archiveFileName) {

    override fun getContent(): String {
        return fullPathName?.let { Files.readString(it) } ?: throw IllegalArgumentException("Error, fullPathName is null")
    }

    /**
     * 安全获取FullPathName，为空的情况下会抛出异常
     */
    fun safeGetFullPathName(): Path {
        return fullPathName ?: throw IllegalArgumentException("Error, fullPathName is null")
    }
}
