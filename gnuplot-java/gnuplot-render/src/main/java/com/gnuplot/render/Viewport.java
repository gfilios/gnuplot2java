package com.gnuplot.render;

/**
 * Defines the visible coordinate space for rendering.
 * Maps from data coordinates to screen/canvas coordinates.
 *
 * @since 1.0
 */
public final class Viewport {
    /**
     * Default 2D viewport with range [0, 1] for both axes.
     */
    public static final Viewport DEFAULT = Viewport.of2D(0.0, 1.0, 0.0, 1.0);

    private final double xMin;
    private final double xMax;
    private final double yMin;
    private final double yMax;
    private final double zMin;
    private final double zMax;
    private final boolean is3D;
    // Tick steps calculated from original data range (before axis extension)
    private final double xTicStep;
    private final double yTicStep;
    private final double zTicStep;
    // Original data Z minimum (before ticslevel adjustment)
    private final double zDataMin;

    private Viewport(Builder builder) {
        this.xMin = builder.xMin;
        this.xMax = builder.xMax;
        this.yMin = builder.yMin;
        this.yMax = builder.yMax;
        this.zMin = builder.zMin;
        this.zMax = builder.zMax;
        this.is3D = builder.is3D;
        this.xTicStep = builder.xTicStep;
        this.yTicStep = builder.yTicStep;
        this.zTicStep = builder.zTicStep;
        this.zDataMin = builder.zDataMin;

        if (xMin >= xMax) {
            throw new IllegalArgumentException("xMin must be less than xMax");
        }
        if (yMin >= yMax) {
            throw new IllegalArgumentException("yMin must be less than yMax");
        }
        if (is3D && zMin >= zMax) {
            throw new IllegalArgumentException("zMin must be less than zMax for 3D viewport");
        }
    }

    /**
     * Creates a 2D viewport with the specified bounds.
     *
     * @param xMin minimum x coordinate
     * @param xMax maximum x coordinate
     * @param yMin minimum y coordinate
     * @param yMax maximum y coordinate
     * @return 2D viewport
     */
    public static Viewport of2D(double xMin, double xMax, double yMin, double yMax) {
        return builder()
                .xRange(xMin, xMax)
                .yRange(yMin, yMax)
                .build();
    }

    /**
     * Creates a 3D viewport with the specified bounds.
     *
     * @param xMin minimum x coordinate
     * @param xMax maximum x coordinate
     * @param yMin minimum y coordinate
     * @param yMax maximum y coordinate
     * @param zMin minimum z coordinate
     * @param zMax maximum z coordinate
     * @return 3D viewport
     */
    public static Viewport of3D(double xMin, double xMax, double yMin, double yMax, double zMin, double zMax) {
        return builder()
                .xRange(xMin, xMax)
                .yRange(yMin, yMax)
                .zRange(zMin, zMax)
                .build();
    }

    public static Builder builder() {
        return new Builder();
    }

    public double getXMin() {
        return xMin;
    }

    public double getXMax() {
        return xMax;
    }

    public double getYMin() {
        return yMin;
    }

    public double getYMax() {
        return yMax;
    }

    public double getZMin() {
        return zMin;
    }

    public double getZMax() {
        return zMax;
    }

    public boolean is3D() {
        return is3D;
    }

    /**
     * Returns the X-axis tick step (calculated from original data range).
     * Returns 0 if not set.
     */
    public double getXTicStep() {
        return xTicStep;
    }

    /**
     * Returns the Y-axis tick step (calculated from original data range).
     * Returns 0 if not set.
     */
    public double getYTicStep() {
        return yTicStep;
    }

    /**
     * Returns the Z-axis tick step (calculated from original data range).
     * Returns 0 if not set.
     */
    public double getZTicStep() {
        return zTicStep;
    }

    /**
     * Returns the original data Z minimum (before ticslevel adjustment).
     * Returns 0 if not set. This is used to generate Z-axis ticks at the
     * correct positions (matching C gnuplot behavior).
     */
    public double getZDataMin() {
        return zDataMin;
    }

    /**
     * Returns the width of the viewport (x range).
     *
     * @return xMax - xMin
     */
    public double getWidth() {
        return xMax - xMin;
    }

    /**
     * Returns the height of the viewport (y range).
     *
     * @return yMax - yMin
     */
    public double getHeight() {
        return yMax - yMin;
    }

    /**
     * Returns the depth of the viewport (z range).
     *
     * @return zMax - zMin for 3D viewports, 0 for 2D
     */
    public double getDepth() {
        return is3D ? zMax - zMin : 0.0;
    }

    /**
     * Returns the aspect ratio (width / height).
     *
     * @return aspect ratio
     */
    public double getAspectRatio() {
        return getWidth() / getHeight();
    }

    /**
     * Checks if a point is within this viewport.
     *
     * @param x x coordinate
     * @param y y coordinate
     * @return true if point is within viewport
     */
    public boolean contains(double x, double y) {
        return x >= xMin && x <= xMax && y >= yMin && y <= yMax;
    }

    /**
     * Checks if a 3D point is within this viewport.
     *
     * @param x x coordinate
     * @param y y coordinate
     * @param z z coordinate
     * @return true if point is within viewport
     */
    public boolean contains(double x, double y, double z) {
        return contains(x, y) && (!is3D || (z >= zMin && z <= zMax));
    }

    @Override
    public String toString() {
        if (is3D) {
            return String.format("Viewport3D[x=[%.2f, %.2f], y=[%.2f, %.2f], z=[%.2f, %.2f]]",
                    xMin, xMax, yMin, yMax, zMin, zMax);
        } else {
            return String.format("Viewport2D[x=[%.2f, %.2f], y=[%.2f, %.2f]]",
                    xMin, xMax, yMin, yMax);
        }
    }

    public static class Builder {
        private double xMin = 0.0;
        private double xMax = 1.0;
        private double yMin = 0.0;
        private double yMax = 1.0;
        private double zMin = 0.0;
        private double zMax = 1.0;
        private boolean is3D = false;
        private double xTicStep = 0.0;
        private double yTicStep = 0.0;
        private double zTicStep = 0.0;
        private double zDataMin = 0.0;

        private Builder() {
        }

        public Builder xRange(double min, double max) {
            this.xMin = min;
            this.xMax = max;
            return this;
        }

        public Builder yRange(double min, double max) {
            this.yMin = min;
            this.yMax = max;
            return this;
        }

        public Builder zRange(double min, double max) {
            this.zMin = min;
            this.zMax = max;
            this.is3D = true;
            return this;
        }

        /**
         * Sets the tick steps (calculated from original data range before extension).
         */
        public Builder ticSteps(double xStep, double yStep, double zStep) {
            this.xTicStep = xStep;
            this.yTicStep = yStep;
            this.zTicStep = zStep;
            return this;
        }

        /**
         * Sets the original data Z minimum (before ticslevel adjustment).
         */
        public Builder zDataMin(double zDataMin) {
            this.zDataMin = zDataMin;
            return this;
        }

        public Viewport build() {
            return new Viewport(this);
        }
    }
}
