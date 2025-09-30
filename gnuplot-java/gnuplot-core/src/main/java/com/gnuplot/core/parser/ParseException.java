package com.gnuplot.core.parser;

/**
 * Exception thrown when expression parsing fails.
 *
 * <p>This exception is thrown by {@link ExpressionParser#parseOrThrow(String)}
 * when parsing fails. It contains a descriptive error message indicating what
 * went wrong and where (line and column numbers when available).
 */
public class ParseException extends RuntimeException {

    /**
     * Constructs a new parse exception with the specified detail message.
     *
     * @param message the detail message explaining the parse error
     */
    public ParseException(String message) {
        super(message);
    }

    /**
     * Constructs a new parse exception with the specified detail message and cause.
     *
     * @param message the detail message explaining the parse error
     * @param cause the cause of the parse exception
     */
    public ParseException(String message, Throwable cause) {
        super(message, cause);
    }
}