package com.gnuplot.core.grid;

import java.util.ArrayList;
import java.util.List;

/**
 * Implements 3D grid interpolation (dgrid3d) for scattered data points.
 *
 * <p>Gnuplot's dgrid3d creates a regular grid by interpolating scattered 3D data
 * using various weighting schemes. This implementation supports qnorm weighting.
 *
 * @since 1.0
 */
public class Dgrid3D {

    /**
     * Interpolation mode for dgrid3d.
     */
    public enum InterpolationMode {
        /** Quadratic norm weighting: weight = 1 / (distance^norm) */
        QNORM,
        /** Gauss weighting */
        GAUSS,
        /** Cauchy weighting */
        CAUCHY,
        /** Exponential weighting */
        EXP,
        /** Box weighting */
        BOX
    }

    private final int rows;
    private final int cols;
    private final InterpolationMode mode;
    private final double norm;

    /**
     * Create a dgrid3d interpolator.
     *
     * @param rows number of grid rows
     * @param cols number of grid columns
     * @param mode interpolation mode
     * @param norm norm parameter (for qnorm mode)
     */
    public Dgrid3D(int rows, int cols, InterpolationMode mode, double norm) {
        this.rows = rows;
        this.cols = cols;
        this.mode = mode;
        this.norm = norm;
    }

    /**
     * Interpolate scattered 3D points onto a regular grid.
     *
     * @param scatteredPoints scattered input points
     * @return gridded points in row-major order
     */
    public List<Point3D> interpolate(List<Point3D> scatteredPoints) {
        if (scatteredPoints.isEmpty()) {
            return new ArrayList<>();
        }

        // Find data bounds
        double xMin = Double.POSITIVE_INFINITY, xMax = Double.NEGATIVE_INFINITY;
        double yMin = Double.POSITIVE_INFINITY, yMax = Double.NEGATIVE_INFINITY;

        for (Point3D p : scatteredPoints) {
            if (p.isFinite()) {
                xMin = Math.min(xMin, p.x());
                xMax = Math.max(xMax, p.x());
                yMin = Math.min(yMin, p.y());
                yMax = Math.max(yMax, p.y());
            }
        }

        // Create regular grid
        List<Point3D> gridPoints = new ArrayList<>(rows * cols);

        for (int row = 0; row < rows; row++) {
            double y = yMin + (yMax - yMin) * row / (rows - 1.0);

            for (int col = 0; col < cols; col++) {
                double x = xMin + (xMax - xMin) * col / (cols - 1.0);

                // Interpolate z value at this grid point
                double z = interpolateZ(x, y, scatteredPoints);

                gridPoints.add(new Point3D(x, y, z));
            }
        }

        return gridPoints;
    }

    /**
     * Interpolate Z value at a given (x, y) position using weighted average.
     */
    private double interpolateZ(double x, double y, List<Point3D> points) {
        double weightedSum = 0.0;
        double totalWeight = 0.0;

        for (Point3D p : points) {
            if (!p.isFinite()) {
                continue;
            }

            // Calculate distance in XY plane
            double dx = x - p.x();
            double dy = y - p.y();
            double distance = Math.sqrt(dx * dx + dy * dy);

            // Avoid division by zero - if grid point coincides with data point, use that z
            if (distance < 1e-10) {
                return p.z();
            }

            // Calculate weight based on mode
            double weight = calculateWeight(distance);

            weightedSum += weight * p.z();
            totalWeight += weight;
        }

        // Return weighted average, or 0 if no valid points
        return totalWeight > 0 ? weightedSum / totalWeight : 0.0;
    }

    /**
     * Calculate interpolation weight based on distance and mode.
     */
    private double calculateWeight(double distance) {
        return switch (mode) {
            case QNORM -> 1.0 / Math.pow(distance, norm);
            case GAUSS -> Math.exp(-distance * distance);
            case CAUCHY -> 1.0 / (1.0 + distance * distance);
            case EXP -> Math.exp(-distance);
            case BOX -> distance < 1.0 ? 1.0 : 0.0;
        };
    }
}
