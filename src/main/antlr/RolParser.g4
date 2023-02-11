parser grammar RolParser;

options { tokenVocab = RolLexer; }

file
    : packageStatement? NL* (usingStatement | usingInStatement | NL)* statements EOF
    ;

packageStatement
    : PACKAGE package
    ;

package
    : unqualifiedIdentifier (DOT unqualifiedIdentifier)*
    ;

usingStatement
    : USING package
    ;

usingInStatement
    : USING NL* ((unqualifiedIdentifier NL* (COMMA unqualifiedIdentifier)*) | STAR) NL* IN package
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
    | classDeclaration
    | functionDeclaration
    ;

varDeclaration
    : accessModifier? CONST? NL* VAR NL* unqualifiedIdentifier (COLON NL* type)? NL* (ASSIGN NL* expression)?
    ;

assignment
    : identifier (NL* DOT NL* identifier)* NL* assignmentOp NL* expression
    ;

assignmentOp
    : ASSIGN | PLUS_ASSIGN | MINUS_ASSIGN | MULT_ASSIGN | DIV_ASSIGN | MOD_ASSIGN
    | AND_ASSIGN | OR_ASSIGN | XOR_ASSIGN
    ;

functionDeclaration
    : accessModifier? NL* FUN NL* identifier NL* argList NL* (COLON NL* type)? NL* block
    ;

argList
    : LPAREN NL* (arg (NL* COMMA NL* arg)*)? NL* RPAREN
    ;

arg
    : unqualifiedIdentifier COLON NL* type
    ;

lambda
    : LBRACE NL* (LPAREN NL* type NL* RPAREN)? (arg (NL* COMMA NL* arg)* NL* ARROW NL*)? (statements | expression) NL* RBRACE
    ;

classDeclaration
    : accessModifier? NL* CLASS NL* identifier (NL* COLON NL* identifier)? NL* LBRACE NL*
    ((fieldDeclaration | constructor | functionDeclaration) NL*)* RBRACE
    ;

fieldDeclaration
    : accessModifier? NL* CONST? NL* VAR NL* identifier (COLON NL* type)? NL* ASSIGN NL* expression
    ;

constructor
    : accessModifier? NL* INIT NL* argList NL* block
    ;

expression
    : primary
    | expression NL* DOT NL* expression
    | expression NL* LPAREN NL* (args+=expression (NL* COMMA NL* args+=expression)*)? NL* RPAREN
    | expression NL* nonNullAssertion=NOT
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
    | number
    | string
    | Boolean
    | Null
    | identifier
    | lambda
    | extern
    ;

number
    : Number | HexNumber | BinNumber | OctNumber
    ;

extern
    : EXTERN NL* (EXTERN_LPAREN (EXTERN_ID (EXTERN_COMMA EXTERN_ID)*)? EXTERN_RPAREN)?
        EXTERN_LBRACE EXTERN_CODE* EXTERN_RBRACE
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
    : ((identifier | DYN | functionType) QUESTION?)
    | type (NL* AMP NL* type)+
    | type (NL* PIPE NL* type)+
    ;

functionType
    : LPAREN NL* (args+=type (NL* COMMA NL* args+=type)*)? NL* RPAREN NL* ARROW NL* returnType=type
    ;

// identifier and soft keywords
identifier
    : (package COLON)? unqualifiedIdentifier
    ;

unqualifiedIdentifier
    : Identifier
    | CONST
    | INIT
    ;

accessModifier
    : PRIVATE | PACKAGE
    ;

string
    : String | MultilineString
    ;