package com.gnuplot.render.style;

/**
 * Point marker styles for scatter plots.
 * Corresponds to gnuplot's point types.
 */
public enum PointStyle {
    /** Circular point (filled) */
    CIRCLE,

    /** Square point */
    SQUARE,

    /** Triangular point (pointing up) */
    TRIANGLE_UP,

    /** Triangular point (pointing down) */
    TRIANGLE_DOWN,

    /** Diamond point */
    DIAMOND,

    /** Plus sign (+) */
    PLUS,

    /** Cross sign (×) */
    CROSS,

    /** Star point (5-pointed) */
    STAR,

    /** Hexagon point */
    HEXAGON,

    /** Pentagon point */
    PENTAGON;

    /**
     * Gets the number of sides for regular polygons.
     * Used for rendering polygon-based markers.
     *
     * @return Number of sides, or -1 for non-polygon shapes
     */
    public int getSides() {
        return switch (this) {
            case TRIANGLE_UP, TRIANGLE_DOWN -> 3;
            case SQUARE, DIAMOND -> 4;
            case PENTAGON -> 5;
            case HEXAGON -> 6;
            default -> -1;
        };
    }

    /**
     * Checks if this point style is a filled shape.
     *
     * @return true if the style should be filled by default
     */
    public boolean isFilledByDefault() {
        return switch (this) {
            case CIRCLE, SQUARE, TRIANGLE_UP, TRIANGLE_DOWN, DIAMOND, HEXAGON, PENTAGON, STAR -> true;
            case PLUS, CROSS -> false;
        };
    }

    /**
     * Checks if this is a line-based symbol (not a polygon).
     *
     * @return true for PLUS and CROSS
     */
    public boolean isLineBased() {
        return this == PLUS || this == CROSS;
    }
}
