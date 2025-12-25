# Token索引工作原理 - 完整解析

## 核心概念

### Token是什么？
Token是XML解析器（ANTLR4）将XML文本分解后的最小单位。每个token都有一个**唯一的索引**（从0开始）。

### 示例：你的XML片段

```xml
<skill id="ledge_master" cat="Agility" skill_points_type="Traversal">
  <effect id="ShimmyUpRange" change="0.5"/>  
</skill>
<skill id="ledge_master2" cat="Agility" skill_points_type="Traversal">
  <effect id="ShimmyUpRange" change="1.5" />
</skill>
```

### ANTLR4解析结果（Token列表）

```
[0]  <                        (开始第一个skill)
[1]  skill
[2]  id
[3]  =
[4]  "ledge_master"
[5]  cat
[6]  =
[7]  "Agility"
[8]  skill_points_type
[9]  =
[10] "Traversal"
[11] >
[12] <!--...-->
[13] <                        (effect标签)
[14] effect
[15] id
[16] =
[17] "ShimmyUpRange"
[18] change
[19] =
[20] "0.5"
[21] />
[22] </                       (结束第一个skill)
[23] skill
[24] >
───────────────────────────── (分界线：第一个节点在这里结束)
[25] <                        (开始第二个skill)
[26] skill
[27] id
[28] =
[29] "ledge_master2"
...
[48] >                        (结束第二个skill)
```

## XmlNode中的索引字段

```java
class XmlNode {
    String signature = "element:skill:id=ledge_master";
    int startTokenIndex = 0;   // 指向 token[0] = "<"
    int stopTokenIndex = 24;   // 指向 token[24] = ">"
}
```

**含义**：
- `startTokenIndex = 0` → 这个节点从token[0]开始
- `stopTokenIndex = 24` → 这个节点在token[24]结束
- 这个节点包含的所有tokens = [0, 1, 2, ..., 23, 24] 共25个tokens

## 三种插入场景

### 场景1：在两个兄弟节点之间插入

```
【代码】
XmlNode previousSibling = ledge_master;      // stopTokenIndex = 24
int insertPosition = previousSibling.getStopTokenIndex() + 1;
                  = 24 + 1
                  = 25;
rewriter.insertBefore(insertPosition, newNodeText);
     ↓
rewriter.insertBefore(25, newNodeText);

【结果】
token[24] = ">"  (ledge_master的结束>)
token[25] = "<"  (ledge_master2的开始<)
           ↑
     在这里插入新节点

【最终XML】
<skill id="ledge_master">...</skill>
<skill id="ledge_master3">...</skill>  ← 新插入
<skill id="ledge_master2">...</skill>
```

### 场景2：作为容器的第一个子节点插入

```
【代码】
XmlContainerNode parent = ledge_master;      // stopTokenIndex = 24
if (previousSibling == null) {
    int insertPosition = parent.getStopTokenIndex();
                      = 24;
    rewriter.insertBefore(insertPosition, newNodeText);
}

【结果】
token[23] = "skill"    (</skill>的skill)
token[24] = ">"        (</skill>的>)
           ↑
     在这里之前插入

【最终XML】
<skill id="ledge_master">
  <effect id="NEW" change="1"/>  ← 新插入（第一个子节点）
  <effect id="ShimmyUpRange" change="0.5"/>
</skill>
```

### 场景3：插入多个节点（重点）

```
【代码】
for (NewNodeRecord record : newNodes) {
    XmlNode previousSibling = record.previousSibling();
    
    int insertPosition;
    if (previousSibling != null) {
        insertPosition = previousSibling.getStopTokenIndex() + 1;
    } else {
        insertPosition = parent.getStopTokenIndex();
    }
    
    rewriter.insertBefore(insertPosition, newNodeText);
}

【关键】：TokenStreamRewriter会自动处理多个insertBefore操作
  - 第一次insertBefore(25, nodeA) → 插入nodeA
  - 第二次insertBefore(25, nodeB) → TokenStreamRewriter自动调整位置
  - 结果：nodeB插在nodeA之后（即使同一位置也不会覆盖）
```

