package com.gnuplot.render.projection;

import com.gnuplot.render.elements.Point3D;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a 3D coordinate axis with ticks and labels.
 * Used for rendering X, Y, Z axes in 3D plots.
 */
public class Axis3D {

    public enum AxisType {
        X, Y, Z
    }

    private final AxisType type;
    private final Point3D start;
    private final Point3D end;
    private final double min;
    private final double max;
    private final List<AxisTick> ticks;

    public Axis3D(AxisType type, Point3D start, Point3D end, double min, double max) {
        this.type = type;
        this.start = start;
        this.end = end;
        this.min = min;
        this.max = max;
        this.ticks = new ArrayList<>();
        generateTicks();
    }

    private void generateTicks() {
        // Generate approximately 5 tick marks
        int numTicks = 5;
        double range = max - min;
        double step = range / (numTicks - 1);

        for (int i = 0; i < numTicks; i++) {
            double value = min + i * step;
            double t = i / (double)(numTicks - 1); // Parameter along axis [0, 1]

            // Interpolate position along axis
            Point3D position = new Point3D(
                start.x() + t * (end.x() - start.x()),
                start.y() + t * (end.y() - start.y()),
                start.z() + t * (end.z() - start.z())
            );

            ticks.add(new AxisTick(value, position));
        }
    }

    public AxisType getType() {
        return type;
    }

    public Point3D getStart() {
        return start;
    }

    public Point3D getEnd() {
        return end;
    }

    public List<AxisTick> getTicks() {
        return ticks;
    }

    /**
     * Represents a tick mark on a 3D axis.
     */
    public static class AxisTick {
        private final double value;
        private final Point3D position;

        public AxisTick(double value, Point3D position) {
            this.value = value;
            this.position = position;
        }

        public double getValue() {
            return value;
        }

        public Point3D getPosition() {
            return position;
        }

        public String getLabel() {
            // Format label to match C gnuplot - integers without decimal, others minimal decimals
            double rounded = Math.round(value);
            if (Math.abs(value - rounded) < 1e-9) {
                // Value is effectively an integer
                return String.format("%d", (long) rounded);
            }
            // Non-integer: use 1 decimal place, strip trailing zeros
            String formatted = String.format("%.1f", value);
            return formatted.replaceAll("\\.0$", "");
        }
    }
}
