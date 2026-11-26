package ankol.mod.merger.mergers

import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RepeatedSiblingAlignerTest {
    /** A sibling whose text is [text], with one value and one statement inside */
    private fun c(text: String, value: String = text, shape: String = "k") =
        SiblingContent(text, listOf(value), listOf(shape))

    /** A sibling with several values: a, b, c, d... unless [values] says otherwise */
    private fun rich(text: String, vararg values: String = arrayOf("a", "b", "c", "d"), shape: List<String> = listOf("k"), name: String? = null) =
        SiblingContent(text, name, { values.toList() }, { shape })

    private fun bare(text: String) = SiblingContent(text, emptyList(), emptyList())

    private fun pair(base: List<SiblingContent>, other: List<SiblingContent>) = RepeatedSiblingAligner.pair(base, other).toList()

    private val a = c("a")
    private val b = c("b")
    private val d = c("d")
    private val e = c("e")

    @Test
    fun sameNumberOfSiblingsIsPairedByPosition() {
        assertEquals(listOf(0, 1, 2), pair(listOf(a, b, d), listOf(a, b, d)))
        assertEquals(listOf(0, 1, 2), pair(listOf(a, b, d), listOf(a, c("b1", "b"), d)))
        // also when a sibling becomes what its neighbour was: each one can become the one at its position
        assertEquals(listOf(0, 1, 2), pair(listOf(a, b, d), listOf(b, d, e)))
        assertEquals(listOf(0, 1, 2), pair(listOf(a, b, d), listOf(d, a, b)))
        // or gets a statement that it did not have
        assertEquals(listOf(0, 1), pair(listOf(a, b), listOf(b, SiblingContent("a1", listOf("a"), listOf("k", "new")))))
    }

    @Test
    fun siblingsWithANameOfTheirOwnAreNotReadInPlaceIfTheyAreStillThereElsewhere() {
        // e.g. the second argument of a function block: one was removed and another added
        val x = rich("x", "1", "2", "3", "4", name = "s1")
        val y = rich("y", "5", "6", "7", "8", name = "s2")
        val z = rich("z", "9", "10", "11", "12", name = "s3")
        val w = rich("w", "13", "14", "15", "16", name = "s4")
        assertEquals(listOf(1, 2, -1), pair(listOf(x, y, z), listOf(y, z, w)))
        // and two added: the old ones are not the first ones
        assertEquals(listOf(1, 2, -1, -1), pair(listOf(x, y, z), listOf(y, z, w, rich("v", "17", "18", "19", "20", name = "s5"))))
        // renamed in place: nothing else has its name
        assertEquals(listOf(0, 1, 2), pair(listOf(x, y, z), listOf(x, rich("y1", "5", "6", "7", "8", name = "other"), z)))
        // a name that several of them have is not a name of their own
        val shared = listOf(rich("p", "1", name = "lvl"), rich("q", "2", name = "lvl"), rich("r", "3", name = "lvl"))
        assertEquals(listOf(0, 1, 2), pair(shared, listOf(rich("p1", "2", name = "none"), rich("q1", "3", name = "lvl"), rich("r1", "4", name = "lvl"))))
    }

    @Test
    fun ofTwoCandidatesTheOneThatKeepsTheNameIsTheOriginal() {
        val m = rich("m", name = "s2")
        val renamedCopy = rich("m1", "a", "b", "c", "z", name = "n1")
        val changed = rich("m2", "a", "b", "x", "y", name = "s2")
        // the copy is more like m, but the other one keeps its name: before it or after it
        assertEquals(listOf(0, -1, 1), pair(listOf(a, m), listOf(a, renamedCopy, changed)))
        assertEquals(listOf(0, 1, -1), pair(listOf(a, m), listOf(a, changed, renamedCopy)))
        // and a new one with fewer than half the differences is still not it
        val nearly = rich("m3", "a", "b", "c", "d", name = "n2")
        val rewritten = rich("m4", "a", "x", "y", "z", name = "s2")
        assertEquals(listOf(0, 1, -1), pair(listOf(a, m), listOf(a, rewritten, nearly)))
    }

    @Test
    fun candidatesThatKeepTheNameDoNotCrossEachOther() {
        val first = rich("p", "1", "2", "3", "4", name = "X")
        val second = rich("q", "5", "6", "7", "8", name = "Y")
        val other = listOf(
            rich("p0", "1", "2", "3", "z", name = "lost"), rich("y1", "5", "u", "v", "w", name = "Y"),
            rich("x2", "1", "r", "s", "t", name = "X"), rich("q3", "5", "6", "7", "z", name = "gone"),
        )
        // the first one goes to the one that keeps its name; the second one stays after it
        assertEquals(listOf(-1, -1, 0, 1), pair(listOf(first, second), other))
    }

    @Test
    fun sameNumberOfSiblingsThatCannotBecomeTheOnesAtTheirPositionIsPairedByContent() {
        // each one has other statements inside, so one written over another would keep statements of both
        val x = c("x", shape = "kx")
        val y = c("y", shape = "ky")
        val z = c("z", shape = "kz")
        // swapped
        assertEquals(listOf(1, 0, 2), pair(listOf(x, y, z), listOf(y, x, z)))
        // one removed and another added
        assertEquals(listOf(1, 2, -1), pair(listOf(x, y, z), listOf(y, z, c("w", shape = "kw"))))
    }

    @Test
    fun removedSiblingLeavesTheOthersPaired() {
        assertEquals(listOf(0, 2), pair(listOf(a, b, d), listOf(a, d)))
        assertEquals(listOf(1, 2), pair(listOf(a, b, d), listOf(b, d)))
    }

    @Test
    fun insertedSiblingHasNoPartner() {
        assertEquals(listOf(0, -1, 1), pair(listOf(a, d), listOf(a, b, d)))
        assertEquals(listOf(-1, 0, 1), pair(listOf(a, d), listOf(b, a, d)))
    }

    @Test
    fun copyAddedRightAfterASiblingIsNotTakenForIt() {
        // the mod changes m and adds after it a copy of what m was: the first one is still m
        val m = rich("m")
        val changed = rich("m1", "a", "b", "x", "y")
        val copy = rich("m2", "a", "b", "c", "z")
        assertEquals(listOf(0, 1, -1), pair(listOf(a, m), listOf(a, changed, copy)))
        assertEquals(listOf(0, 1, -1, 2), pair(listOf(a, m, d), listOf(a, changed, copy, d)))
    }

    @Test
    fun siblingAddedBeforeAChangedOneIsFoundByItsContent() {
        // the second candidate has fewer than half the differences of the first
        val m = rich("m")
        val new = rich("n", "p", "q", "r", "s")
        assertEquals(listOf(0, -1, 1), pair(listOf(a, m), listOf(a, new, rich("m1", "a", "b", "c", "z"))))
        assertEquals(listOf(0, -1, 1), pair(listOf(a, m), listOf(a, rich("n1", "a", "x", "y", "z"), rich("m1", "a", "b", "c", "z"))))
    }

    @Test
    fun siblingsAddedInTheMiddleAreFoundWhenAllTheOthersChanged() {
        // nothing is left untouched, but each of the old ones has most in common with its changed version
        val first = rich("p1", "1", "2", "3", "4")
        val second = rich("p2", "5", "6", "7", "8")
        val third = rich("p3", "9", "10", "11", "12")
        val other = listOf(
            rich("q1", "1", "2", "3", "x"), rich("new", "1", "2", "y", "z"), rich("q2", "5", "6", "7", "x"), rich("q3", "9", "10", "11", "x")
        )
        assertEquals(listOf(0, -1, 1, 2), pair(listOf(first, second, third), other))
    }

    @Test
    fun addedSiblingsAreTheLastOnesIfTheContentLeavesOldOnesWithoutAPartner() {
        // 10, 20, 30 become 20, 40, 60 and 70 is new: the first one is not the old 20
        val base = listOf(c("10"), c("20"), c("30"))
        assertEquals(listOf(0, 1, 2, -1), pair(base, listOf(c("20"), c("40"), c("60"), c("70"))))
        // every value changed and one more at the end
        assertEquals(listOf(0, 1, 2, -1), pair(base, listOf(c("15"), c("25"), c("35"), c("45"))))
    }

    @Test
    fun siblingModifiedNextToARemovedOneIsPairedByItsContent() {
        val m = c("m")
        assertEquals(listOf(0, 2, 3), pair(listOf(a, b, m, d), listOf(a, c("m1", "m"), d)))
    }

    @Test
    fun siblingModifiedNextToAnInsertedOneIsPairedByItsContent() {
        val m = c("m")
        assertEquals(listOf(0, -1, 1, 2), pair(listOf(a, m, d), listOf(a, c("n"), c("m1", "m"), d)))
    }

    @Test
    fun siblingThatBecomesWhatItsNeighbourWasIsStillTheSameOne() {
        // one is removed; of the others, the second becomes what the third was and the third changes too
        val first = rich("p1", "a", "b", "c", "1")
        val second = rich("p2", "a", "b", "c", "2")
        val third = rich("p3", "a", "b", "c", "3")
        val base = listOf(a, first, second, third)
        assertEquals(listOf(1, 2, 3), pair(base, listOf(first, rich("p3", "a", "b", "c", "3"), rich("p4", "a", "b", "c", "4"))))
    }

    @Test
    fun siblingsWithNothingInCommonAreNotPaired() {
        assertEquals(listOf(0, -1), pair(listOf(a, b, d), listOf(a, c("x", shape = "other"))))
    }

    @Test
    fun siblingsThatOnlyShareTheNamesOfTheirChildrenArePaired() {
        assertEquals(listOf(0, 1), pair(listOf(a, b, d), listOf(a, c("x"))))
    }

    @Test
    fun siblingWithNothingToCompareIsPairedInOrder() {
        // e.g. an empty block that a mod fills in
        assertEquals(listOf(0, 1), pair(listOf(bare("a"), bare("b"), bare("d")), listOf(bare("a"), bare("b1"))))
        assertEquals(listOf(0, 1), pair(listOf(bare("a"), bare("b"), bare("d")), listOf(bare("a"), c("x"))))
    }

    @Test
    fun movedSiblingsArePairedIfTheyAreIdentical() {
        assertEquals(listOf(2, 0, 1), pair(listOf(a, b, d, e), listOf(d, a, b)))
    }

    @Test
    fun identicalSiblingsKeepTheirOrder() {
        assertEquals(listOf(0, 2), pair(listOf(a, a, b), listOf(a, b)))
    }

    @Test
    fun longListWithOneRemovedSibling() {
        val base = (0 until 5000).map { c("m$it") }
        val other = base.filterIndexed { index, _ -> index != 2500 }
        val started = System.nanoTime()
        val result = pair(base, other)
        assertTrue((System.nanoTime() - started) / 1_000_000 < 2000, "too slow")
        assertEquals((0 until 5000).filter { it != 2500 }, result)
    }

    @Test
    fun longListWhoseEndsWereModified() {
        // nothing to trim at either end, and too long to compare every pair
        val base = (0 until 3000).map { c("m$it") }
        val other = base.toMutableList()
        other[0] = c("first", "m0")
        other[2999] = c("last", "m2999")
        other.removeAt(1500)
        assertEquals((0 until 3000).filter { it != 1500 }, pair(base, other))
    }

    @Test
    fun eachVersionIsAlignedWithVanilla() {
        val m = c("m")
        val ids = RepeatedSiblingAligner.align(listOf(a, m, d), listOf(a, c("m1", "m"), d), listOf(a, d))
        assertEquals(listOf("0", "1", "2"), ids.vanilla)
        assertEquals(listOf("0", "1", "2"), ids.accumulated)
        assertEquals(listOf("0", "2"), ids.incoming)
    }

    @Test
    fun siblingAddedByBothModsIsTheSameOneIfIdentical() {
        val x = c("x")
        val ids = RepeatedSiblingAligner.align(listOf(a), listOf(a, x), listOf(x, a))
        assertEquals(listOf("0", "a1"), ids.accumulated)
        assertEquals(listOf("a1", "0"), ids.incoming)
    }

    @Test
    fun siblingsAddedByModsAreTheSameOneOnlyIfTheyAreAlike() {
        val x = rich("x")
        fun incoming(other: SiblingContent) = RepeatedSiblingAligner.align(listOf(a), listOf(a, x), listOf(a, other)).incoming
        // one value is different: another version of x, whose difference will be asked
        assertEquals(listOf("0", "a1"), incoming(rich("x1", "a", "b", "c", "z")))
        // also when it is its only value
        assertEquals(listOf("0", "a1"), RepeatedSiblingAligner.align(listOf(a), listOf(a, c("30")), listOf(a, c("35"))).incoming)
        // two of four: something else
        assertEquals(listOf("0", "+1"), incoming(rich("y", "a", "b", "y", "z")))
        // two of six: another version
        val six = rich("s", "a", "b", "c", "d", "e", "f")
        assertEquals(listOf("0", "a1"), RepeatedSiblingAligner.align(listOf(a), listOf(a, six), listOf(a, rich("s1", "a", "b", "c", "d", "y", "z"))).incoming)
        // one more statement inside: another version
        assertEquals(listOf("0", "a1"), incoming(SiblingContent("x2", listOf("a", "b", "c", "d", "new"), listOf("k", "k2"))))
        // other statements inside: something else
        assertEquals(listOf("0", "+1"), incoming(rich("x3", shape = listOf("other"))))
        // another name (the second argument of a function block): something else
        val named = rich("x", name = "s1")
        assertEquals(listOf("0", "+1"), RepeatedSiblingAligner.align(listOf(a), listOf(a, named), listOf(a, rich("x4", name = "s2"))).incoming)
        assertEquals(listOf("0", "a1"), RepeatedSiblingAligner.align(listOf(a), listOf(a, named), listOf(a, rich("x5", "a", "b", "c", "z", name = "s1"))).incoming)
    }

    @Test
    fun siblingsAddedByModsInDifferentPlacesAreDifferentOnesUnlessIdentical() {
        val x = rich("x")
        val nearly = rich("x1", "a", "b", "c", "z")
        // one mod adds x after a and the other adds a similar one after b
        var ids = RepeatedSiblingAligner.align(listOf(a, b), listOf(a, x, b), listOf(a, b, nearly))
        assertEquals(listOf("0", "a1", "1"), ids.accumulated)
        assertEquals(listOf("0", "1", "+2"), ids.incoming)
        // the same one in another place is still the same one
        ids = RepeatedSiblingAligner.align(listOf(a, b), listOf(a, x, b), listOf(a, b, x))
        assertEquals(listOf("0", "1", "a1"), ids.incoming)
    }

    @Test
    fun familyThatVanillaDoesNotHaveIsNumberedByTheAccumulatedVersion() {
        val x = rich("x")
        val ids = RepeatedSiblingAligner.align(emptyList(), listOf(x, rich("y", "1", "2", "3", "4")), listOf(x, rich("z", "5", "6", "7", "8")))
        assertEquals(listOf("0", "1"), ids.accumulated)
        assertEquals(listOf("0", "+1"), ids.incoming)
    }

    @Test
    fun withoutVanillaTheTwoVersionsArePairedDirectly() {
        val ids = RepeatedSiblingAligner.align(null, listOf(a, b), listOf(a, c("b1", "b"), c("z", shape = "other")))
        assertEquals(listOf("0", "1"), ids.accumulated)
        assertEquals(listOf("0", "1", "+2"), ids.incoming)
    }
}
