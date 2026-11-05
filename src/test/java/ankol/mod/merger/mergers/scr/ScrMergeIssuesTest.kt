package ankol.mod.merger.mergers.scr

import ankol.mod.merger.antlr.scr.TechlandScriptLexer
import ankol.mod.merger.antlr.scr.TechlandScriptParser
import ankol.mod.merger.api.ConflictResolver
import ankol.mod.merger.constants.UserChoice
import ankol.mod.merger.core.BaseModManager
import ankol.mod.merger.core.files.InMemoryArchiveFile
import ankol.mod.merger.domain.MergeContext
import ankol.mod.merger.mergers.ConflictRecord
import ankol.mod.merger.mergers.DeletionRecord
import ankol.mod.merger.mergers.scr.node.ScrContainerNode
import ankol.mod.merger.tools.Tools
import org.antlr.v4.runtime.*
import org.junit.After
import org.junit.Test
import java.nio.file.Files
import java.nio.file.Path
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * SCR 合并的插入位置、签名生成和冲突来源相关问题的复现测试。
 */
class ScrMergeIssuesTest {
    private val relPath = "scripts/probe/probe_test.scr"
    private val tempDirs = mutableListOf<Path>()
    private val managers = mutableListOf<BaseModManager>()

    /** 冲突一律采用 MOD，并记录需要用户选择的冲突 */
    private class RecordingResolver : ConflictResolver {
        val asked = mutableListOf<ConflictRecord>()

        override fun resolveConflict(conflicts: MutableList<ConflictRecord>) {
            conflicts.filter { it.userChoice == null }.forEach { asked += it.copy() }
            conflicts.forEach { if (it.userChoice == null) it.userChoice = UserChoice.MERGE_MOD }
        }

        override fun resolveDeletionConflicts(deletions: MutableList<DeletionRecord>) {
            deletions.forEach { it.userChoice = UserChoice.BASE_MOD }
        }
    }

    private class ParsedScr(val root: ScrContainerNode, val syntaxErrors: List<String>)

    @After
    fun cleanup() {
        managers.forEach { it.close() }
        tempDirs.forEach { runCatching { Tools.deleteRecursively(it) } }
    }

    /**
     * 模拟 FileMergerEngine 的顺序合并：原版 + mod1，然后依次合并后续 MOD。
     */
    private fun mergeChain(
        vanilla: String,
        vararg mods: String,
        resolver: ConflictResolver = RecordingResolver(),
    ): String {
        val sourceDir = Files.createTempDirectory("smm_scr_issues_test_")
        tempDirs.add(sourceDir)
        ZipOutputStream(Files.newOutputStream(sourceDir.resolve("data0.pak"))).use { zip ->
            zip.putNextEntry(ZipEntry(relPath))
            zip.write(vanilla.toByteArray())
            zip.closeEntry()
        }
        val baseModManager = BaseModManager(sourceDir)
        managers.add(baseModManager)
        val context = MergeContext(baseModManager, resolver)

        var accumulated = vanilla
        var accumulatedName = "data0.pak"
        mods.forEachIndexed { index, modContent ->
            val modName = "mod${index + 1}.pak"
            context.configure(relPath, accumulatedName, modName, index == 0)
            accumulated = TechlandScrFileMerger(context).merge(
                InMemoryArchiveFile("probe_test.scr", relPath, mutableListOf(accumulatedName), accumulated),
                InMemoryArchiveFile("probe_test.scr", relPath, mutableListOf(modName), modContent),
            ).mergedContent
            accumulatedName = modName
        }
        println("[$relPath] merged:\n$accumulated")
        return accumulated
    }

    private fun parse(content: String): ParsedScr {
        val errors = mutableListOf<String>()
        val tokenStream = CommonTokenStream(TechlandScriptLexer(CharStreams.fromString(content)))
        val parser = TechlandScriptParser(tokenStream)
        parser.removeErrorListeners()
        parser.addErrorListener(object : BaseErrorListener() {
            override fun syntaxError(
                recognizer: Recognizer<*, *>?, offendingSymbol: Any?, line: Int,
                charPositionInLine: Int, msg: String?, e: RecognitionException?
            ) {
                errors += "$line:$charPositionInLine $msg"
            }
        })
        val root = TechlandScrFileVisitor(tokenStream).visitFile(parser.file()) as ScrContainerNode
        return ParsedScr(root, errors)
    }

