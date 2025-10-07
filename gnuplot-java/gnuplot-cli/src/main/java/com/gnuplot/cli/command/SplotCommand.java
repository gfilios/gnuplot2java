package com.gnuplot.cli.command;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Represents an SPLOT (3D surface plot) command in Gnuplot.
 */
public final class SplotCommand implements Command {
    private final List<PlotCommand.PlotSpec> plotSpecs;
    private final PlotCommand.Range xRange;
    private final PlotCommand.Range yRange;
    private final PlotCommand.Range zRange;

    public SplotCommand(List<PlotCommand.PlotSpec> plotSpecs) {
        this(plotSpecs, null, null, null);
    }

    public SplotCommand(List<PlotCommand.PlotSpec> plotSpecs, PlotCommand.Range xRange,
                        PlotCommand.Range yRange, PlotCommand.Range zRange) {
        this.plotSpecs = Collections.unmodifiableList(Objects.requireNonNull(plotSpecs, "plotSpecs cannot be null"));
        this.xRange = xRange;
        this.yRange = yRange;
        this.zRange = zRange;
    }

    public List<PlotCommand.PlotSpec> getPlotSpecs() {
        return plotSpecs;
    }

    public PlotCommand.Range getXRange() {
        return xRange;
    }

    public PlotCommand.Range getYRange() {
        return yRange;
    }

    public PlotCommand.Range getZRange() {
        return zRange;
    }

    @Override
    public void accept(CommandVisitor visitor) {
        visitor.visitSplotCommand(this);
    }

    @Override
    public String toString() {
        return String.format("SPLOT %s%s%s %s",
            xRange != null ? xRange + " " : "",
            yRange != null ? yRange + " " : "",
            zRange != null ? zRange + " " : "",
            plotSpecs);
    }
}
