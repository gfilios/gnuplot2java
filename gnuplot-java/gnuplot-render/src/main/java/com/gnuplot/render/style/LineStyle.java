package com.gnuplot.render.style;

/**
 * Defines line styles for plot rendering.
 * Corresponds to gnuplot's line types and dash patterns.
 */
public enum LineStyle {
    /** Solid line (no dashes) */
    SOLID("none"),

    /** Dashed line (long dashes) */
    DASHED("8,4"),

    /** Dotted line (small dots) */
    DOTTED("2,2"),

    /** Dash-dot pattern */
    DASH_DOT("8,4,2,4"),

    /** Dash-dot-dot pattern */
    DASH_DOT_DOT("8,4,2,4,2,4"),

    /** Long dash pattern */
    LONG_DASH("12,4"),

    /** Short dash pattern */
    SHORT_DASH("4,2");

    private final String svgDashArray;

    LineStyle(String svgDashArray) {
        this.svgDashArray = svgDashArray;
    }

    /**
     * Gets the SVG stroke-dasharray attribute value.
     * Returns "none" for solid lines, or a comma-separated list of dash lengths.
     *
     * @return SVG dasharray string
     */
    public String getSvgDashArray() {
        return svgDashArray;
    }

    /**
     * Checks if this is a solid line (no dashes).
     */
    public boolean isSolid() {
        return this == SOLID;
    }
}
