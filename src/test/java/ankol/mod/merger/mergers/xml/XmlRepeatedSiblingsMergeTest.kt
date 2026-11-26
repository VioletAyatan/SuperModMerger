package ankol.mod.merger.mergers.xml

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
 * Dying Light 1 writes values as <prop n="Name" v="Value"/> and repeats element signatures among siblings
 * (default_levels.xml has 30 top-level <level id="1">). Siblings with the same signature are paired by their
 * position while they can be read as changed in place, and by their content when one was added, removed or
 * replaced (see RepeatedSiblingAligner).
 */
class XmlRepeatedSiblingsMergeTest {
    private val relPath = "scripts/skills/levels_test.xml"
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

    private fun newContext(vanillaEntry: String, vanilla: String, resolver: TestResolver): MergeContext {
        val sourceDir = Files.createTempDirectory("smm_xml_repeated_test_")
        tempDirs.add(sourceDir)
        ZipOutputStream(Files.newOutputStream(sourceDir.resolve("data0.pak"))).use { zip ->
            zip.putNextEntry(ZipEntry(vanillaEntry))
            zip.write(vanilla.toByteArray())
            zip.closeEntry()
        }
        val baseModManager = BaseModManager(sourceDir)
        managers.add(baseModManager)
        val policy = MergeRules.buildPolicy(listOf("test" to MergeRules.parse("{}", "test")))
        return MergeContext(baseModManager, resolver, deletionPolicy = policy)
    }

    private fun merge(context: MergeContext, accumulatedName: String, accumulated: String, modName: String, mod: String, first: Boolean): String {
        context.configure(relPath, accumulatedName, modName, first)
        return TechlandXmlFileMerger(context).merge(
            InMemoryArchiveFile("levels_test.xml", relPath, mutableListOf(accumulatedName), accumulated),
            InMemoryArchiveFile("levels_test.xml", relPath, mutableListOf(modName), mod),
        ).mergedContent
    }

    /** Same order as FileMergerEngine: vanilla + mod1, then each following mod */
    private fun mergeChain(vanilla: String, mods: List<String>, resolver: TestResolver = TestResolver()): String {
        val context = newContext(relPath, vanilla, resolver)
        var accumulated = vanilla
        var accumulatedName = "data0.pak"
        mods.forEachIndexed { index, modContent ->
            val modName = "mod${index + 1}.pak"
            accumulated = merge(context, accumulatedName, accumulated, modName, modContent, index == 0)
            accumulatedName = modName
        }
        return accumulated
    }

    private fun String.withoutWhitespace() = filterNot(Char::isWhitespace)

    private fun String.count(part: String) = split(part).size - 1

    private fun level(points: Int) = "    <level id=\"1\">\n        <prop n=\"SkillPoints\" v=\"$points\"/>\n    </level>\n"

    private val vanilla = """
        <skills>
            <level id="1">
                <prop n="SkillPoints" v="1"/>
            </level>
            <level id="1">
                <prop n="SkillPoints" v="2"/>
            </level>
            <level id="1">
                <prop n="SkillPoints" v="3"/>
            </level>
            <prop n="Speed" v="1.0"/>
            <prop n="Jump" v="1.0"/>
        </skills>
    """.trimIndent()

    private val fasterSpeed = vanilla.replace("<prop n=\"Speed\" v=\"1.0\"/>", "<prop n=\"Speed\" v=\"2.0\"/>")

    @Test
    fun propsAreMatchedByN() {
        val higherJump = vanilla.replace("<prop n=\"Jump\" v=\"1.0\"/>", "<prop n=\"Jump\" v=\"3.0\"/>")
        val expected = fasterSpeed.replace("<prop n=\"Jump\" v=\"1.0\"/>", "<prop n=\"Jump\" v=\"3.0\"/>")
        assertEquals(expected, mergeChain(vanilla, listOf(fasterSpeed, higherJump)))
        assertEquals(expected, mergeChain(vanilla, listOf(higherJump, fasterSpeed)))
    }

