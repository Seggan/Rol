lexer grammar RolLexer;

LineComment
    : '//' ~[\r\n]* -> channel(HIDDEN)
    ;

BlockComment
    : '/*' .*? '*/' -> channel(HIDDEN)
    ;

WS
    : [\u0020\u0009\u000C] -> channel(HIDDEN)
    ;

NL: '\r'? '\n';

FUN: 'fun';
EXTERN: 'extern';

VAR: 'var';
CONST: 'const';

IF: 'if';
ELSE: 'else';
IS: 'is';

WHILE: 'while';
FOR: 'for';
IN: 'in';
BREAK: 'break';
CONTINUE: 'continue';
RETURN: 'return';

STRUCT: 'struct';
INIT: 'init';
ENUM: 'enum';
INST: 'inst';

PACKAGE: 'package';
IMPORT: 'import';

PRIVATE: 'private';
DYN: 'dyn';

LPAREN: '(';
RPAREN: ')';
LBRACE: '{';
RBRACE: '}';
LBRACKET: '[';
RBRACKET: ']';

COMMA: ',';
SEMICOLON: ';';
COLON: ':';
DOT: '.';
QUESTION: '?';

ASSIGN: '=';
PLUS_ASSIGN: '+=';
MINUS_ASSIGN: '-=';
MULT_ASSIGN: '*=';
DIV_ASSIGN: '/=';
MOD_ASSIGN: '%=';
AND_ASSIGN: '&=';
OR_ASSIGN: '|=';
XOR_ASSIGN: '^=';

PLUS: '+';
MINUS: '-';
STAR: '*';
SLASH: '/';
MOD: '%';
AMP: '&';
PIPE: '|';
CARET: '^';
TILDE: '~';
SHL: '<<';
SHR: '>>';

LT: '<';
GT: '>';
EQUAL: '==';
NOT_EQUAL: '!=';
LT_EQUAL: '<=';
GT_EQUAL: '>=';
AND: '&&';
OR: '||';
NOT: '!';

INC: '++';
DEC: '--';

Number
    : [0-9]+ ('.' [0-9]+)?
    ;

String
    : '"' .*? '"'
    ;

Boolean
    : 'true' | 'false'
    ;

Null
    : 'null'
    ;

Identifier
    : [a-zA-Z_][a-zA-Z0-9_]*
    ;