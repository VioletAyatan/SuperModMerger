package ankol.mod.merger.merger

import ankol.mod.merger.constants.UserChoice
import ankol.mod.merger.core.BaseTreeNode

/**
 * 冲突记录
 * 
 * @author Ankol
 */
data class ConflictRecord(
    /**
     * 冲突的文件名
     */
    val fileName: String,
    /**
     * 基础模组名称
     */
    val baseModName: String,
    /**
     * 合并模组名称
     */
    val mergeModName: String,
    /**
     * 冲突的签名
     */
    val signature: String,
    var baseNode: BaseTreeNode,
    var modNode: BaseTreeNode,

    /**
     * 用户选择
     */
    var userChoice: UserChoice? = null,
)