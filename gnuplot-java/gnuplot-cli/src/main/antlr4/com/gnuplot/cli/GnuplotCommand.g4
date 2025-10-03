grammar GnuplotCommand;

// Parser Rules

script
    : (statement)* EOF
    ;

statement
    : command NEWLINE
    | comment
    | NEWLINE
    ;

comment
    : COMMENT
    ;

command
    : setCommand
    | unsetCommand
    | plotCommand
    | splotCommand
    | replotCommand
    | pauseCommand
    | resetCommand
    | clearCommand
    | exitCommand
    | quitCommand
    | assignmentCommand
    | functionDefinition
    | showCommand
    ;

// SET commands
setCommand
    : SET setOption
    ;

setOption
    : TITLE string (FONT string)?                   # SetTitle
    | XLABEL string (FONT string)?                  # SetXLabel
    | YLABEL string (FONT string)?                  # SetYLabel
    | ZLABEL string (FONT string)?                  # SetZLabel
    | KEY keyPosition keyOptions*                   # SetKey
    | SAMPLES NUMBER                                # SetSamples
    | XRANGE range                                  # SetXRange
    | YRANGE range                                  # SetYRange
    | ZRANGE range                                  # SetZRange
    | GRID                                          # SetGrid
    | AUTOSCALE                                     # SetAutoscale
    | LOGSCALE axes                                 # SetLogscale
    | TERMINAL terminalType terminalOptions*        # SetTerminal
    | OUTPUT string                                 # SetOutput
    | STYLE styleType styleOptions*                 # SetStyle
    | PARAMETRIC                                    # SetParametric
    | VIEW NUMBER COMMA NUMBER                      # SetView
    | TICSLEVEL NUMBER                              # SetTicsLevel
    | HIDDEN3D                                      # SetHidden3D
    | DGRID3D dgridOptions                          # SetDgrid3D
    ;

keyPosition
    : LEFT | RIGHT | TOP | BOTTOM | CENTER
    | BMARGIN (LEFT | RIGHT | CENTER)?
    | TMARGIN (LEFT | RIGHT | CENTER)?
    | LMARGIN (TOP | BOTTOM | CENTER)?
    | RMARGIN (TOP | BOTTOM | CENTER)?
    ;

keyOptions
    : BOX | NOBOX
    | HORIZONTAL | VERTICAL
    ;

range
    : LBRACKET rangeSpec RBRACKET
    ;

rangeSpec
    : expression? COLON expression?
    | STAR COLON expression
    | expression COLON STAR
    | STAR COLON STAR
    ;

axes
    : IDENTIFIER (COMMA IDENTIFIER)*
    ;

terminalType
    : IDENTIFIER
    ;

terminalOptions
    : SIZE NUMBER COMMA NUMBER          # TerminalSize
    | IDENTIFIER                        # TerminalOption
    | NUMBER                            # TerminalNumber
    | string                            # TerminalString
    ;

styleType
    : DATA | LINE | ARROW | FILL
    ;

styleOptions
    : POINTS | LINES | LINESPOINTS | IMPULSES | DOTS
    | IDENTIFIER
    | NUMBER
    ;

dgridOptions
    : NUMBER COMMA NUMBER (IDENTIFIER NUMBER?)?
    ;

// UNSET commands
unsetCommand
    : UNSET unsetOption
    ;

unsetOption
    : GRID
    | KEY
    | LOGSCALE
    | PARAMETRIC
    | HIDDEN3D
    | IDENTIFIER
    ;

// PLOT command
plotCommand
    : PLOT range? range? plotSpec (COMMA plotSpec)*
    ;

plotSpec
    : range? (expression | dataSource) plotModifiers*
    ;

dataSource
    : string                                        # FileData
    | MINUS                                         # StdinData
    ;

plotModifiers
    : WITH plotStyle
    | TITLE string
    | NOTITLE
    | AXES IDENTIFIER
    | LINESTYLE NUMBER
    | LINETYPE NUMBER
    | LINEWIDTH NUMBER
    | LINECOLOR colorSpec
    | POINTTYPE NUMBER
    | POINTSIZE NUMBER
    | USING usingSpec
    ;

plotStyle
    : LINES
    | POINTS
    | LINESPOINTS
    | IMPULSES
    | DOTS
    | STEPS
    | BOXES
    | IDENTIFIER
    ;

colorSpec
    : IDENTIFIER
    | RGB string
    | NUMBER
    ;

usingSpec
    : columnSpec (COLON columnSpec)*
    ;

columnSpec
    : NUMBER
    | expression
    ;

// SPLOT command (3D plot)
splotCommand
    : SPLOT plotSpec (COMMA plotSpec)*
    ;

// REPLOT command
replotCommand
    : REPLOT plotSpec*
    ;

// PAUSE command
pauseCommand
    : PAUSE (NUMBER | MINUS NUMBER) string?
    ;

// RESET command
resetCommand
    : RESET
    ;

// CLEAR command
clearCommand
    : CLEAR
    ;

// EXIT/QUIT commands
exitCommand
    : EXIT
    ;

quitCommand
    : QUIT
    ;

// SHOW command
showCommand
    : SHOW IDENTIFIER
    ;

// Assignment
assignmentCommand
    : IDENTIFIER EQUALS expression
    ;

// Function definition
functionDefinition
    : IDENTIFIER LPAREN parameterList? RPAREN EQUALS expression
    ;

