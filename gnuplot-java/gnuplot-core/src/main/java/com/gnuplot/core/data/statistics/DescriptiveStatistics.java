package com.gnuplot.core.data.statistics;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Calculates descriptive statistics for data sets.
 * Provides common statistical measures including central tendency, dispersion, and correlation.
 *
 * <p>Example usage:
 * <pre>{@code
 * double[] data = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
 *
 * // Calculate statistics
 * double mean = DescriptiveStatistics.mean(data);
 * double median = DescriptiveStatistics.median(data);
 * double stddev = DescriptiveStatistics.standardDeviation(data);
 *
 * // Get all statistics at once
 * StatisticsSummary summary = DescriptiveStatistics.summarize(data);
 * }</pre>
 *
 * @since 1.0
 */
public final class DescriptiveStatistics {

    private DescriptiveStatistics() {
        // Utility class
    }

    // ============================================================
    // Central Tendency
    // ============================================================

    /**
     * Calculates the arithmetic mean (average).
     *
     * @param data input data
     * @return mean value
     * @throws IllegalArgumentException if data is empty
     */
    public static double mean(double[] data) {
        validateNotEmpty(data);

        double sum = 0;
        for (double value : data) {
            sum += value;
        }
        return sum / data.length;
    }

    /**
     * Calculates the median (middle value).
     * For even-length arrays, returns the average of the two middle values.
     *
     * @param data input data
     * @return median value
     * @throws IllegalArgumentException if data is empty
     */
    public static double median(double[] data) {
        validateNotEmpty(data);

        double[] sorted = data.clone();
        Arrays.sort(sorted);

        int n = sorted.length;
        if (n % 2 == 0) {
            return (sorted[n / 2 - 1] + sorted[n / 2]) / 2.0;
        } else {
            return sorted[n / 2];
        }
    }

    /**
     * Calculates the mode (most frequent value).
     * If multiple modes exist, returns the smallest one.
     *
     * @param data input data
     * @return mode value
     * @throws IllegalArgumentException if data is empty
     */
    public static double mode(double[] data) {
        validateNotEmpty(data);

        Map<Double, Integer> frequencies = new HashMap<>();
        for (double value : data) {
            frequencies.put(value, frequencies.getOrDefault(value, 0) + 1);
        }

        double mode = data[0];
        int maxCount = 0;

        for (Map.Entry<Double, Integer> entry : frequencies.entrySet()) {
            int count = entry.getValue();
            if (count > maxCount || (count == maxCount && entry.getKey() < mode)) {
                mode = entry.getKey();
                maxCount = count;
            }
        }

        return mode;
    }

    // ============================================================
    // Dispersion
    // ============================================================

    /**
     * Calculates the variance (average squared deviation from mean).
     * Uses n-1 denominator (sample variance).
     *
     * @param data input data
     * @return variance
     * @throws IllegalArgumentException if data has fewer than 2 elements
     */
    public static double variance(double[] data) {
        if (data == null || data.length < 2) {
            throw new IllegalArgumentException("Need at least 2 data points for variance");
        }

        double mean = mean(data);
        double sumSquaredDiffs = 0;

        for (double value : data) {
            double diff = value - mean;
            sumSquaredDiffs += diff * diff;
        }

        return sumSquaredDiffs / (data.length - 1);
    }

    /**
     * Calculates the population variance (uses n denominator).
     *
     * @param data input data
     * @return population variance
     * @throws IllegalArgumentException if data is empty
     */
    public static double populationVariance(double[] data) {
        validateNotEmpty(data);

        double mean = mean(data);
        double sumSquaredDiffs = 0;

        for (double value : data) {
            double diff = value - mean;
            sumSquaredDiffs += diff * diff;
        }

        return sumSquaredDiffs / data.length;
    }

    /**
     * Calculates the standard deviation (square root of variance).
     * Uses n-1 denominator (sample standard deviation).
     *
     * @param data input data
     * @return standard deviation
     * @throws IllegalArgumentException if data has fewer than 2 elements
     */
    public static double standardDeviation(double[] data) {
        return Math.sqrt(variance(data));
    }

