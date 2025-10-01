package com.gnuplot.core.data.csv;

import com.gnuplot.core.data.DataRecord;

import java.util.Collections;
import java.util.List;

/**
 * DataRecord implementation for CSV data.
 *
 * @since 1.0
 */
final class CsvRecord implements DataRecord {

    private final List<String> values;
    private final CsvMetadata metadata;
    private final long lineNumber;

    CsvRecord(List<String> values, CsvMetadata metadata, long lineNumber) {
        this.values = Collections.unmodifiableList(values);
        this.metadata = metadata;
        this.lineNumber = lineNumber;
    }

    @Override
    public Object getValue(int index) {
        validateIndex(index);
        return values.get(index);
    }

    @Override
    public Object getValue(String columnName) {
        int index = getColumnIndexOrThrow(columnName);
        return getValue(index);
    }

    @Override
    public double getDouble(int index) {
        Object value = getValue(index);
        if (value == null) {
            throw new NumberFormatException("Null value at index " + index);
        }
        try {
            return Double.parseDouble(value.toString());
        } catch (NumberFormatException e) {
            throw new NumberFormatException(
                    "Cannot convert value '" + value + "' at index " + index + " to double"
            );
        }
    }

    @Override
    public double getDouble(String columnName) {
        int index = getColumnIndexOrThrow(columnName);
        return getDouble(index);
    }

    @Override
    public String getString(int index) {
        Object value = getValue(index);
        return value == null ? null : value.toString();
    }

    @Override
    public String getString(String columnName) {
        int index = getColumnIndexOrThrow(columnName);
        return getString(index);
    }

    @Override
    public List<Object> getValues() {
        return Collections.unmodifiableList(values);
    }

    @Override
    public int size() {
        return values.size();
    }

    @Override
    public boolean isNull(int index) {
        validateIndex(index);
        return values.get(index) == null;
    }

    @Override
    public boolean isNull(String columnName) {
        int index = getColumnIndexOrThrow(columnName);
        return isNull(index);
    }

    private void validateIndex(int index) {
        if (index < 0 || index >= values.size()) {
            throw new IndexOutOfBoundsException(
                    "Index " + index + " out of bounds for record with " + values.size() + " fields"
            );
        }
    }

    private int getColumnIndexOrThrow(String columnName) {
        return metadata.getColumnIndex(columnName)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Column '" + columnName + "' not found in CSV metadata"
                ));
    }

    @Override
    public String toString() {
        return "CsvRecord{line=" + lineNumber + ", values=" + values + "}";
    }
}
