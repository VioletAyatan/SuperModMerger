package ankol.mod.merger;

import ankol.mod.merger.TreeComparator.DiffResult;

import java.util.*;

public class ConflictResolver {

    public enum MergeChoice {
        KEEP_MOD1("Keep Mod1"),
        KEEP_MOD2("Keep Mod2"),
        KEEP_BOTH("Keep Both"),
        SKIP("Skip"),
        MANUAL("Manual Edit");

        private final String description;

        MergeChoice(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    public static class MergeDecision {
        public final DiffResult diff;
        public final MergeChoice choice;
        public final String customContent;

        public MergeDecision(DiffResult diff, MergeChoice choice) {
            this(diff, choice, null);
        }

        public MergeDecision(DiffResult diff, MergeChoice choice, String customContent) {
            this.diff = diff;
            this.choice = choice;
            this.customContent = customContent;
        }
    }

    private static final Scanner scanner = new Scanner(System.in);

    public static MergeDecision resolveConflict(DiffResult diff, int index, int total) {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("Conflict " + index + "/" + total + ": " + diff.ruleName);
        System.out.println("=".repeat(80));
        System.out.println("\nDescription: " + diff.description);

        if (diff.tree1 != null) {
            System.out.println("\n[Mod1]");
            System.out.println(formatCode(diff.tree1.getText(), 100));
        }

        if (diff.tree2 != null) {
            System.out.println("\n[Mod2]");
            System.out.println(formatCode(diff.tree2.getText(), 100));
        }

        System.out.println("\nSelect merge strategy:");
        int choice = 1;
        for (MergeChoice option : MergeChoice.values()) {
            System.out.println("  " + choice + ". " + option.getDescription());
            choice++;
        }

        System.out.print("\nEnter choice (1-" + MergeChoice.values().length + "): ");

        try {
            String input = scanner.nextLine().trim();
            int choiceIndex = Integer.parseInt(input) - 1;

            if (choiceIndex < 0 || choiceIndex >= MergeChoice.values().length) {
                System.out.println("Invalid input, using default: KEEP_MOD1");
                return new MergeDecision(diff, MergeChoice.KEEP_MOD1);
            }

            MergeChoice selected = MergeChoice.values()[choiceIndex];

            if (selected == MergeChoice.MANUAL) {
                System.out.print("\nEnter content (end with EOF):\n> ");
                StringBuilder custom = new StringBuilder();
                while (scanner.hasNextLine()) {
                    String line = scanner.nextLine();
                    if ("EOF".equals(line.trim())) {
                        break;
                    }
                    custom.append(line).append("\n");
                }
                return new MergeDecision(diff, selected, custom.toString().trim());
            }

            return new MergeDecision(diff, selected);

        } catch (NumberFormatException e) {
            System.out.println("Invalid input, using default: KEEP_MOD1");
            return new MergeDecision(diff, MergeChoice.KEEP_MOD1);
        }
    }

    public static List<MergeDecision> resolveConflicts(List<DiffResult> diffs) {
        List<MergeDecision> decisions = new ArrayList<>();

        if (diffs.isEmpty()) {
            System.out.println("\nNo conflicts found.");
            return decisions;
        }

        System.out.println("\nFound " + diffs.size() + " conflicts.");

        for (int i = 0; i < diffs.size(); i++) {
            DiffResult diff = diffs.get(i);
            MergeDecision decision = resolveConflict(diff, i + 1, diffs.size());
            decisions.add(decision);

            if (i < diffs.size() - 1) {
                System.out.print("\nPress Enter to continue...");
                scanner.nextLine();
            }
        }

        return decisions;
    }

    public static List<MergeDecision> autoResolve(List<DiffResult> diffs, MergeChoice defaultChoice) {
        List<MergeDecision> decisions = new ArrayList<>();

        for (DiffResult diff : diffs) {
            decisions.add(new MergeDecision(diff, defaultChoice));
        }

        return decisions;
    }

    private static String formatCode(String code, int maxLength) {
        if (code.length() > maxLength) {
            return code.substring(0, maxLength) + "...\n[Code truncated]";
        }
        return code;
    }

    public static void close() {
        if (scanner != null) {
            scanner.close();
        }
    }
}

