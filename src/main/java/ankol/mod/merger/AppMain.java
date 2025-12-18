package ankol.mod.merger;

import ankol.mod.merger.core.ModMergerEngine;
import ankol.mod.merger.merger.scr.ScrConflictResolver;
import ankol.mod.merger.tools.Localizations;
import ankol.mod.merger.tools.SimpleArgParser;
import ankol.mod.merger.tools.Tools;
import cn.hutool.core.util.StrUtil;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * Techland模组合并工具 - 主应用入口类
 */
public class AppMain {

    public static void main(String[] args) {
        try {
            Localizations.init(); //初始化国际化文件
            //解析命令行参数
            SimpleArgParser argParser = registerArgsParser();
            argParser.parse(args);
            if (argParser.hasOption("h")) {
                //显示帮助信息并退出
                argParser.printHelp();
                System.exit(0);
            }
            //读取合并目录
            Path mergingModDir = Tools.getMergingModDir();
            List<Path> modsToMerge = new ArrayList<>();

            //先遍历获取需要合并的模组
            try (Stream<Path> pathStream = Files.walk(mergingModDir)) {
                pathStream.forEach(filePath -> {
                    if (Files.isRegularFile(filePath)
                            && StrUtil.endWithAny(filePath.getFileName().toString(), ".pak", ".zip")
                    ) {
                        modsToMerge.add(filePath);
                    }
                });
            }

            ModMergerEngine merger = new ModMergerEngine(modsToMerge);
//            merger.merge();

            System.out.println("\nDone!");
            System.exit(0);
        } catch (IllegalArgumentException e) {
            // 参数错误处理：打印错误信息，退出码1
            System.err.println("Error: " + e.getMessage());
            System.exit(1);
        } catch (IOException e) {
            // 文件IO错误处理：打印错误信息和堆栈跟踪，退出码2
            System.err.println("IO Error: " + e.getMessage());
            e.printStackTrace();
            System.exit(2);
        } catch (Exception e) {
            // 其他运行时异常处理：打印错误信息和堆栈跟踪，退出码3
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
            System.exit(3);
        } finally {
            ScrConflictResolver.close();
        }
    }

    private static SimpleArgParser registerArgsParser() {
        SimpleArgParser argParser = new SimpleArgParser();
        argParser.addOption("o", "output", true, "指定输出位置"); //指定文件输出在哪
        argParser.addOption("b", "base", true, "基准mod所在的位置"); //mod合并工具所需的基准mod所在目录
        argParser.addOption("h", "help", false, "显示帮助信息"); //显示帮助信息
        return argParser;
    }
}
