package com.gnuplot.render.svg;

import com.gnuplot.render.*;
import com.gnuplot.render.color.Color;
import com.gnuplot.render.elements.Axis;
import com.gnuplot.render.elements.Legend;
import com.gnuplot.render.elements.LinePlot;
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
        this.viewport = scene.getViewport() != null ? scene.getViewport() : Viewport.DEFAULT;
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

            // Determine stroke-dasharray based on line style
            String dashArray = getStrokeDashArray(linePlot.getLineStyle());

            // Write polyline
            writer.write(String.format(Locale.US,
                    "  <polyline points=\"%s\" fill=\"none\" stroke=\"%s\" stroke-width=\"2\"%s/>\n",
                    points, linePlot.getColor(), dashArray));

        } catch (IOException e) {
            throw new RuntimeException("Failed to render line plot", e);
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
            // Calculate legend position
            int[] pos = getLegendPosition(legend.getPosition());
            int x = pos[0];
            int y = pos[1];

            // Draw legend box if border is enabled
            if (legend.isShowBorder()) {
                writer.write(String.format(Locale.US,
                        "  <rect x=\"%d\" y=\"%d\" width=\"120\" height=\"%d\" fill=\"white\" stroke=\"black\" stroke-width=\"1\"/>\n",
                        x, y, legend.getEntries().size() * 20 + 10));
            }

            // Draw legend entries
            int entryY = y + 15;
            for (Legend.LegendEntry entry : legend.getEntries()) {
                // Draw line sample
                String dashArray = getStrokeDashArray(entry.getLineStyle());
                writer.write(String.format(Locale.US,
                        "  <line x1=\"%d\" y1=\"%d\" x2=\"%d\" y2=\"%d\" stroke=\"%s\" stroke-width=\"2\"%s/>\n",
                        x + 5, entryY, x + 25, entryY, entry.getColor(), dashArray));

                // Draw label
                writer.write(String.format(Locale.US,
                        "  <text x=\"%d\" y=\"%d\" font-size=\"10\" alignment-baseline=\"middle\">%s</text>\n",
                        x + 30, entryY + 2, escapeXml(entry.getLabel())));

                entryY += 20;
            }

        } catch (IOException e) {
            throw new RuntimeException("Failed to render legend", e);
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
     * Get legend position coordinates.
     */
    private int[] getLegendPosition(Legend.Position position) {
        int margin = 10;
        return switch (position) {
            case TOP_LEFT -> new int[]{margin, margin};
            case TOP_RIGHT -> new int[]{scene.getWidth() - 130 - margin, margin};
            case BOTTOM_LEFT -> new int[]{margin, scene.getHeight() - 100 - margin};
            case BOTTOM_RIGHT -> new int[]{scene.getWidth() - 130 - margin, scene.getHeight() - 100 - margin};
            case TOP_CENTER -> new int[]{scene.getWidth() / 2 - 60, margin};
            case BOTTOM_CENTER -> new int[]{scene.getWidth() / 2 - 60, scene.getHeight() - 100 - margin};
            case LEFT_CENTER -> new int[]{margin, scene.getHeight() / 2 - 50};
            case RIGHT_CENTER -> new int[]{scene.getWidth() - 130 - margin, scene.getHeight() / 2 - 50};
            case CENTER -> new int[]{scene.getWidth() / 2 - 60, scene.getHeight() / 2 - 50};
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
