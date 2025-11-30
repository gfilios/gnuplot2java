package com.gnuplot.render.elements;

import com.gnuplot.render.SceneElement;
import com.gnuplot.render.SceneElementVisitor;
import com.gnuplot.render.style.MarkerStyle;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents a 3D surface plot or point cloud in the scene graph.
 * Can render as:
 * - Point cloud (data style points)
 * - Wireframe/mesh (data style lines)
 * - Solid surface (data style surface)
 *
 * @since 1.0
 */
public final class SurfacePlot3D implements SceneElement {

    private final String id;
    private final List<Point3D> points;
    private final String color;
    private final String label;
    private final PlotStyle3D plotStyle;
    private final MarkerStyle markerStyle;  // For point rendering
    private final int gridRows;  // Grid rows for wireframe rendering (0 = not a grid)
    private final int gridCols;  // Grid columns for wireframe rendering (0 = not a grid)

    private SurfacePlot3D(Builder builder) {
        this.id = builder.id;
        this.points = Collections.unmodifiableList(new ArrayList<>(builder.points));
        this.color = builder.color;
        this.label = builder.label;
        this.plotStyle = builder.plotStyle;
        this.markerStyle = builder.markerStyle;
        this.gridRows = builder.gridRows;
        this.gridCols = builder.gridCols;
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
        visitor.visitSurfacePlot3D(this);
    }

    public String getId() {
        return id;
    }

    public List<Point3D> getPoints() {
        return points;
    }

    public String getColor() {
        return color;
    }

    public String getLabel() {
        return label;
    }

    public PlotStyle3D getPlotStyle() {
        return plotStyle;
    }

    public MarkerStyle getMarkerStyle() {
        return markerStyle;
    }

    public int getGridRows() {
        return gridRows;
    }

    public int getGridCols() {
        return gridCols;
    }

    /**
     * Returns true if this plot has grid structure (for wireframe mesh rendering).
     */
    public boolean hasGridStructure() {
        return gridRows > 0 && gridCols > 0;
    }

    /**
     * 3D plot styles.
     */
    public enum PlotStyle3D {
        POINTS,      // Scatter plot (points only)
        LINES,       // Wireframe/mesh
        SURFACE,     // Solid surface
        DOTS         // Tiny dots
    }

    /**
     * Builder for SurfacePlot3D.
     */
    public static class Builder {
        private String id = "surface";
        private List<Point3D> points = new ArrayList<>();
        private String color = "#0000FF";
        private String label = "";
        private PlotStyle3D plotStyle = PlotStyle3D.POINTS;
        private MarkerStyle markerStyle = null;
        private int gridRows = 0;  // 0 means not a grid
        private int gridCols = 0;

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder addPoint(Point3D point) {
            this.points.add(point);
            return this;
        }

        public Builder addPoint(double x, double y, double z) {
            this.points.add(new Point3D(x, y, z));
            return this;
        }

        public Builder points(List<Point3D> points) {
            this.points = new ArrayList<>(points);
            return this;
        }

        public Builder color(String color) {
            this.color = color;
            return this;
        }

        public Builder label(String label) {
            this.label = label;
            return this;
        }

        public Builder plotStyle(PlotStyle3D plotStyle) {
            this.plotStyle = plotStyle;
            return this;
        }

        public Builder markerStyle(MarkerStyle markerStyle) {
            this.markerStyle = markerStyle;
            return this;
        }

        public Builder gridDimensions(int rows, int cols) {
            this.gridRows = rows;
            this.gridCols = cols;
            return this;
        }

        public SurfacePlot3D build() {
            return new SurfacePlot3D(this);
        }
    }
}
