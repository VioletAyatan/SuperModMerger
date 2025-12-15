package ankol.mod.merger;

import ankol.mod.merger.ConflictResolver.MergeChoice;
import java.nio.file.Path;
import java.nio.file.Paths;

public class MergeConfig {
    public Path mod1Directory;
    public Path mod2Directory;
    public Path outputDirectory;
    public boolean interactiveMode;
    public MergeChoice defaultMergeStrategy;
    public boolean verbose;

    public MergeConfig() {
        this.interactiveMode = true;
        this.defaultMergeStrategy = MergeChoice.KEEP_MOD1;
        this.verbose = false;
        this.outputDirectory = Paths.get("./merged_mod");
    }

    public static MergeConfig fromArgs(String[] args) throws IllegalArgumentException {
        MergeConfig config = new MergeConfig();

        // 检查帮助标志优先
        for (String arg : args) {
            if ("-h".equals(arg) || "--help".equals(arg)) {
                printHelp();
                System.exit(0);
            }
        }

        if (args.length < 2) {
            throw new IllegalArgumentException(
                "Usage: mod1_dir mod2_dir [output_dir] [options]\n" +
                "Options:\n" +
                "  -o, --output <dir>        Output directory (default: ./merged_mod)\n" +
                "  -a, --auto <strategy>     Auto mode (keep-mod1|keep-mod2|keep-both)\n" +
                "  -v, --verbose             Verbose output\n" +
                "  -h, --help                Show help\n"
            );
        }

        config.mod1Directory = Paths.get(args[0]);
        config.mod2Directory = Paths.get(args[1]);

        for (int i = 2; i < args.length; i++) {
            String arg = args[i];

            switch (arg) {
                case "-o":
                case "--output":
                    if (i + 1 < args.length) {
                        config.outputDirectory = Paths.get(args[++i]);
                    }
                    break;

                case "-a":
                case "--auto":
                    config.interactiveMode = false;
                    if (i + 1 < args.length) {
                        String strategy = args[++i];
                        config.defaultMergeStrategy = parseStrategy(strategy);
                    }
                    break;

                case "-v":
                case "--verbose":
                    config.verbose = true;
                    break;

                case "-h":
                case "--help":
                    // 已在前面处理过
                    break;

                default:
                    if (!arg.startsWith("-") && config.outputDirectory.toString().equals("./merged_mod")) {
                        config.outputDirectory = Paths.get(arg);
                    }
                    break;
            }
        }

        return config;
    }

    private static MergeChoice parseStrategy(String strategy) throws IllegalArgumentException {
        return switch (strategy.toLowerCase()) {
            case "keep-mod1" -> MergeChoice.KEEP_MOD1;
            case "keep-mod2" -> MergeChoice.KEEP_MOD2;
            case "keep-both" -> MergeChoice.KEEP_BOTH;
            default -> throw new IllegalArgumentException("Unknown strategy: " + strategy);
        };
    }

    public void validate() throws IllegalArgumentException {
        if (mod1Directory == null || mod2Directory == null) {
            throw new IllegalArgumentException("Must specify two mod directories");
        }

        if (!java.nio.file.Files.exists(mod1Directory)) {
            throw new IllegalArgumentException("Mod1 directory not found: " + mod1Directory);
        }

        if (!java.nio.file.Files.exists(mod2Directory)) {
            throw new IllegalArgumentException("Mod2 directory not found: " + mod2Directory);
        }
    }

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
            "  -a, --auto <strategy> Auto mode\n" +
            "                        Strategies: keep-mod1, keep-mod2, keep-both\n" +
            "  -v, --verbose         Verbose output\n" +
            "  -h, --help            Show this help\n"
        );
    }

    @Override
    public String toString() {
        return "MergeConfig{" +
                "mod1=" + mod1Directory +
                ", mod2=" + mod2Directory +
                ", output=" + outputDirectory +
                ", interactive=" + interactiveMode +
                ", strategy=" + defaultMergeStrategy +
                ", verbose=" + verbose +
                '}';
    }
}

