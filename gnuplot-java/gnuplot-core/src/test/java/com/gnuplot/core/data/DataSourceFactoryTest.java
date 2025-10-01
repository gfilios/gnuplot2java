package com.gnuplot.core.data;

import com.gnuplot.core.data.csv.CsvConfig;
import com.gnuplot.core.data.json.JsonConfig;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test suite for DataSourceFactory.
 */
class DataSourceFactoryTest {

    @TempDir
    Path tempDir;

    private Path csvFile;
    private Path jsonFile;
    private Path tsvFile;
    private Path unknownFile;

    @BeforeEach
    void setUp() throws IOException {
        // Create CSV test file
        csvFile = tempDir.resolve("test.csv");
        Files.writeString(csvFile, "x,y\n1,2\n3,4\n");

        // Create JSON test file
        jsonFile = tempDir.resolve("test.json");
        Files.writeString(jsonFile, "[{\"x\":1,\"y\":2},{\"x\":3,\"y\":4}]");

        // Create TSV test file
        tsvFile = tempDir.resolve("test.tsv");
        Files.writeString(tsvFile, "x\ty\n1\t2\n3\t4\n");

        // Create unknown format file
        unknownFile = tempDir.resolve("test.unknown");
        Files.writeString(unknownFile, "some data");
    }

    @AfterEach
    void tearDown() {
        // Cleanup any custom providers registered during tests
        DataSourceFactory.unregisterProvider("custom");
    }

    // ============================================================
    // Automatic Format Detection
    // ============================================================

    @Test
    void testCreateWithCsvExtension() throws IOException {
        try (DataSource ds = DataSourceFactory.create(csvFile)) {
            assertNotNull(ds);
            assertTrue(ds.getMetadata().getSourceIdentifier().endsWith("test.csv"));
            assertTrue(ds.getMetadata().hasHeader());

            int count = 0;
            for (DataRecord record : ds) {
                count++;
            }
            assertEquals(2, count);
        }
    }

    @Test
    void testCreateWithJsonExtension() throws IOException {
        try (DataSource ds = DataSourceFactory.create(jsonFile)) {
            assertNotNull(ds);
            assertTrue(ds.getMetadata().getSourceIdentifier().endsWith("test.json"));

            int count = 0;
            for (DataRecord record : ds) {
                count++;
            }
            assertEquals(2, count);
        }
    }

    @Test
    void testCreateWithTsvExtension() throws IOException {
        try (DataSource ds = DataSourceFactory.create(tsvFile)) {
            assertNotNull(ds);
            assertTrue(ds.getMetadata().getSourceIdentifier().endsWith("test.tsv"));
            assertTrue(ds.getMetadata().hasHeader());

            int count = 0;
            for (DataRecord record : ds) {
                count++;
            }
            assertEquals(2, count);
        }
    }

    @Test
    void testCreateWithUnknownExtension() {
        assertThrows(UnsupportedFormatException.class, () ->
                DataSourceFactory.create(unknownFile)
        );
    }

    // ============================================================
    // Explicit Format Specification
    // ============================================================

    @Test
    void testCreateWithExplicitCsvFormat() throws IOException {
        try (DataSource ds = DataSourceFactory.create(csvFile, "csv")) {
            assertNotNull(ds);
            int count = 0;
            for (DataRecord record : ds) {
                count++;
            }
            assertEquals(2, count);
        }
    }

    @Test
    void testCreateWithExplicitJsonFormat() throws IOException {
        try (DataSource ds = DataSourceFactory.create(jsonFile, "json")) {
            assertNotNull(ds);
            int count = 0;
            for (DataRecord record : ds) {
                count++;
            }
            assertEquals(2, count);
        }
    }

    @Test
    void testCreateWithUnsupportedFormat() {
        assertThrows(UnsupportedFormatException.class, () ->
                DataSourceFactory.create(csvFile, "xml")
        );
    }

    // ============================================================
    // CSV-Specific Factory Methods
    // ============================================================

    @Test
    void testCreateCsvWithDefaults() throws IOException {
        try (DataSource ds = DataSourceFactory.createCsv(csvFile)) {
            assertNotNull(ds);
            assertTrue(ds.getMetadata().hasHeader());

            int count = 0;
            for (DataRecord record : ds) {
                count++;
            }
            assertEquals(2, count);
        }
    }

    @Test
    void testCreateCsvWithCustomConfig() throws IOException {
        CsvConfig config = CsvConfig.builder()
                .delimiter(',')
                .hasHeader(true)
                .skipEmptyLines(true)
                .build();

        try (DataSource ds = DataSourceFactory.createCsv(csvFile, config)) {
            assertNotNull(ds);
            int count = 0;
            for (DataRecord record : ds) {
                count++;
            }
            assertEquals(2, count);
        }
    }

    @Test
    void testCreateCsvFromReader() throws IOException {
        String csvData = "a,b\n1,2\n3,4\n";
        StringReader reader = new StringReader(csvData);
        CsvConfig config = CsvConfig.builder()
                .hasHeader(true)
                .build();

        try (DataSource ds = DataSourceFactory.createCsv(reader, config, "string-source")) {
            assertNotNull(ds);
            assertEquals("string-source", ds.getMetadata().getSourceIdentifier());

            int count = 0;
            for (DataRecord record : ds) {
                count++;
            }
            assertEquals(2, count);
        }
    }

