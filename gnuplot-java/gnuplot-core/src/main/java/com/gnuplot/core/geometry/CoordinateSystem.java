package com.gnuplot.core.geometry;

/**
 * Represents a coordinate system for transforming between different coordinate spaces.
 *
 * <p>Coordinate systems define how to map between logical coordinates (e.g., polar, spherical)
 * and physical Cartesian coordinates used for rendering.
 *
 * @since 1.0
 */
public interface CoordinateSystem {

    /**
     * Transforms a point from this coordinate system to Cartesian coordinates.
     *
     * @param point the point in this coordinate system
     * @return the point in Cartesian coordinates
     * @throws IllegalArgumentException if point has wrong dimensionality
     */
    Point3D toCartesian(Point3D point);

    /**
     * Transforms a point from Cartesian coordinates to this coordinate system.
     *
     * @param point the point in Cartesian coordinates
     * @return the point in this coordinate system
     * @throws IllegalArgumentException if point has wrong dimensionality
     */
    Point3D fromCartesian(Point3D point);

    /**
     * Returns the dimensionality of this coordinate system (2 or 3).
     *
     * @return number of dimensions
     */
    int getDimensions();

    /**
     * Returns the name of this coordinate system.
     *
     * @return coordinate system name (e.g., "Cartesian", "Polar", "Cylindrical")
     */
    String getName();
}
