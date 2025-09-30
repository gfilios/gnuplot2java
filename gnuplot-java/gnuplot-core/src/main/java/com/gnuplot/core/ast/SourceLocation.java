package com.gnuplot.core.ast;

/**
 * Represents a location in the source code.
 * Used for error reporting and debugging.
 */
public record SourceLocation(
        int line,
        int column,
        int startIndex,
        int endIndex
) {
    public static SourceLocation UNKNOWN = new SourceLocation(-1, -1, -1, -1);

    @Override
    public String toString() {
        if (line < 0) {
            return "unknown location";
        }
        return String.format("line %d, column %d", line, column);
    }
}