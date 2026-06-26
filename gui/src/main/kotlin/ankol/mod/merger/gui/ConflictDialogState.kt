package ankol.mod.merger.gui

import ankol.mod.merger.constants.UserChoice
import ankol.mod.merger.mergers.ConflictRecord
import ankol.mod.merger.mergers.DeletionRecord
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import java.util.concurrent.CompletableFuture

/**
 * 冲突解决对话框的共享状态
 * 桥接后台合并线程（阻塞等待）和 Compose UI线程（响应式展示）
 *
 * 所有 UI 读取的属性都使用 [mutableStateOf]，后台线程修改时会触发 Compose 重组。
 */
object ConflictDialogState {

    // ===== 内容冲突（Compose 可观察） =====

    /** 对话框是否可见 */
    var isConflictDialogVisible by mutableStateOf(false)

    /** 当前正在展示的冲突 */
    var currentConflict: ConflictRecord? by mutableStateOf(null)

    /** 当前冲突序号 */
    var conflictNumber: Int by mutableStateOf(0)

    /** 内容冲突总数 */
    var totalConflicts: Int by mutableStateOf(0)

    // ===== 删除冲突（Compose 可观察） =====

    /** 删除冲突对话框是否可见 */
    var isDeletionDialogVisible by mutableStateOf(false)

    /** 当前正在展示的删除冲突 */
    var currentDeletion: DeletionRecord? by mutableStateOf(null)

    /** 当前删除冲突序号 */
    var deletionNumber: Int by mutableStateOf(0)

    /** 删除冲突总数 */
    var totalDeletions: Int by mutableStateOf(0)

    // ===== 后台线程内部状态（不需要 Compose 可观察） =====

    /** 待解决的内容冲突列表 */
    var pendingConflicts: List<ConflictRecord> = emptyList()

    /** 当前处理的冲突索引 */
    var conflictIndex: Int = 0

    /** 待解决的删除冲突列表 */
    var pendingDeletions: List<DeletionRecord> = emptyList()

    /** 当前处理的删除冲突索引 */
    var deletionIndex: Int = 0

    /** 是否应使用"全部使用左/右"模式 */
    var applyAll: UserChoice? = null

    private var conflictFuture: CompletableFuture<UserChoice>? = null
    private var deletionFuture: CompletableFuture<UserChoice>? = null

    // ===== 内容冲突方法 =====

    /**
     * 在后台线程中调用：显示下一个冲突并等待用户选择
     */
    fun waitForNextConflictChoice(): UserChoice? {
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

        return try {
            future.get()
        } catch (e: Exception) {
            null
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

    /** 关闭内容冲突对话框 */
    fun closeConflictDialog() {
        isConflictDialogVisible = false
        conflictFuture?.complete(null)
        conflictFuture = null
    }

    // ===== 删除冲突方法 =====

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
