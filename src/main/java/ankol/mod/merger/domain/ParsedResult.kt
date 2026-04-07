package ankol.mod.merger.domain

import ankol.mod.merger.core.BaseTreeNode
import org.antlr.v4.runtime.TokenStream

/**
 * 语法树解析结果
 *
 * @param astNode     解析后的AST节点
 * @param tokenStream 原始Token流
 * @author Ankol
 */
data class ParsedResult<N : BaseTreeNode>(val astNode: N, val tokenStream: TokenStream)
