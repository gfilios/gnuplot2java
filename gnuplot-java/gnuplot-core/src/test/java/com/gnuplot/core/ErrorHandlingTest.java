package com.gnuplot.core;

import com.gnuplot.core.ast.SourceLocation;
import com.gnuplot.core.evaluator.EvaluationContext;
import com.gnuplot.core.evaluator.EvaluationException;
import com.gnuplot.core.evaluator.Evaluator;
import com.gnuplot.core.parser.ExpressionParser;
import com.gnuplot.core.parser.ParseException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * Test suite for error handling and error messages.
 *
 * <p>Tests the exception hierarchy, error messages, and suggestions
 * for various error scenarios.
 */
class ErrorHandlingTest {

    private ExpressionParser parser;
    private Evaluator evaluator;
    private EvaluationContext context;

    @BeforeEach
    void setUp() {
        parser = new ExpressionParser();
        context = new EvaluationContext();
        evaluator = new Evaluator(context);
    }

    @Test
    void testGnuplotExceptionHierarchy() {
        // Verify exception hierarchy
        ParseException parseEx = new ParseException("test");
        assertThat(parseEx).isInstanceOf(GnuplotException.class);

        EvaluationException evalEx = new EvaluationException("test");
        assertThat(evalEx).isInstanceOf(GnuplotException.class);
    }

    @Test
    void testParseExceptionWithoutContext() {
        assertThatThrownBy(() -> parser.parseOrThrow("2 + * 3"))
                .isInstanceOf(ParseException.class)
                .hasMessageContaining("Syntax error");
    }

    @Test
    void testParseExceptionMismatchedParentheses() {
        assertThatThrownBy(() -> parser.parseOrThrow("(2 + 3"))
                .isInstanceOf(ParseException.class)
                .hasMessageContaining("missing ')'");
    }

    @Test
    void testEvaluationExceptionUndefinedVariable() {
        assertThatThrownBy(() -> {
            var ast = parser.parseOrThrow("x + 1");
            ast.accept(evaluator);
        })
                .isInstanceOf(EvaluationException.class)
                .hasMessageContaining("Undefined variable");
    }

    @Test
    void testEvaluationExceptionUndefinedFunction() {
        assertThatThrownBy(() -> {
            var ast = parser.parseOrThrow("unknownfunc(1)");
            ast.accept(evaluator);
        })
                .isInstanceOf(EvaluationException.class)
                .hasMessageContaining("Undefined function");
    }

    @Test
    void testEvaluationExceptionDivisionByZero() {
        assertThatThrownBy(() -> {
            var ast = parser.parseOrThrow("1 / 0");
            ast.accept(evaluator);
        })
                .isInstanceOf(EvaluationException.class)
                .hasMessageContaining("Division by zero");
    }

    @Test
    void testGnuplotExceptionWithLocation() {
        SourceLocation location = new SourceLocation(1, 5, 5, 5);
        String expression = "2 + x + 3";
        GnuplotException ex = new GnuplotException("Test error", null, location, expression, null);

        assertThat(ex.getMessage())
                .contains("Test error")
                .contains("line 1")
                .contains("column 5")
                .contains("Expression: 2 + x + 3");

        assertThat(ex.getLocation()).isPresent();
        assertThat(ex.getLocation().get()).isEqualTo(location);
        assertThat(ex.getExpression()).isPresent();
        assertThat(ex.getExpression().get()).isEqualTo(expression);
    }

    @Test
    void testGnuplotExceptionWithSuggestion() {
        String suggestion = "Try using parentheses";
        GnuplotException ex = new GnuplotException("Test error", null, null, null, suggestion);

        assertThat(ex.getMessage())
                .contains("Test error")
                .contains("Suggestion:")
                .contains(suggestion);

        assertThat(ex.getSuggestion()).isPresent();
        assertThat(ex.getSuggestion().get()).isEqualTo(suggestion);
    }

    @Test
    void testParseExceptionFactoryMethod_UnexpectedToken() {
        SourceLocation location = new SourceLocation(1, 5, 5, 5);
        ParseException ex = ParseException.unexpectedToken("expression", "+", location, "2 + + 3");

        assertThat(ex.getMessage())
                .contains("Expected expression but found '+'")
                .contains("line 1, column 5")
                .contains("Expression: 2 + + 3")
                .contains("Suggestion:");
    }

