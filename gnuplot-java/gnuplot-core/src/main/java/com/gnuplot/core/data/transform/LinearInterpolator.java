package com.gnuplot.core.data.transform;

import java.util.Arrays;

/**
 * Linear interpolation implementation.
 * Performs piecewise linear interpolation between data points.
 *
 * <p>Linear interpolation connects consecutive points with straight lines.
 * For points (x₀, y₀) and (x₁, y₁), the interpolated value at x is:
 * <pre>
 * y = y₀ + (y₁ - y₀) × (x - x₀) / (x₁ - x₀)
 * </pre>
 *
 * <p>Example usage:
 * <pre>{@code
 * LinearInterpolator interp = new LinearInterpolator();
 *
 * double[] x = {0, 1, 2, 3};
 * double[] y = {0, 2, 1, 3};
 *
 * // Generate 10 interpolated points
 * InterpolationResult result = interp.interpolate(x, y, 10);
 *
 * // Evaluate at specific points
 * double[] xQuery = {0.5, 1.5, 2.5};
 * double[] yInterp = interp.interpolateAt(x, y, xQuery);
 * }</pre>
 *
 * @since 1.0
 */
public class LinearInterpolator implements Interpolator {

    private static final int DEFAULT_SAMPLES = 100;

    @Override
    public InterpolationResult interpolate(double[] x, double[] y) {
        return interpolate(x, y, DEFAULT_SAMPLES);
    }

    @Override
    public InterpolationResult interpolate(double[] x, double[] y, int numSamples) {
        validateInput(x, y);

        if (numSamples < 2) {
            throw new IllegalArgumentException("Number of samples must be at least 2");
        }

        if (x.length == 0) {
            return new InterpolationResult(new double[0], new double[0]);
        }

        if (x.length == 1) {
            // Single point - return the point itself
            return new InterpolationResult(
                    new double[]{x[0]},
                    new double[]{y[0]}
            );
        }

        // Generate evenly spaced x values
        double xMin = x[0];
        double xMax = x[x.length - 1];
        double[] xInterp = linspace(xMin, xMax, numSamples);

        // Interpolate at these points
        double[] yInterp = interpolateAt(x, y, xInterp);

        return new InterpolationResult(xInterp, yInterp);
    }

    @Override
    public double[] interpolateAt(double[] x, double[] y, double[] xQuery) {
        validateInput(x, y);

        if (xQuery.length == 0) {
            return new double[0];
        }

        if (x.length == 0) {
            throw new IllegalArgumentException("Cannot interpolate with empty input");
        }

        if (x.length == 1) {
            // Single point - return y value for all queries
            double[] result = new double[xQuery.length];
            Arrays.fill(result, y[0]);
            return result;
        }

        double[] result = new double[xQuery.length];

        for (int i = 0; i < xQuery.length; i++) {
            result[i] = interpolateSingle(x, y, xQuery[i]);
        }

        return result;
    }

    /**
     * Interpolates a single point.
     *
     * @param x      input x values (sorted)
     * @param y      input y values
     * @param xQuery query point
     * @return interpolated y value
     */
    private double interpolateSingle(double[] x, double[] y, double xQuery) {
        // Handle out of bounds - extrapolate using nearest segment
        if (xQuery <= x[0]) {
            if (x.length == 1) {
                return y[0];
            }
            // Extrapolate using first segment
            return linearInterp(x[0], y[0], x[1], y[1], xQuery);
        }

        if (xQuery >= x[x.length - 1]) {
            if (x.length == 1) {
                return y[x.length - 1];
            }
            // Extrapolate using last segment
            int n = x.length - 1;
            return linearInterp(x[n - 1], y[n - 1], x[n], y[n], xQuery);
        }

        // Find the segment containing xQuery using binary search
        int idx = findSegment(x, xQuery);

        // Interpolate within the segment
        return linearInterp(x[idx], y[idx], x[idx + 1], y[idx + 1], xQuery);
    }

    /**
     * Finds the index of the segment containing xQuery.
     * Returns i such that x[i] <= xQuery < x[i+1].
     *
     * @param x      sorted array
     * @param xQuery query value
     * @return segment index
     */
    private int findSegment(double[] x, double xQuery) {
        // Binary search for the right segment
        int left = 0;
        int right = x.length - 1;

        while (left < right - 1) {
            int mid = (left + right) / 2;
            if (x[mid] <= xQuery) {
                left = mid;
            } else {
                right = mid;
            }
        }

        return left;
    }

    /**
     * Performs linear interpolation between two points.
     *
     * @param x0 first x coordinate
     * @param y0 first y coordinate
     * @param x1 second x coordinate
     * @param y1 second y coordinate
     * @param x  query x coordinate
     * @return interpolated y value
     */
    private double linearInterp(double x0, double y0, double x1, double y1, double x) {
        if (Math.abs(x1 - x0) < 1e-10) {
            // Points are too close - return average
            return (y0 + y1) / 2.0;
        }

        double t = (x - x0) / (x1 - x0);
        return y0 + t * (y1 - y0);
    }

    /**
     * Generates evenly spaced values.
     *
     * @param start start value
     * @param end   end value
     * @param num   number of values
     * @return array of evenly spaced values
     */
    private double[] linspace(double start, double end, int num) {
        if (num == 1) {
            return new double[]{start};
        }

        double[] result = new double[num];
        double step = (end - start) / (num - 1);

        for (int i = 0; i < num; i++) {
            result[i] = start + i * step;
        }

        // Ensure exact end value
        result[num - 1] = end;

        return result;
    }

    /**
     * Validates input arrays.
     *
     * @param x input x values
     * @param y input y values
     * @throws IllegalArgumentException if validation fails
     */
    private void validateInput(double[] x, double[] y) {
        if (x == null || y == null) {
            throw new IllegalArgumentException("Input arrays cannot be null");
        }

        if (x.length != y.length) {
            throw new IllegalArgumentException(
                    "x and y must have the same length (x: " + x.length + ", y: " + y.length + ")");
        }

        // Check for sorted x values
        for (int i = 1; i < x.length; i++) {
            if (x[i] < x[i - 1]) {
                throw new IllegalArgumentException(
                        "x values must be sorted in ascending order (violation at index " + i + ")");
            }
        }

        // Check for NaN values
        for (int i = 0; i < x.length; i++) {
            if (Double.isNaN(x[i]) || Double.isInfinite(x[i])) {
                throw new IllegalArgumentException("x values cannot contain NaN or Infinity");
            }
            if (Double.isNaN(y[i]) || Double.isInfinite(y[i])) {
                throw new IllegalArgumentException("y values cannot contain NaN or Infinity");
            }
        }
    }
}
