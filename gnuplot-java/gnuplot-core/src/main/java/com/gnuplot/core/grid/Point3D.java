package com.gnuplot.core.grid;

/**
 * Simple 3D point representation for grid interpolation.
 *
 * @param x X coordinate
 * @param y Y coordinate
 * @param z Z coordinate
 */
public record Point3D(double x, double y, double z) {

    /**
     * Check if all coordinates are finite (not NaN or infinite).
     */
    public boolean isFinite() {
        return Double.isFinite(x) && Double.isFinite(y) && Double.isFinite(z);
    }
}
