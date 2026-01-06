# ModMergerTool

**[中文](README.md) | English**

## Game Mod Merge Tool

### 📋 Introduction

**ModMergerTool** is an intelligent mod merging tool designed specifically for the Dying Light series, primarily for Dying Light: The Beast and Dying Light 2. It can merge multiple game mod files (.pak format) into a single file, using AST (Abstract Syntax Tree) analysis to achieve intelligent comparison and conflict resolution for script files.

### ✨ Key Features

- 🔀 **Smart Merging**: Support for merging unlimited number of mod files
- 🔧 **Path Correction**: Automatically corrects incorrect file paths in mods based on original game files
- 🔍 **Conflict Detection**: Deep comparison using AST syntax tree analysis
- 👤 **User Interaction**: Clear command-line interface for user to choose conflict resolution options
- 📊 **Detailed Statistics**: Provides detailed processing statistics after merging
- 🌍 **Internationalization**: Currently supports Chinese and English. English is not my native language, so please bear with any grammatical errors, or feel free to submit a PR on my GitHub repository.
- Supports SCR structured script merging and XML file merging, with line-by-line merging and conflict detection
- Merging is based on original game files, which may fix some issues with outdated mods (only when multiple mods have files with the same name, as non-duplicate files are copied directly without parsing). However, try to avoid merging outdated mods to prevent unexpected issues.

### About This Project

I created this tool initially to solve conflict issues when using multiple mods myself.

At first, I discovered **[Unleash The Mods](https://www.nexusmods.com/dyinglightthebeast/mods/140)**, which is also a great tool. However, it had some minor issues and lacked certain features, so I created this brand new tool based on AST syntax tree analysis. It can intelligently identify conflicts in code and perform smart merging. Even in case of errors, it won't break the file structure. I also want to thank the author of **[Unleash The Mods](https://www.nexusmods.com/dyinglightthebeast/mods/140)** for their hard work - some of my merging concepts were inspired by their tool.

Therefore, the basic usage of this tool is fully compatible with **[Unleash The Mods](https://www.nexusmods.com/dyinglightthebeast/mods/140)**, and no runtime libraries need to be installed - it works out of the box.

**The tool supports smart merging of .scr, .loot, .def, .phx, .ppfx and more SCR syntax structure files, as well as .xml files. It's not limited to common player_variables.scr file merging. The syntax parser has been tested against the entire original data0.pak file to ensure no conflicts. In theory, any file with correct SCR syntax can be recognized and merged by this tool.**

It should theoretically support Dying Light 2 mod merging as well, since Dying Light 2's directory structure is identical to The Beast. Dying Light 1 should also be supported as long as the SCR syntax is compatible, but for Dying Light 1, you'll need to manually specify the base mod location via command line.

### Bug Reports

If you find any issues, please report them to me promptly and provide information about the mods you're trying to merge.

### Supported Operating Systems

- **Windows 10**
- **Windows 11**
- Other Windows versions have not been tested.

### 🚀 Quick Start

#### **1. Place the tool in the game's root directory/ph_ft folder, create a mods directory, and put the mods you want to merge inside**

**Prepare mod files - supports zip, pak, 7z and other formats**

```bash
# Example
Dying Light The Beast\ph_ft\mods
├── mod1.pak
├── mod2.pak
└── mod3.pak
```

#### 2. Run the Merge Program

```bash
# Double-click to run the merge tool
```

#### 3. View Results

The merged mod will be output to the data7.pak file in the source directory. Note: if you already have a data7.pak file, this tool will overwrite it.

