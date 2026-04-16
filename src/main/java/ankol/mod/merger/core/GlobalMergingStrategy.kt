package ankol.mod.merger.core

import ankol.mod.merger.tools.ColorPrinter
import ankol.mod.merger.tools.Localizations.t

/**
 * Global merging strategy
 * @author Ankol
 */
enum class GlobalMergingStrategy {
    /**
     * Traditional mode, no global fix
     */
    NORMAL_MODE,

    /**
     * (Experimental) Global fix mode, may fix some outdated mod issues but takes more time
     */
    GLOBAL_FIX_MODE,
    ;

    companion object {
        /**
         * Currently active merging strategy
         */
        lateinit var activeMode: GlobalMergingStrategy

        /**
         * Ask user whether to enable intelligent code merging
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
