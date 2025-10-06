package com.gnuplot.render.svg;

import com.gnuplot.render.*;
import com.gnuplot.render.axis.TickGenerator;
import com.gnuplot.render.color.Color;
import com.gnuplot.render.elements.Axis;
import com.gnuplot.render.elements.BarChart;
import com.gnuplot.render.elements.Legend;
import com.gnuplot.render.elements.LinePlot;
import com.gnuplot.render.elements.ScatterPlot;
import com.gnuplot.render.style.MarkerStyle;
import com.gnuplot.render.style.PointStyle;
import com.gnuplot.render.style.StrokeStyle;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

/**
 * SVG renderer implementation.
 * Renders scenes to Scalable Vector Graphics format.
 *
 * @since 1.0
 */
public class SvgRenderer implements Renderer, SceneElementVisitor {

    // Plot area margins (matching gnuplot C behavior)
    private static final int MARGIN_LEFT = 54;
    private static final int MARGIN_RIGHT = 25;
    private static final int MARGIN_TOP = 66;  // C gnuplot uses 66.01
    private static final int MARGIN_BOTTOM = 36;

    private Writer writer;
    private Scene scene;
    private Viewport viewport;

    // Plot bounds (actual drawing area after margins)
    private int plotLeft;
    private int plotRight;
    private int plotTop;
    private int plotBottom;

    @Override
    public void render(Scene scene, OutputStream output) throws IOException, RenderException {
        this.scene = scene;
        this.viewport = scene.getViewport();
        this.writer = new OutputStreamWriter(output, StandardCharsets.UTF_8);

        // Calculate plot bounds (scene dimensions minus margins)
        this.plotLeft = MARGIN_LEFT;
        this.plotRight = scene.getWidth() - MARGIN_RIGHT;
        this.plotTop = MARGIN_TOP;
        this.plotBottom = scene.getHeight() - MARGIN_BOTTOM;

        try {
            writeSvgHeader();
            writeElements();
            writeSvgFooter();
            writer.flush();
        } catch (IOException e) {
            throw new RenderException("Failed to render SVG", e);
        }
    }

    /**
     * Render a multi-plot layout to SVG.
     */
    public void render(MultiPlotLayout layout, OutputStream output) throws IOException, RenderException {
        this.writer = new OutputStreamWriter(output, StandardCharsets.UTF_8);

        try {
            // Write SVG header for the entire layout
            writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
            writer.write(String.format(Locale.US,
                    "<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"%d\" height=\"%d\" viewBox=\"0 0 %d %d\">\n",
                    layout.getWidth(), layout.getHeight(), layout.getWidth(), layout.getHeight()));

            // Background
            writer.write(String.format(Locale.US,
                    "  <rect width=\"%d\" height=\"%d\" fill=\"#FFFFFF\"/>\n",
                    layout.getWidth(), layout.getHeight()));

            // Title if present
            if (layout.getTitle() != null && !layout.getTitle().isEmpty()) {
                writer.write(String.format(Locale.US,
                        "  <text x=\"%d\" y=\"21\" font-size=\"18\" text-anchor=\"middle\" font-weight=\"bold\">%s</text>\n",
                        layout.getWidth() / 2, escapeXml(layout.getTitle())));
            }

            // Render each subplot
            if (layout.getMode() == MultiPlotLayout.LayoutMode.GRID) {
                renderGridLayout(layout);
            } else {
                renderCustomLayout(layout);
            }

            // Close SVG
            writer.write("</svg>\n");
            writer.flush();
        } catch (IOException e) {
            throw new RenderException("Failed to render multi-plot layout", e);
        }
    }

    private void renderGridLayout(MultiPlotLayout layout) throws IOException, RenderException {
        int rows = layout.getGridRows();
        int cols = layout.getGridCols();
        int plotWidth = layout.getWidth() / cols;
        int plotHeight = (layout.getHeight() - (layout.getTitle() != null ? 40 : 20)) / rows;

        for (MultiPlotLayout.SubPlot subplot : layout.getSubPlots()) {
            int x = subplot.getCol() * plotWidth;
            int y = (layout.getTitle() != null ? 40 : 20) + subplot.getRow() * plotHeight;

            // Create a group for this subplot with clipping
            writer.write(String.format(Locale.US,
                    "  <g transform=\"translate(%d,%d)\" clip-path=\"url(#clip-%d-%d)\">\n",
                    x, y, subplot.getRow(), subplot.getCol()));
            writer.write(String.format(Locale.US,
                    "    <clipPath id=\"clip-%d-%d\">\n" +
                    "      <rect x=\"0\" y=\"0\" width=\"%d\" height=\"%d\"/>\n" +
                    "    </clipPath>\n",
                    subplot.getRow(), subplot.getCol(), plotWidth, plotHeight));

            // Render the scene in this subplot
            renderSceneInGroup(subplot.getScene(), plotWidth, plotHeight);

            writer.write("  </g>\n");
        }
    }

    private void renderCustomLayout(MultiPlotLayout layout) throws IOException, RenderException {
        int index = 0;
        for (MultiPlotLayout.SubPlot subplot : layout.getSubPlots()) {
            int x = (int) (subplot.getX() * layout.getWidth());
            int y = (int) (subplot.getY() * layout.getHeight());
            int width = (int) (subplot.getWidthFraction() * layout.getWidth());
            int height = (int) (subplot.getHeightFraction() * layout.getHeight());

            // Create a group for this subplot with clipping
            writer.write(String.format(Locale.US,
                    "  <g transform=\"translate(%d,%d)\" clip-path=\"url(#clip-custom-%d)\">\n",
                    x, y, index));
            writer.write(String.format(Locale.US,
                    "    <clipPath id=\"clip-custom-%d\">\n" +
                    "      <rect x=\"0\" y=\"0\" width=\"%d\" height=\"%d\"/>\n" +
                    "    </clipPath>\n",
                    index, width, height));

            // Render the scene in this subplot
            renderSceneInGroup(subplot.getScene(), width, height);

            writer.write("  </g>\n");
            index++;
        }
    }

