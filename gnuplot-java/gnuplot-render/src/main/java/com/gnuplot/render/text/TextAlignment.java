package com.gnuplot.render.text;

/**
 * Text alignment options for positioning text relative to a point.
 */
public enum TextAlignment {
    /** Align text to the left */
    LEFT,
    /** Center text horizontally */
    CENTER,
    /** Align text to the right */
    RIGHT;

    /**
     * Calculates the x-offset for aligned text.
     *
     * @param textWidth Width of the text
     * @return X-offset from the anchor point
     */
    public double getXOffset(double textWidth) {
        return switch (this) {
            case LEFT -> 0;
            case CENTER -> -textWidth / 2.0;
            case RIGHT -> -textWidth;
        };
    }
}
