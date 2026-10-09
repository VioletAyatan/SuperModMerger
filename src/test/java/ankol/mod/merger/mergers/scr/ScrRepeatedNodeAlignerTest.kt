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
 * 重复函数调用对齐的稳定性测试：覆盖确定性和几类已知的边界情况。
 */
class ScrRepeatedNodeAlignerTest {
    private val relPath = listOf("scripts", "inventory", "inventory_test.scr").joinToString(File.separator)
    private val tempDirs = mutableListOf<Path>()
    private val managers = mutableListOf<BaseModManager>()

    private object AcceptModResolver : ConflictResolver {
        override fun resolveConflict(conflicts: MutableList<ConflictRecord>) {
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

    private fun policyOf(json: String): DeletionPolicy =
        MergeRules.buildPolicy(listOf("test" to MergeRules.parse(json, "test")))

    private fun body(vararg statements: String): String =
        "sub main()\n{\n" + statements.joinToString("") { "    $it;\n" } + "}"

    private fun dlc(vararg args: String): Array<String> = args.map { "Dlc(\"$it\")" }.toTypedArray()

    /** 合并结果中 Dlc 调用的参数序列，忽略格式差异 */
    private fun dlcArgs(content: String): List<String> =
        Regex("""Dlc\("([^"]*)"\)""").findAll(content).map { it.groupValues[1] }.toList()

    private fun mergeChain(vanilla: String, mods: List<String>, policy: DeletionPolicy): String {
        val sourceDir = Files.createTempDirectory("smm_aligner_test_")
        tempDirs.add(sourceDir)
        ZipOutputStream(Files.newOutputStream(sourceDir.resolve("data0.pak"))).use { zip ->
            zip.putNextEntry(ZipEntry("scripts/inventory/inventory_test.scr"))
            zip.write(vanilla.toByteArray())
            zip.closeEntry()
        }
        val baseModManager = BaseModManager(sourceDir)
        managers.add(baseModManager)
        val context = MergeContext(baseModManager, AcceptModResolver, deletionPolicy = policy)

        var accumulated = vanilla
        var accumulatedName = "data0.pak"
        mods.forEachIndexed { index, modContent ->
            val modName = "mod${index + 1}.pak"
            context.configure(relPath, accumulatedName, modName, index == 0)
            accumulated = TechlandScrFileMerger(context).merge(
                MemoryFileTree("inventory_test.scr", relPath, mutableListOf(accumulatedName), accumulated),
                MemoryFileTree("inventory_test.scr", relPath, mutableListOf(modName), modContent),
            ).mergedContent
            accumulatedName = modName
        }
        return accumulated
    }

    /** 同一输入多次合并，结果必须完全一致 */
    @Test
    fun alignmentIsDeterministic() {
        val vanilla = body(*dlc("a", "b", "c", "a", "b", "c"))
        val mod = body(*dlc("a", "x", "c", "b", "y"))
        val results = (1..5).map { mergeChain(vanilla, listOf(mod), policyOf("{}")) }
        println("[deterministic] result=${dlcArgs(results.first())}")
        assertEquals(1, results.toSet().size, "same input produced different results: $results")
    }

    /** 情况 1：把 b 改成已存在的值 a，应该识别为修改，结果与 MOD 一致 */
    @Test
    fun modifyingToAnExistingValueIsAModification() {
        val vanilla = body(*dlc("a", "b", "a"))
        val mod = body(*dlc("a", "a", "a"))
        val merged = mergeChain(vanilla, listOf(mod), policyOf("{}"))
        println("[modify-to-existing] result=${dlcArgs(merged)}")
        assertEquals(listOf("a", "a", "a"), dlcArgs(merged))
    }

    /** 情况 2：同一段里同时有删除和修改，本身有歧义；记录当前选择的理解方式 */
    @Test
    fun ambiguousDeleteAndModifyInTheSameGap() {
        val vanilla = body(*dlc("a", "b", "c"))
        val mod = body(*dlc("a", "x"))
        val merged = mergeChain(vanilla, listOf(mod), policyOf("{}"))
        val args = dlcArgs(merged)
        println("[ambiguous] result=$args")
        // b->x + 补回 c，或 c->x + 补回 b，两种理解都算「合理」
        assertTrue(args == listOf("a", "x", "c") || args == listOf("a", "b", "x"), "unexpected: $args")
    }

    /** 情况 3：前一个 MOD 打乱了这组调用（改值 + 插入），后一个 MOD 只带原版内容，不应覆盖前一个 MOD 的改动 */
    @Test
    fun earlierModReorderingIsKept() {
        val vanilla = body(*dlc("a", "b"))
        val mod1 = body(*dlc("a", "y", "x")) // 新增 y，b 改成 x
        val mod2 = body(*dlc("a", "b"))      // 与原版一致
        val afterMod1 = dlcArgs(mergeChain(vanilla, listOf(mod1), policyOf("{}")))
        val merged = dlcArgs(mergeChain(vanilla, listOf(mod1, mod2), policyOf("{}")))
        println("[reorder] afterMod1=$afterMod1 final=$merged")
        assertEquals(afterMod1, merged)
    }

    /** 情况 4：mod1 删掉两个相同 Dlc("a") 中的一个，mod2 在原版基础上新增一个 Dlc("a")，应剩 2 个 */
    @Test
    fun identicalArgumentsDeletionDoesNotBlockNewInsertions() {
        val vanilla = body(*dlc("a", "a"))
        val mod1 = body(*dlc("a"))
        val mod2 = body(*dlc("a", "a", "a"))
        val merged = mergeChain(vanilla, listOf(mod1, mod2), MergeRules.builtinPolicy)
        println("[identical-args] result=${dlcArgs(merged)}")
        assertEquals(2, dlcArgs(merged).size)
    }
}
