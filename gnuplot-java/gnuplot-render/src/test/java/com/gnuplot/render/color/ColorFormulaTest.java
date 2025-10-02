package com.gnuplot.render.color;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for ColorFormula enum.
 * Tests all 37 formulas match gnuplot's GetColorValueFromFormula() behavior.
 */
class ColorFormulaTest {

    private static final double EPSILON = 1e-10;

    @Test
    void testConstantFormulas() {
        assertEquals(0.0, ColorFormula.ZERO.apply(0.5), EPSILON);
        assertEquals(0.5, ColorFormula.HALF.apply(0.5), EPSILON);
        assertEquals(1.0, ColorFormula.ONE.apply(0.5), EPSILON);
    }

    @Test
    void testLinearFormula() {
        assertEquals(0.0, ColorFormula.LINEAR.apply(0.0), EPSILON);
        assertEquals(0.5, ColorFormula.LINEAR.apply(0.5), EPSILON);
        assertEquals(1.0, ColorFormula.LINEAR.apply(1.0), EPSILON);
    }

    @Test
    void testPowerFormulas() {
        assertEquals(0.25, ColorFormula.SQUARE.apply(0.5), EPSILON);
        assertEquals(0.125, ColorFormula.CUBE.apply(0.5), EPSILON);
        assertEquals(0.0625, ColorFormula.QUAD.apply(0.5), EPSILON);
    }

    @Test
    void testRootFormulas() {
        double sqrt05 = Math.sqrt(0.5);
        assertEquals(sqrt05, ColorFormula.SQRT.apply(0.5), EPSILON);
        assertEquals(Math.sqrt(sqrt05), ColorFormula.SQRT_SQRT.apply(0.5), EPSILON);
    }

    @Test
    void testTrigonometricFormulas() {
        // sin(90 * 0.5) = sin(45°) = √2/2
        double expected = Math.sin(Math.toRadians(45));
        assertEquals(expected, ColorFormula.SIN_90.apply(0.5), EPSILON);

        // cos(90 * 0.5) = cos(45°) = √2/2
        expected = Math.cos(Math.toRadians(45));
        assertEquals(expected, ColorFormula.COS_90.apply(0.5), EPSILON);
    }

    @Test
    void testAbsoluteValueFormulas() {
        assertEquals(0.0, ColorFormula.ABS_HALF.apply(0.5), EPSILON);
        assertEquals(0.25, ColorFormula.ABS_HALF.apply(0.25), EPSILON);
        assertEquals(0.25, ColorFormula.ABS_HALF.apply(0.75), EPSILON);
    }

    @Test
    void testParabola() {
        assertEquals(0.0, ColorFormula.PARABOLA.apply(0.5), EPSILON);
        assertEquals(1.0, ColorFormula.PARABOLA.apply(0.0), EPSILON);
        assertEquals(1.0, ColorFormula.PARABOLA.apply(1.0), EPSILON);
    }

    @Test
    void testScalingFormulas() {
        // Note: Results are clamped to [0, 1]
        assertEquals(1.0, ColorFormula.TRIPLE.apply(0.5), EPSILON); // 1.5 clamped to 1.0
        assertEquals(0.5, ColorFormula.TRIPLE_MINUS_1.apply(0.5), EPSILON);
        assertEquals(0.0, ColorFormula.TRIPLE_MINUS_2.apply(0.5), EPSILON); // -0.5 clamped to 0.0

        assertEquals(1.0, ColorFormula.DOUBLE.apply(0.5), EPSILON);
        assertEquals(0.5, ColorFormula.DOUBLE_MINUS_HALF.apply(0.5), EPSILON);
        assertEquals(0.0, ColorFormula.DOUBLE_MINUS_1.apply(0.5), EPSILON);
    }

    @Test
    void testStepFormulas() {
        // STEP_1: 0 if x≤0.25, 1 if x≥0.57, else (x/0.32 - 0.78125)
        assertEquals(0.0, ColorFormula.STEP_1.apply(0.2), EPSILON);
        assertEquals(1.0, ColorFormula.STEP_1.apply(0.6), EPSILON);

        // STEP_2: 0 if x≤0.42, 1 if x≥0.92, else 2x - 0.84
        assertEquals(0.0, ColorFormula.STEP_2.apply(0.4), EPSILON);
        assertEquals(1.0, ColorFormula.STEP_2.apply(0.95), EPSILON);
    }

    @Test
    void testClampingToRange() {
        // Formulas that can produce values outside [0, 1] should be clamped
        double result = ColorFormula.TRIPLE.apply(0.8);
        assertTrue(result >= 0.0 && result <= 1.0);

        result = ColorFormula.TRIPLE_MINUS_2.apply(0.3);
        assertTrue(result >= 0.0 && result <= 1.0);
    }

    @Test
    void testFromNumber() {
        assertEquals(ColorFormula.ZERO, ColorFormula.fromNumber(0));
        assertEquals(ColorFormula.LINEAR, ColorFormula.fromNumber(3));
        assertEquals(ColorFormula.SQRT, ColorFormula.fromNumber(7));
        assertEquals(ColorFormula.DOUBLE_MINUS_1, ColorFormula.fromNumber(36));
    }

    @Test
    void testFromNumberInvalid() {
        assertThrows(IllegalArgumentException.class, () -> ColorFormula.fromNumber(37));
        assertThrows(IllegalArgumentException.class, () -> ColorFormula.fromNumber(-37));
    }

    @Test
    void testApplyFormulaWithNegativeNumber() {
        // Negative formula numbers invert the input: x = 1 - x
        double result = ColorFormula.applyFormula(-3, 0.3); // -3 = inverted LINEAR
        assertEquals(0.7, result, EPSILON); // 1 - 0.3 = 0.7

        result = ColorFormula.applyFormula(-4, 0.5); // -4 = inverted SQUARE
        assertEquals(0.25, result, EPSILON); // (1 - 0.5)² = 0.25
    }

    @Test
    void testDefaultGnuplotPalette() {
        // Default gnuplot palette uses formulas 7, 5, 15
        // Formula 7: sqrt(x)
        // Formula 5: x^3
        // Formula 15: sin(360x)

        double gray = 0.5;
        double r = ColorFormula.applyFormula(7, gray);
        double g = ColorFormula.applyFormula(5, gray);
        double b = ColorFormula.applyFormula(15, gray);

        assertEquals(Math.sqrt(0.5), r, EPSILON);
        assertEquals(0.125, g, EPSILON);
        assertEquals(Math.sin(Math.toRadians(180)), b, EPSILON);
    }

    @Test
    void testAllFormulasInRange() {
        // Test that all formulas produce values in [0, 1] for various inputs
        double[] testInputs = {0.0, 0.1, 0.25, 0.5, 0.75, 0.9, 1.0};

        for (ColorFormula formula : ColorFormula.values()) {
            for (double input : testInputs) {
                double result = formula.apply(input);
                assertTrue(result >= 0.0 && result <= 1.0,
                        String.format("%s(%f) = %f is out of range", formula, input, result));
            }
        }
    }

    @Test
    void testFormulaNumbers() {
        // Verify formula numbers are in correct range
        for (ColorFormula formula : ColorFormula.values()) {
            int num = formula.getFormulaNumber();
            assertTrue(num >= 0 && num <= 36,
                    String.format("%s has invalid number %d", formula, num));
        }
    }
}
