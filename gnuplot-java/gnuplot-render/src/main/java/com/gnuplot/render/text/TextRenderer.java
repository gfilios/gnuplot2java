package com.gnuplot.render.text;

import java.awt.font.FontRenderContext;
import java.awt.font.LineMetrics;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;

/**
 * Renders text with support for fonts, alignment, rotation, and Unicode.
 * Uses Java AWT font rendering for accurate text measurements.
 */
public final class TextRenderer {

    private static final FontRenderContext DEFAULT_FRC =
            new FontRenderContext(null, true, true);

    /**
     * Measures the dimensions of text rendered with the given font.
     *
     * @param text Text to measure
     * @param font Font to use
     * @return Text metrics
     */
    public TextMetrics measureText(String text, Font font) {
        if (text == null || text.isEmpty()) {
            return TextMetrics.empty();
        }

        java.awt.Font awtFont = toAwtFont(font);
        Rectangle2D bounds = awtFont.getStringBounds(text, DEFAULT_FRC);
        LineMetrics lineMetrics = awtFont.getLineMetrics(text, DEFAULT_FRC);

        return new TextMetrics(
                bounds.getWidth(),
                bounds.getHeight(),
                lineMetrics.getAscent(),
                lineMetrics.getDescent()
        );
    }

    /**
     * Calculates the bounding box of rotated text.
     *
     * @param text Text to measure
     * @param font Font to use
     * @param angleDegrees Rotation angle in degrees
     * @return Bounding box dimensions
     */
    public Rectangle2D getRotatedBounds(String text, Font font, double angleDegrees) {
        if (text == null || text.isEmpty()) {
            return new Rectangle2D.Double(0, 0, 0, 0);
        }

        java.awt.Font awtFont = toAwtFont(font);
        Rectangle2D bounds = awtFont.getStringBounds(text, DEFAULT_FRC);

        if (angleDegrees == 0) {
            return bounds;
        }

        // Apply rotation transform to the bounds
        AffineTransform rotation = AffineTransform.getRotateInstance(
                Math.toRadians(angleDegrees)
        );

        return rotation.createTransformedShape(bounds).getBounds2D();
    }

    /**
     * Calculates the baseline position for aligned text.
     *
     * @param x X-coordinate of anchor point
     * @param y Y-coordinate of anchor point
     * @param text Text to position
     * @param font Font to use
     * @param alignment Horizontal alignment
     * @return Baseline coordinates as [x, y]
     */
    public double[] getBaselinePosition(double x, double y, String text,
                                       Font font, TextAlignment alignment) {
        TextMetrics metrics = measureText(text, font);
        double xOffset = alignment.getXOffset(metrics.width());

        return new double[]{x + xOffset, y};
    }

    /**
     * Validates that text contains only valid Unicode characters.
     *
     * @param text Text to validate
     * @return true if text is valid Unicode
     */
    public boolean isValidUnicode(String text) {
        if (text == null) {
            return false;
        }

        // Check for invalid surrogate pairs and control characters
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);

            // Check for unpaired surrogates
            if (Character.isHighSurrogate(c)) {
                if (i + 1 >= text.length() || !Character.isLowSurrogate(text.charAt(i + 1))) {
                    return false;
                }
                i++; // Skip the low surrogate
            } else if (Character.isLowSurrogate(c)) {
                return false; // Unpaired low surrogate
            }
        }

        return true;
    }

    /**
     * Escapes special characters for SVG rendering.
     *
     * @param text Text to escape
     * @return Escaped text
     */
    public String escapeForSvg(String text) {
        if (text == null) {
            return "";
        }

        return text
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&apos;");
    }

    /**
     * Converts a Font to java.awt.Font for measurement.
     */
    private java.awt.Font toAwtFont(Font font) {
        int awtStyle = java.awt.Font.PLAIN;

        if (font.isBold() && font.isItalic()) {
            awtStyle = java.awt.Font.BOLD | java.awt.Font.ITALIC;
        } else if (font.isBold()) {
            awtStyle = java.awt.Font.BOLD;
        } else if (font.isItalic()) {
            awtStyle = java.awt.Font.ITALIC;
        }

        return new java.awt.Font(font.family(), awtStyle, (int) font.size());
    }

    /**
     * Creates a default TextRenderer instance.
     */
    public static TextRenderer create() {
        return new TextRenderer();
    }
}
