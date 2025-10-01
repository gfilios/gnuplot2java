package com.gnuplot.core.data.json;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gnuplot.core.data.DataMetadata;
import com.gnuplot.core.data.DataRecord;
import com.gnuplot.core.data.DataSource;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * DataSource implementation for JSON files.
 * Supports JSONPath extraction, nested objects, and array handling.
 *
 * <p>Example usage:
 * <pre>{@code
 * // Simple array of objects: [{"x": 1, "y": 2}, {"x": 3, "y": 4}]
 * JsonConfig config = JsonConfig.defaults();
 * try (JsonDataSource json = new JsonDataSource(path, config)) {
 *     for (DataRecord record : json) {
 *         double x = record.getDouble("x");
 *         double y = record.getDouble("y");
 *     }
 * }
 *
 * // Extract nested data with JSONPath
 * JsonConfig config = JsonConfig.builder()
 *     .dataPath("$.results.data")
 *     .build();
 * try (JsonDataSource json = new JsonDataSource(path, config)) {
 *     // Process extracted data
 * }
 * }</pre>
 *
 * @since 1.0
 */
public class JsonDataSource implements DataSource {

    private final JsonNode rootNode;
    private final JsonConfig config;
    private final JsonMetadata metadata;
    private final List<JsonNode> records;
    private boolean closed = false;

    /**
     * Creates a JSON data source from a file path.
     *
     * @param path   path to the JSON file
     * @param config JSON parsing configuration
     * @throws IOException if file cannot be read
     */
    public JsonDataSource(Path path, JsonConfig config) throws IOException {
        this(Files.newBufferedReader(path), config, path.toString());
    }

    /**
     * Creates a JSON data source from a reader.
     *
     * @param reader           reader to read JSON data from
     * @param config           JSON parsing configuration
     * @param sourceIdentifier identifier for this source (filename, URL, etc.)
     * @throws IOException if reader cannot be parsed
     */
    public JsonDataSource(Reader reader, JsonConfig config, String sourceIdentifier) throws IOException {
        this.config = config;

        ObjectMapper mapper = new ObjectMapper();
        this.rootNode = mapper.readTree(reader);

        // Extract data using JSONPath
        JsonNode dataNode = extractData();

        // Convert to list of records
        this.records = parseRecords(dataNode);

        // Build metadata
        this.metadata = new JsonMetadata(sourceIdentifier, records, config);
    }

    private JsonNode extractData() {
        String path = config.getDataPath();

        if ("$".equals(path)) {
            return rootNode;
        }

        // Simple path navigation: $.field.nested.path
        if (path.startsWith("$.")) {
            String[] parts = path.substring(2).split("\\.");
            JsonNode current = rootNode;

            for (String part : parts) {
                if (current == null) {
                    throw new JsonParseException("Path not found: " + path);
                }
                current = current.get(part);
            }

            if (current == null) {
                throw new JsonParseException("Path not found: " + path);
            }

            return current;
        }

        throw new JsonParseException("Invalid path format: " + path);
    }

    private List<JsonNode> parseRecords(JsonNode dataNode) {
        List<JsonNode> recordList = new ArrayList<>();

        if (dataNode.isArray()) {
            // Array of records
            for (JsonNode node : dataNode) {
                if (node.isArray() && config.isFlattenArrays()) {
                    // Flatten array-of-arrays: [[1,2], [3,4]] -> records
                    recordList.add(node);
                } else if (node.isObject()) {
                    // Array of objects: [{"x":1}, {"x":2}]
                    recordList.add(node);
                } else {
                    // Array of primitives: [1, 2, 3] - wrap in object
                    ObjectMapper mapper = new ObjectMapper();
                    recordList.add(mapper.createObjectNode().set("value", node));
                }
            }
        } else if (dataNode.isObject()) {
            // Single object - check if it has a data array property
            JsonNode arrayNode = dataNode.get(config.getArrayName());
            if (arrayNode != null && arrayNode.isArray()) {
                return parseRecords(arrayNode);
            }
            // Otherwise treat as single record
            recordList.add(dataNode);
        } else {
            // Single primitive value
            ObjectMapper mapper = new ObjectMapper();
            recordList.add(mapper.createObjectNode().set("value", dataNode));
        }

        return recordList;
    }

    @Override
    public DataMetadata getMetadata() {
        return metadata;
    }

    @Override
    public Iterator<DataRecord> iterator() {
        if (closed) {
            throw new IllegalStateException("DataSource has been closed");
        }
        return new JsonIterator();
    }

    @Override
    public void close() {
        closed = true;
    }

    @Override
    public boolean isClosed() {
        return closed;
    }

    /**
     * Iterator implementation for JSON records.
     */
    private class JsonIterator implements Iterator<DataRecord> {
        private int currentIndex = 0;

        @Override
        public boolean hasNext() {
            return currentIndex < records.size();
        }

        @Override
        public DataRecord next() {
            if (!hasNext()) {
                throw new java.util.NoSuchElementException("No more records");
            }

            JsonNode node = records.get(currentIndex);
            currentIndex++;

            return new JsonRecord(node, metadata, currentIndex);
        }
    }
}
