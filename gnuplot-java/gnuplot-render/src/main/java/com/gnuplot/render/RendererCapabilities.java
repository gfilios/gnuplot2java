package com.gnuplot.render;

/**
 * Describes the capabilities of a renderer.
 *
 * <p>Different renderers support different features (e.g., transparency, 3D, animation).
 * This class allows querying what a renderer can do.
 *
 * @since 1.0
 */
public final class RendererCapabilities {

    private final boolean supportsTransparency;
    private final boolean supports3D;
    private final boolean supportsAnimation;
    private final boolean supportsInteractivity;
    private final boolean supportsVectorGraphics;

    private RendererCapabilities(Builder builder) {
        this.supportsTransparency = builder.supportsTransparency;
        this.supports3D = builder.supports3D;
        this.supportsAnimation = builder.supportsAnimation;
        this.supportsInteractivity = builder.supportsInteractivity;
        this.supportsVectorGraphics = builder.supportsVectorGraphics;
    }

    public static Builder builder() {
        return new Builder();
    }

    public boolean supportsTransparency() {
        return supportsTransparency;
    }

    public boolean supports3D() {
        return supports3D;
    }

    public boolean supportsAnimation() {
        return supportsAnimation;
    }

    public boolean supportsInteractivity() {
        return supportsInteractivity;
    }

    public boolean supportsVectorGraphics() {
        return supportsVectorGraphics;
    }

    public static class Builder {
        private boolean supportsTransparency = false;
        private boolean supports3D = false;
        private boolean supportsAnimation = false;
        private boolean supportsInteractivity = false;
        private boolean supportsVectorGraphics = false;

        public Builder transparency(boolean supports) {
            this.supportsTransparency = supports;
            return this;
        }

        public Builder threeD(boolean supports) {
            this.supports3D = supports;
            return this;
        }

        public Builder supports3D(boolean supports) {
            this.supports3D = supports;
            return this;
        }

        public Builder animation(boolean supports) {
            this.supportsAnimation = supports;
            return this;
        }

        public Builder interactivity(boolean supports) {
            this.supportsInteractivity = supports;
            return this;
        }

        public Builder vectorGraphics(boolean supports) {
            this.supportsVectorGraphics = supports;
            return this;
        }

        public RendererCapabilities build() {
            return new RendererCapabilities(this);
        }
    }

    @Override
    public String toString() {
        return String.format("RendererCapabilities{transparency=%s, 3D=%s, animation=%s, " +
                        "interactivity=%s, vector=%s}",
                supportsTransparency, supports3D, supportsAnimation,
                supportsInteractivity, supportsVectorGraphics);
    }
}
