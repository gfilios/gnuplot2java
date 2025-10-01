package com.gnuplot.core.data;

import java.util.List;
import java.util.Optional;

/**
 * Metadata describing the structure and properties of a data source.
 *
 * @since 1.0
 */
public interface DataMetadata {

    /**
     * Returns the number of columns in the data.
     *
     * @return column count
     */
    int getColumnCount();

    /**
     * Returns the names of all columns, if available.
     * Empty if the data source doesn't have named columns.
     *
     * @return list of column names
     */
    List<String> getColumnNames();

    /**
     * Returns the name of the column at the specified index.
     *
     * @param index zero-based column index
     * @return column name, or empty if not available
     */
    Optional<String> getColumnName(int index);

    /**
     * Returns the index of the column with the specified name.
     *
     * @param columnName name of the column
     * @return column index, or empty if not found
     */
    Optional<Integer> getColumnIndex(String columnName);

    /**
     * Returns true if the data source has a header row with column names.
     *
     * @return true if header is present
     */
    boolean hasHeader();

    /**
     * Returns the estimated number of records, if known.
     * Returns empty for streaming sources where count is unknown.
     *
     * @return estimated record count
     */
    Optional<Long> getRecordCount();

    /**
     * Returns the source identifier (filename, URL, etc.).
     *
     * @return source identifier
     */
    String getSourceIdentifier();

    /**
     * Returns additional properties specific to the data source type.
     *
     * @param key property key
     * @return property value, or empty if not present
     */
    Optional<String> getProperty(String key);
}
