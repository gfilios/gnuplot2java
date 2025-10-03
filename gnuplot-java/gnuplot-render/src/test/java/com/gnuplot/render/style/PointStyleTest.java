package com.gnuplot.render.style;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for PointStyle enum.
 */
class PointStyleTest {

    @Test
    void testGetSides() {
        assertEquals(3, PointStyle.TRIANGLE_UP.getSides());
        assertEquals(3, PointStyle.TRIANGLE_DOWN.getSides());
        assertEquals(4, PointStyle.SQUARE.getSides());
        assertEquals(4, PointStyle.DIAMOND.getSides());
        assertEquals(5, PointStyle.PENTAGON.getSides());
        assertEquals(6, PointStyle.HEXAGON.getSides());
        assertEquals(-1, PointStyle.CIRCLE.getSides());
        assertEquals(-1, PointStyle.PLUS.getSides());
        assertEquals(-1, PointStyle.CROSS.getSides());
        assertEquals(-1, PointStyle.STAR.getSides());
    }

    @Test
    void testIsFilledByDefault() {
        assertTrue(PointStyle.CIRCLE.isFilledByDefault());
        assertTrue(PointStyle.SQUARE.isFilledByDefault());
        assertTrue(PointStyle.TRIANGLE_UP.isFilledByDefault());
        assertTrue(PointStyle.DIAMOND.isFilledByDefault());
        assertFalse(PointStyle.PLUS.isFilledByDefault());
        assertFalse(PointStyle.CROSS.isFilledByDefault());
    }

    @Test
    void testIsLineBased() {
        assertTrue(PointStyle.PLUS.isLineBased());
        assertTrue(PointStyle.CROSS.isLineBased());
        assertFalse(PointStyle.CIRCLE.isLineBased());
        assertFalse(PointStyle.SQUARE.isLineBased());
    }
}
