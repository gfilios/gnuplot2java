package com.gnuplot.render.text;

import java.io.FileWriter;
import java.io.IOException;

/**
 * Demo class to visualize text rendering in SVG format.
 * Run this to generate an SVG file showing various text styles.
 */
public class TextRenderingDemo {

    public static void main(String[] args) throws IOException {
        TextRenderer renderer = TextRenderer.create();
        StringBuilder svg = new StringBuilder();

        // SVG header
        svg.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        svg.append("<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"800\" height=\"600\" viewBox=\"0 0 800 600\">\n");
        svg.append("  <!-- Background -->\n");
        svg.append("  <rect width=\"800\" height=\"600\" fill=\"white\"/>\n\n");

        // Title
        svg.append("  <!-- Title -->\n");
        Font titleFont = Font.TITLE;
        String titleText = "Gnuplot Text Rendering Demo";
        svg.append("  <text x=\"400\" y=\"40\" font-family=\"").append(titleFont.family()).append("\" ");
        svg.append("font-size=\"").append(titleFont.size()).append("\" ");
        svg.append("font-weight=\"").append(titleFont.isBold() ? "bold" : "normal").append("\" ");
        svg.append("text-anchor=\"middle\" fill=\"#000000\">").append(titleText).append("</text>\n\n");

        // Font styles demo
        svg.append("  <!-- Font Styles -->\n");
        int y = 100;

        Font plain = Font.plain("Arial", 14);
        svg.append(createTextElement(50, y, "Plain Text", plain, renderer));
        y += 40;

        Font bold = Font.bold("Arial", 14);
        svg.append(createTextElement(50, y, "Bold Text", bold, renderer));
        y += 40;

        Font italic = Font.italic("Arial", 14);
        svg.append(createTextElement(50, y, "Italic Text", italic, renderer));
        y += 40;

        Font boldItalic = Font.boldItalic("Arial", 14);
        svg.append(createTextElement(50, y, "Bold Italic Text", boldItalic, renderer));
        y += 60;

        // Different sizes
        svg.append("  <!-- Font Sizes -->\n");
        for (double size : new double[]{10, 12, 14, 18, 24}) {
            Font sizeFont = Font.plain("Arial", size);
            svg.append(createTextElement(50, y, "Font size " + (int)size + "pt", sizeFont, renderer));
            y += (int)(size * 1.5);
        }

        // Alignment demo
        svg.append("\n  <!-- Text Alignment -->\n");
        svg.append("  <line x1=\"400\" y1=\"380\" x2=\"400\" y2=\"480\" stroke=\"red\" stroke-width=\"1\" stroke-dasharray=\"5,5\"/>\n");

        Font alignFont = Font.plain("Arial", 12);

        // Left aligned
        double[] leftPos = renderer.getBaselinePosition(400, 400, "Left Aligned", alignFont, TextAlignment.LEFT);
        svg.append(createTextElement((int)leftPos[0], (int)leftPos[1], "Left Aligned", alignFont, renderer));

        // Center aligned
        double[] centerPos = renderer.getBaselinePosition(400, 430, "Center Aligned", alignFont, TextAlignment.CENTER);
        svg.append(createTextElement((int)centerPos[0], (int)centerPos[1], "Center Aligned", alignFont, renderer));

        // Right aligned
        double[] rightPos = renderer.getBaselinePosition(400, 460, "Right Aligned", alignFont, TextAlignment.RIGHT);
        svg.append(createTextElement((int)rightPos[0], (int)rightPos[1], "Right Aligned", alignFont, renderer));

        // Unicode demo
        svg.append("\n  <!-- Unicode Support -->\n");
        y = 510;
        Font unicodeFont = Font.plain("Arial", 14);
        svg.append(createTextElement(50, y, "Unicode: α β γ δ ε • → ← ↑ ↓ ✓ ✗", unicodeFont, renderer));
        y += 30;
        svg.append(createTextElement(50, y, "Chinese: 你好世界", unicodeFont, renderer));
        y += 30;
        svg.append(createTextElement(50, y, "Greek: Γειά σου κόσμε", unicodeFont, renderer));

        // SVG footer
        svg.append("\n</svg>");

        // Write to file
        String outputPath = "text-rendering-demo.svg";
        try (FileWriter writer = new FileWriter(outputPath)) {
            writer.write(svg.toString());
        }

        System.out.println("SVG demo generated: " + outputPath);
        System.out.println("Open it in a web browser to view the rendered text.");
    }

    private static String createTextElement(int x, int y, String text, Font font, TextRenderer renderer) {
        String escaped = renderer.escapeForSvg(text);
        StringBuilder element = new StringBuilder();

        element.append("  <text x=\"").append(x).append("\" y=\"").append(y).append("\" ");
        element.append("font-family=\"").append(font.family()).append("\" ");
        element.append("font-size=\"").append(font.size()).append("pt\" ");

        if (font.isBold()) {
            element.append("font-weight=\"bold\" ");
        }
        if (font.isItalic()) {
            element.append("font-style=\"italic\" ");
        }

        element.append("fill=\"#000000\">");
        element.append(escaped);
        element.append("</text>\n");

        return element.toString();
    }
}
