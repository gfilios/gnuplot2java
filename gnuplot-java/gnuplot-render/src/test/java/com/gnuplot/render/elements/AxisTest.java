package com.gnuplot.render.elements;

import com.gnuplot.render.SceneElement;
import com.gnuplot.render.axis.TickGenerator;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for Axis class.
 */
class AxisTest {

    @Test
    void testBuilder() {
        Axis axis = Axis.builder()
                .id("xaxis")
                .axisType(Axis.AxisType.X_AXIS)
                .scaleType(Axis.ScaleType.LINEAR)
                .range(0.0, 10.0)
                .label("X Axis")
                .showGrid(true)
                .build();

        assertEquals("xaxis", axis.getId());
        assertEquals(Axis.AxisType.X_AXIS, axis.getAxisType());
        assertEquals(Axis.ScaleType.LINEAR, axis.getScaleType());
        assertEquals(0.0, axis.getMin());
        assertEquals(10.0, axis.getMax());
        assertEquals("X Axis", axis.getLabel());
        assertTrue(axis.isShowGrid());
    }

    @Test
    void testMinimalAxis() {
        Axis axis = Axis.builder()
                .id("axis1")
                .build();

        assertEquals("axis1", axis.getId());
        assertEquals(Axis.AxisType.X_AXIS, axis.getAxisType());
        assertEquals(Axis.ScaleType.LINEAR, axis.getScaleType());
        assertEquals(0.0, axis.getMin());
        assertEquals(1.0, axis.getMax());
        assertNull(axis.getLabel());
        assertTrue(axis.isShowGrid());
    }

    @Test
    void testYAxis() {
        Axis axis = Axis.builder()
                .id("yaxis")
                .axisType(Axis.AxisType.Y_AXIS)
                .range(-5.0, 5.0)
                .build();

        assertEquals(Axis.AxisType.Y_AXIS, axis.getAxisType());
        assertEquals(-5.0, axis.getMin());
        assertEquals(5.0, axis.getMax());
    }

    @Test
    void testLogarithmicScale() {
        Axis axis = Axis.builder()
                .id("logaxis")
                .scaleType(Axis.ScaleType.LOGARITHMIC)
                .range(1.0, 1000.0)
                .build();

        assertEquals(Axis.ScaleType.LOGARITHMIC, axis.getScaleType());
    }

    @Test
    void testTimeScale() {
        Axis axis = Axis.builder()
                .id("timeaxis")
                .scaleType(Axis.ScaleType.TIME)
                .build();

        assertEquals(Axis.ScaleType.TIME, axis.getScaleType());
    }

    @Test
    void testSecondaryAxes() {
        Axis x2 = Axis.builder()
                .id("x2")
                .axisType(Axis.AxisType.X2_AXIS)
                .build();

        Axis y2 = Axis.builder()
                .id("y2")
                .axisType(Axis.AxisType.Y2_AXIS)
                .build();

        assertEquals(Axis.AxisType.X2_AXIS, x2.getAxisType());
        assertEquals(Axis.AxisType.Y2_AXIS, y2.getAxisType());
    }

    @Test
    void testNoGrid() {
        Axis axis = Axis.builder()
                .id("axis1")
                .showGrid(false)
                .build();

        assertFalse(axis.isShowGrid());
    }

    @Test
    void testElementType() {
        Axis axis = Axis.builder()
                .id("axis1")
                .build();

        assertEquals(SceneElement.ElementType.AXIS, axis.getType());
    }

    @Test
    void testRequiredId() {
        assertThrows(IllegalStateException.class, () ->
                Axis.builder().build());
    }

    @Test
    void testNullId() {
        assertThrows(NullPointerException.class, () ->
                Axis.builder().id(null));
    }

    @Test
    void testInvalidRange() {
        assertThrows(IllegalArgumentException.class, () ->
                Axis.builder()
                        .id("axis1")
                        .range(10.0, 10.0)
                        .build());

        assertThrows(IllegalArgumentException.class, () ->
                Axis.builder()
                        .id("axis1")
                        .range(10.0, 5.0)
                        .build());
    }

    @Test
    void testNullAxisType() {
        assertThrows(NullPointerException.class, () ->
                Axis.builder().axisType(null));
    }

    @Test
    void testNullScaleType() {
        assertThrows(NullPointerException.class, () ->
                Axis.builder().scaleType(null));
    }

    @Test
    void testAllAxisTypes() {
        for (Axis.AxisType type : Axis.AxisType.values()) {
            Axis axis = Axis.builder()
                    .id("axis_" + type)
                    .axisType(type)
                    .build();

            assertEquals(type, axis.getAxisType());
        }
    }

    @Test
    void testAllScaleTypes() {
        for (Axis.ScaleType type : Axis.ScaleType.values()) {
            Axis axis = Axis.builder()
                    .id("axis_" + type)
                    .scaleType(type)
                    .build();

            assertEquals(type, axis.getScaleType());
        }
    }

    @Test
    void testToString() {
        Axis axis = Axis.builder()
                .id("xaxis")
                .axisType(Axis.AxisType.X_AXIS)
                .scaleType(Axis.ScaleType.LINEAR)
                .range(0.0, 100.0)
                .label("Distance (m)")
                .showGrid(true)
                .build();

        String str = axis.toString();
        assertTrue(str.contains("xaxis"));
        assertTrue(str.contains("X_AXIS"));
        assertTrue(str.contains("LINEAR"));
        assertTrue(str.contains("Distance"));
    }

