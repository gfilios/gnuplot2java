package com.gnuplot.core.data.csv;

import com.gnuplot.core.data.DataMetadata;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Metadata implementation for CSV data sources.
 *
 * @since 1.0
 */
final class CsvMetadata implements DataMetadata {

    private final String sourceIdentifier;
    private final List<String> columnNames;
    private final Map<String, Integer> columnIndex;
    private final boolean hasHeader;

    CsvMetadata(String sourceIdentifier, String headerLine, CsvConfig config) {
        this.sourceIdentifier = sourceIdentifier;
        this.hasHeader = config.hasHeader() && headerLine != null;

        if (this.hasHeader) {
            CsvParser parser = new CsvParser(config);
            List<String> fields = parser.parseFields(headerLine);
            this.columnNames = Collections.unmodifiableList(fields);
            this.columnIndex = buildColumnIndex(fields);
        } else {
            this.columnNames = Collections.emptyList();
            this.columnIndex = Collections.emptyMap();
        }
    }

    private Map<String, Integer> buildColumnIndex(List<String> names) {
        Map<String, Integer> index = new HashMap<>();
        for (int i = 0; i < names.size(); i++) {
            index.put(names.get(i), i);
        }
        return Collections.unmodifiableMap(index);
    }

    @Override
    public int getColumnCount() {
        return columnNames.size();
    }

    @Override
    public List<String> getColumnNames() {
        return columnNames;
    }

    @Override
    public Optional<String> getColumnName(int index) {
        if (index >= 0 && index < columnNames.size()) {
            return Optional.of(columnNames.get(index));
        }
        return Optional.empty();
    }

    @Override
    public Optional<Integer> getColumnIndex(String columnName) {
        return Optional.ofNullable(columnIndex.get(columnName));
    }

    @Override
    public boolean hasHeader() {
        return hasHeader;
    }

    @Override
    public Optional<Long> getRecordCount() {
        // Record count is unknown for streaming sources
        return Optional.empty();
    }

    @Override
    public String getSourceIdentifier() {
        return sourceIdentifier;
    }

    @Override
    public Optional<String> getProperty(String key) {
        // No additional properties for CSV
        return Optional.empty();
    }
}
