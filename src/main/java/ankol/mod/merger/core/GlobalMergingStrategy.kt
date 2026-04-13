package ankol.mod.merger.core

import ankol.mod.merger.tools.ColorPrinter
import ankol.mod.merger.tools.Localizations.t

/**
 * GlobalMergingStrategy
 * @author Ankol
 */
enum class GlobalMergingStrategy {
    /**
     * 传统模式，不进行全局修复
     */
    NORMAL_MODE,

    /**
     * （实验性）全局修复模式，可能修复一些过期mod的问题，但会花费更多时间
     */
    GLOBAL_FIX_MODE,
    ;

    companion object {
        /**
         * 当前启用的合并策略
         */
        lateinit var activeMode: GlobalMergingStrategy

        /**
         * 询问用户是否智能合并代码项
         */
        fun askCodeMergingStrategy() {
            ColorPrinter.blue("=".repeat(75))
            ColorPrinter.bold(t("GLOBAL_STRATEGY_TITLE"))
            ColorPrinter.success(t("GLOBAL_STRATEGY_OPTION_1"))
            ColorPrinter.cyan(t("GLOBAL_STRATEGY_OPTION_2"))
            ColorPrinter.blue("=".repeat(75))
            ColorPrinter.bold(t("CRESOLVER_CHOOSE_PROMPT"))
            while (true) {
                val input = readln()
                if (input == "1") {
                    activeMode = NORMAL_MODE
                    break
                } else if (input == "2") {
                    activeMode = GLOBAL_FIX_MODE
                    break
                } else {
                    ColorPrinter.error(t("ASSET_INVALID_INPUT_PLEASE_ENTER_NUMBER", "1", "2"))
                }
            }
        }
    }
}
