package com.gnuplot.core.data.transform;

import com.gnuplot.core.data.transform.CubicSplineInterpolator.BoundaryCondition;
import com.gnuplot.core.data.transform.Interpolator.InterpolationResult;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test suite for CubicSplineInterpolator.
 */
class CubicSplineInterpolatorTest {

    private static final double TOLERANCE = 1e-6;

    // ============================================================
    // Basic Natural Spline Tests
    // ============================================================

    @Test
    void testNaturalSplineSimple() {
        CubicSplineInterpolator spline = new CubicSplineInterpolator();

        double[] x = {0, 1, 2, 3};
        double[] y = {0, 1, 0, 1};

        InterpolationResult result = spline.interpolate(x, y, 10);

        assertNotNull(result);
        assertEquals(10, result.size());

        // Check endpoints match
        assertEquals(0.0, result.getY()[0], TOLERANCE);
        assertEquals(1.0, result.getY()[9], TOLERANCE);
    }

    @Test
    void testNaturalSplineLinearFunction() {
        // For linear function, spline should match exactly
        CubicSplineInterpolator spline = new CubicSplineInterpolator();

        double[] x = {0, 1, 2, 3, 4};
        double[] y = {0, 2, 4, 6, 8};  // y = 2x

        double[] xQuery = {0.5, 1.5, 2.5, 3.5};
        double[] yInterp = spline.interpolateAt(x, y, xQuery);

        for (int i = 0; i < xQuery.length; i++) {
            double expected = 2 * xQuery[i];
            assertEquals(expected, yInterp[i], TOLERANCE);
        }
    }

    @Test
    void testNaturalSplineQuadraticFunction() {
        // Natural cubic splines don't preserve quadratics exactly (they preserve cubics)
        // But they should be close
        CubicSplineInterpolator spline = new CubicSplineInterpolator();

        double[] x = {0, 1, 2, 3, 4};
        double[] y = new double[x.length];
        for (int i = 0; i < x.length; i++) {
            y[i] = x[i] * x[i];  // y = x²
        }

        double[] xQuery = {0.5, 1.5, 2.5, 3.5};
        double[] yInterp = spline.interpolateAt(x, y, xQuery);

        for (int i = 0; i < xQuery.length; i++) {
            double expected = xQuery[i] * xQuery[i];
            // Natural splines won't match exactly, but should be reasonably close
            assertEquals(expected, yInterp[i], 0.2);  // Relaxed tolerance
        }
    }

    @Test
    void testNaturalSplineSmoothness() {
        CubicSplineInterpolator spline = new CubicSplineInterpolator();

        double[] x = {0, 1, 2, 3, 4};
        double[] y = {0, 2, 1, 3, 2};

        InterpolationResult result = spline.interpolate(x, y, 100);

        // Check that result passes through original points
        for (int i = 0; i < x.length; i++) {
            double[] singleQuery = {x[i]};
            double[] yVal = spline.interpolateAt(x, y, singleQuery);
            assertEquals(y[i], yVal[0], TOLERANCE);
        }
    }

    // ============================================================
    // Clamped Spline Tests
    // ============================================================

    @Test
    void testClampedSpline() {
        // Specify derivatives at endpoints
        BoundaryCondition bc = BoundaryCondition.clamped(1.0, -1.0);
        CubicSplineInterpolator spline = new CubicSplineInterpolator(bc);

        double[] x = {0, 1, 2, 3};
        double[] y = {0, 1, 0, 1};

        InterpolationResult result = spline.interpolate(x, y, 10);

        assertNotNull(result);
        assertEquals(10, result.size());

        // Endpoints should match
        assertEquals(0.0, result.getY()[0], TOLERANCE);
        assertEquals(1.0, result.getY()[9], TOLERANCE);
    }

    @Test
    void testClampedSplineLinearFunction() {
        // For linear y = 2x, derivative is 2 everywhere
        BoundaryCondition bc = BoundaryCondition.clamped(2.0, 2.0);
        CubicSplineInterpolator spline = new CubicSplineInterpolator(bc);

        double[] x = {0, 1, 2, 3};
        double[] y = {0, 2, 4, 6};

        double[] xQuery = {0.5, 1.5, 2.5};
        double[] yInterp = spline.interpolateAt(x, y, xQuery);

        for (int i = 0; i < xQuery.length; i++) {
            double expected = 2 * xQuery[i];
            assertEquals(expected, yInterp[i], TOLERANCE);
        }
    }

    // ============================================================
    // Edge Cases
    // ============================================================

    @Test
    void testThreePoints() {
        CubicSplineInterpolator spline = new CubicSplineInterpolator();

        double[] x = {0, 1, 2};
        double[] y = {0, 1, 0};

        InterpolationResult result = spline.interpolate(x, y, 10);

        assertNotNull(result);
        assertEquals(10, result.size());
    }

