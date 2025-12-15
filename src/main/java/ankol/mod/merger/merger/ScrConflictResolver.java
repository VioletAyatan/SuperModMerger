package ankol.mod.merger.merger;

import ankol.mod.merger.merger.ScrTreeComparator.DiffResult;

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
        System.out.println("\n" + "=".repeat(80));
        System.out.println("Conflict " + index + "/" + total + ": " + diff.description);
        System.out.println("=".repeat(80));

        String text1 = diff.tree1 != null ? diff.tree1.getText() : "";
        String text2 = diff.tree2 != null ? diff.tree2.getText() : "";

        // 显示带有行号和高亮差异的文本
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
     * 比较两个文本，并高亮显示第一个差异点附近的上下文。
     *
     * @param textA 第一个文本
     * @param textB 第二个文本
     * @return 高亮处理后的文本A的片段
     */
    private static String highlightDifference(String textA, String textB) {
        if (textA.isEmpty()) return "[EMPTY]";
        
        int diffIndex = -1;
        int lenA = textA.length();
        int lenB = textB.length();
        int maxLen = Math.min(lenA, lenB);

        for (int i = 0; i < maxLen; i++) {
            if (textA.charAt(i) != textB.charAt(i)) {
                diffIndex = i;
                break;
            }
        }

        // 如果没有找到差异，但长度不同，则差异点在较短文本的末尾
        if (diffIndex == -1 && lenA != lenB) {
            diffIndex = maxLen;
        }

        if (diffIndex != -1) {
            int context = 30; // 上下文范围
            int start = Math.max(0, diffIndex - context);
            int end = Math.min(lenA, diffIndex + context);

            String prefix = start > 0 ? "..." : "";
            String suffix = end < lenA ? "..." : "";
            
            // 使用 ANSI 转义码高亮差异点
            String highlighted = "\u001B[31m" + textA.charAt(diffIndex) + "\u001B[0m";

            return prefix + 
                   textA.substring(start, diffIndex) + 
                   highlighted + 
                   textA.substring(diffIndex + 1, end) + 
                   suffix;
        }

        return textA; // 没有差异
    }

    public static List<MergeDecision> resolveConflicts(List<DiffResult> diffs) {
        List<MergeDecision> decisions = new ArrayList<>();
        if (diffs.isEmpty()) {
            System.out.println("\nNo conflicts found.");
            return decisions;
        }
        System.out.println("\nFound " + diffs.size() + " conflicts.");
        for (int i = 0; i < diffs.size(); i++) {
            decisions.add(resolveConflict(diffs.get(i), i + 1, diffs.size()));
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
