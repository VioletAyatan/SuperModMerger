package ankol.mod.merger.mergers.scr

import ankol.mod.merger.core.BaseTreeNode
import ankol.mod.merger.mergers.RepeatedSiblingAligner
import ankol.mod.merger.mergers.SiblingContent
import ankol.mod.merger.mergers.scr.node.AbstractScrNode
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
        val keyOverrides = IdentityHashMap<BaseTreeNode, String>()
        alignFunctionBlocks(vanilla, accumulated, incoming, keyOverrides)
        if (keyOverrides.isEmpty() && repeatedNames.isEmpty()) {
            return AlignedChildren(vanilla?.childrens, accumulated.childrens, incoming.childrens, emptySet())
        }

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

    private val ScrContainerNode?.funBlocks: Map<String, List<ScrContainerNode>>
        get() = this?.childrens?.values?.filterIsInstance<ScrContainerNode>()
            ?.filter { it.nodeType == ScrNodeType.FUN_BLOCK }?.groupBy { it.name }
            ?: emptyMap()

    /**
     * Function blocks whose name is repeated in any version get keys that do not depend on what else each
     * version has in the container: the name and the first argument, as in their signature. Blocks that share the
     * first argument, or have no arguments, were told apart by their position: those are paired by
     * [RepeatedSiblingAligner].
     */
    private fun alignFunctionBlocks(
        vanilla: ScrContainerNode?,
        accumulated: ScrContainerNode,
        incoming: ScrContainerNode,
        keys: MutableMap<BaseTreeNode, String>,
    ) {
        val vanillaBlocks = vanilla?.funBlocks
        val accumulatedBlocks = accumulated.funBlocks
        val incomingBlocks = incoming.funBlocks
        val versions = listOfNotNull(vanillaBlocks, accumulatedBlocks, incomingBlocks)
        val repeatedNames = linkedSetOf<String>()
        for (blocks in versions) {
            blocks.forEach { (name, list) -> if (list.size > 1) repeatedNames += name }
        }

        for (name in repeatedNames) {
            fun families(blocks: Map<String, List<ScrContainerNode>>) =
                blocks[name].orEmpty().groupBy { it.arguments.firstOrNull() }

            val vanillaFamilies = vanillaBlocks?.let(::families)
            val accumulatedFamilies = families(accumulatedBlocks)
            val incomingFamilies = families(incomingBlocks)
            val firstArguments = linkedSetOf<String?>()
            listOfNotNull(vanillaFamilies, accumulatedFamilies, incomingFamilies).forEach { firstArguments += it.keys }
            for (firstArgument in firstArguments) {
                val vanillaFamily = vanillaFamilies?.let { it[firstArgument].orEmpty() }
                val accumulatedFamily = accumulatedFamilies[firstArgument].orEmpty()
                val incomingFamily = incomingFamilies[firstArgument].orEmpty()
                val key = TechlandScrFileVisitor.FUN_BLOCK + ":" + name + (firstArgument?.let { ":$it" } ?: "")
                if (maxOf(vanillaFamily?.size ?: 0, accumulatedFamily.size, incomingFamily.size) <= 1) {
                    // the only one in each version: the same block
                    (vanillaFamily.orEmpty() + accumulatedFamily + incomingFamily).forEach { keys[it] = key }
                    continue
                }
                val ids = RepeatedSiblingAligner.align(
                    vanillaFamily?.map(::content), accumulatedFamily.map(::content), incomingFamily.map(::content)
                )
                // the first one keeps the key it has while it is the only one with its first argument, so that
                // what the merge history says about it is still found when a mod adds another one
                fun keyOf(id: String) = if (id == "0") key else "$key#$id"
                vanillaFamily?.forEachIndexed { index, block -> keys[block] = keyOf(ids.vanilla[index]) }
                accumulatedFamily.forEachIndexed { index, block -> keys[block] = keyOf(ids.accumulated[index]) }
                incomingFamily.forEachIndexed { index, block -> keys[block] = keyOf(ids.incoming[index]) }
            }
        }
    }

    private fun content(block: ScrContainerNode): SiblingContent {
        val parts by lazy(LazyThreadSafetyMode.NONE) {
            val values = ArrayList<String>()
            val shape = ArrayList<String>()
            // the first argument is the same in all the blocks of a family
            block.arguments.forEachIndexed { index, argument -> if (index > 0) values += "#$index=$argument" }
            collect(block, values, shape)
            values to shape
        }
        return SiblingContent(withoutWhitespace(block.sourceText), block.arguments.getOrNull(1), { parts.first }, { parts.second })
    }

    /**
     * Every statement inside a block, however deep: its name is part of the shape of the block, and what it says
     * is one of its values. Calls and block headers count argument by argument, so that a statement whose value
     * changed still has its other arguments to be recognised by.
     */
    private fun collect(container: ScrContainerNode, values: MutableList<String>, shape: MutableList<String>) {
        for (child in container.childrens.values) {
            val name = if (child is AbstractScrNode) "${child.nodeType}:${child.name}" else child.signature
            shape += name
            val arguments = when (child) {
                is ScrContainerNode -> child.arguments
                is ScrFunCallNode -> child.arguments
                else -> null
            }
            if (arguments == null) {
                values += withoutWhitespace(child.sourceText)
            } else {
                arguments.forEachIndexed { index, argument -> values += "$name#$index=$argument" }
            }
            if (child is ScrContainerNode) collect(child, values, shape)
        }
    }

    private fun withoutWhitespace(text: String): String = text.filterNot(Char::isWhitespace)

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
