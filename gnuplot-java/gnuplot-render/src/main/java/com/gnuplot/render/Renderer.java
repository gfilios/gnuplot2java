package com.gnuplot.render;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Base interface for all renderers in the gnuplot rendering system.
 *
 * <p>A renderer is responsible for converting a {@link Scene} (format-agnostic
 * intermediate representation) into a specific output format (SVG, PNG, PDF, etc.).
 *
 * <p>Renderers are pluggable - new output formats can be added by implementing
 * this interface and registering with the {@link RendererRegistry}.
 *
 * <p>Example usage:
 * <pre>{@code
 * Renderer svgRenderer = RendererRegistry.getRenderer("svg");
 * Scene scene = SceneBuilder.create()
 *     .addPlot(plot)
 *     .build();
 * svgRenderer.render(scene, outputStream);
 * }</pre>
 *
 * @since 1.0
 */
public interface Renderer {

    /**
     * Renders a scene to the specified output stream.
     *
     * @param scene the scene to render
     * @param output the output stream to write to
     * @throws IOException if an I/O error occurs
     * @throws RenderException if rendering fails
     */
    void render(Scene scene, OutputStream output) throws IOException, RenderException;

    /**
     * Returns the output format name (e.g., "svg", "png", "pdf").
     *
     * @return format name in lowercase
     */
    String getFormatName();

    /**
     * Returns the MIME type for this format (e.g., "image/svg+xml").
     *
     * @return MIME type
     */
    String getMimeType();

    /**
     * Returns the file extension for this format (e.g., "svg", "png").
     *
     * @return file extension without dot
     */
    String getFileExtension();

    /**
     * Returns the capabilities of this renderer.
     *
     * @return renderer capabilities
     */
    RendererCapabilities getCapabilities();
}
