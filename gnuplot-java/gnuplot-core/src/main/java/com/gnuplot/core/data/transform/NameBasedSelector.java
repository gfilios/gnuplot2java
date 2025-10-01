package com.gnuplot.core.data.transform;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Column selector that selects columns by name.
 *
 * @since 1.0
 */
class NameBasedSelector implements ColumnSelector {

    private final List<String> names;

    NameBasedSelector(String... names) {
        this.names = Arrays.asList(names);
    }

    NameBasedSelector(List<String> names) {
        this.names = new ArrayList<>(names);
    }

    @Override
    public List<Integer> getSelectedIndices(List<String> columnNames) {
        List<Integer> indices = new ArrayList<>();
        for (String name : names) {
            int index = columnNames.indexOf(name);
            if (index >= 0) {
                indices.add(index);
            } else {
                throw new IllegalArgumentException("Column not found: " + name);
            }
        }
        return indices;
    }

    @Override
    public boolean isSelectAll() {
        return false;
    }

    @Override
    public String toString() {
        return "NameBasedSelector{names=" + names + "}";
    }
}