    @Test
    void testParseExceptionFactoryMethod_MismatchedParentheses() {
        SourceLocation location = new SourceLocation(1, 1, 1, 1);
        ParseException ex = ParseException.mismatchedParentheses(location, "(2 + 3");

        assertThat(ex.getMessage())
                .contains("Mismatched parentheses")
                .contains("Suggestion:")
                .contains("matching closing parenthesis");
    }

    @Test
    void testEvaluationExceptionFactoryMethod_UndefinedVariable() {
        SourceLocation location = new SourceLocation(1, 5, 5, 5);
        EvaluationException ex = EvaluationException.undefinedVariable("x", location, "2 + x + 3");

        assertThat(ex.getMessage())
                .contains("Undefined variable: 'x'")
                .contains("line 1, column 5")
                .contains("Expression: 2 + x + 3")
                .contains("Suggestion:")
                .contains("defined before using");
    }

    @Test
    void testEvaluationExceptionFactoryMethod_UndefinedFunction() {
        SourceLocation location = new SourceLocation(1, 1, 1, 1);
        EvaluationException ex = EvaluationException.undefinedFunction("foo", location, "foo(1)");

        assertThat(ex.getMessage())
                .contains("Undefined function: 'foo'")
                .contains("not defined")
                .contains("Suggestion:");
    }

    @Test
    void testEvaluationExceptionFactoryMethod_DivisionByZero() {
        SourceLocation location = new SourceLocation(1, 3, 3, 3);
        EvaluationException ex = EvaluationException.divisionByZero(location, "1 / 0");

        assertThat(ex.getMessage())
                .contains("Division by zero")
                .contains("Suggestion:")
                .contains("divisor is not zero");
    }

    @Test
    void testEvaluationExceptionFactoryMethod_InvalidArgumentCount() {
        SourceLocation location = new SourceLocation(1, 1, 1, 1);
        EvaluationException ex = EvaluationException.invalidArgumentCount("sin", 1, 2, location, "sin(1, 2)");

        assertThat(ex.getMessage())
                .contains("Function 'sin' expects 1 argument(s) but got 2")
                .contains("Suggestion:")
                .contains("exactly 1 argument(s)");
    }

    @Test
    void testEvaluationExceptionFactoryMethod_DomainError() {
        SourceLocation location = new SourceLocation(1, 1, 1, 1);
        EvaluationException ex = EvaluationException.domainError("sqrt", "negative input", location, "sqrt(-1)");

        assertThat(ex.getMessage())
                .contains("Domain error in sqrt")
                .contains("negative input");
    }

    @Test
    void testErrorMessageFormatting_VisualPointer() {
        SourceLocation location = new SourceLocation(1, 5, 5, 5);
        String expression = "2 + x + 3";
        GnuplotException ex = new GnuplotException("Error here", null, location, expression, null);

        String message = ex.getMessage();
        assertThat(message)
                .contains("Expression: 2 + x + 3")
                .contains("^"); // Visual pointer should be present

        // The pointer should be at the right position
        String[] lines = message.split("\n");
        assertThat(lines).hasSizeGreaterThan(2);
        String pointerLine = lines[2];
        int pointerPos = pointerLine.indexOf('^');
        assertThat(pointerPos).isGreaterThan(0);
    }

    @Test
    void testExceptionContextPreservation() {
        SourceLocation location = new SourceLocation(1, 5, 5, 5);
        String expression = "2 + x + 3";
        String suggestion = "Define variable x";

        GnuplotException ex = new GnuplotException("Test", null, location, expression, suggestion);

        assertThat(ex.getLocation()).hasValue(location);
        assertThat(ex.getExpression()).hasValue(expression);
        assertThat(ex.getSuggestion()).hasValue(suggestion);
    }

    @Test
    void testExceptionWithCause() {
        Throwable cause = new IllegalArgumentException("Original cause");
        GnuplotException ex = new GnuplotException("Wrapped error", cause);

        assertThat(ex.getCause()).isEqualTo(cause);
        assertThat(ex.getMessage()).contains("Wrapped error");
    }
}