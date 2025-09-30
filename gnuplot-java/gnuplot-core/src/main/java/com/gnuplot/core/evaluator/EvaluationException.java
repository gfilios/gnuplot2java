package com.gnuplot.core.evaluator;

/**
 * Exception thrown when expression evaluation fails.
 *
 * <p>This exception is thrown when an error occurs during evaluation of an
 * expression, such as undefined variables, division by zero, or function
 * call errors.
 */
public class EvaluationException extends RuntimeException {

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
}