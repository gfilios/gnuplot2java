package com.gnuplot.core.data.json;

import com.fasterxml.jackson.databind.JsonNode;
import com.gnuplot.core.data.DataMetadata;

import java.util.*;

/**
 * Metadata implementation for JSON data sources.
 *
 * @since 1.0
 */
final class JsonMetadata implements DataMetadata {

    private final String sourceIdentifier;
    private final List<String> columnNames;
    private final Map<String, Integer> columnIndex;
    private final long recordCount;

    JsonMetadata(String sourceIdentifier, List<JsonNode> records, JsonConfig config) {
        this.sourceIdentifier = sourceIdentifier;
        this.recordCount = records.size();

        // Extract column names from first record
        if (!records.isEmpty()) {
            JsonNode firstRecord = records.get(0);
            this.columnNames = extractColumnNames(firstRecord);
            this.columnIndex = buildColumnIndex(columnNames);
        } else {
            this.columnNames = Collections.emptyList();
            this.columnIndex = Collections.emptyMap();
        }
    }

    private List<String> extractColumnNames(JsonNode record) {
        List<String> names = new ArrayList<>();

        if (record.isObject()) {
            // Object with named fields
            Iterator<String> fieldNames = record.fieldNames();
            while (fieldNames.hasNext()) {
                names.add(fieldNames.next());
            }
        } else if (record.isArray()) {
            // Array - use indices as column names
            for (int i = 0; i < record.size(); i++) {
                names.add(String.valueOf(i));
            }
        }

        return Collections.unmodifiableList(names);
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
        return !columnNames.isEmpty();
    }

    @Override
    public Optional<Long> getRecordCount() {
        return Optional.of(recordCount);
    }

    @Override
    public String getSourceIdentifier() {
        return sourceIdentifier;
    }

    @Override
    public Optional<String> getProperty(String key) {
        return Optional.empty();
    }
}
