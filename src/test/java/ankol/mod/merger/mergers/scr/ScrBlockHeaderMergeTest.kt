package ankol.mod.merger.mergers.scr

import ankol.mod.merger.api.ConflictResolver
import ankol.mod.merger.constants.UserChoice
import ankol.mod.merger.core.BaseModManager
import ankol.mod.merger.core.files.InMemoryArchiveFile
import ankol.mod.merger.domain.MergeContext
import ankol.mod.merger.mergers.ConflictRecord
import ankol.mod.merger.mergers.DeletionRecord
import ankol.mod.merger.rules.MergeRules
import ankol.mod.merger.tools.Tools
import org.junit.After
import org.junit.Test
import java.nio.file.Files
import java.nio.file.Path
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * The arguments in the header of a function block, e.g. the "Hard" in
 * MeshLootType("furniture_chest.msh", ..., "Hard", false, Color_Violet) { ... } (Dying Light 1, containers.loot),
 * must be merged like the arguments of a function call. Blocks with the same name and first argument are
 * paired by their position while they can be read as changed in place, and by their content when one was added,
 * removed or replaced (see RepeatedSiblingAligner), so that it does not move changes onto its neighbours.
 */
class ScrBlockHeaderMergeTest {
    private val relPath = "scripts/inventory/containers_test.loot"
    private val tempDirs = mutableListOf<Path>()
    private val managers = mutableListOf<BaseModManager>()

    /** Takes the incoming mod in every conflict and keeps what it was asked */
    private class TestResolver : ConflictResolver {
        val asked = mutableListOf<ConflictRecord>()

        override fun resolveConflict(conflicts: MutableList<ConflictRecord>) {
            conflicts.filter { it.userChoice == null }.forEach { asked.add(it) }
            conflicts.forEach { it.userChoice = UserChoice.MERGE_MOD }
        }

        override fun resolveDeletionConflicts(deletions: MutableList<DeletionRecord>) {
            deletions.forEach { it.userChoice = UserChoice.MERGE_MOD }
        }
    }

    @After
    fun cleanup() {
        managers.forEach { it.close() }
        tempDirs.forEach { runCatching { Tools.deleteRecursively(it) } }
    }

    /** Same order as FileMergerEngine: vanilla + mod1, then each following mod */
    private fun mergeChain(
        vanilla: String, mods: List<String>, resolver: TestResolver = TestResolver(), rules: String = "{}",
        vanillaEntry: String = relPath
    ): String {
        val sourceDir = Files.createTempDirectory("smm_block_header_test_")
        tempDirs.add(sourceDir)
        ZipOutputStream(Files.newOutputStream(sourceDir.resolve("data0.pak"))).use { zip ->
            zip.putNextEntry(ZipEntry(vanillaEntry))
            zip.write(vanilla.toByteArray())
            zip.closeEntry()
        }
        val baseModManager = BaseModManager(sourceDir)
        managers.add(baseModManager)
        val policy = MergeRules.buildPolicy(listOf("test" to MergeRules.parse(rules, "test")))
        val context = MergeContext(baseModManager, resolver, deletionPolicy = policy)

        // if the game does not have the file, the following mods are merged into the first one
        val inVanilla = vanillaEntry == relPath
        var accumulated = if (inVanilla) vanilla else mods.first()
        var accumulatedName = if (inVanilla) "data0.pak" else "mod1.pak"
        mods.drop(if (inVanilla) 0 else 1).forEachIndexed { index, modContent ->
            val modName = "mod${index + if (inVanilla) 1 else 2}.pak"
            context.configure(relPath, accumulatedName, modName, inVanilla && index == 0)
            val result = TechlandScrFileMerger(context).merge(
                InMemoryArchiveFile("containers_test.loot", relPath, mutableListOf(accumulatedName), accumulated),
                InMemoryArchiveFile("containers_test.loot", relPath, mutableListOf(modName), modContent),
            )
            accumulated = result.mergedContent
            accumulatedName = modName
        }
        return accumulated
    }

    private val vanilla = """
        sub main()
        {
            MeshLootType("furniture_chest.msh", "ornated_oriental", "WeaponChestHard", "Hard", false)
            {
                SetSlotLootType("hook_loot_01", "ValuablesChestHard", 0.0);
            }
            MeshLootType("furniture_chest.msh", "ornated_golden", "ValuablesChestHard", "Medium", false)
            {
                SetSlotLootType("hook_loot_01", "ValuablesChestHard", 1.0);
            }
            MeshLootType("locker.msh", "default", "Locker", "", false);
        }
    """.trimIndent()

