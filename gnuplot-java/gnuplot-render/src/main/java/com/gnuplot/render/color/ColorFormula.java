package com.gnuplot.render.color;

/**
 * Color transformation formulas for mapping gray values [0,1] to RGB components.
 * These formulas correspond to gnuplot's GetColorValueFromFormula() function in getcolor.c.
 * <p>
 * Each formula maps an input value x in [0,1] to an output value in [0,1].
 * Negative formula numbers invert the input: x = 1 - x before applying the formula.
 */
public enum ColorFormula {
    /** Constant 0 */
    ZERO(0, x -> 0.0),
    /** Constant 0.5 */
    HALF(1, x -> 0.5),
    /** Constant 1 */
    ONE(2, x -> 1.0),
    /** Identity: x */
    LINEAR(3, x -> x),
    /** x² */
    SQUARE(4, x -> x * x),
    /** x³ */
    CUBE(5, x -> x * x * x),
    /** x⁴ */
    QUAD(6, x -> x * x * x * x),
    /** √x */
    SQRT(7, Math::sqrt),
    /** ⁴√x */
    SQRT_SQRT(8, x -> Math.sqrt(Math.sqrt(x))),
    /** sin(90x)° */
    SIN_90(9, x -> Math.sin(Math.toRadians(90 * x))),
    /** cos(90x)° */
    COS_90(10, x -> Math.cos(Math.toRadians(90 * x))),
    /** |x - 0.5| */
    ABS_HALF(11, x -> Math.abs(x - 0.5)),
    /** (2x - 1)² */
    PARABOLA(12, x -> (2 * x - 1) * (2 * x - 1)),
    /** sin(180x)° */
    SIN_180(13, x -> Math.sin(Math.toRadians(180 * x))),
    /** |cos(180x)°| */
    ABS_COS_180(14, x -> Math.abs(Math.cos(Math.toRadians(180 * x)))),
    /** sin(360x)° */
    SIN_360(15, x -> Math.sin(Math.toRadians(360 * x))),
    /** cos(360x)° */
    COS_360(16, x -> Math.cos(Math.toRadians(360 * x))),
    /** |sin(360x)°| */
    ABS_SIN_360(17, x -> Math.abs(Math.sin(Math.toRadians(360 * x)))),
    /** |cos(360x)°| */
    ABS_COS_360(18, x -> Math.abs(Math.cos(Math.toRadians(360 * x)))),
    /** |sin(720x)°| */
    ABS_SIN_720(19, x -> Math.abs(Math.sin(Math.toRadians(720 * x)))),
    /** |cos(720x)°| */
    ABS_COS_720(20, x -> Math.abs(Math.cos(Math.toRadians(720 * x)))),
    /** 3x */
    TRIPLE(21, x -> 3 * x),
    /** 3x - 1 */
    TRIPLE_MINUS_1(22, x -> 3 * x - 1),
    /** 3x - 2 */
    TRIPLE_MINUS_2(23, x -> 3 * x - 2),
    /** |3x - 1| */
    ABS_TRIPLE_MINUS_1(24, x -> Math.abs(3 * x - 1)),
    /** |3x - 2| */
    ABS_TRIPLE_MINUS_2(25, x -> Math.abs(3 * x - 2)),
    /** 1.5x - 0.5 */
    ONE_HALF_X_MINUS_HALF(26, x -> 1.5 * x - 0.5),
    /** 1.5x - 1 */
    ONE_HALF_X_MINUS_1(27, x -> 1.5 * x - 1),
    /** |1.5x - 0.5| */
    ABS_ONE_HALF_X_MINUS_HALF(28, x -> Math.abs(1.5 * x - 0.5)),
    /** |1.5x - 1| */
    ABS_ONE_HALF_X_MINUS_1(29, x -> Math.abs(1.5 * x - 1)),
    /** Piecewise: 0 if x≤0.25, 1 if x≥0.57, else (x/0.32 - 0.78125) */
    STEP_1(30, x -> {
        if (x <= 0.25) return 0.0;
        if (x >= 0.57) return 1.0;
        return x / 0.32 - 0.78125;
    }),
    /** Piecewise: 0 if x≤0.42, 1 if x≥0.92, else 2x - 0.84 */
    STEP_2(31, x -> {
        if (x <= 0.42) return 0.0;
        if (x >= 0.92) return 1.0;
        return 2 * x - 0.84;
    }),
    /** Piecewise: 4x if x≤0.42, -2x+1.84 if x≤0.92, else x/0.08-11.5 */
    STEP_3(32, x -> {
        if (x <= 0.42) return 4 * x;
        if (x <= 0.92) return -2 * x + 1.84;
        return x / 0.08 - 11.5;
    }),
    /** |2x - 0.5| */
    ABS_DOUBLE_MINUS_HALF(33, x -> Math.abs(2 * x - 0.5)),
    /** 2x */
    DOUBLE(34, x -> 2 * x),
    /** 2x - 0.5 */
    DOUBLE_MINUS_HALF(35, x -> 2 * x - 0.5),
    /** 2x - 1 */
    DOUBLE_MINUS_1(36, x -> 2 * x - 1);

    private final int formulaNumber;
    private final ColorFunction function;

    ColorFormula(int formulaNumber, ColorFunction function) {
        this.formulaNumber = formulaNumber;
        this.function = function;
    }

    /**
     * Applies the formula to an input value.
     *
     * @param x Input value in [0, 1]
     * @return Output value in [0, 1]
     */
    public double apply(double x) {
        double result = function.apply(x);
        // Clamp to [0, 1]
        return Math.max(0.0, Math.min(1.0, result));
    }

    /**
     * Gets the formula number (0-36).
     */
    public int getFormulaNumber() {
        return formulaNumber;
    }

    /**
     * Gets a formula by its number (0-36).
     * Negative numbers invert the input before applying the formula.
     *
     * @param formulaNumber Formula number (-36 to 36)
     * @return ColorFormula instance
     * @throws IllegalArgumentException if formulaNumber is out of range
     */
    public static ColorFormula fromNumber(int formulaNumber) {
        int absNumber = Math.abs(formulaNumber);
        if (absNumber < 0 || absNumber > 36) {
            throw new IllegalArgumentException("Formula number must be in range [-36, 36], got: " + formulaNumber);
        }

        for (ColorFormula formula : values()) {
            if (formula.formulaNumber == absNumber) {
                return formula;
            }
        }
        throw new IllegalArgumentException("Unknown formula number: " + formulaNumber);
    }

    /**
     * Applies a formula (possibly with negative number) to a gray value.
     *
     * @param formulaNumber Formula number (-36 to 36)
     * @param gray Input gray value in [0, 1]
     * @return Transformed value in [0, 1]
     */
    public static double applyFormula(int formulaNumber, double gray) {
        // Clamp input
        gray = Math.max(0.0, Math.min(1.0, gray));

        // Handle negative formula numbers (invert input)
        if (formulaNumber < 0) {
            gray = 1.0 - gray;
            formulaNumber = -formulaNumber;
        }

        ColorFormula formula = fromNumber(formulaNumber);
        return formula.apply(gray);
    }

    @FunctionalInterface
    private interface ColorFunction {
        double apply(double x);
    }
}
