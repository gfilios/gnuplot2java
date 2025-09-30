package com.gnuplot.core;

import com.gnuplot.core.ast.SourceLocation;

import java.util.Optional;

/**
 * Base exception for all gnuplot-related errors.
 *
 * <p>This is the root exception class in the gnuplot exception hierarchy.
 * It provides common functionality for error reporting including:
 * <ul>
 *   <li>Error context (source location, expression)</li>
 *   <li>Helpful suggestions for fixing the error</li>
 *   <li>Formatted error messages with line/column information</li>
 * </ul>
 */
public class GnuplotException extends RuntimeException {

    private final SourceLocation location;
    private final String expression;
    private final String suggestion;

    /**
     * Constructs a new gnuplot exception with the specified message.
     *
     * @param message the error message
     */
    public GnuplotException(String message) {
        this(message, null, null, null, null);
    }

    /**
     * Constructs a new gnuplot exception with message and cause.
     *
     * @param message the error message
     * @param cause the underlying cause
     */
    public GnuplotException(String message, Throwable cause) {
        this(message, cause, null, null, null);
    }

    /**
     * Constructs a new gnuplot exception with full context.
     *
     * @param message the error message
     * @param cause the underlying cause (may be null)
     * @param location the source location where error occurred (may be null)
     * @param expression the expression being evaluated (may be null)
     * @param suggestion a helpful suggestion for fixing the error (may be null)
     */
    public GnuplotException(String message, Throwable cause, SourceLocation location,
                           String expression, String suggestion) {
        super(formatMessage(message, location, expression, suggestion), cause);
        this.location = location;
        this.expression = expression;
        this.suggestion = suggestion;
    }

    /**
     * Gets the source location where the error occurred.
     *
     * @return the source location, or empty if not available
     */
    public Optional<SourceLocation> getLocation() {
        return Optional.ofNullable(location);
    }

    /**
     * Gets the expression that caused the error.
     *
     * @return the expression, or empty if not available
     */
    public Optional<String> getExpression() {
        return Optional.ofNullable(expression);
    }

    /**
     * Gets a helpful suggestion for fixing the error.
     *
     * @return the suggestion, or empty if not available
     */
    public Optional<String> getSuggestion() {
        return Optional.ofNullable(suggestion);
    }

    /**
     * Formats an error message with context information.
     *
     * @param message the base error message
     * @param location the source location (may be null)
     * @param expression the expression (may be null)
     * @param suggestion the suggestion (may be null)
     * @return formatted error message
     */
    private static String formatMessage(String message, SourceLocation location,
                                       String expression, String suggestion) {
        StringBuilder sb = new StringBuilder();

        // Main error message
        sb.append(message);

        // Add location if available
        if (location != null) {
            sb.append(" at line ").append(location.line())
              .append(", column ").append(location.column());
        }

        // Add expression context if available
        if (expression != null && !expression.isEmpty()) {
            sb.append("\n  Expression: ").append(expression);

            // Add visual pointer to error location
            if (location != null && location.column() > 0) {
                sb.append("\n  ")
                  .append(" ".repeat(13 + location.column() - 1)) // "  Expression: " = 13 chars
                  .append("^");
            }
        }

        // Add suggestion if available
        if (suggestion != null && !suggestion.isEmpty()) {
            sb.append("\n  Suggestion: ").append(suggestion);
        }

        return sb.toString();
    }
}