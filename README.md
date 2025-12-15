# Techland 模组合并工具 (ModMergerTool)

## 概述

这是一个基于ANTLR4的Techland脚本（Dying Light系列游戏）模组合并工具。它可以将两个模组的脚本文件进行对比和合并，自动检测冲突并提示用户进行交互式或自动解决。

## 功能特性

- ✅ **脚本解析**：使用ANTLR4的TechlandScript语法解析脚本文件为抽象语法树（AST）
- ✅ **智能对比**：对比两个脚本的AST，精确定位差异
- ✅ **冲突检测**：自动识别模组间的冲突定义
- ✅ **交互式解决**：在发现冲突时提示用户选择合并方案
- ✅ **自动模式**：支持无交互的自动合并
- ✅ **灵活合并**：支持多种合并策略
  - 保留模组1的版本
  - 保留模组2的版本
  - 保留两个版本
  - 手动编辑
  - 跳过冲突

## 系统要求

- Java 11+
- Maven 3.6+（仅用于编译）

## 快速开始

### 方式1：使用预编译的JAR（推荐）

1. 获取可执行JAR文件：
   ```bash
   target/ModMergerTool-1.0-SNAPSHOT-all.jar
   ```

2. 运行工具（交互模式）：
   ```bash
   java -jar ModMergerTool-1.0-SNAPSHOT-all.jar ./mod1 ./mod2
   ```

3. 或指定输出目录和自动模式：
   ```bash
   java -jar ModMergerTool-1.0-SNAPSHOT-all.jar ./mod1 ./mod2 -o ./merged_result -a keep-mod1
   ```

### 方式2：从源代码编译

```bash
cd ModMergerTool
mvn clean package
java -jar target/ModMergerTool-1.0-SNAPSHOT-all.jar ./mod1 ./mod2
```

## 使用方法

### 命令行语法

```
java -jar ModMergerTool.jar <mod1_dir> <mod2_dir> [输出目录] [选项]
```

### 必需参数

| 参数 | 说明 |
|------|------|
| `mod1_dir` | 第一个模组目录路径 |
| `mod2_dir` | 第二个模组目录路径 |

### 可选参数

| 选项 | 说明 | 示例 |
|------|------|------|
| `-o, --output <dir>` | 指定输出目录（默认：./merged_mod） | `-o ./result` |
| `-a, --auto <strategy>` | 自动模式（默认为交互模式） | `-a keep-mod1` |
| `-v, --verbose` | 显示详细信息 | `-v` |
| `-h, --help` | 显示帮助信息 | `-h` |

### 自动模式策略

使用 `-a, --auto` 时，可选择以下策略：

| 策略 | 说明 |
|------|------|
| `keep-mod1` | 在冲突时保留模组1的版本 |
| `keep-mod2` | 在冲突时保留模组2的版本 |
| `keep-both` | 在冲突时保留两个版本（添加注释） |

## 使用示例

### 示例1：交互模式合并

```bash
java -jar ModMergerTool-1.0-SNAPSHOT-all.jar ./DyingLight_Mod1 ./DyingLight_Mod2
```

**效果**：会逐个显示冲突项，让用户选择合并方案

### 示例2：自动模式（优先保留Mod1）

```bash
java -jar ModMergerTool-1.0-SNAPSHOT-all.jar ./mod1 ./mod2 -a keep-mod1
```

**效果**：自动合并，所有冲突都选择保留模组1的版本

### 示例3：自动模式（保留两个版本）+ 自定义输出路径

```bash
java -jar ModMergerTool-1.0-SNAPSHOT-all.jar ./mod1 ./mod2 -o ./my_merged_mod -a keep-both
```

**效果**：将合并结果输出到 `./my_merged_mod` 目录

### 示例4：详细信息输出

```bash
java -jar ModMergerTool-1.0-SNAPSHOT-all.jar ./mod1 ./mod2 -v
```

**效果**：显示详细的合并过程信息

## 交互模式说明

当工具以交互模式运行时，每次发现冲突都会显示如下信息：

```
================================================================================
Conflict 1/3: definition
================================================================================

Description: Different: sub:player_init

【Mod1】
sub player_init(health = 100) { ... }

【Mod2】
sub player_init(health = 120) { ... }

Select merge strategy:
  1. Keep Mod1
  2. Keep Mod2
  3. Keep Both
  4. Skip
  5. Manual Edit

Enter choice (1-5): _
```

用户可以选择：
1. **Keep Mod1** - 保留模组1的定义
2. **Keep Mod2** - 使用模组2的定义（覆盖模组1）
3. **Keep Both** - 在输出中同时保留两个版本（添加注释区分）
4. **Skip** - 不处理此冲突，保持原状
5. **Manual Edit** - 手动输入合并后的内容

## 支持的文件类型

工具会自动查找和处理以下文件：
- `.scr` - Techland脚本文件
- `.txt` - 文本脚本文件

其他文件类型会被忽略。

## 输出结果

合并完成后，工具会：

1. 在指定的输出目录中生成合并后的脚本文件
2. 保留模组的文件夹结构
3. 显示统计信息：
   - ✓ 成功合并的文件数
   - ! 包含冲突的文件数
   - + 新增文件数

## 工具架构

### 核心模块

| 类 | 职责 |
|------|------|
| `AppMain` | 应用入口，参数解析和启动 |
| `MergeConfig` | 配置管理和命令行参数处理 |
| `ScriptParser` | ANTLR脚本解析，生成语法树 |
| `TreeComparator` | 语法树对比，识别差异 |
| `ConflictResolver` | 交互式冲突解决 |
| `ModMerger` | 核心合并引擎，协调所有模块 |

### 工作流程

```
命令行输入
    ↓
参数解析 (MergeConfig)
    ↓
目录扫描 (找到所有脚本文件)
    ↓
逐文件处理：
    ├─ 解析 Mod1 脚本 (ScriptParser)
    ├─ 解析 Mod2 脚本 (ScriptParser)
    ├─ 对比 AST (TreeComparator)
    ├─ 检测冲突
    ├─ 解决冲突 (ConflictResolver)
    └─ 合并内容
    ↓
输出结果文件
    ↓
显示统计信息
```

## 语法支持

工具基于 `TechlandScript.g4` ANTLR语法，支持：

- **导入导出**：`import`, `export` 声明
- **函数定义**：`sub` 函数声明
- **变量声明**：带类型和初始化的变量
- **函数调用**：带参数的函数调用
- **宏定义**：`$MACRO()` 形式的宏
- **指令调用**：`!directive()` 形式的指令
- **表达式**：支持算术、逻辑、位运算和三元运算符

## 故障排查

### Q: 工具无法找到脚本文件
**A**: 确保目录结构正确，脚本文件后缀为 `.scr` 或 `.txt`

### Q: 合并后文件内容混乱
**A**: 尝试在交互模式中手动检查冲突，或使用 `-v` 选项查看详细信息

### Q: 内存不足
**A**: 增加JVM堆内存：
```bash
java -Xmx2G -jar ModMergerTool-1.0-SNAPSHOT-all.jar ./mod1 ./mod2
```

### Q: 找不到符号或解析错误
**A**: 检查脚本文件是否符合TechlandScript语法规范

## 版本信息

- **版本**: 1.0-SNAPSHOT
- **Java**: 25+
- **ANTLR**: 4.11.1
- **Maven**: 3.9+

## 依赖库

- `hutool-core` - 工具库
- `antlr4-runtime` - ANTLR4运行时

## 开源许可

遵循项目的许可协议。

## 贡献和反馈

欢迎提交Bug报告和功能请求。

---

**最后修改**: 2025年12月15日


