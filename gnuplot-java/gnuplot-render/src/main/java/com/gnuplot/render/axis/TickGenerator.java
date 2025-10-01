package com.gnuplot.render.axis;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Generates tick marks and labels for axes based on gnuplot's tick spacing algorithm.
 *
 * <p>This class implements the quantize_normal_tics algorithm from gnuplot's axis.c,
 * which automatically determines appropriate tick spacing based on the axis range.
 * The algorithm ensures ticks are placed at "nice" intervals (0.05, 0.1, 0.2, 0.5, 1, 2, 5, 10, etc.)
 * that are easy to read and understand.</p>
 *
 * <p>Example usage:</p>
 * <pre>
 * TickGenerator generator = new TickGenerator();
 * List&lt;Tick&gt; ticks = generator.generateTicks(0.0, 10.0, 20);
 * </pre>
 */
public class TickGenerator {

    /**
     * Represents a single tick mark on an axis.
     */
    public static class Tick {
        private final double position;
        private final String label;
        private final TickType type;

        /**
         * Creates a new tick mark.
         *
         * @param position The position along the axis (in data coordinates)
         * @param label The label to display for this tick
         * @param type The type of tick (major or minor)
         */
        public Tick(double position, String label, TickType type) {
            this.position = position;
            this.label = label;
            this.type = type;
        }

        public double getPosition() {
            return position;
        }

        public String getLabel() {
            return label;
        }

        public TickType getType() {
            return type;
        }
    }

    /**
     * Type of tick mark.
     */
    public enum TickType {
        MAJOR,  // Major tick with label
        MINOR   // Minor tick without label
    }

    /**
     * Default guide parameter (maximum number of ticks).
     * This value of 20 matches gnuplot's default behavior.
     */
    private static final int DEFAULT_GUIDE = 20;

    /**
     * Default number of minor tics between major tics.
     * Setting to 0 disables minor tics.
     */
    private static final int DEFAULT_MINOR_TICS = 0;

    /**
     * Generates tick marks for a linear axis.
     *
     * @param min The minimum value of the axis range
     * @param max The maximum value of the axis range
     * @return A list of tick marks
     */
    public List<Tick> generateTicks(double min, double max) {
        return generateTicks(min, max, DEFAULT_GUIDE, DEFAULT_MINOR_TICS);
    }

    /**
     * Generates tick marks for a linear axis with a specified maximum number of ticks.
     *
     * @param min The minimum value of the axis range
     * @param max The maximum value of the axis range
     * @param guide The approximate maximum number of ticks (typically 20)
     * @return A list of tick marks
     */
    public List<Tick> generateTicks(double min, double max, int guide) {
        return generateTicks(min, max, guide, DEFAULT_MINOR_TICS);
    }

    /**
     * Generates tick marks at custom specified positions.
     *
     * @param positions Array of positions where ticks should be placed
     * @return A list of major tick marks at the specified positions
     */
    public List<Tick> generateCustomTicks(double[] positions) {
        List<Tick> ticks = new ArrayList<>();

        if (positions == null || positions.length == 0) {
            return ticks;
        }

        // Sort positions
        double[] sortedPositions = positions.clone();
        java.util.Arrays.sort(sortedPositions);

        // Calculate a reasonable tick step for formatting
        double range = sortedPositions[sortedPositions.length - 1] - sortedPositions[0];
        double avgSpacing = range / (sortedPositions.length - 1);

        for (double position : sortedPositions) {
            String label = formatTickLabel(position, avgSpacing);
            ticks.add(new Tick(position, label, TickType.MAJOR));
        }

        return ticks;
    }

