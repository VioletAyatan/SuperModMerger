package ankol.mod.merger.mergers.scr

import ankol.mod.merger.api.ConflictResolver
import ankol.mod.merger.constants.UserChoice
import ankol.mod.merger.core.BaseModManager
import ankol.mod.merger.core.filetrees.MemoryFileTree
import ankol.mod.merger.domain.MergeContext
import ankol.mod.merger.mergers.ConflictRecord
import ankol.mod.merger.mergers.DeletionRecord
import ankol.mod.merger.rules.DeletionPolicy
import ankol.mod.merger.rules.MergeRules
import ankol.mod.merger.rules.RuleAction
import ankol.mod.merger.tools.Tools
import org.junit.After
import org.junit.Test
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ScrDeletionMergeTest {
    // 与运行时一致：MOD 解压后的相对路径使用系统分隔符
    private val relPath = listOf("scripts", "inventory", "inventory_test.scr").joinToString(File.separator)
    private val tempDirs = mutableListOf<Path>()
    private val managers = mutableListOf<BaseModManager>()

    /** 冲突一律采用 MOD；删除询问按 [deletionChoice] 回答，并记录被询问的条数 */
    private class TestResolver(private val deletionChoice: UserChoice) : ConflictResolver {
        var askedDeletions = 0

        override fun resolveConflict(conflicts: MutableList<ConflictRecord>) {
            conflicts.forEach { it.userChoice = UserChoice.MERGE_MOD }
        }

        override fun resolveDeletionConflicts(deletions: MutableList<DeletionRecord>) {
            askedDeletions += deletions.size
            deletions.forEach { it.userChoice = deletionChoice }
        }
    }

    @After
    fun cleanup() {
        managers.forEach { it.close() }
        // BaseModManager 关闭后底层 RandomAccessFile 仍可能占用 data0.pak，清理失败不影响测试结果
        tempDirs.forEach { runCatching { Tools.deleteRecursively(it) } }
    }

    private fun policyOf(json: String): DeletionPolicy =
        MergeRules.buildPolicy(listOf("test" to MergeRules.parse(json, "test")))

    /**
     * 模拟 FileMergerEngine 的顺序合并：原版 + mod1，然后依次合并后续 MOD。
     */
    private fun mergeChain(
        vanilla: String,
        mods: List<String>,
        policy: DeletionPolicy,
        resolver: ConflictResolver = TestResolver(UserChoice.MERGE_MOD),
    ): Pair<String, MergeContext> {
        val sourceDir = Files.createTempDirectory("smm_deletion_test_")
        tempDirs.add(sourceDir)
        ZipOutputStream(Files.newOutputStream(sourceDir.resolve("data0.pak"))).use { zip ->
            zip.putNextEntry(ZipEntry("scripts/inventory/inventory_test.scr"))
            zip.write(vanilla.toByteArray())
            zip.closeEntry()
        }
        val baseModManager = BaseModManager(sourceDir)
        managers.add(baseModManager)
        val context = MergeContext(baseModManager, resolver, deletionPolicy = policy)

        var accumulated = vanilla
        var accumulatedName = "data0.pak"
        mods.forEachIndexed { index, modContent ->
            val modName = "mod${index + 1}.pak"
            context.configure(relPath, accumulatedName, modName, index == 0)
            val merger = TechlandScrFileMerger(context)
            val result = merger.merge(
                MemoryFileTree("inventory_test.scr", relPath, mutableListOf(accumulatedName), accumulated),
                MemoryFileTree("inventory_test.scr", relPath, mutableListOf(modName), modContent),
            )
            accumulated = result.mergedContent
            accumulatedName = modName
        }
        return accumulated to context
    }

    private val vanilla = """
        sub main()
        {
            Version(1);
            Item("Knife", CategoryType_Melee)
            {
                Name("&Knife&");
                Damage(10);
                Price(5);
            }
            Dlc("dlc_a");
            Dlc("dlc_b");
            Dlc("dlc_c");
        }
    """.trimIndent()

    @Test
    fun missingNodeIsRestoredByDefault() {
        val mod = vanilla.replace("        Price(5);\n", "").replace("Damage(10)", "Damage(20)")
        val (merged, context) = mergeChain(vanilla, listOf(mod), policyOf("{}"))

        assertTrue("Price(5);" in merged, merged)
        assertTrue("Damage(20);" in merged, merged)
        assertEquals(0, context.deletedNodeCount)
        assertEquals(1, context.restoredNodeCount)
    }

    @Test
    fun deleteRuleRemovesNodeWithoutLeavingBlankLine() {
        val mod = vanilla.replace("        Price(5);\n", "")
        val policy = policyOf("""{"scr":{"deletion":{"rules":[{"file":"scripts/**/inventory_*.scr","nodeType":"funCall","name":"price","action":"DELETE"}]}}}""")
        val (merged, context) = mergeChain(vanilla, listOf(mod), policy)

        assertEquals(mod, merged)
        assertEquals(1, context.deletedNodeCount)
    }

    @Test
    fun deletingTheMiddleRepeatedCallDeletesTheRightOne() {
        val mod = vanilla.replace("    Dlc(\"dlc_b\");\n", "")
        val (merged, _) = mergeChain(vanilla, listOf(mod), MergeRules.builtinPolicy)

        assertTrue("Dlc(\"dlc_a\");" in merged, merged)
        assertFalse("Dlc(\"dlc_b\");" in merged, merged)
        assertTrue("Dlc(\"dlc_c\");" in merged, merged)
        assertEquals(mod, merged)
    }

    @Test
    fun modifyingARepeatedCallIsStillAModification() {
        val mod = vanilla.replace("Dlc(\"dlc_b\")", "Dlc(\"dlc_x\")")
        val (merged, context) = mergeChain(vanilla, listOf(mod), policyOf("{}"))

        assertEquals(mod, merged)
        assertEquals(0, context.restoredNodeCount)
    }

    @Test
    fun askRuleUsesTheUserChoice() {
        val mod = vanilla.replace("        Price(5);\n", "")
        val policy = policyOf("""{"scr":{"deletion":{"defaultAction":"ASK"}}}""")

        val deleteResolver = TestResolver(UserChoice.MERGE_MOD)
        val (deleted, _) = mergeChain(vanilla, listOf(mod), policy, deleteResolver)
        assertEquals(mod, deleted)
        assertEquals(1, deleteResolver.askedDeletions)

        val keepResolver = TestResolver(UserChoice.BASE_MOD)
        val (kept, context) = mergeChain(vanilla, listOf(mod), policy, keepResolver)
        assertEquals(vanilla, kept)
        assertEquals(1, keepResolver.askedDeletions)
        assertEquals(1, context.restoredNodeCount)
    }

    @Test
    fun laterModDoesNotRestoreADeletedNode() {
        val mod1 = vanilla.replace("    Dlc(\"dlc_b\");\n", "")
        val mod2 = vanilla.replace("Damage(10)", "Damage(30)")
        val (merged, _) = mergeChain(vanilla, listOf(mod1, mod2), MergeRules.builtinPolicy)

        assertFalse("Dlc(\"dlc_b\");" in merged, merged)
        assertTrue("Damage(30);" in merged, merged)
        assertTrue("Dlc(\"dlc_a\");" in merged && "Dlc(\"dlc_c\");" in merged, merged)
    }

    @Test
    fun nodesAddedByEarlierModsAreNeverTreatedAsDeletions() {
        val mod1 = vanilla.replace("    Dlc(\"dlc_c\");\n", "    Dlc(\"dlc_c\");\n    Dlc(\"dlc_new\");\n    Speed(3);\n")
        val mod2 = vanilla.replace("Damage(10)", "Damage(30)")
        val (merged, context) = mergeChain(vanilla, listOf(mod1, mod2), policyOf("""{"scr":{"deletion":{"defaultAction":"DELETE"}}}"""))

        assertTrue("Dlc(\"dlc_new\");" in merged, merged)
        assertTrue("Speed(3);" in merged, merged)
        assertTrue("Damage(30);" in merged, merged)
        assertEquals(0, context.deletedNodeCount)
    }

    @Test
    fun deletingAContainerAndInsertingNextToItKeepsTheInsertion() {
        // Item 块被删除，同一位置插入了新节点 Speed(3)：新节点的插入点正好是被删除节点前面的空白 token
        val mod = vanilla.replace(
            Regex("""    Item\("Knife".*?\n    }\n""", RegexOption.DOT_MATCHES_ALL),
            "    Speed(3);\n"
        )
        val policy = policyOf("""{"scr":{"deletion":{"rules":[{"file":"**","nodeType":"funBlock","name":"Item","action":"DELETE"}]}}}""")
        val (merged, context) = mergeChain(vanilla, listOf(mod), policy)

        assertFalse("Knife" in merged, merged)
        assertTrue("Speed(3);" in merged, merged)
        assertTrue("Version(1);" in merged && "Dlc(\"dlc_a\");" in merged, merged)
        assertEquals(1, context.deletedNodeCount)
    }
}
