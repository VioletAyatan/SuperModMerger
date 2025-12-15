package ankol.mod.merger.core;

import ankol.mod.merger.merger.ScrTreeComparator.DiffResult;

import java.util.*;

/**
 * 冲突解决器 - 处理两个脚本之间的冲突，提示用户选择合并方案
 * <p>
 * 功能：
 * 1. 交互式冲突解决（逐一显示冲突并让用户选择处理方案）
 * 2. 自动冲突解决（使用预定义的策略自动处理所有冲突）
 * 3. 支持5种冲突处理方案
 * 4. 支持手动编辑冲突内容
 * <p>
 * 工作模式：
 * - 交互模式：resolveConflicts() - 为每个冲突提示用户选择
 * - 自动模式：autoResolve() - 自动应用一个统一的策略
 */
public class ConflictResolver {

    /**
     * 合并选择枚举 - 表示处理冲突的5种方案
     * <p>
     * 每种方案的含义：
     * - KEEP_MOD1: 保留模组1的版本，模组2的版本被忽略
     * - KEEP_MOD2: 保留模组2的版本，模组1的版本被覆盖
     * - KEEP_BOTH: 同时保留两个版本，用注释标记来源
     * - SKIP: 暂时跳过，不进行合并（冲突保留原样）
     * - MANUAL: 用户手动输入合并后的内容
     */
    public enum MergeChoice {
        // 选项1：保留模组1
        KEEP_MOD1("Keep Mod1"),
        // 选项2：保留模组2
        KEEP_MOD2("Keep Mod2"),
        // 选项3：保留两个版本
        KEEP_BOTH("Keep Both"),
        // 选项4：跳过合并
        SKIP("Skip"),
        // 选项5：手动编辑
        MANUAL("Manual Edit");

        /**
         * 选项的描述文字，用于显示给用户
         */
        private final String description;

        /**
         * 枚举值的构造函数
         *
         * @param description 该选项的描述
         */
        MergeChoice(String description) {
            this.description = description;
        }

        /**
         * 获取选项的描述文字
         *
         * @return 描述文字
         */
        public String getDescription() {
            return description;
        }
    }

    /**
     * 合并决策内部类 - 表示对一个冲突的合并决策
     * <p>
     * 包含：
     * - 冲突的详细信息（两个版本的代码）
     * - 用户的选择（5个选项中的一个）
     * - 如果用户选择MANUAL，还包含自定义的合并内容
     */
    public static class MergeDecision {
        /**
         * 冲突的详细信息
         */
        public final DiffResult diff;
        /**
         * 用户选择的合并方案
         */
        public final MergeChoice choice;
        /**
         * 用户自定义的合并内容（仅当choice==MANUAL时有效）
         */
        public final String customContent;

        /**
         * 构造函数 - 不含自定义内容
         *
         * @param diff   冲突信息
         * @param choice 合并选择
         */
        public MergeDecision(DiffResult diff, MergeChoice choice) {
            this(diff, choice, null);
        }

        /**
         * 构造函数 - 含自定义内容
         *
         * @param diff          冲突信息
         * @param choice        合并选择
         * @param customContent 自定义合并内容
         */
        public MergeDecision(DiffResult diff, MergeChoice choice, String customContent) {
            this.diff = diff;
            this.choice = choice;
            this.customContent = customContent;
        }
    }

    /**
     * 全局扫描器实例，用于读取用户输入
     */
    private static final Scanner scanner = new Scanner(System.in);