    private val noLock = vanilla.replace("\"Hard\", false)", "\"\", false)").replace("\"Medium\", false)", "\"\", false)")
    private val otherChange = vanilla.replace("0.0);", "0.5);").replace("\"Locker\", \"\", false);", "\"Locker\", \"\", true);")
    private val expected = noLock.replace("0.0);", "0.5);").replace("\"Locker\", \"\", false);", "\"Locker\", \"\", true);")

    @Test
    fun headerChangeFromFirstModIsKept() {
        assertEquals(expected, mergeChain(vanilla, listOf(noLock, otherChange)))
    }

    @Test
    fun headerChangeFromLaterModIsKept() {
        assertEquals(expected, mergeChain(vanilla, listOf(otherChange, noLock)))
    }

    @Test
    fun conflictingHeadersAskAboutTheHeaderOnly() {
        val easy = vanilla.replace("\"Hard\", false)", "\"Easy\", false)")
        val resolver = TestResolver()
        val merged = mergeChain(vanilla, listOf(noLock, easy), resolver)

        assertEquals(1, resolver.asked.size)
        assertEquals("""MeshLootType("furniture_chest.msh", "ornated_oriental", "WeaponChestHard", "", false)""",
            resolver.asked[0].baseNode.sourceText)
        assertEquals("""MeshLootType("furniture_chest.msh", "ornated_oriental", "WeaponChestHard", "Easy", false)""",
            resolver.asked[0].modNode.sourceText)
        // the incoming mod was chosen for the header; the other block keeps the first mod's change
        assertEquals(noLock.replace("\"\", false)\n    {\n        SetSlotLootType(\"hook_loot_01\", \"ValuablesChestHard\", 0.0)",
            "\"Easy\", false)\n    {\n        SetSlotLootType(\"hook_loot_01\", \"ValuablesChestHard\", 0.0)"), merged)
        assertTrue("\"Medium\"" !in merged, merged)
    }

    private fun String.withoutWhitespace() = filterNot(Char::isWhitespace)

    private fun String.count(part: String) = split(part).size - 1

    /** Blocks with the same name and first argument: their signature does not tell them apart */
    private fun box(slot: String, level: String = "lvl", value: String = "1.0") =
        "    Box(\"m\", \"$slot\", \"$level\")\n    {\n        Slot(\"a\", $value);\n        Slot(\"$slot\", 2.0);\n    }\n"

    private fun main(vararg statements: String) = "sub main()\n{\n" + statements.joinToString("") + "}"

    private val boxes = main(box("s1"), box("s2"), box("s3"))

    @Test
    fun removedRepeatedBlockIsKeptAndNothingElseChanges() {
        // removals are not applied unless a rule says so
        assertEquals(boxes, mergeChain(boxes, listOf(main(box("s1"), box("s3")))))
    }

    @Test
    fun removedRepeatedBlockIsTheOneDeleted() {
        val withoutMiddle = main(box("s1"), box("s3"))
        val merged = mergeChain(boxes, listOf(withoutMiddle), rules = """{"scr":{"deletion":{"defaultAction":"DELETE"}}}""")
        assertEquals(withoutMiddle.withoutWhitespace(), merged.withoutWhitespace())
    }

    @Test
    fun removedRepeatedBlockDoesNotMoveTheHeadersNestedInsideItsNeighbours() {
        fun group(tag: String, item: String, n: Int) =
            "    Group(\"g\", \"$tag\")\n    {\n        Item(\"$item\", $n)\n        {\n            Slot(\"a\", 1.0);\n        }\n    }\n"
        val vanilla = "sub main()\n{\n" + group("one", "alpha", 1) + group("two", "beta", 2) + group("three", "gamma", 3) + "}"
        for (removed in listOf(group("one", "alpha", 1), group("two", "beta", 2))) {
            val merged = mergeChain(vanilla, listOf(vanilla.replace(removed, "")))
            assertEquals(vanilla, merged)
        }
    }

