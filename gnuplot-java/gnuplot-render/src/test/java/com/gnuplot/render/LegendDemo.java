package com.gnuplot.render;

import com.gnuplot.render.elements.Legend;
import com.gnuplot.render.elements.LinePlot;
import com.gnuplot.render.style.MarkerStyle;
import com.gnuplot.render.style.PointStyle;
import com.gnuplot.render.color.Color;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

/**
 * Demonstrates legend capabilities with various positions, columns, and styles.
 */
public class LegendDemo {

    public static void main(String[] args) throws IOException, RenderException {
        com.gnuplot.render.svg.SvgRenderer renderer = new com.gnuplot.render.svg.SvgRenderer();

        // Demo 1: Single-column legend with different positions
        createPositionDemo(renderer);

        // Demo 2: Multi-column legend
        createMultiColumnDemo(renderer);

        // Demo 3: Custom styling (colors, fonts)
        createCustomStyleDemo(renderer);

        // Demo 4: Mixed line and marker legends
        createMixedSymbolDemo(renderer);

        // Demo 5: Legend with actual plot
        createLegendWithPlotDemo(renderer);

        System.out.println("\nLegend demos completed successfully!");
        System.out.println("Total files created: 5");
    }

    private static void createPositionDemo(com.gnuplot.render.svg.SvgRenderer renderer)
            throws IOException, RenderException {
        // Create plots with dummy data
        LinePlot.Point2D[] points1 = createSinePoints(50, 1.0);
        LinePlot.Point2D[] points2 = createSinePoints(50, 0.5);

        LinePlot line1 = LinePlot.builder()
                .id("line1")
                .label("Series 1")
                .points(List.of(points1))
                .color("#4A90E2")
                .build();

        LinePlot line2 = LinePlot.builder()
                .id("line2")
                .label("Series 2")
                .points(List.of(points2))
                .color("#50C878")
                .build();

        // Legend at top-right (default)
        Legend legend = Legend.builder()
                .id("legend1")
                .position(Legend.Position.TOP_RIGHT)
                .addEntry("Series 1", "#4A90E2", LinePlot.LineStyle.SOLID)
                .addEntry("Series 2", "#50C878", LinePlot.LineStyle.DASHED)
                .build();

        Scene scene = Scene.builder()
                .title("Legend Positions - Top Right")
                .dimensions(800, 600)
                .viewport(Viewport.of2D(0, 10, -1.5, 1.5))
                .addElement(line1)
                .addElement(line2)
                .addElement(legend)
                .build();

        try (FileOutputStream out = new FileOutputStream("legend-position-top-right.svg")) {
            renderer.render(scene, out);
        }
        System.out.println("Created legend-position-top-right.svg");
    }

    private static void createMultiColumnDemo(com.gnuplot.render.svg.SvgRenderer renderer)
            throws IOException, RenderException {
        // Create legend with 6 entries in 2 columns
        Legend legend = Legend.builder()
                .id("legend2")
                .position(Legend.Position.BOTTOM_RIGHT)
                .columns(2)
                .addEntry("Series 1", "#4A90E2", LinePlot.LineStyle.SOLID)
                .addEntry("Series 2", "#50C878", LinePlot.LineStyle.DASHED)
                .addEntry("Series 3", "#FFD700", LinePlot.LineStyle.DOTTED)
                .addEntry("Series 4", "#FF6B6B", LinePlot.LineStyle.DASH_DOT)
                .addEntry("Series 5", "#9C27B0", LinePlot.LineStyle.SOLID)
                .addEntry("Series 6", "#00BCD4", LinePlot.LineStyle.DASHED)
                .build();

        Scene scene = Scene.builder()
                .title("Multi-Column Legend (2 columns)")
                .dimensions(800, 600)
                .viewport(Viewport.of2D(0, 10, 0, 10))
                .addElement(legend)
                .build();

        try (FileOutputStream out = new FileOutputStream("legend-multi-column.svg")) {
            renderer.render(scene, out);
        }
        System.out.println("Created legend-multi-column.svg");
    }

    private static void createCustomStyleDemo(com.gnuplot.render.svg.SvgRenderer renderer)
            throws IOException, RenderException {
        // Custom styled legend
        Legend legend = Legend.builder()
                .id("legend3")
                .position(Legend.Position.TOP_LEFT)
                .fontFamily("Courier New")
                .fontSize(12)
                .borderColor("#FF0000")
                .backgroundColor("#FFFFCC")
                .addEntry("Data A", "#4A90E2", LinePlot.LineStyle.SOLID)
                .addEntry("Data B", "#50C878", LinePlot.LineStyle.DASHED)
                .addEntry("Data C", "#FFD700", LinePlot.LineStyle.DOTTED)
                .build();

        Scene scene = Scene.builder()
                .title("Custom Styled Legend")
                .dimensions(800, 600)
                .viewport(Viewport.of2D(0, 10, 0, 10))
                .addElement(legend)
                .build();

        try (FileOutputStream out = new FileOutputStream("legend-custom-style.svg")) {
            renderer.render(scene, out);
        }
        System.out.println("Created legend-custom-style.svg");
    }

