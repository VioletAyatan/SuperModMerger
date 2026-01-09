package ankol.mod.merger.core.filetrees

/**
 * 内存文件树，用于存储内存中的文件内容
 * 
 * @author Ankol
 */
class MemoryFileTree(
    fileName: String,
    fileEntryName: String,
    archiveFileName: MutableList<String>,
    private val contentStr: String
) : AbstractFileTree(fileName, fileEntryName, archiveFileName) {

    override fun getContent(): String {
        return contentStr
    }
}
