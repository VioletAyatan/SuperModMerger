package ankol.mod.merger.core;

import ankol.mod.merger.merger.scr.ScrScriptFileMerger;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.ReflectUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * 合并器工厂 - 根据文件类型创建和提供合适的合并器
 * <p>
 * 职责：
 * 1. 维护一个文件扩展名到合并器实例的映射。
 * 2. 提供一个静态方法，根据文件名返回一个合适的合并器。
 * 3. 如果找不到合适的合并器，则返回一个空Optional，表示该文件类型不支持智能合并。
 * <p>
 * 设计模式：工厂模式 + 单例模式（通过静态映射实现）
 */
public class MergerFactory {

    // 存储扩展名 -> 合并器实例的映射
    private static final Map<String, Class<? extends IFileMerger>> mergerMap = new HashMap<>();

    // 静态初始化块，用于注册所有支持的合并器
    static {
        // 注册.scr格式的合并器
        registerMerger(ScrScriptFileMerger.class, ".scr", ".def", ".loot", ".ppfx", ".ares", ".mpcloth");
        // 注册.xml文件的合并器
//        registerMerger(XmlFileMerger.class, ".xml");
    }

    /**
     * 注册一个合并器，并关联一个或多个文件扩展名。
     *
     * @param merger     合并器实例。
     * @param extensions 要关联的文件扩展名（例如 ".txt", ".xml"）。
     */
    private static void registerMerger(Class<? extends IFileMerger> merger, String... extensions) {
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
    public static Optional<IFileMerger> getMerger(String fileName, MergerContext context) {
        String extension = "." + FileUtil.extName(fileName);
        Class<? extends IFileMerger> aClass = mergerMap.get(extension.toLowerCase());
        if (aClass == null) {
            return Optional.empty();
        } else {
            return Optional.of(ReflectUtil.newInstance(aClass, context));
        }
    }
}