package com.gnuplot.cli.demo;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Compares two SVG files structurally and semantically.
 */
public class SvgComparator {

    private final double tolerance;

    public SvgComparator(double tolerance) {
        this.tolerance = tolerance;
    }

    public SvgComparator() {
        this(0.01); // 1% tolerance by default
    }

    /**
     * Result of SVG comparison.
     */
    public static class ComparisonResult {
        private final boolean identical;
        private final double similarity;
        private final List<String> differences;

        public ComparisonResult(boolean identical, double similarity, List<String> differences) {
            this.identical = identical;
            this.similarity = similarity;
            this.differences = differences;
        }

        public boolean isIdentical() { return identical; }
        public double getSimilarity() { return similarity; }
        public List<String> getDifferences() { return differences; }

        public boolean isAcceptable(double threshold) {
            return similarity >= threshold;
        }
    }

    /**
     * Compare two SVG files structurally.
     */
    public ComparisonResult compare(Path svg1, Path svg2) throws IOException {
        if (!Files.exists(svg1) || !Files.exists(svg2)) {
            List<String> diffs = new ArrayList<>();
            if (!Files.exists(svg1)) {
                diffs.add("File does not exist: " + svg1);
            }
            if (!Files.exists(svg2)) {
                diffs.add("File does not exist: " + svg2);
            }
            return new ComparisonResult(false, 0.0, diffs);
        }

        try {
            Document doc1 = parseSvg(svg1);
            Document doc2 = parseSvg(svg2);

            List<String> differences = new ArrayList<>();

            // Compare dimensions
            String[] dims1 = getSvgDimensions(doc1);
            String[] dims2 = getSvgDimensions(doc2);
            if (!dims1[0].equals(dims2[0]) || !dims1[1].equals(dims2[1])) {
                differences.add(String.format("Dimension mismatch: %sx%s vs %sx%s",
                    dims1[0], dims1[1], dims2[0], dims2[1]));
            }

            // Compare element counts
            int count1 = countElements(doc1);
            int count2 = countElements(doc2);
            if (count1 != count2) {
                differences.add(String.format("Element count mismatch: %d vs %d", count1, count2));
            }

            // Compare specific element types
            compareElements(doc1, doc2, "rect", differences);
            compareElements(doc1, doc2, "circle", differences);
            compareElements(doc1, doc2, "line", differences);
            compareElements(doc1, doc2, "polyline", differences);
            compareElements(doc1, doc2, "polygon", differences);
            compareElements(doc1, doc2, "path", differences);
            compareElements(doc1, doc2, "text", differences);

            // Calculate similarity
            double similarity = calculateSimilarity(doc1, doc2, differences);

            boolean identical = differences.isEmpty();

            return new ComparisonResult(identical, similarity, differences);

        } catch (Exception e) {
            List<String> diffs = new ArrayList<>();
            diffs.add("Error comparing SVGs: " + e.getMessage());
            return new ComparisonResult(false, 0.0, diffs);
        }
    }

    private Document parseSvg(Path svgFile) throws ParserConfigurationException, IOException, SAXException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        DocumentBuilder builder = factory.newDocumentBuilder();
        return builder.parse(svgFile.toFile());
    }

    private String[] getSvgDimensions(Document doc) {
        Element root = doc.getDocumentElement();
        String width = root.getAttribute("width");
        String height = root.getAttribute("height");
        return new String[]{width.isEmpty() ? "unknown" : width, height.isEmpty() ? "unknown" : height};
    }

    private int countElements(Document doc) {
        return countElements(doc.getDocumentElement());
    }

    private int countElements(Node node) {
        int count = 1;
        NodeList children = node.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if (child.getNodeType() == Node.ELEMENT_NODE) {
                count += countElements(child);
            }
        }
        return count;
    }

    private void compareElements(Document doc1, Document doc2, String tagName, List<String> differences) {
        int count1 = doc1.getElementsByTagName(tagName).getLength();
        int count2 = doc2.getElementsByTagName(tagName).getLength();

        if (count1 != count2) {
            differences.add(String.format("<%s> count: %d vs %d", tagName, count1, count2));
        }
    }

    private double calculateSimilarity(Document doc1, Document doc2, List<String> differences) {
        // If no differences, SVGs are identical
        if (differences.isEmpty()) {
            return 1.0;
        }

        // Simple similarity metric based on structural differences
        int totalElements = Math.max(countElements(doc1), countElements(doc2));

        if (totalElements == 0) {
            return 1.0; // Both empty
        }

        // Count matching elements
        int matches = 0;
        String[] elementTypes = {"rect", "circle", "line", "polyline", "polygon", "path", "text"};

        for (String type : elementTypes) {
            int count1 = doc1.getElementsByTagName(type).getLength();
            int count2 = doc2.getElementsByTagName(type).getLength();
            matches += Math.min(count1, count2);
        }

        // Similarity = (matches / total) with penalty for differences
        double baseSimilarity = (double) matches / totalElements;
        double penalty = Math.min(0.5, differences.size() * 0.05);

        return Math.max(0.0, baseSimilarity - penalty);
    }

    /**
     * Compare text content in SVGs.
     */
    public List<String> compareTextContent(Path svg1, Path svg2) throws IOException {
        List<String> differences = new ArrayList<>();

        try {
            Document doc1 = parseSvg(svg1);
            Document doc2 = parseSvg(svg2);

            NodeList texts1 = doc1.getElementsByTagName("text");
            NodeList texts2 = doc2.getElementsByTagName("text");

            if (texts1.getLength() != texts2.getLength()) {
                differences.add(String.format("Text element count: %d vs %d",
                    texts1.getLength(), texts2.getLength()));
            }

            int minCount = Math.min(texts1.getLength(), texts2.getLength());
            for (int i = 0; i < minCount; i++) {
                String text1 = texts1.item(i).getTextContent().trim();
                String text2 = texts2.item(i).getTextContent().trim();

                if (!text1.equals(text2)) {
                    differences.add(String.format("Text[%d]: '%s' vs '%s'", i, text1, text2));
                }
            }

        } catch (Exception e) {
            differences.add("Error comparing text: " + e.getMessage());
        }

        return differences;
    }

    /**
     * Get SVG statistics for reporting.
     */
    public static class SvgStats {
        public final int totalElements;
        public final int rectangles;
        public final int circles;
        public final int lines;
        public final int polylines;
        public final int polygons;
        public final int paths;
        public final int texts;
        public final String width;
        public final String height;

        public SvgStats(int totalElements, int rectangles, int circles, int lines,
                       int polylines, int polygons, int paths, int texts,
                       String width, String height) {
            this.totalElements = totalElements;
            this.rectangles = rectangles;
            this.circles = circles;
            this.lines = lines;
            this.polylines = polylines;
            this.polygons = polygons;
            this.paths = paths;
            this.texts = texts;
            this.width = width;
            this.height = height;
        }
    }

    public SvgStats getStats(Path svgFile) throws IOException {
        try {
            Document doc = parseSvg(svgFile);
            String[] dims = getSvgDimensions(doc);

            return new SvgStats(
                countElements(doc),
                doc.getElementsByTagName("rect").getLength(),
                doc.getElementsByTagName("circle").getLength(),
                doc.getElementsByTagName("line").getLength(),
                doc.getElementsByTagName("polyline").getLength(),
                doc.getElementsByTagName("polygon").getLength(),
                doc.getElementsByTagName("path").getLength(),
                doc.getElementsByTagName("text").getLength(),
                dims[0],
                dims[1]
            );
        } catch (Exception e) {
            throw new IOException("Error getting SVG stats: " + e.getMessage(), e);
        }
    }
}