    private void renderSceneInGroup(Scene scene, int width, int height) throws IOException, RenderException {
        // Temporarily save state
        Scene oldScene = this.scene;
        Viewport oldViewport = this.viewport;

        // Set new scene context
        this.scene = scene;
        this.viewport = scene.getViewport();

        // Override scene dimensions for this subplot
        Scene adjustedScene = Scene.builder()
                .title(scene.getTitle())
                .dimensions(width, height)
                .viewport(scene.getViewport())
                .elements(scene.getElements())
                .build();
        this.scene = adjustedScene;

        // Write title if present
        if (scene.getTitle() != null && !scene.getTitle().isEmpty()) {
            writer.write(String.format(Locale.US,
                    "    <text x=\"%d\" y=\"15\" font-size=\"12\" text-anchor=\"middle\">%s</text>\n",
                    width / 2, escapeXml(scene.getTitle())));
        }

        // Render elements
        for (SceneElement element : scene.getElements()) {
            element.accept(this);
        }

        // Restore state
        this.scene = oldScene;
        this.viewport = oldViewport;
    }

    @Override
    public String getFormatName() {
        return "SVG";
    }

    @Override
    public String getMimeType() {
        return "image/svg+xml";
    }

    @Override
    public String getFileExtension() {
        return "svg";
    }

    @Override
    public RendererCapabilities getCapabilities() {
        return RendererCapabilities.builder()
                .transparency(true)
                .vectorGraphics(true)
                .interactivity(true)
                .supports3D(false)
                .animation(true)
                .build();
    }

    private void writeSvgHeader() throws IOException {
        writer.write(String.format(Locale.US,
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<svg xmlns=\"http://www.w3.org/2000/svg\" " +
                "xmlns:xlink=\"http://www.w3.org/1999/xlink\" " +
                "width=\"%d\" height=\"%d\" viewBox=\"0 0 %d %d\">\n",
                scene.getWidth(), scene.getHeight(),
                scene.getWidth(), scene.getHeight()));

        // Define clip path and point markers (if viewport is set)
        if (viewport != null) {
            writer.write("  <defs>\n");

            // Clip path for plot area (should clip to actual plot bounds, not entire canvas)
            int clipWidth = plotRight - plotLeft;
            int clipHeight = plotBottom - plotTop;
            writer.write(String.format(Locale.US,
                    "    <clipPath id=\"plotClip\">\n" +
                    "      <rect x=\"%d\" y=\"%d\" width=\"%d\" height=\"%d\"/>\n" +
                    "    </clipPath>\n",
                    plotLeft, plotTop, clipWidth, clipHeight));

            // Point marker definitions (matching C Gnuplot's gpPt0-gpPt14)
            writePointMarkerDefinitions();

            writer.write("  </defs>\n");
        }

        // Background
        String bgColor = scene.getHints().get(RenderingHints.Keys.BACKGROUND_COLOR)
                .orElse("#FFFFFF");
        writer.write(String.format(Locale.US,
                "  <rect width=\"%d\" height=\"%d\" fill=\"%s\"/>\n",
                scene.getWidth(), scene.getHeight(), bgColor));

        // Title if present
        if (scene.getTitle() != null && !scene.getTitle().isEmpty()) {
            int fontSize = scene.getHints().get(RenderingHints.Keys.FONT_SIZE).orElse(20); // gnuplot default: 20
            writer.write(String.format(Locale.US,
                    "  <text x=\"%d\" y=\"%d\" font-size=\"%d\" text-anchor=\"middle\">%s</text>\n",
                    scene.getWidth() / 2, fontSize + 5, fontSize, escapeXml(scene.getTitle())));
        }
    }

    private void writeElements() throws IOException {
        for (SceneElement element : scene.getElements()) {
            element.accept(this);
        }

        // Render plot border (after all other elements)
        if (scene.isShowBorder()) {
            renderPlotBorder();
        }
    }

    /**
     * Render the plot border rectangle around the plot area.
     * This matches C Gnuplot's plot_border() function behavior.
     */
    private void renderPlotBorder() throws IOException {
        // Draw a rectangle around the plot area (from upper-left, clockwise)
        // Using plotLeft, plotRight, plotTop, plotBottom boundaries
        writer.write(String.format(Locale.US,
                "  <path d=\"M %.2f %.2f L %.2f %.2f L %.2f %.2f L %.2f %.2f Z\" " +
                "stroke=\"#000000\" stroke-width=\"1.0\" fill=\"none\"/>\n",
                (double) plotLeft, (double) plotTop,     // upper-left
                (double) plotRight, (double) plotTop,    // upper-right
                (double) plotRight, (double) plotBottom, // lower-right
                (double) plotLeft, (double) plotBottom   // lower-left
        ));
    }

    private void writeSvgFooter() throws IOException {
        writer.write("</svg>\n");
    }

