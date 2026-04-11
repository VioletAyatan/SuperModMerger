package ankol.mod.merger.merger

import ankol.mod.merger.domain.MergerContext
import ankol.mod.merger.merger.json.TechlandJsonFileMerger
import ankol.mod.merger.merger.scr.TechlandScrFileMerger
import ankol.mod.merger.merger.xml.TechlandXmlFileMerger
import java.util.*

/**
 * 合并器获取工厂，通过判断文件扩展名来获取对应支持的合并器
 *
 * @author Ankol
 */
object MergerFactory {
    /**
     * 合并器实例缓存
     */
    private val mergerMap: MutableMap<String, (MergerContext) -> AbstractFileMerger> = HashMap()

    init {
        //.scr格式的合并器
        registerMerger(
            ::TechlandScrFileMerger,
            ".scr", ".def", ".loot", ".phx", ".ppfx", ".ares", ".mpcloth", ".gpufx", ".chs", ".scd"
        )
        //.xml文件的合并器
        registerMerger(::TechlandXmlFileMerger, ".xml")
        //json格式合并器(.model也是json格式，但由于是模型文件，不提供支持合并)
        registerMerger(::TechlandJsonFileMerger, ".gui", ".json")
    }

    /**
     * 注册一个合并器，并关联一个或多个文件扩展名。
     *
     * @param mergerFactory 合并器创建函数。
     * @param extensions 要关联的文件扩展名（例如 .scr, .xml）。
     */
    private fun registerMerger(mergerFactory: (MergerContext) -> AbstractFileMerger, vararg extensions: String) {
        for (ext in extensions) {
            mergerMap[ext.lowercase(Locale.getDefault())] = mergerFactory
        }
    }

    /**
     * 根据文件名获取对应的合并器。
     *
     * @param fileName 文件名（包含扩展名）
     * @return 一个包含合并器实例的 [Optional]；如果找不到合适的合并器，则为空。
     */
    fun getMerger(fileName: String, context: MergerContext): AbstractFileMerger? {
        val extension = "." + fileName.substringAfterLast(".")
        val mergerFactory = mergerMap[extension.lowercase(Locale.getDefault())]
        return mergerFactory?.invoke(context)
    }
}
