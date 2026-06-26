package ankol.mod.merger.gui.components

import ankol.mod.merger.gui.MergeViewModel
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * 待合并 MOD 列表面板
 */
@Composable
fun ModListPanel(
    modList: List<MergeViewModel.ModEntry>,
    isScanning: Boolean,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = Color(0xFF1A1A2E),
        border = BorderStroke(1.dp, Color(0xFF2D2D44))
    ) {
        Column(modifier = Modifier.fillMaxSize().padding(8.dp)) {
            // 标题 + 计数
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 4.dp)
            ) {
                Text(
                    text = "📦 待合并 MOD",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
                if (modList.isNotEmpty()) {
                    Spacer(Modifier.width(8.dp))
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                    ) {
                        Text(
                            text = "${modList.size}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            if (isScanning) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text("扫描中...", fontSize = 13.sp, color = Color(0xFF8888AA))
                    }
                }
            } else if (modList.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "请先选择 MOD 目录",
                        fontSize = 13.sp,
                        color = Color(0xFF555570)
                    )
                }
            } else {
                val scrollState = rememberScrollState()
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                ) {
                    modList.forEachIndexed { index, mod ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp, horizontal = 4.dp)
                        ) {
                            // 序号
                            Text(
                                text = "${index + 1}.",
                                fontSize = 12.sp,
                                color = Color(0xFF666680),
                                modifier = Modifier.width(24.dp)
                            )
                            // 文件图标
                            val icon = when {
                                mod.name.endsWith(".pak") -> "📦"
                                mod.name.endsWith(".zip") -> "🗜️"
                                mod.name.endsWith(".7z") -> "🗜️"
                                mod.name.endsWith(".rar") -> "🗜️"
                                else -> "📄"
                            }
                            Text(
                                text = icon,
                                fontSize = 12.sp,
                                modifier = Modifier.width(20.dp)
                            )
                            // 文件名
                            Text(
                                text = mod.name,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    }
}
