package com.gnuplot.core.functions;

import com.gnuplot.core.evaluator.EvaluationContext;
import com.gnuplot.core.evaluator.MathFunction;
import org.apache.commons.math3.special.Gamma;
import org.apache.commons.math3.special.Beta;

/**
 * Special mathematical functions (Gamma, Beta, etc.).
 *
 * <p>This class provides implementations of special mathematical functions
 * using Apache Commons Math library, matching the behavior of C gnuplot.
 */
public class SpecialFunctions {

    /**
     * Registers all special functions in the given context.
     *
     * @param context the evaluation context
     */
    public static void registerAll(EvaluationContext context) {
        // Gamma functions
        context.registerFunction("gamma", MathFunction.withArgCount(1, args ->
            Gamma.gamma(args[0])));

        context.registerFunction("lgamma", MathFunction.withArgCount(1, args ->
            Gamma.logGamma(args[0])));

        // Beta function: beta(a,b) = gamma(a) * gamma(b) / gamma(a+b)
        context.registerFunction("beta", MathFunction.withArgCount(2, args -> {
            double a = args[0];
            double b = args[1];
            return Math.exp(Gamma.logGamma(a) + Gamma.logGamma(b) - Gamma.logGamma(a + b));
        }));

        // Incomplete gamma functions
        // igamma(a, x) = regularizedGammaP(a, x) * gamma(a)
        context.registerFunction("igamma", MathFunction.withArgCount(2, args -> {
            double a = args[0];
            double x = args[1];
            return Gamma.regularizedGammaP(a, x) * Gamma.gamma(a);
        }));

        // Regularized incomplete gamma function P(a,x)
        context.registerFunction("gammainc", MathFunction.withArgCount(2, args ->
            Gamma.regularizedGammaP(args[0], args[1])));

        // Incomplete beta function
        context.registerFunction("ibeta", MathFunction.withArgCount(3, args -> {
            double a = args[0];
            double b = args[1];
            double x = args[2];
            double betaVal = Math.exp(Gamma.logGamma(a) + Gamma.logGamma(b) - Gamma.logGamma(a + b));
            return Beta.regularizedBeta(x, a, b) * betaVal;
        }));

        // Regularized incomplete beta function I_x(a,b)
        context.registerFunction("betainc", MathFunction.withArgCount(3, args ->
            Beta.regularizedBeta(args[2], args[0], args[1])));
    }

    private SpecialFunctions() {
        // Utility class
    }
}