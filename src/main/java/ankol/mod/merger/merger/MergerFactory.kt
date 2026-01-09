package ankol.mod.merger.merger

import ankol.mod.merger.core.AbstractFileMerger
import ankol.mod.merger.core.MergerContext
import ankol.mod.merger.merger.scr.TechlandScrFileMerger
import ankol.mod.merger.merger.xml.TechlandXmlFileMerger
import java.util.*
import java.util.concurrent.ConcurrentHashMap

/**
 * 合并器获取工厂，通过判断文件扩展名来获取对应支持的合并器
 * 
 * @author Ankol
 */
object MergerFactory {
    private val mergerMap: MutableMap<String, Class<out AbstractFileMerger>> = HashMap()

    /**
     * 实例化后的实例存储在缓存里，不去反复构造实例
     */
    private val mergerCache: MutableMap<Class<*>, AbstractFileMerger> = ConcurrentHashMap()

    init {
        // 注册.scr格式的合并器
        registerMerger(TechlandScrFileMerger::class.java, ".scr", ".def", ".loot", ".phx", ".ppfx", ".ares", ".mpcloth")
        // 注册.xml文件的合并器
        registerMerger(TechlandXmlFileMerger::class.java, ".xml")
        /*Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            DebugTool.printCacheUseRate("mergerCache", (AbstractCache) mergerCache);
        }));*/
    }

    /**
     * 注册一个合并器，并关联一个或多个文件扩展名。
     * 
     * @param merger     合并器实例。
     * @param extensions 要关联的文件扩展名（例如 ".txt", ".xml"）。
     */
    private fun registerMerger(merger: Class<out AbstractFileMerger>, vararg extensions: String) {
        for (ext in extensions) {
            mergerMap[ext.lowercase(Locale.getDefault())] = merger
        }
    }

    /**
     * 根据文件名获取对应的合并器。
     * 
     * @param fileName 文件名（包含扩展名）
     * @return 一个包含合并器实例的 [Optional]；如果找不到合适的合并器，则为空。
     */
    fun getMerger(fileName: String, context: MergerContext): Optional<AbstractFileMerger> {
        val extension = "." + fileName.substringBeforeLast(".")
        val aClass = mergerMap[extension.lowercase(Locale.getDefault())]
        if (aClass == null) {
            return Optional.empty()
        } else {
            val fileMerger = mergerCache[aClass]
            if (fileMerger == null) {
                synchronized(MergerFactory::class.java) {
                    val fileMerger = aClass.getConstructor(context.javaClass).newInstance(context)
                    mergerCache[aClass] = fileMerger
                }
            } else {
                fileMerger.context = context
            }
            return Optional.of(fileMerger!!)
        }
    }
}