    private fun ScrContainerNode.container(signature: String): ScrContainerNode =
        childrens[signature] as? ScrContainerNode
            ?: error("container '$signature' not found, children=${childrens.keys}")

    /** #5：新增的 sub 插在最后一个 import 之后、第一个 sub 之前 */
    @Test
    fun insertedSubGoesBetweenLastImportAndFirstSub() {
        val vanilla = """
            import "a.scr";
            import "b.scr";
            sub main()
            {
            }
            Foo(1);
        """.trimIndent()
        val mod = """
            import "a.scr"
            import "b.scr"
            sub main()
            {
            }
            sub helper()
            {
            }
            Foo(1);
        """.trimIndent()
        val merged = parse(mergeChain(vanilla, mod))
        assertEquals(emptyList(), merged.syntaxErrors)
        assertEquals(
            listOf("import:\"a.scr\"", "import:\"b.scr\"", "sub:helper", "sub:main", "funCall:Foo"),
            merged.root.childrens.keys.toList()
        )
    }

    /** #5：MOD 在文件开头新增 sub，同样插在最后一个 import 之后、第一个 sub 之前 */
    @Test
    fun insertedSubAtFileStartGoesBetweenLastImportAndFirstSub() {
        val vanilla = """
            import "a.scr"
            sub main()
            {
            }
            Foo(1);
        """.trimIndent()
        val mod = """
            sub helper()
            {
            }
            import "a.scr"
            sub main()
            {
            }
            Foo(1);
        """.trimIndent()
        val merged = parse(mergeChain(vanilla, mod))
        assertEquals(emptyList(), merged.syntaxErrors)
        assertEquals(
            listOf("import:\"a.scr\"", "sub:helper", "sub:main", "funCall:Foo"),
            merged.root.childrens.keys.toList()
        )
    }

    /** #5：同时新增多个 sub，保持 MOD 中的顺序 */
    @Test
    fun multipleInsertedSubsKeepOrder() {
        val vanilla = """
            import "a.scr"
            sub main()
            {
            }
        """.trimIndent()
        val mod = """
            import "a.scr"
            sub helper1()
            {
            }
            sub helper2()
            {
            }
            sub main()
            {
            }
        """.trimIndent()
        val merged = parse(mergeChain(vanilla, mod))
        assertEquals(emptyList(), merged.syntaxErrors)
        assertEquals(
            listOf("import:\"a.scr\"", "sub:helper1", "sub:helper2", "sub:main"),
            merged.root.childrens.keys.toList()
        )
    }

    /** #5：没有 sub 时，新增的 sub 插在最后一个 import 之后 */
    @Test
    fun insertedSubGoesAfterLastImportWhenThereIsNoSub() {
        val vanilla = """
            import "a.scr"
            Foo(1);
        """.trimIndent()
        val mod = """
            sub helper()
            {
            }
            import "a.scr"
            Foo(1);
        """.trimIndent()
        val merged = parse(mergeChain(vanilla, mod))
        assertEquals(emptyList(), merged.syntaxErrors)
        assertEquals(listOf("import:\"a.scr\"", "sub:helper", "funCall:Foo"), merged.root.childrens.keys.toList())
    }

    /** #5：没有 import 也没有 sub 时，新增的 sub 插在第一个节点之前 */
    @Test
    fun insertedSubGoesBeforeFirstNodeWhenThereIsNoImportOrSub() {
        val vanilla = """
            Foo(1);
        """.trimIndent()
        val mod = """
            sub helper()
            {
            }
            Foo(1);
        """.trimIndent()
        val merged = parse(mergeChain(vanilla, mod))
        assertEquals(emptyList(), merged.syntaxErrors)
        assertEquals(listOf("sub:helper", "funCall:Foo"), merged.root.childrens.keys.toList())
    }

