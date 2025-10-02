package com.gnuplot.render.text;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for TextAlignment enum.
 */
class TextAlignmentTest {

    private static final double EPSILON = 1e-10;

    @Test
    void testLeftAlignment() {
        TextAlignment alignment = TextAlignment.LEFT;
        assertEquals(0, alignment.getXOffset(100), EPSILON);
    }

    @Test
    void testCenterAlignment() {
        TextAlignment alignment = TextAlignment.CENTER;
        assertEquals(-50, alignment.getXOffset(100), EPSILON);
    }

    @Test
    void testRightAlignment() {
        TextAlignment alignment = TextAlignment.RIGHT;
        assertEquals(-100, alignment.getXOffset(100), EPSILON);
    }

    @Test
    void testAllValues() {
        // Ensure all enum values are accessible
        assertNotNull(TextAlignment.LEFT);
        assertNotNull(TextAlignment.CENTER);
        assertNotNull(TextAlignment.RIGHT);

        assertEquals(3, TextAlignment.values().length);
    }
}
