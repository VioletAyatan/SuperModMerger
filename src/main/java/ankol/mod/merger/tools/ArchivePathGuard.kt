package ankol.mod.merger.tools

import java.nio.file.InvalidPathException
import java.nio.file.Path

/**
 * 在受控的解压根目录内解析归档条目路径。
 *
 * 归档路径会先统一转换为正斜杠，确保使用 ZIP 或 Windows 分隔符构造的
 * 路径穿越在所有支持的平台上都按相同规则处理。
 */
internal object ArchivePathGuard {
    private val windowsDrivePrefix = Regex("^[a-zA-Z]:.*")

    data class ResolvedEntry(
        val relativeEntryName: String,
        val outputPath: Path
    )

    fun resolve(outputDir: Path, entryName: String): ResolvedEntry {
        val canonicalEntryName = entryName.replace('\\', '/')

        if (canonicalEntryName.isBlank()) {
            reject(entryName, "entry path is empty")
        }
        if (canonicalEntryName.startsWith('/') || windowsDrivePrefix.matches(canonicalEntryName)) {
            reject(entryName, "absolute paths are not allowed")
        }
        if (canonicalEntryName.contains(':')) {
            reject(entryName, "drive-qualified paths and alternate data streams are not allowed")
        }




        val relativePath = try {
            Path.of(canonicalEntryName).normalize()
        } catch (e: InvalidPathException) {
            throw SecurityException("Unsafe archive entry '$entryName': invalid path", e)
        }

        if (relativePath.isAbsolute || relativePath.toString().isEmpty() || relativePath.startsWith("..")) {
            reject(entryName, "path escapes the extraction directory")
        }

        val normalizedRoot = outputDir.toAbsolutePath().normalize()
        val outputPath = normalizedRoot.resolve(relativePath).normalize()
        if (outputPath == normalizedRoot || !outputPath.startsWith(normalizedRoot)) {
            reject(entryName, "path escapes the extraction directory")
        }

        val normalizedEntryName = relativePath.toString().replace('\\', '/')
        return ResolvedEntry(normalizedEntryName, outputPath)
    }

    private fun reject(entryName: String, reason: String): Nothing {
        throw SecurityException("Unsafe archive entry '$entryName': $reason")
    }
}
