package com.gnuplot.cli.demo;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Compares SVG files structurally by parsing their DOM and comparing
 * semantic elements like tick marks, labels, data paths, and legends.
 *
 * This approach is more accurate than pixel comparison because it:
 * - Ignores anti-aliasing and font rendering differences
 * - Provides clear, actionable error messages
 * - Is faster than rasterization-based comparison
 */
public class SvgStructuralComparator {

    // Tolerance for comparing coordinate values (handles floating point differences)
    private static final double COORDINATE_TOLERANCE = 2.0;

    // Pattern to extract numbers from path d attribute
    private static final Pattern PATH_NUMBER_PATTERN = Pattern.compile("-?\\d+\\.?\\d*");

    // Pattern to extract points from polyline points attribute
    private static final Pattern POINT_PAIR_PATTERN = Pattern.compile("(-?\\d+\\.?\\d*)\\s+(-?\\d+\\.?\\d*)");

    /**
     * Result of structural comparison between two SVG files.
     */
    public static class StructuralComparisonResult {
        private final List<String> criticalDifferences;
        private final List<String> minorDifferences;
        private final boolean structurallyEquivalent;
        private final SvgMetrics cMetrics;
        private final SvgMetrics javaMetrics;

        public StructuralComparisonResult(List<String> criticalDifferences,
                                         List<String> minorDifferences,
                                         boolean structurallyEquivalent,
                                         SvgMetrics cMetrics,
                                         SvgMetrics javaMetrics) {
            this.criticalDifferences = new ArrayList<>(criticalDifferences);
            this.minorDifferences = new ArrayList<>(minorDifferences);
            this.structurallyEquivalent = structurallyEquivalent;
            this.cMetrics = cMetrics;
            this.javaMetrics = javaMetrics;
        }

        public List<String> getCriticalDifferences() { return criticalDifferences; }
        public List<String> getMinorDifferences() { return minorDifferences; }
        public boolean isStructurallyEquivalent() { return structurallyEquivalent; }
        public SvgMetrics getCMetrics() { return cMetrics; }
        public SvgMetrics getJavaMetrics() { return javaMetrics; }

        public boolean hasCriticalDifferences() {
            return !criticalDifferences.isEmpty();
        }

        public String toDetailedReport() {
            StringBuilder sb = new StringBuilder();
            sb.append("=== SVG Structural Comparison Report ===\n\n");

            sb.append("Overall Status: ");
            sb.append(structurallyEquivalent ? "EQUIVALENT" : "DIFFERENT").append("\n\n");

            sb.append("--- C Gnuplot Metrics ---\n");
            sb.append(cMetrics.toReport()).append("\n");

            sb.append("--- Java Gnuplot Metrics ---\n");
            sb.append(javaMetrics.toReport()).append("\n");

            if (!criticalDifferences.isEmpty()) {
                sb.append("--- Critical Differences ---\n");
                for (String diff : criticalDifferences) {
                    sb.append("  * ").append(diff).append("\n");
                }
                sb.append("\n");
            }

            if (!minorDifferences.isEmpty()) {
                sb.append("--- Minor Differences ---\n");
                for (String diff : minorDifferences) {
                    sb.append("  - ").append(diff).append("\n");
                }
                sb.append("\n");
            }

            return sb.toString();
        }
    }

    /**
     * Metrics extracted from an SVG file.
     */
    public static class SvgMetrics {
        private int lineCount;
        private int pathCount;
        private int polylineCount;
        private int textCount;
        private int rectCount;

        private int xAxisTickCount;
        private int yAxisTickCount;
        private List<String> axisLabels;
        private List<String> legendEntries;
        private List<Integer> dataPointCounts;
        private String title;

        public SvgMetrics() {
            this.axisLabels = new ArrayList<>();
            this.legendEntries = new ArrayList<>();
            this.dataPointCounts = new ArrayList<>();
        }

        public int getLineCount() { return lineCount; }
        public void setLineCount(int lineCount) { this.lineCount = lineCount; }

        public int getPathCount() { return pathCount; }
        public void setPathCount(int pathCount) { this.pathCount = pathCount; }

