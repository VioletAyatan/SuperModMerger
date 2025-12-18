package ankol.mod.merger.tools;

import lombok.Data;

import java.util.*;

/**
 * 简单的命令行参数解析工具
 *
 * @author Ankol
 */
public class SimpleArgParser {
    // --- 定义存储结构 ---
    private final Map<String, Option> options = new LinkedHashMap<>();
    // 关键映射：短名 -> 长名 (例如 "p" -> "port")
    private final Map<String, String> shortToLongMap = new HashMap<>();
    // 解析结果 (统一使用长名作为 Key)
    private final Map<String, String> parsedValues = new HashMap<>();
    private final Set<String> parsedFlags = new HashSet<>();
    private final List<String> positionalArgs = new ArrayList<>();

    /**
     * 添加命令行配置项
     *
     * @param shortName   短名称（-x）
     * @param longName    长名称（--xxx）
     * @param hasValue    是否需要值（支持不带值的命令行参数）
     * @param description 命令行描述
     */
    public void addOption(String shortName, String longName, boolean hasValue, String description) {
        Option option = new Option(shortName, longName, hasValue, description);
        options.put(longName, option);
        //添加到长短名映射中
        if (shortName != null && !shortName.isEmpty()) {
            shortToLongMap.put(shortName, longName);
        }
    }

    /**
     * 解析命令行参数
     *
     * @param args 命令行参数数组
     */
    public void parse(String[] args) {
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if (arg.startsWith("--")) {
                String rawName = arg.substring(2);
                String value = null;
                if (rawName.contains("=")) {
                    String[] parts = rawName.split("=", 2);
                    rawName = parts[0];
                    value = parts[1];
                }
                if (options.containsKey(rawName)) {
                    Option opt = options.get(rawName);
                    if (opt.isHasValue()) {
                        if (value == null && i + 1 < args.length && !args[i + 1].startsWith("-")) {
                            value = args[++i];
                        }
                        parsedValues.put(rawName, value); // 存入长名
                    } else {
                        parsedFlags.add(rawName); // 存入长名
                    }
                }
            } else if (arg.startsWith("-")) {
                String shortName = arg.substring(1);
                if (shortToLongMap.containsKey(shortName)) {
                    String longName = shortToLongMap.get(shortName);
                    Option opt = options.get(longName);
                    if (opt.isHasValue()) {
                        if (i + 1 < args.length && !args[i + 1].startsWith("-")) {
                            parsedValues.put(longName, args[++i]); // 映射为长名存入
                        }
                    } else {
                        parsedFlags.add(longName); // 映射为长名存入
                    }
                } else {
                    // 未知短参，视为位置参数或报错
                    positionalArgs.add(arg);
                }
            } else {
                // 位置参数
                positionalArgs.add(arg);
            }
        }
    }

    /**
     * 将传入的 key (可能是短名，可能是长名) 统一转换为长名
     *
     * @param key key
     * @return 返回转换后的长名
     */
    public String resolveKey(String key) {
        // 如果这个 key 是已知的短名，返回对应的长名
        if (shortToLongMap.containsKey(key)) {
            return shortToLongMap.get(key);
        }
        // 否则假设它是长名
        return key;
    }

    /**
     * 判断是否存在对应的选项
     *
     * @param key key (支持 -v 或 --verbose)
     * @return 对应选项是否存在
     */
    public boolean hasOption(String key) {
        String longKey = resolveKey(key);
        return parsedFlags.contains(longKey) || parsedValues.containsKey(longKey);
    }

    /**
     * 获取选项值
     *
     * @param key (支持 -p 或 --port)
     */
    public String getOptionValue(String key) {
        return parsedValues.get(resolveKey(key));
    }

    /**
     * 获取选项值，带默认值
     *
     * @param key          支持 -p 或 --port
     * @param defaultValue 默认值
     */
    public String getOptionValue(String key, String defaultValue) {
        return parsedValues.getOrDefault(resolveKey(key), defaultValue);
    }

    /**
     * 获取位置参数
     *
     * @return 位置参数列表
     */
    public List<String> getPositionalArgs() {
        return positionalArgs;
    }

    /**
     * 打印帮助信息
     */
    public void printHelp() {
        System.out.println("Usage:");
        for (Option opt : options.values()) {
            String sName = (opt.shortName != null) ? "-" + opt.shortName : "  ";
            String lName = "--" + opt.longName;
            String valParams = opt.hasValue ? " <value>" : "";
            System.out.printf("  %s, %-20s %s%n", sName, lName + valParams, opt.description);
        }
    }

    @Data
    public static class Option {
        private String shortName;
        private String longName;
        private boolean hasValue;
        private String description;

        /**
         * 命令行指令构造函数
         *
         * @param shortName   短名称（-x）
         * @param longName    长名称（--xxx）
         * @param hasValue    是否需要值（支持不带值的命令行参数）
         * @param description 命令行描述
         */
        public Option(String shortName, String longName, boolean hasValue, String description) {
            this.shortName = shortName;
            this.longName = longName;
            this.hasValue = hasValue;
            this.description = description;
        }
    }
}
