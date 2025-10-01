package com.gnuplot.render.elements;

import com.gnuplot.render.SceneElement;
import com.gnuplot.render.SceneElementVisitor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Represents a legend in the scene graph.
 * Legends display information about the data series in the plot.
 *
 * @since 1.0
 */
public final class Legend implements SceneElement {

    private final String id;
    private final Position position;
    private final List<LegendEntry> entries;
    private final boolean showBorder;

    private Legend(Builder builder) {
        this.id = builder.id;
        this.position = builder.position;
        this.entries = Collections.unmodifiableList(new ArrayList<>(builder.entries));
        this.showBorder = builder.showBorder;
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public ElementType getType() {
        return ElementType.LEGEND;
    }

    @Override
    public void accept(SceneElementVisitor visitor) {
        visitor.visitLegend(this);
    }

    public String getId() {
        return id;
    }

    public Position getPosition() {
        return position;
    }

    public List<LegendEntry> getEntries() {
        return entries;
    }

    public boolean isShowBorder() {
        return showBorder;
    }

    /**
     * Legend position enumeration.
     */
    public enum Position {
        TOP_LEFT,
        TOP_RIGHT,
        BOTTOM_LEFT,
        BOTTOM_RIGHT,
        TOP_CENTER,
        BOTTOM_CENTER,
        LEFT_CENTER,
        RIGHT_CENTER,
        CENTER
    }

    /**
     * Single entry in the legend.
     */
    public static final class LegendEntry {
        private final String label;
        private final String color;
        private final LinePlot.LineStyle lineStyle;

        public LegendEntry(String label, String color, LinePlot.LineStyle lineStyle) {
            this.label = Objects.requireNonNull(label, "label cannot be null");
            this.color = Objects.requireNonNull(color, "color cannot be null");
            this.lineStyle = Objects.requireNonNull(lineStyle, "lineStyle cannot be null");
        }

        public String getLabel() {
            return label;
        }

        public String getColor() {
            return color;
        }

        public LinePlot.LineStyle getLineStyle() {
            return lineStyle;
        }

        @Override
        public String toString() {
            return String.format("LegendEntry{label='%s', color='%s', style=%s}",
                    label, color, lineStyle);
        }
    }

    public static class Builder {
        private String id;
        private Position position = Position.TOP_RIGHT;
        private final List<LegendEntry> entries = new ArrayList<>();
        private boolean showBorder = true;

        private Builder() {
        }

        public Builder id(String id) {
            this.id = Objects.requireNonNull(id, "id cannot be null");
            return this;
        }

        public Builder position(Position position) {
            this.position = Objects.requireNonNull(position, "position cannot be null");
            return this;
        }

        public Builder addEntry(String label, String color, LinePlot.LineStyle lineStyle) {
            this.entries.add(new LegendEntry(label, color, lineStyle));
            return this;
        }

        public Builder addEntry(LegendEntry entry) {
            this.entries.add(Objects.requireNonNull(entry, "entry cannot be null"));
            return this;
        }

        public Builder entries(List<LegendEntry> entries) {
            this.entries.clear();
            this.entries.addAll(Objects.requireNonNull(entries, "entries cannot be null"));
            return this;
        }

        public Builder showBorder(boolean showBorder) {
            this.showBorder = showBorder;
            return this;
        }

        public Legend build() {
            if (id == null) {
                throw new IllegalStateException("id is required");
            }
            return new Legend(this);
        }
    }

    @Override
    public String toString() {
        return String.format("Legend{id='%s', position=%s, entries=%d, border=%s}",
                id, position, entries.size(), showBorder);
    }
}
