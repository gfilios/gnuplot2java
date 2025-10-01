package com.gnuplot.core.data.json;

import com.fasterxml.jackson.databind.JsonNode;
import com.gnuplot.core.data.DataRecord;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * DataRecord implementation for JSON data.
 *
 * @since 1.0
 */
final class JsonRecord implements DataRecord {

    private final JsonNode node;
    private final JsonMetadata metadata;
    private final long recordNumber;

    JsonRecord(JsonNode node, JsonMetadata metadata, long recordNumber) {
        this.node = node;
        this.metadata = metadata;
        this.recordNumber = recordNumber;
    }

    @Override
    public Object getValue(int index) {
        validateIndex(index);

        if (node.isObject()) {
            String columnName = metadata.getColumnName(index)
                    .orElseThrow(() -> new IndexOutOfBoundsException("Index " + index + " out of bounds"));
            JsonNode fieldNode = node.get(columnName);
            return extractValue(fieldNode);
        } else if (node.isArray()) {
            JsonNode element = node.get(index);
            return extractValue(element);
        }

        throw new IllegalStateException("Unexpected node type: " + node.getNodeType());
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

        if (value instanceof Number) {
            return ((Number) value).doubleValue();
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
        List<Object> values = new ArrayList<>();

        if (node.isObject()) {
            for (String columnName : metadata.getColumnNames()) {
                JsonNode fieldNode = node.get(columnName);
                values.add(extractValue(fieldNode));
            }
        } else if (node.isArray()) {
            for (JsonNode element : node) {
                values.add(extractValue(element));
            }
        }

        return Collections.unmodifiableList(values);
    }

    @Override
    public int size() {
        if (node.isObject()) {
            return metadata.getColumnCount();
        } else if (node.isArray()) {
            return node.size();
        }
        return 0;
    }

    @Override
    public boolean isNull(int index) {
        validateIndex(index);

        if (node.isObject()) {
            String columnName = metadata.getColumnName(index)
                    .orElseThrow(() -> new IndexOutOfBoundsException("Index " + index + " out of bounds"));
            JsonNode fieldNode = node.get(columnName);
            return fieldNode == null || fieldNode.isNull();
        } else if (node.isArray()) {
            JsonNode element = node.get(index);
            return element == null || element.isNull();
        }

        return true;
    }

    @Override
    public boolean isNull(String columnName) {
        int index = getColumnIndexOrThrow(columnName);
        return isNull(index);
    }

    private Object extractValue(JsonNode jsonNode) {
        if (jsonNode == null || jsonNode.isNull()) {
            return null;
        }

        if (jsonNode.isBoolean()) {
            return jsonNode.asBoolean();
        } else if (jsonNode.isInt() || jsonNode.isLong()) {
            return jsonNode.asLong();
        } else if (jsonNode.isDouble() || jsonNode.isFloat()) {
            return jsonNode.asDouble();
        } else if (jsonNode.isTextual()) {
            return jsonNode.asText();
        } else if (jsonNode.isArray() || jsonNode.isObject()) {
            return jsonNode.toString();
        }

        return jsonNode.asText();
    }

    private void validateIndex(int index) {
        if (index < 0 || index >= size()) {
            throw new IndexOutOfBoundsException(
                    "Index " + index + " out of bounds for record with " + size() + " fields"
            );
        }
    }

    private int getColumnIndexOrThrow(String columnName) {
        return metadata.getColumnIndex(columnName)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Column '" + columnName + "' not found in JSON metadata"
                ));
    }

    @Override
    public String toString() {
        return "JsonRecord{record=" + recordNumber + ", node=" + node + "}";
    }
}