    /**
     * Write point marker definitions matching C Gnuplot's gpPt0-gpPt14.
     * These are referenced when rendering plots with POINTS or LINESPOINTS style.
     */
    private void writePointMarkerDefinitions() throws IOException {
        double strokeWidth = 0.222;

        writer.write("\n    <!-- Point marker definitions (matching C Gnuplot) -->\n");

        // gpDot - small filled circle
        writer.write("    <circle id='gpDot' r='0.5' stroke-width='0.5' stroke='currentColor'/>\n");

        // gpPt0 - plus (+)
        writer.write(String.format(Locale.US,
                "    <path id='gpPt0' stroke-width='%.3f' stroke='currentColor' d='M-1,0 h2 M0,-1 v2'/>\n",
                strokeWidth));

        // gpPt1 - X
        writer.write(String.format(Locale.US,
                "    <path id='gpPt1' stroke-width='%.3f' stroke='currentColor' d='M-1,-1 L1,1 M1,-1 L-1,1'/>\n",
                strokeWidth));

        // gpPt2 - star (*)
        writer.write(String.format(Locale.US,
                "    <path id='gpPt2' stroke-width='%.3f' stroke='currentColor' d='M-1,0 L1,0 M0,-1 L0,1 M-1,-1 L1,1 M-1,1 L1,-1'/>\n",
                strokeWidth));

        // gpPt3 - square (empty)
        writer.write(String.format(Locale.US,
                "    <rect id='gpPt3' stroke-width='%.3f' stroke='currentColor' x='-1' y='-1' width='2' height='2'/>\n",
                strokeWidth));

        // gpPt4 - square (filled)
        writer.write(String.format(Locale.US,
                "    <rect id='gpPt4' stroke-width='%.3f' stroke='currentColor' fill='currentColor' x='-1' y='-1' width='2' height='2'/>\n",
                strokeWidth));

        // gpPt5 - circle (empty)
        writer.write(String.format(Locale.US,
                "    <circle id='gpPt5' stroke-width='%.3f' stroke='currentColor' cx='0' cy='0' r='1'/>\n",
                strokeWidth));

        // gpPt6 - circle (filled)
        writer.write("    <use xlink:href='#gpPt5' id='gpPt6' fill='currentColor' stroke='none'/>\n");

        // gpPt7 - triangle (empty)
        writer.write(String.format(Locale.US,
                "    <path id='gpPt7' stroke-width='%.3f' stroke='currentColor' d='M0,-1.33 L-1.33,0.67 L1.33,0.67 z'/>\n",
                strokeWidth));

        // gpPt8 - triangle (filled)
        writer.write("    <use xlink:href='#gpPt7' id='gpPt8' fill='currentColor' stroke='none'/>\n");

        // gpPt9 - inverted triangle (empty)
        writer.write("    <use xlink:href='#gpPt7' id='gpPt9' stroke='currentColor' transform='rotate(180)'/>\n");

        // gpPt10 - inverted triangle (filled)
        writer.write("    <use xlink:href='#gpPt9' id='gpPt10' fill='currentColor' stroke='none'/>\n");

        // gpPt11 - diamond (empty)
        writer.write("    <use xlink:href='#gpPt3' id='gpPt11' stroke='currentColor' transform='rotate(45)'/>\n");

        // gpPt12 - diamond (filled)
        writer.write("    <use xlink:href='#gpPt11' id='gpPt12' fill='currentColor' stroke='none'/>\n");

        // gpPt13 - pentagon (empty)
        writer.write(String.format(Locale.US,
                "    <path id='gpPt13' stroke-width='%.3f' stroke='currentColor' d='M0,1.330 L1.265,0.411 L0.782,-1.067 L-0.782,-1.076 L-1.265,0.411 z'/>\n",
                strokeWidth));

        // gpPt14 - pentagon (filled)
        writer.write("    <use xlink:href='#gpPt13' id='gpPt14' fill='currentColor' stroke='none'/>\n");
    }

    @Override
    public void visitLinePlot(LinePlot linePlot) {
        try {
            if (linePlot.getPoints().isEmpty()) {
                return;
            }

            LinePlot.PlotStyle plotStyle = linePlot.getPlotStyle();
            String clipAttr = (viewport != null) ? " clip-path=\"url(#plotClip)\"" : "";

            // Render lines (for LINES or LINESPOINTS)
            if (plotStyle == LinePlot.PlotStyle.LINES || plotStyle == LinePlot.PlotStyle.LINESPOINTS) {
                // Build polyline points string, skipping NaN/Infinity values
                StringBuilder points = new StringBuilder();
                for (LinePlot.Point2D point : linePlot.getPoints()) {
                    // Skip invalid points (NaN or Infinity)
                    if (!Double.isFinite(point.getX()) || !Double.isFinite(point.getY())) {
                        continue;
                    }

                    double x = mapX(point.getX());
                    double y = mapY(point.getY());
                    if (points.length() > 0) {
                        points.append(" ");
                    }
                    points.append(String.format(Locale.US, "%.2f %.2f", x, y));
                }

                // Create stroke style from LinePlot properties
                Color color = Color.fromHexString(linePlot.getColor());
                com.gnuplot.render.style.LineStyle styleLineStyle = linePlot.getLineStyle().toStyleLineStyle();
                StrokeStyle stroke = new StrokeStyle(linePlot.getLineWidth(), color, styleLineStyle);

                // Write polyline with stroke attributes and clipping
                writer.write(String.format(Locale.US,
                        "  <polyline points=\"%s\" fill=\"none\" %s%s/>\n",
                        points, stroke.toSvgAttributes(), clipAttr));
            }

            // Render point markers (for POINTS or LINESPOINTS)
            if (plotStyle == LinePlot.PlotStyle.POINTS || plotStyle == LinePlot.PlotStyle.LINESPOINTS) {
                // Use markerStyle if provided, otherwise create default
                MarkerStyle markerStyle = linePlot.getMarkerStyle();
                if (markerStyle == null) {
                    // Create default marker style matching plot color
                    Color color = Color.fromHexString(linePlot.getColor());
                    markerStyle = MarkerStyle.filled(4.0, color, PointStyle.CIRCLE);
                }

                // Render each point as a marker using gpPt definitions
                int markerIndex = 0;
                for (LinePlot.Point2D point : linePlot.getPoints()) {
                    // Skip invalid points (NaN or Infinity)
                    if (!Double.isFinite(point.getX()) || !Double.isFinite(point.getY())) {
                        continue;
                    }

                    double x = mapX(point.getX());
                    double y = mapY(point.getY());

                    // Use gpPt marker definitions (cycling through 0-14)
                    int ptIndex = markerIndex % 15;
                    double scale = markerStyle.size() / 4.0; // Normalize to gnuplot's default size

                    // Write marker reference with transform and color
                    writer.write(String.format(Locale.US,
                            "  <use xlink:href='#gpPt%d' transform='translate(%.2f,%.2f) scale(%.2f)' color='%s'%s/>\n",
                            ptIndex, x, y, scale, linePlot.getColor(), clipAttr));

                    markerIndex++;
                }
            }

        } catch (IOException e) {
            throw new RuntimeException("Failed to render line plot", e);
        }
    }