    /**
     * Calculates the population standard deviation.
     *
     * @param data input data
     * @return population standard deviation
     * @throws IllegalArgumentException if data is empty
     */
    public static double populationStandardDeviation(double[] data) {
        return Math.sqrt(populationVariance(data));
    }

    // ============================================================
    // Range and Extrema
    // ============================================================

    /**
     * Returns the minimum value.
     *
     * @param data input data
     * @return minimum value
     * @throws IllegalArgumentException if data is empty
     */
    public static double min(double[] data) {
        validateNotEmpty(data);

        double min = data[0];
        for (int i = 1; i < data.length; i++) {
            if (data[i] < min) {
                min = data[i];
            }
        }
        return min;
    }

    /**
     * Returns the maximum value.
     *
     * @param data input data
     * @return maximum value
     * @throws IllegalArgumentException if data is empty
     */
    public static double max(double[] data) {
        validateNotEmpty(data);

        double max = data[0];
        for (int i = 1; i < data.length; i++) {
            if (data[i] > max) {
                max = data[i];
            }
        }
        return max;
    }

    /**
     * Returns the range (max - min).
     *
     * @param data input data
     * @return range
     * @throws IllegalArgumentException if data is empty
     */
    public static double range(double[] data) {
        return max(data) - min(data);
    }

    // ============================================================
    // Quartiles and Percentiles
    // ============================================================

    /**
     * Calculates a percentile value.
     * Uses linear interpolation between closest ranks.
     *
     * @param data       input data
     * @param percentile percentile (0-100)
     * @return percentile value
     * @throws IllegalArgumentException if data is empty or percentile out of range
     */
    public static double percentile(double[] data, double percentile) {
        validateNotEmpty(data);

        if (percentile < 0 || percentile > 100) {
            throw new IllegalArgumentException("Percentile must be between 0 and 100");
        }

        double[] sorted = data.clone();
        Arrays.sort(sorted);

        if (percentile == 0) {
            return sorted[0];
        }
        if (percentile == 100) {
            return sorted[sorted.length - 1];
        }

        double index = (percentile / 100.0) * (sorted.length - 1);
        int lower = (int) Math.floor(index);
        int upper = (int) Math.ceil(index);

        if (lower == upper) {
            return sorted[lower];
        }

        double weight = index - lower;
        return sorted[lower] * (1 - weight) + sorted[upper] * weight;
    }

    /**
     * Calculates the first quartile (Q1, 25th percentile).
     *
     * @param data input data
     * @return first quartile
     * @throws IllegalArgumentException if data is empty
     */
    public static double firstQuartile(double[] data) {
        return percentile(data, 25);
    }

    /**
     * Calculates the third quartile (Q3, 75th percentile).
     *
     * @param data input data
     * @return third quartile
     * @throws IllegalArgumentException if data is empty
     */
    public static double thirdQuartile(double[] data) {
        return percentile(data, 75);
    }

    /**
     * Calculates the interquartile range (Q3 - Q1).
     *
     * @param data input data
     * @return interquartile range
     * @throws IllegalArgumentException if data is empty
     */
    public static double interquartileRange(double[] data) {
        return thirdQuartile(data) - firstQuartile(data);
    }

    // ============================================================
    // Correlation
    // ============================================================

