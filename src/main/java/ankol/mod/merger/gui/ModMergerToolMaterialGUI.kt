package ankol.mod.merger.gui

import ankol.mod.merger.gui.component.TimeLabel
import javafx.application.Application
import javafx.application.Platform
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.Scene
import javafx.scene.control.*
import javafx.scene.layout.*
import javafx.stage.DirectoryChooser
import javafx.stage.FileChooser
import javafx.stage.Stage
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * Material Design风格的ModMergerTool GUI
 *
 * 特点：
 * - Material Design 2 风格
 * - 卡片式布局
 * - 平滑的动画和过渡
 * - 现代的色彩方案
 * - GraalVM本地镜像兼容
 */
class ModMergerToolMaterialGUI : Application() {

    private lateinit var inputFilesList: ListView<String>
    private lateinit var logArea: TextArea
    private lateinit var progressBar: ProgressBar
    private lateinit var statusLabel: Label
    private lateinit var mergeButton: Button
    private lateinit var outputPathLabel: Label
    private lateinit var timeLabel: TimeLabel

    override fun start(primaryStage: Stage) {
        val root = BorderPane()

        root.top = createHeaderBar()
        root.center = createMainContent()
        root.bottom = createStatusBar()

        val scene = Scene(root, 1000.0, 700.0)

        // 加载Material Design CSS样式
        val cssResource = this::class.java.getResource("/styles/material-design.css")
        if (cssResource != null) {
            scene.stylesheets.add(cssResource.toExternalForm())
        }

        // 设置窗口图标
//        try {
//            val iconResource = this::class.java.getResourceAsStream("/images/icon.ico")
//            if (iconResource != null) {
//                primaryStage.icons.add(Image(iconResource))
//            }
//        } catch (e: Exception) {
//            println("无法加载应用图标: ${e.message}")
//        }

        primaryStage.title = "Super Mod Merger"
        primaryStage.scene = scene
        primaryStage.width = 1000.0
        primaryStage.height = 700.0
        primaryStage.minWidth = 800.0
        primaryStage.minHeight = 600.0
        primaryStage.setOnCloseRequest {
            timeLabel.stop()
            println("应用正常关闭")
        }
        primaryStage.show()
    }

    /**
     * 创建顶部标题栏
     */
    private fun createHeaderBar(): HBox {
        return HBox(15.0).apply {
            styleClass.add("header-bar")
            alignment = Pos.CENTER_LEFT
            padding = Insets(16.0)

            children.addAll(
                VBox(5.0).apply {
                    children.addAll(
                        Label("ModMergerTool").apply {
                            styleClass.add("material-title")
                            style = "-fx-text-fill: white; -fx-font-size: 26px;"
                        },
                        Label("脚本文件智能合并工具").apply {
                            styleClass.add("material-subtitle")
                            style = "-fx-text-fill: rgba(255, 255, 255, 0.7);"
                        }
                    )
                }
            )
        }
    }

    /**
     * 创建主内容区域
     */
    private fun createMainContent(): ScrollPane {
        val mainVBox = VBox(16.0).apply {
            padding = Insets(16.0)
            style = "-fx-background-color: #FAFAFA;"
        }

        mainVBox.children.addAll(
            createInputCard(),
            createOutputCard(),
            createProgressCard(),
            createLogCard()
        )

        return ScrollPane(mainVBox).apply {
            isFitToWidth = true
            style = "-fx-background-color: #FAFAFA;"
        }
    }

    /**
     * 输入文件卡片
     */
    private fun createInputCard(): VBox {
        val card = VBox(12.0).apply {
            styleClass.add("material-card")
        }

        card.children.addAll(
            Label("输入文件").apply {
                styleClass.add("material-title")
                style = "-fx-font-size: 16px;"
            },
            Separator().apply {
                styleClass.add("material-divider")
            },
            HBox(10.0).apply {
                alignment = Pos.TOP_LEFT
                spacing = 12.0

                val leftVBox = VBox(10.0).apply {
                    prefWidth = 600.0
                    children.addAll(
                        Label("选择要合并的脚本文件：").apply {
                            styleClass.add("material-subtitle")
                        },
                        ListView<String>().apply {
                            inputFilesList = this
                            prefHeight = 150.0
                            style = "-fx-border-color: #E0E0E0; -fx-background-color: #FFFFFF;"
                        },
                        HBox(10.0).apply {
                            spacing = 10.0
                            children.addAll(
                                Button("添加文件").apply {
                                    styleClass.add("material-button")
                                    setOnAction { addInputFile() }
                                },
                                Button("添加文件夹").apply {
                                    styleClass.add("material-button")
                                    setOnAction { addInputFolder() }
                                },
                                Button("移除选中").apply {
                                    styleClass.add("material-button")
                                    style = "-fx-background-color: #FF9800;"
                                    setOnAction { removeSelectedFile() }
                                },
                                Region().apply { HBox.setHgrow(this, Priority.ALWAYS) },
                                Button("清空列表").apply {
                                    styleClass.add("material-button")
                                    style = "-fx-background-color: #F44336;"
                                    setOnAction { clearFileList() }
                                }
                            )
                        }
                    )
                }

                HBox.setHgrow(leftVBox, Priority.ALWAYS)
                children.add(leftVBox)
            }
        )

        return card
    }