    /**
     * Generates tick marks for a logarithmic axis.
     *
     * <p>For logarithmic axes, major ticks are placed at powers of the base (typically 10),
     * and minor ticks are placed at integer multiples within each decade.</p>
     *
     * @param min The minimum value of the axis range (must be positive)
     * @param max The maximum value of the axis range (must be positive and > min)
     * @param base The logarithmic base (typically 10)
     * @param includeMinorTics Whether to include minor ticks
     * @return A list of tick marks for the logarithmic scale
     */
    public List<Tick> generateLogTicks(double min, double max, double base, boolean includeMinorTics) {
        List<Tick> ticks = new ArrayList<>();

        if (min <= 0 || max <= 0 || min >= max || base <= 1) {
            return ticks; // Invalid range for log scale
        }

        // Find the power of base for the first tick
        int firstPower = (int) Math.floor(Math.log(min) / Math.log(base));
        int lastPower = (int) Math.ceil(Math.log(max) / Math.log(base));

        // Generate major ticks at powers of base
        for (int power = firstPower; power <= lastPower; power++) {
            double position = Math.pow(base, power);
            if (position >= min && position <= max) {
                String label = formatLogTickLabel(position, base);
                ticks.add(new Tick(position, label, TickType.MAJOR));
            }
        }

        // Generate minor ticks if requested
        if (includeMinorTics) {
            List<Tick> allTicks = new ArrayList<>();

            for (int power = firstPower; power <= lastPower; power++) {
                double decadeStart = Math.pow(base, power);

                // Add minor ticks within this decade
                for (int mult = 1; mult < (int) base; mult++) {
                    double position = decadeStart * mult;
                    if (position >= min && position <= max) {
                        if (mult == 1) {
                            // This is a major tick
                            String label = formatLogTickLabel(position, base);
                            allTicks.add(new Tick(position, label, TickType.MAJOR));
                        } else {
                            // This is a minor tick
                            allTicks.add(new Tick(position, "", TickType.MINOR));
                        }
                    }
                }
            }

            return allTicks;
        }

        return ticks;
    }

    /**
     * Generates tick marks for a logarithmic axis with base 10.
     *
     * @param min The minimum value of the axis range (must be positive)
     * @param max The maximum value of the axis range (must be positive and > min)
     * @return A list of major tick marks for the log10 scale
     */
    public List<Tick> generateLogTicks(double min, double max) {
        return generateLogTicks(min, max, 10.0, false);
    }

    /**
     * Formats a tick label for logarithmic scales.
     *
     * <p>For values that are exact powers of the base, uses exponential notation.
     * For other values, uses decimal notation.</p>
     *
     * @param value The tick value
     * @param base The logarithmic base
     * @return The formatted label
     */
    private String formatLogTickLabel(double value, double base) {
        // Check if this is a power of the base
        double logValue = Math.log(value) / Math.log(base);
        int power = (int) Math.round(logValue);

        if (Math.abs(logValue - power) < 0.001) {
            // This is a power of the base
            if (base == 10.0) {
                if (power == 0) {
                    return "1";
                } else if (power == 1) {
                    return "10";
                } else {
                    return String.format(Locale.US, "10^%d", power);
                }
            } else {
                return String.format(Locale.US, "%.0f^%d", base, power);
            }
        } else {
            // Not a power of the base, use decimal notation
            if (value >= 1000 || value < 0.01) {
                return String.format(Locale.US, "%.1e", value);
            } else if (value >= 1) {
                return String.format(Locale.US, "%.0f", value);
            } else {
                return String.format(Locale.US, "%.2f", value);
            }
        }
    }

    /**
     * Generates tick marks at custom specified positions with custom labels.
     *
     * @param positions Array of positions where ticks should be placed
     * @param labels Array of labels for each position (must be same length as positions)
     * @return A list of major tick marks at the specified positions with custom labels
     */
    public List<Tick> generateCustomTicks(double[] positions, String[] labels) {
        List<Tick> ticks = new ArrayList<>();

        if (positions == null || positions.length == 0) {
            return ticks;
        }

        if (labels != null && labels.length != positions.length) {
            throw new IllegalArgumentException("Labels array must be same length as positions array");
        }

        // Create array of indices to sort by position
        Integer[] indices = new Integer[positions.length];
        for (int i = 0; i < indices.length; i++) {
            indices[i] = i;
        }
        java.util.Arrays.sort(indices, (a, b) -> Double.compare(positions[a], positions[b]));

        for (int idx : indices) {
            String label = (labels != null) ? labels[idx] : String.valueOf(positions[idx]);
            ticks.add(new Tick(positions[idx], label, TickType.MAJOR));
        }

        return ticks;
    }