    /**
     * 交互式解决单个冲突
     * <p>
     * 执行流程：
     * 1. 显示冲突头部（冲突编号、规则名称）
     * 2. 显示冲突描述
     * 3. 显示模组1的代码（如果存在）
     * 4. 显示模组2的代码（如果存在）
     * 5. 显示5个选项供用户选择
     * 6. 读取用户输入
     * 7. 根据用户选择返回合并决策
     * 8. 如果用户选择MANUAL，等待用户输入自定义内容
     *
     * @param diff  冲突信息
     * @param index 当前冲突的编号（从1开始）
     * @param total 总冲突数
     * @return 合并决策
     */
    public static MergeDecision resolveConflict(DiffResult diff, int index, int total) {
        // 显示冲突的标题和编号
        System.out.println("\n" + "=".repeat(80));
        System.out.println("Conflict " + index + "/" + total + ": " + diff.ruleName);
        System.out.println("=".repeat(80));

        // 显示冲突的详细描述
        System.out.println("\nDescription: " + diff.description);

        // 显示模组1的版本（如果存在）
        if (diff.tree1 != null) {
            System.out.println("\n[Mod1]");
            // formatCode会对代码进行截断处理，防止输出过长
            System.out.println(formatCode(diff.tree1.getText(), 100));
        }

        // 显示模组2的版本（如果存在）
        if (diff.tree2 != null) {
            System.out.println("\n[Mod2]");
            System.out.println(formatCode(diff.tree2.getText(), 100));
        }

        // 显示选项菜单
        System.out.println("\nSelect merge strategy:");
        int choice = 1;
        for (MergeChoice option : MergeChoice.values()) {
            System.out.println("  " + choice + ". " + option.getDescription());
            choice++;
        }

        // 提示用户输入
        System.out.print("\nEnter choice (1-" + MergeChoice.values().length + "): ");

        try {
            // 读取用户输入并转换为选项索引
            String input = scanner.nextLine().trim();
            int choiceIndex = Integer.parseInt(input) - 1;

            // 验证输入范围
            if (choiceIndex < 0 || choiceIndex >= MergeChoice.values().length) {
                System.out.println("Invalid input, using default: KEEP_MOD1");
                return new MergeDecision(diff, MergeChoice.KEEP_MOD1);
            }

            // 获取用户选择的选项
            MergeChoice selected = MergeChoice.values()[choiceIndex];

            // 如果用户选择手动编辑，读取用户输入的内容
            if (selected == MergeChoice.MANUAL) {
                System.out.print("\nEnter content (end with EOF):\n> ");
                StringBuilder custom = new StringBuilder();
                while (scanner.hasNextLine()) {
                    String line = scanner.nextLine();
                    // 用户输入"EOF"表示输入结束
                    if ("EOF".equals(line.trim())) {
                        break;
                    }
                    custom.append(line).append("\n");
                }
                // 返回决策，包含用户输入的自定义内容
                return new MergeDecision(diff, selected, custom.toString().trim());
            }

            // 返回用户的决策
            return new MergeDecision(diff, selected);

        } catch (NumberFormatException e) {
            // 如果用户输入不是数字，使用默认选项
            System.out.println("Invalid input, using default: KEEP_MOD1");
            return new MergeDecision(diff, MergeChoice.KEEP_MOD1);
        }
    }

    /**
     * 批量交互式解决冲突
     * <p>
     * 执行流程：
     * 1. 如果没有冲突，打印提示信息并返回空列表
     * 2. 显示冲突总数
     * 3. 对每个冲突：
     * a. 调用resolveConflict()让用户选择处理方案
     * b. 将决策存入决策列表
     * c. 如果不是最后一个冲突，等待用户按Enter继续
     * 4. 返回所有决策
     *
     * @param diffs 差异列表
     * @return 合并决策列表，顺序与差异列表对应
     */
    public static List<MergeDecision> resolveConflicts(List<DiffResult> diffs) {
        List<MergeDecision> decisions = new ArrayList<>();

        // 如果没有冲突，直接返回
        if (diffs.isEmpty()) {
            System.out.println("\nNo conflicts found.");
            return decisions;
        }

        // 告知用户有多少个冲突需要解决
        System.out.println("\nFound " + diffs.size() + " conflicts.");

        // 逐一解决每个冲突
        for (int i = 0; i < diffs.size(); i++) {
            DiffResult diff = diffs.get(i);
            // 让用户决策如何处理这个冲突
            MergeDecision decision = resolveConflict(diff, i + 1, diffs.size());
            decisions.add(decision);

            // 如果不是最后一个冲突，等待用户按Enter继续处理下一个
            if (i < diffs.size() - 1) {
                System.out.print("\nPress Enter to continue...");
                scanner.nextLine();
            }
        }

        return decisions;
    }

    /**
     * 自动解决所有冲突（不交互）
     * <p>
     * 执行流程：
     * 1. 为每个冲突创建一个决策
     * 2. 所有决策使用相同的合并策略
     * 3. 返回决策列表
     * <p>
     * 适用场景：
     * - 用户通过 -a 参数指定了自动合并策略
     * - 需要快速处理大量冲突而不需要逐一确认
     *
     * @param diffs         差异列表
     * @param defaultChoice 默认的合并策略（应用于所有冲突）
     * @return 合并决策列表
     */
    public static List<MergeDecision> autoResolve(List<DiffResult> diffs, MergeChoice defaultChoice) {
        List<MergeDecision> decisions = new ArrayList<>();

        // 为每个冲突自动创建决策，都使用相同的策略
        for (DiffResult diff : diffs) {
            decisions.add(new MergeDecision(diff, defaultChoice));
        }

        return decisions;
    }

    /**
     * 格式化代码片段，防止显示过长的代码
     * <p>
     * 用途：
     * - 代码过长时进行截断
     * - 避免终端输出过长导致显示混乱
     *
     * @param code      代码内容
     * @param maxLength 最大显示长度
     * @return 格式化后的代码（超长时会被截断并添加省略号）
     */
    private static String formatCode(String code, int maxLength) {
        if (code.length() > maxLength) {
            // 超长代码：显示前maxLength个字符，然后添加省略号和提示
            return code.substring(0, maxLength) + "...\n[Code truncated]";
        }
        return code;
    }

    /**
     * 关闭扫描器，释放资源
     * <p>
     * 用途：
     * - 释放System.in的资源
     * - 防止资源泄漏
     * <p>
     * 调用时机：
     * - 在AppMain的finally块中调用，确保无论成功或失败都会执行
     */
    public static void close() {
        scanner.close();
    }
}

