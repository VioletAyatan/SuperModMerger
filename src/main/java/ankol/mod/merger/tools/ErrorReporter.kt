package ankol.mod.merger.tools

import ankol.mod.merger.tools.Localizations.t
import java.util.concurrent.ConcurrentLinkedQueue

/**
 * Error level for reporting
 */
enum class ErrorLevel {
    /** Warning level, printed in yellow */
    WARNING,
    /** Error level, printed in red */
    ERROR
}

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
         * Print error/warning reports with appropriate colors
         */
        @JvmStatic
        fun printErrors() {
            if (errors.isNotEmpty()) {
                val hasError = errors.any { it.level == ErrorLevel.ERROR }
                val hasWarning = errors.any { it.level == ErrorLevel.WARNING }

                if (hasError) {
                    ColorPrinter.error(t("ERROR_REPORTER_TITLE"))
                }
                if (hasWarning) {
                    ColorPrinter.warning(t("WARNING_REPORTER_TITLE"))
                }

                errors.forEach { error ->
                    when (error.level) {
                        ErrorLevel.WARNING -> ColorPrinter.warning(
                            t("ERROR_REPORTER_MSG", error.errorSource, error.message)
                        )
                        ErrorLevel.ERROR -> ColorPrinter.error(
                            t("ERROR_REPORTER_MSG", error.errorSource, error.message)
                        )
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
