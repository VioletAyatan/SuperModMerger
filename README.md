# ModMergerTool - Techland 游戏模组合并工具

[English](#english) | [中文](#中文)

## 中文

### 📋 项目简介

**ModMergerTool** 是一个专为 Techland 游戏（如 Dying Light 系列）设计的模组智能合并工具。它能够将多个游戏模组文件（.pak 格式）合并为单一文件，并通过 ANTLR4 语法解析实现脚本文件的智能对比和冲突解决。

### ✨ 主要特性

- 🔀 **智能合并**: 支持无限数量的模组文件合并
- 🔧 **路径修正：**基于原版文件对mod中的错误文件路径进行修正
- 🔍 **冲突检测**: 使用AST语法树进行深度对比
- 👤 **用户交互**: 清晰的命令行界面提示用户选择冲突解决方案
- 📊 **详细统计**: 合并后提供详细的处理统计信息
- 🌍 **国际化**: 同时支持中文和英文

### 项目简介

我做这个工具的初衷一开始是为了解决我自己使用多个MOD之间的各种冲突问题。

最初，我发现了 **[Unleash The Mods](https://www.nexusmods.com/dyinglightthebeast/mods/140)** 这款工具，虽然也很不错。但是有一些小问题和一些不支持的特性，所以我自己制作了这款全新的工具，基于AST语法树进行脚本分析，能够智能识别代码中冲突的地方，智能进行合并。即使是报错的情况下也不会破坏文件结构。同时，也感谢**[Unleash The Mods](https://www.nexusmods.com/dyinglightthebeast/mods/140)** 这款工具作者的辛苦付出，我的一些合并思路也参考了他的工具。

因此本工具的基础使用方法也完全兼容 **[Unleash The Mods](https://www.nexusmods.com/dyinglightthebeast/mods/140)** ，并且不需要安装任何运行库，直接即可使用。

**工具支持智能合并.scr文件 .loot .def 等scr脚本结构的文件和.xml文件，不仅限于常见的player_variables.scr文件的合并，语法解析已经对整个原版data0.pak文件进行过实验，确保没有任何冲突。理论上，只要正确的scr语法的文件，工具都能识别并合并。**理论上也支持消光2的MOD合并，因为消光2的目录结构和困兽完全一样。消光1应该也可以支持，只要它们的scr语法都是相同的，工具都能正确识别，但是消光1需要通过命令行手动指令一下基准mod的位置。

### 问题反馈

工具目前处于第一版，可能会有BUG，如果你发现了任何问题，请及时反馈给我，并提供你合并的mod信息等等。

### 支持的操作系统

- **Windows 10**
- **Windows 11**
- 其他版本的windows系统未经过测试。

### 🚀 快速开始

#### **1、将工具放到困兽根目录/ph_ft目录下，并创建mods目录，将要合并的mod放入其中**

 **准备 mod 文件，mod支持zip、pak、7z等格式**

```bash
# 示例
Dying Light The Beast\ph_ft\mods
├── mod1.pak
├── mod2.pak
└── mod3.pak
```

#### 2. 运行合并程序
```bash
# 双击运行合并工具
```

#### 3. 查看结果
合并后的mod会输出到source目录下的data7.pak文件，如果你有data7.pak。注意，此工具会把旧的覆盖掉。

### 📖 文档

- 🚀 [快速开始指南](QUICK_START.md) - 5 分钟上手
- 📚 [完整实现指南](IMPLEMENTATION_GUIDE.md) - 详细的技术说明
- 📋 [项目完成报告](PROJECT_COMPLETION_REPORT.md) - 项目细节
- ✅ [最终项目总结](FINAL_SUMMARY.md) - 完整的项目成果

### 💻 系统要求

- **Java**: JDK 25 或更高版本
- **操作系统**: Windows / Linux / macOS
- **磁盘空间**: 至少为 mod 文件总大小的 3 倍

### 📦 编译和构建

```bash
# 编译
mvn clean compile

# 打包
mvn package -DskipTests

# 生成的文件
# - target/ModMergerTool-1.0-SNAPSHOT.jar (标准 JAR)
# - target/ModMergerTool-1.0-SNAPSHOT-all.jar (包含所有依赖)
```

### 🎮 使用示例

#### 基本用法
```bash
java -jar target/ModMergerTool-1.0-SNAPSHOT-all.jar
```

#### 指定输出路径
```bash
java -jar target/ModMergerTool-1.0-SNAPSHOT-all.jar -o "D:\my_merged_mod.pak"
```

#### 显示帮助
```bash
java -jar target/ModMergerTool-1.0-SNAPSHOT-all.jar -h
```

### 🔧 命令行选项

```
Usage:
  -o, --output <value>     指定输出 PAK 文件位置 (默认: ./merged_mod.pak)
  -b, --base <value>       基准mod所在的位置 (可选)
  -h, --help               显示帮助信息
```

### 📊 示例输出

```
====== Techland Mod Merger ======

📦 Found 3 mod(s) to merge:
  1. data3.pak
  2. data6.pak
  3. data7.pak

📂 Extracting data3.pak...
   ✓ Extracted 10 files
📂 Extracting data6.pak...
   ✓ Extracted 2 files
📂 Extracting data7.pak...
   ✓ Extracted 2472 files

🔄 Processing files...

📦 Creating merged PAK file...
✅ Merged PAK created: merged_mod.pak

==================================================
📊 Merge Statistics:
  Total files processed: 2484
  ✓ Merged (no conflicts): 0
  ⚠️  Merged (with conflicts): 0
  📄 Copied: 2484
==================================================

✅ Merge completed successfully with no conflicts!
```

### ⚙️ 合并策略

1. **相同文件**: 直接复制（去重）
2. **脚本文件** (.scr): 
   - 使用 ANTLR4 解析为语法树
   - 递归对比每个节点
   - 检测参数差异（冲突）
   - 提示用户选择保留版本
3. **其他文件**: 使用最后一个 mod 的版本

### 🧪 测试

项目包含完整的测试资源：

```
test_mods/
├── mod1/jump_parameters.scr
├── mod2/jump_parameters.scr
├── test_mod1.pak
└── test_mod2.pak
```

### 🎯 项目结构

```
ModMergerTool/
├── src/
│   ├── main/antlr4/           # ANTLR4 语法定义
│   ├── main/java/             # Java 源代码
│   └── main/resources/         # 资源文件（国际化等）
├── mods/                       # 输入模组目录
├── test_mods/                  # 测试模组
├── examples/                   # 示例脚本
├── target/                     # 编译输出
├── pom.xml                     # Maven 配置
└── 文档文件

```

### 🔧 技术栈

- **语言**: Java 25+
- **构建**: Maven 3.9+
- **解析**: ANTLR4 4.13.2
- **工具库**: Lombok, Hutool
- **国际化**: 自定义 i18n 模块

### 📝 主要类

- **AppMain**: 主程序入口
- **ModMergerEngine**: 核心合并引擎
- **PakManager**: PAK 文件管理
- **ScrFileMerger**: 脚本文件合并器
- **ScrTreeComparator**: 语法树对比器
- **ScrConflictResolver**: 冲突解决器

### 🎓 学习价值

本项目展示了：
- ANTLR4 的高级应用
- Java NIO 文件操作
- ZIP 文件处理
- 设计模式应用
- 国际化实现

### 📞 常见问题

**Q: 能合并多少个 mod 文件？**  
A: 理论上无限制，程序会处理 `mods` 目录中所有 .pak 文件

**Q: 合并失败了怎么办？**  
A: 检查 mods 目录是否存在，PAK 文件是否有效，磁盘空间是否充足

**Q: 如何自定义冲突解决方案？**  
A: 目前支持交互式选择，未来版本可能支持配置文件

### 📄 许可证

MIT License

### 👨‍💻 贡献者

- **开发**: Ankol
- **时间**: 2025-12-18

---

## English

### 📋 Project Overview

**ModMergerTool** is a smart mod merger tool designed for Techland games (such as Dying Light series). It can merge multiple game mod files (.pak format) into a single file, and implement intelligent comparison and conflict resolution of script files through ANTLR4 syntax parsing.

### ✨ Features

- 🔀 **Smart Merge**: Support unlimited number of mod file merging
- 🔍 **Conflict Detection**: Deep syntax tree comparison using ANTLR4
- 👤 **User Interaction**: Clear command-line interface for conflict resolution
- 📊 **Detailed Statistics**: Comprehensive processing statistics after merging
- 🌍 **Internationalization**: Support both Chinese and English
- ⚡ **High Performance**: Process 2000+ files in just 10 seconds

### 🚀 Quick Start

#### 1. Prepare mod files
```bash
D:\Projects\ModMergerTool\mods\
├── mod1.pak
├── mod2.pak
└── mod3.pak
```

#### 2. Run the merger
```bash
cd D:\Projects\ModMergerTool
java -jar target/ModMergerTool-1.0-SNAPSHOT-all.jar
```

#### 3. Check results
```bash
# Output file location
merged_mod.pak
```

### 📖 Documentation

- 🚀 [Quick Start Guide](QUICK_START.md)
- 📚 [Implementation Guide](IMPLEMENTATION_GUIDE.md)
- 📋 [Project Report](PROJECT_COMPLETION_REPORT.md)
- ✅ [Final Summary](FINAL_SUMMARY.md)

### 💻 System Requirements

- **Java**: JDK 25 or higher
- **OS**: Windows / Linux / macOS
- **Disk Space**: At least 3x the total mod file size

### 📦 Build

```bash
# Compile
mvn clean compile

# Package
mvn package -DskipTests
```

### 🎮 Usage Examples

```bash
# Basic usage
java -jar target/ModMergerTool-1.0-SNAPSHOT-all.jar

# Specify output path
java -jar target/ModMergerTool-1.0-SNAPSHOT-all.jar -o "D:\output.pak"

# Show help
java -jar target/ModMergerTool-1.0-SNAPSHOT-all.jar -h
```

### 📄 License

MIT License

---

**Last Updated**: 2025-12-23  
**Version**: 1.0-SNAPSHOT  
**Status**: ✅ Production Ready  

**Recent Updates**:
- ✅ Three-way comparison merge logic (based on base MOD)
- ✅ Temporary file caching mechanism, 50-75% performance improvement
- ✅ Complete internationalization support (Chinese & English)
- ✅ Support for .7z format and nested archives
- ✅ Optimized path correction strategy

