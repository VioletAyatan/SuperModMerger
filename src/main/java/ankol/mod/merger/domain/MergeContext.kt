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
    /** Current merging file name **/
    var mergingFileName: String = "",
    var accumulatedModName: String = "",
    var mergeModName: String = "",
    /** Whether this is the first merge **/
    var isFirstMerge: Boolean = false
) {
    /** Merging history **/
    val mergedHistory: MergedHistory = MergedHistory()

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
