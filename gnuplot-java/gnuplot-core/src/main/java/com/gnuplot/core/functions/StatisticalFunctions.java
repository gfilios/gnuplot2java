package com.gnuplot.core.functions;

import com.gnuplot.core.evaluator.EvaluationContext;
import com.gnuplot.core.evaluator.MathFunction;
import org.apache.commons.math3.distribution.NormalDistribution;

/**
 * Statistical distribution functions.
 *
 * <p>Provides implementations for:
 * <ul>
 *   <li>norm(x) - Normal (Gaussian) cumulative distribution function (CDF)</li>
 *   <li>invnorm(p) - Inverse normal CDF (quantile function)</li>
 * </ul>
 *
 * <p>Uses Apache Commons Math for distribution implementations.
 * All distributions use standard parameterization unless otherwise noted.
 */
public final class StatisticalFunctions {

    private StatisticalFunctions() {
        // Utility class
    }

    /**
     * Registers all statistical functions with the given evaluation context.
     *
     * @param context the evaluation context to register functions with
     */
    public static void registerAll(EvaluationContext context) {
        // Standard normal distribution (mean=0, stddev=1)
        NormalDistribution standardNormal = new NormalDistribution(0, 1);

        // Normal distribution CDF: norm(x) = P(X <= x) for X ~ N(0,1)
        context.registerFunction("norm", MathFunction.withArgCount(1, args ->
                standardNormal.cumulativeProbability(args[0])
        ));

        // Inverse normal CDF: invnorm(p) = x such that P(X <= x) = p
        context.registerFunction("invnorm", MathFunction.withArgCount(1, args ->
                standardNormal.inverseCumulativeProbability(args[0])
        ));
    }
}