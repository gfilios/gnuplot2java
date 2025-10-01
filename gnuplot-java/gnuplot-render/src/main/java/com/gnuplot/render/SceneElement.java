package com.gnuplot.render;

/**
 * Base interface for all visual elements in a scene (plots, axes, labels, etc.).
 *
 * @since 1.0
 */
public interface SceneElement {

    /**
     * Returns the type of this scene element.
     *
     * @return element type
     */
    ElementType getType();

    /**
     * Accepts a visitor for rendering or processing.
     *
     * @param visitor the visitor
     */
    void accept(SceneElementVisitor visitor);

    /**
     * Types of scene elements.
     */
    enum ElementType {
        PLOT, AXIS, LABEL, LEGEND, GRID, TITLE
    }
}