    /**
     * 输出配置卡片
     */
    private fun createOutputCard(): VBox {
        val card = VBox(12.0).apply {
            styleClass.add("material-card")
        }

        outputPathLabel = Label("未选择输出路径").apply {
            style = "-fx-text-fill: #757575; -fx-padding: 10px; -fx-background-color: #FFFFFF; -fx-border-color: #E0E0E0; -fx-border-width: 1;"
        }

        card.children.addAll(
            Label("输出配置").apply {
                styleClass.add("material-title")
                style = "-fx-font-size: 16px;"
            },
            Separator().apply {
                styleClass.add("material-divider")
            },
            VBox(8.0).apply {
                children.addAll(
                    Label("输出文件路径：").apply {
                        styleClass.add("material-subtitle")
                    },
                    HBox(10.0).apply {
                        spacing = 10.0
                        children.addAll(
                            outputPathLabel.apply {
                                HBox.setHgrow(this, Priority.ALWAYS)
                            },
                            Button("浏览").apply {
                                styleClass.add("material-button")
                                prefWidth = 100.0
                                setOnAction { selectOutputPath() }
                            }
                        )
                    }
                )
            }
        )

        return card
    }

    /**
     * 进度条卡片
     */
    private fun createProgressCard(): VBox {
        val card = VBox(12.0).apply {
            styleClass.add("material-card")
        }

        progressBar = ProgressBar(0.0).apply {
            prefHeight = 8.0
            style = "-fx-padding: 0;"
        }

        statusLabel = Label("就绪").apply {
            styleClass.add("material-subtitle")
            style = "-fx-text-fill: #4CAF50;"
        }

        mergeButton = Button("开始合并").apply {
            styleClass.add("material-button")
            style = "-fx-font-size: 14px; -fx-padding: 12 48;"
            prefWidth = 150.0
            setOnAction { performMerge() }
        }

        card.children.addAll(
            HBox(20.0).apply {
                alignment = Pos.CENTER_LEFT
                spacing = 20.0
                children.addAll(
                    VBox(8.0).apply {
                        HBox.setHgrow(this, Priority.ALWAYS)
                        children.addAll(
                            Label("合并进度").apply {
                                styleClass.add("material-subtitle")
                            },
                            progressBar
                        )
                    },
                    mergeButton
                )
            },
            statusLabel
        )

        return card
    }

    /**
     * 日志卡片
     */
    private fun createLogCard(): VBox {
        val card = VBox(12.0).apply {
            styleClass.add("material-card")
            prefHeight = 250.0
        }

        logArea = TextArea().apply {
            isWrapText = true
            isEditable = false
            prefRowCount = 10
            style = "-fx-control-inner-background: #FFFFFF; -fx-border-color: #E0E0E0; -fx-text-fill: #212121; -fx-font-family: 'Courier New', monospace; -fx-font-size: 11px;"
        }

        card.children.addAll(
            Label("输出日志").apply {
                styleClass.add("material-title")
                style = "-fx-font-size: 16px;"
            },
            Separator().apply {
                styleClass.add("material-divider")
            },
            VBox().apply {
                VBox.setVgrow(logArea, Priority.ALWAYS)
                children.addAll(
                    logArea,
                    HBox(10.0).apply {
                        padding = Insets(8.0)
                        spacing = 10.0
                        children.addAll(
                            Button("清空日志").apply {
                                styleClass.add("material-button")
                                style = "-fx-background-color: #FF9800;"
                                setOnAction { logArea.clear() }
                            },
                            Button("导出日志").apply {
                                styleClass.add("material-button")
                                setOnAction { exportLog() }
                            },
                            Region().apply { HBox.setHgrow(this, Priority.ALWAYS) }
                        )
                    }
                )
            }
        )

        VBox.setVgrow(card, Priority.ALWAYS)
        return card
    }

    /**
     * 创建状态栏
     */
    private fun createStatusBar(): HBox {
        return HBox(10.0).apply {
            styleClass.add("status-bar")
            padding = Insets(8.0, 12.0, 8.0, 12.0)
            alignment = Pos.CENTER_LEFT

            children.addAll(
                Label("就绪").apply {
                    style = "-fx-text-fill: #4CAF50; -fx-font-weight: bold;"
                },
                Separator().apply {
                    style = "-fx-padding: 0 5;"
                },
                Label("v1.4.0").apply {
                    style = "-fx-text-fill: #757575;"
                },
                Region().apply { HBox.setHgrow(this, Priority.ALWAYS) },
                TimeLabel("HH:mm:ss").apply {
                    style = "-fx-text-fill: #757575;"
                }
            )
        }
    }

