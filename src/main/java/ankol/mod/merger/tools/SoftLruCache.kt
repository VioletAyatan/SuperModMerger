package ankol.mod.merger.tools

import java.lang.ref.SoftReference

/**
 * Thread-safe cache combining LRU eviction strategy and SoftReference.
 *
 * - When the number of entries exceeds [maxSize], the least recently used entry will be evicted.
 * - Even within the capacity, JVM may reclaim the value pointed by SoftReference under memory pressure via GC.
 *
 * @param maxSize Maximum number of cache entries
 * @author Ankol
 */
class SoftLruCache<K, V>(private val maxSize: Int) {

    private val map = object : LinkedHashMap<K, SoftReference<V>>(maxSize, 0.75f, true) {

        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<K, SoftReference<V>>?): Boolean {
            return size > maxSize
        }
    }

    /**
     * Get the cached value. If the key does not exist or the corresponding SoftReference has been GC collected, return null and clean up the entry.
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
     * Put a value into the cache, wrapped with SoftReference.
     */
    @Synchronized
    fun put(key: K, value: V) {
        map[key] = SoftReference(value)
    }

    /**
     * Clear all cache entries.
     */
    @Synchronized
    fun clear() {
        map.clear()
    }
}
