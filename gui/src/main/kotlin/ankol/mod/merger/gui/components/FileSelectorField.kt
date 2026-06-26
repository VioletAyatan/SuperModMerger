package ankol.mod.merger.gui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.dialogs.compose.rememberDirectoryPickerLauncher
import io.github.vinceglb.filekit.dialogs.compose.rememberFilePickerLauncher

/**
 * 带浏览按钮的路径选择组件
 *
 * 使用 FileKit 调用平台原生对话框（Windows 上为现代 Win10/11 风格）
 *
 * @param label 标签文本
 * @param value 当前路径值
 * @param onValueChange 路径变化回调
 * @param isDirectory 是否选择目录
 * @param modifier Modifier
 */
@Composable
fun FileSelectorField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    isDirectory: Boolean = false,
    modifier: Modifier = Modifier
) {
    // 目录选择器
    val dirLauncher = rememberDirectoryPickerLauncher(
        onResult = { file: PlatformFile? ->
            file?.let { onValueChange(it.toString()) }
        }
    )

    // 文件选择器
    val fileLauncher = rememberFilePickerLauncher(
        onResult = { file: PlatformFile? ->
            file?.let { onValueChange(it.toString()) }
        }
    )

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.width(95.dp)
        )

        Spacer(Modifier.width(8.dp))

        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text("选择路径...", fontSize = 13.sp) },
            singleLine = true,
            textStyle = MaterialTheme.typography.bodySmall.copy(fontSize = 13.sp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline
            ),
            modifier = Modifier
                .weight(1f)
                .height(56.dp)
        )

        Spacer(Modifier.width(8.dp))

        Button(
            onClick = {
                if (isDirectory) {
                    dirLauncher.launch()
                } else {
                    fileLauncher.launch()
                }
            },
            shape = RoundedCornerShape(4.dp),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
            modifier = Modifier.height(36.dp)
        ) {
            Text("📂", fontSize = 14.sp)
        }
    }
}
