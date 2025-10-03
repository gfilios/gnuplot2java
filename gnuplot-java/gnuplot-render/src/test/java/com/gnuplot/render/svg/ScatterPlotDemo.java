package com.gnuplot.render.svg;

import com.gnuplot.render.RenderException;
import com.gnuplot.render.Scene;
import com.gnuplot.render.Viewport;
import com.gnuplot.render.color.Color;
import com.gnuplot.render.elements.ScatterPlot;
import com.gnuplot.render.style.MarkerStyle;
import com.gnuplot.render.style.PointStyle;

import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Demonstrates different point styles and marker options for scatter plots.
 */
public class ScatterPlotDemo {

    public static void main(String[] args) throws IOException, RenderException {
        // Create viewport
        Viewport viewport = Viewport.builder()
                .xRange(0.0, 10.0)
                .yRange(0.0, 10.0)
                .build();

        // Create scatter plots demonstrating all point styles
        ScatterPlot circles = ScatterPlot.builder()
                .id("circles")
                .label("Circles (filled)")
                .markerStyle(MarkerStyle.filled(5.0, Color.RED, PointStyle.CIRCLE))
                .addPoint(1, 9)
                .addPoint(2, 9)
                .addPoint(3, 9)
                .build();

        ScatterPlot squares = ScatterPlot.builder()
                .id("squares")
                .label("Squares (filled)")
                .markerStyle(MarkerStyle.filled(5.0, Color.BLUE, PointStyle.SQUARE))
                .addPoint(1, 8)
                .addPoint(2, 8)
                .addPoint(3, 8)
                .build();

        ScatterPlot trianglesUp = ScatterPlot.builder()
                .id("triangles_up")
                .label("Triangles Up")
                .markerStyle(MarkerStyle.filled(5.0, Color.GREEN, PointStyle.TRIANGLE_UP))
                .addPoint(1, 7)
                .addPoint(2, 7)
                .addPoint(3, 7)
                .build();

        ScatterPlot trianglesDown = ScatterPlot.builder()
                .id("triangles_down")
                .label("Triangles Down")
                .markerStyle(MarkerStyle.filled(5.0, Color.fromRGB24(0xFFA500), PointStyle.TRIANGLE_DOWN))
                .addPoint(1, 6)
                .addPoint(2, 6)
                .addPoint(3, 6)
                .build();

        ScatterPlot diamonds = ScatterPlot.builder()
                .id("diamonds")
                .label("Diamonds")
                .markerStyle(MarkerStyle.filled(5.0, Color.fromRGB24(0xFF00FF), PointStyle.DIAMOND))
                .addPoint(1, 5)
                .addPoint(2, 5)
                .addPoint(3, 5)
                .build();

        ScatterPlot plus = ScatterPlot.builder()
                .id("plus")
                .label("Plus Signs")
                .markerStyle(MarkerStyle.withDefaultFill(5.0, Color.fromRGB24(0x00FFFF), PointStyle.PLUS))
                .addPoint(1, 4)
                .addPoint(2, 4)
                .addPoint(3, 4)
                .build();

        ScatterPlot cross = ScatterPlot.builder()
                .id("cross")
                .label("Cross Signs")
                .markerStyle(MarkerStyle.withDefaultFill(5.0, Color.fromRGB24(0x8B4513), PointStyle.CROSS))
                .addPoint(1, 3)
                .addPoint(2, 3)
                .addPoint(3, 3)
                .build();

        ScatterPlot stars = ScatterPlot.builder()
                .id("stars")
                .label("Stars")
                .markerStyle(MarkerStyle.filled(5.0, Color.fromRGB24(0xFFD700), PointStyle.STAR))
                .addPoint(1, 2)
                .addPoint(2, 2)
                .addPoint(3, 2)
                .build();

        ScatterPlot hexagons = ScatterPlot.builder()
                .id("hexagons")
                .label("Hexagons")
                .markerStyle(MarkerStyle.filled(5.0, Color.fromRGB24(0x9370DB), PointStyle.HEXAGON))
                .addPoint(1, 1)
                .addPoint(2, 1)
                .addPoint(3, 1)
                .build();

        ScatterPlot pentagons = ScatterPlot.builder()
                .id("pentagons")
                .label("Pentagons")
                .markerStyle(MarkerStyle.filled(5.0, Color.fromRGB24(0x20B2AA), PointStyle.PENTAGON))
                .addPoint(1, 0.2)
                .addPoint(2, 0.2)
                .addPoint(3, 0.2)
                .build();

        // Variable sizing demonstration
        ScatterPlot.Builder variableSizeBuilder = ScatterPlot.builder()
                .id("variable_size")
                .label("Variable Size")
                .markerStyle(MarkerStyle.filled(3.0, Color.fromRGB24(0xFF1493), PointStyle.CIRCLE));

        for (double x = 5; x <= 9; x += 0.5) {
            double size = 2 + (x - 5) * 1.5; // Size from 2 to 8
            variableSizeBuilder.addPoint(x, 5, size, null);
        }
        ScatterPlot variableSize = variableSizeBuilder.build();

        // Filled vs unfilled demonstration
        ScatterPlot filled = ScatterPlot.builder()
                .id("filled")
                .label("Filled circles")
                .markerStyle(MarkerStyle.filled(4.0, Color.RED, PointStyle.CIRCLE))
                .addPoint(5, 8)
                .addPoint(6, 8)
                .addPoint(7, 8)
                .build();

        ScatterPlot unfilled = ScatterPlot.builder()
                .id("unfilled")
                .label("Unfilled circles")
                .markerStyle(MarkerStyle.unfilled(4.0, Color.RED, PointStyle.CIRCLE))
                .addPoint(5, 7)
                .addPoint(6, 7)
                .addPoint(7, 7)
                .build();

        // Create scene with all scatter plots
        Scene scene = Scene.builder()
                .dimensions(800, 600)
                .title("Scatter Plot Marker Styles Demonstration")
                .viewport(viewport)
                .addElement(circles)
                .addElement(squares)
                .addElement(trianglesUp)
                .addElement(trianglesDown)
                .addElement(diamonds)
                .addElement(plus)
                .addElement(cross)
                .addElement(stars)
                .addElement(hexagons)
                .addElement(pentagons)
                .addElement(variableSize)
                .addElement(filled)
                .addElement(unfilled)
                .build();

        // Render to SVG
        SvgRenderer renderer = new SvgRenderer();
        try (FileOutputStream out = new FileOutputStream("scatter-plot-demo.svg")) {
            renderer.render(scene, out);
        }

        System.out.println("Created scatter-plot-demo.svg with 10 marker styles + size variations");
    }
}
