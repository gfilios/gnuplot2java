package com.gnuplot.render;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for RenderingHints class.
 */
class RenderingHintsTest {

    @Test
    void testEmpty() {
        RenderingHints hints = RenderingHints.empty();

        assertFalse(hints.has(RenderingHints.Keys.ANTIALIASING));
        assertFalse(hints.has(RenderingHints.Keys.DPI));
        assertEquals(300, hints.getOrDefault(RenderingHints.Keys.DPI, 300));
    }

    @Test
    void testBuilderSetAndGet() {
        RenderingHints hints = RenderingHints.builder()
                .antialiasing(true)
                .dpi(150)
                .quality("high")
                .build();

        assertTrue(hints.get(RenderingHints.Keys.ANTIALIASING).orElse(false));
        assertEquals(150, hints.get(RenderingHints.Keys.DPI).orElse(0));
        assertEquals("high", hints.get(RenderingHints.Keys.QUALITY).orElse(""));
    }

    @Test
    void testGetOrDefault() {
        RenderingHints hints = RenderingHints.builder()
                .dpi(150)
                .build();

        assertEquals(150, hints.getOrDefault(RenderingHints.Keys.DPI, 300));
        assertEquals(300, hints.getOrDefault(RenderingHints.Keys.FONT_SIZE, 300));
    }

    @Test
    void testHas() {
        RenderingHints hints = RenderingHints.builder()
                .antialiasing(true)
                .build();

        assertTrue(hints.has(RenderingHints.Keys.ANTIALIASING));
        assertFalse(hints.has(RenderingHints.Keys.DPI));
    }

    @Test
    void testSetNull() {
        RenderingHints hints = RenderingHints.builder()
                .antialiasing(true)
                .set(RenderingHints.Keys.ANTIALIASING, null)
                .build();

        assertFalse(hints.has(RenderingHints.Keys.ANTIALIASING));
    }

    @Test
    void testAntialiasing() {
        RenderingHints hints = RenderingHints.builder()
                .antialiasing(true)
                .build();

        assertTrue(hints.get(RenderingHints.Keys.ANTIALIASING).orElse(false));
    }

    @Test
    void testQuality() {
        RenderingHints hints = RenderingHints.builder()
                .quality("medium")
                .build();

        assertEquals("medium", hints.get(RenderingHints.Keys.QUALITY).orElse(""));
    }

    @Test
    void testDpi() {
        RenderingHints hints = RenderingHints.builder()
                .dpi(300)
                .build();

        assertEquals(300, hints.get(RenderingHints.Keys.DPI).orElse(0));
    }

    @Test
    void testBackgroundColor() {
        RenderingHints hints = RenderingHints.builder()
                .backgroundColor("#FFFFFF")
                .build();

        assertEquals("#FFFFFF", hints.get(RenderingHints.Keys.BACKGROUND_COLOR).orElse(""));
    }

    @Test
    void testTransparency() {
        RenderingHints hints = RenderingHints.builder()
                .transparency(true)
                .build();

        assertTrue(hints.get(RenderingHints.Keys.TRANSPARENCY).orElse(false));
    }

    @Test
    void testFontFamily() {
        RenderingHints hints = RenderingHints.builder()
                .fontFamily("Arial")
                .build();

        assertEquals("Arial", hints.get(RenderingHints.Keys.FONT_FAMILY).orElse(""));
    }

    @Test
    void testFontSize() {
        RenderingHints hints = RenderingHints.builder()
                .fontSize(12)
                .build();

        assertEquals(12, hints.get(RenderingHints.Keys.FONT_SIZE).orElse(0));
    }

    @Test
    void testLineWidthScale() {
        RenderingHints hints = RenderingHints.builder()
                .lineWidthScale(1.5)
                .build();

        assertEquals(1.5, hints.get(RenderingHints.Keys.LINE_WIDTH_SCALE).orElse(0.0), 1e-10);
    }

    @Test
    void testGridEnabled() {
        RenderingHints hints = RenderingHints.builder()
                .gridEnabled(true)
                .build();

        assertTrue(hints.get(RenderingHints.Keys.GRID_ENABLED).orElse(false));
    }

    @Test
    void testLegendEnabled() {
        RenderingHints hints = RenderingHints.builder()
                .legendEnabled(false)
                .build();

        assertFalse(hints.get(RenderingHints.Keys.LEGEND_ENABLED).orElse(true));
    }

    @Test
    void testAnimationFps() {
        RenderingHints hints = RenderingHints.builder()
                .animationFps(30)
                .build();

        assertEquals(30, hints.get(RenderingHints.Keys.ANIMATION_FPS).orElse(0));
    }

    @Test
    void testCompressionLevel() {
        RenderingHints hints = RenderingHints.builder()
                .compressionLevel(9)
                .build();

        assertEquals(9, hints.get(RenderingHints.Keys.COMPRESSION_LEVEL).orElse(0));
    }

    @Test
    void testMultipleHints() {
        RenderingHints hints = RenderingHints.builder()
                .antialiasing(true)
                .dpi(300)
                .quality("high")
                .backgroundColor("#FFFFFF")
                .transparency(true)
                .fontFamily("Arial")
                .fontSize(12)
                .lineWidthScale(1.5)
                .gridEnabled(true)
                .legendEnabled(true)
                .animationFps(30)
                .compressionLevel(9)
                .build();

        assertTrue(hints.has(RenderingHints.Keys.ANTIALIASING));
        assertTrue(hints.has(RenderingHints.Keys.DPI));
        assertTrue(hints.has(RenderingHints.Keys.QUALITY));
        assertTrue(hints.has(RenderingHints.Keys.BACKGROUND_COLOR));
        assertTrue(hints.has(RenderingHints.Keys.TRANSPARENCY));
        assertTrue(hints.has(RenderingHints.Keys.FONT_FAMILY));
        assertTrue(hints.has(RenderingHints.Keys.FONT_SIZE));
        assertTrue(hints.has(RenderingHints.Keys.LINE_WIDTH_SCALE));
        assertTrue(hints.has(RenderingHints.Keys.GRID_ENABLED));
        assertTrue(hints.has(RenderingHints.Keys.LEGEND_ENABLED));
        assertTrue(hints.has(RenderingHints.Keys.ANIMATION_FPS));
        assertTrue(hints.has(RenderingHints.Keys.COMPRESSION_LEVEL));
    }

    @Test
    void testCustomKey() {
        RenderingHints.Key<String> customKey = RenderingHints.Key.of("customHint", String.class);

        RenderingHints hints = RenderingHints.builder()
                .set(customKey, "custom value")
                .build();

        assertEquals("custom value", hints.get(customKey).orElse(""));
    }

    @Test
    void testKeyEquality() {
        RenderingHints.Key<Integer> key1 = RenderingHints.Key.of("test", Integer.class);
        RenderingHints.Key<Integer> key2 = RenderingHints.Key.of("test", Integer.class);

        assertEquals(key1, key2);
        assertEquals(key1.hashCode(), key2.hashCode());
    }

    @Test
    void testKeyInequality() {
        RenderingHints.Key<Integer> key1 = RenderingHints.Key.of("test1", Integer.class);
        RenderingHints.Key<Integer> key2 = RenderingHints.Key.of("test2", Integer.class);

        assertNotEquals(key1, key2);
    }

    @Test
    void testKeyToString() {
        RenderingHints.Key<Integer> key = RenderingHints.Key.of("testKey", Integer.class);

        assertEquals("testKey", key.toString());
    }
}
