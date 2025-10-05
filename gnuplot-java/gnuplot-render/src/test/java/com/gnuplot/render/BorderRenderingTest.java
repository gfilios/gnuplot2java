package com.gnuplot.render;

import com.gnuplot.render.elements.LinePlot;
import com.gnuplot.render.svg.SvgRenderer;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BorderRenderingTest {

    @Test
    void testBorderRendering() throws Exception {
        // Create a simple scene with border enabled (default)
        LinePlot.Point2D[] points = new LinePlot.Point2D[]{
                new LinePlot.Point2D(0, 0),
                new LinePlot.Point2D(1, 1),
                new LinePlot.Point2D(2, 4)
        };

        LinePlot plot = LinePlot.builder()
                .id("test-plot")
                .points(List.of(points))
                .color("#FF0000")
                .build();

        Viewport viewport = Viewport.of2D(0, 2, 0, 4);

        Scene scene = Scene.builder()
                .dimensions(800, 600)
                .viewport(viewport)
                .addElement(plot)
                .border(true) // explicitly enable border
                .build();

        SvgRenderer renderer = new SvgRenderer();
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        renderer.render(scene, output);

        String svg = output.toString(StandardCharsets.UTF_8);

        // Verify that the border path element exists
        assertThat(svg)
                .as("SVG should contain border path element")
                .contains("path")
                .contains("stroke=\"#000000\"")
                .contains("stroke-width=\"1.0\"")
                .contains("fill=\"none\"");

        // Verify coordinates match plot boundaries (54, 775, 66, 564 based on margins)
        assertThat(svg)
                .as("Border should use plotLeft, plotRight, plotTop, plotBottom coordinates")
                .contains("M 54.00 66.00");  // upper-left
    }

    @Test
    void testBorderDisabled() throws Exception {
        // Create a simple scene with border disabled
        LinePlot.Point2D[] points = new LinePlot.Point2D[]{
                new LinePlot.Point2D(0, 0),
                new LinePlot.Point2D(1, 1),
                new LinePlot.Point2D(2, 4)
        };

        LinePlot plot = LinePlot.builder()
                .id("test-plot")
                .points(List.of(points))
                .color("#FF0000")
                .build();

        Viewport viewport = Viewport.of2D(0, 2, 0, 4);

        Scene scene = Scene.builder()
                .dimensions(800, 600)
                .viewport(viewport)
                .addElement(plot)
                .border(false) // disable border
                .build();

        SvgRenderer renderer = new SvgRenderer();
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        renderer.render(scene, output);

        String svg = output.toString(StandardCharsets.UTF_8);

        // Verify that scene.isShowBorder() returns false
        assertThat(scene.isShowBorder())
                .as("Scene should have border disabled")
                .isFalse();

        // Border path should NOT exist when disabled
        // We can check by looking for the specific border path pattern
        // Since other elements may have paths, we check for absence of border-specific coordinates
        assertThat(svg.contains("M 54.00 66.00 L 775.00 66.00 L 775.00 564.00"))
                .as("Border path should not exist when border is disabled")
                .isFalse();
    }
}
