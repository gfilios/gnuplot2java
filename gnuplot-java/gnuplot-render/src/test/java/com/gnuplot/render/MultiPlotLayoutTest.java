package com.gnuplot.render;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for MultiPlotLayout functionality.
 */
class MultiPlotLayoutTest {

    @Test
    void testGridLayoutCreation() {
        Scene scene1 = createSimpleScene("Plot 1");
        Scene scene2 = createSimpleScene("Plot 2");
        Scene scene3 = createSimpleScene("Plot 3");
        Scene scene4 = createSimpleScene("Plot 4");

        MultiPlotLayout layout = MultiPlotLayout.builder()
                .title("Test Grid")
                .dimensions(1200, 900)
                .gridLayout(2, 2)
                .addPlot(scene1, 0, 0)
                .addPlot(scene2, 0, 1)
                .addPlot(scene3, 1, 0)
                .addPlot(scene4, 1, 1)
                .build();

        assertEquals(1200, layout.getWidth());
        assertEquals(900, layout.getHeight());
        assertEquals("Test Grid", layout.getTitle());
        assertEquals(MultiPlotLayout.LayoutMode.GRID, layout.getMode());
        assertEquals(2, layout.getGridRows());
        assertEquals(2, layout.getGridCols());
        assertEquals(4, layout.getSubPlots().size());
    }

    @Test
    void testCustomLayoutCreation() {
        Scene scene1 = createSimpleScene("Plot 1");
        Scene scene2 = createSimpleScene("Plot 2");

        MultiPlotLayout layout = MultiPlotLayout.builder()
                .title("Test Custom")
                .dimensions(800, 600)
                .customLayout()
                .addPlot(scene1, 0.0, 0.0, 0.6, 1.0)
                .addPlot(scene2, 0.6, 0.0, 0.4, 1.0)
                .build();

        assertEquals(800, layout.getWidth());
        assertEquals(600, layout.getHeight());
        assertEquals("Test Custom", layout.getTitle());
        assertEquals(MultiPlotLayout.LayoutMode.CUSTOM, layout.getMode());
        assertEquals(2, layout.getSubPlots().size());
    }

    @Test
    void testGridLayoutWithoutTitle() {
        Scene scene = createSimpleScene("Plot");

        MultiPlotLayout layout = MultiPlotLayout.builder()
                .dimensions(800, 600)
                .gridLayout(1, 1)
                .addPlot(scene, 0, 0)
                .build();

        assertNull(layout.getTitle());
        assertEquals(1, layout.getSubPlots().size());
    }

    @Test
    void testSubPlotGridConstructor() {
        Scene scene = createSimpleScene("Test");
        MultiPlotLayout.SubPlot subplot = new MultiPlotLayout.SubPlot(scene, 1, 2);

        assertEquals(scene, subplot.getScene());
        assertEquals(1, subplot.getRow());
        assertEquals(2, subplot.getCol());
        assertEquals(0.0, subplot.getX());
        assertEquals(0.0, subplot.getY());
        assertEquals(0.0, subplot.getWidthFraction());
        assertEquals(0.0, subplot.getHeightFraction());
    }

    @Test
    void testSubPlotCustomConstructor() {
        Scene scene = createSimpleScene("Test");
        MultiPlotLayout.SubPlot subplot = new MultiPlotLayout.SubPlot(scene, 0.25, 0.5, 0.75, 0.5);

        assertEquals(scene, subplot.getScene());
        assertEquals(-1, subplot.getRow());
        assertEquals(-1, subplot.getCol());
        assertEquals(0.25, subplot.getX());
        assertEquals(0.5, subplot.getY());
        assertEquals(0.75, subplot.getWidthFraction());
        assertEquals(0.5, subplot.getHeightFraction());
    }

    @Test
    void testInvalidDimensions() {
        assertThrows(IllegalArgumentException.class, () ->
                MultiPlotLayout.builder()
                        .dimensions(0, 600)
                        .gridLayout(1, 1)
                        .build()
        );

        assertThrows(IllegalArgumentException.class, () ->
                MultiPlotLayout.builder()
                        .dimensions(800, -1)
                        .gridLayout(1, 1)
                        .build()
        );
    }

    @Test
    void testInvalidGridDimensions() {
        assertThrows(IllegalArgumentException.class, () ->
                MultiPlotLayout.builder()
                        .dimensions(800, 600)
                        .gridLayout(0, 1)
                        .build()
        );

        assertThrows(IllegalArgumentException.class, () ->
                MultiPlotLayout.builder()
                        .dimensions(800, 600)
                        .gridLayout(2, 0)
                        .build()
        );
    }

    @Test
    void testInvalidGridPosition() {
        Scene scene = createSimpleScene("Test");

        assertThrows(IllegalArgumentException.class, () ->
                MultiPlotLayout.builder()
                        .dimensions(800, 600)
                        .gridLayout(2, 2)
                        .addPlot(scene, -1, 0)
                        .build()
        );

        assertThrows(IllegalArgumentException.class, () ->
                MultiPlotLayout.builder()
                        .dimensions(800, 600)
                        .gridLayout(2, 2)
                        .addPlot(scene, 0, -1)
                        .build()
        );

        assertThrows(IllegalArgumentException.class, () ->
                MultiPlotLayout.builder()
                        .dimensions(800, 600)
                        .gridLayout(2, 2)
                        .addPlot(scene, 2, 0)
                        .build()
        );

        assertThrows(IllegalArgumentException.class, () ->
                MultiPlotLayout.builder()
                        .dimensions(800, 600)
                        .gridLayout(2, 2)
                        .addPlot(scene, 0, 2)
                        .build()
        );
    }

