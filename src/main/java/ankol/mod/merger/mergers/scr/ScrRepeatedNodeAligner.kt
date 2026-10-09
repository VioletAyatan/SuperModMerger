package ankol.mod.merger.mergers.scr

import ankol.mod.merger.core.BaseTreeNode
import ankol.mod.merger.mergers.scr.node.ScrContainerNode
import ankol.mod.merger.mergers.scr.node.ScrFunCallNode
import ankol.mod.merger.constants.ScrNodeType
import java.util.*

/**
 * 重复函数调用对齐器。
 *
 * 同一个块里重复出现的同名函数调用，Visitor 生成的签名带有位置编号（如 `funCall:Dlc:0`、`funCall:Dlc:1`），
 * 中间删除一个调用后，后续调用的编号整体前移，按签名对比会把「删除中间那个」误判成「修改后面若干个 + 删除最后一个」。
 *
 * 本类对这类同名调用改为按内容对齐：
 * 1. 先按参数完全一致做 LCS，得到不变的锚点；
 * 2. 锚点之间的空隙里，参数个数 > 1 的调用按第一个参数配对（与签名规则一致），参数个数 <= 1 的调用按顺序配对，
 *    配对上的视为「修改」；
 * 3. 剩下的累积侧调用视为删除候选，剩下的新 MOD 侧调用视为新增。
 *
 * 对齐结果以「视图」形式返回：三份子节点映射使用同一套对齐键，其余节点保持原签名作为键，顺序与原容器一致。
 */
object ScrRepeatedNodeAligner {
    /**
     * @param vanilla 原版容器子节点视图（无原版时为 null）
     * @param accumulated 累积结果容器子节点视图
     * @param incoming 新 MOD 容器子节点视图
     * @param alignedFunctions 参与内容对齐的函数名
     */
    data class AlignedChildren(
        val vanilla: Map<String, BaseTreeNode>?,
        val accumulated: Map<String, BaseTreeNode>,
        val incoming: Map<String, BaseTreeNode>,
        val alignedFunctions: Set<String>,
    )

    private val ScrContainerNode?.funCalls: List<ScrFunCallNode>
        get() = this?.childrens?.values?.filterIsInstance<ScrFunCallNode>()
            ?.filter { it.nodeType == ScrNodeType.FUN_CALL }
            ?: emptyList()

    fun align(
        vanilla: ScrContainerNode?,
        accumulated: ScrContainerNode,
        incoming: ScrContainerNode,
    ): AlignedChildren {
        val vanillaCalls = vanilla.funCalls.groupBy { it.functionName }
        val accumulatedCalls = accumulated.funCalls.groupBy { it.functionName }
        val incomingCalls = incoming.funCalls.groupBy { it.functionName }

        // 任意一侧出现重复的函数名才需要按内容对齐
        val repeatedNames = linkedSetOf<String>()
        for (calls in listOf(vanillaCalls, accumulatedCalls, incomingCalls)) {
            calls.forEach { (name, list) -> if (list.size > 1) repeatedNames += name }
        }
        if (repeatedNames.isEmpty()) {
            return AlignedChildren(vanilla?.childrens, accumulated.childrens, incoming.childrens, emptySet())
        }

        val keyOverrides = IdentityHashMap<BaseTreeNode, String>()
        for (name in repeatedNames) {
            val accList = accumulatedCalls[name].orEmpty()
            val incList = incomingCalls[name].orEmpty()
            val vanList = vanillaCalls[name].orEmpty()
            accList.forEachIndexed { index, node -> keyOverrides[node] = alignedKey(name, "$index") }

            val incToAcc = pair(accList, incList)
            incList.forEachIndexed { index, node ->
                val accIndex = incToAcc[index]
                keyOverrides[node] = if (accIndex >= 0) alignedKey(name, "$accIndex") else alignedKey(name, "+$index")
            }

            val vanToAcc = pair(accList, vanList)
            vanList.forEachIndexed { index, node ->
                val accIndex = vanToAcc[index]
                keyOverrides[node] = if (accIndex >= 0) alignedKey(name, "$accIndex") else alignedKey(name, "v$index")
            }
        }

        fun view(container: ScrContainerNode): Map<String, BaseTreeNode> {
            val result = LinkedHashMap<String, BaseTreeNode>(container.childrens.size)
            for ((signature, node) in container.childrens) {
                result[keyOverrides[node] ?: signature] = node
            }
            return result
        }

        return AlignedChildren(vanilla?.let(::view), view(accumulated), view(incoming), repeatedNames)
    }

    private fun alignedKey(functionName: String, id: String) = "${TechlandScrFileVisitor.FUN_CALL}:$functionName#$id"

    /**
     * 将 [other] 中的每个调用配对到 [base] 中的调用。
     *
     * @return 下标为 other 的序号，值为配对到的 base 序号，未配对为 -1
     */
    internal fun pair(base: List<ScrFunCallNode>, other: List<ScrFunCallNode>): IntArray {
        val result = IntArray(other.size) { -1 }
        if (base.isEmpty() || other.isEmpty()) return result

        // 1. LCS：参数完全一致的作为锚点
        val n = base.size
        val m = other.size
        val dp = Array(n + 1) { IntArray(m + 1) }
        for (i in n - 1 downTo 0) {
            for (j in m - 1 downTo 0) {
                dp[i][j] = if (base[i].arguments == other[j].arguments) {
                    dp[i + 1][j + 1] + 1
                } else {
                    maxOf(dp[i + 1][j], dp[i][j + 1])
                }
            }
        }
        val anchors = mutableListOf<Pair<Int, Int>>()
        var i = 0
        var j = 0
        while (i < n && j < m) {
            when {
                base[i].arguments == other[j].arguments && dp[i][j] == dp[i + 1][j + 1] + 1 -> {
                    anchors += i to j
                    i++
                    j++
                }

                dp[i + 1][j] >= dp[i][j + 1] -> i++
                else -> j++
            }
        }
        anchors.forEach { (bi, oj) -> result[oj] = bi }

        // 2. 锚点之间的空隙内配对
        var baseStart = 0
        var otherStart = 0
        for ((bi, oj) in anchors + (n to m)) {
            pairGap(base, baseStart until bi, other, otherStart until oj, result)
            baseStart = bi + 1
            otherStart = oj + 1
        }
        return result
    }

    private fun pairGap(
        base: List<ScrFunCallNode>,
        baseRange: IntRange,
        other: List<ScrFunCallNode>,
        otherRange: IntRange,
        result: IntArray,
    ) {
        if (baseRange.isEmpty() || otherRange.isEmpty()) return
        val usedBase = HashSet<Int>()

        // 参数个数 > 1：按第一个参数配对（与 Visitor 的签名规则保持一致）
        for (oj in otherRange) {
            val args = other[oj].arguments
            if (args.size <= 1) continue
            val match = baseRange.firstOrNull { bi ->
                bi !in usedBase && base[bi].arguments.size > 1 && base[bi].arguments.first() == args.first()
            } ?: continue
            usedBase += match
            result[oj] = match
        }

        // 参数个数 <= 1：按顺序配对
        val restBase = baseRange.filter { it !in usedBase && base[it].arguments.size <= 1 }.iterator()
        for (oj in otherRange) {
            if (result[oj] >= 0 || other[oj].arguments.size > 1) continue
            if (!restBase.hasNext()) break
            result[oj] = restBase.next()
        }
    }
}