    // ============================================================
    // JSON-Specific Factory Methods
    // ============================================================

    @Test
    void testCreateJsonWithDefaults() throws IOException {
        try (DataSource ds = DataSourceFactory.createJson(jsonFile)) {
            assertNotNull(ds);
            int count = 0;
            for (DataRecord record : ds) {
                count++;
            }
            assertEquals(2, count);
        }
    }

    @Test
    void testCreateJsonWithCustomConfig() throws IOException {
        JsonConfig config = JsonConfig.builder()
                .dataPath("$")
                .build();

        try (DataSource ds = DataSourceFactory.createJson(jsonFile, config)) {
            assertNotNull(ds);
            int count = 0;
            for (DataRecord record : ds) {
                count++;
            }
            assertEquals(2, count);
        }
    }

    @Test
    void testCreateJsonFromReader() throws IOException {
        String jsonData = "[{\"x\":1},{\"x\":2}]";
        StringReader reader = new StringReader(jsonData);
        JsonConfig config = JsonConfig.defaults();

        try (DataSource ds = DataSourceFactory.createJson(reader, config, "string-source")) {
            assertNotNull(ds);
            assertEquals("string-source", ds.getMetadata().getSourceIdentifier());

            int count = 0;
            for (DataRecord record : ds) {
                count++;
            }
            assertEquals(2, count);
        }
    }

    // ============================================================
    // Custom Provider Registration
    // ============================================================

    @Test
    void testRegisterCustomProvider() throws IOException {
        // Register custom provider
        DataSourceProvider customProvider = new TestCustomDataSourceProvider();
        DataSourceFactory.registerProvider("custom", customProvider);

        // Verify format is supported
        assertTrue(DataSourceFactory.isFormatSupported("custom"));
        assertTrue(DataSourceFactory.getSupportedFormats().contains("custom"));

        // Create file with custom extension
        Path customFile = tempDir.resolve("test.custom");
        Files.writeString(customFile, "x,y\n1,2\n");

        // Create data source using custom provider
        try (DataSource ds = DataSourceFactory.create(customFile)) {
            assertNotNull(ds);
        }
    }

    @Test
    void testUnregisterProvider() {
        // Register custom provider
        DataSourceProvider customProvider = new TestCustomDataSourceProvider();
        DataSourceFactory.registerProvider("custom", customProvider);
        assertTrue(DataSourceFactory.isFormatSupported("custom"));

        // Unregister
        var removed = DataSourceFactory.unregisterProvider("custom");
        assertTrue(removed.isPresent());
        assertFalse(DataSourceFactory.isFormatSupported("custom"));

        // Unregister again should return empty
        removed = DataSourceFactory.unregisterProvider("custom");
        assertFalse(removed.isPresent());
    }

    @Test
    void testIsFormatSupported() {
        assertTrue(DataSourceFactory.isFormatSupported("csv"));
        assertTrue(DataSourceFactory.isFormatSupported("CSV")); // case insensitive
        assertTrue(DataSourceFactory.isFormatSupported("json"));
        assertTrue(DataSourceFactory.isFormatSupported("tsv"));
        assertFalse(DataSourceFactory.isFormatSupported("xml"));
    }

    @Test
    void testGetSupportedFormats() {
        var formats = DataSourceFactory.getSupportedFormats();
        assertTrue(formats.contains("csv"));
        assertTrue(formats.contains("json"));
        assertTrue(formats.contains("tsv"));
    }

    // ============================================================
    // Edge Cases
    // ============================================================

    @Test
    void testCreateWithNoExtension() {
        Path noExtFile = tempDir.resolve("noextension");
        assertThrows(UnsupportedFormatException.class, () ->
                DataSourceFactory.create(noExtFile)
        );
    }

    @Test
    void testCreateWithEmptyFile() throws IOException {
        Path emptyFile = tempDir.resolve("empty.csv");
        Files.writeString(emptyFile, "");

        try (DataSource ds = DataSourceFactory.create(emptyFile)) {
            assertNotNull(ds);
            int count = 0;
            for (DataRecord record : ds) {
                count++;
            }
            assertEquals(0, count);
        }
    }

    @Test
    void testCaseInsensitiveFormatDetection() throws IOException {
        Path upperFile = tempDir.resolve("test.CSV");
        Files.writeString(upperFile, "x,y\n1,2\n");

        try (DataSource ds = DataSourceFactory.create(upperFile)) {
            assertNotNull(ds);
        }
    }

    // ============================================================
    // Helper Classes
    // ============================================================

    /**
     * Test custom provider that creates CSV sources.
     */
    private static class TestCustomDataSourceProvider implements DataSourceProvider {
        @Override
        public DataSource create(Path path) throws IOException {
            return new com.gnuplot.core.data.csv.CsvDataSource(
                    path,
                    CsvConfig.defaults()
            );
        }
    }
}