    @Override
    public void visitScatterPlot(ScatterPlot scatterPlot) {
        try {
            if (scatterPlot.getPoints().isEmpty()) {
                return;
            }

            MarkerStyle style = scatterPlot.getMarkerStyle();
            String clipAttr = (viewport != null) ? " clip-path=\"url(#plotClip)\"" : "";

            for (ScatterPlot.DataPoint point : scatterPlot.getPoints()) {
                // Skip invalid points (NaN or Infinity)
                if (!Double.isFinite(point.getX()) || !Double.isFinite(point.getY())) {
                    continue;
                }

                double x = mapX(point.getX());
                double y = mapY(point.getY());

                // Use custom size/color if specified, otherwise use marker style defaults
                double size = point.hasCustomSize() ? point.getCustomSize() : style.size();
                String colorHex = point.hasCustomColor() ? point.getCustomColor() : style.getColorHex();

                // Render the marker based on point style
                String marker = renderMarker(x, y, size, colorHex, style.pointStyle(), style.filled());
                writer.write("  " + marker + clipAttr + "/>\n");
            }

        } catch (IOException e) {
            throw new RuntimeException("Failed to render scatter plot", e);
        }
    }

    /**
     * Renders a single marker at the specified position.
     */
    private String renderMarker(double x, double y, double size, String colorHex,
                                 PointStyle pointStyle, boolean filled) {
        return switch (pointStyle) {
            case CIRCLE -> renderCircle(x, y, size, colorHex, filled);
            case SQUARE -> renderSquare(x, y, size, colorHex, filled);
            case TRIANGLE_UP -> renderTriangle(x, y, size, colorHex, filled, false);
            case TRIANGLE_DOWN -> renderTriangle(x, y, size, colorHex, filled, true);
            case DIAMOND -> renderDiamond(x, y, size, colorHex, filled);
            case PLUS -> renderPlus(x, y, size, colorHex);
            case CROSS -> renderCross(x, y, size, colorHex);
            case STAR -> renderStar(x, y, size, colorHex, filled);
            case HEXAGON -> renderPolygon(x, y, size, 6, 0, colorHex, filled);
            case PENTAGON -> renderPolygon(x, y, size, 5, -90, colorHex, filled);
        };
    }

    private String renderCircle(double cx, double cy, double r, String color, boolean filled) {
        if (filled) {
            return String.format(Locale.US, "<circle cx=\"%.2f\" cy=\"%.2f\" r=\"%.2f\" fill=\"%s\"",
                    cx, cy, r, color);
        } else {
            return String.format(Locale.US, "<circle cx=\"%.2f\" cy=\"%.2f\" r=\"%.2f\" fill=\"none\" stroke=\"%s\" stroke-width=\"1.5\"",
                    cx, cy, r, color);
        }
    }

    private String renderSquare(double cx, double cy, double size, String color, boolean filled) {
        double x = cx - size;
        double y = cy - size;
        double width = size * 2;
        if (filled) {
            return String.format(Locale.US, "<rect x=\"%.2f\" y=\"%.2f\" width=\"%.2f\" height=\"%.2f\" fill=\"%s\"",
                    x, y, width, width, color);
        } else {
            return String.format(Locale.US, "<rect x=\"%.2f\" y=\"%.2f\" width=\"%.2f\" height=\"%.2f\" fill=\"none\" stroke=\"%s\" stroke-width=\"1.5\"",
                    x, y, width, width, color);
        }
    }

    private String renderTriangle(double cx, double cy, double size, String color, boolean filled, boolean inverted) {
        double height = size * 1.732; // sqrt(3) for equilateral triangle
        double halfBase = size;

        String points;
        if (inverted) {
            // Triangle pointing down
            points = String.format(Locale.US, "%.2f,%.2f %.2f,%.2f %.2f,%.2f",
                    cx, cy + height * 0.667,           // bottom
                    cx - halfBase, cy - height * 0.333, // top left
                    cx + halfBase, cy - height * 0.333  // top right
            );
        } else {
            // Triangle pointing up
            points = String.format(Locale.US, "%.2f,%.2f %.2f,%.2f %.2f,%.2f",
                    cx, cy - height * 0.667,           // top
                    cx - halfBase, cy + height * 0.333, // bottom left
                    cx + halfBase, cy + height * 0.333  // bottom right
            );
        }

        if (filled) {
            return String.format("<polygon points=\"%s\" fill=\"%s\"", points, color);
        } else {
            return String.format("<polygon points=\"%s\" fill=\"none\" stroke=\"%s\" stroke-width=\"1.5\"",
                    points, color);
        }
    }

    private String renderDiamond(double cx, double cy, double size, String color, boolean filled) {
        String points = String.format(Locale.US, "%.2f,%.2f %.2f,%.2f %.2f,%.2f %.2f,%.2f",
                cx, cy - size,      // top
                cx + size, cy,      // right
                cx, cy + size,      // bottom
                cx - size, cy       // left
        );

        if (filled) {
            return String.format("<polygon points=\"%s\" fill=\"%s\"", points, color);
        } else {
            return String.format("<polygon points=\"%s\" fill=\"none\" stroke=\"%s\" stroke-width=\"1.5\"",
                    points, color);
        }
    }

    private String renderPlus(double cx, double cy, double size, String color) {
        return String.format(Locale.US,
                "<path d=\"M %.2f %.2f L %.2f %.2f M %.2f %.2f L %.2f %.2f\" " +
                "stroke=\"%s\" stroke-width=\"2\" stroke-linecap=\"round\"",
                cx, cy - size, cx, cy + size,  // vertical line
                cx - size, cy, cx + size, cy,  // horizontal line
                color);
    }

    private String renderCross(double cx, double cy, double size, String color) {
        double offset = size * 0.707; // cos(45°)
        return String.format(Locale.US,
                "<path d=\"M %.2f %.2f L %.2f %.2f M %.2f %.2f L %.2f %.2f\" " +
                "stroke=\"%s\" stroke-width=\"2\" stroke-linecap=\"round\"",
                cx - offset, cy - offset, cx + offset, cy + offset,  // diagonal \
                cx - offset, cy + offset, cx + offset, cy - offset,  // diagonal /
                color);
    }

