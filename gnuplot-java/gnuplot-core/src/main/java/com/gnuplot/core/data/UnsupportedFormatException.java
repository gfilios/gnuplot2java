package com.gnuplot.core.data;

/**
 * Exception thrown when attempting to read data in an unsupported format.
 *
 * @since 1.0
 */
public class UnsupportedFormatException extends RuntimeException {

    /**
     * Constructs a new exception with the specified detail message.
     *
     * @param message the detail message
     */
    public UnsupportedFormatException(String message) {
        super(message);
    }

    /**
     * Constructs a new exception with the specified detail message and cause.
     *
     * @param message the detail message
     * @param cause   the cause
     */
    public UnsupportedFormatException(String message, Throwable cause) {
        super(message, cause);
    }
}
