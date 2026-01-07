package ankol.mod.merger.core

import org.antlr.v4.runtime.TokenStream

/**
 * 语法树解析结果
 * 
 * @param astNode     解析后的AST节点
 * @param tokenStream 原始Token流
 * @author Ankol
 */
data class ParsedResult<T : BaseTreeNode>(val astNode: T?, val tokenStream: TokenStream)
