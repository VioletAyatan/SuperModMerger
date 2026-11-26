package ankol.mod.merger.mergers

/**
 * What the aligner looks at in a sibling that its signature cannot tell apart from its neighbours, such as
 * function blocks with the same name and first argument or XML elements with the same identifying attribute.
 *
 * @param text the whole sibling without whitespace: siblings with the same text are the same one, unchanged
 * @param name what names the sibling besides its signature, if anything: the second argument of a function block
 * @param values everything inside it, argument by argument or attribute by attribute
 * @param shape the statements or elements inside it whatever their values, and what a merge cannot rewrite
 */
internal class SiblingContent(
    val text: String,
    val name: String?,
    values: () -> List<String>,
    shape: () -> List<String>,
) {
    constructor(text: String, values: List<String>, shape: List<String>) : this(text, null, { values }, { shape })

    // only the siblings that changed are compared, so these are worked out on demand
    private val valueCounts by lazy(LazyThreadSafetyMode.NONE) { values().groupingBy { it }.eachCount() }
    private val shapeCounts by lazy(LazyThreadSafetyMode.NONE) { shape().groupingBy { it }.eachCount() }
    private val valueSize by lazy(LazyThreadSafetyMode.NONE) { valueCounts.values.sum() }
    private val shapeSize by lazy(LazyThreadSafetyMode.NONE) { shapeCounts.values.sum() }

    /** Nothing to compare, e.g. an empty block */
    private val isBare by lazy(LazyThreadSafetyMode.NONE) { valueCounts.isEmpty() && shapeCounts.isEmpty() }

    /** How much two siblings have in common; 0 if nothing */
    fun similarity(other: SiblingContent): Int {
        // a sibling with nothing to compare can only be told apart by its order
        if (isBare || other.isBare) return 1
        return VALUE * common(valueCounts, other.valueCounts) + minOf(VALUE - 1, common(shapeCounts, other.shapeCounts))
    }

    /** How much two siblings differ; 0 if nothing */
    fun differences(other: SiblingContent): Int =
        valueSize + other.valueSize - 2 * common(valueCounts, other.valueCounts) +
            shapeSize + other.shapeSize - 2 * common(shapeCounts, other.shapeCounts)

    /**
     * Whether merging [other] into this one gives [other]: it has everything this one has, whatever the values,
     * so nothing of this one is left behind.
     */
    fun canBecome(other: SiblingContent): Boolean =
        text == other.text || shapeCounts.all { (part, times) -> (other.shapeCounts[part] ?: 0) >= times }

    /**
     * For siblings that vanilla does not have: two mods carry the same one if it is identical, or if it has the
     * same name, one has every statement or element of the other and, of the values of that other one, at most
     * one or a third are different. Their differences are then asked.
     */
    fun isAnotherVersionOf(other: SiblingContent): Boolean {
        if (text == other.text) return true
        if (name != other.name) return false
        val smaller = when {
            canBecome(other) -> this
            other.canBecome(this) -> other
            else -> return false
        }
        val different = smaller.valueSize - common(valueCounts, other.valueCounts)
        return different <= 1 || 3 * different <= smaller.valueSize
    }

    private fun common(first: Map<String, Int>, second: Map<String, Int>): Int {
        val (small, large) = if (first.size <= second.size) first to second else second to first
        var count = 0
        for ((part, times) in small) count += minOf(times, large[part] ?: 0)
        return count
    }

    private companion object {
        const val VALUE = 1_000
    }
}

/**
 * Pairs the siblings that only their position tells apart (see [SiblingContent]).
 *
 * While their number does not change and they can be read as changed in place (see [inPlace]), they are paired by
 * position, as their signatures did. Otherwise a sibling was added, removed or replaced, the position of the ones
 * after it no longer says which is which, and they are paired by their content instead, the way
 * ScrRepeatedNodeAligner does for repeated function calls: the identical ones at both ends first, and the rest
 * keeping their order, so that the pairs have as much in common as possible. Siblings with nothing in common are
 * not paired: one was removed and the other added.
 */
internal object RepeatedSiblingAligner {
    const val UNPAIRED = -1

