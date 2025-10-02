package com.gnuplot.render.svg;

import com.gnuplot.render.RenderException;
import com.gnuplot.render.Scene;
import com.gnuplot.render.Viewport;
import com.gnuplot.render.elements.LinePlot;

import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Demonstrates different line styles in SVG rendering.
 */
public class LineStyleDemo {

    public static void main(String[] args) throws IOException, RenderException {
        // Create viewport
        Viewport viewport = Viewport.builder()
                .xRange(0.0, 10.0)
                .yRange(0.0, 8.0)
                .build();

        // Create line plots with different styles and widths
        LinePlot solidLine = LinePlot.builder()
                .id("solid")
                .label("Solid (width 2.0)")
                .lineStyle(LinePlot.LineStyle.SOLID)
                .lineWidth(2.0)
                .color("#FF0000")
                .addPoint(0, 7)
                .addPoint(10, 7)
                .build();

        LinePlot dashedLine = LinePlot.builder()
                .id("dashed")
                .label("Dashed (width 1.5)")
                .lineStyle(LinePlot.LineStyle.DASHED)
                .lineWidth(1.5)
                .color("#00FF00")
                .addPoint(0, 6)
                .addPoint(10, 6)
                .build();

        LinePlot dottedLine = LinePlot.builder()
                .id("dotted")
                .label("Dotted (width 1.0)")
                .lineStyle(LinePlot.LineStyle.DOTTED)
                .lineWidth(1.0)
                .color("#0000FF")
                .addPoint(0, 5)
                .addPoint(10, 5)
                .build();

        LinePlot dashDotLine = LinePlot.builder()
                .id("dashdot")
                .label("Dash-Dot (width 2.5)")
                .lineStyle(LinePlot.LineStyle.DASH_DOT)
                .lineWidth(2.5)
                .color("#FF00FF")
                .addPoint(0, 4)
                .addPoint(10, 4)
                .build();

        LinePlot thickSolid = LinePlot.builder()
                .id("thick")
                .label("Thick Solid (width 4.0)")
                .lineStyle(LinePlot.LineStyle.SOLID)
                .lineWidth(4.0)
                .color("#FFA500")
                .addPoint(0, 3)
                .addPoint(10, 3)
                .build();

        LinePlot thinDotted = LinePlot.builder()
                .id("thin")
                .label("Thin Dotted (width 0.5)")
                .lineStyle(LinePlot.LineStyle.DOTTED)
                .lineWidth(0.5)
                .color("#00FFFF")
                .addPoint(0, 2)
                .addPoint(10, 2)
                .build();

        // Sine wave with default width
        LinePlot.Builder sineBuilder = LinePlot.builder()
                .id("sine")
                .label("Sine Wave")
                .lineStyle(LinePlot.LineStyle.SOLID)
                .lineWidth(1.0)
                .color("#8B4513");

        for (double x = 0; x <= 10; x += 0.2) {
            double y = Math.sin(x) + 1;
            sineBuilder.addPoint(x, y);
        }

        LinePlot sineWave = sineBuilder.build();

        // Create scene with all elements
        Scene scene = Scene.builder()
                .dimensions(800, 600)
                .title("Line Style Demonstration")
                .viewport(viewport)
                .addElement(solidLine)
                .addElement(dashedLine)
                .addElement(dottedLine)
                .addElement(dashDotLine)
                .addElement(thickSolid)
                .addElement(thinDotted)
                .addElement(sineWave)
                .build();

        // Render to SVG
        SvgRenderer renderer = new SvgRenderer();
        try (FileOutputStream out = new FileOutputStream("line-styles-demo.svg")) {
            renderer.render(scene, out);
        }

        System.out.println("Created line-styles-demo.svg with 7 different line styles");
    }
}
