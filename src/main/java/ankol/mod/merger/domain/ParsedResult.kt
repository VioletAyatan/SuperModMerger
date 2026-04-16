package ankol.mod.merger.domain

import ankol.mod.merger.core.BaseTreeNode
import org.antlr.v4.runtime.TokenStream

/**
 * Syntax tree parsing result
 *
 * @param astNode     Parsed AST node
 * @param tokenStream Original Token stream
 * @author Ankol
 */
data class ParsedResult<N : BaseTreeNode>(val astNode: N, val tokenStream: TokenStream)
