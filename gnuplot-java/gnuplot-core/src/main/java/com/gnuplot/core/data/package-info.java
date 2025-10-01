/**
 * Data import and processing API.
 *
 * <p>Provides unified interface for reading data from various sources:
 * <ul>
 *   <li>CSV files ({@link com.gnuplot.core.data.csv})</li>
 *   <li>JSON data (future)</li>
 *   <li>Binary formats (future)</li>
 *   <li>Excel files (future)</li>
 * </ul>
 *
 * <p>Core interfaces:
 * <ul>
 *   <li>{@link com.gnuplot.core.data.DataSource} - Main interface for data sources</li>
 *   <li>{@link com.gnuplot.core.data.DataRecord} - Represents a single record/row</li>
 *   <li>{@link com.gnuplot.core.data.DataMetadata} - Metadata about the data structure</li>
 * </ul>
 *
 * @since 1.0
 */
package com.gnuplot.core.data;
