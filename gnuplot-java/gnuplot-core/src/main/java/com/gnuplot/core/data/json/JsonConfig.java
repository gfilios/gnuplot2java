package com.gnuplot.core.data.json;

/**
 * Configuration options for JSON data reading.
 * Immutable configuration object with builder pattern.
 *
 * @since 1.0
 */
public final class JsonConfig {

    private final String dataPath;
    private final boolean flattenArrays;
    private final String arrayName;

    private JsonConfig(Builder builder) {
        this.dataPath = builder.dataPath;
        this.flattenArrays = builder.flattenArrays;
        this.arrayName = builder.arrayName;
    }

    /**
     * Returns the default JSON configuration.
     * Root data path, flatten arrays enabled.
     *
     * @return default configuration
     */
    public static JsonConfig defaults() {
        return builder().build();
    }

    /**
     * Creates a new builder for JSON configuration.
     *
     * @return new builder
     */
    public static Builder builder() {
        return new Builder();
    }

    public String getDataPath() {
        return dataPath;
    }

    public boolean isFlattenArrays() {
        return flattenArrays;
    }

    public String getArrayName() {
        return arrayName;
    }

    /**
     * Builder for JsonConfig.
     */
    public static final class Builder {
        private String dataPath = "$";
        private boolean flattenArrays = true;
        private String arrayName = "data";

        private Builder() {
        }

        /**
         * Sets the JSONPath expression to extract data from the JSON document.
         * Default is "$" (root).
         *
         * @param dataPath JSONPath expression
         * @return this builder
         */
        public Builder dataPath(String dataPath) {
            this.dataPath = dataPath;
            return this;
        }

        /**
         * Sets whether to flatten array-of-arrays into records.
         * Default is true.
         *
         * @param flattenArrays true to flatten arrays
         * @return this builder
         */
        public Builder flattenArrays(boolean flattenArrays) {
            this.flattenArrays = flattenArrays;
            return this;
        }

        /**
         * Sets the name to use for the data array when extracting from objects.
         * Default is "data".
         *
         * @param arrayName array property name
         * @return this builder
         */
        public Builder arrayName(String arrayName) {
            this.arrayName = arrayName;
            return this;
        }

        public JsonConfig build() {
            return new JsonConfig(this);
        }
    }
}
