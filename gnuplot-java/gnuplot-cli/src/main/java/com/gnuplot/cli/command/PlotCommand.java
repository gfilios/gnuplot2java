package com.gnuplot.cli.command;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Represents a PLOT command in Gnuplot.
 */
public final class PlotCommand implements Command {
    private final List<PlotSpec> plotSpecs;
    private final Range xRange;
    private final Range yRange;

    public PlotCommand(List<PlotSpec> plotSpecs) {
        this(plotSpecs, null, null);
    }

    public PlotCommand(List<PlotSpec> plotSpecs, Range xRange, Range yRange) {
        this.plotSpecs = Collections.unmodifiableList(Objects.requireNonNull(plotSpecs, "plotSpecs cannot be null"));
        this.xRange = xRange;
        this.yRange = yRange;
    }

    public List<PlotSpec> getPlotSpecs() {
        return plotSpecs;
    }

    public Range getXRange() {
        return xRange;
    }

    public Range getYRange() {
        return yRange;
    }

    @Override
    public void accept(CommandVisitor visitor) {
        visitor.visitPlotCommand(this);
    }

    @Override
    public String toString() {
        return String.format("PLOT %s%s %s",
            xRange != null ? xRange + " " : "",
            yRange != null ? yRange + " " : "",
            plotSpecs);
    }

    /**
     * Represents a range specification [min:max].
     */
    public static final class Range {
        private final Double min;  // null means auto (*)
        private final Double max;  // null means auto (*)

        public Range(Double min, Double max) {
            this.min = min;
            this.max = max;
        }

        public Double getMin() {
            return min;
        }

        public Double getMax() {
            return max;
        }

        @Override
        public String toString() {
            String minStr = min != null ? String.valueOf(min) : "*";
            String maxStr = max != null ? String.valueOf(max) : "*";
            return String.format("[%s:%s]", minStr, maxStr);
        }
    }

    /**
     * Represents a single plot specification within a plot command.
     */
    public static final class PlotSpec {
        private final String expression;
        private final String title;
        private final String style;

        public PlotSpec(String expression, String title, String style) {
            this.expression = Objects.requireNonNull(expression, "expression cannot be null");
            this.title = title;
            this.style = style;
        }

        public String getExpression() {
            return expression;
        }

        public String getTitle() {
            return title;
        }

        public String getStyle() {
            return style;
        }

        @Override
        public String toString() {
            return String.format("%s title '%s' with %s", expression, title, style);
        }
    }
}
