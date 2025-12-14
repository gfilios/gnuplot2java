grammar GnuplotExpression;

/*
 * Gnuplot Expression Grammar
 *
 * This grammar defines the syntax for mathematical expressions in gnuplot.
 * Based on the C implementation in scanner.c and parse.c, but modernized
 * with ANTLR4's capabilities.
 *
 * Supports:
 * - Basic arithmetic operators: +, -, *, /, %, **
 * - Comparison operators: <, <=, >, >=, ==, !=
 * - Logical operators: &&, ||, !
 * - Bitwise operators: &, |, ^, ~
 * - Function calls: sin(x), log(y), etc.
 * - Variables and constants
 * - Parentheses for grouping
 * - Ternary conditional: condition ? expr1 : expr2
 * - Assignment expression: x = value (returns assigned value)
 * - Comma operator: a, b (evaluates both, returns right)
 */

// ============================================================================
// Parser Rules
// ============================================================================

// Entry point for parsing a complete expression
compilationUnit
    : expression EOF
    ;

// Expression hierarchy (lowest to highest precedence)
expression
    : commaExpression
    ;

// Comma operator: a, b (evaluates both, returns right) - lowest precedence
commaExpression
    : assignmentExpression (COMMA assignmentExpression)*  # CommaExpr
    ;

// Assignment: x = value (returns assigned value)
// Only simple identifiers can be assigned to
assignmentExpression
    : IDENTIFIER ASSIGN assignmentExpression              # AssignExpr
    | ternaryExpression                                   # AssignTernary
    ;

// Ternary conditional: condition ? trueExpr : falseExpr
ternaryExpression
    : logicalOrExpression (QUESTION ternaryExpression COLON ternaryExpression)?
    ;

// Logical OR: ||
logicalOrExpression
    : logicalAndExpression (OR logicalAndExpression)*
    ;

// Logical AND: &&
logicalAndExpression
    : bitwiseOrExpression (AND bitwiseOrExpression)*
    ;

// Bitwise OR: |
bitwiseOrExpression
    : bitwiseXorExpression (BITOR bitwiseXorExpression)*
    ;

// Bitwise XOR: ^
bitwiseXorExpression
    : bitwiseAndExpression (BITXOR bitwiseAndExpression)*
    ;

// Bitwise AND: &
bitwiseAndExpression
    : equalityExpression (BITAND equalityExpression)*
    ;

// Equality: ==, !=
equalityExpression
    : relationalExpression ((EQ | NE) relationalExpression)*
    ;

// Relational: <, <=, >, >=
relationalExpression
    : additiveExpression ((LT | LE | GT | GE) additiveExpression)*
    ;

// Addition and subtraction: +, -
additiveExpression
    : multiplicativeExpression ((PLUS | MINUS) multiplicativeExpression)*
    ;

// Multiplication, division, modulo: *, /, %
multiplicativeExpression
    : powerExpression ((STAR | SLASH | PERCENT) powerExpression)*
    ;

// Power/exponentiation: **
powerExpression
    : unaryExpression (POW unaryExpression)*
    ;

// Unary operators: -, +, !, ~
unaryExpression
    : MINUS unaryExpression           # UnaryMinus
    | PLUS unaryExpression            # UnaryPlus
    | NOT unaryExpression             # LogicalNot
    | BITNOT unaryExpression          # BitwiseNot
    | postfixExpression               # UnaryPostfix
    ;

// Postfix expressions (function calls, array access)
postfixExpression
    : primaryExpression                                         # Primary
    | IDENTIFIER LPAREN argumentList? RPAREN                   # FunctionCall
    ;

// Primary expressions (literals, variables, parenthesized)
primaryExpression
    : NUMBER                                                    # NumberLiteral
    | IDENTIFIER                                                # Variable
    | LPAREN expression RPAREN                                  # ParenthesizedExpr
    ;

// Function argument list
// Use assignmentExpression, not expression, so commas separate arguments
// (not parsed as comma operator)
argumentList
    : assignmentExpression (COMMA assignmentExpression)*
    ;

// ============================================================================
// Lexer Rules
// ============================================================================

// Operators (in order of complexity)

// Arithmetic operators
PLUS        : '+' ;
MINUS       : '-' ;
STAR        : '*' ;
SLASH       : '/' ;
PERCENT     : '%' ;
POW         : '**' ;

// Assignment operator (must be before comparison to avoid conflicts)
ASSIGN      : '=' ;

// Comparison operators
LT          : '<' ;
LE          : '<=' ;
GT          : '>' ;
GE          : '>=' ;
EQ          : '==' ;
NE          : '!=' ;

// Logical operators
AND         : '&&' ;
OR          : '||' ;
NOT         : '!' ;

// Bitwise operators
BITAND      : '&' ;
BITOR       : '|' ;
BITXOR      : '^' ;
BITNOT      : '~' ;

// Ternary
QUESTION    : '?' ;
COLON       : ':' ;

// Delimiters
LPAREN      : '(' ;
RPAREN      : ')' ;
COMMA       : ',' ;

// Identifiers (variable names and function names)
// Must start with letter or underscore, followed by alphanumeric or underscore
IDENTIFIER
    : [a-zA-Z_] [a-zA-Z0-9_]*
    ;

// Numbers
// Supports: integers, decimals, scientific notation
NUMBER
    : DIGIT+ ('.' DIGIT*)? EXPONENT?
    | '.' DIGIT+ EXPONENT?
    ;

fragment
DIGIT
    : [0-9]
    ;

fragment
EXPONENT
    : [eE] [+-]? DIGIT+
    ;

// Whitespace (skip)
WS
    : [ \t\r\n]+ -> skip
    ;

// Comments (skip)
LINE_COMMENT
    : '#' ~[\r\n]* -> skip
    ;