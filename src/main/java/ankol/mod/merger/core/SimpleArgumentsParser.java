package ankol.mod.merger.core;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 合并配置类 - 管理程序的配置和命令行参数解析
 */
public class SimpleArgumentsParser {
    /**
     * 第一个模组的目录路径（baseline）
     */
    public Path mod1Directory;
    /**
     * 第二个模组的目录路径（可选）
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
     * 需要合并的MOD目录所在文件夹
     */
    public Path meringModDir;

    public SimpleArgumentsParser() {
        this.verbose = false;
        this.outputDirectory = Paths.get("./merged_mod");
    }

    /**
     * 从命令行参数构建配置对象
     * 支持： baseline + [mod1 mod2 ...] 以及可选 -o, -v
     */
    public static SimpleArgumentsParser fromArgs(String[] args) throws IllegalArgumentException {
        SimpleArgumentsParser config = new SimpleArgumentsParser();

        for (String arg : args) {
            if ("-h".equals(arg) || "--help".equals(arg)) {
                printHelp();
                System.exit(0);
            }
        }

        // 现在至少需要一个参数：baseline
        if (args.length < 1) {
            throw new IllegalArgumentException(
                    "Usage: baseline_dir [mod_dir ...] [output_dir] [options]\n" +
                            "Options:\n" +
                            "  -o, --output <dir>        Output directory (default: ./merged_mod)\n" +
                            "  -v, --verbose             Verbose output\n" +
                            "  -h, --help                Show help\n"
            );
        }

        // baseline
        config.mod1Directory = Paths.get(args[0]);
        // If there is a second non-option arg, set mod2Directory (optional)
        if (args.length >= 2 && !args[1].startsWith("-")) {
            config.mod2Directory = Paths.get(args[1]);
        }

        // parse options from remaining args
        for (int i = 1; i < args.length; i++) {
            String arg = args[i];

            switch (arg) {
                case "-o":
                case "--output":
                    if (i + 1 < args.length) {
                        config.outputDirectory = Paths.get(args[++i]);
                    }
                    break;
                case "-v":
                case "--verbose":
                    config.verbose = true;
                    break;
                case "-h":
                case "--help":
                    break;
                default:
                    if (!arg.startsWith("-") && config.outputDirectory.toString().equals("./merged_mod")) {
                        // If this arg is not an option and output still default, treat it as output dir
                        // (backwards compatibility)
                        config.outputDirectory = Paths.get(arg);
                    }
                    break;
            }
        }

        return config;
    }

    /**
     * 验证配置的有效性
     */
    public void validate() throws IllegalArgumentException {
        if (mod1Directory == null) {
            throw new IllegalArgumentException("Must specify at least a baseline directory");
        }
        if (!Files.exists(mod1Directory)) {
            throw new IllegalArgumentException("Baseline directory not found: " + mod1Directory);
        }
        if (mod2Directory != null && !Files.exists(mod2Directory)) {
            throw new IllegalArgumentException("Mod directory not found: " + mod2Directory);
        }
    }

    private static void printHelp() {
        System.out.println(
                "Techland Mod Merger v1.0\n" +
                        "\nUsage:\n" +
                        "  java -jar ModMergerTool.jar <baseline_dir> [mod_dir ...] [options]\n" +
                        "\nRequired:\n" +
                        "  baseline_dir           Base game directory to use as reference\n" +
                        "\nOptions:\n" +
                        "  -o, --output <dir>    Output directory (default: ./merged_mod)\n" +
                        "  -v, --verbose         Verbose output\n" +
                        "  -h, --help            Show this help\n"
        );
    }

    @Override
    public String toString() {
        return "MergeConfig{" +
                "baseline=" + mod1Directory +
                ", mod2=" + mod2Directory +
                ", output=" + outputDirectory +
                ", verbose=" + verbose +
                '}';
    }
}