    @Test
    void testInvalidCustomPosition() {
        Scene scene = createSimpleScene("Test");

        assertThrows(IllegalArgumentException.class, () ->
                MultiPlotLayout.builder()
                        .dimensions(800, 600)
                        .customLayout()
                        .addPlot(scene, -0.1, 0.0, 0.5, 0.5)
                        .build()
        );

        assertThrows(IllegalArgumentException.class, () ->
                MultiPlotLayout.builder()
                        .dimensions(800, 600)
                        .customLayout()
                        .addPlot(scene, 0.0, 1.1, 0.5, 0.5)
                        .build()
        );

        assertThrows(IllegalArgumentException.class, () ->
                MultiPlotLayout.builder()
                        .dimensions(800, 600)
                        .customLayout()
                        .addPlot(scene, 0.0, 0.0, 0.0, 0.5)
                        .build()
        );

        assertThrows(IllegalArgumentException.class, () ->
                MultiPlotLayout.builder()
                        .dimensions(800, 600)
                        .customLayout()
                        .addPlot(scene, 0.0, 0.0, 0.5, 1.1)
                        .build()
        );
    }

    @Test
    void testMixedGridAndCustomThrows() {
        Scene scene = createSimpleScene("Test");

        assertThrows(IllegalStateException.class, () ->
                MultiPlotLayout.builder()
                        .dimensions(800, 600)
                        .gridLayout(2, 2)
                        .addPlot(scene, 0.0, 0.0, 0.5, 0.5)
                        .build()
        );

        assertThrows(IllegalStateException.class, () ->
                MultiPlotLayout.builder()
                        .dimensions(800, 600)
                        .customLayout()
                        .addPlot(scene, 0, 0)
                        .build()
        );
    }

    @Test
    void testImmutability() {
        Scene scene1 = createSimpleScene("Plot 1");
        Scene scene2 = createSimpleScene("Plot 2");

        MultiPlotLayout layout = MultiPlotLayout.builder()
                .dimensions(800, 600)
                .gridLayout(1, 2)
                .addPlot(scene1, 0, 0)
                .addPlot(scene2, 0, 1)
                .build();

        assertThrows(UnsupportedOperationException.class, () ->
                layout.getSubPlots().add(new MultiPlotLayout.SubPlot(scene1, 0, 0))
        );
    }

    @Test
    void testLargeGrid() {
        Scene scene = createSimpleScene("Plot");

        MultiPlotLayout.Builder builder = MultiPlotLayout.builder()
                .dimensions(2000, 1500)
                .gridLayout(3, 4);

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 4; col++) {
                builder.addPlot(scene, row, col);
            }
        }

        MultiPlotLayout layout = builder.build();

        assertEquals(12, layout.getSubPlots().size());
        assertEquals(3, layout.getGridRows());
        assertEquals(4, layout.getGridCols());
    }

    @Test
    void testComplexCustomLayout() {
        Scene scene1 = createSimpleScene("Large");
        Scene scene2 = createSimpleScene("Small 1");
        Scene scene3 = createSimpleScene("Small 2");
        Scene scene4 = createSimpleScene("Small 3");

        MultiPlotLayout layout = MultiPlotLayout.builder()
                .title("Dashboard")
                .dimensions(1600, 1200)
                .customLayout()
                .addPlot(scene1, 0.0, 0.0, 0.7, 0.7)    // Large plot
                .addPlot(scene2, 0.7, 0.0, 0.3, 0.35)   // Top-right
                .addPlot(scene3, 0.7, 0.35, 0.3, 0.35)  // Middle-right
                .addPlot(scene4, 0.0, 0.7, 1.0, 0.3)    // Bottom full-width
                .build();

        assertEquals(4, layout.getSubPlots().size());
    }

    @Test
    void testSinglePlot() {
        Scene scene = createSimpleScene("Single");

        MultiPlotLayout layout = MultiPlotLayout.builder()
                .dimensions(800, 600)
                .gridLayout(1, 1)
                .addPlot(scene, 0, 0)
                .build();

        assertEquals(1, layout.getSubPlots().size());
        assertEquals(1, layout.getGridRows());
        assertEquals(1, layout.getGridCols());
    }

    @Test
    void testVerticalPanels() {
        Scene scene1 = createSimpleScene("Top");
        Scene scene2 = createSimpleScene("Middle");
        Scene scene3 = createSimpleScene("Bottom");

        MultiPlotLayout layout = MultiPlotLayout.builder()
                .title("Vertical Stack")
                .dimensions(600, 1200)
                .gridLayout(3, 1)
                .addPlot(scene1, 0, 0)
                .addPlot(scene2, 1, 0)
                .addPlot(scene3, 2, 0)
                .build();

        assertEquals(3, layout.getSubPlots().size());
        assertEquals(3, layout.getGridRows());
        assertEquals(1, layout.getGridCols());
    }

    @Test
    void testHorizontalPanels() {
        Scene scene1 = createSimpleScene("Left");
        Scene scene2 = createSimpleScene("Center");
        Scene scene3 = createSimpleScene("Right");

        MultiPlotLayout layout = MultiPlotLayout.builder()
                .title("Horizontal Stack")
                .dimensions(1800, 600)
                .gridLayout(1, 3)
                .addPlot(scene1, 0, 0)
                .addPlot(scene2, 0, 1)
                .addPlot(scene3, 0, 2)
                .build();

        assertEquals(3, layout.getSubPlots().size());
        assertEquals(1, layout.getGridRows());
        assertEquals(3, layout.getGridCols());
    }

    private Scene createSimpleScene(String title) {
        return Scene.builder()
                .title(title)
                .dimensions(600, 450)
                .viewport(Viewport.of2D(0, 10, 0, 10))
                .build();
    }
}
