# CHANGELOG

## 1.0.0

- 支持scr脚本逐行合并合并，工具使用Antlr将Techland脚本解析为AST语法树进行代码分析，能够理解techland
  scr脚本结构，自动识别冲突的语句，提供友好的交互界面提示用户逐步解决冲突。
- 兼容 [Unleash The Mods](https://www.nexusmods.com/dyinglightthebeast/mods/140) 工具的使用方法，本工具默认使用方法与它一致。
- 自动修正MOD文件错误的放置路径。
- 自动合并多个MOD为同一个。
- 自动解决冲突。
- 语法识别经过了Techland全部文件的校验，确认无问题。
- 无需任何运行库，开箱即用！