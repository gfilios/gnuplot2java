package com.gnuplot.core.data.transform;

import com.gnuplot.core.data.DataRecord;
import com.gnuplot.core.data.DataSource;
import com.gnuplot.core.data.DataSourceFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test suite for FilteredDataSource and related filtering components.
 */
class FilteredDataSourceTest {

    @TempDir
    Path tempDir;

    private Path csvFile;

    @BeforeEach
    void setUp() throws IOException {
        // Create test CSV: id,name,age,score
        csvFile = tempDir.resolve("test.csv");
        Files.writeString(csvFile,
                "id,name,age,score\n" +
                        "1,Alice,25,85.5\n" +
                        "2,Bob,30,72.0\n" +
                        "3,Charlie,18,95.0\n" +
                        "4,David,45,68.5\n" +
                        "5,Eve,22,91.0\n" +
                        "6,Frank,35,78.0\n" +
                        "7,Grace,28,88.5\n" +
                        "8,Henry,19,82.0\n" +
                        "9,Iris,40,76.0\n" +
                        "10,Jack,23,90.0\n");
    }

    // ============================================================
    // Row Filtering Tests
    // ============================================================

    @Test
    void testRowFilterByPredicate() throws IOException {
        try (DataSource source = DataSourceFactory.createCsv(csvFile);
             FilteredDataSource filtered = FilteredDataSource.builder(source)
                     .rowFilter(record -> record.getDouble("age") >= 30)
                     .build()) {

            List<String> names = new ArrayList<>();
            for (DataRecord record : filtered) {
                names.add(record.getString("name"));
            }

            assertEquals(4, names.size());
            assertTrue(names.contains("Bob"));
            assertTrue(names.contains("David"));
            assertTrue(names.contains("Frank"));
            assertTrue(names.contains("Iris"));
        }
    }

    @Test
    void testRowFilterWithMultipleConditions() throws IOException {
        try (DataSource source = DataSourceFactory.createCsv(csvFile);
             FilteredDataSource filtered = FilteredDataSource.builder(source)
                     .rowFilter(record ->
                             record.getDouble("age") >= 20 &&
                                     record.getDouble("score") >= 85.0)
                     .build()) {

            int count = 0;
            for (DataRecord record : filtered) {
                count++;
                double age = record.getDouble("age");
                double score = record.getDouble("score");
                assertTrue(age >= 20);
                assertTrue(score >= 85.0);
            }

            assertEquals(4, count); // Alice, Eve, Grace, Jack
        }
    }

    @Test
    void testRowFilterCombinedWithAnd() throws IOException {
        DataFilter filter1 = record -> record.getDouble("age") >= 25;
        DataFilter filter2 = record -> record.getDouble("score") >= 80.0;
        DataFilter combined = filter1.and(filter2);

        try (DataSource source = DataSourceFactory.createCsv(csvFile);
             FilteredDataSource filtered = FilteredDataSource.builder(source)
                     .rowFilter(combined)
                     .build()) {

            int count = 0;
            for (DataRecord ignored : filtered) {
                count++;
            }

            assertEquals(2, count); // Alice(25,85.5), Grace(28,88.5)
        }
    }

    @Test
    void testRowFilterCombinedWithOr() throws IOException {
        DataFilter filter1 = record -> record.getDouble("age") < 20;
        DataFilter filter2 = record -> record.getDouble("age") > 40;
        DataFilter combined = filter1.or(filter2);

        try (DataSource source = DataSourceFactory.createCsv(csvFile);
             FilteredDataSource filtered = FilteredDataSource.builder(source)
                     .rowFilter(combined)
                     .build()) {

            int count = 0;
            for (DataRecord ignored : filtered) {
                count++;
            }

            assertEquals(3, count); // Charlie(18), Henry(19), David(45)
        }
    }

    @Test
    void testRowFilterNegate() throws IOException {
        DataFilter youngFilter = record -> record.getDouble("age") < 25;
        DataFilter notYoung = youngFilter.negate();

        try (DataSource source = DataSourceFactory.createCsv(csvFile);
             FilteredDataSource filtered = FilteredDataSource.builder(source)
                     .rowFilter(notYoung)
                     .build()) {

            int count = 0;
            for (DataRecord record : filtered) {
                count++;
                assertTrue(record.getDouble("age") >= 25);
            }

            assertEquals(6, count);
        }
    }

    // ============================================================
    // Row Range Tests
    // ============================================================