    @Test
    fun blocksThatBecomeWhatTheirNeighboursWereStayInPlace() {
        // nothing was added or removed: each block is the same one, as in levels_statuslevel.scr with
        // ID(300) -> ID(400) and the next ID(400) -> ID(450)
        fun level(xp: Int) = "    Level()\n    {\n        Xp($xp);\n    }\n"
        val vanilla = main(level(10), level(20), level(30))
        val mod = main(level(20), level(30), level(40))
        assertEquals(mod, mergeChain(vanilla, listOf(mod)))
    }

    private fun slots(name: String, a: String, b: String, c: String) =
        "    Box(\"m\", \"$name\", \"lvl\")\n    {\n        Slot(\"a\", $a);\n        Slot(\"b\", $b);\n        Slot(\"c\", $c);\n    }\n"

    @Test
    fun copyOfABlockAddedAfterItIsNotTakenForIt() {
        // the second mod changes s2 and adds s3, which is what s2 was
        val vanilla = main(slots("s1", "1.0", "2.0", "3.0"), slots("s2", "4.0", "5.0", "6.0"))
        val first = main(slots("s1", "1.0", "2.0", "3.0"), slots("s2", "4.0", "5.0", "6.5"))
        val second = main(slots("s1", "1.0", "2.0", "3.0"), slots("s2", "4.1", "5.1", "6.0"), slots("s3", "4.0", "5.0", "6.0"))
        val expected = main(slots("s1", "1.0", "2.0", "3.0"), slots("s2", "4.1", "5.1", "6.5"), slots("s3", "4.0", "5.0", "6.0"))
        assertEquals(expected.withoutWhitespace(), mergeChain(vanilla, listOf(first, second)).withoutWhitespace())
        assertEquals(expected.withoutWhitespace(), mergeChain(vanilla, listOf(second, first)).withoutWhitespace())
    }

    @Test
    fun copyOfABlockWithoutArgumentsAddedAtTheEndIsNotTakenForIt() {
        fun level(id: Int, a: String = "1.0", b: String = "2.0", c: String = "3.0") =
            "    Level()\n    {\n        Id($id);\n        A($a);\n        B($b);\n        C($c);\n    }\n"
        val vanilla = main(level(1), level(2))
        val first = main(level(1), level(2, a = "1.5"))
        val second = main(level(1), level(2, b = "2.5", c = "3.5"), level(3))
        val expected = main(level(1), level(2, "1.5", "2.5", "3.5"), level(3))
        assertEquals(expected.withoutWhitespace(), mergeChain(vanilla, listOf(first, second)).withoutWhitespace())
        assertEquals(expected.withoutWhitespace(), mergeChain(vanilla, listOf(second, first)).withoutWhitespace())
    }

    private fun tier(a: Int, b: Int, c: Int, d: Int) = "    Tier()\n    {\n        P0($a);\n        P1($b);\n        P2($c);\n        P3($d);\n    }\n"

    @Test
    fun copyOfABlockAddedAfterItInTheMiddleIsNotTakenForIt() {
        val first = tier(1, 10, 100, 1000)
        val third = tier(3, 30, 300, 3000)
        val vanilla = main(first, tier(2, 20, 200, 2000), third)
        // one mod changes a value of the second block; the other changes two more and adds a copy of what it was
        val one = main(first, tier(2, 20, 200, 2001), third)
        val other = main(first, tier(2, 21, 201, 2000), tier(9, 20, 200, 2000), third)
        val expected = main(first, tier(2, 21, 201, 2001), tier(9, 20, 200, 2000), third).withoutWhitespace()
        assertEquals(expected, mergeChain(vanilla, listOf(one, other)).withoutWhitespace())
        assertEquals(expected, mergeChain(vanilla, listOf(other, one)).withoutWhitespace())
    }

