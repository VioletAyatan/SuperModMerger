# JavaFX 快速上手指南

## 核心概念

### 1. **Application & Stage & Scene** 三角形

```
Application（应用入口）
    └── Stage（窗口）
            └── Scene（场景）
                    └── Layout（布局）
                            └── Controls（控件）
```

**例子：**
```kotlin
class MyApp : Application() {
    override fun start(primaryStage: Stage) {
        val root = VBox()  // 布局
        val scene = Scene(root, 800, 600)
        primaryStage.apply {
            title = "我的应用"
            scene = scene
            show()
        }
    }
}
```

### 2. **布局系统** (Layouts)

JavaFX 有多种布局方式：

| 布局 | 用途 | 特点 |
|------|------|------|
| **VBox** | 竖直排列 | 控件垂直堆叠 |
| **HBox** | 水平排列 | 控件水平排列 |
| **BorderPane** | 五区域布局 | Top/Bottom/Left/Center/Right |
| **GridPane** | 网格布局 | 表格式排列 |
| **FlowPane** | 流式布局 | 自动换行 |
| **StackPane** | 叠层布局 | 控件重叠 |
| **AnchorPane** | 锚点布局 | 绝对定位 |

**常用示例：**
```kotlin
// VBox - 竖直排列
VBox(10.0).apply {  // 10.0 是控件间距
    padding = Insets(15.0)
    children.addAll(
        Label("标题"),
        TextField(),
        Button("确定")
    )
}

// BorderPane - 五区域
BorderPane().apply {
    top = Label("顶部")
    left = VBox()
    center = TextArea()
    right = VBox()
    bottom = Label("底部")
}

// GridPane - 网格
GridPane().apply {
    add(Label("用户名："), 0, 0)  // 列0, 行0
    add(TextField(), 1, 0)
    add(Label("密码："), 0, 1)
    add(PasswordField(), 1, 1)
}
```

### 3. **常用控件** (Controls)

| 控件 | 用途 | 示例 |
|------|------|------|
| **Label** | 显示文本 | `Label("Hello")` |
| **Button** | 按钮 | `Button("点击").setOnAction { ... }` |
| **TextField** | 单行文本输入 | `TextField().text` |
| **TextArea** | 多行文本输入 | `TextArea().text` |
| **CheckBox** | 复选框 | `CheckBox("同意条款")` |
| **RadioButton** | 单选按钮 | `RadioButton("选项1")` |
| **ComboBox** | 下拉菜单 | `ComboBox<String>()` |
| **ProgressBar** | 进度条 | `ProgressBar().progress = 0.5` |
| **Alert** | 对话框 | `Alert(AlertType.INFO).showAndWait()` |

**常用示例：**
```kotlin
// 按钮事件处理
Button("确定").apply {
    setOnAction { event ->
        println("按钮被点击")
    }
    // 或更简洁：
    setOnAction { println("点击") }
}

// 文本输入
val textField = TextField().apply {
    promptText = "请输入..."
}
println(textField.text)  // 获取内容

// 对话框
Alert(AlertType.INFORMATION).apply {
    title = "提示"
    headerText = "标题"
    contentText = "内容"
    showAndWait()
}
```

### 4. **样式设置** (CSS Styling)

**方式1：直接设置样式**
```kotlin
Button("确定").apply {
    style = "-fx-font-size: 14px; -fx-padding: 10px; -fx-background-color: #007ACC;"
}
```

**常见 CSS 属性：**
```
-fx-font-size: 14px          // 字体大小
-fx-font-weight: bold         // 字体粗细
-fx-text-fill: #333;          // 文字颜色
-fx-background-color: #FFF;   // 背景色
-fx-padding: 10px;            // 内边距
-fx-border-color: #CCC;       // 边框颜色
-fx-border-radius: 5px;       // 圆角
-fx-effect: dropshadow(...);  // 阴影效果
```

**方式2：外部 CSS 文件**
```kotlin
scene.stylesheets.add("styles.css")
```

### 5. **事件处理** (Event Handling)

```kotlin
// 按钮点击
button.setOnAction { event ->
    println("Button clicked")
}

// 文本变化监听
textField.textProperty().addListener { _, oldValue, newValue ->
    println("从 $oldValue 变为 $newValue")
}

// 窗口关闭
stage.setOnCloseRequest { event ->
    println("窗口要关闭了")
}

// 鼠标事件
node.setOnMouseClicked { event ->
    println("鼠标点击在 (${event.x}, ${event.y})")
}

// 键盘事件
scene.setOnKeyPressed { event ->
    if (event.code == KeyCode.ENTER) {
        println("按下 Enter")
    }
}
```

### 6. **数据绑定** (Property Binding)

```kotlin
val value = SimpleIntegerProperty(10)

// 双向绑定
val textField = TextField()
textField.textProperty().bindBidirectional(value.asString())

// 单向绑定（只读）
val label = Label()
label.textProperty().bind(textField.textProperty())

// 监听属性变化
value.addListener { _, old, new ->
    println("值从 $old 改为 $new")
}
```

## 快速开始步骤

### 第1步：编译
```bash
mvn clean compile
```

### 第2步：运行（使用 JavaFX Maven 插件）
```bash
# 运行 Java 版本
mvn javafx:run -Dexec.mainClass="ankol.mod.merger.gui.JavaFXGuiDemo"

# 或运行 Kotlin 版本
mvn javafx:run -Dexec.mainClass="ankol.mod.merger.gui.JavaFXGuiDemoKt"
```

### 第3步：打包成可执行 JAR
```bash
mvn package
java -jar target/SuperModMerger-1.4.0-all.jar
```

## 常见问题

### Q: 运行时报错 "javafx modules not found"？
A: 需要添加 JVM 参数：
```bash
mvn javafx:run --add-modules javafx.controls,javafx.fxml
```

### Q: 如何打开文件对话框？
```kotlin
val fileChooser = FileChooser().apply {
    title = "选择文件"
    extensionFilters.add(
        FileChooser.ExtensionFilter("文本文件", "*.txt")
    )
}
val file = fileChooser.showOpenDialog(stage)
if (file != null) {
    println("选择的文件：${file.absolutePath}")
}
```

### Q: 如何在后台线程执行任务？
```kotlin
Thread {
    // 耗时操作
    val result = doSomeHeavyWork()
    
    // 更新 UI（必须在 JavaFX 线程）
    Platform.runLater {
        label.text = result
    }
}.start()
```

### Q: 如何支持 GraalVM Native Image？
需要在 `pom.xml` 中配置反射元数据：
```xml
<buildArg>-H:+EnableAllSecurityServices</buildArg>
<buildArg>--initialize-at-build-time=javafx</buildArg>
```

## 推荐资源

- 官方文档：https://openjfx.io/
- CSS 参考：https://openjfx.io/javadoc/21/javafx.graphics/javafx/scene/doc-files/cssref.html
- 教程：https://www.youtube.com/watch?v=FLkOv5IS6g8

## 下一步计划

1. ✅ 创建基础 UI 框架
2. ⬜ 集成现有的合并逻辑
3. ⬜ 添加文件选择对话框
4. ⬜ 实现实时日志输出
5. ⬜ 优化 GraalVM 兼容性
