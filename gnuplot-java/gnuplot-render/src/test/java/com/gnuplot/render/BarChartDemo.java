package com.gnuplot.render;

import com.gnuplot.render.elements.BarChart;

import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Demonstrates bar chart rendering with both vertical and horizontal orientations.
 */
public class BarChartDemo {

    public static void main(String[] args) throws IOException, RenderException {
        // Create vertical bar chart
        BarChart verticalChart = BarChart.builder()
                .id("vertical-sales")
                .label("Quarterly Sales (Vertical)")
                .addBar(1.0, 25.0, "#4A90E2", "Q1")
                .addBar(2.0, 40.0, "#50C878", "Q2")
                .addBar(3.0, 35.0, "#FFD700", "Q3")
                .addBar(4.0, 50.0, "#FF6B6B", "Q4")
                .barWidth(0.6)
                .orientation(BarChart.Orientation.VERTICAL)
                .build();

        Scene verticalScene = Scene.builder()
                .title("Vertical Bar Chart Demo")
                .dimensions(800, 600)
                .viewport(Viewport.of2D(0, 5, 0, 60))
                .addElement(verticalChart)
                .build();

        Renderer renderer = new com.gnuplot.render.svg.SvgRenderer();
        try (FileOutputStream out = new FileOutputStream("vertical-bar-chart.svg")) {
            renderer.render(verticalScene, out);
        }
        System.out.println("Created vertical-bar-chart.svg");

        // Create horizontal bar chart
        BarChart horizontalChart = BarChart.builder()
                .id("horizontal-sales")
                .label("Quarterly Sales (Horizontal)")
                .addBar(1.0, 25.0, "#4A90E2", "Q1")
                .addBar(2.0, 40.0, "#50C878", "Q2")
                .addBar(3.0, 35.0, "#FFD700", "Q3")
                .addBar(4.0, 50.0, "#FF6B6B", "Q4")
                .barWidth(0.6)
                .orientation(BarChart.Orientation.HORIZONTAL)
                .build();

        Scene horizontalScene = Scene.builder()
                .title("Horizontal Bar Chart Demo")
                .dimensions(800, 600)
                .viewport(Viewport.of2D(0, 60, 0, 5))
                .addElement(horizontalChart)
                .build();

        try (FileOutputStream out = new FileOutputStream("horizontal-bar-chart.svg")) {
            renderer.render(horizontalScene, out);
        }
        System.out.println("Created horizontal-bar-chart.svg");

        // Create comparison chart with positive and negative values
        BarChart comparisonChart = BarChart.builder()
                .id("profit-loss")
                .label("Profit/Loss by Department")
                .addBar(1.0, 15.0, "#50C878", "Sales")
                .addBar(2.0, -8.0, "#FF6B6B", "Marketing")
                .addBar(3.0, 22.0, "#50C878", "Engineering")
                .addBar(4.0, -5.0, "#FF6B6B", "Operations")
                .addBar(5.0, 18.0, "#50C878", "Support")
                .barWidth(0.7)
                .build();

        Scene comparisonScene = Scene.builder()
                .title("Profit/Loss Bar Chart")
                .dimensions(800, 600)
                .viewport(Viewport.of2D(0, 6, -10, 35))
                .addElement(comparisonChart)
                .build();

        try (FileOutputStream out = new FileOutputStream("comparison-bar-chart.svg")) {
            renderer.render(comparisonScene, out);
        }
        System.out.println("Created comparison-bar-chart.svg");

        // Create narrow bars chart
        BarChart narrowChart = BarChart.builder()
                .id("narrow-bars")
                .label("Narrow Bars Demo")
                .addBar(1.0, 10.0, "#E91E63")
                .addBar(2.0, 25.0, "#9C27B0")
                .addBar(3.0, 18.0, "#3F51B5")
                .addBar(4.0, 35.0, "#2196F3")
                .addBar(5.0, 22.0, "#00BCD4")
                .addBar(6.0, 40.0, "#009688")
                .addBar(7.0, 15.0, "#4CAF50")
                .barWidth(0.3)
                .build();

        Scene narrowScene = Scene.builder()
                .title("Narrow Bars Chart")
                .dimensions(800, 600)
                .viewport(Viewport.of2D(0, 8, 0, 45))
                .addElement(narrowChart)
                .build();

        try (FileOutputStream out = new FileOutputStream("narrow-bar-chart.svg")) {
            renderer.render(narrowScene, out);
        }
        System.out.println("Created narrow-bar-chart.svg");

        // Create wide bars chart
        BarChart wideChart = BarChart.builder()
                .id("wide-bars")
                .label("Wide Bars Demo")
                .addBar(1.0, 20.0, "#FF5722")
                .addBar(2.0, 35.0, "#795548")
                .addBar(3.0, 28.0, "#607D8B")
                .barWidth(0.95)
                .build();

        Scene wideScene = Scene.builder()
                .title("Wide Bars Chart")
                .dimensions(800, 600)
                .viewport(Viewport.of2D(0, 4, 0, 40))
                .addElement(wideChart)
                .build();

        try (FileOutputStream out = new FileOutputStream("wide-bar-chart.svg")) {
            renderer.render(wideScene, out);
        }
        System.out.println("Created wide-bar-chart.svg");

        System.out.println("\nBar chart demos completed successfully!");
        System.out.println("Total files created: 5");
    }
}
