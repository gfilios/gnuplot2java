package com.gnuplot.core.evaluator;

import com.gnuplot.core.parser.ExpressionParser;
import com.gnuplot.core.parser.ParseResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for ComplexEvaluator focusing on complex number arithmetic.
 */
class ComplexEvaluatorTest {

    private EvaluationContext context;
    private ComplexEvaluator evaluator;
    private ExpressionParser parser;

    @BeforeEach
    void setUp() {
        context = new EvaluationContext();
        evaluator = new ComplexEvaluator(context);
        parser = new ExpressionParser();
    }

    @Test
    void testSqrtOfNegativeNumber() {
        // sqrt(-1) should return i
        ParseResult r = parser.parse("sqrt(-1)");
        assertTrue(r.isSuccess(), "Parse should succeed");

        Complex result = evaluator.evaluate(r.getAst());
        assertEquals(0.0, result.real(), 1e-10, "Real part should be 0");
        assertEquals(1.0, result.imag(), 1e-10, "Imaginary part should be 1");
    }

    @Test
    void testSqrtOfNegative125() {
        // sqrt(-1.25) should return i * sqrt(1.25)
        ParseResult r = parser.parse("sqrt(-1.25)");
        assertTrue(r.isSuccess(), "Parse should succeed");

        Complex result = evaluator.evaluate(r.getAst());
        assertEquals(0.0, result.real(), 1e-10, "Real part should be 0");
        assertEquals(Math.sqrt(1.25), result.imag(), 1e-10, "Imaginary part should be sqrt(1.25)");
    }

    @Test
    void testOverdampedControlSystem() {
        // Test the controls.dem scenario with s = 1.5 (overdamped)
        context.setVariable("s", 1.5);
        context.setVariable("wn", 1.0);
        context.setVariable("t", 1.0);

        // 1.0 - s*s = -1.25
        ParseResult r1 = parser.parse("1.0 - s*s");
        Complex result1 = evaluator.evaluate(r1.getAst());
        assertEquals(-1.25, result1.real(), 1e-10);
        assertEquals(0.0, result1.imag(), 1e-10);

        // sqrt(1.0 - s*s) = i * sqrt(1.25)
        ParseResult r2 = parser.parse("sqrt(1.0 - s*s)");
        Complex result2 = evaluator.evaluate(r2.getAst());
        System.out.println("sqrt(1.0 - s*s) = " + result2);
        assertEquals(0.0, result2.real(), 1e-10);
        assertEquals(Math.sqrt(1.25), result2.imag(), 1e-10);
    }

    @Test
    void testDampFunctionWithOverdampedS() {
        // damp(t) = exp(-s*wn*t)/sqrt(1.0-s*s) for s=1.5
        context.setVariable("s", 1.5);
        context.setVariable("wn", 1.0);
        context.setVariable("t", 1.0);

        // exp(-1.5) / (i * sqrt(1.25)) = exp(-1.5) * (-i) / sqrt(1.25)
        // = -i * exp(-1.5) / sqrt(1.25)
        ParseResult r = parser.parse("exp(-s*wn*t)/sqrt(1.0-s*s)");
        Complex result = evaluator.evaluate(r.getAst());
        System.out.println("exp(-s*wn*t)/sqrt(1.0-s*s) = " + result);

        double expected_imag = -Math.exp(-1.5) / Math.sqrt(1.25);
        assertEquals(0.0, result.real(), 1e-10, "Real part should be 0");
        assertEquals(expected_imag, result.imag(), 1e-10, "Imaginary part should be -exp(-1.5)/sqrt(1.25)");
    }

