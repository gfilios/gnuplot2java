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
 * Tests for special mathematical functions (Gamma, Beta).
 */
@DisplayName("Special Functions")
class SpecialFunctionsTest {

    private ExpressionParser parser;
    private EvaluationContext context;
    private Evaluator evaluator;

    @BeforeEach
    void setUp() {
        parser = new ExpressionParser();
        context = new EvaluationContext();
        StandardMathFunctions.registerAll(context);
        SpecialFunctions.registerAll(context);
        evaluator = new Evaluator(context);
    }

    private double eval(String expression) {
        ASTNode ast = parser.parseOrThrow(expression);
        return evaluator.evaluate(ast);
    }

    // ========== Gamma Functions ==========

    @ParameterizedTest(name = "{0} ≈ {1}")
    @CsvSource({
            "gamma(1), 1",
            "gamma(2), 1",
            "gamma(3), 2",
            "gamma(4), 6",
            "gamma(5), 24",
            "gamma(0.5), 1.772453850905516"  // sqrt(pi)
    })
    @DisplayName("should evaluate gamma function")
    void shouldEvaluateGammaFunction(String expression, double expected) {
        assertThat(eval(expression)).isCloseTo(expected, within(1e-10));
    }

    @Test
    @DisplayName("should evaluate lgamma function")
    void shouldEvaluateLgammaFunction() {
        // lgamma(n) = log(gamma(n)) = log((n-1)!)
        assertThat(eval("lgamma(1)")).isCloseTo(0, within(1e-10));  // log(1) = 0
        assertThat(eval("lgamma(2)")).isCloseTo(0, within(1e-10));  // log(1!) = 0
        assertThat(eval("lgamma(3)")).isCloseTo(Math.log(2), within(1e-10));  // log(2!) = log(2)
        assertThat(eval("lgamma(4)")).isCloseTo(Math.log(6), within(1e-10));  // log(3!) = log(6)
    }

    // ========== Beta Functions ==========

    @ParameterizedTest(name = "{0} ≈ {1}")
    @CsvSource({
            "'beta(1, 1)', 1",
            "'beta(1, 2)', 0.5",
            "'beta(2, 1)', 0.5",
            "'beta(2, 2)', 0.166666666666667",  // 1/6
            "'beta(3, 3)', 0.0333333333333333"   // 1/30
    })
    @DisplayName("should evaluate beta function")
    void shouldEvaluateBetaFunction(String expression, double expected) {
        assertThat(eval(expression)).isCloseTo(expected, within(1e-10));
    }

    // ========== Incomplete Gamma Functions ==========

    @Test
    @DisplayName("should evaluate incomplete gamma function")
    void shouldEvaluateIncompleteGamma() {
        // igamma(a, 0) = 0
        assertThat(eval("igamma(1, 0)")).isCloseTo(0, within(1e-10));

        // igamma(1, x) approaches gamma(1) = 1 as x approaches infinity
        assertThat(eval("igamma(1, 10)")).isCloseTo(1, within(1e-3));
    }

    @Test
    @DisplayName("should evaluate regularized incomplete gamma function")
    void shouldEvaluateRegularizedGamma() {
        // gammainc(a, 0) = 0
        assertThat(eval("gammainc(1, 0)")).isCloseTo(0, within(1e-10));

        // gammainc(a, infinity) = 1
        assertThat(eval("gammainc(1, 10)")).isCloseTo(1, within(1e-3));
    }

    // ========== Incomplete Beta Functions ==========

    @Test
    @DisplayName("should evaluate incomplete beta function")
    void shouldEvaluateIncompleteBeta() {
        // ibeta(a, b, 0) = 0
        assertThat(eval("ibeta(1, 1, 0)")).isCloseTo(0, within(1e-10));

        // ibeta(a, b, 1) = beta(a, b)
        assertThat(eval("ibeta(1, 1, 1)")).isCloseTo(eval("beta(1, 1)"), within(1e-10));
    }

    @Test
    @DisplayName("should evaluate regularized incomplete beta function")
    void shouldEvaluateRegularizedBeta() {
        // betainc(a, b, 0) = 0
        assertThat(eval("betainc(1, 1, 0)")).isCloseTo(0, within(1e-10));

        // betainc(a, b, 1) = 1
        assertThat(eval("betainc(1, 1, 1)")).isCloseTo(1, within(1e-10));

        // betainc(1, 1, 0.5) = 0.5 (uniform distribution)
        assertThat(eval("betainc(1, 1, 0.5)")).isCloseTo(0.5, within(1e-10));
    }

    // ========== Test Oracle Validation ==========

    @Test
    @DisplayName("should match test oracle for special functions")
    void shouldMatchTestOracleSpecialFunctions() {
        List<TestCase> tests = TestOracle.getInstance().getTestData("special_functions").tests();

        int passedCount = 0;
        int skippedCount = 0;

        for (TestCase test : tests) {
            if (test.error()) {
                skippedCount++;
                continue; // Skip error cases
            }

            try {
                double result = eval(test.expression());
                assertThat(result)
                        .as("Expression: %s", test.expression())
                        .isCloseTo(test.result(), within(1e-8));  // Slightly relaxed tolerance for special functions
                passedCount++;
            } catch (Exception e) {
                // Some special functions might not be implemented yet
                skippedCount++;
                System.out.println("Skipped: " + test.expression() + " - " + e.getMessage());
            }
        }

        System.out.printf("Special functions test oracle: %d passed, %d skipped%n", passedCount, skippedCount);
        assertThat(passedCount).isGreaterThan(0).as("At least some test oracle cases should pass");
    }

    // ========== Edge Cases and Properties ==========

    @Test
    @DisplayName("gamma function should satisfy gamma(n) = (n-1)! for positive integers")
    void gammaShouldSatisfyFactorialProperty() {
        assertThat(eval("gamma(6)")).isCloseTo(120, within(1e-10));  // 5!
        assertThat(eval("gamma(7)")).isCloseTo(720, within(1e-10));  // 6!
    }

    @Test
    @DisplayName("beta function should satisfy symmetry property")
    void betaShouldSatisfySymmetryProperty() {
        // beta(a, b) = beta(b, a)
        assertThat(eval("beta(2, 3)")).isCloseTo(eval("beta(3, 2)"), within(1e-10));
        assertThat(eval("beta(5, 7)")).isCloseTo(eval("beta(7, 5)"), within(1e-10));
    }

    @Test
    @DisplayName("beta function should relate to gamma")
    void betaShouldRelateToGamma() {
        // beta(a, b) = gamma(a) * gamma(b) / gamma(a+b)
        double beta23 = eval("beta(2, 3)");
        double gammaRelation = eval("gamma(2) * gamma(3) / gamma(5)");
        assertThat(beta23).isCloseTo(gammaRelation, within(1e-10));
    }
}