    @Test
    fun changeInTheSecondOfRepeatedSiblingsIsMerged() {
        val secondLevel = vanilla.replace("<prop n=\"SkillPoints\" v=\"2\"/>", "<prop n=\"SkillPoints\" v=\"20\"/>")
        val expected = fasterSpeed.replace("<prop n=\"SkillPoints\" v=\"2\"/>", "<prop n=\"SkillPoints\" v=\"20\"/>")
        assertEquals(expected, mergeChain(vanilla, listOf(secondLevel, fasterSpeed)))
        assertEquals(expected, mergeChain(vanilla, listOf(fasterSpeed, secondLevel)))
    }

    @Test
    fun insertedRepeatedSiblingIsAddedAndLaterChangesStayInPlace() {
        val inserted = vanilla.replace(level(2), level(9) + level(2))
        val thirdLevel = vanilla.replace("<prop n=\"SkillPoints\" v=\"3\"/>", "<prop n=\"SkillPoints\" v=\"30\"/>")
        val expected = inserted.replace("<prop n=\"SkillPoints\" v=\"3\"/>", "<prop n=\"SkillPoints\" v=\"30\"/>")
        assertTrue(inserted.count("<level") == 4 && thirdLevel != vanilla)
        for (merged in listOf(mergeChain(vanilla, listOf(inserted, thirdLevel)), mergeChain(vanilla, listOf(thirdLevel, inserted)))) {
            assertEquals(expected.withoutWhitespace(), merged.withoutWhitespace())
        }
    }

    @Test
    fun sameNumberOfSiblingsIsPairedByPosition() {
        // nothing was added or removed: each level is the same one, even if it becomes what its neighbour was
        val shifted = vanilla.replace("v=\"3\"/>", "v=\"4\"/>").replace("v=\"2\"/>", "v=\"3\"/>").replace("v=\"1\"/>", "v=\"2\"/>")
        assertTrue(shifted.count("<level") == 3 && "v=\"1\"/>" !in shifted)
        assertEquals(shifted, mergeChain(vanilla, listOf(shifted)))
        val expected = shifted.replace("<prop n=\"Speed\" v=\"1.0\"/>", "<prop n=\"Speed\" v=\"2.0\"/>")
        assertEquals(expected, mergeChain(vanilla, listOf(shifted, fasterSpeed)))
        assertEquals(expected, mergeChain(vanilla, listOf(fasterSpeed, shifted)))
    }

    @Test
    fun changeNextToARemovedSiblingLandsOnTheRightOne() {
        val thirdLevel = vanilla.replace("<prop n=\"SkillPoints\" v=\"3\"/>", "<prop n=\"SkillPoints\" v=\"30\"/>")
        val withoutSecond = vanilla.replace(level(2), "")
        assertTrue(withoutSecond.count("<level") == 2)
        assertEquals(thirdLevel, mergeChain(vanilla, listOf(thirdLevel, withoutSecond)))
        assertEquals(thirdLevel, mergeChain(vanilla, listOf(withoutSecond, thirdLevel)))
    }

    @Test
    fun siblingChangedByTheModThatRemovedItsNeighbourIsRecognisedByTheRestOfItsContent() {
        fun level(points: Int) =
            "    <level id=\"1\">\n        <prop n=\"SkillPoints\" v=\"$points\"/>\n        <prop n=\"Bonus\" v=\"b$points\"/>\n    </level>\n"
        val vanilla = "<skills>\n" + level(1) + level(2) + level(3) + "</skills>"
        val mod = "<skills>\n" + level(1) + level(3).replace("v=\"3\"", "v=\"30\"") + "</skills>"
        assertEquals(vanilla.replace("v=\"3\"", "v=\"30\""), mergeChain(vanilla, listOf(mod)))
    }

