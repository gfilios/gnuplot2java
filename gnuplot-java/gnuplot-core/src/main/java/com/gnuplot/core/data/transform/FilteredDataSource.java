package com.gnuplot.core.data.transform;

import com.gnuplot.core.data.DataMetadata;
import com.gnuplot.core.data.DataRecord;
import com.gnuplot.core.data.DataSource;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * DataSource decorator that applies filtering and column selection.
 * Uses decorator pattern to wrap an existing DataSource and provide
 * filtered/projected views of the data.
 *
 * <p>Example usage:
 * <pre>{@code
 * // Filter rows where first column > 10
 * DataSource filtered = FilteredDataSource.builder(source)
 *     .rowFilter(record -> record.getDouble(0) > 10)
 *     .build();
 *
 * // Select specific columns
 * DataSource projected = FilteredDataSource.builder(source)
 *     .columns(ColumnSelector.byNames("time", "temperature"))
 *     .build();
 *
 * // Row range selection
 * DataSource ranged = FilteredDataSource.builder(source)
 *     .rowRange(10, 100) // rows 10-99
 *     .build();
 *
 * // Combined filtering
 * DataSource combined = FilteredDataSource.builder(source)
 *     .rowFilter(record -> record.getDouble("age") >= 18)
 *     .columns(ColumnSelector.byIndices(0, 2, 4))
 *     .rowRange(0, 1000)
 *     .build();
 * }</pre>
 *
 * @since 1.0
 */
public class FilteredDataSource implements DataSource {

    private final DataSource delegate;
    private final DataFilter rowFilter;
    private final ColumnSelector columnSelector;
    private final int startRow;
    private final int endRow;
    private final FilteredMetadata metadata;
    private boolean closed = false;

    private FilteredDataSource(Builder builder) {
        this.delegate = builder.delegate;
        this.rowFilter = builder.rowFilter;
        this.columnSelector = builder.columnSelector;
        this.startRow = builder.startRow;
        this.endRow = builder.endRow;
        this.metadata = new FilteredMetadata(
                delegate.getMetadata(),
                columnSelector
        );
    }

    @Override
    public DataMetadata getMetadata() {
        return metadata;
    }

    @Override
    public Iterator<DataRecord> iterator() {
        if (closed) {
            throw new IllegalStateException("DataSource has been closed");
        }
        return new FilteredIterator();
    }

    @Override
    public void close() {
        if (!closed) {
            delegate.close();
            closed = true;
        }
    }

    @Override
    public boolean isClosed() {
        return closed;
    }

    /**
     * Creates a new builder for FilteredDataSource.
     *
     * @param delegate the data source to filter
     * @return builder instance
     */
    public static Builder builder(DataSource delegate) {
        return new Builder(delegate);
    }

    /**
     * Builder for FilteredDataSource.
     */
    public static class Builder {
        private final DataSource delegate;
        private DataFilter rowFilter = DataFilter.acceptAll();
        private ColumnSelector columnSelector = ColumnSelector.all();
        private int startRow = 0;
        private int endRow = Integer.MAX_VALUE;

        private Builder(DataSource delegate) {
            this.delegate = delegate;
        }

        /**
         * Sets the row filter.
         *
         * @param filter row filter
         * @return this builder
         */
        public Builder rowFilter(DataFilter filter) {
            this.rowFilter = filter;
            return this;
        }

        /**
         * Sets the column selector.
         *
         * @param selector column selector
         * @return this builder
         */
        public Builder columns(ColumnSelector selector) {
            this.columnSelector = selector;
            return this;
        }

        /**
         * Sets the row range (inclusive start, exclusive end).
         *
         * @param start start row index (inclusive)
         * @param end   end row index (exclusive)
         * @return this builder
         */
        public Builder rowRange(int start, int end) {
            if (start < 0) {
                throw new IllegalArgumentException("Start row must be >= 0");
            }
            if (end <= start) {
                throw new IllegalArgumentException("End row must be > start row");
            }
            this.startRow = start;
            this.endRow = end;
            return this;
        }

        /**
         * Builds the FilteredDataSource.
         *
         * @return filtered data source
         */
        public FilteredDataSource build() {
            return new FilteredDataSource(this);
        }
    }

    /**
     * Iterator that applies filtering to records.
     */
    private class FilteredIterator implements Iterator<DataRecord> {
        private final Iterator<DataRecord> delegateIterator;
        private DataRecord nextRecord;
        private int currentRowIndex = -1;
        private int includedRowIndex = -1;

        FilteredIterator() {
            this.delegateIterator = delegate.iterator();
            advance();
        }

        private void advance() {
            nextRecord = null;

            while (delegateIterator.hasNext()) {
                currentRowIndex++;
                DataRecord record = delegateIterator.next();

                // Check row range
                if (currentRowIndex < startRow) {
                    continue;
                }
                if (currentRowIndex >= endRow) {
                    break;
                }

                // Apply row filter
                if (!rowFilter.test(record)) {
                    continue;
                }

                // Record passed all filters
                includedRowIndex++;

                // Apply column selection if needed
                if (columnSelector.isSelectAll()) {
                    nextRecord = record;
                } else {
                    nextRecord = new ProjectedRecord(record, metadata);
                }

                break;
            }
        }

