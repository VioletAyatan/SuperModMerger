# SuperModMerger 改良复核报告

复核时间：2026-04-07

说明：

- 本报告基于当前仓库代码重新核对，不再直接沿用旧结论。
- 已在当前代码中确认完成的优化项，统一使用删除线标记。
- 报告中如果出现“手册曾写已完成，但当前代码未完全体现”的情况，会单独列出，不误判为已完成。

## 1. 当前代码已确认完成的优化项

1. ~~合并上下文初始化收敛~~
现状：

- 当前 `MergerContext` 已要求显式传入 `BaseModManager` 构造。
- `FileMergerEngine` 中不再依赖多个 `lateinit` 字段去拼装上下文。
- 每轮合并前通过 `configure(...)` 明确设置当前文件名、基准来源、待合并来源以及是否首次合并。

涉及文件：

- src/main/java/ankol/mod/merger/core/MergerContext.kt
- src/main/java/ankol/mod/merger/core/FileMergerEngine.kt

1. ~~MergerFactory 去除危险的实例复用~~
现状：

- 当前 `MergerFactory` 缓存的是“扩展名 -> 构造函数”映射。
- `getMerger(...)` 每次都会新建 merger 实例，不再复用带可变 context 的对象。

涉及文件：

- src/main/java/ankol/mod/merger/merger/MergerFactory.kt

1. ~~基准包索引和读取安全性增强~~
现状：

- 文件名索引通过 `Tools.getEntryFileName(...)` 统一转为小写。
- `hasPathConflict(...)`、`getSuggestedPath(...)` 也基于该统一规则工作。
- `extractFileContent(...)` 与 `close()` 已增加同步保护。
- 基准包缺失 entry 时会抛出明确异常，而不是依赖空指针失败。
- 路径修正处已经去掉强制非空断言，改为安全回退。

涉及文件：

- src/main/java/ankol/mod/merger/core/BaseModManager.kt
- src/main/java/ankol/mod/merger/tools/Tools.kt
- src/main/java/ankol/mod/merger/core/FileMergerEngine.kt

1. ~~命令行参数校验从静默失败改为显式失败~~
现状：

- 未知短参数、未知长参数会直接抛出 `BusinessException`。
- 缺少参数值时也会立即报错，并附带本地化提示。

涉及文件：

- src/main/java/ankol/mod/merger/tools/SimpleArgParser.java
- src/main/resources/i18n/message.properties
- src/main/resources/i18n/message_en.properties

## 2. 手册写过已完成，但当前代码未完全落地的项

1. 错误收集改为线程安全并支持每轮重置
当前状态：部分完成。

- 已完成部分：`ErrorReporter` 的错误存储确实已经从普通可变列表改为 `ConcurrentLinkedQueue`。
- 未完成部分：当前 `ErrorReporter` 没有 `reset()`。
- 未完成部分：当前 `FileMergerEngine.merge()` 开始前也没有调用错误状态重置逻辑。

结论：

- 这一项不能整体划掉，只能认定为“线程安全已完成，按轮重置未完成”。

涉及文件：

- src/main/java/ankol/mod/merger/tools/ErrorReporter.kt
- src/main/java/ankol/mod/merger/core/FileMergerEngine.kt

## 3. 当前仍未优化的主要问题

### P0：优先级最高

1. `FileMergerEngine` 仍然承担过多职责。
现状：

- 同时负责解包、过滤、路径修正、合并调度、交互选择、输出写包、统计打印。

影响：

- 后续继续加功能时，回归风险和维护成本会继续上升。

1. `MergerContext` 仍然是可变状态容器。
现状：

- 当前仍保留多个 `var` 字段，并通过 `configure(...)` 在每轮合并前重写。

影响：

- 生命周期边界不够清楚，后续继续扩展时容易增加理解和调试成本。

1. 错误收集的“按轮重置”仍未真正完成。
现状：

- 当前只有并发安全，没有运行轮次隔离。

影响：

- 在同一进程内重复执行时，错误列表仍可能残留历史数据。

### P1：建议下一阶段处理

1. 缺少稳定的自动化测试。
现状：

- 当前 `src/test/java` 下主要还是探索型测试与本地路径测试。
- 还没有看到针对 `SimpleArgParser`、`BaseModManager`、路径修正逻辑的稳定断言测试。

建议优先补：

