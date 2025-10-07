package com.gnuplot.render.elements;

import com.gnuplot.render.SceneElement;
import com.gnuplot.render.SceneElementVisitor;
import com.gnuplot.render.style.MarkerStyle;

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
    private final int columns;
    private final String fontFamily;
    private final int fontSize;
    private final String borderColor;
    private final String backgroundColor;

    private Legend(Builder builder) {
        this.id = builder.id;
        this.position = builder.position;
        this.entries = Collections.unmodifiableList(new ArrayList<>(builder.entries));
        this.showBorder = builder.showBorder;
        this.columns = builder.columns;
        this.fontFamily = builder.fontFamily;
        this.fontSize = builder.fontSize;
        this.borderColor = builder.borderColor;
        this.backgroundColor = builder.backgroundColor;
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

    public int getColumns() {
        return columns;
    }

    public String getFontFamily() {
        return fontFamily;
    }

    public int getFontSize() {
        return fontSize;
    }

    public String getBorderColor() {
        return borderColor;
    }

    public String getBackgroundColor() {
        return backgroundColor;
    }

    /**
     * Legend position enumeration.
     * Margin positions (TMARGIN_*, BMARGIN_*) place legend outside the plot area.
     * Non-margin positions place legend inside the plot area.
     */
    public enum Position {
        // Inside plot area
        TOP_LEFT,
        TOP_RIGHT,
        BOTTOM_LEFT,
        BOTTOM_RIGHT,
        TOP_CENTER,
        BOTTOM_CENTER,
        LEFT_CENTER,
        RIGHT_CENTER,
        CENTER,
        // Outside plot area (margins)
        TMARGIN_LEFT,
        TMARGIN_CENTER,
        TMARGIN_RIGHT,
        BMARGIN_LEFT,
        BMARGIN_CENTER,
        BMARGIN_RIGHT
    }

    /**
     * Symbol type for legend entries.
     */
    public enum SymbolType {
        LINE,       // Line style symbol
        MARKER,     // Marker symbol
        LINE_MARKER // Both line and marker
    }

    /**
     * Single entry in the legend.
     */
    public static final class LegendEntry {
        private final String label;
        private final String color;
        private final LinePlot.LineStyle lineStyle;
        private final MarkerStyle markerStyle;
        private final SymbolType symbolType;

        // Line-only constructor (backward compatibility - not ambiguous with MarkerStyle version)
        public LegendEntry(String label, String color, LinePlot.LineStyle lineStyle) {
            this.label = Objects.requireNonNull(label, "label cannot be null");
            this.color = Objects.requireNonNull(color, "color cannot be null");
            this.lineStyle = Objects.requireNonNull(lineStyle, "lineStyle cannot be null");
            this.markerStyle = null;
            this.symbolType = SymbolType.LINE;
        }

        // Marker-only static factory
        public static LegendEntry withMarker(String label, String color, MarkerStyle markerStyle) {
            return new LegendEntry(label, color, null, markerStyle, SymbolType.MARKER);
        }

        // Line and marker static factory
        public static LegendEntry withLineAndMarker(String label, String color,
                                                    LinePlot.LineStyle lineStyle, MarkerStyle markerStyle) {
            return new LegendEntry(label, color, lineStyle, markerStyle, SymbolType.LINE_MARKER);
        }

        // Full private constructor
        private LegendEntry(String label, String color, LinePlot.LineStyle lineStyle,
                           MarkerStyle markerStyle, SymbolType symbolType) {
            this.label = Objects.requireNonNull(label, "label cannot be null");
            this.color = Objects.requireNonNull(color, "color cannot be null");
            this.lineStyle = lineStyle;
            this.markerStyle = markerStyle;
            this.symbolType = Objects.requireNonNull(symbolType, "symbolType cannot be null");

            // Validate that required styles are present
            if (symbolType == SymbolType.LINE && lineStyle == null) {
                throw new IllegalArgumentException("lineStyle required for LINE symbol type");
            }
            if (symbolType == SymbolType.MARKER && markerStyle == null) {
                throw new IllegalArgumentException("markerStyle required for MARKER symbol type");
            }
            if (symbolType == SymbolType.LINE_MARKER && (lineStyle == null || markerStyle == null)) {
                throw new IllegalArgumentException("both lineStyle and markerStyle required for LINE_MARKER symbol type");
            }
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

        public MarkerStyle getMarkerStyle() {
            return markerStyle;
        }

        public SymbolType getSymbolType() {
            return symbolType;
        }

        @Override
        public String toString() {
            if (symbolType == SymbolType.LINE && lineStyle != null) {
                return String.format("LegendEntry{label='%s', color='%s', style=%s}",
                        label, color, lineStyle);
            } else if (symbolType == SymbolType.MARKER && markerStyle != null) {
                return String.format("LegendEntry{label='%s', color='%s', marker=%s}",
                        label, color, markerStyle.pointStyle());
            } else {
                return String.format("LegendEntry{label='%s', color='%s', type=%s}",
                        label, color, symbolType);
            }
        }
    }

    public static class Builder {
        private String id;
        private Position position = Position.TOP_RIGHT;
        private final List<LegendEntry> entries = new ArrayList<>();
        private boolean showBorder = true;
        private int columns = 1;
        private String fontFamily = "Arial";
        private int fontSize = 12; // gnuplot default: 12
        private String borderColor = "#000000";
        private String backgroundColor = "#FFFFFF";

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

        public Builder columns(int columns) {
            if (columns < 1) {
                throw new IllegalArgumentException("columns must be positive, got " + columns);
            }
            this.columns = columns;
            return this;
        }

        public Builder fontFamily(String fontFamily) {
            this.fontFamily = Objects.requireNonNull(fontFamily, "fontFamily cannot be null");
            return this;
        }

        public Builder fontSize(int fontSize) {
            if (fontSize < 6 || fontSize > 72) {
                throw new IllegalArgumentException("fontSize must be between 6 and 72, got " + fontSize);
            }
            this.fontSize = fontSize;
            return this;
        }

        public Builder borderColor(String borderColor) {
            this.borderColor = Objects.requireNonNull(borderColor, "borderColor cannot be null");
            return this;
        }

        public Builder backgroundColor(String backgroundColor) {
            this.backgroundColor = Objects.requireNonNull(backgroundColor, "backgroundColor cannot be null");
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
        return String.format("Legend{id='%s', position=%s, entries=%d, border=%s, columns=%d}",
                id, position, entries.size(), showBorder, columns);
    }
}
