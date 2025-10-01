package com.gnuplot.core.geometry;

/**
 * Polar coordinate system for 2D points.
 *
 * <p>In polar coordinates, points are represented as (r, θ) where:
 * <ul>
 *   <li>r (radius) is the distance from the origin
 *   <li>θ (theta/angle) is the angle from the positive x-axis
 * </ul>
 *
 * <p>Conversion formulas:
 * <ul>
 *   <li>Polar to Cartesian: x = r * cos(θ), y = r * sin(θ)
 *   <li>Cartesian to Polar: r = sqrt(x² + y²), θ = atan2(y, x)
 * </ul>
 *
 * <p>Example usage:
 * <pre>{@code
 * CoordinateSystem polar = PolarCoordinateSystem.create(AngleUnit.RADIANS);
 *
 * // Convert polar (r=5, θ=π/4) to Cartesian
 * Point3D polarPoint = Point3D.of2D(5, Math.PI/4);
 * Point3D cartesian = polar.toCartesian(polarPoint);
 * // Result: approximately (3.536, 3.536, 0)
 *
 * // Convert Cartesian back to polar
 * Point3D backToPolar = polar.fromCartesian(cartesian);
 * // Result: approximately (5, 0.785, 0)
 * }</pre>
 *
 * @since 1.0
 */
public final class PolarCoordinateSystem implements CoordinateSystem {

    /**
     * Angle units for polar coordinates.
     */
    public enum AngleUnit {
        /** Radians (default) - angles in range [0, 2π] */
        RADIANS,
        /** Degrees - angles in range [0, 360] */
        DEGREES
    }

    private final AngleUnit angleUnit;

    private PolarCoordinateSystem(AngleUnit angleUnit) {
        this.angleUnit = angleUnit;
    }

    /**
     * Creates a polar coordinate system with the specified angle unit.
     *
     * @param angleUnit angle unit (RADIANS or DEGREES)
     * @return polar coordinate system
     */
    public static PolarCoordinateSystem create(AngleUnit angleUnit) {
        return new PolarCoordinateSystem(angleUnit);
    }

    /**
     * Creates a polar coordinate system with radians (default).
     *
     * @return polar coordinate system using radians
     */
    public static PolarCoordinateSystem radians() {
        return new PolarCoordinateSystem(AngleUnit.RADIANS);
    }

    /**
     * Creates a polar coordinate system with degrees.
     *
     * @return polar coordinate system using degrees
     */
    public static PolarCoordinateSystem degrees() {
        return new PolarCoordinateSystem(AngleUnit.DEGREES);
    }

    @Override
    public Point3D toCartesian(Point3D point) {
        if (!point.is2D()) {
            throw new IllegalArgumentException(
                    "Polar coordinates are 2D only, got: " + point);
        }

        double r = point.getX();      // radius
        double theta = point.getY();  // angle

        // Convert angle to radians if needed
        double thetaRad = angleUnit == AngleUnit.DEGREES
                ? Math.toRadians(theta)
                : theta;

        double x = r * Math.cos(thetaRad);
        double y = r * Math.sin(thetaRad);

        return Point3D.of2D(x, y);
    }

    @Override
    public Point3D fromCartesian(Point3D point) {
        if (!point.is2D()) {
            throw new IllegalArgumentException(
                    "Polar coordinates are 2D only, got: " + point);
        }

        double x = point.getX();
        double y = point.getY();

        double r = Math.sqrt(x * x + y * y);
        double thetaRad = Math.atan2(y, x);

        // Normalize to [0, 2π] range
        if (thetaRad < 0) {
            thetaRad += 2 * Math.PI;
        }

        // Convert to degrees if needed
        double theta = angleUnit == AngleUnit.DEGREES
                ? Math.toDegrees(thetaRad)
                : thetaRad;

        return Point3D.of2D(r, theta);
    }

    @Override
    public int getDimensions() {
        return 2;
    }

    @Override
    public String getName() {
        return "Polar(" + angleUnit + ")";
    }

    /**
     * Returns the angle unit used by this coordinate system.
     *
     * @return angle unit
     */
    public AngleUnit getAngleUnit() {
        return angleUnit;
    }

    @Override
    public String toString() {
        return getName();
    }
}
