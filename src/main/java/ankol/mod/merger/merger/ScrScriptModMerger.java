package ankol.mod.merger.merger;

import ankol.mod.merger.antlr4.scr.TechlandScriptParser;
import ankol.mod.merger.merger.ScrConflictResolver.MergeChoice;
import ankol.mod.merger.merger.ScrConflictResolver.MergeDecision;
import ankol.mod.merger.merger.ScrTreeComparator.DiffResult;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.stream.Stream;

/**
 * 核心模组合并引擎 - 协调所有模块完成合并操作
 * <p>
 * 主要职责：
 * 1. 扫描两个模组目录，查找所有脚本文件
 * 2. 建立文件映射（相对路径 -> 文件路径）
 * 3. 逐文件进行：解析、对比、冲突解决、合并
 * 4. 处理两个模组中独有的文件（直接复制）
 * 5. 输出合并结果和统计信息
 * <p>
 * 工作流程：
 * 1. 扫描模组1和模组2的脚本文件
 * 2. 对于两个模组都有的文件，进行合并
 * 3. 对于仅在模组1中的文件，直接复制
 * 4. 对于仅在模组2中的文件，直接复制
 * 5. 输出统计信息
 */
public class ScrScriptModMerger {

    /**
     * 模组1的目录路径
     */
    private final Path mod1Dir;
    /**
     * 模组2的目录路径
     */
    private final Path mod2Dir;
    /**
     * 合并结果输出目录
     */
    private final Path outputDir;

    /**
     * 构造函数 - 初始化合并引擎
     *
     * @param mod1Dir   模组1目录
     * @param mod2Dir   模组2目录
     * @param outputDir 输出目录
     */
    public ScrScriptModMerger(Path mod1Dir, Path mod2Dir, Path outputDir) {
        this.mod1Dir = mod1Dir;
        this.mod2Dir = mod2Dir;
        this.outputDir = outputDir;
    }

