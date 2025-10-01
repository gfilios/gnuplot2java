package com.gnuplot.core.geometry;

import com.gnuplot.core.geometry.PolarCoordinateSystem.AngleUnit;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test suite for PolarCoordinateSystem.
 */
class PolarCoordinateSystemTest {

    private static final double TOLERANCE = 1e-10;

    // ============================================================
    // Polar to Cartesian Tests (Radians)
    // ============================================================

    @Test
    void testPolarToCartesianRadians_0Degrees() {
        CoordinateSystem polar = PolarCoordinateSystem.radians();
        Point3D polarPoint = Point3D.of2D(5, 0); // r=5, θ=0

        Point3D cartesian = polar.toCartesian(polarPoint);

        assertEquals(5.0, cartesian.getX(), TOLERANCE);
        assertEquals(0.0, cartesian.getY(), TOLERANCE);
    }

    @Test
    void testPolarToCartesianRadians_90Degrees() {
        CoordinateSystem polar = PolarCoordinateSystem.radians();
        Point3D polarPoint = Point3D.of2D(5, Math.PI / 2); // r=5, θ=π/2

        Point3D cartesian = polar.toCartesian(polarPoint);

        assertEquals(0.0, cartesian.getX(), TOLERANCE);
        assertEquals(5.0, cartesian.getY(), TOLERANCE);
    }

    @Test
    void testPolarToCartesianRadians_45Degrees() {
        CoordinateSystem polar = PolarCoordinateSystem.radians();
        Point3D polarPoint = Point3D.of2D(Math.sqrt(2), Math.PI / 4); // r=√2, θ=π/4

        Point3D cartesian = polar.toCartesian(polarPoint);

        assertEquals(1.0, cartesian.getX(), TOLERANCE);
        assertEquals(1.0, cartesian.getY(), TOLERANCE);
    }

    @Test
    void testPolarToCartesianRadians_180Degrees() {
        CoordinateSystem polar = PolarCoordinateSystem.radians();
        Point3D polarPoint = Point3D.of2D(3, Math.PI); // r=3, θ=π

        Point3D cartesian = polar.toCartesian(polarPoint);

        assertEquals(-3.0, cartesian.getX(), TOLERANCE);
        assertEquals(0.0, cartesian.getY(), TOLERANCE);
    }

    // ============================================================
    // Polar to Cartesian Tests (Degrees)
    // ============================================================

    @Test
    void testPolarToCartesianDegrees_0() {
        CoordinateSystem polar = PolarCoordinateSystem.degrees();
        Point3D polarPoint = Point3D.of2D(5, 0); // r=5, θ=0°

        Point3D cartesian = polar.toCartesian(polarPoint);

        assertEquals(5.0, cartesian.getX(), TOLERANCE);
        assertEquals(0.0, cartesian.getY(), TOLERANCE);
    }

    @Test
    void testPolarToCartesianDegrees_90() {
        CoordinateSystem polar = PolarCoordinateSystem.degrees();
        Point3D polarPoint = Point3D.of2D(5, 90); // r=5, θ=90°

        Point3D cartesian = polar.toCartesian(polarPoint);

        assertEquals(0.0, cartesian.getX(), TOLERANCE);
        assertEquals(5.0, cartesian.getY(), TOLERANCE);
    }

    @Test
    void testPolarToCartesianDegrees_45() {
        CoordinateSystem polar = PolarCoordinateSystem.degrees();
        Point3D polarPoint = Point3D.of2D(Math.sqrt(2), 45); // r=√2, θ=45°

        Point3D cartesian = polar.toCartesian(polarPoint);

        assertEquals(1.0, cartesian.getX(), TOLERANCE);
        assertEquals(1.0, cartesian.getY(), TOLERANCE);
    }

    // ============================================================
    // Cartesian to Polar Tests (Radians)
    // ============================================================

