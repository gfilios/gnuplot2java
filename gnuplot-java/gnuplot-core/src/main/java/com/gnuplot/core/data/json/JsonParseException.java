package com.gnuplot.core.data.json;

import com.gnuplot.core.GnuplotException;

/**
 * Exception thrown when JSON parsing fails.
 *
 * @since 1.0
 */
public class JsonParseException extends GnuplotException {

    /**
     * Creates a new JSON parse exception.
     *
     * @param message error message
     */
    public JsonParseException(String message) {
        super(message);
    }

    /**
     * Creates a new JSON parse exception with a cause.
     *
     * @param message error message
     * @param cause   underlying cause
     */
    public JsonParseException(String message, Throwable cause) {
        super(message, cause);
    }
}
