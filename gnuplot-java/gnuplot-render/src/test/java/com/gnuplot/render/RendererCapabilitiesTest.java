package com.gnuplot.render;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for RendererCapabilities class.
 */
class RendererCapabilitiesTest {

    @Test
    void testDefaultCapabilities() {
        RendererCapabilities caps = RendererCapabilities.builder().build();

        assertFalse(caps.supportsTransparency());
        assertFalse(caps.supports3D());
        assertFalse(caps.supportsAnimation());
        assertFalse(caps.supportsInteractivity());
        assertFalse(caps.supportsVectorGraphics());
    }

    @Test
    void testTransparency() {
        RendererCapabilities caps = RendererCapabilities.builder()
                .transparency(true)
                .build();

        assertTrue(caps.supportsTransparency());
        assertFalse(caps.supports3D());
    }

    @Test
    void test3D() {
        RendererCapabilities caps = RendererCapabilities.builder()
                .supports3D(true)
                .build();

        assertTrue(caps.supports3D());
        assertFalse(caps.supportsTransparency());
    }

    @Test
    void testAnimation() {
        RendererCapabilities caps = RendererCapabilities.builder()
                .animation(true)
                .build();

        assertTrue(caps.supportsAnimation());
        assertFalse(caps.supportsInteractivity());
    }

    @Test
    void testInteractivity() {
        RendererCapabilities caps = RendererCapabilities.builder()
                .interactivity(true)
                .build();

        assertTrue(caps.supportsInteractivity());
        assertFalse(caps.supportsAnimation());
    }

    @Test
    void testVectorGraphics() {
        RendererCapabilities caps = RendererCapabilities.builder()
                .vectorGraphics(true)
                .build();

        assertTrue(caps.supportsVectorGraphics());
        assertFalse(caps.supportsTransparency());
    }

    @Test
    void testAllCapabilities() {
        RendererCapabilities caps = RendererCapabilities.builder()
                .transparency(true)
                .supports3D(true)
                .animation(true)
                .interactivity(true)
                .vectorGraphics(true)
                .build();

        assertTrue(caps.supportsTransparency());
        assertTrue(caps.supports3D());
        assertTrue(caps.supportsAnimation());
        assertTrue(caps.supportsInteractivity());
        assertTrue(caps.supportsVectorGraphics());
    }

    @Test
    void testToString() {
        RendererCapabilities caps = RendererCapabilities.builder()
                .transparency(true)
                .supports3D(true)
                .build();

        String str = caps.toString();
        assertTrue(str.contains("transparency=true"));
        assertTrue(str.contains("3D=true"));
    }

    @Test
    void testSvgLikeCapabilities() {
        RendererCapabilities caps = RendererCapabilities.builder()
                .transparency(true)
                .vectorGraphics(true)
                .interactivity(true)
                .build();

        assertTrue(caps.supportsTransparency());
        assertTrue(caps.supportsVectorGraphics());
        assertTrue(caps.supportsInteractivity());
        assertFalse(caps.supports3D());
        assertFalse(caps.supportsAnimation());
    }

    @Test
    void testPngLikeCapabilities() {
        RendererCapabilities caps = RendererCapabilities.builder()
                .transparency(true)
                .build();

        assertTrue(caps.supportsTransparency());
        assertFalse(caps.supportsVectorGraphics());
        assertFalse(caps.supportsInteractivity());
        assertFalse(caps.supports3D());
        assertFalse(caps.supportsAnimation());
    }
}
