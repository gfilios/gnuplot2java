package com.gnuplot.core.functions;

import com.gnuplot.core.ast.ASTNode;
import com.gnuplot.core.evaluator.EvaluationContext;
import com.gnuplot.core.evaluator.Evaluator;
import com.gnuplot.core.parser.ExpressionParser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests for Bessel functions.
 */
@DisplayName("Bessel Functions")
class BesselFunctionsTest {

    private ExpressionParser parser;
    private EvaluationContext context;
    private Evaluator evaluator;

    @BeforeEach
    void setUp() {
        parser = new ExpressionParser();
        context = new EvaluationContext();
        StandardMathFunctions.registerAll(context);
        BesselFunctions.registerAll(context);
        evaluator = new Evaluator(context);
    }

    private double eval(String expression) {
        ASTNode ast = parser.parseOrThrow(expression);
        return evaluator.evaluate(ast);
    }

    // ========== Bessel J0 Function ==========

    @Test
    @DisplayName("besj0 should evaluate at zero")
    void besj0ShouldEvaluateAtZero() {
        // J_0(0) = 1
        assertThat(eval("besj0(0)")).isCloseTo(1.0, within(1e-10));
    }

    @ParameterizedTest(name = "besj0({0}) ≈ {1}")
    @CsvSource({
            "1.0, 0.7651976865579666",
            "2.0, 0.2238907791412357",
            "5.0, -0.1775967713143383",
            "10.0, -0.2459357644513483"
    })
    @DisplayName("besj0 should match known values")
    void besj0ShouldMatchKnownValues(double x, double expected) {
        double result = eval("besj0(" + x + ")");
        assertThat(result).isCloseTo(expected, within(1e-10));
    }

    // ========== Bessel J1 Function ==========

    @Test
    @DisplayName("besj1 should evaluate at zero")
    void besj1ShouldEvaluateAtZero() {
        // J_1(0) = 0
        assertThat(eval("besj1(0)")).isCloseTo(0.0, within(1e-10));
    }

    @ParameterizedTest(name = "besj1({0}) ≈ {1}")
    @CsvSource({
            "1.0, 0.4400505857449335",
            "2.0, 0.5767248077568734",
            "5.0, -0.3275791375914652",
            "10.0, 0.04347274616886144"
    })
    @DisplayName("besj1 should match known values")
    void besj1ShouldMatchKnownValues(double x, double expected) {
        double result = eval("besj1(" + x + ")");
        assertThat(result).isCloseTo(expected, within(1e-10));
    }

    // ========== Bessel Jn Function (general order) ==========

    @Test
    @DisplayName("besjn should match besj0 for n=0")
    void besjnShouldMatchBesj0() {
        double x = 2.5;
        assertThat(eval("besjn(0, " + x + ")")).isCloseTo(eval("besj0(" + x + ")"), within(1e-10));
    }

    @Test
    @DisplayName("besjn should match besj1 for n=1")
    void besjnShouldMatchBesj1() {
        double x = 3.5;
        assertThat(eval("besjn(1, " + x + ")")).isCloseTo(eval("besj1(" + x + ")"), within(1e-10));
    }

    @ParameterizedTest(name = "besjn({0}, {1}) ≈ {2}")
    @CsvSource({
            "2, 1.0, 0.1149034849319005",
            "3, 1.0, 0.01956335398063803",
            "2, 5.0, 0.04656511627775222",
            "3, 5.0, 0.36483123061366696"
    })
    @DisplayName("besjn should evaluate for various orders")
    void besjnShouldEvaluateVariousOrders(int n, double x, double expected) {
        double result = eval("besjn(" + n + ", " + x + ")");
        assertThat(result).isCloseTo(expected, within(1e-10));
    }

    // ========== Properties and Edge Cases ==========

    @Test
    @DisplayName("Bessel J functions should satisfy recurrence relation")
    void besselJShouldSatisfyRecurrence() {
        // J_{n-1}(x) + J_{n+1}(x) = (2n/x) * J_n(x)
        int n = 2;
        double x = 5.0;

        double jn_minus_1 = eval("besjn(" + (n - 1) + ", " + x + ")");
        double jn = eval("besjn(" + n + ", " + x + ")");
        double jn_plus_1 = eval("besjn(" + (n + 1) + ", " + x + ")");

        double lhs = jn_minus_1 + jn_plus_1;
        double rhs = (2.0 * n / x) * jn;

        assertThat(lhs).isCloseTo(rhs, within(1e-8));
    }

    @Test
    @DisplayName("besj0 should handle negative arguments")
    void besj0ShouldHandleNegativeArguments() {
        // J_0(-x) = J_0(x) (even function)
        double x = 3.0;
        assertThat(eval("besj0(-" + x + ")")).isCloseTo(eval("besj0(" + x + ")"), within(1e-10));
    }

    @Test
    @DisplayName("besj1 should handle negative arguments")
    void besj1ShouldHandleNegativeArguments() {
        // J_1(-x) = -J_1(x) (odd function)
        double x = 3.0;
        assertThat(eval("besj1(-" + x + ")")).isCloseTo(-eval("besj1(" + x + ")"), within(1e-10));
    }

    // ========== Unimplemented Functions ==========

    @Test
    @DisplayName("besy0 should throw UnsupportedOperationException")
    void besy0ShouldThrow() {
        assertThatThrownBy(() -> eval("besy0(1.0)"))
                .isInstanceOf(Exception.class)
                .hasMessageContaining("not yet implemented");
    }

    @Test
    @DisplayName("besi0 should throw UnsupportedOperationException")
    void besi0ShouldThrow() {
        assertThatThrownBy(() -> eval("besi0(1.0)"))
                .isInstanceOf(Exception.class)
                .hasMessageContaining("not yet implemented");
    }
}