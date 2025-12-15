package ankol.mod.merger.core;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 合并配置类 - 管理程序的配置和命令行参数解析
 * <p>
 * 职责：
 * 1. 存储合并操作的所有配置参数
 * 2. 从命令行参数解析配置
 * 3. 验证配置的有效性
 * 4. 显示帮助信息
 * <p>
 * 配置参数：
 * - mod1Directory: 第一个模组目录
 * - mod2Directory: 第二个模组目录
 * - outputDirectory: 合并结果输出目录（默认：./merged_mod）
 * - verbose: 是否显示详细信息（默认：false）
 */
public class SimpleArgumentsParser {
    /**
     * 第一个模组的目录路径
     */
    public Path mod1Directory;
    /**
     * 第二个模组的目录路径
     */
    public Path mod2Directory;
    /**
     * 合并结果的输出目录
     */
    public Path outputDirectory;
    /**
     * 是否启用详细输出
     */
    public boolean verbose;

    /**
     * 默认构造函数 - 设置默认值
     * <p>
     * 默认配置：
     * - 详细模式禁用
     * - 输出目录：./merged_mod
     */
    public SimpleArgumentsParser() {
        this.verbose = false;
        this.outputDirectory = Paths.get("./merged_mod");
    }

    /**
     * 从命令行参数构建配置对象
     * <p>
     * 命令行格式：
     * ModMergerTool <mod1_dir> <mod2_dir> [output_dir] [options]
     * <p>
     * 执行流程：
     * 1. 创建默认配置对象
     * 2. 优先检查帮助标志（-h, --help）
     * 3. 验证必需参数（至少2个）
     * 4. 解析必需参数（mod1目录, mod2目录）
     * 5. 循环解析可选参数
     * 6. 返回配置对象
     * <p>
     * 支持的参数：
     * -o, --output <dir>  - 指定输出目录
     * -v, --verbose       - 启用详细输出
     * -h, --help          - 显示帮助信息并退出
     *
     * @param args 命令行参数数组
     * @return 解析完成的配置对象
     * @throws IllegalArgumentException 如果参数无效
     */
    public static SimpleArgumentsParser fromArgs(String[] args) throws IllegalArgumentException {
        // 创建默认配置对象
        SimpleArgumentsParser config = new SimpleArgumentsParser();

        // 第1步：优先检查帮助标志
        // 如果找到 -h 或 --help，显示帮助信息并退出
        for (String arg : args) {
            if ("-h".equals(arg) || "--help".equals(arg)) {
                printHelp();
                System.exit(0);
            }
        }

        // 第2步：验证必需参数个数
        // 至少需要两个参数（mod1_dir 和 mod2_dir）
        if (args.length < 2) {
            throw new IllegalArgumentException(
                    "Usage: mod1_dir mod2_dir [output_dir] [options]\n" +
                            "Options:\n" +
                            "  -o, --output <dir>        Output directory (default: ./merged_mod)\n" +
                            "  -v, --verbose             Verbose output\n" +
                            "  -h, --help                Show help\n"
            );
        }

        // 第3步：解析必需参数
        config.mod1Directory = Paths.get(args[0]);
        config.mod2Directory = Paths.get(args[1]);

        // 第4步：循环解析可选参数（从第三个参数开始）
        for (int i = 2; i < args.length; i++) {
            String arg = args[i];

            switch (arg) {
                case "-o":
                case "--output":
                    // 输出目录选项，下一个参数是目录路径
                    if (i + 1 < args.length) {
                        config.outputDirectory = Paths.get(args[++i]);
                    }
                    break;
                case "-v":
                case "--verbose":
                    // 详细模式选项，无需参数
                    config.verbose = true;
                    break;
                case "-h":
                case "--help":
                    // 帮助选项，已在前面优先处理过
                    break;
                default:
                    // 如果是未被识别的参数且不以 "-" 开头，
                    // 且输出目录还是默认值，则将其作为输出目录
                    if (!arg.startsWith("-") && config.outputDirectory.toString().equals("./merged_mod")) {
                        config.outputDirectory = Paths.get(arg);
                    }
                    break;
            }
        }

        return config;
    }

    /**
     * 验证配置的有效性
     * <p>
     * 检查内容：
     * 1. mod1Directory 和 mod2Directory 不为null
     * 2. mod1Directory 指向的目录存在
     * 3. mod2Directory 指向的目录存在
     *
     * @throws IllegalArgumentException 如果配置无效
     */
    public void validate() throws IllegalArgumentException {
        // 检查两个必需目录是否都已指定
        if (mod1Directory == null || mod2Directory == null) {
            throw new IllegalArgumentException("Must specify two mod directories");
        }
        // 检查模组1目录是否存在
        if (!Files.exists(mod1Directory)) {
            throw new IllegalArgumentException("Mod1 directory not found: " + mod1Directory);
        }
        // 检查模组2目录是否存在
        if (!Files.exists(mod2Directory)) {
            throw new IllegalArgumentException("Mod2 directory not found: " + mod2Directory);
        }
    }

    /**
     * 打印帮助信息到标准输出
     * <p>
     * 显示内容：
     * - 工具的名称和版本
     * - 使用方法
     * - 必需参数说明
     * - 可选参数说明
     */
    private static void printHelp() {
        System.out.println(
                "Techland Mod Merger v1.0\n" +
                        "\nUsage:\n" +
                        "  java -jar ModMergerTool.jar <mod1_dir> <mod2_dir> [options]\n" +
                        "\nRequired:\n" +
                        "  mod1_dir              First mod directory\n" +
                        "  mod2_dir              Second mod directory\n" +
                        "\nOptions:\n" +
                        "  -o, --output <dir>    Output directory (default: ./merged_mod)\n" +
                        "  -v, --verbose         Verbose output\n" +
                        "  -h, --help            Show this help\n"
        );
    }

    /**
     * 返回配置的字符串表示，用于调试和显示
     * <p>
     * 格式：MergeConfig{field1=value1, field2=value2, ...}
     *
     * @return 配置的字符串表示
     */
    @Override
    public String toString() {
        return "MergeConfig{" +
                "mod1=" + mod1Directory +
                ", mod2=" + mod2Directory +
                ", output=" + outputDirectory +
                ", verbose=" + verbose +
                '}';
    }
}
