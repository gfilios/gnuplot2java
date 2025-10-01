package com.gnuplot.render;

/**
 * Exception thrown when rendering fails.
 *
 * @since 1.0
 */
public class RenderException extends Exception {

    private static final long serialVersionUID = 1L;

    public RenderException(String message) {
        super(message);
    }

    public RenderException(String message, Throwable cause) {
        super(message, cause);
    }

    public RenderException(Throwable cause) {
        super(cause);
    }
}
