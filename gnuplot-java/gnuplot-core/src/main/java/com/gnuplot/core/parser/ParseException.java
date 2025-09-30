package com.gnuplot.core.parser;

import com.gnuplot.core.GnuplotException;
import com.gnuplot.core.ast.SourceLocation;

/**
 * Exception thrown when expression parsing fails.
 *
 * <p>This exception is thrown by {@link ExpressionParser#parseOrThrow(String)}
 * when parsing fails. It contains a descriptive error message indicating what
 * went wrong and where (line and column numbers when available).
 *
 * <p>The exception includes helpful context such as:
 * <ul>
 *   <li>Line and column numbers of the error</li>
 *   <li>Visual pointer to the error location</li>
 *   <li>Suggestions for common parsing mistakes</li>
 * </ul>
 */
public class ParseException extends GnuplotException {

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

    /**
     * Constructs a new parse exception with full context.
     *
     * @param message the error message
     * @param location the source location where error occurred
     * @param expression the expression being parsed
     * @param suggestion a helpful suggestion for fixing the error
     */
    public ParseException(String message, SourceLocation location, String expression, String suggestion) {
        super(message, null, location, expression, suggestion);
    }

    /**
     * Creates a parse exception for unexpected token errors.
     *
     * @param expected description of what was expected
     * @param found the token that was actually found
     * @param location the location of the error
     * @param expression the expression being parsed
     * @return a new ParseException with helpful context
     */
    public static ParseException unexpectedToken(String expected, String found,
                                                 SourceLocation location, String expression) {
        String message = String.format("Expected %s but found '%s'", expected, found);
        String suggestion = suggestFixForUnexpectedToken(expected, found);
        return new ParseException(message, location, expression, suggestion);
    }

    /**
     * Creates a parse exception for mismatched parentheses.
     *
     * @param location the location of the error
     * @param expression the expression being parsed
     * @return a new ParseException with helpful context
     */
    public static ParseException mismatchedParentheses(SourceLocation location, String expression) {
        return new ParseException("Mismatched parentheses", location, expression,
                "Check that every opening parenthesis '(' has a matching closing parenthesis ')'");
    }

    /**
     * Suggests a fix based on the expected and found tokens.
     */
    private static String suggestFixForUnexpectedToken(String expected, String found) {
        if (found.equals("EOF") || found.equals("<EOF>")) {
            return "The expression appears to be incomplete. " + expected + " is missing.";
        }
        if (expected.contains("operator") && found.matches("[a-zA-Z]+")) {
            return "Did you forget an operator between expressions?";
        }
        if (expected.contains("expression") && found.matches("[+\\-*/^%]")) {
            return "Operator '" + found + "' requires an expression on both sides.";
        }
        return null;
    }
}