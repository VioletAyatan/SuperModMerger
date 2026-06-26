package ankol.mod.merger.tools

import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.util.regex.Pattern
import kotlin.io.path.exists
import kotlin.io.path.isDirectory

/**
 * 游戏安装路径自动检测工具
 * 通过 Steam 注册表和 libraryfolders.vdf 定位已安装的游戏
 */
object GameDetector {

    /** 检测结果 */
    data class DetectionResult(
        val gamePath: Path,
        val gameName: String,
        val modDir: Path,
        val baseModDir: Path,
        val defaultOutput: Path
    )

    /** 支持检测的游戏列表 */
    private val supportedGames = listOf(
        GameInfo("Dying Light The Beast", "ph_ft"),
        GameInfo("Dying Light 2", "ph")
    )

    private data class GameInfo(val folderName: String, val subDir: String)

    /**
     * 自动检测已安装的消光游戏
     * @return 检测到的游戏列表（可能多个都装了）
     */
    fun detectAll(): List<DetectionResult> {
        val steamPath = getSteamInstallPath() ?: return emptyList()
        val libraryFolders = getLibraryFolders(steamPath)
        val results = mutableListOf<DetectionResult>()

        for (game in supportedGames) {
            for (lib in libraryFolders) {
                val gameDir = lib.resolve("steamapps/common/${game.folderName}")
                if (gameDir.isDirectory()) {
                    // 检查常见的子目录结构
                    val modsDir = gameDir.resolve("${game.subDir}/mods")
                    val sourceDir = gameDir.resolve("${game.subDir}/source")
                    if (sourceDir.isDirectory()) {
                        results.add(
                            DetectionResult(
                                gamePath = gameDir,
                                gameName = game.folderName,
                                modDir = modsDir,
                                baseModDir = sourceDir,
                                defaultOutput = sourceDir.resolve("data7.pak")
                            )
                        )
                    }
                }
            }
        }
        return results
    }

    // ===== Steam 路径检测 =====

    /** 从 Windows 注册表读取 Steam 安装路径 */
    private fun getSteamInstallPath(): Path? {
        return try {
            val process = Runtime.getRuntime().exec("reg query HKEY_CURRENT_USER\\Software\\Valve\\Steam /v SteamPath")
            val reader = process.inputStream.bufferedReader()
            val line = reader.readLine()
            reader.close()

            // 格式: "    SteamPath    REG_SZ    C:/Program Files (x86)/Steam"
            val parts = line?.trim()?.split(Regex("\\s{4,}"))
            if (parts != null && parts.size >= 3) {
                Path.of(parts[2].replace("/", File.separator))
            } else null
        } catch (e: Exception) {
            null
        }
    }

    /** 解析 libraryfolders.vdf 获取所有 Steam 库目录 */
    private fun getLibraryFolders(steamPath: Path): List<Path> {
        val paths = mutableListOf<Path>()
        val vdfFile = steamPath.resolve("steamapps/libraryfolders.vdf")
        if (!vdfFile.exists()) return paths

        return try {
            val content = Files.readString(vdfFile)
            val pattern = Pattern.compile("\"path\"\\s+\"([^\"]+)\"")
            val matcher = pattern.matcher(content)
            while (matcher.find()) {
                var libPath = matcher.group(1)
                libPath = libPath.replace("\\\\", "\\")
                paths.add(Path.of(libPath))
            }
            paths
        } catch (e: Exception) {
            paths
        }
    }
}
