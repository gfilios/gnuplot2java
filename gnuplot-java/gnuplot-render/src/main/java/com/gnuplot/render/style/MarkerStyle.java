package com.gnuplot.render.style;

import com.gnuplot.render.color.Color;

import java.util.Locale;
import java.util.Objects;

/**
 * Represents the styling for a scatter plot marker (point).
 * Combines point shape, size, color, and fill properties.
 *
 * @param size The size of the marker in pixels (radius or half-width)
 * @param color The color of the marker
 * @param pointStyle The shape of the marker
 * @param filled Whether the marker should be filled
 */
public record MarkerStyle(double size, Color color, PointStyle pointStyle, boolean filled) {

    /**
     * Validates marker style parameters.
     */
    public MarkerStyle {
        Objects.requireNonNull(color, "color cannot be null");
        Objects.requireNonNull(pointStyle, "pointStyle cannot be null");
        if (size <= 0) {
            throw new IllegalArgumentException("size must be positive, got: " + size);
        }
    }

    /**
     * Creates a filled marker with the specified properties.
     *
     * @param size Marker size in pixels
     * @param color Marker color
     * @param pointStyle Marker shape
     * @return Filled marker style
     */
    public static MarkerStyle filled(double size, Color color, PointStyle pointStyle) {
        return new MarkerStyle(size, color, pointStyle, true);
    }

    /**
     * Creates an unfilled (outline) marker with the specified properties.
     *
     * @param size Marker size in pixels
     * @param color Marker color
     * @param pointStyle Marker shape
     * @return Unfilled marker style
     */
    public static MarkerStyle unfilled(double size, Color color, PointStyle pointStyle) {
        return new MarkerStyle(size, color, pointStyle, false);
    }

    /**
     * Creates a marker with default fill based on point style.
     *
     * @param size Marker size in pixels
     * @param color Marker color
     * @param pointStyle Marker shape
     * @return Marker style with default fill
     */
    public static MarkerStyle withDefaultFill(double size, Color color, PointStyle pointStyle) {
        return new MarkerStyle(size, color, pointStyle, pointStyle.isFilledByDefault());
    }

    /**
     * Returns a new marker style with a different size.
     *
     * @param newSize New marker size
     * @return Modified marker style
     */
    public MarkerStyle withSize(double newSize) {
        return new MarkerStyle(newSize, color, pointStyle, filled);
    }

    /**
     * Returns a new marker style with a different color.
     *
     * @param newColor New marker color
     * @return Modified marker style
     */
    public MarkerStyle withColor(Color newColor) {
        return new MarkerStyle(size, newColor, pointStyle, filled);
    }

    /**
     * Returns a new marker style with a different point style.
     *
     * @param newPointStyle New point style
     * @return Modified marker style
     */
    public MarkerStyle withPointStyle(PointStyle newPointStyle) {
        return new MarkerStyle(size, color, newPointStyle, filled);
    }

    /**
     * Returns a new marker style with inverted fill.
     *
     * @return Modified marker style
     */
    public MarkerStyle withFilled(boolean newFilled) {
        return new MarkerStyle(size, color, pointStyle, newFilled);
    }

    /**
     * Converts the color to a hex string for SVG.
     *
     * @return Hex color string (e.g., "#FF0000")
     */
    public String getColorHex() {
        return String.format("#%06X", color.toRGB24());
    }

    /**
     * Gets the stroke width for unfilled markers.
     *
     * @return Stroke width in pixels
     */
    public double getStrokeWidth() {
        return Math.max(1.0, size / 6.0);
    }

    /**
     * Predefined marker styles.
     */
    public static final MarkerStyle DEFAULT = filled(4.0, Color.BLACK, PointStyle.CIRCLE);
    public static final MarkerStyle SMALL = filled(2.0, Color.BLACK, PointStyle.CIRCLE);
    public static final MarkerStyle LARGE = filled(8.0, Color.BLACK, PointStyle.CIRCLE);
    public static final MarkerStyle RED_CIRCLE = filled(4.0, Color.RED, PointStyle.CIRCLE);
    public static final MarkerStyle BLUE_SQUARE = filled(4.0, Color.BLUE, PointStyle.SQUARE);
    public static final MarkerStyle GREEN_TRIANGLE = filled(4.0, Color.GREEN, PointStyle.TRIANGLE_UP);

    @Override
    public String toString() {
        return String.format(Locale.US, "MarkerStyle{size=%.1f, color=%s, style=%s, filled=%b}",
                size, getColorHex(), pointStyle, filled);
    }
}
