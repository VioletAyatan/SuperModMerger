package ankol.mod.merger.core;

import ankol.mod.merger.merger.MergeResult;
import ankol.mod.merger.tools.FileTree;
import ankol.mod.merger.tools.Tools;
import cn.hutool.core.util.StrUtil;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Stream;

/**
 * 模组合并引擎 - 负责执行模组合并的核心逻辑
 *
 * @author Ankol
 */
public class ModMergerEngine {

    private final List<Path> modsToMerge;

    /**
     * 构造函数 - 初始化合并引擎
     */
    public ModMergerEngine(List<Path> modsToMerge) {
        this.modsToMerge = modsToMerge;
    }

    /**
     * 开始执行合并逻辑
     */
    public void merge() throws IOException {
        // 显示配置信息
        System.out.println("====== Techland Mod Merger ======");
        System.out.println();
        // 扫描两个模组目录，查找所有脚本文件
        for (Path path : modsToMerge) {
            Map<String, FileTree> fileTreeMap = Tools.buildFileTree(path);
        }
        // 统计计数器
        int mergedCount = 0;      // 成功合并（无冲突）的文件数
        int conflictCount = 0;     // 包含冲突的文件数
        int copiedCount = 0;       // 直接复制的文件数（不可解析）
        int addedCount = 0;        // 新增文件数
        boolean hasAnyConflict = false; // 是否存在任何冲突

        // 处理模组1中的文件
        Set<String> processedFiles = new HashSet<>();
        for (String filename : mod1FileTree.keySet()) {
            // 标记为已处理，避免后面重复处理
            processedFiles.add(filename);
            if (mod2FileTree.containsKey(filename)) {
                // 两个模组都有这个文件，需要合并
                System.out.println("Processing: " + filename);
                try {
                    Optional<IFileMerger> mergerOptional = MergerFactory.getMerger(filename);
                    if (mergerOptional.isPresent()) {
                        // 可解析的文件（.scr, .txt）进行智能合并和对比
                        MergeResult result = mergerOptional.get().merge(mod1FileTree.get(filename), mod2FileTree.get(filename));
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
//                        Files.copy(mod2.get(filename), outputPath, StandardCopyOption.REPLACE_EXISTING);
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
//                Files.copy(map1.get(filename), outputPath, StandardCopyOption.REPLACE_EXISTING);
                copiedCount++;
            }
        }

        // 处理仅在模组2中存在的文件
        for (String filename : mod2FileTree.keySet()) {
            if (!processedFiles.contains(filename)) {
                // 这个文件只在模组2中，直接复制到输出目录
                System.out.println("Adding: " + filename + " (Mod2 only)");
                Path outputPath = outputDir.resolve(filename);
                Files.createDirectories(outputPath.getParent());
//                Files.copy(map2.get(filename), outputPath, StandardCopyOption.REPLACE_EXISTING);
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
     * 如果 modMap 中有文件路径在 baseMap 中不存在，尝试用文件名在 baseMap 中唯一匹配，
     * 若匹配成功则把 modMap 的 key 重映射到基准的相对路径，从而修正放错位置的文件。
     */
    private void alignPathsToBaseline(Map<String, Path> baseMap, Map<String, Path> modMap) {
        // 构建基准文件名 -> 相对路径 列表
        Map<String, List<String>> nameToPaths = new HashMap<>();
        for (String rel : baseMap.keySet()) {
            String name = Path.of(rel).getFileName().toString();
            nameToPaths.computeIfAbsent(name, k -> new ArrayList<>()).add(rel);
        }

        List<String> toRemove = new ArrayList<>();
        Map<String, Path> toAdd = new HashMap<>();

        for (Map.Entry<String, Path> e : modMap.entrySet()) {
            String rel = e.getKey();
            if (baseMap.containsKey(rel)) continue; // already matches
            String name = e.getValue().getFileName().toString();
            List<String> candidates = nameToPaths.get(name);
            if (candidates != null && candidates.size() == 1) {
                String targetRel = candidates.get(0);
                System.out.println("Relocating file from mod path '" + rel + "' to baseline path '" + targetRel + "'");
                toRemove.add(rel);
                toAdd.put(targetRel, e.getValue());
            }
        }

        for (String r : toRemove) modMap.remove(r);
        for (Map.Entry<String, Path> a : toAdd.entrySet()) modMap.put(a.getKey(), a.getValue());
    }
}