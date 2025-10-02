package com.gnuplot.render.color;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for named color palettes.
 */
class NamedPalettesTest {

    @Test
    void testViridis() {
        ColorPalette viridis = NamedPalettes.viridis();

        // Test that viridis produces valid colors across the range
        Color c0 = viridis.getColor(0.0);
        Color c1 = viridis.getColor(1.0);

        // Viridis starts with dark purple
        assertTrue(c0.r() < 0.5);
        assertTrue(c0.b() > 0.3);

        // Viridis ends with bright yellow
        assertTrue(c1.r() > 0.8);
        assertTrue(c1.g() > 0.8);
    }

    @Test
    void testViridisIsSmooth() {
        ColorPalette viridis = NamedPalettes.viridis();

        // Test that consecutive values produce smooth color transitions
        Color prev = viridis.getColor(0.0);
        for (double gray = 0.01; gray <= 1.0; gray += 0.01) {
            Color current = viridis.getColor(gray);

            // Color changes should be small (smooth)
            double dr = Math.abs(current.r() - prev.r());
            double dg = Math.abs(current.g() - prev.g());
            double db = Math.abs(current.b() - prev.b());

            assertTrue(dr < 0.1, "Red component changes too abruptly at " + gray);
            assertTrue(dg < 0.1, "Green component changes too abruptly at " + gray);
            assertTrue(db < 0.1, "Blue component changes too abruptly at " + gray);

            prev = current;
        }
    }

    @Test
    void testViridisKnownValues() {
        ColorPalette viridis = NamedPalettes.viridis();

        // Test first color (0x440154)
        Color first = viridis.getColor(0.0);
        assertEquals(0x440154, first.toRGB24(), 5); // Small tolerance for rounding

        // Test last color (0xfde725)
        Color last = viridis.getColor(1.0);
        assertEquals(0xfde725, last.toRGB24(), 5);
    }

    @Test
    void testHotPalette() {
        ColorPalette hot = NamedPalettes.hot();

        // Hot: black → red → yellow → white
        Color black = hot.getColor(0.0);
        assertEquals(Color.BLACK, black);

        Color red = hot.getColor(0.33);
        assertEquals(Color.RED.r(), red.r(), 0.01);

        Color yellow = hot.getColor(0.67);
        assertEquals(Color.YELLOW.r(), yellow.r(), 0.01);
        assertEquals(Color.YELLOW.g(), yellow.g(), 0.01);

        Color white = hot.getColor(1.0);
        assertEquals(Color.WHITE, white);
    }

    @Test
    void testCoolPalette() {
        ColorPalette cool = NamedPalettes.cool();

        // Cool: cyan → magenta
        Color cyan = cool.getColor(0.0);
        assertEquals(Color.CYAN, cyan);

        Color magenta = cool.getColor(1.0);
        assertEquals(Color.MAGENTA, magenta);

        // Middle should be a blend
        Color mid = cool.getColor(0.5);
        assertEquals(0.5, mid.r(), 0.01);
        assertEquals(0.5, mid.g(), 0.01);
        assertEquals(1.0, mid.b(), 0.01);
    }

    @Test
    void testRainbowPalette() {
        ColorPalette rainbow = NamedPalettes.rainbow();

        // Rainbow should cycle through all hues
        Color red = rainbow.getColor(0.0);
        assertEquals(Color.RED, red);

        Color yellow = rainbow.getColor(0.2);
        assertEquals(Color.YELLOW.r(), yellow.r(), 0.01);
        assertEquals(Color.YELLOW.g(), yellow.g(), 0.01);

        Color green = rainbow.getColor(0.4);
        assertEquals(Color.GREEN.g(), green.g(), 0.01);

        Color cyan = rainbow.getColor(0.6);
        assertEquals(Color.CYAN.g(), cyan.g(), 0.01);
        assertEquals(Color.CYAN.b(), cyan.b(), 0.01);

        Color blue = rainbow.getColor(0.8);
        assertEquals(Color.BLUE.b(), blue.b(), 0.01);

        Color magenta = rainbow.getColor(1.0);
        assertEquals(Color.MAGENTA, magenta);
    }

    @Test
    void testCubehelixPalette() {
        ColorPalette cubehelix = NamedPalettes.cubehelix();

        // Cubehelix should produce colors across the range
        Color c0 = cubehelix.getColor(0.0);
        Color c1 = cubehelix.getColor(1.0);

        assertNotNull(c0);
        assertNotNull(c1);

        // Should not be the same color
        assertNotEquals(c0, c1);
    }

    @Test
    void testAllNamedPalettesValid() {
        ColorPalette[] palettes = {
                NamedPalettes.viridis(),
                NamedPalettes.hot(),
                NamedPalettes.cool(),
                NamedPalettes.rainbow(),
                NamedPalettes.cubehelix()
        };

        // Test that all palettes produce valid colors
        for (ColorPalette palette : palettes) {
            for (double gray = 0.0; gray <= 1.0; gray += 0.1) {
                Color color = palette.getColor(gray);
                assertNotNull(color);
                assertTrue(color.r() >= 0.0 && color.r() <= 1.0);
                assertTrue(color.g() >= 0.0 && color.g() <= 1.0);
                assertTrue(color.b() >= 0.0 && color.b() <= 1.0);
            }
        }
    }

    @Test
    void testNamedPalettesAreDistinct() {
        // Different palettes should produce different colors
        double gray = 0.5;

        Color viridis = NamedPalettes.viridis().getColor(gray);
        Color hot = NamedPalettes.hot().getColor(gray);
        Color cool = NamedPalettes.cool().getColor(gray);
        Color rainbow = NamedPalettes.rainbow().getColor(gray);

        // Not all should be equal (they are distinct palettes)
        assertTrue(!viridis.equals(hot) || !hot.equals(cool) || !cool.equals(rainbow));
    }
}
