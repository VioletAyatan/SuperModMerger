package ankol.mod.merger.api

import ankol.mod.merger.mergers.ConflictRecord
import ankol.mod.merger.mergers.DeletionRecord

/**
 * 冲突解决策略接口
 * CLI 和 GUI 各自实现此接口以提供不同的交互方式
 */
interface ConflictResolutionStrategy {
    /**
     * 解决内容冲突（两个 Mod 对同一处代码有不同的修改）
     */
    fun resolveConflict(conflicts: MutableList<ConflictRecord>)

    /**
     * 解决删除冲突（incoming mod 中缺失但 accumulated 中存在的节点）
     */
    fun resolveDeletionConflicts(deletions: MutableList<DeletionRecord>)
}
