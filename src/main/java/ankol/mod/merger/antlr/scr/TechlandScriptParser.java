package ankol.mod.merger.antlr.scr;// Generated from TechlandScript.g4 by ANTLR 4.13.2

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.ATN;
import org.antlr.v4.runtime.atn.ATNDeserializer;
import org.antlr.v4.runtime.atn.ParserATNSimulator;
import org.antlr.v4.runtime.atn.PredictionContextCache;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.tree.ParseTreeListener;
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
		KwInt=9, KwFloat=10, KwBool=11, KwString=12, KwVoid=13, KwVec2=14, KwVec3=15, 
		KwVec4=16, LParen=17, RParen=18, LBrace=19, RBrace=20, Semicolon=21, Comma=22, 
		Equals=23, LBracket=24, RBracket=25, Dot=26, Plus=27, Minus=28, Mul=29, 
		Div=30, LogicAnd=31, LogicOr=32, BitOr=33, BitAnd=34, BitNot=35, Question=36, 
		Colon=37, Gt=38, Lt=39, Eq=40, NotEq=41, Gte=42, Lte=43, Bool=44, Id=45, 
		MacroId=46, Number=47, String=48, LineComment=49, BlockComment=50, WhiteSpaces=51;
	public static final int
		RULE_file = 0, RULE_definition = 1, RULE_importDecl = 2, RULE_exportDecl = 3, 
		RULE_externDecl = 4, RULE_directiveCall = 5, RULE_macroDecl = 6, RULE_subDecl = 7, 
		RULE_logicControlDecl = 8, RULE_elseIfClause = 9, RULE_elseClause = 10, 
		RULE_paramList = 11, RULE_param = 12, RULE_functionBlock = 13, RULE_statements = 14, 
		RULE_variableDecl = 15, RULE_funtionCallDecl = 16, RULE_funtionBlockDecl = 17, 
		RULE_useDecl = 18, RULE_valueList = 19, RULE_type = 20, RULE_expression = 21, 
		RULE_fieldAccess = 22, RULE_arrayValue = 23;
	public static final String[] ruleNames = makeRuleNames();
	public static final String _serializedATN =
		"\u0004\u00013\u0128\u0002\u0000\u0007\u0000\u0002\u0001\u0007\u0001\u0002"+
		"\u0002\u0007\u0002\u0002\u0003\u0007\u0003\u0002\u0004\u0007\u0004\u0002"+
		"\u0005\u0007\u0005\u0002\u0006\u0007\u0006\u0002\u0007\u0007\u0007\u0002"+
		"\b\u0007\b\u0002\t\u0007\t\u0002\n\u0007\n\u0002\u000b\u0007\u000b\u0002"+
		"\f\u0007\f\u0002\r\u0007\r\u0002\u000e\u0007\u000e\u0002\u000f\u0007\u000f"+
		"\u0002\u0010\u0007\u0010\u0002\u0011\u0007\u0011\u0002\u0012\u0007\u0012"+
		"\u0002\u0013\u0007\u0013\u0002\u0014\u0007\u0014\u0002\u0015\u0007\u0015"+
		"\u0002\u0016\u0007\u0016\u0002\u0017\u0007\u0017\u0001\u0000\u0005\u0000"+
		"2\b\u0000\n\u0000\f\u00005\t\u0000\u0001\u0000\u0001\u0000\u0001\u0001"+
		"\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001"+
		"\u0001\u0001\u0001\u0001\u0003\u0001B\b\u0001\u0001\u0002\u0001\u0002"+
		"\u0001\u0002\u0003\u0002G\b\u0002\u0001\u0003\u0001\u0003\u0001\u0003"+
		"\u0001\u0003\u0001\u0003\u0001\u0003\u0003\u0003O\b\u0003\u0001\u0004"+
		"\u0001\u0004\u0001\u0004\u0001\u0004\u0003\u0004U\b\u0004\u0001\u0005"+
		"\u0001\u0005\u0001\u0005\u0001\u0005\u0003\u0005[\b\u0005\u0001\u0005"+
		"\u0001\u0005\u0003\u0005_\b\u0005\u0001\u0006\u0001\u0006\u0001\u0006"+
		"\u0001\u0006\u0001\u0006\u0003\u0006f\b\u0006\u0001\u0007\u0001\u0007"+
		"\u0001\u0007\u0001\u0007\u0003\u0007l\b\u0007\u0001\u0007\u0001\u0007"+
		"\u0001\u0007\u0001\b\u0001\b\u0001\b\u0001\b\u0001\b\u0001\b\u0005\bw"+
		"\b\b\n\b\f\bz\t\b\u0001\b\u0003\b}\b\b\u0001\t\u0001\t\u0001\t\u0001\t"+
		"\u0001\t\u0001\t\u0001\t\u0001\n\u0001\n\u0001\n\u0003\n\u0089\b\n\u0001"+
		"\n\u0003\n\u008c\b\n\u0001\n\u0001\n\u0001\u000b\u0001\u000b\u0001\u000b"+
		"\u0005\u000b\u0093\b\u000b\n\u000b\f\u000b\u0096\t\u000b\u0001\f\u0001"+
		"\f\u0001\f\u0001\f\u0003\f\u009c\b\f\u0001\r\u0001\r\u0005\r\u00a0\b\r"+
		"\n\r\f\r\u00a3\t\r\u0001\r\u0001\r\u0001\u000e\u0001\u000e\u0001\u000e"+
		"\u0001\u000e\u0001\u000e\u0001\u000e\u0001\u000e\u0003\u000e\u00ae\b\u000e"+
		"\u0001\u000f\u0001\u000f\u0001\u000f\u0001\u000f\u0003\u000f\u00b4\b\u000f"+
		"\u0001\u000f\u0003\u000f\u00b7\b\u000f\u0001\u0010\u0001\u0010\u0001\u0010"+
		"\u0003\u0010\u00bc\b\u0010\u0001\u0010\u0001\u0010\u0003\u0010\u00c0\b"+
		"\u0010\u0001\u0011\u0001\u0011\u0001\u0011\u0003\u0011\u00c5\b\u0011\u0001"+
		"\u0011\u0001\u0011\u0001\u0011\u0001\u0012\u0001\u0012\u0001\u0012\u0001"+
		"\u0012\u0003\u0012\u00ce\b\u0012\u0001\u0012\u0001\u0012\u0003\u0012\u00d2"+
		"\b\u0012\u0001\u0013\u0001\u0013\u0001\u0013\u0005\u0013\u00d7\b\u0013"+
		"\n\u0013\f\u0013\u00da\t\u0013\u0001\u0014\u0001\u0014\u0001\u0015\u0001"+
		"\u0015\u0001\u0015\u0001\u0015\u0001\u0015\u0001\u0015\u0001\u0015\u0001"+
		"\u0015\u0003\u0015\u00e6\b\u0015\u0001\u0015\u0001\u0015\u0001\u0015\u0001"+
		"\u0015\u0001\u0015\u0001\u0015\u0001\u0015\u0001\u0015\u0001\u0015\u0001"+
		"\u0015\u0001\u0015\u0001\u0015\u0001\u0015\u0001\u0015\u0001\u0015\u0001"+
		"\u0015\u0003\u0015\u00f8\b\u0015\u0001\u0015\u0001\u0015\u0001\u0015\u0001"+
		"\u0015\u0001\u0015\u0001\u0015\u0001\u0015\u0001\u0015\u0001\u0015\u0001"+
		"\u0015\u0001\u0015\u0001\u0015\u0001\u0015\u0001\u0015\u0001\u0015\u0001"+
		"\u0015\u0001\u0015\u0001\u0015\u0001\u0015\u0001\u0015\u0001\u0015\u0001"+
		"\u0015\u0001\u0015\u0001\u0015\u0001\u0015\u0001\u0015\u0001\u0015\u0005"+
		"\u0015\u0115\b\u0015\n\u0015\f\u0015\u0118\t\u0015\u0001\u0016\u0001\u0016"+
		"\u0001\u0016\u0005\u0016\u011d\b\u0016\n\u0016\f\u0016\u0120\t\u0016\u0001"+
		"\u0017\u0001\u0017\u0003\u0017\u0124\b\u0017\u0001\u0017\u0001\u0017\u0001"+
		"\u0017\u0000\u0001*\u0018\u0000\u0002\u0004\u0006\b\n\f\u000e\u0010\u0012"+
		"\u0014\u0016\u0018\u001a\u001c\u001e \"$&(*,.\u0000\u0004\u0002\u0000"+
		"\t\u0010--\u0001\u0000\u001d\u001e\u0001\u0000\u001b\u001c\u0001\u0000"+
		"&+\u0149\u00003\u0001\u0000\u0000\u0000\u0002A\u0001\u0000\u0000\u0000"+
		"\u0004C\u0001\u0000\u0000\u0000\u0006H\u0001\u0000\u0000\u0000\bP\u0001"+
		"\u0000\u0000\u0000\nV\u0001\u0000\u0000\u0000\f`\u0001\u0000\u0000\u0000"+
		"\u000eg\u0001\u0000\u0000\u0000\u0010p\u0001\u0000\u0000\u0000\u0012~"+
		"\u0001\u0000\u0000\u0000\u0014\u0085\u0001\u0000\u0000\u0000\u0016\u008f"+
		"\u0001\u0000\u0000\u0000\u0018\u0097\u0001\u0000\u0000\u0000\u001a\u009d"+
		"\u0001\u0000\u0000\u0000\u001c\u00ad\u0001\u0000\u0000\u0000\u001e\u00af"+
		"\u0001\u0000\u0000\u0000 \u00b8\u0001\u0000\u0000\u0000\"\u00c1\u0001"+
		"\u0000\u0000\u0000$\u00c9\u0001\u0000\u0000\u0000&\u00d3\u0001\u0000\u0000"+
		"\u0000(\u00db\u0001\u0000\u0000\u0000*\u00f7\u0001\u0000\u0000\u0000,"+
		"\u0119\u0001\u0000\u0000\u0000.\u0121\u0001\u0000\u0000\u000002\u0003"+
		"\u0002\u0001\u000010\u0001\u0000\u0000\u000025\u0001\u0000\u0000\u0000"+
		"31\u0001\u0000\u0000\u000034\u0001\u0000\u0000\u000046\u0001\u0000\u0000"+
		"\u000053\u0001\u0000\u0000\u000067\u0005\u0000\u0000\u00017\u0001\u0001"+
		"\u0000\u0000\u00008B\u0003\u0004\u0002\u00009B\u0003\u0006\u0003\u0000"+
		":B\u0003\b\u0004\u0000;B\u0003\u000e\u0007\u0000<B\u0003\n\u0005\u0000"+
		"=B\u0003\f\u0006\u0000>B\u0003\u001e\u000f\u0000?B\u0003 \u0010\u0000"+
		"@B\u0003\"\u0011\u0000A8\u0001\u0000\u0000\u0000A9\u0001\u0000\u0000\u0000"+
		"A:\u0001\u0000\u0000\u0000A;\u0001\u0000\u0000\u0000A<\u0001\u0000\u0000"+
		"\u0000A=\u0001\u0000\u0000\u0000A>\u0001\u0000\u0000\u0000A?\u0001\u0000"+
		"\u0000\u0000A@\u0001\u0000\u0000\u0000B\u0003\u0001\u0000\u0000\u0000"+
		"CD\u0005\u0001\u0000\u0000DF\u00050\u0000\u0000EG\u0005\u0015\u0000\u0000"+
		"FE\u0001\u0000\u0000\u0000FG\u0001\u0000\u0000\u0000G\u0005\u0001\u0000"+
		"\u0000\u0000HI\u0005\u0003\u0000\u0000IJ\u0003(\u0014\u0000JK\u0005-\u0000"+
		"\u0000KL\u0005\u0017\u0000\u0000LN\u0003*\u0015\u0000MO\u0005\u0015\u0000"+
		"\u0000NM\u0001\u0000\u0000\u0000NO\u0001\u0000\u0000\u0000O\u0007\u0001"+
		"\u0000\u0000\u0000PQ\u0005\u0002\u0000\u0000QR\u0003(\u0014\u0000RT\u0005"+
		"-\u0000\u0000SU\u0005\u0015\u0000\u0000TS\u0001\u0000\u0000\u0000TU\u0001"+
		"\u0000\u0000\u0000U\t\u0001\u0000\u0000\u0000VW\u0005\u0006\u0000\u0000"+
		"WX\u0005-\u0000\u0000XZ\u0005\u0011\u0000\u0000Y[\u0003&\u0013\u0000Z"+
		"Y\u0001\u0000\u0000\u0000Z[\u0001\u0000\u0000\u0000[\\\u0001\u0000\u0000"+
		"\u0000\\^\u0005\u0012\u0000\u0000]_\u0005\u0015\u0000\u0000^]\u0001\u0000"+
		"\u0000\u0000^_\u0001\u0000\u0000\u0000_\u000b\u0001\u0000\u0000\u0000"+
		"`a\u0005.\u0000\u0000ab\u0005\u0011\u0000\u0000bc\u0003&\u0013\u0000c"+
		"e\u0005\u0012\u0000\u0000df\u0005\u0015\u0000\u0000ed\u0001\u0000\u0000"+
		"\u0000ef\u0001\u0000\u0000\u0000f\r\u0001\u0000\u0000\u0000gh\u0005\u0004"+
		"\u0000\u0000hi\u0005-\u0000\u0000ik\u0005\u0011\u0000\u0000jl\u0003\u0016"+
		"\u000b\u0000kj\u0001\u0000\u0000\u0000kl\u0001\u0000\u0000\u0000lm\u0001"+
		"\u0000\u0000\u0000mn\u0005\u0012\u0000\u0000no\u0003\u001a\r\u0000o\u000f"+
		"\u0001\u0000\u0000\u0000pq\u0005\u0007\u0000\u0000qr\u0005\u0011\u0000"+
		"\u0000rs\u0003*\u0015\u0000st\u0005\u0012\u0000\u0000tx\u0003\u001a\r"+
		"\u0000uw\u0003\u0012\t\u0000vu\u0001\u0000\u0000\u0000wz\u0001\u0000\u0000"+
		"\u0000xv\u0001\u0000\u0000\u0000xy\u0001\u0000\u0000\u0000y|\u0001\u0000"+
		"\u0000\u0000zx\u0001\u0000\u0000\u0000{}\u0003\u0014\n\u0000|{\u0001\u0000"+
		"\u0000\u0000|}\u0001\u0000\u0000\u0000}\u0011\u0001\u0000\u0000\u0000"+
		"~\u007f\u0005\b\u0000\u0000\u007f\u0080\u0005\u0007\u0000\u0000\u0080"+
		"\u0081\u0005\u0011\u0000\u0000\u0081\u0082\u0003*\u0015\u0000\u0082\u0083"+
		"\u0005\u0012\u0000\u0000\u0083\u0084\u0003\u001a\r\u0000\u0084\u0013\u0001"+
		"\u0000\u0000\u0000\u0085\u008b\u0005\b\u0000\u0000\u0086\u0088\u0005\u0011"+
		"\u0000\u0000\u0087\u0089\u0003*\u0015\u0000\u0088\u0087\u0001\u0000\u0000"+
		"\u0000\u0088\u0089\u0001\u0000\u0000\u0000\u0089\u008a\u0001\u0000\u0000"+
		"\u0000\u008a\u008c\u0005\u0012\u0000\u0000\u008b\u0086\u0001\u0000\u0000"+
		"\u0000\u008b\u008c\u0001\u0000\u0000\u0000\u008c\u008d\u0001\u0000\u0000"+
		"\u0000\u008d\u008e\u0003\u001a\r\u0000\u008e\u0015\u0001\u0000\u0000\u0000"+
		"\u008f\u0094\u0003\u0018\f\u0000\u0090\u0091\u0005\u0016\u0000\u0000\u0091"+
		"\u0093\u0003\u0018\f\u0000\u0092\u0090\u0001\u0000\u0000\u0000\u0093\u0096"+
		"\u0001\u0000\u0000\u0000\u0094\u0092\u0001\u0000\u0000\u0000\u0094\u0095"+
		"\u0001\u0000\u0000\u0000\u0095\u0017\u0001\u0000\u0000\u0000\u0096\u0094"+
		"\u0001\u0000\u0000\u0000\u0097\u0098\u0003(\u0014\u0000\u0098\u009b\u0005"+
		"-\u0000\u0000\u0099\u009a\u0005\u0017\u0000\u0000\u009a\u009c\u0003*\u0015"+
		"\u0000\u009b\u0099\u0001\u0000\u0000\u0000\u009b\u009c\u0001\u0000\u0000"+
		"\u0000\u009c\u0019\u0001\u0000\u0000\u0000\u009d\u00a1\u0005\u0013\u0000"+
		"\u0000\u009e\u00a0\u0003\u001c\u000e\u0000\u009f\u009e\u0001\u0000\u0000"+
		"\u0000\u00a0\u00a3\u0001\u0000\u0000\u0000\u00a1\u009f\u0001\u0000\u0000"+
		"\u0000\u00a1\u00a2\u0001\u0000\u0000\u0000\u00a2\u00a4\u0001\u0000\u0000"+
		"\u0000\u00a3\u00a1\u0001\u0000\u0000\u0000\u00a4\u00a5\u0005\u0014\u0000"+
		"\u0000\u00a5\u001b\u0001\u0000\u0000\u0000\u00a6\u00ae\u0003 \u0010\u0000"+
		"\u00a7\u00ae\u0003\"\u0011\u0000\u00a8\u00ae\u0003$\u0012\u0000\u00a9"+
		"\u00ae\u0003\u001e\u000f\u0000\u00aa\u00ae\u0003\b\u0004\u0000\u00ab\u00ae"+
		"\u0003\u0010\b\u0000\u00ac\u00ae\u0003\f\u0006\u0000\u00ad\u00a6\u0001"+
		"\u0000\u0000\u0000\u00ad\u00a7\u0001\u0000\u0000\u0000\u00ad\u00a8\u0001"+
		"\u0000\u0000\u0000\u00ad\u00a9\u0001\u0000\u0000\u0000\u00ad\u00aa\u0001"+
		"\u0000\u0000\u0000\u00ad\u00ab\u0001\u0000\u0000\u0000\u00ad\u00ac\u0001"+
		"\u0000\u0000\u0000\u00ae\u001d\u0001\u0000\u0000\u0000\u00af\u00b0\u0003"+
		"(\u0014\u0000\u00b0\u00b3\u0005-\u0000\u0000\u00b1\u00b2\u0005\u0017\u0000"+
		"\u0000\u00b2\u00b4\u0003*\u0015\u0000\u00b3\u00b1\u0001\u0000\u0000\u0000"+
		"\u00b3\u00b4\u0001\u0000\u0000\u0000\u00b4\u00b6\u0001\u0000\u0000\u0000"+
		"\u00b5\u00b7\u0005\u0015\u0000\u0000\u00b6\u00b5\u0001\u0000\u0000\u0000"+
		"\u00b6\u00b7\u0001\u0000\u0000\u0000\u00b7\u001f\u0001\u0000\u0000\u0000"+
		"\u00b8\u00b9\u0005-\u0000\u0000\u00b9\u00bb\u0005\u0011\u0000\u0000\u00ba"+
		"\u00bc\u0003&\u0013\u0000\u00bb\u00ba\u0001\u0000\u0000\u0000\u00bb\u00bc"+
		"\u0001\u0000\u0000\u0000\u00bc\u00bd\u0001\u0000\u0000\u0000\u00bd\u00bf"+
		"\u0005\u0012\u0000\u0000\u00be\u00c0\u0005\u0015\u0000\u0000\u00bf\u00be"+
		"\u0001\u0000\u0000\u0000\u00bf\u00c0\u0001\u0000\u0000\u0000\u00c0!\u0001"+
		"\u0000\u0000\u0000\u00c1\u00c2\u0005-\u0000\u0000\u00c2\u00c4\u0005\u0011"+
		"\u0000\u0000\u00c3\u00c5\u0003&\u0013\u0000\u00c4\u00c3\u0001\u0000\u0000"+
		"\u0000\u00c4\u00c5\u0001\u0000\u0000\u0000\u00c5\u00c6\u0001\u0000\u0000"+
		"\u0000\u00c6\u00c7\u0005\u0012\u0000\u0000\u00c7\u00c8\u0003\u001a\r\u0000"+
		"\u00c8#\u0001\u0000\u0000\u0000\u00c9\u00ca\u0005\u0005\u0000\u0000\u00ca"+
		"\u00cb\u0005-\u0000\u0000\u00cb\u00cd\u0005\u0011\u0000\u0000\u00cc\u00ce"+
		"\u0003&\u0013\u0000\u00cd\u00cc\u0001\u0000\u0000\u0000\u00cd\u00ce\u0001"+
		"\u0000\u0000\u0000\u00ce\u00cf\u0001\u0000\u0000\u0000\u00cf\u00d1\u0005"+
		"\u0012\u0000\u0000\u00d0\u00d2\u0005\u0015\u0000\u0000\u00d1\u00d0\u0001"+
		"\u0000\u0000\u0000\u00d1\u00d2\u0001\u0000\u0000\u0000\u00d2%\u0001\u0000"+
		"\u0000\u0000\u00d3\u00d8\u0003*\u0015\u0000\u00d4\u00d5\u0005\u0016\u0000"+
		"\u0000\u00d5\u00d7\u0003*\u0015\u0000\u00d6\u00d4\u0001\u0000\u0000\u0000"+
		"\u00d7\u00da\u0001\u0000\u0000\u0000\u00d8\u00d6\u0001\u0000\u0000\u0000"+
		"\u00d8\u00d9\u0001\u0000\u0000\u0000\u00d9\'\u0001\u0000\u0000\u0000\u00da"+
		"\u00d8\u0001\u0000\u0000\u0000\u00db\u00dc\u0007\u0000\u0000\u0000\u00dc"+
		")\u0001\u0000\u0000\u0000\u00dd\u00de\u0006\u0015\uffff\uffff\u0000\u00de"+
		"\u00df\u0005\u0011\u0000\u0000\u00df\u00e0\u0003*\u0015\u0000\u00e0\u00e1"+
		"\u0005\u0012\u0000\u0000\u00e1\u00f8\u0001\u0000\u0000\u0000\u00e2\u00e3"+
		"\u0003,\u0016\u0000\u00e3\u00e5\u0005\u0011\u0000\u0000\u00e4\u00e6\u0003"+
		"&\u0013\u0000\u00e5\u00e4\u0001\u0000\u0000\u0000\u00e5\u00e6\u0001\u0000"+
		"\u0000\u0000\u00e6\u00e7\u0001\u0000\u0000\u0000\u00e7\u00e8\u0005\u0012"+
		"\u0000\u0000\u00e8\u00f8\u0001\u0000\u0000\u0000\u00e9\u00f8\u0003,\u0016"+
		"\u0000\u00ea\u00f8\u0005/\u0000\u0000\u00eb\u00f8\u00050\u0000\u0000\u00ec"+
		"\u00f8\u0005,\u0000\u0000\u00ed\u00f8\u0003.\u0017\u0000\u00ee\u00ef\u0005"+
		"-\u0000\u0000\u00ef\u00f0\u0005\u0017\u0000\u0000\u00f0\u00f8\u0003*\u0015"+
		"\f\u00f1\u00f2\u0005#\u0000\u0000\u00f2\u00f8\u0003*\u0015\u000b\u00f3"+
		"\u00f4\u0005\u0006\u0000\u0000\u00f4\u00f8\u0003*\u0015\n\u00f5\u00f6"+
		"\u0005\u001c\u0000\u0000\u00f6\u00f8\u0003*\u0015\t\u00f7\u00dd\u0001"+
		"\u0000\u0000\u0000\u00f7\u00e2\u0001\u0000\u0000\u0000\u00f7\u00e9\u0001"+
		"\u0000\u0000\u0000\u00f7\u00ea\u0001\u0000\u0000\u0000\u00f7\u00eb\u0001"+
		"\u0000\u0000\u0000\u00f7\u00ec\u0001\u0000\u0000\u0000\u00f7\u00ed\u0001"+
		"\u0000\u0000\u0000\u00f7\u00ee\u0001\u0000\u0000\u0000\u00f7\u00f1\u0001"+
		"\u0000\u0000\u0000\u00f7\u00f3\u0001\u0000\u0000\u0000\u00f7\u00f5\u0001"+
		"\u0000\u0000\u0000\u00f8\u0116\u0001\u0000\u0000\u0000\u00f9\u00fa\n\b"+
		"\u0000\u0000\u00fa\u00fb\u0007\u0001\u0000\u0000\u00fb\u0115\u0003*\u0015"+
		"\t\u00fc\u00fd\n\u0007\u0000\u0000\u00fd\u00fe\u0007\u0002\u0000\u0000"+
		"\u00fe\u0115\u0003*\u0015\b\u00ff\u0100\n\u0006\u0000\u0000\u0100\u0101"+
		"\u0005!\u0000\u0000\u0101\u0115\u0003*\u0015\u0007\u0102\u0103\n\u0005"+
		"\u0000\u0000\u0103\u0104\u0005\"\u0000\u0000\u0104\u0115\u0003*\u0015"+
		"\u0006\u0105\u0106\n\u0004\u0000\u0000\u0106\u0107\u0007\u0003\u0000\u0000"+
		"\u0107\u0115\u0003*\u0015\u0005\u0108\u0109\n\u0003\u0000\u0000\u0109"+
		"\u010a\u0005\u001f\u0000\u0000\u010a\u0115\u0003*\u0015\u0004\u010b\u010c"+
		"\n\u0002\u0000\u0000\u010c\u010d\u0005 \u0000\u0000\u010d\u0115\u0003"+
		"*\u0015\u0003\u010e\u010f\n\u0001\u0000\u0000\u010f\u0110\u0005$\u0000"+
		"\u0000\u0110\u0111\u0003*\u0015\u0000\u0111\u0112\u0005%\u0000\u0000\u0112"+
		"\u0113\u0003*\u0015\u0002\u0113\u0115\u0001\u0000\u0000\u0000\u0114\u00f9"+
		"\u0001\u0000\u0000\u0000\u0114\u00fc\u0001\u0000\u0000\u0000\u0114\u00ff"+
		"\u0001\u0000\u0000\u0000\u0114\u0102\u0001\u0000\u0000\u0000\u0114\u0105"+
		"\u0001\u0000\u0000\u0000\u0114\u0108\u0001\u0000\u0000\u0000\u0114\u010b"+
		"\u0001\u0000\u0000\u0000\u0114\u010e\u0001\u0000\u0000\u0000\u0115\u0118"+
		"\u0001\u0000\u0000\u0000\u0116\u0114\u0001\u0000\u0000\u0000\u0116\u0117"+
		"\u0001\u0000\u0000\u0000\u0117+\u0001\u0000\u0000\u0000\u0118\u0116\u0001"+
		"\u0000\u0000\u0000\u0119\u011e\u0005-\u0000\u0000\u011a\u011b\u0005\u001a"+
		"\u0000\u0000\u011b\u011d\u0005-\u0000\u0000\u011c\u011a\u0001\u0000\u0000"+
		"\u0000\u011d\u0120\u0001\u0000\u0000\u0000\u011e\u011c\u0001\u0000\u0000"+
		"\u0000\u011e\u011f\u0001\u0000\u0000\u0000\u011f-\u0001\u0000\u0000\u0000"+
		"\u0120\u011e\u0001\u0000\u0000\u0000\u0121\u0123\u0005\u0018\u0000\u0000"+
		"\u0122\u0124\u0003&\u0013\u0000\u0123\u0122\u0001\u0000\u0000\u0000\u0123"+
		"\u0124\u0001\u0000\u0000\u0000\u0124\u0125\u0001\u0000\u0000\u0000\u0125"+
		"\u0126\u0005\u0019\u0000\u0000\u0126/\u0001\u0000\u0000\u0000\u001f3A"+
		"FNTZ^ekx|\u0088\u008b\u0094\u009b\u00a1\u00ad\u00b3\u00b6\u00bb\u00bf"+
		"\u00c4\u00cd\u00d1\u00d8\u00e5\u00f7\u0114\u0116\u011e\u0123";
	private static final String[] _LITERAL_NAMES = makeLiteralNames();

	private static String[] makeRuleNames() {
		return new String[] {
			"file", "definition", "importDecl", "exportDecl", "externDecl", "directiveCall",
			"macroDecl", "subDecl", "logicControlDecl", "elseIfClause", "elseClause",
			"paramList", "param", "functionBlock", "statements", "variableDecl",
			"funtionCallDecl", "funtionBlockDecl", "useDecl", "valueList", "type",
			"expression", "fieldAccess", "arrayValue"
		};
	}

	private static String[] makeLiteralNames() {
		return new String[] {
			null, "'import'", "'extern'", "'export'", "'sub'", "'use'", "'!'", null,
			null, "'int'", "'float'", "'bool'", "'string'", "'void'", "'vec2'", "'vec3'",
			"'vec4'", "'('", "')'", "'{'", "'}'", "';'", "','", "'='", "'['", "']'",
			"'.'", "'+'", "'-'", "'*'", "'/'", "'&&'", "'||'", "'|'", "'&'", "'~'",
			"'?'", "':'", "'>'", "'<'", "'=='", "'!='", "'>='", "'<='"
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

	@SuppressWarnings("CheckReturnValue")
	public static class FileContext extends ParserRuleContext {
		public TerminalNode EOF() { return getToken(TechlandScriptParser.EOF, 0); }
		public List<DefinitionContext> definition() {
			return getRuleContexts(DefinitionContext.class);
		}
		public DefinitionContext definition(int i) {
			return getRuleContext(DefinitionContext.class,i);
		}
		public FileContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
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
	}

	private static String[] makeSymbolicNames() {
		return new String[] {
			null, "Import", "Extern", "Export", "Sub", "Use", "Exclamation", "KwIf",
			"KwElse", "KwInt", "KwFloat", "KwBool", "KwString", "KwVoid", "KwVec2",
			"KwVec3", "KwVec4", "LParen", "RParen", "LBrace", "RBrace", "Semicolon",
			"Comma", "Equals", "LBracket", "RBracket", "Dot", "Plus", "Minus", "Mul",
			"Div", "LogicAnd", "LogicOr", "BitOr", "BitAnd", "BitNot", "Question",
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
			setState(51);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while ((((_la) & ~0x3f) == 0 && ((1L << _la) & 105553116397150L) != 0)) {
				{
				{
				setState(48);
				definition();
				}
				}
				setState(53);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(54);
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
			setState(65);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,1,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(56);
				importDecl();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(57);
				exportDecl();
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(58);
				externDecl();
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(59);
				subDecl();
				}
				break;
			case 5:
				enterOuterAlt(_localctx, 5);
				{
				setState(60);
				directiveCall();
				}
				break;
			case 6:
				enterOuterAlt(_localctx, 6);
				{
				setState(61);
				macroDecl();
				}
				break;
			case 7:
				enterOuterAlt(_localctx, 7);
				{
				setState(62);
				variableDecl();
				}
				break;
			case 8:
				enterOuterAlt(_localctx, 8);
				{
				setState(63);
				funtionCallDecl();
				}
				break;
			case 9:
				enterOuterAlt(_localctx, 9);
				{
				setState(64);
				funtionBlockDecl();
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

	@SuppressWarnings("CheckReturnValue")
	public static class ImportDeclContext extends ParserRuleContext {
		public TerminalNode Import() { return getToken(TechlandScriptParser.Import, 0); }
		public TerminalNode String() { return getToken(TechlandScriptParser.String, 0); }
		public TerminalNode Semicolon() { return getToken(TechlandScriptParser.Semicolon, 0); }
		public ImportDeclContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_importDecl; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TechlandScriptListener ) ((TechlandScriptListener)listener).enterImportDecl(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TechlandScriptListener ) ((TechlandScriptListener)listener).exitImportDecl(this);
		}
	}

	public final ImportDeclContext importDecl() throws RecognitionException {
		ImportDeclContext _localctx = new ImportDeclContext(_ctx, getState());
		enterRule(_localctx, 4, RULE_importDecl);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(67);
			match(Import);
			setState(68);
			match(String);
			setState(70);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==Semicolon) {
				{
				setState(69);
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
	public static class ExportDeclContext extends ParserRuleContext {
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
		public ExportDeclContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_exportDecl; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TechlandScriptListener ) ((TechlandScriptListener)listener).enterExportDecl(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TechlandScriptListener ) ((TechlandScriptListener)listener).exitExportDecl(this);
		}
	}

	public final ExportDeclContext exportDecl() throws RecognitionException {
		ExportDeclContext _localctx = new ExportDeclContext(_ctx, getState());
		enterRule(_localctx, 6, RULE_exportDecl);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(72);
			match(Export);
			setState(73);
			type();
			setState(74);
			match(Id);
			setState(75);
			match(Equals);
			setState(76);
			expression(0);
			setState(78);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==Semicolon) {
				{
				setState(77);
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
	public static class ExternDeclContext extends ParserRuleContext {
		public TerminalNode Extern() { return getToken(TechlandScriptParser.Extern, 0); }
		public TypeContext type() {
			return getRuleContext(TypeContext.class,0);
		}
		public TerminalNode Id() { return getToken(TechlandScriptParser.Id, 0); }
		public TerminalNode Semicolon() { return getToken(TechlandScriptParser.Semicolon, 0); }
		public ExternDeclContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_externDecl; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TechlandScriptListener ) ((TechlandScriptListener)listener).enterExternDecl(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TechlandScriptListener ) ((TechlandScriptListener)listener).exitExternDecl(this);
		}
	}

	public final ExternDeclContext externDecl() throws RecognitionException {
		ExternDeclContext _localctx = new ExternDeclContext(_ctx, getState());
		enterRule(_localctx, 8, RULE_externDecl);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(80);
			match(Extern);
			setState(81);
			type();
			setState(82);
			match(Id);
			setState(84);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==Semicolon) {
				{
				setState(83);
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
	public static class DirectiveCallContext extends ParserRuleContext {
		public TerminalNode Exclamation() { return getToken(TechlandScriptParser.Exclamation, 0); }
		public TerminalNode Id() { return getToken(TechlandScriptParser.Id, 0); }
		public TerminalNode LParen() { return getToken(TechlandScriptParser.LParen, 0); }
		public TerminalNode RParen() { return getToken(TechlandScriptParser.RParen, 0); }
		public ValueListContext valueList() {
			return getRuleContext(ValueListContext.class,0);
		}
		public TerminalNode Semicolon() { return getToken(TechlandScriptParser.Semicolon, 0); }
		public DirectiveCallContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_directiveCall; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TechlandScriptListener ) ((TechlandScriptListener)listener).enterDirectiveCall(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TechlandScriptListener ) ((TechlandScriptListener)listener).exitDirectiveCall(this);
		}
	}

	public final DirectiveCallContext directiveCall() throws RecognitionException {
		DirectiveCallContext _localctx = new DirectiveCallContext(_ctx, getState());
		enterRule(_localctx, 10, RULE_directiveCall);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(86);
			match(Exclamation);
			setState(87);
			match(Id);
			setState(88);
			match(LParen);
			setState(90);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if ((((_la) & ~0x3f) == 0 && ((1L << _la) & 475023668281408L) != 0)) {
				{
				setState(89);
				valueList();
				}
			}

			setState(92);
			match(RParen);
			setState(94);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==Semicolon) {
				{
				setState(93);
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
	public static class MacroDeclContext extends ParserRuleContext {
		public TerminalNode MacroId() { return getToken(TechlandScriptParser.MacroId, 0); }
		public TerminalNode LParen() { return getToken(TechlandScriptParser.LParen, 0); }
		public ValueListContext valueList() {
			return getRuleContext(ValueListContext.class,0);
		}
		public TerminalNode RParen() { return getToken(TechlandScriptParser.RParen, 0); }
		public TerminalNode Semicolon() { return getToken(TechlandScriptParser.Semicolon, 0); }
		public MacroDeclContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_macroDecl; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TechlandScriptListener ) ((TechlandScriptListener)listener).enterMacroDecl(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TechlandScriptListener ) ((TechlandScriptListener)listener).exitMacroDecl(this);
		}
	}

	public final MacroDeclContext macroDecl() throws RecognitionException {
		MacroDeclContext _localctx = new MacroDeclContext(_ctx, getState());
		enterRule(_localctx, 12, RULE_macroDecl);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(96);
			match(MacroId);
			setState(97);
			match(LParen);
			setState(98);
			valueList();
			setState(99);
			match(RParen);
			setState(101);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==Semicolon) {
				{
				setState(100);
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
	public static class SubDeclContext extends ParserRuleContext {
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
		public SubDeclContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
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
	}

	public final SubDeclContext subDecl() throws RecognitionException {
		SubDeclContext _localctx = new SubDeclContext(_ctx, getState());
		enterRule(_localctx, 14, RULE_subDecl);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(103);
			match(Sub);
			setState(104);
			match(Id);
			setState(105);
			match(LParen);
			setState(107);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if ((((_la) & ~0x3f) == 0 && ((1L << _la) & 35184372219392L) != 0)) {
				{
				setState(106);
				paramList();
				}
			}

			setState(109);
			match(RParen);
			setState(110);
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

	public final LogicControlDeclContext logicControlDecl() throws RecognitionException {
		LogicControlDeclContext _localctx = new LogicControlDeclContext(_ctx, getState());
		enterRule(_localctx, 16, RULE_logicControlDecl);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(112);
			match(KwIf);
			setState(113);
			match(LParen);
			setState(114);
			expression(0);
			setState(115);
			match(RParen);
			setState(116);
			functionBlock();
			setState(120);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,9,_ctx);
			while ( _alt!=2 && _alt!= ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(117);
					elseIfClause();
					}
					}
				}
				setState(122);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,9,_ctx);
			}
			setState(124);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==KwElse) {
				{
				setState(123);
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

	public final ElseClauseContext elseClause() throws RecognitionException {
		ElseClauseContext _localctx = new ElseClauseContext(_ctx, getState());
		enterRule(_localctx, 20, RULE_elseClause);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(133);
			match(KwElse);
			setState(139);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==LParen) {
				{
				setState(134);
				match(LParen);
				setState(136);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if ((((_la) & ~0x3f) == 0 && ((1L << _la) & 475023668281408L) != 0)) {
					{
					setState(135);
					expression(0);
					}
				}

				setState(138);
				match(RParen);
				}
			}

			setState(141);
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

	public final ParamListContext paramList() throws RecognitionException {
		ParamListContext _localctx = new ParamListContext(_ctx, getState());
		enterRule(_localctx, 22, RULE_paramList);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(143);
			param();
			setState(148);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==Comma) {
				{
				{
				setState(144);
				match(Comma);
				setState(145);
				param();
				}
				}
				setState(150);
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

	public final ElseIfClauseContext elseIfClause() throws RecognitionException {
		ElseIfClauseContext _localctx = new ElseIfClauseContext(_ctx, getState());
		enterRule(_localctx, 18, RULE_elseIfClause);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(126);
			match(KwElse);
			setState(127);
			match(KwIf);
			setState(128);
			match(LParen);
			setState(129);
			expression(0);
			setState(130);
			match(RParen);
			setState(131);
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

	public final ParamContext param() throws RecognitionException {
		ParamContext _localctx = new ParamContext(_ctx, getState());
		enterRule(_localctx, 24, RULE_param);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(151);
			type();
			setState(152);
			match(Id);
			setState(155);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==Equals) {
				{
				setState(153);
				match(Equals);
				setState(154);
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

	public final FunctionBlockContext functionBlock() throws RecognitionException {
		FunctionBlockContext _localctx = new FunctionBlockContext(_ctx, getState());
		enterRule(_localctx, 26, RULE_functionBlock);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(157);
			match(LBrace);
			setState(161);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while ((((_la) & ~0x3f) == 0 && ((1L << _la) & 105553116397220L) != 0)) {
				{
				{
				setState(158);
				statements();
				}
				}
				setState(163);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(164);
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

	public final StatementsContext statements() throws RecognitionException {
		StatementsContext _localctx = new StatementsContext(_ctx, getState());
		enterRule(_localctx, 28, RULE_statements);
		try {
			setState(173);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,16,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(166);
				funtionCallDecl();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(167);
				funtionBlockDecl();
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(168);
				useDecl();
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(169);
				variableDecl();
				}
				break;
			case 5:
				enterOuterAlt(_localctx, 5);
				{
				setState(170);
				externDecl();
				}
				break;
			case 6:
				enterOuterAlt(_localctx, 6);
				{
				setState(171);
				logicControlDecl();
				}
				break;
			case 7:
				enterOuterAlt(_localctx, 7);
				{
				setState(172);
				macroDecl();
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

	public final VariableDeclContext variableDecl() throws RecognitionException {
		VariableDeclContext _localctx = new VariableDeclContext(_ctx, getState());
		enterRule(_localctx, 30, RULE_variableDecl);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(175);
			type();
			setState(176);
			match(Id);
			setState(179);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==Equals) {
				{
				setState(177);
				match(Equals);
				setState(178);
				expression(0);
				}
			}

			setState(182);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==Semicolon) {
				{
				setState(181);
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

	public final FuntionCallDeclContext funtionCallDecl() throws RecognitionException {
		FuntionCallDeclContext _localctx = new FuntionCallDeclContext(_ctx, getState());
		enterRule(_localctx, 32, RULE_funtionCallDecl);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(184);
			match(Id);
			setState(185);
			match(LParen);
			setState(187);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if ((((_la) & ~0x3f) == 0 && ((1L << _la) & 475023668281408L) != 0)) {
				{
				setState(186);
				valueList();
				}
			}

			setState(189);
			match(RParen);
			setState(191);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==Semicolon) {
				{
				setState(190);
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

	public final FuntionBlockDeclContext funtionBlockDecl() throws RecognitionException {
		FuntionBlockDeclContext _localctx = new FuntionBlockDeclContext(_ctx, getState());
		enterRule(_localctx, 34, RULE_funtionBlockDecl);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(193);
			match(Id);
			setState(194);
			match(LParen);
			setState(196);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if ((((_la) & ~0x3f) == 0 && ((1L << _la) & 475023668281408L) != 0)) {
				{
				setState(195);
				valueList();
				}
			}

			setState(198);
			match(RParen);
			setState(199);
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

	public final UseDeclContext useDecl() throws RecognitionException {
		UseDeclContext _localctx = new UseDeclContext(_ctx, getState());
		enterRule(_localctx, 36, RULE_useDecl);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(201);
			match(Use);
			setState(202);
			match(Id);
			setState(203);
			match(LParen);
			setState(205);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if ((((_la) & ~0x3f) == 0 && ((1L << _la) & 475023668281408L) != 0)) {
				{
				setState(204);
				valueList();
				}
			}

			setState(207);
			match(RParen);
			setState(209);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==Semicolon) {
				{
				setState(208);
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

	public final ValueListContext valueList() throws RecognitionException {
		ValueListContext _localctx = new ValueListContext(_ctx, getState());
		enterRule(_localctx, 38, RULE_valueList);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(211);
			expression(0);
			setState(216);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==Comma) {
				{
				{
				setState(212);
				match(Comma);
				setState(213);
				expression(0);
				}
				}
				setState(218);
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

	public final TypeContext type() throws RecognitionException {
		TypeContext _localctx = new TypeContext(_ctx, getState());
		enterRule(_localctx, 40, RULE_type);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(219);
			_la = _input.LA(1);
			if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & 35184372219392L) != 0)) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
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

	private ExpressionContext expression(int _p) throws RecognitionException {
		ParserRuleContext _parentctx = _ctx;
		int _parentState = getState();
		ExpressionContext _localctx = new ExpressionContext(_ctx, _parentState);
		ExpressionContext _prevctx = _localctx;
		int _startState = 42;
		enterRecursionRule(_localctx, 42, RULE_expression, _p);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(247);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,26,_ctx) ) {
			case 1:
				{
				setState(222);
				match(LParen);
				setState(223);
				expression(0);
				setState(224);
				match(RParen);
				}
				break;
			case 2:
				{
				setState(226);
				fieldAccess();
				setState(227);
				match(LParen);
				setState(229);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if ((((_la) & ~0x3f) == 0 && ((1L << _la) & 475023668281408L) != 0)) {
					{
					setState(228);
					valueList();
					}
				}

				setState(231);
				match(RParen);
				}
				break;
			case 3:
				{
				setState(233);
				fieldAccess();
				}
				break;
			case 4:
				{
				setState(234);
				match(Number);
				}
				break;
			case 5:
				{
				setState(235);
				match(String);
				}
				break;
			case 6:
				{
				setState(236);
				match(Bool);
				}
				break;
			case 7:
				{
				setState(237);
				arrayValue();
				}
				break;
			case 8:
				{
				setState(238);
				match(Id);
				setState(239);
				match(Equals);
				setState(240);
				expression(12);
				}
				break;
			case 9:
				{
				setState(241);
				match(BitNot);
				setState(242);
				expression(11);
				}
				break;
			case 10:
				{
				setState(243);
				match(Exclamation);
				setState(244);
				expression(10);
				}
				break;
			case 11:
				{
				setState(245);
				match(Minus);
				setState(246);
				expression(9);
				}
				break;
			}
			_ctx.stop = _input.LT(-1);
			setState(278);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,28,_ctx);
			while ( _alt!=2 && _alt!= ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					if ( _parseListeners!=null ) triggerExitRuleEvent();
					_prevctx = _localctx;
					{
					setState(276);
					_errHandler.sync(this);
					switch ( getInterpreter().adaptivePredict(_input,27,_ctx) ) {
					case 1:
						{
						_localctx = new ExpressionContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(249);
						if (!(precpred(_ctx, 8))) throw new FailedPredicateException(this, "precpred(_ctx, 8)");
						setState(250);
						_la = _input.LA(1);
						if ( !(_la==Mul || _la==Div) ) {
						_errHandler.recoverInline(this);
						}
						else {
							if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
							_errHandler.reportMatch(this);
							consume();
						}
						setState(251);
						expression(9);
						}
						break;
					case 2:
						{
						_localctx = new ExpressionContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(252);
						if (!(precpred(_ctx, 7))) throw new FailedPredicateException(this, "precpred(_ctx, 7)");
						setState(253);
						_la = _input.LA(1);
						if ( !(_la==Plus || _la==Minus) ) {
						_errHandler.recoverInline(this);
						}
						else {
							if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
							_errHandler.reportMatch(this);
							consume();
						}
						setState(254);
						expression(8);
						}
						break;
					case 3:
						{
						_localctx = new ExpressionContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(255);
						if (!(precpred(_ctx, 6))) throw new FailedPredicateException(this, "precpred(_ctx, 6)");
						setState(256);
						match(BitOr);
						setState(257);
						expression(7);
						}
						break;
					case 4:
						{
						_localctx = new ExpressionContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(258);
						if (!(precpred(_ctx, 5))) throw new FailedPredicateException(this, "precpred(_ctx, 5)");
						setState(259);
						match(BitAnd);
						setState(260);
						expression(6);
						}
						break;
					case 5:
						{
						_localctx = new ExpressionContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(261);
						if (!(precpred(_ctx, 4))) throw new FailedPredicateException(this, "precpred(_ctx, 4)");
						setState(262);
						_la = _input.LA(1);
						if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & 17317308137472L) != 0)) ) {
						_errHandler.recoverInline(this);
						}
						else {
							if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
							_errHandler.reportMatch(this);
							consume();
						}
						setState(263);
						expression(5);
						}
						break;
					case 6:
						{
						_localctx = new ExpressionContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(264);
						if (!(precpred(_ctx, 3))) throw new FailedPredicateException(this, "precpred(_ctx, 3)");
						setState(265);
						match(LogicAnd);
						setState(266);
						expression(4);
						}
						break;
					case 7:
						{
						_localctx = new ExpressionContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(267);
						if (!(precpred(_ctx, 2))) throw new FailedPredicateException(this, "precpred(_ctx, 2)");
						setState(268);
						match(LogicOr);
						setState(269);
						expression(3);
						}
						break;
					case 8:
						{
						_localctx = new ExpressionContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(270);
						if (!(precpred(_ctx, 1))) throw new FailedPredicateException(this, "precpred(_ctx, 1)");
						setState(271);
						match(Question);
						setState(272);
						expression(0);
						setState(273);
						match(Colon);
						setState(274);
						expression(2);
						}
						break;
					}
					}
				}
				setState(280);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,28,_ctx);
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

	public final FieldAccessContext fieldAccess() throws RecognitionException {
		FieldAccessContext _localctx = new FieldAccessContext(_ctx, getState());
		enterRule(_localctx, 44, RULE_fieldAccess);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(281);
			match(Id);
			setState(286);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,29,_ctx);
			while ( _alt!=2 && _alt!= ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(282);
					match(Dot);
					setState(283);
					match(Id);
					}
					}
				}
				setState(288);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,29,_ctx);
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

	public final ArrayValueContext arrayValue() throws RecognitionException {
		ArrayValueContext _localctx = new ArrayValueContext(_ctx, getState());
		enterRule(_localctx, 46, RULE_arrayValue);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(289);
			match(LBracket);
			setState(291);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if ((((_la) & ~0x3f) == 0 && ((1L << _la) & 475023668281408L) != 0)) {
				{
				setState(290);
				valueList();
				}
			}

			setState(293);
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

		public SubDeclContext subDecl() {
			return getRuleContext(SubDeclContext.class,0);
		}

		public DirectiveCallContext directiveCall() {
			return getRuleContext(DirectiveCallContext.class,0);
		}

		public MacroDeclContext macroDecl() {
			return getRuleContext(MacroDeclContext.class,0);
		}

		public VariableDeclContext variableDecl() {
			return getRuleContext(VariableDeclContext.class,0);
		}

		public FuntionCallDeclContext funtionCallDecl() {
			return getRuleContext(FuntionCallDeclContext.class,0);
		}

		public FuntionBlockDeclContext funtionBlockDecl() {
			return getRuleContext(FuntionBlockDeclContext.class,0);
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
	}

	@SuppressWarnings("CheckReturnValue")
	public static class ElseIfClauseContext extends ParserRuleContext {
		public ElseIfClauseContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}

		public TerminalNode KwElse() { return getToken(TechlandScriptParser.KwElse, 0); }

		public TerminalNode KwIf() { return getToken(TechlandScriptParser.KwIf, 0); }

		public TerminalNode LParen() { return getToken(TechlandScriptParser.LParen, 0); }

		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}

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
	}

	@SuppressWarnings("CheckReturnValue")
	public static class ElseClauseContext extends ParserRuleContext {
		public ElseClauseContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}

		public TerminalNode KwElse() { return getToken(TechlandScriptParser.KwElse, 0); }

		public FunctionBlockContext functionBlock() {
			return getRuleContext(FunctionBlockContext.class,0);
		}

		public TerminalNode LParen() { return getToken(TechlandScriptParser.LParen, 0); }

		public TerminalNode RParen() { return getToken(TechlandScriptParser.RParen, 0); }

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
	}

	@SuppressWarnings("CheckReturnValue")
	public static class ParamContext extends ParserRuleContext {
		public ParamContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}

		public TypeContext type() {
			return getRuleContext(TypeContext.class,0);
		}

		public TerminalNode Id() { return getToken(TechlandScriptParser.Id, 0); }

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
	}

	@SuppressWarnings("CheckReturnValue")
	public static class StatementsContext extends ParserRuleContext {
		public StatementsContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}

		public FuntionCallDeclContext funtionCallDecl() {
			return getRuleContext(FuntionCallDeclContext.class,0);
		}

		public FuntionBlockDeclContext funtionBlockDecl() {
			return getRuleContext(FuntionBlockDeclContext.class,0);
		}

		public UseDeclContext useDecl() {
			return getRuleContext(UseDeclContext.class,0);
		}

		public VariableDeclContext variableDecl() {
			return getRuleContext(VariableDeclContext.class,0);
		}

		public ExternDeclContext externDecl() {
			return getRuleContext(ExternDeclContext.class,0);
		}

		public LogicControlDeclContext logicControlDecl() {
			return getRuleContext(LogicControlDeclContext.class,0);
		}

		public MacroDeclContext macroDecl() {
			return getRuleContext(MacroDeclContext.class,0);
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
	}

	@SuppressWarnings("CheckReturnValue")
	public static class VariableDeclContext extends ParserRuleContext {
		public TypeContext type() {
			return getRuleContext(TypeContext.class,0);
		}
		public TerminalNode Id() { return getToken(TechlandScriptParser.Id, 0); }
		public TerminalNode Equals() { return getToken(TechlandScriptParser.Equals, 0); }
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public VariableDeclContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}

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
	}

	@SuppressWarnings("CheckReturnValue")
	public static class FuntionCallDeclContext extends ParserRuleContext {
		public FuntionCallDeclContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}

		public TerminalNode Id() { return getToken(TechlandScriptParser.Id, 0); }

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
	}

	@SuppressWarnings("CheckReturnValue")
	public static class FuntionBlockDeclContext extends ParserRuleContext {
		public FuntionBlockDeclContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}

		public TerminalNode Id() { return getToken(TechlandScriptParser.Id, 0); }

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
	}

	@SuppressWarnings("CheckReturnValue")
	public static class UseDeclContext extends ParserRuleContext {
		public UseDeclContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}

		public TerminalNode Use() { return getToken(TechlandScriptParser.Use, 0); }

		public TerminalNode Id() { return getToken(TechlandScriptParser.Id, 0); }

		public TerminalNode LParen() { return getToken(TechlandScriptParser.LParen, 0); }

		public TerminalNode RParen() { return getToken(TechlandScriptParser.RParen, 0); }

		public ValueListContext valueList() {
			return getRuleContext(ValueListContext.class,0);
		}

		public TerminalNode Semicolon() { return getToken(TechlandScriptParser.Semicolon, 0); }

		@Override public int getRuleIndex() { return RULE_useDecl; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TechlandScriptListener ) ((TechlandScriptListener)listener).enterUseDecl(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TechlandScriptListener ) ((TechlandScriptListener)listener).exitUseDecl(this);
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
	}

	@SuppressWarnings("CheckReturnValue")
	public static class TypeContext extends ParserRuleContext {
		public TypeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}

		public TerminalNode KwInt() { return getToken(TechlandScriptParser.KwInt, 0); }

		public TerminalNode KwFloat() { return getToken(TechlandScriptParser.KwFloat, 0); }

		public TerminalNode KwBool() { return getToken(TechlandScriptParser.KwBool, 0); }

		public TerminalNode KwString() { return getToken(TechlandScriptParser.KwString, 0); }

		public TerminalNode KwVoid() { return getToken(TechlandScriptParser.KwVoid, 0); }

		public TerminalNode KwVec2() { return getToken(TechlandScriptParser.KwVec2, 0); }

		public TerminalNode KwVec3() { return getToken(TechlandScriptParser.KwVec3, 0); }
		public TerminalNode Id() { return getToken(TechlandScriptParser.Id, 0); }

		public TerminalNode KwVec4() { return getToken(TechlandScriptParser.KwVec4, 0); }

		@Override public int getRuleIndex() { return RULE_type; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TechlandScriptListener ) ((TechlandScriptListener)listener).enterType(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TechlandScriptListener ) ((TechlandScriptListener)listener).exitType(this);
		}
	}

	@SuppressWarnings("CheckReturnValue")
	public static class ExpressionContext extends ParserRuleContext {
		public TerminalNode LParen() { return getToken(TechlandScriptParser.LParen, 0); }
		public ExpressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}

		public List<ExpressionContext> expression() {
			return getRuleContexts(ExpressionContext.class);
		}
		public TerminalNode RParen() { return getToken(TechlandScriptParser.RParen, 0); }

		public ExpressionContext expression(int i) {
			return getRuleContext(ExpressionContext.class,i);
		}
		public ValueListContext valueList() {
			return getRuleContext(ValueListContext.class,0);
		}

		public FieldAccessContext fieldAccess() {
			return getRuleContext(FieldAccessContext.class,0);
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

		@Override public int getRuleIndex() { return RULE_expression; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TechlandScriptListener ) ((TechlandScriptListener)listener).enterExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TechlandScriptListener ) ((TechlandScriptListener)listener).exitExpression(this);
		}
	}

	@SuppressWarnings("CheckReturnValue")
	public static class FieldAccessContext extends ParserRuleContext {
		public FieldAccessContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}

		public List<TerminalNode> Id() { return getTokens(TechlandScriptParser.Id); }

		public TerminalNode Id(int i) {
			return getToken(TechlandScriptParser.Id, i);
		}

		public List<TerminalNode> Dot() { return getTokens(TechlandScriptParser.Dot); }

		public TerminalNode Dot(int i) {
			return getToken(TechlandScriptParser.Dot, i);
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
	}

	public boolean sempred(RuleContext _localctx, int ruleIndex, int predIndex) {
		switch (ruleIndex) {
		case 21:
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