/*
 * Gnuplot Java - Contour Line
 * Port of gnuplot-c/src/graph3d.h:gnuplot_contours
 */
package com.gnuplot.core.grid;

import com.gnuplot.core.geometry.Point3D;
import java.util.List;

/**
 * Represents a single contour line at a specific z-level.
 * <p>
 * A contour line is a polyline connecting all points where the surface
 * crosses a particular z-value. It may be closed (a loop) or open
 * (starting and ending at the boundary).
 * </p>
 *
 * @param zLevel The z-value at which this contour is drawn
 * @param points The list of 3D points forming the contour polyline
 * @param isClosed True if the contour forms a closed loop
 * @param label The formatted label string for this contour level (e.g., "0.5")
 * @param color The color for this contour line (e.g., "#9400D3")
 *
 * @see <a href="file:///gnuplot-c/src/graph3d.h">graph3d.h:gnuplot_contours</a>
 */
public record ContourLine(
    double zLevel,
    List<Point3D> points,
    boolean isClosed,
    String label,
    String color
) {
    /**
     * Default color for contours (black).
     */
    private static final String DEFAULT_COLOR = "#000000";

    /**
     * Creates a contour line with auto-generated label and default color.
     */
    public ContourLine(double zLevel, List<Point3D> points, boolean isClosed) {
        this(zLevel, points, isClosed, formatLabel(zLevel, "%g"), DEFAULT_COLOR);
    }

    /**
     * Creates a contour line with auto-generated label and specified color.
     */
    public ContourLine(double zLevel, List<Point3D> points, boolean isClosed, String color) {
        this(zLevel, points, isClosed, formatLabel(zLevel, "%g"), color);
    }

    /**
     * Creates a contour line with custom format for label and default color.
     */
    public static ContourLine withFormat(double zLevel, List<Point3D> points,
                                          boolean isClosed, String format) {
        return new ContourLine(zLevel, points, isClosed, formatLabel(zLevel, format), DEFAULT_COLOR);
    }

    /**
     * Creates a contour line with custom format for label and specified color.
     */
    public static ContourLine withFormatAndColor(double zLevel, List<Point3D> points,
                                                  boolean isClosed, String format, String color) {
        return new ContourLine(zLevel, points, isClosed, formatLabel(zLevel, format), color);
    }

    /**
     * Creates a copy of this contour line with a new color.
     */
    public ContourLine withColor(String newColor) {
        return new ContourLine(zLevel, points, isClosed, label, newColor);
    }

    /**
     * Formats the z-level as a label string.
     */
    private static String formatLabel(double zLevel, String format) {
        try {
            return String.format(format, zLevel);
        } catch (Exception e) {
            return String.valueOf(zLevel);
        }
    }

    /**
     * Returns the number of points in this contour.
     */
    public int numPoints() {
        return points != null ? points.size() : 0;
    }

    /**
     * Returns true if this contour has enough points to be rendered.
     */
    public boolean isValid() {
        return points != null && points.size() >= 2;
    }

    @Override
    public String toString() {
        return String.format("ContourLine{z=%.4f, points=%d, closed=%s, label='%s', color='%s'}",
                zLevel, numPoints(), isClosed, label, color);
    }
}
