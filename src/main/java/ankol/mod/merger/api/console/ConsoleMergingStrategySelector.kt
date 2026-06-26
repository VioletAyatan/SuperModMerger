package ankol.mod.merger.api.console

import ankol.mod.merger.api.MergingStrategySelector
import ankol.mod.merger.core.GlobalMergingStrategy
import ankol.mod.merger.tools.ConsoleColorPrinter
import ankol.mod.merger.tools.Localizations.t

/**
 * 控制台实现的全局合并策略选择器
 * 保留原有的 readln() 交互行为
 */
object ConsoleMergingStrategySelector : MergingStrategySelector {

    override fun askStrategy(): GlobalMergingStrategy {
        ConsoleColorPrinter.blue("=".repeat(75))
        ConsoleColorPrinter.bold(t("GLOBAL_STRATEGY_TITLE"))
        ConsoleColorPrinter.success(t("GLOBAL_STRATEGY_OPTION_1"))
        ConsoleColorPrinter.cyan(t("GLOBAL_STRATEGY_OPTION_2"))
        ConsoleColorPrinter.blue("=".repeat(75))
        ConsoleColorPrinter.bold(t("CRESOLVER_CHOOSE_PROMPT"))
        while (true) {
            val input = readln()
            if (input == "1") {
                return GlobalMergingStrategy.NORMAL_MODE
            } else if (input == "2") {
                return GlobalMergingStrategy.GLOBAL_FIX_MODE
            } else {
                ConsoleColorPrinter.error(t("ASSET_INVALID_INPUT_PLEASE_ENTER_NUMBER", "1", "2"))
            }
        }
    }
}
