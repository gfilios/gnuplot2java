package com.gnuplot.core.evaluator;

import java.util.HashMap;
import java.util.Map;

/**
 * Context for expression evaluation containing variables and functions.
 *
 * <p>This class manages the environment in which expressions are evaluated,
 * including variable bindings and function definitions.
 *
 * <h2>Usage Example:</h2>
 * <pre>{@code
 * EvaluationContext context = new EvaluationContext();
 * context.setVariable("x", 5.0);
 * context.setVariable("y", 3.0);
 *
 * Evaluator evaluator = new Evaluator(context);
 * double result = evaluator.evaluate(parser.parseOrThrow("x + y"));
 * // result = 8.0
 * }</pre>
 */
public class EvaluationContext {

    private final Map<String, Double> variables;
    private final Map<String, MathFunction> functions;

    /**
     * Creates a new empty evaluation context.
     */
    public EvaluationContext() {
        this.variables = new HashMap<>();
        this.functions = new HashMap<>();
        registerStandardConstants();
        registerStandardFunctions();
    }

    /**
     * Registers standard mathematical constants.
     */
    private void registerStandardConstants() {
        // Mathematical constants (match gnuplot naming)
        variables.put("pi", Math.PI);
        variables.put("e", Math.E);
    }

    /**
     * Registers standard mathematical functions.
     */
    private void registerStandardFunctions() {
        // Trigonometric functions
        registerFunction("sin", MathFunction.withArgCount(1, args -> Math.sin(args[0])));
        registerFunction("cos", MathFunction.withArgCount(1, args -> Math.cos(args[0])));
        registerFunction("tan", MathFunction.withArgCount(1, args -> Math.tan(args[0])));
        registerFunction("asin", MathFunction.withArgCount(1, args -> Math.asin(args[0])));
        registerFunction("acos", MathFunction.withArgCount(1, args -> Math.acos(args[0])));
        registerFunction("atan", MathFunction.withArgCount(1, args -> Math.atan(args[0])));
        registerFunction("atan2", MathFunction.withArgCount(2, args -> Math.atan2(args[0], args[1])));

        // Hyperbolic functions
        registerFunction("sinh", MathFunction.withArgCount(1, args -> Math.sinh(args[0])));
        registerFunction("cosh", MathFunction.withArgCount(1, args -> Math.cosh(args[0])));
        registerFunction("tanh", MathFunction.withArgCount(1, args -> Math.tanh(args[0])));

        // Exponential and logarithmic functions
        registerFunction("exp", MathFunction.withArgCount(1, args -> Math.exp(args[0])));
        registerFunction("log", MathFunction.withArgCount(1, args -> Math.log(args[0])));
        registerFunction("log10", MathFunction.withArgCount(1, args -> Math.log10(args[0])));
        registerFunction("sqrt", MathFunction.withArgCount(1, args -> Math.sqrt(args[0])));

        // Power and rounding functions
        registerFunction("abs", MathFunction.withArgCount(1, args -> Math.abs(args[0])));
        registerFunction("ceil", MathFunction.withArgCount(1, args -> Math.ceil(args[0])));
        registerFunction("floor", MathFunction.withArgCount(1, args -> Math.floor(args[0])));
        registerFunction("round", MathFunction.withArgCount(1, args -> (double) Math.round(args[0])));

        // Sign and comparison functions
        registerFunction("sgn", MathFunction.withArgCount(1, args -> Math.signum(args[0])));
        registerFunction("min", MathFunction.withMinArgCount(2, args -> {
            double min = args[0];
            for (int i = 1; i < args.length; i++) {
                min = Math.min(min, args[i]);
            }
            return min;
        }));
        registerFunction("max", MathFunction.withMinArgCount(2, args -> {
            double max = args[0];
            for (int i = 1; i < args.length; i++) {
                max = Math.max(max, args[i]);
            }
            return max;
        }));

        // Bessel functions (used in simple.dem)
        // For now, use Apache Commons Math approximations or simplified versions
        registerFunction("besj0", MathFunction.withArgCount(1, args -> besselJ0(args[0])));
        registerFunction("besj1", MathFunction.withArgCount(1, args -> besselJ1(args[0])));

        // Complex number functions (treat as real-only for now)
        registerFunction("real", MathFunction.withArgCount(1, args -> args[0]));
        registerFunction("imag", MathFunction.withArgCount(1, args -> 0.0));

        // Random function
        registerFunction("rand", MathFunction.withArgCount(1, args -> Math.random() * args[0]));
    }

