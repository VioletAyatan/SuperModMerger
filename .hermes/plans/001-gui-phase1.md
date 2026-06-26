# Phase 1 — 核心接口抽象 + GUI 框架搭建

## 目标
在**不改动现有 CLI 行为**的前提下，抽象出可被 GUI 替代的交互接口，并搭建 Compose Desktop 基本框架。

---

## 1. 接口抽象（改动现有代码）

### 1.1 新增 `api/` 包

新建目录 `src/main/java/ankol/mod/merger/api/`，存放所有接口定义。

#### `api/ConflictResolutionStrategy.kt`
```kotlin
interface ConflictResolutionStrategy {
    fun resolveConflict(conflicts: MutableList<ConflictRecord>)
    fun resolveDeletionConflicts(deletions: MutableList<DeletionRecord>)
}
```

#### `api/MergingStrategySelector.kt`
```kotlin
interface MergingStrategySelector {
    fun askStrategy(): GlobalMergingStrategy
}
```

#### `api/AssetConflictResolver.kt`
```kotlin
interface AssetConflictResolver {
    fun chooseAsset(relPath: String, sources: MutableList<PathFileTree>, mergedDir: Path)
}
```

#### `api/MergeProgressCallback.kt`
```kotlin
interface MergeProgressCallback {
    fun onLog(level: LogLevel, message: String)
    fun onProgress(current: Int, total: Int, fileName: String)
    fun onError(fileName: String, message: String)
    fun onComplete(result: MergeStatistics)
}

enum class LogLevel { DEBUG, INFO, WARN, ERROR, SUCCESS }

data class MergeStatistics(
    val totalProcessed: Int,
    val mergedCount: Int,
    val pathCorrectionCount: Int,
    val errors: List<String>
)
```

### 1.2 改造现有类实现接口

#### `ConflictResolver` → 实现 `ConflictResolutionStrategy`
- 让 `object ConflictResolver` 实现 `ConflictResolutionStrategy`
- 代码几乎不变，只是标记 `object ConflictResolver : ConflictResolutionStrategy`

#### `GlobalMergingStrategy` → 关联 `MergingStrategySelector`
- 将 `askCodeMergingStrategy()` 方法提取到 `object ConsoleMergingStrategySelector : MergingStrategySelector`
- `GlobalMergingStrategy.askCodeMergingStrategy()` 改为委托给 `ConsoleMergingStrategySelector`

#### `FileMergerEngine` 构造函数增加可选的策略参数
```kotlin
class FileMergerEngine(
    private val mergeableMods: List<MergingModInfo>,
    private val outputPath: Path,
    private val basePakDirPath: Path,
    private val conflictStrategy: ConflictResolutionStrategy = ConflictResolver,
    private val strategySelector: MergingStrategySelector = ConsoleMergingStrategySelector,
    private val assetResolver: AssetConflictResolver = ConsoleAssetConflictResolver,
    private val progressCallback: MergeProgressCallback? = null
)
```

**关键设计**：所有参数都有默认值（= 现有控制台行为），所以 CLI 的 `AppMain` 调用处 **完全不需要修改**。

#### 引擎中触发回调
在 `mergeAllFiles()` 循环中调用 `progressCallback.onProgress(...)`，在 `ColorPrinter` 的输出处增加 `progressCallback.onLog(...)`，在异常处调用 `progressCallback.onError(...)`。

### 1.3 验证 CLI 构建不受影响
- `mvn clean compile` 正常
- `ConflictResolver` 原有行为不变（默认参数）
- `GlobalMergingStrategy.askCodeMergingStrategy()` 行为不变

---

## 2. GUI Gradle 模块搭建

### 2.1 创建目录结构
```
gui/
├── build.gradle.kts
├── settings.gradle.kts
├── gradle.properties
├── gradlew
├── gradlew.bat
├── gradle/
│   └── wrapper/
│       ├── gradle-wrapper.jar
│       └── gradle-wrapper.properties
└── src/
    └── main/
        └── kotlin/
            └── ankol/
                └── mod/
                    └── merger/
                        └── gui/
                            ├── App.kt           ← Compose 入口
                            ├── MainWindow.kt    ← 主窗口布局
                            ├── GuiConflictResolver.kt   ← GUI 冲突解决
                            ├── GuiMergeProgress.kt      ← GUI 进度展示
                            └── theme/
                                └── Theme.kt    ← 主题/颜色
```

