package com.gnuplot.core.data.json;

import com.gnuplot.core.data.DataMetadata;
import com.gnuplot.core.data.DataRecord;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for JsonDataSource.
 */
class JsonDataSourceTest {

    @Test
    void shouldReadArrayOfObjects() throws IOException {
        String json = "[{\"x\": 1.0, \"y\": 2.0}, {\"x\": 3.0, \"y\": 4.0}]";
        JsonConfig config = JsonConfig.defaults();

        try (JsonDataSource source = new JsonDataSource(new StringReader(json), config, "test.json")) {
            DataMetadata meta = source.getMetadata();
            assertThat(meta.getColumnNames()).containsExactlyInAnyOrder("x", "y");
            assertThat(meta.getRecordCount()).hasValue(2L);

            List<DataRecord> records = collectRecords(source);
            assertThat(records).hasSize(2);

            assertThat(records.get(0).getDouble("x")).isEqualTo(1.0);
            assertThat(records.get(0).getDouble("y")).isEqualTo(2.0);

            assertThat(records.get(1).getDouble("x")).isEqualTo(3.0);
            assertThat(records.get(1).getDouble("y")).isEqualTo(4.0);
        }
    }

    @Test
    void shouldReadArrayOfArrays() throws IOException {
        String json = "[[1.0, 2.0], [3.0, 4.0]]";
        JsonConfig config = JsonConfig.defaults();

        try (JsonDataSource source = new JsonDataSource(new StringReader(json), config, "test.json")) {
            List<DataRecord> records = collectRecords(source);
            assertThat(records).hasSize(2);

            assertThat(records.get(0).getDouble(0)).isEqualTo(1.0);
            assertThat(records.get(0).getDouble(1)).isEqualTo(2.0);

            assertThat(records.get(1).getDouble(0)).isEqualTo(3.0);
            assertThat(records.get(1).getDouble(1)).isEqualTo(4.0);
        }
    }

    @Test
    void shouldReadSingleObject() throws IOException {
        String json = "{\"x\": 1.0, \"y\": 2.0}";
        JsonConfig config = JsonConfig.defaults();

        try (JsonDataSource source = new JsonDataSource(new StringReader(json), config, "test.json")) {
            List<DataRecord> records = collectRecords(source);
            assertThat(records).hasSize(1);

            assertThat(records.get(0).getDouble("x")).isEqualTo(1.0);
            assertThat(records.get(0).getDouble("y")).isEqualTo(2.0);
        }
    }

    @Test
    void shouldReadNestedDataWithJSONPath() throws IOException {
        String json = "{\"results\": {\"data\": [{\"x\": 1, \"y\": 2}, {\"x\": 3, \"y\": 4}]}}";
        JsonConfig config = JsonConfig.builder()
                .dataPath("$.results.data")
                .build();

        try (JsonDataSource source = new JsonDataSource(new StringReader(json), config, "test.json")) {
            List<DataRecord> records = collectRecords(source);
            assertThat(records).hasSize(2);

            assertThat(records.get(0).getDouble("x")).isEqualTo(1.0);
            assertThat(records.get(1).getDouble("x")).isEqualTo(3.0);
        }
    }

    @Test
    void shouldReadObjectWithDataArray() throws IOException {
        String json = "{\"data\": [[1.0, 2.0], [3.0, 4.0]]}";
        JsonConfig config = JsonConfig.defaults();

        try (JsonDataSource source = new JsonDataSource(new StringReader(json), config, "test.json")) {
            List<DataRecord> records = collectRecords(source);
            assertThat(records).hasSize(2);

            assertThat(records.get(0).getDouble(0)).isEqualTo(1.0);
            assertThat(records.get(1).getDouble(0)).isEqualTo(3.0);
        }
    }

    @Test
    void shouldHandleMixedTypes() throws IOException {
        String json = "[{\"name\": \"A\", \"value\": 42}, {\"name\": \"B\", \"value\": 99}]";
        JsonConfig config = JsonConfig.defaults();

        try (JsonDataSource source = new JsonDataSource(new StringReader(json), config, "test.json")) {
            List<DataRecord> records = collectRecords(source);

            assertThat(records.get(0).getString("name")).isEqualTo("A");
            assertThat(records.get(0).getDouble("value")).isEqualTo(42.0);

            assertThat(records.get(1).getString("name")).isEqualTo("B");
            assertThat(records.get(1).getDouble("value")).isEqualTo(99.0);
        }
    }

    @Test
    void shouldHandleNullValues() throws IOException {
        String json = "[{\"x\": 1, \"y\": null}, {\"x\": null, \"y\": 3}]";
        JsonConfig config = JsonConfig.defaults();

        try (JsonDataSource source = new JsonDataSource(new StringReader(json), config, "test.json")) {
            List<DataRecord> records = collectRecords(source);

            assertThat(records.get(0).isNull("y")).isTrue();
            assertThat(records.get(0).isNull("x")).isFalse();

            assertThat(records.get(1).isNull("x")).isTrue();
            assertThat(records.get(1).isNull("y")).isFalse();
        }
    }

    @Test
    void shouldHandleBooleanValues() throws IOException {
        String json = "[{\"flag\": true}, {\"flag\": false}]";
        JsonConfig config = JsonConfig.defaults();

        try (JsonDataSource source = new JsonDataSource(new StringReader(json), config, "test.json")) {
            List<DataRecord> records = collectRecords(source);

            assertThat(records.get(0).getValue("flag")).isEqualTo(true);
            assertThat(records.get(1).getValue("flag")).isEqualTo(false);
        }
    }

