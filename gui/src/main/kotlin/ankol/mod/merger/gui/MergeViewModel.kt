package ankol.mod.merger.gui

import ankol.mod.merger.api.MergeProgressCallback
import ankol.mod.merger.core.GlobalMergingStrategy
import ankol.mod.merger.tools.GameDetector
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import java.io.File
import java.nio.file.Path
import kotlin.io.path.name
import kotlin.io.path.Path
import kotlin.io.path.extension
import kotlin.io.path.isDirectory
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.isRegularFile

/**
 * GUI 合并流程的状态管理
 */
class MergeViewModel {

    // === 路径配置 ===
    /** MOD 合并目录 */
    var modDirPath by mutableStateOf("")
    /** 基准 MOD 路径 */
    var baseModPath by mutableStateOf("")
    /** 输出路径 */
    var outputPath by mutableStateOf("")

    // === 自动检测 ===
    /** 检测到的游戏列表 */
    var detectedGames by mutableStateOf<List<GameDetector.DetectionResult>>(emptyList())
    /** 是否正在检测 */
    var isDetecting by mutableStateOf(false)
    /** 选中的游戏索引 */
    var selectedGameIndex by mutableStateOf(-1)
    /** 检测状态文本 */
    var detectionStatus by mutableStateOf("")

    // === MOD 列表 ===
    /** 扫描到的可合并 MOD 列表 */
    var modList by mutableStateOf<List<ModEntry>>(emptyList())
    /** 是否正在扫描 */
    var isScanning by mutableStateOf(false)

    // === 合并策略 ===
    /** 全局修复模式开关 */
    var globalFixMode by mutableStateOf(false)

    // === 合并状态 ===
    /** 是否正在合并 */
    var isMerging by mutableStateOf(false)
    /** 合并进度 (0~1) */
    var progress by mutableStateOf(0f)
    /** 当前正在处理的文件名 */
    var currentFileName by mutableStateOf("")
    /** 日志行列表 */
    var logLines by mutableStateOf(listOf<LogLine>())

    // === 合并完成 ===
    /** 合并是否已完成 */
    var isComplete by mutableStateOf(false)
    /** 总处理文件数 */
    var totalProcessed by mutableStateOf(0)
    /** 成功合并数 */
    var mergedCount by mutableStateOf(0)
    /** 错误数 */
    var errorCount by mutableStateOf(0)

    /** MOD 条目 */
    data class ModEntry(
        val name: String,
        val path: Path
    )

    /** 日志行 */
    data class LogLine(
        val level: MergeProgressCallback.Level,
        val message: String,
        val timestamp: String = java.time.LocalTime.now().toString().take(8)
    )

    /** 扫描目录下的 MOD 文件 */
    fun scanModDir(dirPath: String) {
        val dir = Path(dirPath)
        if (!dir.isDirectory()) return

        isScanning = true
        modList = emptyList()

        try {
            val exts = setOf("pak", "zip", "7z", "rar")
            val found = dir.listDirectoryEntries()
                .filter { it.isRegularFile() && it.extension in exts }
                .map { ModEntry(it.name, it) }
            modList = found
        } catch (e: Exception) {
            addLog(MergeProgressCallback.Level.ERROR, "扫描失败: ${e.message}")
        } finally {
            isScanning = false
        }
    }

    /** 添加日志 */
    fun addLog(level: MergeProgressCallback.Level, message: String) {
        logLines = logLines + LogLine(level, message)
    }

    /** 自动检测已安装的游戏 */
    fun detectGame() {
        isDetecting = true
        detectionStatus = "正在检测..."
        detectedGames = emptyList()
        selectedGameIndex = -1

        try {
            val results = GameDetector.detectAll()
            detectedGames = results
            if (results.isEmpty()) {
                detectionStatus = "未检测到游戏安装"
            } else if (results.size == 1) {
                detectionStatus = "检测到: ${results[0].gameName}"
                applyDetection(0)
            } else {
                detectionStatus = "检测到多个游戏，请选择"
            }
        } catch (e: Exception) {
            detectionStatus = "检测失败: ${e.message}"
        } finally {
            isDetecting = false
        }
    }

    /** 应用检测结果到路径配置 */
    fun applyDetection(index: Int) {
        if (index < 0 || index >= detectedGames.size) return
        selectedGameIndex = index
        val result = detectedGames[index]
        modDirPath = result.modDir.toString()
        baseModPath = result.baseModDir.toString()
        outputPath = result.defaultOutput.toString()
        detectionStatus = "已应用: ${result.gameName}"
        scanModDir(result.modDir.toString())
    }

    /** 重置状态 */
    fun reset() {
        isMerging = false
        progress = 0f
        currentFileName = ""
        isComplete = false
        totalProcessed = 0
        mergedCount = 0
        errorCount = 0
    }
}