    /**
     * 执行合并操作 - 核心主方法
     * <p>
     * 执行流程：
     * 1. 显示配置信息
     * 2. 创建输出目录
     * 3. 扫描模组1和模组2的脚本文件
     * 4. 建立文件映射
     * 5. 处理共同文件（解析、对比、合并、冲突解决）
     * 6. 处理模组1独有文件（复制）
     * 7. 处理模组2独有文件（复制）
     * 8. 输出统计信息
     *
     * @throws IOException 如果文件操作失败
     */
    public void merge() throws IOException {
        // 显示配置信息
        System.out.println("====== Techland Mod Merger ======");
        System.out.println("Mod1: " + mod1Dir);
        System.out.println("Mod2: " + mod2Dir);
        System.out.println("Output: " + outputDir);
        System.out.println();
        // 创建输出目录（如果不存在）
        FileUtil.mkdir(outputDir);
        // 扫描两个模组目录，查找所有脚本文件
        List<Path> scripts1 = findScriptFiles(mod1Dir);
        List<Path> scripts2 = findScriptFiles(mod2Dir);

        System.out.println("Found " + scripts1.size() + " scripts in Mod1");
        System.out.println("Found " + scripts2.size() + " scripts in Mod2");
        System.out.println();

        // 建立文件映射：相对路径 -> 完整路径
        Map<String, Path> map1 = buildFileMap(mod1Dir, scripts1);
        Map<String, Path> map2 = buildFileMap(mod2Dir, scripts2);

        // 统计计数器
        int mergedCount = 0;      // 成功合并（无冲突）的文件数
        int conflictCount = 0;     // 包含冲突的文件数
        int copiedCount = 0;       // 直接复制的文件数（不可解析）
        int addedCount = 0;        // 新增文件数
        boolean hasAnyConflict = false; // 是否存在任何冲突

        // 处理模组1中的文件
        Set<String> processedFiles = new HashSet<>();
        for (String filename : map1.keySet()) {
            // 标记为已处理，避免后面重复处理
            processedFiles.add(filename);

            if (map2.containsKey(filename)) {
                // 两个模组都有这个文件，需要合并
                System.out.println("Processing: " + filename);
                try {
                    if (isParseableFile(filename)) {
                        // 可解析的文件（.scr, .txt）进行智能合并和对比
                        MergeResult result = mergeScriptFiles(map1.get(filename), map2.get(filename));
                        // 写入合并结果
                        Path outputPath = outputDir.resolve(filename);
                        Files.createDirectories(outputPath.getParent());
                        Files.writeString(outputPath, result.mergedContent);
                        // 根据是否有冲突进行计数
                        if (result.hasConflicts) {
                            hasAnyConflict = true;
                            conflictCount++;
                            System.out.println("  ⚠ " + result.conflicts.size() + " conflicts detected");
                        } else {
                            mergedCount++;
                            System.out.println("  ✓ Merged (no conflicts)");
                        }
                    } else {
                        // 不可解析的文件（.def, .model, .loot, .xml等）直接使用mod2版本
                        Path outputPath = outputDir.resolve(filename);
                        Files.createDirectories(outputPath.getParent());
                        Files.copy(map2.get(filename), outputPath, StandardCopyOption.REPLACE_EXISTING);
                        copiedCount++;
                        System.out.println("  ✓ Copied (Mod2 version - non-parseable)");
                    }

                } catch (Exception e) {
                    // 如果合并过程中出错，打印错误信息但继续处理其他文件
                    System.err.println("  ✗ ERROR: " + e.getMessage());
                }
            } else {
                // 只在模组1中存在的文件，直接复制到输出目录
                System.out.println("Copying: " + filename + " (Mod1 only)");
                Path outputPath = outputDir.resolve(filename);
                Files.createDirectories(outputPath.getParent());
                Files.copy(map1.get(filename), outputPath, StandardCopyOption.REPLACE_EXISTING);
                copiedCount++;
            }
        }

        // 处理仅在模组2中存在的文件
        for (String filename : map2.keySet()) {
            if (!processedFiles.contains(filename)) {
                // 这个文件只在模组2中，直接复制到输出目录
                System.out.println("Adding: " + filename + " (Mod2 only)");
                Path outputPath = outputDir.resolve(filename);
                Files.createDirectories(outputPath.getParent());
                Files.copy(map2.get(filename), outputPath, StandardCopyOption.REPLACE_EXISTING);
                addedCount++;
            }
        }

        // 如果存在冲突，给出警告信息
        if (hasAnyConflict) {
            System.out.println("\n⚠️  WARNING: Conflicts detected during merge!");
            System.out.println("Please review the conflicts and ensure all files are correct.");
        }

        // 输出合并完成信息和统计
        System.out.println("\n====== Merge Complete ======");
        System.out.println("Successfully merged: " + mergedCount);
        System.out.println("Copied (no conflicts): " + copiedCount);
        System.out.println("With conflicts: " + conflictCount);
        System.out.println("New files from Mod2: " + addedCount);
        System.out.println("─────────────────────────────");
        System.out.println("Total files: " + (mergedCount + copiedCount + conflictCount + addedCount));
        System.out.println("Output: " + outputDir);
    }

    /**
     * 合并两个脚本文件
     * <p>
     * 执行流程：
     * 1. 使用ScriptParser解析两个脚本为AST
     * 2. 使用TreeComparator对比AST，找出差异
     * 3. 如果没有差异，直接返回模组1的内容
     * 4. 如果有差异，必须进入交互模式让用户选择如何解决冲突
     * 5. 根据用户的冲突决策构建合并结果
     * <p>
     * 重要：当检测到冲突时，即使指定了自动模式（-a），也会强制进入交互模式，
     * 提示用户逐一处理每个冲突，确保用户了解和确认所有的修改。
     *
     * @param script1 模组1的脚本文件路径
     * @param script2 模组2的脚本文件路径
     * @return 合并结果（包含合并后的内容和是否有冲突的标志）
     * @throws IOException 如果文件读取或解析失败
     */
    @SuppressWarnings("all")
    private MergeResult mergeScriptFiles(Path script1, Path script2) throws IOException {
        // 1、使用Antlr4将文件内容解析为AST语法树
        ScrScriptParser script1Parser = new ScrScriptParser();
        ScrScriptParser script2Parser = new ScrScriptParser();
        TechlandScriptParser.FileContext fileTree1 = script1Parser.parseFile(script1);
        TechlandScriptParser.FileContext fileTree2 = script2Parser.parseFile(script2);

        // 第2步：对比两个AST，获取差异列表
        List<DiffResult> diffs = ScrTreeComparator.compareFiles(fileTree1, fileTree2, script1Parser, script2Parser);

        // 初始化合并结果
        MergeResult result = new MergeResult();

        // 第3步：如果没有差异，直接返回模组1的内容
        if (diffs.isEmpty()) {
            result.mergedContent = Files.readString(script1);
            result.hasConflicts = false;
            return result;
        }

        // 第4步：检测到冲突，必须进入交互模式
        // 即使用户指定了自动模式（-a），也要强制进入交互模式处理冲突
        List<MergeDecision> decisions = new ArrayList<>();
        // 交互模式：为每个冲突让用户决策
        if (!diffs.isEmpty()) {
            System.out.println("\n" + "=".repeat(80));
            System.out.println("⚠️ CONFLICTS DETECTED IN [" + script1.getFileName() + "] - User Interaction Required");
            System.out.println("=".repeat(80));
            decisions = ScrConflictResolver.resolveConflicts(diffs);
        }
        // 第5步：根据决策构建合并后的内容
        result = buildMergedContent(script1, script2, decisions);
        return result;
    }

