package com.gnuplot.render.elements;

import com.gnuplot.render.SceneElement;
import com.gnuplot.render.SceneElementVisitor;
import com.gnuplot.render.axis.TickGenerator;

import java.util.List;
import java.util.Objects;

/**
 * Represents an axis in the scene graph.
 * Axes define the coordinate system and provide scale information.
 *
 * @since 1.0
 */
public final class Axis implements SceneElement {

    private final String id;
    private final AxisType axisType;
    private final ScaleType scaleType;
    private final double min;
    private final double max;
    private final String label;
    private final boolean showGrid;
    private final boolean showTicks;
    private final int minorTicsCount;
    private final TickGenerator tickGenerator;

    private Axis(Builder builder) {
        this.id = builder.id;
        this.axisType = builder.axisType;
        this.scaleType = builder.scaleType;
        this.min = builder.min;
        this.max = builder.max;
        this.label = builder.label;
        this.showGrid = builder.showGrid;
        this.showTicks = builder.showTicks;
        this.minorTicsCount = builder.minorTicsCount;
        this.tickGenerator = builder.tickGenerator != null ? builder.tickGenerator : new TickGenerator();
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public ElementType getType() {
        return ElementType.AXIS;
    }

    @Override
    public void accept(SceneElementVisitor visitor) {
        visitor.visitAxis(this);
    }

    public String getId() {
        return id;
    }

    public AxisType getAxisType() {
        return axisType;
    }

    public ScaleType getScaleType() {
        return scaleType;
    }

    public double getMin() {
        return min;
    }

    public double getMax() {
        return max;
    }

    public String getLabel() {
        return label;
    }

    public boolean isShowGrid() {
        return showGrid;
    }

    public boolean isShowTicks() {
        return showTicks;
    }

    public int getMinorTicsCount() {
        return minorTicsCount;
    }

    /**
     * Generates tick marks for this axis based on its configuration.
     *
     * @return A list of tick marks for this axis
     */
    public List<TickGenerator.Tick> generateTicks() {
        switch (scaleType) {
            case LINEAR:
                return tickGenerator.generateTicks(min, max, 20, minorTicsCount);
            case LOGARITHMIC:
                return tickGenerator.generateLogTicks(min, max, 10.0, minorTicsCount > 0);
            case TIME:
                return tickGenerator.generateTimeTicks(min, max, 20);
            default:
                throw new IllegalStateException("Unknown scale type: " + scaleType);
        }
    }

    /**
     * Generates tick marks for this axis with a specific guide parameter.
     *
     * @param guide The approximate maximum number of major ticks
     * @return A list of tick marks for this axis
     */
    public List<TickGenerator.Tick> generateTicks(int guide) {
        switch (scaleType) {
            case LINEAR:
                return tickGenerator.generateTicks(min, max, guide, minorTicsCount);
            case LOGARITHMIC:
                return tickGenerator.generateLogTicks(min, max, 10.0, minorTicsCount > 0);
            case TIME:
                return tickGenerator.generateTimeTicks(min, max, guide);
            default:
                throw new IllegalStateException("Unknown scale type: " + scaleType);
        }
    }

    /**
     * Axis type enumeration.
     */
    public enum AxisType {
        X_AXIS,
        Y_AXIS,
        Z_AXIS,
        X2_AXIS,  // Secondary x-axis
        Y2_AXIS   // Secondary y-axis
    }

    /**
     * Scale type enumeration.
     */
    public enum ScaleType {
        LINEAR,
        LOGARITHMIC,
        TIME
    }

    public static class Builder {
        private String id;
        private AxisType axisType = AxisType.X_AXIS;
        private ScaleType scaleType = ScaleType.LINEAR;
        private double min = 0.0;
        private double max = 1.0;
        private String label;
        private boolean showGrid = true;
        private boolean showTicks = true;
        private int minorTicsCount = 0;
        private TickGenerator tickGenerator;

        private Builder() {
        }

        public Builder id(String id) {
            this.id = Objects.requireNonNull(id, "id cannot be null");
            return this;
        }

        public Builder axisType(AxisType axisType) {
            this.axisType = Objects.requireNonNull(axisType, "axisType cannot be null");
            return this;
        }

        public Builder scaleType(ScaleType scaleType) {
            this.scaleType = Objects.requireNonNull(scaleType, "scaleType cannot be null");
            return this;
        }

        public Builder range(double min, double max) {
            if (min >= max) {
                throw new IllegalArgumentException("min must be less than max");
            }
            this.min = min;
            this.max = max;
            return this;
        }

        public Builder label(String label) {
            this.label = label;
            return this;
        }

        public Builder showGrid(boolean showGrid) {
            this.showGrid = showGrid;
            return this;
        }

        public Builder showTicks(boolean showTicks) {
            this.showTicks = showTicks;
            return this;
        }

        public Builder minorTicsCount(int minorTicsCount) {
            if (minorTicsCount < 0) {
                throw new IllegalArgumentException("minorTicsCount must be non-negative");
            }
            this.minorTicsCount = minorTicsCount;
            return this;
        }

        public Builder tickGenerator(TickGenerator tickGenerator) {
            this.tickGenerator = Objects.requireNonNull(tickGenerator, "tickGenerator cannot be null");
            return this;
        }

        public Axis build() {
            if (id == null) {
                throw new IllegalStateException("id is required");
            }
            return new Axis(this);
        }
    }

    @Override
    public String toString() {
        return String.format("Axis{id='%s', type=%s, scale=%s, range=[%.2f, %.2f], label='%s', grid=%s, ticks=%s, minorTics=%d}",
                id, axisType, scaleType, min, max, label, showGrid, showTicks, minorTicsCount);
    }
}