    @Test
    void testCartesianToPolarRadians_PositiveX() {
        CoordinateSystem polar = PolarCoordinateSystem.radians();
        Point3D cartesian = Point3D.of2D(5, 0);

        Point3D polarPoint = polar.fromCartesian(cartesian);

        assertEquals(5.0, polarPoint.getX(), TOLERANCE); // r
        assertEquals(0.0, polarPoint.getY(), TOLERANCE); // θ
    }

    @Test
    void testCartesianToPolarRadians_PositiveY() {
        CoordinateSystem polar = PolarCoordinateSystem.radians();
        Point3D cartesian = Point3D.of2D(0, 5);

        Point3D polarPoint = polar.fromCartesian(cartesian);

        assertEquals(5.0, polarPoint.getX(), TOLERANCE); // r
        assertEquals(Math.PI / 2, polarPoint.getY(), TOLERANCE); // θ
    }

    @Test
    void testCartesianToPolarRadians_NegativeX() {
        CoordinateSystem polar = PolarCoordinateSystem.radians();
        Point3D cartesian = Point3D.of2D(-3, 0);

        Point3D polarPoint = polar.fromCartesian(cartesian);

        assertEquals(3.0, polarPoint.getX(), TOLERANCE); // r
        assertEquals(Math.PI, polarPoint.getY(), TOLERANCE); // θ
    }

    @Test
    void testCartesianToPolarRadians_NegativeY() {
        CoordinateSystem polar = PolarCoordinateSystem.radians();
        Point3D cartesian = Point3D.of2D(0, -5);

        Point3D polarPoint = polar.fromCartesian(cartesian);

        assertEquals(5.0, polarPoint.getX(), TOLERANCE); // r
        assertEquals(3 * Math.PI / 2, polarPoint.getY(), TOLERANCE); // θ (normalized to [0, 2π])
    }

    // ============================================================
    // Cartesian to Polar Tests (Degrees)
    // ============================================================

    @Test
    void testCartesianToPolarDegrees_PositiveX() {
        CoordinateSystem polar = PolarCoordinateSystem.degrees();
        Point3D cartesian = Point3D.of2D(5, 0);

        Point3D polarPoint = polar.fromCartesian(cartesian);

        assertEquals(5.0, polarPoint.getX(), TOLERANCE); // r
        assertEquals(0.0, polarPoint.getY(), TOLERANCE); // θ
    }

    @Test
    void testCartesianToPolarDegrees_PositiveY() {
        CoordinateSystem polar = PolarCoordinateSystem.degrees();
        Point3D cartesian = Point3D.of2D(0, 5);

        Point3D polarPoint = polar.fromCartesian(cartesian);

        assertEquals(5.0, polarPoint.getX(), TOLERANCE); // r
        assertEquals(90.0, polarPoint.getY(), TOLERANCE); // θ
    }

    @Test
    void testCartesianToPolarDegrees_NegativeX() {
        CoordinateSystem polar = PolarCoordinateSystem.degrees();
        Point3D cartesian = Point3D.of2D(-3, 0);

        Point3D polarPoint = polar.fromCartesian(cartesian);

        assertEquals(3.0, polarPoint.getX(), TOLERANCE); // r
        assertEquals(180.0, polarPoint.getY(), TOLERANCE); // θ
    }

    // ============================================================
    // Invertibility Tests
    // ============================================================

    @Test
    void testInvertibilityRadians() {
        CoordinateSystem polar = PolarCoordinateSystem.radians();
        Point3D original = Point3D.of2D(5, Math.PI / 3);

        Point3D cartesian = polar.toCartesian(original);
        Point3D backToPolar = polar.fromCartesian(cartesian);

        assertEquals(original.getX(), backToPolar.getX(), TOLERANCE);
        assertEquals(original.getY(), backToPolar.getY(), TOLERANCE);
    }

    @Test
    void testInvertibilityDegrees() {
        CoordinateSystem polar = PolarCoordinateSystem.degrees();
        Point3D original = Point3D.of2D(7, 120);

        Point3D cartesian = polar.toCartesian(original);
        Point3D backToPolar = polar.fromCartesian(cartesian);

        assertEquals(original.getX(), backToPolar.getX(), TOLERANCE);
        assertEquals(original.getY(), backToPolar.getY(), TOLERANCE);
    }

