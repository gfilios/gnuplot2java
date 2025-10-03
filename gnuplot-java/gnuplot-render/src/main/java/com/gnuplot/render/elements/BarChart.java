package com.gnuplot.render.elements;

import com.gnuplot.render.SceneElement;
import com.gnuplot.render.SceneElementVisitor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Represents a bar chart in the scene graph.
 * Supports vertical and horizontal bars.
 *
 * @since 1.0
 */
public final class BarChart implements SceneElement {

    private final String id;
    private final List<Bar> bars;
    private final Orientation orientation;
    private final double barWidth;
    private final String label;

    private BarChart(Builder builder) {
        this.id = builder.id;
        this.bars = Collections.unmodifiableList(new ArrayList<>(builder.bars));
        this.orientation = builder.orientation;
        this.barWidth = builder.barWidth;
        this.label = builder.label;
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public ElementType getType() {
        return ElementType.PLOT;
    }

    @Override
    public void accept(SceneElementVisitor visitor) {
        visitor.visitBarChart(this);
    }

    public String getId() {
        return id;
    }

    public List<Bar> getBars() {
        return bars;
    }

    public Orientation getOrientation() {
        return orientation;
    }

    public double getBarWidth() {
        return barWidth;
    }

    public String getLabel() {
        return label;
    }

    /**
     * Bar orientation.
     */
    public enum Orientation {
        VERTICAL,
        HORIZONTAL
    }

    /**
     * Individual bar in the chart.
     */
    public static final class Bar {
        private final double x;
        private final double height;
        private final String color;
        private final String label;

        public Bar(double x, double height) {
            this(x, height, "#4A90E2", null);
        }

        public Bar(double x, double height, String color, String label) {
            this.x = x;
            this.height = height;
            this.color = Objects.requireNonNull(color, "color cannot be null");
            this.label = label;
        }

        public double getX() {
            return x;
        }

        public double getHeight() {
            return height;
        }

        public String getColor() {
            return color;
        }

        public String getLabel() {
            return label;
        }

        @Override
        public String toString() {
            return String.format("Bar{x=%.2f, height=%.2f, color='%s'}", x, height, color);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (!(obj instanceof Bar)) return false;
            Bar other = (Bar) obj;
            return Double.compare(x, other.x) == 0
                    && Double.compare(height, other.height) == 0
                    && color.equals(other.color)
                    && Objects.equals(label, other.label);
        }

        @Override
        public int hashCode() {
            return Objects.hash(x, height, color, label);
        }
    }

    public static class Builder {
        private String id;
        private final List<Bar> bars = new ArrayList<>();
        private Orientation orientation = Orientation.VERTICAL;
        private double barWidth = 0.8;
        private String label;

        private Builder() {
        }

        public Builder id(String id) {
            this.id = Objects.requireNonNull(id, "id cannot be null");
            return this;
        }

        public Builder addBar(double x, double height) {
            this.bars.add(new Bar(x, height));
            return this;
        }

        public Builder addBar(double x, double height, String color) {
            this.bars.add(new Bar(x, height, color, null));
            return this;
        }

        public Builder addBar(double x, double height, String color, String label) {
            this.bars.add(new Bar(x, height, color, label));
            return this;
        }

        public Builder addBar(Bar bar) {
            this.bars.add(Objects.requireNonNull(bar, "bar cannot be null"));
            return this;
        }

        public Builder bars(List<Bar> bars) {
            this.bars.clear();
            this.bars.addAll(Objects.requireNonNull(bars, "bars cannot be null"));
            return this;
        }

        public Builder orientation(Orientation orientation) {
            this.orientation = Objects.requireNonNull(orientation, "orientation cannot be null");
            return this;
        }

        public Builder barWidth(double barWidth) {
            if (barWidth <= 0 || barWidth > 1) {
                throw new IllegalArgumentException("barWidth must be in (0, 1], got: " + barWidth);
            }
            this.barWidth = barWidth;
            return this;
        }

        public Builder label(String label) {
            this.label = label;
            return this;
        }

        public BarChart build() {
            if (id == null) {
                throw new IllegalStateException("id is required");
            }
            if (bars.isEmpty()) {
                throw new IllegalStateException("at least one bar is required");
            }
            return new BarChart(this);
        }
    }

    @Override
    public String toString() {
        return String.format("BarChart{id='%s', bars=%d, orientation=%s, barWidth=%.2f, label='%s'}",
                id, bars.size(), orientation, barWidth, label);
    }
}
