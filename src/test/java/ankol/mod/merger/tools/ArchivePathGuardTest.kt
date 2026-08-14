package ankol.mod.merger.tools

import org.junit.Test
import java.nio.file.Files
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import kotlin.io.path.exists
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlin.test.assertFailsWith

class ArchivePathGuardTest {

    @Test
    fun acceptsAndNormalizesAnEntryInsideTheExtractionRoot() {
        val root = Files.createTempDirectory("archive_path_guard_")
        try {
            val resolved = ArchivePathGuard.resolve(root, "scripts/player/../inventory.scr")

            assertEquals("scripts/inventory.scr", resolved.relativeEntryName)
            assertEquals(root.resolve("scripts/inventory.scr").toAbsolutePath().normalize(), resolved.outputPath)
            assertTrue(resolved.outputPath.startsWith(root.toAbsolutePath().normalize()))
        } finally {
            Tools.deleteRecursively(root)
        }
    }

    @Test
    fun rejectsRelativeTraversalWithEitherSeparator() {
        val root = Files.createTempDirectory("archive_path_guard_")
        try {
            listOf(
                "../escaped.txt",
                "safe/../../escaped.txt",
                "..\\escaped.txt",
                "safe\\..\\..\\escaped.txt"
            ).forEach { entryName ->
                assertFailsWith<SecurityException>(entryName) {
                    ArchivePathGuard.resolve(root, entryName)
                }
            }
        } finally {
            Tools.deleteRecursively(root)
        }
    }

    @Test
    fun rejectsAbsoluteDriveUncAndAlternateStreamPaths() {
        val root = Files.createTempDirectory("archive_path_guard_")
        try {
            listOf(
                "/absolute/escaped.txt",
                "C:/absolute/escaped.txt",
                "C:\\absolute\\escaped.txt",
                "\\\\server\\share\\escaped.txt",
                "safe.txt:stream"
            ).forEach { entryName ->
                assertFailsWith<SecurityException>(entryName) {
                    ArchivePathGuard.resolve(root, entryName)
                }
            }
        } finally {
            Tools.deleteRecursively(root)
        }
    }

    @Test
    fun rejectsEntriesThatResolveToTheExtractionRoot() {
        val root = Files.createTempDirectory("archive_path_guard_")
        try {
            listOf("", ".", "folder/..").forEach { entryName ->
                assertFailsWith<SecurityException>(entryName) {
                    ArchivePathGuard.resolve(root, entryName)
                }
            }
        } finally {
            Tools.deleteRecursively(root)
        }
    }

    @Test
    fun pakExtractionRejectsAMaliciousZipWithoutWritingOutsideTheRoot() {
        val sandbox = Files.createTempDirectory("archive_path_guard_integration_")
        try {
            val archive = sandbox.resolve("malicious.zip")
            ZipOutputStream(Files.newOutputStream(archive)).use { zip ->
                zip.putNextEntry(ZipEntry("../escaped.txt"))
                zip.write("must not escape".toByteArray())
                zip.closeEntry()
            }

            val extractionRoot = sandbox.resolve("extracted")
            val failure = runCatching {
                PakManager.extractPak("malicious.zip", archive, extractionRoot)
            }.exceptionOrNull()

            assertNotNull(failure, "malicious archive extraction must fail")
            assertFalse(sandbox.resolve("escaped.txt").exists(), "archive entry must not be written outside extraction root")
        } finally {
            Tools.deleteRecursively(sandbox)
        }
    }
}