    private static void createMixedSymbolDemo(com.gnuplot.render.svg.SvgRenderer renderer)
            throws IOException, RenderException {
        // Legend with lines, markers, and combined symbols
        Legend.Builder legendBuilder = Legend.builder()
                .id("legend4")
                .position(Legend.Position.RIGHT_CENTER)
                .columns(1);

        // Add line entries
        legendBuilder.addEntry("Line Plot", "#4A90E2", LinePlot.LineStyle.SOLID);

        // Add marker entries
        MarkerStyle circleMarker = MarkerStyle.filled(8, Color.fromRGB24(0x50C878), PointStyle.CIRCLE);
        legendBuilder.addEntry(Legend.LegendEntry.withMarker("Circles", "#50C878", circleMarker));

        MarkerStyle squareMarker = MarkerStyle.filled(8, Color.fromRGB24(0xFFD700), PointStyle.SQUARE);
        legendBuilder.addEntry(Legend.LegendEntry.withMarker("Squares", "#FFD700", squareMarker));

        // Add combined line+marker entries
        MarkerStyle triangleMarker = MarkerStyle.filled(8, Color.fromRGB24(0xFF6B6B), PointStyle.TRIANGLE_UP);
        legendBuilder.addEntry(Legend.LegendEntry.withLineAndMarker(
                "Line+Marker", "#FF6B6B", LinePlot.LineStyle.DASHED, triangleMarker));

        Legend legend = legendBuilder.build();

        Scene scene = Scene.builder()
                .title("Mixed Symbol Types Legend")
                .dimensions(800, 600)
                .viewport(Viewport.of2D(0, 10, 0, 10))
                .addElement(legend)
                .build();

        try (FileOutputStream out = new FileOutputStream("legend-mixed-symbols.svg")) {
            renderer.render(scene, out);
        }
        System.out.println("Created legend-mixed-symbols.svg");
    }

    private static void createLegendWithPlotDemo(com.gnuplot.render.svg.SvgRenderer renderer)
            throws IOException, RenderException {
        // Create three line plots with different styles
        LinePlot.Point2D[] points1 = createSinePoints(100, 1.0);
        LinePlot.Point2D[] points2 = createSinePoints(100, 0.7);
        LinePlot.Point2D[] points3 = createSinePoints(100, 0.4);

        LinePlot line1 = LinePlot.builder()
                .id("sine1")
                .label("Amplitude 1.0")
                .points(List.of(points1))
                .color("#4A90E2")
                .lineStyle(LinePlot.LineStyle.SOLID)
                .build();

        LinePlot line2 = LinePlot.builder()
                .id("sine2")
                .label("Amplitude 0.7")
                .points(List.of(points2))
                .color("#50C878")
                .lineStyle(LinePlot.LineStyle.DASHED)
                .build();

        LinePlot line3 = LinePlot.builder()
                .id("sine3")
                .label("Amplitude 0.4")
                .points(List.of(points3))
                .color("#FFD700")
                .lineStyle(LinePlot.LineStyle.DOTTED)
                .build();

        // Create legend
        Legend legend = Legend.builder()
                .id("legend5")
                .position(Legend.Position.TOP_LEFT)
                .fontSize(11)
                .addEntry("Amplitude 1.0", "#4A90E2", LinePlot.LineStyle.SOLID)
                .addEntry("Amplitude 0.7", "#50C878", LinePlot.LineStyle.DASHED)
                .addEntry("Amplitude 0.4", "#FFD700", LinePlot.LineStyle.DOTTED)
                .build();

        Scene scene = Scene.builder()
                .title("Sine Waves with Legend")
                .dimensions(800, 600)
                .viewport(Viewport.of2D(0, 2 * Math.PI, -1.5, 1.5))
                .addElement(line1)
                .addElement(line2)
                .addElement(line3)
                .addElement(legend)
                .build();

        try (FileOutputStream out = new FileOutputStream("legend-with-plot.svg")) {
            renderer.render(scene, out);
        }
        System.out.println("Created legend-with-plot.svg");
    }

    private static LinePlot.Point2D[] createSinePoints(int count, double amplitude) {
        LinePlot.Point2D[] points = new LinePlot.Point2D[count];
        for (int i = 0; i < count; i++) {
            double x = i * 2 * Math.PI / (count - 1);
            double y = amplitude * Math.sin(x);
            points[i] = new LinePlot.Point2D(x, y);
        }
        return points;
    }
}
