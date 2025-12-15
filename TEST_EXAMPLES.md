# 测试样例说明

本目录包含了用于测试ModMergerTool的示例模组文件。

## 文件结构

```
test_mods/
├── mod1/
│   └── scripts/
│       ├── player.scr
│       └── npc.scr
├── mod2/
│   └── scripts/
│       ├── player.scr
│       └── weapon.scr
└── expected_output/
    └── 预期的合并结果
```

## 如何运行测试

### 1. 交互模式测试
```bash
java -jar target/ModMergerTool-1.0-SNAPSHOT-all.jar test_mods/mod1 test_mods/mod2 -o test_mods/result_interactive
```

程序会提示你处理每个冲突。

### 2. 自动模式测试（保留Mod1）
```bash
java -jar target/ModMergerTool-1.0-SNAPSHOT-all.jar test_mods/mod1 test_mods/mod2 -o test_mods/result_mod1 -a keep-mod1
```

### 3. 自动模式测试（保留Mod2）
```bash
java -jar target/ModMergerTool-1.0-SNAPSHOT-all.jar test_mods/mod1 test_mods/mod2 -o test_mods/result_mod2 -a keep-mod2
```

### 4. 自动模式测试（保留两个）
```bash
java -jar target/ModMergerTool-1.0-SNAPSHOT-all.jar test_mods/mod1 test_mods/mod2 -o test_mods/result_both -a keep-both
```

## 预期输出

合并成功后，各结果目录应包含：
- `scripts/player.scr` - 合并的玩家脚本
- `scripts/npc.scr` - 来自mod1的NPC脚本
- `scripts/weapon.scr` - 来自mod2的武器脚本

## 验证结果

对比生成的文件与 `expected_output/` 目录中的文件，确认合并结果正确。


