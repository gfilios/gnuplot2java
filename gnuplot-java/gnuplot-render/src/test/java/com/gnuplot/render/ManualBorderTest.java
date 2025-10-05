package com.gnuplot.render;

import com.gnuplot.render.elements.Axis;
import com.gnuplot.render.elements.LinePlot;
import com.gnuplot.render.svg.SvgRenderer;

import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Manual test to generate SVG files with and without borders.
 * Run this main method to generate SVG files for visual inspection.
 */
public class ManualBorderTest {

    public static void main(String[] args) throws Exception {
        System.out.println("Generating border comparison SVG files...");

        // Generate points for a sine wave
        List<LinePlot.Point2D> sinePoints = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            double x = -10 + i * 20.0 / 99;
            double y = Math.sin(x);
            sinePoints.add(new LinePlot.Point2D(x, y));
        }

        LinePlot sinePlot = LinePlot.builder()
                .id("sine-plot")
                .points(sinePoints)
                .color("#9400D3")
                .label("sin(x)")
                .build();

        Viewport viewport = Viewport.of2D(-10, 10, -1.2, 1.2);

        // Test 1: With border (default)
        Scene sceneWithBorder = Scene.builder()
                .dimensions(800, 600)
                .title("With Border (default)")
                .viewport(viewport)
                .addElement(createXAxis())
                .addElement(createYAxis())
                .addElement(sinePlot)
                .border(true)
                .build();

        String withBorderPath = "border_enabled.svg";
        renderScene(sceneWithBorder, withBorderPath);
        System.out.println("Generated: " + withBorderPath);

        // Test 2: Without border
        Scene sceneWithoutBorder = Scene.builder()
                .dimensions(800, 600)
                .title("Without Border (unset)")
                .viewport(viewport)
                .addElement(createXAxis())
                .addElement(createYAxis())
                .addElement(sinePlot)
                .border(false)
                .build();

        String withoutBorderPath = "border_disabled.svg";
        renderScene(sceneWithoutBorder, withoutBorderPath);
        System.out.println("Generated: " + withoutBorderPath);

        System.out.println("\nVisual Verification:");
        System.out.println("1. Open both files in a browser");
        System.out.println("2. Verify 'border_enabled.svg' has a black rectangle around the plot area");
        System.out.println("3. Verify 'border_disabled.svg' has NO rectangle around the plot area");
    }

    private static void renderScene(Scene scene, String outputPath) throws Exception {
        SvgRenderer renderer = new SvgRenderer();
        try (FileOutputStream out = new FileOutputStream(outputPath)) {
            renderer.render(scene, out);
        }
    }

    private static Axis createXAxis() {
        return Axis.builder()
                .id("xaxis")
                .axisType(Axis.AxisType.X_AXIS)
                .range(-10, 10)
                .showTicks(true)
                .label("x")
                .build();
    }

    private static Axis createYAxis() {
        return Axis.builder()
                .id("yaxis")
                .axisType(Axis.AxisType.Y_AXIS)
                .range(-1.2, 1.2)
                .showTicks(true)
                .label("y")
                .build();
    }
}
