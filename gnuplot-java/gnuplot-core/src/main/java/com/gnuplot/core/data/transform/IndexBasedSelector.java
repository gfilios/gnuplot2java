package com.gnuplot.core.data.transform;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Column selector that selects columns by index.
 *
 * @since 1.0
 */
class IndexBasedSelector implements ColumnSelector {

    private final List<Integer> indices;

    IndexBasedSelector(int... indices) {
        this.indices = new ArrayList<>();
        for (int index : indices) {
            this.indices.add(index);
        }
    }

    IndexBasedSelector(List<Integer> indices) {
        this.indices = new ArrayList<>(indices);
    }

    @Override
    public List<Integer> getSelectedIndices(List<String> columnNames) {
        return new ArrayList<>(indices);
    }

    @Override
    public boolean isSelectAll() {
        return false;
    }

    @Override
    public String toString() {
        return "IndexBasedSelector{indices=" + indices + "}";
    }
}
