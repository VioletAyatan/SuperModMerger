package ankol.mod.merger

import ankol.mod.merger.core.FileMergerEngine
import ankol.mod.merger.core.GlobalMergingStrategy
import ankol.mod.merger.domain.MergingModInfo
import ankol.mod.merger.exception.BusinessException
import ankol.mod.merger.exception.ExitProcessException
import ankol.mod.merger.tools.*
import ankol.mod.merger.tools.Localizations.t
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
                val modsToMerge = locateMergingMod(argParser)
                // 确定输出路径
                var outputPath = Path(Tools.userDir, "source", "data7.pak")
                if (argParser.hasOption("o")) {
                    outputPath = Path(argParser.getOptionValue("o")!!)
                }
                // 定位基准MOD的位置
                val basePakDirPath = locateBaseModPath(argParser)
                //询问自动合并代码策略
                GlobalMergingStrategy.askCodeMergingStrategy()
                // 执行合并
                val start = System.currentTimeMillis()
                FileMergerEngine(modsToMerge, outputPath, basePakDirPath).merge()
                val end = System.currentTimeMillis()
                ColorPrinter.success(t("APP_MAIN_DONE", end - start))
            } catch (e: Exception) {
                exitCode = 1
                when (e) {
                    is BusinessException -> {
                        ColorPrinter.error(t("APP_MAIN_ERROR", e.message))
                    }

                    is ExitProcessException -> {
                        ColorPrinter.error(e.errorMessage)
                    }

                    else -> {
                        log.error(e.message, e)
                    }
                }
            } finally {
                parseAnyKeyToExit(exitCode)
            }
        }

        private fun locateMergingMod(argParser: SimpleArgParser): List<MergingModInfo> {
            var vortexDeploy = false
            val mergingModDir = Tools.getMergingModDir(argParser.getOptionValue("m")?.let { Path(it) })
            //先找下当前目录有没有Vortex的部署文件，如果有说明是用vortex进行的部署
            mergingModDir.listDirectoryEntries().forEach {
                if (it.isRegularFile() && it.name.contains("""vortex\.deployment\.[^.]+\.json""".toRegex())) {
                    vortexDeploy = true
                }
            }
            val mergingMods = mutableListOf<MergingModInfo>()
            val exts = setOf("pak", "zip", "7z", "rar")
            mergingModDir.walk(PathWalkOption.FOLLOW_LINKS)
                .forEach { path: Path ->
                    if (path.isRegularFile() && path.extension in exts) {
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
                Paths.get(argParser.getOptionValue("b")!!)
            } else {
                Paths.get(Tools.userDir, "source")
            }
            if (baseModPath.notExists() || baseModPath.isRegularFile()) {
                throw BusinessException(t("APP_MAIN_BASE_MOD_NOT_FOUND"))
            }
            return baseModPath
        }

        private fun registerArgsParser(): SimpleArgParser {
            val argParser = SimpleArgParser()
            argParser.addOption("m", "merge", true, t("APP_MAIN_OPTION_MERGE_DESC"))
            argParser.addOption("o", "output", true, t("APP_MAIN_OPTION_OUTPUT_DESC"))
            argParser.addOption("b", "base", true, t("APP_MAIN_OPTION_BASE_DESC"))
            argParser.addOption("h", "help", false, t("APP_MAIN_OPTION_HELP_DESC"))
            return argParser
        }

        private fun initCharset() {
            try {
                val osName = System.getProperty("os.name", "")
                if (osName.contains("Windows")) {
                    // 执行 chcp 命令
                    ProcessBuilder("cmd", "/c", "chcp 65001").inheritIO().start().waitFor(2, TimeUnit.SECONDS)
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
            ColorPrinter.success(t("APP_MAIN_PRESS_ANY_KEY_EXIT"))
            readlnOrNull()
            exitProcess(exitCode)
        }
    }
}
