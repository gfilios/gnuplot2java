package com.gnuplot.core.data.statistics;

import com.gnuplot.core.data.statistics.DescriptiveStatistics.StatisticsSummary;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test suite for DescriptiveStatistics.
 */
class DescriptiveStatisticsTest {

    private static final double TOLERANCE = 1e-10;

    // ============================================================
    // Central Tendency Tests
    // ============================================================

    @Test
    void testMean() {
        double[] data = {1, 2, 3, 4, 5};
        assertEquals(3.0, DescriptiveStatistics.mean(data), TOLERANCE);
    }

    @Test
    void testMeanWithNegativeValues() {
        double[] data = {-5, -3, -1, 0, 1, 3, 5};
        assertEquals(0.0, DescriptiveStatistics.mean(data), TOLERANCE);
    }

    @Test
    void testMedianOddLength() {
        double[] data = {1, 3, 5, 7, 9};
        assertEquals(5.0, DescriptiveStatistics.median(data), TOLERANCE);
    }

    @Test
    void testMedianEvenLength() {
        double[] data = {1, 2, 3, 4};
        assertEquals(2.5, DescriptiveStatistics.median(data), TOLERANCE);
    }

    @Test
    void testMedianUnsorted() {
        double[] data = {9, 1, 5, 3, 7};
        assertEquals(5.0, DescriptiveStatistics.median(data), TOLERANCE);
    }

    @Test
    void testMode() {
        double[] data = {1, 2, 2, 3, 3, 3, 4};
        assertEquals(3.0, DescriptiveStatistics.mode(data), TOLERANCE);
    }

    @Test
    void testModeMultipleWithSameFrequency() {
        // When there's a tie, return smallest value
        double[] data = {1, 1, 2, 2, 3};
        assertEquals(1.0, DescriptiveStatistics.mode(data), TOLERANCE);
    }

    // ============================================================
    // Dispersion Tests
    // ============================================================

    @Test
    void testVariance() {
        double[] data = {2, 4, 4, 4, 5, 5, 7, 9};
        // Calculated manually: variance = 4.571428...
        assertEquals(4.571428571428571, DescriptiveStatistics.variance(data), 1e-6);
    }

    @Test
    void testPopulationVariance() {
        double[] data = {2, 4, 4, 4, 5, 5, 7, 9};
        double variance = DescriptiveStatistics.populationVariance(data);
        assertTrue(variance < DescriptiveStatistics.variance(data)); // Population < sample
    }

    @Test
    void testStandardDeviation() {
        double[] data = {2, 4, 4, 4, 5, 5, 7, 9};
        double stddev = DescriptiveStatistics.standardDeviation(data);
        assertEquals(Math.sqrt(4.571428571428571), stddev, 1e-6);
    }

    @Test
    void testStandardDeviationKnownValue() {
        double[] data = {1, 2, 3, 4, 5};
        // Manual calculation: mean=3, variance=2.5, stddev=1.581...
        assertEquals(Math.sqrt(2.5), DescriptiveStatistics.standardDeviation(data), 1e-10);
    }

    // ============================================================
    // Range and Extrema Tests
    // ============================================================

    @Test
    void testMin() {
        double[] data = {5, 3, 9, 1, 7};
        assertEquals(1.0, DescriptiveStatistics.min(data), TOLERANCE);
    }

    @Test
    void testMax() {
        double[] data = {5, 3, 9, 1, 7};
        assertEquals(9.0, DescriptiveStatistics.max(data), TOLERANCE);
    }

    @Test
    void testRange() {
        double[] data = {5, 3, 9, 1, 7};
        assertEquals(8.0, DescriptiveStatistics.range(data), TOLERANCE);
    }

    // ============================================================
    // Quartiles and Percentiles Tests
    // ============================================================

    @Test
    void testPercentile0() {
        double[] data = {1, 2, 3, 4, 5};
        assertEquals(1.0, DescriptiveStatistics.percentile(data, 0), TOLERANCE);
    }