    /**
     * Approximation of Bessel function J0(x).
     * Based on polynomial approximation for |x| <= 3.
     */
    private double besselJ0(double x) {
        double ax = Math.abs(x);
        if (ax < 8.0) {
            double y = x * x;
            double ans1 = 57568490574.0 + y * (-13362590354.0 + y * (651619640.7
                    + y * (-11214424.18 + y * (77392.33017 + y * (-184.9052456)))));
            double ans2 = 57568490411.0 + y * (1029532985.0 + y * (9494680.718
                    + y * (59272.64853 + y * (267.8532712 + y * 1.0))));
            return ans1 / ans2;
        } else {
            double z = 8.0 / ax;
            double y = z * z;
            double xx = ax - 0.785398164;
            double ans1 = 1.0 + y * (-0.1098628627e-2 + y * (0.2734510407e-4
                    + y * (-0.2073370639e-5 + y * 0.2093887211e-6)));
            double ans2 = -0.1562499995e-1 + y * (0.1430488765e-3
                    + y * (-0.6911147651e-5 + y * (0.7621095161e-6
                    - y * 0.934935152e-7)));
            return Math.sqrt(0.636619772 / ax) * (Math.cos(xx) * ans1 - z * Math.sin(xx) * ans2);
        }
    }

    /**
     * Approximation of Bessel function J1(x).
     * Based on polynomial approximation for |x| <= 3.
     */
    private double besselJ1(double x) {
        double ax = Math.abs(x);
        if (ax < 8.0) {
            double y = x * x;
            double ans1 = x * (72362614232.0 + y * (-7895059235.0 + y * (242396853.1
                    + y * (-2972611.439 + y * (15704.48260 + y * (-30.16036606))))));
            double ans2 = 144725228442.0 + y * (2300535178.0 + y * (18583304.74
                    + y * (99447.43394 + y * (376.9991397 + y * 1.0))));
            return ans1 / ans2;
        } else {
            double z = 8.0 / ax;
            double y = z * z;
            double xx = ax - 2.356194491;
            double ans1 = 1.0 + y * (0.183105e-2 + y * (-0.3516396496e-4
                    + y * (0.2457520174e-5 + y * (-0.240337019e-6))));
            double ans2 = 0.04687499995 + y * (-0.2002690873e-3
                    + y * (0.8449199096e-5 + y * (-0.88228987e-6
                    + y * 0.105787412e-6)));
            double ans = Math.sqrt(0.636619772 / ax) * (Math.cos(xx) * ans1 - z * Math.sin(xx) * ans2);
            return x < 0.0 ? -ans : ans;
        }
    }

    /**
     * Sets a variable value.
     *
     * @param name the variable name
     * @param value the variable value
     */
    public void setVariable(String name, double value) {
        variables.put(name, value);
    }

    /**
     * Gets a variable value.
     *
     * @param name the variable name
     * @return the variable value
     * @throws IllegalArgumentException if variable is not defined
     */
    public double getVariable(String name) {
        if (!variables.containsKey(name)) {
            throw new IllegalArgumentException("Undefined variable: " + name);
        }
        return variables.get(name);
    }

    /**
     * Checks if a variable is defined.
     *
     * @param name the variable name
     * @return true if the variable is defined
     */
    public boolean hasVariable(String name) {
        return variables.containsKey(name);
    }

    /**
     * Removes a variable.
     *
     * @param name the variable name
     */
    public void removeVariable(String name) {
        variables.remove(name);
    }

    /**
     * Clears all variables (except constants).
     */
    public void clearVariables() {
        variables.clear();
        registerStandardConstants();
    }

    /**
     * Registers a function.
     *
     * @param name the function name
     * @param function the function implementation
     */
    public void registerFunction(String name, MathFunction function) {
        functions.put(name, function);
    }

    /**
     * Gets a function.
     *
     * @param name the function name
     * @return the function
     * @throws IllegalArgumentException if function is not defined
     */
    public MathFunction getFunction(String name) {
        if (!functions.containsKey(name)) {
            throw new IllegalArgumentException("Undefined function: " + name);
        }
        return functions.get(name);
    }

    /**
     * Checks if a function is defined.
     *
     * @param name the function name
     * @return true if the function is defined
     */
    public boolean hasFunction(String name) {
        return functions.containsKey(name);
    }

    /**
     * Removes a function.
     *
     * @param name the function name
     */
    public void removeFunction(String name) {
        functions.remove(name);
    }

    /**
     * Clears all functions.
     */
    public void clearFunctions() {
        functions.clear();
    }
}