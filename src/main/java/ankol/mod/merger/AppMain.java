package ankol.mod.merger;

import ankol.mod.merger.core.ConflictResolver;
import ankol.mod.merger.core.MergeConfig;
import ankol.mod.merger.merger.ScrScriptModMerger;

import java.io.IOException;

/**
 * Techland模组合并工具 - 主应用入口类
 * <p>
 * 职责：
 * 1. 解析命令行参数
 * 2. 初始化合并配置
 * 3. 启动核心合并引擎
 * 4. 处理异常并返回适当的退出码
 * 5. 清理资源（关闭扫描器等）
 * <p>
 * 使用方式：
 * java -jar ModMergerTool.jar <mod1_dir> <mod2_dir> [options]
 * <p>
 * 退出码：
 * 0 - 成功完成
 * 1 - 参数错误
 * 2 - 文件IO错误
 * 3 - 其他运行时错误
 */
public class AppMain {
    /**
     * 主程序入口方法
     * <p>
     * 执行流程：
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
            // 第1步：从命令行参数解析配置
            // MergeConfig.fromArgs会处理所有选项解析和验证
            MergeConfig config = MergeConfig.fromArgs(args);

            // 第2步：验证配置的合法性
            // 检查模组目录是否存在等
            config.validate();

            // 第3步：如果启用详细模式，打印配置信息用于调试
            if (config.verbose) {
                System.out.println("Config: " + config);
            }

            // 第4步：创建核心合并引擎实例
            // 传入：两个模组目录、输出目录、交互模式标志、默认合并策略
            ScrScriptModMerger merger = new ScrScriptModMerger(
                    config.mod1Directory,
                    config.mod2Directory,
                    config.outputDirectory,
                    config.interactiveMode,
                    config.defaultMergeStrategy
            );

            // 第5步：执行合并操作
            // 这是主要的业务逻辑，会扫描目录、解析脚本、对比差异、处理冲突
            merger.merge();
            // 第6步：合并成功，打印完成信息
            System.out.println("\nDone!");
            // 以成功退出码退出
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
            // 资源清理：无论成功或失败，都关闭扫描器（在交互模式中创建）
            // 防止资源泄漏
            ConflictResolver.close();
        }
    }
}

