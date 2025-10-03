package com.gnuplot.render;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Manages multiple plots in a single figure with grid or custom layouts.
 *
 * @since 1.0
 */
public final class MultiPlotLayout {

    private final int width;
    private final int height;
    private final String title;
    private final List<SubPlot> subPlots;
    private final LayoutMode mode;
    private final int gridRows;
    private final int gridCols;

    private MultiPlotLayout(Builder builder) {
        this.width = builder.width;
        this.height = builder.height;
        this.title = builder.title;
        this.subPlots = Collections.unmodifiableList(new ArrayList<>(builder.subPlots));
        this.mode = builder.mode;
        this.gridRows = builder.gridRows;
        this.gridCols = builder.gridCols;
    }

    public static Builder builder() {
        return new Builder();
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public String getTitle() {
        return title;
    }

    public List<SubPlot> getSubPlots() {
        return subPlots;
    }

    public LayoutMode getMode() {
        return mode;
    }

    public int getGridRows() {
        return gridRows;
    }

    public int getGridCols() {
        return gridCols;
    }

    /**
     * Layout mode for organizing subplots.
     */
    public enum LayoutMode {
        /** Grid layout with rows and columns */
        GRID,
        /** Custom positioning with explicit coordinates */
        CUSTOM
    }

    /**
     * A subplot within the multi-plot layout.
     */
    public static final class SubPlot {
        private final Scene scene;
        private final int row;
        private final int col;
        private final double x;
        private final double y;
        private final double widthFraction;
        private final double heightFraction;

        /**
         * Create a subplot for grid layout.
         */
        public SubPlot(Scene scene, int row, int col) {
            this(scene, row, col, 0, 0, 0, 0);
        }

        /**
         * Create a subplot for custom layout.
         */
        public SubPlot(Scene scene, double x, double y, double widthFraction, double heightFraction) {
            this(scene, -1, -1, x, y, widthFraction, heightFraction);
        }

        private SubPlot(Scene scene, int row, int col, double x, double y,
                       double widthFraction, double heightFraction) {
            this.scene = Objects.requireNonNull(scene, "scene cannot be null");
            this.row = row;
            this.col = col;
            this.x = x;
            this.y = y;
            this.widthFraction = widthFraction;
            this.heightFraction = heightFraction;
        }

        public Scene getScene() {
            return scene;
        }

        public int getRow() {
            return row;
        }

        public int getCol() {
            return col;
        }

        public double getX() {
            return x;
        }

        public double getY() {
            return y;
        }

        public double getWidthFraction() {
            return widthFraction;
        }

        public double getHeightFraction() {
            return heightFraction;
        }
    }

    public static class Builder {
        private int width = 800;
        private int height = 600;
        private String title;
        private final List<SubPlot> subPlots = new ArrayList<>();
        private LayoutMode mode = LayoutMode.GRID;
        private int gridRows = 1;
        private int gridCols = 1;

        private Builder() {
        }

        public Builder dimensions(int width, int height) {
            if (width <= 0 || height <= 0) {
                throw new IllegalArgumentException("width and height must be positive");
            }
            this.width = width;
            this.height = height;
            return this;
        }

        public Builder title(String title) {
            this.title = title;
            return this;
        }

        public Builder gridLayout(int rows, int cols) {
            if (rows <= 0 || cols <= 0) {
                throw new IllegalArgumentException("rows and cols must be positive");
            }
            this.mode = LayoutMode.GRID;
            this.gridRows = rows;
            this.gridCols = cols;
            return this;
        }

        public Builder customLayout() {
            this.mode = LayoutMode.CUSTOM;
            return this;
        }

        public Builder addPlot(Scene scene, int row, int col) {
            if (mode == LayoutMode.CUSTOM) {
                throw new IllegalStateException("cannot use grid positions with custom layout mode");
            }
            if (row < 0) {
                throw new IllegalArgumentException("row must be non-negative, got " + row);
            }
            if (col < 0) {
                throw new IllegalArgumentException("col must be non-negative, got " + col);
            }
            if (mode == LayoutMode.GRID && (row >= gridRows || col >= gridCols)) {
                throw new IllegalArgumentException(
                    String.format("subplot position (%d, %d) is outside grid %dx%d",
                        row, col, gridRows, gridCols));
            }
            this.subPlots.add(new SubPlot(scene, row, col));
            return this;
        }

        public Builder addPlot(Scene scene, double x, double y, double widthFraction, double heightFraction) {
            if (mode == LayoutMode.GRID) {
                throw new IllegalStateException("cannot use custom positions with grid layout mode");
            }
            if (x < 0.0 || x >= 1.0) {
                throw new IllegalArgumentException("x must be in range [0, 1), got " + x);
            }
            if (y < 0.0 || y >= 1.0) {
                throw new IllegalArgumentException("y must be in range [0, 1), got " + y);
            }
            if (widthFraction <= 0.0 || widthFraction > 1.0) {
                throw new IllegalArgumentException("widthFraction must be in range (0, 1], got " + widthFraction);
            }
            if (heightFraction <= 0.0 || heightFraction > 1.0) {
                throw new IllegalArgumentException("heightFraction must be in range (0, 1], got " + heightFraction);
            }
            this.subPlots.add(new SubPlot(scene, x, y, widthFraction, heightFraction));
            return this;
        }

        public Builder addSubPlot(SubPlot subPlot) {
            this.subPlots.add(Objects.requireNonNull(subPlot, "subPlot cannot be null"));
            return this;
        }

        public MultiPlotLayout build() {
            if (subPlots.isEmpty()) {
                throw new IllegalStateException("at least one subplot is required");
            }
            // All validation is done in addPlot methods
            return new MultiPlotLayout(this);
        }
    }

    @Override
    public String toString() {
        return String.format("MultiPlotLayout{mode=%s, plots=%d, grid=%dx%d}",
                mode, subPlots.size(), gridRows, gridCols);
    }
}
