package ankol.mod.merger.tools

import ankol.mod.merger.tools.Localizations.t
import java.util.concurrent.ConcurrentLinkedQueue

class ErrorReporter {
    companion object {
        private val errors = ConcurrentLinkedQueue<ErrorMsg>()

        /**
         * Add an error report record
         */
        @JvmStatic
        fun addErrorReport(errorSource: String, message: String) {
            errors.add(ErrorMsg(errorSource, message))
        }

        /**
         * Print error reports
         */
        @JvmStatic
        fun printErrors() {
            if (errors.isNotEmpty()) {
                ColorPrinter.error(t("ERROR_REPORTER_TITLE"))
                errors.forEach { error ->
                    ColorPrinter.error(t("ERROR_REPORTER_MSG", error.errorSource, error.message))
                }
            }
        }
    }

    class ErrorMsg(val errorSource: String, val message: String)
}
