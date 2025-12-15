# ModMergerTool - Techland 脚本合并工具

> 一个基于ANTLR4的Dying Light游戏模组脚本合并工具，支持智能冲突检测和交互式解决方案

## 🎮 项目背景

Techland的Dying Light系列游戏支持模组，每个模组可能包含多个脚本文件。当需要合并两个模组时，手动对比和合并脚本文件非常耗时且容易出错。

**ModMergerTool** 就是为了解决这个问题而开发的，它使用ANTLR4语法解析框架自动：
- 解析脚本文件
- 对比语法树
- 检测冲突
- 辅助合并

## ✨ 核心优势

- **AST级别合并** - 不是简单的文本比对，而是基于抽象语法树的智能合并
- **交互式冲突解决** - 对每个冲突提供5种解决方案供用户选择
- **自动模式** - 支持无人值守的自动合并
- **完整的命令行工具** - 易于集成到自动化流程

## 🚀 快速开始

### 最简单的使用方式

```bash
# 交互模式（会提示处理冲突）
java -jar ModMergerTool-1.0-SNAPSHOT-all.jar ./mod1 ./mod2

# 自动模式（快速合并，所有冲突优先保留mod1）
java -jar ModMergerTool-1.0-SNAPSHOT-all.jar ./mod1 ./mod2 -a keep-mod1 -o ./result
```

### 查看帮助

```bash
java -jar ModMergerTool-1.0-SNAPSHOT-all.jar -h
```

## 📋 文档

- **[README.md](README.md)** - 完整功能文档和技术规格
- **[QUICKSTART.md](QUICKSTART.md)** - 5分钟快速开始指南  
- **[TEST_EXAMPLES.md](TEST_EXAMPLES.md)** - 测试用例说明

## 🏗️ 项目结构

```
ModMergerTool/
├── src/main/antlr4/
│   └── TechlandScript.g4          # ANTLR4语法定义
├── src/main/java/ankol/mod/merger/
│   ├── AppMain.java               # 应用入口
│   ├── MergeConfig.java           # 配置管理
│   ├── ScriptParser.java          # 脚本解析
│   ├── TreeComparator.java        # AST对比
│   ├── ConflictResolver.java      # 冲突解决
│   └── ModMerger.java             # 合并引擎
├── target/
│   └── ModMergerTool-1.0-SNAPSHOT-all.jar  # 可执行JAR
└── pom.xml                        # Maven构建配置
```

## 🔧 构建项目

### 要求
- Java 11 或更高版本
- Maven 3.6 或更高版本

### 编译和打包

```bash
mvn clean package
```

生成的JAR文件：
- `target/ModMergerTool-1.0-SNAPSHOT.jar` - 标准JAR
- `target/ModMergerTool-1.0-SNAPSHOT-all.jar` - 包含依赖的FAT JAR（推荐使用）

## 📖 使用示例

### 例子1：基础交互式合并

```bash
java -jar ModMergerTool-1.0-SNAPSHOT-all.jar \
  ./MyMod \
  ./AnotherMod
```

程序会：
1. 扫描两个模组目录
2. 对比所有脚本文件
3. 对每个冲突提示用户选择合并方案
4. 将结果保存到 `./merged_mod/`

### 例子2：自动合并（保留mod2版本）

```bash
java -jar ModMergerTool-1.0-SNAPSHOT-all.jar \
  ./ModA \
  ./ModB \
  -o ./custom_output \
  -a keep-mod2
```

### 例子3：保留两个版本供审查

```bash
java -jar ModMergerTool-1.0-SNAPSHOT-all.jar \
  ./mod1 \
  ./mod2 \
  -a keep-both \
  -v
```

## 🎯 合并策略说明

| 策略 | 说明 |
|------|------|
| `keep-mod1` | 冲突时保留模组1的版本 |
| `keep-mod2` | 冲突时保留模组2的版本 |
| `keep-both` | 同时保留两个版本（用注释区分） |

交互模式还支持：
- `skip` - 暂不处理
- `manual` - 自定义输入内容

## 💡 工作原理

```
用户输入两个模组路径
    ↓
逐文件解析为ANTLR AST
    ↓
对比两个AST
    ↓
检测差异和冲突
    ↓
交互式或自动解决冲突
    ↓
生成合并结果
    ↓
输出到指定目录
```

## 🔍 支持的脚本元素

基于TechlandScript语法，工具能够识别和对比：

- ✓ 导入导出声明 (`import`, `export`)
- ✓ 函数定义 (`sub`)
- ✓ 变量声明
- ✓ 宏定义 (`$MACRO()`)
- ✓ 函数调用
- ✓ 指令调用 (`!directive()`)

## 🐛 故障排查

**问题：工具无法找到脚本文件**
- 检查文件后缀是否为 `.scr` 或 `.txt`

**问题：合并结果不符合预期**
- 使用交互模式逐一检查冲突
- 使用 `-v` 选项查看详细信息

**问题：内存不足**
- 增加JVM堆大小：`java -Xmx2G -jar ModMergerTool.jar ...`

## 📦 依赖库

- **ANTLR4 Runtime** (4.11.1) - 语法解析
- **Hutool** (5.8.41) - 工具库

所有依赖都已包含在FAT JAR中。

## 🎓 技术细节

### ANTLR4集成
工具使用ANTLR4自动生成词法分析器和语法解析器，支持TechlandScript完整语法。

### 语法树对比
不采用简单的文本比对，而是：
1. 解析两个脚本为完整的抽象语法树（AST）
2. 按定义类型和名称进行索引
3. 精确对比语法元素
4. 报告差异位置和具体变化

### 交互式冲突解决
为每个冲突显示：
- 冲突的具体位置
- 两个版本的代码
- 多个解决方案供用户选择

## 🚢 生产环境部署

```bash
# 复制JAR文件到目标环境
cp target/ModMergerTool-1.0-SNAPSHOT-all.jar /opt/tools/

# 创建别名便于使用
alias modmerge="java -jar /opt/tools/ModMergerTool-1.0-SNAPSHOT-all.jar"

# 使用
modmerge ./mod1 ./mod2 -a keep-mod1
```

## 🔗 相关资源

- Dying Light 官方网站
- ANTLR4 文档：https://www.antlr.org/
- TechlandScript 语法定义：`src/main/antlr4/TechlandScript.g4`

## 📞 技术支持

- 查阅完整文档：`README.md`
- 快速开始指南：`QUICKSTART.md`
- 命令行帮助：`java -jar ModMergerTool.jar -h`

## 🎉 项目状态

✅ **生产就绪** - 所有功能已实现并测试

## 📄 许可证

[项目许可证信息]

---

**开发日期**：2025年12月15日

**版本**：1.0-SNAPSHOT

**状态**：✅ 完成并可投入使用


