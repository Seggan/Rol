parser grammar RolParser;

options { tokenVocab = RolLexer; }

file
    : statements EOF
    ;

statements: (statement (NL | SEMICOLON)+)* statement?;

statement
    : assignment
    | declaration
    | expression
    ;

declaration
    : varDeclaration
    | structDeclaration
    | functionDeclaration
    | externDeclaration
    ;

varDeclaration
    : CONST? NL* VAR NL* identifier (COLON NL* type)? NL* (assignmentOp NL* expression)?
    ;

assignment
    : identifier NL* assignmentOp NL* expression
    ;

assignmentOp
    : ASSIGN | PLUS_ASSIGN | MINUS_ASSIGN | MULT_ASSIGN | DIV_ASSIGN | MOD_ASSIGN
    | AND_ASSIGN | OR_ASSIGN | XOR_ASSIGN
    ;

functionDeclaration
    : accessModifier? NL* INST? NL* FUN NL* identifier NL* argList NL* (COLON NL* type)? NL* block
    ;

argList
    : LPAREN NL* (arg (NL* COMMA NL* arg)*)? NL* RPAREN
    ;

arg
    : Identifier COLON NL* type
    ;

structDeclaration
    : STRUCT NL* identifier NL* LBRACE NL* (varDeclaration NL*)* (constructorDeclaration NL*)* RBRACE
    ;

constructorDeclaration
    : accessModifier? NL* INIT NL* argList NL* block
    ;

externDeclaration
    : EXTERN NL* identifier NL* noTypeArgList NL* (COLON NL* DYN)? (IS identifier)?
    ;

noTypeArgList
    : LPAREN NL* (Identifier (NL* COMMA NL* Identifier)*)? NL* RPAREN
    ;

expression: or;

or
    : and (NL* OR NL* and)*
    ;

and
    : bitOr (NL* AND NL* bitOr)*
    ;

bitOr
    : bitXor (NL* PIPE NL* bitXor)*
    ;

bitXor
    : bitAnd (NL* CARET NL* bitAnd)*
    ;

bitAnd
    : equality (NL* AMP NL* equality)*
    ;

equality
    : comparison ((EQUAL | NOT_EQUAL) NL* comparison)*
    ;

comparison
    : bitShift ((LT | GT | LT_EQUAL | GT_EQUAL) NL* bitShift)*
    ;

bitShift
    : addition ((SHL | SHR) NL* addition)*
    ;

addition
    : multiplication ((PLUS | MINUS) NL* multiplication)*
    ;

multiplication
    : prefix ((STAR | SLASH | MOD) NL* prefix)*
    ;

prefix
    : (NOT | TILDE | MINUS | PLUS | INC | DEC)* postfix
    ;

postfix
    : access (INC | DEC)*
    ;

access
    : primary (DOT NL* primary)*
    ;

primary
    : LPAREN NL* expression NL* RPAREN
    | call
    | Number
    | String
    | Boolean
    | Null
    | identifier
    ;

call
    : identifier NL* LPAREN NL* (expression (NL* COMMA NL* expression)*)? NL* RPAREN
    ;

block
    : LBRACE statements RBRACE
    ;

type
    : (Identifier QUESTION?) | DYN
    ;

// identifier and soft keywords
identifier
    : Identifier
    | CONST
    | INST
    | EXTERN
    | INIT
    ;

accessModifier
    : PRIVATE | PACKAGE
    ;