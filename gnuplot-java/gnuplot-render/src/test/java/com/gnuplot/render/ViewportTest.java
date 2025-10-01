package com.gnuplot.render;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for Viewport class.
 */
class ViewportTest {

    @Test
    void testOf2D() {
        Viewport viewport = Viewport.of2D(0.0, 10.0, -5.0, 5.0);

        assertEquals(0.0, viewport.getXMin());
        assertEquals(10.0, viewport.getXMax());
        assertEquals(-5.0, viewport.getYMin());
        assertEquals(5.0, viewport.getYMax());
        assertFalse(viewport.is3D());
    }

    @Test
    void testOf3D() {
        Viewport viewport = Viewport.of3D(0.0, 10.0, -5.0, 5.0, -1.0, 1.0);

        assertEquals(0.0, viewport.getXMin());
        assertEquals(10.0, viewport.getXMax());
        assertEquals(-5.0, viewport.getYMin());
        assertEquals(5.0, viewport.getYMax());
        assertEquals(-1.0, viewport.getZMin());
        assertEquals(1.0, viewport.getZMax());
        assertTrue(viewport.is3D());
    }

    @Test
    void testBuilder2D() {
        Viewport viewport = Viewport.builder()
                .xRange(0.0, 100.0)
                .yRange(0.0, 50.0)
                .build();

        assertEquals(0.0, viewport.getXMin());
        assertEquals(100.0, viewport.getXMax());
        assertEquals(0.0, viewport.getYMin());
        assertEquals(50.0, viewport.getYMax());
        assertFalse(viewport.is3D());
    }

    @Test
    void testBuilder3D() {
        Viewport viewport = Viewport.builder()
                .xRange(0.0, 100.0)
                .yRange(0.0, 50.0)
                .zRange(-10.0, 10.0)
                .build();

        assertEquals(0.0, viewport.getXMin());
        assertEquals(100.0, viewport.getXMax());
        assertEquals(0.0, viewport.getYMin());
        assertEquals(50.0, viewport.getYMax());
        assertEquals(-10.0, viewport.getZMin());
        assertEquals(10.0, viewport.getZMax());
        assertTrue(viewport.is3D());
    }

    @Test
    void testInvalidXRange() {
        assertThrows(IllegalArgumentException.class, () ->
                Viewport.of2D(10.0, 10.0, 0.0, 10.0));

        assertThrows(IllegalArgumentException.class, () ->
                Viewport.of2D(10.0, 5.0, 0.0, 10.0));
    }

    @Test
    void testInvalidYRange() {
        assertThrows(IllegalArgumentException.class, () ->
                Viewport.of2D(0.0, 10.0, 10.0, 10.0));

        assertThrows(IllegalArgumentException.class, () ->
                Viewport.of2D(0.0, 10.0, 10.0, 5.0));
    }

    @Test
    void testInvalidZRange() {
        assertThrows(IllegalArgumentException.class, () ->
                Viewport.of3D(0.0, 10.0, 0.0, 10.0, 10.0, 10.0));

        assertThrows(IllegalArgumentException.class, () ->
                Viewport.of3D(0.0, 10.0, 0.0, 10.0, 10.0, 5.0));
    }

    @Test
    void testGetWidth() {
        Viewport viewport = Viewport.of2D(0.0, 10.0, 0.0, 5.0);
        assertEquals(10.0, viewport.getWidth(), 1e-10);
    }

    @Test
    void testGetHeight() {
        Viewport viewport = Viewport.of2D(0.0, 10.0, 0.0, 5.0);
        assertEquals(5.0, viewport.getHeight(), 1e-10);
    }

    @Test
    void testGetDepth2D() {
        Viewport viewport = Viewport.of2D(0.0, 10.0, 0.0, 5.0);
        assertEquals(0.0, viewport.getDepth(), 1e-10);
    }

    @Test
    void testGetDepth3D() {
        Viewport viewport = Viewport.of3D(0.0, 10.0, 0.0, 5.0, -2.0, 8.0);
        assertEquals(10.0, viewport.getDepth(), 1e-10);
    }

    @Test
    void testGetAspectRatio() {
        Viewport viewport = Viewport.of2D(0.0, 16.0, 0.0, 9.0);
        assertEquals(16.0 / 9.0, viewport.getAspectRatio(), 1e-10);
    }

    @Test
    void testContains2D() {
        Viewport viewport = Viewport.of2D(0.0, 10.0, -5.0, 5.0);

        assertTrue(viewport.contains(5.0, 0.0));
        assertTrue(viewport.contains(0.0, -5.0)); // boundary
        assertTrue(viewport.contains(10.0, 5.0)); // boundary
        assertFalse(viewport.contains(-1.0, 0.0));
        assertFalse(viewport.contains(11.0, 0.0));
        assertFalse(viewport.contains(5.0, -6.0));
        assertFalse(viewport.contains(5.0, 6.0));
    }

    @Test
    void testContains3D() {
        Viewport viewport = Viewport.of3D(0.0, 10.0, -5.0, 5.0, -1.0, 1.0);

        assertTrue(viewport.contains(5.0, 0.0, 0.0));
        assertTrue(viewport.contains(0.0, -5.0, -1.0)); // boundary
        assertTrue(viewport.contains(10.0, 5.0, 1.0)); // boundary
        assertFalse(viewport.contains(5.0, 0.0, -2.0));
        assertFalse(viewport.contains(5.0, 0.0, 2.0));
        assertFalse(viewport.contains(-1.0, 0.0, 0.0));
    }

    @Test
    void testToString2D() {
        Viewport viewport = Viewport.of2D(0.0, 10.0, -5.0, 5.0);
        String str = viewport.toString();

        assertTrue(str.contains("Viewport2D"));
        // Check for numbers - format may vary by locale
        assertTrue(str.matches(".*[0\\.,]\\d{2}.*")); // contains formatted numbers
    }

    @Test
    void testToString3D() {
        Viewport viewport = Viewport.of3D(0.0, 10.0, -5.0, 5.0, -1.0, 1.0);
        String str = viewport.toString();

        assertTrue(str.contains("Viewport3D"));
        assertTrue(str.contains("x="));
        assertTrue(str.contains("y="));
        assertTrue(str.contains("z="));
    }

    @Test
    void testNegativeCoordinates() {
        Viewport viewport = Viewport.of2D(-10.0, -5.0, -20.0, -10.0);

        assertEquals(-10.0, viewport.getXMin());
        assertEquals(-5.0, viewport.getXMax());
        assertEquals(-20.0, viewport.getYMin());
        assertEquals(-10.0, viewport.getYMax());
        assertEquals(5.0, viewport.getWidth(), 1e-10);
        assertEquals(10.0, viewport.getHeight(), 1e-10);
    }
}
