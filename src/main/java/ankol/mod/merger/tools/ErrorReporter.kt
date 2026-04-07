package ankol.mod.merger.tools

import java.util.concurrent.ConcurrentLinkedQueue

class ErrorReporter {
    companion object {
        private val errors = ConcurrentLinkedQueue<ErrorMsg>()

        /**
         * 添加错误报告记录
         */
        @JvmStatic
        fun addErrorReport(errorSource: String, message: String) {
            errors.add(ErrorMsg(errorSource, message))
        }

        /**
         * 打印错误报告
         */
        @JvmStatic
        fun printErrors() {
            if (errors.isNotEmpty()) {
                ColorPrinter.error(Localizations.t("ERROR_REPORTER_TITLE"))
                errors.forEach { error ->
                    ColorPrinter.error(Localizations.t("ERROR_REPORTER_MSG", error.errorSource, error.message))
                }
            }
        }
    }

    class ErrorMsg(val errorSource: String, val message: String)
}
