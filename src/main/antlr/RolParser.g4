parser grammar RolParser;

options { tokenVocab = RolLexer; }

file
    : packageStatement? NL* (IMPORT package NL*)* statements EOF
    ;

packageStatement
    : PACKAGE package
    ;

package
    : Identifier (DOT Identifier)*
    ;

statements: (statement (NL | SEMICOLON)+)* statement?;

statement
    : assignment
    | declaration
    | expression
    | control
    ;

declaration
    : varDeclaration
    | structDeclaration
    | functionDeclaration
    | externDeclaration
    ;

varDeclaration
    : accessModifier? CONST? NL* VAR NL* identifier (COLON NL* type)? NL* (ASSIGN NL* expression)?
    ;

assignment
    : identifier (NL* DOT NL* identifier)* NL* assignmentOp NL* expression
    ;

assignmentOp
    : ASSIGN | PLUS_ASSIGN | MINUS_ASSIGN | MULT_ASSIGN | DIV_ASSIGN | MOD_ASSIGN
    | AND_ASSIGN | OR_ASSIGN | XOR_ASSIGN
    ;

functionDeclaration
    : accessModifier? NL* CONST? NL* INST? NL* FUN NL* identifier NL* argList NL* (COLON NL* type)? NL* block
    ;

argList
    : LPAREN NL* (arg (NL* COMMA NL* arg)*)? NL* RPAREN
    ;

arg
    : Identifier COLON NL* type
    ;

structDeclaration
    : accessModifier? NL* CONST? NL* STRUCT NL* identifier NL* LBRACE NL* (structField NL*)* RBRACE
    ;

structField
    : accessModifier? NL* CONST? NL* Identifier NL* COLON NL* type
    ;

externDeclaration
    : accessModifier? NL* EXTERN NL* FUN NL* identifier NL* argList NL* (COLON NL* type)? NL* ASSIGN NL* string
    ;

expression
    : primary
    | expression NL* nonNullAssertion=NOT
    | expression NL* DOT NL* (call | identifier)
    | expression NL* postfixOp=(INC | DEC)
    | prefixOp=(NOT | TILDE | MINUS | INC | DEC) NL* expression
    | expression NL* op=(STAR | SLASH | MOD) NL* expression
    | expression NL* op=(PLUS | MINUS) NL* expression
    | expression NL* op=(SHL | SHR) NL* expression
    | expression NL* op=(LT | GT | LT_EQUAL | GT_EQUAL) NL* expression
    | expression NL* op=(EQUAL | NOT_EQUAL) NL* expression
    | expression NL* op=AMP NL* expression
    | expression NL* op=CARET NL* expression
    | expression NL* op=PIPE NL* expression
    | expression NL* op=AND NL* expression
    | expression NL* op=OR NL* expression
    ;

primary
    : LPAREN NL* expression NL* RPAREN
    | structInit
    | call
    | number
    | string
    | Boolean
    | Null
    | identifier
    ;

structInit
    : identifier LBRACE NL* (fieldInit NL*)? (COMMA NL* fieldInit NL*)* RBRACE
    ;

fieldInit
    : Identifier NL* ASSIGN NL* expression
    ;

call
    : identifier NL* LPAREN NL* (expression (NL* COMMA NL* expression)*)? NL* RPAREN
    ;

number
    : Number | HexNumber | BinNumber | OctNumber
    ;

control
    : ifStatement
    | matchStatement
    | whileStatement
    | forStatement
    | foreachStatement
    | returnStatement
    | block
    | throwStatement
    ;

ifStatement
    : IF NL* LPAREN NL* expression NL* RPAREN NL* block (NL* ELSE NL* block)?
    ;

matchStatement
    : MATCH NL* LPAREN NL* expression NL* RPAREN NL* LBRACE NL* (matchCase NL*)* RBRACE
    ;

matchCase
    : expression (NL* COMMA NL* expression)* NL* COLON NL* (block | expression)
    ;

whileStatement
    : WHILE NL* LPAREN NL* expression NL* RPAREN NL* block
    ;

forStatement
    : FOR NL* LPAREN NL* (varDeclaration)? NL* SEMICOLON NL* expression? NL* SEMICOLON NL* (assignment)? NL* RPAREN NL* block
    ;

foreachStatement
    : FOR NL* LPAREN NL* VAR NL* identifier NL* IN NL* expression NL* RPAREN NL* block
    ;

returnStatement
    : RETURN NL* expression?
    ;

block
    : LBRACE NL* statements NL* RBRACE
    ;

throwStatement
    : THROW NL* expression
    ;

type
    : (identifier | DYN) QUESTION?
    ;

// identifier and soft keywords
identifier
    : (package SLASH)? name=(Identifier
    | CONST
    | INST
    | EXTERN
    | INIT)
    ;

accessModifier
    : PRIVATE | PACKAGE
    ;

string
    : String | MultilineString
    ;