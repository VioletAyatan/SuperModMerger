package ankol.mod.merger.api

/**
 * 日志打印器
 * 抽象出单独的接口用于CLI和GUI两个版本的适配
 * @author Ankol
 */
interface ColorPrinter {
    /**
     * Print blue log
     *
     * @param message Text
     */
    fun blue(message: String)

    /**
     * Print blue log with formatted arguments
     *
     * @param format Text template
     * @param args   Formatting arguments
     */
    fun blue(format: String, vararg args: Any)

    /**
     * Print cyan log
     */
    fun cyan(message: String)

    /**
     * Print cyan log with formatted arguments
     */
    fun cyan(format: String, vararg args: Any)

    /**
     * Print success message (green)
     */
    fun success(message: String)

    /**
     * Print success message (green) with formatted arguments
     */
    fun success(format: String, vararg args: Any)

    /**
     * Print warning message (yellow)
     */
    fun warning(message: String)

    /**
     * Print warning message (yellow) with formatted arguments
     */
    fun warning(format: String, vararg args: Any)

    /**
     * Print error message (red)
     */
    fun error(message: String)

    /**
     * Print error message (red) with formatted arguments
     */
    fun error(format: String, vararg args: Any?)

    /**
     * Print debug message (cyan)
     */
    fun debug(message: String)

    /**
     * Print debug message (cyan) with formatted arguments
     */
    fun debug(format: String, vararg args: Any)

    /**
     * Print normal message (white)
     */
    fun print(message: String)

    /**
     * Print normal message (white) with formatted arguments
     */
    fun print(format: String, vararg args: Any)

    /**
     * Print bold message (white bold)
     */
    fun bold(message: String)

    /**
     * Print bold message (white bold) with formatted arguments
     */
    fun bold(format: String, vararg args: Any)

    /**
     * Print highlight message (magenta)
     */
    fun highlight(message: String)

    /**
     * Print highlight message (magenta) with formatted arguments
     */
    fun highlight(format: String, vararg args: Any)
}
