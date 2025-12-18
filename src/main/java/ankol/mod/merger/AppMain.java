package ankol.mod.merger;

import ankol.mod.merger.core.SimpleArgumentsParser;
import ankol.mod.merger.core.ModMergerEngine;
import ankol.mod.merger.merger.scr.ScrConflictResolver;
import ankol.mod.merger.tools.Localizations;
import ankol.mod.merger.tools.Tools;
import cn.hutool.core.io.FileUtil;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Techland模组合并工具 - 主应用入口类
 */
public class AppMain {

    public static void main(String[] args) {
        try {
            Localizations.init(); //初始化国际化文件
            //1、解析命令行参数
            SimpleArgumentsParser config = SimpleArgumentsParser.fromArgs(args);
            // 第2步：验证配置的合法性
            config.validate();
            // 第3步：如果启用详细模式，打印配置信息用于调试
            if (config.verbose) {
                System.out.println("Config: " + config);
            }

            Path mergingModDir = Tools.getMergingModDir(config);
            List<Path> modsToMerge = new ArrayList<>();
                if (!a.startsWith("-")) {
                    modsToMerge.add(Path.of(a));
                }

            Path baseline = Path.of(args[0]);
            Path output = config.outputDirectory;

            // 1) 先把 baseline 内容复制到 output 作为合并基准
            System.out.println("Copying baseline to output: " + baseline + " -> " + output);
            // 确保输出目录存在
            FileUtil.mkdir(output.toFile());
            // 递归复制目录，覆盖模式
            FileUtil.copy(baseline.toFile(), output.toFile(), true);

            // 2) 依次将每个 mod 合并�� output（把 output 当作 mod1，mod 当作 mod2）
            for (Path mod : modsToMerge) {
                System.out.println("Merging mod into output: " + mod);
                ModMergerEngine merger = new ModMergerEngine(output, mod, output);
                merger.merge();
            }

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
}
