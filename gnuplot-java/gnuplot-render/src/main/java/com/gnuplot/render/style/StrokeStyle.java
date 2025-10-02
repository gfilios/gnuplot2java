package com.gnuplot.render.style;

import com.gnuplot.render.color.Color;

/**
 * Defines the appearance of a line stroke (width, color, style).
 * Immutable record for line rendering properties.
 *
 * @param width Line width in pixels
 * @param color Line color
 * @param lineStyle Line dash pattern
 */
public record StrokeStyle(double width, Color color, LineStyle lineStyle) {

    /**
     * Validates stroke parameters.
     */
    public StrokeStyle {
        if (width <= 0) {
            throw new IllegalArgumentException("Stroke width must be positive, got: " + width);
        }
        if (color == null) {
            throw new IllegalArgumentException("Color cannot be null");
        }
        if (lineStyle == null) {
            throw new IllegalArgumentException("LineStyle cannot be null");
        }
    }

    /**
     * Creates a solid stroke with the given width and color.
     */
    public static StrokeStyle solid(double width, Color color) {
        return new StrokeStyle(width, color, LineStyle.SOLID);
    }

    /**
     * Creates a dashed stroke with the given width and color.
     */
    public static StrokeStyle dashed(double width, Color color) {
        return new StrokeStyle(width, color, LineStyle.DASHED);
    }

    /**
     * Creates a dotted stroke with the given width and color.
     */
    public static StrokeStyle dotted(double width, Color color) {
        return new StrokeStyle(width, color, LineStyle.DOTTED);
    }

    /**
     * Creates a new stroke with a different width.
     */
    public StrokeStyle withWidth(double newWidth) {
        return new StrokeStyle(newWidth, color, lineStyle);
    }

    /**
     * Creates a new stroke with a different color.
     */
    public StrokeStyle withColor(Color newColor) {
        return new StrokeStyle(width, newColor, lineStyle);
    }

    /**
     * Creates a new stroke with a different line style.
     */
    public StrokeStyle withLineStyle(LineStyle newLineStyle) {
        return new StrokeStyle(width, color, newLineStyle);
    }

    /**
     * Converts to SVG stroke attributes.
     */
    public String toSvgAttributes() {
        StringBuilder attrs = new StringBuilder();

        // Stroke color
        attrs.append("stroke=\"#").append(String.format("%06X", color.toRGB24())).append("\" ");

        // Stroke width
        attrs.append("stroke-width=\"").append(width).append("\" ");

        // Dash array
        if (!lineStyle.isSolid()) {
            attrs.append("stroke-dasharray=\"").append(lineStyle.getSvgDashArray()).append("\" ");
        }

        // Line cap and join for smooth rendering
        attrs.append("stroke-linecap=\"round\" stroke-linejoin=\"round\"");

        return attrs.toString();
    }

    // Common default strokes
    public static final StrokeStyle DEFAULT = solid(1.0, Color.BLACK);
    public static final StrokeStyle THIN = solid(0.5, Color.BLACK);
    public static final StrokeStyle THICK = solid(2.0, Color.BLACK);
    public static final StrokeStyle AXIS = solid(1.0, Color.BLACK);
    public static final StrokeStyle GRID = dashed(0.5, Color.fromRGB24(0xCCCCCC));
}