    @Test
    fun blockAddedInTheMiddleIsStillFoundAfterLaterModsChangeAllTheOthers() {
        val vanilla = main(slots("s1", "1.0", "2.0", "3.0"), slots("s2", "4.0", "5.0", "9.0"), slots("s3", "7.0", "8.0", "9.0"))
        val added = main(slots("s1", "1.0", "2.0", "3.0"), slots("n1", "0.1", "0.2", "0.3"), slots("s2", "4.0", "5.0", "9.0"), slots("s3", "7.0", "8.0", "9.0"))
        val changed = main(slots("s1", "1.1", "2.0", "3.0"), slots("s2", "4.4", "5.0", "9.0"), slots("s3", "7.7", "8.0", "9.0"))
        val last = main(slots("s1", "1.0", "2.0", "3.0"), slots("s2", "4.0", "5.0", "9.0"), slots("s3", "7.0", "8.0", "9.9"))
        val expected = main(
            slots("s1", "1.1", "2.0", "3.0"), slots("n1", "0.1", "0.2", "0.3"), slots("s2", "4.4", "5.0", "9.0"), slots("s3", "7.7", "8.0", "9.9")
        ).withoutWhitespace()
        for (mods in listOf(listOf(added, changed, last), listOf(changed, added, last), listOf(added, last, changed))) {
            val resolver = TestResolver()
            assertEquals(expected, mergeChain(vanilla, mods, resolver).withoutWhitespace())
            assertEquals(0, resolver.asked.size)
        }
    }

    @Test
    fun blockAddedBeforeAChangedOneIsNotTakenForIt() {
        val vanilla = main(slots("s1", "1.0", "2.0", "3.0"), "    Call(\"x\", 1);\n")
        val one = main(slots("s1", "1.0", "2.0", "3.5"), "    Call(\"x\", 1);\n")
        val other = main(slots("n0", "9.0", "9.0", "3.0"), slots("s1", "1.1", "2.0", "3.0"), "    Call(\"x\", 1);\n")
        val expected = main(slots("n0", "9.0", "9.0", "3.0"), slots("s1", "1.1", "2.0", "3.5"), "    Call(\"x\", 1);\n").withoutWhitespace()
        assertEquals(expected, mergeChain(vanilla, listOf(one, other)).withoutWhitespace())
        assertEquals(expected, mergeChain(vanilla, listOf(other, one)).withoutWhitespace())
    }

    @Test
    fun namedBlocksAreToldApartByTheirNameWhenOneIsRemovedAndTwoAreAdded() {
        // as in containers.loot: the second argument names each block, so the ones that are still there are not read in place
        val s1 = slots("s1", "1.0", "2.0", "3.0")
        val s2 = slots("s2", "4.0", "5.0", "6.0")
        val s4 = slots("s4", "1.5", "2.5", "3.5")
        val added = slots("n1", "0.1", "0.2", "0.3") + slots("n2", "0.4", "0.5", "0.6")
        val vanilla = main(s1, s2, slots("s3", "7.0", "8.0", "9.0"), s4)
        val one = main(s1, slots("s3", "7.0", "8.0", "9.0"), s4, added)
        val other = main(s1, s2, slots("s3", "7.0", "8.0", "9.9"), s4)
        // the removed one is kept, as removals are not applied unless a rule says so
        val expected = main(s1, s2, slots("s3", "7.0", "8.0", "9.9"), s4, added).withoutWhitespace()
        for (mods in listOf(listOf(one, other), listOf(other, one))) {
            val resolver = TestResolver()
            assertEquals(expected, mergeChain(vanilla, mods, resolver).withoutWhitespace())
            assertEquals(0, resolver.asked.size)
        }
    }

    @Test
    fun blockThatKeepsItsNameIsTheOriginalWhenACopyWithAnotherNameIsAddedBeforeIt() {
        val s1 = slots("s1", "1.0", "2.0", "3.0")
        val s3 = slots("s3", "7.0", "8.0", "9.0")
        val copy = slots("n1", "4.0", "5.0", "6.1")
        val vanilla = main(s1, slots("s2", "4.0", "5.0", "6.0"), s3)
        val added = main(s1, copy, slots("s2", "4.0", "5.0", "6.0"), s3)
        val first = main(s1, slots("s2", "4.4", "5.0", "6.0"), s3)
        val second = main(s1, slots("s2", "4.0", "5.5", "6.0"), s3)
        val expected = main(s1, copy, slots("s2", "4.4", "5.5", "6.0"), s3).withoutWhitespace()
        for (mods in listOf(listOf(added, first, second), listOf(first, added, second))) {
            val resolver = TestResolver()
            assertEquals(expected, mergeChain(vanilla, mods, resolver).withoutWhitespace())
            assertEquals(0, resolver.asked.size)
        }
    }

    @Test
    fun blocksChangedInPlaceWithOneMoreAtTheEndStayInPlace() {
        // 10, 20, 30 become 20, 40, 60 and 70 is new: the first block is not the old second one
        fun level(xp: Int) = "    Level()\n    {\n        Xp($xp);\n    }\n"
        val mod = main(level(20), level(40), level(60), level(70))
        assertEquals(mod.withoutWhitespace(), mergeChain(main(level(10), level(20), level(30)), listOf(mod)).withoutWhitespace())
    }