    @Test
    void testUserDefinedDampFunction() {
        context.setVariable("s", 1.5);
        context.setVariable("wn", 1.0);

        // Register damp(t) = exp(-s*wn*t)/sqrt(1.0-s*s)
        context.registerUserFunction("damp", List.of("t"), "exp(-s*wn*t)/sqrt(1.0-s*s)");

        ParseResult r = parser.parse("damp(1.0)");
        Complex result = evaluator.evaluate(r.getAst());
        System.out.println("damp(1.0) = " + result);

        double expected_imag = -Math.exp(-1.5) / Math.sqrt(1.25);
        assertEquals(0.0, result.real(), 1e-10, "Real part should be 0");
        assertEquals(expected_imag, result.imag(), 1e-10, "Imaginary part should be -exp(-1.5)/sqrt(1.25)");
    }

    @Test
    void testComplexMultiplicationResultsInReal() {
        // When we multiply two pure imaginary numbers, we get a real number
        // i * i = -1
        // (ai) * (bi) = -ab (real)
        context.setVariable("s", 1.5);
        context.setVariable("wn", 1.0);
        context.setVariable("t", 1.0);

        // damp(t) = exp(-s*wn*t)/sqrt(1.0-s*s) = -i * A (pure imaginary)
        // per(t) = sin(wn*sqrt(1.0-s**2)*t - atan(-sqrt(1.0-s**2)/s)) = i * B (pure imaginary)
        // damp(t) * per(t) = (-i * A) * (i * B) = -i^2 * A * B = A * B (real)

        context.registerUserFunction("damp", List.of("t"), "exp(-s*wn*t)/sqrt(1.0-s*s)");
        context.registerUserFunction("per", List.of("t"), "sin(wn*sqrt(1.0-s**2)*t - atan(-sqrt(1.0-s**2)/s))");
        context.registerUserFunction("c", List.of("t"), "1-damp(t)*per(t)");

        ParseResult r = parser.parse("c(1.0)");
        Complex result = evaluator.evaluate(r.getAst());
        System.out.println("c(1.0) for s=1.5 = " + result);

        // The result should be real (imaginary part should be essentially 0)
        assertEquals(0.0, result.imag(), 1e-10, "Imaginary part should be 0");
        // The real part should be a valid value (not NaN)
        assertFalse(Double.isNaN(result.real()), "Real part should not be NaN");
        System.out.println("c(1.0) real value = " + result.real());
    }

    @Test
    void testControlsDemAllDampingFactors() {
        context.setVariable("wn", 1.0);
        context.registerUserFunction("damp", List.of("t"), "exp(-s*wn*t)/sqrt(1.0-s*s)");
        context.registerUserFunction("per", List.of("t"), "sin(wn*sqrt(1.0-s**2)*t - atan(-sqrt(1.0-s**2)/s))");
        context.registerUserFunction("c", List.of("t"), "1-damp(t)*per(t)");

        // Note: s=1.0 is critically damped and results in division by zero (sqrt(0))
        // This is handled by returning NaN, which gnuplot also does
        double[] dampingFactors = {0.1, 0.3, 0.5, 0.7, 0.9, 1.5, 2.0};

        for (double s : dampingFactors) {
            context.setVariable("s", s);

            ParseResult r = parser.parse("c(5.0)");  // t = 5.0
            Complex result = evaluator.evaluate(r.getAst());

            System.out.println("s=" + s + ", c(5.0) = " + result.real());

            // All results should be real (or have negligible imaginary part)
            assertEquals(0.0, result.imag(), 1e-8, "s=" + s + ": imaginary part should be ~0");
            assertFalse(Double.isNaN(result.real()), "s=" + s + ": result should not be NaN");
        }
    }

    @Test
    void testCriticallyDamped() {
        // s=1.0 is critically damped, which results in division by zero
        context.setVariable("wn", 1.0);
        context.setVariable("s", 1.0);
        context.registerUserFunction("damp", List.of("t"), "exp(-s*wn*t)/sqrt(1.0-s*s)");

        ParseResult r = parser.parse("damp(5.0)");
        Complex result = evaluator.evaluate(r.getAst());

        // Division by zero should produce NaN
        System.out.println("s=1.0, damp(5.0) = " + result);
        assertTrue(Double.isNaN(result.real()) || Double.isInfinite(result.real()),
                "Critically damped case should produce NaN or Infinity");
    }
}
