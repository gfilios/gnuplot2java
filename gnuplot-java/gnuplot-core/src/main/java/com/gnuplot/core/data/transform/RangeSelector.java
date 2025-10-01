package com.gnuplot.core.data.transform;

import java.util.ArrayList;
import java.util.List;

/**
 * Column selector that selects a range of columns.
 *
 * @since 1.0
 */
class RangeSelector implements ColumnSelector {

    private final int start;
    private final int end;

    RangeSelector(int start, int end) {
        if (start < 0) {
            throw new IllegalArgumentException("Start index must be >= 0");
        }
        if (end <= start) {
            throw new IllegalArgumentException("End index must be > start index");
        }
        this.start = start;
        this.end = end;
    }

    @Override
    public List<Integer> getSelectedIndices(List<String> columnNames) {
        List<Integer> indices = new ArrayList<>();
        int actualEnd = Math.min(end, columnNames.size());
        for (int i = start; i < actualEnd; i++) {
            indices.add(i);
        }
        return indices;
    }

    @Override
    public boolean isSelectAll() {
        return false;
    }

    @Override
    public String toString() {
        return "RangeSelector{start=" + start + ", end=" + end + "}";
    }
}
