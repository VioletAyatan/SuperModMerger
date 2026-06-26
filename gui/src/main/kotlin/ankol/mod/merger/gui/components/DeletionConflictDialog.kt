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
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogWindow
import androidx.compose.ui.window.rememberDialogState

/**
 * 删除冲突解决对话框
 */
@Composable
fun DeletionConflictDialog() {
    val deletion = ConflictDialogState.currentDeletion
    if (!ConflictDialogState.isDeletionDialogVisible || deletion == null) return

    DialogWindow(
        onCloseRequest = { ConflictDialogState.closeDeletionDialog() },
        title = "🗑️ 删除冲突 (${ConflictDialogState.deletionNumber}/${ConflictDialogState.totalDeletions})",
        state = rememberDialogState(size = DpSize(700.dp, 400.dp))
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
                        text = "📄 ${deletion.fileName}",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }

                Spacer(Modifier.height(8.dp))

                // ===== 冲突说明 =====
                if (deletion.isModifyDeleteConflict) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFFFFAA44).copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = "⚠️ 修改-删除冲突：\"${deletion.deletingModName}\" 试图删除此节点，但 \"${deletion.previousModName}\" 已对其进行了修改",
                            fontSize = 12.sp,
                            color = Color(0xFFFFAA44),
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                }

                // ===== MOD 信息 =====
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    InfoChip("执行删除", deletion.deletingModName, Color(0xFFFF5555))
                    InfoChip("最后修改", deletion.previousModName, Color(0xFF8888AA))
                    InfoChip("签名", deletion.signature, Color(0xFF7C5CFC))
                }

                Spacer(Modifier.height(8.dp))

                // ===== 节点代码 =====
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(6.dp),
                    color = Color(0xFF0D0D1A),
                    border = BorderStroke(1.dp, Color(0xFF2D2D44))
                ) {
                    Column(modifier = Modifier.fillMaxSize().padding(8.dp)) {
                        Text(
                            text = "待删除的节点内容:",
                            fontSize = 11.sp,
                            color = Color(0xFF8888AA),
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )

                        val scrollState = rememberScrollState()
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(scrollState)
                                .background(Color(0xFF0A0A16))
                        ) {
                            Row(modifier = Modifier.padding(4.dp)) {
                                Text(
                                    text = deletion.accumulatedNode.lineNumber.toString().padStart(4),
                                    fontSize = 12.sp,
                                    color = Color(0xFF444466),
                                    fontFamily = FontFamily.Monospace,
                                    modifier = Modifier.width(36.dp)
                                )
                                Text(
                                    text = deletion.accumulatedNode.sourceText.replace("\t", "    "),
                                    fontSize = 12.sp,
                                    color = Color(0xFFE0E0F0),
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }
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
                            text = "操作:",
                            fontSize = 12.sp,
                            color = Color(0xFF8888AA)
                        )

                        Spacer(Modifier.weight(1f))

                        // 保留
                        Button(
                            onClick = { ConflictDialogState.resolveDeletionChoice(UserChoice.BASE_MOD) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C5CFC)),
                            shape = RoundedCornerShape(6.dp),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp)
                        ) {
                            Text("✓  保留节点", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        }

                        // 删除
                        Button(
                            onClick = { ConflictDialogState.resolveDeletionChoice(UserChoice.MERGE_MOD) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5555)),
                            shape = RoundedCornerShape(6.dp),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp)
                        ) {
                            Text("✗  删除节点", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        }

                        // 全部保留
                        OutlinedButton(
                            onClick = { ConflictDialogState.resolveDeletionChoice(UserChoice.USE_ALL_BASE) },
                            shape = RoundedCornerShape(6.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF8888AA))
                        ) {
                            Text("全部保留", fontSize = 12.sp)
                        }

                        // 全部删除
                        OutlinedButton(
                            onClick = { ConflictDialogState.resolveDeletionChoice(UserChoice.USE_ALL_MERGE) },
                            shape = RoundedCornerShape(6.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF8888AA))
                        ) {
                            Text("全部删除", fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun InfoChip(label: String, value: String, color: Color) {
    Surface(
        shape = RoundedCornerShape(4.dp),
        color = color.copy(alpha = 0.15f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "$label: ",
                fontSize = 11.sp,
                color = Color(0xFF8888AA)
            )
            Text(
                text = value,
                fontSize = 11.sp,
                color = color,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
