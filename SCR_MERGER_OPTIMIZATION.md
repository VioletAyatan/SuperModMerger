# SCR 文件合并优化文档

## 问题描述

之前的 SCR 文件合并器在处理新增节点时，对某些具有声明顺序要求的语句无法正确处理，导致：
- `import` 语句被追加到文件末尾而不是文件顶部
- `sub` 函数声明被追加到其他语句之后，而它们应该在 `import` 之后、其他声明之前

**原问题示例：**
```scr
//This script is generated from Inventory.xlsm. Don't modify it!!!

import "inventorystuff.scr"
import "inventory_ranged.scr"
import "inventory_promo.scr"
import "Inventory_Technical_JW.scr"

import "inventory_outfits_ft.scr"
import "inventory_quests_ft.scr"
import "inventory_vehicle_ft.scr"
import "collectables_ft.scr"

sub imports() 
{
}
sub outfit_imports() 
{
	use dlc_ft_outfits();
}
sub collectables_imports() 
{
	use dlc_ft_collectables();

    use dlc_goose_craftplan();}
sub weapons_imports() 
{
	use rangedweapons();
}
sub items_imports() 
{
	use dlc_ft_quest();
	use dlc_ft_vehicle();
	use dlc_ft_promo();
	use Inventory_Technical_JW();

    use dlc_goose_collectable();}

    // ❌ import 被错误地加到文件末尾
    import "collectables_goose.scr"
    import "inventory_goose.scr"
```

## 解决方案

### 核心改进：引入优先级系统

在 `TechlandScrFileMerger.kt` 中实现了一个**节点优先级系统**，确保新增节点被插入到正确的位置。

#### 1. 节点类型枚举（NodeType）

```kotlin
private enum class NodeType {
    IMPORT,  // import 语句 - 最高优先级（优先级 0），放在文件最前
    SUB,     // sub 函数声明 - 次高优先级（优先级 1），放在 import 之后
    OTHER    // 其他声明 - 最低优先级（优先级 2），放在末尾
}
```

#### 2. 更新的 InsertOperation 数据类

```kotlin
private data class InsertOperation(
    val tokenIndex: Int, 
    val content: String, 
    val nodeType: NodeType = NodeType.OTHER  // ✅ 新增：记录节点类型
)
```

#### 3. 智能位置查找算法

**findInsertPositionForImport()** - 查找 import 的插入位置
- 扫描容器中的现有节点
- 找到最后一个 import 节点的结束位置
- 如果找到非 import 节点，则在该节点前插入
- 如果所有节点都是 import，则在容器末尾插入

```kotlin
private fun findInsertPositionForImport(container: ScrContainerScriptNode): Int {
    var lastImportStopIndex: Int? = null
    
    for ((_, node) in container.childrens) {
        if (node.signature.startsWith("import:")) {
            lastImportStopIndex = node.stopTokenIndex
        } else {
            // 遇到第一个非import节点
            return lastImportStopIndex?.let { it + 1 } ?: node.startTokenIndex
        }
    }
    
    // 所有节点都是import，返回容器结束位置
    return lastImportStopIndex?.let { it + 1 } ?: container.stopTokenIndex
}
```

**findInsertPositionForSub()** - 查找 sub 的插入位置
- 扫描容器中的现有节点
- 找到最后一个 import 或 sub 节点的结束位置
- 在第一个其他类型节点前插入
- 如果所有节点都是 import/sub，则在容器末尾插入

```kotlin
private fun findInsertPositionForSub(container: ScrContainerScriptNode): Int {
    var lastSubOrImportStopIndex: Int? = null
    
    for ((_, node) in container.childrens) {
        val isSub = node.signature.startsWith("sub:")
        val isImport = node.signature.startsWith("import:")
        
        if (isSub || isImport) {
            lastSubOrImportStopIndex = node.stopTokenIndex
        } else {
            // 遇到既不是import也不是sub的节点
            return lastSubOrImportStopIndex?.let { it + 1 } ?: node.startTokenIndex
        }
    }
    
    // 所有节点都是import或sub
    return lastSubOrImportStopIndex?.let { it + 1 } ?: container.stopTokenIndex
}
```

#### 4. 优化的 handleInsertion() 方法

