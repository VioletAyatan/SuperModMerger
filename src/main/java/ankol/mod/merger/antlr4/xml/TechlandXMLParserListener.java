// Generated from java-escape by ANTLR 4.11.1
package ankol.mod.merger.antlr4.xml;
import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link TechlandXMLParser}.
 */
public interface TechlandXMLParserListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link TechlandXMLParser#document}.
	 * @param ctx the parse tree
	 */
	void enterDocument(TechlandXMLParser.DocumentContext ctx);
	/**
	 * Exit a parse tree produced by {@link TechlandXMLParser#document}.
	 * @param ctx the parse tree
	 */
	void exitDocument(TechlandXMLParser.DocumentContext ctx);
	/**
	 * Enter a parse tree produced by {@link TechlandXMLParser#prolog}.
	 * @param ctx the parse tree
	 */
	void enterProlog(TechlandXMLParser.PrologContext ctx);
	/**
	 * Exit a parse tree produced by {@link TechlandXMLParser#prolog}.
	 * @param ctx the parse tree
	 */
	void exitProlog(TechlandXMLParser.PrologContext ctx);
	/**
	 * Enter a parse tree produced by {@link TechlandXMLParser#content}.
	 * @param ctx the parse tree
	 */
	void enterContent(TechlandXMLParser.ContentContext ctx);
	/**
	 * Exit a parse tree produced by {@link TechlandXMLParser#content}.
	 * @param ctx the parse tree
	 */
	void exitContent(TechlandXMLParser.ContentContext ctx);
	/**
	 * Enter a parse tree produced by {@link TechlandXMLParser#element}.
	 * @param ctx the parse tree
	 */
	void enterElement(TechlandXMLParser.ElementContext ctx);
	/**
	 * Exit a parse tree produced by {@link TechlandXMLParser#element}.
	 * @param ctx the parse tree
	 */
	void exitElement(TechlandXMLParser.ElementContext ctx);
	/**
	 * Enter a parse tree produced by {@link TechlandXMLParser#reference}.
	 * @param ctx the parse tree
	 */
	void enterReference(TechlandXMLParser.ReferenceContext ctx);
	/**
	 * Exit a parse tree produced by {@link TechlandXMLParser#reference}.
	 * @param ctx the parse tree
	 */
	void exitReference(TechlandXMLParser.ReferenceContext ctx);
	/**
	 * Enter a parse tree produced by {@link TechlandXMLParser#attribute}.
	 * @param ctx the parse tree
	 */
	void enterAttribute(TechlandXMLParser.AttributeContext ctx);
	/**
	 * Exit a parse tree produced by {@link TechlandXMLParser#attribute}.
	 * @param ctx the parse tree
	 */
	void exitAttribute(TechlandXMLParser.AttributeContext ctx);
	/**
	 * Enter a parse tree produced by {@link TechlandXMLParser#chardata}.
	 * @param ctx the parse tree
	 */
	void enterChardata(TechlandXMLParser.ChardataContext ctx);
	/**
	 * Exit a parse tree produced by {@link TechlandXMLParser#chardata}.
	 * @param ctx the parse tree
	 */
	void exitChardata(TechlandXMLParser.ChardataContext ctx);
	/**
	 * Enter a parse tree produced by {@link TechlandXMLParser#misc}.
	 * @param ctx the parse tree
	 */
	void enterMisc(TechlandXMLParser.MiscContext ctx);
	/**
	 * Exit a parse tree produced by {@link TechlandXMLParser#misc}.
	 * @param ctx the parse tree
	 */
	void exitMisc(TechlandXMLParser.MiscContext ctx);
}