### 2.2 `build.gradle.kts`
```kotlin
import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    kotlin("jvm") version "2.3.0"
    id("org.jetbrains.compose") version "1.11.1"
    id("org.jetbrains.kotlin.plugin.compose") version "2.3.0"
}

group = "ankol.mod.merger"
version = "1.7.1"

repositories {
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    google()
}

dependencies {
    // 依赖 CLI 编译后的 fat-jar
    implementation(files("../target/SuperModMerger-1.7.1-SNAPSHOT-all.jar"))

    // Compose Desktop
    implementation(compose.desktop.currentOs)
    implementation(compose.material3)
    
    // 日志桥接（GUI 面板捕获 SLF4J 输出）
    implementation("ch.qos.logback:logback-classic:1.5.32")
}

compose.desktop {
    application {
        mainClass = "ankol.mod.merger.gui.AppKt"
        
        nativeDistributions {
            targetFormats(TargetFormat.Msi, TargetFormat.Exe)
            packageName = "SuperModMerger"
            packageVersion = "1.7.1"
            vendor = "Ankol"
            description = "Dying Light Mod Merger Tool"
            
            windows {
                menuGroup = "SuperModMerger"
                upgradeUuid = "a1b2c3d4-e5f6-7890-abcd-ef1234567890"
                iconFile.set(project.file("../builds/icon.ico"))
            }
        }
    }
}
```

### 2.3 `settings.gradle.kts`
```kotlin
pluginManagement {
    repositories {
        gradlePluginPortal()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
        google()
        mavenCentral()
    }
}

rootProject.name = "SuperModMerger-GUI"
```

### 2.4 `App.kt` — 入口
```kotlin
package ankol.mod.merger.gui

import androidx.compose.ui.resource.resource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState

fun main() = application {
    val windowState = rememberWindowState(width = 960.dp, height = 720.dp)
    Window(
        onCloseRequest = ::exitApplication,
        title = "SuperModMerger v1.7.1",
        state = windowState
    ) {
        MainWindow()
    }
}
```

### 2.5 `MainWindow.kt` — 主窗口（骨架）
```kotlin
@Composable
fun MainWindow() {
    MaterialTheme {
        // TODO: 后续填充完整布局
        Text("SuperModMerger GUI - Coming Soon")
    }
}
```

---

## 3. 构建与运行验证

### 3.1 CLI 验证
```bash
cd /d/Projects/Mergers/SuperModMerger
mvn clean package -DskipTests
# → 生成 target/SuperModMerger-1.7.1-SNAPSHOT-all.jar
```

### 3.2 GUI 验证
```bash
cd gui
./gradlew run
# → Compose Desktop 窗口弹出
```

### 3.3 Git 忽略
在 `.gitignore` 中添加：
```
gui/gradle/
gui/gradlew
gui/gradlew.bat
gui/build/
```

---

## 文件变更清单

| 操作 | 文件 | 说明 |
|------|------|------|
| 🆕 | `src/.../api/ConflictResolutionStrategy.kt` | 冲突解决接口 |
| 🆕 | `src/.../api/MergingStrategySelector.kt` | 策略选择接口 |
| 🆕 | `src/.../api/AssetConflictResolver.kt` | 资源冲突接口 |
| 🆕 | `src/.../api/MergeProgressCallback.kt` | 进度回调接口 |
| 🆕 | `src/.../api/console/ConsoleConflictResolutionStrategy.kt` | 控制台实现 |
| 🆕 | `src/.../api/console/ConsoleMergingStrategySelector.kt` | 控制台实现 |
| 🆕 | `src/.../api/console/ConsoleAssetConflictResolver.kt` | 控制台实现 |
| ✏️ | `core/ConflictResolver.kt` | 标记实现接口 |
| ✏️ | `core/GlobalMergingStrategy.kt` | 提取到 Selector |
| ✏️ | `core/FileMergerEngine.kt` | 增加策略参数 + 回调 |
| ✏️ | `AppMain.kt` | 传递 `GlobalMergingStrategy` 改为通过 Selector |
| 🆕 | `gui/build.gradle.kts` | GUI 构建 |
| 🆕 | `gui/settings.gradle.kts` | GUI 设置 |
| 🆕 | `gui/src/.../App.kt` | GUI 入口 |
| 🆕 | `gui/src/.../MainWindow.kt` | 主窗口 |
| ✏️ | `.gitignore` | 添加 GUI gradle 缓存 |

---

## 预计工作量

| 步骤 | 预估 |
|------|------|
| 接口定义 + 控制台实现 | ~30 分钟 |
| 改造 ConflictResolver / GlobalMergingStrategy | ~15 分钟 |
| 改造 FileMergerEngine | ~20 分钟 |
| gui/ 模块搭建 + build.gradle.kts | ~20 分钟 |
| App.kt + MainWindow.kt 骨架 | ~10 分钟 |
| 构建验证 | ~15 分钟 |
| **总计** | **~1.5 小时** |