        public int getPolylineCount() { return polylineCount; }
        public void setPolylineCount(int polylineCount) { this.polylineCount = polylineCount; }

        public int getTextCount() { return textCount; }
        public void setTextCount(int textCount) { this.textCount = textCount; }

        public int getRectCount() { return rectCount; }
        public void setRectCount(int rectCount) { this.rectCount = rectCount; }

        public int getXAxisTickCount() { return xAxisTickCount; }
        public void setXAxisTickCount(int xAxisTickCount) { this.xAxisTickCount = xAxisTickCount; }

        public int getYAxisTickCount() { return yAxisTickCount; }
        public void setYAxisTickCount(int yAxisTickCount) { this.yAxisTickCount = yAxisTickCount; }

        public List<String> getAxisLabels() { return axisLabels; }
        public void setAxisLabels(List<String> axisLabels) { this.axisLabels = axisLabels; }

        public List<String> getLegendEntries() { return legendEntries; }
        public void setLegendEntries(List<String> legendEntries) { this.legendEntries = legendEntries; }

        public List<Integer> getDataPointCounts() { return dataPointCounts; }
        public void setDataPointCounts(List<Integer> dataPointCounts) { this.dataPointCounts = dataPointCounts; }

        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }

        public int getTotalDataPoints() {
            return dataPointCounts.stream().mapToInt(Integer::intValue).sum();
        }