    /**
     * 根据冲突决策构建合并后的内容
     * <p>
     * 执行流程：
     * 1. 以模组1的内容为基础
     * 2. 遍历每个冲突决策
     * 3. 根据决策类型应用不同的合并策略：
     * - KEEP_MOD1: 保持原样（不做改动）
     * - KEEP_MOD2: 用模组2的内容替换模组1的内容
     * - KEEP_BOTH: 保留两个版本，用注释标记
     * - MANUAL: 用用户自定义的内容替换
     * - SKIP: 不处理（保持原样）
     * 4. 返回合并后的内容
     *
     * @param script1Path 模组1的脚本路径（用于读取原始内容）
     * @param script2Path 模组2的脚本路径（用于读取原始内容）
     * @param decisions   冲突决策列表
     * @return 包含合并内容和冲突信息的MergeResult
     * @throws IOException 如果文件读取失败
     */
    private MergeResult buildMergedContent(Path script1Path, Path script2Path, List<MergeDecision> decisions) throws IOException {
        MergeResult result = new MergeResult();

        // 读取两个脚本的原始内容
        String content1 = Files.readString(script1Path);
        String content2 = Files.readString(script2Path);

        // 以模组1的内容为基础开始构建合并结果
        StringBuilder merged = new StringBuilder(content1);

        // 遍历每个冲突决策，按决策应用不同的合并策略
        for (MergeDecision decision : decisions) {
            DiffResult diff = decision.diff();

            switch (decision.choice()) {
                case KEEP_MOD1:
                    // 保留模组1的版本，不做任何改动
                    break;

                case KEEP_MOD2:
                    // 用模组2的版本替换或添加
                    if (diff.tree1 != null && diff.tree2 != null) {
                        // 两个版本都存在，进行替换
                        String text1 = diff.tree1.getText();
                        String text2 = diff.tree2.getText();
                        String contentToMerge = merged.toString();
                        int index = contentToMerge.indexOf(text1);
                        if (index >= 0) {
                            // 找到了模组1的内容，用模组2的替换它
                            merged.replace(index, index + text1.length(), text2);
                        }
                    } else if (diff.tree2 != null) {
                        // 只有模组2存在（模组1中不存在），添加模组2的内容
                        merged.append("\n\n").append(diff.tree2.getText());
                    }
                    break;

                case KEEP_BOTH:
                    // 保留两个版本
                    if (diff.tree2 != null) {
                        // 在末尾添加模组2的内容，并用注释标记来源
                        merged.append("\n\n// Merged from Mod2\n");
                        merged.append(diff.tree2.getText());
                    }
                    break;

                case MANUAL:
                    // 使用用户手动输入的自定义内容
                    if (decision.customContent() != null && !decision.customContent().isEmpty()) {
                        if (diff.tree1 != null) {
                            // 用自定义内容替换模组1的版本
                            String text1 = diff.tree1.getText();
                            String contentToMerge = merged.toString();
                            int index = contentToMerge.indexOf(text1);
                            if (index >= 0) {
                                merged.replace(index, index + text1.length(), decision.customContent());
                            }
                        }
                    }
                    break;

                case SKIP:
                    // 跳过，不处理这个冲突
                    break;
            }

            // 记录所有非SKIP的决策为冲突（用于统计）
            if (decision.choice() != MergeChoice.SKIP) {
                result.conflicts.add(diff);
            }
        }

        // 返回合并结果
        result.mergedContent = merged.toString();
        // hasConflicts 表示是否有被处理的冲突（不包括被跳过的）
        result.hasConflicts = !result.conflicts.isEmpty();

        return result;
    }

