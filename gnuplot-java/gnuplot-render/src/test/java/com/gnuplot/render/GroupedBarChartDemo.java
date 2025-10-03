package com.gnuplot.render;

import com.gnuplot.render.elements.BarChart;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

/**
 * Demonstrates grouped and stacked bar chart rendering.
 */
public class GroupedBarChartDemo {

    public static void main(String[] args) throws IOException, RenderException {
        Renderer renderer = new com.gnuplot.render.svg.SvgRenderer();

        // Create grouped bar chart (side-by-side bars)
        BarChart groupedChart = BarChart.builder()
                .id("grouped-sales")
                .label("Quarterly Sales by Region (Grouped)")
                .addGroup(1.0, List.of(25.0, 30.0, 20.0),
                         List.of("#4A90E2", "#50C878", "#FFD700"))
                .addGroup(2.0, List.of(40.0, 35.0, 38.0),
                         List.of("#4A90E2", "#50C878", "#FFD700"))
                .addGroup(3.0, List.of(35.0, 42.0, 30.0),
                         List.of("#4A90E2", "#50C878", "#FFD700"))
                .addGroup(4.0, List.of(50.0, 45.0, 48.0),
                         List.of("#4A90E2", "#50C878", "#FFD700"))
                .barWidth(0.7)
                .groupingMode(BarChart.GroupingMode.GROUPED)
                .build();

        Scene groupedScene = Scene.builder()
                .title("Grouped Bar Chart Demo")
                .dimensions(800, 600)
                .viewport(Viewport.of2D(0, 5, 0, 60))
                .addElement(groupedChart)
                .build();

        try (FileOutputStream out = new FileOutputStream("grouped-bar-chart.svg")) {
            renderer.render(groupedScene, out);
        }
        System.out.println("Created grouped-bar-chart.svg");

        // Create stacked bar chart
        BarChart stackedChart = BarChart.builder()
                .id("stacked-sales")
                .label("Quarterly Sales by Region (Stacked)")
                .addGroup(1.0, List.of(25.0, 30.0, 20.0),
                         List.of("#4A90E2", "#50C878", "#FFD700"))
                .addGroup(2.0, List.of(40.0, 35.0, 38.0),
                         List.of("#4A90E2", "#50C878", "#FFD700"))
                .addGroup(3.0, List.of(35.0, 42.0, 30.0),
                         List.of("#4A90E2", "#50C878", "#FFD700"))
                .addGroup(4.0, List.of(50.0, 45.0, 48.0),
                         List.of("#4A90E2", "#50C878", "#FFD700"))
                .barWidth(0.6)
                .groupingMode(BarChart.GroupingMode.STACKED)
                .build();

        Scene stackedScene = Scene.builder()
                .title("Stacked Bar Chart Demo")
                .dimensions(800, 600)
                .viewport(Viewport.of2D(0, 5, 0, 150))
                .addElement(stackedChart)
                .build();

        try (FileOutputStream out = new FileOutputStream("stacked-bar-chart.svg")) {
            renderer.render(stackedScene, out);
        }
        System.out.println("Created stacked-bar-chart.svg");

        // Create grouped chart with many series (7 series)
        BarChart multiSeriesChart = BarChart.builder()
                .id("multi-series")
                .label("Weekly Data by Day")
                .addGroup(1.0, List.of(10.0, 12.0, 8.0, 15.0, 11.0, 9.0, 13.0))
                .addGroup(2.0, List.of(14.0, 16.0, 12.0, 18.0, 15.0, 13.0, 17.0))
                .addGroup(3.0, List.of(18.0, 20.0, 16.0, 22.0, 19.0, 17.0, 21.0))
                .barWidth(0.9)
                .build();

        Scene multiSeriesScene = Scene.builder()
                .title("Multi-Series Grouped Bar Chart")
                .dimensions(800, 600)
                .viewport(Viewport.of2D(0, 4, 0, 25))
                .addElement(multiSeriesChart)
                .build();

        try (FileOutputStream out = new FileOutputStream("multi-series-bar-chart.svg")) {
            renderer.render(multiSeriesScene, out);
        }
        System.out.println("Created multi-series-bar-chart.svg");

        // Create stacked horizontal bars
        BarChart horizontalStackedChart = BarChart.builder()
                .id("horizontal-stacked")
                .label("Horizontal Stacked Bar Chart")
                .addGroup(1.0, List.of(20.0, 15.0, 10.0),
                         List.of("#FF6B6B", "#4ECDC4", "#95E1D3"))
                .addGroup(2.0, List.of(30.0, 20.0, 15.0),
                         List.of("#FF6B6B", "#4ECDC4", "#95E1D3"))
                .addGroup(3.0, List.of(25.0, 18.0, 12.0),
                         List.of("#FF6B6B", "#4ECDC4", "#95E1D3"))
                .orientation(BarChart.Orientation.HORIZONTAL)
                .groupingMode(BarChart.GroupingMode.STACKED)
                .barWidth(0.5)
                .build();

        Scene horizontalStackedScene = Scene.builder()
                .title("Horizontal Stacked Bar Chart Demo")
                .dimensions(800, 600)
                .viewport(Viewport.of2D(0, 70, 0, 4))
                .addElement(horizontalStackedChart)
                .build();

        try (FileOutputStream out = new FileOutputStream("horizontal-stacked-bar-chart.svg")) {
            renderer.render(horizontalStackedScene, out);
        }
        System.out.println("Created horizontal-stacked-bar-chart.svg");

        // Create comparison: two groups side-by-side vs stacked
        BarChart twoSeriesGrouped = BarChart.builder()
                .id("two-series-grouped")
                .label("Revenue vs Expenses (Grouped)")
                .addGroup(1.0, List.of(50.0, 30.0), List.of("#50C878", "#FF6B6B"))
                .addGroup(2.0, List.of(60.0, 35.0), List.of("#50C878", "#FF6B6B"))
                .addGroup(3.0, List.of(55.0, 40.0), List.of("#50C878", "#FF6B6B"))
                .addGroup(4.0, List.of(70.0, 45.0), List.of("#50C878", "#FF6B6B"))
                .barWidth(0.6)
                .build();

        Scene twoSeriesScene = Scene.builder()
                .title("Two-Series Comparison")
                .dimensions(800, 600)
                .viewport(Viewport.of2D(0, 5, 0, 80))
                .addElement(twoSeriesGrouped)
                .build();

        try (FileOutputStream out = new FileOutputStream("two-series-grouped-bar-chart.svg")) {
            renderer.render(twoSeriesScene, out);
        }
        System.out.println("Created two-series-grouped-bar-chart.svg");

        System.out.println("\nGrouped and stacked bar chart demos completed successfully!");
        System.out.println("Total files created: 5");
    }
}
