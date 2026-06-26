package ankol.mod.merger.gui

import ankol.mod.merger.api.MergeProgressCallback
import ankol.mod.merger.api.console.ConsoleAssetConflictResolver
import ankol.mod.merger.core.FileMergerEngine
import ankol.mod.merger.core.GlobalMergingStrategy
import ankol.mod.merger.domain.MergingModInfo
import ankol.mod.merger.gui.components.ConflictDialog
import ankol.mod.merger.gui.components.DeletionConflictDialog
import ankol.mod.merger.gui.components.FileSelectorField
import ankol.mod.merger.gui.components.LogPanel
import ankol.mod.merger.gui.components.ModListPanel
import ankol.mod.merger.tools.Tools
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.io.path.Path

// ===== 配色 =====
private val Purple = Color(0xFF7C5CFC)
private val PurpleDark = Color(0xFF5A3FD4)
private val Green = Color(0xFF44DD88)
private val Red = Color(0xFFFF5555)
private val Orange = Color(0xFFFFAA44)
private val BgDark = Color(0xFF0D0D1A)
private val SurfaceDark = Color(0xFF151528)
private val CardDark = Color(0xFF1C1C36)
private val BorderColor = Color(0xFF2D2D44)
private val TextDim = Color(0xFF666680)
private val TextMuted = Color(0xFF8888AA)
private val TextBright = Color(0xFFE0E0F0)

/**
 * SuperModMerger 主窗口 — 美化版
 */
@Composable
fun MainWindow() {
    val vm = remember { MergeViewModel() }

    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = Purple,
            secondary = Green,
            surface = SurfaceDark,
            background = BgDark,
            onSurface = TextBright,
            onBackground = TextBright,
        )
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier.fillMaxSize().padding(16.dp)
            ) {
                // ═══════ 顶部标题栏 ═══════
                HeaderBar(vm)

                Spacer(Modifier.height(10.dp))

                // ═══════ 路径配置区 ═══════
                PathConfigCard(vm)

                Spacer(Modifier.height(10.dp))

                // ═══════ 中间区域 ═══════
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // 左侧：MOD 列表
                    ModListPanel(
                        modList = vm.modList,
                        isScanning = vm.isScanning,
                        modifier = Modifier
                            .weight(0.6f)
                            .fillMaxHeight()
                    )

                    // 右侧：合并配置 + 按钮
                    MergeConfigCard(
                        vm = vm,
                        modifier = Modifier
                            .weight(0.4f)
                            .fillMaxHeight()
                    )
                }

                Spacer(Modifier.height(10.dp))

                // ═══════ 日志面板 ═══════
                LogPanel(
                    logLines = vm.logLines,
                    progress = vm.progress,
                    currentFileName = vm.currentFileName,
                    isMerging = vm.isMerging,
                    isComplete = vm.isComplete,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp)
                )

                // ═══════ 冲突对话框 ═══════
                ConflictDialog()
                DeletionConflictDialog()
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// 顶部标题栏
// ═══════════════════════════════════════════════════════════════
@Composable
private fun HeaderBar(vm: MergeViewModel) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = CardDark,
        border = BorderStroke(1.dp, Purple.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(listOf(Purple.copy(alpha = 0.08f), CardDark))
                )
                .padding(horizontal = 20.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 标题
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "SuperModMerger",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Purple
                    )
                    Spacer(Modifier.width(10.dp))
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Purple.copy(alpha = 0.2f)
                    ) {
                        Text(
                            text = "v1.7.1",
                            fontSize = 11.sp,
                            color = Purple,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }
                Spacer(Modifier.height(2.dp))
                Text(
                    text = "Dying Light Mod 智能合并工具",
                    fontSize = 12.sp,
                    color = TextDim
                )
            }

            // 检测游戏按钮
            DetectGameButton(vm)
        }
    }
}

