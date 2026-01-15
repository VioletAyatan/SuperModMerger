package ankol.mod.merger.merger

import ankol.mod.merger.constants.UserChoice
import ankol.mod.merger.core.BaseTreeNode

/**
 * 冲突类型
 */
enum class ConflictType {
    /**
     * 内容修改冲突（双方都修改了同一节点）
     */
    MODIFICATION,

    /**
     * 删除冲突（MOD删除/注释了某个节点）
     */
    REMOVAL
}

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
    /**
     * MOD节点，对于删除类型的冲突，此字段为null
     */
    var modNode: BaseTreeNode?,

    /**
     * 用户选择
     */
    var userChoice: UserChoice? = null,

    /**
     * 冲突类型
     */
    val conflictType: ConflictType = ConflictType.MODIFICATION,
)