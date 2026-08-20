package ankol.mod.merger.tools

import ankol.mod.merger.constants.ErrorLevel
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.util.Locale
import kotlin.test.assertEquals

class ErrorReporterTest {
    @Before
    fun setUpLocale() {
        Localizations.setLocale(Locale.ENGLISH)
    }

    @After
    fun restoreLocale() {
        Localizations.setLocale(Locale.getDefault())
    }

    @Test
    fun groupsWarningsBySourceAndFileWithoutDroppingReasons() {
        val reports = listOf(
            report(
                ErrorLevel.WARNING,
                "MOD-A",
                "scripts\\weather\\morning_rainy_a.scr",
                "Line 107:0: first reason",
                "Syntax warning summary"
            ),
            report(
                ErrorLevel.WARNING,
                "MOD-A",
                "scripts\\weather\\morning_rainy_a.scr",
                "Line 108:4: second reason",
                "Syntax warning summary"
            ),
            report(
                ErrorLevel.WARNING,
                "MOD-A",
                "scripts\\weather\\morning_clear_a.scr",
                "Line 90:46: third reason",
                "Syntax warning summary"
            ),
            report(
                ErrorLevel.WARNING,
                "MOD-B",
                "scripts\\inventory\\inventory.scr",
                "Line 5:1: fourth reason",
                "Syntax warning summary"
            )
        )

        val warningLines = ErrorReporter.formatReports(reports).getValue(ErrorLevel.WARNING)

        assertEquals(
            listOf(
                "Warning Info:",
                "  Syntax warning summary",
                "",
                "MOD-A:",
                "  scripts\\weather\\morning_rainy_a.scr:",
                "    - Line 107:0: first reason",
                "    - Line 108:4: second reason",
                "  scripts\\weather\\morning_clear_a.scr:",
                "    - Line 90:46: third reason",
                "",
                "MOD-B:",
                "  scripts\\inventory\\inventory.scr:",
                "    - Line 5:1: fourth reason"
            ),
            warningLines
        )
    }

    @Test
    fun fallsBackToReasonsDirectlyUnderSourceWhenFileIsMissing() {
        val reports = listOf(
            ErrorReporter.ErrorMsg(ErrorLevel.ERROR, "BROKEN-MOD", "Extraction failed"),
            ErrorReporter.ErrorMsg(ErrorLevel.ERROR, "BROKEN-MOD", "Archive is unreadable")
        )

        val errorLines = ErrorReporter.formatReports(reports).getValue(ErrorLevel.ERROR)

        assertEquals(
            listOf(
                "Error Info:",
                "BROKEN-MOD:",
                "  - Extraction failed",
                "  - Archive is unreadable"
            ),
            errorLines
        )
    }

    @Test
    fun keepsWarningAndErrorSectionsSeparateAndInLevelOrder() {
        val reports = listOf(
            ErrorReporter.ErrorMsg(ErrorLevel.ERROR, "ERROR-SOURCE", "error reason"),
            ErrorReporter.ErrorMsg(ErrorLevel.WARNING, "WARNING-SOURCE", "warning reason")
        )

        val formatted = ErrorReporter.formatReports(reports)

        assertEquals(listOf(ErrorLevel.WARNING, ErrorLevel.ERROR), formatted.keys.toList())
        assertEquals("Warning Info:", formatted.getValue(ErrorLevel.WARNING).first())
        assertEquals("Error Info:", formatted.getValue(ErrorLevel.ERROR).first())
        assertEquals(
            listOf("WARNING-SOURCE:", "  - warning reason"),
            formatted.getValue(ErrorLevel.WARNING).drop(1)
        )
        assertEquals(
            listOf("ERROR-SOURCE:", "  - error reason"),
            formatted.getValue(ErrorLevel.ERROR).drop(1)
        )
    }

    @Test
    fun rendersChineseHierarchyWithExactIndentation() {
        Localizations.setLocale(Locale.SIMPLIFIED_CHINESE)
        val reports = listOf(
            report(
                ErrorLevel.WARNING,
                "MOD-A",
                "scripts\\weather\\morning_rainy_a.scr",
                Localizations.t("ERROR_SYNTAX_REASON", 107, 0, "no viable alternative"),
                Localizations.t("ERROR_SYNTAX_WARNING_NOTICE")
            )
        )

        val warningLines = ErrorReporter.formatReports(reports).getValue(ErrorLevel.WARNING)

        assertEquals(
            listOf(
                "警告信息：",
                "  检测到以下文件存在语法错误，这不会影响合并结果，但可能会导致游戏内部出现问题，你可以尝试将以下错误信息反馈给MOD作者处理",
                "",
                "MOD-A:",
                "  scripts\\weather\\morning_rainy_a.scr:",
                "    - 行 107:0：no viable alternative"
            ),
            warningLines
        )
    }

    private fun report(
        level: ErrorLevel,
        source: String,
        fileName: String,
        message: String,
        notice: String
    ): ErrorReporter.ErrorMsg =
        ErrorReporter.ErrorMsg(level, source, message, fileName, notice)
}
