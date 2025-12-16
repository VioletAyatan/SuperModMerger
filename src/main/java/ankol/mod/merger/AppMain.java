package ankol.mod.merger;

import ankol.mod.merger.core.SimpleArgumentsParser;
import ankol.mod.merger.core.ModMergerEngine;
import ankol.mod.merger.merger.scr.ScrConflictResolver;
import ankol.mod.merger.tools.Localizations;
import cn.hutool.core.io.FileUtil;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Techland模组合并工具 - 主应用入口类
 */
public class AppMain {
    /**
     * 主程序入口方法
     * <p>
     * ���行流程：
     * 1. 解析命令行参数生成配置对象 (MergeConfig.fromArgs)
     * 2. 验证配置的有效性 (config.validate)
     * 3. 如果启用详细模式，打印配置信息
     * 4. 创建合并引擎实例，传入配置参数
     * 5. 启动合并过程 (merger.merge)
     * 6. 合并成功则打印完成信息并以0退出
     * <p>
     * 异常处理：
     * - IllegalArgumentException: 参数验证失败，退出码1
     * - IOException: 文件操作失败，退出码2，打印堆栈跟踪
     * - 其他异常: 未预期的运行时错误，退出码3，打印堆栈跟踪
     * <p>
     * finally块：
     * - 无论成功或失败，都会关闭ConflictResolver的扫描器资源
     *
     * @param args 命令行参数数组
     *             args[0] - 第一个模组目录路径
     *             args[1] - 第二个模组目录路径
     *             可选参数 - -o, -a, -v, -h 等
     */
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

            // 支持 baseline + 多 mod 的合并流程：
            // args[0] = baseline (官方/原版目录), args[1..N] = mod dirs to merge on top
            List<Path> modsToMerge = new ArrayList<>();
            // 从原始 args 中收集除了第0个 baseline 之外的 mod 目录
            for (int i = 1; i < args.length; i++) {
                String a = args[i];
                if (!a.startsWith("-")) {
                    modsToMerge.add(Path.of(a));
                }
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
