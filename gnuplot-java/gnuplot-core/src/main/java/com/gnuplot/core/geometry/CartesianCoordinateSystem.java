package com.gnuplot.core.geometry;

/**
 * Cartesian coordinate system for 2D and 3D points.
 *
 * <p>In Cartesian coordinates, points are represented as (x, y) in 2D or (x, y, z) in 3D.
 * This is the identity transformation - points are already in Cartesian form.
 *
 * <p>Example usage:
 * <pre>{@code
 * CoordinateSystem cartesian2D = CartesianCoordinateSystem.twoD();
 * Point3D point = Point3D.of2D(3, 4);
 * Point3D transformed = cartesian2D.toCartesian(point); // Returns same point
 *
 * CoordinateSystem cartesian3D = CartesianCoordinateSystem.threeD();
 * Point3D point3D = Point3D.of3D(1, 2, 3);
 * Point3D transformed3D = cartesian3D.toCartesian(point3D); // Returns same point
 * }</pre>
 *
 * @since 1.0
 */
public final class CartesianCoordinateSystem implements CoordinateSystem {

    private final int dimensions;

    private CartesianCoordinateSystem(int dimensions) {
        if (dimensions != 2 && dimensions != 3) {
            throw new IllegalArgumentException("Dimensions must be 2 or 3, got: " + dimensions);
        }
        this.dimensions = dimensions;
    }

    /**
     * Creates a 2D Cartesian coordinate system.
     *
     * @return 2D Cartesian system
     */
    public static CartesianCoordinateSystem twoD() {
        return new CartesianCoordinateSystem(2);
    }

    /**
     * Creates a 3D Cartesian coordinate system.
     *
     * @return 3D Cartesian system
     */
    public static CartesianCoordinateSystem threeD() {
        return new CartesianCoordinateSystem(3);
    }

    @Override
    public Point3D toCartesian(Point3D point) {
        validatePoint(point);
        return point; // Identity transformation
    }

    @Override
    public Point3D fromCartesian(Point3D point) {
        validatePoint(point);
        return point; // Identity transformation
    }

    @Override
    public int getDimensions() {
        return dimensions;
    }

    @Override
    public String getName() {
        return dimensions == 2 ? "Cartesian2D" : "Cartesian3D";
    }

    private void validatePoint(Point3D point) {
        if (dimensions == 2 && !point.is2D()) {
            throw new IllegalArgumentException(
                    "Point must be 2D for 2D Cartesian system, got: " + point);
        }
        if (dimensions == 3 && point.is2D()) {
            throw new IllegalArgumentException(
                    "Point must be 3D for 3D Cartesian system, got: " + point);
        }
    }

    @Override
    public String toString() {
        return getName();
    }
}
