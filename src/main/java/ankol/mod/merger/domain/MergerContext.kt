package ankol.mod.merger.domain

import ankol.mod.merger.core.BaseModManager
import ankol.mod.merger.tools.logger
import java.util.concurrent.ConcurrentHashMap

class MergerContext(
    /** 基准模组管理器 **/
    val baseModManager: BaseModManager,
    /** 当前合并文件名 **/
    var mergingFileName: String = "",
    var accumulatedModName: String = "",
    var mergeModName: String = "",
    /** 合并历史 **/
    var mergedHistory: MergedHistory = MergedHistory(),
    /** 是否是第一次合并 **/
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
         * 标记这个签名
         */
        fun markSignture(signature: String, modName: String) {
            if (map.putIfAbsent(signature, modName) != null) {
                log.debug("警告，合并历史中检测到重复的签名插入：${signature}，跳过处理.")
            }
        }

        /**
         * 从记录的签名中获取这个冲突来源的真正MOD名字
         */
        fun getModNameFromSignature(signature: String): String? {
            return map[signature]
        }
    }
}
