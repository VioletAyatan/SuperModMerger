package ankol.mod.merger.tools

import ankol.mod.merger.constants.ErrorLevel
import ankol.mod.merger.tools.Localizations.t
import java.util.concurrent.ConcurrentLinkedQueue

class ErrorReporter {
    companion object {
        private val errors = ConcurrentLinkedQueue<ErrorMsg>()

        /**
         * Add an error report record (defaults to ERROR level)
         */
        @JvmStatic
        fun addErrorReport(errorSource: String, message: String) {
            addErrorReport(ErrorLevel.ERROR, errorSource, message)
        }

        /**
         * Add an error report record with specified level
         *
         * @param level       Error level (WARNING or ERROR)
         * @param errorSource Source of the error
         * @param message     Error description
         */
        @JvmStatic
        fun addErrorReport(level: ErrorLevel, errorSource: String, message: String) {
            errors.add(ErrorMsg(level, errorSource, message))
        }

        /**
         * 获取错误总数
         */
        @JvmStatic
        fun getErrorCount(): Int = errors.size

        /**
         * Print error/warning reports with appropriate colors
         */
        @JvmStatic
        fun printErrors() {
            if (errors.isNotEmpty()) {
                ErrorLevel.entries.forEach { level: ErrorLevel ->
                    val grouped = errors.filter { it.level == level }
                    if (grouped.isNotEmpty()) {
                        when (level) {
                            ErrorLevel.WARNING -> ColorPrinter.warning(t("WARNING_REPORTER_TITLE"))
                            ErrorLevel.ERROR -> ColorPrinter.error(t("ERROR_REPORTER_TITLE"))
                        }
                        grouped.forEach { error ->
                            val msg = t("ERROR_REPORTER_MSG", error.errorSource, error.message)
                            when (level) {
                                ErrorLevel.WARNING -> ColorPrinter.warning(msg)
                                ErrorLevel.ERROR -> ColorPrinter.error(msg)
                            }
                        }
                    }
                }
            }
        }
    }

    class ErrorMsg(
        val level: ErrorLevel,
        val errorSource: String,
        val message: String
    )
}