    @Test
    fun blocksWithOtherStatementsAreNotMixedWhenOneIsRemovedAndAnotherAdded() {
        fun part(statement: String) = "    Part()\n    {\n        $statement(1);\n    }\n"
        val vanilla = main(part("Alpha"), part("Beta"), part("Gamma"))
        val merged = mergeChain(vanilla, listOf(main(part("Beta"), part("Gamma"), part("Delta"))))
        // the removed one is kept, as removals are not applied unless a rule says so
        assertEquals(main(part("Alpha"), part("Beta"), part("Gamma"), part("Delta")).withoutWhitespace(), merged.withoutWhitespace())
    }

    @Test
    fun blockRenamedNextToARemovedOneIsFoundByItsContent() {
        fun chest(name: String, level: String, slot: String = name) =
            "    Chest(\"m\", \"$name\", \"$level\")\n    {\n        Slot(\"$slot-1\", 1.0);\n        Slot(\"$slot-2\", 2.0);\n    }\n"
        val vanilla = main(chest("a", "Hard"), chest("b", "Medium"), chest("c", "Hard"))
        val mod = main(chest("b2", "Hard", "b"), chest("c", "Hard"))
        assertEquals(main(chest("a", "Hard"), chest("b2", "Hard", "b"), chest("c", "Hard")), mergeChain(vanilla, listOf(mod)))
    }

    @Test
    fun emptyBlockThatAModFillsIsNotDuplicated() {
        val vanilla = main("    Slot()\n    {\n    }\n", "    Slot()\n    {\n    }\n")
        val merged = mergeChain(vanilla, listOf(main("    Slot()\n    {\n    }\n", "    Slot()\n    {\n        A(1);\n    }\n")))
        assertEquals(2, merged.count("Slot()"), merged)
        assertTrue("A(1);" in merged, merged)
    }

    @Test
    fun statementDeletedInABlockStaysDeletedWhenALaterModAddsAnotherBlockLikeIt() {
        val rules = """{"scr":{"deletion":{"rules":[{"file":"**","nodeType":"funCall","name":"X","action":"DELETE"}]}}}"""
        val vanilla = "sub main()\n{\n    Foo(\"a\")\n    {\n        X(1);\n        Y(2);\n    }\n    Foo(\"b\")\n    {\n        X(3);\n    }\n}"
        val withoutX = vanilla.replace("        X(1);\n", "")
        val anotherBlock = vanilla.replace("    Foo(\"b\")", "    Foo(\"a\")\n    {\n        Z(9);\n    }\n    Foo(\"b\")")
        val merged = mergeChain(vanilla, listOf(withoutX, anotherBlock), rules = rules)
        assertTrue("X(1);" !in merged, merged)
        assertTrue("Z(9);" in merged && "X(3);" in merged, merged)
    }

    @Test
    fun blockAddedByAModAndChangedByALaterOneIsAsked() {
        // vanilla has no Box("m", ...): the second mod carries the blocks of the first with a difference
        val plain = "sub main()\n{\n    Call(\"x\", 1);\n}"
        val added = plain.replace("}", slots("s1", "1.0", "2.0", "3.0") + slots("s2", "4.0", "5.0", "6.0") + "}")
        val resolver = TestResolver()
        val merged = mergeChain(plain, listOf(added, added.replace("6.0", "6.6")), resolver)
        assertEquals(2, merged.count("Box(\"m\""), merged)
        assertTrue("Slot(\"c\", 6.6)" in merged && "6.0" !in merged, merged)
        assertEquals(1, resolver.asked.size)
    }

    @Test
    fun smallBlockAddedByTwoModsInTheSamePlaceWithAnotherValueIsAsked() {
        fun level(xp: Int) = "    Level()\n    {\n        Xp($xp);\n    }\n"
        val vanilla = main("    Call(\"x\", 1);\n", level(10), level(20))
        val resolver = TestResolver()
        val merged = mergeChain(vanilla, listOf(vanilla.replace(level(20), level(20) + level(30)), vanilla.replace(level(20), level(20) + level(35))), resolver)
        assertEquals(3, merged.count("Level()"), merged)
        assertTrue("Xp(35);" in merged && "Xp(30);" !in merged, merged)
        assertEquals(1, resolver.asked.size)
    }

