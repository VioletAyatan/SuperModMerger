package ankol.mod.merger;

import ankol.mod.merger.core.ModMergerEngine;
import ankol.mod.merger.tools.ColorPrinter;
import ankol.mod.merger.tools.Localizations;
import ankol.mod.merger.tools.SimpleArgParser;
import ankol.mod.merger.tools.Tools;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Techland模组合并工具 - 主应用入口类
 */
public class AppMain {

    static void main(String[] args) {
        try {
            initCharset(); //初始化控制台字符集为UTF-8
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
            List<Path> modsToMerge = Tools.scanFiles(Tools.getMergingModDir(), ".pak", ".zip");
            // 确定输出路径
            Path outputPath = Path.of(Tools.getUserDir(), "merged_mod.pak");
            if (argParser.hasOption("o")) {
                outputPath = Path.of(argParser.getOptionValue("o"));
            }
            // 定位基准MOD的位置
            Path baseModPath = locateBaseModPath(argParser);
            // 执行合并
            ModMergerEngine merger = new ModMergerEngine(modsToMerge, outputPath, baseModPath);
            merger.merge();
            //完成
            ColorPrinter.success("\n✅ Done!");
            System.exit(0);
        } catch (RuntimeException | IOException e) {
            ColorPrinter.error("错误: " + e.getMessage(), e);
            System.exit(1);
        }
    }

    private static Path locateBaseModPath(SimpleArgParser argParser) {
        Path baseModPath;
        if (argParser.hasOption("b")) {
            baseModPath = Path.of(argParser.getOptionValue("b"));
        } else {
            // 如果没有指定，尝试使用默认位置 source/data0.pak
            Path defaultBaseMod = Path.of(Tools.getUserDir(), "source", "data0.pak");
            if (Files.exists(defaultBaseMod)) {
                baseModPath = defaultBaseMod;
            } else {
                throw new IllegalArgumentException("未找到data0.pak文件，请将此工具放于Dying Light The Beast" + File.separator + "ph_ft目录下，或者使用-b参数指定data0.pak位置");
            }
        }
        return baseModPath;
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
