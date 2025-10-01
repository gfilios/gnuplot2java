package com.gnuplot.core.data.csv;

/**
 * Configuration options for CSV parsing.
 * Immutable configuration object with builder pattern.
 *
 * @since 1.0
 */
public final class CsvConfig {

    private final char delimiter;
    private final char quoteChar;
    private final char escapeChar;
    private final boolean hasHeader;
    private final boolean skipEmptyLines;
    private final boolean trimFields;
    private final int skipLines;
    private final String commentPrefix;

    private CsvConfig(Builder builder) {
        this.delimiter = builder.delimiter;
        this.quoteChar = builder.quoteChar;
        this.escapeChar = builder.escapeChar;
        this.hasHeader = builder.hasHeader;
        this.skipEmptyLines = builder.skipEmptyLines;
        this.trimFields = builder.trimFields;
        this.skipLines = builder.skipLines;
        this.commentPrefix = builder.commentPrefix;
    }

    /**
     * Returns the default CSV configuration.
     * Delimiter: comma, Quote: double-quote, Header: true.
     *
     * @return default configuration
     */
    public static CsvConfig defaults() {
        return builder().build();
    }

    /**
     * Creates a new builder for CSV configuration.
     *
     * @return new builder
     */
    public static Builder builder() {
        return new Builder();
    }

    public char getDelimiter() {
        return delimiter;
    }

    public char getQuoteChar() {
        return quoteChar;
    }

    public char getEscapeChar() {
        return escapeChar;
    }

    public boolean hasHeader() {
        return hasHeader;
    }

    public boolean isSkipEmptyLines() {
        return skipEmptyLines;
    }

    public boolean isTrimFields() {
        return trimFields;
    }

    public int getSkipLines() {
        return skipLines;
    }

    public String getCommentPrefix() {
        return commentPrefix;
    }

    /**
     * Builder for CsvConfig.
     */
    public static final class Builder {
        private char delimiter = ',';
        private char quoteChar = '"';
        private char escapeChar = '\\';
        private boolean hasHeader = true;
        private boolean skipEmptyLines = true;
        private boolean trimFields = false;
        private int skipLines = 0;
        private String commentPrefix = "#";

        private Builder() {
        }

        public Builder delimiter(char delimiter) {
            this.delimiter = delimiter;
            return this;
        }

        public Builder quoteChar(char quoteChar) {
            this.quoteChar = quoteChar;
            return this;
        }

        public Builder escapeChar(char escapeChar) {
            this.escapeChar = escapeChar;
            return this;
        }

        public Builder hasHeader(boolean hasHeader) {
            this.hasHeader = hasHeader;
            return this;
        }

        public Builder skipEmptyLines(boolean skipEmptyLines) {
            this.skipEmptyLines = skipEmptyLines;
            return this;
        }

        public Builder trimFields(boolean trimFields) {
            this.trimFields = trimFields;
            return this;
        }

        public Builder skipLines(int skipLines) {
            if (skipLines < 0) {
                throw new IllegalArgumentException("skipLines must be non-negative");
            }
            this.skipLines = skipLines;
            return this;
        }

        public Builder commentPrefix(String commentPrefix) {
            this.commentPrefix = commentPrefix;
            return this;
        }

        public CsvConfig build() {
            return new CsvConfig(this);
        }
    }
}
