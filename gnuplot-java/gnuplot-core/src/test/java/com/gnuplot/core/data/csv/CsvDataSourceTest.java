package com.gnuplot.core.data.csv;

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
 * Unit tests for CsvDataSource.
 */
class CsvDataSourceTest {

    @Test
    void shouldReadSimpleCsvWithHeader() throws IOException {
        String csv = "x,y\n1.0,2.0\n3.0,4.0";
        CsvConfig config = CsvConfig.defaults();

        try (CsvDataSource source = new CsvDataSource(new StringReader(csv), config, "test.csv")) {
            DataMetadata meta = source.getMetadata();
            assertThat(meta.hasHeader()).isTrue();
            assertThat(meta.getColumnNames()).containsExactly("x", "y");

            List<DataRecord> records = collectRecords(source);
            assertThat(records).hasSize(2);

            assertThat(records.get(0).getDouble(0)).isEqualTo(1.0);
            assertThat(records.get(0).getDouble(1)).isEqualTo(2.0);

            assertThat(records.get(1).getDouble(0)).isEqualTo(3.0);
            assertThat(records.get(1).getDouble(1)).isEqualTo(4.0);
        }
    }

    @Test
    void shouldReadCsvWithoutHeader() throws IOException {
        String csv = "1.0,2.0\n3.0,4.0";
        CsvConfig config = CsvConfig.builder()
                .hasHeader(false)
                .build();

        try (CsvDataSource source = new CsvDataSource(new StringReader(csv), config, "test.csv")) {
            DataMetadata meta = source.getMetadata();
            assertThat(meta.hasHeader()).isFalse();
            assertThat(meta.getColumnNames()).isEmpty();

            List<DataRecord> records = collectRecords(source);
            assertThat(records).hasSize(2);

            assertThat(records.get(0).getDouble(0)).isEqualTo(1.0);
            assertThat(records.get(0).getDouble(1)).isEqualTo(2.0);
        }
    }

    @Test
    void shouldAccessFieldsByColumnName() throws IOException {
        String csv = "x,y,z\n1.0,2.0,3.0";
        CsvConfig config = CsvConfig.defaults();

        try (CsvDataSource source = new CsvDataSource(new StringReader(csv), config, "test.csv")) {
            List<DataRecord> records = collectRecords(source);
            DataRecord record = records.get(0);

            assertThat(record.getDouble("x")).isEqualTo(1.0);
            assertThat(record.getDouble("y")).isEqualTo(2.0);
            assertThat(record.getDouble("z")).isEqualTo(3.0);
        }
    }

    @Test
    void shouldSkipEmptyLines() throws IOException {
        String csv = "x,y\n1.0,2.0\n\n3.0,4.0";
        CsvConfig config = CsvConfig.builder()
                .skipEmptyLines(true)
                .build();

        try (CsvDataSource source = new CsvDataSource(new StringReader(csv), config, "test.csv")) {
            List<DataRecord> records = collectRecords(source);
            assertThat(records).hasSize(2);
        }
    }

    @Test
    void shouldSkipCommentLines() throws IOException {
        String csv = "x,y\n# This is a comment\n1.0,2.0\n# Another comment\n3.0,4.0";
        CsvConfig config = CsvConfig.defaults();

        try (CsvDataSource source = new CsvDataSource(new StringReader(csv), config, "test.csv")) {
            List<DataRecord> records = collectRecords(source);
            assertThat(records).hasSize(2);
        }
    }

    @Test
    void shouldSkipInitialLines() throws IOException {
        String csv = "Meta: some data\nVersion: 1.0\nx,y\n1.0,2.0";
        CsvConfig config = CsvConfig.builder()
                .skipLines(2)
                .build();

        try (CsvDataSource source = new CsvDataSource(new StringReader(csv), config, "test.csv")) {
            DataMetadata meta = source.getMetadata();
            assertThat(meta.getColumnNames()).containsExactly("x", "y");

            List<DataRecord> records = collectRecords(source);
            assertThat(records).hasSize(1);
        }
    }

    @Test
    void shouldHandleQuotedFields() throws IOException {
        String csv = "name,value\n\"hello, world\",42";
        CsvConfig config = CsvConfig.defaults();

        try (CsvDataSource source = new CsvDataSource(new StringReader(csv), config, "test.csv")) {
            List<DataRecord> records = collectRecords(source);
            DataRecord record = records.get(0);

            assertThat(record.getString("name")).isEqualTo("hello, world");
            assertThat(record.getDouble("value")).isEqualTo(42.0);
        }
    }