    /**
     * Generates tick marks for a linear axis with major and minor ticks.
     *
     * @param min The minimum value of the axis range
     * @param max The maximum value of the axis range
     * @param guide The approximate maximum number of major ticks (typically 20)
     * @param minorTics The number of minor tics between major tics (0 to disable)
     * @return A list of tick marks including both major and minor ticks
     */
    public List<Tick> generateTicks(double min, double max, int guide, int minorTics) {
        List<Tick> ticks = new ArrayList<>();

        if (min >= max) {
            return ticks; // Invalid range
        }

        // Calculate tick spacing using gnuplot's algorithm
        double range = Math.abs(max - min);
        double tickStep = quantizeNormalTics(range, guide);

        // Find the first tick position (rounded down to nearest tick step)
        double firstTick = Math.floor(min / tickStep) * tickStep;
        if (firstTick < min) {
            firstTick += tickStep;
        }

        // Generate major tick marks
        double position = firstTick;
        double epsilon = tickStep * 1e-9; // Very small epsilon for floating point comparison
        while (position <= max + epsilon) {
            if (position >= min - epsilon) {
                // Clamp position to range to avoid floating point errors
                double clampedPosition = Math.min(Math.max(position, min), max);
                String label = formatTickLabel(clampedPosition, tickStep);
                ticks.add(new Tick(clampedPosition, label, TickType.MAJOR));
            }
            position += tickStep;
        }

        // Generate minor tick marks if requested
        if (minorTics > 0) {
            List<Tick> allTicks = new ArrayList<>();
            double minorStep = tickStep / (minorTics + 1);

            // For each interval between major ticks, add minor ticks
            for (int i = 0; i < ticks.size(); i++) {
                // Add the major tick
                allTicks.add(ticks.get(i));

                // Add minor ticks between this major tick and the next
                if (i < ticks.size() - 1) {
                    double majorPos = ticks.get(i).getPosition();
                    for (int j = 1; j <= minorTics; j++) {
                        double minorPos = majorPos + j * minorStep;
                        // Clamp to range
                        minorPos = Math.min(Math.max(minorPos, min), max);
                        // Only add if within range and not too close to next major tick
                        if (minorPos < ticks.get(i + 1).getPosition() - epsilon) {
                            allTicks.add(new Tick(minorPos, "", TickType.MINOR));
                        }
                    }
                }
            }
            return allTicks;
        }

        return ticks;
    }

    /**
     * Implements gnuplot's quantize_normal_tics algorithm.
     *
     * <p>This algorithm determines the "nice" tick spacing for a given range.
     * It calculates the order of magnitude of the range and then chooses a
     * tick interval that will produce approximately the requested number of ticks.</p>
     *
     * <p>The chosen intervals are always one of: 0.05, 0.1, 0.2, 0.5, 1, 2, 5
     * multiplied by an appropriate power of 10.</p>
     *
     * @param range The range of the axis
     * @param guide The approximate maximum number of ticks
     * @return The tick spacing
     */
    private double quantizeNormalTics(double range, int guide) {
        // Order of magnitude of the range
        double power = Math.pow(10.0, Math.floor(Math.log10(range)));

        // Normalize range to [1, 10)
        double xnorm = range / power;

        // Approximate number of tick positions per decade
        double posns = guide / xnorm;

        // Choose tick interval based on desired number of ticks
        double tics;
        if (posns > 40) {
            tics = 0.05;    // e.g., 0, 0.05, 0.10, ...
        } else if (posns > 20) {
            tics = 0.1;     // e.g., 0, 0.1, 0.2, ...
        } else if (posns > 10) {
            tics = 0.2;     // e.g., 0, 0.2, 0.4, ...
        } else if (posns > 4) {
            tics = 0.5;     // e.g., 0, 0.5, 1, ...
        } else if (posns > 2) {
            tics = 1;       // e.g., 0, 1, 2, ...
        } else if (posns > 0.5) {
            tics = 2;       // e.g., 0, 2, 4, ...
        } else {
            // Getting desperate... ceil to make sure we go over rather than under
            tics = Math.ceil(xnorm);
        }

        return tics * power;
    }

    /**
     * Formats a tick label with appropriate precision.
     *
     * <p>The precision is chosen based on the tick step to avoid unnecessary
     * decimal places while ensuring all ticks can be distinguished.</p>
     *
     * @param value The tick value
     * @param tickStep The tick spacing
     * @return The formatted label
     */
    private String formatTickLabel(double value, double tickStep) {
        // Handle values very close to zero
        if (Math.abs(value) < tickStep * 0.001) {
            return "0";
        }

        // Determine appropriate number of decimal places
        int decimalPlaces = 0;
        if (tickStep < 0.1) {
            decimalPlaces = (int) Math.ceil(-Math.log10(tickStep));
        } else if (tickStep < 1.0) {
            decimalPlaces = 1;
        }

        // Format the value
        if (decimalPlaces == 0) {
            // No decimal places needed
            long rounded = Math.round(value);
            return String.format(Locale.US, "%d", rounded);
        } else {
            // Use appropriate decimal places
            String format = String.format(Locale.US, "%%.%df", decimalPlaces);
            return String.format(Locale.US, format, value);
        }
    }
}
