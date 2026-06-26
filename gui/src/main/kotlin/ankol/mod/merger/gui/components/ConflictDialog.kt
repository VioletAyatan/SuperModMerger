package ankol.mod.merger.gui.components

import ankol.mod.merger.constants.UserChoice
import ankol.mod.merger.gui.ConflictDialogState
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogWindow
import androidx.compose.ui.window.rememberDialogState

/**
 * 内容冲突解决对话框 — 左右双栏对比
 */
@Composable
fun ConflictDialog() {
    val conflict = ConflictDialogState.currentConflict
    if (!ConflictDialogState.isConflictDialogVisible || conflict == null) return

    DialogWindow(
        onCloseRequest = { ConflictDialogState.closeConflictDialog() },
        title = "🔀 冲突解决 (${ConflictDialogState.conflictNumber}/${ConflictDialogState.totalConflicts})",
        state = rememberDialogState(size = DpSize(900.dp, 600.dp))
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color(0xFF121220),
            border = BorderStroke(1.dp, Color(0xFF2D2D44))
        ) {
            Column(
                modifier = Modifier.fillMaxSize().padding(16.dp)
            ) {
                // ===== 文件信息 =====
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color(0xFF1E1E35)
                ) {
                    Text(
                        text = "📄 ${conflict.fileName}",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }

                Spacer(Modifier.height(8.dp))

                // ===== 签名信息 =====
                Text(
                    text = "签名: ${conflict.signature}",
                    fontSize = 11.sp,
                    color = Color(0xFF8888AA),
                    fontFamily = FontFamily.Monospace,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(Modifier.height(8.dp))

                // ===== 左右双栏对比 =====
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // 左侧：基准 Mod
                    SourceCodePanel(
                        modName = conflict.baseModName,
                        sourceText = conflict.baseNode.sourceText,
                        lineNumber = conflict.baseNode.lineNumber,
                        accentColor = Color(0xFF7C5CFC),
                        modifier = Modifier.weight(1f).fillMaxHeight()
                    )

                    // 右侧：合并 Mod
                    SourceCodePanel(
                        modName = conflict.mergeModName,
                        sourceText = conflict.modNode.sourceText,
                        lineNumber = conflict.modNode.lineNumber,
                        accentColor = Color(0xFF44DD88),
                        modifier = Modifier.weight(1f).fillMaxHeight()
                    )
                }

                Spacer(Modifier.height(12.dp))

                // ===== 操作按钮 =====
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF1A1A2E),
                    border = BorderStroke(1.dp, Color(0xFF2D2D44))
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "选择版本:",
                            fontSize = 12.sp,
                            color = Color(0xFF8888AA)
                        )

                        Spacer(Modifier.weight(1f))

                        // 用左侧
                        Button(
                            onClick = { ConflictDialogState.resolveConflictChoice(UserChoice.BASE_MOD) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C5CFC)),
                            shape = RoundedCornerShape(6.dp),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp)
                        ) {
                            Text("◀  用左侧", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        }

                        // 用右侧
                        Button(
                            onClick = { ConflictDialogState.resolveConflictChoice(UserChoice.MERGE_MOD) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF44DD88)),
                            shape = RoundedCornerShape(6.dp),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp)
                        ) {
                            Text("用右侧  ▶", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF0D0D1A))
                        }

                        // 全部用左侧
                        OutlinedButton(
                            onClick = { ConflictDialogState.resolveConflictChoice(UserChoice.USE_ALL_BASE) },
                            shape = RoundedCornerShape(6.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF8888AA))
                        ) {
                            Text("全部左", fontSize = 12.sp)
                        }

                        // 全部用右侧
                        OutlinedButton(
                            onClick = { ConflictDialogState.resolveConflictChoice(UserChoice.USE_ALL_MERGE) },
                            shape = RoundedCornerShape(6.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF8888AA))
                        ) {
                            Text("全部右", fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

/**
 * 源代码展示面板（左右两侧共用）
 */
@Composable
private fun SourceCodePanel(
    modName: String,
    sourceText: String,
    lineNumber: Int,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(6.dp),
        color = Color(0xFF0D0D1A),
        border = BorderStroke(1.dp, Color(0xFF2D2D44))
    ) {
        Column(modifier = Modifier.fillMaxSize().padding(8.dp)) {
            // MOD 名称标签
            Surface(
                shape = RoundedCornerShape(4.dp),
                color = accentColor.copy(alpha = 0.2f)
            ) {
                Text(
                    text = modName,
                    fontSize = 11.sp,
                    color = accentColor,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                )
            }

            Spacer(Modifier.height(6.dp))

            // 代码内容（滚动）
            val scrollState = rememberScrollState()
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .background(Color(0xFF0A0A16))
            ) {
                Column(modifier = Modifier.padding(4.dp)) {
                    // 行号 + 代码
                    Row(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // 行号
                        Text(
                            text = lineNumber.toString().padStart(4),
                            fontSize = 12.sp,
                            color = Color(0xFF444466),
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.width(36.dp)
                        )
                        // 代码
                        Text(
                            text = sourceText.replace("\t", "    "),
                            fontSize = 12.sp,
                            color = Color(0xFFE0E0F0),
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    // 差异标记
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = "───── 差异行 ─────",
                        fontSize = 10.sp,
                        color = accentColor.copy(alpha = 0.5f),
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    }
}
