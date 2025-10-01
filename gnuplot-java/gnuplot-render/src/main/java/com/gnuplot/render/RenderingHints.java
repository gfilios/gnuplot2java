package com.gnuplot.render;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Provides hints to the renderer about quality, performance, and style preferences.
 * Hints are optional suggestions; renderers may ignore unsupported hints.
 *
 * @since 1.0
 */
public final class RenderingHints {
    /**
     * Default empty rendering hints.
     */
    public static final RenderingHints DEFAULT = RenderingHints.empty();

    private final Map<Key<?>, Object> hints;

    private RenderingHints(Builder builder) {
        this.hints = new HashMap<>(builder.hints);
    }

    /**
     * Gets a hint value by key.
     *
     * @param key the hint key
     * @param <T> the hint value type
     * @return Optional containing the value if present
     */
    @SuppressWarnings("unchecked")
    public <T> Optional<T> get(Key<T> key) {
        return Optional.ofNullable((T) hints.get(key));
    }

    /**
     * Gets a hint value with a default fallback.
     *
     * @param key the hint key
     * @param defaultValue default value if hint not set
     * @param <T> the hint value type
     * @return the hint value or default
     */
    public <T> T getOrDefault(Key<T> key, T defaultValue) {
        return get(key).orElse(defaultValue);
    }

    /**
     * Checks if a hint is set.
     *
     * @param key the hint key
     * @return true if hint is set
     */
    public boolean has(Key<?> key) {
        return hints.containsKey(key);
    }

    public static Builder builder() {
        return new Builder();
    }

    /**
     * Creates empty rendering hints.
     *
     * @return empty hints
     */
    public static RenderingHints empty() {
        return new Builder().build();
    }

    /**
     * Type-safe key for rendering hints.
     *
     * @param <T> the value type for this key
     */
    public static final class Key<T> {
        private final String name;
        private final Class<T> type;

        private Key(String name, Class<T> type) {
            this.name = name;
            this.type = type;
        }

        public static <T> Key<T> of(String name, Class<T> type) {
            return new Key<>(name, type);
        }

        public String getName() {
            return name;
        }

        public Class<T> getType() {
            return type;
        }

        @Override
        public String toString() {
            return name;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (!(obj instanceof Key)) return false;
            Key<?> other = (Key<?>) obj;
            return name.equals(other.name) && type.equals(other.type);
        }

        @Override
        public int hashCode() {
            return 31 * name.hashCode() + type.hashCode();
        }
    }

    /**
     * Standard rendering hint keys.
     */
    public static final class Keys {
        private Keys() {
        }

        /**
         * Antialiasing quality (Boolean).
         */
        public static final Key<Boolean> ANTIALIASING = Key.of("antialiasing", Boolean.class);

        /**
         * Rendering quality level: "low", "medium", "high" (String).
         */
        public static final Key<String> QUALITY = Key.of("quality", String.class);

        /**
         * DPI for raster output (Integer).
         */
        public static final Key<Integer> DPI = Key.of("dpi", Integer.class);

        /**
         * Background color as hex string (String).
         */
        public static final Key<String> BACKGROUND_COLOR = Key.of("backgroundColor", String.class);

        /**
         * Enable transparency (Boolean).
         */
        public static final Key<Boolean> TRANSPARENCY = Key.of("transparency", Boolean.class);

        /**
         * Font family name (String).
         */
        public static final Key<String> FONT_FAMILY = Key.of("fontFamily", String.class);

        /**
         * Default font size in points (Integer).
         */
        public static final Key<Integer> FONT_SIZE = Key.of("fontSize", Integer.class);

        /**
         * Line width scaling factor (Double).
         */
        public static final Key<Double> LINE_WIDTH_SCALE = Key.of("lineWidthScale", Double.class);

        /**
         * Enable grid lines (Boolean).
         */
        public static final Key<Boolean> GRID_ENABLED = Key.of("gridEnabled", Boolean.class);

        /**
         * Enable legend (Boolean).
         */
        public static final Key<Boolean> LEGEND_ENABLED = Key.of("legendEnabled", Boolean.class);

        /**
         * Animation frame rate for animated outputs (Integer).
         */
        public static final Key<Integer> ANIMATION_FPS = Key.of("animationFps", Integer.class);

        /**
         * Compression level for outputs that support it: 0-9 (Integer).
         */
        public static final Key<Integer> COMPRESSION_LEVEL = Key.of("compressionLevel", Integer.class);
    }

    public static class Builder {
        private final Map<Key<?>, Object> hints = new HashMap<>();

        private Builder() {
        }

        public <T> Builder set(Key<T> key, T value) {
            if (value == null) {
                hints.remove(key);
            } else {
                hints.put(key, value);
            }
            return this;
        }

        public Builder antialiasing(boolean enabled) {
            return set(Keys.ANTIALIASING, enabled);
        }

        public Builder quality(String quality) {
            return set(Keys.QUALITY, quality);
        }

        public Builder dpi(int dpi) {
            return set(Keys.DPI, dpi);
        }

        public Builder backgroundColor(String color) {
            return set(Keys.BACKGROUND_COLOR, color);
        }

        public Builder transparency(boolean enabled) {
            return set(Keys.TRANSPARENCY, enabled);
        }

        public Builder fontFamily(String family) {
            return set(Keys.FONT_FAMILY, family);
        }

        public Builder fontSize(int size) {
            return set(Keys.FONT_SIZE, size);
        }

        public Builder lineWidthScale(double scale) {
            return set(Keys.LINE_WIDTH_SCALE, scale);
        }

        public Builder gridEnabled(boolean enabled) {
            return set(Keys.GRID_ENABLED, enabled);
        }

        public Builder legendEnabled(boolean enabled) {
            return set(Keys.LEGEND_ENABLED, enabled);
        }

        public Builder animationFps(int fps) {
            return set(Keys.ANIMATION_FPS, fps);
        }

        public Builder compressionLevel(int level) {
            return set(Keys.COMPRESSION_LEVEL, level);
        }

        public RenderingHints build() {
            return new RenderingHints(this);
        }
    }
}
