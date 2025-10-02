package com.gnuplot.render.color;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents a color palette for mapping grayscale values [0,1] to RGB colors.
 * Corresponds to gnuplot's t_sm_palette structure and color mapping functions.
 * <p>
 * A palette can be created using:
 * <ul>
 *   <li>RGB formulas (three formula numbers for R, G, B channels)</li>
 *   <li>Gradient points (interpolated smooth colors)</li>
 *   <li>Grayscale (with gamma correction)</li>
 *   <li>Cubehelix scheme</li>
 *   <li>Named palettes (viridis, etc.)</li>
 * </ul>
 */
public final class ColorPalette {

    private final PaletteMode mode;
    private final int formulaR;
    private final int formulaG;
    private final int formulaB;
    private final List<GradientPoint> gradient;
    private final double gamma;
    private final CubehelixParams cubehelix;

    private ColorPalette(Builder builder) {
        this.mode = builder.mode;
        this.formulaR = builder.formulaR;
        this.formulaG = builder.formulaG;
        this.formulaB = builder.formulaB;
        this.gradient = builder.gradient != null ?
                        Collections.unmodifiableList(new ArrayList<>(builder.gradient)) :
                        Collections.emptyList();
        this.gamma = builder.gamma;
        this.cubehelix = builder.cubehelix;
    }

    /**
     * Maps a gray value [0,1] to an RGB color using this palette.
     *
     * @param gray Gray value in [0, 1]
     * @return RGB color
     */
    public Color getColor(double gray) {
        gray = Math.max(0.0, Math.min(1.0, gray));

        return switch (mode) {
            case GRAYSCALE -> {
                double value = Math.pow(gray, 1.0 / gamma);
                yield Color.gray(value);
            }
            case RGB_FORMULAS -> {
                double r = ColorFormula.applyFormula(formulaR, gray);
                double g = ColorFormula.applyFormula(formulaG, gray);
                double b = ColorFormula.applyFormula(formulaB, gray);
                yield new Color(r, g, b);
            }
            case GRADIENT -> interpolateGradient(gray);
            case CUBEHELIX -> generateCubehelix(gray);
        };
    }

    /**
     * Interpolates color from gradient points.
     */
    private Color interpolateGradient(double gray) {
        if (gradient.isEmpty()) {
            return Color.gray(gray);
        }
        if (gradient.size() == 1) {
            return gradient.get(0).color();
        }

        // Find the two gradient points to interpolate between
        GradientPoint before = gradient.get(0);
        GradientPoint after = gradient.get(gradient.size() - 1);

        for (int i = 0; i < gradient.size() - 1; i++) {
            GradientPoint p1 = gradient.get(i);
            GradientPoint p2 = gradient.get(i + 1);

            if (gray >= p1.position() && gray <= p2.position()) {
                before = p1;
                after = p2;
                break;
            }
        }

        // Handle edge cases
        if (gray <= before.position()) {
            return before.color();
        }
        if (gray >= after.position()) {
            return after.color();
        }

        // Linear interpolation
        double range = after.position() - before.position();
        double t = (gray - before.position()) / range;
        return before.color().interpolate(after.color(), t);
    }

    /**
     * Generates color using the cubehelix scheme.
     * Based on D.A. Green (2011) http://arxiv.org/abs/1108.5083
     */
    private Color generateCubehelix(double gray) {
        if (cubehelix == null) {
            return Color.gray(gray);
        }

        double phi = 2.0 * Math.PI * (cubehelix.start / 3.0 + gray * cubehelix.cycles);
        if (gamma != 1.0) {
            gray = Math.pow(gray, 1.0 / gamma);
        }
        double a = cubehelix.saturation * gray * (1.0 - gray) / 2.0;

        double r = gray + a * (-0.14861 * Math.cos(phi) + 1.78277 * Math.sin(phi));
        double g = gray + a * (-0.29227 * Math.cos(phi) - 0.90649 * Math.sin(phi));
        double b = gray + a * (1.97294 * Math.cos(phi));

        return new Color(r, g, b);
    }

