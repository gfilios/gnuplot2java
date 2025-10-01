/**
 * JSON data source implementation with simple path navigation.
 *
 * <p>Key features:
 * <ul>
 *   <li>Read JSON files with Jackson</li>
 *   <li>Simple path extraction for nested data ($.field.nested.path)</li>
 *   <li>Array-of-objects and array-of-arrays support</li>
 *   <li>Nested object navigation</li>
 *   <li>Type conversion (Number, String, Boolean)</li>
 * </ul>
 *
 * <p>Example usage:
 * <pre>{@code
 * // Simple array of objects
 * String json = "[{\"x\": 1, \"y\": 2}, {\"x\": 3, \"y\": 4}]";
 * JsonConfig config = JsonConfig.defaults();
 *
 * try (JsonDataSource source = new JsonDataSource(
 *         new StringReader(json), config, "data.json")) {
 *     for (DataRecord record : source) {
 *         double x = record.getDouble("x");
 *         double y = record.getDouble("y");
 *         // process data...
 *     }
 * }
 *
 * // Extract nested data with simple path
 * JsonConfig config = JsonConfig.builder()
 *     .dataPath("$.results.measurements")
 *     .build();
 *
 * try (JsonDataSource source = new JsonDataSource(path, config)) {
 *     // Process extracted array
 * }
 * }</pre>
 *
 * @since 1.0
 */
package com.gnuplot.core.data.json;
