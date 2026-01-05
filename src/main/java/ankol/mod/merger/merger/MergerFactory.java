package ankol.mod.merger.merger;

import ankol.mod.merger.core.AbstractFileMerger;
import ankol.mod.merger.core.MergerContext;
import ankol.mod.merger.merger.scr.TechlandScrFileMerger;
import ankol.mod.merger.merger.xml.TechlandXmlFileMerger;
import ankol.mod.merger.tools.DebugTool;
import cn.hutool.cache.Cache;
import cn.hutool.cache.CacheUtil;
import cn.hutool.cache.impl.AbstractCache;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.ReflectUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * 合并器获取工厂，通过判断文件扩展名来获取对应支持的合并器
 *
 * @author Ankol
 */
public class MergerFactory {

    private static final Map<String, Class<? extends AbstractFileMerger>> mergerMap = new HashMap<>();
    private static final Cache<Class<?>, AbstractFileMerger> mergerCache = CacheUtil.newLFUCache(20, 30 * 1000);

    static {
        // 注册.scr格式的合并器
        registerMerger(TechlandScrFileMerger.class, ".scr", ".def", ".loot", ".phx", ".ppfx", ".ares", ".mpcloth");
        // 注册.xml文件的合并器
        registerMerger(TechlandXmlFileMerger.class, ".xml");
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            DebugTool.printCacheUseRate("mergerCache", (AbstractCache) mergerCache);
        }));
    }

    /**
     * 注册一个合并器，并关联一个或多个文件扩展名。
     *
     * @param merger     合并器实例。
     * @param extensions 要关联的文件扩展名（例如 ".txt", ".xml"）。
     */
    private static void registerMerger(Class<? extends AbstractFileMerger> merger, String... extensions) {
        for (String ext : extensions) {
            mergerMap.put(ext.toLowerCase(), merger);
        }
    }

    /**
     * 根据文件名获取对应的合并器。
     *
     * @param fileName 文件名（包含扩展名）。
     * @return 一个包含合并器实例的 {@link Optional}；如果找不到合适的合并器，则为空。
     */
    public static Optional<AbstractFileMerger> getMerger(String fileName, MergerContext context) {
        String extension = "." + FileUtil.extName(fileName);
        Class<? extends AbstractFileMerger> aClass = mergerMap.get(extension.toLowerCase());
        if (aClass == null) {
            return Optional.empty();
        } else {
            return Optional.of(mergerCache.get(aClass, () -> ReflectUtil.newInstance(aClass, context)));
        }
    }
}