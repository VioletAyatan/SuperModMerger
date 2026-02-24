package ankol.mod.merger.core

class MergerContext {
    /**
     * 当前正在合并的文件名
     */
    lateinit var mergingFileName: String

    /**
     * 基准MOD名称
     */
    lateinit var baseModName: String

    /**
     * 待合并MOD名称
     */
    lateinit var mergeModName: String

    /**
     * 基准MOD管理器
     */
    lateinit var baseModManager: BaseModManager

    /**
     * 合并历史
     */
    var mergedHistory: MergedHistory = MergedHistory()

    /**
     * 是否是第一个MOD与data0.pak的合并
     * 当为true时，第一个MOD相对于data0.pak的修改应该被自动接受，不提示冲突
     */
    var isFirstModMergeWithBaseMod = false

    class MergedHistory {
        private val map: MutableMap<String, String> = mutableMapOf()

        /**
         * 标记这个签名
         */
        fun markSignture(signature: String, modName: String) {
            if (!map.containsKey(signature)) {
                map[signature] = modName
            } /*else {
                System.err.println("错误，检测到重复的签名插入：${signature}，跳过处理.")
            }*/
        }

        /**
         * 从记录的签名中获取这个冲突来源的真正MOD名字
         */
        fun getModNameFromSignature(signature: String): String? {
            return map[signature]
        }
    }
}
