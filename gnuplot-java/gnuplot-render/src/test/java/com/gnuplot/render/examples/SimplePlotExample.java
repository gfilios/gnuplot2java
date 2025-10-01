package com.gnuplot.render.examples;

import com.gnuplot.render.*;
import com.gnuplot.render.elements.Axis;
import com.gnuplot.render.elements.Legend;
import com.gnuplot.render.elements.LinePlot;
import com.gnuplot.render.svg.SvgRenderer;

import java.io.FileOutputStream;

/**
 * Simple examples demonstrating how to create plots.
 */
public class SimplePlotExample {

    /**
     * Creates a minimal line plot.
     */
    public static void createMinimalPlot() throws Exception {
        // 1. Create a line plot with some data points
        LinePlot linePlot = LinePlot.builder()
                .id("myplot")
                .addPoint(0.0, 0.0)
                .addPoint(1.0, 1.0)
                .addPoint(2.0, 4.0)
                .addPoint(3.0, 9.0)
                .color("#0000FF")
                .build();

        // 2. Create a scene and add the plot
        Scene scene = Scene.builder()
                .dimensions(800, 600)
                .title("My First Plot")
                .viewport(Viewport.of2D(0.0, 3.0, 0.0, 9.0))
                .addElement(linePlot)
                .build();

        // 3. Render to SVG
        SvgRenderer renderer = new SvgRenderer();
        try (FileOutputStream output = new FileOutputStream("plot.svg")) {
            renderer.render(scene, output);
        }

        System.out.println("Plot saved to plot.svg");
    }

    /**
     * Creates a complete plot with axes and legend.
     */
    public static void createCompletePlot() throws Exception {
        // 1. Create the line plot
        LinePlot sinePlot = LinePlot.builder()
                .id("sine")
                .addPoint(0.0, 0.0)
                .addPoint(0.5, 0.5)
                .addPoint(1.0, 1.0)
                .addPoint(1.5, 0.5)
                .addPoint(2.0, 0.0)
                .addPoint(2.5, -0.5)
                .addPoint(3.0, -1.0)
                .color("#FF0000")
                .lineStyle(LinePlot.LineStyle.SOLID)
                .label("sin(x)")
                .build();

        // 2. Create X axis
        Axis xAxis = Axis.builder()
                .id("xaxis")
                .axisType(Axis.AxisType.X_AXIS)
                .range(0.0, 3.0)
                .label("X Axis")
                .showGrid(true)
                .build();

        // 3. Create Y axis
        Axis yAxis = Axis.builder()
                .id("yaxis")
                .axisType(Axis.AxisType.Y_AXIS)
                .range(-1.0, 1.0)
                .label("Y Axis")
                .showGrid(true)
                .build();

        // 4. Create legend
        Legend legend = Legend.builder()
                .id("legend")
                .position(Legend.Position.TOP_RIGHT)
                .addEntry("sin(x)", "#FF0000", LinePlot.LineStyle.SOLID)
                .showBorder(true)
                .build();

        // 5. Create scene with all elements
        Scene scene = Scene.builder()
                .dimensions(800, 600)
                .title("Sine Wave")
                .viewport(Viewport.of2D(0.0, 3.0, -1.0, 1.0))
                .addElement(xAxis)
                .addElement(yAxis)
                .addElement(sinePlot)
                .addElement(legend)
                .build();

        // 6. Render to SVG
        SvgRenderer renderer = new SvgRenderer();
        try (FileOutputStream output = new FileOutputStream("sine_wave.svg")) {
            renderer.render(scene, output);
        }

        System.out.println("Plot saved to sine_wave.svg");
    }

    /**
     * Creates a plot with multiple lines.
     */
    public static void createMultiLinePlot() throws Exception {
        // Create first line
        LinePlot line1 = LinePlot.builder()
                .id("line1")
                .addPoint(0.0, 1.0)
                .addPoint(1.0, 2.0)
                .addPoint(2.0, 3.0)
                .addPoint(3.0, 4.0)
                .color("#FF0000")
                .lineStyle(LinePlot.LineStyle.SOLID)
                .label("Linear")
                .build();

        // Create second line
        LinePlot line2 = LinePlot.builder()
                .id("line2")
                .addPoint(0.0, 1.0)
                .addPoint(1.0, 1.5)
                .addPoint(2.0, 2.5)
                .addPoint(3.0, 4.0)
                .color("#0000FF")
                .lineStyle(LinePlot.LineStyle.DASHED)
                .label("Quadratic")
                .build();

        // Create axes
        Axis xAxis = Axis.builder()
                .id("xaxis")
                .axisType(Axis.AxisType.X_AXIS)
                .range(0.0, 3.0)
                .label("X")
                .build();

        Axis yAxis = Axis.builder()
                .id("yaxis")
                .axisType(Axis.AxisType.Y_AXIS)
                .range(0.0, 5.0)
                .label("Y")
                .build();

        // Create legend
        Legend legend = Legend.builder()
                .id("legend")
                .position(Legend.Position.TOP_LEFT)
                .addEntry("Linear", "#FF0000", LinePlot.LineStyle.SOLID)
                .addEntry("Quadratic", "#0000FF", LinePlot.LineStyle.DASHED)
                .build();

        // Create scene
        Scene scene = Scene.builder()
                .dimensions(800, 600)
                .title("Multiple Lines")
                .viewport(Viewport.of2D(0.0, 3.0, 0.0, 5.0))
                .addElement(xAxis)
                .addElement(yAxis)
                .addElement(line1)
                .addElement(line2)
                .addElement(legend)
                .build();

        // Render
        SvgRenderer renderer = new SvgRenderer();
        try (FileOutputStream output = new FileOutputStream("multi_line.svg")) {
            renderer.render(scene, output);
        }

        System.out.println("Plot saved to multi_line.svg");
    }

    /**
     * Main method to run examples.
     */
    public static void main(String[] args) {
        try {
            System.out.println("Creating plots...\n");

            System.out.println("1. Minimal plot:");
            createMinimalPlot();

            System.out.println("\n2. Complete plot with axes and legend:");
            createCompletePlot();

            System.out.println("\n3. Multi-line plot:");
            createMultiLinePlot();

            System.out.println("\n✅ All plots created successfully!");
            System.out.println("Open the SVG files in a browser to view them.");

        } catch (Exception e) {
            System.err.println("Error creating plots: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
