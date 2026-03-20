package ankol.mod.merger

import ankol.mod.merger.core.FileMergerEngine
import ankol.mod.merger.core.GlobalMergingStrategy
import ankol.mod.merger.domain.MergingModInfo
import ankol.mod.merger.exception.BusinessException
import ankol.mod.merger.tools.*
import org.apache.commons.lang3.Strings
import java.io.FileDescriptor
import java.io.FileOutputStream
import java.io.PrintStream
import java.nio.charset.StandardCharsets
import java.nio.file.Path
import java.nio.file.Paths
import java.util.concurrent.TimeUnit
import kotlin.io.path.*
import kotlin.system.exitProcess

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
                val modsToMerge = locateMergingMod()
                // 确定输出路径
                var outputPath = Path(Tools.userDir, "source", "data7.pak")
                if (argParser.hasOption("o")) {
                    outputPath = Path(argParser.getOptionValue("o"))
                }
                // 定位基准MOD的位置
                val baseModPath = locateBaseModPath(argParser)
                //询问自动合并代码策略
                GlobalMergingStrategy.askCodeMergingStrategy()
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

        private fun locateMergingMod(): List<MergingModInfo> {
            var vortexDeploy = false
            val mergingModDir = Tools.getMergingModDir()
            //先找下当前目录有没有Vortex的部署文件，如果有说明是用vortex进行的部署
            mergingModDir.listDirectoryEntries().forEach {
                if (it.isRegularFile() && it.name.contains("""vortex\.deployment\.[^.]+\.json""".toRegex())) {
                    vortexDeploy = true
                }
            }
            val mergingMods = mutableListOf<MergingModInfo>()
            mergingModDir.walk(PathWalkOption.FOLLOW_LINKS)
                .forEach { path: Path ->
                    if (path.isRegularFile() && Strings.CI.equalsAny(path.extension, "pak", "zip", "7z")) {
                        if (vortexDeploy) {
                            val modName = path.parent.name
                            if (modName == "mods") {
                                mergingMods.add(MergingModInfo(path.name, path))
                            } else {
                                mergingMods.add(MergingModInfo(modName, path))
                            }
                        } else {
                            mergingMods.add(MergingModInfo(path.name, path))
                        }
                    }
                }
            return mergingMods
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
            return argParser
        }

        private fun initCharset() {
            try {
                // 1. 无论什么系统，优先设置属性，辅助第三方库识别
                System.setProperty("file.encoding", "UTF-8")
                System.setProperty("sun.stdout.encoding", "UTF-8")
                System.setProperty("sun.stderr.encoding", "UTF-8")

                val isWindows = System.getProperty("os.name")?.startsWith("Windows", ignoreCase = true) == true

                // 2. 针对 Windows 环境的特殊处理
                if (isWindows) {
                    // 尝试切换控制台代码页到 UTF-8 (chcp 65001)
                    // 放到 try-catch 中，即使是在没有 cmd 的环境（如 IDE、服务、CI）失败也不影响后续逻辑
                    try {
                        // 只有看起来像是在交互式终端时才尝试 chcp，避免不必要的进程创建开销
                        // 但为了兼容性，如果判断不准也无所谓，ProcessBuilder 吃掉异常即可
                        ProcessBuilder("cmd", "/c", "chcp", "65001")
                            .redirectOutput(ProcessBuilder.Redirect.DISCARD)
                            .redirectError(ProcessBuilder.Redirect.DISCARD)
                            .start()
                            .waitFor(1, TimeUnit.SECONDS)
                    } catch (_: Exception) {
                        // 忽略所有 chcp 失败，这只是为了让 cmd 显示正常，不影响程序内部编码
                    }
                }

                // 3. 【核心】强制重置标准输出流为 UTF-8
                // 这一步是关键：无论是在控制台、还是重定向到文件(> log.txt)、还是被服务通过管道捕获
                // 都强制写入 UTF-8 字节。Native Image 默认可能跟随系统 ANSI (GBK)，必须强转。
                val out = PrintStream(FileOutputStream(FileDescriptor.out), true, StandardCharsets.UTF_8)
                val err = PrintStream(FileOutputStream(FileDescriptor.err), true, StandardCharsets.UTF_8)
                System.setOut(out)
                System.setErr(err)

            } catch (e: Exception) {
                // 兜底：如果连设置流都失败，打印原始错误（尽量不崩溃）
                System.err.println("Encodeing set error: " + e.message)
            }
        }

        private fun parseAnyKeyToExit(exitCode: Int) {
            ColorPrinter.success(Localizations.t("APP_MAIN_PRESS_ANY_KEY_EXIT"))
            readlnOrNull()
            exitProcess(exitCode)
        }
    }
}
