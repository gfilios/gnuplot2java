package com.gnuplot.core.data.transform;

import java.util.ArrayList;
import java.util.List;

/**
 * Column selector that selects all columns.
 *
 * @since 1.0
 */
class AllColumnsSelector implements ColumnSelector {

    @Override
    public List<Integer> getSelectedIndices(List<String> columnNames) {
        List<Integer> indices = new ArrayList<>(columnNames.size());
        for (int i = 0; i < columnNames.size(); i++) {
            indices.add(i);
        }
        return indices;
    }

    @Override
    public boolean isSelectAll() {
        return true;
    }

    @Override
    public String toString() {
        return "AllColumnsSelector{}";
    }
}