    /**
     * Represents a point in a gradient palette.
     *
     * @param position Position in [0, 1]
     * @param color RGB color at this position
     */
    public record GradientPoint(double position, Color color) {
        public GradientPoint {
            if (position < 0.0 || position > 1.0) {
                throw new IllegalArgumentException("Gradient position must be in [0, 1], got: " + position);
            }
        }
    }

    /**
     * Parameters for cubehelix color scheme.
     */
    private record CubehelixParams(double start, double cycles, double saturation) {}

    /**
     * Palette generation mode.
     */
    public enum PaletteMode {
        GRAYSCALE,
        RGB_FORMULAS,
        GRADIENT,
        CUBEHELIX
    }

    /**
     * Builder for ColorPalette.
     */
    public static class Builder {
        private PaletteMode mode = PaletteMode.RGB_FORMULAS;
        private int formulaR = 7;  // sqrt(x)
        private int formulaG = 5;  // x^3
        private int formulaB = 15; // sin(360x)
        private List<GradientPoint> gradient;
        private double gamma = 1.5;
        private CubehelixParams cubehelix;

        /**
         * Creates a palette using RGB formulas (gnuplot default: 7,5,15).
         *
         * @param formulaR Formula for red channel (-36 to 36)
         * @param formulaG Formula for green channel (-36 to 36)
         * @param formulaB Formula for blue channel (-36 to 36)
         */
        public Builder rgbFormulas(int formulaR, int formulaG, int formulaB) {
            this.mode = PaletteMode.RGB_FORMULAS;
            this.formulaR = formulaR;
            this.formulaG = formulaG;
            this.formulaB = formulaB;
            return this;
        }

        /**
         * Creates a grayscale palette with gamma correction.
         *
         * @param gamma Gamma value (default 1.5)
         */
        public Builder grayscale(double gamma) {
            this.mode = PaletteMode.GRAYSCALE;
            this.gamma = gamma;
            return this;
        }

        /**
         * Creates a gradient palette from interpolated points.
         *
         * @param gradientPoints List of gradient points (sorted by position)
         */
        public Builder gradient(List<GradientPoint> gradientPoints) {
            this.mode = PaletteMode.GRADIENT;
            this.gradient = new ArrayList<>(gradientPoints);
            // Sort by position
            this.gradient.sort((a, b) -> Double.compare(a.position(), b.position()));
            return this;
        }

        /**
         * Adds a gradient point.
         *
         * @param position Position in [0, 1]
         * @param color Color at this position
         */
        public Builder addGradientPoint(double position, Color color) {
            if (this.gradient == null) {
                this.gradient = new ArrayList<>();
                this.mode = PaletteMode.GRADIENT;
            }
            this.gradient.add(new GradientPoint(position, color));
            return this;
        }

        /**
         * Creates a cubehelix palette.
         *
         * @param start Start angle (radians offset from colorwheel 0)
         * @param cycles Number of rotations through the colorwheel
         * @param saturation Color saturation
         */
        public Builder cubehelix(double start, double cycles, double saturation) {
            this.mode = PaletteMode.CUBEHELIX;
            this.cubehelix = new CubehelixParams(start, cycles, saturation);
            return this;
        }

        /**
         * Sets the gamma value for grayscale and cubehelix palettes.
         *
         * @param gamma Gamma value
         */
        public Builder gamma(double gamma) {
            this.gamma = gamma;
            return this;
        }

        /**
         * Builds the ColorPalette.
         */
        public ColorPalette build() {
            return new ColorPalette(this);
        }
    }

    /**
     * Creates a builder for constructing palettes.
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Creates the default gnuplot palette (formulas 7,5,15).
     */
    public static ColorPalette defaultPalette() {
        return builder().rgbFormulas(7, 5, 15).build();
    }

    /**
     * Creates a simple grayscale palette.
     */
    public static ColorPalette grayscale() {
        return builder().grayscale(1.0).build();
    }

    /**
     * Creates a grayscale palette with gamma correction.
     */
    public static ColorPalette grayscale(double gamma) {
        return builder().grayscale(gamma).build();
    }
}
