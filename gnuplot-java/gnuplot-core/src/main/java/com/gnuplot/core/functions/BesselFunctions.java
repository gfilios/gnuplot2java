package com.gnuplot.core.functions;

import com.gnuplot.core.evaluator.EvaluationContext;
import com.gnuplot.core.evaluator.MathFunction;
import org.apache.commons.math3.special.BesselJ;

/**
 * Bessel functions of the first and second kind.
 *
 * <p>This class provides implementations of Bessel functions using Apache
 * Commons Math library, matching the behavior of C gnuplot.
 */
public class BesselFunctions {

    /**
     * Registers all Bessel functions in the given context.
     *
     * @param context the evaluation context
     */
    public static void registerAll(EvaluationContext context) {
        // Bessel functions of the first kind (J)
        // J_n(-x) = (-1)^n * J_n(x)
        context.registerFunction("besj0", MathFunction.withArgCount(1, args -> {
            double x = args[0];
            BesselJ besselJ = new BesselJ(0);
            return besselJ.value(Math.abs(x));  // J_0 is even
        }));

        context.registerFunction("besj1", MathFunction.withArgCount(1, args -> {
            double x = args[0];
            BesselJ besselJ = new BesselJ(1);
            if (x < 0) {
                return -besselJ.value(-x);  // J_1 is odd
            }
            return besselJ.value(x);
        }));

        context.registerFunction("besjn", MathFunction.withArgCount(2, args -> {
            int n = (int) args[0];
            double x = args[1];
            BesselJ besselJ = new BesselJ(n);
            if (x < 0) {
                // J_n(-x) = (-1)^n * J_n(x)
                double result = besselJ.value(-x);
                return (n % 2 == 0) ? result : -result;
            }
            return besselJ.value(x);
        }));

        // Note: Apache Commons Math 3.6.1 does not provide Bessel Y functions
        // or modified Bessel I functions directly. These would need to be
        // implemented using algorithms from literature or a different library.
        // For now, we'll provide stub implementations that throw exceptions.

        context.registerFunction("besy0", MathFunction.withArgCount(1, args -> {
            throw new UnsupportedOperationException(
                "Bessel Y functions not yet implemented - requires additional library or custom implementation");
        }));

        context.registerFunction("besy1", MathFunction.withArgCount(1, args -> {
            throw new UnsupportedOperationException(
                "Bessel Y functions not yet implemented - requires additional library or custom implementation");
        }));

        context.registerFunction("besyn", MathFunction.withArgCount(2, args -> {
            throw new UnsupportedOperationException(
                "Bessel Y functions not yet implemented - requires additional library or custom implementation");
        }));

        context.registerFunction("besi0", MathFunction.withArgCount(1, args -> {
            throw new UnsupportedOperationException(
                "Modified Bessel I functions not yet implemented - requires additional library or custom implementation");
        }));

        context.registerFunction("besi1", MathFunction.withArgCount(1, args -> {
            throw new UnsupportedOperationException(
                "Modified Bessel I functions not yet implemented - requires additional library or custom implementation");
        }));
    }

    private BesselFunctions() {
        // Utility class
    }
}