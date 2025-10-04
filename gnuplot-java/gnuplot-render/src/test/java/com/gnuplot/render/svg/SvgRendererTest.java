package com.gnuplot.render.svg;

import com.gnuplot.render.*;
import com.gnuplot.render.elements.Axis;
import com.gnuplot.render.elements.Legend;
import com.gnuplot.render.elements.LinePlot;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for SvgRenderer class.
 */
class SvgRendererTest {

    @Test
    void testRendererMetadata() {
        SvgRenderer renderer = new SvgRenderer();

        assertEquals("SVG", renderer.getFormatName());
        assertEquals("image/svg+xml", renderer.getMimeType());
        assertEquals("svg", renderer.getFileExtension());
    }

    @Test
    void testCapabilities() {
        SvgRenderer renderer = new SvgRenderer();
        RendererCapabilities caps = renderer.getCapabilities();

        assertTrue(caps.supportsTransparency());
        assertTrue(caps.supportsVectorGraphics());
        assertTrue(caps.supportsInteractivity());
        assertTrue(caps.supportsAnimation());
        assertFalse(caps.supports3D());
    }

    @Test
    void testMinimalScene() throws Exception {
        Scene scene = Scene.builder()
                .dimensions(800, 600)
                .build();

        SvgRenderer renderer = new SvgRenderer();
        ByteArrayOutputStream output = new ByteArrayOutputStream();

        renderer.render(scene, output);

        String svg = output.toString(StandardCharsets.UTF_8);
        assertTrue(svg.contains("<?xml version=\"1.0\""));
        assertTrue(svg.contains("<svg"));
        assertTrue(svg.contains("width=\"800\""));
        assertTrue(svg.contains("height=\"600\""));
        assertTrue(svg.contains("</svg>"));
    }

    @Test
    void testSceneWithTitle() throws Exception {
        Scene scene = Scene.builder()
                .dimensions(800, 600)
                .title("Test Plot")
                .build();

        SvgRenderer renderer = new SvgRenderer();
        ByteArrayOutputStream output = new ByteArrayOutputStream();

        renderer.render(scene, output);

        String svg = output.toString(StandardCharsets.UTF_8);
        assertTrue(svg.contains("Test Plot"));
        assertTrue(svg.contains("<text"));
    }

    @Test
    void testLinePlot() throws Exception {
        LinePlot linePlot = LinePlot.builder()
                .id("line1")
                .addPoint(0.0, 0.0)
                .addPoint(1.0, 1.0)
                .addPoint(2.0, 0.0)
                .color("#FF0000")
                .lineStyle(LinePlot.LineStyle.SOLID)
                .build();

        Scene scene = Scene.builder()
                .dimensions(800, 600)
                .viewport(Viewport.of2D(0.0, 2.0, 0.0, 1.0))
                .addElement(linePlot)
                .build();

        SvgRenderer renderer = new SvgRenderer();
        ByteArrayOutputStream output = new ByteArrayOutputStream();

        renderer.render(scene, output);

        String svg = output.toString(StandardCharsets.UTF_8);
        assertTrue(svg.contains("<polyline"));
        assertTrue(svg.contains("#FF0000"));
        assertTrue(svg.contains("points="));
    }

    @Test
    void testDashedLinePlot() throws Exception {
        LinePlot linePlot = LinePlot.builder()
                .id("line1")
                .addPoint(0.0, 0.0)
                .addPoint(1.0, 1.0)
                .lineStyle(LinePlot.LineStyle.DASHED)
                .build();

        Scene scene = Scene.builder()
                .dimensions(800, 600)
                .addElement(linePlot)
                .build();

        SvgRenderer renderer = new SvgRenderer();
        ByteArrayOutputStream output = new ByteArrayOutputStream();

        renderer.render(scene, output);

        String svg = output.toString(StandardCharsets.UTF_8);
        assertTrue(svg.contains("stroke-dasharray"));
    }

