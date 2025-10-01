package com.gnuplot.render.elements;

import com.gnuplot.render.SceneElement;
import org.junit.jupiter.api.Test;

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
}
