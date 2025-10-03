package com.gnuplot.cli.command;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Represents a PLOT command in Gnuplot.
 */
public final class PlotCommand implements Command {
    private final List<PlotSpec> plotSpecs;

    public PlotCommand(List<PlotSpec> plotSpecs) {
        this.plotSpecs = Collections.unmodifiableList(Objects.requireNonNull(plotSpecs, "plotSpecs cannot be null"));
    }

    public List<PlotSpec> getPlotSpecs() {
        return plotSpecs;
    }

    @Override
    public void accept(CommandVisitor visitor) {
        visitor.visitPlotCommand(this);
    }

    @Override
    public String toString() {
        return String.format("PLOT %s", plotSpecs);
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
