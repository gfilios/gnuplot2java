package com.gnuplot.render.elements;

import com.gnuplot.render.SceneElement;
import com.gnuplot.render.SceneElementVisitor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Represents a line plot in the scene graph.
 * A line plot connects a series of points with lines.
 *
 * @since 1.0
 */
public final class LinePlot implements SceneElement {

    private final String id;
    private final List<Point2D> points;
    private final LineStyle lineStyle;
    private final String color;
    private final String label;

    private LinePlot(Builder builder) {
        this.id = builder.id;
        this.points = Collections.unmodifiableList(new ArrayList<>(builder.points));
        this.lineStyle = builder.lineStyle;
        this.color = builder.color;
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
        visitor.visitLinePlot(this);
    }

    public String getId() {
        return id;
    }

    public List<Point2D> getPoints() {
        return points;
    }

    public LineStyle getLineStyle() {
        return lineStyle;
    }

    public String getColor() {
        return color;
    }

    public String getLabel() {
        return label;
    }

    /**
     * 2D point for line plots.
     */
    public static final class Point2D {
        private final double x;
        private final double y;

        public Point2D(double x, double y) {
            this.x = x;
            this.y = y;
        }

        public double getX() {
            return x;
        }

        public double getY() {
            return y;
        }

        @Override
        public String toString() {
            return String.format("(%.2f, %.2f)", x, y);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (!(obj instanceof Point2D)) return false;
            Point2D other = (Point2D) obj;
            return Double.compare(x, other.x) == 0 && Double.compare(y, other.y) == 0;
        }

        @Override
        public int hashCode() {
            return Objects.hash(x, y);
        }
    }

    /**
     * Line style enumeration.
     */
    public enum LineStyle {
        SOLID,
        DASHED,
        DOTTED,
        DASH_DOT,
        NONE
    }

    public static class Builder {
        private String id;
        private final List<Point2D> points = new ArrayList<>();
        private LineStyle lineStyle = LineStyle.SOLID;
        private String color = "#000000";
        private String label;

        private Builder() {
        }

        public Builder id(String id) {
            this.id = Objects.requireNonNull(id, "id cannot be null");
            return this;
        }

        public Builder addPoint(double x, double y) {
            this.points.add(new Point2D(x, y));
            return this;
        }

        public Builder addPoint(Point2D point) {
            this.points.add(Objects.requireNonNull(point, "point cannot be null"));
            return this;
        }

        public Builder points(List<Point2D> points) {
            this.points.clear();
            this.points.addAll(Objects.requireNonNull(points, "points cannot be null"));
            return this;
        }

        public Builder lineStyle(LineStyle lineStyle) {
            this.lineStyle = Objects.requireNonNull(lineStyle, "lineStyle cannot be null");
            return this;
        }

        public Builder color(String color) {
            this.color = Objects.requireNonNull(color, "color cannot be null");
            return this;
        }

        public Builder label(String label) {
            this.label = label;
            return this;
        }

        public LinePlot build() {
            if (id == null) {
                throw new IllegalStateException("id is required");
            }
            if (points.isEmpty()) {
                throw new IllegalStateException("at least one point is required");
            }
            return new LinePlot(this);
        }
    }

    @Override
    public String toString() {
        return String.format("LinePlot{id='%s', points=%d, style=%s, color='%s', label='%s'}",
                id, points.size(), lineStyle, color, label);
    }
}
