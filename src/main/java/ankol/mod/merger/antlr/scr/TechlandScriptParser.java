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
		RULE_variableDecl = 15, RULE_variableAssignDecl = 16, RULE_functionCallDecl = 17,
		RULE_methodReferenceFunCallDecl = 18, RULE_functionBlockDecl = 19, RULE_useDecl = 20,
		RULE_valueList = 21, RULE_type = 22, RULE_expression = 23, RULE_fieldAccess = 24,
		RULE_arrayValue = 25;
	public static final String _serializedATN =
		"\u0004\u0001,\u0147\u0002\u0000\u0007\u0000\u0002\u0001\u0007\u0001\u0002"+
		"\u0002\u0007\u0002\u0002\u0003\u0007\u0003\u0002\u0004\u0007\u0004\u0002"+
		"\u0005\u0007\u0005\u0002\u0006\u0007\u0006\u0002\u0007\u0007\u0007\u0002"+
		"\b\u0007\b\u0002\t\u0007\t\u0002\n\u0007\n\u0002\u000b\u0007\u000b\u0002"+
		"\f\u0007\f\u0002\r\u0007\r\u0002\u000e\u0007\u000e\u0002\u000f\u0007\u000f"+
		"\u0002\u0010\u0007\u0010\u0002\u0011\u0007\u0011\u0002\u0012\u0007\u0012"+
		"\u0002\u0013\u0007\u0013\u0002\u0014\u0007\u0014\u0002\u0015\u0007\u0015"+
		"\u0002\u0016\u0007\u0016\u0002\u0017\u0007\u0017\u0002\u0018\u0007\u0018"+
		"\u0002\u0019\u0007\u0019\u0001\u0000\u0005\u00006\b\u0000\n\u0000\f\u0000"+
		"9\t\u0000\u0001\u0000\u0001\u0000\u0001\u0001\u0001\u0001\u0001\u0001"+
		"\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001"+
		"\u0001\u0001\u0003\u0001G\b\u0001\u0001\u0002\u0001\u0002\u0001\u0002"+
		"\u0003\u0002L\b\u0002\u0001\u0003\u0001\u0003\u0001\u0003\u0001\u0003"+
		"\u0001\u0003\u0001\u0003\u0003\u0003T\b\u0003\u0001\u0004\u0001\u0004"+
		"\u0001\u0004\u0001\u0004\u0003\u0004Z\b\u0004\u0001\u0005\u0001\u0005"+
		"\u0001\u0005\u0001\u0005\u0003\u0005`\b\u0005\u0001\u0005\u0001\u0005"+
		"\u0003\u0005d\b\u0005\u0001\u0006\u0001\u0006\u0001\u0006\u0001\u0006"+
		"\u0001\u0006\u0003\u0006k\b\u0006\u0001\u0007\u0001\u0007\u0001\u0007"+
		"\u0001\u0007\u0003\u0007q\b\u0007\u0001\u0007\u0001\u0007\u0001\u0007"+
		"\u0001\b\u0001\b\u0001\b\u0001\b\u0001\b\u0005\b{\b\b\n\b\f\b~\t\b\u0001"+
		"\b\u0001\b\u0001\b\u0005\b\u0083\b\b\n\b\f\b\u0086\t\b\u0001\b\u0003\b"+
		"\u0089\b\b\u0001\t\u0001\t\u0001\t\u0001\t\u0001\t\u0001\t\u0001\t\u0001"+
		"\n\u0001\n\u0001\n\u0003\n\u0095\b\n\u0001\n\u0003\n\u0098\b\n\u0001\n"+
		"\u0001\n\u0001\u000b\u0001\u000b\u0001\u000b\u0005\u000b\u009f\b\u000b"+
		"\n\u000b\f\u000b\u00a2\t\u000b\u0001\f\u0001\f\u0001\f\u0001\f\u0003\f"+
		"\u00a8\b\f\u0001\r\u0001\r\u0005\r\u00ac\b\r\n\r\f\r\u00af\t\r\u0001\r"+
		"\u0001\r\u0001\u000e\u0001\u000e\u0001\u000e\u0001\u000e\u0001\u000e\u0001"+
		"\u000e\u0001\u000e\u0001\u000e\u0001\u000e\u0003\u000e\u00bc\b\u000e\u0001"+
		"\u000f\u0001\u000f\u0001\u000f\u0001\u000f\u0003\u000f\u00c2\b\u000f\u0001"+
		"\u000f\u0003\u000f\u00c5\b\u000f\u0001\u0010\u0001\u0010\u0001\u0010\u0001"+
		"\u0010\u0003\u0010\u00cb\b\u0010\u0001\u0011\u0001\u0011\u0001\u0011\u0003"+
		"\u0011\u00d0\b\u0011\u0001\u0011\u0001\u0011\u0003\u0011\u00d4\b\u0011"+
		"\u0001\u0012\u0001\u0012\u0001\u0012\u0001\u0012\u0001\u0012\u0003\u0012"+
		"\u00db\b\u0012\u0001\u0012\u0001\u0012\u0003\u0012\u00df\b\u0012\u0001"+
		"\u0013\u0001\u0013\u0001\u0013\u0003\u0013\u00e4\b\u0013\u0001\u0013\u0001"+
		"\u0013\u0001\u0013\u0001\u0014\u0001\u0014\u0001\u0014\u0001\u0014\u0003"+
		"\u0014\u00ed\b\u0014\u0001\u0014\u0001\u0014\u0003\u0014\u00f1\b\u0014"+
		"\u0001\u0015\u0001\u0015\u0001\u0015\u0005\u0015\u00f6\b\u0015\n\u0015"+
		"\f\u0015\u00f9\t\u0015\u0001\u0016\u0001\u0016\u0001\u0017\u0001\u0017"+
		"\u0001\u0017\u0001\u0017\u0001\u0017\u0001\u0017\u0001\u0017\u0001\u0017"+
		"\u0003\u0017\u0105\b\u0017\u0001\u0017\u0001\u0017\u0001\u0017\u0001\u0017"+
		"\u0001\u0017\u0001\u0017\u0001\u0017\u0001\u0017\u0001\u0017\u0001\u0017"+
		"\u0001\u0017\u0001\u0017\u0001\u0017\u0001\u0017\u0001\u0017\u0001\u0017"+
		"\u0003\u0017\u0117\b\u0017\u0001\u0017\u0001\u0017\u0001\u0017\u0001\u0017"+
		"\u0001\u0017\u0001\u0017\u0001\u0017\u0001\u0017\u0001\u0017\u0001\u0017"+
		"\u0001\u0017\u0001\u0017\u0001\u0017\u0001\u0017\u0001\u0017\u0001\u0017"+
		"\u0001\u0017\u0001\u0017\u0001\u0017\u0001\u0017\u0001\u0017\u0001\u0017"+
		"\u0001\u0017\u0001\u0017\u0001\u0017\u0001\u0017\u0001\u0017\u0005\u0017"+
		"\u0134\b\u0017\n\u0017\f\u0017\u0137\t\u0017\u0001\u0018\u0001\u0018\u0001"+
		"\u0018\u0005\u0018\u013c\b\u0018\n\u0018\f\u0018\u013f\t\u0018\u0001\u0019"+
		"\u0001\u0019\u0003\u0019\u0143\b\u0019\u0001\u0019\u0001\u0019\u0001\u0019"+
		"\u0000\u0001.\u001a\u0000\u0002\u0004\u0006\b\n\f\u000e\u0010\u0012\u0014"+
		"\u0016\u0018\u001a\u001c\u001e \"$&(*,.02\u0000\u0003\u0001\u0000\u0016"+
		"\u0017\u0001\u0000\u0014\u0015\u0001\u0000\u001f$\u016d\u00007\u0001\u0000"+
		"\u0000\u0000\u0002F\u0001\u0000\u0000\u0000\u0004H\u0001\u0000\u0000\u0000"+
		"\u0006M\u0001\u0000\u0000\u0000\bU\u0001\u0000\u0000\u0000\n[\u0001\u0000"+
		"\u0000\u0000\fe\u0001\u0000\u0000\u0000\u000el\u0001\u0000\u0000\u0000"+
		"\u0010u\u0001\u0000\u0000\u0000\u0012\u008a\u0001\u0000\u0000\u0000\u0014"+
		"\u0091\u0001\u0000\u0000\u0000\u0016\u009b\u0001\u0000\u0000\u0000\u0018"+
		"\u00a3\u0001\u0000\u0000\u0000\u001a\u00a9\u0001\u0000\u0000\u0000\u001c"+
		"\u00bb\u0001\u0000\u0000\u0000\u001e\u00bd\u0001\u0000\u0000\u0000 \u00c6"+
		"\u0001\u0000\u0000\u0000\"\u00cc\u0001\u0000\u0000\u0000$\u00d5\u0001"+
		"\u0000\u0000\u0000&\u00e0\u0001\u0000\u0000\u0000(\u00e8\u0001\u0000\u0000"+
		"\u0000*\u00f2\u0001\u0000\u0000\u0000,\u00fa\u0001\u0000\u0000\u0000."+
		"\u0116\u0001\u0000\u0000\u00000\u0138\u0001\u0000\u0000\u00002\u0140\u0001"+
		"\u0000\u0000\u000046\u0003\u0002\u0001\u000054\u0001\u0000\u0000\u0000"+
		"69\u0001\u0000\u0000\u000075\u0001\u0000\u0000\u000078\u0001\u0000\u0000"+
		"\u00008:\u0001\u0000\u0000\u000097\u0001\u0000\u0000\u0000:;\u0005\u0000"+
		"\u0000\u0001;\u0001\u0001\u0000\u0000\u0000<G\u0003\u0004\u0002\u0000"+
		"=G\u0003\u0006\u0003\u0000>G\u0003\b\u0004\u0000?G\u0003\n\u0005\u0000"+
		"@G\u0003\f\u0006\u0000AG\u0003\u000e\u0007\u0000BG\u0003\u001e\u000f\u0000"+
		"CG\u0003&\u0013\u0000DG\u0003$\u0012\u0000EG\u0003\"\u0011\u0000F<\u0001"+
		"\u0000\u0000\u0000F=\u0001\u0000\u0000\u0000F>\u0001\u0000\u0000\u0000"+
		"F?\u0001\u0000\u0000\u0000F@\u0001\u0000\u0000\u0000FA\u0001\u0000\u0000"+
		"\u0000FB\u0001\u0000\u0000\u0000FC\u0001\u0000\u0000\u0000FD\u0001\u0000"+
		"\u0000\u0000FE\u0001\u0000\u0000\u0000G\u0003\u0001\u0000\u0000\u0000"+
		"HI\u0005\u0001\u0000\u0000IK\u0005)\u0000\u0000JL\u0005\r\u0000\u0000"+
		"KJ\u0001\u0000\u0000\u0000KL\u0001\u0000\u0000\u0000L\u0005\u0001\u0000"+
		"\u0000\u0000MN\u0005\u0003\u0000\u0000NO\u0003,\u0016\u0000OP\u0005&\u0000"+
		"\u0000PQ\u0005\u000f\u0000\u0000QS\u0003.\u0017\u0000RT\u0005\r\u0000"+
		"\u0000SR\u0001\u0000\u0000\u0000ST\u0001\u0000\u0000\u0000T\u0007\u0001"+
		"\u0000\u0000\u0000UV\u0005\u0002\u0000\u0000VW\u0003,\u0016\u0000WY\u0005"+
		"&\u0000\u0000XZ\u0005\r\u0000\u0000YX\u0001\u0000\u0000\u0000YZ\u0001"+
		"\u0000\u0000\u0000Z\t\u0001\u0000\u0000\u0000[\\\u0005\u0006\u0000\u0000"+
		"\\]\u0005&\u0000\u0000]_\u0005\t\u0000\u0000^`\u0003*\u0015\u0000_^\u0001"+
		"\u0000\u0000\u0000_`\u0001\u0000\u0000\u0000`a\u0001\u0000\u0000\u0000"+
		"ac\u0005\n\u0000\u0000bd\u0005\r\u0000\u0000cb\u0001\u0000\u0000\u0000"+
		"cd\u0001\u0000\u0000\u0000d\u000b\u0001\u0000\u0000\u0000ef\u0005\'\u0000"+
		"\u0000fg\u0005\t\u0000\u0000gh\u0003*\u0015\u0000hj\u0005\n\u0000\u0000"+
		"ik\u0005\r\u0000\u0000ji\u0001\u0000\u0000\u0000jk\u0001\u0000\u0000\u0000"+
		"k\r\u0001\u0000\u0000\u0000lm\u0005\u0004\u0000\u0000mn\u0005&\u0000\u0000"+
		"np\u0005\t\u0000\u0000oq\u0003\u0016\u000b\u0000po\u0001\u0000\u0000\u0000"+
		"pq\u0001\u0000\u0000\u0000qr\u0001\u0000\u0000\u0000rs\u0005\n\u0000\u0000"+
		"st\u0003\u001a\r\u0000t\u000f\u0001\u0000\u0000\u0000uv\u0005\u0007\u0000"+
		"\u0000vw\u0005\t\u0000\u0000w|\u0003.\u0017\u0000xy\u0005\u000e\u0000"+
		"\u0000y{\u0003.\u0017\u0000zx\u0001\u0000\u0000\u0000{~\u0001\u0000\u0000"+
		"\u0000|z\u0001\u0000\u0000\u0000|}\u0001\u0000\u0000\u0000}\u007f\u0001"+
		"\u0000\u0000\u0000~|\u0001\u0000\u0000\u0000\u007f\u0080\u0005\n\u0000"+
		"\u0000\u0080\u0084\u0003\u001a\r\u0000\u0081\u0083\u0003\u0012\t\u0000"+
		"\u0082\u0081\u0001\u0000\u0000\u0000\u0083\u0086\u0001\u0000\u0000\u0000"+
		"\u0084\u0082\u0001\u0000\u0000\u0000\u0084\u0085\u0001\u0000\u0000\u0000"+
		"\u0085\u0088\u0001\u0000\u0000\u0000\u0086\u0084\u0001\u0000\u0000\u0000"+
		"\u0087\u0089\u0003\u0014\n\u0000\u0088\u0087\u0001\u0000\u0000\u0000\u0088"+
		"\u0089\u0001\u0000\u0000\u0000\u0089\u0011\u0001\u0000\u0000\u0000\u008a"+
		"\u008b\u0005\b\u0000\u0000\u008b\u008c\u0005\u0007\u0000\u0000\u008c\u008d"+
		"\u0005\t\u0000\u0000\u008d\u008e\u0003.\u0017\u0000\u008e\u008f\u0005"+
		"\n\u0000\u0000\u008f\u0090\u0003\u001a\r\u0000\u0090\u0013\u0001\u0000"+
		"\u0000\u0000\u0091\u0097\u0005\b\u0000\u0000\u0092\u0094\u0005\t\u0000"+
		"\u0000\u0093\u0095\u0003.\u0017\u0000\u0094\u0093\u0001\u0000\u0000\u0000"+
		"\u0094\u0095\u0001\u0000\u0000\u0000\u0095\u0096\u0001\u0000\u0000\u0000"+
		"\u0096\u0098\u0005\n\u0000\u0000\u0097\u0092\u0001\u0000\u0000\u0000\u0097"+
		"\u0098\u0001\u0000\u0000\u0000\u0098\u0099\u0001\u0000\u0000\u0000\u0099"+
		"\u009a\u0003\u001a\r\u0000\u009a\u0015\u0001\u0000\u0000\u0000\u009b\u00a0"+
		"\u0003\u0018\f\u0000\u009c\u009d\u0005\u000e\u0000\u0000\u009d\u009f\u0003"+
		"\u0018\f\u0000\u009e\u009c\u0001\u0000\u0000\u0000\u009f\u00a2\u0001\u0000"+
		"\u0000\u0000\u00a0\u009e\u0001\u0000\u0000\u0000\u00a0\u00a1\u0001\u0000"+
		"\u0000\u0000\u00a1\u0017\u0001\u0000\u0000\u0000\u00a2\u00a0\u0001\u0000"+
		"\u0000\u0000\u00a3\u00a4\u0003,\u0016\u0000\u00a4\u00a7\u0005&\u0000\u0000"+
		"\u00a5\u00a6\u0005\u000f\u0000\u0000\u00a6\u00a8\u0003.\u0017\u0000\u00a7"+
		"\u00a5\u0001\u0000\u0000\u0000\u00a7\u00a8\u0001\u0000\u0000\u0000\u00a8"+
		"\u0019\u0001\u0000\u0000\u0000\u00a9\u00ad\u0005\u000b\u0000\u0000\u00aa"+
		"\u00ac\u0003\u001c\u000e\u0000\u00ab\u00aa\u0001\u0000\u0000\u0000\u00ac"+
		"\u00af\u0001\u0000\u0000\u0000\u00ad\u00ab\u0001\u0000\u0000\u0000\u00ad"+
		"\u00ae\u0001\u0000\u0000\u0000\u00ae\u00b0\u0001\u0000\u0000\u0000\u00af"+
		"\u00ad\u0001\u0000\u0000\u0000\u00b0\u00b1\u0005\f\u0000\u0000\u00b1\u001b"+
		"\u0001\u0000\u0000\u0000\u00b2\u00bc\u0003\u0010\b\u0000\u00b3\u00bc\u0003"+
		"(\u0014\u0000\u00b4\u00bc\u0003&\u0013\u0000\u00b5\u00bc\u0003$\u0012"+
		"\u0000\u00b6\u00bc\u0003\u001e\u000f\u0000\u00b7\u00bc\u0003 \u0010\u0000"+
		"\u00b8\u00bc\u0003\b\u0004\u0000\u00b9\u00bc\u0003\f\u0006\u0000\u00ba"+
		"\u00bc\u0003\"\u0011\u0000\u00bb\u00b2\u0001\u0000\u0000\u0000\u00bb\u00b3"+
		"\u0001\u0000\u0000\u0000\u00bb\u00b4\u0001\u0000\u0000\u0000\u00bb\u00b5"+
		"\u0001\u0000\u0000\u0000\u00bb\u00b6\u0001\u0000\u0000\u0000\u00bb\u00b7"+
		"\u0001\u0000\u0000\u0000\u00bb\u00b8\u0001\u0000\u0000\u0000\u00bb\u00b9"+
		"\u0001\u0000\u0000\u0000\u00bb\u00ba\u0001\u0000\u0000\u0000\u00bc\u001d"+
		"\u0001\u0000\u0000\u0000\u00bd\u00be\u0003,\u0016\u0000\u00be\u00c1\u0005"+
		"&\u0000\u0000\u00bf\u00c0\u0005\u000f\u0000\u0000\u00c0\u00c2\u0003.\u0017"+
		"\u0000\u00c1\u00bf\u0001\u0000\u0000\u0000\u00c1\u00c2\u0001\u0000\u0000"+
		"\u0000\u00c2\u00c4\u0001\u0000\u0000\u0000\u00c3\u00c5\u0005\r\u0000\u0000"+
		"\u00c4\u00c3\u0001\u0000\u0000\u0000\u00c4\u00c5\u0001\u0000\u0000\u0000"+
		"\u00c5\u001f\u0001\u0000\u0000\u0000\u00c6\u00c7\u0005&\u0000\u0000\u00c7"+
		"\u00c8\u0005\u000f\u0000\u0000\u00c8\u00ca\u0003.\u0017\u0000\u00c9\u00cb"+
		"\u0005\r\u0000\u0000\u00ca\u00c9\u0001\u0000\u0000\u0000\u00ca\u00cb\u0001"+
		"\u0000\u0000\u0000\u00cb!\u0001\u0000\u0000\u0000\u00cc\u00cd\u0005&\u0000"+
		"\u0000\u00cd\u00cf\u0005\t\u0000\u0000\u00ce\u00d0\u0003*\u0015\u0000"+
		"\u00cf\u00ce\u0001\u0000\u0000\u0000\u00cf\u00d0\u0001\u0000\u0000\u0000"+
		"\u00d0\u00d1\u0001\u0000\u0000\u0000\u00d1\u00d3\u0005\n\u0000\u0000\u00d2"+
		"\u00d4\u0005\r\u0000\u0000\u00d3\u00d2\u0001\u0000\u0000\u0000\u00d3\u00d4"+
		"\u0001\u0000\u0000\u0000\u00d4#\u0001\u0000\u0000\u0000\u00d5\u00d6\u0005"+
		"&\u0000\u0000\u00d6\u00d7\u0005\u0013\u0000\u0000\u00d7\u00d8\u0005&\u0000"+
		"\u0000\u00d8\u00da\u0005\t\u0000\u0000\u00d9\u00db\u0003*\u0015\u0000"+
		"\u00da\u00d9\u0001\u0000\u0000\u0000\u00da\u00db\u0001\u0000\u0000\u0000"+
		"\u00db\u00dc\u0001\u0000\u0000\u0000\u00dc\u00de\u0005\n\u0000\u0000\u00dd"+
		"\u00df\u0005\r\u0000\u0000\u00de\u00dd\u0001\u0000\u0000\u0000\u00de\u00df"+
		"\u0001\u0000\u0000\u0000\u00df%\u0001\u0000\u0000\u0000\u00e0\u00e1\u0005"+
		"&\u0000\u0000\u00e1\u00e3\u0005\t\u0000\u0000\u00e2\u00e4\u0003*\u0015"+
		"\u0000\u00e3\u00e2\u0001\u0000\u0000\u0000\u00e3\u00e4\u0001\u0000\u0000"+
		"\u0000\u00e4\u00e5\u0001\u0000\u0000\u0000\u00e5\u00e6\u0005\n\u0000\u0000"+
		"\u00e6\u00e7\u0003\u001a\r\u0000\u00e7\'\u0001\u0000\u0000\u0000\u00e8"+
		"\u00e9\u0005\u0005\u0000\u0000\u00e9\u00ea\u0005&\u0000\u0000\u00ea\u00ec"+
		"\u0005\t\u0000\u0000\u00eb\u00ed\u0003*\u0015\u0000\u00ec\u00eb\u0001"+
		"\u0000\u0000\u0000\u00ec\u00ed\u0001\u0000\u0000\u0000\u00ed\u00ee\u0001"+
		"\u0000\u0000\u0000\u00ee\u00f0\u0005\n\u0000\u0000\u00ef\u00f1\u0005\r"+
		"\u0000\u0000\u00f0\u00ef\u0001\u0000\u0000\u0000\u00f0\u00f1\u0001\u0000"+
		"\u0000\u0000\u00f1)\u0001\u0000\u0000\u0000\u00f2\u00f7\u0003.\u0017\u0000"+
		"\u00f3\u00f4\u0005\u000e\u0000\u0000\u00f4\u00f6\u0003.\u0017\u0000\u00f5"+
		"\u00f3\u0001\u0000\u0000\u0000\u00f6\u00f9\u0001\u0000\u0000\u0000\u00f7"+
		"\u00f5\u0001\u0000\u0000\u0000\u00f7\u00f8\u0001\u0000\u0000\u0000\u00f8"+
		"+\u0001\u0000\u0000\u0000\u00f9\u00f7\u0001\u0000\u0000\u0000\u00fa\u00fb"+
		"\u0005&\u0000\u0000\u00fb-\u0001\u0000\u0000\u0000\u00fc\u00fd\u0006\u0017"+
		"\uffff\uffff\u0000\u00fd\u00fe\u0005\t\u0000\u0000\u00fe\u00ff\u0003."+
		"\u0017\u0000\u00ff\u0100\u0005\n\u0000\u0000\u0100\u0117\u0001\u0000\u0000"+
		"\u0000\u0101\u0102\u00030\u0018\u0000\u0102\u0104\u0005\t\u0000\u0000"+
		"\u0103\u0105\u0003*\u0015\u0000\u0104\u0103\u0001\u0000\u0000\u0000\u0104"+
		"\u0105\u0001\u0000\u0000\u0000\u0105\u0106\u0001\u0000\u0000\u0000\u0106"+
		"\u0107\u0005\n\u0000\u0000\u0107\u0117\u0001\u0000\u0000\u0000\u0108\u0117"+
		"\u00030\u0018\u0000\u0109\u0117\u0005(\u0000\u0000\u010a\u0117\u0005)"+
		"\u0000\u0000\u010b\u0117\u0005%\u0000\u0000\u010c\u0117\u00032\u0019\u0000"+
		"\u010d\u010e\u0005&\u0000\u0000\u010e\u010f\u0005\u000f\u0000\u0000\u010f"+
		"\u0117\u0003.\u0017\f\u0110\u0111\u0005\u001c\u0000\u0000\u0111\u0117"+
		"\u0003.\u0017\u000b\u0112\u0113\u0005\u0006\u0000\u0000\u0113\u0117\u0003"+
		".\u0017\n\u0114\u0115\u0005\u0015\u0000\u0000\u0115\u0117\u0003.\u0017"+
		"\t\u0116\u00fc\u0001\u0000\u0000\u0000\u0116\u0101\u0001\u0000\u0000\u0000"+
		"\u0116\u0108\u0001\u0000\u0000\u0000\u0116\u0109\u0001\u0000\u0000\u0000"+
		"\u0116\u010a\u0001\u0000\u0000\u0000\u0116\u010b\u0001\u0000\u0000\u0000"+
		"\u0116\u010c\u0001\u0000\u0000\u0000\u0116\u010d\u0001\u0000\u0000\u0000"+
		"\u0116\u0110\u0001\u0000\u0000\u0000\u0116\u0112\u0001\u0000\u0000\u0000"+
		"\u0116\u0114\u0001\u0000\u0000\u0000\u0117\u0135\u0001\u0000\u0000\u0000"+
		"\u0118\u0119\n\b\u0000\u0000\u0119\u011a\u0007\u0000\u0000\u0000\u011a"+
		"\u0134\u0003.\u0017\t\u011b\u011c\n\u0007\u0000\u0000\u011c\u011d\u0007"+
		"\u0001\u0000\u0000\u011d\u0134\u0003.\u0017\b\u011e\u011f\n\u0006\u0000"+
		"\u0000\u011f\u0120\u0005\u001a\u0000\u0000\u0120\u0134\u0003.\u0017\u0007"+
		"\u0121\u0122\n\u0005\u0000\u0000\u0122\u0123\u0005\u001b\u0000\u0000\u0123"+
		"\u0134\u0003.\u0017\u0006\u0124\u0125\n\u0004\u0000\u0000\u0125\u0126"+
		"\u0007\u0002\u0000\u0000\u0126\u0134\u0003.\u0017\u0005\u0127\u0128\n"+
		"\u0003\u0000\u0000\u0128\u0129\u0005\u0018\u0000\u0000\u0129\u0134\u0003"+
		".\u0017\u0004\u012a\u012b\n\u0002\u0000\u0000\u012b\u012c\u0005\u0019"+
		"\u0000\u0000\u012c\u0134\u0003.\u0017\u0003\u012d\u012e\n\u0001\u0000"+
		"\u0000\u012e\u012f\u0005\u001d\u0000\u0000\u012f\u0130\u0003.\u0017\u0000"+
		"\u0130\u0131\u0005\u001e\u0000\u0000\u0131\u0132\u0003.\u0017\u0002\u0132"+
		"\u0134\u0001\u0000\u0000\u0000\u0133\u0118\u0001\u0000\u0000\u0000\u0133"+
		"\u011b\u0001\u0000\u0000\u0000\u0133\u011e\u0001\u0000\u0000\u0000\u0133"+
		"\u0121\u0001\u0000\u0000\u0000\u0133\u0124\u0001\u0000\u0000\u0000\u0133"+
		"\u0127\u0001\u0000\u0000\u0000\u0133\u012a\u0001\u0000\u0000\u0000\u0133"+
		"\u012d\u0001\u0000\u0000\u0000\u0134\u0137\u0001\u0000\u0000\u0000\u0135"+
		"\u0133\u0001\u0000\u0000\u0000\u0135\u0136\u0001\u0000\u0000\u0000\u0136"+
		"/\u0001\u0000\u0000\u0000\u0137\u0135\u0001\u0000\u0000\u0000\u0138\u013d"+
		"\u0005&\u0000\u0000\u0139\u013a\u0005\u0012\u0000\u0000\u013a\u013c\u0005"+
		"&\u0000\u0000\u013b\u0139\u0001\u0000\u0000\u0000\u013c\u013f\u0001\u0000"+
		"\u0000\u0000\u013d\u013b\u0001\u0000\u0000\u0000\u013d\u013e\u0001\u0000"+
		"\u0000\u0000\u013e1\u0001\u0000\u0000\u0000\u013f\u013d\u0001\u0000\u0000"+
		"\u0000\u0140\u0142\u0005\u0010\u0000\u0000\u0141\u0143\u0003*\u0015\u0000"+
		"\u0142\u0141\u0001\u0000\u0000\u0000\u0142\u0143\u0001\u0000\u0000\u0000"+
		"\u0143\u0144\u0001\u0000\u0000\u0000\u0144\u0145\u0005\u0011\u0000\u0000"+
		"\u01453\u0001\u0000\u0000\u0000#7FKSY_cjp|\u0084\u0088\u0094\u0097\u00a0"+
		"\u00a7\u00ad\u00bb\u00c1\u00c4\u00ca\u00cf\u00d3\u00da\u00de\u00e3\u00ec"+
		"\u00f0\u00f7\u0104\u0116\u0133\u0135\u013d\u0142";
	public static final String[] ruleNames = makeRuleNames();

	private static String[] makeLiteralNames() {
		return new String[] {
			null, "'import'", "'extern'", "'export'", "'sub'", "'use'", "'!'", null,
			null, "'('", "')'", "'{'", "'}'", "';'", "','", "'='", "'['", "']'",
			"'.'", "'::'", "'+'", "'-'", "'*'", "'/'", "'&&'", "'||'", "'|'", "'&'",
			"'~'", "'?'", "':'", "'>'", "'<'", "'=='", "'!='", "'>='", "'<='"
		};
	}
	private static final String[] _LITERAL_NAMES = makeLiteralNames();

	private static String[] makeRuleNames() {
		return new String[] {
			"file", "definition", "importDecl", "exportDecl", "externDecl", "directiveCall",
			"macroDecl", "subDecl", "logicControlDecl", "elseIfClause", "elseClause",
			"paramList", "param", "functionBlock", "statements", "variableDecl",
			"variableAssignDecl", "functionCallDecl", "methodReferenceFunCallDecl",
			"functionBlockDecl", "useDecl", "valueList", "type", "expression", "fieldAccess",
			"arrayValue"
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
			setState(55);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while ((((_la) & ~0x3f) == 0 && ((1L << _la) & 824633720926L) != 0)) {
				{
				{
				setState(52);
				definition();
				}
				}
				setState(57);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(58);
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
			setState(70);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,1,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(60);
				importDecl();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(61);
				exportDecl();
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(62);
				externDecl();
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(63);
				directiveCall();
				}
				break;
			case 5:
				enterOuterAlt(_localctx, 5);
				{
				setState(64);
				macroDecl();
				}
				break;
			case 6:
				enterOuterAlt(_localctx, 6);
				{
				setState(65);
				subDecl();
				}
				break;
			case 7:
				enterOuterAlt(_localctx, 7);
				{
				setState(66);
				variableDecl();
				}
				break;
			case 8:
				enterOuterAlt(_localctx, 8);
				{
				setState(67);
				functionBlockDecl();
				}
				break;
			case 9:
				enterOuterAlt(_localctx, 9);
				{
				setState(68);
				methodReferenceFunCallDecl();
				}
				break;
			case 10:
				enterOuterAlt(_localctx, 10);
				{
				setState(69);
				functionCallDecl();
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
			setState(72);
			match(Import);
			setState(73);
			match(String);
			setState(75);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==Semicolon) {
				{
				setState(74);
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
			setState(77);
			match(Export);
			setState(78);
			type();
			setState(79);
			match(Id);
			setState(80);
			match(Equals);
			setState(81);
			expression(0);
			setState(83);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==Semicolon) {
				{
				setState(82);
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

	public final ExternDeclContext externDecl() throws RecognitionException {
		ExternDeclContext _localctx = new ExternDeclContext(_ctx, getState());
		enterRule(_localctx, 8, RULE_externDecl);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(85);
			match(Extern);
			setState(86);
			type();
			setState(87);
			match(Id);
			setState(89);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==Semicolon) {
				{
				setState(88);
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

	public final DirectiveCallContext directiveCall() throws RecognitionException {
		DirectiveCallContext _localctx = new DirectiveCallContext(_ctx, getState());
		enterRule(_localctx, 10, RULE_directiveCall);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(91);
			match(Exclamation);
			setState(92);
			match(Id);
			setState(93);
			match(LParen);
			setState(95);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if ((((_la) & ~0x3f) == 0 && ((1L << _la) & 3711122342464L) != 0)) {
				{
				setState(94);
				valueList();
				}
			}

			setState(97);
			match(RParen);
			setState(99);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==Semicolon) {
				{
				setState(98);
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

	public final MacroDeclContext macroDecl() throws RecognitionException {
		MacroDeclContext _localctx = new MacroDeclContext(_ctx, getState());
		enterRule(_localctx, 12, RULE_macroDecl);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(101);
			match(MacroId);
			setState(102);
			match(LParen);
			setState(103);
			valueList();
			setState(104);
			match(RParen);
			setState(106);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==Semicolon) {
				{
				setState(105);
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

	public final SubDeclContext subDecl() throws RecognitionException {
		SubDeclContext _localctx = new SubDeclContext(_ctx, getState());
		enterRule(_localctx, 14, RULE_subDecl);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(108);
			match(Sub);
			setState(109);
			match(Id);
			setState(110);
			match(LParen);
			setState(112);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==Id) {
				{
				setState(111);
				paramList();
				}
			}

			setState(114);
			match(RParen);
			setState(115);
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
			setState(117);
			match(KwIf);
			setState(118);
			match(LParen);
			setState(119);
			expression(0);
			setState(124);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==Comma) {
				{
				{
				setState(120);
				match(Comma);
				setState(121);
				expression(0);
				}
				}
				setState(126);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(127);
			match(RParen);
			setState(128);
			functionBlock();
			setState(132);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,10,_ctx);
			while ( _alt!=2 && _alt!= ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(129);
					elseIfClause();
					}
					}
				}
				setState(134);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,10,_ctx);
			}
			setState(136);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==KwElse) {
				{
				setState(135);
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

	public final ElseIfClauseContext elseIfClause() throws RecognitionException {
		ElseIfClauseContext _localctx = new ElseIfClauseContext(_ctx, getState());
		enterRule(_localctx, 18, RULE_elseIfClause);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(138);
			match(KwElse);
			setState(139);
			match(KwIf);
			setState(140);
			match(LParen);
			setState(141);
			expression(0);
			setState(142);
			match(RParen);
			setState(143);
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

	public final ElseClauseContext elseClause() throws RecognitionException {
		ElseClauseContext _localctx = new ElseClauseContext(_ctx, getState());
		enterRule(_localctx, 20, RULE_elseClause);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(145);
			match(KwElse);
			setState(151);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==LParen) {
				{
				setState(146);
				match(LParen);
				setState(148);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if ((((_la) & ~0x3f) == 0 && ((1L << _la) & 3711122342464L) != 0)) {
					{
					setState(147);
					expression(0);
					}
				}

				setState(150);
				match(RParen);
				}
			}

			setState(153);
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
			setState(155);
			param();
			setState(160);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==Comma) {
				{
				{
				setState(156);
				match(Comma);
				setState(157);
				param();
				}
				}
				setState(162);
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

	public final ParamContext param() throws RecognitionException {
		ParamContext _localctx = new ParamContext(_ctx, getState());
		enterRule(_localctx, 24, RULE_param);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(163);
			type();
			setState(164);
			match(Id);
			setState(167);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==Equals) {
				{
				setState(165);
				match(Equals);
				setState(166);
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
			setState(169);
			match(LBrace);
			setState(173);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while ((((_la) & ~0x3f) == 0 && ((1L << _la) & 824633720996L) != 0)) {
				{
				{
				setState(170);
				statements();
				}
				}
				setState(175);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(176);
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
			setState(187);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,17,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(178);
				logicControlDecl();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(179);
				useDecl();
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(180);
				functionBlockDecl();
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(181);
				methodReferenceFunCallDecl();
				}
				break;
			case 5:
				enterOuterAlt(_localctx, 5);
				{
				setState(182);
				variableDecl();
				}
				break;
			case 6:
				enterOuterAlt(_localctx, 6);
				{
				setState(183);
				variableAssignDecl();
				}
				break;
			case 7:
				enterOuterAlt(_localctx, 7);
				{
				setState(184);
				externDecl();
				}
				break;
			case 8:
				enterOuterAlt(_localctx, 8);
				{
				setState(185);
				macroDecl();
				}
				break;
			case 9:
				enterOuterAlt(_localctx, 9);
				{
				setState(186);
				functionCallDecl();
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
			setState(189);
			type();
			setState(190);
			match(Id);
			setState(193);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==Equals) {
				{
				setState(191);
				match(Equals);
				setState(192);
				expression(0);
				}
			}

			setState(196);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==Semicolon) {
				{
				setState(195);
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

	public final VariableAssignDeclContext variableAssignDecl() throws RecognitionException {
		VariableAssignDeclContext _localctx = new VariableAssignDeclContext(_ctx, getState());
		enterRule(_localctx, 32, RULE_variableAssignDecl);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(198);
			match(Id);
			setState(199);
			match(Equals);
			setState(200);
			expression(0);
			setState(202);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==Semicolon) {
				{
				setState(201);
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

	public final FunctionCallDeclContext functionCallDecl() throws RecognitionException {
		FunctionCallDeclContext _localctx = new FunctionCallDeclContext(_ctx, getState());
		enterRule(_localctx, 34, RULE_functionCallDecl);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(204);
			match(Id);
			setState(205);
			match(LParen);
			setState(207);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if ((((_la) & ~0x3f) == 0 && ((1L << _la) & 3711122342464L) != 0)) {
				{
				setState(206);
				valueList();
				}
			}

			setState(209);
			match(RParen);
			setState(211);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==Semicolon) {
				{
				setState(210);
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
		enterRule(_localctx, 36, RULE_methodReferenceFunCallDecl);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(213);
			match(Id);
			setState(214);
			match(DoubleColon);
			setState(215);
			match(Id);
			setState(216);
			match(LParen);
			setState(218);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if ((((_la) & ~0x3f) == 0 && ((1L << _la) & 3711122342464L) != 0)) {
				{
				setState(217);
				valueList();
				}
			}

			setState(220);
			match(RParen);
			setState(222);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==Semicolon) {
				{
				setState(221);
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

	public final FunctionBlockDeclContext functionBlockDecl() throws RecognitionException {
		FunctionBlockDeclContext _localctx = new FunctionBlockDeclContext(_ctx, getState());
		enterRule(_localctx, 38, RULE_functionBlockDecl);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(224);
			match(Id);
			setState(225);
			match(LParen);
			setState(227);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if ((((_la) & ~0x3f) == 0 && ((1L << _la) & 3711122342464L) != 0)) {
				{
				setState(226);
				valueList();
				}
			}

			setState(229);
			match(RParen);
			setState(230);
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
		enterRule(_localctx, 40, RULE_useDecl);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(232);
			match(Use);
			setState(233);
			match(Id);
			setState(234);
			match(LParen);
			setState(236);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if ((((_la) & ~0x3f) == 0 && ((1L << _la) & 3711122342464L) != 0)) {
				{
				setState(235);
				valueList();
				}
			}

			setState(238);
			match(RParen);
			setState(240);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==Semicolon) {
				{
				setState(239);
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
		enterRule(_localctx, 42, RULE_valueList);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(242);
			expression(0);
			setState(247);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==Comma) {
				{
				{
				setState(243);
				match(Comma);
				setState(244);
				expression(0);
				}
				}
				setState(249);
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
		enterRule(_localctx, 44, RULE_type);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(250);
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

	private ExpressionContext expression(int _p) throws RecognitionException {
		ParserRuleContext _parentctx = _ctx;
		int _parentState = getState();
		ExpressionContext _localctx = new ExpressionContext(_ctx, _parentState);
		ExpressionContext _prevctx = _localctx;
		int _startState = 46;
		enterRecursionRule(_localctx, 46, RULE_expression, _p);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(278);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,30,_ctx) ) {
			case 1:
				{
				setState(253);
				match(LParen);
				setState(254);
				expression(0);
				setState(255);
				match(RParen);
				}
				break;
			case 2:
				{
				setState(257);
				fieldAccess();
				setState(258);
				match(LParen);
				setState(260);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if ((((_la) & ~0x3f) == 0 && ((1L << _la) & 3711122342464L) != 0)) {
					{
					setState(259);
					valueList();
					}
				}

				setState(262);
				match(RParen);
				}
				break;
			case 3:
				{
				setState(264);
				fieldAccess();
				}
				break;
			case 4:
				{
				setState(265);
				match(Number);
				}
				break;
			case 5:
				{
				setState(266);
				match(String);
				}
				break;
			case 6:
				{
				setState(267);
				match(Bool);
				}
				break;
			case 7:
				{
				setState(268);
				arrayValue();
				}
				break;
			case 8:
				{
				setState(269);
				match(Id);
				setState(270);
				match(Equals);
				setState(271);
				expression(12);
				}
				break;
			case 9:
				{
				setState(272);
				match(BitNot);
				setState(273);
				expression(11);
				}
				break;
			case 10:
				{
				setState(274);
				match(Exclamation);
				setState(275);
				expression(10);
				}
				break;
			case 11:
				{
				setState(276);
				match(Minus);
				setState(277);
				expression(9);
				}
				break;
			}
			_ctx.stop = _input.LT(-1);
			setState(309);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,32,_ctx);
			while ( _alt!=2 && _alt!= ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					if ( _parseListeners!=null ) triggerExitRuleEvent();
					_prevctx = _localctx;
					{
					setState(307);
					_errHandler.sync(this);
					switch ( getInterpreter().adaptivePredict(_input,31,_ctx) ) {
					case 1:
						{
						_localctx = new ExpressionContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(280);
						if (!(precpred(_ctx, 8))) throw new FailedPredicateException(this, "precpred(_ctx, 8)");
						setState(281);
						_la = _input.LA(1);
						if ( !(_la==Mul || _la==Div) ) {
						_errHandler.recoverInline(this);
						}
						else {
							if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
							_errHandler.reportMatch(this);
							consume();
						}
						setState(282);
						expression(9);
						}
						break;
					case 2:
						{
						_localctx = new ExpressionContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(283);
						if (!(precpred(_ctx, 7))) throw new FailedPredicateException(this, "precpred(_ctx, 7)");
						setState(284);
						_la = _input.LA(1);
						if ( !(_la==Plus || _la==Minus) ) {
						_errHandler.recoverInline(this);
						}
						else {
							if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
							_errHandler.reportMatch(this);
							consume();
						}
						setState(285);
						expression(8);
						}
						break;
					case 3:
						{
						_localctx = new ExpressionContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(286);
						if (!(precpred(_ctx, 6))) throw new FailedPredicateException(this, "precpred(_ctx, 6)");
						setState(287);
						match(BitOr);
						setState(288);
						expression(7);
						}
						break;
					case 4:
						{
						_localctx = new ExpressionContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(289);
						if (!(precpred(_ctx, 5))) throw new FailedPredicateException(this, "precpred(_ctx, 5)");
						setState(290);
						match(BitAnd);
						setState(291);
						expression(6);
						}
						break;
					case 5:
						{
						_localctx = new ExpressionContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(292);
						if (!(precpred(_ctx, 4))) throw new FailedPredicateException(this, "precpred(_ctx, 4)");
						setState(293);
						_la = _input.LA(1);
						if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & 135291469824L) != 0)) ) {
						_errHandler.recoverInline(this);
						}
						else {
							if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
							_errHandler.reportMatch(this);
							consume();
						}
						setState(294);
						expression(5);
						}
						break;
					case 6:
						{
						_localctx = new ExpressionContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(295);
						if (!(precpred(_ctx, 3))) throw new FailedPredicateException(this, "precpred(_ctx, 3)");
						setState(296);
						match(LogicAnd);
						setState(297);
						expression(4);
						}
						break;
					case 7:
						{
						_localctx = new ExpressionContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(298);
						if (!(precpred(_ctx, 2))) throw new FailedPredicateException(this, "precpred(_ctx, 2)");
						setState(299);
						match(LogicOr);
						setState(300);
						expression(3);
						}
						break;
					case 8:
						{
						_localctx = new ExpressionContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(301);
						if (!(precpred(_ctx, 1))) throw new FailedPredicateException(this, "precpred(_ctx, 1)");
						setState(302);
						match(Question);
						setState(303);
						expression(0);
						setState(304);
						match(Colon);
						setState(305);
						expression(2);
						}
						break;
					}
					}
				}
				setState(311);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,32,_ctx);
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
		enterRule(_localctx, 48, RULE_fieldAccess);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(312);
			match(Id);
			setState(317);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,33,_ctx);
			while ( _alt!=2 && _alt!= ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(313);
					match(Dot);
					setState(314);
					match(Id);
					}
					}
				}
				setState(319);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,33,_ctx);
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
		enterRule(_localctx, 50, RULE_arrayValue);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(320);
			match(LBracket);
			setState(322);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if ((((_la) & ~0x3f) == 0 && ((1L << _la) & 3711122342464L) != 0)) {
				{
				setState(321);
				valueList();
				}
			}

			setState(324);
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
		case 23:
			return expression_sempred((ExpressionContext)_localctx, predIndex);
		}
		return true;
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

		public FunctionBlockDeclContext functionBlockDecl() {
			return getRuleContext(FunctionBlockDeclContext.class,0);
		}

		public MethodReferenceFunCallDeclContext methodReferenceFunCallDecl() {
			return getRuleContext(MethodReferenceFunCallDeclContext.class,0);
		}

		public FunctionCallDeclContext functionCallDecl() {
			return getRuleContext(FunctionCallDeclContext.class,0);
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

	@SuppressWarnings("CheckReturnValue")
	public static class LogicControlDeclContext extends ParserRuleContext {
		public LogicControlDeclContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}

		public TerminalNode KwIf() { return getToken(TechlandScriptParser.KwIf, 0); }

		public TerminalNode LParen() { return getToken(TechlandScriptParser.LParen, 0); }

		public List<ExpressionContext> expression() {
			return getRuleContexts(ExpressionContext.class);
		}

		public ExpressionContext expression(int i) {
			return getRuleContext(ExpressionContext.class,i);
		}

		public TerminalNode RParen() { return getToken(TechlandScriptParser.RParen, 0); }

		public FunctionBlockContext functionBlock() {
			return getRuleContext(FunctionBlockContext.class,0);
		}

		public List<TerminalNode> Comma() { return getTokens(TechlandScriptParser.Comma); }

		public TerminalNode Comma(int i) {
			return getToken(TechlandScriptParser.Comma, i);
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
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof TechlandScriptVisitor ) return ((TechlandScriptVisitor<? extends T>)visitor).visitElseClause(this);
			else return visitor.visitChildren(this);
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
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof TechlandScriptVisitor ) return ((TechlandScriptVisitor<? extends T>)visitor).visitParamList(this);
			else return visitor.visitChildren(this);
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

		public FunctionBlockDeclContext functionBlockDecl() {
			return getRuleContext(FunctionBlockDeclContext.class,0);
		}

		public MethodReferenceFunCallDeclContext methodReferenceFunCallDecl() {
			return getRuleContext(MethodReferenceFunCallDeclContext.class,0);
		}

		public VariableDeclContext variableDecl() {
			return getRuleContext(VariableDeclContext.class,0);
		}

		public VariableAssignDeclContext variableAssignDecl() {
			return getRuleContext(VariableAssignDeclContext.class,0);
		}

		public ExternDeclContext externDecl() {
			return getRuleContext(ExternDeclContext.class,0);
		}

		public MacroDeclContext macroDecl() {
			return getRuleContext(MacroDeclContext.class,0);
		}

		public FunctionCallDeclContext functionCallDecl() {
			return getRuleContext(FunctionCallDeclContext.class,0);
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

	@SuppressWarnings("CheckReturnValue")
	public static class VariableDeclContext extends ParserRuleContext {
		public VariableDeclContext(ParserRuleContext parent, int invokingState) {
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

	@SuppressWarnings("CheckReturnValue")
	public static class VariableAssignDeclContext extends ParserRuleContext {
		public VariableAssignDeclContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}

		public TerminalNode Id() { return getToken(TechlandScriptParser.Id, 0); }

		public TerminalNode Equals() { return getToken(TechlandScriptParser.Equals, 0); }

		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}

		public TerminalNode Semicolon() { return getToken(TechlandScriptParser.Semicolon, 0); }

		@Override public int getRuleIndex() { return RULE_variableAssignDecl; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TechlandScriptListener ) ((TechlandScriptListener)listener).enterVariableAssignDecl(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TechlandScriptListener ) ((TechlandScriptListener)listener).exitVariableAssignDecl(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof TechlandScriptVisitor ) return ((TechlandScriptVisitor<? extends T>)visitor).visitVariableAssignDecl(this);
			else return visitor.visitChildren(this);
		}
	}

	@SuppressWarnings("CheckReturnValue")
	public static class FunctionCallDeclContext extends ParserRuleContext {
		public FunctionCallDeclContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}

		public TerminalNode Id() { return getToken(TechlandScriptParser.Id, 0); }

		public TerminalNode LParen() { return getToken(TechlandScriptParser.LParen, 0); }

		public TerminalNode RParen() { return getToken(TechlandScriptParser.RParen, 0); }

		public ValueListContext valueList() {
			return getRuleContext(ValueListContext.class,0);
		}

		public TerminalNode Semicolon() { return getToken(TechlandScriptParser.Semicolon, 0); }

		@Override public int getRuleIndex() { return RULE_functionCallDecl; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TechlandScriptListener ) ((TechlandScriptListener)listener).enterFunctionCallDecl(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TechlandScriptListener ) ((TechlandScriptListener)listener).exitFunctionCallDecl(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof TechlandScriptVisitor ) return ((TechlandScriptVisitor<? extends T>)visitor).visitFunctionCallDecl(this);
			else return visitor.visitChildren(this);
		}
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

	public final ExpressionContext expression() throws RecognitionException {
		return expression(0);
	}

	@SuppressWarnings("CheckReturnValue")
	public static class FunctionBlockDeclContext extends ParserRuleContext {
		public FunctionBlockDeclContext(ParserRuleContext parent, int invokingState) {
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

		@Override public int getRuleIndex() { return RULE_functionBlockDecl; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TechlandScriptListener ) ((TechlandScriptListener)listener).enterFunctionBlockDecl(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TechlandScriptListener ) ((TechlandScriptListener)listener).exitFunctionBlockDecl(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof TechlandScriptVisitor ) return ((TechlandScriptVisitor<? extends T>)visitor).visitFunctionBlockDecl(this);
			else return visitor.visitChildren(this);
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
		public TypeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}

		public TerminalNode Id() { return getToken(TechlandScriptParser.Id, 0); }

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
		public ExpressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}

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
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof TechlandScriptVisitor ) return ((TechlandScriptVisitor<? extends T>)visitor).visitFieldAccess(this);
			else return visitor.visitChildren(this);
		}
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
