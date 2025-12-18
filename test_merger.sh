#!/bin/bash
# 测试脚本 - 使用 test_mods 目录中的 PAK 文件进行测试合并

cd "$(dirname "$0")" || exit

echo "📦 Testing Mod Merger with test PAK files..."
echo ""

# 创建临时测试目录
TEST_MODS_DIR="test_mods_temp"
mkdir -p "$TEST_MODS_DIR"

# 复制测试 PAK 文件
cp test_mods/test_mod1.pak "$TEST_MODS_DIR/"
cp test_mods/test_mod2.pak "$TEST_MODS_DIR/"

# 运行合并程序
echo "🚀 Running ModMergerTool..."
java -jar target/ModMergerTool-1.0-SNAPSHOT-all.jar

# 检查输出文件
if [ -f "merged_mod.pak" ]; then
    SIZE=$(ls -lh merged_mod.pak | awk '{print $5}')
    echo ""
    echo "✅ Test completed successfully!"
    echo "📦 Output file size: $SIZE"
else
    echo "❌ Test failed! merged_mod.pak not created."
fi

