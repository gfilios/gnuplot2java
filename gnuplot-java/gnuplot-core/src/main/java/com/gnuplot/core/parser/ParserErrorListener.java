package com.gnuplot.core.parser;

import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;

/**
 * Custom error listener for ANTLR parser that collects error messages.
 *
 * <p>This listener accumulates syntax errors encountered during parsing
 * and formats them into user-friendly error messages with line and column
 * information.
 */
class ParserErrorListener extends BaseErrorListener {

    private final StringBuilder errorMessages = new StringBuilder();
    private boolean hasErrors = false;

    @Override
    public void syntaxError(
            Recognizer<?, ?> recognizer,
            Object offendingSymbol,
            int line,
            int charPositionInLine,
            String msg,
            RecognitionException e
    ) {
        hasErrors = true;

        if (errorMessages.length() > 0) {
            errorMessages.append("\n");
        }

        errorMessages.append(String.format(
                "Syntax error at line %d, column %d: %s",
                line,
                charPositionInLine,
                msg
        ));
    }

    /**
     * Returns whether any errors were encountered.
     *
     * @return true if errors occurred, false otherwise
     */
    public boolean hasErrors() {
        return hasErrors;
    }

    /**
     * Returns the accumulated error message.
     *
     * @return the error message, or empty string if no errors
     */
    public String getErrorMessage() {
        return errorMessages.toString();
    }
}