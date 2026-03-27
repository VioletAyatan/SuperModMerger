package ankol.mod.merger.tools

import ankol.mod.merger.tools.Tools.format
import java.util.*

/**
 * 控制台彩色打印工具类
 *
 * 支持ANSI彩色输出，仅在Windows10+和Unix-like系统中有效，使用示例：
 * - ColorPrinter.cyan("This is cyan message");
 * - ColorPrinter.success("Operation successful");
 * - ColorPrinter.warning("Warning message");
 * - ColorPrinter.error("Error occurred");
 * @author Ankol
 */
object ColorPrinter {
    private val log = logger()

    // ANSI 颜色代码
    private const val RESET = "\u001b[0m"
    private const val BLACK = "\u001b[30m"
    private const val RED = "\u001b[31m"
    private const val GREEN = "\u001b[32m"
    private const val YELLOW = "\u001b[33m"
    private const val BLUE = "\u001b[34m"
    private const val MAGENTA = "\u001b[35m"
    private const val CYAN = "\u001b[36m"
    private const val WHITE = "\u001b[37m"

    // 高亮颜色
    private const val BRIGHT_RED = "\u001b[91m"
    private const val BRIGHT_GREEN = "\u001b[92m"
    private const val BRIGHT_YELLOW = "\u001b[93m"
    private const val BRIGHT_BLUE = "\u001b[94m"
    private const val BRIGHT_MAGENTA = "\u001b[95m"
    private const val BRIGHT_CYAN = "\u001b[96m"
    private const val BRIGHT_WHITE = "\u001b[97m"

    // 背景颜色
    private const val BG_RED = "\u001b[41m"
    private const val BG_GREEN = "\u001b[42m"
    private const val BG_YELLOW = "\u001b[43m"
    private const val BG_BLUE = "\u001b[44m"

    // 样式
    private const val BOLD = "\u001b[1m"
    private const val DIM = "\u001b[2m"
    private const val ITALIC = "\u001b[3m"
    private const val UNDERLINE = "\u001b[4m"

    /**
     * 检查是否支持彩色输出
     */
    // 检查是否支持彩色输出（Windows 10+ 或 Unix-like 系统）
    val isColorSupported: Boolean = supportsColor()

    /**
     * 检查系统是否支持 ANSI 彩色输出
     */
    private fun supportsColor(): Boolean {
        // Windows 10+ 支持 ANSI，通过检查 OS 和版本
        val os = System.getProperty("os.name", "").lowercase(Locale.getDefault())
        val osVersion = System.getProperty("os.version", "")

        // Unix-like 系统（Linux, macOS 等）
        if (os.contains("linux") || os.contains("mac") || os.contains("unix")) {
            return true
        }

        // Windows 10+ 支持 ANSI
        if (os.contains("windows")) {
            try {
                // Windows 10 及更高版本
                val versionParts: Array<String?> =
                    osVersion.split("\\.".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                if (versionParts.isNotEmpty()) {
                    val major = versionParts[0]!!.toInt()
                    // Windows 10 = 10.0，Windows 11 = 10.0 但 build > 21000
                    return major >= 10
                }
            } catch (e: Exception) {
                return false
            }
        }

        return false
    }

    /**
     * 应用颜色（如果不支持，返回原文本）
     */
    private fun applyColor(text: String, color: String): String {
        if (!isColorSupported) {
            return text
        }
        return color + text + RESET
    }

    /**
     * 打印蓝色日志
     *
     * @param message 文本
     */
    fun blue(message: String) {
        log.info(applyColor(message, BRIGHT_BLUE))
    }

    /**
     * 打印蓝色日志，带格式化参数
     *
     * @param format 文本模板
     * @param args   格式化参数
     */
    fun blue(format: String, vararg args: Any) {
        ColorPrinter.blue(format(format, *args))
    }

    /**
     * 打印青色日志
     */
    @JvmStatic
    fun cyan(message: String) {
        log.info(applyColor(message, BRIGHT_CYAN))
    }

    /**
     * 打印青色日志，带格式化参数
     */
    @JvmStatic
    fun cyan(format: String, vararg args: Any) {
        ColorPrinter.cyan(format(format, *args))
    }

    /**
     * 打印成功消息（绿色）
     */
    fun success(message: String) {
        log.info(applyColor(message, BRIGHT_GREEN))
    }

    /**
     * 打印成功消息（绿色），带格式化参数
     */
    fun success(format: String, vararg args: Any) {
        ColorPrinter.success(format(format, *args))
    }

    /**
     * 打印警告消息（黄色）
     */
    fun warning(message: String) {
        log.info(applyColor(message, BRIGHT_YELLOW))
    }

    /**
     * 打印警告消息（黄色），带格式化参数
     */
    fun warning(format: String, vararg args: Any) {
        ColorPrinter.warning(format(format, *args))
    }

    /**
     * 打印错误消息（红色）
     */
    fun error(message: String) {
        log.info(applyColor(message, BRIGHT_RED))
    }

    /**
     * 打印错误消息（红色），带格式化参数
     */
    fun error(format: String, vararg args: Any?) {
        ColorPrinter.error(format(format, *args))
    }

    /**
     * 打印调试消息（青色）
     */
    fun debug(message: String) {
        log.info(applyColor(message, BRIGHT_CYAN))
    }

    /**
     * 打印调试消息（青色），带格式化参数
     */
    fun debug(format: String, vararg args: Any) {
        ColorPrinter.debug(format(format, *args))
    }

    /**
     * 打印普通消息（白色）
     */
    fun print(message: String) {
        log.info(applyColor(message, WHITE))
    }

    /**
     * 打印普通消息（白色），带格式化参数
     */
    fun print(format: String, vararg args: Any) {
        ColorPrinter.print(format(format, *args))
    }

    /**
     * 打印加粗消息（白色加粗）
     */
    fun bold(message: String) {
        log.info(applyColor(BOLD + message, RESET))
    }

    /**
     * 打印加粗消息（白色加粗），带格式化参数
     */
    fun bold(format: String, vararg args: Any) {
        ColorPrinter.bold(format(format, *args))
    }

    /**
     * 打印强调消息（洋红色）
     */
    fun highlight(message: String) {
        log.info(applyColor(message, BRIGHT_MAGENTA))
    }

    /**
     * 打印强调消息（洋红色），带格式化参数
     */
    fun highlight(format: String, vararg args: Any) {
        log.info(applyColor(format(format, *args), BRIGHT_MAGENTA))
    }

    /**
     * 自定义颜色打印
     *
     * @param message   消息内容
     * @param colorCode ANSI 颜色代码（如 ColorPrinter.RED）
     */
    fun printWithColor(message: String, colorCode: String) {
        log.info(applyColor(message, colorCode))
    }

    /**
     * 获取彩色文本（不直接打印）
     */
    fun getColoredText(text: String, colorCode: String): String {
        return applyColor(text, colorCode)
    }
}

