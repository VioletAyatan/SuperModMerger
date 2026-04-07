# SuperModMerger 改进报告

## 1. 本轮已完成改进

### 1.1 合并上下文初始化收敛

涉及文件：

- src/main/java/ankol/mod/merger/core/MergerContext.kt
- src/main/java/ankol/mod/merger/core/FileMergerEngine.kt

已完成内容：

- 将 `MergerContext` 从依赖多个 `lateinit` 字段的脆弱状态对象，调整为必须显式传入 `BaseModManager` 的构造方式。
- 增加 `configureMerge(...)` 统一设置每次合并的文件名、base mod 名称、merge mod 名称和“是否首次与 data0.pak 合并”标记。
- 消除了 `FileMergerEngine` 中多个 `apply {}` 分散初始化上下文的写法，降低漏填字段和未初始化访问风险。

收益：

- 减少 `UninitializedPropertyAccessException` 风险。
- 让上下文创建和切换更可控，后续继续演进为不可变上下文会更容易。

### 1.2 MergerFactory 去除危险的实例复用

涉及文件：

- src/main/java/ankol/mod/merger/merger/MergerFactory.kt

已完成内容：

- 移除了按类型缓存 `AbstractFileMerger` 实例的机制。
- 改为每次 `getMerger(...)` 调用时创建新的 merger 实例。

原因：

- 旧实现会复用带有可变 `context` 的 merger 实例。
- 在多文件、多轮合并场景中，这会造成状态串用和隐性线程安全问题。

收益：

- 消除 merger 实例内部状态污染风险。
- 为后续并发提取与串行合并的边界治理打基础。

### 1.3 错误收集改为线程安全并支持每轮重置

涉及文件：

- src/main/java/ankol/mod/merger/tools/ErrorReporter.kt
- src/main/java/ankol/mod/merger/core/FileMergerEngine.kt

已完成内容：

- 将错误存储从 `mutableListOf` 改为 `ConcurrentLinkedQueue`。
- 增加 `reset()`，并在 `FileMergerEngine.merge()` 开始前调用，避免多次运行串错误数据。

收益：

- 降低并行提取 mod 时的错误写入竞争问题。
- 同一进程多次运行时，错误报告不再混杂历史结果。

### 1.4 基准包索引和读取安全性增强

涉及文件：

- src/main/java/ankol/mod/merger/core/BaseModManager.kt
- src/main/java/ankol/mod/merger/tools/Tools.kt
- src/main/java/ankol/mod/merger/core/FileMergerEngine.kt

已完成内容：

- 基准包文件名索引统一改为按小写文件名检索，减少大小写不一致导致的查找失败。
- `BaseModManager.hasPathConflict()` 与 `getSuggestedPath()` 也统一使用小写文件名匹配。
- 为 `extractFileContent()` 和 `close()` 增加同步保护。
- 将 `extractFileFromPak()` 中的 `zipFileConnection!!` 替换为显式 `requireNotNull(...)`。
- 当基准包中不存在目标 entry 时，改为抛出有意义的 IO 异常，而不是隐式空指针崩溃。
- `FileMergerEngine.correctPathsForMod()` 去掉了 `getSuggestedPath(...)!!` 的强制非空断言，改为安全回退。

收益：

- 基准包缺失文件时不再直接空指针崩溃。
- 路径修正逻辑更稳，大小写兼容性更好。
- 基准内容提取与缓存访问的生命周期更清晰。

### 1.5 命令行参数校验从静默失败改为显式失败

涉及文件：

- src/main/java/ankol/mod/merger/tools/SimpleArgParser.java
- src/main/resources/i18n/message.properties
- src/main/resources/i18n/message_en.properties

已完成内容：

- 未知短参数、未知长参数不再被当作位置参数悄悄吞掉，改为直接抛出 `BusinessException`。
- 对缺失参数值的情况增加了显式校验和本地化错误提示。
- 新增中英文提示文案：未知参数、参数缺值。

收益：

