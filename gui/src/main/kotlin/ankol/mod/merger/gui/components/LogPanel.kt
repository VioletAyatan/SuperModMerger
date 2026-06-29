package ankol.mod.merger.gui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ankol.mod.merger.api.MergeProgressCallback
import ankol.mod.merger.gui.MergeViewModel

/**
 * 日志面板 + 进度条组件
 */
@Composable
fun LogPanel(
    logLines: List<MergeViewModel.LogLine>,
    progress: Float,
    currentFileName: String,
    isMerging: Boolean,
    isComplete: Boolean,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = Color(0xFF1A1A2E),
        border = BorderStroke(1.dp, Color(0xFF2D2D44))
    ) {
        Column(modifier = Modifier.fillMaxSize().padding(8.dp)) {
            // 标题
            Text(
                text = "📋 日志输出",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 4.dp)
            )

            // 滚动日志区域
            val scrollState = rememberScrollState()
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                ) {
                    if (logLines.isEmpty()) {
                        Text(
                            text = "等待操作...",
                            fontSize = 12.sp,
                            color = Color(0xFF666680),
                            fontFamily = FontFamily.Monospace
                        )
                    } else {
                        // 自动滚动到底部
                        LaunchedEffect(logLines.size) {
                            scrollState.animateScrollTo(scrollState.maxValue)
                        }
                        logLines.forEach { line ->
                            Text(
                                text = "[${line.timestamp}] ${line.message}",
                                fontSize = 12.sp,
                                color = logLevelColor(line.level),
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(4.dp))

            // 进度条
            if (isMerging || isComplete) {
                Column {
                    // 当前文件信息
                    if (currentFileName.isNotEmpty()) {
                        Text(
                            text = currentFileName,
                            fontSize = 11.sp,
                            color = Color(0xFF8888AA),
                            fontFamily = FontFamily.Monospace,
                            maxLines = 1
                        )
                    }

                    Spacer(Modifier.height(2.dp))

                    // 进度条
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.fillMaxWidth().height(6.dp),
                        color = if (isComplete) Color(0xFF4CAF50) else MaterialTheme.colorScheme.primary,
                        trackColor = Color(0xFF2D2D44),
                    )

                    // 进度百分比
                    Text(
                        text = "${(progress * 100).toInt()}%",
                        fontSize = 11.sp,
                        color = Color(0xFF8888AA),
                        modifier = Modifier.align(Alignment.End)
                    )
                }
            }
        }
    }
}

/** 日志级别对应的颜色 */
private fun logLevelColor(level: MergeProgressCallback.Level): Color {
    return when (level) {
        MergeProgressCallback.Level.DEBUG -> Color(0xFF8888AA)
        MergeProgressCallback.Level.INFO -> Color(0xFFCCCCDD)
        MergeProgressCallback.Level.WARN -> Color(0xFFFFAA44)
        MergeProgressCallback.Level.ERROR -> Color(0xFFFF5555)
        MergeProgressCallback.Level.SUCCESS -> Color(0xFF44DD88)
    }
}
