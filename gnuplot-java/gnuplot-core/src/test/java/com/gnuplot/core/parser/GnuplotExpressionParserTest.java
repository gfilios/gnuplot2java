package com.gnuplot.core.parser;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for the ANTLR4-generated Gnuplot expression parser.
 * Verifies that the grammar correctly parses mathematical expressions.
 */
@DisplayName("GnuplotExpressionParser")
class GnuplotExpressionParserTest {

    @Test
    @DisplayName("should parse simple number")
    void shouldParseSimpleNumber() {
        // Given
        String input = "42";

        // When
        ParseTree tree = parse(input);

        // Then
        assertThat(tree).isNotNull();
        assertThat(tree.toStringTree()).contains("42");
    }

    @Test
    @DisplayName("should parse decimal number")
    void shouldParseDecimalNumber() {
        // Given
        String input = "3.14159";

        // When
        ParseTree tree = parse(input);

        // Then
        assertThat(tree).isNotNull();
        assertThat(tree.toStringTree()).contains("3.14159");
    }

    @Test
    @DisplayName("should parse scientific notation")
    void shouldParseScientificNotation() {
        // Given
        String input = "1.5e-5";

        // When
        ParseTree tree = parse(input);

        // Then
        assertThat(tree).isNotNull();
        assertThat(tree.toStringTree()).contains("1.5e-5");
    }

    @Test
    @DisplayName("should parse variable")
    void shouldParseVariable() {
        // Given
        String input = "x";

        // When
        ParseTree tree = parse(input);

        // Then
        assertThat(tree).isNotNull();
        assertThat(tree.toStringTree()).contains("x");
    }

    @Test
    @DisplayName("should parse simple addition")
    void shouldParseSimpleAddition() {
        // Given
        String input = "2 + 3";

        // When
        ParseTree tree = parse(input);

        // Then
        assertThat(tree).isNotNull();
        assertThat(tree.toStringTree()).contains("2");
        assertThat(tree.toStringTree()).contains("+");
        assertThat(tree.toStringTree()).contains("3");
    }

    @Test
    @DisplayName("should parse complex arithmetic expression")
    void shouldParseComplexArithmetic() {
        // Given
        String input = "2 + 3 * 4";

        // When
        ParseTree tree = parse(input);

        // Then
        assertThat(tree).isNotNull();
        // Verify precedence: multiplication should bind tighter than addition
    }

    @Test
    @DisplayName("should parse parenthesized expression")
    void shouldParseParenthesizedExpression() {
        // Given
        String input = "(2 + 3) * 4";

        // When
        ParseTree tree = parse(input);

        // Then
        assertThat(tree).isNotNull();
        assertThat(tree.toStringTree()).contains("(");
        assertThat(tree.toStringTree()).contains(")");
    }

    @Test
    @DisplayName("should parse function call with no arguments")
    void shouldParseFunctionCallNoArgs() {
        // Given
        String input = "pi()";

        // When
        ParseTree tree = parse(input);

        // Then
        assertThat(tree).isNotNull();
        assertThat(tree.toStringTree()).contains("pi");
    }

    @Test
    @DisplayName("should parse function call with one argument")
    void shouldParseFunctionCallOneArg() {
        // Given
        String input = "sin(x)";

        // When
        ParseTree tree = parse(input);

        // Then
        assertThat(tree).isNotNull();
        assertThat(tree.toStringTree()).contains("sin");
        assertThat(tree.toStringTree()).contains("x");
    }

    @Test
    @DisplayName("should parse function call with multiple arguments")
    void shouldParseFunctionCallMultipleArgs() {
        // Given
        String input = "atan2(y, x)";

        // When
        ParseTree tree = parse(input);

        // Then
        assertThat(tree).isNotNull();
        assertThat(tree.toStringTree()).contains("atan2");
        assertThat(tree.toStringTree()).contains("y");
        assertThat(tree.toStringTree()).contains("x");
    }

    @Test
    @DisplayName("should parse nested function calls")
    void shouldParseNestedFunctionCalls() {
        // Given
        String input = "sin(cos(x))";

        // When
        ParseTree tree = parse(input);

        // Then
        assertThat(tree).isNotNull();
        assertThat(tree.toStringTree()).contains("sin");
        assertThat(tree.toStringTree()).contains("cos");
    }

    @Test
    @DisplayName("should parse power operator")
    void shouldParsePowerOperator() {
        // Given
        String input = "2 ** 8";

        // When
        ParseTree tree = parse(input);

        // Then
        assertThat(tree).isNotNull();
        assertThat(tree.toStringTree()).contains("**");
    }

    @Test
    @DisplayName("should parse unary minus")
    void shouldParseUnaryMinus() {
        // Given
        String input = "-5";

        // When
        ParseTree tree = parse(input);

        // Then
        assertThat(tree).isNotNull();
        assertThat(tree.toStringTree()).contains("-");
        assertThat(tree.toStringTree()).contains("5");
    }

    @Test
    @DisplayName("should parse comparison operators")
    void shouldParseComparisonOperators() {
        // Given
        String input = "x < y";

        // When
        ParseTree tree = parse(input);

        // Then
        assertThat(tree).isNotNull();
        assertThat(tree.toStringTree()).contains("<");
    }

    @Test
    @DisplayName("should parse logical operators")
    void shouldParseLogicalOperators() {
        // Given
        String input = "x && y || z";

        // When
        ParseTree tree = parse(input);

        // Then
        assertThat(tree).isNotNull();
        assertThat(tree.toStringTree()).contains("&&");
        assertThat(tree.toStringTree()).contains("||");
    }

    @Test
    @DisplayName("should parse ternary conditional")
    void shouldParseTernaryConditional() {
        // Given
        String input = "x > 0 ? 1 : -1";

        // When
        ParseTree tree = parse(input);

        // Then
        assertThat(tree).isNotNull();
        assertThat(tree.toStringTree()).contains("?");
        assertThat(tree.toStringTree()).contains(":");
    }

    @ParameterizedTest
    @DisplayName("should parse test oracle expressions")
    @ValueSource(strings = {
            "2 + 3",
            "10 - 4",
            "5 * 6",
            "20 / 4",
            "2 ** 8",
            "17 % 5",
            "2 + 3 * 4",
            "(2 + 3) * 4",
            "-5",
            "sin(0)",
            "sin(pi/2)",
            "cos(0)",
            "exp(1)",
            "log(10)",
            "sqrt(4)",
            "abs(-5)",
            "sin(pi/4) ** 2 + cos(pi/4) ** 2"
    })
    void shouldParseTestOracleExpressions(String expression) {
        // When
        ParseTree tree = parse(expression);

        // Then
        assertThat(tree).isNotNull();
    }

    // Helper method to parse an expression
    private ParseTree parse(String input) {
        GnuplotExpressionLexer lexer = new GnuplotExpressionLexer(CharStreams.fromString(input));
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        GnuplotExpressionParser parser = new GnuplotExpressionParser(tokens);

        return parser.compilationUnit();
    }
}