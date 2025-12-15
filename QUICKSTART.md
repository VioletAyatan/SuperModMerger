# 快速开始指南

## 工具简介

Techland模组合并工具用于合并两个Dying Light游戏模组的脚本文件。工具会自动对比两个模组的差异，并在发现冲突时提示用户选择合并方案。

## 5分钟快速上手

### 第1步：准备模组目录

假设你有两个模组目录：
```
MyMod/
├── scripts/
│   ├── player.scr
│   ├── npc.scr
│   └── ...

AnotherMod/
├── scripts/
│   ├── player.scr
│   ├── npc.scr
│   └── ...
```

### 第2步：运行工具

**最简单的方式（交互模式）**：
```bash
java -jar ModMergerTool-1.0-SNAPSHOT-all.jar ./MyMod ./AnotherMod
```

这会将合并结果保存到 `./merged_mod` 目录。

### 第3步：选择合并方案

当遇到冲突时，工具会显示两个版本的代码，并让你选择：

```
Conflict 1/3: definition
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

Enter choice (1-5): 1
```

输入数字选择方案，然后按Enter继续。

### 第4步：查看结果

合并完成后，所有合并的脚本文件会保存在 `./merged_mod/` 目录中。

## 常见使用场景

### 场景1：快速合并，优先保留第一个模组

```bash
java -jar ModMergerTool-1.0-SNAPSHOT-all.jar ./mod1 ./mod2 -a keep-mod1
```

所有冲突都会自动选择保留mod1的版本，无需交互。

### 场景2：合并后保存到自定义位置

```bash
java -jar ModMergerTool-1.0-SNAPSHOT-all.jar ./mod1 ./mod2 -o ./custom_output
```

### 场景3：保留两个版本（用注释区分）

```bash
java -jar ModMergerTool-1.0-SNAPSHOT-all.jar ./mod1 ./mod2 -a keep-both
```

有冲突的定义会同时保留，方便后续手动审查和编辑。

### 场景4：显示详细信息

```bash
java -jar ModMergerTool-1.0-SNAPSHOT-all.jar ./mod1 ./mod2 -v
```

会显示配置信息和每个文件的处理状态。

## 选择合并策略说明

### 1. Keep Mod1（保留模组1）
- 当两个模组有冲突时，**选择保留模组1的版本**
- 适用场景：你信任模组1的定义，想用它覆盖模组2

### 2. Keep Mod2（保留模组2）
- 当两个模组有冲突时，**选择保留模组2的版本**
- 适用场景：你信任模组2的定义，想用它覆盖模组1

### 3. Keep Both（保留两个版本）
- 当两个模组有冲突时，**同时保留两个版本**
- 会在输出中添加注释来区分来源
- 适用场景：你希望保留两个版本供后续手动选择

### 4. Skip（跳过）
- 遇到冲突时，**不做任何修改**
- 仅在交互模式中可用
- 适用场景：你想稍后手动处理这个冲突

### 5. Manual（手动输入）
- 仅在交互模式中可用
- 输入自己定义的内容来解决冲突
- 按EOF结束输入

## 交互模式 vs 自动模式

### 交互模式（默认）
```bash
java -jar ModMergerTool.jar ./mod1 ./mod2
```

**优点**：
- 对每个冲突精确控制
- 可以逐一检查和手动编辑

**缺点**：
- 如果冲突很多，需要大量手动操作
- 耗时较长

### 自动模式
```bash
java -jar ModMergerTool.jar ./mod1 ./mod2 -a keep-mod1
```

**优点**：
- 快速处理
- 适合大规模合并

**缺点**：
- 冲突处理方式固定
- 无法对特定冲突进行定制

## 输出文件结构

合并完成后，输出目录会保持原有的文件夹结构：

```
merged_mod/
├── scripts/
│   ├── player.scr
│   ├── npc.scr
│   ├── weapon.scr
│   └── ...
├── config/
│   └── ...
└── ...
```

## 常见问题

**Q: 合并会修改原始文件吗？**
A: 不会。原始模组目录不会被修改，所有输出都保存到新目录。

**Q: 能处理多个文件夹吗？**
A: 当前版本支持两个模组的合并。如需合并多个模组，可以分步进行：先合并mod1和mod2，再用结果与mod3合并。

**Q: 合并速度如何？**
A: 取决于文件数量和文件大小。通常几十个脚本文件的合并在几秒内完成。

**Q: 支持哪些文件格式？**
A: 支持 `.scr` 和 `.txt` 扩展名的脚本文件。

**Q: 如果没有冲突会怎样？**
A: 工具会直接合并，生成的文件完全相同，无需用户交互。

## 需要帮助？

查看完整文档：`README.md`

查看所有命令行选项：
```bash
java -jar ModMergerTool-1.0-SNAPSHOT-all.jar -h
```

---

**提示**：在命令行中使用相对路径或绝对路径都可以。建议在有权限的目录中运行工具。


