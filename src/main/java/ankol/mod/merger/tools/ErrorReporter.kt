package ankol.mod.merger.tools

class ErrorReporter {
    companion object {
        private val errors = mutableListOf<ErrorMsg>()

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
            if (errors.isEmpty()) {
                println("No errors reported.")
            } else {
                println("Errors reported:")
                errors.forEach { error ->
                    println("- [${error.errorSource}] ${error.message}")
                }
            }
        }
    }

    class ErrorMsg(val errorSource: String, val message: String)
}
