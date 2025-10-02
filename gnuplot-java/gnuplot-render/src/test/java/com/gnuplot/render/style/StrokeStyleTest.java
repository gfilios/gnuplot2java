package com.gnuplot.render.style;

import com.gnuplot.render.color.Color;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for StrokeStyle class.
 */
class StrokeStyleTest {

    @Test
    void testStrokeConstruction() {
        StrokeStyle stroke = new StrokeStyle(2.0, Color.RED, LineStyle.SOLID);

        assertEquals(2.0, stroke.width());
        assertEquals(Color.RED, stroke.color());
        assertEquals(LineStyle.SOLID, stroke.lineStyle());
    }

    @Test
    void testInvalidWidth() {
        assertThrows(IllegalArgumentException.class, () ->
                new StrokeStyle(0, Color.RED, LineStyle.SOLID)
        );
        assertThrows(IllegalArgumentException.class, () ->
                new StrokeStyle(-1, Color.RED, LineStyle.SOLID)
        );
    }

    @Test
    void testNullColor() {
        assertThrows(IllegalArgumentException.class, () ->
                new StrokeStyle(1.0, null, LineStyle.SOLID)
        );
    }

    @Test
    void testNullLineStyle() {
        assertThrows(IllegalArgumentException.class, () ->
                new StrokeStyle(1.0, Color.RED, null)
        );
    }

    @Test
    void testSolidStroke() {
        StrokeStyle stroke = StrokeStyle.solid(1.5, Color.BLUE);

        assertEquals(1.5, stroke.width());
        assertEquals(Color.BLUE, stroke.color());
        assertEquals(LineStyle.SOLID, stroke.lineStyle());
    }

    @Test
    void testDashedStroke() {
        StrokeStyle stroke = StrokeStyle.dashed(2.0, Color.GREEN);

        assertEquals(2.0, stroke.width());
        assertEquals(Color.GREEN, stroke.color());
        assertEquals(LineStyle.DASHED, stroke.lineStyle());
    }

    @Test
    void testDottedStroke() {
        StrokeStyle stroke = StrokeStyle.dotted(1.0, Color.RED);

        assertEquals(1.0, stroke.width());
        assertEquals(Color.RED, stroke.color());
        assertEquals(LineStyle.DOTTED, stroke.lineStyle());
    }

    @Test
    void testWithWidth() {
        StrokeStyle original = StrokeStyle.solid(1.0, Color.BLACK);
        StrokeStyle modified = original.withWidth(3.0);

        assertEquals(3.0, modified.width());
        assertEquals(Color.BLACK, modified.color());
        assertEquals(LineStyle.SOLID, modified.lineStyle());

        // Original should be unchanged
        assertEquals(1.0, original.width());
    }

    @Test
    void testWithColor() {
        StrokeStyle original = StrokeStyle.solid(1.0, Color.BLACK);
        StrokeStyle modified = original.withColor(Color.RED);

        assertEquals(1.0, modified.width());
        assertEquals(Color.RED, modified.color());
        assertEquals(LineStyle.SOLID, modified.lineStyle());

        // Original should be unchanged
        assertEquals(Color.BLACK, original.color());
    }

    @Test
    void testWithLineStyle() {
        StrokeStyle original = StrokeStyle.solid(1.0, Color.BLACK);
        StrokeStyle modified = original.withLineStyle(LineStyle.DASHED);

        assertEquals(1.0, modified.width());
        assertEquals(Color.BLACK, modified.color());
        assertEquals(LineStyle.DASHED, modified.lineStyle());

        // Original should be unchanged
        assertEquals(LineStyle.SOLID, original.lineStyle());
    }

    @Test
    void testToSvgAttributesSolid() {
        StrokeStyle stroke = StrokeStyle.solid(2.0, Color.RED);
        String svg = stroke.toSvgAttributes();

        assertTrue(svg.contains("stroke=\"#FF0000\""));
        assertTrue(svg.contains("stroke-width=\"2.0\""));
        assertTrue(svg.contains("stroke-linecap=\"round\""));
        assertTrue(svg.contains("stroke-linejoin=\"round\""));
        assertFalse(svg.contains("stroke-dasharray"));
    }

    @Test
    void testToSvgAttributesDashed() {
        StrokeStyle stroke = StrokeStyle.dashed(1.5, Color.BLUE);
        String svg = stroke.toSvgAttributes();

        assertTrue(svg.contains("stroke=\"#0000FF\""));
        assertTrue(svg.contains("stroke-width=\"1.5\""));
        assertTrue(svg.contains("stroke-dasharray=\"8,4\""));
        assertTrue(svg.contains("stroke-linecap=\"round\""));
    }

    @Test
    void testDefaultStrokes() {
        assertNotNull(StrokeStyle.DEFAULT);
        assertNotNull(StrokeStyle.THIN);
        assertNotNull(StrokeStyle.THICK);
        assertNotNull(StrokeStyle.AXIS);
        assertNotNull(StrokeStyle.GRID);

        // Verify default values
        assertEquals(1.0, StrokeStyle.DEFAULT.width());
        assertEquals(Color.BLACK, StrokeStyle.DEFAULT.color());

        assertEquals(0.5, StrokeStyle.THIN.width());
        assertEquals(2.0, StrokeStyle.THICK.width());

        assertEquals(LineStyle.DASHED, StrokeStyle.GRID.lineStyle());
    }

    @Test
    void testEquality() {
        StrokeStyle s1 = StrokeStyle.solid(1.0, Color.BLACK);
        StrokeStyle s2 = StrokeStyle.solid(1.0, Color.BLACK);
        StrokeStyle s3 = StrokeStyle.solid(2.0, Color.BLACK);

        assertEquals(s1, s2);
        assertNotEquals(s1, s3);
    }
}
