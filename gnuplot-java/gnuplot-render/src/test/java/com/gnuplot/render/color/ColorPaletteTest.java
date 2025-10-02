package com.gnuplot.render.color;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for ColorPalette class.
 */
class ColorPaletteTest {

    private static final double EPSILON = 1e-10;

    @Test
    void testDefaultPalette() {
        ColorPalette palette = ColorPalette.defaultPalette();
        Color color = palette.getColor(0.5);

        // Default palette uses formulas 7, 5, 15
        double expectedR = Math.sqrt(0.5);
        double expectedG = 0.125; // 0.5^3
        double expectedB = Math.sin(Math.toRadians(180)); // ~0

        assertEquals(expectedR, color.r(), EPSILON);
        assertEquals(expectedG, color.g(), EPSILON);
        assertEquals(expectedB, color.b(), 0.01);
    }

    @Test
    void testGrayscalePalette() {
        ColorPalette palette = ColorPalette.grayscale();

        Color black = palette.getColor(0.0);
        assertEquals(0.0, black.r(), EPSILON);
        assertEquals(0.0, black.g(), EPSILON);
        assertEquals(0.0, black.b(), EPSILON);

        Color white = palette.getColor(1.0);
        assertEquals(1.0, white.r(), EPSILON);
        assertEquals(1.0, white.g(), EPSILON);
        assertEquals(1.0, white.b(), EPSILON);

        Color gray = palette.getColor(0.5);
        assertEquals(0.5, gray.r(), EPSILON);
        assertEquals(0.5, gray.g(), EPSILON);
        assertEquals(0.5, gray.b(), EPSILON);
    }

    @Test
    void testGrayscaleWithGamma() {
        ColorPalette palette = ColorPalette.grayscale(2.0);

        Color gray = palette.getColor(0.5);
        double expected = Math.pow(0.5, 0.5); // gamma correction

        assertEquals(expected, gray.r(), EPSILON);
        assertEquals(expected, gray.g(), EPSILON);
        assertEquals(expected, gray.b(), EPSILON);
    }

    @Test
    void testRGBFormulasPalette() {
        // Create a palette with simple formulas
        ColorPalette palette = ColorPalette.builder()
                .rgbFormulas(3, 3, 3) // All linear (identity)
                .build();

        Color color = palette.getColor(0.5);
        assertEquals(0.5, color.r(), EPSILON);
        assertEquals(0.5, color.g(), EPSILON);
        assertEquals(0.5, color.b(), EPSILON);
    }

    @Test
    void testGradientPalette() {
        ColorPalette palette = ColorPalette.builder()
                .addGradientPoint(0.0, Color.BLACK)
                .addGradientPoint(0.5, Color.RED)
                .addGradientPoint(1.0, Color.WHITE)
                .build();

        // At gradient points, should match exactly
        Color black = palette.getColor(0.0);
        assertEquals(Color.BLACK, black);

        Color red = palette.getColor(0.5);
        assertEquals(Color.RED, red);

        Color white = palette.getColor(1.0);
        assertEquals(Color.WHITE, white);

        // Midway between black and red should be dark red
        Color darkRed = palette.getColor(0.25);
        assertEquals(0.5, darkRed.r(), EPSILON);
        assertEquals(0.0, darkRed.g(), EPSILON);
        assertEquals(0.0, darkRed.b(), EPSILON);

        // Midway between red and white should be pink
        Color pink = palette.getColor(0.75);
        assertEquals(1.0, pink.r(), EPSILON);
        assertEquals(0.5, pink.g(), EPSILON);
        assertEquals(0.5, pink.b(), EPSILON);
    }

    @Test
    void testGradientInterpolation() {
        ColorPalette palette = ColorPalette.builder()
                .addGradientPoint(0.0, Color.BLACK)
                .addGradientPoint(1.0, Color.WHITE)
                .build();

        // Linear interpolation from black to white
        for (double gray = 0.0; gray <= 1.0; gray += 0.1) {
            Color color = palette.getColor(gray);
            assertEquals(gray, color.r(), 0.01);
            assertEquals(gray, color.g(), 0.01);
            assertEquals(gray, color.b(), 0.01);
        }
    }