    @Test
    void testRowRange() throws IOException {
        try (DataSource source = DataSourceFactory.createCsv(csvFile);
             FilteredDataSource filtered = FilteredDataSource.builder(source)
                     .rowRange(2, 5) // rows 2-4 (0-indexed)
                     .build()) {

            List<String> names = new ArrayList<>();
            for (DataRecord record : filtered) {
                names.add(record.getString("name"));
            }

            assertEquals(3, names.size());
            assertEquals("Charlie", names.get(0));
            assertEquals("David", names.get(1));
            assertEquals("Eve", names.get(2));
        }
    }

    @Test
    void testRowRangeFirstThree() throws IOException {
        try (DataSource source = DataSourceFactory.createCsv(csvFile);
             FilteredDataSource filtered = FilteredDataSource.builder(source)
                     .rowRange(0, 3)
                     .build()) {

            int count = 0;
            for (DataRecord ignored : filtered) {
                count++;
            }

            assertEquals(3, count);
        }
    }

    @Test
    void testRowRangeAndFilter() throws IOException {
        try (DataSource source = DataSourceFactory.createCsv(csvFile);
             FilteredDataSource filtered = FilteredDataSource.builder(source)
                     .rowRange(0, 5) // First 5 rows
                     .rowFilter(record -> record.getDouble("score") >= 85.0)
                     .build()) {

            int count = 0;
            for (DataRecord ignored : filtered) {
                count++;
            }

            assertEquals(3, count); // Alice(85.5), Charlie(95.0), Eve(91.0)
        }
    }

    // ============================================================
    // Column Selection Tests
    // ============================================================

    @Test
    void testColumnSelectionByIndices() throws IOException {
        try (DataSource source = DataSourceFactory.createCsv(csvFile);
             FilteredDataSource filtered = FilteredDataSource.builder(source)
                     .columns(ColumnSelector.byIndices(0, 2)) // id, age
                     .build()) {

            assertEquals(2, filtered.getMetadata().getColumnCount());

            for (DataRecord record : filtered) {
                assertEquals(2, record.size());
                // Can still access by new indices
                assertNotNull(record.getValue(0)); // id
                assertNotNull(record.getValue(1)); // age
            }
        }
    }

    @Test
    void testColumnSelectionByNames() throws IOException {
        try (DataSource source = DataSourceFactory.createCsv(csvFile);
             FilteredDataSource filtered = FilteredDataSource.builder(source)
                     .columns(ColumnSelector.byNames("name", "score"))
                     .build()) {

            assertEquals(2, filtered.getMetadata().getColumnCount());
            assertEquals(List.of("name", "score"), filtered.getMetadata().getColumnNames());

            for (DataRecord record : filtered) {
                assertEquals(2, record.size());
                assertNotNull(record.getString("name"));
                assertTrue(record.getDouble("score") > 0);
            }
        }
    }

    @Test
    void testColumnSelectionRange() throws IOException {
        try (DataSource source = DataSourceFactory.createCsv(csvFile);
             FilteredDataSource filtered = FilteredDataSource.builder(source)
                     .columns(ColumnSelector.range(1, 3)) // name, age
                     .build()) {

            assertEquals(2, filtered.getMetadata().getColumnCount());

            for (DataRecord record : filtered) {
                assertEquals(2, record.size());
            }
        }
    }

    @Test
    void testColumnSelectionAll() throws IOException {
        try (DataSource source = DataSourceFactory.createCsv(csvFile);
             FilteredDataSource filtered = FilteredDataSource.builder(source)
                     .columns(ColumnSelector.all())
                     .build()) {

            assertEquals(4, filtered.getMetadata().getColumnCount());

            for (DataRecord record : filtered) {
                assertEquals(4, record.size());
            }
        }
    }

    // ============================================================
    // Combined Filtering Tests
    // ============================================================

    @Test
    void testCombinedFilteringAndProjection() throws IOException {
        try (DataSource source = DataSourceFactory.createCsv(csvFile);
             FilteredDataSource filtered = FilteredDataSource.builder(source)
                     .rowFilter(record -> record.getDouble("age") >= 25)
                     .columns(ColumnSelector.byNames("name", "age"))
                     .build()) {

            assertEquals(2, filtered.getMetadata().getColumnCount());

            int count = 0;
            for (DataRecord record : filtered) {
                count++;
                assertEquals(2, record.size());
                assertTrue(record.getDouble("age") >= 25);
            }

            assertEquals(6, count);
        }
    }

