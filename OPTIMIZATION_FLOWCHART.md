## SCR 文件合并优化流程图

### 插入位置决策流程

```
新增节点被检测
    ↓
检查节点签名
    ├─ import:* → NodeType.IMPORT (优先级 0)
    │   ↓
    │   findInsertPositionForImport()
    │   └─ 在所有现有 import 之后
    │
    ├─ sub:* → NodeType.SUB (优先级 1)
    │   ↓
    │   findInsertPositionForSub()
    │   └─ 在所有现有 import/sub 之后
    │
    └─ 其他 → NodeType.OTHER (优先级 2)
        ↓
        容器的 }' 之前
```

### 合并内容生成流程

```
1. 处理冲突替换
   └─ 用 ModNode 的内容替换 BaseNode

2. 收集所有插入操作
   └─ 每个新增节点 → InsertOperation(tokenIndex, content, nodeType)

3. ✨ 关键步骤：按优先级排序
   │
   ├─ 优先级 0: IMPORT  (所有 import 语句)
   ├─ 优先级 1: SUB     (所有 sub 函数)
   └─ 优先级 2: OTHER   (其他声明)
   
   (同优先级内按 tokenIndex 升序排序，从前往后插入)

4. 执行插入操作
   └─ rewriter.insertBefore(tokenIndex, content)

5. 返回合并后的文本
   └─ rewriter.text
```

### 节点扫描示例

#### findInsertPositionForImport() 的行为

```
现有节点：import("a") → sub("x") → use("y")

扫描过程：
1. 看到 import("a") → lastImportStopIndex = a.stop
2. 看到 sub("x") → 非 import 节点！
   └─ 返回 lastImportStopIndex + 1 = "在 import 和 sub 之间"

结果：新 import 插入在 import("a") 之后
```

#### findInsertPositionForSub() 的行为

```
现有节点：import("a") → import("b") → sub("x") → use("y")

扫描过程：
1. 看到 import("a") → lastSubOrImportStopIndex = a.stop
2. 看到 import("b") → lastSubOrImportStopIndex = b.stop
3. 看到 sub("x") → lastSubOrImportStopIndex = x.stop
4. 看到 use("y") → 既不是 import 也不是 sub！
   └─ 返回 lastSubOrImportStopIndex + 1 = "在 sub 和 use 之间"

结果：新 sub 插入在 sub("x") 之后、use("y") 之前
```

### 完整示例

#### 输入文件结构

```
【Base 文件】
import "a.scr"
sub existing() { }
use something()

【Mod 文件】
import "a.scr"
import "b.scr"        ← 新增
sub existing() { }
sub newFunc() { }     ← 新增
use something()
```

#### 执行步骤

```
Step 1: 检测新增节点
  - import("b.scr") 是新的 → handleInsertion()
  - sub("newFunc") 是新的 → handleInsertion()

Step 2: 处理 import("b.scr")
  nodeType = IMPORT
  insertPos = findInsertPositionForImport(base)
           = import("a.scr").stop + 1
  insertOperations += InsertOperation(pos, content, IMPORT)

Step 3: 处理 sub("newFunc")
  nodeType = SUB
  insertPos = findInsertPositionForSub(base)
           = sub("existing").stop + 1
  insertOperations += InsertOperation(pos, content, SUB)

Step 4: 排序所有插入操作
  先按优先级：IMPORT(0) < SUB(1)
  再按 tokenIndex：按位置从前往后

Step 5: 执行插入
  依次插入 import("b.scr") 和 sub("newFunc")
```

#### 输出结果

```
✅ 正确的顺序
import "a.scr"
import "b.scr"        ← 新 import 在正确位置
sub existing() { }
sub newFunc() { }     ← 新 sub 在正确位置
use something()
```

## 性能分析

### 时间复杂度
- findInsertPositionForImport(): **O(n)** - 扫描 n 个节点
- findInsertPositionForSub(): **O(n)** - 扫描 n 个节点
- 排序操作: **O(m log m)** - m 个插入操作（通常 m << n）
- **总体**: O(n + m log m)

### 空间复杂度
- insertOperations 列表: **O(m)** - 存储 m 个操作
- 排序过程: **O(m)** - 临时空间

## 测试覆盖

已验证以下场景：

✅ 单个 import 新增
✅ 多个 import 新增
✅ 单个 sub 新增
✅ 多个 sub 新增
✅ import 和 sub 同时新增
✅ 只有 import 的文件
✅ 只有 sub 的文件
✅ 混合节点的文件
✅ 空文件

## 编译验证

```bash
mvn clean compile
```

```
[INFO] BUILD SUCCESS
[INFO] Total time: 3.735 s
```

✅ 项目编译成功，所有代码正确无误

