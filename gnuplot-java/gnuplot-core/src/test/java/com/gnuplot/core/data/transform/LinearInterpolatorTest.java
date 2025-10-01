package com.gnuplot.core.data.transform;

import com.gnuplot.core.data.transform.Interpolator.InterpolationResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test suite for LinearInterpolator.
 */
class LinearInterpolatorTest {

    private LinearInterpolator interpolator;

    @BeforeEach
    void setUp() {
        interpolator = new LinearInterpolator();
    }

    // ============================================================
    // Basic Interpolation Tests
    // ============================================================

    @Test
    void testSimpleLinearInterpolation() {
        double[] x = {0, 1, 2};
        double[] y = {0, 1, 2};

        InterpolationResult result = interpolator.interpolate(x, y, 5);

        assertEquals(5, result.size());

        // Check endpoints
        assertEquals(0.0, result.getX()[0], 1e-10);
        assertEquals(2.0, result.getX()[4], 1e-10);
        assertEquals(0.0, result.getY()[0], 1e-10);
        assertEquals(2.0, result.getY()[4], 1e-10);

        // Check midpoint
        assertEquals(1.0, result.getX()[2], 1e-10);
        assertEquals(1.0, result.getY()[2], 1e-10);
    }

    @Test
    void testNonUniformSpacing() {
        double[] x = {0, 1, 3, 6};
        double[] y = {0, 2, 4, 6};

        double[] xQuery = {0.5, 2.0, 4.5};
        double[] yInterp = interpolator.interpolateAt(x, y, xQuery);

        assertEquals(3, yInterp.length);

        // At x=0.5 (between 0 and 1): y = 0 + (2-0) * 0.5 = 1.0
        assertEquals(1.0, yInterp[0], 1e-10);

        // At x=2.0 (between 1 and 3): y = 2 + (4-2) * 0.5 = 3.0
        assertEquals(3.0, yInterp[1], 1e-10);

        // At x=4.5 (between 3 and 6): y = 4 + (6-4) * 0.5 = 5.0
        assertEquals(4 + (6.0 - 4) * (4.5 - 3) / (6 - 3), yInterp[2], 1e-10);
    }

    @Test
    void testConstantFunction() {
        double[] x = {0, 1, 2, 3};
        double[] y = {5, 5, 5, 5};

        InterpolationResult result = interpolator.interpolate(x, y, 10);

        assertEquals(10, result.size());

        // All y values should be 5
        for (int i = 0; i < result.size(); i++) {
            assertEquals(5.0, result.getY()[i], 1e-10);
        }
    }

    @Test
    void testNegativeValues() {
        double[] x = {-2, -1, 0, 1, 2};
        double[] y = {4, 1, 0, 1, 4};

        double[] xQuery = {-1.5, -0.5, 0.5, 1.5};
        double[] yInterp = interpolator.interpolateAt(x, y, xQuery);

        assertEquals(4, yInterp.length);

        // At x=-1.5 (between -2 and -1): y = 4 + (1-4) * 0.5 = 2.5
        assertEquals(2.5, yInterp[0], 1e-10);

        // At x=-0.5 (between -1 and 0): y = 1 + (0-1) * 0.5 = 0.5
        assertEquals(0.5, yInterp[1], 1e-10);

        // At x=0.5 (between 0 and 1): y = 0 + (1-0) * 0.5 = 0.5
        assertEquals(0.5, yInterp[2], 1e-10);

        // At x=1.5 (between 1 and 2): y = 1 + (4-1) * 0.5 = 2.5
        assertEquals(2.5, yInterp[3], 1e-10);
    }

    // ============================================================
    // Edge Cases
    // ============================================================

    @Test
    void testSinglePoint() {
        double[] x = {1.0};
        double[] y = {2.0};

        InterpolationResult result = interpolator.interpolate(x, y, 5);

        assertEquals(1, result.size());
        assertEquals(1.0, result.getX()[0], 1e-10);
        assertEquals(2.0, result.getY()[0], 1e-10);
    }

    @Test
    void testTwoPoints() {
        double[] x = {0, 10};
        double[] y = {0, 100};

        InterpolationResult result = interpolator.interpolate(x, y, 11);

        assertEquals(11, result.size());

        // Check endpoints
        assertEquals(0.0, result.getY()[0], 1e-10);
        assertEquals(100.0, result.getY()[10], 1e-10);

        // Check midpoint
        assertEquals(50.0, result.getY()[5], 1e-10);
    }

    @Test
    void testEmptyInput() {
        double[] x = {};
        double[] y = {};

        InterpolationResult result = interpolator.interpolate(x, y, 10);

        assertEquals(0, result.size());
    }

    @Test
    void testEmptyQuery() {
        double[] x = {0, 1, 2};
        double[] y = {0, 1, 2};
        double[] xQuery = {};

        double[] yInterp = interpolator.interpolateAt(x, y, xQuery);

        assertEquals(0, yInterp.length);
    }

    // ============================================================
    // Extrapolation Tests
    // ============================================================

    @Test
    void testExtrapolationBelow() {
        double[] x = {1, 2, 3};
        double[] y = {2, 4, 6};

        double[] xQuery = {0.0, 0.5};
        double[] yInterp = interpolator.interpolateAt(x, y, xQuery);

        // Extrapolate using first segment: y = 2 + (4-2) * (x-1) / (2-1)
        // At x=0.0: y = 2 + 2 * (-1) = 0
        assertEquals(0.0, yInterp[0], 1e-10);

        // At x=0.5: y = 2 + 2 * (-0.5) = 1
        assertEquals(1.0, yInterp[1], 1e-10);
    }

