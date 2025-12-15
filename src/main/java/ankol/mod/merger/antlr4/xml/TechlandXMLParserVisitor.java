// Generated from java-escape by ANTLR 4.11.1
package ankol.mod.merger.antlr4.xml;
import org.antlr.v4.runtime.tree.ParseTreeVisitor;

/**
 * This interface defines a complete generic visitor for a parse tree produced
 * by {@link TechlandXMLParser}.
 *
 * @param <T> The return type of the visit operation. Use {@link Void} for
 * operations with no return type.
 */
public interface TechlandXMLParserVisitor<T> extends ParseTreeVisitor<T> {
	/**
	 * Visit a parse tree produced by {@link TechlandXMLParser#document}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDocument(TechlandXMLParser.DocumentContext ctx);
	/**
	 * Visit a parse tree produced by {@link TechlandXMLParser#prolog}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitProlog(TechlandXMLParser.PrologContext ctx);
	/**
	 * Visit a parse tree produced by {@link TechlandXMLParser#content}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitContent(TechlandXMLParser.ContentContext ctx);
	/**
	 * Visit a parse tree produced by {@link TechlandXMLParser#element}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitElement(TechlandXMLParser.ElementContext ctx);
	/**
	 * Visit a parse tree produced by {@link TechlandXMLParser#reference}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitReference(TechlandXMLParser.ReferenceContext ctx);
	/**
	 * Visit a parse tree produced by {@link TechlandXMLParser#attribute}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAttribute(TechlandXMLParser.AttributeContext ctx);
	/**
	 * Visit a parse tree produced by {@link TechlandXMLParser#chardata}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitChardata(TechlandXMLParser.ChardataContext ctx);
	/**
	 * Visit a parse tree produced by {@link TechlandXMLParser#misc}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMisc(TechlandXMLParser.MiscContext ctx);
}