#!/usr/bin/env pwsh
# 创建测试用的 .pak 文件（ZIP 压缩包）

$mod1Dir = "D:\Projects\ModMergerTool\test_mods\mod1"
$mod2Dir = "D:\Projects\ModMergerTool\test_mods\mod2"

# 清理旧的测试 pak 文件
Remove-Item "D:\Projects\ModMergerTool\test_mods\test_mod1.pak" -Force -ErrorAction SilentlyContinue
Remove-Item "D:\Projects\ModMergerTool\test_mods\test_mod2.pak" -Force -ErrorAction SilentlyContinue

# 创建 test_mod1.pak
Add-Type -AssemblyName System.IO.Compression.FileSystem
[System.IO.Compression.ZipFile]::CreateFromDirectory($mod1Dir, "D:\Projects\ModMergerTool\test_mods\test_mod1.pak", "Optimal", $false)

# 创建 test_mod2.pak
[System.IO.Compression.ZipFile]::CreateFromDirectory($mod2Dir, "D:\Projects\ModMergerTool\test_mods\test_mod2.pak", "Optimal", $false)

Write-Host "✅ Created test PAK files:"
Write-Host "  - test_mods\test_mod1.pak"
Write-Host "  - test_mods\test_mod2.pak"

