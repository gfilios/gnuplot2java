package com.gnuplot.render.elements;

/**
 * Represents a 3D point with x, y, z coordinates.
 * Immutable value class for 3D plotting.
 *
 * @since 1.0
 */
public record Point3D(double x, double y, double z) {

    /**
     * Creates a new 3D point.
     *
     * @param x the x-coordinate
     * @param y the y-coordinate
     * @param z the z-coordinate
     */
    public Point3D {
        // Validation in compact constructor
    }

    /**
     * Creates a 3D point from 2D coordinates with z=0.
     *
     * @param x the x-coordinate
     * @param y the y-coordinate
     * @return a new Point3D with z=0
     */
    public static Point3D from2D(double x, double y) {
        return new Point3D(x, y, 0.0);
    }

    /**
     * Checks if all coordinates are finite (not NaN or Infinity).
     *
     * @return true if all coordinates are finite
     */
    public boolean isFinite() {
        return Double.isFinite(x) && Double.isFinite(y) && Double.isFinite(z);
    }

    @Override
    public String toString() {
        return String.format("Point3D(%.2f, %.2f, %.2f)", x, y, z);
    }
}
