// Generated from java-escape by ANTLR 4.11.1
package ankol.mod.merger.antlr4.scr;
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.*;
import org.antlr.v4.runtime.tree.*;
import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast", "CheckReturnValue"})
public class TechlandScriptParser extends Parser {
	static { RuntimeMetaData.checkVersion("4.11.1", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		Import=1, Extern=2, Export=3, Sub=4, Use=5, Exclamation=6, LParen=7, RParen=8, 
		LBrace=9, RBrace=10, Semicolon=11, Comma=12, Equals=13, LBracket=14, RBracket=15, 
		Dot=16, Plus=17, Minus=18, Mul=19, Div=20, BitOr=21, BitAnd=22, BitNot=23, 
		Question=24, Colon=25, Gt=26, Lt=27, Eq=28, NotEq=29, Gte=30, Lte=31, 
		Bool=32, Id=33, MacroId=34, Number=35, String=36, LineComment=37, BlockComment=38, 
		WhiteSpaces=39;
	public static final int
		RULE_file = 0, RULE_definition = 1, RULE_importDecl = 2, RULE_exportDecl = 3, 
		RULE_externDecl = 4, RULE_directiveCall = 5, RULE_macroDecl = 6, RULE_subDecl = 7, 
		RULE_paramList = 8, RULE_param = 9, RULE_functionBlock = 10, RULE_statements = 11, 
		RULE_variableDecl = 12, RULE_funtionCallDecl = 13, RULE_funtionBlockDecl = 14, 
		RULE_useDecl = 15, RULE_valueList = 16, RULE_type = 17, RULE_expression = 18, 
		RULE_fieldAccess = 19, RULE_arrayValue = 20;
	private static String[] makeRuleNames() {
		return new String[] {
			"file", "definition", "importDecl", "exportDecl", "externDecl", "directiveCall", 
			"macroDecl", "subDecl", "paramList", "param", "functionBlock", "statements", 
			"variableDecl", "funtionCallDecl", "funtionBlockDecl", "useDecl", "valueList", 
			"type", "expression", "fieldAccess", "arrayValue"
		};
	}
	public static final String[] ruleNames = makeRuleNames();

	private static String[] makeLiteralNames() {
		return new String[] {
			null, "'import'", "'extern'", "'export'", "'sub'", "'use'", "'!'", "'('", 
			"')'", "'{'", "'}'", "';'", "','", "'='", "'['", "']'", "'.'", "'+'", 
			"'-'", "'*'", "'/'", "'|'", "'&'", "'~'", "'?'", "':'", "'>'", "'<'", 
			"'=='", "'!='", "'>='", "'<='"
		};
	}
	private static final String[] _LITERAL_NAMES = makeLiteralNames();
	private static String[] makeSymbolicNames() {
		return new String[] {
			null, "Import", "Extern", "Export", "Sub", "Use", "Exclamation", "LParen", 
			"RParen", "LBrace", "RBrace", "Semicolon", "Comma", "Equals", "LBracket", 
			"RBracket", "Dot", "Plus", "Minus", "Mul", "Div", "BitOr", "BitAnd", 
			"BitNot", "Question", "Colon", "Gt", "Lt", "Eq", "NotEq", "Gte", "Lte", 
			"Bool", "Id", "MacroId", "Number", "String", "LineComment", "BlockComment", 
			"WhiteSpaces"
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
	public String getGrammarFileName() { return "java-escape"; }

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
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof TechlandScriptVisitor ) return ((TechlandScriptVisitor<? extends T>)visitor).visitFile(this);
			else return visitor.visitChildren(this);
		}
	}

	public final FileContext file() throws RecognitionException {
		FileContext _localctx = new FileContext(_ctx, getState());
		enterRule(_localctx, 0, RULE_file);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(45);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (((_la) & ~0x3f) == 0 && ((1L << _la) & 25769803866L) != 0) {
				{
				{
				setState(42);
				definition();
				}
				}
				setState(47);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(48);
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

	@SuppressWarnings("CheckReturnValue")
	public static class DefinitionContext extends ParserRuleContext {
		public ImportDeclContext importDecl() {
			return getRuleContext(ImportDeclContext.class,0);
		}
		public ExportDeclContext exportDecl() {
			return getRuleContext(ExportDeclContext.class,0);
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
		public DefinitionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
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

	public final DefinitionContext definition() throws RecognitionException {
		DefinitionContext _localctx = new DefinitionContext(_ctx, getState());
		enterRule(_localctx, 2, RULE_definition);
		try {
			setState(58);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,1,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(50);
				importDecl();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(51);
				exportDecl();
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(52);
				subDecl();
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(53);
				directiveCall();
				}
				break;
			case 5:
				enterOuterAlt(_localctx, 5);
				{
				setState(54);
				macroDecl();
				}
				break;
			case 6:
				enterOuterAlt(_localctx, 6);
				{
				setState(55);
				variableDecl();
				}
				break;
			case 7:
				enterOuterAlt(_localctx, 7);
				{
				setState(56);
				funtionCallDecl();
				}
				break;
			case 8:
				enterOuterAlt(_localctx, 8);
				{
				setState(57);
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
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof TechlandScriptVisitor ) return ((TechlandScriptVisitor<? extends T>)visitor).visitImportDecl(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ImportDeclContext importDecl() throws RecognitionException {
		ImportDeclContext _localctx = new ImportDeclContext(_ctx, getState());
		enterRule(_localctx, 4, RULE_importDecl);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(60);
			match(Import);
			setState(61);
			match(String);
			setState(63);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==Semicolon) {
				{
				setState(62);
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
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof TechlandScriptVisitor ) return ((TechlandScriptVisitor<? extends T>)visitor).visitExportDecl(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ExportDeclContext exportDecl() throws RecognitionException {
		ExportDeclContext _localctx = new ExportDeclContext(_ctx, getState());
		enterRule(_localctx, 6, RULE_exportDecl);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(65);
			match(Export);
			setState(66);
			type();
			setState(67);
			match(Id);
			setState(68);
			match(Equals);
			setState(69);
			expression(0);
			setState(71);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==Semicolon) {
				{
				setState(70);
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
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof TechlandScriptVisitor ) return ((TechlandScriptVisitor<? extends T>)visitor).visitExternDecl(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ExternDeclContext externDecl() throws RecognitionException {
		ExternDeclContext _localctx = new ExternDeclContext(_ctx, getState());
		enterRule(_localctx, 8, RULE_externDecl);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(73);
			match(Extern);
			setState(74);
			type();
			setState(75);
			match(Id);
			setState(77);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==Semicolon) {
				{
				setState(76);
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
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof TechlandScriptVisitor ) return ((TechlandScriptVisitor<? extends T>)visitor).visitDirectiveCall(this);
			else return visitor.visitChildren(this);
		}
	}

	public final DirectiveCallContext directiveCall() throws RecognitionException {
		DirectiveCallContext _localctx = new DirectiveCallContext(_ctx, getState());
		enterRule(_localctx, 10, RULE_directiveCall);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(79);
			match(Exclamation);
			setState(80);
			match(Id);
			setState(81);
			match(LParen);
			setState(83);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (((_la) & ~0x3f) == 0 && ((1L << _la) & 115972784256L) != 0) {
				{
				setState(82);
				valueList();
				}
			}

			setState(85);
			match(RParen);
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
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof TechlandScriptVisitor ) return ((TechlandScriptVisitor<? extends T>)visitor).visitMacroDecl(this);
			else return visitor.visitChildren(this);
		}
	}

	public final MacroDeclContext macroDecl() throws RecognitionException {
		MacroDeclContext _localctx = new MacroDeclContext(_ctx, getState());
		enterRule(_localctx, 12, RULE_macroDecl);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(89);
			match(MacroId);
			setState(90);
			match(LParen);
			setState(91);
			valueList();
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
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof TechlandScriptVisitor ) return ((TechlandScriptVisitor<? extends T>)visitor).visitSubDecl(this);
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
			setState(96);
			match(Sub);
			setState(97);
			match(Id);
			setState(98);
			match(LParen);
			setState(100);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==Id) {
				{
				setState(99);
				paramList();
				}
			}

			setState(102);
			match(RParen);
			setState(103);
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
	public static class ParamListContext extends ParserRuleContext {
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
		public ParamListContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
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

	public final ParamListContext paramList() throws RecognitionException {
		ParamListContext _localctx = new ParamListContext(_ctx, getState());
		enterRule(_localctx, 16, RULE_paramList);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(105);
			param();
			setState(110);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==Comma) {
				{
				{
				setState(106);
				match(Comma);
				setState(107);
				param();
				}
				}
				setState(112);
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
	public static class ParamContext extends ParserRuleContext {
		public TypeContext type() {
			return getRuleContext(TypeContext.class,0);
		}
		public TerminalNode Id() { return getToken(TechlandScriptParser.Id, 0); }
		public TerminalNode Equals() { return getToken(TechlandScriptParser.Equals, 0); }
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public ParamContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
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

	public final ParamContext param() throws RecognitionException {
		ParamContext _localctx = new ParamContext(_ctx, getState());
		enterRule(_localctx, 18, RULE_param);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(113);
			type();
			setState(114);
			match(Id);
			setState(117);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==Equals) {
				{
				setState(115);
				match(Equals);
				setState(116);
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
	public static class FunctionBlockContext extends ParserRuleContext {
		public TerminalNode LBrace() { return getToken(TechlandScriptParser.LBrace, 0); }
		public TerminalNode RBrace() { return getToken(TechlandScriptParser.RBrace, 0); }
		public List<StatementsContext> statements() {
			return getRuleContexts(StatementsContext.class);
		}
		public StatementsContext statements(int i) {
			return getRuleContext(StatementsContext.class,i);
		}
		public FunctionBlockContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
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

	public final FunctionBlockContext functionBlock() throws RecognitionException {
		FunctionBlockContext _localctx = new FunctionBlockContext(_ctx, getState());
		enterRule(_localctx, 20, RULE_functionBlock);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(119);
			match(LBrace);
			setState(123);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (((_la) & ~0x3f) == 0 && ((1L << _la) & 8589934628L) != 0) {
				{
				{
				setState(120);
				statements();
				}
				}
				setState(125);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(126);
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
	public static class StatementsContext extends ParserRuleContext {
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
		public StatementsContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
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

	public final StatementsContext statements() throws RecognitionException {
		StatementsContext _localctx = new StatementsContext(_ctx, getState());
		enterRule(_localctx, 22, RULE_statements);
		try {
			setState(133);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,12,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(128);
				funtionCallDecl();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(129);
				funtionBlockDecl();
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(130);
				useDecl();
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(131);
				variableDecl();
				}
				break;
			case 5:
				enterOuterAlt(_localctx, 5);
				{
				setState(132);
				externDecl();
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
	public static class VariableDeclContext extends ParserRuleContext {
		public TerminalNode Id() { return getToken(TechlandScriptParser.Id, 0); }
		public TypeContext type() {
			return getRuleContext(TypeContext.class,0);
		}
		public TerminalNode Equals() { return getToken(TechlandScriptParser.Equals, 0); }
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public TerminalNode Semicolon() { return getToken(TechlandScriptParser.Semicolon, 0); }
		public VariableDeclContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
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

	public final VariableDeclContext variableDecl() throws RecognitionException {
		VariableDeclContext _localctx = new VariableDeclContext(_ctx, getState());
		enterRule(_localctx, 24, RULE_variableDecl);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(136);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,13,_ctx) ) {
			case 1:
				{
				setState(135);
				type();
				}
				break;
			}
			setState(138);
			match(Id);
			setState(141);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==Equals) {
				{
				setState(139);
				match(Equals);
				setState(140);
				expression(0);
				}
			}

			setState(144);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==Semicolon) {
				{
				setState(143);
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
		public TerminalNode LParen() { return getToken(TechlandScriptParser.LParen, 0); }
		public TerminalNode RParen() { return getToken(TechlandScriptParser.RParen, 0); }
		public ValueListContext valueList() {
			return getRuleContext(ValueListContext.class,0);
		}
		public TerminalNode Semicolon() { return getToken(TechlandScriptParser.Semicolon, 0); }
		public FuntionCallDeclContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
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

	public final FuntionCallDeclContext funtionCallDecl() throws RecognitionException {
		FuntionCallDeclContext _localctx = new FuntionCallDeclContext(_ctx, getState());
		enterRule(_localctx, 26, RULE_funtionCallDecl);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(146);
			match(Id);
			setState(147);
			match(LParen);
			setState(149);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (((_la) & ~0x3f) == 0 && ((1L << _la) & 115972784256L) != 0) {
				{
				setState(148);
				valueList();
				}
			}

			setState(151);
			match(RParen);
			setState(153);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==Semicolon) {
				{
				setState(152);
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
	public static class FuntionBlockDeclContext extends ParserRuleContext {
		public TerminalNode Id() { return getToken(TechlandScriptParser.Id, 0); }
		public TerminalNode LParen() { return getToken(TechlandScriptParser.LParen, 0); }
		public TerminalNode RParen() { return getToken(TechlandScriptParser.RParen, 0); }
		public FunctionBlockContext functionBlock() {
			return getRuleContext(FunctionBlockContext.class,0);
		}
		public ValueListContext valueList() {
			return getRuleContext(ValueListContext.class,0);
		}
		public FuntionBlockDeclContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
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

	public final FuntionBlockDeclContext funtionBlockDecl() throws RecognitionException {
		FuntionBlockDeclContext _localctx = new FuntionBlockDeclContext(_ctx, getState());
		enterRule(_localctx, 28, RULE_funtionBlockDecl);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(155);
			match(Id);
			setState(156);
			match(LParen);
			setState(158);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (((_la) & ~0x3f) == 0 && ((1L << _la) & 115972784256L) != 0) {
				{
				setState(157);
				valueList();
				}
			}

			setState(160);
			match(RParen);
			setState(161);
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
	public static class UseDeclContext extends ParserRuleContext {
		public TerminalNode Use() { return getToken(TechlandScriptParser.Use, 0); }
		public TerminalNode Id() { return getToken(TechlandScriptParser.Id, 0); }
		public TerminalNode LParen() { return getToken(TechlandScriptParser.LParen, 0); }
		public TerminalNode RParen() { return getToken(TechlandScriptParser.RParen, 0); }
		public ValueListContext valueList() {
			return getRuleContext(ValueListContext.class,0);
		}
		public TerminalNode Semicolon() { return getToken(TechlandScriptParser.Semicolon, 0); }
		public UseDeclContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
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

	public final UseDeclContext useDecl() throws RecognitionException {
		UseDeclContext _localctx = new UseDeclContext(_ctx, getState());
		enterRule(_localctx, 30, RULE_useDecl);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(163);
			match(Use);
			setState(164);
			match(Id);
			setState(165);
			match(LParen);
			setState(167);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (((_la) & ~0x3f) == 0 && ((1L << _la) & 115972784256L) != 0) {
				{
				setState(166);
				valueList();
				}
			}

			setState(169);
			match(RParen);
			setState(171);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==Semicolon) {
				{
				setState(170);
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
	public static class ValueListContext extends ParserRuleContext {
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
		public ValueListContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
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

	public final ValueListContext valueList() throws RecognitionException {
		ValueListContext _localctx = new ValueListContext(_ctx, getState());
		enterRule(_localctx, 32, RULE_valueList);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(173);
			expression(0);
			setState(178);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==Comma) {
				{
				{
				setState(174);
				match(Comma);
				setState(175);
				expression(0);
				}
				}
				setState(180);
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

	public final TypeContext type() throws RecognitionException {
		TypeContext _localctx = new TypeContext(_ctx, getState());
		enterRule(_localctx, 34, RULE_type);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(181);
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

	public final ExpressionContext expression() throws RecognitionException {
		return expression(0);
	}

	private ExpressionContext expression(int _p) throws RecognitionException {
		ParserRuleContext _parentctx = _ctx;
		int _parentState = getState();
		ExpressionContext _localctx = new ExpressionContext(_ctx, _parentState);
		ExpressionContext _prevctx = _localctx;
		int _startState = 36;
		enterRecursionRule(_localctx, 36, RULE_expression, _p);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(207);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,23,_ctx) ) {
			case 1:
				{
				setState(184);
				match(LParen);
				setState(185);
				expression(0);
				setState(186);
				match(RParen);
				}
				break;
			case 2:
				{
				setState(188);
				fieldAccess();
				setState(189);
				match(LParen);
				setState(191);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (((_la) & ~0x3f) == 0 && ((1L << _la) & 115972784256L) != 0) {
					{
					setState(190);
					valueList();
					}
				}

				setState(193);
				match(RParen);
				}
				break;
			case 3:
				{
				setState(195);
				fieldAccess();
				}
				break;
			case 4:
				{
				setState(196);
				match(Number);
				}
				break;
			case 5:
				{
				setState(197);
				match(String);
				}
				break;
			case 6:
				{
				setState(198);
				match(Bool);
				}
				break;
			case 7:
				{
				setState(199);
				arrayValue();
				}
				break;
			case 8:
				{
				setState(200);
				match(Id);
				setState(201);
				match(Equals);
				setState(202);
				expression(9);
				}
				break;
			case 9:
				{
				setState(203);
				match(BitNot);
				setState(204);
				expression(8);
				}
				break;
			case 10:
				{
				setState(205);
				match(Minus);
				setState(206);
				expression(7);
				}
				break;
			}
			_ctx.stop = _input.LT(-1);
			setState(232);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,25,_ctx);
			while ( _alt!=2 && _alt!= ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					if ( _parseListeners!=null ) triggerExitRuleEvent();
					_prevctx = _localctx;
					{
					setState(230);
					_errHandler.sync(this);
					switch ( getInterpreter().adaptivePredict(_input,24,_ctx) ) {
					case 1:
						{
						_localctx = new ExpressionContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(209);
						if (!(precpred(_ctx, 6))) throw new FailedPredicateException(this, "precpred(_ctx, 6)");
						setState(210);
						_la = _input.LA(1);
						if ( !(_la==Mul || _la==Div) ) {
						_errHandler.recoverInline(this);
						}
						else {
							if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
							_errHandler.reportMatch(this);
							consume();
						}
						setState(211);
						expression(7);
						}
						break;
					case 2:
						{
						_localctx = new ExpressionContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(212);
						if (!(precpred(_ctx, 5))) throw new FailedPredicateException(this, "precpred(_ctx, 5)");
						setState(213);
						_la = _input.LA(1);
						if ( !(_la==Plus || _la==Minus) ) {
						_errHandler.recoverInline(this);
						}
						else {
							if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
							_errHandler.reportMatch(this);
							consume();
						}
						setState(214);
						expression(6);
						}
						break;
					case 3:
						{
						_localctx = new ExpressionContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(215);
						if (!(precpred(_ctx, 4))) throw new FailedPredicateException(this, "precpred(_ctx, 4)");
						setState(216);
						match(BitOr);
						setState(217);
						expression(5);
						}
						break;
					case 4:
						{
						_localctx = new ExpressionContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(218);
						if (!(precpred(_ctx, 3))) throw new FailedPredicateException(this, "precpred(_ctx, 3)");
						setState(219);
						match(BitAnd);
						setState(220);
						expression(4);
						}
						break;
					case 5:
						{
						_localctx = new ExpressionContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(221);
						if (!(precpred(_ctx, 2))) throw new FailedPredicateException(this, "precpred(_ctx, 2)");
						setState(222);
						_la = _input.LA(1);
						if ( !(((_la) & ~0x3f) == 0 && ((1L << _la) & 4227858432L) != 0) ) {
						_errHandler.recoverInline(this);
						}
						else {
							if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
							_errHandler.reportMatch(this);
							consume();
						}
						setState(223);
						expression(3);
						}
						break;
					case 6:
						{
						_localctx = new ExpressionContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(224);
						if (!(precpred(_ctx, 1))) throw new FailedPredicateException(this, "precpred(_ctx, 1)");
						setState(225);
						match(Question);
						setState(226);
						expression(0);
						setState(227);
						match(Colon);
						setState(228);
						expression(2);
						}
						break;
					}
					} 
				}
				setState(234);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,25,_ctx);
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

	public final FieldAccessContext fieldAccess() throws RecognitionException {
		FieldAccessContext _localctx = new FieldAccessContext(_ctx, getState());
		enterRule(_localctx, 38, RULE_fieldAccess);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(235);
			match(Id);
			setState(240);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,26,_ctx);
			while ( _alt!=2 && _alt!= ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(236);
					match(Dot);
					setState(237);
					match(Id);
					}
					} 
				}
				setState(242);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,26,_ctx);
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
	public static class ArrayValueContext extends ParserRuleContext {
		public TerminalNode LBracket() { return getToken(TechlandScriptParser.LBracket, 0); }
		public TerminalNode RBracket() { return getToken(TechlandScriptParser.RBracket, 0); }
		public ValueListContext valueList() {
			return getRuleContext(ValueListContext.class,0);
		}
		public ArrayValueContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
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

	public final ArrayValueContext arrayValue() throws RecognitionException {
		ArrayValueContext _localctx = new ArrayValueContext(_ctx, getState());
		enterRule(_localctx, 40, RULE_arrayValue);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(243);
			match(LBracket);
			setState(245);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (((_la) & ~0x3f) == 0 && ((1L << _la) & 115972784256L) != 0) {
				{
				setState(244);
				valueList();
				}
			}

			setState(247);
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
		case 18:
			return expression_sempred((ExpressionContext)_localctx, predIndex);
		}
		return true;
	}
	private boolean expression_sempred(ExpressionContext _localctx, int predIndex) {
		switch (predIndex) {
		case 0:
			return precpred(_ctx, 6);
		case 1:
			return precpred(_ctx, 5);
		case 2:
			return precpred(_ctx, 4);
		case 3:
			return precpred(_ctx, 3);
		case 4:
			return precpred(_ctx, 2);
		case 5:
			return precpred(_ctx, 1);
		}
		return true;
	}

	public static final String _serializedATN =
		"\u0004\u0001\'\u00fa\u0002\u0000\u0007\u0000\u0002\u0001\u0007\u0001\u0002"+
		"\u0002\u0007\u0002\u0002\u0003\u0007\u0003\u0002\u0004\u0007\u0004\u0002"+
		"\u0005\u0007\u0005\u0002\u0006\u0007\u0006\u0002\u0007\u0007\u0007\u0002"+
		"\b\u0007\b\u0002\t\u0007\t\u0002\n\u0007\n\u0002\u000b\u0007\u000b\u0002"+
		"\f\u0007\f\u0002\r\u0007\r\u0002\u000e\u0007\u000e\u0002\u000f\u0007\u000f"+
		"\u0002\u0010\u0007\u0010\u0002\u0011\u0007\u0011\u0002\u0012\u0007\u0012"+
		"\u0002\u0013\u0007\u0013\u0002\u0014\u0007\u0014\u0001\u0000\u0005\u0000"+
		",\b\u0000\n\u0000\f\u0000/\t\u0000\u0001\u0000\u0001\u0000\u0001\u0001"+
		"\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001"+
		"\u0001\u0001\u0003\u0001;\b\u0001\u0001\u0002\u0001\u0002\u0001\u0002"+
		"\u0003\u0002@\b\u0002\u0001\u0003\u0001\u0003\u0001\u0003\u0001\u0003"+
		"\u0001\u0003\u0001\u0003\u0003\u0003H\b\u0003\u0001\u0004\u0001\u0004"+
		"\u0001\u0004\u0001\u0004\u0003\u0004N\b\u0004\u0001\u0005\u0001\u0005"+
		"\u0001\u0005\u0001\u0005\u0003\u0005T\b\u0005\u0001\u0005\u0001\u0005"+
		"\u0003\u0005X\b\u0005\u0001\u0006\u0001\u0006\u0001\u0006\u0001\u0006"+
		"\u0001\u0006\u0003\u0006_\b\u0006\u0001\u0007\u0001\u0007\u0001\u0007"+
		"\u0001\u0007\u0003\u0007e\b\u0007\u0001\u0007\u0001\u0007\u0001\u0007"+
		"\u0001\b\u0001\b\u0001\b\u0005\bm\b\b\n\b\f\bp\t\b\u0001\t\u0001\t\u0001"+
		"\t\u0001\t\u0003\tv\b\t\u0001\n\u0001\n\u0005\nz\b\n\n\n\f\n}\t\n\u0001"+
		"\n\u0001\n\u0001\u000b\u0001\u000b\u0001\u000b\u0001\u000b\u0001\u000b"+
		"\u0003\u000b\u0086\b\u000b\u0001\f\u0003\f\u0089\b\f\u0001\f\u0001\f\u0001"+
		"\f\u0003\f\u008e\b\f\u0001\f\u0003\f\u0091\b\f\u0001\r\u0001\r\u0001\r"+
		"\u0003\r\u0096\b\r\u0001\r\u0001\r\u0003\r\u009a\b\r\u0001\u000e\u0001"+
		"\u000e\u0001\u000e\u0003\u000e\u009f\b\u000e\u0001\u000e\u0001\u000e\u0001"+
		"\u000e\u0001\u000f\u0001\u000f\u0001\u000f\u0001\u000f\u0003\u000f\u00a8"+
		"\b\u000f\u0001\u000f\u0001\u000f\u0003\u000f\u00ac\b\u000f\u0001\u0010"+
		"\u0001\u0010\u0001\u0010\u0005\u0010\u00b1\b\u0010\n\u0010\f\u0010\u00b4"+
		"\t\u0010\u0001\u0011\u0001\u0011\u0001\u0012\u0001\u0012\u0001\u0012\u0001"+
		"\u0012\u0001\u0012\u0001\u0012\u0001\u0012\u0001\u0012\u0003\u0012\u00c0"+
		"\b\u0012\u0001\u0012\u0001\u0012\u0001\u0012\u0001\u0012\u0001\u0012\u0001"+
		"\u0012\u0001\u0012\u0001\u0012\u0001\u0012\u0001\u0012\u0001\u0012\u0001"+
		"\u0012\u0001\u0012\u0001\u0012\u0003\u0012\u00d0\b\u0012\u0001\u0012\u0001"+
		"\u0012\u0001\u0012\u0001\u0012\u0001\u0012\u0001\u0012\u0001\u0012\u0001"+
		"\u0012\u0001\u0012\u0001\u0012\u0001\u0012\u0001\u0012\u0001\u0012\u0001"+
		"\u0012\u0001\u0012\u0001\u0012\u0001\u0012\u0001\u0012\u0001\u0012\u0001"+
		"\u0012\u0001\u0012\u0005\u0012\u00e7\b\u0012\n\u0012\f\u0012\u00ea\t\u0012"+
		"\u0001\u0013\u0001\u0013\u0001\u0013\u0005\u0013\u00ef\b\u0013\n\u0013"+
		"\f\u0013\u00f2\t\u0013\u0001\u0014\u0001\u0014\u0003\u0014\u00f6\b\u0014"+
		"\u0001\u0014\u0001\u0014\u0001\u0014\u0000\u0001$\u0015\u0000\u0002\u0004"+
		"\u0006\b\n\f\u000e\u0010\u0012\u0014\u0016\u0018\u001a\u001c\u001e \""+
		"$&(\u0000\u0003\u0001\u0000\u0013\u0014\u0001\u0000\u0011\u0012\u0001"+
		"\u0000\u001a\u001f\u0115\u0000-\u0001\u0000\u0000\u0000\u0002:\u0001\u0000"+
		"\u0000\u0000\u0004<\u0001\u0000\u0000\u0000\u0006A\u0001\u0000\u0000\u0000"+
		"\bI\u0001\u0000\u0000\u0000\nO\u0001\u0000\u0000\u0000\fY\u0001\u0000"+
		"\u0000\u0000\u000e`\u0001\u0000\u0000\u0000\u0010i\u0001\u0000\u0000\u0000"+
		"\u0012q\u0001\u0000\u0000\u0000\u0014w\u0001\u0000\u0000\u0000\u0016\u0085"+
		"\u0001\u0000\u0000\u0000\u0018\u0088\u0001\u0000\u0000\u0000\u001a\u0092"+
		"\u0001\u0000\u0000\u0000\u001c\u009b\u0001\u0000\u0000\u0000\u001e\u00a3"+
		"\u0001\u0000\u0000\u0000 \u00ad\u0001\u0000\u0000\u0000\"\u00b5\u0001"+
		"\u0000\u0000\u0000$\u00cf\u0001\u0000\u0000\u0000&\u00eb\u0001\u0000\u0000"+
		"\u0000(\u00f3\u0001\u0000\u0000\u0000*,\u0003\u0002\u0001\u0000+*\u0001"+
		"\u0000\u0000\u0000,/\u0001\u0000\u0000\u0000-+\u0001\u0000\u0000\u0000"+
		"-.\u0001\u0000\u0000\u0000.0\u0001\u0000\u0000\u0000/-\u0001\u0000\u0000"+
		"\u000001\u0005\u0000\u0000\u00011\u0001\u0001\u0000\u0000\u00002;\u0003"+
		"\u0004\u0002\u00003;\u0003\u0006\u0003\u00004;\u0003\u000e\u0007\u0000"+
		"5;\u0003\n\u0005\u00006;\u0003\f\u0006\u00007;\u0003\u0018\f\u00008;\u0003"+
		"\u001a\r\u00009;\u0003\u001c\u000e\u0000:2\u0001\u0000\u0000\u0000:3\u0001"+
		"\u0000\u0000\u0000:4\u0001\u0000\u0000\u0000:5\u0001\u0000\u0000\u0000"+
		":6\u0001\u0000\u0000\u0000:7\u0001\u0000\u0000\u0000:8\u0001\u0000\u0000"+
		"\u0000:9\u0001\u0000\u0000\u0000;\u0003\u0001\u0000\u0000\u0000<=\u0005"+
		"\u0001\u0000\u0000=?\u0005$\u0000\u0000>@\u0005\u000b\u0000\u0000?>\u0001"+
		"\u0000\u0000\u0000?@\u0001\u0000\u0000\u0000@\u0005\u0001\u0000\u0000"+
		"\u0000AB\u0005\u0003\u0000\u0000BC\u0003\"\u0011\u0000CD\u0005!\u0000"+
		"\u0000DE\u0005\r\u0000\u0000EG\u0003$\u0012\u0000FH\u0005\u000b\u0000"+
		"\u0000GF\u0001\u0000\u0000\u0000GH\u0001\u0000\u0000\u0000H\u0007\u0001"+
		"\u0000\u0000\u0000IJ\u0005\u0002\u0000\u0000JK\u0003\"\u0011\u0000KM\u0005"+
		"!\u0000\u0000LN\u0005\u000b\u0000\u0000ML\u0001\u0000\u0000\u0000MN\u0001"+
		"\u0000\u0000\u0000N\t\u0001\u0000\u0000\u0000OP\u0005\u0006\u0000\u0000"+
		"PQ\u0005!\u0000\u0000QS\u0005\u0007\u0000\u0000RT\u0003 \u0010\u0000S"+
		"R\u0001\u0000\u0000\u0000ST\u0001\u0000\u0000\u0000TU\u0001\u0000\u0000"+
		"\u0000UW\u0005\b\u0000\u0000VX\u0005\u000b\u0000\u0000WV\u0001\u0000\u0000"+
		"\u0000WX\u0001\u0000\u0000\u0000X\u000b\u0001\u0000\u0000\u0000YZ\u0005"+
		"\"\u0000\u0000Z[\u0005\u0007\u0000\u0000[\\\u0003 \u0010\u0000\\^\u0005"+
		"\b\u0000\u0000]_\u0005\u000b\u0000\u0000^]\u0001\u0000\u0000\u0000^_\u0001"+
		"\u0000\u0000\u0000_\r\u0001\u0000\u0000\u0000`a\u0005\u0004\u0000\u0000"+
		"ab\u0005!\u0000\u0000bd\u0005\u0007\u0000\u0000ce\u0003\u0010\b\u0000"+
		"dc\u0001\u0000\u0000\u0000de\u0001\u0000\u0000\u0000ef\u0001\u0000\u0000"+
		"\u0000fg\u0005\b\u0000\u0000gh\u0003\u0014\n\u0000h\u000f\u0001\u0000"+
		"\u0000\u0000in\u0003\u0012\t\u0000jk\u0005\f\u0000\u0000km\u0003\u0012"+
		"\t\u0000lj\u0001\u0000\u0000\u0000mp\u0001\u0000\u0000\u0000nl\u0001\u0000"+
		"\u0000\u0000no\u0001\u0000\u0000\u0000o\u0011\u0001\u0000\u0000\u0000"+
		"pn\u0001\u0000\u0000\u0000qr\u0003\"\u0011\u0000ru\u0005!\u0000\u0000"+
		"st\u0005\r\u0000\u0000tv\u0003$\u0012\u0000us\u0001\u0000\u0000\u0000"+
		"uv\u0001\u0000\u0000\u0000v\u0013\u0001\u0000\u0000\u0000w{\u0005\t\u0000"+
		"\u0000xz\u0003\u0016\u000b\u0000yx\u0001\u0000\u0000\u0000z}\u0001\u0000"+
		"\u0000\u0000{y\u0001\u0000\u0000\u0000{|\u0001\u0000\u0000\u0000|~\u0001"+
		"\u0000\u0000\u0000}{\u0001\u0000\u0000\u0000~\u007f\u0005\n\u0000\u0000"+
		"\u007f\u0015\u0001\u0000\u0000\u0000\u0080\u0086\u0003\u001a\r\u0000\u0081"+
		"\u0086\u0003\u001c\u000e\u0000\u0082\u0086\u0003\u001e\u000f\u0000\u0083"+
		"\u0086\u0003\u0018\f\u0000\u0084\u0086\u0003\b\u0004\u0000\u0085\u0080"+
		"\u0001\u0000\u0000\u0000\u0085\u0081\u0001\u0000\u0000\u0000\u0085\u0082"+
		"\u0001\u0000\u0000\u0000\u0085\u0083\u0001\u0000\u0000\u0000\u0085\u0084"+
		"\u0001\u0000\u0000\u0000\u0086\u0017\u0001\u0000\u0000\u0000\u0087\u0089"+
		"\u0003\"\u0011\u0000\u0088\u0087\u0001\u0000\u0000\u0000\u0088\u0089\u0001"+
		"\u0000\u0000\u0000\u0089\u008a\u0001\u0000\u0000\u0000\u008a\u008d\u0005"+
		"!\u0000\u0000\u008b\u008c\u0005\r\u0000\u0000\u008c\u008e\u0003$\u0012"+
		"\u0000\u008d\u008b\u0001\u0000\u0000\u0000\u008d\u008e\u0001\u0000\u0000"+
		"\u0000\u008e\u0090\u0001\u0000\u0000\u0000\u008f\u0091\u0005\u000b\u0000"+
		"\u0000\u0090\u008f\u0001\u0000\u0000\u0000\u0090\u0091\u0001\u0000\u0000"+
		"\u0000\u0091\u0019\u0001\u0000\u0000\u0000\u0092\u0093\u0005!\u0000\u0000"+
		"\u0093\u0095\u0005\u0007\u0000\u0000\u0094\u0096\u0003 \u0010\u0000\u0095"+
		"\u0094\u0001\u0000\u0000\u0000\u0095\u0096\u0001\u0000\u0000\u0000\u0096"+
		"\u0097\u0001\u0000\u0000\u0000\u0097\u0099\u0005\b\u0000\u0000\u0098\u009a"+
		"\u0005\u000b\u0000\u0000\u0099\u0098\u0001\u0000\u0000\u0000\u0099\u009a"+
		"\u0001\u0000\u0000\u0000\u009a\u001b\u0001\u0000\u0000\u0000\u009b\u009c"+
		"\u0005!\u0000\u0000\u009c\u009e\u0005\u0007\u0000\u0000\u009d\u009f\u0003"+
		" \u0010\u0000\u009e\u009d\u0001\u0000\u0000\u0000\u009e\u009f\u0001\u0000"+
		"\u0000\u0000\u009f\u00a0\u0001\u0000\u0000\u0000\u00a0\u00a1\u0005\b\u0000"+
		"\u0000\u00a1\u00a2\u0003\u0014\n\u0000\u00a2\u001d\u0001\u0000\u0000\u0000"+
		"\u00a3\u00a4\u0005\u0005\u0000\u0000\u00a4\u00a5\u0005!\u0000\u0000\u00a5"+
		"\u00a7\u0005\u0007\u0000\u0000\u00a6\u00a8\u0003 \u0010\u0000\u00a7\u00a6"+
		"\u0001\u0000\u0000\u0000\u00a7\u00a8\u0001\u0000\u0000\u0000\u00a8\u00a9"+
		"\u0001\u0000\u0000\u0000\u00a9\u00ab\u0005\b\u0000\u0000\u00aa\u00ac\u0005"+
		"\u000b\u0000\u0000\u00ab\u00aa\u0001\u0000\u0000\u0000\u00ab\u00ac\u0001"+
		"\u0000\u0000\u0000\u00ac\u001f\u0001\u0000\u0000\u0000\u00ad\u00b2\u0003"+
		"$\u0012\u0000\u00ae\u00af\u0005\f\u0000\u0000\u00af\u00b1\u0003$\u0012"+
		"\u0000\u00b0\u00ae\u0001\u0000\u0000\u0000\u00b1\u00b4\u0001\u0000\u0000"+
		"\u0000\u00b2\u00b0\u0001\u0000\u0000\u0000\u00b2\u00b3\u0001\u0000\u0000"+
		"\u0000\u00b3!\u0001\u0000\u0000\u0000\u00b4\u00b2\u0001\u0000\u0000\u0000"+
		"\u00b5\u00b6\u0005!\u0000\u0000\u00b6#\u0001\u0000\u0000\u0000\u00b7\u00b8"+
		"\u0006\u0012\uffff\uffff\u0000\u00b8\u00b9\u0005\u0007\u0000\u0000\u00b9"+
		"\u00ba\u0003$\u0012\u0000\u00ba\u00bb\u0005\b\u0000\u0000\u00bb\u00d0"+
		"\u0001\u0000\u0000\u0000\u00bc\u00bd\u0003&\u0013\u0000\u00bd\u00bf\u0005"+
		"\u0007\u0000\u0000\u00be\u00c0\u0003 \u0010\u0000\u00bf\u00be\u0001\u0000"+
		"\u0000\u0000\u00bf\u00c0\u0001\u0000\u0000\u0000\u00c0\u00c1\u0001\u0000"+
		"\u0000\u0000\u00c1\u00c2\u0005\b\u0000\u0000\u00c2\u00d0\u0001\u0000\u0000"+
		"\u0000\u00c3\u00d0\u0003&\u0013\u0000\u00c4\u00d0\u0005#\u0000\u0000\u00c5"+
		"\u00d0\u0005$\u0000\u0000\u00c6\u00d0\u0005 \u0000\u0000\u00c7\u00d0\u0003"+
		"(\u0014\u0000\u00c8\u00c9\u0005!\u0000\u0000\u00c9\u00ca\u0005\r\u0000"+
		"\u0000\u00ca\u00d0\u0003$\u0012\t\u00cb\u00cc\u0005\u0017\u0000\u0000"+
		"\u00cc\u00d0\u0003$\u0012\b\u00cd\u00ce\u0005\u0012\u0000\u0000\u00ce"+
		"\u00d0\u0003$\u0012\u0007\u00cf\u00b7\u0001\u0000\u0000\u0000\u00cf\u00bc"+
		"\u0001\u0000\u0000\u0000\u00cf\u00c3\u0001\u0000\u0000\u0000\u00cf\u00c4"+
		"\u0001\u0000\u0000\u0000\u00cf\u00c5\u0001\u0000\u0000\u0000\u00cf\u00c6"+
		"\u0001\u0000\u0000\u0000\u00cf\u00c7\u0001\u0000\u0000\u0000\u00cf\u00c8"+
		"\u0001\u0000\u0000\u0000\u00cf\u00cb\u0001\u0000\u0000\u0000\u00cf\u00cd"+
		"\u0001\u0000\u0000\u0000\u00d0\u00e8\u0001\u0000\u0000\u0000\u00d1\u00d2"+
		"\n\u0006\u0000\u0000\u00d2\u00d3\u0007\u0000\u0000\u0000\u00d3\u00e7\u0003"+
		"$\u0012\u0007\u00d4\u00d5\n\u0005\u0000\u0000\u00d5\u00d6\u0007\u0001"+
		"\u0000\u0000\u00d6\u00e7\u0003$\u0012\u0006\u00d7\u00d8\n\u0004\u0000"+
		"\u0000\u00d8\u00d9\u0005\u0015\u0000\u0000\u00d9\u00e7\u0003$\u0012\u0005"+
		"\u00da\u00db\n\u0003\u0000\u0000\u00db\u00dc\u0005\u0016\u0000\u0000\u00dc"+
		"\u00e7\u0003$\u0012\u0004\u00dd\u00de\n\u0002\u0000\u0000\u00de\u00df"+
		"\u0007\u0002\u0000\u0000\u00df\u00e7\u0003$\u0012\u0003\u00e0\u00e1\n"+
		"\u0001\u0000\u0000\u00e1\u00e2\u0005\u0018\u0000\u0000\u00e2\u00e3\u0003"+
		"$\u0012\u0000\u00e3\u00e4\u0005\u0019\u0000\u0000\u00e4\u00e5\u0003$\u0012"+
		"\u0002\u00e5\u00e7\u0001\u0000\u0000\u0000\u00e6\u00d1\u0001\u0000\u0000"+
		"\u0000\u00e6\u00d4\u0001\u0000\u0000\u0000\u00e6\u00d7\u0001\u0000\u0000"+
		"\u0000\u00e6\u00da\u0001\u0000\u0000\u0000\u00e6\u00dd\u0001\u0000\u0000"+
		"\u0000\u00e6\u00e0\u0001\u0000\u0000\u0000\u00e7\u00ea\u0001\u0000\u0000"+
		"\u0000\u00e8\u00e6\u0001\u0000\u0000\u0000\u00e8\u00e9\u0001\u0000\u0000"+
		"\u0000\u00e9%\u0001\u0000\u0000\u0000\u00ea\u00e8\u0001\u0000\u0000\u0000"+
		"\u00eb\u00f0\u0005!\u0000\u0000\u00ec\u00ed\u0005\u0010\u0000\u0000\u00ed"+
		"\u00ef\u0005!\u0000\u0000\u00ee\u00ec\u0001\u0000\u0000\u0000\u00ef\u00f2"+
		"\u0001\u0000\u0000\u0000\u00f0\u00ee\u0001\u0000\u0000\u0000\u00f0\u00f1"+
		"\u0001\u0000\u0000\u0000\u00f1\'\u0001\u0000\u0000\u0000\u00f2\u00f0\u0001"+
		"\u0000\u0000\u0000\u00f3\u00f5\u0005\u000e\u0000\u0000\u00f4\u00f6\u0003"+
		" \u0010\u0000\u00f5\u00f4\u0001\u0000\u0000\u0000\u00f5\u00f6\u0001\u0000"+
		"\u0000\u0000\u00f6\u00f7\u0001\u0000\u0000\u0000\u00f7\u00f8\u0005\u000f"+
		"\u0000\u0000\u00f8)\u0001\u0000\u0000\u0000\u001c-:?GMSW^dnu{\u0085\u0088"+
		"\u008d\u0090\u0095\u0099\u009e\u00a7\u00ab\u00b2\u00bf\u00cf\u00e6\u00e8"+
		"\u00f0\u00f5";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}