    @Test
    void shouldHandleScientificNotation() throws IOException {
        String json = "[{\"x\": 1.5e10}, {\"x\": 2.3E-5}]";
        JsonConfig config = JsonConfig.defaults();

        try (JsonDataSource source = new JsonDataSource(new StringReader(json), config, "test.json")) {
            List<DataRecord> records = collectRecords(source);

            assertThat(records.get(0).getDouble("x")).isEqualTo(1.5e10);
            assertThat(records.get(1).getDouble("x")).isEqualTo(2.3E-5);
        }
    }

    @Test
    void shouldHandleArrayOfPrimitives() throws IOException {
        String json = "[1, 2, 3, 4, 5]";
        JsonConfig config = JsonConfig.defaults();

        try (JsonDataSource source = new JsonDataSource(new StringReader(json), config, "test.json")) {
            List<DataRecord> records = collectRecords(source);
            assertThat(records).hasSize(5);

            assertThat(records.get(0).getDouble("value")).isEqualTo(1.0);
            assertThat(records.get(4).getDouble("value")).isEqualTo(5.0);
        }
    }

    @Test
    void shouldThrowOnInvalidJSONPath() throws IOException {
        String json = "{\"data\": [1, 2, 3]}";
        JsonConfig config = JsonConfig.builder()
                .dataPath("$.nonexistent.path")
                .build();

        assertThatThrownBy(() -> new JsonDataSource(new StringReader(json), config, "test.json"))
                .isInstanceOf(JsonParseException.class)
                .hasMessageContaining("Path not found");
    }

    @Test
    void shouldThrowOnInvalidColumnName() throws IOException {
        String json = "[{\"x\": 1}]";
        JsonConfig config = JsonConfig.defaults();

        try (JsonDataSource source = new JsonDataSource(new StringReader(json), config, "test.json")) {
            List<DataRecord> records = collectRecords(source);
            DataRecord record = records.get(0);

            assertThatThrownBy(() -> record.getDouble("nonexistent"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("not found");
        }
    }

    @Test
    void shouldThrowOnNonNumericConversion() throws IOException {
        String json = "[{\"text\": \"hello\"}]";
        JsonConfig config = JsonConfig.defaults();

        try (JsonDataSource source = new JsonDataSource(new StringReader(json), config, "test.json")) {
            List<DataRecord> records = collectRecords(source);
            DataRecord record = records.get(0);

            assertThatThrownBy(() -> record.getDouble("text"))
                    .isInstanceOf(NumberFormatException.class);
        }
    }

    @Test
    void shouldThrowWhenIteratingClosedSource() throws IOException {
        String json = "[{\"x\": 1}]";
        JsonConfig config = JsonConfig.defaults();

        JsonDataSource source = new JsonDataSource(new StringReader(json), config, "test.json");
        source.close();

        assertThat(source.isClosed()).isTrue();
        assertThatThrownBy(source::iterator)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("closed");
    }

    @Test
    void shouldReadFromFile(@TempDir Path tempDir) throws IOException {
        Path file = tempDir.resolve("test.json");
        Files.writeString(file, "[{\"x\": 1, \"y\": 2}, {\"x\": 3, \"y\": 4}]");

        JsonConfig config = JsonConfig.defaults();
        try (JsonDataSource source = new JsonDataSource(file, config)) {
            List<DataRecord> records = collectRecords(source);
            assertThat(records).hasSize(2);
        }
    }

    @Test
    void shouldHandleComplexNestedStructure() throws IOException {
        String json = """
                {
                    "metadata": {"version": "1.0"},
                    "results": {
                        "measurements": [
                            {"time": 0, "temp": 20.5, "pressure": 101.3},
                            {"time": 1, "temp": 21.0, "pressure": 101.2},
                            {"time": 2, "temp": 21.5, "pressure": 101.1}
                        ]
                    }
                }
                """;

        JsonConfig config = JsonConfig.builder()
                .dataPath("$.results.measurements")
                .build();

        try (JsonDataSource source = new JsonDataSource(new StringReader(json), config, "test.json")) {
            List<DataRecord> records = collectRecords(source);
            assertThat(records).hasSize(3);

            assertThat(records.get(0).getDouble("time")).isEqualTo(0.0);
            assertThat(records.get(0).getDouble("temp")).isEqualTo(20.5);
            assertThat(records.get(0).getDouble("pressure")).isEqualTo(101.3);

            assertThat(records.get(2).getDouble("time")).isEqualTo(2.0);
        }
    }

    @Test
    void shouldGetAllValues() throws IOException {
        String json = "[{\"a\": 1, \"b\": 2, \"c\": 3}]";
        JsonConfig config = JsonConfig.defaults();

        try (JsonDataSource source = new JsonDataSource(new StringReader(json), config, "test.json")) {
            List<DataRecord> records = collectRecords(source);
            List<Object> values = records.get(0).getValues();

            assertThat(values).hasSize(3);
            assertThat(values).contains(1L, 2L, 3L);
        }
    }

    private List<DataRecord> collectRecords(JsonDataSource source) {
        List<DataRecord> records = new ArrayList<>();
        for (DataRecord record : source) {
            records.add(record);
        }
        return records;
    }
}
