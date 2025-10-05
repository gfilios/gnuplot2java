package com.gnuplot.render;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Format-agnostic intermediate representation of a complete plot scene.
 *
 * <p>A Scene is the central data structure in the rendering pipeline. It contains
 * all the visual elements needed to render a plot in any output format.
 *
 * <p>The Scene acts as an intermediate layer between the data/math layer and
 * the format-specific renderers, allowing the same scene to be rendered to SVG,
 * PNG, PDF, or any other format.
 *
 * <p>Example:
 * <pre>{@code
 * Scene scene = Scene.builder()
 *     .dimensions(800, 600)
 *     .title("My Plot")
 *     .addElement(linePlot)
 *     .addElement(xAxis)
 *     .addElement(yAxis)
 *     .build();
 * }</pre>
 *
 * @since 1.0
 */
public final class Scene {

    private final int width;
    private final int height;
    private final String title;
    private final Viewport viewport;
    private final List<SceneElement> elements;
    private final RenderingHints hints;
    private final boolean showBorder;

    private Scene(Builder builder) {
        this.width = builder.width;
        this.height = builder.height;
        this.title = builder.title;
        this.viewport = builder.viewport;
        this.elements = Collections.unmodifiableList(new ArrayList<>(builder.elements));
        this.hints = builder.hints != null ? builder.hints : RenderingHints.empty();
        this.showBorder = builder.showBorder;
    }

    public static Builder builder() {
        return new Builder();
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public String getTitle() {
        return title;
    }

    public Viewport getViewport() {
        return viewport;
    }

    public List<SceneElement> getElements() {
        return elements;
    }

    public RenderingHints getHints() {
        return hints;
    }

    public boolean isShowBorder() {
        return showBorder;
    }

    /**
     * Returns the aspect ratio (width / height).
     *
     * @return aspect ratio
     */
    public double getAspectRatio() {
        return (double) width / height;
    }

    public static class Builder {
        private int width = 800;
        private int height = 600;
        private String title;
        private Viewport viewport;
        private final List<SceneElement> elements = new ArrayList<>();
        private RenderingHints hints;
        private boolean showBorder = true; // Default: true (matching C Gnuplot)

        public Builder dimensions(int width, int height) {
            if (width <= 0 || height <= 0) {
                throw new IllegalArgumentException(
                        "Dimensions must be positive: " + width + "x" + height);
            }
            this.width = width;
            this.height = height;
            return this;
        }

        public Builder title(String title) {
            this.title = Objects.requireNonNull(title, "title cannot be null");
            return this;
        }

        public Builder viewport(Viewport viewport) {
            this.viewport = Objects.requireNonNull(viewport, "viewport cannot be null");
            return this;
        }

        public Builder addElement(SceneElement element) {
            this.elements.add(Objects.requireNonNull(element, "element cannot be null"));
            return this;
        }

        public Builder elements(List<SceneElement> elements) {
            this.elements.clear();
            this.elements.addAll(Objects.requireNonNull(elements, "elements cannot be null"));
            return this;
        }

        public Builder hints(RenderingHints hints) {
            this.hints = Objects.requireNonNull(hints, "hints cannot be null");
            return this;
        }

        public Builder border(boolean showBorder) {
            this.showBorder = showBorder;
            return this;
        }

        public Scene build() {
            return new Scene(this);
        }
    }

    @Override
    public String toString() {
        return String.format("Scene{%dx%d, title='%s', elements=%d}",
                width, height, title, elements.size());
    }
}