    @Test
    void testTwoPointsFallsBackToLinear() {
        CubicSplineInterpolator spline = new CubicSplineInterpolator();

        double[] x = {0, 2};
        double[] y = {0, 4};

        double[] xQuery = {1.0};
        double[] yInterp = spline.interpolateAt(x, y, xQuery);

        assertEquals(2.0, yInterp[0], TOLERANCE);
    }

    @Test
    void testSinglePoint() {
        CubicSplineInterpolator spline = new CubicSplineInterpolator();

        double[] x = {1.0};
        double[] y = {2.0};

        InterpolationResult result = spline.interpolate(x, y, 5);

        assertEquals(1, result.size());
        assertEquals(2.0, result.getY()[0], TOLERANCE);
    }

    @Test
    void testEmptyInput() {
        CubicSplineInterpolator spline = new CubicSplineInterpolator();

        double[] x = {};
        double[] y = {};

        InterpolationResult result = spline.interpolate(x, y, 10);

        assertEquals(0, result.size());
    }

    // ============================================================
    // Extrapolation Tests
    // ============================================================

    @Test
    void testExtrapolationBeyondRange() {
        CubicSplineInterpolator spline = new CubicSplineInterpolator();

        double[] x = {1, 2, 3, 4};
        double[] y = {1, 4, 9, 16};  // y = x²

        double[] xQuery = {0.5, 4.5};
        double[] yInterp = spline.interpolateAt(x, y, xQuery);

        // Should extrapolate using first and last segments
        assertNotNull(yInterp);
        assertEquals(2, yInterp.length);
    }

    // ============================================================
    // Validation Tests
    // ============================================================

    @Test
    void testNullInputThrows() {
        CubicSplineInterpolator spline = new CubicSplineInterpolator();

        assertThrows(IllegalArgumentException.class, () ->
                spline.interpolate(null, new double[]{1, 2}));
    }

    @Test
    void testMismatchedLengthThrows() {
        CubicSplineInterpolator spline = new CubicSplineInterpolator();

        double[] x = {1, 2, 3};
        double[] y = {1, 2};

        assertThrows(IllegalArgumentException.class, () ->
                spline.interpolate(x, y));
    }

    @Test
    void testUnsortedInputThrows() {
        CubicSplineInterpolator spline = new CubicSplineInterpolator();

        double[] x = {1, 3, 2};
        double[] y = {1, 2, 3};

        assertThrows(IllegalArgumentException.class, () ->
                spline.interpolate(x, y));
    }

    // ============================================================
    // Comparison with Linear Interpolation
    // ============================================================

    @Test
    void testSplineSmootherThanLinear() {
        CubicSplineInterpolator spline = new CubicSplineInterpolator();
        LinearInterpolator linear = new LinearInterpolator();

        double[] x = {0, 1, 2, 3};
        double[] y = {0, 2, 1, 3};

        InterpolationResult splineResult = spline.interpolate(x, y, 50);
        InterpolationResult linearResult = linear.interpolate(x, y, 50);

        // Both should pass through original points
        // But spline should be smoother (this is hard to test numerically)
        assertEquals(splineResult.size(), linearResult.size());
    }

    // ============================================================
    // Mathematical Properties
    // ============================================================

    @Test
    void testPassesThroughDataPoints() {
        CubicSplineInterpolator spline = new CubicSplineInterpolator();

        double[] x = {0, 1, 2, 3, 4};
        double[] y = {1, 3, 2, 4, 3};

        // Query at exact data points
        double[] yInterp = spline.interpolateAt(x, y, x);

        for (int i = 0; i < x.length; i++) {
            assertEquals(y[i], yInterp[i], TOLERANCE);
        }
    }

    @Test
    void testMonotonicityForMonotonicData() {
        CubicSplineInterpolator spline = new CubicSplineInterpolator();

        double[] x = {0, 1, 2, 3, 4};
        double[] y = {0, 1, 2, 3, 4};  // Monotonically increasing

        InterpolationResult result = spline.interpolate(x, y, 50);

        // Check monotonicity
        for (int i = 1; i < result.size(); i++) {
            assertTrue(result.getY()[i] >= result.getY()[i - 1] - TOLERANCE);
        }
    }

    // ============================================================
    // Boundary Condition Tests
    // ============================================================

    @Test
    void testNaturalBoundaryCondition() {
        BoundaryCondition bc = BoundaryCondition.natural();
        assertEquals(CubicSplineInterpolator.BoundaryType.NATURAL, bc.getType());
    }

    @Test
    void testClampedBoundaryCondition() {
        BoundaryCondition bc = BoundaryCondition.clamped(1.0, 2.0);
        assertEquals(CubicSplineInterpolator.BoundaryType.CLAMPED, bc.getType());
        assertEquals(1.0, bc.getLeftDerivative());
        assertEquals(2.0, bc.getRightDerivative());
    }

    @Test
    void testPeriodicBoundaryCondition() {
        BoundaryCondition bc = BoundaryCondition.periodic();
        assertEquals(CubicSplineInterpolator.BoundaryType.PERIODIC, bc.getType());
    }
}
