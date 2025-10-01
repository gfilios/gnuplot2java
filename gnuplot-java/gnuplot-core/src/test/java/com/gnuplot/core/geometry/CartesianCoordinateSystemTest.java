package com.gnuplot.core.geometry;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test suite for CartesianCoordinateSystem.
 */
class CartesianCoordinateSystemTest {

    private static final double TOLERANCE = 1e-10;

    // ============================================================
    // 2D Cartesian Tests
    // ============================================================

    @Test
    void test2DIdentityTransformation() {
        CoordinateSystem cartesian = CartesianCoordinateSystem.twoD();
        Point3D point = Point3D.of2D(3, 4);

        Point3D toCartesian = cartesian.toCartesian(point);
        Point3D fromCartesian = cartesian.fromCartesian(point);

        assertEquals(point, toCartesian);
        assertEquals(point, fromCartesian);
    }

    @Test
    void test2DGetDimensions() {
        CoordinateSystem cartesian = CartesianCoordinateSystem.twoD();
        assertEquals(2, cartesian.getDimensions());
    }

    @Test
    void test2DGetName() {
        CoordinateSystem cartesian = CartesianCoordinateSystem.twoD();
        assertEquals("Cartesian2D", cartesian.getName());
    }

    @Test
    void test2DRejectsThreeDPoints() {
        CoordinateSystem cartesian = CartesianCoordinateSystem.twoD();
        Point3D point3D = Point3D.of3D(1, 2, 3);

        assertThrows(IllegalArgumentException.class, () ->
                cartesian.toCartesian(point3D));
        assertThrows(IllegalArgumentException.class, () ->
                cartesian.fromCartesian(point3D));
    }

    // ============================================================
    // 3D Cartesian Tests
    // ============================================================

    @Test
    void test3DIdentityTransformation() {
        CoordinateSystem cartesian = CartesianCoordinateSystem.threeD();
        Point3D point = Point3D.of3D(1, 2, 3);

        Point3D toCartesian = cartesian.toCartesian(point);
        Point3D fromCartesian = cartesian.fromCartesian(point);

        assertEquals(point, toCartesian);
        assertEquals(point, fromCartesian);
    }

    @Test
    void test3DGetDimensions() {
        CoordinateSystem cartesian = CartesianCoordinateSystem.threeD();
        assertEquals(3, cartesian.getDimensions());
    }

    @Test
    void test3DGetName() {
        CoordinateSystem cartesian = CartesianCoordinateSystem.threeD();
        assertEquals("Cartesian3D", cartesian.getName());
    }

    @Test
    void test3DRejectsTwoDPoints() {
        CoordinateSystem cartesian = CartesianCoordinateSystem.threeD();
        Point3D point2D = Point3D.of2D(3, 4);

        assertThrows(IllegalArgumentException.class, () ->
                cartesian.toCartesian(point2D));
        assertThrows(IllegalArgumentException.class, () ->
                cartesian.fromCartesian(point2D));
    }

    // ============================================================
    // Edge Cases and Properties
    // ============================================================

    @Test
    void testNegativeCoordinates() {
        CoordinateSystem cartesian = CartesianCoordinateSystem.twoD();
        Point3D point = Point3D.of2D(-5, -3);

        assertEquals(point, cartesian.toCartesian(point));
        assertEquals(point, cartesian.fromCartesian(point));
    }

    @Test
    void testZeroPoint2D() {
        CoordinateSystem cartesian2D = CartesianCoordinateSystem.twoD();
        Point3D zero2D = Point3D.of2D(0, 0);

        assertEquals(zero2D, cartesian2D.toCartesian(zero2D));
    }

    @Test
    void testZeroPoint3D() {
        CoordinateSystem cartesian3D = CartesianCoordinateSystem.threeD();
        Point3D zero3D = Point3D.of3D(0, 0, 0);

        assertEquals(zero3D, cartesian3D.toCartesian(zero3D));
    }

    @Test
    void testInvertibility() {
        CoordinateSystem cartesian = CartesianCoordinateSystem.threeD();
        Point3D original = Point3D.of3D(7, 8, 9);

        Point3D transformed = cartesian.toCartesian(original);
        Point3D backAgain = cartesian.fromCartesian(transformed);

        assertEquals(original, backAgain);
    }

    @Test
    void testToString() {
        CoordinateSystem cartesian2D = CartesianCoordinateSystem.twoD();
        CoordinateSystem cartesian3D = CartesianCoordinateSystem.threeD();

        assertEquals("Cartesian2D", cartesian2D.toString());
        assertEquals("Cartesian3D", cartesian3D.toString());
    }
}
