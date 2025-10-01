package com.gnuplot.core.geometry;

import java.util.Objects;

/**
 * Immutable 3D point with x, y, z coordinates.
 *
 * <p>Can also represent 2D points by setting z=0. Provides common operations
 * for point manipulation and coordinate transformations.
 *
 * @since 1.0
 */
public final class Point3D {

    private final double x;
    private final double y;
    private final double z;
    private final boolean is2D;

    private Point3D(double x, double y, double z, boolean is2D) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.is2D = is2D;
    }

    /**
     * Creates a 3D point.
     *
     * @param x x-coordinate
     * @param y y-coordinate
     * @param z z-coordinate
     */
    public Point3D(double x, double y, double z) {
        this(x, y, z, false);
    }

    /**
     * Creates a 2D point (z=0).
     *
     * @param x x-coordinate
     * @param y y-coordinate
     * @return 2D point
     */
    public static Point3D of2D(double x, double y) {
        return new Point3D(x, y, 0, true);
    }

    /**
     * Creates a 3D point.
     *
     * @param x x-coordinate
     * @param y y-coordinate
     * @param z z-coordinate
     * @return 3D point
     */
    public static Point3D of3D(double x, double y, double z) {
        return new Point3D(x, y, z);
    }

    /**
     * Returns the x-coordinate.
     *
     * @return x value
     */
    public double getX() {
        return x;
    }

    /**
     * Returns the y-coordinate.
     *
     * @return y value
     */
    public double getY() {
        return y;
    }

    /**
     * Returns the z-coordinate.
     *
     * @return z value
     */
    public double getZ() {
        return z;
    }

    /**
     * Returns true if this is a 2D point.
     *
     * @return true if 2D point
     */
    public boolean is2D() {
        return is2D;
    }

    /**
     * Adds another point to this point.
     *
     * @param other the point to add
     * @return new point with summed coordinates
     */
    public Point3D add(Point3D other) {
        return new Point3D(x + other.x, y + other.y, z + other.z, is2D && other.is2D);
    }

    /**
     * Subtracts another point from this point.
     *
     * @param other the point to subtract
     * @return new point with subtracted coordinates
     */
    public Point3D subtract(Point3D other) {
        return new Point3D(x - other.x, y - other.y, z - other.z, is2D && other.is2D);
    }

    /**
     * Multiplies this point by a scalar.
     *
     * @param scalar the scalar to multiply by
     * @return new point with scaled coordinates
     */
    public Point3D multiply(double scalar) {
        return new Point3D(x * scalar, y * scalar, z * scalar, is2D);
    }

    /**
     * Calculates the Euclidean distance to another point.
     *
     * @param other the other point
     * @return distance
     */
    public double distanceTo(Point3D other) {
        double dx = x - other.x;
        double dy = y - other.y;
        double dz = z - other.z;
        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }

    /**
     * Calculates the magnitude (length) of this point as a vector.
     *
     * @return magnitude
     */
    public double magnitude() {
        return Math.sqrt(x * x + y * y + z * z);
    }

    /**
     * Normalizes this point as a unit vector.
     *
     * @return normalized point
     * @throws ArithmeticException if magnitude is zero
     */
    public Point3D normalize() {
        double mag = magnitude();
        if (mag == 0) {
            throw new ArithmeticException("Cannot normalize zero-length vector");
        }
        return new Point3D(x / mag, y / mag, z / mag, is2D);
    }

    /**
     * Calculates the dot product with another point.
     *
     * @param other the other point
     * @return dot product
     */
    public double dot(Point3D other) {
        return x * other.x + y * other.y + z * other.z;
    }

    /**
     * Calculates the cross product with another point.
     *
     * @param other the other point
     * @return cross product
     */
    public Point3D cross(Point3D other) {
        return new Point3D(
                y * other.z - z * other.y,
                z * other.x - x * other.z,
                x * other.y - y * other.x,
                false  // Cross product always yields 3D vector
        );
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Point3D point3D = (Point3D) o;
        return is2D == point3D.is2D &&
                Double.compare(point3D.x, x) == 0 &&
                Double.compare(point3D.y, y) == 0 &&
                Double.compare(point3D.z, z) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y, z, is2D);
    }

    @Override
    public String toString() {
        if (is2D()) {
            return String.format("Point2D(%.4f, %.4f)", x, y);
        }
        return String.format("Point3D(%.4f, %.4f, %.4f)", x, y, z);
    }
}
