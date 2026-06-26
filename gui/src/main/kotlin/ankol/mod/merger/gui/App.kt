@file:OptIn(ExperimentalComposeUiApi::class)

package ankol.mod.merger.gui

import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import javax.swing.UIManager

fun main() {
    // 设置 Windows 原生外观，让 Swing 对话框（目录选择器）更美观
    try {
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())
    } catch (_: Exception) {}

    application {
        val windowState = rememberWindowState(width = 1025.dp, height = 950.dp)
        Window(
            onCloseRequest = ::exitApplication,
            title = "SuperModMerger v1.7.1",
            state = windowState
        ) {
            MainWindow()
        }
    }
}
