// Generated from TechlandScript.g4 by ANTLR 4.13.2
package ankol.mod.merger.antlr.scr;
import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link TechlandScriptParser}.
 */
public interface TechlandScriptListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link TechlandScriptParser#file}.
	 * @param ctx the parse tree
	 */
	void enterFile(TechlandScriptParser.FileContext ctx);
	/**
	 * Exit a parse tree produced by {@link TechlandScriptParser#file}.
	 * @param ctx the parse tree
	 */
	void exitFile(TechlandScriptParser.FileContext ctx);
	/**
	 * Enter a parse tree produced by {@link TechlandScriptParser#definition}.
	 * @param ctx the parse tree
	 */
	void enterDefinition(TechlandScriptParser.DefinitionContext ctx);
	/**
	 * Exit a parse tree produced by {@link TechlandScriptParser#definition}.
	 * @param ctx the parse tree
	 */
	void exitDefinition(TechlandScriptParser.DefinitionContext ctx);
	/**
	 * Enter a parse tree produced by {@link TechlandScriptParser#importDecl}.
	 * @param ctx the parse tree
	 */
	void enterImportDecl(TechlandScriptParser.ImportDeclContext ctx);
	/**
	 * Exit a parse tree produced by {@link TechlandScriptParser#importDecl}.
	 * @param ctx the parse tree
	 */
	void exitImportDecl(TechlandScriptParser.ImportDeclContext ctx);
	/**
	 * Enter a parse tree produced by {@link TechlandScriptParser#exportDecl}.
	 * @param ctx the parse tree
	 */
	void enterExportDecl(TechlandScriptParser.ExportDeclContext ctx);
	/**
	 * Exit a parse tree produced by {@link TechlandScriptParser#exportDecl}.
	 * @param ctx the parse tree
	 */
	void exitExportDecl(TechlandScriptParser.ExportDeclContext ctx);
	/**
	 * Enter a parse tree produced by {@link TechlandScriptParser#externDecl}.
	 * @param ctx the parse tree
	 */
	void enterExternDecl(TechlandScriptParser.ExternDeclContext ctx);
	/**
	 * Exit a parse tree produced by {@link TechlandScriptParser#externDecl}.
	 * @param ctx the parse tree
	 */
	void exitExternDecl(TechlandScriptParser.ExternDeclContext ctx);
	/**
	 * Enter a parse tree produced by {@link TechlandScriptParser#directiveCall}.
	 * @param ctx the parse tree
	 */
	void enterDirectiveCall(TechlandScriptParser.DirectiveCallContext ctx);
	/**
	 * Exit a parse tree produced by {@link TechlandScriptParser#directiveCall}.
	 * @param ctx the parse tree
	 */
	void exitDirectiveCall(TechlandScriptParser.DirectiveCallContext ctx);
	/**
	 * Enter a parse tree produced by {@link TechlandScriptParser#macroDecl}.
	 * @param ctx the parse tree
	 */
	void enterMacroDecl(TechlandScriptParser.MacroDeclContext ctx);
	/**
	 * Exit a parse tree produced by {@link TechlandScriptParser#macroDecl}.
	 * @param ctx the parse tree
	 */
	void exitMacroDecl(TechlandScriptParser.MacroDeclContext ctx);
	/**
	 * Enter a parse tree produced by {@link TechlandScriptParser#subDecl}.
	 * @param ctx the parse tree
	 */
	void enterSubDecl(TechlandScriptParser.SubDeclContext ctx);
	/**
	 * Exit a parse tree produced by {@link TechlandScriptParser#subDecl}.
	 * @param ctx the parse tree
	 */
	void exitSubDecl(TechlandScriptParser.SubDeclContext ctx);
	/**
	 * Enter a parse tree produced by {@link TechlandScriptParser#logicControlDecl}.
	 * @param ctx the parse tree
	 */
	void enterLogicControlDecl(TechlandScriptParser.LogicControlDeclContext ctx);
	/**
	 * Exit a parse tree produced by {@link TechlandScriptParser#logicControlDecl}.
	 * @param ctx the parse tree
	 */
	void exitLogicControlDecl(TechlandScriptParser.LogicControlDeclContext ctx);
	/**
	 * Enter a parse tree produced by {@link TechlandScriptParser#elseIfClause}.
	 * @param ctx the parse tree
	 */
	void enterElseIfClause(TechlandScriptParser.ElseIfClauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link TechlandScriptParser#elseIfClause}.
	 * @param ctx the parse tree
	 */
	void exitElseIfClause(TechlandScriptParser.ElseIfClauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link TechlandScriptParser#elseClause}.
	 * @param ctx the parse tree
	 */
	void enterElseClause(TechlandScriptParser.ElseClauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link TechlandScriptParser#elseClause}.
	 * @param ctx the parse tree
	 */
	void exitElseClause(TechlandScriptParser.ElseClauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link TechlandScriptParser#paramList}.
	 * @param ctx the parse tree
	 */
	void enterParamList(TechlandScriptParser.ParamListContext ctx);
	/**
	 * Exit a parse tree produced by {@link TechlandScriptParser#paramList}.
	 * @param ctx the parse tree
	 */
	void exitParamList(TechlandScriptParser.ParamListContext ctx);
	/**
	 * Enter a parse tree produced by {@link TechlandScriptParser#param}.
	 * @param ctx the parse tree
	 */
	void enterParam(TechlandScriptParser.ParamContext ctx);
	/**
	 * Exit a parse tree produced by {@link TechlandScriptParser#param}.
	 * @param ctx the parse tree
	 */
	void exitParam(TechlandScriptParser.ParamContext ctx);
	/**
	 * Enter a parse tree produced by {@link TechlandScriptParser#functionBlock}.
	 * @param ctx the parse tree
	 */
	void enterFunctionBlock(TechlandScriptParser.FunctionBlockContext ctx);
	/**
	 * Exit a parse tree produced by {@link TechlandScriptParser#functionBlock}.
	 * @param ctx the parse tree
	 */
	void exitFunctionBlock(TechlandScriptParser.FunctionBlockContext ctx);
	/**
	 * Enter a parse tree produced by {@link TechlandScriptParser#statements}.
	 * @param ctx the parse tree
	 */
	void enterStatements(TechlandScriptParser.StatementsContext ctx);
	/**
	 * Exit a parse tree produced by {@link TechlandScriptParser#statements}.
	 * @param ctx the parse tree
	 */
	void exitStatements(TechlandScriptParser.StatementsContext ctx);
	/**
	 * Enter a parse tree produced by {@link TechlandScriptParser#variableDecl}.
	 * @param ctx the parse tree
	 */
	void enterVariableDecl(TechlandScriptParser.VariableDeclContext ctx);
	/**
	 * Exit a parse tree produced by {@link TechlandScriptParser#variableDecl}.
	 * @param ctx the parse tree
	 */
	void exitVariableDecl(TechlandScriptParser.VariableDeclContext ctx);
	/**
	 * Enter a parse tree produced by {@link TechlandScriptParser#funtionCallDecl}.
	 * @param ctx the parse tree
	 */
	void enterFuntionCallDecl(TechlandScriptParser.FuntionCallDeclContext ctx);
	/**
	 * Exit a parse tree produced by {@link TechlandScriptParser#funtionCallDecl}.
	 * @param ctx the parse tree
	 */
	void exitFuntionCallDecl(TechlandScriptParser.FuntionCallDeclContext ctx);
	/**
	 * Enter a parse tree produced by {@link TechlandScriptParser#methodReflectFunCallDelc}.
	 * @param ctx the parse tree
	 */
	void enterMethodReflectFunCallDelc(TechlandScriptParser.MethodReflectFunCallDelcContext ctx);
	/**
	 * Exit a parse tree produced by {@link TechlandScriptParser#methodReflectFunCallDelc}.
	 * @param ctx the parse tree
	 */
	void exitMethodReflectFunCallDelc(TechlandScriptParser.MethodReflectFunCallDelcContext ctx);
	/**
	 * Enter a parse tree produced by {@link TechlandScriptParser#funtionBlockDecl}.
	 * @param ctx the parse tree
	 */
	void enterFuntionBlockDecl(TechlandScriptParser.FuntionBlockDeclContext ctx);
	/**
	 * Exit a parse tree produced by {@link TechlandScriptParser#funtionBlockDecl}.
	 * @param ctx the parse tree
	 */
	void exitFuntionBlockDecl(TechlandScriptParser.FuntionBlockDeclContext ctx);
	/**
	 * Enter a parse tree produced by {@link TechlandScriptParser#useDecl}.
	 * @param ctx the parse tree
	 */
	void enterUseDecl(TechlandScriptParser.UseDeclContext ctx);
	/**
	 * Exit a parse tree produced by {@link TechlandScriptParser#useDecl}.
	 * @param ctx the parse tree
	 */
	void exitUseDecl(TechlandScriptParser.UseDeclContext ctx);
	/**
	 * Enter a parse tree produced by {@link TechlandScriptParser#valueList}.
	 * @param ctx the parse tree
	 */
	void enterValueList(TechlandScriptParser.ValueListContext ctx);
	/**
	 * Exit a parse tree produced by {@link TechlandScriptParser#valueList}.
	 * @param ctx the parse tree
	 */
	void exitValueList(TechlandScriptParser.ValueListContext ctx);
	/**
	 * Enter a parse tree produced by {@link TechlandScriptParser#type}.
	 * @param ctx the parse tree
	 */
	void enterType(TechlandScriptParser.TypeContext ctx);
	/**
	 * Exit a parse tree produced by {@link TechlandScriptParser#type}.
	 * @param ctx the parse tree
	 */
	void exitType(TechlandScriptParser.TypeContext ctx);
	/**
	 * Enter a parse tree produced by {@link TechlandScriptParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterExpression(TechlandScriptParser.ExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link TechlandScriptParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitExpression(TechlandScriptParser.ExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link TechlandScriptParser#fieldAccess}.
	 * @param ctx the parse tree
	 */
	void enterFieldAccess(TechlandScriptParser.FieldAccessContext ctx);
	/**
	 * Exit a parse tree produced by {@link TechlandScriptParser#fieldAccess}.
	 * @param ctx the parse tree
	 */
	void exitFieldAccess(TechlandScriptParser.FieldAccessContext ctx);
	/**
	 * Enter a parse tree produced by {@link TechlandScriptParser#arrayValue}.
	 * @param ctx the parse tree
	 */
	void enterArrayValue(TechlandScriptParser.ArrayValueContext ctx);
	/**
	 * Exit a parse tree produced by {@link TechlandScriptParser#arrayValue}.
	 * @param ctx the parse tree
	 */
	void exitArrayValue(TechlandScriptParser.ArrayValueContext ctx);
}