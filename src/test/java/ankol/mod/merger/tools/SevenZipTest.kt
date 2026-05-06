package ankol.mod.merger.tools

import net.sf.sevenzipjbinding.*
import net.sf.sevenzipjbinding.impl.OutItem
import net.sf.sevenzipjbinding.impl.OutItemFactory
import net.sf.sevenzipjbinding.impl.RandomAccessFileInStream
import net.sf.sevenzipjbinding.impl.RandomAccessFileOutStream
import org.junit.AfterClass
import org.junit.BeforeClass
import org.junit.FixMethodOrder
import org.junit.Test
import org.junit.runners.MethodSorters
import java.io.*
import java.nio.file.*
import kotlin.io.path.Path
import kotlin.io.path.exists
import kotlin.io.path.extension
import kotlin.io.path.inputStream
import kotlin.io.path.isRegularFile
import kotlin.io.path.name
import kotlin.io.path.nameWithoutExtension
import kotlin.io.path.walk
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
class SevenZipTest {

    companion object {
        private lateinit var tempDir: Path
        private lateinit var outputDir: File
        private val userDir = System.getProperty("user.dir")

        private val testFiles = mapOf(
            "hello.txt" to "Hello, SevenZipJBinding!\n",
            "world.txt" to "This is a test file.\n",
            "subdir/nested.txt" to "Nested file content.\n"
        )

        @BeforeClass
        @JvmStatic
        fun setup() {
            //初始化运行库
            SevenZip.initSevenZipFromPlatformJAR()
            //准备临时文件
            tempDir = Files.createTempDirectory("7zip_test_")
            outputDir = tempDir.resolve("extracted").toFile().also { it.mkdirs() }

            // 准备源文件
            testFiles.forEach { (relativePath, content) ->
                val file = tempDir.resolve(relativePath).toFile()
                file.parentFile.mkdirs()
                file.writeText(content)
            }
        }

        @AfterClass
        @JvmStatic
        fun teardown() {
//            tempDir.toFile().deleteRecursively()
        }

        private fun sourceFiles() = testFiles.keys.map { tempDir.resolve(it).toFile() }

        // ── 通用压缩 ──────────────────────────────────────────────
        private fun compress(format: ArchiveFormat, archiveFile: File) {
            val files = sourceFiles()
            val outStream = RandomAccessFileOutStream(RandomAccessFile(archiveFile, "rw"))
            SevenZip.openOutArchive(format).use { outArchive ->
                outArchive.createArchive(
                    outStream, files.size,
                    object : IOutCreateCallback<OutItem> {
                        override fun setOperationResult(ok: Boolean) =
                            assertTrue(ok, "[$format] 压缩操作应成功")

                        override fun setTotal(total: Long) {}
                        override fun setCompleted(complete: Long) {}

                        override fun getItemInformation(
                            index: Int,
                            outItemFactory: OutItemFactory<OutItem>
                        ): OutItem = outItemFactory.createOutItem().also { item ->
                            val file = files[index]
                            item.propertyPath = testFiles.keys.toList()[index]
                            if (file.isDirectory) item.propertyIsDir = true
                            else item.dataSize = file.length()
                        }

                        override fun getStream(index: Int): ISequentialInStream? {
                            val file = files[index]
                            return if (file.isFile)
                                RandomAccessFileInStream(RandomAccessFile(file, "r"))
                            else null
                        }
                    })
            }
        }

        // ── 通用解压 ──────────────────────────────────────────────
        private fun extract(archiveFile: File, extractTo: File) {
            RandomAccessFileInStream(RandomAccessFile(archiveFile, "r")).use { inStream ->
                SevenZip.openInArchive(null, inStream).use { archive ->
                    for (item in archive.simpleInterface.archiveItems) {
                        if (item.isFolder) continue
                        val outFile = File(extractTo, item.path).also { it.parentFile.mkdirs() }
                        FileOutputStream(outFile).use { fos ->
                            val result = item.extractSlow { data -> fos.write(data); data.size }
                            assertEquals(ExtractOperationResult.OK, result, "解压 ${item.path} 应成功")
                        }
                    }
                }
            }
        }

        // ── 解压后内容校验 ─────────────────────────────────────────
        private fun verifyExtracted(extractTo: File, formatTag: String) {
            testFiles.forEach { (path, content) ->
                val file = File(extractTo, path)
                assertTrue(file.exists(), "[$formatTag] 文件 $path 应存在")
                assertEquals(content, file.readText(), "[$formatTag] 文件 $path 内容应匹配")
                println("✓ [$formatTag] $path 验证通过")
            }
        }
    }

    // ────────────────────────────────────────────────
    // 1. 7z
    // ────────────────────────────────────────────────
    @Test
    fun test1_sevenZip_compressAndExtract() {
        val archive = tempDir.resolve("test.7z").toFile()
        compress(ArchiveFormat.SEVEN_ZIP, archive)
        assertTrue(archive.exists() && archive.length() > 0, "7z 文件应已创建")

        val out = outputDir.resolve("7z").also { it.mkdirs() }
        extract(archive, out)
        verifyExtracted(out, "7z")
    }

    // ────────────────────────────────────────────────
    // 2. zip
    // ────────────────────────────────────────────────
    @Test
    fun test2_zip_compressAndExtract() {
        val archive = tempDir.resolve("test.zip").toFile()
        compress(ArchiveFormat.ZIP, archive)
        assertTrue(archive.exists() && archive.length() > 0, "zip 文件应已创建")

        val out = outputDir.resolve("zip").also { it.mkdirs() }
        extract(archive, out)
        verifyExtracted(out, "zip")
    }

    // ────────────────────────────────────────────────
    // 3. rar（仅解压）
    // ────────────────────────────────────────────────
    @Test
    fun test3_rar_extractOnly() {
        val examplesDir = Path(userDir, "examples")
        if (!examplesDir.exists()) {
            println("⚠ 跳过 RAR 测试：examples 目录不存在")
            return
        }

        val rarFiles = examplesDir.walk()
            .filter { it.isRegularFile() && it.extension.lowercase() == "rar" }
            .toList()

        if (rarFiles.isEmpty()) {
            println("⚠ 跳过 RAR 测试：examples 目录下没有找到 .rar 文件")
            return
        }

        rarFiles.forEach { rarFile: Path ->
            val magic = rarFile.inputStream().use { it.readNBytes(8) }
            val rarMagic4 = byteArrayOf(0x52, 0x61, 0x72, 0x21, 0x1A, 0x07, 0x00)
            val rarMagic5 = byteArrayOf(0x52, 0x61, 0x72, 0x21, 0x1A, 0x07, 0x01, 0x00)

            val isRar4 = magic.take(7) == rarMagic4.toList()
            val isRar5 = magic.take(8) == rarMagic5.toList()

            if (!isRar4 && !isRar5) {
                println("⚠ 跳过 ${rarFile.name}：魔数校验失败，不是有效的 RAR 文件")
                return@forEach
            }

            val version = if (isRar5) "RAR5" else "RAR4"
            println("📦 [${version}] 开始解压：${rarFile.name}")

            val out = outputDir.resolve("rar/${rarFile.nameWithoutExtension}").also { it.mkdirs() }
            extract(rarFile.toFile(), out)

            val extractedFiles = out.walkTopDown().filter { it.isFile }.toList()
            assertTrue(extractedFiles.isNotEmpty(), "[rar] ${rarFile.name} 应解压出至少一个文件")
            extractedFiles.forEach { println("✓ [$version] ${rarFile.name} -> ${it.relativeTo(out)} 解压成功，位置：${out.absolutePath}") }
        }
    }
}
