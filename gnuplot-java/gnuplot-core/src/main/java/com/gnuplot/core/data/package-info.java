/**
 * Data source abstraction layer for gnuplot-java.
 *
 * <h2>Overview</h2>
 * This package provides a unified interface for reading data from various sources
 * (CSV, JSON, binary, etc.) with support for streaming, metadata access, and
 * pluggable custom data sources.
 *
 * <h2>Core Interfaces</h2>
 * <ul>
 *   <li>{@link com.gnuplot.core.data.DataSource} - Main interface for data sources</li>
 *   <li>{@link com.gnuplot.core.data.DataRecord} - Represents a single data record</li>
 *   <li>{@link com.gnuplot.core.data.DataMetadata} - Metadata about a data source</li>
 *   <li>{@link com.gnuplot.core.data.DataSourceProvider} - SPI for custom data sources</li>
 * </ul>
 *
 * <h2>Usage Examples</h2>
 *
 * <h3>Basic Usage - Automatic Format Detection</h3>
 * <pre>{@code
 * // Read CSV file
 * try (DataSource ds = DataSourceFactory.create(Path.of("data.csv"))) {
 *     for (DataRecord record : ds) {
 *         double x = record.getDouble(0);
 *         double y = record.getDouble(1);
 *         System.out.println("x=" + x + ", y=" + y);
 *     }
 * }
 * }</pre>
 *
 * <h3>CSV with Custom Configuration</h3>
 * <pre>{@code
 * CsvConfig config = CsvConfig.builder()
 *     .delimiter('\t')           // Tab-separated
 *     .hasHeader(true)           // First row is header
 *     .commentPrefix("#")        // Lines starting with # are comments
 *     .skipEmptyLines(true)      // Skip empty lines
 *     .build();
 *
 * try (DataSource ds = DataSourceFactory.createCsv(path, config)) {
 *     // Access columns by name
 *     for (DataRecord record : ds) {
 *         double x = record.getDouble("time");
 *         double y = record.getDouble("value");
 *         System.out.println("time=" + x + ", value=" + y);
 *     }
 * }
 * }</pre>
 *
 * <h3>JSON with Path Extraction</h3>
 * <pre>{@code
 * // Extract nested data: {"results": {"data": [{"x":1, "y":2}, ...]}}
 * JsonConfig config = JsonConfig.builder()
 *     .dataPath("$.results.data")
 *     .build();
 *
 * try (DataSource ds = DataSourceFactory.createJson(path, config)) {
 *     for (DataRecord record : ds) {
 *         double x = record.getDouble("x");
 *         double y = record.getDouble("y");
 *         System.out.println("x=" + x + ", y=" + y);
 *     }
 * }
 * }</pre>
 *
 * <h3>Working with Metadata</h3>
 * <pre>{@code
 * try (DataSource ds = DataSourceFactory.create(path)) {
 *     DataMetadata meta = ds.getMetadata();
 *
 *     System.out.println("Source: " + meta.getSourceIdentifier());
 *     System.out.println("Columns: " + meta.getColumnCount());
 *     System.out.println("Has header: " + meta.hasHeader());
 *
 *     if (meta.hasHeader()) {
 *         System.out.println("Column names: " + meta.getColumnNames());
 *     }
 *
 *     meta.getRecordCount().ifPresent(count ->
 *         System.out.println("Estimated records: " + count));
 * }
 * }</pre>
 *
 * <h2>Extending with Custom Data Sources</h2>
 *
 * <h3>Step 1: Implement DataSourceProvider</h3>
 * <pre>{@code
 * public class XmlDataSourceProvider implements DataSourceProvider {
 *     @Override
 *     public DataSource create(Path path) throws IOException {
 *         return new XmlDataSource(path, XmlConfig.defaults());
 *     }
 * }
 * }</pre>
 *
 * <h3>Step 2: Implement DataSource</h3>
 * <pre>{@code
 * public class XmlDataSource implements DataSource {
 *     private final Document document;
 *     private final XmlMetadata metadata;
 *     private boolean closed = false;
 *
 *     public XmlDataSource(Path path, XmlConfig config) throws IOException {
 *         // Parse XML and build document
 *         this.document = parseXml(path);
 *         this.metadata = new XmlMetadata(path.toString(), document);
 *     }
 *
 *     @Override
 *     public DataMetadata getMetadata() {
 *         return metadata;
 *     }
 *
 *     @Override
 *     public Iterator<DataRecord> iterator() {
 *         if (closed) {
 *             throw new IllegalStateException("DataSource has been closed");
 *         }
 *         return new XmlIterator(document);
 *     }
 *
 *     @Override
 *     public void close() {
 *         closed = true;
 *     }
 *
 *     @Override
 *     public boolean isClosed() {
 *         return closed;
 *     }
 * }
 * }</pre>
 *
 * <h3>Step 3: Implement DataRecord</h3>
 * <pre>{@code
 * public class XmlRecord implements DataRecord {
 *     private final Element element;
 *     private final XmlMetadata metadata;
 *
 *     @Override
 *     public Object getValue(int index) {
 *         // Extract value from XML element
 *     }
 *
 *     @Override
 *     public double getDouble(int index) {
 *         Object value = getValue(index);
 *         if (value instanceof Number) {
 *             return ((Number) value).doubleValue();
 *         }
 *         return Double.parseDouble(value.toString());
 *     }
 *
 *     // Implement other methods...
 * }
 * }</pre>
 *
 * <h3>Step 4: Implement DataMetadata</h3>
 * <pre>{@code
 * public class XmlMetadata implements DataMetadata {
 *     private final String sourceIdentifier;
 *     private final List<String> columnNames;
 *
 *     public XmlMetadata(String sourceIdentifier, Document document) {
 *         this.sourceIdentifier = sourceIdentifier;
 *         this.columnNames = extractColumnNames(document);
 *     }
 *
 *     @Override
 *     public int getColumnCount() {
 *         return columnNames.size();
 *     }
 *
 *     @Override
 *     public List<String> getColumnNames() {
 *         return Collections.unmodifiableList(columnNames);
 *     }
 *
 *     // Implement other methods...
 * }
 * }</pre>
 *
 * <h3>Step 5: Register with Factory</h3>
 * <pre>{@code
 * // In your application startup code
 * DataSourceFactory.registerProvider("xml", new XmlDataSourceProvider());
 *
 * // Now XML files can be read automatically
 * try (DataSource ds = DataSourceFactory.create(Path.of("data.xml"))) {
 *     for (DataRecord record : ds) {
 *         // Process data...
 *     }
 * }
 * }</pre>
 *
 * <h2>Built-in Data Sources</h2>
 * <ul>
 *   <li><b>CSV</b> - Comma-separated values ({@link com.gnuplot.core.data.csv})</li>
 *   <li><b>JSON</b> - JavaScript Object Notation ({@link com.gnuplot.core.data.json})</li>
 * </ul>
 *
 * <h2>Design Principles</h2>
 * <ul>
 *   <li><b>Streaming</b> - Support for large files through iterators</li>
 *   <li><b>Type Safety</b> - Strong typing with runtime type conversion</li>
 *   <li><b>Resource Management</b> - Closeable interface for proper cleanup</li>
 *   <li><b>Extensibility</b> - Service provider interface for custom formats</li>
 *   <li><b>Metadata</b> - Rich metadata about data structure and properties</li>
 * </ul>
 *
 * <h2>Thread Safety</h2>
 * <p>
 * DataSource implementations are <b>not thread-safe</b> by default. If multiple threads
 * need to access the same data, use synchronization or create separate DataSource
 * instances per thread.
 * </p>
 *
 * <h2>Error Handling</h2>
 * <p>
 * Data source operations throw the following exceptions:
 * </p>
 * <ul>
 *   <li>{@link java.io.IOException} - I/O errors during reading</li>
 *   <li>{@link com.gnuplot.core.data.UnsupportedFormatException} - Unsupported file format</li>
 *   <li>{@link java.lang.IllegalStateException} - Invalid state (e.g., closed source)</li>
 *   <li>{@link java.lang.NumberFormatException} - Type conversion errors</li>
 * </ul>
 *
 * @since 1.0
 * @see com.gnuplot.core.data.DataSourceFactory
 * @see com.gnuplot.core.data.csv
 * @see com.gnuplot.core.data.json
 */
package com.gnuplot.core.data;
