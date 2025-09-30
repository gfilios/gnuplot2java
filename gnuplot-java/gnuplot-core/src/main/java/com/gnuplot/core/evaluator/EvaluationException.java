package com.gnuplot.core.evaluator;

import com.gnuplot.core.GnuplotException;
import com.gnuplot.core.ast.SourceLocation;

/**
 * Exception thrown when expression evaluation fails.
 *
 * <p>This exception is thrown when an error occurs during evaluation of an
 * expression, such as undefined variables, division by zero, or function
 * call errors.
 *
 * <p>The exception includes helpful context such as:
 * <ul>
 *   <li>Location where the error occurred in the expression</li>
 *   <li>The expression being evaluated</li>
 *   <li>Suggestions for fixing common evaluation errors</li>
 * </ul>
 */
public class EvaluationException extends GnuplotException {

    /**
     * Constructs a new evaluation exception with the specified detail message.
     *
     * @param message the detail message explaining the evaluation error
     */
    public EvaluationException(String message) {
        super(message);
    }

    /**
     * Constructs a new evaluation exception with the specified detail message and cause.
     *
     * @param message the detail message explaining the evaluation error
     * @param cause the cause of the evaluation exception
     */
    public EvaluationException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a new evaluation exception with full context.
     *
     * @param message the error message
     * @param location the source location where error occurred
     * @param expression the expression being evaluated
     * @param suggestion a helpful suggestion for fixing the error
     */
    public EvaluationException(String message, SourceLocation location, String expression, String suggestion) {
        super(message, null, location, expression, suggestion);
    }

    /**
     * Creates an evaluation exception for undefined variables.
     *
     * @param variableName the name of the undefined variable
     * @param location the location of the error
     * @param expression the expression being evaluated
     * @return a new EvaluationException with helpful context
     */
    public static EvaluationException undefinedVariable(String variableName, SourceLocation location, String expression) {
        String message = String.format("Undefined variable: '%s'", variableName);
        String suggestion = "Make sure the variable is defined before using it, or check for typos";
        return new EvaluationException(message, location, expression, suggestion);
    }

    /**
     * Creates an evaluation exception for undefined functions.
     *
     * @param functionName the name of the undefined function
     * @param location the location of the error
     * @param expression the expression being evaluated
     * @return a new EvaluationException with helpful context
     */
    public static EvaluationException undefinedFunction(String functionName, SourceLocation location, String expression) {
        String message = String.format("Undefined function: '%s'", functionName);
        String suggestion = String.format("Function '%s' is not defined. Check spelling or ensure it's registered", functionName);
        return new EvaluationException(message, location, expression, suggestion);
    }

    /**
     * Creates an evaluation exception for division by zero.
     *
     * @param location the location of the error
     * @param expression the expression being evaluated
     * @return a new EvaluationException with helpful context
     */
    public static EvaluationException divisionByZero(SourceLocation location, String expression) {
        return new EvaluationException("Division by zero", location, expression,
                "Check that divisor is not zero, or add a conditional check");
    }

    /**
     * Creates an evaluation exception for invalid function argument count.
     *
     * @param functionName the function name
     * @param expected the expected argument count
     * @param actual the actual argument count
     * @param location the location of the error
     * @param expression the expression being evaluated
     * @return a new EvaluationException with helpful context
     */
    public static EvaluationException invalidArgumentCount(String functionName, int expected, int actual,
                                                          SourceLocation location, String expression) {
        String message = String.format("Function '%s' expects %d argument(s) but got %d",
                functionName, expected, actual);
        String suggestion = String.format("Call '%s' with exactly %d argument(s)", functionName, expected);
        return new EvaluationException(message, location, expression, suggestion);
    }

    /**
     * Creates an evaluation exception for domain errors (invalid input).
     *
     * @param functionName the function name
     * @param reason why the input is invalid
     * @param location the location of the error
     * @param expression the expression being evaluated
     * @return a new EvaluationException with helpful context
     */
    public static EvaluationException domainError(String functionName, String reason,
                                                 SourceLocation location, String expression) {
        String message = String.format("Domain error in %s: %s", functionName, reason);
        return new EvaluationException(message, location, expression, null);
    }
}