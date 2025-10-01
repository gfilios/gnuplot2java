package com.gnuplot.core.data.csv;

import com.gnuplot.core.GnuplotException;

/**
 * Exception thrown when CSV parsing fails.
 *
 * @since 1.0
 */
public class CsvParseException extends GnuplotException {

    /**
     * Creates a new CSV parse exception.
     *
     * @param message error message
     */
    public CsvParseException(String message) {
        super(message);
    }

    /**
     * Creates a new CSV parse exception with a cause.
     *
     * @param message error message
     * @param cause   underlying cause
     */
    public CsvParseException(String message, Throwable cause) {
        super(message, cause);
    }
}
