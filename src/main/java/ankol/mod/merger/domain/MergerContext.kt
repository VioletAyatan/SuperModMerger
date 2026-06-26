package ankol.mod.merger.domain

import ankol.mod.merger.api.ConflictResolutionStrategy
import ankol.mod.merger.core.BaseModManager
import ankol.mod.merger.core.ConflictResolver
import ankol.mod.merger.tools.logger
import java.util.concurrent.ConcurrentHashMap

/**
 * Mod Merging Context
 * @author Ankol
 */
class MergerContext(
    /** Base mod manager **/
    val baseModManager: BaseModManager,
    /** Conflict resolution strategy (switchable for GUI) **/
    var conflictStrategy: ConflictResolutionStrategy = ConflictResolver,
    /** Current merging file name **/
    var mergingFileName: String = "",
    var accumulatedModName: String = "",
    var mergeModName: String = "",
    /** Merging history **/
    var mergedHistory: MergedHistory = MergedHistory(),
    /** Whether this is the first merge **/
    var isFirstMerge: Boolean = false
) {
    companion object {
        private val log = logger()
    }

    fun configure(mergingFileName: String, accumulatedModName: String, incomingModName: String, isFirstMerge: Boolean) {
        this.mergingFileName = mergingFileName
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
