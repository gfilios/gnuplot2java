package com.gnuplot.render.text;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for TextMetrics class.
 */
class TextMetricsTest {

    private static final double EPSILON = 1e-10;

    @Test
    void testTextMetricsConstruction() {
        TextMetrics metrics = new TextMetrics(100, 20, 15, 5);

        assertEquals(100, metrics.width(), EPSILON);
        assertEquals(20, metrics.height(), EPSILON);
        assertEquals(15, metrics.ascent(), EPSILON);
        assertEquals(5, metrics.descent(), EPSILON);
    }

    @Test
    void testBaseline() {
        TextMetrics metrics = new TextMetrics(100, 20, 15, 5);
        assertEquals(15, metrics.baseline(), EPSILON);
    }

    @Test
    void testEmpty() {
        TextMetrics empty = TextMetrics.empty();

        assertEquals(0, empty.width(), EPSILON);
        assertEquals(0, empty.height(), EPSILON);
        assertEquals(0, empty.ascent(), EPSILON);
        assertEquals(0, empty.descent(), EPSILON);
    }

    @Test
    void testInvalidWidth() {
        assertThrows(IllegalArgumentException.class, () ->
                new TextMetrics(-1, 20, 15, 5)
        );
    }

    @Test
    void testInvalidHeight() {
        assertThrows(IllegalArgumentException.class, () ->
                new TextMetrics(100, -1, 15, 5)
        );
    }

    @Test
    void testInvalidAscent() {
        assertThrows(IllegalArgumentException.class, () ->
                new TextMetrics(100, 20, -1, 5)
        );
    }

    @Test
    void testInvalidDescent() {
        assertThrows(IllegalArgumentException.class, () ->
                new TextMetrics(100, 20, 15, -1)
        );
    }

    @Test
    void testZeroMetrics() {
        // Zero values should be valid
        TextMetrics metrics = new TextMetrics(0, 0, 0, 0);
        assertNotNull(metrics);
    }

    @Test
    void testEquality() {
        TextMetrics m1 = new TextMetrics(100, 20, 15, 5);
        TextMetrics m2 = new TextMetrics(100, 20, 15, 5);
        TextMetrics m3 = new TextMetrics(100, 20, 16, 5);

        assertEquals(m1, m2);
        assertNotEquals(m1, m3);
    }
}
