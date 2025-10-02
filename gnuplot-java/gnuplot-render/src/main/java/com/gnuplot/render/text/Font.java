package com.gnuplot.render.text;

/**
 * Represents a font for text rendering in plots.
 * Immutable record containing font family, size, and style information.
 *
 * @param family Font family name (e.g., "Arial", "Times New Roman", "Courier")
 * @param size Font size in points
 * @param style Font style (combination of PLAIN, BOLD, ITALIC)
 */
public record Font(String family, double size, int style) {

    /** Plain font style */
    public static final int PLAIN = 0;
    /** Bold font style */
    public static final int BOLD = 1;
    /** Italic font style */
    public static final int ITALIC = 2;
    /** Bold and italic font style */
    public static final int BOLD_ITALIC = BOLD | ITALIC;

    /**
     * Validates font parameters.
     */
    public Font {
        if (family == null || family.isBlank()) {
            throw new IllegalArgumentException("Font family cannot be null or blank");
        }
        if (size <= 0) {
            throw new IllegalArgumentException("Font size must be positive, got: " + size);
        }
        if (style < PLAIN || style > BOLD_ITALIC) {
            throw new IllegalArgumentException("Invalid font style: " + style);
        }
    }

    /**
     * Checks if this font is bold.
     */
    public boolean isBold() {
        return (style & BOLD) != 0;
    }

    /**
     * Checks if this font is italic.
     */
    public boolean isItalic() {
        return (style & ITALIC) != 0;
    }

    /**
     * Creates a plain font.
     */
    public static Font plain(String family, double size) {
        return new Font(family, size, PLAIN);
    }

    /**
     * Creates a bold font.
     */
    public static Font bold(String family, double size) {
        return new Font(family, size, BOLD);
    }

    /**
     * Creates an italic font.
     */
    public static Font italic(String family, double size) {
        return new Font(family, size, ITALIC);
    }

    /**
     * Creates a bold italic font.
     */
    public static Font boldItalic(String family, double size) {
        return new Font(family, size, BOLD_ITALIC);
    }

    /**
     * Creates a new font with a different size.
     */
    public Font withSize(double newSize) {
        return new Font(family, newSize, style);
    }

    /**
     * Creates a new font with a different family.
     */
    public Font withFamily(String newFamily) {
        return new Font(newFamily, size, style);
    }

    /**
     * Creates a new font with a different style.
     */
    public Font withStyle(int newStyle) {
        return new Font(family, size, newStyle);
    }

    /**
     * Converts to a CSS font string for SVG rendering.
     */
    public String toCssString() {
        StringBuilder css = new StringBuilder();

        if (isItalic()) {
            css.append("italic ");
        }
        if (isBold()) {
            css.append("bold ");
        }

        css.append(size).append("pt ");
        css.append("'").append(family).append("'");

        return css.toString();
    }

    // Common default fonts
    public static final Font DEFAULT = plain("Arial", 12);
    public static final Font TITLE = bold("Arial", 16);
    public static final Font AXIS_LABEL = plain("Arial", 10);
    public static final Font TICK_LABEL = plain("Arial", 9);
    public static final Font LEGEND = plain("Arial", 10);
    public static final Font MONOSPACE = plain("Courier New", 10);
}
