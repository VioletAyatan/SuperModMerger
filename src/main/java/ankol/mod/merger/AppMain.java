package ankol.mod.merger;

import ankol.mod.merger.core.ModMergerEngine;
import ankol.mod.merger.tools.ColorPrinter;
import ankol.mod.merger.tools.Localizations;
import ankol.mod.merger.tools.SimpleArgParser;
import ankol.mod.merger.tools.Tools;

import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Techland模组合并工具 - 主应用入口类
 */
public class AppMain {

    public static void main(String[] args) {
        try {
            initCharset();
            Localizations.init(); //初始化国际化文件
            //解析命令行参数
            SimpleArgParser argParser = registerArgsParser();
            argParser.parse(args);
            if (argParser.hasOption("h")) {
                //显示帮助信息并退出
                argParser.printHelp();
                System.exit(0);
            }
            // 扫描需要合并的MOD目录
            List<Path> modsToMerge = Tools.scanModFiles(Tools.getMergingModDir());
            // 确定输出路径
            Path outputPath = Path.of(System.getProperty("user.dir"), "merged_mod.pak");
            if (argParser.hasOption("o")) {
                outputPath = Path.of(argParser.getOptionValue("o"));
            }
            // 执行合并
            ModMergerEngine merger = new ModMergerEngine(modsToMerge, outputPath);
            merger.merge();
            //完成
            ColorPrinter.success("\n✅ Done!");
            System.exit(0);
        } catch (IllegalArgumentException e) {
            // 参数错误处理：打印错误信息，退出码1
            ColorPrinter.error("❌ Error: {}", e.getMessage());
            System.exit(1);
        } catch (IOException e) {
            // 文件IO错误处理：打印错误信息和堆栈跟踪，退出码2
            ColorPrinter.error("❌ IO Error: {}", e.getMessage());
            e.printStackTrace();
            System.exit(2);
        } catch (Exception e) {
            // 其他运行时异常处理：打印错误信息和堆栈跟踪，退出码3
            ColorPrinter.error("❌ Error: {}", e.getMessage());
            e.printStackTrace();
            System.exit(3);
        }
    }

    private static SimpleArgParser registerArgsParser() {
        SimpleArgParser argParser = new SimpleArgParser();
        argParser.addOption("o", "output", true, "指定输出 PAK 文件位置 (默认: ./merged_mod.pak)");
        argParser.addOption("b", "base", true, "基准mod所在的位置 (可选)");
        argParser.addOption("h", "help", false, "显示帮助信息");
        return argParser;
    }


    private static void initCharset() {
        try {
            Process p = new ProcessBuilder("cmd", "/c", "chcp", "65001")
                    .inheritIO() // 让子进程输出到相同控制台（可见 chcp 的反馈）
                    .start();
            if (!p.waitFor(2, TimeUnit.SECONDS)) {
                p.destroyForcibly();
            }
            PrintStream psOut = new PrintStream(new FileOutputStream(FileDescriptor.out), true, StandardCharsets.UTF_8);
            PrintStream psErr = new PrintStream(new FileOutputStream(FileDescriptor.err), true, StandardCharsets.UTF_8);
            System.setOut(psOut);
            System.setErr(psErr);
        } catch (Exception e) {
            System.err.println("Error executing command [chcp] Skip!" + e.getMessage());
        }
    }
}
