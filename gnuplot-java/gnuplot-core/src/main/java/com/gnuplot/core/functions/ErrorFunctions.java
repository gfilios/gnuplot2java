package com.gnuplot.core.functions;

import com.gnuplot.core.evaluator.EvaluationContext;
import com.gnuplot.core.evaluator.MathFunction;
import org.apache.commons.math3.special.Erf;

/**
 * Error functions and complementary error functions.
 *
 * <p>Provides implementations for:
 * <ul>
 *   <li>erf(x) - Error function</li>
 *   <li>erfc(x) - Complementary error function</li>
 *   <li>inverf(x) - Inverse error function</li>
 *   <li>inverfc(x) - Inverse complementary error function (not in gnuplot 6.0.3)</li>
 * </ul>
 */
public final class ErrorFunctions {

    private ErrorFunctions() {
        // Utility class
    }

    /**
     * Registers all error functions with the given evaluation context.
     *
     * @param context the evaluation context to register functions with
     */
    public static void registerAll(EvaluationContext context) {
        // Error function: erf(x) = (2/sqrt(pi)) * integral from 0 to x of e^(-t^2) dt
        context.registerFunction("erf", MathFunction.withArgCount(1, args ->
                Erf.erf(args[0])
        ));

        // Complementary error function: erfc(x) = 1 - erf(x)
        context.registerFunction("erfc", MathFunction.withArgCount(1, args ->
                Erf.erfc(args[0])
        ));

        // Inverse error function
        context.registerFunction("inverf", MathFunction.withArgCount(1, args ->
                Erf.erfInv(args[0])
        ));

        // Inverse complementary error function (not in gnuplot 6.0.3, but available in Commons Math)
        context.registerFunction("inverfc", MathFunction.withArgCount(1, args ->
                Erf.erfcInv(args[0])
        ));
    }
}