    @Test
    fun siblingChangedDeepInsideIsToldApartFromANeighbourThatSharesAWholeChild() {
        fun level(talk: String, line: String, pause: String = "1") =
            "    <level id=\"1\">\n        <group n=\"Talk\">\n            <prop n=\"Say\" v=\"$talk\"/>\n        </group>\n" +
                "        <group n=\"Attracted\">\n            <prop n=\"Say\" v=\"$line\"/>\n            <prop n=\"Pause\" v=\"$pause\"/>\n        </group>\n    </level>\n"
        val vanilla = "<skills>\n" + level("a", "one") + level("b", "two") + level("b", "three") + "</skills>"
        val mod = "<skills>\n" + level("a", "one") + level("b", "three", "9") + "</skills>"
        assertEquals("<skills>\n" + level("a", "one") + level("b", "two") + level("b", "three", "9") + "</skills>", mergeChain(vanilla, listOf(mod)))
    }

    @Test
    fun siblingWhoseOnlyDistinctChildChangedIsToldApartByTheRestOfItsAttributes() {
        fun level(x: String, z: String = "9") =
            "    <level id=\"1\">\n        <prop n=\"Radius\" v=\"0.15\"/>\n        <prop n=\"Center\" x=\"$x\" y=\"5\" z=\"$z\"/>\n    </level>\n"
        val vanilla = "<skills>\n" + level("1") + level("2") + level("3") + "</skills>"
        val mod = "<skills>\n" + level("1") + level("3", "9.5") + "</skills>"
        assertEquals("<skills>\n" + level("1") + level("2") + level("3", "9.5") + "</skills>", mergeChain(vanilla, listOf(mod)))
    }

    @Test
    fun siblingWhoseWholeContentChangedNextToARemovedOneIsAmbiguous() {
        // the mod may have removed the second level and changed the third, or removed the third and changed the second
        val mod = vanilla.replace(level(2), "").replace("<prop n=\"SkillPoints\" v=\"3\"/>", "<prop n=\"SkillPoints\" v=\"30\"/>")
        val merged = mergeChain(vanilla, listOf(mod))
        assertTrue(merged == vanilla.replace("v=\"3\"/>", "v=\"30\"/>") || merged == vanilla.replace("v=\"2\"/>", "v=\"30\"/>"), merged)
    }

    @Test
    fun newRepeatedSiblingsAreAllAdded() {
        val twoNew = fasterSpeed.replace(
            "</skills>",
            "    <item id=\"x\">\n        <prop n=\"A\" v=\"1\"/>\n    </item>\n    <item id=\"x\">\n        <prop n=\"A\" v=\"2\"/>\n    </item>\n</skills>"
        )
        val merged = mergeChain(vanilla, listOf(twoNew))
        assertTrue("<prop n=\"A\" v=\"1\"/>" in merged, merged)
        assertTrue("<prop n=\"A\" v=\"2\"/>" in merged, merged)
        assertTrue(merged.indexOf("<prop n=\"A\" v=\"1\"/>") < merged.indexOf("<prop n=\"A\" v=\"2\"/>"), merged)
        assertTrue("<prop n=\"Speed\" v=\"2.0\"/>" in merged, merged)
    }

    private fun typed(type: String, prop: String, value: String) =
        "    <level id=\"1\" type=\"$type\">\n        <prop n=\"$prop\" v=\"$value\"/>\n    </level>\n"

    private val typedLevels = "<skills>\n" + typed("Fighter", "X", "1") + typed("Runner", "X", "2") + typed("Status", "X", "3") + "</skills>"

    @Test
    fun swappedSiblingsWithOtherAttributesAreNotMixed() {
        // an element with children keeps its own attributes: written over each other, each would get the other's children
        val swapped = "<skills>\n" + typed("Runner", "X", "2") + typed("Fighter", "X", "1") + typed("Status", "X", "3") + "</skills>"
        assertEquals(typedLevels, mergeChain(typedLevels, listOf(swapped)))
    }