    /** Above this many pairs, identical siblings are paired first and only the ones between them are compared */
    private const val MAX_COMPARED_PAIRS = 250_000L

    /** Above this, the identical siblings are looked for among the ones that appear only once */
    private const val MAX_ANCHOR_CELLS = 4_000_000L

    /** The id of each sibling of one family in the three versions: siblings with the same id are the same one */
    class FamilyIds(val vanilla: List<String>, val accumulated: List<String>, val incoming: List<String>)

    /**
     * With vanilla, each version is paired with it, so a change in one version is not mistaken for a change in
     * the other. Siblings that vanilla does not have (added by mods) are the same one in both versions only if
     * they are in the same place and one is another version of the other (see [SiblingContent.isAnotherVersionOf]),
     * or if they are identical; otherwise both are kept.
     * Without vanilla (null: there is no vanilla file, or mods added the container) the two versions are paired
     * with each other.
     */
    fun align(
        vanilla: List<SiblingContent>?,
        accumulated: List<SiblingContent>,
        incoming: List<SiblingContent>,
    ): FamilyIds {
        if (vanilla.isNullOrEmpty()) {
            // numbered by the accumulated version
            val partners = if (vanilla == null) pair(accumulated, incoming) else pairAdded(accumulated, incoming)
            return FamilyIds(
                emptyList(),
                accumulated.indices.map { "$it" },
                incoming.indices.map { if (partners[it] == UNPAIRED) "+$it" else "${partners[it]}" },
            )
        }
        val accumulatedPartners = pair(vanilla, accumulated)
        val incomingPartners = pair(vanilla, incoming)
        val accumulatedIds = accumulated.indices.map {
            if (accumulatedPartners[it] == UNPAIRED) "a$it" else "${accumulatedPartners[it]}"
        }
        val incomingIds = incoming.indices.mapTo(ArrayList()) {
            if (incomingPartners[it] == UNPAIRED) "+$it" else "${incomingPartners[it]}"
        }
        // siblings that vanilla does not have: another version of the same one must be in the same place,
        // that is, after the same sibling of vanilla
        val accumulatedPlaces = places(accumulatedPartners)
        val incomingPlaces = places(incomingPartners)
        val addedAccumulated = accumulated.indices.filter { accumulatedPartners[it] == UNPAIRED }
        val addedIncoming = incoming.indices.filter { incomingPartners[it] == UNPAIRED }
        val taken = HashSet<Int>()
        for ((place, incomingGroup) in addedIncoming.groupBy { incomingPlaces[it] }) {
            val accumulatedGroup = addedAccumulated.filter { accumulatedPlaces[it] == place }
            val partners = pairAdded(accumulatedGroup.map { accumulated[it] }, incomingGroup.map { incoming[it] })
            incomingGroup.forEachIndexed { position, index ->
                if (partners[position] != UNPAIRED) {
                    incomingIds[index] = accumulatedIds[accumulatedGroup[partners[position]]]
                    taken += accumulatedGroup[partners[position]]
                }
            }
        }
        // identical ones are the same one wherever they are
        val free = HashMap<String, ArrayDeque<Int>>()
        for (index in addedAccumulated) if (index !in taken) free.getOrPut(accumulated[index].text) { ArrayDeque() }.addLast(index)
        for (index in addedIncoming) {
            if (incomingIds[index].startsWith("+")) {
                free[incoming[index].text]?.removeFirstOrNull()?.let { incomingIds[index] = accumulatedIds[it] }
            }
        }
        return FamilyIds(vanilla.indices.map { "$it" }, accumulatedIds, incomingIds)
    }

    /** For each sibling, the sibling of vanilla that the last paired one before it is paired with; -1 before them all */
    private fun places(partners: IntArray): IntArray {
        var last = UNPAIRED
        return IntArray(partners.size) {
            if (partners[it] != UNPAIRED) last = partners[it]
            last
        }
    }

