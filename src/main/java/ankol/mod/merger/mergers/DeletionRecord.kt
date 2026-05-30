package ankol.mod.merger.mergers

import ankol.mod.merger.constants.UserChoice
import ankol.mod.merger.core.BaseTreeNode

/**
 * 删除操作记录，用于追踪 incoming mod 中缺失但在 accumulated 中存在的节点。
 *
 * 节点缺失有两种原因：
 *  - mod 作者故意删除（应应用删除）
 *  - mod 基于旧版本制作，不知道该节点的存在（应保留）
 *
 * @param fileName               正在合并的文件名
 * @param deletingModName        执行删除操作的 Mod 名称（即 incoming mod）
 * @param previousModName        最后修改该节点的 Mod 名称，或 "Vanilla"
 * @param signature              节点签名
 * @param accumulatedNode        accumulated 中的节点（待确认是否删除）
 * @param isModifyDeleteConflict 是否为修改-删除冲突（accumulated 已被其他 mod 修改，而 incoming 要删除）
 * @param userChoice             用户的选择
 */
data class DeletionRecord(
    val fileName: String,
    val deletingModName: String,
    val previousModName: String,
    val signature: String,
    val accumulatedNode: BaseTreeNode,
    val isModifyDeleteConflict: Boolean = false,
    var userChoice: UserChoice? = null
)
