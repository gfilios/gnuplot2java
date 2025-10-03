package com.gnuplot.render;

import com.gnuplot.render.elements.BarChart;

import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Demonstrates error bar rendering on bar charts.
 */
public class ErrorBarDemo {

    public static void main(String[] args) throws IOException, RenderException {
        Renderer renderer = new com.gnuplot.render.svg.SvgRenderer();

        // Create vertical bar chart with symmetric error bars
        BarChart symmetricChart = BarChart.builder()
                .id("symmetric-errors")
                .label("Measurement Data with Symmetric Errors")
                .addBar(1.0, 25.0, "#4A90E2", "A", 3.0, 3.0)
                .addBar(2.0, 40.0, "#50C878", "B", 4.0, 4.0)
                .addBar(3.0, 35.0, "#FFD700", "C", 3.5, 3.5)
                .addBar(4.0, 50.0, "#FF6B6B", "D", 5.0, 5.0)
                .barWidth(0.6)
                .build();

        Scene symmetricScene = Scene.builder()
                .title("Symmetric Error Bars")
                .dimensions(800, 600)
                .viewport(Viewport.of2D(0, 5, 0, 60))
                .addElement(symmetricChart)
                .build();

        try (FileOutputStream out = new FileOutputStream("symmetric-error-bars.svg")) {
            renderer.render(symmetricScene, out);
        }
        System.out.println("Created symmetric-error-bars.svg");

        // Create bar chart with asymmetric error bars
        BarChart asymmetricChart = BarChart.builder()
                .id("asymmetric-errors")
                .label("Scientific Data with Asymmetric Errors")
                .addBar(1.0, 30.0, "#4A90E2", "Trial 1", 2.0, 5.0)
                .addBar(2.0, 45.0, "#50C878", "Trial 2", 3.0, 6.0)
                .addBar(3.0, 38.0, "#FFD700", "Trial 3", 4.0, 4.0)
                .addBar(4.0, 52.0, "#FF6B6B", "Trial 4", 3.5, 7.0)
                .barWidth(0.7)
                .build();

        Scene asymmetricScene = Scene.builder()
                .title("Asymmetric Error Bars")
                .dimensions(800, 600)
                .viewport(Viewport.of2D(0, 5, 0, 65))
                .addElement(asymmetricChart)
                .build();

        try (FileOutputStream out = new FileOutputStream("asymmetric-error-bars.svg")) {
            renderer.render(asymmetricScene, out);
        }
        System.out.println("Created asymmetric-error-bars.svg");

        // Create horizontal bar chart with error bars
        BarChart horizontalErrorChart = BarChart.builder()
                .id("horizontal-errors")
                .label("Horizontal Bars with Error Bars")
                .addBar(1.0, 35.0, "#9C27B0", "Method A", 4.0, 6.0)
                .addBar(2.0, 48.0, "#E91E63", "Method B", 5.0, 5.0)
                .addBar(3.0, 42.0, "#FF9800", "Method C", 3.0, 7.0)
                .orientation(BarChart.Orientation.HORIZONTAL)
                .barWidth(0.5)
                .build();

        Scene horizontalErrorScene = Scene.builder()
                .title("Horizontal Bars with Error Bars")
                .dimensions(800, 600)
                .viewport(Viewport.of2D(0, 60, 0, 4))
                .addElement(horizontalErrorChart)
                .build();

        try (FileOutputStream out = new FileOutputStream("horizontal-error-bars.svg")) {
            renderer.render(horizontalErrorScene, out);
        }
        System.out.println("Created horizontal-error-bars.svg");

        // Create chart with only upper error bars
        BarChart upperOnlyChart = BarChart.builder()
                .id("upper-only")
                .label("Upper Error Bars Only")
                .addBar(1.0, 20.0, "#00BCD4", "Q1", null, 5.0)
                .addBar(2.0, 35.0, "#4CAF50", "Q2", null, 6.0)
                .addBar(3.0, 28.0, "#FFC107", "Q3", null, 4.0)
                .addBar(4.0, 42.0, "#F44336", "Q4", null, 7.0)
                .barWidth(0.6)
                .build();

        Scene upperOnlyScene = Scene.builder()
                .title("Upper Error Bars Only")
                .dimensions(800, 600)
                .viewport(Viewport.of2D(0, 5, 0, 55))
                .addElement(upperOnlyChart)
                .build();

        try (FileOutputStream out = new FileOutputStream("upper-error-bars.svg")) {
            renderer.render(upperOnlyScene, out);
        }
        System.out.println("Created upper-error-bars.svg");

        // Create chart with only lower error bars
        BarChart lowerOnlyChart = BarChart.builder()
                .id("lower-only")
                .label("Lower Error Bars Only")
                .addBar(1.0, 30.0, "#3F51B5", "Sample 1", 4.0, null)
                .addBar(2.0, 45.0, "#009688", "Sample 2", 5.0, null)
                .addBar(3.0, 38.0, "#8BC34A", "Sample 3", 3.0, null)
                .addBar(4.0, 52.0, "#FF5722", "Sample 4", 6.0, null)
                .barWidth(0.6)
                .build();

        Scene lowerOnlyScene = Scene.builder()
                .title("Lower Error Bars Only")
                .dimensions(800, 600)
                .viewport(Viewport.of2D(0, 5, 0, 60))
                .addElement(lowerOnlyChart)
                .build();

        try (FileOutputStream out = new FileOutputStream("lower-error-bars.svg")) {
            renderer.render(lowerOnlyScene, out);
        }
        System.out.println("Created lower-error-bars.svg");

        System.out.println("\nError bar demos completed successfully!");
        System.out.println("Total files created: 5");
    }
}
