@echo off
setlocal enabledelayedexpansion

REM 查找当前目录下的exe文件（排除rcedit-x64.exe）
set "target_exe="
for %%f in (*.exe) do (
    if /i not "%%f"=="rcedit-x64.exe" (
        set "target_exe=%%f"
    )
)

REM 检查是否找到exe文件
if "%target_exe%"=="" (
    echo 错误：未找到目标exe文件！
    echo 请确保打包后的exe文件已复制到builds目录。
    pause
    exit /b 1
)

echo 找到目标文件：%target_exe%
echo 正在修改exe属性...

rcedit-x64.exe "%target_exe%" --set-icon .\wukong.ico ^
--set-file-version 1.1.0 ^
--set-product-version 1.1.0 ^
--set-version-string ProductName "Super Mod Merger" ^
--set-version-string FileDescription "Super Mod Merger" ^
--set-version-string CompanyName "Ankol" ^
--set-version-string LegalCopyright "Copyright (C) 2024 Ankol"

if %errorlevel% equ 0 (
    echo 成功修改exe属性！
) else (
    echo 修改exe属性失败！
)

pause
