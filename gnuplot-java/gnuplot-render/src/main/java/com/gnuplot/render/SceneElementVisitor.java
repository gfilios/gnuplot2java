package com.gnuplot.render;

import com.gnuplot.render.elements.Axis;
import com.gnuplot.render.elements.Legend;
import com.gnuplot.render.elements.LinePlot;

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
}