    private String renderStar(double cx, double cy, double size, String color, boolean filled) {
        // 5-pointed star
        StringBuilder points = new StringBuilder();
        for (int i = 0; i < 10; i++) {
            double angle = Math.toRadians(i * 36 - 90); // Start from top
            double r = (i % 2 == 0) ? size : size * 0.4; // Alternate between outer and inner radius
            double px = cx + r * Math.cos(angle);
            double py = cy + r * Math.sin(angle);
            if (i > 0) points.append(" ");
            points.append(String.format(Locale.US, "%.2f,%.2f", px, py));
        }

        if (filled) {
            return String.format("<polygon points=\"%s\" fill=\"%s\"", points, color);
        } else {
            return String.format("<polygon points=\"%s\" fill=\"none\" stroke=\"%s\" stroke-width=\"1.5\"",
                    points, color);
        }
    }

    private String renderPolygon(double cx, double cy, double size, int sides, double rotationDegrees,
                                   String color, boolean filled) {
        StringBuilder points = new StringBuilder();
        for (int i = 0; i < sides; i++) {
            double angle = Math.toRadians(i * 360.0 / sides + rotationDegrees);
            double px = cx + size * Math.cos(angle);
            double py = cy + size * Math.sin(angle);
            if (i > 0) points.append(" ");
            points.append(String.format(Locale.US, "%.2f,%.2f", px, py));
        }

        if (filled) {
            return String.format("<polygon points=\"%s\" fill=\"%s\"", points, color);
        } else {
            return String.format("<polygon points=\"%s\" fill=\"none\" stroke=\"%s\" stroke-width=\"1.5\"",
                    points, color);
        }
    }

