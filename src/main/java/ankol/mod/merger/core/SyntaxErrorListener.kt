package ankol.mod.merger.core

import ankol.mod.merger.domain.MergerContext
import ankol.mod.merger.tools.ErrorLevel
import ankol.mod.merger.tools.ErrorReporter
import org.antlr.v4.runtime.ANTLRErrorListener
import org.antlr.v4.runtime.Parser
import org.antlr.v4.runtime.RecognitionException
import org.antlr.v4.runtime.Recognizer
import org.antlr.v4.runtime.atn.ATNConfigSet
import org.antlr.v4.runtime.dfa.DFA
import java.util.*

class SyntaxErrorListener(val context: MergerContext) : ANTLRErrorListener {

    override fun syntaxError(
        recognizer: Recognizer<*, *>,
        offendingSymbol: Any,
        line: Int,
        charPositionInLine: Int,
        msg: String,
        e: RecognitionException?
    ) {
        println("SyntaxErrorListener.syntaxError")
        ErrorReporter.addErrorReport(
            ErrorLevel.WARNING,
            context.mergeModName,
            "在文件 ${context.mergingFileName} 中识别语法错误，这不会影响合并结果，但可能会导致游戏内部出现问题，你可以尝试联系MOD作者处理\n" +
                    "   行${line}:${charPositionInLine} $msg"
        )
    }

    override fun reportAmbiguity(
        recognizer: Parser?,
        dfa: DFA?,
        startIndex: Int,
        stopIndex: Int,
        exact: Boolean,
        ambigAlts: BitSet?,
        configs: ATNConfigSet?
    ) {
    }

    override fun reportAttemptingFullContext(
        recognizer: Parser?,
        dfa: DFA?,
        startIndex: Int,
        stopIndex: Int,
        conflictingAlts: BitSet?,
        configs: ATNConfigSet?
    ) {
    }

    override fun reportContextSensitivity(
        recognizer: Parser?,
        dfa: DFA?,
        startIndex: Int,
        stopIndex: Int,
        prediction: Int,
        configs: ATNConfigSet?
    ) {
    }

}
