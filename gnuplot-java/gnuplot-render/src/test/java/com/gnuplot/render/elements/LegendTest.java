package com.gnuplot.render.elements;

import com.gnuplot.render.SceneElement;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for Legend class.
 */
class LegendTest {

    @Test
    void testBuilder() {
        Legend legend = Legend.builder()
                .id("legend1")
                .position(Legend.Position.TOP_RIGHT)
                .addEntry("Series 1", "#FF0000", LinePlot.LineStyle.SOLID)
                .addEntry("Series 2", "#00FF00", LinePlot.LineStyle.DASHED)
                .showBorder(true)
                .build();

        assertEquals("legend1", legend.getId());
        assertEquals(Legend.Position.TOP_RIGHT, legend.getPosition());
        assertEquals(2, legend.getEntries().size());
        assertTrue(legend.isShowBorder());
    }

    @Test
    void testMinimalLegend() {
        Legend legend = Legend.builder()
                .id("legend1")
                .build();

        assertEquals("legend1", legend.getId());
        assertEquals(Legend.Position.TOP_RIGHT, legend.getPosition());
        assertEquals(0, legend.getEntries().size());
        assertTrue(legend.isShowBorder());
    }

    @Test
    void testLegendEntry() {
        Legend.LegendEntry entry = new Legend.LegendEntry(
                "Test", "#0000FF", LinePlot.LineStyle.DOTTED);

        assertEquals("Test", entry.getLabel());
        assertEquals("#0000FF", entry.getColor());
        assertEquals(LinePlot.LineStyle.DOTTED, entry.getLineStyle());
    }

    @Test
    void testAddEntryObject() {
        Legend.LegendEntry entry = new Legend.LegendEntry(
                "Test", "#0000FF", LinePlot.LineStyle.DOTTED);

        Legend legend = Legend.builder()
                .id("legend1")
                .addEntry(entry)
                .build();

        assertEquals(1, legend.getEntries().size());
        assertEquals(entry, legend.getEntries().get(0));
    }

    @Test
    void testAllPositions() {
        for (Legend.Position pos : Legend.Position.values()) {
            Legend legend = Legend.builder()
                    .id("legend_" + pos)
                    .position(pos)
                    .build();

            assertEquals(pos, legend.getPosition());
        }
    }

    @Test
    void testNoBorder() {
        Legend legend = Legend.builder()
                .id("legend1")
                .showBorder(false)
                .build();

        assertFalse(legend.isShowBorder());
    }

    @Test
    void testElementType() {
        Legend legend = Legend.builder()
                .id("legend1")
                .build();

        assertEquals(SceneElement.ElementType.LEGEND, legend.getType());
    }

    @Test
    void testImmutableEntries() {
        Legend legend = Legend.builder()
                .id("legend1")
                .addEntry("Test", "#000000", LinePlot.LineStyle.SOLID)
                .build();

        assertThrows(UnsupportedOperationException.class, () ->
                legend.getEntries().add(new Legend.LegendEntry(
                        "New", "#FFFFFF", LinePlot.LineStyle.DASHED)));
    }

    @Test
    void testRequiredId() {
        assertThrows(IllegalStateException.class, () ->
                Legend.builder().build());
    }

    @Test
    void testNullId() {
        assertThrows(NullPointerException.class, () ->
                Legend.builder().id(null));
    }

    @Test
    void testNullPosition() {
        assertThrows(NullPointerException.class, () ->
                Legend.builder().position(null));
    }

    @Test
    void testNullEntry() {
        assertThrows(NullPointerException.class, () ->
                Legend.builder().addEntry(null));
    }

    @Test
    void testNullEntryFields() {
        assertThrows(NullPointerException.class, () ->
                new Legend.LegendEntry(null, "#000000", LinePlot.LineStyle.SOLID));

        assertThrows(NullPointerException.class, () ->
                new Legend.LegendEntry("Test", null, LinePlot.LineStyle.SOLID));

        assertThrows(NullPointerException.class, () ->
                new Legend.LegendEntry("Test", "#000000", null));
    }

    @Test
    void testMultipleEntries() {
        Legend legend = Legend.builder()
                .id("legend1")
                .addEntry("Line 1", "#FF0000", LinePlot.LineStyle.SOLID)
                .addEntry("Line 2", "#00FF00", LinePlot.LineStyle.DASHED)
                .addEntry("Line 3", "#0000FF", LinePlot.LineStyle.DOTTED)
                .build();

        assertEquals(3, legend.getEntries().size());
        assertEquals("Line 1", legend.getEntries().get(0).getLabel());
        assertEquals("Line 2", legend.getEntries().get(1).getLabel());
        assertEquals("Line 3", legend.getEntries().get(2).getLabel());
    }

    @Test
    void testEntryToString() {
        Legend.LegendEntry entry = new Legend.LegendEntry(
                "Test Series", "#FF8800", LinePlot.LineStyle.DASH_DOT);

        String str = entry.toString();
        assertTrue(str.contains("Test Series"));
        assertTrue(str.contains("#FF8800"));
        assertTrue(str.contains("DASH_DOT"));
    }

    @Test
    void testToString() {
        Legend legend = Legend.builder()
                .id("legend1")
                .position(Legend.Position.BOTTOM_LEFT)
                .addEntry("Series", "#000000", LinePlot.LineStyle.SOLID)
                .showBorder(false)
                .build();

        String str = legend.toString();
        assertTrue(str.contains("legend1"));
        assertTrue(str.contains("BOTTOM_LEFT"));
        assertTrue(str.contains("1")); // entry count
        assertTrue(str.contains("false")); // border
    }
}