    @Test
    void shouldHandleCustomDelimiter() throws IOException {
        String csv = "x;y\n1.0;2.0";
        CsvConfig config = CsvConfig.builder()
                .delimiter(';')
                .build();

        try (CsvDataSource source = new CsvDataSource(new StringReader(csv), config, "test.csv")) {
            List<DataRecord> records = collectRecords(source);
            assertThat(records.get(0).getDouble(0)).isEqualTo(1.0);
            assertThat(records.get(0).getDouble(1)).isEqualTo(2.0);
        }
    }

    @Test
    void shouldHandleNullValues() throws IOException {
        String csv = "x,y\n1.0,\n,3.0";
        CsvConfig config = CsvConfig.defaults();

        try (CsvDataSource source = new CsvDataSource(new StringReader(csv), config, "test.csv")) {
            List<DataRecord> records = collectRecords(source);

            assertThat(records.get(0).isNull("y")).isTrue();
            assertThat(records.get(1).isNull("x")).isTrue();
        }
    }

    @Test
    void shouldThrowOnInvalidColumnName() throws IOException {
        String csv = "x,y\n1.0,2.0";
        CsvConfig config = CsvConfig.defaults();

        try (CsvDataSource source = new CsvDataSource(new StringReader(csv), config, "test.csv")) {
            List<DataRecord> records = collectRecords(source);
            DataRecord record = records.get(0);

            assertThatThrownBy(() -> record.getDouble("z"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Column 'z' not found");
        }
    }

    @Test
    void shouldThrowOnInvalidIndex() throws IOException {
        String csv = "x,y\n1.0,2.0";
        CsvConfig config = CsvConfig.defaults();

        try (CsvDataSource source = new CsvDataSource(new StringReader(csv), config, "test.csv")) {
            List<DataRecord> records = collectRecords(source);
            DataRecord record = records.get(0);

            assertThatThrownBy(() -> record.getDouble(5))
                    .isInstanceOf(IndexOutOfBoundsException.class);
        }
    }

    @Test
    void shouldThrowOnNonNumericConversion() throws IOException {
        String csv = "x,y\ntext,2.0";
        CsvConfig config = CsvConfig.defaults();

        try (CsvDataSource source = new CsvDataSource(new StringReader(csv), config, "test.csv")) {
            List<DataRecord> records = collectRecords(source);
            DataRecord record = records.get(0);

            assertThatThrownBy(() -> record.getDouble("x"))
                    .isInstanceOf(NumberFormatException.class)
                    .hasMessageContaining("Cannot convert value 'text'");
        }
    }

    @Test
    void shouldThrowWhenIteratingClosedSource() throws IOException {
        String csv = "x,y\n1.0,2.0";
        CsvConfig config = CsvConfig.defaults();

        CsvDataSource source = new CsvDataSource(new StringReader(csv), config, "test.csv");
        source.close();

        assertThat(source.isClosed()).isTrue();
        assertThatThrownBy(source::iterator)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("closed");
    }

    @Test
    void shouldReadFromFile(@TempDir Path tempDir) throws IOException {
        Path file = tempDir.resolve("test.csv");
        Files.writeString(file, "x,y\n1.0,2.0\n3.0,4.0");

        CsvConfig config = CsvConfig.defaults();
        try (CsvDataSource source = new CsvDataSource(file, config)) {
            List<DataRecord> records = collectRecords(source);
            assertThat(records).hasSize(2);
        }
    }

    @Test
    void shouldStreamLargeFile(@TempDir Path tempDir) throws IOException {
        // Create a larger CSV file
        Path file = tempDir.resolve("large.csv");
        StringBuilder csv = new StringBuilder("x,y\n");
        for (int i = 0; i < 1000; i++) {
            csv.append(i).append(",").append(i * 2.0).append("\n");
        }
        Files.writeString(file, csv.toString());

        CsvConfig config = CsvConfig.defaults();
        try (CsvDataSource source = new CsvDataSource(file, config)) {
            int count = 0;
            for (DataRecord record : source) {
                assertThat(record.getDouble("x")).isEqualTo(count);
                assertThat(record.getDouble("y")).isEqualTo(count * 2.0);
                count++;
            }
            assertThat(count).isEqualTo(1000);
        }
    }

    @Test
    void shouldHandleScientificNotation() throws IOException {
        String csv = "x,y\n1.5e10,2.3E-5";
        CsvConfig config = CsvConfig.defaults();

        try (CsvDataSource source = new CsvDataSource(new StringReader(csv), config, "test.csv")) {
            List<DataRecord> records = collectRecords(source);
            DataRecord record = records.get(0);

            assertThat(record.getDouble(0)).isEqualTo(1.5e10);
            assertThat(record.getDouble(1)).isEqualTo(2.3E-5);
        }
    }

    private List<DataRecord> collectRecords(CsvDataSource source) {
        List<DataRecord> records = new ArrayList<>();
        for (DataRecord record : source) {
            records.add(record);
        }
        return records;
    }
}
