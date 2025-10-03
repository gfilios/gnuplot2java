package com.gnuplot.render.elements;

import com.gnuplot.render.SceneElement;
import com.gnuplot.render.SceneElementVisitor;
import com.gnuplot.render.style.MarkerStyle;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Represents a scatter plot in the scene graph.
 * A scatter plot displays individual data points with customizable markers.
 *
 * @since 1.0
 */
public final class ScatterPlot implements SceneElement {

    private final String id;
    private final List<DataPoint> points;
    private final MarkerStyle markerStyle;
    private final String label;

    private ScatterPlot(Builder builder) {
        this.id = builder.id;
        this.points = Collections.unmodifiableList(new ArrayList<>(builder.points));
        this.markerStyle = builder.markerStyle;
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
        visitor.visitScatterPlot(this);
    }

    public String getId() {
        return id;
    }

    public List<DataPoint> getPoints() {
        return points;
    }

    public MarkerStyle getMarkerStyle() {
        return markerStyle;
    }

    public String getLabel() {
        return label;
    }

    /**
     * Data point for scatter plots.
     * Can optionally have individual size/color overrides.
     */
    public static final class DataPoint {
        private final double x;
        private final double y;
        private final Double customSize;  // null = use default from MarkerStyle
        private final String customColor; // null = use default from MarkerStyle

        public DataPoint(double x, double y) {
            this(x, y, null, null);
        }

        public DataPoint(double x, double y, Double customSize, String customColor) {
            this.x = x;
            this.y = y;
            this.customSize = customSize;
            this.customColor = customColor;
        }

        public double getX() {
            return x;
        }

        public double getY() {
            return y;
        }

        public Double getCustomSize() {
            return customSize;
        }

        public String getCustomColor() {
            return customColor;
        }

        public boolean hasCustomSize() {
            return customSize != null;
        }

        public boolean hasCustomColor() {
            return customColor != null;
        }

        @Override
        public String toString() {
            if (customSize != null || customColor != null) {
                return String.format("(%.2f, %.2f, size=%.1f, color=%s)",
                        x, y, customSize, customColor);
            }
            return String.format("(%.2f, %.2f)", x, y);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (!(obj instanceof DataPoint)) return false;
            DataPoint other = (DataPoint) obj;
            return Double.compare(x, other.x) == 0
                    && Double.compare(y, other.y) == 0
                    && Objects.equals(customSize, other.customSize)
                    && Objects.equals(customColor, other.customColor);
        }

        @Override
        public int hashCode() {
            return Objects.hash(x, y, customSize, customColor);
        }
    }

    public static class Builder {
        private String id;
        private final List<DataPoint> points = new ArrayList<>();
        private MarkerStyle markerStyle = MarkerStyle.DEFAULT;
        private String label;

        private Builder() {
        }

        public Builder id(String id) {
            this.id = Objects.requireNonNull(id, "id cannot be null");
            return this;
        }

        public Builder addPoint(double x, double y) {
            this.points.add(new DataPoint(x, y));
            return this;
        }

        public Builder addPoint(double x, double y, Double customSize, String customColor) {
            this.points.add(new DataPoint(x, y, customSize, customColor));
            return this;
        }

        public Builder addPoint(DataPoint point) {
            this.points.add(Objects.requireNonNull(point, "point cannot be null"));
            return this;
        }

        public Builder points(List<DataPoint> points) {
            this.points.clear();
            this.points.addAll(Objects.requireNonNull(points, "points cannot be null"));
            return this;
        }

        public Builder markerStyle(MarkerStyle markerStyle) {
            this.markerStyle = Objects.requireNonNull(markerStyle, "markerStyle cannot be null");
            return this;
        }

        public Builder label(String label) {
            this.label = label;
            return this;
        }

        public ScatterPlot build() {
            if (id == null) {
                throw new IllegalStateException("id is required");
            }
            if (points.isEmpty()) {
                throw new IllegalStateException("at least one point is required");
            }
            return new ScatterPlot(this);
        }
    }

    @Override
    public String toString() {
        return String.format("ScatterPlot{id='%s', points=%d, marker=%s, label='%s'}",
                id, points.size(), markerStyle, label);
    }
}
