package com.gnuplot.core.parser;

import com.gnuplot.core.ast.ASTBuilder;
import com.gnuplot.core.ast.ASTNode;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ConsoleErrorListener;
import org.antlr.v4.runtime.RecognitionException;

/**
 * High-level API for parsing mathematical expressions.
 *
 * <p>This class provides a simple interface for parsing gnuplot-style mathematical
 * expressions into Abstract Syntax Trees (ASTs). It handles all ANTLR setup,
 * error handling, and provides clean error messages.
 *
 * <h2>Usage Example:</h2>
 * <pre>{@code
 * ExpressionParser parser = new ExpressionParser();
 * ParseResult result = parser.parse("2 + 3 * sin(x)");
 *
 * if (result.isSuccess()) {
 *     ASTNode ast = result.getAst();
 *     // Use the AST...
 * } else {
 *     System.err.println("Parse error: " + result.getError());
 * }
 * }</pre>
 *
 * <p>The parser supports:
 * <ul>
 *   <li>Arithmetic operators: +, -, *, /, %, **</li>
 *   <li>Comparison operators: &lt;, &lt;=, &gt;, &gt;=, ==, !=</li>
 *   <li>Logical operators: &amp;&amp;, ||, !</li>
 *   <li>Bitwise operators: &amp;, |, ^, ~</li>
 *   <li>Function calls: sin(x), atan2(y, x), etc.</li>
 *   <li>Variables and numeric literals</li>
 *   <li>Ternary conditional: condition ? trueValue : falseValue</li>
 * </ul>
 *
 * @see ParseResult
 * @see ASTNode
 */
public class ExpressionParser {

    /**
     * Parses a mathematical expression into an AST.
     *
     * <p>This method attempts to parse the given expression string and returns
     * a {@link ParseResult} containing either the resulting AST or error information.
     *
     * @param expression the expression to parse (e.g., "2 + 3 * 4")
     * @return a ParseResult containing the AST on success or error details on failure
     * @throws IllegalArgumentException if expression is null or empty
     */
    public ParseResult parse(String expression) {
        if (expression == null || expression.trim().isEmpty()) {
            throw new IllegalArgumentException("Expression cannot be null or empty");
        }

        try {
            // Create lexer
            GnuplotExpressionLexer lexer = new GnuplotExpressionLexer(
                    CharStreams.fromString(expression)
            );

            // Remove default error listeners and add custom one
            lexer.removeErrorListener(ConsoleErrorListener.INSTANCE);
            ParserErrorListener errorListener = new ParserErrorListener();
            lexer.addErrorListener(errorListener);

            // Create parser
            CommonTokenStream tokens = new CommonTokenStream(lexer);
            GnuplotExpressionParser parser = new GnuplotExpressionParser(tokens);

            // Remove default error listeners and add custom one
            parser.removeErrorListener(ConsoleErrorListener.INSTANCE);
            parser.addErrorListener(errorListener);

            // Parse
            GnuplotExpressionParser.CompilationUnitContext tree = parser.compilationUnit();

            // Check for errors
            if (errorListener.hasErrors()) {
                return ParseResult.failure(errorListener.getErrorMessage());
            }

            // Build AST
            ASTBuilder astBuilder = new ASTBuilder();
            ASTNode ast = astBuilder.visit(tree);

            return ParseResult.success(ast);

        } catch (RecognitionException e) {
            return ParseResult.failure(
                    String.format("Parse error at line %d, column %d: %s",
                            e.getOffendingToken().getLine(),
                            e.getOffendingToken().getCharPositionInLine(),
                            e.getMessage())
            );
        } catch (Exception e) {
            return ParseResult.failure("Unexpected error during parsing: " + e.getMessage());
        }
    }

    /**
     * Convenience method that parses an expression and returns the AST directly.
     *
     * <p>This method throws an exception if parsing fails, making it suitable
     * for cases where you want to fail fast on parse errors.
     *
     * @param expression the expression to parse
     * @return the parsed AST
     * @throws ParseException if the expression cannot be parsed
     * @throws IllegalArgumentException if expression is null or empty
     */
    public ASTNode parseOrThrow(String expression) {
        ParseResult result = parse(expression);
        if (!result.isSuccess()) {
            throw new ParseException(result.getError());
        }
        return result.getAst();
    }
}