    /**
     * Calculates the Pearson correlation coefficient between two data sets.
     * Returns a value between -1 and 1, where:
     * - 1 indicates perfect positive correlation
     * - 0 indicates no correlation
     * - -1 indicates perfect negative correlation
     *
     * @param x first data set
     * @param y second data set
     * @return correlation coefficient
     * @throws IllegalArgumentException if arrays have different lengths or < 2 elements
     */
    public static double correlation(double[] x, double[] y) {
        if (x == null || y == null) {
            throw new IllegalArgumentException("Input arrays cannot be null");
        }

        if (x.length != y.length) {
            throw new IllegalArgumentException(
                    "Arrays must have the same length (x: " + x.length + ", y: " + y.length + ")");
        }

        if (x.length < 2) {
            throw new IllegalArgumentException("Need at least 2 data points for correlation");
        }

        double meanX = mean(x);
        double meanY = mean(y);

        double sumXY = 0;
        double sumX2 = 0;
        double sumY2 = 0;

        for (int i = 0; i < x.length; i++) {
            double dx = x[i] - meanX;
            double dy = y[i] - meanY;

            sumXY += dx * dy;
            sumX2 += dx * dx;
            sumY2 += dy * dy;
        }

        if (sumX2 == 0 || sumY2 == 0) {
            // One or both variables are constant
            return 0;
        }

        return sumXY / Math.sqrt(sumX2 * sumY2);
    }

    /**
     * Calculates covariance between two data sets.
     * Uses n-1 denominator (sample covariance).
     *
     * @param x first data set
     * @param y second data set
     * @return covariance
     * @throws IllegalArgumentException if arrays have different lengths or < 2 elements
     */
    public static double covariance(double[] x, double[] y) {
        if (x == null || y == null) {
            throw new IllegalArgumentException("Input arrays cannot be null");
        }

        if (x.length != y.length) {
            throw new IllegalArgumentException(
                    "Arrays must have the same length (x: " + x.length + ", y: " + y.length + ")");
        }

        if (x.length < 2) {
            throw new IllegalArgumentException("Need at least 2 data points for covariance");
        }

        double meanX = mean(x);
        double meanY = mean(y);

        double sum = 0;
        for (int i = 0; i < x.length; i++) {
            sum += (x[i] - meanX) * (y[i] - meanY);
        }

        return sum / (x.length - 1);
    }

    // ============================================================
    // Summary Statistics
    // ============================================================

    /**
     * Calculates a comprehensive summary of statistics.
     *
     * @param data input data
     * @return summary statistics
     * @throws IllegalArgumentException if data is empty
     */
    public static StatisticsSummary summarize(double[] data) {
        validateNotEmpty(data);

        return new StatisticsSummary(
                data.length,
                mean(data),
                median(data),
                mode(data),
                data.length >= 2 ? standardDeviation(data) : Double.NaN,
                data.length >= 2 ? variance(data) : Double.NaN,
                min(data),
                max(data),
                firstQuartile(data),
                thirdQuartile(data)
        );
    }

    // ============================================================
    // Helper Methods
    // ============================================================

    private static void validateNotEmpty(double[] data) {
        if (data == null || data.length == 0) {
            throw new IllegalArgumentException("Data array cannot be null or empty");
        }
    }

    /**
     * Summary statistics container.
     */
    public static class StatisticsSummary {
        private final int count;
        private final double mean;
        private final double median;
        private final double mode;
        private final double standardDeviation;
        private final double variance;
        private final double min;
        private final double max;
        private final double q1;
        private final double q3;

        StatisticsSummary(int count, double mean, double median, double mode,
                          double standardDeviation, double variance,
                          double min, double max, double q1, double q3) {
            this.count = count;
            this.mean = mean;
            this.median = median;
            this.mode = mode;
            this.standardDeviation = standardDeviation;
            this.variance = variance;
            this.min = min;
            this.max = max;
            this.q1 = q1;
            this.q3 = q3;
        }

        public int getCount() { return count; }
        public double getMean() { return mean; }
        public double getMedian() { return median; }
        public double getMode() { return mode; }
        public double getStandardDeviation() { return standardDeviation; }
        public double getVariance() { return variance; }
        public double getMin() { return min; }
        public double getMax() { return max; }
        public double getRange() { return max - min; }
        public double getQ1() { return q1; }
        public double getQ3() { return q3; }
        public double getIQR() { return q3 - q1; }

        @Override
        public String toString() {
            return String.format(
                    "Statistics[count=%d, mean=%.4f, median=%.4f, std=%.4f, min=%.4f, max=%.4f]",
                    count, mean, median, standardDeviation, min, max
            );
        }
    }
}
