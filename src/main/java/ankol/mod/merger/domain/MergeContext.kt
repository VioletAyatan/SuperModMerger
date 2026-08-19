package ankol.mod.merger.domain

import ankol.mod.merger.api.ConflictResolver
import ankol.mod.merger.core.BaseModManager
import ankol.mod.merger.core.CliConflictResolver
import ankol.mod.merger.tools.logger
import java.util.concurrent.ConcurrentHashMap

/**
 * Mod Merging Context
 * @author Ankol
 */
class MergeContext(
    /** Base mod manager **/
    val baseModManager: BaseModManager,
    /** Conflict resolution strategy (switchable for GUI) **/
    var conflictResolver: ConflictResolver = CliConflictResolver,
    /**
     * 当前处理的文件名称（不带路径）
     */
    var currentFileName: String = "",
    var accumulatedModName: String = "",
    /**
     * 当前进行合并的mod名称
     */
    var mergeModName: String = "",
    /**
     * 是否是第一次合并
     */
    var isFirstMerge: Boolean = false
) {
    /** Merging history **/
    val mergedHistory: MergedHistory = MergedHistory()

    companion object {
        private val log = logger()
    }

    fun configure(mergingFileName: String, accumulatedModName: String, incomingModName: String, isFirstMerge: Boolean) {
        this.currentFileName = mergingFileName
        this.accumulatedModName = accumulatedModName
        this.mergeModName = incomingModName
        this.isFirstMerge = isFirstMerge
    }

    class MergedHistory {
        private val map: MutableMap<String, String> = ConcurrentHashMap()

        /**
         * Mark this signature
         */
        fun markSignture(signature: String, modName: String) {
            if (map.putIfAbsent(signature, modName) != null) {
                log.debug("Warning, duplicate signature detected in merge history: ${signature}, skipping.")
            }
        }

        /**
         * Get the real mod name for this conflict source from the recorded signatures
         */
        fun getModNameFromSignature(signature: String): String? {
            return map[signature]
        }
    }
}
