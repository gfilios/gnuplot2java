/*
 * Gnuplot Java - Contour Plot 3D Scene Element
 * Port of gnuplot-c/src/graph3d.c contour rendering
 */
package com.gnuplot.render.elements;

import com.gnuplot.render.SceneElement;
import com.gnuplot.render.SceneElementVisitor;
import com.gnuplot.core.grid.ContourLine;
import com.gnuplot.core.grid.ContourParams;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents contour lines in a 3D surface plot.
 * <p>
 * Contour lines can be drawn on the base plane (projected at z=base),
 * on the surface (at their actual z-level), or both.
 * </p>
 *
 * @see <a href="file:///gnuplot-c/src/graph3d.c">graph3d.c:cntr3d_lines()</a>
 * @since 1.0
 */
public final class ContourPlot3D implements SceneElement {

    private final String id;
    private final List<ContourLine> contourLines;
    private final ContourParams.ContourPlace place;
    private final String color;
    private final boolean showLabels;
    private final double baseZ;  // z-value for base plane projection

    private ContourPlot3D(Builder builder) {
        this.id = builder.id;
        this.contourLines = Collections.unmodifiableList(new ArrayList<>(builder.contourLines));
        this.place = builder.place;
        this.color = builder.color;
        this.showLabels = builder.showLabels;
        this.baseZ = builder.baseZ;
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
        visitor.visitContourPlot3D(this);
    }

    public String getId() {
        return id;
    }

    public List<ContourLine> getContourLines() {
        return contourLines;
    }

    public ContourParams.ContourPlace getPlace() {
        return place;
    }

    public String getColor() {
        return color;
    }

    public boolean isShowLabels() {
        return showLabels;
    }

    public double getBaseZ() {
        return baseZ;
    }

    /**
     * Returns true if contours should be drawn on the base plane.
     */
    public boolean shouldDrawOnBase() {
        return place == ContourParams.ContourPlace.BASE ||
               place == ContourParams.ContourPlace.BOTH;
    }

    /**
     * Returns true if contours should be drawn on the surface.
     */
    public boolean shouldDrawOnSurface() {
        return place == ContourParams.ContourPlace.SURFACE ||
               place == ContourParams.ContourPlace.BOTH;
    }

    /**
     * Builder for ContourPlot3D.
     */
    public static class Builder {
        private String id = "contour";
        private List<ContourLine> contourLines = new ArrayList<>();
        private ContourParams.ContourPlace place = ContourParams.ContourPlace.BASE;
        private String color = "#000000";
        private boolean showLabels = false;
        private double baseZ = 0;

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder contourLines(List<ContourLine> contourLines) {
            this.contourLines = new ArrayList<>(contourLines);
            return this;
        }

        public Builder addContourLine(ContourLine line) {
            this.contourLines.add(line);
            return this;
        }

        public Builder place(ContourParams.ContourPlace place) {
            this.place = place;
            return this;
        }

        public Builder color(String color) {
            this.color = color;
            return this;
        }

        public Builder showLabels(boolean showLabels) {
            this.showLabels = showLabels;
            return this;
        }

        public Builder baseZ(double baseZ) {
            this.baseZ = baseZ;
            return this;
        }

        public ContourPlot3D build() {
            return new ContourPlot3D(this);
        }
    }
}
