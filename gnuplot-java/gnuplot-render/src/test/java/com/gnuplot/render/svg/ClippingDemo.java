package com.gnuplot.render.svg;

import com.gnuplot.render.RenderException;
import com.gnuplot.render.Scene;
import com.gnuplot.render.Viewport;
import com.gnuplot.render.elements.LinePlot;

import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Demonstrates viewport clipping for lines that extend beyond the plot area.
 */
public class ClippingDemo {

    public static void main(String[] args) throws IOException, RenderException {
        // Create viewport that only shows x:[0,10], y:[0,10]
        Viewport viewport = Viewport.builder()
                .xRange(0.0, 10.0)
                .yRange(0.0, 10.0)
                .build();

        // Line that extends beyond viewport on both sides
        LinePlot extendingLine = LinePlot.builder()
                .id("extending")
                .label("Extends beyond viewport (should be clipped)")
                .lineStyle(LinePlot.LineStyle.SOLID)
                .lineWidth(3.0)
                .color("#FF0000")
                .addPoint(-5, 5)   // Outside viewport (left)
                .addPoint(5, 5)    // Inside viewport
                .addPoint(15, 5)   // Outside viewport (right)
                .build();

        // Diagonal line from outside to outside
        LinePlot diagonalLine = LinePlot.builder()
                .id("diagonal")
                .label("Diagonal through viewport")
                .lineStyle(LinePlot.LineStyle.DASHED)
                .lineWidth(2.0)
                .color("#00FF00")
                .addPoint(-2, -2)  // Outside (bottom-left)
                .addPoint(12, 12)  // Outside (top-right)
                .build();

        // Completely outside viewport (should not be visible)
        LinePlot outsideLine = LinePlot.builder()
                .id("outside")
                .label("Completely outside")
                .lineStyle(LinePlot.LineStyle.DOTTED)
                .lineWidth(2.0)
                .color("#0000FF")
                .addPoint(15, 15)  // Outside
                .addPoint(20, 20)  // Outside
                .build();

        // Completely inside viewport (fully visible)
        LinePlot insideLine = LinePlot.builder()
                .id("inside")
                .label("Completely inside")
                .lineStyle(LinePlot.LineStyle.SOLID)
                .lineWidth(2.0)
                .color("#FF00FF")
                .addPoint(2, 2)
                .addPoint(8, 8)
                .build();

        // Sine wave that goes outside viewport bounds
        LinePlot.Builder sineBuilder = LinePlot.builder()
                .id("sine")
                .label("Sine wave (clipped at edges)")
                .lineStyle(LinePlot.LineStyle.SOLID)
                .lineWidth(1.5)
                .color("#FFA500");

        for (double x = -2; x <= 12; x += 0.2) {
            double y = 5 + 3 * Math.sin(x);
            sineBuilder.addPoint(x, y);
        }
        LinePlot sineWave = sineBuilder.build();

        // Create scene with clipping viewport
        Scene scene = Scene.builder()
                .dimensions(800, 600)
                .title("Viewport Clipping Demonstration")
                .viewport(viewport)
                .addElement(extendingLine)
                .addElement(diagonalLine)
                .addElement(outsideLine)
                .addElement(insideLine)
                .addElement(sineWave)
                .build();

        // Render to SVG
        SvgRenderer renderer = new SvgRenderer();
        try (FileOutputStream out = new FileOutputStream("clipping-demo.svg")) {
            renderer.render(scene, out);
        }

        System.out.println("Created clipping-demo.svg demonstrating viewport clipping");
        System.out.println("Expected behavior:");
        System.out.println("  - Red horizontal line: clipped at left and right edges");
        System.out.println("  - Green diagonal: clipped at viewport boundaries");
        System.out.println("  - Blue line: completely outside, not visible");
        System.out.println("  - Magenta diagonal: completely inside, fully visible");
        System.out.println("  - Orange sine: clipped where it extends beyond viewport");
    }
}
