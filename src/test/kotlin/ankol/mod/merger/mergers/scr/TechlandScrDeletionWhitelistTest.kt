package ankol.mod.merger.mergers.scr

import ankol.mod.merger.antlr.scr.TechlandScriptLexer
import ankol.mod.merger.antlr.scr.TechlandScriptParser
import ankol.mod.merger.core.BaseModManager
import ankol.mod.merger.core.filetrees.MemoryFileTree
import ankol.mod.merger.domain.MergeContext
import ankol.mod.merger.mergers.scr.node.ScrContainerNode
import ankol.mod.merger.mergers.scr.node.ScrFunCallNode
import ankol.mod.merger.tools.AntPathMatcher
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import org.junit.Test
import java.nio.file.Files
import java.nio.file.Path
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TechlandScrDeletionWhitelistTest {
    private val vanillaContent = """
        sub main()
        {
            Dlc();
            Condition(-1);
        }
    """.trimIndent()

    private val incomingWithoutCalls = """
        sub main()
        {
        }
    """.trimIndent()

    @Test
    fun `allowlist recognizes direct Dlc call in inventory path`() {
        val root = parse(vanillaContent)
        val main = root.childrens.values.single() as ScrContainerNode
        val dlc = main.childrens.values.filterIsInstance<ScrFunCallNode>().first()

        assertEquals("Dlc", dlc.functionName)
        assertTrue(ScrDeletionWhitelist.allows("scripts\\inventory\\inventory_special.scr", dlc))
    }

    @Test
    fun `allowlist path pattern preserves the existing inventory file scope`() {
        val root = parse(vanillaContent)
        val main = root.childrens.values.single() as ScrContainerNode
        val dlc = main.childrens.values.filterIsInstance<ScrFunCallNode>().first()

        assertTrue(ScrDeletionWhitelist.allows("scripts/inventory/inventory.scr", dlc))
        assertTrue(ScrDeletionWhitelist.allows("SCRIPTS/INVENTORY/InventoryStuff.SCR", dlc))
        assertFalse(ScrDeletionWhitelist.allows("scripts/inventory/nested/inventory_special.scr", dlc))
        assertFalse(ScrDeletionWhitelist.allows("scripts/items/inventory_special.scr", dlc))
    }

    @Test
    fun `ant path matcher supports star question mark and double star`() {
        assertTrue(
            AntPathMatcher.matches(
                "scripts/inventory/inventory_*.scr",
                "scripts/inventory/inventory_special.scr"
            )
        )
        assertFalse(
            AntPathMatcher.matches(
                "scripts/inventory/inventory_*.scr",
                "scripts/inventory/nested/inventory_special.scr"
            )
        )

        assertTrue(
            AntPathMatcher.matches(
                "scripts/inventory/inventory_?.scr",
                "scripts/inventory/inventory_a.scr"
            )
        )
        assertFalse(
            AntPathMatcher.matches(
                "scripts/inventory/inventory_?.scr",
                "scripts/inventory/inventory_ab.scr"
            )
        )

        assertTrue(
            AntPathMatcher.matches(
                "scripts/**/inventory_*.scr",
                "scripts/items/inventory/inventory_special.scr"
            )
        )
        assertTrue(
            AntPathMatcher.matches(
                "scripts/**/inventory_*.scr",
                "scripts/inventory_special.scr"
            )
        )
    }

    @Test
    fun `only Dlc without arguments is deleted from inventory files`() {
        val entryPath = "scripts\\inventory\\inventory_special.scr"
        withBasePak(entryPath, vanillaContent) { manager ->
            val context = MergeContext(manager)
            context.configure(entryPath, "data0.pak", "delete-dlc.pak", true)

            val result = TechlandScrFileMerger(context).merge(
                memoryFile(entryPath, "data0.pak", vanillaContent),
                memoryFile(entryPath, "delete-dlc.pak", incomingWithoutCalls)
            )

            assertFalse(result.mergedContent.contains("Dlc();"))
            assertContains(result.mergedContent, "Condition(-1);")
        }
    }

    @Test
    fun `Dlc deletion is not allowed outside inventory-prefixed files`() {
        val entryPath = "scripts\\inventory\\itemaffixes.scr"
        withBasePak(entryPath, vanillaContent) { manager ->
            val context = MergeContext(manager)
            context.configure(entryPath, "data0.pak", "delete-dlc.pak", true)

            val result = TechlandScrFileMerger(context).merge(
                memoryFile(entryPath, "data0.pak", vanillaContent),
                memoryFile(entryPath, "delete-dlc.pak", incomingWithoutCalls)
            )

            assertContains(result.mergedContent, "Dlc();")
            assertContains(result.mergedContent, "Condition(-1);")
        }
    }

    @Test
    fun `Dlc deletion is not allowed when it has arguments`() {
        val entryPath = "scripts\\inventory\\inventorystuff.scr"
        val vanillaWithArgument = vanillaContent.replace("Dlc();", "Dlc(1);")
        withBasePak(entryPath, vanillaWithArgument) { manager ->
            val context = MergeContext(manager)
            context.configure(entryPath, "data0.pak", "delete-dlc.pak", true)

            val result = TechlandScrFileMerger(context).merge(
                memoryFile(entryPath, "data0.pak", vanillaWithArgument),
                memoryFile(entryPath, "delete-dlc.pak", incomingWithoutCalls)
            )

            assertContains(result.mergedContent, "Dlc(1);")
        }
    }

    @Test
    fun `accepted Dlc deletion is not restored by a later mod`() {
        val entryPath = "scripts\\inventory\\inventory.scr"
        withBasePak(entryPath, vanillaContent) { manager ->
            val context = MergeContext(manager)
            val merger = TechlandScrFileMerger(context)

            context.configure(entryPath, "data0.pak", "delete-dlc.pak", true)
            val firstResult = merger.merge(
                memoryFile(entryPath, "data0.pak", vanillaContent),
                memoryFile(entryPath, "delete-dlc.pak", incomingWithoutCalls)
            )

            context.configure(entryPath, "delete-dlc.pak", "older-mod.pak", false)
            val secondResult = merger.merge(
                memoryFile(entryPath, "delete-dlc.pak", firstResult.mergedContent),
                memoryFile(entryPath, "older-mod.pak", vanillaContent)
            )

            assertFalse(secondResult.mergedContent.contains("Dlc();"))
            assertContains(secondResult.mergedContent, "Condition(-1);")
        }
    }

    private fun memoryFile(entryPath: String, archiveName: String, content: String) =
        MemoryFileTree(
            entryPath.replace('\\', '/').substringAfterLast('/'),
            entryPath,
            mutableListOf(archiveName),
            content
        )

    private fun parse(content: String): ScrContainerNode {
        val lexer = TechlandScriptLexer(CharStreams.fromString(content))
        val tokens = CommonTokenStream(lexer)
        val parser = TechlandScriptParser(tokens)
        return TechlandScrFileVisitor(tokens).visitFile(parser.file()) as ScrContainerNode
    }

    private fun withBasePak(entryPath: String, content: String, block: (BaseModManager) -> Unit) {
        val tempDir = Files.createTempDirectory("smm-deletion-test-")
        try {
            createPak(tempDir.resolve("data0.pak"), entryPath, content)
            BaseModManager(tempDir).use(block)
        } finally {
            tempDir.toFile().deleteRecursively()
        }
    }

    private fun createPak(pakPath: Path, entryPath: String, content: String) {
        ZipOutputStream(Files.newOutputStream(pakPath)).use { zip ->
            zip.putNextEntry(ZipEntry(entryPath.replace('\\', '/')))
            zip.write(content.toByteArray(Charsets.UTF_8))
            zip.closeEntry()
        }
    }
}
