package com.gnuplot.render.text;

/**
 * Represents the measured dimensions of rendered text.
 * Provides width, height, and baseline information for text layout.
 *
 * @param width Text width in pixels
 * @param height Text height in pixels (ascent + descent)
 * @param ascent Distance from baseline to top of tallest character
 * @param descent Distance from baseline to bottom of lowest character
 */
public record TextMetrics(double width, double height, double ascent, double descent) {

    /**
     * Validates text metrics.
     */
    public TextMetrics {
        if (width < 0) {
            throw new IllegalArgumentException("Width cannot be negative: " + width);
        }
        if (height < 0) {
            throw new IllegalArgumentException("Height cannot be negative: " + height);
        }
        if (ascent < 0) {
            throw new IllegalArgumentException("Ascent cannot be negative: " + ascent);
        }
        if (descent < 0) {
            throw new IllegalArgumentException("Descent cannot be negative: " + descent);
        }
    }

    /**
     * Gets the baseline position from the top of the text.
     */
    public double baseline() {
        return ascent;
    }

    /**
     * Creates text metrics with zero dimensions.
     */
    public static TextMetrics empty() {
        return new TextMetrics(0, 0, 0, 0);
    }
}
