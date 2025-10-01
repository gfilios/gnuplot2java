package com.gnuplot.render;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.List;

/**
 * Tests for Scene class.
 */
class SceneTest {

    @Test
    void testMinimalScene() {
        Scene scene = Scene.builder()
                .dimensions(800, 600)
                .build();

        assertEquals(800, scene.getWidth());
        assertEquals(600, scene.getHeight());
        assertNull(scene.getTitle());
        assertNull(scene.getViewport());
        assertTrue(scene.getElements().isEmpty());
        assertNotNull(scene.getHints());
    }

    @Test
    void testFullScene() {
        Viewport viewport = Viewport.of2D(0.0, 10.0, 0.0, 10.0);
        RenderingHints hints = RenderingHints.builder()
                .antialiasing(true)
                .build();

        MockSceneElement element1 = new MockSceneElement(SceneElement.ElementType.PLOT);
        MockSceneElement element2 = new MockSceneElement(SceneElement.ElementType.AXIS);

        Scene scene = Scene.builder()
                .dimensions(800, 600)
                .title("Test Plot")
                .viewport(viewport)
                .addElement(element1)
                .addElement(element2)
                .hints(hints)
                .build();

        assertEquals(800, scene.getWidth());
        assertEquals(600, scene.getHeight());
        assertEquals("Test Plot", scene.getTitle());
        assertEquals(viewport, scene.getViewport());
        assertEquals(2, scene.getElements().size());
        assertEquals(element1, scene.getElements().get(0));
        assertEquals(element2, scene.getElements().get(1));
        assertEquals(hints, scene.getHints());
    }

    @Test
    void testBuilderWithElements() {
        MockSceneElement element1 = new MockSceneElement(SceneElement.ElementType.PLOT);
        MockSceneElement element2 = new MockSceneElement(SceneElement.ElementType.AXIS);
        List<SceneElement> elements = Arrays.asList(element1, element2);

        Scene scene = Scene.builder()
                .dimensions(800, 600)
                .elements(elements)
                .build();

        assertEquals(2, scene.getElements().size());
        assertEquals(element1, scene.getElements().get(0));
        assertEquals(element2, scene.getElements().get(1));
    }

    @Test
    void testAddMultipleElements() {
        MockSceneElement element1 = new MockSceneElement(SceneElement.ElementType.PLOT);
        MockSceneElement element2 = new MockSceneElement(SceneElement.ElementType.AXIS);
        MockSceneElement element3 = new MockSceneElement(SceneElement.ElementType.LEGEND);

        Scene scene = Scene.builder()
                .dimensions(800, 600)
                .addElement(element1)
                .addElement(element2)
                .addElement(element3)
                .build();

        assertEquals(3, scene.getElements().size());
    }

    @Test
    void testInvalidDimensions() {
        assertThrows(IllegalArgumentException.class, () ->
                Scene.builder().dimensions(0, 600).build());

        assertThrows(IllegalArgumentException.class, () ->
                Scene.builder().dimensions(800, 0).build());

        assertThrows(IllegalArgumentException.class, () ->
                Scene.builder().dimensions(-1, 600).build());

        assertThrows(IllegalArgumentException.class, () ->
                Scene.builder().dimensions(800, -1).build());
    }

    @Test
    void testDefaultHints() {
        Scene scene = Scene.builder()
                .dimensions(800, 600)
                .build();

        assertNotNull(scene.getHints());
        assertFalse(scene.getHints().has(RenderingHints.Keys.ANTIALIASING));
    }

    @Test
    void testGetAspectRatio() {
        Scene scene = Scene.builder()
                .dimensions(1600, 900)
                .build();

        assertEquals(16.0 / 9.0, scene.getAspectRatio(), 1e-10);
    }

    @Test
    void testImmutableElements() {
        MockSceneElement element = new MockSceneElement(SceneElement.ElementType.PLOT);

        Scene scene = Scene.builder()
                .dimensions(800, 600)
                .addElement(element)
                .build();

        List<SceneElement> elements = scene.getElements();
        assertThrows(UnsupportedOperationException.class, () ->
                elements.add(new MockSceneElement(SceneElement.ElementType.AXIS)));
    }

    @Test
    void testToString() {
        Scene scene = Scene.builder()
                .dimensions(800, 600)
                .title("Test Scene")
                .build();

        String str = scene.toString();
        assertTrue(str.contains("800x600"));
        assertTrue(str.contains("Test Scene"));
    }

    /**
     * Mock implementation of SceneElement for testing.
     */
    private static class MockSceneElement implements SceneElement {
        private final ElementType type;

        public MockSceneElement(ElementType type) {
            this.type = type;
        }

        @Override
        public ElementType getType() {
            return type;
        }

        @Override
        public void accept(SceneElementVisitor visitor) {
            // No-op for testing
        }
    }
}
