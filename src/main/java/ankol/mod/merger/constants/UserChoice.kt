package ankol.mod.merger.constants

/**
 * 用户选择的冲突解决方案
 */
enum class UserChoice {
    /**
     * 使用基础模组的修改
     */
    BASE_MOD,

    /**
     * 使用合并模组的修改
     */
    MERGE_MOD,

    /**
     * 全部使用基础模组的修改
     */
    USE_ALL_BASE,

    /**
     * 全部使用合并模组的修改
     */
    USE_ALL_MERGE,
    ;

    companion object {
        fun findByOrder(order: Int?): UserChoice? {
            if (order == null) return null
            for (choice in entries) {
                //枚举中的ordinal是从0开始的，但是用户提示选择的顺序从1开始，所以要加1
                if (choice.ordinal + 1 == order) {
                    return choice
                }
            }
            return null
        }
    }
}