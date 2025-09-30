package com.gnuplot.core.functions;

import com.gnuplot.core.ast.ASTNode;
import com.gnuplot.core.evaluator.EvaluationContext;
import com.gnuplot.core.evaluator.Evaluator;
import com.gnuplot.core.oracle.TestCase;
import com.gnuplot.core.oracle.TestOracle;
import com.gnuplot.core.parser.ExpressionParser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests for standard mathematical functions.
 */
@DisplayName("Standard Math Functions")
class StandardMathFunctionsTest {

    private ExpressionParser parser;
    private EvaluationContext context;
    private Evaluator evaluator;

    @BeforeEach
    void setUp() {
        parser = new ExpressionParser();
        context = new EvaluationContext();
        StandardMathFunctions.registerAll(context);
        evaluator = new Evaluator(context);
    }

    private double eval(String expression) {
        ASTNode ast = parser.parseOrThrow(expression);
        return evaluator.evaluate(ast);
    }

    // ========== Trigonometric Functions ==========

    @ParameterizedTest(name = "{0} ≈ {1}")
    @CsvSource({
            "sin(0), 0",
            "sin(pi/2), 1",
            "sin(pi), 0",
            "cos(0), 1",
            "cos(pi/2), 0",
            "cos(pi), -1",
            "tan(0), 0",
            "tan(pi/4), 1"
    })
    @DisplayName("should evaluate basic trigonometric functions")
    void shouldEvaluateBasicTrigonometry(String expression, double expected) {
        assertThat(eval(expression)).isCloseTo(expected, within(1e-10));
    }

    @Test
    @DisplayName("should evaluate inverse trigonometric functions")
    void shouldEvaluateInverseTrigonometry() {
        assertThat(eval("asin(0)")).isCloseTo(0, within(1e-10));
        assertThat(eval("asin(1)")).isCloseTo(Math.PI / 2, within(1e-10));
        assertThat(eval("acos(1)")).isCloseTo(0, within(1e-10));
        assertThat(eval("acos(0)")).isCloseTo(Math.PI / 2, within(1e-10));
        assertThat(eval("atan(0)")).isCloseTo(0, within(1e-10));
        assertThat(eval("atan(1)")).isCloseTo(Math.PI / 4, within(1e-10));
    }

    @Test
    @DisplayName("should evaluate atan2 function")
    void shouldEvaluateAtan2() {
        assertThat(eval("atan2(0, 1)")).isCloseTo(0, within(1e-10));
        assertThat(eval("atan2(1, 0)")).isCloseTo(Math.PI / 2, within(1e-10));
        assertThat(eval("atan2(1, 1)")).isCloseTo(Math.PI / 4, within(1e-10));
    }

    // ========== Hyperbolic Functions ==========

    @Test
    @DisplayName("should evaluate hyperbolic functions")
    void shouldEvaluateHyperbolicFunctions() {
        assertThat(eval("sinh(0)")).isCloseTo(0, within(1e-10));
        assertThat(eval("cosh(0)")).isCloseTo(1, within(1e-10));
        assertThat(eval("tanh(0)")).isCloseTo(0, within(1e-10));

        // sinh(1) ≈ 1.1752011936438014
        assertThat(eval("sinh(1)")).isCloseTo(1.1752011936438014, within(1e-10));
        // cosh(1) ≈ 1.5430806348152437
        assertThat(eval("cosh(1)")).isCloseTo(1.5430806348152437, within(1e-10));
        // tanh(1) ≈ 0.7615941559557649
        assertThat(eval("tanh(1)")).isCloseTo(0.7615941559557649, within(1e-10));
    }

    // ========== Exponential and Logarithmic Functions ==========

    @ParameterizedTest(name = "{0} ≈ {1}")
    @CsvSource({
            "exp(0), 1",
            "exp(1), 2.718281828",
            "log(1), 0",
            "log(e), 1",
            "log10(1), 0",
            "log10(10), 1",
            "log10(100), 2"
    })
    @DisplayName("should evaluate exponential and logarithmic functions")
    void shouldEvaluateExpAndLog(String expression, double expected) {
        assertThat(eval(expression)).isCloseTo(expected, within(1e-8));
    }

    // ========== Power and Root Functions ==========

    @ParameterizedTest(name = "{0} = {1}")
    @CsvSource({
            "sqrt(0), 0",
            "sqrt(1), 1",
            "sqrt(4), 2",
            "sqrt(16), 4",
            "cbrt(0), 0",
            "cbrt(1), 1",
            "cbrt(8), 2",
            "cbrt(27), 3",
            "'pow(2, 3)', 8",
            "'pow(10, 2)', 100"
    })
    @DisplayName("should evaluate power and root functions")
    void shouldEvaluatePowerAndRoot(String expression, double expected) {
        assertThat(eval(expression)).isCloseTo(expected, within(1e-10));
    }