    @Test
    fun siblingRemovedAndAnotherAddedDoNotMixTheOnesBetweenThem() {
        // as in default_levels.xml: the same number of <level id="1">, but not the same ones
        val vanilla = "<skills>\n" + typed("Status", "Penalty", "-60") + typed("Status", "Reward", "0.4") + typed("Runner", "Fitness", "1.5") +
            typed("Driver", "Points", "0") + "</skills>"
        val mod = vanilla.replace(typed("Status", "Penalty", "-60"), "").replace("</skills>", typed("Status", "New", "1") + "</skills>")
        assertTrue(mod.count("<level") == 4)
        val added = vanilla.replace("</skills>", typed("Status", "New", "1") + "</skills>")
        assertEquals(added.withoutWhitespace(), mergeChain(vanilla, listOf(mod)).withoutWhitespace())
        // another mod's change lands on the same level in both orders
        val fitter = vanilla.replace("v=\"1.5\"", "v=\"2.5\"")
        val expected = added.replace("v=\"1.5\"", "v=\"2.5\"").withoutWhitespace()
        assertEquals(expected, mergeChain(vanilla, listOf(mod, fitter)).withoutWhitespace())
        assertEquals(expected, mergeChain(vanilla, listOf(fitter, mod)).withoutWhitespace())
    }

    @Test
    fun copyAddedRightAfterASiblingIsNotTakenForIt() {
        fun level(a: Int, b: Int, c: Int, d: Int) = "    <level id=\"1\">\n        <prop n=\"A\" v=\"$a\"/>\n        <prop n=\"B\" v=\"$b\"/>\n" +
            "        <prop n=\"C\" v=\"$c\"/>\n        <prop n=\"D\" v=\"$d\"/>\n    </level>\n"
        fun skills(vararg levels: String) = "<skills>\n" + levels.joinToString("") + "</skills>"
        val first = level(1, 10, 100, 1000)
        val third = level(3, 30, 300, 3000)
        val vanilla = skills(first, level(2, 20, 200, 2000), third)
        // one mod changes a value of the second level; the other changes two more and adds a copy of what it was
        val one = skills(first, level(2, 20, 200, 2001), third)
        val other = skills(first, level(2, 21, 201, 2000), level(9, 20, 200, 2000), third)
        val expected = skills(first, level(2, 21, 201, 2001), level(9, 20, 200, 2000), third).withoutWhitespace()
        assertEquals(expected, mergeChain(vanilla, listOf(one, other)).withoutWhitespace())
        assertEquals(expected, mergeChain(vanilla, listOf(other, one)).withoutWhitespace())
    }

    @Test
    fun differentSiblingsAddedByTwoModsAreBothKept() {
        fun level(prop: String) = "    <level id=\"1\">\n        <prop n=\"$prop\" v=\"1\"/>\n    </level>\n"
        val resolver = TestResolver()
        val merged = mergeChain(
            vanilla, listOf(vanilla.replace(level(3), level(3) + level("Bonus")), vanilla.replace(level(3), level(3) + level("Extra"))), resolver
        )
        assertTrue("<prop n=\"Bonus\" v=\"1\"/>" in merged && "<prop n=\"Extra\" v=\"1\"/>" in merged, merged)
        assertEquals(5, merged.count("<level"), merged)
        assertEquals(0, resolver.asked.size)
    }

    @Test
    fun siblingAddedByTwoModsInTheSamePlaceWithAnotherValueIsAsked() {
        // both add a fourth level: with 4 points in one mod and 5 in the other
        val resolver = TestResolver()
        val merged = mergeChain(
            vanilla, listOf(vanilla.replace(level(3), level(3) + level(4)), vanilla.replace(level(3), level(3) + level(5))), resolver
        )
        assertEquals(4, merged.count("<level"), merged)
        assertTrue("v=\"5\"/>" in merged && "v=\"4\"/>" !in merged, merged)
        assertEquals(1, resolver.asked.size)
    }

