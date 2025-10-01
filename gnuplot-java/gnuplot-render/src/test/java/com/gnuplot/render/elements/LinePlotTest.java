package com.gnuplot.render.elements;

import com.gnuplot.render.SceneElement;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for LinePlot class.
 */
class LinePlotTest {

    @Test
    void testBuilder() {
        LinePlot plot = LinePlot.builder()
                .id("plot1")
                .addPoint(0.0, 0.0)
                .addPoint(1.0, 1.0)
                .addPoint(2.0, 4.0)
                .lineStyle(LinePlot.LineStyle.DASHED)
                .color("#FF0000")
                .label("Test Plot")
                .build();

        assertEquals("plot1", plot.getId());
        assertEquals(3, plot.getPoints().size());
        assertEquals(LinePlot.LineStyle.DASHED, plot.getLineStyle());
        assertEquals("#FF0000", plot.getColor());
        assertEquals("Test Plot", plot.getLabel());
    }

    @Test
    void testMinimalPlot() {
        LinePlot plot = LinePlot.builder()
                .id("plot1")
                .addPoint(0.0, 0.0)
                .build();

        assertEquals("plot1", plot.getId());
        assertEquals(1, plot.getPoints().size());
        assertEquals(LinePlot.LineStyle.SOLID, plot.getLineStyle());
        assertEquals("#000000", plot.getColor());
        assertNull(plot.getLabel());
    }

    @Test
    void testPointsList() {
        LinePlot.Point2D p1 = new LinePlot.Point2D(0.0, 0.0);
        LinePlot.Point2D p2 = new LinePlot.Point2D(1.0, 1.0);

        LinePlot plot = LinePlot.builder()
                .id("plot1")
                .points(Arrays.asList(p1, p2))
                .build();

        assertEquals(2, plot.getPoints().size());
        assertEquals(p1, plot.getPoints().get(0));
        assertEquals(p2, plot.getPoints().get(1));
    }

    @Test
    void testPointEquality() {
        LinePlot.Point2D p1 = new LinePlot.Point2D(1.0, 2.0);
        LinePlot.Point2D p2 = new LinePlot.Point2D(1.0, 2.0);
        LinePlot.Point2D p3 = new LinePlot.Point2D(1.0, 3.0);

        assertEquals(p1, p2);
        assertNotEquals(p1, p3);
        assertEquals(p1.hashCode(), p2.hashCode());
    }

    @Test
    void testPointToString() {
        LinePlot.Point2D point = new LinePlot.Point2D(1.5, 2.75);
        String str = point.toString();

        assertTrue(str.contains("1.5") || str.contains("1,5")); // locale-dependent
        assertTrue(str.contains("2.75") || str.contains("2,75"));
    }

    @Test
    void testElementType() {
        LinePlot plot = LinePlot.builder()
                .id("plot1")
                .addPoint(0.0, 0.0)
                .build();

        assertEquals(SceneElement.ElementType.PLOT, plot.getType());
    }

    @Test
    void testImmutablePoints() {
        LinePlot plot = LinePlot.builder()
                .id("plot1")
                .addPoint(0.0, 0.0)
                .build();

        assertThrows(UnsupportedOperationException.class, () ->
                plot.getPoints().add(new LinePlot.Point2D(1.0, 1.0)));
    }

    @Test
    void testRequiredId() {
        assertThrows(IllegalStateException.class, () ->
                LinePlot.builder()
                        .addPoint(0.0, 0.0)
                        .build());
    }

    @Test
    void testRequiredPoints() {
        assertThrows(IllegalStateException.class, () ->
                LinePlot.builder()
                        .id("plot1")
                        .build());
    }

    @Test
    void testNullId() {
        assertThrows(NullPointerException.class, () ->
                LinePlot.builder().id(null));
    }

    @Test
    void testNullPoint() {
        assertThrows(NullPointerException.class, () ->
                LinePlot.builder().addPoint(null));
    }

    @Test
    void testNullLineStyle() {
        assertThrows(NullPointerException.class, () ->
                LinePlot.builder().lineStyle(null));
    }

    @Test
    void testNullColor() {
        assertThrows(NullPointerException.class, () ->
                LinePlot.builder().color(null));
    }

    @Test
    void testAllLineStyles() {
        for (LinePlot.LineStyle style : LinePlot.LineStyle.values()) {
            LinePlot plot = LinePlot.builder()
                    .id("plot1")
                    .addPoint(0.0, 0.0)
                    .lineStyle(style)
                    .build();

            assertEquals(style, plot.getLineStyle());
        }
    }

    @Test
    void testToString() {
        LinePlot plot = LinePlot.builder()
                .id("plot1")
                .addPoint(0.0, 0.0)
                .addPoint(1.0, 1.0)
                .lineStyle(LinePlot.LineStyle.DOTTED)
                .color("#0000FF")
                .label("Blue Line")
                .build();

        String str = plot.toString();
        assertTrue(str.contains("plot1"));
        assertTrue(str.contains("2")); // point count
        assertTrue(str.contains("DOTTED"));
        assertTrue(str.contains("#0000FF"));
        assertTrue(str.contains("Blue Line"));
    }
}
