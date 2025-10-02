package com.gnuplot.render.style;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for LineStyle enum.
 */
class LineStyleTest {

    @Test
    void testSolidLine() {
        LineStyle solid = LineStyle.SOLID;
        assertEquals("none", solid.getSvgDashArray());
        assertTrue(solid.isSolid());
    }

    @Test
    void testDashedLine() {
        LineStyle dashed = LineStyle.DASHED;
        assertEquals("8,4", dashed.getSvgDashArray());
        assertFalse(dashed.isSolid());
    }

    @Test
    void testDottedLine() {
        LineStyle dotted = LineStyle.DOTTED;
        assertEquals("2,2", dotted.getSvgDashArray());
        assertFalse(dotted.isSolid());
    }

    @Test
    void testDashDotLine() {
        LineStyle dashDot = LineStyle.DASH_DOT;
        assertEquals("8,4,2,4", dashDot.getSvgDashArray());
        assertFalse(dashDot.isSolid());
    }

    @Test
    void testDashDotDotLine() {
        LineStyle dashDotDot = LineStyle.DASH_DOT_DOT;
        assertEquals("8,4,2,4,2,4", dashDotDot.getSvgDashArray());
        assertFalse(dashDotDot.isSolid());
    }

    @Test
    void testLongDashLine() {
        LineStyle longDash = LineStyle.LONG_DASH;
        assertEquals("12,4", longDash.getSvgDashArray());
        assertFalse(longDash.isSolid());
    }

    @Test
    void testShortDashLine() {
        LineStyle shortDash = LineStyle.SHORT_DASH;
        assertEquals("4,2", shortDash.getSvgDashArray());
        assertFalse(shortDash.isSolid());
    }

    @Test
    void testAllValues() {
        // Ensure all enum values are accessible
        assertNotNull(LineStyle.SOLID);
        assertNotNull(LineStyle.DASHED);
        assertNotNull(LineStyle.DOTTED);
        assertNotNull(LineStyle.DASH_DOT);
        assertNotNull(LineStyle.DASH_DOT_DOT);
        assertNotNull(LineStyle.LONG_DASH);
        assertNotNull(LineStyle.SHORT_DASH);

        assertEquals(7, LineStyle.values().length);
    }
}