    @Test
    void testPercentile100() {
        double[] data = {1, 2, 3, 4, 5};
        assertEquals(5.0, DescriptiveStatistics.percentile(data, 100), TOLERANCE);
    }

    @Test
    void testPercentile50() {
        double[] data = {1, 2, 3, 4, 5};
        // 50th percentile should equal median
        assertEquals(DescriptiveStatistics.median(data),
                DescriptiveStatistics.percentile(data, 50), TOLERANCE);
    }

    @Test
    void testFirstQuartile() {
        double[] data = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
        double q1 = DescriptiveStatistics.firstQuartile(data);
        assertTrue(q1 > DescriptiveStatistics.min(data));
        assertTrue(q1 < DescriptiveStatistics.median(data));
    }

    @Test
    void testThirdQuartile() {
        double[] data = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
        double q3 = DescriptiveStatistics.thirdQuartile(data);
        assertTrue(q3 > DescriptiveStatistics.median(data));
        assertTrue(q3 < DescriptiveStatistics.max(data));
    }

    @Test
    void testInterquartileRange() {
        double[] data = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
        double iqr = DescriptiveStatistics.interquartileRange(data);
        double q1 = DescriptiveStatistics.firstQuartile(data);
        double q3 = DescriptiveStatistics.thirdQuartile(data);
        assertEquals(q3 - q1, iqr, TOLERANCE);
    }

    // ============================================================
    // Correlation Tests
    // ============================================================

    @Test
    void testPerfectPositiveCorrelation() {
        double[] x = {1, 2, 3, 4, 5};
        double[] y = {2, 4, 6, 8, 10}; // y = 2x
        assertEquals(1.0, DescriptiveStatistics.correlation(x, y), 1e-10);
    }

    @Test
    void testPerfectNegativeCorrelation() {
        double[] x = {1, 2, 3, 4, 5};
        double[] y = {5, 4, 3, 2, 1}; // Perfect negative
        assertEquals(-1.0, DescriptiveStatistics.correlation(x, y), 1e-10);
    }

    @Test
    void testNoCorrelation() {
        double[] x = {1, 2, 3, 4, 5};
        double[] y = {3, 3, 3, 3, 3}; // Constant
        assertEquals(0.0, DescriptiveStatistics.correlation(x, y), TOLERANCE);
    }

    @Test
    void testCorrelationKnownValue() {
        double[] x = {1, 2, 3, 4, 5};
        double[] y = {2, 4, 5, 4, 5};
        double corr = DescriptiveStatistics.correlation(x, y);
        assertTrue(corr > 0); // Positive correlation
        assertTrue(corr < 1); // Not perfect
    }

    @Test
    void testCovariance() {
        double[] x = {1, 2, 3, 4, 5};
        double[] y = {2, 4, 6, 8, 10};
        double cov = DescriptiveStatistics.covariance(x, y);
        assertTrue(cov > 0); // Positive covariance
    }

    @Test
    void testCovarianceSymmetric() {
        double[] x = {1, 2, 3, 4, 5};
        double[] y = {2, 4, 5, 4, 5};
        double covXY = DescriptiveStatistics.covariance(x, y);
        double covYX = DescriptiveStatistics.covariance(y, x);
        assertEquals(covXY, covYX, TOLERANCE);
    }

    // ============================================================
    // Summary Statistics Tests
    // ============================================================

    @Test
    void testSummarize() {
        double[] data = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
        StatisticsSummary summary = DescriptiveStatistics.summarize(data);

        assertEquals(10, summary.getCount());
        assertEquals(5.5, summary.getMean(), TOLERANCE);
        assertEquals(5.5, summary.getMedian(), TOLERANCE);
        assertEquals(1.0, summary.getMin(), TOLERANCE);
        assertEquals(10.0, summary.getMax(), TOLERANCE);
        assertEquals(9.0, summary.getRange(), TOLERANCE);
        assertFalse(Double.isNaN(summary.getStandardDeviation()));
        assertFalse(Double.isNaN(summary.getVariance()));
    }

