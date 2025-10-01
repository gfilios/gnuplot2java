package com.gnuplot.core.data.transform;

/**
 * Interface for data interpolation algorithms.
 * Interpolators generate new data points between existing points.
 *
 * @since 1.0
 */
public interface Interpolator {

    /**
     * Interpolates data points to generate a smooth curve.
     *
     * @param x input x values (must be sorted in ascending order)
     * @param y input y values (same length as x)
     * @return interpolated data points
     * @throws IllegalArgumentException if arrays have different lengths or x is not sorted
     */
    InterpolationResult interpolate(double[] x, double[] y);

    /**
     * Interpolates data points with a specified number of output points.
     *
     * @param x          input x values (must be sorted in ascending order)
     * @param y          input y values (same length as x)
     * @param numSamples number of interpolated points to generate
     * @return interpolated data points
     * @throws IllegalArgumentException if arrays have different lengths or x is not sorted
     */
    InterpolationResult interpolate(double[] x, double[] y, int numSamples);

    /**
     * Interpolates data points at specific x coordinates.
     *
     * @param x      input x values (must be sorted in ascending order)
     * @param y      input y values (same length as x)
     * @param xQuery x coordinates where to evaluate the interpolation
     * @return interpolated y values corresponding to xQuery
     * @throws IllegalArgumentException if arrays have different lengths or x is not sorted
     */
    double[] interpolateAt(double[] x, double[] y, double[] xQuery);

    /**
     * Result of an interpolation operation.
     */
    class InterpolationResult {
        private final double[] x;
        private final double[] y;

        public InterpolationResult(double[] x, double[] y) {
            if (x.length != y.length) {
                throw new IllegalArgumentException("x and y must have the same length");
            }
            this.x = x;
            this.y = y;
        }

        /**
         * Returns the interpolated x values.
         *
         * @return x values
         */
        public double[] getX() {
            return x.clone();
        }

        /**
         * Returns the interpolated y values.
         *
         * @return y values
         */
        public double[] getY() {
            return y.clone();
        }

        /**
         * Returns the number of interpolated points.
         *
         * @return number of points
         */
        public int size() {
            return x.length;
        }
    }
}
