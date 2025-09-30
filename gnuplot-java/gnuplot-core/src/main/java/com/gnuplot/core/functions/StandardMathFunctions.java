package com.gnuplot.core.functions;

import com.gnuplot.core.evaluator.EvaluationContext;
import com.gnuplot.core.evaluator.MathFunction;

/**
 * Standard mathematical functions (trigonometric, logarithmic, etc.).
 *
 * <p>This class provides implementations of common mathematical functions
 * matching the behavior of C gnuplot.
 */
public class StandardMathFunctions {

    /**
     * Registers all standard math functions in the given context.
     *
     * @param context the evaluation context
     */
    public static void registerAll(EvaluationContext context) {
        // Trigonometric functions
        context.registerFunction("sin", MathFunction.withArgCount(1, args -> Math.sin(args[0])));
        context.registerFunction("cos", MathFunction.withArgCount(1, args -> Math.cos(args[0])));
        context.registerFunction("tan", MathFunction.withArgCount(1, args -> Math.tan(args[0])));
        context.registerFunction("asin", MathFunction.withArgCount(1, args -> Math.asin(args[0])));
        context.registerFunction("acos", MathFunction.withArgCount(1, args -> Math.acos(args[0])));
        context.registerFunction("atan", MathFunction.withArgCount(1, args -> Math.atan(args[0])));
        context.registerFunction("atan2", MathFunction.withArgCount(2, args -> Math.atan2(args[0], args[1])));

        // Hyperbolic functions
        context.registerFunction("sinh", MathFunction.withArgCount(1, args -> Math.sinh(args[0])));
        context.registerFunction("cosh", MathFunction.withArgCount(1, args -> Math.cosh(args[0])));
        context.registerFunction("tanh", MathFunction.withArgCount(1, args -> Math.tanh(args[0])));

        // Exponential and logarithmic functions
        context.registerFunction("exp", MathFunction.withArgCount(1, args -> Math.exp(args[0])));
        context.registerFunction("log", MathFunction.withArgCount(1, args -> Math.log(args[0])));
        context.registerFunction("log10", MathFunction.withArgCount(1, args -> Math.log10(args[0])));

        // Power and root functions
        context.registerFunction("sqrt", MathFunction.withArgCount(1, args -> Math.sqrt(args[0])));
        context.registerFunction("cbrt", MathFunction.withArgCount(1, args -> Math.cbrt(args[0])));
        context.registerFunction("pow", MathFunction.withArgCount(2, args -> Math.pow(args[0], args[1])));

        // Rounding functions
        context.registerFunction("abs", MathFunction.withArgCount(1, args -> Math.abs(args[0])));
        context.registerFunction("ceil", MathFunction.withArgCount(1, args -> Math.ceil(args[0])));
        context.registerFunction("floor", MathFunction.withArgCount(1, args -> Math.floor(args[0])));
        context.registerFunction("round", MathFunction.withArgCount(1, args -> (double) Math.round(args[0])));

        // Sign and comparison functions
        context.registerFunction("sgn", MathFunction.withArgCount(1, args ->
            args[0] > 0 ? 1.0 : (args[0] < 0 ? -1.0 : 0.0)));
        context.registerFunction("min", MathFunction.withMinArgCount(2, args -> {
            double min = args[0];
            for (int i = 1; i < args.length; i++) {
                min = Math.min(min, args[i]);
            }
            return min;
        }));
        context.registerFunction("max", MathFunction.withMinArgCount(2, args -> {
            double max = args[0];
            for (int i = 1; i < args.length; i++) {
                max = Math.max(max, args[i]);
            }
            return max;
        }));
    }

    private StandardMathFunctions() {
        // Utility class
    }
}