    @Test
    fun identicalSiblingsAddedByTwoModsAreNotDuplicated() {
        val added = vanilla.replace(level(3), level(3) + level(4))
        val merged = mergeChain(vanilla, listOf(added, added.replace("<prop n=\"Speed\" v=\"1.0\"/>", "<prop n=\"Speed\" v=\"2.0\"/>")))
        assertEquals(fasterSpeed.replace(level(3), level(3) + level(4)).withoutWhitespace(), merged.withoutWhitespace())
    }

    @Test
    fun differentSiblingsThatVanillaDoesNotHaveAreBothKept() {
        // vanilla has no <item id="x">: the first mod adds two and the second mod brings the first one and another
        val added = vanilla.replace("</skills>", item("1", "2", "3") + item("4", "5", "6") + "</skills>")
        val other = added.replace(item("4", "5", "6"), item("7", "8", "9"))
        val resolver = TestResolver()
        val merged = mergeChain(vanilla, listOf(added, other), resolver)
        for (value in listOf("1", "4", "7")) assertEquals(1, merged.count("<item id=\"x\" a=\"$value\""), merged)
        assertEquals(3, merged.count("<item"), merged)
        assertEquals(0, resolver.asked.size)
    }

    private fun item(a: String, b: String, c: String) = "    <item id=\"x\" a=\"$a\" b=\"$b\" c=\"$c\"/>\n"

    @Test
    fun siblingAddedByAModAndChangedByALaterOneIsAsked() {
        // vanilla has no <item id="x">: the second mod carries the ones of the first with a difference
        val added = vanilla.replace("</skills>", item("1", "2", "3") + item("4", "5", "6") + "</skills>")
        val resolver = TestResolver()
        val merged = mergeChain(vanilla, listOf(added, added.replace("c=\"6\"", "c=\"7\"")), resolver)
        assertEquals(2, merged.count("<item"), merged)
        assertTrue("c=\"3\"" in merged && "c=\"7\"" in merged, merged)
        assertEquals(1, resolver.asked.size)
    }

    @Test
    fun siblingWithFewAttributesAddedByAModAndChangedByALaterOneIsAsked() {
        val added = vanilla.replace("</skills>", "    <item id=\"x\" v=\"3\" w=\"1\"/>\n</skills>")
        val resolver = TestResolver()
        val merged = mergeChain(vanilla, listOf(added, added.replace("v=\"3\"", "v=\"4\"")), resolver)
        assertEquals(1, merged.count("<item"), merged)
        assertTrue("v=\"4\"" in merged, merged)
        assertEquals(1, resolver.asked.size)
    }

    @Test
    fun siblingWithItsAttributesInAnotherOrderIsNotDuplicated() {
        val added = vanilla.replace("</skills>", item("1", "2", "3") + item("4", "5", "6") + "</skills>")
        val reordered = added.replace(item("4", "5", "6"), "    <item b=\"5\" id=\"x\" c=\"6\" a=\"4\"/>\n")
        assertTrue(reordered != added)
        val resolver = TestResolver()
        assertEquals(added.withoutWhitespace(), mergeChain(vanilla, listOf(added, reordered), resolver).withoutWhitespace())
        assertEquals(0, resolver.asked.size)
    }

    @Test
    fun withoutVanillaTheDifferenceBetweenTwoVersionsIsAsked() {
        // the file is not in the game: the repeated siblings of the two mods are matched with each other
        val resolver = TestResolver()
        val context = newContext("scripts/skills/other.xml", "<other/>", resolver)
        val second = vanilla.replace("<prop n=\"SkillPoints\" v=\"2\"/>", "<prop n=\"SkillPoints\" v=\"20\"/>")
        val merged = merge(context, "mod1.pak", vanilla, "mod2.pak", second, false)
        assertEquals(second, merged)
        assertEquals(1, resolver.asked.size)
        assertEquals("<prop n=\"SkillPoints\" v=\"2\"/>", resolver.asked[0].baseNode.sourceText)
    }
}
