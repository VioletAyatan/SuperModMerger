package ankol.mod.merger.core

import ankol.mod.merger.api.console.ConsoleMergingStrategySelector
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
         * Delegates to [ConsoleMergingStrategySelector]
         */
        fun askCodeMergingStrategy() {
            activeMode = ConsoleMergingStrategySelector.askStrategy()
        }
    }
}
