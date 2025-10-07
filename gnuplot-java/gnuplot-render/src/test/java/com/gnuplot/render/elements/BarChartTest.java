package com.gnuplot.render.elements;

import com.gnuplot.render.SceneElement;
import com.gnuplot.render.SceneElementVisitor;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BarChartTest {

    @Test
    void testBasicBarChartCreation() {
        BarChart chart = BarChart.builder()
                .id("chart1")
                .addBar(1.0, 5.0)
                .addBar(2.0, 8.0)
                .addBar(3.0, 3.0)
                .build();

        assertEquals("chart1", chart.getId());
        assertEquals(3, chart.getBars().size());
        assertEquals(BarChart.Orientation.VERTICAL, chart.getOrientation());
        assertEquals(0.8, chart.getBarWidth());
    }

    @Test
    void testBarWithCustomColor() {
        BarChart chart = BarChart.builder()
                .id("chart1")
                .addBar(1.0, 5.0, "#FF0000")
                .build();

        BarChart.Bar bar = chart.getBars().get(0);
        assertEquals(1.0, bar.getX());
        assertEquals(5.0, bar.getHeight());
        assertEquals("#FF0000", bar.getColor());
    }

    @Test
    void testBarWithColorAndLabel() {
        BarChart chart = BarChart.builder()
                .id("chart1")
                .addBar(1.0, 5.0, "#FF0000", "Sales")
                .build();

        BarChart.Bar bar = chart.getBars().get(0);
        assertEquals(1.0, bar.getX());
        assertEquals(5.0, bar.getHeight());
        assertEquals("#FF0000", bar.getColor());
        assertEquals("Sales", bar.getLabel());
    }

    @Test
    void testAddBarDirectly() {
        BarChart.Bar bar = new BarChart.Bar(1.0, 5.0, "#00FF00", "Revenue");
        BarChart chart = BarChart.builder()
                .id("chart1")
                .addBar(bar)
                .build();

        assertEquals(1, chart.getBars().size());
        assertSame(bar, chart.getBars().get(0));
    }

    @Test
    void testSetBarsList() {
        List<BarChart.Bar> bars = List.of(
                new BarChart.Bar(1.0, 5.0),
                new BarChart.Bar(2.0, 8.0),
                new BarChart.Bar(3.0, 3.0)
        );

        BarChart chart = BarChart.builder()
                .id("chart1")
                .bars(bars)
                .build();

        assertEquals(3, chart.getBars().size());
        assertEquals(5.0, chart.getBars().get(0).getHeight());
        assertEquals(8.0, chart.getBars().get(1).getHeight());
        assertEquals(3.0, chart.getBars().get(2).getHeight());
    }

    @Test
    void testHorizontalOrientation() {
        BarChart chart = BarChart.builder()
                .id("chart1")
                .addBar(1.0, 5.0)
                .orientation(BarChart.Orientation.HORIZONTAL)
                .build();

        assertEquals(BarChart.Orientation.HORIZONTAL, chart.getOrientation());
    }

    @Test
    void testCustomBarWidth() {
        BarChart chart = BarChart.builder()
                .id("chart1")
                .addBar(1.0, 5.0)
                .barWidth(0.5)
                .build();

        assertEquals(0.5, chart.getBarWidth());
    }

    @Test
    void testBarWidthValidation() {
        BarChart.Builder builder = BarChart.builder()
                .id("chart1")
                .addBar(1.0, 5.0);

        assertThrows(IllegalArgumentException.class, () -> builder.barWidth(0.0));
        assertThrows(IllegalArgumentException.class, () -> builder.barWidth(-0.5));
        assertThrows(IllegalArgumentException.class, () -> builder.barWidth(1.5));
    }

    @Test
    void testLabelOptional() {
        BarChart chart = BarChart.builder()
                .id("chart1")
                .addBar(1.0, 5.0)
                .label("Monthly Sales")
                .build();

        assertEquals("Monthly Sales", chart.getLabel());
    }

    @Test
    void testIdRequired() {
        BarChart.Builder builder = BarChart.builder()
                .addBar(1.0, 5.0);

        assertThrows(IllegalStateException.class, builder::build);
    }

    @Test
    void testAtLeastOneBarRequired() {
        BarChart.Builder builder = BarChart.builder()
                .id("chart1");

        assertThrows(IllegalStateException.class, builder::build);
    }

    @Test
    void testBarImmutability() {
        BarChart chart = BarChart.builder()
                .id("chart1")
                .addBar(1.0, 5.0)
                .build();

        assertThrows(UnsupportedOperationException.class,
                () -> chart.getBars().add(new BarChart.Bar(2.0, 3.0)));
    }

    @Test
    void testBarDefaultColor() {
        BarChart.Bar bar = new BarChart.Bar(1.0, 5.0);
        assertEquals("#4A90E2", bar.getColor());
        assertNull(bar.getLabel());
    }

    @Test
    void testBarEquality() {
        BarChart.Bar bar1 = new BarChart.Bar(1.0, 5.0, "#FF0000", "A");
        BarChart.Bar bar2 = new BarChart.Bar(1.0, 5.0, "#FF0000", "A");
        BarChart.Bar bar3 = new BarChart.Bar(1.0, 5.0, "#00FF00", "A");

        assertEquals(bar1, bar2);
        assertNotEquals(bar1, bar3);
        assertEquals(bar1.hashCode(), bar2.hashCode());
    }

    @Test
    void testBarToString() {
        BarChart.Bar bar = new BarChart.Bar(1.5, 5.75, "#FF0000", null);
        String str = bar.toString();
        assertTrue(str.contains("1") && str.contains("5"));
        assertTrue(str.contains("5") && str.contains("7"));
        assertTrue(str.contains("#FF0000"));
    }

    @Test
    void testChartToString() {
        BarChart chart = BarChart.builder()
                .id("sales-chart")
                .addBar(1.0, 5.0)
                .addBar(2.0, 8.0)
                .barWidth(0.6)
                .label("Sales Data")
                .build();

        String str = chart.toString();
        assertTrue(str.contains("sales-chart"));
        assertTrue(str.contains("bars=2"));
        assertTrue(str.contains("VERTICAL"));
        assertTrue(str.contains("0") && str.contains("6"));
        assertTrue(str.contains("Sales Data"));
    }

    @Test
    void testAcceptVisitor() {
        BarChart chart = BarChart.builder()
                .id("chart1")
                .addBar(1.0, 5.0)
                .build();

        TestVisitor visitor = new TestVisitor();
        chart.accept(visitor);
        assertTrue(visitor.visitedBarChart);
    }

    @Test
    void testElementType() {
        BarChart chart = BarChart.builder()
                .id("chart1")
                .addBar(1.0, 5.0)
                .build();

        assertEquals(SceneElement.ElementType.PLOT, chart.getType());
    }

    @Test
    void testNegativeHeights() {
        BarChart chart = BarChart.builder()
                .id("chart1")
                .addBar(1.0, -5.0)
                .addBar(2.0, 3.0)
                .addBar(3.0, -2.0)
                .build();

        assertEquals(-5.0, chart.getBars().get(0).getHeight());
        assertEquals(3.0, chart.getBars().get(1).getHeight());
        assertEquals(-2.0, chart.getBars().get(2).getHeight());
    }

    @Test
    void testNullColorRejected() {
        assertThrows(NullPointerException.class,
                () -> new BarChart.Bar(1.0, 5.0, null, "label"));
    }

    @Test
    void testNullBarRejected() {
        BarChart.Builder builder = BarChart.builder().id("chart1");
        assertThrows(NullPointerException.class, () -> builder.addBar(null));
    }

    @Test
    void testNullBarsListRejected() {
        BarChart.Builder builder = BarChart.builder().id("chart1");
        assertThrows(NullPointerException.class, () -> builder.bars(null));
    }

    @Test
    void testNullOrientationRejected() {
        BarChart.Builder builder = BarChart.builder().id("chart1").addBar(1.0, 5.0);
        assertThrows(NullPointerException.class, () -> builder.orientation(null));
    }

    @Test
    void testNullIdRejected() {
        BarChart.Builder builder = BarChart.builder();
        assertThrows(NullPointerException.class, () -> builder.id(null));
    }

    // BarGroup tests

    @Test
    void testBarGroupCreation() {
        BarChart.BarGroup group = new BarChart.BarGroup(1.0, List.of(5.0, 8.0, 3.0));
        assertEquals(1.0, group.getX());
        assertEquals(3, group.getBarCount());
        assertEquals(List.of(5.0, 8.0, 3.0), group.getValues());
        assertNull(group.getColors());
        assertNull(group.getSeriesLabels());
    }

    @Test
    void testBarGroupWithColors() {
        List<String> colors = List.of("#FF0000", "#00FF00", "#0000FF");
        BarChart.BarGroup group = new BarChart.BarGroup(1.0, List.of(5.0, 8.0, 3.0), colors, null, null);
        assertEquals(colors, group.getColors());
    }

    @Test
    void testBarGroupWithLabels() {
        List<String> labels = List.of("Series A", "Series B", "Series C");
        BarChart.BarGroup group = new BarChart.BarGroup(1.0, List.of(5.0, 8.0, 3.0), null, labels, "Group 1");
        assertEquals(labels, group.getSeriesLabels());
        assertEquals("Group 1", group.getGroupLabel());
    }

    @Test
    void testBarGroupEmptyValuesRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> new BarChart.BarGroup(1.0, List.of()));
    }

    @Test
    void testBarGroupColorsSizeMismatch() {
        List<String> colors = List.of("#FF0000", "#00FF00");
        assertThrows(IllegalArgumentException.class,
                () -> new BarChart.BarGroup(1.0, List.of(5.0, 8.0, 3.0), colors, null, null));
    }

    @Test
    void testBarGroupLabelsSizeMismatch() {
        List<String> labels = List.of("A", "B");
        assertThrows(IllegalArgumentException.class,
                () -> new BarChart.BarGroup(1.0, List.of(5.0, 8.0, 3.0), null, labels, null));
    }

    @Test
    void testBarGroupEquality() {
        BarChart.BarGroup group1 = new BarChart.BarGroup(1.0, List.of(5.0, 8.0));
        BarChart.BarGroup group2 = new BarChart.BarGroup(1.0, List.of(5.0, 8.0));
        BarChart.BarGroup group3 = new BarChart.BarGroup(1.0, List.of(5.0, 9.0));

        assertEquals(group1, group2);
        assertNotEquals(group1, group3);
        assertEquals(group1.hashCode(), group2.hashCode());
    }

    // Grouped bar chart tests

    @Test
    void testGroupedBarChartCreation() {
        BarChart chart = BarChart.builder()
                .id("grouped-chart")
                .addGroup(1.0, List.of(5.0, 8.0, 3.0))
                .addGroup(2.0, List.of(7.0, 4.0, 9.0))
                .build();

        assertEquals("grouped-chart", chart.getId());
        assertEquals(2, chart.getGroups().size());
        assertEquals(BarChart.GroupingMode.GROUPED, chart.getGroupingMode());
        assertTrue(chart.getBars().isEmpty());
    }

    @Test
    void testGroupedBarChartWithColors() {
        List<String> colors = List.of("#FF0000", "#00FF00", "#0000FF");
        BarChart chart = BarChart.builder()
                .id("chart")
                .addGroup(1.0, List.of(5.0, 8.0, 3.0), colors)
                .build();

        assertEquals(1, chart.getGroups().size());
        assertEquals(colors, chart.getGroups().get(0).getColors());
    }

    @Test
    void testStackedBarChartCreation() {
        BarChart chart = BarChart.builder()
                .id("stacked-chart")
                .addGroup(1.0, List.of(5.0, 8.0, 3.0))
                .addGroup(2.0, List.of(7.0, 4.0, 9.0))
                .groupingMode(BarChart.GroupingMode.STACKED)
                .build();

        assertEquals(BarChart.GroupingMode.STACKED, chart.getGroupingMode());
        assertEquals(2, chart.getGroups().size());
    }

    @Test
    void testCannotMixBarsAndGroups() {
        BarChart.Builder builder = BarChart.builder()
                .id("chart")
                .addBar(1.0, 5.0)
                .addGroup(2.0, List.of(3.0, 4.0));

        assertThrows(IllegalStateException.class, builder::build);
    }

    @Test
    void testGroupingModeValidation() {
        BarChart.Builder builder = BarChart.builder().id("chart").addBar(1.0, 5.0);
        assertThrows(NullPointerException.class, () -> builder.groupingMode(null));
    }

    @Test
    void testNullGroupRejected() {
        BarChart.Builder builder = BarChart.builder().id("chart");
        assertThrows(NullPointerException.class, () -> builder.addGroup(null));
    }

    @Test
    void testNullGroupsListRejected() {
        BarChart.Builder builder = BarChart.builder().id("chart");
        assertThrows(NullPointerException.class, () -> builder.groups(null));
    }

    @Test
    void testGroupImmutability() {
        BarChart chart = BarChart.builder()
                .id("chart")
                .addGroup(1.0, List.of(5.0, 8.0))
                .build();

        assertThrows(UnsupportedOperationException.class,
                () -> chart.getGroups().add(new BarChart.BarGroup(2.0, List.of(3.0))));
    }

    // Error bar tests

    @Test
    void testBarWithErrorBars() {
        BarChart.Bar bar = new BarChart.Bar(1.0, 10.0, "#FF0000", "Test", 2.0, 3.0);
        assertEquals(1.0, bar.getX());
        assertEquals(10.0, bar.getHeight());
        assertEquals(2.0, bar.getErrorLow());
        assertEquals(3.0, bar.getErrorHigh());
        assertTrue(bar.hasErrorBars());
    }

    @Test
    void testBarWithOnlyLowError() {
        BarChart.Bar bar = new BarChart.Bar(1.0, 10.0, "#FF0000", "Test", 2.0, null);
        assertEquals(2.0, bar.getErrorLow());
        assertNull(bar.getErrorHigh());
        assertTrue(bar.hasErrorBars());
    }

    @Test
    void testBarWithOnlyHighError() {
        BarChart.Bar bar = new BarChart.Bar(1.0, 10.0, "#FF0000", "Test", null, 3.0);
        assertNull(bar.getErrorLow());
        assertEquals(3.0, bar.getErrorHigh());
        assertTrue(bar.hasErrorBars());
    }

    @Test
    void testBarWithoutErrorBars() {
        BarChart.Bar bar = new BarChart.Bar(1.0, 10.0);
        assertNull(bar.getErrorLow());
        assertNull(bar.getErrorHigh());
        assertFalse(bar.hasErrorBars());
    }

    @Test
    void testChartWithErrorBars() {
        BarChart chart = BarChart.builder()
                .id("chart")
                .addBar(1.0, 10.0, "#FF0000", "A", 2.0, 3.0)
                .addBar(2.0, 15.0, "#00FF00", "B", 1.5, 2.5)
                .build();

        assertEquals(2, chart.getBars().size());
        assertTrue(chart.getBars().get(0).hasErrorBars());
        assertTrue(chart.getBars().get(1).hasErrorBars());
    }

    @Test
    void testBarEqualityWithErrorBars() {
        BarChart.Bar bar1 = new BarChart.Bar(1.0, 10.0, "#FF0000", "Test", 2.0, 3.0);
        BarChart.Bar bar2 = new BarChart.Bar(1.0, 10.0, "#FF0000", "Test", 2.0, 3.0);
        BarChart.Bar bar3 = new BarChart.Bar(1.0, 10.0, "#FF0000", "Test", 1.0, 3.0);

        assertEquals(bar1, bar2);
        assertNotEquals(bar1, bar3);
        assertEquals(bar1.hashCode(), bar2.hashCode());
    }

    private static class TestVisitor implements SceneElementVisitor {
        boolean visitedBarChart = false;

        @Override
        public void visitLinePlot(LinePlot linePlot) {}

        @Override
        public void visitScatterPlot(ScatterPlot scatterPlot) {}

        @Override
        public void visitAxis(Axis axis) {}

        @Override
        public void visitLegend(Legend legend) {}

        @Override
        public void visitSurfacePlot3D(com.gnuplot.render.elements.SurfacePlot3D surfacePlot) {}

        @Override
        public void visitBarChart(BarChart barChart) {
            visitedBarChart = true;
        }
    }
}