- 用户输入错误时能立即得到清晰反馈。
- 减少“程序没有按预期工作但没有报错”的诊断成本。

## 2. 已完成验证

验证方式：

- 使用 Maven 执行 `mvn -q -DskipTests compile`

验证结果：

- 编译通过。
- 当前未发现由本轮修改引入的编译错误。
- 构建日志中仍有 Lombok 在较新 JDK 上触发的 `sun.misc.Unsafe` 弃用警告，但不影响当前构建通过。

## 3. 当前仍然存在的主要问题

### P0：需要优先处理

1. `FileMergerEngine` 职责仍然过重。
现状：

- 同时承担提取、过滤、路径修正、合并调度、统计、输出打包等职责。
风险：
- 后续功能继续叠加后，维护成本会快速上升。

1. `BaseModManager` 的缓存策略仍偏向磁盘中转。
现状：

- 当前仍是“按需从 pak 提取到临时目录，再读文本”。
风险：
- 对大量小文件会产生较高 IO 开销。
- 生命周期与缓存失效机制仍不够明确。

1. 合并上下文虽然稳定了，但还不是不可变设计。
现状：

- `MergerContext` 仍允许每轮修改字段。
风险：
- 复杂调用链下仍然可能出现状态残留理解成本。

### P1：建议下一阶段处理

1. 缺少针对入口层和基础设施层的自动化测试。
建议优先补：

- `SimpleArgParser`
- `BaseModManager`
- `Tools.indexPakFile`
- `FileMergerEngine.correctPathsForMod`

1. 错误处理仍以控制台输出为主，日志上下文不够完整。
建议：

- 将用户可见提示与开发者日志分离。
- 为关键失败路径补充 file/mod/context 信息。

1. README 和实际行为仍需对齐。
建议：

- 参数默认值
- 交互式策略选择说明
- base mod 与 mods 目录约定

### P2：中长期整理项

1. 提取独立组件：

- ModExtractor
- PathCorrectionService
- MergeCoordinator
- MergeOutputWriter

1. 进一步清理 ANTLR 生成代码与手写逻辑的目录边界。

2. 评估 Lombok 使用范围，逐步减少对新 JDK 下兼容性较差路径的依赖。

## 4. 建议的下一步实施顺序

### 阶段 2：补基础测试和入口文档

目标：

- 先为已改造过的基础设施补测试，锁住行为。
建议任务：
- 为 `SimpleArgParser` 增加未知参数、缺值参数、合法参数解析测试。
- 为 `BaseModManager` 增加索引命中、大小写兼容、缺失 entry 异常测试。
- 为 `Tools.indexPakFile` 增加重复文件名与小写索引测试。

### 阶段 3：拆 FileMergerEngine

目标：

- 把主引擎按职责切开，先拆外围流程，不碰核心 AST 语义。
建议拆分：
- `extractAllMods()` 抽到 extractor/service
- `correctPathsForMod()` 抽到 path correction service
- `processFiles()` 与统计逻辑解耦

### 阶段 4：收紧合并态模型

目标：

- 逐步把 `MergerContext` 从“可变状态容器”继续推进到“明确生命周期的数据对象”。
建议：
- 把“本轮合并配置”和“跨轮 merged history”分开。
- 让 merger 更依赖显式参数，而不是共享上下文对象。

## 5. 当前结论

本轮改进属于“稳定性加固”和“入口可用性修复”。
它没有重写 SMM 的核心 AST 合并规则，但已经先处理了最容易导致运行时崩溃、状态串用、错误难以诊断的基础问题。

从工程推进顺序看，这是合理的：

- 先稳住基础设施；
- 再补测试和文档；
- 最后再拆主引擎和进一步重构状态模型。

## 6. 后续任务保持

建议保持以下后续任务队列：

1. 补基础设施测试。
2. 输出更细的入口到合并链路设计文档。
3. 拆分 `FileMergerEngine` 外围职责。
4. 继续收紧 `MergerContext` 和错误日志模型。
5. 对 README 与 CLI 行为做一致性整理。
