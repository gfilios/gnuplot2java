package com.gnuplot.core.data;

import java.io.Closeable;
import java.util.Iterator;

/**
 * Interface for reading data from various sources (CSV, JSON, binary, etc.).
 * Provides unified access to tabular data for plotting.
 *
 * <p>DataSource implementations should be:
 * <ul>
 *   <li>Iterable - Support streaming through large datasets</li>
 *   <li>Closeable - Properly manage resources (files, connections)</li>
 *   <li>Thread-safe - If accessed from multiple threads</li>
 * </ul>
 *
 * @since 1.0
 */
public interface DataSource extends Iterable<DataRecord>, Closeable {

    /**
     * Returns metadata about this data source.
     *
     * @return metadata describing the structure and properties
     */
    DataMetadata getMetadata();

    /**
     * Returns an iterator over data records.
     * For streaming sources, records are loaded on-demand.
     *
     * @return iterator over data records
     */
    @Override
    Iterator<DataRecord> iterator();

    /**
     * Closes this data source and releases any system resources.
     */
    @Override
    void close();

    /**
     * Returns true if this data source has been closed.
     *
     * @return true if closed
     */
    boolean isClosed();
}