## 为什么要 +1？

```
ledge_master:  token[0] ... token[24]
ledge_master2: token[25] ... token[48]

如果 insertPosition = 24:
  rewriter.insertBefore(24, newNode)
  → 在token[24]之前插入
  → 新节点会插入到ledge_master内部 ❌ 错误

如果 insertPosition = 25:
  rewriter.insertBefore(25, newNode)
  → 在token[25]之前插入
  → 新节点会插入到ledge_master2之前 ✓ 正确

所以：insertPosition = stopTokenIndex + 1
```

## 为什么第一个子节点不用 +1？

```
parent:        token[0] ... token[11]>  token[12-23] ... token[24]>
               ↑                          ↑                      ↑
          开始标签                     内容                  结束标签

如果要插入第一个子节点（没有previousSibling）：
  insertPosition = parent.getStopTokenIndex() = 24
  rewriter.insertBefore(24, newNode)
  → 在token[24]（结束标签的>）之前插入
  → 新节点会在结束标签前面 ✓ 正确

这样新节点就成为容器的第一个子节点了
```

## TokenStreamRewriter的工作原理

```java
// 原始tokens
[0]< [1]skill ... [24]> [25]< ... [48]>

// 操作1：替换effect节点
rewriter.replace(13, 21, newEffectText);

// 操作2：在节点之后插入
rewriter.insertBefore(25, newSkillText);

// 获取最终结果
String result = rewriter.getText();
```

TokenStreamRewriter会：
1. 记录所有的修改操作（replace、insertBefore、insertAfter等）
2. 维护这些操作与token索引的对应关系
3. 最后一次性生成完整的修改后文本
4. 自动处理重叠和冲突

## 代码实际应用

```java
// 你的实际代码
for (NewNodeRecord record : newNodes) {
    XmlNode previousSibling = record.previousSibling();
    XmlNode newNode = record.newNode();

    int insertPosition;
    if (previousSibling != null) {
        // 有前一个兄弟：在其后面（+1是为了跳过该节点的最后一个token）
        insertPosition = previousSibling.getStopTokenIndex() + 1;
    } else {
        // 没有前一个兄弟：在父容器结束标签前（不需要+1）
        XmlContainerNode parentContainer = record.parentContainer();
        insertPosition = parentContainer.getStopTokenIndex();
    }

    // 在计算出的位置之前插入
    rewriter.insertBefore(insertPosition, "\n    " + newNode.getSourceText());
}

// 获取最终的合并文本
String mergedContent = rewriter.getText();
```

## 图示总结

```
场景1：有前一个兄弟
━━━━━━━━━━━━━━━━━━
node1:  [0 ...  24]>
               ↑↑↑
           24  25(insertBefore)
                  node2: [25 ...  48]>

node2被插在这里：[0 ... 24]> [NEW_NODE] [25 ... 48]>


场景2：没有前一个兄弟（第一个子节点）
━━━━━━━━━━━━━━━━━━━━━━━━━
parent: [0 ... 11]> [内容 ...  23] [24]>
                                   ↑↑
                              24(insertBefore)

新节点被插在这里：[0 ... 11]> [内容] [NEW_NODE] [23] [24]>
```

## 总结

| 情况 | insertPosition计算 | 结果 |
|------|-------------------|------|
| 有前一个兄弟节点 | stopTokenIndex + 1 | 新节点插在前一个节点之后 |
| 没有前一个兄弟节点 | stopTokenIndex（父容器） | 新节点插在结束标签之前 |
| 多个节点都插同位置 | TokenStreamRewriter自动调整 | 都被正确插入，不会覆盖 |

现在你应该清楚地理解了token索引是如何工作的！