parameterList
    : IDENTIFIER (COMMA IDENTIFIER)*
    ;

// Expressions (simplified - reuse from GnuplotExpression grammar)
expression
    : expression (STAR | SLASH | PERCENT) expression     # MulDivMod
    | expression (PLUS | MINUS) expression               # AddSub
    | expression (LT | LE | GT | GE | EQ | NE) expression # Comparison
    | expression (AND | OR) expression                   # LogicalOp
    | MINUS expression                                   # UnaryMinus
    | PLUS expression                                    # UnaryPlus
    | NOT expression                                     # LogicalNot
    | functionCall                                       # FuncCall
    | IDENTIFIER                                         # Variable
    | NUMBER                                             # Number
    | string                                             # StringLiteral
    | LPAREN expression RPAREN                           # Parens
    | expression QUESTION expression COLON expression    # TernaryOp
    | expression POWER expression                        # PowerOp
    ;

functionCall
    : IDENTIFIER LPAREN (expression (COMMA expression)*)? RPAREN
    ;

string
    : IDENTIFIER
    | QUOTED_STRING
    ;

// Lexer Rules

// Keywords
SET         : 'set' ;
UNSET       : 'unset' ;
PLOT        : 'plot' ;
SPLOT       : 'splot' ;
REPLOT      : 'replot' ;
PAUSE       : 'pause' ;
RESET       : 'reset' ;
CLEAR       : 'clear' ;
EXIT        : 'exit' ;
QUIT        : 'quit' ;
SHOW        : 'show' ;

// Set options
TITLE       : 'title' ;
XLABEL      : 'xlabel' ;
YLABEL      : 'ylabel' ;
ZLABEL      : 'zlabel' ;
KEY         : 'key' ;
FONT        : 'font' ;
SAMPLES     : 'samples' ;
XRANGE      : 'xrange' ;
YRANGE      : 'yrange' ;
ZRANGE      : 'zrange' ;
GRID        : 'grid' ;
AUTOSCALE   : 'autoscale' ;
LOGSCALE    : 'logscale' ;
TERMINAL    : 'terminal' | 'term' ;
OUTPUT      : 'output' ;
STYLE       : 'style' ;
SIZE        : 'size' ;
PARAMETRIC  : 'parametric' ;
VIEW        : 'view' ;
TICSLEVEL   : 'ticslevel' ;
HIDDEN3D    : 'hidden3d' ;
DGRID3D     : 'dgrid3d' ;

// Position keywords
LEFT        : 'left' ;
RIGHT       : 'right' ;
TOP         : 'top' ;
BOTTOM      : 'bottom' ;
CENTER      : 'center' ;
BMARGIN     : 'bmargin' ;
TMARGIN     : 'tmargin' ;
LMARGIN     : 'lmargin' ;
RMARGIN     : 'rmargin' ;

// Key options
BOX         : 'box' ;
NOBOX       : 'nobox' ;
HORIZONTAL  : 'horizontal' ;
VERTICAL    : 'vertical' ;

// Plot modifiers
WITH        : 'with' ;
NOTITLE     : 'notitle' ;
AXES        : 'axes' ;
LINESTYLE   : 'linestyle' | 'ls' ;
LINETYPE    : 'linetype' | 'lt' ;
LINEWIDTH   : 'linewidth' | 'lw' ;
LINECOLOR   : 'linecolor' | 'lc' ;
POINTTYPE   : 'pointtype' | 'pt' ;
POINTSIZE   : 'pointsize' | 'ps' ;
USING       : 'using' | 'u' ;
RGB         : 'rgb' ;

// Plot styles
LINES       : 'lines' ;
POINTS      : 'points' ;
LINESPOINTS : 'linespoints' ;
IMPULSES    : 'impulses' ;
DOTS        : 'dots' ;
STEPS       : 'steps' ;
BOXES       : 'boxes' ;

// Style types
DATA        : 'data' ;
LINE        : 'line' ;
ARROW       : 'arrow' ;
FILL        : 'fill' ;

// Operators
PLUS        : '+' ;
MINUS       : '-' ;
STAR        : '*' ;
SLASH       : '/' ;
PERCENT     : '%' ;
POWER       : '**' ;
EQUALS      : '=' ;
LT          : '<' ;
LE          : '<=' ;
GT          : '>' ;
GE          : '>=' ;
EQ          : '==' ;
NE          : '!=' ;
AND         : '&&' ;
OR          : '||' ;
NOT         : '!' ;
QUESTION    : '?' ;

// Punctuation
LPAREN      : '(' ;
RPAREN      : ')' ;
LBRACKET    : '[' ;
RBRACKET    : ']' ;
COMMA       : ',' ;
COLON       : ':' ;

// Literals
NUMBER
    : [0-9]+ ('.' [0-9]+)? ([eE] [+-]? [0-9]+)?
    | '.' [0-9]+ ([eE] [+-]? [0-9]+)?
    ;

QUOTED_STRING
    : '"' (~["\r\n] | '\\' .)* '"'
    | '\'' (~['\r\n] | '\\' .)* '\''
    ;

IDENTIFIER
    : [a-zA-Z_] [a-zA-Z0-9_]*
    ;

// Comments
COMMENT
    : '#' ~[\r\n]* -> skip
    ;

// Whitespace
WS
    : [ \t\r]+ -> skip
    ;

NEWLINE
    : '\n'
    ;
