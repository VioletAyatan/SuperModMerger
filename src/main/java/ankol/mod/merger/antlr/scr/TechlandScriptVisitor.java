// Generated from TechlandScript.g4 by ANTLR 4.13.2
package ankol.mod.merger.antlr.scr;
import org.antlr.v4.runtime.tree.ParseTreeVisitor;

/**
 * This interface defines a complete generic visitor for a parse tree produced
 * by {@link TechlandScriptParser}.
 *
 * @param <T> The return type of the visit operation. Use {@link Void} for
 * operations with no return type.
 */
public interface TechlandScriptVisitor<T> extends ParseTreeVisitor<T> {
	/**
	 * Visit a parse tree produced by {@link TechlandScriptParser#file}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFile(TechlandScriptParser.FileContext ctx);
	/**
	 * Visit a parse tree produced by {@link TechlandScriptParser#definition}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDefinition(TechlandScriptParser.DefinitionContext ctx);
	/**
	 * Visit a parse tree produced by {@link TechlandScriptParser#importDecl}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitImportDecl(TechlandScriptParser.ImportDeclContext ctx);
	/**
	 * Visit a parse tree produced by {@link TechlandScriptParser#exportDecl}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExportDecl(TechlandScriptParser.ExportDeclContext ctx);
	/**
	 * Visit a parse tree produced by {@link TechlandScriptParser#externDecl}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExternDecl(TechlandScriptParser.ExternDeclContext ctx);
	/**
	 * Visit a parse tree produced by {@link TechlandScriptParser#directiveCall}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDirectiveCall(TechlandScriptParser.DirectiveCallContext ctx);
	/**
	 * Visit a parse tree produced by {@link TechlandScriptParser#macroDecl}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMacroDecl(TechlandScriptParser.MacroDeclContext ctx);
	/**
	 * Visit a parse tree produced by {@link TechlandScriptParser#subDecl}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSubDecl(TechlandScriptParser.SubDeclContext ctx);
	/**
	 * Visit a parse tree produced by {@link TechlandScriptParser#logicControlDecl}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLogicControlDecl(TechlandScriptParser.LogicControlDeclContext ctx);
	/**
	 * Visit a parse tree produced by {@link TechlandScriptParser#elseIfClause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitElseIfClause(TechlandScriptParser.ElseIfClauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link TechlandScriptParser#elseClause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitElseClause(TechlandScriptParser.ElseClauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link TechlandScriptParser#paramList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitParamList(TechlandScriptParser.ParamListContext ctx);
	/**
	 * Visit a parse tree produced by {@link TechlandScriptParser#param}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitParam(TechlandScriptParser.ParamContext ctx);
	/**
	 * Visit a parse tree produced by {@link TechlandScriptParser#functionBlock}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFunctionBlock(TechlandScriptParser.FunctionBlockContext ctx);
	/**
	 * Visit a parse tree produced by {@link TechlandScriptParser#statements}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStatements(TechlandScriptParser.StatementsContext ctx);
	/**
	 * Visit a parse tree produced by {@link TechlandScriptParser#variableDecl}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitVariableDecl(TechlandScriptParser.VariableDeclContext ctx);
	/**
	 * Visit a parse tree produced by {@link TechlandScriptParser#funtionCallDecl}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFuntionCallDecl(TechlandScriptParser.FuntionCallDeclContext ctx);
	/**
	 * Visit a parse tree produced by {@link TechlandScriptParser#funtionBlockDecl}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFuntionBlockDecl(TechlandScriptParser.FuntionBlockDeclContext ctx);
	/**
	 * Visit a parse tree produced by {@link TechlandScriptParser#useDecl}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitUseDecl(TechlandScriptParser.UseDeclContext ctx);
	/**
	 * Visit a parse tree produced by {@link TechlandScriptParser#valueList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitValueList(TechlandScriptParser.ValueListContext ctx);
	/**
	 * Visit a parse tree produced by {@link TechlandScriptParser#type}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitType(TechlandScriptParser.TypeContext ctx);
	/**
	 * Visit a parse tree produced by {@link TechlandScriptParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExpression(TechlandScriptParser.ExpressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link TechlandScriptParser#fieldAccess}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFieldAccess(TechlandScriptParser.FieldAccessContext ctx);
	/**
	 * Visit a parse tree produced by {@link TechlandScriptParser#arrayValue}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitArrayValue(TechlandScriptParser.ArrayValueContext ctx);
}