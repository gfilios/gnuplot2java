package com.gnuplot.core.geometry;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test suite for Point3D.
 */
class Point3DTest {

    private static final double TOLERANCE = 1e-10;

    // ============================================================
    // Construction Tests
    // ============================================================

    @Test
    void testConstruction2D() {
        Point3D point = Point3D.of2D(3, 4);
        assertEquals(3.0, point.getX(), TOLERANCE);
        assertEquals(4.0, point.getY(), TOLERANCE);
        assertEquals(0.0, point.getZ(), TOLERANCE);
        assertTrue(point.is2D());
    }

    @Test
    void testConstruction3D() {
        Point3D point = Point3D.of3D(1, 2, 3);
        assertEquals(1.0, point.getX(), TOLERANCE);
        assertEquals(2.0, point.getY(), TOLERANCE);
        assertEquals(3.0, point.getZ(), TOLERANCE);
        assertFalse(point.is2D());
    }

    // ============================================================
    // Arithmetic Operations Tests
    // ============================================================

    @Test
    void testAdd() {
        Point3D p1 = Point3D.of3D(1, 2, 3);
        Point3D p2 = Point3D.of3D(4, 5, 6);
        Point3D result = p1.add(p2);

        assertEquals(5.0, result.getX(), TOLERANCE);
        assertEquals(7.0, result.getY(), TOLERANCE);
        assertEquals(9.0, result.getZ(), TOLERANCE);
    }

    @Test
    void testSubtract() {
        Point3D p1 = Point3D.of3D(5, 7, 9);
        Point3D p2 = Point3D.of3D(1, 2, 3);
        Point3D result = p1.subtract(p2);

        assertEquals(4.0, result.getX(), TOLERANCE);
        assertEquals(5.0, result.getY(), TOLERANCE);
        assertEquals(6.0, result.getZ(), TOLERANCE);
    }

    @Test
    void testMultiplyScalar() {
        Point3D point = Point3D.of3D(1, 2, 3);
        Point3D result = point.multiply(2.5);

        assertEquals(2.5, result.getX(), TOLERANCE);
        assertEquals(5.0, result.getY(), TOLERANCE);
        assertEquals(7.5, result.getZ(), TOLERANCE);
    }

    // ============================================================
    // Distance and Magnitude Tests
    // ============================================================

    @Test
    void testDistance2D() {
        Point3D p1 = Point3D.of2D(0, 0);
        Point3D p2 = Point3D.of2D(3, 4);

        assertEquals(5.0, p1.distanceTo(p2), TOLERANCE);
        assertEquals(5.0, p2.distanceTo(p1), TOLERANCE);
    }

    @Test
    void testDistance3D() {
        Point3D p1 = Point3D.of3D(1, 2, 3);
        Point3D p2 = Point3D.of3D(4, 6, 8);

        double expected = Math.sqrt(9 + 16 + 25); // sqrt(50)
        assertEquals(expected, p1.distanceTo(p2), TOLERANCE);
    }

    @Test
    void testMagnitude() {
        Point3D point = Point3D.of3D(3, 4, 0);
        assertEquals(5.0, point.magnitude(), TOLERANCE);
    }

    @Test
    void testMagnitude3D() {
        Point3D point = Point3D.of3D(1, 2, 2);
        assertEquals(3.0, point.magnitude(), TOLERANCE);
    }

    // ============================================================
    // Vector Operations Tests
    // ============================================================

    @Test
    void testNormalize() {
        Point3D point = Point3D.of3D(3, 4, 0);
        Point3D normalized = point.normalize();

        assertEquals(0.6, normalized.getX(), TOLERANCE);
        assertEquals(0.8, normalized.getY(), TOLERANCE);
        assertEquals(0.0, normalized.getZ(), TOLERANCE);
        assertEquals(1.0, normalized.magnitude(), TOLERANCE);
    }

    @Test
    void testNormalizeZeroVectorThrows() {
        Point3D zero = Point3D.of3D(0, 0, 0);
        assertThrows(ArithmeticException.class, zero::normalize);
    }