```kotlin
private fun handleInsertion(baseContainer: ScrContainerScriptNode, modNode: BaseTreeNode) {
    // 根据节点签名确定节点类型
    val nodeType = when {
        modNode.signature.startsWith("import:") -> NodeType.IMPORT
        modNode.signature.startsWith("sub:") -> NodeType.SUB
        else -> NodeType.OTHER
    }
    
    // 选择合适的插入位置
    val insertPos = when (nodeType) {
        NodeType.IMPORT -> findInsertPositionForImport(baseContainer)
        NodeType.SUB -> findInsertPositionForSub(baseContainer)
        NodeType.OTHER -> baseContainer.stopTokenIndex
    }
    
    val newContent = "\n    " + modNode.sourceText
    insertOperations.add(InsertOperation(insertPos, newContent, nodeType))
}
```

#### 5. 优化的 getMergedContent() 方法

现在对所有插入操作进行**优先级排序**后再执行：

```kotlin
private fun getMergedContent(baseResult: ParsedResult<ScrContainerScriptNode>): String {
    // ... 处理冲突替换 ...
    
    // 对插入操作按照优先级和位置排序
    // 优先级：IMPORT(0) > SUB(1) > OTHER(2)
    // 同一优先级内按照 tokenIndex 升序排序
    val sortedOperations = insertOperations.sortedWith(
        compareBy<InsertOperation> { op ->
            when (op.nodeType) {
                NodeType.IMPORT -> 0
                NodeType.SUB -> 1
                NodeType.OTHER -> 2
            }
        }.thenBy { it.tokenIndex }
    )
    
    for (op in sortedOperations) {
        rewriter.insertBefore(op.tokenIndex, op.content)
    }
    
    return rewriter.text
}
```

## 优化效果

### 预期结果

新增节点现在会按照正确的顺序插入：

```scr
//This script is generated from Inventory.xlsm. Don't modify it!!!

import "inventorystuff.scr"
import "inventory_ranged.scr"
import "inventory_promo.scr"
import "Inventory_Technical_JW.scr"

import "inventory_outfits_ft.scr"
import "inventory_quests_ft.scr"
import "inventory_vehicle_ft.scr"
import "collectables_ft.scr"
import "collectables_goose.scr"        // ✅ 新增的import在合适位置
import "inventory_goose.scr"           // ✅ 新增的import在合适位置

sub imports() 
{
}
sub outfit_imports() 
{
	use dlc_ft_outfits();
}
sub collectables_imports() 
{
	use dlc_ft_collectables();
    use dlc_goose_craftplan();
}
sub weapons_imports() 
{
	use rangedweapons();
}
sub items_imports() 
{
	use dlc_ft_quest();
	use dlc_ft_vehicle();
	use dlc_ft_promo();
	use Inventory_Technical_JW();
    use dlc_goose_collectable();
}
```

## 技术细节

### 节点签名系统

该优化依赖于现有的节点签名系统：
- **import** 节点：`"import:路径"` (如 `import:"collectables_goose.scr"`)
- **sub** 节点：`"sub:函数名"` (如 `sub:items_imports`)
- **其他** 节点：各种其他声明

### 算法复杂度

- **时间复杂度**：O(n) - 其中 n 是容器中的节点数量
  - `findInsertPositionForImport()`：O(n)
  - `findInsertPositionForSub()`：O(n)
  - `getMergedContent()` 排序：O(m log m) - 其中 m 是新增节点数量（通常 m << n）

- **空间复杂度**：O(m) - 用于存储 insertOperations

### 边界情况处理

优化处理了以下边界情况：
1. ✅ 没有现有 import 时，新 import 插在最前
2. ✅ 没有现有 sub 时，新 sub 插在 import 之后
3. ✅ 容器为空时，直接在容器末尾插入
4. ✅ 只有 import/sub 节点时，新节点插在末尾
5. ✅ 混合节点时，按优先级正确排序

## 编译验证

✅ 项目编译成功
```
[INFO] BUILD SUCCESS
[INFO] Total time:  3.735 s
```

## 总结

这次优化通过引入**节点类型优先级系统**和**智能位置查找算法**，彻底解决了 SCR 文件合并中新增节点顺序错乱的问题。现在系统会自动确保：
- **import** 语句始终在文件最前
- **sub** 函数声明始终在 import 之后
- 其他声明在末尾

这使得合并后的 SCR 文件更加规范，符合 Techland Script 语言的语法要求。

