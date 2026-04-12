package ankol.mod.merger.tools

import java.lang.ref.SoftReference

/**
 * 结合 LRU 淘汰策略与 SoftReference 的线程安全缓存。
 *
 * - 当条目数超过 [maxSize] 时，最久未访问的条目会被淘汰。
 * - 即使在容量内，JVM 在内存压力时也可通过 GC 回收 SoftReference 指向的值。
 *
 * @param maxSize 缓存最大条目数
 * @author Ankol
 */
class SoftLruCache<K, V>(private val maxSize: Int) {

    private val map = object : LinkedHashMap<K, SoftReference<V>>(maxSize, 0.75f, true) {

        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<K, SoftReference<V>>?): Boolean {
            return size > maxSize
        }
    }

    /**
     * 获取缓存值。若 key 不存在或对应的 SoftReference 已被 GC 回收，返回 null 并清理该条目。
     */
    @Synchronized
    fun get(key: K): V? {
        val ref = map[key] ?: return null
        val value = ref.get()
        if (value == null) {
            map.remove(key)
        }
        return value
    }

    /**
     * 存入缓存值，用 SoftReference 包装。
     */
    @Synchronized
    fun put(key: K, value: V) {
        map[key] = SoftReference(value)
    }

    /**
     * 清空全部缓存。
     */
    @Synchronized
    fun clear() {
        map.clear()
    }
}