    @Override
    public void visitAxis(Axis axis) {
        try {
            if (axis.getAxisType() == Axis.AxisType.X_AXIS) {
                renderXAxis(axis);
            } else if (axis.getAxisType() == Axis.AxisType.Y_AXIS) {
                renderYAxis(axis);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to render axis", e);
        }
    }

    private void renderXAxis(Axis axis) throws IOException {
        // X-axis is at the bottom of the plot area
        double y = plotBottom;

        // Render axis line (from plotLeft to plotRight)
        writer.write(String.format(Locale.US,
                "  <line x1=\"%d\" y1=\"%.2f\" x2=\"%d\" y2=\"%.2f\" stroke=\"#000\" stroke-width=\"1\"/>\n",
                plotLeft, y, plotRight, y));

        // Render ticks and labels if enabled
        if (axis.isShowTicks()) {
            var ticks = axis.generateTicks();
            for (var tick : ticks) {
                double x = mapX(tick.getPosition());

                // Tick mark on bottom border (pointing upward into the plot area, like C gnuplot)
                int tickLength = tick.getType() == TickGenerator.TickType.MINOR ? 3 : 6;
                writer.write(String.format(Locale.US,
                        "  <line x1=\"%.2f\" y1=\"%.2f\" x2=\"%.2f\" y2=\"%.2f\" stroke=\"#000\" stroke-width=\"1\"/>\n",
                        x, y, x, y - tickLength));

                // Mirror tick on top border (pointing downward into the box)
                double topY = plotTop;
                writer.write(String.format(Locale.US,
                        "  <line x1=\"%.2f\" y1=\"%.2f\" x2=\"%.2f\" y2=\"%.2f\" stroke=\"#000\" stroke-width=\"1\"/>\n",
                        x, topY, x, topY + tickLength));

                // Tick label (only for major ticks)
                if (tick.getType() == TickGenerator.TickType.MAJOR && tick.getLabel() != null) {
                    writer.write(String.format(Locale.US,
                            "  <text x=\"%.2f\" y=\"%.2f\" text-anchor=\"middle\" font-size=\"12\">%s</text>\n",
                            x, y + 18, escapeXml(tick.getLabel())));
                }

                // Grid line if enabled
                if (axis.isShowGrid() && tick.getType() == TickGenerator.TickType.MAJOR) {
                    writer.write(String.format(Locale.US,
                            "  <line x1=\"%.2f\" y1=\"0\" x2=\"%.2f\" y2=\"%d\" stroke=\"#ccc\" stroke-width=\"0.5\" stroke-dasharray=\"2,2\"/>\n",
                            x, x, scene.getHeight()));
                }
            }
        }

        // Axis label
        if (axis.getLabel() != null) {
            writer.write(String.format(Locale.US,
                    "  <text x=\"%d\" y=\"%.2f\" text-anchor=\"middle\" font-size=\"12\">%s</text>\n",
                    scene.getWidth() / 2, y + 35, escapeXml(axis.getLabel())));
        }
    }

    private void renderYAxis(Axis axis) throws IOException {
        // Y-axis is at the left of the plot area
        double x = plotLeft;

        // Render axis line (from plotTop to plotBottom)
        writer.write(String.format(Locale.US,
                "  <line x1=\"%.2f\" y1=\"%d\" x2=\"%.2f\" y2=\"%d\" stroke=\"#000\" stroke-width=\"1\"/>\n",
                x, plotTop, x, plotBottom));

        // Render ticks and labels if enabled
        if (axis.isShowTicks()) {
            var ticks = axis.generateTicks();
            for (var tick : ticks) {
                double y = mapY(tick.getPosition());

                // Tick mark on left border (pointing rightward into the plot area, like C gnuplot)
                int tickLength = tick.getType() == TickGenerator.TickType.MINOR ? 3 : 6;
                writer.write(String.format(Locale.US,
                        "  <line x1=\"%.2f\" y1=\"%.2f\" x2=\"%.2f\" y2=\"%.2f\" stroke=\"#000\" stroke-width=\"1\"/>\n",
                        x, y, x + tickLength, y));

                // Mirror tick on right border (pointing leftward into the box)
                double rightX = plotRight;
                writer.write(String.format(Locale.US,
                        "  <line x1=\"%.2f\" y1=\"%.2f\" x2=\"%.2f\" y2=\"%.2f\" stroke=\"#000\" stroke-width=\"1\"/>\n",
                        rightX, y, rightX - tickLength, y));

                // Tick label (only for major ticks)
                if (tick.getType() == TickGenerator.TickType.MAJOR && tick.getLabel() != null) {
                    writer.write(String.format(Locale.US,
                            "  <text x=\"%.2f\" y=\"%.2f\" text-anchor=\"end\" font-size=\"12\">%s</text>\n",
                            x - 10, y + 4, escapeXml(tick.getLabel())));
                }

                // Grid line if enabled
                if (axis.isShowGrid() && tick.getType() == TickGenerator.TickType.MAJOR) {
                    writer.write(String.format(Locale.US,
                            "  <line x1=\"0\" y1=\"%.2f\" x2=\"%d\" y2=\"%.2f\" stroke=\"#ccc\" stroke-width=\"0.5\" stroke-dasharray=\"2,2\"/>\n",
                            y, scene.getWidth(), y));
                }
            }
        }

        // Axis label
        if (axis.getLabel() != null) {
            writer.write(String.format(Locale.US,
                    "  <text x=\"%.2f\" y=\"%d\" text-anchor=\"middle\" font-size=\"12\" transform=\"rotate(-90 %.2f %d)\">%s</text>\n",
                    x - 40, scene.getHeight() / 2, x - 40, scene.getHeight() / 2, escapeXml(axis.getLabel())));
        }
    }

    @Override
    public void visitLegend(Legend legend) {
        try {
            // Calculate legend dimensions dynamically based on text width
            int columns = legend.getColumns();
            int rows = (int) Math.ceil((double) legend.getEntries().size() / columns);
            int rowHeight = 18;    // Height per row (C gnuplot uses 18px)
            int padding = 10;
            int symbolWidth = 45;  // Space for line/marker symbol after text

            // Calculate column width based on longest label
            // Use font size to estimate character width: ~0.6 * fontSize for Arial
            int maxLabelWidth = 0;
            for (Legend.LegendEntry entry : legend.getEntries()) {
                int estimatedWidth = (int) (entry.getLabel().length() * legend.getFontSize() * 0.6);
                maxLabelWidth = Math.max(maxLabelWidth, estimatedWidth);
            }

            int columnWidth = maxLabelWidth + symbolWidth;
            int legendWidth = columns * columnWidth + padding * 2;
            int legendHeight = rows * rowHeight + padding * 2;

            // Calculate legend position
            int[] pos = getLegendPosition(legend.getPosition(), legendWidth, legendHeight);
            int x = pos[0];
            int y = pos[1];

            // Draw legend box if border is enabled
            if (legend.isShowBorder()) {
                writer.write(String.format(Locale.US,
                        "  <rect x=\"%d\" y=\"%d\" width=\"%d\" height=\"%d\" fill=\"%s\" stroke=\"%s\" stroke-width=\"1\"/>\n",
                        x, y, legendWidth, legendHeight,
                        legend.getBackgroundColor(), legend.getBorderColor()));
            }

            // Draw legend entries in multi-column layout
            int entryIndex = 0;
            for (Legend.LegendEntry entry : legend.getEntries()) {
                int col = entryIndex % columns;
                int row = entryIndex / columns;

                int entryX = x + padding + col * columnWidth;
                int entryY = y + padding + row * rowHeight + rowHeight / 2;

                // Draw label FIRST (matching gnuplot C behavior: text before symbol)
                // Text is right-aligned, ending where symbol begins
                int textX = entryX + maxLabelWidth;
                writer.write(String.format(Locale.US,
                        "  <text x=\"%d\" y=\"%d\" font-family=\"%s\" font-size=\"%d\" alignment-baseline=\"middle\" text-anchor=\"end\">%s</text>\n",
                        textX, entryY + 3, legend.getFontFamily(), legend.getFontSize(),
                        escapeXml(entry.getLabel())));

                // Render symbol AFTER text (matching gnuplot C behavior)
                int symbolX = textX + 5; // Symbol starts 5px after text
                switch (entry.getSymbolType()) {
                    case LINE:
                        renderLegendLine(entry, symbolX, entryY, legend.getFontSize());
                        break;
                    case MARKER:
                        renderLegendMarker(entry, symbolX, entryY, legend.getFontSize());
                        break;
                    case LINE_MARKER:
                        renderLegendLineAndMarker(entry, symbolX, entryY, legend.getFontSize());
                        break;
                }

                entryIndex++;
            }

        } catch (IOException e) {
            throw new RuntimeException("Failed to render legend", e);
        }
    }

    private void renderLegendLine(Legend.LegendEntry entry, int x, int y, int fontSize) throws IOException {
        String dashArray = getStrokeDashArray(entry.getLineStyle());
        writer.write(String.format(Locale.US,
                "  <line x1=\"%d\" y1=\"%d\" x2=\"%d\" y2=\"%d\" stroke=\"%s\" stroke-width=\"2\"%s/>\n",
                x + 5, y, x + 30, y, entry.getColor(), dashArray));
    }

    private void renderLegendMarker(Legend.LegendEntry entry, int x, int y, int fontSize) throws IOException {
        MarkerStyle markerStyle = entry.getMarkerStyle();
        String markerSvg = renderMarker(x + 17, y, markerStyle.size(),
                entry.getColor(), markerStyle.pointStyle(), markerStyle.filled());
        writer.write("  ");
        writer.write(markerSvg);
        writer.write("/>\n");
    }

    private void renderLegendLineAndMarker(Legend.LegendEntry entry, int x, int y, int fontSize) throws IOException {
        // Draw line
        renderLegendLine(entry, x, y, fontSize);
        // Draw marker in center of line
        renderLegendMarker(entry, x, y, fontSize);
    }

    private int[] getLegendPosition(Legend.Position position, int legendWidth, int legendHeight) {
        int padding = 5; // Small padding inside plot area
        return switch (position) {
            case TOP_LEFT -> new int[]{plotLeft + padding, plotTop + padding};
            case TOP_RIGHT -> new int[]{plotRight - legendWidth - padding, plotTop + padding};
            case BOTTOM_LEFT -> new int[]{plotLeft + padding, plotBottom - legendHeight - padding};
            case BOTTOM_RIGHT -> new int[]{plotRight - legendWidth - padding, plotBottom - legendHeight - padding};
            case TOP_CENTER -> new int[]{(plotLeft + plotRight - legendWidth) / 2, plotTop + padding};
            // BOTTOM_CENTER (bmargin) should be BELOW the plot area, not inside it
            case BOTTOM_CENTER -> new int[]{(plotLeft + plotRight - legendWidth) / 2, plotBottom + padding};
            case LEFT_CENTER -> new int[]{plotLeft + padding, (plotTop + plotBottom - legendHeight) / 2};
            case RIGHT_CENTER -> new int[]{plotRight - legendWidth - padding, (plotTop + plotBottom - legendHeight) / 2};
            case CENTER -> new int[]{(plotLeft + plotRight - legendWidth) / 2, (plotTop + plotBottom - legendHeight) / 2};
        };
    }

    @Override
    public void visitBarChart(BarChart barChart) {
        try {
            if (barChart.getGroupingMode() == BarChart.GroupingMode.NONE) {
                // Render individual bars
                renderIndividualBars(barChart);
            } else if (barChart.getGroupingMode() == BarChart.GroupingMode.GROUPED) {
                // Render grouped bars side-by-side
                renderGroupedBars(barChart);
            } else if (barChart.getGroupingMode() == BarChart.GroupingMode.STACKED) {
                // Render stacked bars
                renderStackedBars(barChart);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to render bar chart", e);
        }
    }

    private void renderIndividualBars(BarChart barChart) throws IOException {
        for (BarChart.Bar bar : barChart.getBars()) {
            double mappedX = mapX(bar.getX());
            double mappedHeight = Math.abs(mapY(bar.getHeight()) - mapY(0));
            double mappedY = mapY(Math.max(0, bar.getHeight()));

            // Calculate bar width in screen coordinates
            double dataBarWidth = barChart.getBarWidth();
            double screenBarWidth = (viewport != null)
                ? (dataBarWidth / viewport.getWidth()) * scene.getWidth()
                : dataBarWidth;

            if (barChart.getOrientation() == BarChart.Orientation.VERTICAL) {
                // Vertical bars: x is position, height determines bar height
                double barX = mappedX - screenBarWidth / 2;
                writer.write(String.format(Locale.US,
                        "  <rect x=\"%.2f\" y=\"%.2f\" width=\"%.2f\" height=\"%.2f\" fill=\"%s\"/>\n",
                        barX, mappedY, screenBarWidth, mappedHeight, bar.getColor()));

                // Render error bars if present
                if (bar.hasErrorBars()) {
                    renderErrorBar(mappedX, bar.getHeight(), bar.getErrorLow(), bar.getErrorHigh(),
                                 screenBarWidth, barChart.getOrientation());
                }
            } else {
                // Horizontal bars: x is baseline (0), height determines bar length
                double barY = mappedX - screenBarWidth / 2;
                double barWidth = Math.abs(mapX(bar.getHeight()) - mapX(0));
                double barX = mapX(Math.min(0, bar.getHeight()));
                writer.write(String.format(Locale.US,
                        "  <rect x=\"%.2f\" y=\"%.2f\" width=\"%.2f\" height=\"%.2f\" fill=\"%s\"/>\n",
                        barX, barY, barWidth, screenBarWidth, bar.getColor()));

                // Render error bars if present
                if (bar.hasErrorBars()) {
                    renderErrorBar(mappedX, bar.getHeight(), bar.getErrorLow(), bar.getErrorHigh(),
                                 screenBarWidth, barChart.getOrientation());
                }
            }
        }
    }

    private void renderGroupedBars(BarChart barChart) throws IOException {
        String[] defaultColors = {"#4A90E2", "#50C878", "#FFD700", "#FF6B6B", "#9C27B0", "#FF9800", "#00BCD4"};

        for (BarChart.BarGroup group : barChart.getGroups()) {
            int barCount = group.getBarCount();
            double groupX = mapX(group.getX());

            // Calculate total width for the group
            double dataBarWidth = barChart.getBarWidth();
            double totalGroupWidth = (viewport != null)
                ? (dataBarWidth / viewport.getWidth()) * scene.getWidth()
                : dataBarWidth;

            // Calculate individual bar width and spacing
            double individualBarWidth = totalGroupWidth / barCount;
            double startX = groupX - totalGroupWidth / 2;

            for (int i = 0; i < barCount; i++) {
                double value = group.getValues().get(i);
                String color = (group.getColors() != null && i < group.getColors().size())
                    ? group.getColors().get(i)
                    : defaultColors[i % defaultColors.length];

                double mappedHeight = Math.abs(mapY(value) - mapY(0));
                double mappedY = mapY(Math.max(0, value));

                if (barChart.getOrientation() == BarChart.Orientation.VERTICAL) {
                    double barX = startX + i * individualBarWidth;
                    writer.write(String.format(Locale.US,
                            "  <rect x=\"%.2f\" y=\"%.2f\" width=\"%.2f\" height=\"%.2f\" fill=\"%s\"/>\n",
                            barX, mappedY, individualBarWidth, mappedHeight, color));
                } else {
                    double barY = startX + i * individualBarWidth;
                    double barWidth = Math.abs(mapX(value) - mapX(0));
                    double barX = mapX(Math.min(0, value));
                    writer.write(String.format(Locale.US,
                            "  <rect x=\"%.2f\" y=\"%.2f\" width=\"%.2f\" height=\"%.2f\" fill=\"%s\"/>\n",
                            barX, barY, barWidth, individualBarWidth, color));
                }
            }
        }
    }

    private void renderStackedBars(BarChart barChart) throws IOException {
        String[] defaultColors = {"#4A90E2", "#50C878", "#FFD700", "#FF6B6B", "#9C27B0", "#FF9800", "#00BCD4"};

        for (BarChart.BarGroup group : barChart.getGroups()) {
            double groupX = mapX(group.getX());
            double dataBarWidth = barChart.getBarWidth();
            double screenBarWidth = (viewport != null)
                ? (dataBarWidth / viewport.getWidth()) * scene.getWidth()
                : dataBarWidth;

            double cumulativeValue = 0;
            for (int i = 0; i < group.getBarCount(); i++) {
                double value = group.getValues().get(i);
                String color = (group.getColors() != null && i < group.getColors().size())
                    ? group.getColors().get(i)
                    : defaultColors[i % defaultColors.length];

                if (barChart.getOrientation() == BarChart.Orientation.VERTICAL) {
                    double barHeight = Math.abs(mapY(cumulativeValue + value) - mapY(cumulativeValue));
                    double barY = mapY(cumulativeValue + value);
                    double barX = groupX - screenBarWidth / 2;

                    writer.write(String.format(Locale.US,
                            "  <rect x=\"%.2f\" y=\"%.2f\" width=\"%.2f\" height=\"%.2f\" fill=\"%s\"/>\n",
                            barX, barY, screenBarWidth, barHeight, color));
                } else {
                    double barWidth = Math.abs(mapX(cumulativeValue + value) - mapX(cumulativeValue));
                    double barX = mapX(cumulativeValue);
                    double barY = groupX - screenBarWidth / 2;

                    writer.write(String.format(Locale.US,
                            "  <rect x=\"%.2f\" y=\"%.2f\" width=\"%.2f\" height=\"%.2f\" fill=\"%s\"/>\n",
                            barX, barY, barWidth, screenBarWidth, color));
                }

                cumulativeValue += value;
            }
        }
    }

    /**
     * Render error bar for a bar chart.
     */
    private void renderErrorBar(double centerX, double value, Double errorLow, Double errorHigh,
                               double barWidth, BarChart.Orientation orientation) throws IOException {
        if (orientation == BarChart.Orientation.VERTICAL) {
            // Vertical error bars
            double valueY = mapY(value);
            double capWidth = barWidth * 0.3; // Cap is 30% of bar width

            if (errorHigh != null) {
                double highY = mapY(value + errorHigh);
                // Vertical line
                writer.write(String.format(Locale.US,
                        "  <line x1=\"%.2f\" y1=\"%.2f\" x2=\"%.2f\" y2=\"%.2f\" stroke=\"black\" stroke-width=\"1.5\"/>\n",
                        centerX, valueY, centerX, highY));
                // Top cap
                writer.write(String.format(Locale.US,
                        "  <line x1=\"%.2f\" y1=\"%.2f\" x2=\"%.2f\" y2=\"%.2f\" stroke=\"black\" stroke-width=\"1.5\"/>\n",
                        centerX - capWidth / 2, highY, centerX + capWidth / 2, highY));
            }

            if (errorLow != null) {
                double lowY = mapY(value - errorLow);
                // Vertical line
                writer.write(String.format(Locale.US,
                        "  <line x1=\"%.2f\" y1=\"%.2f\" x2=\"%.2f\" y2=\"%.2f\" stroke=\"black\" stroke-width=\"1.5\"/>\n",
                        centerX, valueY, centerX, lowY));
                // Bottom cap
                writer.write(String.format(Locale.US,
                        "  <line x1=\"%.2f\" y1=\"%.2f\" x2=\"%.2f\" y2=\"%.2f\" stroke=\"black\" stroke-width=\"1.5\"/>\n",
                        centerX - capWidth / 2, lowY, centerX + capWidth / 2, lowY));
            }
        } else {
            // Horizontal error bars
            double valueX = mapX(value);
            double capHeight = barWidth * 0.3; // Cap is 30% of bar width

            if (errorHigh != null) {
                double highX = mapX(value + errorHigh);
                // Horizontal line
                writer.write(String.format(Locale.US,
                        "  <line x1=\"%.2f\" y1=\"%.2f\" x2=\"%.2f\" y2=\"%.2f\" stroke=\"black\" stroke-width=\"1.5\"/>\n",
                        valueX, centerX, highX, centerX));
                // Right cap
                writer.write(String.format(Locale.US,
                        "  <line x1=\"%.2f\" y1=\"%.2f\" x2=\"%.2f\" y2=\"%.2f\" stroke=\"black\" stroke-width=\"1.5\"/>\n",
                        highX, centerX - capHeight / 2, highX, centerX + capHeight / 2));
            }

            if (errorLow != null) {
                double lowX = mapX(value - errorLow);
                // Horizontal line
                writer.write(String.format(Locale.US,
                        "  <line x1=\"%.2f\" y1=\"%.2f\" x2=\"%.2f\" y2=\"%.2f\" stroke=\"black\" stroke-width=\"1.5\"/>\n",
                        valueX, centerX, lowX, centerX));
                // Left cap
                writer.write(String.format(Locale.US,
                        "  <line x1=\"%.2f\" y1=\"%.2f\" x2=\"%.2f\" y2=\"%.2f\" stroke=\"black\" stroke-width=\"1.5\"/>\n",
                        lowX, centerX - capHeight / 2, lowX, centerX + capHeight / 2));
            }
        }
    }

    /**
     * Map data x-coordinate to SVG x-coordinate.
     */
    private double mapX(double dataX) {
        if (viewport == null) {
            return dataX;
        }
        double viewportWidth = viewport.getWidth();
        double plotWidth = plotRight - plotLeft;
        return plotLeft + ((dataX - viewport.getXMin()) / viewportWidth) * plotWidth;
    }

    /**
     * Map data y-coordinate to SVG y-coordinate (inverted).
     */
    private double mapY(double dataY) {
        if (viewport == null) {
            return dataY;
        }
        double viewportHeight = viewport.getHeight();
        double plotHeight = plotBottom - plotTop;
        // Invert Y axis (SVG has origin at top-left)
        return plotBottom - ((dataY - viewport.getYMin()) / viewportHeight) * plotHeight;
    }

    /**
     * Get SVG stroke-dasharray for line style.
     */
    private String getStrokeDashArray(LinePlot.LineStyle style) {
        return switch (style) {
            case DASHED -> " stroke-dasharray=\"10,5\"";
            case DOTTED -> " stroke-dasharray=\"2,3\"";
            case DASH_DOT -> " stroke-dasharray=\"10,5,2,5\"";
            case NONE -> " stroke-width=\"0\"";
            default -> "";
        };
    }

    /**
     * Escape XML special characters.
     */
    private String escapeXml(String text) {
        return text.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&apos;");
    }
}