    @Test
    void testAxis() throws Exception {
        Axis xAxis = Axis.builder()
                .id("xaxis")
                .axisType(Axis.AxisType.X_AXIS)
                .range(0.0, 10.0)
                .label("X Axis")
                .build();

        Scene scene = Scene.builder()
                .dimensions(800, 600)
                .viewport(Viewport.of2D(0.0, 10.0, -5.0, 5.0))
                .addElement(xAxis)
                .build();

        SvgRenderer renderer = new SvgRenderer();
        ByteArrayOutputStream output = new ByteArrayOutputStream();

        renderer.render(scene, output);

        String svg = output.toString(StandardCharsets.UTF_8);
        assertTrue(svg.contains("<line"));
        assertTrue(svg.contains("X Axis"));
    }

    @Test
    void testYAxis() throws Exception {
        Axis yAxis = Axis.builder()
                .id("yaxis")
                .axisType(Axis.AxisType.Y_AXIS)
                .range(-5.0, 5.0)
                .label("Y Axis")
                .build();

        Scene scene = Scene.builder()
                .dimensions(800, 600)
                .viewport(Viewport.of2D(0.0, 10.0, -5.0, 5.0))
                .addElement(yAxis)
                .build();

        SvgRenderer renderer = new SvgRenderer();
        ByteArrayOutputStream output = new ByteArrayOutputStream();

        renderer.render(scene, output);

        String svg = output.toString(StandardCharsets.UTF_8);
        assertTrue(svg.contains("<line"));
        assertTrue(svg.contains("Y Axis"));
        assertTrue(svg.contains("rotate"));
    }

    @Test
    void testAxisWithTicksAndLabels() throws Exception {
        // Create X axis with ticks enabled
        Axis xAxis = Axis.builder()
                .id("xaxis")
                .axisType(Axis.AxisType.X_AXIS)
                .range(0.0, 10.0)
                .label("X Axis")
                .showTicks(true)
                .build();

        Scene scene = Scene.builder()
                .dimensions(800, 600)
                .viewport(Viewport.of2D(0.0, 10.0, -5.0, 5.0))
                .addElement(xAxis)
                .build();

        SvgRenderer renderer = new SvgRenderer();
        ByteArrayOutputStream output = new ByteArrayOutputStream();

        renderer.render(scene, output);

        String svg = output.toString(StandardCharsets.UTF_8);

        // Should contain axis line
        assertTrue(svg.contains("<line"), "Should render axis line");

        // Should contain tick marks (small lines perpendicular to axis)
        // Tick marks are rendered as small line segments
        assertTrue(svg.split("<line").length > 2, "Should render multiple tick mark lines");

        // Should contain tick labels (numbers at tick positions)
        assertTrue(svg.contains("<text"), "Should render tick labels");
        // Should have numeric labels like "0", "2", "4", "6", "8", "10"
        assertTrue(svg.contains(">0<") || svg.contains(">0."), "Should have label for 0");
        assertTrue(svg.contains(">10<") || svg.contains(">10."), "Should have label for 10");
    }

    @Test
    void testAxisWithGrid() throws Exception {
        // Create X axis with grid enabled
        Axis xAxis = Axis.builder()
                .id("xaxis")
                .axisType(Axis.AxisType.X_AXIS)
                .range(0.0, 10.0)
                .showGrid(true)
                .build();

        Axis yAxis = Axis.builder()
                .id("yaxis")
                .axisType(Axis.AxisType.Y_AXIS)
                .range(-5.0, 5.0)
                .showGrid(true)
                .build();

        Scene scene = Scene.builder()
                .dimensions(800, 600)
                .viewport(Viewport.of2D(0.0, 10.0, -5.0, 5.0))
                .addElement(xAxis)
                .addElement(yAxis)
                .build();

        SvgRenderer renderer = new SvgRenderer();
        ByteArrayOutputStream output = new ByteArrayOutputStream();

        renderer.render(scene, output);

        String svg = output.toString(StandardCharsets.UTF_8);

        // Should contain grid lines
        assertTrue(svg.contains("stroke-dasharray"), "Grid lines should be dashed");

        // Should have multiple lines (grid + axes)
        int lineCount = svg.split("<line").length - 1;
        assertTrue(lineCount > 5, "Should have multiple grid lines, found: " + lineCount);
    }