        public String toReport() {
            StringBuilder sb = new StringBuilder();
            sb.append("  Lines: ").append(lineCount).append("\n");
            sb.append("  Paths: ").append(pathCount).append("\n");
            sb.append("  Polylines: ").append(polylineCount).append("\n");
            sb.append("  Text elements: ").append(textCount).append("\n");
            sb.append("  X-axis ticks: ").append(xAxisTickCount).append("\n");
            sb.append("  Y-axis ticks: ").append(yAxisTickCount).append("\n");
            sb.append("  Axis labels: ").append(axisLabels).append("\n");
            sb.append("  Legend entries: ").append(legendEntries).append("\n");
            sb.append("  Data series: ").append(dataPointCounts.size()).append("\n");
            sb.append("  Total data points: ").append(getTotalDataPoints()).append("\n");
            if (title != null) {
                sb.append("  Title: ").append(title).append("\n");
            }
            return sb.toString();
        }
    }

    /**
     * Compare two SVG files structurally.
     */
    public StructuralComparisonResult compare(Path cSvg, Path javaSvg) throws IOException {
        if (!Files.exists(cSvg) || !Files.exists(javaSvg)) {
            List<String> critical = new ArrayList<>();
            if (!Files.exists(cSvg)) {
                critical.add("C SVG file does not exist: " + cSvg);
            }
            if (!Files.exists(javaSvg)) {
                critical.add("Java SVG file does not exist: " + javaSvg);
            }
            return new StructuralComparisonResult(critical, new ArrayList<>(), false,
                    new SvgMetrics(), new SvgMetrics());
        }

        try {
            Document cDoc = parseSvg(cSvg);
            Document javaDoc = parseSvg(javaSvg);

            SvgMetrics cMetrics = extractMetrics(cDoc);
            SvgMetrics javaMetrics = extractMetrics(javaDoc);

            List<String> critical = new ArrayList<>();
            List<String> minor = new ArrayList<>();

            // Compare tick counts
            if (cMetrics.getXAxisTickCount() != javaMetrics.getXAxisTickCount()) {
                critical.add(String.format("X-axis tick count differs: C=%d, Java=%d",
                        cMetrics.getXAxisTickCount(), javaMetrics.getXAxisTickCount()));
            }

            if (cMetrics.getYAxisTickCount() != javaMetrics.getYAxisTickCount()) {
                critical.add(String.format("Y-axis tick count differs: C=%d, Java=%d",
                        cMetrics.getYAxisTickCount(), javaMetrics.getYAxisTickCount()));
            }

            // Compare axis labels
            Set<String> cLabels = new HashSet<>(cMetrics.getAxisLabels());
            Set<String> javaLabels = new HashSet<>(javaMetrics.getAxisLabels());

            Set<String> missingInJava = new HashSet<>(cLabels);
            missingInJava.removeAll(javaLabels);
            if (!missingInJava.isEmpty()) {
                critical.add("Axis labels missing in Java: " + missingInJava);
            }

            Set<String> extraInJava = new HashSet<>(javaLabels);
            extraInJava.removeAll(cLabels);
            if (!extraInJava.isEmpty()) {
                minor.add("Extra axis labels in Java: " + extraInJava);
            }

            // Compare legend entries
            Set<String> cLegend = new HashSet<>(cMetrics.getLegendEntries());
            Set<String> javaLegend = new HashSet<>(javaMetrics.getLegendEntries());

            Set<String> missingLegend = new HashSet<>(cLegend);
            missingLegend.removeAll(javaLegend);
            if (!missingLegend.isEmpty()) {
                critical.add("Legend entries missing in Java: " + missingLegend);
            }

            // Compare data series count
            if (cMetrics.getDataPointCounts().size() != javaMetrics.getDataPointCounts().size()) {
                critical.add(String.format("Data series count differs: C=%d, Java=%d",
                        cMetrics.getDataPointCounts().size(), javaMetrics.getDataPointCounts().size()));
            } else {
                // Compare point counts per series (with tolerance)
                for (int i = 0; i < cMetrics.getDataPointCounts().size(); i++) {
                    int cPoints = cMetrics.getDataPointCounts().get(i);
                    int javaPoints = javaMetrics.getDataPointCounts().get(i);
                    double diff = Math.abs(cPoints - javaPoints);
                    double tolerance = Math.max(cPoints, javaPoints) * 0.1; // 10% tolerance

                    if (diff > tolerance && diff > 5) {
                        critical.add(String.format("Data series %d point count differs significantly: C=%d, Java=%d",
                                i + 1, cPoints, javaPoints));
                    } else if (cPoints != javaPoints) {
                        minor.add(String.format("Data series %d point count differs slightly: C=%d, Java=%d",
                                i + 1, cPoints, javaPoints));
                    }
                }
            }

            // Compare text element count (with tolerance)
            int textDiff = Math.abs(cMetrics.getTextCount() - javaMetrics.getTextCount());
            if (textDiff > 2) {
                minor.add(String.format("Text element count differs: C=%d, Java=%d",
                        cMetrics.getTextCount(), javaMetrics.getTextCount()));
            }

            boolean equivalent = critical.isEmpty();

            return new StructuralComparisonResult(critical, minor, equivalent, cMetrics, javaMetrics);

        } catch (ParserConfigurationException | SAXException e) {
            throw new IOException("Error parsing SVG: " + e.getMessage(), e);
        }
    }

    /**
     * Parse an SVG file into a DOM Document.
     */
    private Document parseSvg(Path svgPath) throws IOException, ParserConfigurationException, SAXException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        // Disable external entities for security
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", false);
        factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
        factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);

        DocumentBuilder builder = factory.newDocumentBuilder();
        return builder.parse(svgPath.toFile());
    }

    /**
     * Extract metrics from an SVG document.
     */
    private SvgMetrics extractMetrics(Document doc) {
        SvgMetrics metrics = new SvgMetrics();

        // Count basic elements
        NodeList lines = doc.getElementsByTagName("line");
        NodeList paths = doc.getElementsByTagName("path");
        NodeList polylines = doc.getElementsByTagName("polyline");
        NodeList texts = doc.getElementsByTagName("text");
        NodeList rects = doc.getElementsByTagName("rect");

        metrics.setLineCount(lines.getLength());
        metrics.setPathCount(paths.getLength());
        metrics.setPolylineCount(polylines.getLength());
        metrics.setTextCount(texts.getLength());
        metrics.setRectCount(rects.getLength());

        // Extract tick marks from line elements (Java gnuplot)
        extractTickMarks(lines, metrics);

        // Extract tick marks from path elements (C gnuplot)
        extractTickMarksFromPaths(paths, metrics);

        // Extract text labels
        extractTextLabels(texts, metrics);

        // Extract data point counts
        extractDataPointCounts(paths, polylines, metrics);

        // Extract title
        NodeList titles = doc.getElementsByTagName("title");
        if (titles.getLength() > 0) {
            metrics.setTitle(titles.item(0).getTextContent());
        }

        return metrics;
    }

    /**
     * Identify and count tick marks from line elements and path elements.
     * Tick marks are typically short lines perpendicular to axes.
     * C gnuplot uses path elements, Java gnuplot uses line elements.
     */
    private void extractTickMarks(NodeList lines, SvgMetrics metrics) {
        int xTicks = 0;
        int yTicks = 0;

        for (int i = 0; i < lines.getLength(); i++) {
            Element line = (Element) lines.item(i);

            double x1 = parseDouble(line.getAttribute("x1"), 0);
            double y1 = parseDouble(line.getAttribute("y1"), 0);
            double x2 = parseDouble(line.getAttribute("x2"), 0);
            double y2 = parseDouble(line.getAttribute("y2"), 0);

            double dx = Math.abs(x2 - x1);
            double dy = Math.abs(y2 - y1);

            // X-axis ticks: vertical short lines (dy small, dx ~= 0)
            if (dx < 2 && dy > 2 && dy < 20) {
                xTicks++;
            }
            // Y-axis ticks: horizontal short lines (dx small, dy ~= 0)
            else if (dy < 2 && dx > 2 && dx < 20) {
                yTicks++;
            }
        }

        // Tick marks come in pairs (top+bottom for X, left+right for Y)
        metrics.setXAxisTickCount(xTicks / 2);
        metrics.setYAxisTickCount(yTicks / 2);
    }

    /**
     * Extract tick marks from path elements (C gnuplot format).
     * C gnuplot encodes tick marks as short line segments in path d attribute.
     * Example: d='M54.53,564.00 L63.53,564.00 M774.82,564.00 L765.82,564.00'
     */
    private void extractTickMarksFromPaths(NodeList paths, SvgMetrics metrics) {
        int xTicks = 0;
        int yTicks = 0;

        // Pattern to extract M x,y L x,y segments
        Pattern segmentPattern = Pattern.compile("M\\s*(-?[\\d.]+)\\s*,\\s*(-?[\\d.]+)\\s*L\\s*(-?[\\d.]+)\\s*,\\s*(-?[\\d.]+)");

        for (int i = 0; i < paths.getLength(); i++) {
            Element path = (Element) paths.item(i);
            String d = path.getAttribute("d");
            String stroke = path.getAttribute("stroke");

            // Only consider black strokes (tick marks are typically black)
            if (d == null || d.isEmpty()) continue;
            if (stroke == null || (!stroke.equalsIgnoreCase("black") && !stroke.equals("#000") && !stroke.equals("#000000"))) continue;

            Matcher m = segmentPattern.matcher(d);
            while (m.find()) {
                try {
                    double x1 = Double.parseDouble(m.group(1));
                    double y1 = Double.parseDouble(m.group(2));
                    double x2 = Double.parseDouble(m.group(3));
                    double y2 = Double.parseDouble(m.group(4));

                    double dx = Math.abs(x2 - x1);
                    double dy = Math.abs(y2 - y1);

                    // X-axis ticks: vertical short lines
                    if (dx < 2 && dy > 2 && dy < 20) {
                        xTicks++;
                    }
                    // Y-axis ticks: horizontal short lines
                    else if (dy < 2 && dx > 2 && dx < 20) {
                        yTicks++;
                    }
                } catch (NumberFormatException e) {
                    // Skip malformed segments
                }
            }
        }

        // Add path-based ticks to existing counts
        // Tick marks come in pairs (top+bottom for X, left+right for Y)
        metrics.setXAxisTickCount(metrics.getXAxisTickCount() + xTicks / 2);
        metrics.setYAxisTickCount(metrics.getYAxisTickCount() + yTicks / 2);
    }

    /**
     * Extract text labels and legend entries.
     */
    private void extractTextLabels(NodeList texts, SvgMetrics metrics) {
        List<String> axisLabels = new ArrayList<>();
        List<String> legendEntries = new ArrayList<>();

        for (int i = 0; i < texts.getLength(); i++) {
            Element text = (Element) texts.item(i);
            String content = text.getTextContent().trim();

            if (content.isEmpty()) {
                continue;
            }

            // Try to get position
            double x = parseDouble(text.getAttribute("x"), 0);
            double y = parseDouble(text.getAttribute("y"), 0);

            // Check if it's in the transform attribute
            String transform = text.getAttribute("transform");
            if (transform != null && !transform.isEmpty()) {
                Pattern translatePattern = Pattern.compile("translate\\(([^,]+),([^)]+)\\)");
                Matcher m = translatePattern.matcher(transform);
                if (m.find()) {
                    x = parseDouble(m.group(1), x);
                    y = parseDouble(m.group(2), y);
                }
            }

            // Classify based on content and position
            if (isNumericLabel(content)) {
                axisLabels.add(content);
            } else if (isFunctionName(content)) {
                legendEntries.add(content);
            }
        }

        metrics.setAxisLabels(axisLabels);
        metrics.setLegendEntries(legendEntries);
    }

    /**
     * Extract data point counts from path and polyline elements.
     */
    private void extractDataPointCounts(NodeList paths, NodeList polylines, SvgMetrics metrics) {
        List<Integer> pointCounts = new ArrayList<>();

        // Count points in path elements
        for (int i = 0; i < paths.getLength(); i++) {
            Element path = (Element) paths.item(i);
            String d = path.getAttribute("d");

            // Skip paths that look like tick marks, borders, or legend boxes
            String stroke = path.getAttribute("stroke");
            if (stroke == null || stroke.isEmpty()) {
                stroke = "black";
            }

            // Data paths typically have colored strokes (not black)
            boolean isDataPath = !stroke.equalsIgnoreCase("black") &&
                                 !stroke.equals("#000") &&
                                 !stroke.equals("#000000");

            if (isDataPath && d != null && !d.isEmpty()) {
                int pointCount = countPathPoints(d);
                if (pointCount > 5) {  // Data series have many points
                    pointCounts.add(pointCount);
                }
            }
        }

        // Count points in polyline elements
        for (int i = 0; i < polylines.getLength(); i++) {
            Element polyline = (Element) polylines.item(i);
            String points = polyline.getAttribute("points");

            if (points != null && !points.isEmpty()) {
                int pointCount = countPolylinePoints(points);
                if (pointCount > 5) {  // Data series have many points
                    pointCounts.add(pointCount);
                }
            }
        }

        metrics.setDataPointCounts(pointCounts);
    }

    /**
     * Count the number of points in an SVG path's d attribute.
     */
    private int countPathPoints(String d) {
        // Count M (moveto) and L (lineto) commands, which represent points
        int count = 0;
        Matcher m = PATH_NUMBER_PATTERN.matcher(d);
        int numberCount = 0;
        while (m.find()) {
            numberCount++;
        }
        // Each point has x,y coordinates
        count = numberCount / 2;
        return count;
    }

    /**
     * Count the number of points in a polyline's points attribute.
     */
    private int countPolylinePoints(String points) {
        int count = 0;
        Matcher m = POINT_PAIR_PATTERN.matcher(points);
        while (m.find()) {
            count++;
        }
        return count;
    }

    /**
     * Check if a string looks like a numeric axis label.
     */
    private boolean isNumericLabel(String s) {
        if (s == null || s.isEmpty()) {
            return false;
        }
        s = s.trim();
        try {
            Double.parseDouble(s);
            return true;
        } catch (NumberFormatException e) {
            // Check for common axis label patterns like "-1.5", "0.5", etc.
            return s.matches("^-?\\d+(\\.\\d+)?$");
        }
    }

    /**
     * Check if a string looks like a function name (legend entry).
     */
    private boolean isFunctionName(String s) {
        if (s == null || s.isEmpty()) {
            return false;
        }
        s = s.trim();
        // Common patterns: sin(x), cos(x), atan(x), cos(atan(x)), etc.
        return s.matches(".*\\(.*\\).*") ||
               s.matches("^[a-zA-Z_][a-zA-Z0-9_]*$");
    }

    /**
     * Safely parse a double from a string.
     */
    private double parseDouble(String s, double defaultValue) {
        if (s == null || s.isEmpty()) {
            return defaultValue;
        }
        try {
            return Double.parseDouble(s.trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
}