    /** @return for each sibling of [other], the index of its partner in [base], or [UNPAIRED] */
    fun pair(base: List<SiblingContent>, other: List<SiblingContent>): IntArray {
        if (base.size == other.size && inPlace(base, base.indices.toList(), other, other.indices.toList(), ownNames(base))) {
            return IntArray(other.size) { it }
        }
        return pairByContent(base, other, inPlace = true) { first, second -> first.similarity(second) }
    }

    /** The names that tell a sibling apart: the ones that only one of them has */
    private fun ownNames(siblings: List<SiblingContent>): Lazy<Set<String>> = lazy(LazyThreadSafetyMode.NONE) {
        siblings.mapNotNull { it.name }.groupingBy { it }.eachCount().filterValues { it == 1 }.keys
    }

    /**
     * Whether each sibling of [base] can be read as changed into the one at its position:
     * - it can become it (see [SiblingContent.canBecome]), so the merge leaves nothing of another sibling in it, and
     * - it is not a sibling with a name of its own that is still there, under that name, somewhere else.
     */
    private fun inPlace(
        base: List<SiblingContent>, baseIndices: List<Int>,
        other: List<SiblingContent>, otherIndices: List<Int>,
        ownNames: Lazy<Set<String>>,
    ): Boolean {
        var names: Set<String?>? = null
        for (k in baseIndices.indices) {
            val old = base[baseIndices[k]]
            val new = other[otherIndices[k]]
            if (old.text == new.text) continue
            if (!old.canBecome(new)) return false
            val name = old.name
            if (name != null && name != new.name && name in ownNames.value) {
                if (names == null) names = otherIndices.mapTo(HashSet()) { other[it].name }
                if (name in names) return false
            }
        }
        return true
    }

    /** Pairs the siblings that vanilla does not have: only the ones that are another version of the same one */
    private fun pairAdded(base: List<SiblingContent>, other: List<SiblingContent>): IntArray =
        pairByContent(base, other, inPlace = false) { first, second ->
            if (first.isAnotherVersionOf(second)) first.similarity(second) + 1 else 0
        }

    /** @param inPlace whether the siblings of [base] can be read as changed in place (see [pairStretch]) */
    private fun pairByContent(
        base: List<SiblingContent>,
        other: List<SiblingContent>,
        inPlace: Boolean,
        score: (SiblingContent, SiblingContent) -> Int,
    ): IntArray {
        val result = IntArray(other.size) { UNPAIRED }
        val used = BooleanArray(base.size)
        fun link(baseIndex: Int, otherIndex: Int) {
            result[otherIndex] = baseIndex
            used[baseIndex] = true
        }

        // texts as numbers, to compare them quickly
        val numbers = HashMap<String, Int>()
        val baseText = IntArray(base.size) { numbers.getOrPut(base[it].text) { numbers.size } }
        val otherText = IntArray(other.size) { numbers.getOrPut(other[it].text) { numbers.size } }

        // identical siblings at both ends are the same ones
        var start = 0
        while (start < base.size && start < other.size && baseText[start] == otherText[start]) {
            link(start, start)
            start++
        }
        var baseEnd = base.size
        var otherEnd = other.size
        while (baseEnd > start && otherEnd > start && baseText[baseEnd - 1] == otherText[otherEnd - 1]) {
            link(--baseEnd, --otherEnd)
        }

        val ownNames = ownNames(base)
        if ((baseEnd - start).toLong() * (otherEnd - start) <= MAX_COMPARED_PAIRS) {
            pairStretch(
                base, (start until baseEnd).toList(), other, (start until otherEnd).toList(), inPlace, ownNames, score, ::link
            )
        } else {
            // too many to compare them all: the identical ones first, then the ones between them
            val anchors = identicalInOrder(baseText, start, baseEnd, otherText, start, otherEnd)
            anchors.forEach { (baseIndex, otherIndex) -> link(baseIndex, otherIndex) }
            var baseFrom = start
            var otherFrom = start
            for ((baseIndex, otherIndex) in anchors + (baseEnd to otherEnd)) {
                pairStretch(
                    base, (baseFrom until baseIndex).toList(), other, (otherFrom until otherIndex).toList(),
                    inPlace, ownNames, score, ::link
                )
                baseFrom = baseIndex + 1
                otherFrom = otherIndex + 1
            }
        }

        // identical siblings left on both sides: moved
        val left = HashMap<Int, ArrayDeque<Int>>()
        for (index in base.indices) if (!used[index]) left.getOrPut(baseText[index]) { ArrayDeque() }.addLast(index)
        for (index in other.indices) {
            if (result[index] == UNPAIRED) left[otherText[index]]?.removeFirstOrNull()?.let { link(it, index) }
        }
        return result
    }

