package com.gnuplot.render.elements;

import com.gnuplot.render.SceneElement;
import com.gnuplot.render.color.Color;
import com.gnuplot.render.style.MarkerStyle;
import com.gnuplot.render.style.PointStyle;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for ScatterPlot class.
 */
class ScatterPlotTest {

    @Test
    void testScatterPlotCreation() {
        ScatterPlot plot = ScatterPlot.builder()
                .id("plot1")
                .markerStyle(MarkerStyle.DEFAULT)
                .addPoint(1.0, 2.0)
                .addPoint(3.0, 4.0)
                .label("Test Plot")
                .build();

        assertEquals("plot1", plot.getId());
        assertEquals(2, plot.getPoints().size());
        assertEquals("Test Plot", plot.getLabel());
        assertEquals(MarkerStyle.DEFAULT, plot.getMarkerStyle());
    }

    @Test
    void testDataPoint() {
        ScatterPlot.DataPoint point = new ScatterPlot.DataPoint(1.0, 2.0);
        assertEquals(1.0, point.getX());
        assertEquals(2.0, point.getY());
        assertFalse(point.hasCustomSize());
        assertFalse(point.hasCustomColor());
    }

    @Test
    void testDataPointWithCustomProperties() {
        ScatterPlot.DataPoint point = new ScatterPlot.DataPoint(1.0, 2.0, 5.0, "#FF0000");
        assertTrue(point.hasCustomSize());
        assertTrue(point.hasCustomColor());
        assertEquals(5.0, point.getCustomSize());
        assertEquals("#FF0000", point.getCustomColor());
    }

    @Test
    void testElementType() {
        ScatterPlot plot = ScatterPlot.builder()
                .id("plot1")
                .addPoint(0, 0)
                .build();
        assertEquals(SceneElement.ElementType.PLOT, plot.getType());
    }

    @Test
    void testBuilderRequiresId() {
        assertThrows(IllegalStateException.class, () ->
                ScatterPlot.builder().addPoint(0, 0).build());
    }

    @Test
    void testBuilderRequiresPoints() {
        assertThrows(IllegalStateException.class, () ->
                ScatterPlot.builder().id("plot1").build());
    }

    @Test
    void testImmutablePoints() {
        ScatterPlot plot = ScatterPlot.builder()
                .id("plot1")
                .addPoint(1, 2)
                .build();

        assertThrows(UnsupportedOperationException.class, () ->
                plot.getPoints().add(new ScatterPlot.DataPoint(3, 4)));
    }
}
