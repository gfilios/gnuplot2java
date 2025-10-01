package com.gnuplot.core.data.csv;

import com.gnuplot.core.data.DataRecord;

import java.util.ArrayList;
import java.util.List;

/**
 * Low-level CSV parser handling quoted fields, escape sequences, and delimiters.
 * Implements RFC 4180 with extensions for escape characters.
 *
 * @since 1.0
 */
final class CsvParser {

    private final CsvConfig config;

    CsvParser(CsvConfig config) {
        this.config = config;
    }

    /**
     * Parses a CSV line into fields.
     *
     * @param line line to parse
     * @return list of field values
     */
    List<String> parseFields(String line) {
        List<String> fields = new ArrayList<>();
        if (line == null || line.isEmpty()) {
            return fields;
        }

        StringBuilder currentField = new StringBuilder();
        boolean inQuotes = false;
        boolean escapeNext = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);

            if (escapeNext) {
                currentField.append(c);
                escapeNext = false;
                continue;
            }

            if (c == config.getEscapeChar()) {
                escapeNext = true;
                continue;
            }

            if (c == config.getQuoteChar()) {
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == config.getQuoteChar()) {
                    // Double quote - treat as escaped quote
                    currentField.append(config.getQuoteChar());
                    i++; // Skip next quote
                } else {
                    // Toggle quote state
                    inQuotes = !inQuotes;
                }
                continue;
            }

            if (c == config.getDelimiter() && !inQuotes) {
                // End of field
                fields.add(processField(currentField.toString()));
                currentField.setLength(0);
                continue;
            }

            currentField.append(c);
        }

        // Add last field
        fields.add(processField(currentField.toString()));

        return fields;
    }

    /**
     * Parses a CSV line into a DataRecord.
     *
     * @param line       line to parse
     * @param metadata   CSV metadata
     * @param lineNumber line number for error reporting
     * @return parsed data record
     */
    DataRecord parseLine(String line, CsvMetadata metadata, long lineNumber) {
        List<String> fields = parseFields(line);
        return new CsvRecord(fields, metadata, lineNumber);
    }

    private String processField(String field) {
        if (config.isTrimFields()) {
            field = field.trim();
        }
        return field.isEmpty() ? null : field;
    }
}