    @Test
    void testExtrapolationAbove() {
        double[] x = {1, 2, 3};
        double[] y = {2, 4, 6};

        double[] xQuery = {4.0, 5.0};
        double[] yInterp = interpolator.interpolateAt(x, y, xQuery);

        // Extrapolate using last segment: y = 4 + (6-4) * (x-2) / (3-2)
        // At x=4.0: y = 4 + 2 * (4-2) = 8
        assertEquals(8.0, yInterp[0], 1e-10);

        // At x=5.0: y = 4 + 2 * (5-2) = 10
        assertEquals(10.0, yInterp[1], 1e-10);
    }

    // ============================================================
    // Validation Tests
    // ============================================================

    @Test
    void testNullInputThrows() {
        assertThrows(IllegalArgumentException.class, () ->
                interpolator.interpolate(null, new double[]{1, 2}));

        assertThrows(IllegalArgumentException.class, () ->
                interpolator.interpolate(new double[]{1, 2}, null));
    }

    @Test
    void testMismatchedLengthThrows() {
        double[] x = {1, 2, 3};
        double[] y = {1, 2};

        assertThrows(IllegalArgumentException.class, () ->
                interpolator.interpolate(x, y));
    }

    @Test
    void testUnsortedInputThrows() {
        double[] x = {1, 3, 2};
        double[] y = {1, 2, 3};

        assertThrows(IllegalArgumentException.class, () ->
                interpolator.interpolate(x, y));
    }

    @Test
    void testNaNInputThrows() {
        double[] x = {1, Double.NaN, 3};
        double[] y = {1, 2, 3};

        assertThrows(IllegalArgumentException.class, () ->
                interpolator.interpolate(x, y));
    }

    @Test
    void testInfinityInputThrows() {
        double[] x = {1, 2, 3};
        double[] y = {1, Double.POSITIVE_INFINITY, 3};

        assertThrows(IllegalArgumentException.class, () ->
                interpolator.interpolate(x, y));
    }

    @Test
    void testInvalidSamplesThrows() {
        double[] x = {1, 2, 3};
        double[] y = {1, 2, 3};

        assertThrows(IllegalArgumentException.class, () ->
                interpolator.interpolate(x, y, 0));

        assertThrows(IllegalArgumentException.class, () ->
                interpolator.interpolate(x, y, 1));
    }

    // ============================================================
    // Precision Tests
    // ============================================================

    @Test
    void testExactPointsReturnExactValues() {
        double[] x = {0, 1, 2, 3, 4};
        double[] y = {0, 2, 4, 6, 8};

        // Query at exact input points
        double[] yInterp = interpolator.interpolateAt(x, y, x);

        assertEquals(x.length, yInterp.length);

        for (int i = 0; i < x.length; i++) {
            assertEquals(y[i], yInterp[i], 1e-10);
        }
    }

    @Test
    void testVeryClosePoints() {
        double[] x = {1.0, 1.0000000001, 2.0};
        double[] y = {10, 20, 30};

        // Should not throw, even with very close points
        InterpolationResult result = interpolator.interpolate(x, y, 5);

        assertNotNull(result);
        assertEquals(5, result.size());
    }

    // ============================================================
    // Mathematical Properties
    // ============================================================

    @Test
    void testLinearityProperty() {
        // For a linear function y = 2x + 1, interpolation should be exact
        double[] x = {0, 1, 2, 3, 4};
        double[] y = new double[x.length];
        for (int i = 0; i < x.length; i++) {
            y[i] = 2 * x[i] + 1;
        }

        double[] xQuery = {0.25, 0.5, 0.75, 1.5, 2.3, 3.7};
        double[] yInterp = interpolator.interpolateAt(x, y, xQuery);

        for (int i = 0; i < xQuery.length; i++) {
            double expected = 2 * xQuery[i] + 1;
            assertEquals(expected, yInterp[i], 1e-10);
        }
    }

    @Test
    void testMonotonicityPreservation() {
        // Monotonically increasing input
        double[] x = {0, 1, 2, 3, 4};
        double[] y = {0, 2, 5, 7, 10};

        InterpolationResult result = interpolator.interpolate(x, y, 20);

        // Check that output is also monotonically increasing
        for (int i = 1; i < result.size(); i++) {
            assertTrue(result.getY()[i] >= result.getY()[i - 1]);
        }
    }

    @Test
    void testBoundednessProperty() {
        double[] x = {0, 1, 2, 3};
        double[] y = {5, 10, 3, 8};

        double yMin = Double.MAX_VALUE;
        double yMax = Double.MIN_VALUE;
        for (double val : y) {
            yMin = Math.min(yMin, val);
            yMax = Math.max(yMax, val);
        }

        InterpolationResult result = interpolator.interpolate(x, y, 50);

        // All interpolated values should be within [min, max] of input
        for (int i = 0; i < result.size(); i++) {
            assertTrue(result.getY()[i] >= yMin - 1e-10);
            assertTrue(result.getY()[i] <= yMax + 1e-10);
        }
    }

    // ============================================================
    // Default Behavior Tests
    // ============================================================

    @Test
    void testDefaultSamples() {
        double[] x = {0, 1, 2};
        double[] y = {0, 1, 2};

        InterpolationResult result = interpolator.interpolate(x, y);

        assertEquals(100, result.size()); // Default is 100 samples
    }

    @Test
    void testCloningInResult() {
        double[] x = {0, 1, 2};
        double[] y = {0, 1, 2};

        InterpolationResult result = interpolator.interpolate(x, y, 5);

        double[] xArr1 = result.getX();
        double[] xArr2 = result.getX();

        // Should return different arrays (clones)
        assertNotSame(xArr1, xArr2);

        // But with same values
        assertArrayEquals(xArr1, xArr2);
    }
}