    @Test
    void testSummarySingleElement() {
        double[] data = {42.0};
        StatisticsSummary summary = DescriptiveStatistics.summarize(data);

        assertEquals(1, summary.getCount());
        assertEquals(42.0, summary.getMean(), TOLERANCE);
        assertEquals(42.0, summary.getMedian(), TOLERANCE);
        assertEquals(42.0, summary.getMin(), TOLERANCE);
        assertEquals(42.0, summary.getMax(), TOLERANCE);
        assertEquals(0.0, summary.getRange(), TOLERANCE);
        assertTrue(Double.isNaN(summary.getStandardDeviation())); // Need >= 2 for std dev
    }

    // ============================================================
    // Validation Tests
    // ============================================================

    @Test
    void testMeanThrowsOnEmpty() {
        assertThrows(IllegalArgumentException.class, () ->
                DescriptiveStatistics.mean(new double[]{}));
    }

    @Test
    void testMeanThrowsOnNull() {
        assertThrows(IllegalArgumentException.class, () ->
                DescriptiveStatistics.mean(null));
    }

    @Test
    void testVarianceRequiresTwoPoints() {
        assertThrows(IllegalArgumentException.class, () ->
                DescriptiveStatistics.variance(new double[]{1}));
    }

    @Test
    void testCorrelationRequiresSameLength() {
        double[] x = {1, 2, 3};
        double[] y = {1, 2};
        assertThrows(IllegalArgumentException.class, () ->
                DescriptiveStatistics.correlation(x, y));
    }

    @Test
    void testCorrelationRequiresTwoPoints() {
        double[] x = {1};
        double[] y = {1};
        assertThrows(IllegalArgumentException.class, () ->
                DescriptiveStatistics.correlation(x, y));
    }

    @Test
    void testPercentileOutOfRange() {
        double[] data = {1, 2, 3};
        assertThrows(IllegalArgumentException.class, () ->
                DescriptiveStatistics.percentile(data, -1));
        assertThrows(IllegalArgumentException.class, () ->
                DescriptiveStatistics.percentile(data, 101));
    }

    // ============================================================
    // Mathematical Properties Tests
    // ============================================================

    @Test
    void testMeanPropertyLinearTransform() {
        double[] data = {1, 2, 3, 4, 5};
        double mean = DescriptiveStatistics.mean(data);

        // Transform: y = 2x + 3
        double[] transformed = new double[data.length];
        for (int i = 0; i < data.length; i++) {
            transformed[i] = 2 * data[i] + 3;
        }

        double transformedMean = DescriptiveStatistics.mean(transformed);
        assertEquals(2 * mean + 3, transformedMean, TOLERANCE);
    }

    @Test
    void testVariancePropertyScaling() {
        double[] data = {1, 2, 3, 4, 5};
        double variance = DescriptiveStatistics.variance(data);

        // Scale by factor of 2
        double[] scaled = new double[data.length];
        for (int i = 0; i < data.length; i++) {
            scaled[i] = 2 * data[i];
        }

        double scaledVariance = DescriptiveStatistics.variance(scaled);
        // Variance scales by square of scaling factor
        assertEquals(4 * variance, scaledVariance, 1e-6);
    }

    @Test
    void testCorrelationInvariantToScaling() {
        double[] x = {1, 2, 3, 4, 5};
        double[] y = {2, 4, 6, 8, 10};

        double corr1 = DescriptiveStatistics.correlation(x, y);

        // Scale both arrays
        double[] xScaled = new double[x.length];
        double[] yScaled = new double[y.length];
        for (int i = 0; i < x.length; i++) {
            xScaled[i] = x[i] * 10;
            yScaled[i] = y[i] * 5;
        }

        double corr2 = DescriptiveStatistics.correlation(xScaled, yScaled);

        // Correlation should be invariant to scaling
        assertEquals(corr1, corr2, TOLERANCE);
    }
}