        @Override
        public boolean hasNext() {
            return nextRecord != null;
        }

        @Override
        public DataRecord next() {
            if (!hasNext()) {
                throw new NoSuchElementException("No more records");
            }

            DataRecord result = nextRecord;
            advance();
            return result;
        }
    }

    /**
     * Metadata for filtered data source.
     */
    private static class FilteredMetadata implements DataMetadata {
        private final DataMetadata delegate;
        private final ColumnSelector columnSelector;
        private final List<Integer> selectedIndices;
        private final List<String> selectedColumnNames;

        FilteredMetadata(DataMetadata delegate, ColumnSelector columnSelector) {
            this.delegate = delegate;
            this.columnSelector = columnSelector;

            // Resolve selected columns
            this.selectedIndices = columnSelector.getSelectedIndices(
                    delegate.getColumnNames()
            );

            // Build selected column names
            this.selectedColumnNames = new ArrayList<>();
            for (int index : selectedIndices) {
                delegate.getColumnName(index).ifPresent(selectedColumnNames::add);
            }
        }

        @Override
        public int getColumnCount() {
            return columnSelector.isSelectAll()
                    ? delegate.getColumnCount()
                    : selectedIndices.size();
        }

        @Override
        public List<String> getColumnNames() {
            return columnSelector.isSelectAll()
                    ? delegate.getColumnNames()
                    : new ArrayList<>(selectedColumnNames);
        }

        @Override
        public java.util.Optional<String> getColumnName(int index) {
            if (columnSelector.isSelectAll()) {
                return delegate.getColumnName(index);
            }
            if (index < 0 || index >= selectedColumnNames.size()) {
                return java.util.Optional.empty();
            }
            return java.util.Optional.of(selectedColumnNames.get(index));
        }

        @Override
        public java.util.Optional<Integer> getColumnIndex(String columnName) {
            if (columnSelector.isSelectAll()) {
                return delegate.getColumnIndex(columnName);
            }
            int index = selectedColumnNames.indexOf(columnName);
            return index >= 0
                    ? java.util.Optional.of(index)
                    : java.util.Optional.empty();
        }

        @Override
        public boolean hasHeader() {
            return delegate.hasHeader();
        }

        @Override
        public java.util.Optional<Long> getRecordCount() {
            // Cannot estimate after filtering
            return java.util.Optional.empty();
        }

        @Override
        public String getSourceIdentifier() {
            return delegate.getSourceIdentifier();
        }

        @Override
        public java.util.Optional<String> getProperty(String key) {
            return delegate.getProperty(key);
        }

        List<Integer> getSelectedIndices() {
            return new ArrayList<>(selectedIndices);
        }
    }

    /**
     * Record that projects only selected columns.
     */
    private static class ProjectedRecord implements DataRecord {
        private final DataRecord delegate;
        private final FilteredMetadata metadata;
        private final List<Integer> selectedIndices;

        ProjectedRecord(DataRecord delegate, FilteredMetadata metadata) {
            this.delegate = delegate;
            this.metadata = metadata;
            this.selectedIndices = metadata.getSelectedIndices();
        }

        @Override
        public Object getValue(int index) {
            if (index < 0 || index >= selectedIndices.size()) {
                throw new IndexOutOfBoundsException("Index: " + index);
            }
            int actualIndex = selectedIndices.get(index);
            return delegate.getValue(actualIndex);
        }

        @Override
        public Object getValue(String columnName) {
            int index = metadata.getColumnIndex(columnName)
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Column not found: " + columnName));
            return getValue(index);
        }

        @Override
        public double getDouble(int index) {
            if (index < 0 || index >= selectedIndices.size()) {
                throw new IndexOutOfBoundsException("Index: " + index);
            }
            int actualIndex = selectedIndices.get(index);
            return delegate.getDouble(actualIndex);
        }

        @Override
        public double getDouble(String columnName) {
            int index = metadata.getColumnIndex(columnName)
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Column not found: " + columnName));
            return getDouble(index);
        }

        @Override
        public String getString(int index) {
            if (index < 0 || index >= selectedIndices.size()) {
                throw new IndexOutOfBoundsException("Index: " + index);
            }
            int actualIndex = selectedIndices.get(index);
            return delegate.getString(actualIndex);
        }

        @Override
        public String getString(String columnName) {
            int index = metadata.getColumnIndex(columnName)
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Column not found: " + columnName));
            return getString(index);
        }

        @Override
        public List<Object> getValues() {
            List<Object> values = new ArrayList<>();
            for (int index : selectedIndices) {
                values.add(delegate.getValue(index));
            }
            return values;
        }

        @Override
        public int size() {
            return selectedIndices.size();
        }

        @Override
        public boolean isNull(int index) {
            if (index < 0 || index >= selectedIndices.size()) {
                throw new IndexOutOfBoundsException("Index: " + index);
            }
            int actualIndex = selectedIndices.get(index);
            return delegate.isNull(actualIndex);
        }

        @Override
        public boolean isNull(String columnName) {
            int index = metadata.getColumnIndex(columnName)
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Column not found: " + columnName));
            return isNull(index);
        }
    }
}
