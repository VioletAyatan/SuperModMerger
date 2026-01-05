package ankol.mod.merger

import ankol.mod.merger.core.FileMergerEngine
import ankol.mod.merger.core.GlobalMergingStrategy
import ankol.mod.merger.exception.BusinessException
import ankol.mod.merger.tools.*
import java.io.File
import java.io.FileDescriptor
import java.io.FileOutputStream
import java.io.PrintStream
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.util.concurrent.TimeUnit
import kotlin.io.path.Path
import kotlin.system.exitProcess

/**
 * Techland模组合并工具 - 主应用入口类
 */
class AppMain {
    companion object {
        private val log = logger<AppMain>()

        @JvmStatic
        fun main(args: Array<String>) {
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
                var outputPath = Path(Tools.userDir, "source", "data7.pak")
                if (argParser.hasOption("o")) {
                    outputPath = Path(argParser.getOptionValue("o"))
                }
                // 定位基准MOD的位置
                val baseModPath = locateBaseModPath(argParser)
                //询问自动合并代码策略
                GlobalMergingStrategy.askAutoMergingCode()
                // 执行合并
                val start = System.currentTimeMillis()
                FileMergerEngine(modsToMerge, outputPath, baseModPath).merge()
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

        private fun locateBaseModPath(argParser: SimpleArgParser): Path {
            val baseModPath: Path
            if (argParser.hasOption("b")) {
                baseModPath = Path.of(argParser.getOptionValue("b"))
            } else {
                // 如果没有指定，尝试使用默认位置 source/data0.pak
                val defaultBaseMod = Path.of(Tools.userDir, "source", "data0.pak")
                if (Files.exists(defaultBaseMod)) {
                    baseModPath = defaultBaseMod
                } else {
                    throw BusinessException(Localizations.t("APP_MAIN_BASE_MOD_NOT_FOUND", File.separator))
                }
            }
            return baseModPath
        }

        private fun registerArgsParser(): SimpleArgParser {
            val argParser = SimpleArgParser()
            argParser.addOption("o", "output", true, Localizations.t("APP_MAIN_OPTION_OUTPUT_DESC"))
            argParser.addOption("b", "base", true, Localizations.t("APP_MAIN_OPTION_BASE_DESC"))
            argParser.addOption("h", "help", false, Localizations.t("APP_MAIN_OPTION_HELP_DESC"))
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


