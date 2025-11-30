package com.gnuplot.render.axis;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
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
     * Generates tick marks for a time-based axis.
     *
     * <p>Time ticks are placed at "nice" intervals based on the time range:
     * seconds, minutes, hours, days, weeks, months, or years.</p>
     *
     * @param minTime The minimum time value (Unix timestamp in seconds)
     * @param maxTime The maximum time value (Unix timestamp in seconds)
     * @param guide The approximate maximum number of ticks
     * @return A list of tick marks for the time scale
     */
    public List<Tick> generateTimeTicks(double minTime, double maxTime, int guide) {
        List<Tick> ticks = new ArrayList<>();

        if (minTime >= maxTime) {
            return ticks; // Invalid range
        }

        // Convert to Instant for time manipulation
        Instant minInstant = Instant.ofEpochSecond((long) minTime);
        Instant maxInstant = Instant.ofEpochSecond((long) maxTime);

        // Calculate time span in seconds
        long spanSeconds = (long) (maxTime - minTime);

        // Determine appropriate time unit and interval
        TimeInterval interval = determineTimeInterval(spanSeconds, guide);

        // Generate ticks at the determined interval
        LocalDateTime current = LocalDateTime.ofInstant(minInstant, ZoneId.systemDefault());
        LocalDateTime end = LocalDateTime.ofInstant(maxInstant, ZoneId.systemDefault());

        // Round current to interval boundary
        current = roundToTimeInterval(current, interval);

        while (!current.isAfter(end)) {
            long timestamp = current.atZone(ZoneId.systemDefault()).toEpochSecond();
            if (timestamp >= minTime && timestamp <= maxTime) {
                String label = formatTimeLabel(current, interval);
                ticks.add(new Tick(timestamp, label, TickType.MAJOR));
            }
            current = advanceByInterval(current, interval);
        }

        return ticks;
    }

    /**
     * Generates tick marks for a time-based axis with default guide parameter.
     *
     * @param minTime The minimum time value (Unix timestamp in seconds)
     * @param maxTime The maximum time value (Unix timestamp in seconds)
     * @return A list of tick marks for the time scale
     */
    public List<Tick> generateTimeTicks(double minTime, double maxTime) {
        return generateTimeTicks(minTime, maxTime, DEFAULT_GUIDE);
    }

    /**
     * Time interval enumeration for tick placement.
     */
    private enum TimeInterval {
        SECOND(1),
        FIVE_SECONDS(5),
        TEN_SECONDS(10),
        THIRTY_SECONDS(30),
        MINUTE(60),
        FIVE_MINUTES(300),
        TEN_MINUTES(600),
        THIRTY_MINUTES(1800),
        HOUR(3600),
        SIX_HOURS(21600),
        TWELVE_HOURS(43200),
        DAY(86400),
        WEEK(604800),
        MONTH(2592000),  // Approximate (30 days)
        YEAR(31536000);  // Approximate (365 days)

        final long seconds;

        TimeInterval(long seconds) {
            this.seconds = seconds;
        }
    }

    /**
     * Determines the appropriate time interval based on the span and desired tick count.
     */
    private TimeInterval determineTimeInterval(long spanSeconds, int guide) {
        // Target interval in seconds
        long targetInterval = spanSeconds / guide;

        // Find the closest "nice" interval
        TimeInterval[] intervals = TimeInterval.values();
        TimeInterval best = intervals[0];
        long bestDiff = Math.abs(intervals[0].seconds - targetInterval);

        for (TimeInterval interval : intervals) {
            long diff = Math.abs(interval.seconds - targetInterval);
            if (diff < bestDiff) {
                bestDiff = diff;
                best = interval;
            }
        }

        return best;
    }

    /**
     * Rounds a time to the nearest interval boundary.
     */
    private LocalDateTime roundToTimeInterval(LocalDateTime time, TimeInterval interval) {
        switch (interval) {
            case SECOND:
            case FIVE_SECONDS:
            case TEN_SECONDS:
            case THIRTY_SECONDS:
                int seconds = (time.getSecond() / (int) interval.seconds) * (int) interval.seconds;
                return time.withSecond(seconds).withNano(0);

            case MINUTE:
            case FIVE_MINUTES:
            case TEN_MINUTES:
            case THIRTY_MINUTES:
                int minutes = (time.getMinute() / ((int) interval.seconds / 60)) * ((int) interval.seconds / 60);
                return time.withMinute(minutes).withSecond(0).withNano(0);

            case HOUR:
            case SIX_HOURS:
            case TWELVE_HOURS:
                int hours = (time.getHour() / ((int) interval.seconds / 3600)) * ((int) interval.seconds / 3600);
                return time.withHour(hours).withMinute(0).withSecond(0).withNano(0);

            case DAY:
                return time.withHour(0).withMinute(0).withSecond(0).withNano(0);

            case WEEK:
                return time.withHour(0).withMinute(0).withSecond(0).withNano(0)
                        .minusDays(time.getDayOfWeek().getValue() - 1);

            case MONTH:
                return time.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0);

            case YEAR:
                return time.withDayOfYear(1).withHour(0).withMinute(0).withSecond(0).withNano(0);

            default:
                return time;
        }
    }

    /**
     * Advances time by the specified interval.
     */
    private LocalDateTime advanceByInterval(LocalDateTime time, TimeInterval interval) {
        switch (interval) {
            case SECOND:
            case FIVE_SECONDS:
            case TEN_SECONDS:
            case THIRTY_SECONDS:
            case MINUTE:
            case FIVE_MINUTES:
            case TEN_MINUTES:
            case THIRTY_MINUTES:
            case HOUR:
            case SIX_HOURS:
            case TWELVE_HOURS:
            case DAY:
            case WEEK:
                return time.plus(interval.seconds, ChronoUnit.SECONDS);

            case MONTH:
                return time.plusMonths(1);

            case YEAR:
                return time.plusYears(1);

            default:
                return time;
        }
    }

    /**
     * Formats a time label based on the interval.
     */
    private String formatTimeLabel(LocalDateTime time, TimeInterval interval) {
        switch (interval) {
            case SECOND:
            case FIVE_SECONDS:
            case TEN_SECONDS:
            case THIRTY_SECONDS:
                return time.format(DateTimeFormatter.ofPattern("HH:mm:ss"));

            case MINUTE:
            case FIVE_MINUTES:
            case TEN_MINUTES:
            case THIRTY_MINUTES:
                return time.format(DateTimeFormatter.ofPattern("HH:mm"));

            case HOUR:
            case SIX_HOURS:
            case TWELVE_HOURS:
                return time.format(DateTimeFormatter.ofPattern("HH:mm"));

            case DAY:
                return time.format(DateTimeFormatter.ofPattern("MMM dd"));

            case WEEK:
                return time.format(DateTimeFormatter.ofPattern("MMM dd"));

            case MONTH:
                return time.format(DateTimeFormatter.ofPattern("MMM yyyy"));

            case YEAR:
                return time.format(DateTimeFormatter.ofPattern("yyyy"));

            default:
                return time.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
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
     * Generates tick marks for a linear axis using a pre-computed tick step.
     * This is useful when the tick step was calculated from the original data range
     * before the axis was extended to tick boundaries (like C gnuplot does).
     *
     * @param min The minimum value of the axis range (may be extended)
     * @param max The maximum value of the axis range (may be extended)
     * @param tickStep The pre-computed tick spacing
     * @return A list of tick marks
     */
    public List<Tick> generateTicksWithStep(double min, double max, double tickStep) {
        List<Tick> ticks = new ArrayList<>();

        if (min >= max || tickStep <= 0) {
            return ticks; // Invalid range or step
        }

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

        return ticks;
    }

    /**
     * Calculate the tick step for a given range and guide.
     * This is useful for extending axis ranges to tick boundaries.
     *
     * @param min The minimum value
     * @param max The maximum value
     * @param guide The approximate maximum number of ticks
     * @return The tick spacing
     */
    public double calculateTickStep(double min, double max, int guide) {
        double range = Math.abs(max - min);
        return quantizeNormalTics(range, guide);
    }

    /**
     * Extend axis range to next tick boundary (gnuplot's round_outward behavior).
     * Ported from gnuplot-c/src/axis.c:round_outward()
     *
     * <p>This extends the axis range so that the min is rounded down to the nearest
     * tick boundary and max is rounded up to the nearest tick boundary.</p>
     *
     * @param min The current minimum value
     * @param max The current maximum value
     * @param tickStep The tick spacing
     * @return A 2-element array: [extended_min, extended_max]
     */
    public double[] extendRangeToTicks(double min, double max, double tickStep) {
        // Round min down to next tick boundary (floor)
        double extendedMin = Math.floor(min / tickStep) * tickStep;

        // Round max up to next tick boundary (ceil)
        double extendedMax = Math.ceil(max / tickStep) * tickStep;

        return new double[] { extendedMin, extendedMax };
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

        // Check if value is effectively an integer (matches C gnuplot behavior)
        // This avoids showing -1.0 when -1 would suffice
        double rounded = Math.round(value);
        if (Math.abs(value - rounded) < 1e-9) {
            // Value is an integer, format without decimals
            return String.format(Locale.US, "%d", (long) rounded);
        }

        // Determine appropriate number of decimal places for non-integers
        int decimalPlaces = 0;
        if (tickStep < 0.1) {
            decimalPlaces = (int) Math.ceil(-Math.log10(tickStep));
        } else if (tickStep < 1.0) {
            decimalPlaces = 1;
        }

        // Format with minimal decimal places, removing trailing zeros
        String format = String.format(Locale.US, "%%.%df", decimalPlaces);
        String formatted = String.format(Locale.US, format, value);

        // Remove trailing zeros after decimal point (but keep at least one decimal if decimalPlaces > 0)
        if (formatted.contains(".")) {
            formatted = formatted.replaceAll("0+$", "").replaceAll("\\.$", "");
        }

        return formatted;
    }
}
