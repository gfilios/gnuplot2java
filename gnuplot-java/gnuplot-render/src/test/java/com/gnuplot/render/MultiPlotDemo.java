package com.gnuplot.render;

import com.gnuplot.render.elements.BarChart;
import com.gnuplot.render.elements.LinePlot;
import com.gnuplot.render.elements.ScatterPlot;
import com.gnuplot.render.style.MarkerStyle;
import com.gnuplot.render.style.PointStyle;
import com.gnuplot.render.color.Color;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

/**
 * Demonstrates multi-plot layouts with grid and custom positioning.
 */
public class MultiPlotDemo {

    public static void main(String[] args) throws IOException, RenderException {
        com.gnuplot.render.svg.SvgRenderer renderer = new com.gnuplot.render.svg.SvgRenderer();

        // Demo 1: 2x2 Grid Layout
        Scene sinePlot = createSinePlot();
        Scene cosinePlot = createCosinePlot();
        Scene barPlot = createBarPlot();
        Scene scatterPlot = createScatterPlot();

        MultiPlotLayout grid2x2 = MultiPlotLayout.builder()
                .title("2x2 Grid Layout - Multiple Plot Types")
                .dimensions(1200, 900)
                .gridLayout(2, 2)
                .addPlot(sinePlot, 0, 0)
                .addPlot(cosinePlot, 0, 1)
                .addPlot(barPlot, 1, 0)
                .addPlot(scatterPlot, 1, 1)
                .build();

        try (FileOutputStream out = new FileOutputStream("multi-plot-2x2-grid.svg")) {
            renderer.render(grid2x2, out);
        }
        System.out.println("Created multi-plot-2x2-grid.svg");

        // Demo 2: 3x1 Grid Layout (horizontal panels)
        MultiPlotLayout grid3x1 = MultiPlotLayout.builder()
                .title("3x1 Grid Layout - Horizontal Panels")
                .dimensions(1600, 500)
                .gridLayout(1, 3)
                .addPlot(sinePlot, 0, 0)
                .addPlot(cosinePlot, 0, 1)
                .addPlot(scatterPlot, 0, 2)
                .build();

        try (FileOutputStream out = new FileOutputStream("multi-plot-3x1-grid.svg")) {
            renderer.render(grid3x1, out);
        }
        System.out.println("Created multi-plot-3x1-grid.svg");

        // Demo 3: 1x3 Grid Layout (vertical panels)
        MultiPlotLayout grid1x3 = MultiPlotLayout.builder()
                .title("1x3 Grid Layout - Vertical Panels")
                .dimensions(700, 1200)
                .gridLayout(3, 1)
                .addPlot(sinePlot, 0, 0)
                .addPlot(barPlot, 1, 0)
                .addPlot(scatterPlot, 2, 0)
                .build();

        try (FileOutputStream out = new FileOutputStream("multi-plot-1x3-grid.svg")) {
            renderer.render(grid1x3, out);
        }
        System.out.println("Created multi-plot-1x3-grid.svg");

        // Demo 4: Custom Layout (dashboard style)
        Scene largePlot = createSinePlot();
        Scene smallPlot1 = createBarPlot();
        Scene smallPlot2 = createScatterPlot();

        MultiPlotLayout customLayout = MultiPlotLayout.builder()
                .title("Custom Layout - Dashboard Style")
                .dimensions(1200, 800)
                .customLayout()
                .addPlot(largePlot, 0, 0, 0.65, 1.0)      // Left 65%
                .addPlot(smallPlot1, 0.65, 0, 0.35, 0.5)   // Top-right 35%
                .addPlot(smallPlot2, 0.65, 0.5, 0.35, 0.5) // Bottom-right 35%
                .build();

        try (FileOutputStream out = new FileOutputStream("multi-plot-custom-layout.svg")) {
            renderer.render(customLayout, out);
        }
        System.out.println("Created multi-plot-custom-layout.svg");

        System.out.println("\nMulti-plot demos completed successfully!");
        System.out.println("Total files created: 4");
    }

    private static Scene createSinePlot() {
        LinePlot.Point2D[] points = new LinePlot.Point2D[100];
        for (int i = 0; i < 100; i++) {
            double x = i * 2 * Math.PI / 100;
            double y = Math.sin(x);
            points[i] = new LinePlot.Point2D(x, y);
        }

        LinePlot linePlot = LinePlot.builder()
                .id("sine")
                .label("sin(x)")
                .points(List.of(points))
                .color("#4A90E2")
                .build();

        return Scene.builder()
                .title("Sine Wave")
                .dimensions(600, 450)
                .viewport(Viewport.of2D(0, 2 * Math.PI, -1.2, 1.2))
                .addElement(linePlot)
                .build();
    }

    private static Scene createCosinePlot() {
        LinePlot.Point2D[] points = new LinePlot.Point2D[100];
        for (int i = 0; i < 100; i++) {
            double x = i * 2 * Math.PI / 100;
            double y = Math.cos(x);
            points[i] = new LinePlot.Point2D(x, y);
        }

        LinePlot linePlot = LinePlot.builder()
                .id("cosine")
                .label("cos(x)")
                .points(List.of(points))
                .color("#50C878")
                .build();

        return Scene.builder()
                .title("Cosine Wave")
                .dimensions(600, 450)
                .viewport(Viewport.of2D(0, 2 * Math.PI, -1.2, 1.2))
                .addElement(linePlot)
                .build();
    }

    private static Scene createBarPlot() {
        BarChart barChart = BarChart.builder()
                .id("bars")
                .label("Sample Data")
                .addBar(1.0, 25.0, "#FFD700", "A")
                .addBar(2.0, 40.0, "#FF6B6B", "B")
                .addBar(3.0, 35.0, "#9C27B0", "C")
                .addBar(4.0, 50.0, "#00BCD4", "D")
                .barWidth(0.7)
                .build();

        return Scene.builder()
                .title("Bar Chart")
                .dimensions(600, 450)
                .viewport(Viewport.of2D(0, 5, 0, 60))
                .addElement(barChart)
                .build();
    }

    private static Scene createScatterPlot() {
        ScatterPlot.DataPoint[] points = new ScatterPlot.DataPoint[50];
        for (int i = 0; i < 50; i++) {
            double x = Math.random() * 10;
            double y = Math.random() * 10;
            points[i] = new ScatterPlot.DataPoint(x, y);
        }

        MarkerStyle markerStyle = MarkerStyle.filled(8, Color.fromRGB24(0xFF6B6B), PointStyle.CIRCLE);
        ScatterPlot scatterPlot = ScatterPlot.builder()
                .id("scatter")
                .label("Random Data")
                .points(List.of(points))
                .markerStyle(markerStyle)
                .build();

        return Scene.builder()
                .title("Scatter Plot")
                .dimensions(600, 450)
                .viewport(Viewport.of2D(0, 10, 0, 10))
                .addElement(scatterPlot)
                .build();
    }
}
