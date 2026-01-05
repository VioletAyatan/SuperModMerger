package ankol.mod.merger.tools;

import cn.hutool.cache.impl.AbstractCache;

public class DebugTool {
    @SuppressWarnings("rawtypes")
    public static void printCacheUseRate(String cacheName, AbstractCache abstractCache) {
        System.out.println(cacheName + " 缓存命中率" + abstractCache.getHitCount());
        System.out.println(cacheName + " 缓存丢失率" + abstractCache.getMissCount());
    }
}