    /** #5b：原文件没有 import 时，新增的 import 独占一行 */
    @Test
    fun insertedImportStaysOnItsOwnLine() {
        val vanilla = """
            sub main()
            {
            }
        """.trimIndent()
        val mod = """
            import "b.scr"
            sub main()
            {
            }
        """.trimIndent()
        val merged = mergeChain(vanilla, mod)
        val lines = merged.lines().map { it.trim() }
        assertTrue("import \"b.scr\"" in lines, "import is not on its own line:\n$merged")
        assertTrue("sub main()" in lines, "sub main() is not on its own line:\n$merged")
    }

    /** #6：块开头新增的多个节点保持在原位置且顺序不变 */
    @Test
    fun nodesInsertedAtBlockStartKeepPositionAndOrder() {
        val vanilla = """
            sub main()
            {
              A(1);
            }
        """.trimIndent()
        val mod = """
            sub main()
            {
              NewA(1);
              NewB(2);
              A(1);
            }
        """.trimIndent()
        val main = parse(mergeChain(vanilla, mod)).root.container("sub:main")
        assertEquals(listOf("funCall:NewA", "funCall:NewB", "funCall:A"), main.childrens.keys.toList())
    }

    /** #6：块开头新增的节点沿用相邻兄弟节点的缩进 */
    @Test
    fun nodesInsertedAtBlockStartUseSiblingIndentation() {
        val vanilla = """
            sub main()
            {
              A(1);
            }
        """.trimIndent()
        val mod = """
            sub main()
            {
              NewA(1);
              NewB(2);
              A(1);
            }
        """.trimIndent()
        assertEquals(mod, mergeChain(vanilla, mod).replace("\r\n", "\n"))
    }

    /** #5b：没有 import 时新增的 import 与后面的 sub 分行 */
    @Test
    fun insertedImportKeepsTheOriginalLayout() {
        val vanilla = """
            sub main()
            {
            }
        """.trimIndent()
        val mod = """
            import "b.scr"
            sub main()
            {
            }
        """.trimIndent()
        assertEquals(mod, mergeChain(vanilla, mod).replace("\r\n", "\n"))
    }

    /** 对照组：同一兄弟节点之后新增的多个节点保持顺序 */
    @Test
    fun nodesInsertedAfterTheSameSiblingKeepOrder() {
        val vanilla = """
            sub main()
            {
              A(1);
              Z(1);
            }
        """.trimIndent()
        val mod = """
            sub main()
            {
              A(1);
              N1(1);
              N2(2);
              Z(1);
            }
        """.trimIndent()
        val main = parse(mergeChain(vanilla, mod)).root.container("sub:main")
        assertEquals(listOf("funCall:A", "funCall:N1", "funCall:N2", "funCall:Z"), main.childrens.keys.toList())
    }

    /** #7：4 个同名 sub 都要保留在语法树中 */
    @Test
    fun repeatedSubsAreAllKeptInTheTree() {
        val content = """
            sub X()
            {
            }
            sub X()
            {
            }
            sub X()
            {
            }
            sub X()
            {
            }
        """.trimIndent()
        val root = parse(content).root
        assertEquals(4, root.childrens.size, "children=${root.childrens.keys}")
    }

    /** #7：同一块中 4 个同名变量声明都要保留在语法树中 */
    @Test
    fun repeatedVariableDeclarationsAreAllKeptInTheTree() {
        val content = """
            sub main()
            {
              float a = 1;
              float a = 1;
              float a = 1;
              float a = 1;
            }
        """.trimIndent()
        val main = parse(content).root.container("sub:main")
        assertEquals(4, main.childrens.size, "children=${main.childrens.keys}")
    }

    /** #7：修改 4 个同名变量声明中的第 1 个，修改要合并进结果 */
    @Test
    fun modifyingTheFirstOfRepeatedVariableDeclarationsIsMerged() {
        val vanilla = """
            sub main()
            {
              float a = 1;
              float a = 2;
              float a = 3;
              float a = 4;
            }
        """.trimIndent()
        val mod = """
            sub main()
            {
              float a = 9;
              float a = 2;
              float a = 3;
              float a = 4;
            }
        """.trimIndent()
        val merged = mergeChain(vanilla, mod)
        assertTrue("float a = 9;" in merged, "modification is lost:\n$merged")
        assertFalse("float a = 1;" in merged, "original declaration is kept:\n$merged")
    }