    @Test
    void testDotProduct() {
        Point3D p1 = Point3D.of3D(1, 2, 3);
        Point3D p2 = Point3D.of3D(4, 5, 6);

        // 1*4 + 2*5 + 3*6 = 4 + 10 + 18 = 32
        assertEquals(32.0, p1.dot(p2), TOLERANCE);
    }

    @Test
    void testDotProductOrthogonal() {
        Point3D p1 = Point3D.of3D(1, 0, 0);
        Point3D p2 = Point3D.of3D(0, 1, 0);

        assertEquals(0.0, p1.dot(p2), TOLERANCE);
    }

    @Test
    void testCrossProduct() {
        Point3D p1 = Point3D.of3D(1, 0, 0);
        Point3D p2 = Point3D.of3D(0, 1, 0);
        Point3D result = p1.cross(p2);

        assertEquals(0.0, result.getX(), TOLERANCE);
        assertEquals(0.0, result.getY(), TOLERANCE);
        assertEquals(1.0, result.getZ(), TOLERANCE);
    }

    @Test
    void testCrossProductAnticommutative() {
        Point3D p1 = Point3D.of3D(2, 3, 4);
        Point3D p2 = Point3D.of3D(5, 6, 7);

        Point3D cross1 = p1.cross(p2);
        Point3D cross2 = p2.cross(p1);

        assertEquals(cross1.getX(), -cross2.getX(), TOLERANCE);
        assertEquals(cross1.getY(), -cross2.getY(), TOLERANCE);
        assertEquals(cross1.getZ(), -cross2.getZ(), TOLERANCE);
    }

    // ============================================================
    // Equality and Hash Tests
    // ============================================================

    @Test
    void testEquals() {
        Point3D p1 = Point3D.of3D(1, 2, 3);
        Point3D p2 = Point3D.of3D(1, 2, 3);
        Point3D p3 = Point3D.of3D(1, 2, 4);

        assertEquals(p1, p2);
        assertNotEquals(p1, p3);
        assertNotEquals(p1, null);
        assertNotEquals(p1, "not a point");
    }

    @Test
    void testHashCode() {
        Point3D p1 = Point3D.of3D(1, 2, 3);
        Point3D p2 = Point3D.of3D(1, 2, 3);

        assertEquals(p1.hashCode(), p2.hashCode());
    }

    @Test
    void testToString2D() {
        Point3D point = Point3D.of2D(3.14159, 2.71828);
        String str = point.toString();

        assertTrue(str.contains("Point2D"));
        // Check that it contains the numbers (with either . or , as decimal separator)
        assertTrue(str.contains("3") && str.contains("14"));
        assertTrue(str.contains("2") && str.contains("71"));
    }

    @Test
    void testToString3D() {
        Point3D point = Point3D.of3D(1, 2, 3);
        String str = point.toString();

        assertTrue(str.contains("Point3D"));
        assertTrue(str.contains("1"));
        assertTrue(str.contains("2"));
        assertTrue(str.contains("3"));
    }

    // ============================================================
    // Edge Cases Tests
    // ============================================================

    @Test
    void testNegativeCoordinates() {
        Point3D point = Point3D.of3D(-1, -2, -3);
        assertEquals(-1.0, point.getX(), TOLERANCE);
        assertEquals(-2.0, point.getY(), TOLERANCE);
        assertEquals(-3.0, point.getZ(), TOLERANCE);
    }

    @Test
    void testZeroPoint() {
        Point3D zero = Point3D.of3D(0, 0, 0);
        assertEquals(0.0, zero.magnitude(), TOLERANCE);
        assertEquals(0.0, zero.distanceTo(zero), TOLERANCE);
    }

    @Test
    void testVeryLargeCoordinates() {
        Point3D point = Point3D.of3D(1e100, 1e100, 1e100);
        double expected = Math.sqrt(3) * 1e100;
        assertEquals(expected, point.magnitude(), expected * 1e-10);
    }
}