    @Test
    fun blockAddedByAModAndCarriedWithAnotherHeaderByALaterOneIsAsked() {
        val vanilla = main("    Call(\"x\", 1);\n", slots("s1", "1.0", "2.0", "3.0"), slots("s2", "4.0", "5.0", "6.0"))
        val added = vanilla.replace(slots("s2", "4.0", "5.0", "6.0"), slots("s2", "4.0", "5.0", "6.0") + slots("s3", "7.0", "8.0", "9.0"))
        val resolver = TestResolver()
        val merged = mergeChain(vanilla, listOf(added, added.replace("\"s3\", \"lvl\"", "\"s3\", \"none\"")), resolver)
        assertEquals(1, merged.count("Box(\"m\", \"s3\""), merged)
        assertTrue("Box(\"m\", \"s3\", \"none\")" in merged, merged)
        assertEquals(1, resolver.asked.size)
        assertEquals("Box(\"m\", \"s3\", \"lvl\")", resolver.asked[0].baseNode.sourceText)
    }

    @Test
    fun blockAddedByAModAndCarriedWithOneMoreStatementByALaterOneIsNotDuplicated() {
        val vanilla = main("    Call(\"x\", 1);\n", slots("s1", "1.0", "2.0", "3.0"), slots("s2", "4.0", "5.0", "6.0"))
        val added = vanilla.replace(slots("s2", "4.0", "5.0", "6.0"), slots("s2", "4.0", "5.0", "6.0") + slots("s3", "7.0", "8.0", "9.0"))
        val resolver = TestResolver()
        val merged = mergeChain(vanilla, listOf(added, added.replace("Slot(\"c\", 9.0);", "Slot(\"c\", 9.0);\n        Extra(1);")), resolver)
        assertEquals(1, merged.count("Box(\"m\", \"s3\""), merged)
        assertTrue("Extra(1);" in merged, merged)
        assertEquals(0, resolver.asked.size)
    }

    @Test
    fun blockModifiedNextToARemovedOneIsMerged() {
        val newValue = main(box("s1"), box("s2"), box("s3", value = "5.0"))
        val removedAndNewHeader = main(box("s1"), box("s3", "none"))
        val expected = main(box("s1"), box("s2"), box("s3", "none", "5.0"))
        assertEquals(expected, mergeChain(boxes, listOf(newValue, removedAndNewHeader)))
        assertEquals(expected, mergeChain(boxes, listOf(removedAndNewHeader, newValue)))
    }

    @Test
    fun insertedBlockDoesNotMoveLaterChanges() {
        val newValue = main(box("s1"), box("s2"), box("s3", value = "5.0"))
        val inserted = main(box("s1"), box("new"), box("s2"), box("s3"))
        val expected = main(box("s1"), box("new"), box("s2"), box("s3", value = "5.0")).withoutWhitespace()
        assertEquals(expected, mergeChain(boxes, listOf(newValue, inserted)).withoutWhitespace())
        assertEquals(expected, mergeChain(boxes, listOf(inserted, newValue)).withoutWhitespace())
    }

    @Test
    fun differentBlocksAddedByTwoModsAreBothKept() {
        val resolver = TestResolver()
        val merged = mergeChain(
            boxes, listOf(main(box("s1"), box("s2"), box("s3"), box("s4")), main(box("s1"), box("s2"), box("s3"), box("s5"))), resolver
        )
        for (slot in 1..5) assertEquals(1, merged.count("Box(\"m\", \"s$slot\""), merged)
        assertEquals(0, resolver.asked.size)
    }

    @Test
    fun identicalBlocksAddedByTwoModsAreNotDuplicated() {
        val added = main(box("s1"), box("s2"), box("s3"), box("s4"))
        val merged = mergeChain(boxes, listOf(added, added.replace("Slot(\"s1\", 2.0)", "Slot(\"s1\", 3.0)")))
        assertEquals(added.replace("Slot(\"s1\", 2.0)", "Slot(\"s1\", 3.0)").withoutWhitespace(), merged.withoutWhitespace())
    }

