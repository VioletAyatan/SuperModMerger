package ankol.mod.merger.api

import ankol.mod.merger.core.GlobalMergingStrategy

/**
 * 全局合并策略选择接口
 * 让用户选择普通模式还是全局修复模式
 */
fun interface MergingStrategySelector {
    fun askStrategy(): GlobalMergingStrategy
}
