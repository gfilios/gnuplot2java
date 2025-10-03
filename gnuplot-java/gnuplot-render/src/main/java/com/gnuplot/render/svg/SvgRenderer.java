package com.gnuplot.render.svg;

import com.gnuplot.render.*;
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

    private Writer writer;
    private Scene scene;
    private Viewport viewport;

    @Override
    public void render(Scene scene, OutputStream output) throws IOException, RenderException {
        this.scene = scene;
        this.viewport = scene.getViewport();
        this.writer = new OutputStreamWriter(output, StandardCharsets.UTF_8);

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
                "width=\"%d\" height=\"%d\" viewBox=\"0 0 %d %d\">\n",
                scene.getWidth(), scene.getHeight(),
                scene.getWidth(), scene.getHeight()));

        // Define clip path for plot area (if viewport is set)
        if (viewport != null) {
            writer.write("  <defs>\n");
            writer.write(String.format(Locale.US,
                    "    <clipPath id=\"plotClip\">\n" +
                    "      <rect x=\"0\" y=\"0\" width=\"%d\" height=\"%d\"/>\n" +
                    "    </clipPath>\n",
                    scene.getWidth(), scene.getHeight()));
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
            int fontSize = scene.getHints().get(RenderingHints.Keys.FONT_SIZE).orElse(16);
            writer.write(String.format(Locale.US,
                    "  <text x=\"%d\" y=\"%d\" font-size=\"%d\" text-anchor=\"middle\">%s</text>\n",
                    scene.getWidth() / 2, fontSize + 5, fontSize, escapeXml(scene.getTitle())));
        }
    }

    private void writeElements() throws IOException {
        for (SceneElement element : scene.getElements()) {
            element.accept(this);
        }
    }

    private void writeSvgFooter() throws IOException {
        writer.write("</svg>\n");
    }

    @Override
    public void visitLinePlot(LinePlot linePlot) {
        try {
            if (linePlot.getPoints().isEmpty()) {
                return;
            }

            // Build polyline points string
            StringBuilder points = new StringBuilder();
            for (LinePlot.Point2D point : linePlot.getPoints()) {
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
            String clipAttr = (viewport != null) ? " clip-path=\"url(#plotClip)\"" : "";
            writer.write(String.format(Locale.US,
                    "  <polyline points=\"%s\" fill=\"none\" %s%s/>\n",
                    points, stroke.toSvgAttributes(), clipAttr));

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
            // For now, render a simple axis line
            if (axis.getAxisType() == Axis.AxisType.X_AXIS) {
                double y = mapY(0);
                writer.write(String.format(Locale.US,
                        "  <line x1=\"0\" y1=\"%.2f\" x2=\"%d\" y2=\"%.2f\" stroke=\"#000\" stroke-width=\"1\"/>\n",
                        y, scene.getWidth(), y));

                // Axis label
                if (axis.getLabel() != null) {
                    writer.write(String.format(Locale.US,
                            "  <text x=\"%d\" y=\"%.2f\" text-anchor=\"middle\" font-size=\"12\">%s</text>\n",
                            scene.getWidth() / 2, y + 25, escapeXml(axis.getLabel())));
                }
            } else if (axis.getAxisType() == Axis.AxisType.Y_AXIS) {
                double x = mapX(0);
                writer.write(String.format(Locale.US,
                        "  <line x1=\"%.2f\" y1=\"0\" x2=\"%.2f\" y2=\"%d\" stroke=\"#000\" stroke-width=\"1\"/>\n",
                        x, x, scene.getHeight()));

                // Axis label
                if (axis.getLabel() != null) {
                    writer.write(String.format(Locale.US,
                            "  <text x=\"%.2f\" y=\"%d\" text-anchor=\"middle\" font-size=\"12\" transform=\"rotate(-90 %.2f %d)\">%s</text>\n",
                            x - 30, scene.getHeight() / 2, x - 30, scene.getHeight() / 2, escapeXml(axis.getLabel())));
                }
            }

        } catch (IOException e) {
            throw new RuntimeException("Failed to render axis", e);
        }
    }

    @Override
    public void visitLegend(Legend legend) {
        try {
            // Calculate legend dimensions
            int columns = legend.getColumns();
            int rows = (int) Math.ceil((double) legend.getEntries().size() / columns);
            int columnWidth = 150; // Width per column
            int rowHeight = 25;    // Height per row
            int padding = 10;

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

                // Render symbol based on type
                switch (entry.getSymbolType()) {
                    case LINE:
                        renderLegendLine(entry, entryX, entryY, legend.getFontSize());
                        break;
                    case MARKER:
                        renderLegendMarker(entry, entryX, entryY, legend.getFontSize());
                        break;
                    case LINE_MARKER:
                        renderLegendLineAndMarker(entry, entryX, entryY, legend.getFontSize());
                        break;
                }

                // Draw label
                writer.write(String.format(Locale.US,
                        "  <text x=\"%d\" y=\"%d\" font-family=\"%s\" font-size=\"%d\" alignment-baseline=\"middle\">%s</text>\n",
                        entryX + 40, entryY + 3, legend.getFontFamily(), legend.getFontSize(),
                        escapeXml(entry.getLabel())));

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
        int margin = 10;
        return switch (position) {
            case TOP_LEFT -> new int[]{margin, margin};
            case TOP_RIGHT -> new int[]{scene.getWidth() - legendWidth - margin, margin};
            case BOTTOM_LEFT -> new int[]{margin, scene.getHeight() - legendHeight - margin};
            case BOTTOM_RIGHT -> new int[]{scene.getWidth() - legendWidth - margin, scene.getHeight() - legendHeight - margin};
            case TOP_CENTER -> new int[]{(scene.getWidth() - legendWidth) / 2, margin};
            case BOTTOM_CENTER -> new int[]{(scene.getWidth() - legendWidth) / 2, scene.getHeight() - legendHeight - margin};
            case LEFT_CENTER -> new int[]{margin, (scene.getHeight() - legendHeight) / 2};
            case RIGHT_CENTER -> new int[]{scene.getWidth() - legendWidth - margin, (scene.getHeight() - legendHeight) / 2};
            case CENTER -> new int[]{(scene.getWidth() - legendWidth) / 2, (scene.getHeight() - legendHeight) / 2};
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
        double sceneWidth = scene.getWidth();
        return ((dataX - viewport.getXMin()) / viewportWidth) * sceneWidth;
    }

    /**
     * Map data y-coordinate to SVG y-coordinate (inverted).
     */
    private double mapY(double dataY) {
        if (viewport == null) {
            return dataY;
        }
        double viewportHeight = viewport.getHeight();
        double sceneHeight = scene.getHeight();
        // Invert Y axis (SVG has origin at top-left)
        return sceneHeight - ((dataY - viewport.getYMin()) / viewportHeight) * sceneHeight;
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
