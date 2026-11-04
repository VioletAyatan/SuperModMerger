package ankol.mod.merger.core.files

import java.nio.file.Files
import java.nio.file.Path

/**
 * 已解压到磁盘的压缩包文件条目，内容从 [filePath] 读取
 * @param fileHash 文件hash值
 * @author Ankol
 */
class ExtractedArchiveFile(
    fileName: String,
    fileEntryName: String,
    archiveFileName: MutableList<String>,
    var fileHash: String? = null,
    /**
     * 解压出来后的文件路径
     */
    var filePath: Path? = null
) : AbstractArchiveFile(fileName, fileEntryName, archiveFileName) {

    override fun getContent(): String {
        return filePath?.let { Files.readString(it) } ?: throw IllegalArgumentException("Error, fullPathName is null")
    }

    /**
     * 安全获取FullPathName，为空的情况下会抛出异常
     */
    fun safeGetFilePath(): Path {
        return filePath ?: throw IllegalArgumentException("Error, fullPathName is null")
    }
}
