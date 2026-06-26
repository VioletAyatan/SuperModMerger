package ankol.mod.merger.gui

import ankol.mod.merger.constants.UserChoice
import ankol.mod.merger.mergers.ConflictRecord
import ankol.mod.merger.mergers.DeletionRecord
import java.util.concurrent.CompletableFuture

/**
 * 冲突解决对话框的共享状态
 * 桥接后台合并线程（阻塞等待）和 Compose UI线程（响应式展示）
 */
object ConflictDialogState {

    // ===== 内容冲突 =====

    /** 待解决的内容冲突列表 */
    var pendingConflicts: List<ConflictRecord> = emptyList()

    /** 当前处理的冲突索引 */
    var conflictIndex: Int = 0

    /** 对话框是否可见 */
    var isConflictDialogVisible: Boolean = false

    /** 当前正在展示的冲突（供 UI 读取） */
    var currentConflict: ConflictRecord? = null
        private set

    /** 内容冲突总数 */
    var totalConflicts: Int = 0

    /** 当前冲突序号 */
    var conflictNumber: Int = 0

    /** 是否应使用"全部使用左/右"模式 */
    var applyAll: UserChoice? = null

    private var conflictFuture: CompletableFuture<UserChoice>? = null

    // ===== 删除冲突 =====

    var pendingDeletions: List<DeletionRecord> = emptyList()
    var deletionIndex: Int = 0
    var isDeletionDialogVisible: Boolean = false
    var currentDeletion: DeletionRecord? = null
        private set
    var totalDeletions: Int = 0
    var deletionNumber: Int = 0

    private var deletionFuture: CompletableFuture<UserChoice>? = null

    // ===== 内容冲突方法 =====

    /**
     * 在后台线程中调用：显示下一个冲突并等待用户选择
     * @return 用户的 [UserChoice] 选择
     */
    fun waitForNextConflictChoice(): UserChoice? {
        // 如果已有 applyAll，直接返回对应选择
        applyAll?.let {
            return when (it) {
                UserChoice.USE_ALL_BASE -> UserChoice.BASE_MOD
                UserChoice.USE_ALL_MERGE -> UserChoice.MERGE_MOD
                else -> it
            }
        }

        currentConflict = pendingConflicts.getOrNull(conflictIndex)
        val conflict = currentConflict ?: return null

        conflictNumber = conflictIndex + 1
        totalConflicts = pendingConflicts.size
        isConflictDialogVisible = true

        val future = CompletableFuture<UserChoice>()
        conflictFuture = future

        // 阻塞等待用户选择
        return try {
            future.get()
        } catch (e: Exception) {
            null
        } finally {
            // 不要在 finally 中关闭，因为后续还可以继续
        }
    }

    /**
     * 在 Compose UI 中调用：用户做出了选择
     */
    fun resolveConflictChoice(choice: UserChoice) {
        when (choice) {
            UserChoice.USE_ALL_BASE, UserChoice.USE_ALL_MERGE -> {
                applyAll = choice
            }
            else -> {}
        }
        currentConflict?.userChoice = when (choice) {
            UserChoice.USE_ALL_BASE -> UserChoice.BASE_MOD
            UserChoice.USE_ALL_MERGE -> UserChoice.MERGE_MOD
            else -> choice
        }
        conflictFuture?.complete(choice)
        conflictFuture = null
        conflictIndex++

        if (conflictIndex >= pendingConflicts.size) {
            isConflictDialogVisible = false
        }
    }

    /** 关闭内容冲突对话框（无更多冲突） */
    fun closeConflictDialog() {
        isConflictDialogVisible = false
        conflictFuture?.complete(null)
        conflictFuture = null
    }

    // ===== 删除冲突方法 =====

    /**
     * 在后台线程中调用：显示下一个删除冲突并等待用户选择
     */
    fun waitForNextDeletionChoice(): UserChoice? {
        applyAll?.let {
            return when (it) {
                UserChoice.USE_ALL_BASE -> UserChoice.BASE_MOD
                UserChoice.USE_ALL_MERGE -> UserChoice.MERGE_MOD
                else -> it
            }
        }

        currentDeletion = pendingDeletions.getOrNull(deletionIndex)
        val deletion = currentDeletion ?: return null

        deletionNumber = deletionIndex + 1
        totalDeletions = pendingDeletions.size
        isDeletionDialogVisible = true

        val future = CompletableFuture<UserChoice>()
        deletionFuture = future

        return try {
            future.get()
        } catch (e: Exception) {
            null
        }
    }

    /**
     * 在 Compose UI 中调用：用户做出了删除选择
     */
    fun resolveDeletionChoice(choice: UserChoice) {
        when (choice) {
            UserChoice.USE_ALL_BASE, UserChoice.USE_ALL_MERGE -> {
                applyAll = choice
            }
            else -> {}
        }
        currentDeletion?.userChoice = when (choice) {
            UserChoice.USE_ALL_BASE -> UserChoice.BASE_MOD
            UserChoice.USE_ALL_MERGE -> UserChoice.MERGE_MOD
            else -> choice
        }
        deletionFuture?.complete(choice)
        deletionFuture = null
        deletionIndex++

        if (deletionIndex >= pendingDeletions.size) {
            isDeletionDialogVisible = false
        }
    }

    /** 关闭删除冲突对话框 */
    fun closeDeletionDialog() {
        isDeletionDialogVisible = false
        deletionFuture?.complete(null)
        deletionFuture = null
    }

    /** 完全重置状态 */
    fun reset() {
        pendingConflicts = emptyList()
        conflictIndex = 0
        isConflictDialogVisible = false
        currentConflict = null
        totalConflicts = 0
        conflictNumber = 0
        applyAll = null
        conflictFuture?.complete(null)
        conflictFuture = null

        pendingDeletions = emptyList()
        deletionIndex = 0
        isDeletionDialogVisible = false
        currentDeletion = null
        totalDeletions = 0
        deletionNumber = 0
        deletionFuture?.complete(null)
        deletionFuture = null
    }
}
