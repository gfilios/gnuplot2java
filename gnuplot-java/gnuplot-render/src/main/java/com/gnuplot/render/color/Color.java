package com.gnuplot.render.color;

/**
 * Represents a color in RGB space with components in the range [0, 1].
 * This class is immutable and corresponds to gnuplot's rgb_color struct.
 *
 * @param r Red component (0.0 to 1.0)
 * @param g Green component (0.0 to 1.0)
 * @param b Blue component (0.0 to 1.0)
 */
public record Color(double r, double g, double b) {

    /**
     * Validates and normalizes color components.
     */
    public Color {
        r = clamp(r);
        g = clamp(g);
        b = clamp(b);
    }

    /**
     * Creates a Color from RGB components in the range [0, 255].
     *
     * @param r Red component (0-255)
     * @param g Green component (0-255)
     * @param b Blue component (0-255)
     * @return Color with normalized components
     */
    public static Color fromRGB255(int r, int g, int b) {
        return new Color(r / 255.0, g / 255.0, b / 255.0);
    }

    /**
     * Creates a Color from a 24-bit RGB hex value (0xRRGGBB).
     *
     * @param rgb24 24-bit RGB value
     * @return Color with normalized components
     */
    public static Color fromRGB24(int rgb24) {
        int r = (rgb24 >> 16) & 0xFF;
        int g = (rgb24 >> 8) & 0xFF;
        int b = rgb24 & 0xFF;
        return fromRGB255(r, g, b);
    }

    /**
     * Creates a grayscale color.
     *
     * @param gray Grayscale value (0.0 to 1.0)
     * @return Grayscale color
     */
    public static Color gray(double gray) {
        double value = clamp(gray);
        return new Color(value, value, value);
    }

    /**
     * Converts this color to a 24-bit RGB integer (0xRRGGBB).
     *
     * @return 24-bit RGB value
     */
    public int toRGB24() {
        int red = (int) Math.round(r * 255);
        int green = (int) Math.round(g * 255);
        int blue = (int) Math.round(b * 255);
        return (red << 16) | (green << 8) | blue;
    }

    /**
     * Converts this color to HSV color space.
     *
     * @return HSV representation as {hue, saturation, value}
     */
    public double[] toHSV() {
        double max = Math.max(r, Math.max(g, b));
        double min = Math.min(r, Math.min(g, b));
        double delta = max - min;

        double hue = 0.0;
        if (delta != 0) {
            if (max == r) {
                hue = 60 * (((g - b) / delta) % 6);
            } else if (max == g) {
                hue = 60 * (((b - r) / delta) + 2);
            } else {
                hue = 60 * (((r - g) / delta) + 4);
            }
            if (hue < 0) {
                hue += 360;
            }
        }

        double saturation = (max == 0) ? 0 : (delta / max);
        double value = max;

        return new double[]{hue, saturation, value};
    }

    /**
     * Creates a color from HSV values.
     *
     * @param hue Hue in degrees (0-360)
     * @param saturation Saturation (0-1)
     * @param value Value (0-1)
     * @return RGB color
     */
    public static Color fromHSV(double hue, double saturation, double value) {
        saturation = clamp(saturation);
        value = clamp(value);

        // Normalize hue to [0, 360)
        hue = hue % 360;
        if (hue < 0) {
            hue += 360;
        }

        double c = value * saturation;
        double x = c * (1 - Math.abs((hue / 60) % 2 - 1));
        double m = value - c;

        double r, g, b;
        if (hue < 60) {
            r = c; g = x; b = 0;
        } else if (hue < 120) {
            r = x; g = c; b = 0;
        } else if (hue < 180) {
            r = 0; g = c; b = x;
        } else if (hue < 240) {
            r = 0; g = x; b = c;
        } else if (hue < 300) {
            r = x; g = 0; b = c;
        } else {
            r = c; g = 0; b = x;
        }

        return new Color(r + m, g + m, b + m);
    }

    /**
     * Linearly interpolates between two colors.
     *
     * @param other The target color
     * @param t Interpolation factor (0.0 to 1.0)
     * @return Interpolated color
     */
    public Color interpolate(Color other, double t) {
        t = clamp(t);
        return new Color(
                r + t * (other.r - r),
                g + t * (other.g - g),
                b + t * (other.b - b)
        );
    }

    /**
     * Clamps a value to the range [0, 1].
     */
    private static double clamp(double value) {
        return Math.max(0.0, Math.min(1.0, value));
    }

    // Common colors
    public static final Color BLACK = new Color(0, 0, 0);
    public static final Color WHITE = new Color(1, 1, 1);
    public static final Color RED = new Color(1, 0, 0);
    public static final Color GREEN = new Color(0, 1, 0);
    public static final Color BLUE = new Color(0, 0, 1);
    public static final Color YELLOW = new Color(1, 1, 0);
    public static final Color CYAN = new Color(0, 1, 1);
    public static final Color MAGENTA = new Color(1, 0, 1);
}
