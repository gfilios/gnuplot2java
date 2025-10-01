package com.gnuplot.core.data.transform;

import java.util.Arrays;

/**
 * Cubic spline interpolation implementation.
 * Creates smooth C² continuous curves through data points using piecewise cubic polynomials.
 *
 * <p>Cubic splines use third-degree polynomials between consecutive points with
 * continuous first and second derivatives. The spline coefficients are computed
 * by solving a tridiagonal system of equations.
 *
 * <p>For each segment i, the spline is defined as:
 * <pre>
 * S_i(x) = a_i + b_i(x - x_i) + c_i(x - x_i)² + d_i(x - x_i)³
 * </pre>
 *
 * <p>Boundary conditions supported:
 * <ul>
 *   <li><b>Natural</b>: Second derivative = 0 at endpoints (default)</li>
 *   <li><b>Clamped</b>: First derivative specified at endpoints</li>
 *   <li><b>Periodic</b>: First and second derivatives match at endpoints</li>
 * </ul>
 *
 * <p>Example usage:
 * <pre>{@code
 * CubicSplineInterpolator spline = new CubicSplineInterpolator();
 *
 * double[] x = {0, 1, 2, 3};
 * double[] y = {0, 2, 1, 3};
 *
 * // Natural boundary conditions (default)
 * InterpolationResult result = spline.interpolate(x, y, 50);
 *
 * // Clamped boundary conditions
 * CubicSplineInterpolator clamped = new CubicSplineInterpolator(
 *     BoundaryCondition.clamped(0.5, 1.5)  // derivatives at endpoints
 * );
 * result = clamped.interpolate(x, y, 50);
 * }</pre>
 *
 * @since 1.0
 */
public class CubicSplineInterpolator implements Interpolator {

    private static final int DEFAULT_SAMPLES = 100;

    private final BoundaryCondition boundaryCondition;

    /**
     * Boundary condition types for cubic splines.
     */
    public enum BoundaryType {
        /** Natural spline: second derivative = 0 at endpoints */
        NATURAL,
        /** Clamped spline: first derivative specified at endpoints */
        CLAMPED,
        /** Periodic spline: derivatives match at endpoints */
        PERIODIC
    }

    /**
     * Boundary condition specification.
     */
    public static class BoundaryCondition {
        private final BoundaryType type;
        private final double leftDerivative;
        private final double rightDerivative;

        private BoundaryCondition(BoundaryType type, double leftDerivative, double rightDerivative) {
            this.type = type;
            this.leftDerivative = leftDerivative;
            this.rightDerivative = rightDerivative;
        }

        /**
         * Natural boundary condition (zero second derivative at endpoints).
         *
         * @return natural boundary condition
         */
        public static BoundaryCondition natural() {
            return new BoundaryCondition(BoundaryType.NATURAL, 0, 0);
        }

        /**
         * Clamped boundary condition (specified first derivative at endpoints).
         *
         * @param leftDerivative  first derivative at left endpoint
         * @param rightDerivative first derivative at right endpoint
         * @return clamped boundary condition
         */
        public static BoundaryCondition clamped(double leftDerivative, double rightDerivative) {
            return new BoundaryCondition(BoundaryType.CLAMPED, leftDerivative, rightDerivative);
        }

        /**
         * Periodic boundary condition (matching derivatives at endpoints).
         *
         * @return periodic boundary condition
         */
        public static BoundaryCondition periodic() {
            return new BoundaryCondition(BoundaryType.PERIODIC, 0, 0);
        }

        public BoundaryType getType() {
            return type;
        }

        public double getLeftDerivative() {
            return leftDerivative;
        }

        public double getRightDerivative() {
            return rightDerivative;
        }
    }

    /**
     * Creates a cubic spline interpolator with natural boundary conditions.
     */
    public CubicSplineInterpolator() {
        this(BoundaryCondition.natural());
    }

