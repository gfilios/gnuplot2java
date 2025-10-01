package com.gnuplot.core.data;

import java.util.List;

/**
 * Represents a single record (row) of data.
 * Provides both indexed and named access to fields.
 *
 * @since 1.0
 */
public interface DataRecord {

    /**
     * Returns the value at the specified column index.
     *
     * @param index zero-based column index
     * @return value at the specified index, or null if not present
     * @throws IndexOutOfBoundsException if index is out of range
     */
    Object getValue(int index);

    /**
     * Returns the value for the specified column name.
     *
     * @param columnName name of the column
     * @return value for the column, or null if not present
     * @throws IllegalArgumentException if column name doesn't exist
     */
    Object getValue(String columnName);

    /**
     * Returns the value at the specified index as a double.
     * Performs type conversion if necessary.
     *
     * @param index zero-based column index
     * @return numeric value
     * @throws NumberFormatException if value cannot be converted
     * @throws IndexOutOfBoundsException if index is out of range
     */
    double getDouble(int index);

    /**
     * Returns the value for the specified column as a double.
     *
     * @param columnName name of the column
     * @return numeric value
     * @throws NumberFormatException if value cannot be converted
     * @throws IllegalArgumentException if column name doesn't exist
     */
    double getDouble(String columnName);

    /**
     * Returns the value at the specified index as a string.
     *
     * @param index zero-based column index
     * @return string value
     * @throws IndexOutOfBoundsException if index is out of range
     */
    String getString(int index);

    /**
     * Returns the value for the specified column as a string.
     *
     * @param columnName name of the column
     * @return string value
     * @throws IllegalArgumentException if column name doesn't exist
     */
    String getString(String columnName);

    /**
     * Returns all values in this record as a list.
     *
     * @return list of values
     */
    List<Object> getValues();

    /**
     * Returns the number of fields in this record.
     *
     * @return field count
     */
    int size();

    /**
     * Returns true if the value at the specified index is null or missing.
     *
     * @param index zero-based column index
     * @return true if value is null or missing
     */
    boolean isNull(int index);

    /**
     * Returns true if the value for the specified column is null or missing.
     *
     * @param columnName name of the column
     * @return true if value is null or missing
     */
    boolean isNull(String columnName);
}