    /** #8：if 块内唯一的调用，签名不受外层同名调用是否重复的影响 */
    @Test
    fun nestedCallSignatureDoesNotDependOnOuterRepetition() {
        val single = """
            sub main()
            {
              Foo(1);
              if (a)
              {
                Foo(3);
              }
            }
        """.trimIndent()
        val repeated = """
            sub main()
            {
              Foo(1);
              Foo(2);
              if (a)
              {
                Foo(3);
              }
            }
        """.trimIndent()

        fun innerKeys(content: String) = parse(content).root.container("sub:main").container("if").childrens.keys
        assertEquals(innerKeys(single), innerKeys(repeated))
    }

    /** #8：MOD 删除一个外层调用并修改 if 块内的调用，if 块内只保留修改后的调用 */
    @Test
    fun nestedCallModificationIsNotDuplicated() {
        val vanilla = """
            sub main()
            {
              Foo(1);
              Foo(2);
              if (a)
              {
                Foo(3);
              }
            }
        """.trimIndent()
        val mod = """
            sub main()
            {
              Foo(1);
              if (a)
              {
                Foo(4);
              }
            }
        """.trimIndent()
        val merged = mergeChain(vanilla, mod)
        val ifBlock = parse(merged).root.container("sub:main").container("if")
        assertEquals(1, ifBlock.childrens.size, "if block children=${ifBlock.childrens.keys}")
        assertTrue("Foo(4)" in merged && "Foo(3)" !in merged, "unexpected if block:\n$merged")
    }

    /** #8：sub 之后的根级调用，签名不受 sub 内同名调用是否重复的影响 */
    @Test
    fun rootCallSignatureDoesNotDependOnRepetitionInsideSub() {
        val single = """
            sub main()
            {
              Foo(1);
            }
            Foo(9);
        """.trimIndent()
        val repeated = """
            sub main()
            {
              Foo(1);
              Foo(2);
            }
            Foo(9);
        """.trimIndent()

        fun rootCallKeys(content: String) = parse(content).root.childrens.keys.filter { it.startsWith("funCall:") }
        assertEquals(rootCallKeys(single), rootCallKeys(repeated))
    }

    /** 待确认：变量赋值改值后，结果只保留新值 */
    @Test
    fun variableAssignmentValueChangeIsAModification() {
        val vanilla = """
            sub main()
            {
              x = 5;
            }
        """.trimIndent()
        val mod = """
            sub main()
            {
              x = 6;
            }
        """.trimIndent()
        val merged = mergeChain(vanilla, mod)
        assertTrue("x = 6;" in merged && "x = 5;" !in merged, "unexpected result:\n$merged")
    }

    /** 冲突提示中累积侧的 MOD 名是该节点当前内容的来源：函数调用 */
    @Test
    fun conflictShowsTheModThatLastChangedTheCall() {
        val vanilla = """
            sub main()
            {
              X(1);
            }
        """.trimIndent()
        val resolver = RecordingResolver()
        mergeChain(
            vanilla,
            vanilla.replace("X(1)", "X(2)"),
            vanilla.replace("X(1)", "X(3)"),
            vanilla.replace("X(1)", "X(4)"),
            resolver = resolver,
        )
        assertEquals(listOf("mod1.pak", "mod2.pak"), resolver.asked.map { it.baseModName })
    }

    /** 冲突提示中累积侧的 MOD 名是该节点当前内容的来源：中间 MOD 未改动该节点 */
    @Test
    fun conflictShowsTheModThatChangedTheDeclarationWhenAnIntermediateModDidNot() {
        val vanilla = """
            sub main()
            {
              float a = 1;
            }
        """.trimIndent()
        val resolver = RecordingResolver()
        mergeChain(vanilla, vanilla.replace("1;", "2;"), vanilla, vanilla.replace("1;", "3;"), resolver = resolver)
        assertEquals(listOf("mod1.pak"), resolver.asked.map { it.baseModName })
    }
}
