package com.gnuplot.render.text;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.awt.geom.Rectangle2D;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for TextRenderer class.
 */
class TextRendererTest {

    private TextRenderer renderer;

    @BeforeEach
    void setUp() {
        renderer = TextRenderer.create();
    }

    @Test
    void testMeasureText() {
        Font font = Font.plain("Arial", 12);
        TextMetrics metrics = renderer.measureText("Hello", font);

        // Text should have positive dimensions
        assertTrue(metrics.width() > 0);
        assertTrue(metrics.height() > 0);
        assertTrue(metrics.ascent() > 0);
        assertTrue(metrics.descent() >= 0);
    }

    @Test
    void testMeasureEmptyText() {
        Font font = Font.plain("Arial", 12);

        TextMetrics empty = renderer.measureText("", font);
        assertEquals(TextMetrics.empty(), empty);

        TextMetrics nullMetrics = renderer.measureText(null, font);
        assertEquals(TextMetrics.empty(), nullMetrics);
    }

    @Test
    void testMeasureTextSizeScaling() {
        Font small = Font.plain("Arial", 10);
        Font large = Font.plain("Arial", 20);

        TextMetrics smallMetrics = renderer.measureText("Test", small);
        TextMetrics largeMetrics = renderer.measureText("Test", large);

        // Larger font should produce larger dimensions
        assertTrue(largeMetrics.width() > smallMetrics.width());
        assertTrue(largeMetrics.height() > smallMetrics.height());
    }

    @Test
    void testMeasureTextBoldImpact() {
        Font plain = Font.plain("Arial", 12);
        Font bold = Font.bold("Arial", 12);

        TextMetrics plainMetrics = renderer.measureText("Test", plain);
        TextMetrics boldMetrics = renderer.measureText("Test", bold);

        // Bold text is typically wider
        assertTrue(boldMetrics.width() >= plainMetrics.width());
    }

    @Test
    void testGetRotatedBounds() {
        Font font = Font.plain("Arial", 12);

        // No rotation
        Rectangle2D bounds0 = renderer.getRotatedBounds("Hello", font, 0);
        assertTrue(bounds0.getWidth() > 0);
        assertTrue(bounds0.getHeight() > 0);

        // 90 degree rotation - width and height should swap approximately
        Rectangle2D bounds90 = renderer.getRotatedBounds("Hello", font, 90);
        assertTrue(bounds90.getWidth() > 0);
        assertTrue(bounds90.getHeight() > 0);

        // 180 degree rotation - dimensions should be similar to 0 degrees
        Rectangle2D bounds180 = renderer.getRotatedBounds("Hello", font, 180);
        assertEquals(bounds0.getWidth(), bounds180.getWidth(), 5);
        assertEquals(bounds0.getHeight(), bounds180.getHeight(), 5);
    }

    @Test
    void testGetRotatedBoundsEmpty() {
        Font font = Font.plain("Arial", 12);

        Rectangle2D empty = renderer.getRotatedBounds("", font, 45);
        assertEquals(0, empty.getWidth());
        assertEquals(0, empty.getHeight());

        Rectangle2D nullBounds = renderer.getRotatedBounds(null, font, 45);
        assertEquals(0, nullBounds.getWidth());
        assertEquals(0, nullBounds.getHeight());
    }

    @Test
    void testGetBaselinePosition() {
        Font font = Font.plain("Arial", 12);

        // Left alignment
        double[] leftPos = renderer.getBaselinePosition(100, 50, "Test", font, TextAlignment.LEFT);
        assertEquals(100, leftPos[0], 1);
        assertEquals(50, leftPos[1]);

        // Center alignment
        double[] centerPos = renderer.getBaselinePosition(100, 50, "Test", font, TextAlignment.CENTER);
        assertTrue(centerPos[0] < 100); // Should be offset to the left
        assertEquals(50, centerPos[1]);

        // Right alignment
        double[] rightPos = renderer.getBaselinePosition(100, 50, "Test", font, TextAlignment.RIGHT);
        assertTrue(rightPos[0] < 100); // Should be offset to the left
        assertEquals(50, rightPos[1]);
    }

    @Test
    void testIsValidUnicode() {
        // Valid Unicode
        assertTrue(renderer.isValidUnicode("Hello"));
        assertTrue(renderer.isValidUnicode("Hello World"));
        assertTrue(renderer.isValidUnicode("你好")); // Chinese
        assertTrue(renderer.isValidUnicode("مرحبا")); // Arabic
        assertTrue(renderer.isValidUnicode("Γειά σου")); // Greek
        assertTrue(renderer.isValidUnicode("😀")); // Emoji

        // Invalid Unicode
        assertFalse(renderer.isValidUnicode(null));

        // Unpaired surrogates (invalid)
        assertFalse(renderer.isValidUnicode("\uD800")); // High surrogate alone
        assertFalse(renderer.isValidUnicode("\uDC00")); // Low surrogate alone
    }

    @Test
    void testEscapeForSvg() {
        assertEquals("", renderer.escapeForSvg(null));
        assertEquals("Hello", renderer.escapeForSvg("Hello"));

        // XML special characters
        assertEquals("&lt;tag&gt;", renderer.escapeForSvg("<tag>"));
        assertEquals("A &amp; B", renderer.escapeForSvg("A & B"));
        assertEquals("&quot;quoted&quot;", renderer.escapeForSvg("\"quoted\""));
        assertEquals("&apos;apostrophe&apos;", renderer.escapeForSvg("'apostrophe'"));

        // Multiple special characters
        assertEquals("&lt;a&gt; &amp; &quot;b&quot;",
                    renderer.escapeForSvg("<a> & \"b\""));
    }

    @Test
    void testUnicodeTextMeasurement() {
        Font font = Font.plain("Arial", 12);

        // Unicode text should be measurable
        TextMetrics chinese = renderer.measureText("你好", font);
        assertTrue(chinese.width() > 0);
        assertTrue(chinese.height() > 0);

        TextMetrics emoji = renderer.measureText("😀", font);
        assertTrue(emoji.width() > 0);
        assertTrue(emoji.height() > 0);
    }

    @Test
    void testTextRendererCreation() {
        TextRenderer renderer1 = TextRenderer.create();
        TextRenderer renderer2 = TextRenderer.create();

        assertNotNull(renderer1);
        assertNotNull(renderer2);
        assertNotSame(renderer1, renderer2);
    }
}
