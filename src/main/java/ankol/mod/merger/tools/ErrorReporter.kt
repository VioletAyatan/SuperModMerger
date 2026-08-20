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
         * Add a structured error report record (defaults to ERROR level)
         */
        @JvmStatic
        fun addErrorReport(errorSource: String, fileName: String, message: String) {
            addErrorReport(ErrorLevel.ERROR, errorSource, fileName, message)
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
            errors.add(ErrorMsg(level, errorSource, message, null, null))
        }

        /**
         * Add a structured error report record with an optional file hierarchy.
         */
        @JvmStatic
        fun addErrorReport(level: ErrorLevel, errorSource: String, fileName: String, message: String) {
            addErrorReport(level, errorSource, fileName, message, null)
        }

        /**
         * Add a structured error report record with an optional level-wide notice.
         * Identical notices are printed only once for each error level.
         */
        @JvmStatic
        fun addErrorReport(
            level: ErrorLevel,
            errorSource: String,
            fileName: String,
            message: String,
            levelNotice: String?
        ) {
            errors.add(ErrorMsg(level, errorSource, message, fileName, levelNotice))
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
            val formattedReports = formatReports(errors.toList())
            for ((level, lines) in formattedReports) {
                for (line in lines) {
                    when (level) {
                        ErrorLevel.WARNING -> ConsoleColorPrinter.warning(line)
                        ErrorLevel.ERROR -> ConsoleColorPrinter.error(line)
                    }
                }
            }
        }

        /**
         * Build color-free report lines so grouping and layout can be tested independently.
         */
        internal fun formatReports(snapshot: List<ErrorMsg>): Map<ErrorLevel, List<String>> {
            val formattedReports = LinkedHashMap<ErrorLevel, List<String>>()

            ErrorLevel.entries.forEach { level ->
                val levelErrors = snapshot.filter { it.level == level }
                if (levelErrors.isEmpty()) {
                    return@forEach
                }

                val lines = mutableListOf<String>()
                lines += when (level) {
                    ErrorLevel.WARNING -> t("WARNING_REPORTER_TITLE")
                    ErrorLevel.ERROR -> t("ERROR_REPORTER_TITLE")
                }

                val notices = LinkedHashSet<String>()
                for (error in levelErrors) {
                    error.levelNotice?.takeIf { it.isNotBlank() }?.let(notices::add)
                }
                if (notices.isNotEmpty()) {
                    notices.forEach { lines += t("ERROR_REPORTER_NOTICE", it) }
                    lines += ""
                }

                val errorsBySource = LinkedHashMap<String, MutableList<ErrorMsg>>()
                for (error in levelErrors) {
                    errorsBySource.getOrPut(error.errorSource) { mutableListOf() }.add(error)
                }

                for ((sourceIndex, sourceEntry) in errorsBySource.entries.withIndex()) {
                    if (sourceIndex > 0) {
                        lines += ""
                    }
                    val (source, sourceErrors) = sourceEntry
                    lines += t("ERROR_REPORTER_SOURCE", source)

                    val errorsByFile = LinkedHashMap<String?, MutableList<ErrorMsg>>()
                    for (error in sourceErrors) {
                        val fileName = error.fileName?.takeIf { it.isNotBlank() }
                        errorsByFile.getOrPut(fileName) { mutableListOf() }.add(error)
                    }

                    for ((fileName, fileErrors) in errorsByFile) {
                        if (fileName == null) {
                            fileErrors.forEach { lines += t("ERROR_REPORTER_REASON", it.message) }
                        } else {
                            lines += t("ERROR_REPORTER_FILE", fileName)
                            fileErrors.forEach { lines += t("ERROR_REPORTER_FILE_REASON", it.message) }
                        }
                    }
                }

                formattedReports[level] = lines
            }

            return formattedReports
        }
    }

    class ErrorMsg(
        val level: ErrorLevel,
        val errorSource: String,
        val message: String,
        val fileName: String?,
        val levelNotice: String?
    ) {
        constructor(level: ErrorLevel, errorSource: String, message: String) :
            this(level, errorSource, message, null, null)
    }
}