    /**
     * 判断文件是否可以被ANTLR4解析
     * <p>
     * 可解析的文件类型：
     * - .scr - 脚本文件（主要）
     * - .txt - 文本脚本文件
     * <p>
     * 不可解析的文件类型（直接复制）：
     * - .def  - 定义文件
     * - .model - 模型文件
     * - .loot - 掉落定义
     * - .xml  - XML文件
     *
     * @param filename 文件名
     * @return true 如果文件可解析，false 否则
     */
    private boolean isParseableFile(String filename) {
        String lower = filename.toLowerCase();
        return StrUtil.endWithAny(lower, ".scr", ".txt");
    }

    /**
     * 递归查找目录中的所有脚本文件
     * <p>
     * 支持的文件类型：
     * - .scr  - Techland脚本文件（主要，可解析对比）
     * - .txt  - 文本脚本文件（可解析对比）
     * - .def  - 定义文件（复制）
     * - .model - 模型定义文件（复制）
     * - .loot - 掉落物品定义（复制）
     * - .xml  - XML配置文件（复制）
     * <p>
     * 执行流程：
     * 1. 检查目录是否存在，不存在返回空列表
     * 2. 使用Files.walk进行深度优先遍历
     * 3. 过滤出常规文件（非目录）
     * 4. 过滤出支持的所有文件类型
     * 5. 返回文件列表
     *
     * @param directory 要扫描的目录
     * @return 找到的脚本文件路径列表
     * @throws IOException 如果目录访问失败
     */
    private List<Path> findScriptFiles(Path directory) throws IOException {
        // 检查目录是否存在
        if (!Files.exists(directory)) {
            return new ArrayList<>();
        }
        List<Path> scripts = new ArrayList<>();
        // 遍历目录树（包括子目录）
        try (Stream<Path> walk = Files.walk(directory)) {
            // 过滤出常规文件（不是目录等其他类型）
            walk.filter(Files::isRegularFile)
                    // 过滤出支持的脚本文件类型
                    .filter(p -> {
                        String fileName = p.getFileName().toString().toLowerCase();
                        // 支持的所有文件类型
                        return StrUtil.endWithAny(fileName,
                                ".scr",     // 脚本文件（可解析）
                                ".txt",     // 文本文件（可解析）
                                ".def",     // 定义文件（复制）
                                ".model",   // 模型定义（复制）
                                ".loot",    // 掉落物品定义（复制）
                                ".xml");    // XML配置（复制）
                    })
                    // 添加到列表
                    .forEach(scripts::add);
        }
        return scripts;
    }

    /**
     * 构建文件映射表
     * <p>
     * 目的：将脚本文件相对路径映射到完整路径，便于后续查找和处理
     * <p>
     * 执行流程：
     * 1. 创建一个LinkedHashMap（有序Map）
     * 2. 对于每个脚本文件，计算其相对于基目录的相对路径
     * 3. 将相对路径和完整路径存入Map
     * 4. 返回Map
     *
     * @param baseDir 基础目录
     * @param scripts 脚本文件路径列表
     * @return 映射表，键为相对路径，值为完整路径
     */
    private Map<String, Path> buildFileMap(Path baseDir, List<Path> scripts) {
        Map<String, Path> map = new LinkedHashMap<>();

        for (Path script : scripts) {
            // 计算脚本相对于基目录的相对路径
            String relativePath = baseDir.relativize(script).toString();
            // 存入映射表
            map.put(relativePath, script);
        }

        return map;
    }

    /**
     * 合并结果内部类 - 存储单个文件的合并结果
     * <p>
     * 字段说明：
     * - mergedContent: 合并后的脚本内容
     * - hasConflicts: 是否包含被处理的冲突（不包括被跳过的）
     * - conflicts: 被处理的冲突列表
     */
    public static class MergeResult {
        /**
         * 合并后的脚本文本内容
         */
        public String mergedContent;
        /**
         * 是否存在冲突（被处理的冲突）
         */
        public boolean hasConflicts;
        /**
         * 冲突列表
         */
        public List<DiffResult> conflicts = new ArrayList<>();
    }
}

