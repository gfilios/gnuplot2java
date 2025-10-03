package com.gnuplot.core.evaluator;

import com.gnuplot.core.parser.ExpressionParser;
import com.gnuplot.core.parser.ParseResult;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

/**
 * Test that standard math functions are registered and working.
 */
class MathFunctionsTest {

    private final ExpressionParser parser = new ExpressionParser();
    private final EvaluationContext context = new EvaluationContext();
    private final Evaluator evaluator = new Evaluator(context);

    private double eval(String expression) {
        ParseResult result = parser.parse(expression);
        assertThat(result.isSuccess()).isTrue();
        return evaluator.evaluate(result.getAst());
    }

    @Test
    void testSinFunction() {
        assertThat(eval("sin(0)")).isCloseTo(0.0, within(0.0001));
        assertThat(eval("sin(1.5708)")).isCloseTo(1.0, within(0.001)); // pi/2
    }

    @Test
    void testCosFunction() {
        assertThat(eval("cos(0)")).isCloseTo(1.0, within(0.0001));
        assertThat(eval("cos(3.14159)")).isCloseTo(-1.0, within(0.001)); // pi
    }

    @Test
    void testAtanFunction() {
        assertThat(eval("atan(0)")).isCloseTo(0.0, within(0.0001));
        assertThat(eval("atan(1)")).isCloseTo(0.7854, within(0.001)); // pi/4
    }

    @Test
    void testBesselJ0Function() {
        assertThat(eval("besj0(0)")).isCloseTo(1.0, within(0.0001));
        assertThat(eval("besj0(2.4048)")).isCloseTo(0.0, within(0.01)); // First zero
    }

    @Test
    void testRealFunction() {
        assertThat(eval("real(5.7)")).isCloseTo(5.7, within(0.0001));
    }

    @Test
    void testWithVariable() {
        context.setVariable("x", 0.5);
        assertThat(eval("sin(x)")).isCloseTo(Math.sin(0.5), within(0.0001));
    }
}