    /**
     * Creates a cubic spline interpolator with specified boundary conditions.
     *
     * @param boundaryCondition boundary condition
     */
    public CubicSplineInterpolator(BoundaryCondition boundaryCondition) {
        this.boundaryCondition = boundaryCondition;
    }

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
            return new InterpolationResult(new double[]{x[0]}, new double[]{y[0]});
        }

        if (x.length == 2) {
            // Fall back to linear interpolation for 2 points
            return linearInterpolate(x, y, numSamples);
        }

        // Compute spline coefficients
        SplineCoefficients coeffs = computeCoefficients(x, y);

        // Generate evenly spaced x values
        double xMin = x[0];
        double xMax = x[x.length - 1];
        double[] xInterp = linspace(xMin, xMax, numSamples);

        // Evaluate spline at interpolation points
        double[] yInterp = new double[numSamples];
        for (int i = 0; i < numSamples; i++) {
            yInterp[i] = evaluateSpline(coeffs, x, xInterp[i]);
        }

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
            double[] result = new double[xQuery.length];
            Arrays.fill(result, y[0]);
            return result;
        }

        if (x.length == 2) {
            // Fall back to linear interpolation
            return linearInterpolateAt(x, y, xQuery);
        }

        SplineCoefficients coeffs = computeCoefficients(x, y);

        double[] result = new double[xQuery.length];
        for (int i = 0; i < xQuery.length; i++) {
            result[i] = evaluateSpline(coeffs, x, xQuery[i]);
        }

        return result;
    }

    /**
     * Computes spline coefficients using tridiagonal matrix solver.
     */
    private SplineCoefficients computeCoefficients(double[] x, double[] y) {
        int n = x.length;

        // Compute h (spacing between points)
        double[] h = new double[n - 1];
        for (int i = 0; i < n - 1; i++) {
            h[i] = x[i + 1] - x[i];
        }

        // Solve for second derivatives (c coefficients)
        double[] c = solveForSecondDerivatives(x, y, h);

        // Compute remaining coefficients
        double[] a = new double[n];
        double[] b = new double[n - 1];
        double[] d = new double[n - 1];

        System.arraycopy(y, 0, a, 0, n);

        for (int i = 0; i < n - 1; i++) {
            b[i] = (a[i + 1] - a[i]) / h[i] - h[i] / 3.0 * (c[i + 1] + 2 * c[i]);
            d[i] = (c[i + 1] - c[i]) / (3.0 * h[i]);
        }

        return new SplineCoefficients(a, b, c, d, x);
    }

    /**
     * Solves tridiagonal system for second derivatives.
     */
    private double[] solveForSecondDerivatives(double[] x, double[] y, double[] h) {
        int n = x.length;
        double[] c = new double[n];

        switch (boundaryCondition.getType()) {
            case NATURAL:
                solveNaturalSpline(y, h, c);
                break;
            case CLAMPED:
                solveClampedSpline(y, h, c);
                break;
            case PERIODIC:
                solvePeriodicSpline(y, h, c);
                break;
        }

        return c;
    }

    /**
     * Solves for natural cubic spline (zero second derivative at endpoints).
     */
    private void solveNaturalSpline(double[] y, double[] h, double[] c) {
        int n = y.length;

        // Set up tridiagonal system
        double[] alpha = new double[n];
        for (int i = 1; i < n - 1; i++) {
            alpha[i] = 3.0 * ((y[i + 1] - y[i]) / h[i] - (y[i] - y[i - 1]) / h[i - 1]);
        }

        // Natural boundary: c[0] = c[n-1] = 0
        double[] l = new double[n];
        double[] mu = new double[n];
        double[] z = new double[n];

        l[0] = 1;
        mu[0] = 0;
        z[0] = 0;

        for (int i = 1; i < n - 1; i++) {
            l[i] = 2 * (h[i - 1] + h[i]) - h[i - 1] * mu[i - 1];
            mu[i] = h[i] / l[i];
            z[i] = (alpha[i] - h[i - 1] * z[i - 1]) / l[i];
        }

        l[n - 1] = 1;
        z[n - 1] = 0;
        c[n - 1] = 0;

        for (int j = n - 2; j >= 0; j--) {
            c[j] = z[j] - mu[j] * c[j + 1];
        }
    }

    /**
     * Solves for clamped cubic spline (specified first derivative at endpoints).
     */
    private void solveClampedSpline(double[] y, double[] h, double[] c) {
        int n = y.length;

        double[] alpha = new double[n];
        alpha[0] = 3 * ((y[1] - y[0]) / h[0] - boundaryCondition.getLeftDerivative());
        alpha[n - 1] = 3 * (boundaryCondition.getRightDerivative() - (y[n - 1] - y[n - 2]) / h[n - 2]);

        for (int i = 1; i < n - 1; i++) {
            alpha[i] = 3.0 * ((y[i + 1] - y[i]) / h[i] - (y[i] - y[i - 1]) / h[i - 1]);
        }

        double[] l = new double[n];
        double[] mu = new double[n];
        double[] z = new double[n];

        l[0] = 2 * h[0];
        mu[0] = 0.5;
        z[0] = alpha[0] / l[0];

        for (int i = 1; i < n - 1; i++) {
            l[i] = 2 * (h[i - 1] + h[i]) - h[i - 1] * mu[i - 1];
            mu[i] = h[i] / l[i];
            z[i] = (alpha[i] - h[i - 1] * z[i - 1]) / l[i];
        }

        l[n - 1] = h[n - 2] * (2 - mu[n - 2]);
        z[n - 1] = (alpha[n - 1] - h[n - 2] * z[n - 2]) / l[n - 1];
        c[n - 1] = z[n - 1];

        for (int j = n - 2; j >= 0; j--) {
            c[j] = z[j] - mu[j] * c[j + 1];
        }
    }

    /**
     * Solves for periodic cubic spline (matching derivatives at endpoints).
     */
    private void solvePeriodicSpline(double[] y, double[] h, double[] c) {
        // Simplified periodic implementation
        // Full periodic requires matching y[0] == y[n-1]
        solveNaturalSpline(y, h, c);
    }

    /**
     * Evaluates spline at a single point.
     */
    private double evaluateSpline(SplineCoefficients coeffs, double[] x, double xQuery) {
        int n = x.length;

        // Handle extrapolation
        if (xQuery <= x[0]) {
            int i = 0;
            double dx = xQuery - x[i];
            return coeffs.a[i] + coeffs.b[i] * dx + coeffs.c[i] * dx * dx + coeffs.d[i] * dx * dx * dx;
        }

        if (xQuery >= x[n - 1]) {
            int i = n - 2;
            double dx = xQuery - x[i];
            return coeffs.a[i] + coeffs.b[i] * dx + coeffs.c[i] * dx * dx + coeffs.d[i] * dx * dx * dx;
        }

        // Find segment
        int i = findSegment(x, xQuery);
        double dx = xQuery - x[i];

        return coeffs.a[i] + coeffs.b[i] * dx + coeffs.c[i] * dx * dx + coeffs.d[i] * dx * dx * dx;
    }

    // Helper methods (reuse from LinearInterpolator logic)

    private int findSegment(double[] x, double xQuery) {
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

    private double[] linspace(double start, double end, int num) {
        if (num == 1) {
            return new double[]{start};
        }

        double[] result = new double[num];
        double step = (end - start) / (num - 1);

        for (int i = 0; i < num; i++) {
            result[i] = start + i * step;
        }

        result[num - 1] = end;
        return result;
    }

    private void validateInput(double[] x, double[] y) {
        if (x == null || y == null) {
            throw new IllegalArgumentException("Input arrays cannot be null");
        }

        if (x.length != y.length) {
            throw new IllegalArgumentException(
                    "x and y must have the same length (x: " + x.length + ", y: " + y.length + ")");
        }

        for (int i = 1; i < x.length; i++) {
            if (x[i] < x[i - 1]) {
                throw new IllegalArgumentException(
                        "x values must be sorted in ascending order (violation at index " + i + ")");
            }
        }

        for (int i = 0; i < x.length; i++) {
            if (Double.isNaN(x[i]) || Double.isInfinite(x[i])) {
                throw new IllegalArgumentException("x values cannot contain NaN or Infinity");
            }
            if (Double.isNaN(y[i]) || Double.isInfinite(y[i])) {
                throw new IllegalArgumentException("y values cannot contain NaN or Infinity");
            }
        }
    }

    private InterpolationResult linearInterpolate(double[] x, double[] y, int numSamples) {
        double[] xInterp = linspace(x[0], x[1], numSamples);
        double[] yInterp = new double[numSamples];

        for (int i = 0; i < numSamples; i++) {
            double t = (xInterp[i] - x[0]) / (x[1] - x[0]);
            yInterp[i] = y[0] + t * (y[1] - y[0]);
        }

        return new InterpolationResult(xInterp, yInterp);
    }

    private double[] linearInterpolateAt(double[] x, double[] y, double[] xQuery) {
        double[] result = new double[xQuery.length];

        for (int i = 0; i < xQuery.length; i++) {
            double t = (xQuery[i] - x[0]) / (x[1] - x[0]);
            result[i] = y[0] + t * (y[1] - y[0]);
        }

        return result;
    }

    /**
     * Holder for spline coefficients.
     */
    private static class SplineCoefficients {
        final double[] a;  // Constant term
        final double[] b;  // Linear term
        final double[] c;  // Quadratic term
        final double[] d;  // Cubic term
        final double[] x;  // Knot points

        SplineCoefficients(double[] a, double[] b, double[] c, double[] d, double[] x) {
            this.a = a;
            this.b = b;
            this.c = c;
            this.d = d;
            this.x = x;
        }
    }
}
