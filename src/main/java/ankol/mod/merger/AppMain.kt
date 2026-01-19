package ankol.mod.merger

import ankol.mod.merger.core.FileMergerEngine
import ankol.mod.merger.core.GlobalMergingStrategy
import ankol.mod.merger.exception.BusinessException
import ankol.mod.merger.gui.ModMergerToolMaterialGUI
import ankol.mod.merger.tools.*
import javafx.application.Application
import java.io.FileDescriptor
import java.io.FileOutputStream
import java.io.PrintStream
import java.nio.charset.StandardCharsets
import java.nio.file.Path
import java.nio.file.Paths
import java.util.concurrent.TimeUnit
import kotlin.io.path.notExists
import kotlin.system.exitProcess

/**
 * Techland模组合并工具 - 主应用入口类
 *
 * 支持两种启动模式：
 * 1. GUI 模式（推荐）：不传递任何参数，直接启动 GUI 应用
 * 2. 命令行模式：传递命令行参数运行
 */
class AppMain {
    companion object {
        private val log = logger<AppMain>()

        @JvmStatic
        fun main(args: Array<String>) {
            /*// 如果没有命令行参数，启动 GUI 版本
            if (args.isEmpty()) {
                launchGUI()
                return
            }

            // 如果指定了 --gui 参数，启动 GUI 版本
            if (args.contains("--gui")) {
                launchGUI()
                return
            }*/

            // 否则运行原来的命令行版本
            launchCLI(args)
        }

        /**
         * 启动 GUI 版本
         */
        private fun launchGUI() {
            try {
                log.info("启动 GUI 版本...")
                Application.launch(ModMergerToolMaterialGUI::class.java)
            } catch (e: Exception) {
                log.error("启动 GUI 失败", e)
                e.printStackTrace()
                exitProcess(1)
            }
        }

        /**
         * 启动命令行版本
         */
        private fun launchCLI(args: Array<String>) {
            var exitCode = 0
            try {
                initCharset() //初始化控制台字符集为UTF-8
                Localizations.init() //初始化国际化文件
                //解析命令行参数
                val argParser = registerArgsParser()
                argParser.parse(args)
                if (argParser.hasOption("h")) {
                    //显示帮助信息并退出
                    argParser.printHelp()
                    exitProcess(0)
                }
                // 扫描需要合并的MOD目录
                val modsToMerge = Tools.scanFiles(Tools.getMergingModDir(), ".pak", ".zip", ".7z")
                // 确定输出路径
                var outputPath = Paths.get(Tools.userDir, "source", "data7.pak")
                if (argParser.hasOption("o")) {
                    outputPath = Paths.get(argParser.getOptionValue("o"))
                }
                // 定位基准MOD的位置
                val baseModPath = locateBaseModPath(argParser)
                //询问自动合并代码策略
                GlobalMergingStrategy.askCodeMergingStrategy()
                // 执行合并
                val start = System.currentTimeMillis()
                FileMergerEngine(modsToMerge, outputPath, baseModPath, argParser).merge()
                val end = System.currentTimeMillis()
                ColorPrinter.success(Localizations.t("APP_MAIN_DONE", end - start))
            } catch (e: Exception) {
                exitCode = 1
                if (e is BusinessException) {
                    ColorPrinter.error(Localizations.t("APP_MAIN_ERROR", e.message))
                } else {
                    log.error(e.message, e)
                }
            } finally {
                parseAnyKeyToExit(exitCode)
            }
        }

        /**
         * 定位基准MOD所在位置
         */
        private fun locateBaseModPath(argParser: SimpleArgParser): Path {
            val baseModPath: Path = if (argParser.hasOption("b")) {
                Paths.get(argParser.getOptionValue("b"))
            } else {
                Paths.get(Tools.userDir, "source", "data0.pak")
            }
            if (baseModPath.notExists()) {
                throw BusinessException(Localizations.t("APP_MAIN_BASE_MOD_NOT_FOUND"))
            }
            return baseModPath
        }

        private fun registerArgsParser(): SimpleArgParser {
            val argParser = SimpleArgParser()
            argParser.addOption("o", "output", true, Localizations.t("APP_MAIN_OPTION_OUTPUT_DESC"))
            argParser.addOption("b", "base", true, Localizations.t("APP_MAIN_OPTION_BASE_DESC"))
            argParser.addOption("h", "help", false, Localizations.t("APP_MAIN_OPTION_HELP_DESC"))
            argParser.addOption("f", "fix", false, Localizations.t("STRATEGYS_GLOBAL_FIX_ENABLE"))
            return argParser
        }

        private fun initCharset() {
            try {
                val p = ProcessBuilder("cmd", "/c", "chcp", "65001")
                    .inheritIO() // 让子进程输出到相同控制台（可见 chcp 的反馈）
                    .start()
                if (!p.waitFor(2, TimeUnit.SECONDS)) {
                    p.destroyForcibly()
                }
                val psOut = PrintStream(FileOutputStream(FileDescriptor.out), true, StandardCharsets.UTF_8)
                val psErr = PrintStream(FileOutputStream(FileDescriptor.err), true, StandardCharsets.UTF_8)
                System.setOut(psOut)
                System.setErr(psErr)
            } catch (e: Exception) {
                System.err.println("Error executing command [chcp] Skip!" + e.message)
            }
        }

        private fun parseAnyKeyToExit(exitCode: Int) {
            ColorPrinter.success(Localizations.t("APP_MAIN_PRESS_ANY_KEY_EXIT"))
            readlnOrNull()
            exitProcess(exitCode)
        }
    }
}
