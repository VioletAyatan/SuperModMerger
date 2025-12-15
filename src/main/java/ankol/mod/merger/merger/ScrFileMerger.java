package ankol.mod.merger.merger;

import ankol.mod.merger.antlr4.scr.TechlandScriptParser;
import ankol.mod.merger.merger.ScrConflictResolver.MergeDecision;
import ankol.mod.merger.merger.ScrTreeComparator.DiffResult;
import org.antlr.v4.runtime.misc.Interval;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ScrFileMerger implements IFileMerger {

    @Override
    @SuppressWarnings("all")
    public MergeResult merge(Path script1, Path script2) throws IOException {
        // 1. 使用Antlr4将文件内容解析为AST语法树
        ScrScriptParser script1Parser = new ScrScriptParser();
        ScrScriptParser script2Parser = new ScrScriptParser();
        TechlandScriptParser.FileContext fileTree1 = script1Parser.parseFile(script1);
        TechlandScriptParser.FileContext fileTree2 = script2Parser.parseFile(script2);

        // 2. 对比两个AST，获取差异列表
        List<DiffResult> diffs = ScrTreeComparator.compareFiles(fileTree1, fileTree2, script1Parser, script2Parser);

        MergeResult result = new MergeResult();

        // 3. 如果没有差异，直接返回模组1的内容
        if (diffs.isEmpty()) {
            result.mergedContent = Files.readString(script1);
            result.hasConflicts = false;
            return result;
        }

        // 4. 检测到冲突，进入交互模式让用户决策
        System.out.println("\n" + "=".repeat(80));
        System.out.println("⚠️ CONFLICTS DETECTED IN [" + script1.getFileName() + "] - User Interaction Required");
        System.out.println("=".repeat(80));
        List<MergeDecision> decisions = ScrConflictResolver.resolveConflicts(diffs);

        // 5. 根据决策构建合并后的内容
        result = buildMergedContent(script1, decisions);
        return result;
    }

    /**
     * 一个简单的记录类，用于表示对源文件的文本替换操作。
     *
     * @param start 起始索引（包含）
     * @param end   结束索引（不包含）
     * @param text  要替换成的新文本
     */
    private record Replacement(int start, int end, String text) {
    }

    /**
     * 根据用户的冲突决策来构建最终合并后的文件内容。
     * <p>
     * <b>修复策略：</b>
     * 此方法修复了一个关键错误：之前使用 `String.indexOf` 来定位和替换文本，当文件中存在重复内容时，
     * 这种方法会错误地替换第一个匹配项，而不是冲突发生的实际位置。
     * <p>
     * <b>新实现流程：</b>
     * <ol>
     *   <li><b>收集变更：</b>遍历所有用户决策。对于每个决定保留Mod2的冲突，我们创建一个`Replacement`对象。
     *       这个对象精确地记录了要替换文本在Mod1中的起始和结束位置（使用ANTLR的词法分析器提供的偏移量），以及来自Mod2的新文本。
     *       对于仅在Mod2中存在的新增内容，我们将其收集起来，准备追加到文件末尾。</li>
     *   <li><b>反向排序：</b>对所有`Replacement`对象列表进行<b>反向排序</b>（从文件末尾到文件开头）。
     *       这是至关重要的步骤，因为从后往前应用变更可以确保每次替换操作不会影响到后续（即文件位置更靠前）操作的索引准确性。</li>
     *   <li><b>应用变更：</b>基于Mod1的原始内容，从后往前执行所有替换操作。</li>
     *   <li><b>追加内容：</b>将所有仅在Mod2中存在的新增内容追加到合并后内容的末尾。</li>
     *   <li><b>返回结果：</b>封装合并后的内容和冲突信息并返回。</li>
     * </ol>
     *
     * @param script1Path Mod1的脚本文件路径（作为合并基础）。
     * @param decisions   用户对所有冲突做出的决策列表。
     * @return 一个 {@link MergeResult} 对象，包含合并后的内容和冲突详情。
     * @throws IOException 如果读取文件时发生错误。
     */
    private MergeResult buildMergedContent(Path script1Path, List<MergeDecision> decisions) throws IOException {
        MergeResult result = new MergeResult();
        String content1 = Files.readString(script1Path);
        StringBuilder mergedContent = new StringBuilder(content1);

        List<Replacement> replacements = new ArrayList<>();
        StringBuilder additions = new StringBuilder();

        // 1. 收集所有变更和新增内容
        for (MergeDecision decision : decisions) {
            result.conflicts.add(decision.diff()); // 记录为已处理的冲突

            if (decision.choice() == ScrConflictResolver.MergeChoice.KEEP_MOD2) {
                DiffResult diff = decision.diff();
                if (diff.tree1 != null && diff.tree2 != null) {
                    // 这是修改操作：Mod1和Mod2都有内容
                    Interval interval = diff.tree1.getSourceInterval();
                    replacements.add(new Replacement(interval.a, interval.b + 1, diff.tree2.getText()));
                } else if (diff.tree2 != null) {
                    // 这是新增操作：内容只在Mod2中存在
                    additions.append("\n\n").append(diff.tree2.getText());
                }
            }
            // 如果是 KEEP_MOD1，则无需任何操作
        }

        // 2. 将替换操作按起始位置从大到小排序（从后往前）
        replacements.sort(Comparator.comparingInt(Replacement::start).reversed());

        // 3. 从后往前应用所有替换操作，避免索引失效
        for (Replacement rep : replacements) {
            mergedContent.replace(rep.start, rep.end, rep.text);
        }

        // 4. 在末尾追加所有新增内容
        if (!additions.isEmpty()) {
            mergedContent.append(additions);
        }

        result.mergedContent = mergedContent.toString();
        result.hasConflicts = !result.conflicts.isEmpty();

        return result;
    }
}