package ankol.mod.merger.tools

import org.junit.Test
import tool.TestTool
import java.nio.file.Path
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import kotlin.io.path.*
import kotlin.test.assertNotNull

class TechlandSimpleTest {
    private val matchPak = listOf("data0.pak", "data1.pak", "databt.mpak")

    private val fileTypeMap = mutableMapOf<String, MutableSet<String>>()

    @Test
    fun findData0pakAllFileType() {
        val dltbPath = TestTool.getDltbPath()
        assertNotNull(dltbPath, "未找到消逝的光芒困兽安装目录")
        Path(dltbPath, "ph_ft", "source").walk()
            .filter { it.isRegularFile() }
            .forEach { pakFile: Path ->
                if (pakFile.name in matchPak) {
                    ZipFile(pakFile.absolutePathString()).entries().asSequence()
                        .forEach { item: ZipEntry ->
                            val substringAfter = item.name.substringAfterLast(".") //截取扩展名
                            fileTypeMap.computeIfAbsent(pakFile.name) { mutableSetOf() }.add(substringAfter)
                        }
                }
            }
        println("fileTypeMap = ${fileTypeMap}")
    }
}