    @Test
    void testRoundTripCartesianRadians() {
        CoordinateSystem polar = PolarCoordinateSystem.radians();
        Point3D original = Point3D.of2D(3, 4);

        Point3D polarPoint = polar.fromCartesian(original);
        Point3D backToCartesian = polar.toCartesian(polarPoint);

        assertEquals(original.getX(), backToCartesian.getX(), TOLERANCE);
        assertEquals(original.getY(), backToCartesian.getY(), TOLERANCE);
    }

    // ============================================================
    // Validation and Edge Cases
    // ============================================================

    @Test
    void testRejects3DPoints() {
        CoordinateSystem polar = PolarCoordinateSystem.radians();
        Point3D point3D = Point3D.of3D(1, 2, 3);

        assertThrows(IllegalArgumentException.class, () ->
                polar.toCartesian(point3D));
        assertThrows(IllegalArgumentException.class, () ->
                polar.fromCartesian(point3D));
    }

    @Test
    void testZeroRadius() {
        CoordinateSystem polar = PolarCoordinateSystem.radians();
        Point3D polarPoint = Point3D.of2D(0, Math.PI / 4);

        Point3D cartesian = polar.toCartesian(polarPoint);

        assertEquals(0.0, cartesian.getX(), TOLERANCE);
        assertEquals(0.0, cartesian.getY(), TOLERANCE);
    }

    @Test
    void testOriginToAnyAngle() {
        CoordinateSystem polar = PolarCoordinateSystem.radians();
        Point3D cartesian = Point3D.of2D(0, 0);

        Point3D polarPoint = polar.fromCartesian(cartesian);

        assertEquals(0.0, polarPoint.getX(), TOLERANCE); // r=0
        // Angle can be anything for origin, just check it's normalized
        assertTrue(polarPoint.getY() >= 0 && polarPoint.getY() < 2 * Math.PI);
    }

    @Test
    void testFullCircleNormalization() {
        CoordinateSystem polar = PolarCoordinateSystem.radians();
        Point3D polarPoint = Point3D.of2D(5, 2 * Math.PI + 0.5); // θ > 2π

        Point3D cartesian = polar.toCartesian(polarPoint);
        Point3D expected = polar.toCartesian(Point3D.of2D(5, 0.5));

        assertEquals(expected.getX(), cartesian.getX(), TOLERANCE);
        assertEquals(expected.getY(), cartesian.getY(), TOLERANCE);
    }

    // ============================================================
    // Properties Tests
    // ============================================================

    @Test
    void testGetDimensions() {
        CoordinateSystem polar = PolarCoordinateSystem.radians();
        assertEquals(2, polar.getDimensions());
    }

    @Test
    void testGetName() {
        CoordinateSystem polarRad = PolarCoordinateSystem.radians();
        CoordinateSystem polarDeg = PolarCoordinateSystem.degrees();

        assertEquals("Polar(RADIANS)", polarRad.getName());
        assertEquals("Polar(DEGREES)", polarDeg.getName());
    }

    @Test
    void testGetAngleUnit() {
        PolarCoordinateSystem polarRad = PolarCoordinateSystem.radians();
        PolarCoordinateSystem polarDeg = PolarCoordinateSystem.degrees();

        assertEquals(AngleUnit.RADIANS, polarRad.getAngleUnit());
        assertEquals(AngleUnit.DEGREES, polarDeg.getAngleUnit());
    }

    @Test
    void testToString() {
        CoordinateSystem polarRad = PolarCoordinateSystem.radians();
        CoordinateSystem polarDeg = PolarCoordinateSystem.degrees();

        assertEquals("Polar(RADIANS)", polarRad.toString());
        assertEquals("Polar(DEGREES)", polarDeg.toString());
    }
}