    @Test
    void testLegend() throws Exception {
        Legend legend = Legend.builder()
                .id("legend1")
                .position(Legend.Position.TOP_RIGHT)
                .addEntry("Series 1", "#FF0000", LinePlot.LineStyle.SOLID)
                .addEntry("Series 2", "#00FF00", LinePlot.LineStyle.DASHED)
                .showBorder(true)
                .build();

        Scene scene = Scene.builder()
                .dimensions(800, 600)
                .addElement(legend)
                .build();

        SvgRenderer renderer = new SvgRenderer();
        ByteArrayOutputStream output = new ByteArrayOutputStream();

        renderer.render(scene, output);

        String svg = output.toString(StandardCharsets.UTF_8);
        assertTrue(svg.contains("Series 1"));
        assertTrue(svg.contains("Series 2"));
        assertTrue(svg.contains("#FF0000"));
        assertTrue(svg.contains("#00FF00"));
        assertTrue(svg.contains("<rect")); // legend border
    }

    @Test
    void testLegendNoBorder() throws Exception {
        Legend legend = Legend.builder()
                .id("legend1")
                .position(Legend.Position.TOP_LEFT)
                .addEntry("Test", "#000000", LinePlot.LineStyle.SOLID)
                .showBorder(false)
                .build();

        Scene scene = Scene.builder()
                .dimensions(800, 600)
                .addElement(legend)
                .build();

        SvgRenderer renderer = new SvgRenderer();
        ByteArrayOutputStream output = new ByteArrayOutputStream();

        renderer.render(scene, output);

        String svg = output.toString(StandardCharsets.UTF_8);
        assertTrue(svg.contains("Test"));
        // Should not contain legend border rect, only background rect
        int rectCount = svg.split("<rect").length - 1;
        assertEquals(1, rectCount); // Only background rect
    }

    @Test
    void testBackgroundColor() throws Exception {
        RenderingHints hints = RenderingHints.builder()
                .backgroundColor("#F0F0F0")
                .build();

        Scene scene = Scene.builder()
                .dimensions(800, 600)
                .hints(hints)
                .build();

        SvgRenderer renderer = new SvgRenderer();
        ByteArrayOutputStream output = new ByteArrayOutputStream();

        renderer.render(scene, output);

        String svg = output.toString(StandardCharsets.UTF_8);
        assertTrue(svg.contains("#F0F0F0"));
    }

    @Test
    void testCompleteScene() throws Exception {
        // Build a complete scene with all elements
        LinePlot linePlot = LinePlot.builder()
                .id("sine")
                .addPoint(0.0, 0.0)
                .addPoint(1.0, 1.0)
                .addPoint(2.0, 0.0)
                .addPoint(3.0, -1.0)
                .addPoint(4.0, 0.0)
                .color("#0000FF")
                .lineStyle(LinePlot.LineStyle.SOLID)
                .label("sin(x)")
                .build();

        Axis xAxis = Axis.builder()
                .id("xaxis")
                .axisType(Axis.AxisType.X_AXIS)
                .range(0.0, 4.0)
                .label("X")
                .build();

        Axis yAxis = Axis.builder()
                .id("yaxis")
                .axisType(Axis.AxisType.Y_AXIS)
                .range(-1.0, 1.0)
                .label("Y")
                .build();

        Legend legend = Legend.builder()
                .id("legend")
                .position(Legend.Position.TOP_RIGHT)
                .addEntry("sin(x)", "#0000FF", LinePlot.LineStyle.SOLID)
                .build();

        Scene scene = Scene.builder()
                .dimensions(800, 600)
                .title("Sine Wave")
                .viewport(Viewport.of2D(0.0, 4.0, -1.0, 1.0))
                .addElement(xAxis)
                .addElement(yAxis)
                .addElement(linePlot)
                .addElement(legend)
                .build();

        SvgRenderer renderer = new SvgRenderer();
        ByteArrayOutputStream output = new ByteArrayOutputStream();

        renderer.render(scene, output);

        String svg = output.toString(StandardCharsets.UTF_8);

        // Verify all components are present
        assertTrue(svg.contains("Sine Wave"));
        assertTrue(svg.contains("sin(x)"));
        assertTrue(svg.contains("#0000FF"));
        assertTrue(svg.contains("<polyline"));
        assertTrue(svg.contains("<line")); // axes

        // Verify structure
        assertTrue(svg.startsWith("<?xml"));
        assertTrue(svg.endsWith("</svg>\n"));
    }