    /**
     * Pairs the siblings of a stretch by their content (see [mostInCommon]), with two exceptions in which they are
     * read as by position:
     *
     * - Siblings were added, the old ones can be read as changed in place (see [RepeatedSiblingAligner.inPlace]) and
     *   their content would leave some of them without a partner. The new ones are the last, as while their number
     *   does not change.
     * - A sibling has two candidates next to each other, such as a block and the copy of it that a mod added right
     *   after it. The first one is the original, unless the other has fewer than half its differences. If the
     *   sibling has a name of its own and only one of the two keeps it, that one is the original.
     */
    private fun pairStretch(
        base: List<SiblingContent>, baseIndices: List<Int>,
        other: List<SiblingContent>, otherIndices: List<Int>,
        inPlace: Boolean,
        ownNames: Lazy<Set<String>>,
        score: (SiblingContent, SiblingContent) -> Int,
        link: (Int, Int) -> Unit,
    ) {
        val n = baseIndices.size
        val m = otherIndices.size
        if (n == 0 || m == 0) return
        if (n.toLong() * m > MAX_COMPARED_PAIRS) {
            // too many to compare: one by one in order
            for (k in 0 until minOf(n, m)) link(baseIndices[k], otherIndices[k])
            return
        }
        val partners = mostInCommon(base, baseIndices, other, otherIndices, score)
        if (inPlace && m > n && partners.count { it != UNPAIRED } < n &&
            inPlace(base, baseIndices, other, otherIndices, ownNames)
        ) {
            for (k in 0 until n) link(baseIndices[k], otherIndices[k])
            return
        }
        val found = partners.copyOf()
        var previous = -1 // where the partner of the sibling before ended up
        for (j in 0 until m) {
            val i = found[j]
            if (i == UNPAIRED) continue
            // the candidates that the content left without a partner right before and right after the one it chose
            var first = j
            while (first > previous + 1 && found[first - 1] == UNPAIRED) first--
            var last = j
            while (last < m - 1 && found[last + 1] == UNPAIRED) last++
            previous = j
            if (first == last) continue

            val sibling = base[baseIndices[i]]
            val chosen = other[otherIndices[j]]
            val name = sibling.name?.takeIf { it in ownNames.value }
            val chosenKeepsName = name != null && chosen.name == name
            fun keepsName(candidate: Int) = name != null && other[otherIndices[candidate]].name == name
            fun alike(candidate: Int) = score(sibling, other[otherIndices[candidate]]) > 0

            val differences = sibling.differences(chosen)
            val original = (first until j).firstOrNull { candidate ->
                alike(candidate) && when {
                    keepsName(candidate) != chosenKeepsName -> !chosenKeepsName
                    else -> sibling.differences(other[otherIndices[candidate]]) <= 2 * differences
                }
            } ?: (j + 1..last).firstOrNull { candidate -> !chosenKeepsName && keepsName(candidate) && alike(candidate) }
            if (original != null) {
                partners[original] = i
                partners[j] = UNPAIRED
                previous = original
            }
        }
        for (j in 0 until m) if (partners[j] != UNPAIRED) link(baseIndices[partners[j]], otherIndices[j])
    }

