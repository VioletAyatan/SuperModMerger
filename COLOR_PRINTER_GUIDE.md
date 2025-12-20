# 彩色打印功能使用指南

## 📝 概述

已为项目添加了彩色打印功能 (`ColorPrinter` 工具类)，支持 ANSI 彩色输出，可在 Windows 10+、Linux 和 macOS 等系统中显示彩色文本。所有 System.out 和 System.err 已替换为彩色打印调用。

## ✅ 完成清单

| 项目 | 数量 | 状态 |
|------|------|------|
| ColorPrinter 工具类 | 1 个 | ✅ 已创建 |
| 彩色打印替换 | 30+ 处 | ✅ 已完成 |
| 编译验证 | BUILD SUCCESS | ✅ 通过 |

## 🎨 支持的颜色

### 基础颜色
- **RED** (红色) - 错误消息
- **GREEN** (绿色) - 成功消息  
- **YELLOW** (黄色) - 警告消息
- **BLUE** (蓝色) - 信息消息
- **CYAN** (青色) - 调试消息
- **MAGENTA** (洋红) - 强调消息

### 高亮颜色 (更亮的版本)
- **BRIGHT_RED** - 亮红色
- **BRIGHT_GREEN** - 亮绿色
- **BRIGHT_YELLOW** - 亮黄色
- **BRIGHT_BLUE** - 亮蓝色
- **BRIGHT_CYAN** - 亮青色
- **BRIGHT_MAGENTA** - 亮洋红

## 📚 API 使用

### 1. 基础使用

```java
import ankol.mod.merger.tools.ColorPrinter;

// 信息消息（蓝色）
ColorPrinter.info("This is an info message");
ColorPrinter.info("Found {} mods", count);  // 支持格式化

// 成功消息（绿色）
ColorPrinter.success("Operation completed");

// 警告消息（黄色）
ColorPrinter.warning("Warning: {} conflicts detected", conflictCount);

// 错误消息（红色）
ColorPrinter.error("Error occurred: {}", errorMsg);

// 调试消息（青色）
ColorPrinter.debug("Debug info: {}", debugValue);

// 自定义颜色
ColorPrinter.printWithColor("Custom message", ColorPrinter.BRIGHT_MAGENTA_CODE);
```

### 2. 参数化格式化

```java
// 类似 String.format() 的用法
ColorPrinter.info("Processing file: {} from {}", fileName, sourceModName);
ColorPrinter.success("✓ Merged successfully");
ColorPrinter.warning("⚠️  {} file(s) have conflicts", conflictCount);
```

### 3. 获取彩色文本（不直接打印）

```java
// 获取带颜色的文本用于其他用途
String coloredText = ColorPrinter.getColoredText("Important!", ColorPrinter.BRIGHT_RED_CODE);
System.out.println(coloredText);
```

### 4. 检查颜色支持

```java
// 检查系统是否支持彩色输出
if (ColorPrinter.isColorSupported()) {
    ColorPrinter.success("Colored output is supported!");
} else {
    System.out.println("Colored output is not supported");
}
```

## 🌈 颜色输出示例

运行程序时会看到以下彩色输出：

```
====== Techland Mod Merger ====== (蓝色)
📦 Found 3 mod(s) to merge: (蓝色)
  1. data2.pak (蓝色)
  2. data3.pak (蓝色)
  3. data7.pak (蓝色)

📂 Extracting data2.pak... (蓝色)
✓ Extracted 150 files (绿色)
📂 Extracting data3.pak... (蓝色)
✓ Extracted 148 files (绿色)
🔄 Processing files... (蓝色)
🔀Merging: game_tags.scr (3 mods) (蓝色)
⚠️  2 conflict(s) resolved (黄色)
✓ Merged successfully (绿色)

📦 Creating merged PAK file... (蓝色)
✅ Merged PAK created: merged_mod.pak (绿色)

================================================== (蓝色)
📊 Merge Statistics: (蓝色)
   Total files processed: 443 (蓝色)
✓  Merged (no conflicts): 10 (绿色)
⚠️  Merged (with conflicts): 2 (黄色)
📄 Copied: 431 (蓝色)
================================================== (蓝色)

✅ Merge completed successfully with no conflicts! (绿色)
✅ Done! (绿色)
```

## 🔧 ColorPrinter 类详解

### 核心方法

| 方法 | 用途 | 颜色 | 输出流 |
|------|------|------|--------|
| `info()` | 信息消息 | BRIGHT_BLUE | stdout |
| `success()` | 成功消息 | BRIGHT_GREEN | stdout |
| `warning()` | 警告消息 | BRIGHT_YELLOW | stdout |
| `error()` | 错误消息 | BRIGHT_RED | stderr |
| `debug()` | 调试消息 | BRIGHT_CYAN | stdout |
| `print()` | 普通消息 | WHITE | stdout |
| `bold()` | 加粗消息 | WHITE (加粗) | stdout |
| `highlight()` | 强调消息 | BRIGHT_MAGENTA | stdout |