    @Test
    void testLinearTickGeneration() {
        Axis axis = Axis.builder()
                .id("xaxis")
                .range(0.0, 10.0)
                .scaleType(Axis.ScaleType.LINEAR)
                .build();

        List<TickGenerator.Tick> ticks = axis.generateTicks();

        assertFalse(ticks.isEmpty());
        // All ticks should be in range
        for (TickGenerator.Tick tick : ticks) {
            assertTrue(tick.getPosition() >= 0.0);
            assertTrue(tick.getPosition() <= 10.0);
        }
    }

    @Test
    void testLogarithmicTickGeneration() {
        Axis axis = Axis.builder()
                .id("yaxis")
                .range(1.0, 1000.0)
                .scaleType(Axis.ScaleType.LOGARITHMIC)
                .build();

        List<TickGenerator.Tick> ticks = axis.generateTicks();

        assertFalse(ticks.isEmpty());
        // Should have ticks at powers of 10
        boolean has1 = false, has10 = false, has100 = false, has1000 = false;
        for (TickGenerator.Tick tick : ticks) {
            if (Math.abs(tick.getPosition() - 1.0) < 0.01) has1 = true;
            if (Math.abs(tick.getPosition() - 10.0) < 0.01) has10 = true;
            if (Math.abs(tick.getPosition() - 100.0) < 0.01) has100 = true;
            if (Math.abs(tick.getPosition() - 1000.0) < 0.01) has1000 = true;
        }
        assertTrue(has1 && has10 && has100 && has1000);
    }

    @Test
    void testMinorTics() {
        Axis axis = Axis.builder()
                .id("xaxis")
                .range(0.0, 10.0)
                .scaleType(Axis.ScaleType.LINEAR)
                .minorTicsCount(4)
                .build();

        List<TickGenerator.Tick> ticks = axis.generateTicks();

        // Count major and minor ticks
        long majorCount = ticks.stream()
                .filter(t -> t.getType() == TickGenerator.TickType.MAJOR)
                .count();
        long minorCount = ticks.stream()
                .filter(t -> t.getType() == TickGenerator.TickType.MINOR)
                .count();

        assertTrue(majorCount > 0);
        assertTrue(minorCount > 0);
        assertEquals(4, axis.getMinorTicsCount());
    }

    @Test
    void testLogMinorTics() {
        Axis axis = Axis.builder()
                .id("yaxis")
                .range(1.0, 100.0)
                .scaleType(Axis.ScaleType.LOGARITHMIC)
                .minorTicsCount(4)
                .build();

        List<TickGenerator.Tick> ticks = axis.generateTicks();

        long minorCount = ticks.stream()
                .filter(t -> t.getType() == TickGenerator.TickType.MINOR)
                .count();

        assertTrue(minorCount > 0, "Log axis with minor tics should have minor ticks");
    }

    @Test
    void testCustomGuide() {
        Axis axis = Axis.builder()
                .id("xaxis")
                .range(0.0, 100.0)
                .scaleType(Axis.ScaleType.LINEAR)
                .build();

        List<TickGenerator.Tick> ticks1 = axis.generateTicks(10);
        List<TickGenerator.Tick> ticks2 = axis.generateTicks(30);

        // More guide should generally produce more (or equal) major ticks
        long major1 = ticks1.stream().filter(t -> t.getType() == TickGenerator.TickType.MAJOR).count();
        long major2 = ticks2.stream().filter(t -> t.getType() == TickGenerator.TickType.MAJOR).count();

        assertTrue(major2 >= major1);
    }

    @Test
    void testShowTicksDefault() {
        Axis axis = Axis.builder()
                .id("xaxis")
                .build();

        assertTrue(axis.isShowTicks());
    }

    @Test
    void testShowTicksFalse() {
        Axis axis = Axis.builder()
                .id("xaxis")
                .showTicks(false)
                .build();

        assertFalse(axis.isShowTicks());
    }

    @Test
    void testMinorTicsCountDefault() {
        Axis axis = Axis.builder()
                .id("xaxis")
                .build();

        assertEquals(0, axis.getMinorTicsCount());
    }

    @Test
    void testNegativeMinorTicsCount() {
        assertThrows(IllegalArgumentException.class, () ->
                Axis.builder()
                        .id("xaxis")
                        .minorTicsCount(-1)
                        .build());
    }

    @Test
    void testCustomTickGenerator() {
        TickGenerator customGenerator = new TickGenerator();
        Axis axis = Axis.builder()
                .id("xaxis")
                .range(0.0, 10.0)
                .tickGenerator(customGenerator)
                .build();

        List<TickGenerator.Tick> ticks = axis.generateTicks();
        assertFalse(ticks.isEmpty());
    }

    @Test
    void testNullTickGenerator() {
        assertThrows(NullPointerException.class, () ->
                Axis.builder()
                        .id("xaxis")
                        .tickGenerator(null)
                        .build());
    }

    @Test
    void testTimeScaleTickGeneration() {
        // TIME scale should fall back to linear for now
        Axis axis = Axis.builder()
                .id("timeaxis")
                .range(0.0, 100.0)
                .scaleType(Axis.ScaleType.TIME)
                .build();

        List<TickGenerator.Tick> ticks = axis.generateTicks();
        assertFalse(ticks.isEmpty());
    }
}