    // ========== Rounding Functions ==========

    @ParameterizedTest(name = "{0} = {1}")
    @CsvSource({
            "abs(5), 5",
            "abs(-5), 5",
            "abs(0), 0",
            "ceil(3.2), 4",
            "ceil(3.8), 4",
            "ceil(-3.2), -3",
            "floor(3.2), 3",
            "floor(3.8), 3",
            "floor(-3.2), -4",
            "round(3.2), 3",
            "round(3.5), 4",
            "round(3.8), 4"
    })
    @DisplayName("should evaluate rounding functions")
    void shouldEvaluateRoundingFunctions(String expression, double expected) {
        assertThat(eval(expression)).isEqualTo(expected);
    }

    // ========== Sign and Comparison Functions ==========

    @ParameterizedTest(name = "{0} = {1}")
    @CsvSource({
            "sgn(5), 1",
            "sgn(-5), -1",
            "sgn(0), 0",
            "'min(5, 3)', 3",
            "'min(3, 5)', 3",
            "'min(3, 5, 2, 8)', 2",
            "'max(5, 3)', 5",
            "'max(3, 5)', 5",
            "'max(3, 5, 9, 1)', 9"
    })
    @DisplayName("should evaluate sign and comparison functions")
    void shouldEvaluateSignAndComparison(String expression, double expected) {
        assertThat(eval(expression)).isEqualTo(expected);
    }

    // ========== Test Oracle Validation ==========

    @Test
    @DisplayName("should match test oracle for basic arithmetic")
    void shouldMatchTestOracleBasicArithmetic() {
        List<TestCase> tests = TestOracle.getInstance().getTestData("basic_arithmetic").tests();

        for (TestCase test : tests) {
            if (test.error()) {
                continue; // Skip error cases for now
            }
            double result = eval(test.expression());
            assertThat(result)
                    .as("Expression: %s", test.expression())
                    .isCloseTo(test.result(), within(1e-10));
        }
    }

    @Test
    @DisplayName("should match test oracle for trigonometric functions")
    void shouldMatchTestOracleTrigonometric() {
        List<TestCase> tests = TestOracle.getInstance().getTestData("trigonometric").tests();

        for (TestCase test : tests) {
            if (test.error()) {
                continue; // Skip error cases
            }
            double result = eval(test.expression());
            assertThat(result)
                    .as("Expression: %s", test.expression())
                    .isCloseTo(test.result(), within(1e-10));
        }
    }

    @Test
    @DisplayName("should match test oracle for exponential and logarithmic functions")
    void shouldMatchTestOracleExpLog() {
        List<TestCase> tests = TestOracle.getInstance().getTestData("exponential_logarithmic").tests();

        for (TestCase test : tests) {
            if (test.error()) {
                continue; // Skip error cases
            }
            double result = eval(test.expression());
            assertThat(result)
                    .as("Expression: %s", test.expression())
                    .isCloseTo(test.result(), within(1e-10));
        }
    }

    @Test
    @DisplayName("should match test oracle for hyperbolic functions")
    void shouldMatchTestOracleHyperbolic() {
        List<TestCase> tests = TestOracle.getInstance().getTestData("hyperbolic").tests();

        for (TestCase test : tests) {
            if (test.error()) {
                continue; // Skip error cases
            }
            double result = eval(test.expression());
            assertThat(result)
                    .as("Expression: %s", test.expression())
                    .isCloseTo(test.result(), within(1e-10));
        }
    }

    @Test
    @DisplayName("should match test oracle for constants")
    void shouldMatchTestOracleConstants() {
        List<TestCase> tests = TestOracle.getInstance().getTestData("constants").tests();

        for (TestCase test : tests) {
            if (test.error()) {
                continue; // Skip error cases
            }
            double result = eval(test.expression());
            assertThat(result)
                    .as("Expression: %s", test.expression())
                    .isCloseTo(test.result(), within(1e-10));
        }
    }

    @Test
    @DisplayName("should match test oracle for complex expressions")
    void shouldMatchTestOracleComplexExpressions() {
        List<TestCase> tests = TestOracle.getInstance().getTestData("complex_expressions").tests();

        for (TestCase test : tests) {
            if (test.error()) {
                continue; // Skip error cases
            }
            double result = eval(test.expression());
            assertThat(result)
                    .as("Expression: %s", test.expression())
                    .isCloseTo(test.result(), within(1e-10));
        }
    }
}