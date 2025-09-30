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