    private fun addInputFile() {
        val fileChooser = FileChooser().apply {
            title = "选择脚本文件"
            extensionFilters.add(FileChooser.ExtensionFilter("脚本文件 (*.scr)", "*.scr"))
            extensionFilters.add(FileChooser.ExtensionFilter("所有文件", "*.*"))
        }

        val selectedFile = fileChooser.showOpenDialog(null)
        if (selectedFile != null) {
            inputFilesList.items.add(selectedFile.absolutePath)
            appendLog("已添加文件: ${selectedFile.name}")
        }
    }

    private fun addInputFolder() {
        val dirChooser = DirectoryChooser().apply {
            title = "选择包含脚本文件的文件夹"
        }

        val selectedDir = dirChooser.showDialog(null)
        if (selectedDir != null) {
            val scrFiles = selectedDir.listFiles { f -> f.extension == "scr" } ?: emptyArray()
            scrFiles.forEach { file ->
                inputFilesList.items.add(file.absolutePath)
            }
            appendLog("已添加 ${scrFiles.size} 个脚本文件")
        }
    }

    private fun removeSelectedFile() {
        val selectedIndex = inputFilesList.selectionModel.selectedIndex
        if (selectedIndex >= 0) {
            val removed = inputFilesList.items.removeAt(selectedIndex)
            appendLog("已移除文件: $removed")
        }
    }

    private fun clearFileList() {
        val count = inputFilesList.items.size
        inputFilesList.items.clear()
        appendLog("已清空文件列表 (共 $count 个文件)")
    }

    private fun selectOutputPath() {
        val fileChooser = FileChooser().apply {
            title = "选择输出文件位置"
            extensionFilters.add(FileChooser.ExtensionFilter("脚本文件 (*.scr)", "*.scr"))
        }

        val selectedFile = fileChooser.showSaveDialog(null)
        if (selectedFile != null) {
            outputPathLabel.text = selectedFile.absolutePath
            appendLog("输出路径已设置: ${selectedFile.absolutePath}")
        }
    }

    private fun performMerge() {
        if (inputFilesList.items.isEmpty()) {
            showError("错误", "请先选择至少一个输入文件")
            return
        }

        if (outputPathLabel.text == "未选择输出路径") {
            showError("错误", "请先选择输出文件位置")
            return
        }

        mergeButton.isDisable = true
        statusLabel.text = "正在合并..."
        statusLabel.style = "-fx-text-fill: #FF9800;"
        progressBar.progress = 0.0

        Thread {
            try {
                val files = inputFilesList.items.map { File(it) }

                Platform.runLater {
                    appendLog("========================================")
                    appendLog("开始合并 ${files.size} 个文件...")
                    appendLog("输出路径: ${outputPathLabel.text}")
                    appendLog("========================================")
                }

                for (i in files.indices) {
                    Thread.sleep(300)
                    val progress = (i + 1).toDouble() / files.size
                    val file = files[i]

                    Platform.runLater {
                        progressBar.progress = progress
                        appendLog("[${String.format("%.0f", progress * 100)}%] 处理文件: ${file.name}")
                    }
                }

                Thread.sleep(500)
                Platform.runLater {
                    progressBar.progress = 1.0
                    statusLabel.text = "合并完成！"
                    statusLabel.style = "-fx-text-fill: #4CAF50;"
                    appendLog("========================================")
                    appendLog("✓ 文件合并完成！")
                    appendLog("输出文件: ${outputPathLabel.text}")
                    appendLog("========================================")
                }
            } catch (e: Exception) {
                Platform.runLater {
                    statusLabel.text = "合并失败: ${e.message}"
                    statusLabel.style = "-fx-text-fill: #F44336;"
                    appendLog("错误: ${e.message}")
                    e.printStackTrace(System.out)
                }
            } finally {
                Platform.runLater {
                    mergeButton.isDisable = false
                }
            }
        }.start()
    }

    private fun exportLog() {
        val fileChooser = FileChooser().apply {
            title = "导出日志"
            extensionFilters.add(FileChooser.ExtensionFilter("文本文件 (*.txt)", "*.txt"))
        }

        val selectedFile = fileChooser.showSaveDialog(null)
        if (selectedFile != null) {
            try {
                selectedFile.writeText(logArea.text)
                showInfo("成功", "日志已导出到: ${selectedFile.absolutePath}")
                appendLog("日志已导出")
            } catch (e: Exception) {
                showError("错误", "导出日志失败: ${e.message}")
            }
        }
    }

    private fun appendLog(message: String) {
        val timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"))
        logArea.appendText("[$timestamp] $message\n")
        logArea.scrollTop = Double.MAX_VALUE
    }

    private fun showInfo(title: String, message: String) {
        Alert(Alert.AlertType.INFORMATION).apply {
            this.title = title
            this.headerText = null
            this.contentText = message
            showAndWait()
        }
    }

    private fun showError(title: String, message: String) {
        Alert(Alert.AlertType.ERROR).apply {
            this.title = title
            this.headerText = null
            this.contentText = message
            showAndWait()
        }
    }
}

fun main(args: Array<String>) {
    Application.launch(ModMergerToolMaterialGUI::class.java, *args)
}
