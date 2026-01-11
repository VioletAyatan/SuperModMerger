grammar TechlandScript;

// ---------------- 解析规则 (Parser Rules) ----------------

file
    : (definition)* EOF
    ;

definition
    : importDecl
    | exportDecl
    | externDecl
    | subDecl
    | directiveCall
    | macroDecl
    | variableDecl
    | funtionCallDecl
    | funtionBlockDecl
    ;
//导入导出声明
importDecl
    : Import String Semicolon?
    ;
exportDecl
    : Export type Id Equals expression Semicolon? // 统一使用 expression
    ;
// Extern声明
externDecl
    : Extern type Id Semicolon?
    ;
//!开头的声明，不清楚具体意思，只是添加了这个解析规则。类似：!include("xxx")
directiveCall
    : Exclamation Id LParen valueList? RParen Semicolon?
    ;
//宏定义($ID(value, value, ...))
macroDecl
    : MacroId LParen valueList RParen Semicolon?
    ;
//sub函数声明
subDecl
    : Sub Id LParen paramList? RParen functionBlock
    ;
//逻辑控制语句
logicControlDecl
    : KwIf LParen expression RParen functionBlock elseIfClause* elseClause?
    ;
elseIfClause
    : KwElse KwIf LParen expression RParen functionBlock  // 支持 else if
    ;
elseClause
    : KwElse (LParen expression? RParen)? functionBlock  // else 块
    ;

paramList
    : param (Comma param)*
    ;

param
    : type Id (Equals expression)? // 统一使用 expression
    ;

functionBlock
    : LBrace statements* RBrace
    ;

statements
    : funtionCallDecl
    | funtionBlockDecl
    | useDecl
    | variableDecl
    | externDecl
    | logicControlDecl
    | macroDecl
    ;

// 变量声明 (带类型, 例如: float health_critical = ...;)
variableDecl
    : type? Id (Equals expression)? Semicolon?
    ;

// 函数调用 (例如: Set("f_pp_light_leak", light_leak);)
funtionCallDecl
    : Id LParen valueList? RParen Semicolon?
    ;

//函数块 (例如: Item(...) {...})
funtionBlockDecl
    : Id LParen valueList? RParen functionBlock
    ;

//use语句
useDecl
    : Use Id LParen valueList? RParen Semicolon?
    ;

//值列表的匹配
valueList
    : expression (Comma expression)*
    ;

type
    : Id
    ;

// ------------------ 表达式规则 (Expression Rules) ------------------
expression
    // 1. 最高优先级：原子值、括号表达式、函数调用
    : LParen expression RParen
    | fieldAccess LParen valueList? RParen
    | fieldAccess
    | Number
    | String
    | Bool
    | arrayValue
    // 类似kotlin的具名参数
    | Id Equals expression
    // 一元运算符
    | BitNot expression
    | Exclamation expression
    | Minus expression
    // 乘除运算
    | expression (Mul | Div) expression
    // 加减运算
    | expression (Plus | Minus) expression
    // 位或运算
    | expression BitOr expression
    // 位与运算
    | expression BitAnd expression
    // 比较运算符
    | expression (Gt | Lt | Gte | Lte | Eq | NotEq) expression
    // 逻辑与运算
    | expression LogicAnd expression
    // 逻辑或运算
    | expression LogicOr expression
    // 三元运算符
    | expression Question expression Colon expression
    ;

//字段访问
fieldAccess
    : Id (Dot Id)*
    ;

// 数组类型
arrayValue: LBracket valueList? RBracket;

// ---------------- 词法规则 (Lexer Rules) ----------------

// 1. 符号和关键字
Import: 'import';
Extern: 'extern';
Export: 'export';
Sub: 'sub';
Use: 'use';
Exclamation: '!';
//逻辑判读关键字
KwIf: 'if' | 'If';
KwElse: 'else' | 'Else';

LParen: '(';
RParen: ')';
LBrace: '{';
RBrace: '}';
Semicolon: ';';
Comma: ',';
Equals: '=';
LBracket: '[';
RBracket: ']';

Dot: '.';
// 运算符
Plus: '+';
Minus: '-';
Mul: '*';
Div: '/';
//逻辑运算符
LogicAnd: '&&';
LogicOr: '||';
//位运算符
BitOr: '|';
BitAnd: '&';
BitNot: '~';
Question: '?';
Colon: ':';


// 比较运算符
Gt: '>';
Lt: '<';
Eq: '==';
NotEq: '!=';
Gte: '>=';
Lte: '<=';

// 2. 布尔值定义
fragment TRUE_LITERAL: 'true';
fragment FALSE_LITERAL: 'false';
Bool: TRUE_LITERAL | FALSE_LITERAL;

// 3. 标识符
Id: [a-zA-Z_] [a-zA-Z0-9_@]*;
//宏定义标识符 $XXX
MacroId: '$' [a-zA-Z_] [a-zA-Z0-9_@]*;


// 数字和字符串
Number
    // 16进制声明
    : ('0' ('x'|'X')) [0-9a-fA-F]+
    // 匹配 1.2e-5, .5, 123, 123.45 (科学计数法和浮点数/整数)
    | ( [0-9]+ ( '.' [0-9]* )? EXPONENT? )
    | ( '.' [0-9]+ EXPONENT? )
    ;
// 科学计数法的通用片段（例如 e5, E+10, e-3）
fragment EXPONENT: ('e'|'E') ('+'|'-')? [0-9]+;
//字符串格式定义，支持双引号和单引号
String: ('"' .*? '"'| '\'' .*? '\'');

// 注释和空白字符
LineComment
    : '//' ~[\r\n]* -> channel(HIDDEN)
    ;
BlockComment
    : '/*' .*? '*/' -> channel(HIDDEN)
    ;
WhiteSpaces
    : [ \t\r\n]+ -> channel(HIDDEN)
    ;