@Composable
private fun DetectGameButton(vm: MergeViewModel) {
    var showMenu by remember { mutableStateOf(false) }

    Box {
        Button(
            onClick = {
                if (vm.detectedGames.isEmpty()) {
                    vm.detectGame()
                } else {
                    showMenu = true
                }
            },
            enabled = !vm.isDetecting && !vm.isMerging,
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Purple,
                disabledContainerColor = BorderColor
            ),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
        ) {
            if (vm.isDetecting) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    strokeWidth = 2.dp,
                    color = Color.White
                )
                Spacer(Modifier.width(8.dp))
                Text("检测中...", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            } else {
                Text(
                    text = if (vm.detectionStatus.isNotEmpty()) "🔍 重新检测" else "🔍 自动检测游戏",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        // 检测到多个游戏时显示选择菜单
        if (showMenu && vm.detectedGames.size > 1) {
            DropdownMenu(
                expanded = showMenu,
                onDismissRequest = { showMenu = false }
            ) {
                vm.detectedGames.forEachIndexed { index, result ->
                    DropdownMenuItem(
                        text = {
                            Column {
                                Text(result.gameName, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                                Text(result.gamePath.toString(), fontSize = 11.sp, color = TextMuted, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }
                        },
                        onClick = {
                            vm.applyDetection(index)
                            showMenu = false
                        }
                    )
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// 路径配置卡片
// ═══════════════════════════════════════════════════════════════
@Composable
private fun PathConfigCard(vm: MergeViewModel) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = CardDark,
        border = BorderStroke(1.dp, BorderColor)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            // 标题行
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "📂 路径配置",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Purple
                )
                Spacer(Modifier.weight(1f))

                // 检测状态
                if (vm.detectionStatus.isNotEmpty()) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = when {
                            vm.detectionStatus.contains("成功") || vm.detectionStatus.contains("检测到") || vm.detectionStatus.contains("已应用") -> Green.copy(alpha = 0.15f)
                            vm.detectionStatus.contains("失败") || vm.detectionStatus.contains("未检测到") -> Red.copy(alpha = 0.15f)
                            else -> Orange.copy(alpha = 0.15f)
                        }
                    ) {
                        Text(
                            text = vm.detectionStatus,
                            fontSize = 11.sp,
                            color = when {
                                vm.detectionStatus.contains("成功") || vm.detectionStatus.contains("检测到") || vm.detectionStatus.contains("已应用") -> Green
                                vm.detectionStatus.contains("失败") || vm.detectionStatus.contains("未检测到") -> Red
                                else -> Orange
                            },
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.height(10.dp))
            HorizontalDivider(color = BorderColor, thickness = 1.dp)
            Spacer(Modifier.height(10.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                FileSelectorField(
                    label = "合并目录",
                    value = vm.modDirPath,
                    onValueChange = {
                        vm.modDirPath = it
                        vm.scanModDir(it)
                    },
                    isDirectory = true
                )
                FileSelectorField(
                    label = "基准MOD",
                    value = vm.baseModPath,
                    onValueChange = { vm.baseModPath = it },
                    isDirectory = true
                )
                FileSelectorField(
                    label = "输出路径",
                    value = vm.outputPath,
                    onValueChange = { vm.outputPath = it },
                    isDirectory = false
                )
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// 合并配置卡片（右侧）
// ═══════════════════════════════════════════════════════════════
@Composable
private fun MergeConfigCard(
    vm: MergeViewModel,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxHeight(),
        shape = RoundedCornerShape(12.dp),
        color = CardDark,
        border = BorderStroke(1.dp, BorderColor)
    ) {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            // 合并策略
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "⚙️ 合并策略",
                        fontSize = 13.sp,
                        color = Purple,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Spacer(Modifier.width(6.dp))
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = Purple.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = if (vm.globalFixMode) "全局修复（实验性）" else "标准",
                            fontSize = 10.sp,
                            color = Purple,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Spacer(Modifier.height(6.dp))
                HorizontalDivider(color = BorderColor, thickness = 1.dp)
                Spacer(Modifier.height(8.dp))

                // 普通模式
                StrategyOption(
                    selected = !vm.globalFixMode,
                    onClick = { vm.globalFixMode = false },
                    title = "普通模式",
                    desc = "仅对冲突文件进行合并，速度较快"
                )

                Spacer(Modifier.height(5.dp))

                // 全局修复模式
                StrategyOption(
                    selected = vm.globalFixMode,
                    onClick = { vm.globalFixMode = true },
                    title = "全局修复模式（实验性）",
                    desc = "对所有文件进行解析，可能修复过期MOD"
                )
            }

            Spacer(Modifier.height(5.dp))

            // 合并按钮
            Button(
                onClick = {
                    vm.reset()
                    startMerge(vm)
                },
                enabled = !vm.isMerging && !vm.isScanning && vm.modList.isNotEmpty(),
                modifier = Modifier.fillMaxWidth().requiredHeight(45.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Purple,
                    disabledContainerColor = BorderColor
                )
            ) {
                if (vm.isMerging) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = Color.White
                    )
                    Spacer(Modifier.width(10.dp))
                    Text("合并中...", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                } else if (vm.isComplete) {
                    Text("✅  合并完成", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Green)
                } else {
                    Text("🔄  开始合并", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun StrategyOption(
    selected: Boolean,
    onClick: () -> Unit,
    title: String,
    desc: String
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = if (selected) Purple.copy(alpha = 0.08f) else Color.Transparent,
        border = if (selected) BorderStroke(1.dp, Purple.copy(alpha = 0.3f)) else null,
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = selected,
                onClick = onClick,
                colors = RadioButtonDefaults.colors(selectedColor = Purple)
            )
            Column(modifier = Modifier.padding(start = 4.dp)) {
                Text(
                    text = title,
                    fontSize = 13.sp,
                    color = if (selected) TextBright else TextDim,
                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
                )
                if (desc.isNotEmpty()) {
                    Text(
                        text = desc,
                        fontSize = 10.sp,
                        color = TextMuted,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// 合并执行
// ═══════════════════════════════════════════════════════════════
private fun startMerge(vm: MergeViewModel) {
    vm.isMerging = true
    vm.addLog(MergeProgressCallback.Level.INFO, "开始合并...")
    vm.addLog(MergeProgressCallback.Level.INFO, "策略: ${if (vm.globalFixMode) "全局修复模式" else "普通模式"}")

    val modsToMerge = vm.modList.map { MergingModInfo(it.name, it.path) }

    val outputPath = if (vm.outputPath.isNotBlank()) {
        Path(vm.outputPath)
    } else {
        Path(Tools.userDir, "source", "data7.pak")
    }

    val basePakDirPath = if (vm.baseModPath.isNotBlank()) {
        Path(vm.baseModPath)
    } else {
        Path(Tools.userDir, "source")
    }

    GlobalMergingStrategy.activeMode = if (vm.globalFixMode) {
        GlobalMergingStrategy.GLOBAL_FIX_MODE
    } else {
        GlobalMergingStrategy.NORMAL_MODE
    }

    val progressCallback = object : MergeProgressCallback {
        override fun onLog(level: MergeProgressCallback.Level, message: String) {
            vm.addLog(level, message)
        }
        override fun onProgress(current: Int, total: Int, fileName: String) {
            vm.progress = if (total > 0) current.toFloat() / total else 0f
            vm.currentFileName = fileName
        }
        override fun onError(fileName: String, message: String) {
            vm.addLog(MergeProgressCallback.Level.ERROR, "[$fileName] $message")
        }
        override fun onComplete(totalProcessed: Int, mergedCount: Int, pathCorrectionCount: Int, errorCount: Int) {
            vm.totalProcessed = totalProcessed
            vm.mergedCount = mergedCount
            vm.errorCount = errorCount
            vm.progress = 1f
            vm.isComplete = true
            vm.isMerging = false
            vm.addLog(MergeProgressCallback.Level.SUCCESS, "合并完成！处理 $totalProcessed 个文件，合并 $mergedCount 个，$errorCount 个错误")
        }
    }

    Thread {
        try {
            FileMergerEngine(
                mergeableMods = modsToMerge,
                outputPath = outputPath,
                basePakDirPath = basePakDirPath,
                conflictStrategy = GuiConflictResolutionStrategy(
                    onLog = { level, msg -> vm.addLog(level, msg) }
                ),
                assetConflictResolver = ConsoleAssetConflictResolver,
                progressCallback = progressCallback
            ).merge()
        } catch (e: Exception) {
            vm.addLog(MergeProgressCallback.Level.ERROR, "合并失败: ${e.message}")
            vm.isMerging = false
        }
    }.apply { isDaemon = true }.start()
}
