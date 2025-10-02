package com.gnuplot.render.color;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the Color class.
 */
class ColorTest {

    private static final double EPSILON = 1e-10;

    @Test
    void testColorConstruction() {
        Color color = new Color(0.5, 0.3, 0.1);
        assertEquals(0.5, color.r(), EPSILON);
        assertEquals(0.3, color.g(), EPSILON);
        assertEquals(0.1, color.b(), EPSILON);
    }

    @Test
    void testColorClamping() {
        // Values should be clamped to [0, 1]
        Color color = new Color(-0.1, 1.5, 0.5);
        assertEquals(0.0, color.r(), EPSILON);
        assertEquals(1.0, color.g(), EPSILON);
        assertEquals(0.5, color.b(), EPSILON);
    }

    @Test
    void testFromRGB255() {
        Color color = Color.fromRGB255(255, 128, 0);
        assertEquals(1.0, color.r(), EPSILON);
        assertEquals(128.0 / 255.0, color.g(), 0.01);
        assertEquals(0.0, color.b(), EPSILON);
    }

    @Test
    void testFromRGB24() {
        Color color = Color.fromRGB24(0xFF8000); // Orange
        assertEquals(1.0, color.r(), EPSILON);
        assertEquals(128.0 / 255.0, color.g(), 0.01);
        assertEquals(0.0, color.b(), EPSILON);
    }

    @Test
    void testToRGB24() {
        Color color = new Color(1.0, 0.5, 0.0);
        int rgb = color.toRGB24();
        // 0.5 * 255 = 127.5, which rounds to 128 (0x80)
        // So the result should be 0xFF8000
        assertEquals(0xFF8000, rgb);
    }

    @Test
    void testGrayscale() {
        Color gray = Color.gray(0.5);
        assertEquals(0.5, gray.r(), EPSILON);
        assertEquals(0.5, gray.g(), EPSILON);
        assertEquals(0.5, gray.b(), EPSILON);
    }

    @Test
    void testInterpolate() {
        Color black = Color.BLACK;
        Color white = Color.WHITE;

        Color mid = black.interpolate(white, 0.5);
        assertEquals(0.5, mid.r(), EPSILON);
        assertEquals(0.5, mid.g(), EPSILON);
        assertEquals(0.5, mid.b(), EPSILON);

        Color quarter = black.interpolate(white, 0.25);
        assertEquals(0.25, quarter.r(), EPSILON);
        assertEquals(0.25, quarter.g(), EPSILON);
        assertEquals(0.25, quarter.b(), EPSILON);
    }

    @Test
    void testHSVConversion() {
        // Test red (0°, 100%, 100%)
        Color red = Color.fromHSV(0, 1.0, 1.0);
        assertEquals(1.0, red.r(), EPSILON);
        assertEquals(0.0, red.g(), EPSILON);
        assertEquals(0.0, red.b(), EPSILON);

        // Test green (120°, 100%, 100%)
        Color green = Color.fromHSV(120, 1.0, 1.0);
        assertEquals(0.0, green.r(), EPSILON);
        assertEquals(1.0, green.g(), EPSILON);
        assertEquals(0.0, green.b(), EPSILON);

        // Test blue (240°, 100%, 100%)
        Color blue = Color.fromHSV(240, 1.0, 1.0);
        assertEquals(0.0, blue.r(), EPSILON);
        assertEquals(0.0, blue.g(), EPSILON);
        assertEquals(1.0, blue.b(), EPSILON);
    }

    @Test
    void testToHSV() {
        // Test red
        double[] hsv = Color.RED.toHSV();
        assertEquals(0.0, hsv[0], EPSILON); // Hue
        assertEquals(1.0, hsv[1], EPSILON); // Saturation
        assertEquals(1.0, hsv[2], EPSILON); // Value

        // Test white (no saturation)
        hsv = Color.WHITE.toHSV();
        assertEquals(0.0, hsv[1], EPSILON); // Saturation
        assertEquals(1.0, hsv[2], EPSILON); // Value

        // Test black
        hsv = Color.BLACK.toHSV();
        assertEquals(0.0, hsv[2], EPSILON); // Value
    }

    @Test
    void testHSVRoundTrip() {
        // Create color from HSV, convert back, and verify
        Color original = Color.fromHSV(180, 0.5, 0.8);
        double[] hsv = original.toHSV();
        Color reconstructed = Color.fromHSV(hsv[0], hsv[1], hsv[2]);

        assertEquals(original.r(), reconstructed.r(), 0.01);
        assertEquals(original.g(), reconstructed.g(), 0.01);
        assertEquals(original.b(), reconstructed.b(), 0.01);
    }

    @Test
    void testCommonColors() {
        assertEquals(0.0, Color.BLACK.r(), EPSILON);
        assertEquals(0.0, Color.BLACK.g(), EPSILON);
        assertEquals(0.0, Color.BLACK.b(), EPSILON);

        assertEquals(1.0, Color.WHITE.r(), EPSILON);
        assertEquals(1.0, Color.WHITE.g(), EPSILON);
        assertEquals(1.0, Color.WHITE.b(), EPSILON);

        assertEquals(1.0, Color.RED.r(), EPSILON);
        assertEquals(0.0, Color.RED.g(), EPSILON);
        assertEquals(0.0, Color.RED.b(), EPSILON);

        assertEquals(0.0, Color.GREEN.r(), EPSILON);
        assertEquals(1.0, Color.GREEN.g(), EPSILON);
        assertEquals(0.0, Color.GREEN.b(), EPSILON);

        assertEquals(0.0, Color.BLUE.r(), EPSILON);
        assertEquals(0.0, Color.BLUE.g(), EPSILON);
        assertEquals(1.0, Color.BLUE.b(), EPSILON);
    }

    @Test
    void testFromHexString() {
        // Test with # prefix
        Color color1 = Color.fromHexString("#FF0000");
        assertEquals(1.0, color1.r(), EPSILON);
        assertEquals(0.0, color1.g(), EPSILON);
        assertEquals(0.0, color1.b(), EPSILON);

        // Test without # prefix
        Color color2 = Color.fromHexString("00FF00");
        assertEquals(0.0, color2.r(), EPSILON);
        assertEquals(1.0, color2.g(), EPSILON);
        assertEquals(0.0, color2.b(), EPSILON);

        // Test mixed case
        Color color3 = Color.fromHexString("#fF8000");
        assertEquals(1.0, color3.r(), EPSILON);
        assertEquals(128.0 / 255.0, color3.g(), 0.01);
        assertEquals(0.0, color3.b(), EPSILON);
    }

    @Test
    void testFromHexStringInvalid() {
        assertThrows(IllegalArgumentException.class, () -> Color.fromHexString(null));
        assertThrows(IllegalArgumentException.class, () -> Color.fromHexString(""));
        assertThrows(IllegalArgumentException.class, () -> Color.fromHexString("#FF"));
        assertThrows(IllegalArgumentException.class, () -> Color.fromHexString("FF"));
        assertThrows(IllegalArgumentException.class, () -> Color.fromHexString("#GGGGGG"));
        assertThrows(IllegalArgumentException.class, () -> Color.fromHexString("ZZZZZZ"));
    }

    @Test
    void testEquality() {
        Color c1 = new Color(0.5, 0.3, 0.1);
        Color c2 = new Color(0.5, 0.3, 0.1);
        Color c3 = new Color(0.5, 0.3, 0.2);

        assertEquals(c1, c2);
        assertNotEquals(c1, c3);
    }
}
