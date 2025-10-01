/**
 * CSV data source implementation with streaming support for large files.
 *
 * <p>Key features:
 * <ul>
 *   <li>RFC 4180 compliant parsing with extensions</li>
 *   <li>Configurable delimiters, quote characters, and escape sequences</li>
 *   <li>Header row support with named column access</li>
 *   <li>Streaming iterator for memory-efficient large file processing</li>
 *   <li>Comment line and empty line skipping</li>
 * </ul>
 *
 * <p>Example usage:
 * <pre>{@code
 * CsvConfig config = CsvConfig.builder()
 *     .delimiter(',')
 *     .hasHeader(true)
 *     .skipEmptyLines(true)
 *     .build();
 *
 * try (CsvDataSource csv = new CsvDataSource(Paths.get("data.csv"), config)) {
 *     DataMetadata meta = csv.getMetadata();
 *     System.out.println("Columns: " + meta.getColumnNames());
 *
 *     for (DataRecord record : csv) {
 *         double x = record.getDouble("x");
 *         double y = record.getDouble("y");
 *         // process data...
 *     }
 * }
 * }</pre>
 *
 * @since 1.0
 */
package com.gnuplot.core.data.csv;
