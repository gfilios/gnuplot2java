package com.gnuplot.render.svg;

import com.gnuplot.render.*;
import com.gnuplot.render.axis.TickGenerator;
import com.gnuplot.render.color.Color;
import com.gnuplot.render.elements.Axis;
import com.gnuplot.render.elements.BarChart;
import com.gnuplot.render.elements.Legend;
import com.gnuplot.render.elements.LinePlot;
import com.gnuplot.render.elements.Point3D;
import com.gnuplot.render.elements.ScatterPlot;
import com.gnuplot.render.elements.SurfacePlot3D;
import com.gnuplot.render.projection.ViewTransform3D;
import com.gnuplot.render.style.MarkerStyle;
import com.gnuplot.render.style.PointStyle;
import com.gnuplot.render.style.StrokeStyle;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
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

        // Calculate plot bounds following C gnuplot's algorithm (graph3d.c:369-530)
        // C gnuplot for 800x600 with no custom margins:
        // xleft = h_char * 2 + h_tic ≈ 7*2 + 5 = 19
        // xright = xsize * xmax - h_char * 2 - h_tic ≈ 800 - 7*2 - 5 = 781
        // ybot = v_char * 2.5 + 1 ≈ 13*2.5 + 1 = 34
        // ytop = ysize * ymax - v_char * (titlelin + 1.5) - 1 ≈ 600 - 13*2.5 - 1 = 567

        boolean is3D = viewport != null && viewport.is3D();

        if (is3D) {
            // Match C gnuplot's 3D plot bounds calculation
            // Assuming h_char≈7, h_tic≈5, v_char≈13 for standard terminal
            int h_char = 7;
            int h_tic = 5;
            int v_char = 13;
            int titleLines = scene.getTitle() != null && !scene.getTitle().isEmpty() ? 1 : 0;

            this.plotLeft = h_char * 2 + h_tic;  // ≈ 19
            this.plotRight = scene.getWidth() - h_char * 2 - h_tic;  // ≈ 781
            this.plotTop = v_char * (titleLines + 2);  // ≈ 39 with title, 26 without
        } else {
            // 2D plots use simpler margins
            this.plotLeft = MARGIN_LEFT;
            this.plotRight = scene.getWidth() - MARGIN_RIGHT;
            this.plotTop = MARGIN_TOP;
        }

        // Check if there's a bottom margin legend that needs extra space
        // C gnuplot reduces plot area height when legend is in bottom margin
        int bottomMarginAdjustment = 0;
        for (var element : scene.getElements()) {
            if (element instanceof com.gnuplot.render.elements.Legend) {
                com.gnuplot.render.elements.Legend legend = (com.gnuplot.render.elements.Legend) element;
                if (legend.getPosition().name().startsWith("BMARGIN")) {
                    // Legend in bottom margin needs extra space
                    // Legend height ~18px per row + x-axis label space ~22px = ~40px total
                    int legendRows = (int) Math.ceil((double) legend.getEntries().size() / legend.getColumns());
                    int legendHeight = legendRows * 18;
                    bottomMarginAdjustment = legendHeight + 5; // legend height + small gap
                    break;
                }
            }
        }

        if (is3D) {
            // Match C gnuplot: ybot = v_char * 2.5 + 1
            int v_char = 13;
            int ybot = (int)(v_char * 2.5 + 1);  // ≈ 34
            this.plotBottom = scene.getHeight() - ybot - bottomMarginAdjustment;
        } else {
            this.plotBottom = scene.getHeight() - MARGIN_BOTTOM - bottomMarginAdjustment;
        }

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
        // Check if scene contains 3D plots and render axes first
        boolean has3DPlots = scene.getElements().stream()
                .anyMatch(e -> e instanceof SurfacePlot3D);

        if (has3DPlots) {
            render3DAxes();
        }

        for (SceneElement element : scene.getElements()) {
            element.accept(this);
        }

        // Render plot border (after all other elements)
        if (scene.isShowBorder()) {
            renderPlotBorder();
        }
    }

    /**
     * Render 3D coordinate axes for 3D plots.
     * Creates X, Y, Z axes with tick marks and labels.
     */
    private void render3DAxes() throws IOException {
        Viewport viewport = scene.getViewport();

        // Get axis ranges from viewport
        double xMin = viewport.getXMin();
        double xMax = viewport.getXMax();
        double yMin = viewport.getYMin();
        double yMax = viewport.getYMax();
        double zMin = viewport.getZMin();
        double zMax = viewport.getZMax();

        // Calculate actual data Z range (before ticslevel adjustment)
        // If zMin is negative (due to ticslevel), the data range starts at 0
        double zDataMin = (zMin < 0) ? 0 : zMin;

        // Determine which corners to use for each axis based on view rotation
        // This matches C gnuplot's setup_3d_box_corners() logic
        // Reference: gnuplot-c/src/graph3d.c:setup_3d_box_corners()
        double surfaceRotZ = 30.0; // TODO: read from script file

        // Setup corners based on rotation (matches C gnuplot algorithm)
        int quadrant = (int)(surfaceRotZ / 90);

        double zaxisX, zaxisY, rightX, rightY;

        // Determine X coordinates (from quadrant Z-rotation)
        if (((quadrant + 1) & 2) != 0) {
            zaxisX = 1;   // X_AXIS.max
            rightX = -1;  // X_AXIS.min
        } else {
            zaxisX = -1;  // X_AXIS.min
            rightX = 1;   // X_AXIS.max
        }

        // Determine Y coordinates (from quadrant Z-rotation)
        if ((quadrant & 2) != 0) {
            zaxisY = 1;   // Y_AXIS.max
            rightY = -1;  // Y_AXIS.min
        } else {
            zaxisY = -1;  // Y_AXIS.min
            rightY = 1;   // Y_AXIS.max
        }

        // Define 3D axis endpoints in data coordinates (not normalized)
        // Convert normalized [-1, 1] coordinates to actual data coordinates
        // Z-axis starts at (zaxisX, zaxisY) corner
        double originDataX = zaxisX < 0 ? xMin : xMax;
        double originDataY = zaxisY < 0 ? yMin : yMax;
        double xEndDataX = rightX < 0 ? xMin : xMax;
        double yEndDataY = rightY < 0 ? yMin : yMax;

        Point3D origin = new Point3D(originDataX, originDataY, zMin);
        Point3D xEnd = new Point3D(xEndDataX, originDataY, zMin);
        Point3D yEnd = new Point3D(originDataX, yEndDataY, zMin);
        Point3D zEnd = new Point3D(originDataX, originDataY, zMax);

        // Project to 2D using the same pipeline as points
        double[] originScreen = map3d_to_screen(origin.x(), origin.y(), origin.z(), viewport);
        double[] xEndScreen = map3d_to_screen(xEnd.x(), xEnd.y(), xEnd.z(), viewport);
        double[] yEndScreen = map3d_to_screen(yEnd.x(), yEnd.y(), yEnd.z(), viewport);
        double[] zEndScreen = map3d_to_screen(zEnd.x(), zEnd.y(), zEnd.z(), viewport);

        double ox = originScreen[0];
        double oy = originScreen[1];
        double xx = xEndScreen[0];
        double xy = xEndScreen[1];
        double yx = yEndScreen[0];
        double yy = yEndScreen[1];
        double zx = zEndScreen[0];
        double zy = zEndScreen[1];

        // Calculate axis directions for perpendicular tick marks
        double xDirX = xx - ox;
        double xDirY = xy - oy;
        double xLen = Math.sqrt(xDirX * xDirX + xDirY * xDirY);
        double xNormX = xDirX / xLen;
        double xNormY = xDirY / xLen;

        double yDirX = yx - ox;
        double yDirY = yy - oy;
        double yLen = Math.sqrt(yDirX * yDirX + yDirY * yDirY);
        double yNormX = yDirX / yLen;
        double yNormY = yDirY / yLen;

        double zDirX = zx - ox;
        double zDirY = zy - oy;
        double zLen = Math.sqrt(zDirX * zDirX + zDirY * zDirY);
        double zNormX = zDirX / zLen;
        double zNormY = zDirY / zLen;

        // Perpendicular directions (rotate 90° clockwise: (x,y) -> (y,-x))
        double xPerpX = xNormY;
        double xPerpY = -xNormX;
        double yPerpX = yNormY;
        double yPerpY = -yNormX;
        double zPerpX = zNormY;
        double zPerpY = -zNormX;

        // Draw complete 3D box edges (12 edges total)
        // A 3D box has 8 corners and 12 edges
        // We need to project all 8 corners and draw the edges

        // Define all 8 corners of the 3D box in data coordinates
        Point3D[] corners = new Point3D[8];
        corners[0] = new Point3D(xMin, yMin, zMin);  // origin
        corners[1] = new Point3D(xMax, yMin, zMin);   // x-end
        corners[2] = new Point3D(xMin, yMax, zMin);   // y-end
        corners[3] = new Point3D(xMax, yMax, zMin);    // x+y corner (bottom)
        corners[4] = new Point3D(xMin, yMin, zMax);   // z-end (from origin)
        corners[5] = new Point3D(xMax, yMin, zMax);    // x+z corner
        corners[6] = new Point3D(xMin, yMax, zMax);    // y+z corner
        corners[7] = new Point3D(xMax, yMax, zMax);     // top corner

        // Project all corners to 2D using the same pipeline as points
        double[][] screenCorners = new double[8][2];
        for (int i = 0; i < 8; i++) {
            double[] screen = map3d_to_screen(corners[i].x(), corners[i].y(), corners[i].z(), viewport);
            screenCorners[i][0] = screen[0];
            screenCorners[i][1] = screen[1];
        }

        writer.write(String.format(Locale.US,
                "  <g stroke=\"#000000\" stroke-width=\"1.0\" fill=\"none\">\n"));

        // Draw 5 edges: 3 base axes + 2 edges to close the bottom face
        // X-axis: from origin (corner 0) to x-end (corner 1)
        writer.write(String.format(Locale.US,
                "    <path d=\"M %.2f,%.2f L %.2f,%.2f\"/>\n",
                screenCorners[0][0], screenCorners[0][1], screenCorners[1][0], screenCorners[1][1]));

        // Y-axis: from origin (corner 0) to y-end (corner 2)
        writer.write(String.format(Locale.US,
                "    <path d=\"M %.2f,%.2f L %.2f,%.2f\"/>\n",
                screenCorners[0][0], screenCorners[0][1], screenCorners[2][0], screenCorners[2][1]));

        // Z-axis: from origin (corner 0) to z-end (corner 4)
        writer.write(String.format(Locale.US,
                "    <path d=\"M %.2f,%.2f L %.2f,%.2f\"/>\n",
                screenCorners[0][0], screenCorners[0][1], screenCorners[4][0], screenCorners[4][1]));

        // Additional edge 1: from x-end (corner 1) to x+y corner (corner 3) - closes right side
        writer.write(String.format(Locale.US,
                "    <path d=\"M %.2f,%.2f L %.2f,%.2f\"/>\n",
                screenCorners[1][0], screenCorners[1][1], screenCorners[3][0], screenCorners[3][1]));

        // Additional edge 2: from y-end (corner 2) to x+y corner (corner 3) - closes back side
        writer.write(String.format(Locale.US,
                "    <path d=\"M %.2f,%.2f L %.2f,%.2f\"/>\n",
                screenCorners[2][0], screenCorners[2][1], screenCorners[3][0], screenCorners[3][1]));

        writer.write("  </g>\n");

        // Add tick marks and labels (11 ticks per axis) perpendicular to each axis
        // C gnuplot uses 11 ticks from -1 to 1 in steps of 0.2
        double tickLength = 5.0;
        double labelOffset = 15.0; // Distance from tick end to label position
        int numTicks = 11;

        // ticslevel determines where Z=0 plane sits relative to XY base
        // With ticslevel=0.5, the visual Z range is [zMin - 0.5*(zMax-zMin), zMax]
        // Note: zMin from viewport is already the visual minimum (includes ticslevel offset)
        double ticslevel = 0.5;  // TODO: get from command parser

        // zDataMin already calculated above for base plane positioning
        double zDataMax = zMax;
        double zDataRange = zDataMax - zDataMin;

        for (int i = 0; i < numTicks; i++) {
            double t = i / (numTicks - 1.0); // Parameter along axis [0, 1]

            // Calculate actual data values
            double xValue = xMin + t * (xMax - xMin);
            double yValue = yMin + t * (yMax - yMin);

            // For Z-axis: ticks should be at data values [0, 0.1, 0.2, ..., 1.0]
            // positioned at their corresponding visual locations
            double zDataValue = zDataMin + t * zDataRange;

            // Visual Z position is same as data value (since data values are absolute)
            double zValueVisual = zDataValue;

            // For Z-axis: all ticks in data range should have labels
            boolean zTickBelowZero = false;

            // X-axis tick and label (only on front bottom edge)
            Point3D xTick = new Point3D(
                    origin.x() + t * (xEnd.x() - origin.x()),
                    origin.y() + t * (xEnd.y() - origin.y()),
                    origin.z() + t * (xEnd.z() - origin.z())
            );
            double[] xTickScreen = map3d_to_screen(xTick.x(), xTick.y(), xTick.z(), viewport);
            double xtx = xTickScreen[0];
            double xty = xTickScreen[1];

            writer.write(String.format(Locale.US,
                    "  <g stroke=\"#000000\" stroke-width=\"1.0\" fill=\"none\">\n"));
            writer.write(String.format(Locale.US,
                    "    <path d=\"M %.2f,%.2f L %.2f,%.2f\"/>\n",
                    xtx, xty, xtx + xPerpX * tickLength, xty + xPerpY * tickLength));
            writer.write("  </g>\n");

            // X-axis label
            double xLabelX = xtx + xPerpX * (tickLength + labelOffset);
            double xLabelY = xty + xPerpY * (tickLength + labelOffset);
            writer.write(String.format(Locale.US,
                    "  <g transform=\"translate(%.2f,%.2f)\" stroke=\"none\" fill=\"black\" font-family=\"Arial\" font-size=\"12.00\" text-anchor=\"middle\">\n",
                    xLabelX, xLabelY));
            writer.write(String.format(Locale.US,
                    "    <text><tspan font-family=\"Arial\">%.2g</tspan></text>\n",
                    xValue));
            writer.write("  </g>\n");

            // Y-axis tick and label (only on left bottom edge)
            Point3D yTick = new Point3D(
                    origin.x() + t * (yEnd.x() - origin.x()),
                    origin.y() + t * (yEnd.y() - origin.y()),
                    origin.z() + t * (yEnd.z() - origin.z())
            );
            double[] yTickScreen = map3d_to_screen(yTick.x(), yTick.y(), yTick.z(), viewport);
            double ytx = yTickScreen[0];
            double yty = yTickScreen[1];

            writer.write(String.format(Locale.US,
                    "  <g stroke=\"#000000\" stroke-width=\"1.0\" fill=\"none\">\n"));
            writer.write(String.format(Locale.US,
                    "    <path d=\"M %.2f,%.2f L %.2f,%.2f\"/>\n",
                    ytx, yty, ytx + yPerpX * tickLength, yty + yPerpY * tickLength));
            writer.write("  </g>\n");

            // Y-axis label
            double yLabelX = ytx + yPerpX * (tickLength + labelOffset);
            double yLabelY = yty + yPerpY * (tickLength + labelOffset);
            writer.write(String.format(Locale.US,
                    "  <g transform=\"translate(%.2f,%.2f)\" stroke=\"none\" fill=\"black\" font-family=\"Arial\" font-size=\"12.00\" text-anchor=\"middle\">\n",
                    yLabelX, yLabelY));
            writer.write(String.format(Locale.US,
                    "    <text><tspan font-family=\"Arial\">%.2g</tspan></text>\n",
                    yValue));
            writer.write("  </g>\n");

            // Z-axis tick and label (only on left front edge)
            // Map data Z value to parameter along visual Z-axis geometry
            double tZ = (zDataValue - zMin) / (zMax - zMin);

            Point3D zTick = new Point3D(
                    origin.x() + tZ * (zEnd.x() - origin.x()),
                    origin.y() + tZ * (zEnd.y() - origin.y()),
                    origin.z() + tZ * (zEnd.z() - origin.z())
            );
            double[] zTickScreen = map3d_to_screen(zTick.x(), zTick.y(), zTick.z(), viewport);
            double ztx = zTickScreen[0];
            double zty = zTickScreen[1];

            writer.write(String.format(Locale.US,
                    "  <g stroke=\"#000000\" stroke-width=\"1.0\" fill=\"none\">\n"));
            writer.write(String.format(Locale.US,
                    "    <path d=\"M %.2f,%.2f L %.2f,%.2f\"/>\n",
                    ztx, zty, ztx + zPerpX * tickLength, zty + zPerpY * tickLength));
            writer.write("  </g>\n");

            // Z-axis label (use text-anchor="end" to align right and prevent clipping)
            // Only render labels for ticks at or above Z=0 data plane (controlled by ticslevel)
            if (!zTickBelowZero) {
                double zLabelX = ztx + zPerpX * (tickLength + labelOffset);
                double zLabelY = zty + zPerpY * (tickLength + labelOffset);
                writer.write(String.format(Locale.US,
                        "  <g transform=\"translate(%.2f,%.2f)\" stroke=\"none\" fill=\"black\" font-family=\"Arial\" font-size=\"12.00\" text-anchor=\"end\">\n",
                        zLabelX, zLabelY));
                writer.write(String.format(Locale.US,
                        "    <text><tspan font-family=\"Arial\">%.2g</tspan></text>\n",
                        zValueVisual));
                writer.write("  </g>\n");
            }
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

            // Render impulses (vertical lines from baseline to each point)
            if (plotStyle == LinePlot.PlotStyle.IMPULSES) {
                // Find baseline y-coordinate (where y-axis crosses zero)
                // In C gnuplot, this is called xaxis_y (the y-coordinate of the x-axis)
                double baselineY = mapY(0.0);

                // Create stroke style from LinePlot properties
                Color color = Color.fromHexString(linePlot.getColor());
                com.gnuplot.render.style.LineStyle styleLineStyle = linePlot.getLineStyle().toStyleLineStyle();
                StrokeStyle stroke = new StrokeStyle(linePlot.getLineWidth(), color, styleLineStyle);

                // Draw vertical line from baseline to each point
                for (LinePlot.Point2D point : linePlot.getPoints()) {
                    // Skip invalid points (NaN or Infinity)
                    if (!Double.isFinite(point.getX()) || !Double.isFinite(point.getY())) {
                        continue;
                    }

                    double x = mapX(point.getX());
                    double y = mapY(point.getY());

                    // Draw vertical line from (x, baselineY) to (x, y)
                    writer.write(String.format(Locale.US,
                            "  <path d=\"M %.2f %.2f L %.2f %.2f\" %s%s/>\n",
                            x, baselineY, x, y, stroke.toSvgAttributes(), clipAttr));
                }
            }

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
                    // C gnuplot uses scale(4.50) for point markers, so size = 4.50 * 4.0 = 18.0
                    // Default point type is CROSS (gpPt1 - the X symbol)
                    Color color = Color.fromHexString(linePlot.getColor());
                    markerStyle = MarkerStyle.unfilled(18.0, color, PointStyle.CROSS);
                }

                // Map PointStyle to gpPt index (matching C gnuplot point types)
                int ptIndex = mapPointStyleToGpPt(markerStyle.pointStyle());
                // C gnuplot uses scale(4.50) for default point markers
                double scale = markerStyle.size() / 4.0; // Normalize: 18.0/4.0 = 4.50

                // Get the path geometry for this point type
                String pathData = getGpPtPathData(ptIndex);

                // Set stroke-width to be clearly visible
                // In modern SVG, stroke-width is NOT scaled by transform
                // So we use an absolute value (2.0 pixels)
                double strokeWidth = 2.0;

                // IMPORTANT: clip-path doesn't work on <path> elements with transform attribute
                // We need to wrap the paths in a <g> element with the clip-path
                writer.write(String.format("<%s>\n", clipAttr.isEmpty() ? "g" : "g" + clipAttr));

                // Render each point directly
                for (LinePlot.Point2D point : linePlot.getPoints()) {
                    // Skip invalid points (NaN or Infinity)
                    if (!Double.isFinite(point.getX()) || !Double.isFinite(point.getY())) {
                        continue;
                    }

                    double x = mapX(point.getX());
                    double y = mapY(point.getY());

                    // Render path inside the <g> wrapper (clip-path is on the parent <g>)
                    writer.write(String.format(Locale.US,
                            "  <path d=\"%s\" transform=\"translate(%.2f,%.2f) scale(%.2f)\" " +
                            "stroke=\"%s\" stroke-width=\"%.3f\" fill=\"none\"/>\n",
                            pathData, x, y, scale, linePlot.getColor(), strokeWidth));
                }

                writer.write("</g>\n");
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
            int padding = 0;       // C gnuplot uses no padding (height = rows * 18)
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
        int padding = 5; // Small padding
        return switch (position) {
            // Inside plot area
            case TOP_LEFT -> new int[]{plotLeft + padding, plotTop + padding};
            case TOP_RIGHT -> new int[]{plotRight - legendWidth - padding, plotTop + padding};
            case BOTTOM_LEFT -> new int[]{plotLeft + padding, plotBottom - legendHeight - padding};
            case BOTTOM_RIGHT -> new int[]{plotRight - legendWidth - padding, plotBottom - legendHeight - padding};
            case TOP_CENTER -> new int[]{(plotLeft + plotRight - legendWidth) / 2, plotTop + padding};
            case BOTTOM_CENTER -> new int[]{(plotLeft + plotRight - legendWidth) / 2, plotBottom - legendHeight - padding};
            case LEFT_CENTER -> new int[]{plotLeft + padding, (plotTop + plotBottom - legendHeight) / 2};
            case RIGHT_CENTER -> new int[]{plotRight - legendWidth - padding, (plotTop + plotBottom - legendHeight) / 2};
            case CENTER -> new int[]{(plotLeft + plotRight - legendWidth) / 2, (plotTop + plotBottom - legendHeight) / 2};
            // Outside plot area (margins) - below/above the plot box
            // X-axis labels are at plotBottom + 18, need to position legend below them
            // Label height ~4px + gap ~5px = 27px total offset from plotBottom
            case BMARGIN_LEFT -> new int[]{plotLeft, plotBottom + 27};
            case BMARGIN_CENTER -> new int[]{(plotLeft + plotRight - legendWidth) / 2, plotBottom + 27};
            case BMARGIN_RIGHT -> new int[]{plotRight - legendWidth, plotBottom + 27};
            case TMARGIN_LEFT -> new int[]{plotLeft, plotTop - legendHeight - padding};
            case TMARGIN_CENTER -> new int[]{(plotLeft + plotRight - legendWidth) / 2, plotTop - legendHeight - padding};
            case TMARGIN_RIGHT -> new int[]{plotRight - legendWidth, plotTop - legendHeight - padding};
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
     * Maps PointStyle enum to gnuplot point type index (gpPt0-gpPt14).
     * This matches C gnuplot's point type definitions in the SVG terminal.
     *
     * @param pointStyle The point style to map
     * @return gpPt index (0-14)
     */
    private int mapPointStyleToGpPt(PointStyle pointStyle) {
        return switch (pointStyle) {
            case PLUS -> 0;           // gpPt0: plus (+)
            case CROSS -> 1;          // gpPt1: cross (×) - DEFAULT
            case CIRCLE -> 5;         // gpPt5: circle (unfilled)
            case SQUARE -> 3;         // gpPt3: square (unfilled)
            case DIAMOND -> 11;       // gpPt11: diamond (rotated square)
            case TRIANGLE_UP -> 7;    // gpPt7: triangle up
            case TRIANGLE_DOWN -> 9;  // gpPt9: triangle down
            case STAR -> 2;           // gpPt2: asterisk (*)
            case HEXAGON -> 13;       // gpPt13: pentagon (closest to hexagon)
            case PENTAGON -> 13;      // gpPt13: pentagon
        };
    }

    /**
     * Get the SVG path data for a gnuplot point marker type.
     * These match the gpPt definitions from C gnuplot.
     */
    private String getGpPtPathData(int ptIndex) {
        return switch (ptIndex) {
            case 0 -> "M-1,0 h2 M0,-1 v2";                                      // plus (+)
            case 1 -> "M-1,-1 L1,1 M1,-1 L-1,1";                                // cross (×)
            case 2 -> "M-1,0 L1,0 M0,-1 L0,1 M-1,-1 L1,1 M-1,1 L1,-1";         // asterisk (*)
            case 3 -> "M-1,-1 L1,-1 L1,1 L-1,1 Z";                              // square
            case 5 -> "M0,-1 A1,1 0 1,1 0,1 A1,1 0 1,1 0,-1";                   // circle
            case 7 -> "M0,-1.33 L-1.33,0.67 L1.33,0.67 Z";                      // triangle up
            case 9 -> "M0,1.33 L1.33,-0.67 L-1.33,-0.67 Z";                     // triangle down
            case 11 -> "M0,-1.414 L1.414,0 L0,1.414 L-1.414,0 Z";               // diamond (rotated square)
            case 13 -> "M0,1.330 L1.265,0.411 L0.782,-1.067 L-0.782,-1.076 L-1.265,0.411 Z"; // pentagon
            default -> "M-1,-1 L1,1 M1,-1 L-1,1";                               // default to cross
        };
    }

    @Override
    public void visitSurfacePlot3D(SurfacePlot3D surfacePlot) {
        try {
            // Get view transformation from scene (default to gnuplot standard view)
            ViewTransform3D viewTransform = ViewTransform3D.gnuplotDefault();

            // TODO: Extract view parameters from scene settings when implemented
            // For now, use default view (60, 30 degrees)

            String clipAttr = " clip-path=\"url(#plotClip)\"";

            // Render based on plot style
            if (surfacePlot.getPlotStyle() == SurfacePlot3D.PlotStyle3D.POINTS) {
                renderPointCloud3D(surfacePlot, viewTransform, clipAttr);
            } else if (surfacePlot.getPlotStyle() == SurfacePlot3D.PlotStyle3D.LINES) {
                renderWireframe3D(surfacePlot, viewTransform, clipAttr);
            } else {
                // Default to points
                renderPointCloud3D(surfacePlot, viewTransform, clipAttr);
            }

        } catch (IOException e) {
            throw new RuntimeException("Failed to render 3D surface plot", e);
        }
    }

    /**
     * NEW IMPLEMENTATION - Direct 1:1 translation from C gnuplot
     * Based on graph3d.c and util3d.c
     */
    private void renderPointCloud3D(SurfacePlot3D surfacePlot, ViewTransform3D viewTransform, String clipAttr) throws IOException {
        // Get marker style or use default
        MarkerStyle markerStyle = surfacePlot.getMarkerStyle();
        if (markerStyle == null) {
            Color color = Color.fromHexString(surfacePlot.getColor());
            markerStyle = MarkerStyle.unfilled(18.0, color, PointStyle.CROSS);
        }

        int ptIndex = mapPointStyleToGpPt(markerStyle.pointStyle());
        double scale = markerStyle.size() / 4.0;

        // Get data ranges from viewport
        Viewport viewport = scene.getViewport();

        // Project all 3D points to 2D using C gnuplot algorithm
        writer.write(String.format("<%s>\n", clipAttr.isEmpty() ? "g" : "g" + clipAttr));

        for (Point3D point3D : surfacePlot.getPoints()) {
            if (!point3D.isFinite()) {
                continue;
            }

            // Call NEW map3d_to_screen method that follows C code exactly
            double[] screenCoords = map3d_to_screen(point3D.x(), point3D.y(), point3D.z(), viewport);
            double x = screenCoords[0];
            double y = screenCoords[1];

            // Render point marker using <use> reference (like C gnuplot)
            writer.write(String.format(Locale.US,
                    "  <use xlink:href='#gpPt%d' transform='translate(%.2f,%.2f) scale(%.2f)' color='%s'/>\n",
                    ptIndex, x, y, scale, surfacePlot.getColor()));
        }

        writer.write("</g>\n");
    }

    /**
     * Direct translation of C gnuplot's 3D to 2D projection pipeline.
     * Follows these C functions step-by-step:
     * 1. map_x3d(), map_y3d(), map_z3d() - util3d.c:1025-1065
     * 2. map3d_xyz() - util3d.c:844-875 (transformation matrix multiplication)
     * 3. TERMCOORD macro - util3d.h:64-72 (screen coordinate mapping)
     *
     * @param x data x coordinate
     * @param y data y coordinate
     * @param z data z coordinate
     * @param viewport axis ranges
     * @return [screenX, screenY]
     */
    private double[] map3d_to_screen(double x, double y, double z, Viewport viewport) {
        // STEP 1: Extract axis ranges (graph3d.c:840-843)
        double X_AXIS_min = viewport.getXMin();
        double X_AXIS_max = viewport.getXMax();
        double Y_AXIS_min = viewport.getYMin();
        double Y_AXIS_max = viewport.getYMax();
        double floor_z = viewport.getZMin();  // This already includes ticslevel
        double ceiling_z = viewport.getZMax();

        // STEP 2: Calculate scale factors (graph3d.c:841-843)
        // C: xscale3d = 2.0 / (X_AXIS.max - X_AXIS.min);
        double surface_zscale = 1.0;  // Default value from graph3d.c:107
        double xscale3d = 2.0 / (X_AXIS_max - X_AXIS_min);
        double yscale3d = 2.0 / (Y_AXIS_max - Y_AXIS_min);
        double zscale3d = 2.0 / (ceiling_z - floor_z) * surface_zscale;

        // STEP 3: Calculate center offsets (graph3d.c:853-872)
        // C: xcenter3d = ycenter3d = zcenter3d = 0.0;
        // C: zcenter3d = -(ceiling_z - floor_z) / 2.0 * zscale3d + 1;
        double xcenter3d = 0.0;
        double ycenter3d = 0.0;
        double zcenter3d = -(ceiling_z - floor_z) / 2.0 * zscale3d + 1.0;

        // STEP 4: Normalize to [-1, 1] (util3d.c:1025-1065)
        // C: map_x3d: return ((x - xaxis->min)*xscale3d + xcenter3d - 1.0);
        // C: map_y3d: return ((y - yaxis->min)*yscale3d + ycenter3d - 1.0);
        // C: map_z3d: return ((z - floor_z1)*zscale3d + zcenter3d - 1.0);
        double V0 = (x - X_AXIS_min) * xscale3d + xcenter3d - 1.0;
        double V1 = (y - Y_AXIS_min) * yscale3d + ycenter3d - 1.0;
        double V2 = (z - floor_z) * zscale3d + zcenter3d - 1.0;
        // V3 = 1.0 (homogeneous coordinate, implicit in calculation)

        // STEP 5: Get transformation matrix (graph3d.c:742-750)
        // C: mat_rot_z(surface_rot_z, trans_mat);  // surface_rot_z = 30
        // C: mat_rot_x(surface_rot_x, mat);         // surface_rot_x = 60
        // C: mat_mult(trans_mat, trans_mat, mat);
        // C: mat_scale(surface_scale / 2.0, surface_scale / 2.0, surface_scale / 2.0, mat);
        // C: mat_mult(trans_mat, trans_mat, mat);

        double surface_rot_z = 30.0;  // Default from "set view 60,30"
        double surface_rot_x = 60.0;
        double surface_scale = 1.0;   // Default from graph3d.c:106

        double[][] trans_mat = compute_transformation_matrix(surface_rot_x, surface_rot_z, surface_scale);

        // STEP 6: Apply transformation matrix (util3d.c:858-866)
        // C: Res[] = V[] * trans_mat[][] (uses row-vectors)
        // C: for (i = 0; i < 4; i++) {
        // C:     Res[i] = trans_mat[3][i];  // V[3] is always 1.
        // C:     Res[i] += V[0] * trans_mat[0][i];
        // C:     Res[i] += V[1] * trans_mat[1][i];
        // C:     Res[i] += V[2] * trans_mat[2][i];
        // C: }
        double[] Res = new double[4];
        for (int i = 0; i < 4; i++) {
            Res[i] = trans_mat[3][i];  // V3 is always 1.0
            Res[i] += V0 * trans_mat[0][i];
            Res[i] += V1 * trans_mat[1][i];
            Res[i] += V2 * trans_mat[2][i];
        }

        // STEP 7: Homogeneous divide (util3d.c:868-871)
        // C: if (Res[3] == 0) Res[3] = 1.0e-5;
        // C: out->x = Res[0] / Res[3];
        // C: out->y = Res[1] / Res[3];
        if (Res[3] == 0) {
            Res[3] = 1.0e-5;
        }
        double vx = Res[0] / Res[3];
        double vy = Res[1] / Res[3];

        // STEP 8: TERMCOORD - Convert to screen coordinates (util3d.h:64-69, graph3d.c:534-539)
        // C: xmiddle = (plot_bounds.xright + plot_bounds.xleft) / 2;
        // C: ymiddle = (plot_bounds.ytop + plot_bounds.ybot) / 2;
        // C: xscaler = ((plot_bounds.xright - plot_bounds.xleft) * 4L) / 7L;
        // C: yscaler = ((plot_bounds.ytop - plot_bounds.ybot) * 4L) / 7L;
        // C: xvar = (((v)->x * xscaler)) + xmiddle;
        // C: yvar = (((v)->y * yscaler)) + ymiddle;

        double xmiddle = (plotRight + plotLeft) / 2.0;
        double ymiddle = (plotTop + plotBottom) / 2.0;
        double xscaler = ((plotRight - plotLeft) * 4.0) / 7.0;
        double yscaler = ((plotBottom - plotTop) * 4.0) / 7.0;

        double screenX = (vx * xscaler) + xmiddle;
        double screenY = ymiddle - (vy * yscaler);  // Invert Y for SVG coordinate system

        return new double[]{screenX, screenY};
    }

    /**
     * Compute 4x4 transformation matrix exactly as C gnuplot does.
     * C code: graph3d.c:742-750
     */
    private double[][] compute_transformation_matrix(double rot_x_deg, double rot_z_deg, double scale) {
        // Convert to radians
        double rot_x = Math.toRadians(rot_x_deg);
        double rot_z = Math.toRadians(rot_z_deg);

        // Identity matrix
        double[][] mat = {
            {1, 0, 0, 0},
            {0, 1, 0, 0},
            {0, 0, 1, 0},
            {0, 0, 0, 1}
        };

        // Step 1: Rotate around Z axis (horizontal rotation)
        // C: mat_rot_z(surface_rot_z, trans_mat);
        double[][] rot_z_mat = {
            {Math.cos(rot_z), -Math.sin(rot_z), 0, 0},
            {Math.sin(rot_z), Math.cos(rot_z), 0, 0},
            {0, 0, 1, 0},
            {0, 0, 0, 1}
        };
        mat = matrixMultiply(mat, rot_z_mat);

        // Step 2: Rotate around X axis (vertical rotation)
        // C: mat_rot_x(surface_rot_x, mat);
        // Use rot_x directly (SVG Y inversion handled in TERMCOORD step)
        double[][] rot_x_mat = {
            {1, 0, 0, 0},
            {0, Math.cos(rot_x), -Math.sin(rot_x), 0},
            {0, Math.sin(rot_x), Math.cos(rot_x), 0},
            {0, 0, 0, 1}
        };
        mat = matrixMultiply(mat, rot_x_mat);

        // Step 3: Scale
        // C: mat_scale(surface_scale / 2.0, surface_scale / 2.0, surface_scale / 2.0, mat);
        double s = scale / 2.0;
        double[][] scale_mat = {
            {s, 0, 0, 0},
            {0, s, 0, 0},
            {0, 0, s, 0},
            {0, 0, 0, 1}
        };
        mat = matrixMultiply(mat, scale_mat);

        return mat;
    }

    /**
     * 4x4 matrix multiplication: result = m1 * m2
     */
    private double[][] matrixMultiply(double[][] m1, double[][] m2) {
        double[][] result = new double[4][4];
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                result[i][j] = 0;
                for (int k = 0; k < 4; k++) {
                    result[i][j] += m1[i][k] * m2[k][j];
                }
            }
        }
        return result;
    }

    /**
     * NEW IMPLEMENTATION - Direct 1:1 translation from C gnuplot for wireframe rendering
     */
    private void renderWireframe3D(SurfacePlot3D surfacePlot, ViewTransform3D viewTransform, String clipAttr) throws IOException {
        List<Point3D> points = surfacePlot.getPoints();
        if (points.isEmpty()) {
            return;
        }

        // Get data ranges from viewport
        Viewport viewport = scene.getViewport();

        // Project all points to 2D using NEW C-based algorithm
        List<double[]> screenPoints = new ArrayList<>();
        for (Point3D point3D : points) {
            if (!point3D.isFinite()) {
                screenPoints.add(null);
                continue;
            }

            // Use NEW map3d_to_screen method
            double[] screenCoords = map3d_to_screen(point3D.x(), point3D.y(), point3D.z(), viewport);
            screenPoints.add(screenCoords);
        }

        // Render as connected polyline through all points
        writer.write(String.format("<%s>\n", clipAttr.isEmpty() ? "g" : "g" + clipAttr));

        // Build path by connecting consecutive valid points
        StringBuilder pathData = new StringBuilder();
        boolean firstPoint = true;

        for (double[] screenCoords : screenPoints) {
            if (screenCoords == null) {
                // Break in data - start new path segment
                firstPoint = true;
                continue;
            }

            double x = screenCoords[0];
            double y = screenCoords[1];

            if (firstPoint) {
                if (pathData.length() > 0) {
                    // Write previous path segment
                    writer.write(String.format(Locale.US,
                            "  <path d=\"%s\" stroke=\"%s\" stroke-width=\"1.0\" fill=\"none\"/>\n",
                            pathData.toString(), surfacePlot.getColor()));
                    pathData = new StringBuilder();
                }
                pathData.append(String.format(Locale.US, "M %.2f,%.2f", x, y));
                firstPoint = false;
            } else {
                pathData.append(String.format(Locale.US, " L %.2f,%.2f", x, y));
            }
        }

        // Write final path segment if any
        if (pathData.length() > 0) {
            writer.write(String.format(Locale.US,
                    "  <path d=\"%s\" stroke=\"%s\" stroke-width=\"1.0\" fill=\"none\"/>\n",
                    pathData.toString(), surfacePlot.getColor()));
        }

        writer.write("</g>\n");
    }

    private double mapProjectedX(double x) {
        // Map normalized x [-1, 1] to screen coordinates
        // Matches C gnuplot's TERMCOORD macro: xvar = (v->x * xscaler) + xmiddle
        // Reference: gnuplot-c/src/graph3d.c:538-539 and util3d.h:TERMCOORD
        double xscaler = (plotRight - plotLeft) * 4.0 / 7.0;  // 4/7 viewport width
        double xmiddle = (plotLeft + plotRight) / 2.0;
        return x * xscaler + xmiddle;
    }

    private double mapProjectedY(double y) {
        // Map normalized y [-1, 1] to screen coordinates
        // Matches C gnuplot's TERMCOORD macro: yvar = (v->y * yscaler) + ymiddle
        // Note: SVG y-axis is inverted (top=0, bottom=height)
        // Reference: gnuplot-c/src/graph3d.c:538-539 and util3d.h:TERMCOORD
        double yscaler = (plotBottom - plotTop) * 4.0 / 7.0;  // 4/7 viewport height
        double ymiddle = (plotTop + plotBottom) / 2.0;
        return ymiddle - y * yscaler;  // Invert y-axis for SVG
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