### 颜色常量

```java
public static final String RED_CODE;           // 红色代码
public static final String GREEN_CODE;         // 绿色代码
public static final String YELLOW_CODE;        // 黄色代码
public static final String BRIGHT_RED_CODE;    // 亮红色代码
public static final String BRIGHT_GREEN_CODE;  // 亮绿色代码
// ...更多常量
```

## 💻 实现细节

### ANSI 转义序列

项目使用标准的 ANSI 转义序列实现彩色输出：

```
\033[31m - 红色文本
\033[32m - 绿色文本
\033[33m - 黄色文本
\033[91m - 亮红色文本
\033[92m - 亮绿色文本
...
\033[0m - 重置所有属性
```

### 系统兼容性检查

ColorPrinter 会自动检测系统是否支持 ANSI 颜色：

```java
// 支持的系统
✅ Windows 10+
✅ Linux (所有发行版)
✅ macOS (所有版本)

// 不支持的系统
❌ Windows 7 及更早版本（除非启用 VT100 模式）
```

在不支持的系统上，彩色代码会被自动剥离，文本仍可正常显示。

## 🔍 应用位置

ColorPrinter 已应用于以下核心类：

| 类 | 方法 | 替换数 |
|---|------|---------|
| ModMergerEngine | merge() | 4 |
| | extractAllMods() | 2 |
| | processFiles() | 1 |
| | mergeFiles() | 2 |
| | printStatistics() | 10 |
| | cleanupTempDir() | 1 |
| ScrFileMerger | resolveConflictsInteractively() | 2 |
| AppMain | main() | 4 |
| Localizations | init() | 1 |
| Tools | buildFileTreeMap() | 1 |
| SimpleArgParser | printHelp() | 2 |
| SimpleArgumentsParser | printHelp() | 1 |

**总计：31 处替换**

## 🎯 使用示例

### 示例 1：简单的信息输出

```java
ColorPrinter.info("Starting merge process");
ColorPrinter.success("✓ Process completed");
```

### 示例 2：格式化输出

```java
int fileCount = 150;
String modName = "data2.pak";
ColorPrinter.info("📂 Extracting {}...", modName);
ColorPrinter.success("✓ Extracted {} files", fileCount);
```

### 示例 3：错误处理

```java
try {
    // 业务逻辑
} catch (IOException e) {
    ColorPrinter.error("❌ Failed to process file: {}", fileName);
    e.printStackTrace();
}
```

### 示例 4：条件输出

```java
if (conflictDetected) {
    ColorPrinter.warning("⚠️  {} conflict(s) detected", conflictCount);
} else {
    ColorPrinter.success("✅ No conflicts found!");
}
```

## 🧪 测试彩色输出

编译后直接运行程序即可看到彩色输出：

```bash
cd D:\Projects\ModMergerTool
mvn clean package
java -jar target/ModMergerTool-1.0-SNAPSHOT-all.jar
```

在控制台中应该能看到彩色的日志输出。

## ⚙️ 环境变量配置

如果在某些环境中彩色输出不工作，可以尝试以下方法：

### Windows 10+

确保启用了 VT100 转义序列支持：
```cmd
reg add HKCU\Console /v VirtualTerminalLevel /t REG_DWORD /d 1
```

### Linux/macOS

一般默认支持，如遇问题可检查 `TERM` 变量：
```bash
echo $TERM  # 应该显示 xterm, xterm-256color 等
```

## 📖 完整 API 参考

### info(String message)
打印蓝色的信息消息

### info(String format, Object... args)
打印格式化的蓝色信息消息

### success(String message)
打印绿色的成功消息

### success(String format, Object... args)
打印格式化的绿色成功消息

### warning(String message)
打印黄色的警告消息

### warning(String format, Object... args)
打印格式化的黄色警告消息

### error(String message)
打印红色的错误消息到 stderr

### error(String format, Object... args)
打印格式化的红色错误消息到 stderr

### error(String message, Throwable e)
打印红色错误消息和异常堆栈跟踪

### debug(String message)
打印青色的调试消息

### debug(String format, Object... args)
打印格式化的青色调试消息

### print(String message)
打印白色的普通消息

### print(String format, Object... args)
打印格式化的白色普通消息

### bold(String message)
打印加粗的消息

### bold(String format, Object... args)
打印格式化的加粗消息

### highlight(String message)
打印洋红色的强调消息

### highlight(String format, Object... args)
打印格式化的洋红色强调消息

### printWithColor(String message, String colorCode)
使用自定义颜色代码打印消息

### getColoredText(String text, String colorCode)
获取带颜色的文本（不直接打印）

### isColorSupported()
检查系统是否支持彩色输出

---

**创建时间**：2025-12-20  
**编译状态**：✅ BUILD SUCCESS  
**生产就绪**：✅ YES

