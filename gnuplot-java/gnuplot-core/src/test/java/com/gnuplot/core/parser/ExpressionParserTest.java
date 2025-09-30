package com.gnuplot.core.parser;

import com.gnuplot.core.ast.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests for the high-level Expression Parser API.
 */
@DisplayName("ExpressionParser")
class ExpressionParserTest {

    private ExpressionParser parser;

    @BeforeEach
    void setUp() {
        parser = new ExpressionParser();
    }

    @Test
    @DisplayName("should successfully parse simple number")
    void shouldParseSimpleNumber() {
        // When
        ParseResult result = parser.parse("42");

        // Then
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getAst()).isInstanceOf(NumberLiteral.class);
        NumberLiteral number = (NumberLiteral) result.getAst();
        assertThat(number.value()).isEqualTo(42.0);
    }

    @Test
    @DisplayName("should successfully parse variable")
    void shouldParseVariable() {
        // When
        ParseResult result = parser.parse("x");

        // Then
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getAst()).isInstanceOf(Variable.class);
        Variable var = (Variable) result.getAst();
        assertThat(var.name()).isEqualTo("x");
    }

    @Test
    @DisplayName("should successfully parse simple arithmetic")
    void shouldParseSimpleArithmetic() {
        // When
        ParseResult result = parser.parse("2 + 3");

        // Then
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getAst()).isInstanceOf(BinaryOperation.class);
        BinaryOperation binOp = (BinaryOperation) result.getAst();
        assertThat(binOp.operator()).isEqualTo(BinaryOperation.Operator.ADD);
    }

    @Test
    @DisplayName("should successfully parse complex expression")
    void shouldParseComplexExpression() {
        // When
        ParseResult result = parser.parse("sin(x) * 2 + cos(y) ** 2");

        // Then
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getAst()).isInstanceOf(BinaryOperation.class);
    }

    @Test
    @DisplayName("should successfully parse function call")
    void shouldParseFunctionCall() {
        // When
        ParseResult result = parser.parse("sin(3.14159)");

        // Then
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getAst()).isInstanceOf(FunctionCall.class);
        FunctionCall func = (FunctionCall) result.getAst();
        assertThat(func.functionName()).isEqualTo("sin");
        assertThat(func.arguments()).hasSize(1);
    }

    @Test
    @DisplayName("should successfully parse ternary conditional")
    void shouldParseTernaryConditional() {
        // When
        ParseResult result = parser.parse("x > 0 ? 1 : -1");

        // Then
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getAst()).isInstanceOf(TernaryConditional.class);
    }

    @Test
    @DisplayName("should fail on invalid syntax")
    void shouldFailOnInvalidSyntax() {
        // When - using truly invalid syntax (missing operand)
        ParseResult result = parser.parse("2 + * 3");

        // Then
        assertThat(result.isFailure()).isTrue();
        assertThat(result.getError()).contains("Syntax error");
    }

    @Test
    @DisplayName("should fail on unmatched parenthesis")
    void shouldFailOnUnmatchedParenthesis() {
        // When
        ParseResult result = parser.parse("(2 + 3");

        // Then
        assertThat(result.isFailure()).isTrue();
        assertThat(result.getError()).contains("Syntax error");
    }

    @Test
    @DisplayName("should fail on empty expression")
    void shouldFailOnEmptyExpression() {
        // When/Then
        assertThatThrownBy(() -> parser.parse(""))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("cannot be null or empty");
    }

    @Test
    @DisplayName("should fail on null expression")
    void shouldFailOnNullExpression() {
        // When/Then
        assertThatThrownBy(() -> parser.parse(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("cannot be null or empty");
    }

    @Test
    @DisplayName("should fail on whitespace-only expression")
    void shouldFailOnWhitespaceOnlyExpression() {
        // When/Then
        assertThatThrownBy(() -> parser.parse("   "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("cannot be null or empty");
    }

    @Test
    @DisplayName("parseOrThrow should return AST on success")
    void parseOrThrowShouldReturnAstOnSuccess() {
        // When
        ASTNode ast = parser.parseOrThrow("2 + 3");

        // Then
        assertThat(ast).isInstanceOf(BinaryOperation.class);
    }

    @Test
    @DisplayName("parseOrThrow should throw ParseException on failure")
    void parseOrThrowShouldThrowOnFailure() {
        // When/Then
        assertThatThrownBy(() -> parser.parseOrThrow("2 + * 3"))
                .isInstanceOf(ParseException.class)
                .hasMessageContaining("Syntax error");
    }

    @Test
    @DisplayName("should provide line and column information in errors")
    void shouldProvideLineAndColumnInErrors() {
        // When
        ParseResult result = parser.parse("2 + @");

        // Then
        assertThat(result.isFailure()).isTrue();
        assertThat(result.getError())
                .contains("line")
                .contains("column");
    }

    @Test
    @DisplayName("should handle expressions with whitespace")
    void shouldHandleExpressionsWithWhitespace() {
        // When
        ParseResult result = parser.parse("  2  +  3  ");

        // Then
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getAst()).isInstanceOf(BinaryOperation.class);
    }

    @Test
    @DisplayName("should handle expressions with newlines")
    void shouldHandleExpressionsWithNewlines() {
        // When
        ParseResult result = parser.parse("2 +\n3");

        // Then
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getAst()).isInstanceOf(BinaryOperation.class);
    }

    @Test
    @DisplayName("should parse all test oracle expressions successfully")
    void shouldParseAllTestOracleExpressions() {
        // Test a representative sample from test oracle
        String[] expressions = {
                "2 + 3",
                "10 - 4",
                "5 * 6",
                "20 / 4",
                "17 % 5",
                "2 ** 8",
                "sin(0)",
                "cos(0)",
                "exp(1)",
                "log(2.718281828)",
                "sqrt(16)",
                "abs(-5)",
                "sin(pi/4) ** 2 + cos(pi/4) ** 2"
        };

        for (String expr : expressions) {
            ParseResult result = parser.parse(expr);
            assertThat(result.isSuccess())
                    .as("Expression '%s' should parse successfully", expr)
                    .isTrue();
        }
    }

    @Test
    @DisplayName("ParseResult should not allow accessing AST on failure")
    void parseResultShouldNotAllowAccessingAstOnFailure() {
        // Given
        ParseResult result = parser.parse("2 + * 3");

        // When/Then
        assertThat(result.isFailure()).isTrue();
        assertThatThrownBy(result::getAst)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Cannot get AST from failed parse");
    }

    @Test
    @DisplayName("ParseResult should not allow accessing error on success")
    void parseResultShouldNotAllowAccessingErrorOnSuccess() {
        // Given
        ParseResult result = parser.parse("2 + 3");

        // When/Then
        assertThat(result.isSuccess()).isTrue();
        assertThatThrownBy(result::getError)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Cannot get error from successful parse");
    }
}