package com.gnuplot.render.style;

import com.gnuplot.render.color.Color;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for MarkerStyle record.
 */
class MarkerStyleTest {

    @Test
    void testMarkerStyleCreation() {
        MarkerStyle marker = new MarkerStyle(4.0, Color.RED, PointStyle.CIRCLE, true);
        assertEquals(4.0, marker.size());
        assertEquals(Color.RED, marker.color());
        assertEquals(PointStyle.CIRCLE, marker.pointStyle());
        assertTrue(marker.filled());
    }

    @Test
    void testFilledFactory() {
        MarkerStyle marker = MarkerStyle.filled(5.0, Color.BLUE, PointStyle.SQUARE);
        assertTrue(marker.filled());
        assertEquals(5.0, marker.size());
    }

    @Test
    void testUnfilledFactory() {
        MarkerStyle marker = MarkerStyle.unfilled(5.0, Color.GREEN, PointStyle.TRIANGLE_UP);
        assertFalse(marker.filled());
    }

    @Test
    void testWithDefaultFill() {
        MarkerStyle filledByDefault = MarkerStyle.withDefaultFill(4.0, Color.RED, PointStyle.CIRCLE);
        assertTrue(filledByDefault.filled());

        MarkerStyle unfilledByDefault = MarkerStyle.withDefaultFill(4.0, Color.RED, PointStyle.PLUS);
        assertFalse(unfilledByDefault.filled());
    }

    @Test
    void testWithSize() {
        MarkerStyle original = MarkerStyle.DEFAULT;
        MarkerStyle modified = original.withSize(10.0);
        assertEquals(10.0, modified.size());
        assertEquals(original.color(), modified.color());
        assertEquals(original.pointStyle(), modified.pointStyle());
    }

    @Test
    void testWithColor() {
        MarkerStyle original = MarkerStyle.DEFAULT;
        MarkerStyle modified = original.withColor(Color.BLUE);
        assertEquals(Color.BLUE, modified.color());
        assertEquals(original.size(), modified.size());
    }

    @Test
    void testWithPointStyle() {
        MarkerStyle original = MarkerStyle.DEFAULT;
        MarkerStyle modified = original.withPointStyle(PointStyle.SQUARE);
        assertEquals(PointStyle.SQUARE, modified.pointStyle());
    }

    @Test
    void testWithFilled() {
        MarkerStyle filled = MarkerStyle.filled(4.0, Color.RED, PointStyle.CIRCLE);
        MarkerStyle unfilled = filled.withFilled(false);
        assertFalse(unfilled.filled());
    }

    @Test
    void testGetColorHex() {
        MarkerStyle marker = MarkerStyle.filled(4.0, Color.RED, PointStyle.CIRCLE);
        assertEquals("#FF0000", marker.getColorHex());
    }

    @Test
    void testGetStrokeWidth() {
        MarkerStyle small = MarkerStyle.filled(3.0, Color.RED, PointStyle.CIRCLE);
        MarkerStyle large = MarkerStyle.filled(12.0, Color.RED, PointStyle.CIRCLE);
        assertTrue(small.getStrokeWidth() >= 1.0);
        assertTrue(large.getStrokeWidth() > small.getStrokeWidth());
    }

    @Test
    void testPredefinedStyles() {
        assertNotNull(MarkerStyle.DEFAULT);
        assertNotNull(MarkerStyle.SMALL);
        assertNotNull(MarkerStyle.LARGE);
        assertNotNull(MarkerStyle.RED_CIRCLE);
        assertNotNull(MarkerStyle.BLUE_SQUARE);
        assertNotNull(MarkerStyle.GREEN_TRIANGLE);
    }

    @Test
    void testInvalidSize() {
        assertThrows(IllegalArgumentException.class, () ->
                new MarkerStyle(0, Color.RED, PointStyle.CIRCLE, true));
        assertThrows(IllegalArgumentException.class, () ->
                new MarkerStyle(-1, Color.RED, PointStyle.CIRCLE, true));
    }

    @Test
    void testNullColor() {
        assertThrows(NullPointerException.class, () ->
                new MarkerStyle(4.0, null, PointStyle.CIRCLE, true));
    }

    @Test
    void testNullPointStyle() {
        assertThrows(NullPointerException.class, () ->
                new MarkerStyle(4.0, Color.RED, null, true));
    }
}
