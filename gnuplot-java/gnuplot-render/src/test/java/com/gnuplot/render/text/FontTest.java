package com.gnuplot.render.text;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for Font class.
 */
class FontTest {

    @Test
    void testFontConstruction() {
        Font font = new Font("Arial", 12, Font.PLAIN);
        assertEquals("Arial", font.family());
        assertEquals(12, font.size());
        assertEquals(Font.PLAIN, font.style());
    }

    @Test
    void testInvalidFamily() {
        assertThrows(IllegalArgumentException.class, () ->
                new Font(null, 12, Font.PLAIN)
        );
        assertThrows(IllegalArgumentException.class, () ->
                new Font("", 12, Font.PLAIN)
        );
        assertThrows(IllegalArgumentException.class, () ->
                new Font("  ", 12, Font.PLAIN)
        );
    }

    @Test
    void testInvalidSize() {
        assertThrows(IllegalArgumentException.class, () ->
                new Font("Arial", 0, Font.PLAIN)
        );
        assertThrows(IllegalArgumentException.class, () ->
                new Font("Arial", -5, Font.PLAIN)
        );
    }

    @Test
    void testInvalidStyle() {
        assertThrows(IllegalArgumentException.class, () ->
                new Font("Arial", 12, -1)
        );
        assertThrows(IllegalArgumentException.class, () ->
                new Font("Arial", 12, 4)
        );
    }

    @Test
    void testBoldFont() {
        Font font = Font.bold("Arial", 12);
        assertTrue(font.isBold());
        assertFalse(font.isItalic());
        assertEquals(Font.BOLD, font.style());
    }

    @Test
    void testItalicFont() {
        Font font = Font.italic("Arial", 12);
        assertFalse(font.isBold());
        assertTrue(font.isItalic());
        assertEquals(Font.ITALIC, font.style());
    }

    @Test
    void testBoldItalicFont() {
        Font font = Font.boldItalic("Arial", 12);
        assertTrue(font.isBold());
        assertTrue(font.isItalic());
        assertEquals(Font.BOLD_ITALIC, font.style());
    }

    @Test
    void testPlainFont() {
        Font font = Font.plain("Arial", 12);
        assertFalse(font.isBold());
        assertFalse(font.isItalic());
        assertEquals(Font.PLAIN, font.style());
    }

    @Test
    void testWithSize() {
        Font original = Font.plain("Arial", 12);
        Font resized = original.withSize(18);

        assertEquals("Arial", resized.family());
        assertEquals(18, resized.size());
        assertEquals(Font.PLAIN, resized.style());
    }

    @Test
    void testWithFamily() {
        Font original = Font.plain("Arial", 12);
        Font newFamily = original.withFamily("Times New Roman");

        assertEquals("Times New Roman", newFamily.family());
        assertEquals(12, newFamily.size());
        assertEquals(Font.PLAIN, newFamily.style());
    }

    @Test
    void testWithStyle() {
        Font original = Font.plain("Arial", 12);
        Font bold = original.withStyle(Font.BOLD);

        assertEquals("Arial", bold.family());
        assertEquals(12, bold.size());
        assertEquals(Font.BOLD, bold.style());
    }

    @Test
    void testToCssString() {
        Font plain = Font.plain("Arial", 12);
        assertEquals("12.0pt 'Arial'", plain.toCssString());

        Font bold = Font.bold("Arial", 12);
        assertEquals("bold 12.0pt 'Arial'", bold.toCssString());

        Font italic = Font.italic("Arial", 12);
        assertEquals("italic 12.0pt 'Arial'", italic.toCssString());

        Font boldItalic = Font.boldItalic("Arial", 12);
        assertEquals("italic bold 12.0pt 'Arial'", boldItalic.toCssString());
    }

    @Test
    void testDefaultFonts() {
        assertNotNull(Font.DEFAULT);
        assertNotNull(Font.TITLE);
        assertNotNull(Font.AXIS_LABEL);
        assertNotNull(Font.TICK_LABEL);
        assertNotNull(Font.LEGEND);
        assertNotNull(Font.MONOSPACE);

        // Verify default sizes
        assertEquals(12, Font.DEFAULT.size());
        assertEquals(16, Font.TITLE.size());
        assertEquals(10, Font.AXIS_LABEL.size());

        // Verify styles
        assertTrue(Font.TITLE.isBold());
        assertFalse(Font.DEFAULT.isBold());
    }

    @Test
    void testEquality() {
        Font f1 = Font.plain("Arial", 12);
        Font f2 = Font.plain("Arial", 12);
        Font f3 = Font.plain("Arial", 14);
        Font f4 = Font.bold("Arial", 12);

        assertEquals(f1, f2);
        assertNotEquals(f1, f3);
        assertNotEquals(f1, f4);
    }
}
