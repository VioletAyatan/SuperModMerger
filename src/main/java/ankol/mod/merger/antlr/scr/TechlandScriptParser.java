// Generated from TechlandScript.g4 by ANTLR 4.13.2
package ankol.mod.merger.antlr.scr;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.ATN;
import org.antlr.v4.runtime.atn.ATNDeserializer;
import org.antlr.v4.runtime.atn.ParserATNSimulator;
import org.antlr.v4.runtime.atn.PredictionContextCache;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.tree.ParseTreeListener;
import org.antlr.v4.runtime.tree.ParseTreeVisitor;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.util.List;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast", "CheckReturnValue", "this-escape"})
public class TechlandScriptParser extends Parser {
	static { RuntimeMetaData.checkVersion("4.13.2", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		Import=1, Extern=2, Export=3, Sub=4, Use=5, Exclamation=6, KwIf=7, KwElse=8, 
		LParen=9, RParen=10, LBrace=11, RBrace=12, Semicolon=13, Comma=14, Equals=15, 
		LBracket=16, RBracket=17, Dot=18, DoubleColon=19, Plus=20, Minus=21, Mul=22, 
		Div=23, LogicAnd=24, LogicOr=25, BitOr=26, BitAnd=27, BitNot=28, Question=29, 
		Colon=30, Gt=31, Lt=32, Eq=33, NotEq=34, Gte=35, Lte=36, Bool=37, Id=38, 
		MacroId=39, Number=40, String=41, LineComment=42, BlockComment=43, WhiteSpaces=44;
	public static final int
		RULE_file = 0, RULE_definition = 1, RULE_importDecl = 2, RULE_exportDecl = 3, 
		RULE_externDecl = 4, RULE_directiveCall = 5, RULE_macroDecl = 6, RULE_subDecl = 7, 
		RULE_logicControlDecl = 8, RULE_elseIfClause = 9, RULE_elseClause = 10, 
		RULE_paramList = 11, RULE_param = 12, RULE_functionBlock = 13, RULE_statements = 14, 
		RULE_variableDecl = 15, RULE_funtionCallDecl = 16, RULE_methodReferenceFunCallDecl = 17, 
		RULE_funtionBlockDecl = 18, RULE_useDecl = 19, RULE_valueList = 20, RULE_type = 21, 
		RULE_expression = 22, RULE_fieldAccess = 23, RULE_arrayValue = 24;
	public static final String[] ruleNames = makeRuleNames();
	public static final String _serializedATN =
		"\u0004\u0001,\u0137\u0002\u0000\u0007\u0000\u0002\u0001\u0007\u0001\u0002"+
		"\u0002\u0007\u0002\u0002\u0003\u0007\u0003\u0002\u0004\u0007\u0004\u0002"+
		"\u0005\u0007\u0005\u0002\u0006\u0007\u0006\u0002\u0007\u0007\u0007\u0002"+
		"\b\u0007\b\u0002\t\u0007\t\u0002\n\u0007\n\u0002\u000b\u0007\u000b\u0002"+
		"\f\u0007\f\u0002\r\u0007\r\u0002\u000e\u0007\u000e\u0002\u000f\u0007\u000f"+
		"\u0002\u0010\u0007\u0010\u0002\u0011\u0007\u0011\u0002\u0012\u0007\u0012"+
		"\u0002\u0013\u0007\u0013\u0002\u0014\u0007\u0014\u0002\u0015\u0007\u0015"+
		"\u0002\u0016\u0007\u0016\u0002\u0017\u0007\u0017\u0002\u0018\u0007\u0018"+
		"\u0001\u0000\u0005\u00004\b\u0000\n\u0000\f\u00007\t\u0000\u0001\u0000"+
		"\u0001\u0000\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001"+
		"\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0003\u0001"+
		"E\b\u0001\u0001\u0002\u0001\u0002\u0001\u0002\u0003\u0002J\b\u0002\u0001"+
		"\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0003"+
		"\u0003R\b\u0003\u0001\u0004\u0001\u0004\u0001\u0004\u0001\u0004\u0003"+
		"\u0004X\b\u0004\u0001\u0005\u0001\u0005\u0001\u0005\u0001\u0005\u0003"+
		"\u0005^\b\u0005\u0001\u0005\u0001\u0005\u0003\u0005b\b\u0005\u0001\u0006"+
		"\u0001\u0006\u0001\u0006\u0001\u0006\u0001\u0006\u0003\u0006i\b\u0006"+
		"\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0003\u0007o\b\u0007"+
		"\u0001\u0007\u0001\u0007\u0001\u0007\u0001\b\u0001\b\u0001\b\u0001\b\u0001"+
		"\b\u0001\b\u0005\bz\b\b\n\b\f\b}\t\b\u0001\b\u0003\b\u0080\b\b\u0001\t"+
		"\u0001\t\u0001\t\u0001\t\u0001\t\u0001\t\u0001\t\u0001\n\u0001\n\u0001"+
		"\n\u0003\n\u008c\b\n\u0001\n\u0003\n\u008f\b\n\u0001\n\u0001\n\u0001\u000b"+
		"\u0001\u000b\u0001\u000b\u0005\u000b\u0096\b\u000b\n\u000b\f\u000b\u0099"+
		"\t\u000b\u0001\f\u0001\f\u0001\f\u0001\f\u0003\f\u009f\b\f\u0001\r\u0001"+
		"\r\u0005\r\u00a3\b\r\n\r\f\r\u00a6\t\r\u0001\r\u0001\r\u0001\u000e\u0001"+
		"\u000e\u0001\u000e\u0001\u000e\u0001\u000e\u0001\u000e\u0001\u000e\u0001"+
		"\u000e\u0003\u000e\u00b2\b\u000e\u0001\u000f\u0001\u000f\u0001\u000f\u0001"+
		"\u000f\u0003\u000f\u00b8\b\u000f\u0001\u000f\u0003\u000f\u00bb\b\u000f"+
		"\u0001\u0010\u0001\u0010\u0001\u0010\u0003\u0010\u00c0\b\u0010\u0001\u0010"+
		"\u0001\u0010\u0003\u0010\u00c4\b\u0010\u0001\u0011\u0001\u0011\u0001\u0011"+
		"\u0001\u0011\u0001\u0011\u0003\u0011\u00cb\b\u0011\u0001\u0011\u0001\u0011"+
		"\u0003\u0011\u00cf\b\u0011\u0001\u0012\u0001\u0012\u0001\u0012\u0003\u0012"+
		"\u00d4\b\u0012\u0001\u0012\u0001\u0012\u0001\u0012\u0001\u0013\u0001\u0013"+
		"\u0001\u0013\u0001\u0013\u0003\u0013\u00dd\b\u0013\u0001\u0013\u0001\u0013"+
		"\u0003\u0013\u00e1\b\u0013\u0001\u0014\u0001\u0014\u0001\u0014\u0005\u0014"+
		"\u00e6\b\u0014\n\u0014\f\u0014\u00e9\t\u0014\u0001\u0015\u0001\u0015\u0001"+
		"\u0016\u0001\u0016\u0001\u0016\u0001\u0016\u0001\u0016\u0001\u0016\u0001"+
		"\u0016\u0001\u0016\u0003\u0016\u00f5\b\u0016\u0001\u0016\u0001\u0016\u0001"+
		"\u0016\u0001\u0016\u0001\u0016\u0001\u0016\u0001\u0016\u0001\u0016\u0001"+
		"\u0016\u0001\u0016\u0001\u0016\u0001\u0016\u0001\u0016\u0001\u0016\u0001"+
		"\u0016\u0001\u0016\u0003\u0016\u0107\b\u0016\u0001\u0016\u0001\u0016\u0001"+
		"\u0016\u0001\u0016\u0001\u0016\u0001\u0016\u0001\u0016\u0001\u0016\u0001"+
		"\u0016\u0001\u0016\u0001\u0016\u0001\u0016\u0001\u0016\u0001\u0016\u0001"+
		"\u0016\u0001\u0016\u0001\u0016\u0001\u0016\u0001\u0016\u0001\u0016\u0001"+
		"\u0016\u0001\u0016\u0001\u0016\u0001\u0016\u0001\u0016\u0001\u0016\u0001"+
		"\u0016\u0005\u0016\u0124\b\u0016\n\u0016\f\u0016\u0127\t\u0016\u0001\u0017"+
		"\u0001\u0017\u0001\u0017\u0005\u0017\u012c\b\u0017\n\u0017\f\u0017\u012f"+
		"\t\u0017\u0001\u0018\u0001\u0018\u0003\u0018\u0133\b\u0018\u0001\u0018"+
		"\u0001\u0018\u0001\u0018\u0000\u0001,\u0019\u0000\u0002\u0004\u0006\b"+
		"\n\f\u000e\u0010\u0012\u0014\u0016\u0018\u001a\u001c\u001e \"$&(*,.0\u0000"+
		"\u0003\u0001\u0000\u0016\u0017\u0001\u0000\u0014\u0015\u0001\u0000\u001f"+
		"$\u015b\u00005\u0001\u0000\u0000\u0000\u0002D\u0001\u0000\u0000\u0000"+
		"\u0004F\u0001\u0000\u0000\u0000\u0006K\u0001\u0000\u0000\u0000\bS\u0001"+
		"\u0000\u0000\u0000\nY\u0001\u0000\u0000\u0000\fc\u0001\u0000\u0000\u0000"+
		"\u000ej\u0001\u0000\u0000\u0000\u0010s\u0001\u0000\u0000\u0000\u0012\u0081"+
		"\u0001\u0000\u0000\u0000\u0014\u0088\u0001\u0000\u0000\u0000\u0016\u0092"+
		"\u0001\u0000\u0000\u0000\u0018\u009a\u0001\u0000\u0000\u0000\u001a\u00a0"+
		"\u0001\u0000\u0000\u0000\u001c\u00b1\u0001\u0000\u0000\u0000\u001e\u00b3"+
		"\u0001\u0000\u0000\u0000 \u00bc\u0001\u0000\u0000\u0000\"\u00c5\u0001"+
		"\u0000\u0000\u0000$\u00d0\u0001\u0000\u0000\u0000&\u00d8\u0001\u0000\u0000"+
		"\u0000(\u00e2\u0001\u0000\u0000\u0000*\u00ea\u0001\u0000\u0000\u0000,"+
		"\u0106\u0001\u0000\u0000\u0000.\u0128\u0001\u0000\u0000\u00000\u0130\u0001"+
		"\u0000\u0000\u000024\u0003\u0002\u0001\u000032\u0001\u0000\u0000\u0000"+
		"47\u0001\u0000\u0000\u000053\u0001\u0000\u0000\u000056\u0001\u0000\u0000"+
		"\u000068\u0001\u0000\u0000\u000075\u0001\u0000\u0000\u000089\u0005\u0000"+
		"\u0000\u00019\u0001\u0001\u0000\u0000\u0000:E\u0003\u0004\u0002\u0000"+
		";E\u0003\u0006\u0003\u0000<E\u0003\b\u0004\u0000=E\u0003\n\u0005\u0000"+
		">E\u0003\f\u0006\u0000?E\u0003\u000e\u0007\u0000@E\u0003\u001e\u000f\u0000"+
		"AE\u0003$\u0012\u0000BE\u0003\"\u0011\u0000CE\u0003 \u0010\u0000D:\u0001"+
		"\u0000\u0000\u0000D;\u0001\u0000\u0000\u0000D<\u0001\u0000\u0000\u0000"+
		"D=\u0001\u0000\u0000\u0000D>\u0001\u0000\u0000\u0000D?\u0001\u0000\u0000"+
		"\u0000D@\u0001\u0000\u0000\u0000DA\u0001\u0000\u0000\u0000DB\u0001\u0000"+
		"\u0000\u0000DC\u0001\u0000\u0000\u0000E\u0003\u0001\u0000\u0000\u0000"+
		"FG\u0005\u0001\u0000\u0000GI\u0005)\u0000\u0000HJ\u0005\r\u0000\u0000"+
		"IH\u0001\u0000\u0000\u0000IJ\u0001\u0000\u0000\u0000J\u0005\u0001\u0000"+
		"\u0000\u0000KL\u0005\u0003\u0000\u0000LM\u0003*\u0015\u0000MN\u0005&\u0000"+
		"\u0000NO\u0005\u000f\u0000\u0000OQ\u0003,\u0016\u0000PR\u0005\r\u0000"+
		"\u0000QP\u0001\u0000\u0000\u0000QR\u0001\u0000\u0000\u0000R\u0007\u0001"+
		"\u0000\u0000\u0000ST\u0005\u0002\u0000\u0000TU\u0003*\u0015\u0000UW\u0005"+
		"&\u0000\u0000VX\u0005\r\u0000\u0000WV\u0001\u0000\u0000\u0000WX\u0001"+
		"\u0000\u0000\u0000X\t\u0001\u0000\u0000\u0000YZ\u0005\u0006\u0000\u0000"+
		"Z[\u0005&\u0000\u0000[]\u0005\t\u0000\u0000\\^\u0003(\u0014\u0000]\\\u0001"+
		"\u0000\u0000\u0000]^\u0001\u0000\u0000\u0000^_\u0001\u0000\u0000\u0000"+
		"_a\u0005\n\u0000\u0000`b\u0005\r\u0000\u0000a`\u0001\u0000\u0000\u0000"+
		"ab\u0001\u0000\u0000\u0000b\u000b\u0001\u0000\u0000\u0000cd\u0005\'\u0000"+
		"\u0000de\u0005\t\u0000\u0000ef\u0003(\u0014\u0000fh\u0005\n\u0000\u0000"+
		"gi\u0005\r\u0000\u0000hg\u0001\u0000\u0000\u0000hi\u0001\u0000\u0000\u0000"+
		"i\r\u0001\u0000\u0000\u0000jk\u0005\u0004\u0000\u0000kl\u0005&\u0000\u0000"+
		"ln\u0005\t\u0000\u0000mo\u0003\u0016\u000b\u0000nm\u0001\u0000\u0000\u0000"+
		"no\u0001\u0000\u0000\u0000op\u0001\u0000\u0000\u0000pq\u0005\n\u0000\u0000"+
		"qr\u0003\u001a\r\u0000r\u000f\u0001\u0000\u0000\u0000st\u0005\u0007\u0000"+
		"\u0000tu\u0005\t\u0000\u0000uv\u0003,\u0016\u0000vw\u0005\n\u0000\u0000"+
		"w{\u0003\u001a\r\u0000xz\u0003\u0012\t\u0000yx\u0001\u0000\u0000\u0000"+
		"z}\u0001\u0000\u0000\u0000{y\u0001\u0000\u0000\u0000{|\u0001\u0000\u0000"+
		"\u0000|\u007f\u0001\u0000\u0000\u0000}{\u0001\u0000\u0000\u0000~\u0080"+
		"\u0003\u0014\n\u0000\u007f~\u0001\u0000\u0000\u0000\u007f\u0080\u0001"+
		"\u0000\u0000\u0000\u0080\u0011\u0001\u0000\u0000\u0000\u0081\u0082\u0005"+
		"\b\u0000\u0000\u0082\u0083\u0005\u0007\u0000\u0000\u0083\u0084\u0005\t"+
		"\u0000\u0000\u0084\u0085\u0003,\u0016\u0000\u0085\u0086\u0005\n\u0000"+
		"\u0000\u0086\u0087\u0003\u001a\r\u0000\u0087\u0013\u0001\u0000\u0000\u0000"+
		"\u0088\u008e\u0005\b\u0000\u0000\u0089\u008b\u0005\t\u0000\u0000\u008a"+
		"\u008c\u0003,\u0016\u0000\u008b\u008a\u0001\u0000\u0000\u0000\u008b\u008c"+
		"\u0001\u0000\u0000\u0000\u008c\u008d\u0001\u0000\u0000\u0000\u008d\u008f"+
		"\u0005\n\u0000\u0000\u008e\u0089\u0001\u0000\u0000\u0000\u008e\u008f\u0001"+
		"\u0000\u0000\u0000\u008f\u0090\u0001\u0000\u0000\u0000\u0090\u0091\u0003"+
		"\u001a\r\u0000\u0091\u0015\u0001\u0000\u0000\u0000\u0092\u0097\u0003\u0018"+
		"\f\u0000\u0093\u0094\u0005\u000e\u0000\u0000\u0094\u0096\u0003\u0018\f"+
		"\u0000\u0095\u0093\u0001\u0000\u0000\u0000\u0096\u0099\u0001\u0000\u0000"+
		"\u0000\u0097\u0095\u0001\u0000\u0000\u0000\u0097\u0098\u0001\u0000\u0000"+
		"\u0000\u0098\u0017\u0001\u0000\u0000\u0000\u0099\u0097\u0001\u0000\u0000"+
		"\u0000\u009a\u009b\u0003*\u0015\u0000\u009b\u009e\u0005&\u0000\u0000\u009c"+
		"\u009d\u0005\u000f\u0000\u0000\u009d\u009f\u0003,\u0016\u0000\u009e\u009c"+
		"\u0001\u0000\u0000\u0000\u009e\u009f\u0001\u0000\u0000\u0000\u009f\u0019"+
		"\u0001\u0000\u0000\u0000\u00a0\u00a4\u0005\u000b\u0000\u0000\u00a1\u00a3"+
		"\u0003\u001c\u000e\u0000\u00a2\u00a1\u0001\u0000\u0000\u0000\u00a3\u00a6"+
		"\u0001\u0000\u0000\u0000\u00a4\u00a2\u0001\u0000\u0000\u0000\u00a4\u00a5"+
		"\u0001\u0000\u0000\u0000\u00a5\u00a7\u0001\u0000\u0000\u0000\u00a6\u00a4"+
		"\u0001\u0000\u0000\u0000\u00a7\u00a8\u0005\f\u0000\u0000\u00a8\u001b\u0001"+
		"\u0000\u0000\u0000\u00a9\u00b2\u0003\u0010\b\u0000\u00aa\u00b2\u0003&"+
		"\u0013\u0000\u00ab\u00b2\u0003$\u0012\u0000\u00ac\u00b2\u0003\"\u0011"+
		"\u0000\u00ad\u00b2\u0003\u001e\u000f\u0000\u00ae\u00b2\u0003\b\u0004\u0000"+
		"\u00af\u00b2\u0003\f\u0006\u0000\u00b0\u00b2\u0003 \u0010\u0000\u00b1"+
		"\u00a9\u0001\u0000\u0000\u0000\u00b1\u00aa\u0001\u0000\u0000\u0000\u00b1"+
		"\u00ab\u0001\u0000\u0000\u0000\u00b1\u00ac\u0001\u0000\u0000\u0000\u00b1"+
		"\u00ad\u0001\u0000\u0000\u0000\u00b1\u00ae\u0001\u0000\u0000\u0000\u00b1"+
		"\u00af\u0001\u0000\u0000\u0000\u00b1\u00b0\u0001\u0000\u0000\u0000\u00b2"+
		"\u001d\u0001\u0000\u0000\u0000\u00b3\u00b4\u0003*\u0015\u0000\u00b4\u00b7"+
		"\u0005&\u0000\u0000\u00b5\u00b6\u0005\u000f\u0000\u0000\u00b6\u00b8\u0003"+
		",\u0016\u0000\u00b7\u00b5\u0001\u0000\u0000\u0000\u00b7\u00b8\u0001\u0000"+
		"\u0000\u0000\u00b8\u00ba\u0001\u0000\u0000\u0000\u00b9\u00bb\u0005\r\u0000"+
		"\u0000\u00ba\u00b9\u0001\u0000\u0000\u0000\u00ba\u00bb\u0001\u0000\u0000"+
		"\u0000\u00bb\u001f\u0001\u0000\u0000\u0000\u00bc\u00bd\u0005&\u0000\u0000"+
		"\u00bd\u00bf\u0005\t\u0000\u0000\u00be\u00c0\u0003(\u0014\u0000\u00bf"+
		"\u00be\u0001\u0000\u0000\u0000\u00bf\u00c0\u0001\u0000\u0000\u0000\u00c0"+
		"\u00c1\u0001\u0000\u0000\u0000\u00c1\u00c3\u0005\n\u0000\u0000\u00c2\u00c4"+
		"\u0005\r\u0000\u0000\u00c3\u00c2\u0001\u0000\u0000\u0000\u00c3\u00c4\u0001"+
		"\u0000\u0000\u0000\u00c4!\u0001\u0000\u0000\u0000\u00c5\u00c6\u0005&\u0000"+
		"\u0000\u00c6\u00c7\u0005\u0013\u0000\u0000\u00c7\u00c8\u0005&\u0000\u0000"+
		"\u00c8\u00ca\u0005\t\u0000\u0000\u00c9\u00cb\u0003(\u0014\u0000\u00ca"+
		"\u00c9\u0001\u0000\u0000\u0000\u00ca\u00cb\u0001\u0000\u0000\u0000\u00cb"+
		"\u00cc\u0001\u0000\u0000\u0000\u00cc\u00ce\u0005\n\u0000\u0000\u00cd\u00cf"+
		"\u0005\r\u0000\u0000\u00ce\u00cd\u0001\u0000\u0000\u0000\u00ce\u00cf\u0001"+
		"\u0000\u0000\u0000\u00cf#\u0001\u0000\u0000\u0000\u00d0\u00d1\u0005&\u0000"+
		"\u0000\u00d1\u00d3\u0005\t\u0000\u0000\u00d2\u00d4\u0003(\u0014\u0000"+
		"\u00d3\u00d2\u0001\u0000\u0000\u0000\u00d3\u00d4\u0001\u0000\u0000\u0000"+
		"\u00d4\u00d5\u0001\u0000\u0000\u0000\u00d5\u00d6\u0005\n\u0000\u0000\u00d6"+
		"\u00d7\u0003\u001a\r\u0000\u00d7%\u0001\u0000\u0000\u0000\u00d8\u00d9"+
		"\u0005\u0005\u0000\u0000\u00d9\u00da\u0005&\u0000\u0000\u00da\u00dc\u0005"+
		"\t\u0000\u0000\u00db\u00dd\u0003(\u0014\u0000\u00dc\u00db\u0001\u0000"+
		"\u0000\u0000\u00dc\u00dd\u0001\u0000\u0000\u0000\u00dd\u00de\u0001\u0000"+
		"\u0000\u0000\u00de\u00e0\u0005\n\u0000\u0000\u00df\u00e1\u0005\r\u0000"+
		"\u0000\u00e0\u00df\u0001\u0000\u0000\u0000\u00e0\u00e1\u0001\u0000\u0000"+
		"\u0000\u00e1\'\u0001\u0000\u0000\u0000\u00e2\u00e7\u0003,\u0016\u0000"+
		"\u00e3\u00e4\u0005\u000e\u0000\u0000\u00e4\u00e6\u0003,\u0016\u0000\u00e5"+
		"\u00e3\u0001\u0000\u0000\u0000\u00e6\u00e9\u0001\u0000\u0000\u0000\u00e7"+
		"\u00e5\u0001\u0000\u0000\u0000\u00e7\u00e8\u0001\u0000\u0000\u0000\u00e8"+
		")\u0001\u0000\u0000\u0000\u00e9\u00e7\u0001\u0000\u0000\u0000\u00ea\u00eb"+
		"\u0005&\u0000\u0000\u00eb+\u0001\u0000\u0000\u0000\u00ec\u00ed\u0006\u0016"+
		"\uffff\uffff\u0000\u00ed\u00ee\u0005\t\u0000\u0000\u00ee\u00ef\u0003,"+
		"\u0016\u0000\u00ef\u00f0\u0005\n\u0000\u0000\u00f0\u0107\u0001\u0000\u0000"+
		"\u0000\u00f1\u00f2\u0003.\u0017\u0000\u00f2\u00f4\u0005\t\u0000\u0000"+
		"\u00f3\u00f5\u0003(\u0014\u0000\u00f4\u00f3\u0001\u0000\u0000\u0000\u00f4"+
		"\u00f5\u0001\u0000\u0000\u0000\u00f5\u00f6\u0001\u0000\u0000\u0000\u00f6"+
		"\u00f7\u0005\n\u0000\u0000\u00f7\u0107\u0001\u0000\u0000\u0000\u00f8\u0107"+
		"\u0003.\u0017\u0000\u00f9\u0107\u0005(\u0000\u0000\u00fa\u0107\u0005)"+
		"\u0000\u0000\u00fb\u0107\u0005%\u0000\u0000\u00fc\u0107\u00030\u0018\u0000"+
		"\u00fd\u00fe\u0005&\u0000\u0000\u00fe\u00ff\u0005\u000f\u0000\u0000\u00ff"+
		"\u0107\u0003,\u0016\f\u0100\u0101\u0005\u001c\u0000\u0000\u0101\u0107"+
		"\u0003,\u0016\u000b\u0102\u0103\u0005\u0006\u0000\u0000\u0103\u0107\u0003"+
		",\u0016\n\u0104\u0105\u0005\u0015\u0000\u0000\u0105\u0107\u0003,\u0016"+
		"\t\u0106\u00ec\u0001\u0000\u0000\u0000\u0106\u00f1\u0001\u0000\u0000\u0000"+
		"\u0106\u00f8\u0001\u0000\u0000\u0000\u0106\u00f9\u0001\u0000\u0000\u0000"+
		"\u0106\u00fa\u0001\u0000\u0000\u0000\u0106\u00fb\u0001\u0000\u0000\u0000"+
		"\u0106\u00fc\u0001\u0000\u0000\u0000\u0106\u00fd\u0001\u0000\u0000\u0000"+
		"\u0106\u0100\u0001\u0000\u0000\u0000\u0106\u0102\u0001\u0000\u0000\u0000"+
		"\u0106\u0104\u0001\u0000\u0000\u0000\u0107\u0125\u0001\u0000\u0000\u0000"+
		"\u0108\u0109\n\b\u0000\u0000\u0109\u010a\u0007\u0000\u0000\u0000\u010a"+
		"\u0124\u0003,\u0016\t\u010b\u010c\n\u0007\u0000\u0000\u010c\u010d\u0007"+
		"\u0001\u0000\u0000\u010d\u0124\u0003,\u0016\b\u010e\u010f\n\u0006\u0000"+
		"\u0000\u010f\u0110\u0005\u001a\u0000\u0000\u0110\u0124\u0003,\u0016\u0007"+
		"\u0111\u0112\n\u0005\u0000\u0000\u0112\u0113\u0005\u001b\u0000\u0000\u0113"+
		"\u0124\u0003,\u0016\u0006\u0114\u0115\n\u0004\u0000\u0000\u0115\u0116"+
		"\u0007\u0002\u0000\u0000\u0116\u0124\u0003,\u0016\u0005\u0117\u0118\n"+
		"\u0003\u0000\u0000\u0118\u0119\u0005\u0018\u0000\u0000\u0119\u0124\u0003"+
		",\u0016\u0004\u011a\u011b\n\u0002\u0000\u0000\u011b\u011c\u0005\u0019"+
		"\u0000\u0000\u011c\u0124\u0003,\u0016\u0003\u011d\u011e\n\u0001\u0000"+
		"\u0000\u011e\u011f\u0005\u001d\u0000\u0000\u011f\u0120\u0003,\u0016\u0000"+
		"\u0120\u0121\u0005\u001e\u0000\u0000\u0121\u0122\u0003,\u0016\u0002\u0122"+
		"\u0124\u0001\u0000\u0000\u0000\u0123\u0108\u0001\u0000\u0000\u0000\u0123"+
		"\u010b\u0001\u0000\u0000\u0000\u0123\u010e\u0001\u0000\u0000\u0000\u0123"+
		"\u0111\u0001\u0000\u0000\u0000\u0123\u0114\u0001\u0000\u0000\u0000\u0123"+
		"\u0117\u0001\u0000\u0000\u0000\u0123\u011a\u0001\u0000\u0000\u0000\u0123"+
		"\u011d\u0001\u0000\u0000\u0000\u0124\u0127\u0001\u0000\u0000\u0000\u0125"+
		"\u0123\u0001\u0000\u0000\u0000\u0125\u0126\u0001\u0000\u0000\u0000\u0126"+
		"-\u0001\u0000\u0000\u0000\u0127\u0125\u0001\u0000\u0000\u0000\u0128\u012d"+
		"\u0005&\u0000\u0000\u0129\u012a\u0005\u0012\u0000\u0000\u012a\u012c\u0005"+
		"&\u0000\u0000\u012b\u0129\u0001\u0000\u0000\u0000\u012c\u012f\u0001\u0000"+
		"\u0000\u0000\u012d\u012b\u0001\u0000\u0000\u0000\u012d\u012e\u0001\u0000"+
		"\u0000\u0000\u012e/\u0001\u0000\u0000\u0000\u012f\u012d\u0001\u0000\u0000"+
		"\u0000\u0130\u0132\u0005\u0010\u0000\u0000\u0131\u0133\u0003(\u0014\u0000"+
		"\u0132\u0131\u0001\u0000\u0000\u0000\u0132\u0133\u0001\u0000\u0000\u0000"+
		"\u0133\u0134\u0001\u0000\u0000\u0000\u0134\u0135\u0005\u0011\u0000\u0000"+
		"\u01351\u0001\u0000\u0000\u0000!5DIQW]ahn{\u007f\u008b\u008e\u0097\u009e"+
		"\u00a4\u00b1\u00b7\u00ba\u00bf\u00c3\u00ca\u00ce\u00d3\u00dc\u00e0\u00e7"+
		"\u00f4\u0106\u0123\u0125\u012d\u0132";
	private static final String[] _LITERAL_NAMES = makeLiteralNames();

	private static String[] makeRuleNames() {
		return new String[] {
			"file", "definition", "importDecl", "exportDecl", "externDecl", "directiveCall",
			"macroDecl", "subDecl", "logicControlDecl", "elseIfClause", "elseClause",
			"paramList", "param", "functionBlock", "statements", "variableDecl",
			"funtionCallDecl", "methodReferenceFunCallDecl", "funtionBlockDecl",
			"useDecl", "valueList", "type", "expression", "fieldAccess", "arrayValue"
		};
	}

	private static String[] makeLiteralNames() {
		return new String[] {
			null, "'import'", "'extern'", "'export'", "'sub'", "'use'", "'!'", null,
			null, "'('", "')'", "'{'", "'}'", "';'", "','", "'='", "'['", "']'",
			"'.'", "'::'", "'+'", "'-'", "'*'", "'/'", "'&&'", "'||'", "'|'", "'&'",
			"'~'", "'?'", "':'", "'>'", "'<'", "'=='", "'!='", "'>='", "'<='"
		};
	}
	private static final String[] _SYMBOLIC_NAMES = makeSymbolicNames();
	public static final Vocabulary VOCABULARY = new VocabularyImpl(_LITERAL_NAMES, _SYMBOLIC_NAMES);

	/**
	 * @deprecated Use {@link #VOCABULARY} instead.
	 */
	@Deprecated
	public static final String[] tokenNames;
	static {
		tokenNames = new String[_SYMBOLIC_NAMES.length];
		for (int i = 0; i < tokenNames.length; i++) {
			tokenNames[i] = VOCABULARY.getLiteralName(i);
			if (tokenNames[i] == null) {
				tokenNames[i] = VOCABULARY.getSymbolicName(i);
			}

			if (tokenNames[i] == null) {
				tokenNames[i] = "<INVALID>";
			}
		}
	}

	@Override
	@Deprecated
	public String[] getTokenNames() {
		return tokenNames;
	}

	@Override

	public Vocabulary getVocabulary() {
		return VOCABULARY;
	}

	@Override
	public String getGrammarFileName() { return "TechlandScript.g4"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public ATN getATN() { return _ATN; }

	public TechlandScriptParser(TokenStream input) {
		super(input);
		_interp = new ParserATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	private static String[] makeSymbolicNames() {
		return new String[] {
			null, "Import", "Extern", "Export", "Sub", "Use", "Exclamation", "KwIf",
			"KwElse", "LParen", "RParen", "LBrace", "RBrace", "Semicolon", "Comma",
			"Equals", "LBracket", "RBracket", "Dot", "DoubleColon", "Plus", "Minus",
			"Mul", "Div", "LogicAnd", "LogicOr", "BitOr", "BitAnd", "BitNot", "Question",
			"Colon", "Gt", "Lt", "Eq", "NotEq", "Gte", "Lte", "Bool", "Id", "MacroId",
			"Number", "String", "LineComment", "BlockComment", "WhiteSpaces"
		};
	}

	public final FileContext file() throws RecognitionException {
		FileContext _localctx = new FileContext(_ctx, getState());
		enterRule(_localctx, 0, RULE_file);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(53);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while ((((_la) & ~0x3f) == 0 && ((1L << _la) & 824633720926L) != 0)) {
				{
				{
				setState(50);
				definition();
				}
				}
				setState(55);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(56);
			match(EOF);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public final DefinitionContext definition() throws RecognitionException {
		DefinitionContext _localctx = new DefinitionContext(_ctx, getState());
		enterRule(_localctx, 2, RULE_definition);
		try {
			setState(68);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,1,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(58);
				importDecl();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(59);
				exportDecl();
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(60);
				externDecl();
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(61);
				directiveCall();
				}
				break;
			case 5:
				enterOuterAlt(_localctx, 5);
				{
				setState(62);
				macroDecl();
				}
				break;
			case 6:
				enterOuterAlt(_localctx, 6);
				{
				setState(63);
				subDecl();
				}
				break;
			case 7:
				enterOuterAlt(_localctx, 7);
				{
				setState(64);
				variableDecl();
				}
				break;
			case 8:
				enterOuterAlt(_localctx, 8);
				{
				setState(65);
				funtionBlockDecl();
				}
				break;
			case 9:
				enterOuterAlt(_localctx, 9);
				{
				setState(66);
				methodReferenceFunCallDecl();
				}
				break;
			case 10:
				enterOuterAlt(_localctx, 10);
				{
				setState(67);
				funtionCallDecl();
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public final LogicControlDeclContext logicControlDecl() throws RecognitionException {
		LogicControlDeclContext _localctx = new LogicControlDeclContext(_ctx, getState());
		enterRule(_localctx, 16, RULE_logicControlDecl);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(115);
			match(KwIf);
			setState(116);
			match(LParen);
			setState(117);
			expression(0);
			setState(118);
			match(RParen);
			setState(119);
			functionBlock();
			setState(123);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,9,_ctx);
			while ( _alt!=2 && _alt!= ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(120);
					elseIfClause();
					}
					}
				}
				setState(125);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,9,_ctx);
			}
			setState(127);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==KwElse) {
				{
				setState(126);
				elseClause();
				}
			}

			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public final StatementsContext statements() throws RecognitionException {
		StatementsContext _localctx = new StatementsContext(_ctx, getState());
		enterRule(_localctx, 28, RULE_statements);
		try {
			setState(177);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,16,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(169);
				logicControlDecl();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(170);
				useDecl();
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(171);
				funtionBlockDecl();
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(172);
				methodReferenceFunCallDecl();
				}
				break;
			case 5:
				enterOuterAlt(_localctx, 5);
				{
				setState(173);
				variableDecl();
				}
				break;
			case 6:
				enterOuterAlt(_localctx, 6);
				{
				setState(174);
				externDecl();
				}
				break;
			case 7:
				enterOuterAlt(_localctx, 7);
				{
				setState(175);
				macroDecl();
				}
				break;
			case 8:
				enterOuterAlt(_localctx, 8);
				{
				setState(176);
				funtionCallDecl();
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public final ImportDeclContext importDecl() throws RecognitionException {
		ImportDeclContext _localctx = new ImportDeclContext(_ctx, getState());
		enterRule(_localctx, 4, RULE_importDecl);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(70);
			match(Import);
			setState(71);
			match(String);
			setState(73);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==Semicolon) {
				{
				setState(72);
				match(Semicolon);
				}
			}

			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public final MethodReferenceFunCallDeclContext methodReferenceFunCallDecl() throws RecognitionException {
		MethodReferenceFunCallDeclContext _localctx = new MethodReferenceFunCallDeclContext(_ctx, getState());
		enterRule(_localctx, 34, RULE_methodReferenceFunCallDecl);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(197);
			match(Id);
			setState(198);
			match(DoubleColon);
			setState(199);
			match(Id);
			setState(200);
			match(LParen);
			setState(202);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if ((((_la) & ~0x3f) == 0 && ((1L << _la) & 3711122342464L) != 0)) {
				{
				setState(201);
				valueList();
				}
			}

			setState(204);
			match(RParen);
			setState(206);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==Semicolon) {
				{
				setState(205);
				match(Semicolon);
				}
			}

			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public final ExportDeclContext exportDecl() throws RecognitionException {
		ExportDeclContext _localctx = new ExportDeclContext(_ctx, getState());
		enterRule(_localctx, 6, RULE_exportDecl);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(75);
			match(Export);
			setState(76);
			type();
			setState(77);
			match(Id);
			setState(78);
			match(Equals);
			setState(79);
			expression(0);
			setState(81);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==Semicolon) {
				{
				setState(80);
				match(Semicolon);
				}
			}

			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public final ExpressionContext expression() throws RecognitionException {
		return expression(0);
	}

	public final ExternDeclContext externDecl() throws RecognitionException {
		ExternDeclContext _localctx = new ExternDeclContext(_ctx, getState());
		enterRule(_localctx, 8, RULE_externDecl);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(83);
			match(Extern);
			setState(84);
			type();
			setState(85);
			match(Id);
			setState(87);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==Semicolon) {
				{
				setState(86);
				match(Semicolon);
				}
			}

			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	private ExpressionContext expression(int _p) throws RecognitionException {
		ParserRuleContext _parentctx = _ctx;
		int _parentState = getState();
		ExpressionContext _localctx = new ExpressionContext(_ctx, _parentState);
		ExpressionContext _prevctx = _localctx;
		int _startState = 44;
		enterRecursionRule(_localctx, 44, RULE_expression, _p);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(262);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,28,_ctx) ) {
			case 1:
				{
				setState(237);
				match(LParen);
				setState(238);
				expression(0);
				setState(239);
				match(RParen);
				}
				break;
			case 2:
				{
				setState(241);
				fieldAccess();
				setState(242);
				match(LParen);
				setState(244);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if ((((_la) & ~0x3f) == 0 && ((1L << _la) & 3711122342464L) != 0)) {
					{
					setState(243);
					valueList();
					}
				}

				setState(246);
				match(RParen);
				}
				break;
			case 3:
				{
				setState(248);
				fieldAccess();
				}
				break;
			case 4:
				{
				setState(249);
				match(Number);
				}
				break;
			case 5:
				{
				setState(250);
				match(String);
				}
				break;
			case 6:
				{
				setState(251);
				match(Bool);
				}
				break;
			case 7:
				{
				setState(252);
				arrayValue();
				}
				break;
			case 8:
				{
				setState(253);
				match(Id);
				setState(254);
				match(Equals);
				setState(255);
				expression(12);
				}
				break;
			case 9:
				{
				setState(256);
				match(BitNot);
				setState(257);
				expression(11);
				}
				break;
			case 10:
				{
				setState(258);
				match(Exclamation);
				setState(259);
				expression(10);
				}
				break;
			case 11:
				{
				setState(260);
				match(Minus);
				setState(261);
				expression(9);
				}
				break;
			}
			_ctx.stop = _input.LT(-1);
			setState(293);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,30,_ctx);
			while ( _alt!=2 && _alt!= ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					if ( _parseListeners!=null ) triggerExitRuleEvent();
					_prevctx = _localctx;
					{
					setState(291);
					_errHandler.sync(this);
					switch ( getInterpreter().adaptivePredict(_input,29,_ctx) ) {
					case 1:
						{
						_localctx = new ExpressionContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(264);
						if (!(precpred(_ctx, 8))) throw new FailedPredicateException(this, "precpred(_ctx, 8)");
						setState(265);
						_la = _input.LA(1);
						if ( !(_la==Mul || _la==Div) ) {
						_errHandler.recoverInline(this);
						}
						else {
							if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
							_errHandler.reportMatch(this);
							consume();
						}
						setState(266);
						expression(9);
						}
						break;
					case 2:
						{
						_localctx = new ExpressionContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(267);
						if (!(precpred(_ctx, 7))) throw new FailedPredicateException(this, "precpred(_ctx, 7)");
						setState(268);
						_la = _input.LA(1);
						if ( !(_la==Plus || _la==Minus) ) {
						_errHandler.recoverInline(this);
						}
						else {
							if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
							_errHandler.reportMatch(this);
							consume();
						}
						setState(269);
						expression(8);
						}
						break;
					case 3:
						{
						_localctx = new ExpressionContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(270);
						if (!(precpred(_ctx, 6))) throw new FailedPredicateException(this, "precpred(_ctx, 6)");
						setState(271);
						match(BitOr);
						setState(272);
						expression(7);
						}
						break;
					case 4:
						{
						_localctx = new ExpressionContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(273);
						if (!(precpred(_ctx, 5))) throw new FailedPredicateException(this, "precpred(_ctx, 5)");
						setState(274);
						match(BitAnd);
						setState(275);
						expression(6);
						}
						break;
					case 5:
						{
						_localctx = new ExpressionContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(276);
						if (!(precpred(_ctx, 4))) throw new FailedPredicateException(this, "precpred(_ctx, 4)");
						setState(277);
						_la = _input.LA(1);
						if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & 135291469824L) != 0)) ) {
						_errHandler.recoverInline(this);
						}
						else {
							if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
							_errHandler.reportMatch(this);
							consume();
						}
						setState(278);
						expression(5);
						}
						break;
					case 6:
						{
						_localctx = new ExpressionContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(279);
						if (!(precpred(_ctx, 3))) throw new FailedPredicateException(this, "precpred(_ctx, 3)");
						setState(280);
						match(LogicAnd);
						setState(281);
						expression(4);
						}
						break;
					case 7:
						{
						_localctx = new ExpressionContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(282);
						if (!(precpred(_ctx, 2))) throw new FailedPredicateException(this, "precpred(_ctx, 2)");
						setState(283);
						match(LogicOr);
						setState(284);
						expression(3);
						}
						break;
					case 8:
						{
						_localctx = new ExpressionContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(285);
						if (!(precpred(_ctx, 1))) throw new FailedPredicateException(this, "precpred(_ctx, 1)");
						setState(286);
						match(Question);
						setState(287);
						expression(0);
						setState(288);
						match(Colon);
						setState(289);
						expression(2);
						}
						break;
					}
					}
				}
				setState(295);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,30,_ctx);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			unrollRecursionContexts(_parentctx);
		}
		return _localctx;
	}

	public final DirectiveCallContext directiveCall() throws RecognitionException {
		DirectiveCallContext _localctx = new DirectiveCallContext(_ctx, getState());
		enterRule(_localctx, 10, RULE_directiveCall);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(89);
			match(Exclamation);
			setState(90);
			match(Id);
			setState(91);
			match(LParen);
			setState(93);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if ((((_la) & ~0x3f) == 0 && ((1L << _la) & 3711122342464L) != 0)) {
				{
				setState(92);
				valueList();
				}
			}

			setState(95);
			match(RParen);
			setState(97);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==Semicolon) {
				{
				setState(96);
				match(Semicolon);
				}
			}

			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public final FieldAccessContext fieldAccess() throws RecognitionException {
		FieldAccessContext _localctx = new FieldAccessContext(_ctx, getState());
		enterRule(_localctx, 46, RULE_fieldAccess);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(296);
			match(Id);
			setState(301);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,31,_ctx);
			while ( _alt!=2 && _alt!= ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(297);
					match(Dot);
					setState(298);
					match(Id);
					}
					}
				}
				setState(303);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,31,_ctx);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public final MacroDeclContext macroDecl() throws RecognitionException {
		MacroDeclContext _localctx = new MacroDeclContext(_ctx, getState());
		enterRule(_localctx, 12, RULE_macroDecl);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(99);
			match(MacroId);
			setState(100);
			match(LParen);
			setState(101);
			valueList();
			setState(102);
			match(RParen);
			setState(104);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==Semicolon) {
				{
				setState(103);
				match(Semicolon);
				}
			}

			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class FileContext extends ParserRuleContext {
		public FileContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}

		public TerminalNode EOF() { return getToken(TechlandScriptParser.EOF, 0); }

		public List<DefinitionContext> definition() {
			return getRuleContexts(DefinitionContext.class);
		}

		public DefinitionContext definition(int i) {
			return getRuleContext(DefinitionContext.class,i);
		}

		@Override public int getRuleIndex() { return RULE_file; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TechlandScriptListener ) ((TechlandScriptListener)listener).enterFile(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TechlandScriptListener ) ((TechlandScriptListener)listener).exitFile(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof TechlandScriptVisitor ) return ((TechlandScriptVisitor<? extends T>)visitor).visitFile(this);
			else return visitor.visitChildren(this);
		}
	}

	public final SubDeclContext subDecl() throws RecognitionException {
		SubDeclContext _localctx = new SubDeclContext(_ctx, getState());
		enterRule(_localctx, 14, RULE_subDecl);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(106);
			match(Sub);
			setState(107);
			match(Id);
			setState(108);
			match(LParen);
			setState(110);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==Id) {
				{
				setState(109);
				paramList();
				}
			}

			setState(112);
			match(RParen);
			setState(113);
			functionBlock();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class DefinitionContext extends ParserRuleContext {
		public DefinitionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}

		public ImportDeclContext importDecl() {
			return getRuleContext(ImportDeclContext.class,0);
		}

		public ExportDeclContext exportDecl() {
			return getRuleContext(ExportDeclContext.class,0);
		}

		public ExternDeclContext externDecl() {
			return getRuleContext(ExternDeclContext.class,0);
		}

		public DirectiveCallContext directiveCall() {
			return getRuleContext(DirectiveCallContext.class,0);
		}

		public MacroDeclContext macroDecl() {
			return getRuleContext(MacroDeclContext.class,0);
		}

		public SubDeclContext subDecl() {
			return getRuleContext(SubDeclContext.class,0);
		}

		public VariableDeclContext variableDecl() {
			return getRuleContext(VariableDeclContext.class,0);
		}

		public FuntionBlockDeclContext funtionBlockDecl() {
			return getRuleContext(FuntionBlockDeclContext.class,0);
		}

		public MethodReferenceFunCallDeclContext methodReferenceFunCallDecl() {
			return getRuleContext(MethodReferenceFunCallDeclContext.class,0);
		}

		public FuntionCallDeclContext funtionCallDecl() {
			return getRuleContext(FuntionCallDeclContext.class,0);
		}

		@Override public int getRuleIndex() { return RULE_definition; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TechlandScriptListener ) ((TechlandScriptListener)listener).enterDefinition(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TechlandScriptListener ) ((TechlandScriptListener)listener).exitDefinition(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof TechlandScriptVisitor ) return ((TechlandScriptVisitor<? extends T>)visitor).visitDefinition(this);
			else return visitor.visitChildren(this);
		}
	}

	@SuppressWarnings("CheckReturnValue")
	public static class ImportDeclContext extends ParserRuleContext {
		public ImportDeclContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}

		public TerminalNode Import() { return getToken(TechlandScriptParser.Import, 0); }

		public TerminalNode String() { return getToken(TechlandScriptParser.String, 0); }

		public TerminalNode Semicolon() { return getToken(TechlandScriptParser.Semicolon, 0); }

		@Override public int getRuleIndex() { return RULE_importDecl; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TechlandScriptListener ) ((TechlandScriptListener)listener).enterImportDecl(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TechlandScriptListener ) ((TechlandScriptListener)listener).exitImportDecl(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof TechlandScriptVisitor ) return ((TechlandScriptVisitor<? extends T>)visitor).visitImportDecl(this);
			else return visitor.visitChildren(this);
		}
	}

	@SuppressWarnings("CheckReturnValue")
	public static class ExportDeclContext extends ParserRuleContext {
		public ExportDeclContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}

		public TerminalNode Export() { return getToken(TechlandScriptParser.Export, 0); }

		public TypeContext type() {
			return getRuleContext(TypeContext.class,0);
		}

		public TerminalNode Id() { return getToken(TechlandScriptParser.Id, 0); }

		public TerminalNode Equals() { return getToken(TechlandScriptParser.Equals, 0); }

		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}

		public TerminalNode Semicolon() { return getToken(TechlandScriptParser.Semicolon, 0); }

		@Override public int getRuleIndex() { return RULE_exportDecl; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TechlandScriptListener ) ((TechlandScriptListener)listener).enterExportDecl(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TechlandScriptListener ) ((TechlandScriptListener)listener).exitExportDecl(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof TechlandScriptVisitor ) return ((TechlandScriptVisitor<? extends T>)visitor).visitExportDecl(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ElseIfClauseContext elseIfClause() throws RecognitionException {
		ElseIfClauseContext _localctx = new ElseIfClauseContext(_ctx, getState());
		enterRule(_localctx, 18, RULE_elseIfClause);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(129);
			match(KwElse);
			setState(130);
			match(KwIf);
			setState(131);
			match(LParen);
			setState(132);
			expression(0);
			setState(133);
			match(RParen);
			setState(134);
			functionBlock();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class ExternDeclContext extends ParserRuleContext {
		public ExternDeclContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}

		public TerminalNode Extern() { return getToken(TechlandScriptParser.Extern, 0); }

		public TypeContext type() {
			return getRuleContext(TypeContext.class,0);
		}

		public TerminalNode Id() { return getToken(TechlandScriptParser.Id, 0); }

		public TerminalNode Semicolon() { return getToken(TechlandScriptParser.Semicolon, 0); }

		@Override public int getRuleIndex() { return RULE_externDecl; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TechlandScriptListener ) ((TechlandScriptListener)listener).enterExternDecl(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TechlandScriptListener ) ((TechlandScriptListener)listener).exitExternDecl(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof TechlandScriptVisitor ) return ((TechlandScriptVisitor<? extends T>)visitor).visitExternDecl(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ElseClauseContext elseClause() throws RecognitionException {
		ElseClauseContext _localctx = new ElseClauseContext(_ctx, getState());
		enterRule(_localctx, 20, RULE_elseClause);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(136);
			match(KwElse);
			setState(142);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==LParen) {
				{
				setState(137);
				match(LParen);
				setState(139);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if ((((_la) & ~0x3f) == 0 && ((1L << _la) & 3711122342464L) != 0)) {
					{
					setState(138);
					expression(0);
					}
				}

				setState(141);
				match(RParen);
				}
			}

			setState(144);
			functionBlock();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class DirectiveCallContext extends ParserRuleContext {
		public DirectiveCallContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}

		public TerminalNode Exclamation() { return getToken(TechlandScriptParser.Exclamation, 0); }

		public TerminalNode Id() { return getToken(TechlandScriptParser.Id, 0); }

		public TerminalNode LParen() { return getToken(TechlandScriptParser.LParen, 0); }

		public TerminalNode RParen() { return getToken(TechlandScriptParser.RParen, 0); }

		public ValueListContext valueList() {
			return getRuleContext(ValueListContext.class,0);
		}

		public TerminalNode Semicolon() { return getToken(TechlandScriptParser.Semicolon, 0); }

		@Override public int getRuleIndex() { return RULE_directiveCall; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TechlandScriptListener ) ((TechlandScriptListener)listener).enterDirectiveCall(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TechlandScriptListener ) ((TechlandScriptListener)listener).exitDirectiveCall(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof TechlandScriptVisitor ) return ((TechlandScriptVisitor<? extends T>)visitor).visitDirectiveCall(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ParamListContext paramList() throws RecognitionException {
		ParamListContext _localctx = new ParamListContext(_ctx, getState());
		enterRule(_localctx, 22, RULE_paramList);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(146);
			param();
			setState(151);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==Comma) {
				{
				{
				setState(147);
				match(Comma);
				setState(148);
				param();
				}
				}
				setState(153);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class MacroDeclContext extends ParserRuleContext {
		public MacroDeclContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}

		public TerminalNode MacroId() { return getToken(TechlandScriptParser.MacroId, 0); }

		public TerminalNode LParen() { return getToken(TechlandScriptParser.LParen, 0); }

		public ValueListContext valueList() {
			return getRuleContext(ValueListContext.class,0);
		}

		public TerminalNode RParen() { return getToken(TechlandScriptParser.RParen, 0); }

		public TerminalNode Semicolon() { return getToken(TechlandScriptParser.Semicolon, 0); }

		@Override public int getRuleIndex() { return RULE_macroDecl; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TechlandScriptListener ) ((TechlandScriptListener)listener).enterMacroDecl(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TechlandScriptListener ) ((TechlandScriptListener)listener).exitMacroDecl(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof TechlandScriptVisitor ) return ((TechlandScriptVisitor<? extends T>)visitor).visitMacroDecl(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ParamContext param() throws RecognitionException {
		ParamContext _localctx = new ParamContext(_ctx, getState());
		enterRule(_localctx, 24, RULE_param);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(154);
			type();
			setState(155);
			match(Id);
			setState(158);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==Equals) {
				{
				setState(156);
				match(Equals);
				setState(157);
				expression(0);
				}
			}

			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class SubDeclContext extends ParserRuleContext {
		public SubDeclContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}

		public TerminalNode Sub() { return getToken(TechlandScriptParser.Sub, 0); }

		public TerminalNode Id() { return getToken(TechlandScriptParser.Id, 0); }

		public TerminalNode LParen() { return getToken(TechlandScriptParser.LParen, 0); }

		public TerminalNode RParen() { return getToken(TechlandScriptParser.RParen, 0); }

		public FunctionBlockContext functionBlock() {
			return getRuleContext(FunctionBlockContext.class,0);
		}

		public ParamListContext paramList() {
			return getRuleContext(ParamListContext.class,0);
		}

		@Override public int getRuleIndex() { return RULE_subDecl; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TechlandScriptListener ) ((TechlandScriptListener)listener).enterSubDecl(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TechlandScriptListener ) ((TechlandScriptListener)listener).exitSubDecl(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof TechlandScriptVisitor ) return ((TechlandScriptVisitor<? extends T>)visitor).visitSubDecl(this);
			else return visitor.visitChildren(this);
		}
	}

	public final FunctionBlockContext functionBlock() throws RecognitionException {
		FunctionBlockContext _localctx = new FunctionBlockContext(_ctx, getState());
		enterRule(_localctx, 26, RULE_functionBlock);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(160);
			match(LBrace);
			setState(164);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while ((((_la) & ~0x3f) == 0 && ((1L << _la) & 824633720996L) != 0)) {
				{
				{
				setState(161);
				statements();
				}
				}
				setState(166);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(167);
			match(RBrace);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class LogicControlDeclContext extends ParserRuleContext {
		public LogicControlDeclContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}

		public TerminalNode KwIf() { return getToken(TechlandScriptParser.KwIf, 0); }

		public TerminalNode LParen() { return getToken(TechlandScriptParser.LParen, 0); }

		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}

		public TerminalNode RParen() { return getToken(TechlandScriptParser.RParen, 0); }

		public FunctionBlockContext functionBlock() {
			return getRuleContext(FunctionBlockContext.class,0);
		}

		public List<ElseIfClauseContext> elseIfClause() {
			return getRuleContexts(ElseIfClauseContext.class);
		}

		public ElseIfClauseContext elseIfClause(int i) {
			return getRuleContext(ElseIfClauseContext.class,i);
		}

		public ElseClauseContext elseClause() {
			return getRuleContext(ElseClauseContext.class,0);
		}

		@Override public int getRuleIndex() { return RULE_logicControlDecl; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TechlandScriptListener ) ((TechlandScriptListener)listener).enterLogicControlDecl(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TechlandScriptListener ) ((TechlandScriptListener)listener).exitLogicControlDecl(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof TechlandScriptVisitor ) return ((TechlandScriptVisitor<? extends T>)visitor).visitLogicControlDecl(this);
			else return visitor.visitChildren(this);
		}
	}

	@SuppressWarnings("CheckReturnValue")
	public static class ElseIfClauseContext extends ParserRuleContext {
		public ElseIfClauseContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}

		public TerminalNode KwElse() { return getToken(TechlandScriptParser.KwElse, 0); }

		public TerminalNode KwIf() { return getToken(TechlandScriptParser.KwIf, 0); }
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}

		public TerminalNode LParen() { return getToken(TechlandScriptParser.LParen, 0); }

		public TerminalNode RParen() { return getToken(TechlandScriptParser.RParen, 0); }

		public FunctionBlockContext functionBlock() {
			return getRuleContext(FunctionBlockContext.class,0);
		}

		@Override public int getRuleIndex() { return RULE_elseIfClause; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TechlandScriptListener ) ((TechlandScriptListener)listener).enterElseIfClause(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TechlandScriptListener ) ((TechlandScriptListener)listener).exitElseIfClause(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof TechlandScriptVisitor ) return ((TechlandScriptVisitor<? extends T>)visitor).visitElseIfClause(this);
			else return visitor.visitChildren(this);
		}
	}

	@SuppressWarnings("CheckReturnValue")
	public static class ElseClauseContext extends ParserRuleContext {
		public ElseClauseContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}

		public TerminalNode KwElse() { return getToken(TechlandScriptParser.KwElse, 0); }
		public TerminalNode LParen() { return getToken(TechlandScriptParser.LParen, 0); }
		public TerminalNode RParen() { return getToken(TechlandScriptParser.RParen, 0); }

		public FunctionBlockContext functionBlock() {
			return getRuleContext(FunctionBlockContext.class,0);
		}

		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}

		@Override public int getRuleIndex() { return RULE_elseClause; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TechlandScriptListener ) ((TechlandScriptListener)listener).enterElseClause(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TechlandScriptListener ) ((TechlandScriptListener)listener).exitElseClause(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof TechlandScriptVisitor ) return ((TechlandScriptVisitor<? extends T>)visitor).visitElseClause(this);
			else return visitor.visitChildren(this);
		}
	}

	public final VariableDeclContext variableDecl() throws RecognitionException {
		VariableDeclContext _localctx = new VariableDeclContext(_ctx, getState());
		enterRule(_localctx, 30, RULE_variableDecl);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(179);
			type();
			setState(180);
			match(Id);
			setState(183);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==Equals) {
				{
				setState(181);
				match(Equals);
				setState(182);
				expression(0);
				}
			}

			setState(186);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==Semicolon) {
				{
				setState(185);
				match(Semicolon);
				}
			}

			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class ParamListContext extends ParserRuleContext {
		public ParamListContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}

		public List<ParamContext> param() {
			return getRuleContexts(ParamContext.class);
		}

		public ParamContext param(int i) {
			return getRuleContext(ParamContext.class,i);
		}

		public List<TerminalNode> Comma() { return getTokens(TechlandScriptParser.Comma); }

		public TerminalNode Comma(int i) {
			return getToken(TechlandScriptParser.Comma, i);
		}

		@Override public int getRuleIndex() { return RULE_paramList; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TechlandScriptListener ) ((TechlandScriptListener)listener).enterParamList(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TechlandScriptListener ) ((TechlandScriptListener)listener).exitParamList(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof TechlandScriptVisitor ) return ((TechlandScriptVisitor<? extends T>)visitor).visitParamList(this);
			else return visitor.visitChildren(this);
		}
	}

	public final FuntionCallDeclContext funtionCallDecl() throws RecognitionException {
		FuntionCallDeclContext _localctx = new FuntionCallDeclContext(_ctx, getState());
		enterRule(_localctx, 32, RULE_funtionCallDecl);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(188);
			match(Id);
			setState(189);
			match(LParen);
			setState(191);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if ((((_la) & ~0x3f) == 0 && ((1L << _la) & 3711122342464L) != 0)) {
				{
				setState(190);
				valueList();
				}
			}

			setState(193);
			match(RParen);
			setState(195);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==Semicolon) {
				{
				setState(194);
				match(Semicolon);
				}
			}

			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class ParamContext extends ParserRuleContext {
		public ParamContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		public TerminalNode Id() { return getToken(TechlandScriptParser.Id, 0); }

		public TypeContext type() {
			return getRuleContext(TypeContext.class,0);
		}

		public TerminalNode Equals() { return getToken(TechlandScriptParser.Equals, 0); }

		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}

		@Override public int getRuleIndex() { return RULE_param; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TechlandScriptListener ) ((TechlandScriptListener)listener).enterParam(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TechlandScriptListener ) ((TechlandScriptListener)listener).exitParam(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof TechlandScriptVisitor ) return ((TechlandScriptVisitor<? extends T>)visitor).visitParam(this);
			else return visitor.visitChildren(this);
		}
	}

	@SuppressWarnings("CheckReturnValue")
	public static class FunctionBlockContext extends ParserRuleContext {
		public FunctionBlockContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}

		public TerminalNode LBrace() { return getToken(TechlandScriptParser.LBrace, 0); }

		public TerminalNode RBrace() { return getToken(TechlandScriptParser.RBrace, 0); }

		public List<StatementsContext> statements() {
			return getRuleContexts(StatementsContext.class);
		}

		public StatementsContext statements(int i) {
			return getRuleContext(StatementsContext.class,i);
		}

		@Override public int getRuleIndex() { return RULE_functionBlock; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TechlandScriptListener ) ((TechlandScriptListener)listener).enterFunctionBlock(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TechlandScriptListener ) ((TechlandScriptListener)listener).exitFunctionBlock(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof TechlandScriptVisitor ) return ((TechlandScriptVisitor<? extends T>)visitor).visitFunctionBlock(this);
			else return visitor.visitChildren(this);
		}
	}

	@SuppressWarnings("CheckReturnValue")
	public static class StatementsContext extends ParserRuleContext {
		public StatementsContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}

		public LogicControlDeclContext logicControlDecl() {
			return getRuleContext(LogicControlDeclContext.class,0);
		}

		public UseDeclContext useDecl() {
			return getRuleContext(UseDeclContext.class,0);
		}

		public FuntionBlockDeclContext funtionBlockDecl() {
			return getRuleContext(FuntionBlockDeclContext.class,0);
		}

		public MethodReferenceFunCallDeclContext methodReferenceFunCallDecl() {
			return getRuleContext(MethodReferenceFunCallDeclContext.class,0);
		}

		public VariableDeclContext variableDecl() {
			return getRuleContext(VariableDeclContext.class,0);
		}

		public ExternDeclContext externDecl() {
			return getRuleContext(ExternDeclContext.class,0);
		}

		public MacroDeclContext macroDecl() {
			return getRuleContext(MacroDeclContext.class,0);
		}

		public FuntionCallDeclContext funtionCallDecl() {
			return getRuleContext(FuntionCallDeclContext.class,0);
		}

		@Override public int getRuleIndex() { return RULE_statements; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TechlandScriptListener ) ((TechlandScriptListener)listener).enterStatements(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TechlandScriptListener ) ((TechlandScriptListener)listener).exitStatements(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof TechlandScriptVisitor ) return ((TechlandScriptVisitor<? extends T>)visitor).visitStatements(this);
			else return visitor.visitChildren(this);
		}
	}

	public final FuntionBlockDeclContext funtionBlockDecl() throws RecognitionException {
		FuntionBlockDeclContext _localctx = new FuntionBlockDeclContext(_ctx, getState());
		enterRule(_localctx, 36, RULE_funtionBlockDecl);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(208);
			match(Id);
			setState(209);
			match(LParen);
			setState(211);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if ((((_la) & ~0x3f) == 0 && ((1L << _la) & 3711122342464L) != 0)) {
				{
				setState(210);
				valueList();
				}
			}

			setState(213);
			match(RParen);
			setState(214);
			functionBlock();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class VariableDeclContext extends ParserRuleContext {
		public VariableDeclContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}

		public TypeContext type() {
			return getRuleContext(TypeContext.class,0);
		}

		public TerminalNode Id() { return getToken(TechlandScriptParser.Id, 0); }
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}

		public TerminalNode Equals() { return getToken(TechlandScriptParser.Equals, 0); }

		public TerminalNode Semicolon() { return getToken(TechlandScriptParser.Semicolon, 0); }

		@Override public int getRuleIndex() { return RULE_variableDecl; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TechlandScriptListener ) ((TechlandScriptListener)listener).enterVariableDecl(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TechlandScriptListener ) ((TechlandScriptListener)listener).exitVariableDecl(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof TechlandScriptVisitor ) return ((TechlandScriptVisitor<? extends T>)visitor).visitVariableDecl(this);
			else return visitor.visitChildren(this);
		}
	}

	public final UseDeclContext useDecl() throws RecognitionException {
		UseDeclContext _localctx = new UseDeclContext(_ctx, getState());
		enterRule(_localctx, 38, RULE_useDecl);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(216);
			match(Use);
			setState(217);
			match(Id);
			setState(218);
			match(LParen);
			setState(220);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if ((((_la) & ~0x3f) == 0 && ((1L << _la) & 3711122342464L) != 0)) {
				{
				setState(219);
				valueList();
				}
			}

			setState(222);
			match(RParen);
			setState(224);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==Semicolon) {
				{
				setState(223);
				match(Semicolon);
				}
			}

			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class FuntionCallDeclContext extends ParserRuleContext {
		public TerminalNode Id() { return getToken(TechlandScriptParser.Id, 0); }
		public FuntionCallDeclContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}

		public TerminalNode LParen() { return getToken(TechlandScriptParser.LParen, 0); }

		public TerminalNode RParen() { return getToken(TechlandScriptParser.RParen, 0); }

		public ValueListContext valueList() {
			return getRuleContext(ValueListContext.class,0);
		}

		public TerminalNode Semicolon() { return getToken(TechlandScriptParser.Semicolon, 0); }

		@Override public int getRuleIndex() { return RULE_funtionCallDecl; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TechlandScriptListener ) ((TechlandScriptListener)listener).enterFuntionCallDecl(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TechlandScriptListener ) ((TechlandScriptListener)listener).exitFuntionCallDecl(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof TechlandScriptVisitor ) return ((TechlandScriptVisitor<? extends T>)visitor).visitFuntionCallDecl(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ValueListContext valueList() throws RecognitionException {
		ValueListContext _localctx = new ValueListContext(_ctx, getState());
		enterRule(_localctx, 40, RULE_valueList);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(226);
			expression(0);
			setState(231);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==Comma) {
				{
				{
				setState(227);
				match(Comma);
				setState(228);
				expression(0);
				}
				}
				setState(233);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class MethodReferenceFunCallDeclContext extends ParserRuleContext {
		public MethodReferenceFunCallDeclContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}

		public List<TerminalNode> Id() { return getTokens(TechlandScriptParser.Id); }

		public TerminalNode Id(int i) {
			return getToken(TechlandScriptParser.Id, i);
		}

		public TerminalNode DoubleColon() { return getToken(TechlandScriptParser.DoubleColon, 0); }

		public TerminalNode LParen() { return getToken(TechlandScriptParser.LParen, 0); }

		public TerminalNode RParen() { return getToken(TechlandScriptParser.RParen, 0); }

		public ValueListContext valueList() {
			return getRuleContext(ValueListContext.class,0);
		}

		public TerminalNode Semicolon() { return getToken(TechlandScriptParser.Semicolon, 0); }

		@Override public int getRuleIndex() { return RULE_methodReferenceFunCallDecl; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TechlandScriptListener ) ((TechlandScriptListener)listener).enterMethodReferenceFunCallDecl(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TechlandScriptListener ) ((TechlandScriptListener)listener).exitMethodReferenceFunCallDecl(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof TechlandScriptVisitor ) return ((TechlandScriptVisitor<? extends T>)visitor).visitMethodReferenceFunCallDecl(this);
			else return visitor.visitChildren(this);
		}
	}

	public final TypeContext type() throws RecognitionException {
		TypeContext _localctx = new TypeContext(_ctx, getState());
		enterRule(_localctx, 42, RULE_type);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(234);
			match(Id);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class FuntionBlockDeclContext extends ParserRuleContext {
		public TerminalNode Id() { return getToken(TechlandScriptParser.Id, 0); }
		public FuntionBlockDeclContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}

		public TerminalNode LParen() { return getToken(TechlandScriptParser.LParen, 0); }

		public TerminalNode RParen() { return getToken(TechlandScriptParser.RParen, 0); }

		public FunctionBlockContext functionBlock() {
			return getRuleContext(FunctionBlockContext.class,0);
		}

		public ValueListContext valueList() {
			return getRuleContext(ValueListContext.class,0);
		}

		@Override public int getRuleIndex() { return RULE_funtionBlockDecl; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TechlandScriptListener ) ((TechlandScriptListener)listener).enterFuntionBlockDecl(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TechlandScriptListener ) ((TechlandScriptListener)listener).exitFuntionBlockDecl(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof TechlandScriptVisitor ) return ((TechlandScriptVisitor<? extends T>)visitor).visitFuntionBlockDecl(this);
			else return visitor.visitChildren(this);
		}
	}

	@SuppressWarnings("CheckReturnValue")
	public static class UseDeclContext extends ParserRuleContext {
		public UseDeclContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		public TerminalNode Id() { return getToken(TechlandScriptParser.Id, 0); }
		public TerminalNode LParen() { return getToken(TechlandScriptParser.LParen, 0); }
		public TerminalNode RParen() { return getToken(TechlandScriptParser.RParen, 0); }
		public ValueListContext valueList() {
			return getRuleContext(ValueListContext.class,0);
		}
		public TerminalNode Semicolon() { return getToken(TechlandScriptParser.Semicolon, 0); }

		public TerminalNode Use() { return getToken(TechlandScriptParser.Use, 0); }

		@Override public int getRuleIndex() { return RULE_useDecl; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TechlandScriptListener ) ((TechlandScriptListener)listener).enterUseDecl(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TechlandScriptListener ) ((TechlandScriptListener)listener).exitUseDecl(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof TechlandScriptVisitor ) return ((TechlandScriptVisitor<? extends T>)visitor).visitUseDecl(this);
			else return visitor.visitChildren(this);
		}
	}

	@SuppressWarnings("CheckReturnValue")
	public static class ValueListContext extends ParserRuleContext {
		public ValueListContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}

		public List<ExpressionContext> expression() {
			return getRuleContexts(ExpressionContext.class);
		}

		public ExpressionContext expression(int i) {
			return getRuleContext(ExpressionContext.class,i);
		}

		public List<TerminalNode> Comma() { return getTokens(TechlandScriptParser.Comma); }

		public TerminalNode Comma(int i) {
			return getToken(TechlandScriptParser.Comma, i);
		}

		@Override public int getRuleIndex() { return RULE_valueList; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TechlandScriptListener ) ((TechlandScriptListener)listener).enterValueList(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TechlandScriptListener ) ((TechlandScriptListener)listener).exitValueList(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof TechlandScriptVisitor ) return ((TechlandScriptVisitor<? extends T>)visitor).visitValueList(this);
			else return visitor.visitChildren(this);
		}
	}

	@SuppressWarnings("CheckReturnValue")
	public static class TypeContext extends ParserRuleContext {
		public TerminalNode Id() { return getToken(TechlandScriptParser.Id, 0); }
		public TypeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_type; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TechlandScriptListener ) ((TechlandScriptListener)listener).enterType(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TechlandScriptListener ) ((TechlandScriptListener)listener).exitType(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof TechlandScriptVisitor ) return ((TechlandScriptVisitor<? extends T>)visitor).visitType(this);
			else return visitor.visitChildren(this);
		}
	}

	@SuppressWarnings("CheckReturnValue")
	public static class ExpressionContext extends ParserRuleContext {
		public TerminalNode LParen() { return getToken(TechlandScriptParser.LParen, 0); }
		public List<ExpressionContext> expression() {
			return getRuleContexts(ExpressionContext.class);
		}
		public ExpressionContext expression(int i) {
			return getRuleContext(ExpressionContext.class,i);
		}
		public TerminalNode RParen() { return getToken(TechlandScriptParser.RParen, 0); }
		public FieldAccessContext fieldAccess() {
			return getRuleContext(FieldAccessContext.class,0);
		}
		public ValueListContext valueList() {
			return getRuleContext(ValueListContext.class,0);
		}
		public TerminalNode Number() { return getToken(TechlandScriptParser.Number, 0); }
		public TerminalNode String() { return getToken(TechlandScriptParser.String, 0); }
		public TerminalNode Bool() { return getToken(TechlandScriptParser.Bool, 0); }
		public ArrayValueContext arrayValue() {
			return getRuleContext(ArrayValueContext.class,0);
		}
		public TerminalNode Id() { return getToken(TechlandScriptParser.Id, 0); }
		public TerminalNode Equals() { return getToken(TechlandScriptParser.Equals, 0); }
		public TerminalNode BitNot() { return getToken(TechlandScriptParser.BitNot, 0); }
		public TerminalNode Exclamation() { return getToken(TechlandScriptParser.Exclamation, 0); }
		public TerminalNode Minus() { return getToken(TechlandScriptParser.Minus, 0); }
		public TerminalNode Mul() { return getToken(TechlandScriptParser.Mul, 0); }
		public TerminalNode Div() { return getToken(TechlandScriptParser.Div, 0); }
		public TerminalNode Plus() { return getToken(TechlandScriptParser.Plus, 0); }
		public TerminalNode BitOr() { return getToken(TechlandScriptParser.BitOr, 0); }
		public TerminalNode BitAnd() { return getToken(TechlandScriptParser.BitAnd, 0); }
		public TerminalNode Gt() { return getToken(TechlandScriptParser.Gt, 0); }
		public TerminalNode Lt() { return getToken(TechlandScriptParser.Lt, 0); }
		public TerminalNode Gte() { return getToken(TechlandScriptParser.Gte, 0); }
		public TerminalNode Lte() { return getToken(TechlandScriptParser.Lte, 0); }
		public TerminalNode Eq() { return getToken(TechlandScriptParser.Eq, 0); }
		public TerminalNode NotEq() { return getToken(TechlandScriptParser.NotEq, 0); }
		public TerminalNode LogicAnd() { return getToken(TechlandScriptParser.LogicAnd, 0); }
		public TerminalNode LogicOr() { return getToken(TechlandScriptParser.LogicOr, 0); }
		public TerminalNode Question() { return getToken(TechlandScriptParser.Question, 0); }
		public TerminalNode Colon() { return getToken(TechlandScriptParser.Colon, 0); }
		public ExpressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_expression; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TechlandScriptListener ) ((TechlandScriptListener)listener).enterExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TechlandScriptListener ) ((TechlandScriptListener)listener).exitExpression(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof TechlandScriptVisitor ) return ((TechlandScriptVisitor<? extends T>)visitor).visitExpression(this);
			else return visitor.visitChildren(this);
		}
	}

	@SuppressWarnings("CheckReturnValue")
	public static class FieldAccessContext extends ParserRuleContext {
		public List<TerminalNode> Id() { return getTokens(TechlandScriptParser.Id); }
		public TerminalNode Id(int i) {
			return getToken(TechlandScriptParser.Id, i);
		}
		public List<TerminalNode> Dot() { return getTokens(TechlandScriptParser.Dot); }
		public TerminalNode Dot(int i) {
			return getToken(TechlandScriptParser.Dot, i);
		}
		public FieldAccessContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_fieldAccess; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TechlandScriptListener ) ((TechlandScriptListener)listener).enterFieldAccess(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TechlandScriptListener ) ((TechlandScriptListener)listener).exitFieldAccess(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof TechlandScriptVisitor ) return ((TechlandScriptVisitor<? extends T>)visitor).visitFieldAccess(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ArrayValueContext arrayValue() throws RecognitionException {
		ArrayValueContext _localctx = new ArrayValueContext(_ctx, getState());
		enterRule(_localctx, 48, RULE_arrayValue);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(304);
			match(LBracket);
			setState(306);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if ((((_la) & ~0x3f) == 0 && ((1L << _la) & 3711122342464L) != 0)) {
				{
				setState(305);
				valueList();
				}
			}

			setState(308);
			match(RBracket);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public boolean sempred(RuleContext _localctx, int ruleIndex, int predIndex) {
		switch (ruleIndex) {
		case 22:
			return expression_sempred((ExpressionContext)_localctx, predIndex);
		}
		return true;
	}
	private boolean expression_sempred(ExpressionContext _localctx, int predIndex) {
		switch (predIndex) {
		case 0:
			return precpred(_ctx, 8);
		case 1:
			return precpred(_ctx, 7);
		case 2:
			return precpred(_ctx, 6);
		case 3:
			return precpred(_ctx, 5);
		case 4:
			return precpred(_ctx, 4);
		case 5:
			return precpred(_ctx, 3);
		case 6:
			return precpred(_ctx, 2);
		case 7:
			return precpred(_ctx, 1);
		}
		return true;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class ArrayValueContext extends ParserRuleContext {
		public ArrayValueContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}

		public TerminalNode LBracket() { return getToken(TechlandScriptParser.LBracket, 0); }

		public TerminalNode RBracket() { return getToken(TechlandScriptParser.RBracket, 0); }

		public ValueListContext valueList() {
			return getRuleContext(ValueListContext.class,0);
		}

		@Override public int getRuleIndex() { return RULE_arrayValue; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TechlandScriptListener ) ((TechlandScriptListener)listener).enterArrayValue(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TechlandScriptListener ) ((TechlandScriptListener)listener).exitArrayValue(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof TechlandScriptVisitor ) return ((TechlandScriptVisitor<? extends T>)visitor).visitArrayValue(this);
			else return visitor.visitChildren(this);
		}
	}
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}