package ankol.mod.merger.tools

import ankol.mod.merger.tools.Tools.format
import java.util.*

/**
 * Console color printing utility class
 *
 * Supports ANSI color output, effective only on Windows 10+ and Unix-like systems. Usage examples:
 * - ColorPrinter.cyan("This is cyan message");
 * - ColorPrinter.success("Operation successful");
 * - ColorPrinter.warning("Warning message");
 * - ColorPrinter.error("Error occurred");
 * @author Ankol
 */
object ColorPrinter {
    private val log = logger()

    // ANSI color codes
    private const val RESET = "\u001b[0m"
    private const val BLACK = "\u001b[30m"
    private const val RED = "\u001b[31m"
    private const val GREEN = "\u001b[32m"
    private const val YELLOW = "\u001b[33m"
    private const val BLUE = "\u001b[34m"
    private const val MAGENTA = "\u001b[35m"
    private const val CYAN = "\u001b[36m"
    private const val WHITE = "\u001b[37m"

    // Bright colors
    private const val BRIGHT_RED = "\u001b[91m"
    private const val BRIGHT_GREEN = "\u001b[92m"
    private const val BRIGHT_YELLOW = "\u001b[93m"
    private const val BRIGHT_BLUE = "\u001b[94m"
    private const val BRIGHT_MAGENTA = "\u001b[95m"
    private const val BRIGHT_CYAN = "\u001b[96m"
    private const val BRIGHT_WHITE = "\u001b[97m"

    // Background colors
    private const val BG_RED = "\u001b[41m"
    private const val BG_GREEN = "\u001b[42m"
    private const val BG_YELLOW = "\u001b[43m"
    private const val BG_BLUE = "\u001b[44m"

    // Styles
    private const val BOLD = "\u001b[1m"
    private const val DIM = "\u001b[2m"
    private const val ITALIC = "\u001b[3m"
    private const val UNDERLINE = "\u001b[4m"

    /**
     * Check if color output is supported
     */
    // Check if color output is supported (Windows 10+ or Unix-like systems)
    val isColorSupported: Boolean = supportsColor()

    /**
     * Check if the system supports ANSI color output
     */
    private fun supportsColor(): Boolean {
        // Windows 10+ supports ANSI, check OS and version
        val os = System.getProperty("os.name", "").lowercase(Locale.getDefault())
        val osVersion = System.getProperty("os.version", "")

        // Unix-like systems (Linux, macOS, etc.)
        if (os.contains("linux") || os.contains("mac") || os.contains("unix")) {
            return true
        }

        // Windows 10+ supports ANSI
        if (os.contains("windows")) {
            try {
                // Windows 10 and above
                val versionParts: Array<String?> =
                    osVersion.split("\\.".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                if (versionParts.isNotEmpty()) {
                    val major = versionParts[0]!!.toInt()
                    // Windows 10 = 10.0, Windows 11 = 10.0 but build > 21000
                    return major >= 10
                }
            } catch (e: Exception) {
                return false
            }
        }

        return false
    }

    /**
     * Apply color (if not supported, return original text)
     */
    private fun applyColor(text: String, color: String): String {
        if (!isColorSupported) {
            return text
        }
        return color + text + RESET
    }

    /**
     * Print blue log
     *
     * @param message Text
     */
    fun blue(message: String) {
        log.info(applyColor(message, BRIGHT_BLUE))
    }

    /**
     * Print blue log with formatted arguments
     *
     * @param format Text template
     * @param args   Formatting arguments
     */
    fun blue(format: String, vararg args: Any) {
        ColorPrinter.blue(format(format, *args))
    }

    /**
     * Print cyan log
     */
    @JvmStatic
    fun cyan(message: String) {
        log.info(applyColor(message, BRIGHT_CYAN))
    }

    /**
     * Print cyan log with formatted arguments
     */
    @JvmStatic
    fun cyan(format: String, vararg args: Any) {
        ColorPrinter.cyan(format(format, *args))
    }

    /**
     * Print success message (green)
     */
    fun success(message: String) {
        log.info(applyColor(message, BRIGHT_GREEN))
    }

    /**
     * Print success message (green) with formatted arguments
     */
    fun success(format: String, vararg args: Any) {
        ColorPrinter.success(format(format, *args))
    }

    /**
     * Print warning message (yellow)
     */
    fun warning(message: String) {
        log.info(applyColor(message, BRIGHT_YELLOW))
    }

    /**
     * Print warning message (yellow) with formatted arguments
     */
    fun warning(format: String, vararg args: Any) {
        ColorPrinter.warning(format(format, *args))
    }

    /**
     * Print error message (red)
     */
    fun error(message: String) {
        log.info(applyColor(message, BRIGHT_RED))
    }

    /**
     * Print error message (red) with formatted arguments
     */
    fun error(format: String, vararg args: Any?) {
        ColorPrinter.error(format(format, *args))
    }

    /**
     * Print debug message (cyan)
     */
    fun debug(message: String) {
        log.info(applyColor(message, BRIGHT_CYAN))
    }

    /**
     * Print debug message (cyan) with formatted arguments
     */
    fun debug(format: String, vararg args: Any) {
        ColorPrinter.debug(format(format, *args))
    }

    /**
     * Print normal message (white)
     */
    fun print(message: String) {
        log.info(applyColor(message, WHITE))
    }

    /**
     * Print normal message (white) with formatted arguments
     */
    fun print(format: String, vararg args: Any) {
        ColorPrinter.print(format(format, *args))
    }

    /**
     * Print bold message (white bold)
     */
    fun bold(message: String) {
        log.info(applyColor(BOLD + message, RESET))
    }

    /**
     * Print bold message (white bold) with formatted arguments
     */
    fun bold(format: String, vararg args: Any) {
        ColorPrinter.bold(format(format, *args))
    }

    /**
     * Print highlight message (magenta)
     */
    fun highlight(message: String) {
        log.info(applyColor(message, BRIGHT_MAGENTA))
    }

    /**
     * Print highlight message (magenta) with formatted arguments
     */
    fun highlight(format: String, vararg args: Any) {
        log.info(applyColor(format(format, *args), BRIGHT_MAGENTA))
    }

    /**
     * Print with custom color
     *
     * @param message   Message content
     * @param colorCode ANSI color code (e.g. ColorPrinter.RED)
     */
    fun printWithColor(message: String, colorCode: String) {
        log.info(applyColor(message, colorCode))
    }

    /**
     * Get colored text (does not print directly)
     */
    fun getColoredText(text: String, colorCode: String): String {
        return applyColor(text, colorCode)
    }
}
