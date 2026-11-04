package ankol.mod.merger.core.files

/**
 * 内容保存在内存中的压缩包文件条目，用于原版文件内容和中间合并结果
 * 
 * @author Ankol
 */
class InMemoryArchiveFile(
    fileName: String,
    fileEntryName: String,
    archiveFileName: MutableList<String>,
    private val contentStr: String
) : AbstractArchiveFile(fileName, fileEntryName, archiveFileName) {

    override fun getContent(): String {
        return contentStr
    }
}
