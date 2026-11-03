package ankol.mod.merger.mergers.scr

import ankol.mod.merger.api.ConflictResolver
import ankol.mod.merger.constants.UserChoice
import ankol.mod.merger.core.BaseModManager
import ankol.mod.merger.core.filetrees.MemoryFileTree
import ankol.mod.merger.domain.MergeContext
import ankol.mod.merger.mergers.ConflictRecord
import ankol.mod.merger.mergers.DeletionRecord
import ankol.mod.merger.rules.MergeRules
import ankol.mod.merger.tools.Tools
import org.junit.After
import org.junit.Test
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * The arguments in the header of a function block, e.g. the "Hard" in
 * MeshLootType("furniture_chest.msh", ..., "Hard", false, Color_Violet) { ... } (Dying Light 1, containers.loot),
 * must be merged like the arguments of a function call.
 */
class ScrBlockHeaderMergeTest {
    private val relPath = listOf("scripts", "inventory", "containers_test.loot").joinToString(File.separator)
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
    private fun mergeChain(vanilla: String, mods: List<String>, resolver: TestResolver = TestResolver()): String {
        val sourceDir = Files.createTempDirectory("smm_block_header_test_")
        tempDirs.add(sourceDir)
        ZipOutputStream(Files.newOutputStream(sourceDir.resolve("data0.pak"))).use { zip ->
            zip.putNextEntry(ZipEntry("scripts/inventory/containers_test.loot"))
            zip.write(vanilla.toByteArray())
            zip.closeEntry()
        }
        val baseModManager = BaseModManager(sourceDir)
        managers.add(baseModManager)
        val policy = MergeRules.buildPolicy(listOf("test" to MergeRules.parse("{}", "test")))
        val context = MergeContext(baseModManager, resolver, deletionPolicy = policy)

        var accumulated = vanilla
        var accumulatedName = "data0.pak"
        mods.forEachIndexed { index, modContent ->
            val modName = "mod${index + 1}.pak"
            context.configure(relPath, accumulatedName, modName, index == 0)
            val result = TechlandScrFileMerger(context).merge(
                MemoryFileTree("containers_test.loot", relPath, mutableListOf(accumulatedName), accumulated),
                MemoryFileTree("containers_test.loot", relPath, mutableListOf(modName), modContent),
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
}
