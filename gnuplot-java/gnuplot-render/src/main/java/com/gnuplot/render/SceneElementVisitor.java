package com.gnuplot.render;

import com.gnuplot.render.elements.Axis;
import com.gnuplot.render.elements.BarChart;
import com.gnuplot.render.elements.ContourPlot3D;
import com.gnuplot.render.elements.Legend;
import com.gnuplot.render.elements.LinePlot;
import com.gnuplot.render.elements.ScatterPlot;
import com.gnuplot.render.elements.SurfacePlot3D;

/**
 * Visitor pattern interface for processing scene elements.
 * Renderers implement this interface to handle different element types.
 *
 * @since 1.0
 */
public interface SceneElementVisitor {

    /**
     * Visit a line plot element.
     *
     * @param linePlot the line plot to visit
     */
    void visitLinePlot(LinePlot linePlot);

    /**
     * Visit a scatter plot element.
     *
     * @param scatterPlot the scatter plot to visit
     */
    void visitScatterPlot(ScatterPlot scatterPlot);

    /**
     * Visit an axis element.
     *
     * @param axis the axis to visit
     */
    void visitAxis(Axis axis);

    /**
     * Visit a legend element.
     *
     * @param legend the legend to visit
     */
    void visitLegend(Legend legend);

    /**
     * Visit a bar chart element.
     *
     * @param barChart the bar chart to visit
     */
    void visitBarChart(BarChart barChart);

    /**
     * Visit a 3D surface plot element.
     *
     * @param surfacePlot the 3D surface plot to visit
     */
    void visitSurfacePlot3D(SurfacePlot3D surfacePlot);

    /**
     * Visit a 3D contour plot element.
     *
     * @param contourPlot the 3D contour plot to visit
     */
    void visitContourPlot3D(ContourPlot3D contourPlot);
}