    @Test
    void testCubehelixPalette() {
        ColorPalette palette = ColorPalette.builder()
                .cubehelix(0.5, -1.5, 1.0)
                .gamma(1.0)
                .build();

        // Cubehelix should produce colors that spiral through color space
        Color c0 = palette.getColor(0.0);
        Color c1 = palette.getColor(1.0);

        // At 0, should be close to black
        assertTrue(c0.r() < 0.5 && c0.g() < 0.5 && c0.b() < 0.5);

        // At 1, should be close to white
        assertTrue(c1.r() > 0.5 && c1.g() > 0.5 && c1.b() > 0.5);
    }

    @Test
    void testPaletteClampingInputs() {
        ColorPalette palette = ColorPalette.defaultPalette();

        // Values outside [0, 1] should be clamped
        Color c1 = palette.getColor(-0.5);
        Color c2 = palette.getColor(0.0);
        assertEquals(c1, c2);

        Color c3 = palette.getColor(1.5);
        Color c4 = palette.getColor(1.0);
        assertEquals(c3, c4);
    }

    @Test
    void testEmptyGradient() {
        // Empty gradient should return grayscale
        ColorPalette palette = ColorPalette.builder()
                .gradient(java.util.Collections.emptyList())
                .build();

        Color gray = palette.getColor(0.5);
        assertEquals(0.5, gray.r(), EPSILON);
        assertEquals(0.5, gray.g(), EPSILON);
        assertEquals(0.5, gray.b(), EPSILON);
    }

    @Test
    void testSingleGradientPoint() {
        ColorPalette palette = ColorPalette.builder()
                .addGradientPoint(0.5, Color.RED)
                .build();

        // All values should return the single color
        assertEquals(Color.RED, palette.getColor(0.0));
        assertEquals(Color.RED, palette.getColor(0.5));
        assertEquals(Color.RED, palette.getColor(1.0));
    }

    @Test
    void testGradientSorting() {
        // Gradient points should be automatically sorted by position
        ColorPalette palette = ColorPalette.builder()
                .addGradientPoint(1.0, Color.WHITE)
                .addGradientPoint(0.0, Color.BLACK)
                .addGradientPoint(0.5, Color.RED)
                .build();

        Color black = palette.getColor(0.0);
        Color red = palette.getColor(0.5);
        Color white = palette.getColor(1.0);

        assertEquals(Color.BLACK, black);
        assertEquals(Color.RED, red);
        assertEquals(Color.WHITE, white);
    }

    @Test
    void testGradientEdgeCases() {
        ColorPalette palette = ColorPalette.builder()
                .addGradientPoint(0.25, Color.RED)
                .addGradientPoint(0.75, Color.BLUE)
                .build();

        // Below first point should return first color
        Color c1 = palette.getColor(0.0);
        assertEquals(Color.RED, c1);

        // Above last point should return last color
        Color c2 = palette.getColor(1.0);
        assertEquals(Color.BLUE, c2);
    }

    @Test
    void testInvalidGradientPosition() {
        assertThrows(IllegalArgumentException.class, () ->
                new ColorPalette.GradientPoint(-0.1, Color.RED)
        );

        assertThrows(IllegalArgumentException.class, () ->
                new ColorPalette.GradientPoint(1.1, Color.RED)
        );
    }

    @Test
    void testBuilderDefaults() {
        // Builder without explicit settings should create RGB formula palette
        ColorPalette palette = ColorPalette.builder().build();
        Color color = palette.getColor(0.5);

        // Should use default formulas 7, 5, 15
        assertNotNull(color);
    }

    @Test
    void testMultiplePalettesIndependent() {
        ColorPalette p1 = ColorPalette.builder()
                .addGradientPoint(0.0, Color.RED)
                .addGradientPoint(1.0, Color.BLUE)
                .build();

        ColorPalette p2 = ColorPalette.builder()
                .addGradientPoint(0.0, Color.GREEN)
                .addGradientPoint(1.0, Color.YELLOW)
                .build();

        // Palettes should be independent
        assertEquals(Color.RED, p1.getColor(0.0));
        assertEquals(Color.GREEN, p2.getColor(0.0));
    }
}
