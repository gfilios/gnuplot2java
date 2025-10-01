package com.gnuplot.core.data.transform;

import java.util.List;

/**
 * Defines which columns to include in a filtered data source.
 * Supports selection by index or by name.
 *
 * <p>Example usage:
 * <pre>{@code
 * // Select specific columns by index
 * ColumnSelector selector = ColumnSelector.byIndices(0, 2, 4);
 *
 * // Select columns by name
 * ColumnSelector selector = ColumnSelector.byNames("time", "temperature", "pressure");
 *
 * // Select all columns
 * ColumnSelector selector = ColumnSelector.all();
 *
 * // Select range of columns
 * ColumnSelector selector = ColumnSelector.range(0, 5); // columns 0-4
 * }</pre>
 *
 * @since 1.0
 */
public interface ColumnSelector {

    /**
     * Returns the indices of selected columns.
     * If selection is by name, indices are resolved using metadata.
     *
     * @param columnNames available column names from metadata
     * @return list of selected column indices
     */
    List<Integer> getSelectedIndices(List<String> columnNames);

    /**
     * Returns true if this selector selects all columns.
     *
     * @return true if all columns are selected
     */
    boolean isSelectAll();

    /**
     * Creates a selector that selects all columns.
     *
     * @return selector that selects all columns
     */
    static ColumnSelector all() {
        return new AllColumnsSelector();
    }

    /**
     * Creates a selector that selects specific columns by index.
     *
     * @param indices zero-based column indices
     * @return selector for the specified indices
     */
    static ColumnSelector byIndices(int... indices) {
        return new IndexBasedSelector(indices);
    }

    /**
     * Creates a selector that selects specific columns by index.
     *
     * @param indices zero-based column indices
     * @return selector for the specified indices
     */
    static ColumnSelector byIndices(List<Integer> indices) {
        return new IndexBasedSelector(indices);
    }

    /**
     * Creates a selector that selects columns by name.
     *
     * @param names column names
     * @return selector for the specified names
     */
    static ColumnSelector byNames(String... names) {
        return new NameBasedSelector(names);
    }

    /**
     * Creates a selector that selects columns by name.
     *
     * @param names column names
     * @return selector for the specified names
     */
    static ColumnSelector byNames(List<String> names) {
        return new NameBasedSelector(names);
    }

    /**
     * Creates a selector that selects a range of columns.
     *
     * @param start start index (inclusive)
     * @param end   end index (exclusive)
     * @return selector for the specified range
     */
    static ColumnSelector range(int start, int end) {
        return new RangeSelector(start, end);
    }
}
