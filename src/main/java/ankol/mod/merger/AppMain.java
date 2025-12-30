package ankol.mod.merger;

import ankol.mod.merger.core.ModMergerEngine;
import ankol.mod.merger.exception.BusinessException;
import ankol.mod.merger.tools.ColorPrinter;
import ankol.mod.merger.tools.Localizations;
import ankol.mod.merger.tools.SimpleArgParser;
import ankol.mod.merger.tools.Tools;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

/**
 * Techland模组合并工具 - 主应用入口类
 */
@Slf4j
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
            List<Path> modsToMerge = Tools.scanFiles(Tools.getMergingModDir(), ".pak", ".zip", ".7z");
            // 确定输出路径
            Path outputPath = Path.of(Tools.getUserDir(), "source", "data7.pak");
            if (argParser.hasOption("o")) {
                outputPath = Path.of(argParser.getOptionValue("o"));
            }
            // 定位基准MOD的位置
            Path baseModPath = locateBaseModPath(argParser);
            // 执行合并
            ModMergerEngine merger = new ModMergerEngine(modsToMerge, outputPath, baseModPath);
            merger.merge();
            ColorPrinter.success(Localizations.t("APP_MAIN_DONE"));
            parseAnyKeyToExit();
            System.exit(0);
        } catch (BusinessException e) {
            ColorPrinter.error(Localizations.t("APP_MAIN_ERROR", e.getMessage()));
            parseAnyKeyToExit();
            System.exit(1);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            parseAnyKeyToExit();
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
                throw new BusinessException(Localizations.t("APP_MAIN_BASE_MOD_NOT_FOUND", File.separator));
            }
        }
        return baseModPath;
    }

    private static SimpleArgParser registerArgsParser() {
        SimpleArgParser argParser = new SimpleArgParser();
        argParser.addOption("o", "output", true, Localizations.t("APP_MAIN_OPTION_OUTPUT_DESC"));
        argParser.addOption("b", "base", true, Localizations.t("APP_MAIN_OPTION_BASE_DESC"));
        argParser.addOption("h", "help", false, Localizations.t("APP_MAIN_OPTION_HELP_DESC"));
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

    private static void parseAnyKeyToExit() {
        Scanner scanner = new Scanner(System.in);
        ColorPrinter.error(Localizations.t("APP_MAIN_PRESS_ANY_KEY_EXIT"));
        scanner.nextLine();
    }
}