    @Test
    void testXmlEscaping() throws Exception {
        Scene scene = Scene.builder()
                .dimensions(800, 600)
                .title("Test <Plot> & \"Data\"")
                .build();

        SvgRenderer renderer = new SvgRenderer();
        ByteArrayOutputStream output = new ByteArrayOutputStream();

        renderer.render(scene, output);

        String svg = output.toString(StandardCharsets.UTF_8);
        assertTrue(svg.contains("&lt;Plot&gt;"));
        assertTrue(svg.contains("&amp;"));
        assertTrue(svg.contains("&quot;Data&quot;"));
    }

    @Test
    void testClipPathDefinedWithViewport() throws Exception {
        Viewport viewport = Viewport.builder()
                .xRange(0.0, 10.0)
                .yRange(0.0, 10.0)
                .build();

        LinePlot linePlot = LinePlot.builder()
                .id("line1")
                .lineStyle(LinePlot.LineStyle.SOLID)
                .lineWidth(1.0)
                .color("#FF0000")
                .addPoint(0, 0)
                .addPoint(10, 10)
                .build();

        Scene scene = Scene.builder()
                .dimensions(800, 600)
                .viewport(viewport)
                .addElement(linePlot)
                .build();

        SvgRenderer renderer = new SvgRenderer();
        ByteArrayOutputStream output = new ByteArrayOutputStream();

        renderer.render(scene, output);

        String svg = output.toString(StandardCharsets.UTF_8);
        // Should define clipPath
        assertTrue(svg.contains("<clipPath id=\"plotClip\">"));
        assertTrue(svg.contains("</clipPath>"));
        // Polyline should reference the clip path
        assertTrue(svg.contains("clip-path=\"url(#plotClip)\""));
    }

    @Test
    void testNoClipPathWithoutViewport() throws Exception {
        LinePlot linePlot = LinePlot.builder()
                .id("line1")
                .lineStyle(LinePlot.LineStyle.SOLID)
                .lineWidth(1.0)
                .color("#FF0000")
                .addPoint(0, 0)
                .addPoint(10, 10)
                .build();

        Scene scene = Scene.builder()
                .dimensions(800, 600)
                .addElement(linePlot)
                .build();

        SvgRenderer renderer = new SvgRenderer();
        ByteArrayOutputStream output = new ByteArrayOutputStream();

        renderer.render(scene, output);

        String svg = output.toString(StandardCharsets.UTF_8);
        // Should NOT define clipPath without viewport
        assertFalse(svg.contains("<clipPath"));
        assertFalse(svg.contains("clip-path=\"url(#plotClip)\""));
    }

    @Test
    void testClippingAppliedToAllPolylines() throws Exception {
        Viewport viewport = Viewport.builder()
                .xRange(0.0, 10.0)
                .yRange(0.0, 10.0)
                .build();

        LinePlot line1 = LinePlot.builder()
                .id("line1")
                .lineStyle(LinePlot.LineStyle.SOLID)
                .lineWidth(1.0)
                .color("#FF0000")
                .addPoint(0, 0)
                .addPoint(10, 10)
                .build();

        LinePlot line2 = LinePlot.builder()
                .id("line2")
                .lineStyle(LinePlot.LineStyle.DASHED)
                .lineWidth(2.0)
                .color("#00FF00")
                .addPoint(0, 10)
                .addPoint(10, 0)
                .build();

        Scene scene = Scene.builder()
                .dimensions(800, 600)
                .viewport(viewport)
                .addElement(line1)
                .addElement(line2)
                .build();

        SvgRenderer renderer = new SvgRenderer();
        ByteArrayOutputStream output = new ByteArrayOutputStream();

        renderer.render(scene, output);

        String svg = output.toString(StandardCharsets.UTF_8);
        // Count occurrences of clip-path attribute
        int clipPathCount = svg.split("clip-path=\"url\\(#plotClip\\)\"").length - 1;
        assertEquals(2, clipPathCount, "Both polylines should have clip-path attribute");
    }
}