    @Test
    fun differentBlocksThatVanillaDoesNotHaveAreBothKept() {
        // vanilla has no Box("m", ...): the first mod adds two and the second mod brings the first one and another
        val plain = "sub main()\n{\n    Call(\"x\", 1);\n}"
        val added = plain.replace("}", box("s1") + box("s2") + "}")
        val other = plain.replace("}", box("s1") + box("s3") + "}")
        val resolver = TestResolver()
        val merged = mergeChain(plain, listOf(added, other), resolver)
        for (slot in 1..3) assertEquals(1, merged.count("Box(\"m\", \"s$slot\""), merged)
        assertEquals(0, resolver.asked.size)
    }

    @Test
    fun blockNameRepeatedOnlyInTheModIsNotDuplicated() {
        // the signature of the only Box of vanilla must not depend on the mod having two
        val single = "sub main()\n{\n" + box("s1") + "    Call(\"x\", 1);\n}"
        val another = single.replace("    Call(", "    Box(\"n\", \"t1\", \"lvl\")\n    {\n        Slot(\"a\", 1.0);\n    }\n    Call(")
        val otherChange = single.replace("Call(\"x\", 1)", "Call(\"x\", 2)").replace("Slot(\"a\", 1.0)", "Slot(\"a\", 7.0)")
        for (merged in listOf(mergeChain(single, listOf(another, otherChange)), mergeChain(single, listOf(otherChange, another)))) {
            assertEquals(1, merged.count("Box(\"m\""), merged)
            assertEquals(1, merged.count("Box(\"n\""), merged)
            assertTrue("Slot(\"a\", 7.0)" in merged && "Call(\"x\", 2)" in merged, merged)
        }
    }

    @Test
    fun blocksWithoutArgumentsAreMatchedByTheirContent() {
        fun segment(tag: String, height: String) = "    Segment()\n    {\n        Height($height);\n        Tag(\"$tag\");\n    }\n"
        val vanilla = main(segment("g1", "1.0"), segment("g2", "2.0"), segment("g3", "3.0"))
        val mod = main(segment("g2", "9.0"), segment("g3", "3.0"))
        assertEquals(vanilla.replace("Height(2.0)", "Height(9.0)"), mergeChain(vanilla, listOf(mod)))
    }

    @Test
    fun blockChangedDeepInsideIsToldApartFromANeighbourThatSharesAWholeChild() {
        // as in Dying Light 1's action_patrol.scr: the second and third Variation() have the same Group("Talk")
        fun variation(talk: String, line: String, pause: String = "0.5") =
            "    Variation()\n    {\n        Group(\"Talk\")\n        {\n            Say(\"$talk\");\n        }\n" +
                "        Group(\"Attracted\")\n        {\n            Say(\"$line\");\n            Pause($pause);\n        }\n    }\n"
        val vanilla = main(variation("a", "one"), variation("b", "two"), variation("b", "three"))
        val mod = main(variation("a", "one"), variation("b", "three", "0.9"))
        assertEquals(main(variation("a", "one"), variation("b", "two"), variation("b", "three", "0.9")), mergeChain(vanilla, listOf(mod)))
    }

    @Test
    fun blockWhoseOnlyDistinctStatementChangedIsToldApartByTheRestOfItsArguments() {
        // as in Dying Light 1's bountiesdaily.scr: only Center(...) tells these blocks apart, and the mod changes it
        fun sphere(x: String, z: String = "9.0") = "    VolumeSphere()\n    {\n        Radius(0.15);\n        Center($x, 5.0, $z);\n    }\n"
        val vanilla = main(sphere("1.0"), sphere("2.0"), sphere("3.0"))
        val mod = main(sphere("1.0"), sphere("3.0", "9.5"))
        assertEquals(main(sphere("1.0"), sphere("2.0"), sphere("3.0", "9.5")), mergeChain(vanilla, listOf(mod)))
    }

    @Test
    fun withoutVanillaTheDifferenceBetweenTwoVersionsIsAsked() {
        // the file is not in the game: the repeated blocks of the two mods are matched with each other
        val resolver = TestResolver()
        val second = main(box("s1"), box("s2", "none"), box("s3"))
        val merged = mergeChain("sub other()\n{\n}", listOf(boxes, second), resolver, vanillaEntry = "scripts/inventory/other.loot")
        assertEquals(second, merged)
        assertEquals(1, resolver.asked.size)
        assertEquals("Box(\"m\", \"s2\", \"lvl\")", resolver.asked[0].baseNode.sourceText)
    }
}