    /**
     * Pairs the siblings keeping their order, so that what the pairs have in common adds up to the most.
     * Siblings whose [score] is 0 are not paired.
     *
     * @return for each of [otherIndices], the position of its partner in [baseIndices], or [UNPAIRED]
     */
    private fun mostInCommon(
        base: List<SiblingContent>, baseIndices: List<Int>,
        other: List<SiblingContent>, otherIndices: List<Int>,
        score: (SiblingContent, SiblingContent) -> Int,
    ): IntArray {
        val n = baseIndices.size
        val m = otherIndices.size
        val scores = Array(n) { i -> IntArray(m) { j -> score(base[baseIndices[i]], other[otherIndices[j]]) } }
        val best = Array(n + 1) { LongArray(m + 1) }
        for (i in n - 1 downTo 0) {
            for (j in m - 1 downTo 0) {
                var value = maxOf(best[i + 1][j], best[i][j + 1])
                if (scores[i][j] > 0) value = maxOf(value, best[i + 1][j + 1] + scores[i][j])
                best[i][j] = value
            }
        }
        val partners = IntArray(m) { UNPAIRED }
        var i = 0
        var j = 0
        while (i < n && j < m) {
            when {
                scores[i][j] > 0 && best[i][j] == best[i + 1][j + 1] + scores[i][j] -> {
                    partners[j] = i
                    i++
                    j++
                }

                best[i][j] == best[i + 1][j] -> i++
                else -> j++
            }
        }
        return partners
    }

    /** Identical siblings in the same order in both ranges, as pairs of (base index, other index) */
    private fun identicalInOrder(
        base: IntArray, baseFrom: Int, baseTo: Int,
        other: IntArray, otherFrom: Int, otherTo: Int,
    ): List<Pair<Int, Int>> {
        val n = baseTo - baseFrom
        val m = otherTo - otherFrom
        if (n == 0 || m == 0) return emptyList()
        if (n.toLong() * m > MAX_ANCHOR_CELLS) return uniqueInOrder(base, baseFrom, baseTo, other, otherFrom, otherTo)

        // longest common subsequence
        val lengths = Array(n + 1) { IntArray(m + 1) }
        for (i in n - 1 downTo 0) {
            for (j in m - 1 downTo 0) {
                lengths[i][j] = if (base[baseFrom + i] == other[otherFrom + j]) {
                    lengths[i + 1][j + 1] + 1
                } else {
                    maxOf(lengths[i + 1][j], lengths[i][j + 1])
                }
            }
        }
        val anchors = ArrayList<Pair<Int, Int>>()
        var i = 0
        var j = 0
        while (i < n && j < m) {
            when {
                base[baseFrom + i] == other[otherFrom + j] -> {
                    anchors += (baseFrom + i) to (otherFrom + j)
                    i++
                    j++
                }

                lengths[i + 1][j] >= lengths[i][j + 1] -> i++
                else -> j++
            }
        }
        return anchors
    }

    /** For very long lists: the siblings that appear exactly once on each side, in the longest run that keeps their order */
    private fun uniqueInOrder(
        base: IntArray, baseFrom: Int, baseTo: Int,
        other: IntArray, otherFrom: Int, otherTo: Int,
    ): List<Pair<Int, Int>> {
        val basePosition = HashMap<Int, Int>() // -1: appears more than once
        for (index in baseFrom until baseTo) basePosition[base[index]] = if (base[index] in basePosition) -1 else index
        val otherCount = HashMap<Int, Int>()
        for (index in otherFrom until otherTo) otherCount.merge(other[index], 1, Int::plus)

        val candidates = ArrayList<Pair<Int, Int>>() // in the order of the other side
        for (index in otherFrom until otherTo) {
            val position = basePosition[other[index]] ?: continue
            if (position >= 0 && otherCount[other[index]] == 1) candidates += position to index
        }

        // longest increasing run of base positions
        val tails = ArrayList<Int>() // tails[k]: candidate that ends the best run of length k + 1
        val previous = IntArray(candidates.size) { -1 }
        for (current in candidates.indices) {
            var low = 0
            var high = tails.size
            while (low < high) {
                val middle = (low + high) ushr 1
                if (candidates[tails[middle]].first < candidates[current].first) low = middle + 1 else high = middle
            }
            if (low > 0) previous[current] = tails[low - 1]
            if (low == tails.size) tails += current else tails[low] = current
        }
        val anchors = ArrayList<Pair<Int, Int>>()
        var current = tails.lastOrNull() ?: -1
        while (current >= 0) {
            anchors += candidates[current]
            current = previous[current]
        }
        anchors.reverse()
        return anchors
    }
}
