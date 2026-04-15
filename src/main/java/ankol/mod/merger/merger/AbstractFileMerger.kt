package ankol.mod.merger.merger

import ankol.mod.merger.domain.MergerContext
import ankol.mod.merger.core.BaseTreeNode
import ankol.mod.merger.core.filetrees.AbstractFileTree
import ankol.mod.merger.domain.MergeResult
import ankol.mod.merger.domain.ParsedResult
import org.antlr.v4.runtime.CharStream
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import org.antlr.v4.runtime.Lexer
import org.antlr.v4.runtime.Parser
import org.antlr.v4.runtime.tree.ParseTree

/**
 * 文件合并器通用接口
 *
 * @param context 当前执行合并逻辑的上下文信息
 * 定义了所有文件类型合并器必须实现的方法。
 * 每个实现类负责处理一种特定的文件类型（如.scr, .xml等）。
 */
abstract class AbstractFileMerger(val context: MergerContext) {
    /**
     * 合并两个文件。
     *
     * @param accumulatedFile 第一个文件（来自Mod1）的路径。
     * @param incomingModFile 第二个文件（来自Mod2）的路径。
     * @return 一个包含合并后内容和冲突信息的 [ankol.mod.merger.domain.MergeResult] 对象。
     */
    abstract fun merge(accumulatedFile: AbstractFileTree, incomingModFile: AbstractFileTree): MergeResult

    /**
     * 解析模板：统一 CharStream/TokenStream 构建流程，子类仅提供各自语法入口和 AST 访问逻辑。
     */
    protected fun <T : BaseTreeNode, L : Lexer, P : Parser, PT : ParseTree> parseContentWithTemplate(
        content: String,
        lexerFactory: (CharStream) -> L,
        parserFactory: (CommonTokenStream) -> P,
        parseTreeFactory: (P) -> PT,
        astBuilder: (CommonTokenStream, PT) -> T
    ): ParsedResult<T> {
        val charStream = CharStreams.fromString(content)
        val lexer = lexerFactory(charStream)
        val tokenStream = CommonTokenStream(lexer)
        val parser = parserFactory(tokenStream)
        val parseTree = parseTreeFactory(parser)
        val astNode = astBuilder(tokenStream, parseTree)
        return ParsedResult(astNode, tokenStream)
    }
}