- `SimpleArgParser`
- `BaseModManager`
- `FileMergerEngine.correctPathsForMod`
- `Tools.getEntryFileName` 及相关索引规则

1. 错误处理仍以控制台输出为主，开发日志上下文不够完整。
现状：

- 用户可见输出主要依赖 `ColorPrinter`。
- 开发日志虽然已通过 logback 写文件，但失败路径里的 file/mod/context 信息还不系统。

建议：

- 继续分离用户提示和开发诊断日志。
- 为关键失败路径补充文件名、mod 名称、合并阶段等上下文。

1. README 与实际行为仍未完全对齐。
现状：

- README 与 README_CN 仍写有命令行 `-f` 全局修复模式说明。
- 当前 `AppMain` 实际只注册了 `m`、`o`、`b`、`h` 四个参数。
- 全局修复模式目前是运行时交互选择，不是命令行参数。

建议：

- 对齐参数说明。
- 明确默认输出位置、默认 base mod 位置、mods 目录约定。
- 说明交互式策略选择的真实行为。

1. `BaseModManager` 的内容读取策略仍偏向“临时目录 + 按需读取”的折中方案。
现状：

- 当前已经有内容缓存和语法树缓存，但外围流程仍围绕解包后文件树进行调度。

影响：

- 对大量小文件场景，磁盘 IO 和临时目录生命周期仍然比较重。

### P2：中长期整理项

1. 提取独立组件仍未开始。
建议候选：

- `ModExtractor`
- `PathCorrectionService`
- `MergeCoordinator`
- `MergeOutputWriter`

1. ANTLR 生成代码与手写逻辑的目录边界仍不清晰。
现状：

- 生成产物仍出现在 `src/main/java/ankol/mod/merger/antlr`。
- 同时 `target/generated-sources/antlr4` 也存在一份生成结果。

影响：

- 维护时容易混淆“源码”和“生成物”的真实边界。

1. Lombok 依赖链仍可继续清理。
现状：

- 当前主源码里基本看不到直接使用 Lombok 注解。
- 但构建链路里仍保留了 `kotlin-maven-lombok`、`lombok` 和注解处理器配置。

建议：

- 先确认是否还有生成代码或未来计划依赖 Lombok。
- 如果没有，逐步从构建配置中移除，减少高版本 JDK 下的兼容性噪音。

## 4. 建议的下一步实施顺序

### 阶段 2：先补测试并修正文档

目标：

- 先把基础行为锁住，再继续重构。

建议任务：

- 为 `SimpleArgParser` 增加未知参数、缺值参数、正常参数测试。
- 为 `BaseModManager` 增加大小写索引命中、缺失 entry、建议路径测试。
- 为路径修正流程补测试。
- 对齐 README、README_CN 与实际 CLI/交互行为。

### 阶段 3：拆主引擎外围流程

目标：

- 先拆流程职责，不先碰 AST 核心语义。

建议拆分：

- `extractAllMods()`
- `correctPathsForMod()`
- `processFiles()`
- 输出写包与统计打印

### 阶段 4：继续收紧状态模型与错误模型

目标：

- 把共享可变状态继续往明确生命周期的数据模型推进。

建议：

- 将“本轮合并配置”和“跨轮历史”拆开。
- 让 merger 进一步依赖显式参数，而不是共享可变 context。

## 5. 当前结论

从当前代码复核结果看，已经可以确认完成并划掉的主要是四类工作：

- 合并上下文初始化收敛
- merger 实例复用风险移除
- base mod 索引与读取安全性增强
- CLI 参数显式报错

但以下关键问题仍然没有完成：

- 主引擎拆分
- 上下文不可变化
- 错误收集按轮重置
- 自动化测试补齐
- README 与实际行为对齐
- ANTLR 生成代码边界清理

因此，当前项目状态更准确的判断是：

- 基础稳定性已经比旧版本更好；
- 但工程化收尾和结构性重构还没有完成；
- 旧手册里有一小部分“已完成”结论需要收回到“部分完成”。

## 6. 仍需保留的后续任务队列

1. 补基础设施测试。
2. 修正 README 与 README_CN 的实际行为说明。
3. 拆分 `FileMergerEngine` 外围职责。
4. 继续收紧 `MergerContext`。
5. 清理 ANTLR 生成代码和构建链中的 Lombok 依赖残留。
