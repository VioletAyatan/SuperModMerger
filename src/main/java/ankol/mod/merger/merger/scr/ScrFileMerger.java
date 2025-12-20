package ankol.mod.merger.merger.scr;

import ankol.mod.merger.antlr4.scr.TechlandScriptParser;
import ankol.mod.merger.core.MergerContext;
import ankol.mod.merger.merger.MergeResult;
import ankol.mod.merger.merger.scr.ScrConflictResolver.MergeDecision;
import ankol.mod.merger.merger.scr.ScrTreeComparator.DiffResult;
import ankol.mod.merger.core.IFileMerger;
import ankol.mod.merger.tools.FileTree;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.misc.Interval;
import org.antlr.v4.runtime.tree.ParseTree;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * 脚本文件合并器 - 处理 .scr 等脚本文件的智能合并
 */
public class ScrFileMerger extends IFileMerger {

    public ScrFileMerger(MergerContext context) {
        super(context);
    }

    @Override
    public MergeResult merge(FileTree script1, FileTree script2) {
        try {
            ScrScriptParser parser = new ScrScriptParser();
            ScrScriptParser.ParsedScript p1 = parser.parseFileWithTokens(Path.of(script1.getFullPathName()));
            ScrScriptParser.ParsedScript p2 = parser.parseFileWithTokens(Path.of(script2.getFullPathName()));

            TechlandScriptParser.FileContext fileTree1 = p1.file();
            TechlandScriptParser.FileContext fileTree2 = p2.file();

            List<DiffResult> diffs = ScrTreeComparator.compareFiles(fileTree1, fileTree2);

            if (diffs.isEmpty()) {
                // 两个文件完全相同
                return new MergeResult(Files.readString(Path.of(script1.getFullPathName())), false);
            }

            // 有差异，需要用户选择如何合并
            System.out.println("\n" + "=".repeat(80));
            System.out.println("⚠️ CONFLICTS DETECTED IN [" + script1.getFileName() + "]");
            System.out.println("=".repeat(80));

            List<MergeDecision> decisions = ScrConflictResolver.resolveConflicts(diffs, script1, script2);

            MergeResult result = buildMergedContent(Path.of(script1.getFullPathName()), decisions, p1, p2);
            // 记录冲突信息
            result.conflicts.addAll(diffs);
            return result;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private record Replacement(int start, int end, String text) {
    }

    /**
     * 根据用户的决策构建合并后的内容
     */
    private MergeResult buildMergedContent(Path script1Path, List<MergeDecision> decisions,
                                           ScrScriptParser.ParsedScript p1, ScrScriptParser.ParsedScript p2) throws IOException {
        String content1 = Files.readString(script1Path);
        StringBuilder mergedContent = new StringBuilder(content1);
        List<Replacement> replacements = new ArrayList<>();

        // 应用所有用户决策
        for (MergeDecision decision : decisions) {
            DiffResult diff = decision.diff();
            ParseTree tree1 = diff.tree1;
            ParseTree tree2 = diff.tree2;

            if (decision.choice() == ScrConflictResolver.MergeChoice.KEEP_MOD2) {
                if (tree1 != null && tree2 == null) {
                    // 在 Mod2 中被删除
                    Interval interval = tree1.getSourceInterval();
                    int charStart = tokenStartChar(p1.tokens(), interval.a);
                    int charEnd = tokenEndChar(p1.tokens(), interval.b);
                    replacements.add(new Replacement(charStart, charEnd, ""));

                } else if (tree1 == null && tree2 != null) {
                    // 在 Mod2 中被添加
                    int insertPos = findInsertionPoint(mergedContent, diff.lineNumber2, tree2);
                    replacements.add(new Replacement(insertPos, insertPos, "\t" + tree2.getText() + "\n"));

                } else if (tree1 != null && tree2 != null) {
                    // 在 Mod2 中被修改
                    Interval interval = tree1.getSourceInterval();
                    int charStart = tokenStartChar(p1.tokens(), interval.a);
                    int charEnd = tokenEndChar(p1.tokens(), interval.b);
                    replacements.add(new Replacement(charStart, charEnd, tree2.getText()));
                }
            }
            // 如果选择 KEEP_MOD1，则不做任何替换
        }

        // 从后往前应用替换，防止位置偏移
        replacements.sort(Comparator.comparingInt(Replacement::start).reversed());

        for (Replacement rep : replacements) {
            if (rep.start <= mergedContent.length() && rep.end <= mergedContent.length()) {
                mergedContent.replace(rep.start, rep.end, rep.text);
            }
        }

        return new MergeResult(mergedContent.toString(), !decisions.isEmpty());
    }

    /**
     * 获取令牌对应的字符开始位置
     */
    private int tokenStartChar(TokenStream tokens, int tokenIndex) {
        if (tokens == null) return 0;
        try {
            org.antlr.v4.runtime.CommonTokenStream cts = (org.antlr.v4.runtime.CommonTokenStream) tokens;
            if (tokenIndex < 0 || tokenIndex >= cts.getTokens().size()) return 0;
            Token t = cts.getTokens().get(tokenIndex);
            return Math.max(0, t.getStartIndex());
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * 获取令牌对应的字符结束位置
     */
    private int tokenEndChar(TokenStream tokens, int tokenIndex) {
        if (tokens == null) return 0;
        try {
            org.antlr.v4.runtime.CommonTokenStream cts = (org.antlr.v4.runtime.CommonTokenStream) tokens;
            if (tokenIndex < 0 || tokenIndex >= cts.getTokens().size()) return 0;
            Token t = cts.getTokens().get(tokenIndex);
            return Math.max(0, t.getStopIndex() + 1);
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * 寻找合适的插入位置（在相同的函数块或作用域内）
     */
    private int findInsertionPoint(StringBuilder content, int targetLine, ParseTree nodeToInsert) {
        String contentStr = content.toString();
        String[] lines = contentStr.split("\n", -1);

        if (targetLine <= 0 || targetLine > lines.length) {
            // 默认在文件末尾添加
            return contentStr.length();
        }

        // 从目标行向后查找，寻找合适的插入点（通常在函数块的末尾）
        int searchStartLine = Math.min(targetLine, lines.length - 1);
        int braceCount = 0;
        int lineOffset = 0;

        // 先计算搜索开始行的偏移
        for (int i = 0; i < searchStartLine; i++) {
            lineOffset += lines[i].length() + 1; // +1 for newline
        }

        // 从搜索行向后查找合适的插入点
        for (int i = searchStartLine; i < lines.length; i++) {
            String line = lines[i];

            // 计算当前行的括号数
            for (char c : line.toCharArray()) {
                if (c == '{') braceCount++;
                if (c == '}') braceCount--;
            }

            // 在括号级别降低时，这是一个合适的插入点
            if (braceCount < 0) {
                // 找到了结束的右括号，在它之前插入
                int closeBracePos = line.lastIndexOf('}');
                if (closeBracePos >= 0) {
                    return lineOffset + closeBracePos;
                }
            }

            lineOffset += line.length() + 1;
        }

        // 如果没有找到合适的位置，就在文件末尾添加
        return contentStr.length();
    }
}