    @Test
    void testComplexCombination() throws IOException {
        try (DataSource source = DataSourceFactory.createCsv(csvFile);
             FilteredDataSource filtered = FilteredDataSource.builder(source)
                     .rowRange(0, 7) // First 7 rows
                     .rowFilter(record ->
                             record.getDouble("age") >= 20 &&
                                     record.getDouble("score") >= 80.0)
                     .columns(ColumnSelector.byIndices(1, 3)) // name, score
                     .build()) {

            assertEquals(2, filtered.getMetadata().getColumnCount());

            List<String> names = new ArrayList<>();
            for (DataRecord record : filtered) {
                names.add(record.getString(0)); // name is now at index 0
                assertTrue(record.getDouble(1) >= 80.0); // score is now at index 1
            }

            // Alice(25,85.5), Eve(22,91.0), Grace(28,88.5)
            assertEquals(3, names.size());
        }
    }

    // ============================================================
    // Expression Filter Tests
    // ============================================================
    // NOTE: These tests use column name bindings since the parser
    // doesn't support $ prefixes for column references

    @Test
    void testExpressionFilterWithBindings() throws IOException {
        Map<String, Integer> bindings = new HashMap<>();
        bindings.put("age", 2);
        bindings.put("score", 3);

        DataFilter filter = ExpressionFilter.compileWithBindings(
                "age >= 25 && score >= 85.0",
                bindings
        );

        try (DataSource source = DataSourceFactory.createCsv(csvFile);
             FilteredDataSource filtered = FilteredDataSource.builder(source)
                     .rowFilter(filter)
                     .build()) {

            int count = 0;
            for (DataRecord record : filtered) {
                count++;
                assertTrue(record.getDouble("age") >= 25);
                assertTrue(record.getDouble("score") >= 85.0);
            }

            // Alice(25,85.5), Grace(28,88.5)
            assertEquals(2, count);
        }
    }

    // ============================================================
    // Edge Cases
    // ============================================================

    @Test
    void testEmptyFilterResult() throws IOException {
        try (DataSource source = DataSourceFactory.createCsv(csvFile);
             FilteredDataSource filtered = FilteredDataSource.builder(source)
                     .rowFilter(record -> record.getDouble("age") > 100)
                     .build()) {

            int count = 0;
            for (DataRecord ignored : filtered) {
                count++;
            }

            assertEquals(0, count);
        }
    }

    @Test
    void testAcceptAllFilter() throws IOException {
        try (DataSource source = DataSourceFactory.createCsv(csvFile);
             FilteredDataSource filtered = FilteredDataSource.builder(source)
                     .rowFilter(DataFilter.acceptAll())
                     .build()) {

            int count = 0;
            for (DataRecord ignored : filtered) {
                count++;
            }

            assertEquals(10, count);
        }
    }

    @Test
    void testRejectAllFilter() throws IOException {
        try (DataSource source = DataSourceFactory.createCsv(csvFile);
             FilteredDataSource filtered = FilteredDataSource.builder(source)
                     .rowFilter(DataFilter.rejectAll())
                     .build()) {

            int count = 0;
            for (DataRecord ignored : filtered) {
                count++;
            }

            assertEquals(0, count);
        }
    }

    @Test
    void testMetadataColumnCountAfterProjection() throws IOException {
        try (DataSource source = DataSourceFactory.createCsv(csvFile);
             FilteredDataSource filtered = FilteredDataSource.builder(source)
                     .columns(ColumnSelector.byIndices(0, 2))
                     .build()) {

            assertEquals(2, filtered.getMetadata().getColumnCount());
            assertFalse(filtered.getMetadata().getRecordCount().isPresent());
        }
    }

    @Test
    void testClosePropagatesToDelegate() throws IOException {
        DataSource source = DataSourceFactory.createCsv(csvFile);
        FilteredDataSource filtered = FilteredDataSource.builder(source)
                .build();

        assertFalse(filtered.isClosed());
        assertFalse(source.isClosed());

        filtered.close();

        assertTrue(filtered.isClosed());
        assertTrue(source.isClosed());
    }

    @Test
    void testIteratorThrowsWhenClosed() throws IOException {
        DataSource source = DataSourceFactory.createCsv(csvFile);
        FilteredDataSource filtered = FilteredDataSource.builder(source)
                .build();

        filtered.close();

        assertThrows(IllegalStateException.class, filtered::iterator);
    }
}
