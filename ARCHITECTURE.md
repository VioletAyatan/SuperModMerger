# SuperModMerger 代码架构与原理详解

**[中文] | [English README](README.md)**

本文档从技术角度深入解释 SuperModMerger 的代码作用与核心原理，面向希望了解或二次开发本工具的开发者。

---

## 目录

1. [项目总体作用](#1-项目总体作用)
2. [技术栈](#2-技术栈)
3. [代码结构总览](#3-代码结构总览)
4. [核心模块详解](#4-核心模块详解)
   - [4.1 程序入口 (AppMain)](#41-程序入口-appmain)
   - [4.2 合并引擎 (FileMergerEngine)](#42-合并引擎-filemergerengine)
   - [4.3 基准 MOD 管理器 (BaseModManager)](#43-基准-mod-管理器-basemodmanager)
   - [4.4 合并器工厂 (MergerFactory)](#44-合并器工厂-mergerfactory)
   - [4.5 文件合并器实现](#45-文件合并器实现)
   - [4.6 冲突解决器 (ConflictResolver)](#46-冲突解决器-conflictresolver)
   - [4.7 全局合并策略 (GlobalMergingStrategy)](#47-全局合并策略-globalmergingstrategy)
   - [4.8 PAK 归档管理 (PakManager)](#48-pak-归档管理-pakmanager)
5. [AST 解析原理](#5-ast-解析原理)
   - [5.1 ANTLR 语法文件](#51-antlr-语法文件)
   - [5.2 Visitor 模式](#52-visitor-模式)
   - [5.3 节点签名机制](#53-节点签名机制)
6. [三方合并算法详解](#6-三方合并算法详解)
7. [TokenStreamRewriter 工作原理](#7-tokenstreamrewriter-工作原理)
8. [完整合并流程图](#8-完整合并流程图)
9. [国际化机制](#9-国际化机制)
10. [构建与编译](#10-构建与编译)

---

## 1. 项目总体作用

SuperModMerger 是专为 **消光系列（Dying Light）** 游戏设计的多 MOD 智能合并工具。

游戏 MOD 通常以 `.pak`（本质是 ZIP 归档）格式分发，内部包含脚本（`.scr`/`.def`/`.loot` 等）、数据（`.xml`）和界面（`.gui`）文件。当玩家同时安装多个 MOD 时，如果两个 MOD 修改了同一个文件，后安装的 MOD 会完全覆盖先安装的 MOD，导致部分 MOD 功能失效。

本工具的作用：
- 解析多个 MOD 的 `.pak` 归档，提取所有文件。
- 对同名文件执行 **基于 AST（抽象语法树）的智能合并**，而非简单的文本覆盖。
- 检测真正的内容冲突，通过交互式命令行让用户选择保留哪个 MOD 的版本。
- 将合并结果重新打包为单一 `data7.pak` 文件，供游戏加载。

---

## 2. 技术栈

| 技术 | 版本 | 用途 |
|------|------|------|
| Kotlin | 2.3.0 | 主要实现语言（约 80% 代码） |
| Java | 25 | 编译目标平台 |
| ANTLR 4 | 4.13.2 | 语法驱动的脚本/XML/JSON 解析器生成 |
| Maven | 3.x | 构建与依赖管理 |
| GraalVM | — | 编译为 Windows 原生可执行文件（.exe） |
| Apache Commons Compress | — | ZIP/7z PAK 归档的读写 |
| SLF4J + Logback | — | 日志 |
| Lombok | — | 减少样板代码 |

---

## 3. 代码结构总览

```
src/main/
├── antlr4/                          # ANTLR 语法定义文件
│   ├── TechlandScript.g4            # SCR/DEF/LOOT 等脚本的语法规则
│   ├── xml/
│   │   ├── TechlandXMLLexer.g4      # XML 词法规则
│   │   └── TechlandXMLParser.g4     # XML 语法规则
│   └── json/
│       └── JSON.g4                  # JSON/.gui 语法规则
└── java/ankol/mod/merger/
    ├── AppMain.kt                   # 程序入口
    ├── antlr/                       # ANTLR 生成的词法/语法分析器
    │   ├── scr/                     # TechlandScript 解析器
    │   ├── xml/                     # XML 解析器
    │   └── json/                    # JSON 解析器
    ├── constants/
    │   └── UserChoice.kt            # 用户冲突解决选项枚举
    ├── core/                        # 核心合并逻辑
    │   ├── AppMain.kt               # 程序主入口
    │   ├── FileMergerEngine.kt      # 合并引擎（主流程编排）
    │   ├── AbstractFileMerger.kt    # 所有合并器的抽象基类
    │   ├── BaseModManager.kt        # 基准 MOD（data0.pak）管理
    │   ├── BaseTreeNode.kt          # AST 节点基类
    │   ├── ConflictResolver.kt      # 交互式冲突解决
    │   ├── GlobalMergingStrategy.kt # 全局合并模式选择
    │   ├── MergerContext.kt         # 合并上下文（在合并过程中传递状态）
    │   ├── ParsedResult.kt          # 文件解析结果（AST + TokenStream）
    │   └── filetrees/               # 文件内容抽象（路径型/内存型）
    ├── domain/
    │   └── MergingModInfo.kt        # 待合并 MOD 的数据类
    ├── exception/
    │   └── BusinessException.kt     # 业务异常
    ├── merger/                      # 各文件类型的合并器实现
    │   ├── ConflictRecord.kt        # 冲突记录（保存冲突的两个节点及用户选择）
    │   ├── MergeResult.kt           # 合并结果（合并后内容）
    │   ├── MergerFactory.kt         # 工厂：按扩展名分发合并器
    │   ├── scr/                     # SCR 脚本合并器
    │   │   ├── TechlandScrFileMerger.kt
    │   │   ├── TechlandScrFileVisitor.kt
    │   │   └── node/                # SCR AST 节点类型
    │   ├── xml/                     # XML 合并器
    │   │   ├── TechlandXmlFileMerger.kt
    │   │   ├── TechlandXmlFileVisitor.kt
    │   │   └── node/
    │   └── json/                    # JSON/.gui 合并器
    │       ├── TechlandJsonFileMerger.kt
    │       ├── TechlandJsonFileVisitor.kt
    │       └── node/
    └── tools/                       # 工具类
        ├── ColorPrinter.java        # 彩色控制台输出
        ├── ErrorReporter.kt         # 错误汇报
        ├── KtExtensions.kt          # Kotlin 扩展函数
        ├── Localizations.kt         # 国际化（中/英）
        ├── PakManager.kt            # PAK 归档读写
        ├── SimpleArgParser.java     # 命令行参数解析
        └── Tools.kt                 # 通用工具函数
```

---

## 4. 核心模块详解

### 4.1 程序入口 (AppMain)

`AppMain.kt` 是整个程序的入口，负责：

1. **初始化控制台字符集为 UTF-8**（调用 `chcp 65001`，确保中文正常显示）。
2. **加载国际化资源**（`Localizations.init()`）。
3. **解析命令行参数**，支持：
   - `-o <路径>`：指定输出文件路径（默认 `source/data7.pak`）。
   - `-b <路径>`：指定基准 MOD 路径（默认 `source/data0.pak`）。
   - `-h`：打印帮助信息。
4. **扫描 MOD 目录**（`mods/` 子目录），自动识别 Vortex 部署结构。
5. **询问合并策略**（普通模式 / 全局修复模式）。
6. **调用 `FileMergerEngine.merge()`** 执行合并，并打印耗时。

---

### 4.2 合并引擎 (FileMergerEngine)

`FileMergerEngine` 是整个合并流程的**主编排者**，`merge()` 方法的执行流程如下：

```
merge()
  │
  ├─ 清理上次遗留的临时目录
  │
  ├─ extractAllMods()         并行提取所有 MOD 的 PAK 文件
  │    ├─ PakManager.extractPak()   解压到临时目录
  │    ├─ filterFiles()             过滤不支持合并的文件（.txt/.dll/.rpack）
  │    └─ correctPathsForMod()      对照 data0.pak 修正错误路径
  │
  ├─ processFiles()           按相对路径处理所有文件
  │    ├─ 文件只来自一个 MOD   → 直接复制（或在全局修复模式下解析后重新序列化）
  │    └─ 文件来自多个 MOD    → mergeFiles() 合并
  │         ├─ 所有文件内容相同 → 直接复制第一个
  │         ├─ 无对应合并器    → 让用户手动选择用哪个版本
  │         └─ 有对应合并器    → 顺序三方合并
  │
  ├─ PakManager.createPak()   将合并结果打包为 data7.pak
  │
  └─ printStatistics()        打印统计数据，清理临时文件
```

**路径修正逻辑**：MOD 作者有时将文件放在错误的目录下（如 `data/scripts/...` 而非 `data0/scripts/...`）。`correctPathsForMod()` 将 MOD 内的文件路径与 `data0.pak` 中的路径进行模糊匹配，找到最接近的正确路径并自动修正。

**顺序合并**：多个 MOD 的合并顺序为：
```
data0.pak（基准）← MOD1 → 中间结果 ← MOD2 → 中间结果 ← MOD3 → 最终结果
```
每次合并都以上一次的合并结果作为新的"基准"。

---

### 4.3 基准 MOD 管理器 (BaseModManager)

`BaseModManager` 封装了对 `data0.pak`（游戏原版文件）的访问：

- **路径冲突检测**：检查 MOD 内的路径是否与 `data0.pak` 中的路径（忽略大小写）匹配，若路径不一致则标记为需要修正。
- **文件内容提取**：按需从 `data0.pak` 中提取单个文件内容，供三方合并使用。
- **AST 缓存**：将解析过的 `data0.pak` 文件的 AST 结果缓存，避免重复解析（因为多个 MOD 可能都修改同一个文件）。

---

### 4.4 合并器工厂 (MergerFactory)

`MergerFactory` 实现了简单的**工厂模式**，根据文件扩展名返回对应的合并器实例：

```
.scr / .def / .loot / .phx / .ppfx / .ares / .mpcloth  →  TechlandScrFileMerger
.xml                                                     →  TechlandXmlFileMerger
.gui                                                     →  TechlandJsonFileMerger
其他扩展名                                                →  null（不支持，由用户手动选择）
```

合并器实例被缓存（`mergerCache`），同一类型的文件不会重复实例化合并器。每次调用时会更新 `context` 引用，将当前合并上下文注入到复用的合并器中。

---

### 4.5 文件合并器实现

三种合并器均继承自 `AbstractFileMerger`，实现同一个接口：

```kotlin
abstract fun merge(file1: AbstractFileTree, file2: AbstractFileTree): MergeResult
```

**通用合并步骤**：
1. 将 `file1`（基准）和 `file2`（当前 MOD）的文本内容交给 ANTLR 解析，生成 AST。
2. 从 `BaseModManager` 获取 `data0.pak` 中对应文件的 AST（若存在）。
3. 调用 `deepCompare()` 进行三方深度对比，收集冲突记录（`ConflictRecord`）。
4. 将冲突交给 `ConflictResolver` 处理（自动合并或提示用户选择）。
5. 使用 ANTLR 的 `TokenStreamRewriter` 在 `file1` 的 Token 流上执行替换/插入操作，生成最终合并文本。

---

### 4.6 冲突解决器 (ConflictResolver)

`ConflictResolver` 负责处理合并器收集到的冲突列表：

**自动合并**（无需用户干预）：
- 如果 `file1` 中的节点内容与 `data0.pak` 原版相同，而 `file2`（当前 MOD）对该节点做了修改，则认为这是 MOD 的有效修改，自动选择使用 `file2` 的版本。这对应"MOD 新增/修改了原版内容"这一常见场景。

**交互式解决**（真正冲突）：
- 当两个 MOD 都对同一个节点做了不同修改时（相对于原版），工具会在控制台展示两个版本的代码内容，提示用户选择：
  1. 使用版本 1（来自 `file1`/上一个 MOD）
  2. 使用版本 2（来自 `file2`/当前 MOD）
  3. 后续所有冲突全部使用版本 1
  4. 后续所有冲突全部使用版本 2

---

### 4.7 全局合并策略 (GlobalMergingStrategy)

程序启动时询问用户选择合并模式：

| 模式 | 说明 |
|------|------|
| **普通模式（NORMAL_MODE）** | 只对多个 MOD 都包含的重复文件执行解析和合并；单个 MOD 独有的文件直接复制。 |
| **全局修复模式（GLOBAL_FIX_MODE）** | 对所有文件（包括只有一个 MOD 的文件）都执行解析和重新序列化，可修复使用旧版语法的 MOD 兼容性问题，但速度较慢。 |

也可通过命令行参数 `-f` 直接启用全局修复模式。

---

### 4.8 PAK 归档管理 (PakManager)

`PakManager` 封装了对 PAK（ZIP 格式）和 7z 归档文件的操作：

- **`extractPak()`**：将 MOD 归档解压到临时目录，返回 `文件相对路径 → PathFileTree` 的映射。
- **`createPak()`**：将合并后的临时目录打包为 ZIP 格式的 `.pak` 文件。
- **`areFilesIdentical()`**：通过比较文件大小和 CRC/哈希值快速判断两个文件是否完全相同，避免不必要的合并。

---

## 5. AST 解析原理

### 5.1 ANTLR 语法文件

项目使用 [ANTLR 4](https://www.antlr.org/) 通过语法规则文件（`.g4`）自动生成词法分析器（Lexer）和语法分析器（Parser）。

三套语法文件对应三种文件类型：

| 语法文件 | 对应文件类型 | 说明 |
|----------|-------------|------|
| `TechlandScript.g4` | `.scr`/`.def`/`.loot`/`.phx`/`.ppfx`/`.ares`/`.mpcloth` | Techland 自定义脚本语言，类似 C 风格的函数调用和结构体声明 |
| `TechlandXMLLexer.g4` + `TechlandXMLParser.g4` | `.xml` | 标准 XML，但针对 Techland 数据文件做了专项优化 |
| `JSON.g4` | `.gui` | 标准 JSON，用于 UI 布局文件 |

ANTLR 根据这些语法文件在编译期生成 `src/main/java/ankol/mod/merger/antlr/` 下的 Java 类。

### 5.2 Visitor 模式

ANTLR 生成的解析树通过 **Visitor 模式**转换为本工具定义的 AST 节点：

```
原始文本
   ↓ ANTLR Lexer（词法分析）
Token 流（带索引）
   ↓ ANTLR Parser（语法分析）
ANTLR ParseTree（规则树）
   ↓ TechlandScrFileVisitor / TechlandXmlFileVisitor / TechlandJsonFileVisitor
自定义 AST（ScrContainerScriptNode / XmlContainerNode 等）
```

每个 Visitor 类（如 `TechlandScrFileVisitor`）遍历 ANTLR 生成的语法树，将每个有意义的语法结构转换为对应的自定义 AST 节点（`BaseTreeNode` 的子类），并记录节点在 Token 流中的**起止索引**（`startTokenIndex` / `stopTokenIndex`）。

### 5.3 节点签名机制

每个 AST 节点都有一个**签名（signature）**，用于唯一标识该节点的"语义身份"。签名的设计使得两个不同文件中修改了同一个逻辑实体（如同名函数、同名 XML 元素）的节点能够被正确配对以进行对比。

签名示例：

| 文件类型 | 代码 | 签名 |
|----------|------|------|
| SCR 脚本 | `sub OnInit() { ... }` | `sub:OnInit` |
| SCR 脚本 | `import "scripts/base.scr"` | `import:scripts/base.scr` |
| XML | `<skill id="ledge_master">` | `element:skill:id=ledge_master` |
| JSON | `"health": 100` | `key:health` |

合并器使用签名作为 Map 的键，将两个文件中拥有相同签名的节点配对，然后比较它们的内容。

---

## 6. 三方合并算法详解

SuperModMerger 使用经典的**三方合并（Three-Way Merge）**算法，与 `git merge` 的原理类似：

```
         data0.pak（原版 O）
               │
        ┌──────┴──────┐
        │             │
     MOD A            MOD B
（对 O 进行了修改 A）  （对 O 进行了修改 B）
```

合并逻辑的判断矩阵：

| O（原版）节点内容 | A（基准）节点内容 | B（当前 MOD）节点内容 | 结论 |
|-----------------|-----------------|----------------------|------|
| 相同 | 相同 | 不同 | B 做了修改 → **自动采用 B** |
| 相同 | 不同 | 相同 | A 做了修改 → **保留 A（不变）** |
| 相同 | 不同 | 不同（且 A≠B） | 两者都做了不同修改 → **真正冲突，提示用户** |
| A 中不存在此节点 | — | B 中新增 | B 新增了节点 → **自动插入** |
| B 中不存在此节点 | A 中存在 | — | A 独有的节点 → **保留 A（不变）** |

**无原版文件时的退化**：若 `data0.pak` 中不存在对应文件（如 MOD 新增的文件），则退化为简单的二方合并：A 中存在而 B 中不存在的节点保留，B 中存在而 A 中不存在的节点插入，两者都存在但内容不同时视为冲突。

**合并历史追踪**（`MergerContext.mergedHistory`）：在顺序合并多个 MOD 时，工具会记录每个节点最后是由哪个 MOD 修改的，以便在后续合并中正确展示冲突来源 MOD 的名称。

---

## 7. TokenStreamRewriter 工作原理

合并器不直接操作字符串，而是使用 ANTLR 提供的 `TokenStreamRewriter` 在 Token 流上记录操作，最后一次性生成修改后的文本。

**工作原理**：
1. 解析文件时，ANTLR 将原始文本分解为一个带索引的 **Token 序列**（每个 token 对应一个词法单元，如关键字、标识符、括号、数字等）。
2. 每个 AST 节点记录它在 Token 序列中的起始索引（`startTokenIndex`）和终止索引（`stopTokenIndex`）。
3. 当需要"替换"一个节点时，调用 `rewriter.replace(startIndex, stopIndex, newText)`，标记这段 token 区间用新文本替换。
4. 当需要"插入"一个新节点时，调用 `rewriter.insertBefore(position, newText)`，在指定 token 位置前插入文本。
5. 最后调用 `rewriter.text` 一次性将所有操作应用到 Token 流，生成最终文本。

这种方式的优势：
- **保留原始格式**：只修改冲突的节点，其他内容（包括注释、缩进、换行）完全保留原样。
- **操作可组合**：多个 replace/insert 操作可以叠加，不会互相干扰。

详细的 Token 索引工作原理请参阅 [NOTE.md](NOTE.md)。

---

## 8. 完整合并流程图

```
启动
 │
 ├─ 初始化（字符集、国际化、命令行参数解析）
 │
 ├─ 扫描 mods/ 目录，收集 .pak/.zip/.7z 文件
 │
 ├─ 加载 data0.pak（基准 MOD）
 │
 ├─ 询问合并策略（普通 / 全局修复）
 │
 ├─ 并行提取所有 MOD 到临时目录
 │    ├─ 过滤不支持的文件类型（.txt/.dll/.rpack）
 │    └─ 对照 data0.pak 修正文件路径
 │
 ├─ 按文件相对路径分组
 │
 ├─ 处理每个文件路径：
 │    ├─ [仅1个 MOD 包含]
 │    │    ├─ 普通模式：直接复制
 │    │    └─ 全局修复模式：解析后重新序列化
 │    │
 │    └─ [多个 MOD 包含]
 │         ├─ 文件内容完全相同？→ 直接复制
 │         ├─ 无对应合并器？    → 提示用户手动选择
 │         └─ 有合并器（SCR/XML/JSON）：
 │              ├─ 用 data0.pak 作基准与 MOD1 合并 → 中间结果
 │              ├─ 用中间结果与 MOD2 合并 → 中间结果
 │              └─ ...依次合并所有 MOD
 │
 │              每次合并内部流程：
 │              ├─ ANTLR 解析两个文件 → 两棵 AST
 │              ├─ deepCompare() 三方对比 → 冲突列表
 │              ├─ ConflictResolver：
 │              │    ├─ 自动合并（只有一方相对原版做了修改）
 │              │    └─ 提示用户选择（两方都做了不同修改）
 │              └─ TokenStreamRewriter 生成最终文本
 │
 ├─ 将合并结果打包为 data7.pak
 │
 └─ 打印统计信息（处理文件数、合并数、路径修正数、错误报告）
```

---

## 9. 国际化机制

工具通过 `Localizations.kt` 实现中英文双语支持：

- 语言资源以 `Map<String, String>` 形式内嵌在代码中（中文和英文各一个 Map）。
- 启动时根据系统语言（`Locale`）自动选择语言，中文系统使用中文，其他系统使用英文。
- 所有用户可见的字符串通过 `Localizations.t("KEY")` 获取，方便扩展新语言。

---

## 10. 构建与编译

**标准构建（生成 Fat JAR）**：
```bash
mvn clean package
```
输出：`target/SuperModMerger-<version>-all.jar`

**GraalVM 原生镜像构建（生成 Windows .exe）**：
```bash
mvn clean package -Pnative
```
需要安装 GraalVM JDK 并配置 `JAVA_HOME`。
输出：`target/SuperModMerger-<version>.exe`

**运行测试**：
```bash
mvn test
```

**`build-native.bat`**：Windows 下的原生构建脚本，会额外调用 `rcedit-x64.exe` 为生成的 `.exe` 嵌入版本信息和程序图标。

**GraalVM 运行时元数据（`reachability-metadata.json`）**：用于告知 GraalVM 原生镜像编译器哪些类/方法/字段需要通过反射访问，确保运行时不会因为反射调用被裁剪而出现错误。
