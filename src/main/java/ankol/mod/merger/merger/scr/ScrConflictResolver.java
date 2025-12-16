package ankol.mod.merger.merger.scr;

import ankol.mod.merger.merger.scr.ScrTreeComparator.DiffResult;
import ankol.mod.merger.tools.FileTree;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * 冲突解决器 - 处理两个脚本之间的冲突，提示用户选择合并方案
 */
public class ScrConflictResolver {

    public enum MergeChoice {
        KEEP_MOD1("Keep Mod1"),
        KEEP_MOD2("Keep Mod2");

        private final String description;
        MergeChoice(String description) { this.description = description; }
        public String getDescription() { return description; }
    }

    public record MergeDecision(DiffResult diff, MergeChoice choice) {}

    private static final Scanner scanner = new Scanner(System.in);

    /**
     * 交互式解决单个冲突，并高亮显示第一个差异点。
     */
    public static MergeDecision resolveConflict(DiffResult diff, int index, int total) {
        // 保持向后兼容的原有实现（不带文件路径）
        System.out.println("\n" + "=".repeat(80));
        System.out.println("Conflict " + index + "/" + total + ": " + diff.description);
        System.out.println("=".repeat(80));

        String text1 = diff.tree1 != null ? diff.tree1.getText() : "";
        String text2 = diff.tree2 != null ? diff.tree2.getText() : "";

        System.out.println("\n--- Mod1 (Line " + diff.lineNumber1 + ") ---");
        System.out.println(highlightDifference(text1, text2));

        System.out.println("\n--- Mod2 (Line " + diff.lineNumber2 + ") ---");
        System.out.println(highlightDifference(text2, text1));

        System.out.println("\nSelect merge strategy:");
        int choiceNum = 1;
        for (MergeChoice option : MergeChoice.values()) {
            System.out.println("  " + choiceNum++ + ". " + option.getDescription());
        }

        System.out.print("\nEnter choice (1-" + MergeChoice.values().length + "): ");
        try {
            String input = scanner.nextLine().trim();
            int choiceIndex = Integer.parseInt(input) - 1;
            if (choiceIndex >= 0 && choiceIndex < MergeChoice.values().length) {
                return new MergeDecision(diff, MergeChoice.values()[choiceIndex]);
            }
        } catch (NumberFormatException e) {
            // 忽略无效输入
        }
        System.out.println("Invalid input, using default: KEEP_MOD1");
        return new MergeDecision(diff, MergeChoice.KEEP_MOD1);
    }

    /**
     * 新的 resolveConflict：带上文件路径，会打印出冲突发生的具体文件和行内容
     */
    public static MergeDecision resolveConflict(DiffResult diff, int index, int total, Path file1, Path file2) {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("Conflict " + index + "/" + total + ": " + diff.description);
        System.out.println("=".repeat(80));

        String line1 = "[line not available]";
        String line2 = "[line not available]";
        try {
            if (file1 != null && diff.lineNumber1 > 0) {
                List<String> lines = Files.readAllLines(file1);
                if (diff.lineNumber1 <= lines.size()) line1 = lines.get(diff.lineNumber1 - 1).trim();
            }
        } catch (IOException ignored) {}
        try {
            if (file2 != null && diff.lineNumber2 > 0) {
                List<String> lines = Files.readAllLines(file2);
                if (diff.lineNumber2 <= lines.size()) line2 = lines.get(diff.lineNumber2 - 1).trim();
            }
        } catch (IOException ignored) {}

        System.out.println("\n--- File: " + (file1 != null ? file1.toString() : "(unknown)") + " (Line " + diff.lineNumber1 + ") ---");
        System.out.println(line1);

        System.out.println("\n--- File: " + (file2 != null ? file2.toString() : "(unknown)") + " (Line " + diff.lineNumber2 + ") ---");
        System.out.println(line2);

        // 保留高亮差异视图（基于 parse tree 截取的文本）
        String text1 = diff.tree1 != null ? diff.tree1.getText() : "";
        String text2 = diff.tree2 != null ? diff.tree2.getText() : "";
        System.out.println("\nContext (Mod1 snippet):");
        System.out.println(highlightDifference(text1, text2));
        System.out.println("\nContext (Mod2 snippet):");
        System.out.println(highlightDifference(text2, text1));

        System.out.println("\nSelect merge strategy:");
        int choiceNum = 1;
        for (MergeChoice option : MergeChoice.values()) {
            System.out.println("  " + choiceNum++ + ". " + option.getDescription());
        }

        System.out.print("\nEnter choice (1-" + MergeChoice.values().length + "): ");
        try {
            String input = scanner.nextLine().trim();
            int choiceIndex = Integer.parseInt(input) - 1;
            if (choiceIndex >= 0 && choiceIndex < MergeChoice.values().length) {
                return new MergeDecision(diff, MergeChoice.values()[choiceIndex]);
            }
        } catch (NumberFormatException e) {
            // ignore
        }
        System.out.println("Invalid input, using default: KEEP_MOD1");
        return new MergeDecision(diff, MergeChoice.KEEP_MOD1);
    }

    /**
     * 比较两个文本，并高亮显示第一个差异点附近的上下文。
     */
    private static String highlightDifference(String textA, String textB) {
        if (textA == null || textA.isEmpty()) return "[EMPTY]";

        int diffIndex = -1;
        int lenA = textA.length();
        int lenB = textB != null ? textB.length() : 0;
        int maxLen = Math.min(lenA, lenB);

        for (int i = 0; i < maxLen; i++) {
            if (textA.charAt(i) != textB.charAt(i)) {
                diffIndex = i;
                break;
            }
        }

        if (diffIndex == -1 && lenA != lenB) {
            diffIndex = maxLen;
        }

        if (diffIndex != -1) {
            int context = 60; // show more context
            int start = Math.max(0, diffIndex - context);
            int end = Math.min(lenA, diffIndex + context);

            String prefix = start > 0 ? "..." : "";
            String suffix = end < lenA ? "..." : "";

            StringBuilder sb = new StringBuilder();
            sb.append(prefix);
            sb.append(textA.substring(start, diffIndex));
            sb.append("\u001B[31m").append(textA.charAt(diffIndex)).append("\u001B[0m");
            if (diffIndex + 1 < end) sb.append(textA.substring(diffIndex + 1, end));
            sb.append(suffix);
            return sb.toString();
        }

        return textA;
    }

    public static List<MergeDecision> resolveConflicts(List<DiffResult> diffs) {
        // backward-compatible: call file-aware overload with null paths
        return resolveConflicts(diffs, null, null);
    }

    /**
     * Resolve conflicts with knowledge of the two file paths so we can show exact file/line info.
     */
    public static List<MergeDecision> resolveConflicts(List<DiffResult> diffs, FileTree file1, FileTree file2) {
        List<MergeDecision> decisions = new ArrayList<>();
        if (diffs.isEmpty()) {
            System.out.println("\nNo conflicts found.");
            return decisions;
        }
        System.out.println("\nFound " + diffs.size() + " conflicts.");
        for (int i = 0; i < diffs.size(); i++) {
            decisions.add(resolveConflict(diffs.get(i), i + 1, diffs.size(), file1, file2));
            if (i < diffs.size() - 1) {
                System.out.print("\nPress Enter to continue...");
                scanner.nextLine();
            }
        }
        return decisions;
    }

    public static void close() {
        scanner.close();
    }
}
