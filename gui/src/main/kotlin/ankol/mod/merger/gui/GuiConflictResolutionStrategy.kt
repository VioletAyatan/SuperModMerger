package ankol.mod.merger.gui

import ankol.mod.merger.api.ConflictResolutionStrategy
import ankol.mod.merger.api.MergeProgressCallback
import ankol.mod.merger.mergers.ConflictRecord
import ankol.mod.merger.mergers.DeletionRecord

/**
 * GUI 冲突解决策略
 * 通过 [ConflictDialogState] 与 Compose UI 通信，替代控制台 readln()
 */
class GuiConflictResolutionStrategy(
    private val onLog: (MergeProgressCallback.Level, String) -> Unit = { _, _ -> }
) : ConflictResolutionStrategy {

    override fun resolveConflict(conflicts: MutableList<ConflictRecord>) {
        if (conflicts.isEmpty()) return

        // 重置状态
        ConflictDialogState.applyAll = null
        ConflictDialogState.pendingConflicts = conflicts.toList()
        ConflictDialogState.conflictIndex = 0
        ConflictDialogState.isConflictDialogVisible = true

        onLog(MergeProgressCallback.Level.INFO, "检测到 ${conflicts.size} 处内容冲突")

        // 逐一处理每个冲突
        for (i in conflicts.indices) {
            val choice = ConflictDialogState.waitForNextConflictChoice()
            if (choice == null) break // 对话框被关闭

            when (choice) {
                // "全部选X" 模式会设置 applyAll，后续冲突自动处理
                else -> {
                    conflicts[i].userChoice = ConflictDialogState.currentConflict?.userChoice
                }
            }
        }

        // 处理完所有冲突后关闭对话框
        ConflictDialogState.isConflictDialogVisible = false
    }

    override fun resolveDeletionConflicts(deletions: MutableList<DeletionRecord>) {
        if (deletions.isEmpty()) return

        ConflictDialogState.pendingDeletions = deletions.toList()
        ConflictDialogState.deletionIndex = 0
        ConflictDialogState.isDeletionDialogVisible = true

        onLog(MergeProgressCallback.Level.INFO, "检测到 ${deletions.size} 处删除冲突")

        for (i in deletions.indices) {
            val choice = ConflictDialogState.waitForNextDeletionChoice()
            if (choice == null) break

            deletions[i].userChoice = ConflictDialogState.currentDeletion?.userChoice
        }

        ConflictDialogState.isDeletionDialogVisible = false
    }
}
