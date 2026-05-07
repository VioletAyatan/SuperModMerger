package ankol.mod.merger.core.filetrees

data class BaseFile(
    val fileName: String,
    val archiveEntryName: String,
    /**
     * ArchiveEntry在压缩包中的索引，后续需要这个索引来寻找对应的文件.
     * Sevenzip对于压缩包中对于所有entry统一以索引方式记录
     */
    val archiveEntryIndex: Int,
    /**
     * 当前索引文件来源的ModPak包名字
     */
    val basePakName: String,
)
