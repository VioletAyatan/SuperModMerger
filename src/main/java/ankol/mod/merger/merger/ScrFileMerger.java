package ankol.mod.merger.merger;

import ankol.mod.merger.antlr4.scr.TechlandScriptParser;
import ankol.mod.merger.merger.ScrConflictResolver.MergeChoice;
import ankol.mod.merger.merger.ScrConflictResolver.MergeDecision;
import ankol.mod.merger.merger.ScrTreeComparator.DiffResult;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class ScrFileMerger implements IFileMerger {

    @Override
    @SuppressWarnings("all")
    public MergeResult merge(Path script1, Path script2) throws IOException {
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
}