package ankol.mod.merger.constants

/**
 * User's conflict resolution choice
 */
enum class UserChoice {
    /**
     * Use the modification from the base mod
     */
    BASE_MOD,

    /**
     * Use the modification from the merged mod
     */
    MERGE_MOD,

    /**
     * Use all modifications from the base mod
     */
    USE_ALL_BASE,

    /**
     * Use all modifications from the merged mod
     */
    USE_ALL_MERGE,
    ;

    companion object {
        fun findByOrder(order: Int?): UserChoice? {
            if (order == null) return null
            for (choice in entries) {
                // The ordinal in enum starts from 0, but the user prompt order starts from 1, so add 1
                if (choice.ordinal + 1 == order) {
                    return choice
                }
            }
            return null
        }
    }
}
