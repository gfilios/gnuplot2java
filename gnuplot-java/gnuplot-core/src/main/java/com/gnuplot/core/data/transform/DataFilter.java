package com.gnuplot.core.data.transform;

import com.gnuplot.core.data.DataRecord;

/**
 * Functional interface for filtering data records.
 * Used to determine which records should be included in filtered results.
 *
 * <p>Example usage:
 * <pre>{@code
 * // Filter records where first column > 10
 * DataFilter filter = record -> record.getDouble(0) > 10;
 *
 * // Filter records where "age" column >= 18
 * DataFilter filter = record -> record.getDouble("age") >= 18;
 *
 * // Combine filters
 * DataFilter combined = filter1.and(filter2).or(filter3);
 * }</pre>
 *
 * @since 1.0
 */
@FunctionalInterface
public interface DataFilter {

    /**
     * Tests whether a record should be included in the filtered results.
     *
     * @param record the data record to test
     * @return true if the record passes the filter
     */
    boolean test(DataRecord record);

    /**
     * Returns a filter that represents the logical AND of this filter and another.
     *
     * @param other the other filter
     * @return combined filter
     */
    default DataFilter and(DataFilter other) {
        return record -> this.test(record) && other.test(record);
    }

    /**
     * Returns a filter that represents the logical OR of this filter and another.
     *
     * @param other the other filter
     * @return combined filter
     */
    default DataFilter or(DataFilter other) {
        return record -> this.test(record) || other.test(record);
    }

    /**
     * Returns a filter that represents the logical negation of this filter.
     *
     * @return negated filter
     */
    default DataFilter negate() {
        return record -> !this.test(record);
    }

    /**
     * Returns a filter that accepts all records.
     *
     * @return filter that accepts all records
     */
    static DataFilter acceptAll() {
        return record -> true;
    }

    /**
     * Returns a filter that rejects all records.
     *
     * @return filter that rejects all records
     */
    static DataFilter rejectAll() {
        return record -> false;
    }
}
