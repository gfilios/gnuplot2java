package com.gnuplot.render.axis;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the TickGenerator class.
 */
class TickGeneratorTest {

    private final TickGenerator generator = new TickGenerator();

    @Test
    void testBasicRange() {
        List<TickGenerator.Tick> ticks = generator.generateTicks(0.0, 10.0);

        assertFalse(ticks.isEmpty());
        assertTrue(ticks.size() >= 5 && ticks.size() <= 20);

        // Verify all ticks are within range
        for (TickGenerator.Tick tick : ticks) {
            assertTrue(tick.getPosition() >= 0.0);
            assertTrue(tick.getPosition() <= 10.0);
        }
    }

    @Test
    void testSmallRange() {
        List<TickGenerator.Tick> ticks = generator.generateTicks(0.0, 1.0);

        assertFalse(ticks.isEmpty());

        // Should have ticks at nice intervals like 0, 0.2, 0.4, 0.6, 0.8, 1.0
        assertTrue(ticks.size() >= 3);
        assertTrue(ticks.size() <= 15);
    }

    @Test
    void testLargeRange() {
        List<TickGenerator.Tick> ticks = generator.generateTicks(0.0, 1000.0);

        assertFalse(ticks.isEmpty());

        // Should have reasonable number of ticks
        assertTrue(ticks.size() >= 3);
        assertTrue(ticks.size() <= 25);

        // Verify tick spacing is appropriate
        if (ticks.size() >= 2) {
            double spacing = ticks.get(1).getPosition() - ticks.get(0).getPosition();
            assertTrue(spacing > 0);
            // Spacing should be a nice round number like 50, 100, 200, etc.
            assertTrue(spacing >= 50);
        }
    }

    @Test
    void testNegativeRange() {
        List<TickGenerator.Tick> ticks = generator.generateTicks(-10.0, 0.0);

        assertFalse(ticks.isEmpty());

        // Verify all ticks are within range
        for (TickGenerator.Tick tick : ticks) {
            assertTrue(tick.getPosition() >= -10.0);
            assertTrue(tick.getPosition() <= 0.0);
        }
    }

    @Test
    void testMixedRange() {
        List<TickGenerator.Tick> ticks = generator.generateTicks(-5.0, 5.0);

        assertFalse(ticks.isEmpty());

        // Should include zero
        boolean hasZero = false;
        for (TickGenerator.Tick tick : ticks) {
            if (Math.abs(tick.getPosition()) < 0.001) {
                hasZero = true;
                break;
            }
        }
        assertTrue(hasZero, "Symmetric range around zero should include a tick at 0");
    }

    @Test
    void testTickLabels() {
        List<TickGenerator.Tick> ticks = generator.generateTicks(0.0, 10.0);

        for (TickGenerator.Tick tick : ticks) {
            assertNotNull(tick.getLabel());
            assertFalse(tick.getLabel().isEmpty());

            // Label should be parseable as a number
            assertDoesNotThrow(() -> Double.parseDouble(tick.getLabel()));
        }
    }

    @Test
    void testTickType() {
        List<TickGenerator.Tick> ticks = generator.generateTicks(0.0, 10.0);

        // All ticks should be MAJOR type (for now)
        for (TickGenerator.Tick tick : ticks) {
            assertEquals(TickGenerator.TickType.MAJOR, tick.getType());
        }
    }

    @Test
    void testUniformSpacing() {
        List<TickGenerator.Tick> ticks = generator.generateTicks(0.0, 10.0);

        if (ticks.size() >= 3) {
            // Calculate spacing between consecutive ticks
            double firstSpacing = ticks.get(1).getPosition() - ticks.get(0).getPosition();

            for (int i = 2; i < ticks.size(); i++) {
                double spacing = ticks.get(i).getPosition() - ticks.get(i - 1).getPosition();
                // Spacing should be uniform (within floating point tolerance)
                assertEquals(firstSpacing, spacing, firstSpacing * 0.01,
                        "Tick spacing should be uniform");
            }
        }
    }

    @Test
    void testVerySmallRange() {
        List<TickGenerator.Tick> ticks = generator.generateTicks(0.0, 0.1);

        assertFalse(ticks.isEmpty());

        // Should handle small ranges with appropriate precision
        for (TickGenerator.Tick tick : ticks) {
            assertTrue(tick.getPosition() >= 0.0);
            assertTrue(tick.getPosition() <= 0.1);
        }
    }

    @Test
    void testFractionalRange() {
        List<TickGenerator.Tick> ticks = generator.generateTicks(1.5, 3.5);

        assertFalse(ticks.isEmpty());

        // First tick should be at or after 1.5
        assertTrue(ticks.get(0).getPosition() >= 1.5);

        // Last tick should be at or before 3.5
        assertTrue(ticks.get(ticks.size() - 1).getPosition() <= 3.5);
    }

    @Test
    void testInvalidRange() {
        // Min equals max
        List<TickGenerator.Tick> ticks1 = generator.generateTicks(5.0, 5.0);
        assertTrue(ticks1.isEmpty());

        // Min greater than max
        List<TickGenerator.Tick> ticks2 = generator.generateTicks(10.0, 5.0);
        assertTrue(ticks2.isEmpty());
    }

    @Test
    void testZeroInLabel() {
        List<TickGenerator.Tick> ticks = generator.generateTicks(-1.0, 1.0);

        // Find the tick at zero
        TickGenerator.Tick zeroTick = null;
        for (TickGenerator.Tick tick : ticks) {
            if (Math.abs(tick.getPosition()) < 0.001) {
                zeroTick = tick;
                break;
            }
        }

        assertNotNull(zeroTick);
        assertEquals("0", zeroTick.getLabel());
    }

    @Test
    void testLabelPrecision() {
        // For range 0-1, should have appropriate decimal places
        List<TickGenerator.Tick> ticks = generator.generateTicks(0.0, 1.0);

        for (TickGenerator.Tick tick : ticks) {
            String label = tick.getLabel();
            // Label should not have excessive trailing zeros
            assertFalse(label.matches(".*\\..*0{3,}.*"), "Label should not have excessive trailing zeros: " + label);
        }
    }

    @Test
    void testCustomGuide() {
        // Test with fewer ticks (guide = 10)
        List<TickGenerator.Tick> ticks1 = generator.generateTicks(0.0, 100.0, 10);

        // Test with more ticks (guide = 30)
        List<TickGenerator.Tick> ticks2 = generator.generateTicks(0.0, 100.0, 30);

        // More guide should generally produce more (or equal) ticks
        assertTrue(ticks2.size() >= ticks1.size());
    }

    @Test
    void testScientificRanges() {
        // Very small range
        List<TickGenerator.Tick> ticks1 = generator.generateTicks(0.0, 0.01);
        assertFalse(ticks1.isEmpty());

        // Very large range
        List<TickGenerator.Tick> ticks2 = generator.generateTicks(0.0, 100000.0);
        assertFalse(ticks2.isEmpty());
        assertTrue(ticks2.size() < 50, "Should not generate excessive ticks for large range");
    }

    @Test
    void testTicksIncludeEndpoints() {
        List<TickGenerator.Tick> ticks = generator.generateTicks(0.0, 10.0);

        // Should have ticks near the endpoints
        double firstTick = ticks.get(0).getPosition();
        double lastTick = ticks.get(ticks.size() - 1).getPosition();

        assertTrue(firstTick <= 1.0, "First tick should be near the start");
        assertTrue(lastTick >= 9.0, "Last tick should be near the end");
    }

    @Test
    void testNiceNumbers() {
        List<TickGenerator.Tick> ticks = generator.generateTicks(0.0, 10.0);

        if (ticks.size() >= 2) {
            double spacing = ticks.get(1).getPosition() - ticks.get(0).getPosition();

            // Spacing should be a "nice" number (0.05, 0.1, 0.2, 0.5, 1, 2, 5, 10, etc.)
            // Normalized to [0.05, 10), it should be one of these values
            double magnitude = Math.pow(10, Math.floor(Math.log10(spacing)));
            double normalized = spacing / magnitude;

            // Check if it's close to one of the nice numbers
            boolean isNice = isCloseToAny(normalized, new double[]{0.05, 0.1, 0.2, 0.5, 1.0, 2.0, 5.0});
            assertTrue(isNice, "Tick spacing should be a nice number, got: " + spacing + " (normalized: " + normalized + ")");
        }
    }

    private boolean isCloseToAny(double value, double[] targets) {
        for (double target : targets) {
            if (Math.abs(value - target) < 0.01) {
                return true;
            }
        }
        return false;
    }

    @Test
    void testMinorTics() {
        // Generate ticks with 4 minor tics between major tics
        List<TickGenerator.Tick> ticks = generator.generateTicks(0.0, 10.0, 20, 4);

        assertFalse(ticks.isEmpty());

        // Count major and minor ticks
        long majorCount = ticks.stream()
                .filter(t -> t.getType() == TickGenerator.TickType.MAJOR)
                .count();
        long minorCount = ticks.stream()
                .filter(t -> t.getType() == TickGenerator.TickType.MINOR)
                .count();

        // Should have both major and minor ticks
        assertTrue(majorCount > 0, "Should have major ticks");
        assertTrue(minorCount > 0, "Should have minor ticks");

        // For each major tick interval, there should be approximately 4 minor ticks
        // (may be less for the last interval)
        assertTrue(minorCount >= (majorCount - 1) * 3, "Should have at least 3 minor ticks per interval");
    }

    @Test
    void testMinorTicsLabels() {
        List<TickGenerator.Tick> ticks = generator.generateTicks(0.0, 10.0, 20, 4);

        // Major ticks should have labels
        for (TickGenerator.Tick tick : ticks) {
            if (tick.getType() == TickGenerator.TickType.MAJOR) {
                assertNotNull(tick.getLabel());
                assertFalse(tick.getLabel().isEmpty());
            } else {
                // Minor ticks should have empty labels
                assertEquals("", tick.getLabel());
            }
        }
    }

    @Test
    void testMinorTicsOrder() {
        List<TickGenerator.Tick> ticks = generator.generateTicks(0.0, 10.0, 20, 4);

        // Ticks should be in ascending order
        for (int i = 1; i < ticks.size(); i++) {
            assertTrue(ticks.get(i).getPosition() > ticks.get(i - 1).getPosition(),
                    "Ticks should be in ascending order");
        }
    }

    @Test
    void testMinorTicsSpacing() {
        List<TickGenerator.Tick> ticks = generator.generateTicks(0.0, 10.0, 20, 4);

        // Find first two consecutive major ticks
        int firstMajorIdx = -1;
        int secondMajorIdx = -1;
        for (int i = 0; i < ticks.size(); i++) {
            if (ticks.get(i).getType() == TickGenerator.TickType.MAJOR) {
                if (firstMajorIdx == -1) {
                    firstMajorIdx = i;
                } else {
                    secondMajorIdx = i;
                    break;
                }
            }
        }

        if (firstMajorIdx >= 0 && secondMajorIdx >= 0) {
            // Count minor ticks between these two major ticks
            int minorsBetween = secondMajorIdx - firstMajorIdx - 1;

            // Should be 4 minor ticks between major ticks
            assertEquals(4, minorsBetween, "Should have exactly 4 minor ticks between major ticks");

            // Check spacing is uniform
            double majorSpacing = ticks.get(secondMajorIdx).getPosition() - ticks.get(firstMajorIdx).getPosition();
            double expectedMinorSpacing = majorSpacing / 5.0; // 5 intervals = 4 minor ticks + 1

            for (int i = firstMajorIdx; i < secondMajorIdx; i++) {
                double actualSpacing = ticks.get(i + 1).getPosition() - ticks.get(i).getPosition();
                assertEquals(expectedMinorSpacing, actualSpacing, expectedMinorSpacing * 0.01,
                        "Minor tick spacing should be uniform");
            }
        }
    }

    @Test
    void testNoMinorTics() {
        // Test with 0 minor tics (should be same as not specifying)
        List<TickGenerator.Tick> ticks1 = generator.generateTicks(0.0, 10.0, 20, 0);
        List<TickGenerator.Tick> ticks2 = generator.generateTicks(0.0, 10.0, 20);

        // Should have same number of ticks
        assertEquals(ticks2.size(), ticks1.size());

        // All should be major ticks
        for (TickGenerator.Tick tick : ticks1) {
            assertEquals(TickGenerator.TickType.MAJOR, tick.getType());
        }
    }

    @Test
    void testCustomTics() {
        double[] positions = {0.0, 2.5, 5.0, 7.5, 10.0};
        List<TickGenerator.Tick> ticks = generator.generateCustomTicks(positions);

        assertEquals(5, ticks.size());

        // Verify positions
        for (int i = 0; i < positions.length; i++) {
            assertEquals(positions[i], ticks.get(i).getPosition(), 0.001);
        }

        // All should be major ticks
        for (TickGenerator.Tick tick : ticks) {
            assertEquals(TickGenerator.TickType.MAJOR, tick.getType());
        }
    }

    @Test
    void testCustomTicsWithLabels() {
        double[] positions = {1.0, 2.0, 3.0};
        String[] labels = {"One", "Two", "Three"};
        List<TickGenerator.Tick> ticks = generator.generateCustomTicks(positions, labels);

        assertEquals(3, ticks.size());

        // Verify positions and labels
        for (int i = 0; i < positions.length; i++) {
            assertEquals(positions[i], ticks.get(i).getPosition(), 0.001);
            assertEquals(labels[i], ticks.get(i).getLabel());
        }
    }

    @Test
    void testCustomTicsUnsorted() {
        // Test that positions are sorted correctly
        double[] positions = {10.0, 2.5, 7.5, 0.0, 5.0};
        List<TickGenerator.Tick> ticks = generator.generateCustomTicks(positions);

        assertEquals(5, ticks.size());

        // Verify ticks are in ascending order
        for (int i = 1; i < ticks.size(); i++) {
            assertTrue(ticks.get(i).getPosition() > ticks.get(i - 1).getPosition());
        }
    }

    @Test
    void testCustomTicsWithLabelsUnsorted() {
        // Test that positions and labels are sorted together correctly
        double[] positions = {3.0, 1.0, 2.0};
        String[] labels = {"Three", "One", "Two"};
        List<TickGenerator.Tick> ticks = generator.generateCustomTicks(positions, labels);

        assertEquals(3, ticks.size());

        // Should be sorted by position
        assertEquals(1.0, ticks.get(0).getPosition(), 0.001);
        assertEquals("One", ticks.get(0).getLabel());

        assertEquals(2.0, ticks.get(1).getPosition(), 0.001);
        assertEquals("Two", ticks.get(1).getLabel());

        assertEquals(3.0, ticks.get(2).getPosition(), 0.001);
        assertEquals("Three", ticks.get(2).getLabel());
    }

    @Test
    void testCustomTicsEmpty() {
        double[] positions = {};
        List<TickGenerator.Tick> ticks = generator.generateCustomTicks(positions);

        assertTrue(ticks.isEmpty());
    }

    @Test
    void testCustomTicsNull() {
        List<TickGenerator.Tick> ticks = generator.generateCustomTicks(null);
        assertTrue(ticks.isEmpty());
    }

    @Test
    void testCustomTicsLabelMismatch() {
        double[] positions = {1.0, 2.0, 3.0};
        String[] labels = {"One", "Two"};  // Wrong length

        assertThrows(IllegalArgumentException.class, () -> {
            generator.generateCustomTicks(positions, labels);
        });
    }

    @Test
    void testLogTicks() {
        // Test log scale from 1 to 1000
        List<TickGenerator.Tick> ticks = generator.generateLogTicks(1.0, 1000.0);

        assertFalse(ticks.isEmpty());

        // Should have ticks at 1, 10, 100, 1000
        assertTrue(ticks.size() >= 4);

        // Verify major powers of 10 are present
        boolean has1 = false, has10 = false, has100 = false, has1000 = false;
        for (TickGenerator.Tick tick : ticks) {
            if (Math.abs(tick.getPosition() - 1.0) < 0.01) has1 = true;
            if (Math.abs(tick.getPosition() - 10.0) < 0.01) has10 = true;
            if (Math.abs(tick.getPosition() - 100.0) < 0.01) has100 = true;
            if (Math.abs(tick.getPosition() - 1000.0) < 0.01) has1000 = true;
        }

        assertTrue(has1, "Should have tick at 1");
        assertTrue(has10, "Should have tick at 10");
        assertTrue(has100, "Should have tick at 100");
        assertTrue(has1000, "Should have tick at 1000");
    }

    @Test
    void testLogTicksWithMinorTics() {
        // Test log scale with minor ticks
        List<TickGenerator.Tick> ticks = generator.generateLogTicks(1.0, 100.0, 10.0, true);

        assertFalse(ticks.isEmpty());

        // Count major and minor ticks
        long majorCount = ticks.stream()
                .filter(t -> t.getType() == TickGenerator.TickType.MAJOR)
                .count();
        long minorCount = ticks.stream()
                .filter(t -> t.getType() == TickGenerator.TickType.MINOR)
                .count();

        // Should have major ticks at 1, 10, 100
        assertEquals(3, majorCount);

        // Should have minor ticks at 2,3,4,5,6,7,8,9 and 20,30,40,50,60,70,80,90
        // That's 8 in each decade, so 16 total for 2 decades
        assertTrue(minorCount >= 14, "Should have at least 14 minor ticks");
    }

    @Test
    void testLogTicksPartialDecade() {
        // Test log scale that doesn't start/end at exact powers
        List<TickGenerator.Tick> ticks = generator.generateLogTicks(5.0, 50.0);

        assertFalse(ticks.isEmpty());

        // Should have ticks at 10
        boolean has10 = false;
        for (TickGenerator.Tick tick : ticks) {
            if (Math.abs(tick.getPosition() - 10.0) < 0.01) {
                has10 = true;
            }
            // All ticks should be in range
            assertTrue(tick.getPosition() >= 5.0);
            assertTrue(tick.getPosition() <= 50.0);
        }

        assertTrue(has10, "Should have tick at 10");
    }

    @Test
    void testLogTicksLabels() {
        List<TickGenerator.Tick> ticks = generator.generateLogTicks(1.0, 1000.0);

        for (TickGenerator.Tick tick : ticks) {
            assertNotNull(tick.getLabel());
            assertFalse(tick.getLabel().isEmpty());

            // Major ticks at powers of 10 should have appropriate labels
            if (Math.abs(tick.getPosition() - 1.0) < 0.01) {
                assertEquals("1", tick.getLabel());
            } else if (Math.abs(tick.getPosition() - 10.0) < 0.01) {
                assertEquals("10", tick.getLabel());
            } else if (Math.abs(tick.getPosition() - 100.0) < 0.01) {
                assertEquals("10^2", tick.getLabel());
            }
        }
    }

    @Test
    void testLogTicksInvalidRange() {
        // Negative min
        List<TickGenerator.Tick> ticks1 = generator.generateLogTicks(-1.0, 10.0);
        assertTrue(ticks1.isEmpty());

        // Zero min
        List<TickGenerator.Tick> ticks2 = generator.generateLogTicks(0.0, 10.0);
        assertTrue(ticks2.isEmpty());

        // Min >= max
        List<TickGenerator.Tick> ticks3 = generator.generateLogTicks(10.0, 10.0);
        assertTrue(ticks3.isEmpty());

        // Min > max
        List<TickGenerator.Tick> ticks4 = generator.generateLogTicks(100.0, 10.0);
        assertTrue(ticks4.isEmpty());
    }

    @Test
    void testLogTicksSmallRange() {
        // Test with very small log range (less than one decade)
        List<TickGenerator.Tick> ticks = generator.generateLogTicks(1.0, 5.0);

        assertFalse(ticks.isEmpty());

        // Should have tick at 1
        boolean has1 = false;
        for (TickGenerator.Tick tick : ticks) {
            if (Math.abs(tick.getPosition() - 1.0) < 0.01) {
                has1 = true;
            }
        }
        assertTrue(has1);
    }

    @Test
    void testLogTicksOrder() {
        List<TickGenerator.Tick> ticks = generator.generateLogTicks(0.1, 1000.0, 10.0, true);

        // Ticks should be in ascending order
        for (int i = 1; i < ticks.size(); i++) {
            assertTrue(ticks.get(i).getPosition() > ticks.get(i - 1).getPosition(),
                    "Log ticks should be in ascending order");
        }
    }

    @Test
    void testTimeTicksHourRange() {
        // 2025-01-01 00:00:00 to 2025-01-01 12:00:00 (12 hours)
        LocalDateTime start = LocalDateTime.of(2025, 1, 1, 0, 0);
        LocalDateTime end = LocalDateTime.of(2025, 1, 1, 12, 0);

        long startTime = start.atZone(ZoneId.systemDefault()).toEpochSecond();
        long endTime = end.atZone(ZoneId.systemDefault()).toEpochSecond();

        List<TickGenerator.Tick> ticks = generator.generateTimeTicks(startTime, endTime);

        assertFalse(ticks.isEmpty());
        // All ticks should be within range
        for (TickGenerator.Tick tick : ticks) {
            assertTrue(tick.getPosition() >= startTime);
            assertTrue(tick.getPosition() <= endTime);
            assertNotNull(tick.getLabel());
        }
    }

    @Test
    void testTimeTicksDayRange() {
        // 2025-01-01 to 2025-01-31 (1 month)
        LocalDateTime start = LocalDateTime.of(2025, 1, 1, 0, 0);
        LocalDateTime end = LocalDateTime.of(2025, 1, 31, 0, 0);

        long startTime = start.atZone(ZoneId.systemDefault()).toEpochSecond();
        long endTime = end.atZone(ZoneId.systemDefault()).toEpochSecond();

        List<TickGenerator.Tick> ticks = generator.generateTimeTicks(startTime, endTime);

        assertFalse(ticks.isEmpty());
        assertTrue(ticks.size() <= 31); // One month can have up to 31 day ticks
        // All ticks should be within range
        for (TickGenerator.Tick tick : ticks) {
            assertTrue(tick.getPosition() >= startTime);
            assertTrue(tick.getPosition() <= endTime);
        }
    }

    @Test
    void testTimeTicksYearRange() {
        // 2020-01-01 to 2025-01-01 (5 years)
        LocalDateTime start = LocalDateTime.of(2020, 1, 1, 0, 0);
        LocalDateTime end = LocalDateTime.of(2025, 1, 1, 0, 0);

        long startTime = start.atZone(ZoneId.systemDefault()).toEpochSecond();
        long endTime = end.atZone(ZoneId.systemDefault()).toEpochSecond();

        List<TickGenerator.Tick> ticks = generator.generateTimeTicks(startTime, endTime);

        assertFalse(ticks.isEmpty());
        // Should have yearly ticks
        assertTrue(ticks.size() >= 5);
    }

    @Test
    void testTimeTicksMinuteRange() {
        // 10-minute range
        LocalDateTime start = LocalDateTime.of(2025, 1, 1, 10, 0);
        LocalDateTime end = LocalDateTime.of(2025, 1, 1, 10, 10);

        long startTime = start.atZone(ZoneId.systemDefault()).toEpochSecond();
        long endTime = end.atZone(ZoneId.systemDefault()).toEpochSecond();

        List<TickGenerator.Tick> ticks = generator.generateTimeTicks(startTime, endTime);

        assertFalse(ticks.isEmpty());
        // All ticks should be within range
        for (TickGenerator.Tick tick : ticks) {
            assertTrue(tick.getPosition() >= startTime);
            assertTrue(tick.getPosition() <= endTime);
        }
    }

    @Test
    void testTimeTicksWithGuide() {
        LocalDateTime start = LocalDateTime.of(2025, 1, 1, 0, 0);
        LocalDateTime end = LocalDateTime.of(2025, 1, 1, 12, 0);

        long startTime = start.atZone(ZoneId.systemDefault()).toEpochSecond();
        long endTime = end.atZone(ZoneId.systemDefault()).toEpochSecond();

        // Test with different guide parameters
        List<TickGenerator.Tick> ticks1 = generator.generateTimeTicks(startTime, endTime, 5);
        List<TickGenerator.Tick> ticks2 = generator.generateTimeTicks(startTime, endTime, 20);

        // More guide should generally produce more ticks
        assertTrue(ticks2.size() >= ticks1.size());
    }

    @Test
    void testTimeTicksInvalidRange() {
        // Min equals max
        List<TickGenerator.Tick> ticks1 = generator.generateTimeTicks(1000.0, 1000.0);
        assertTrue(ticks1.isEmpty());

        // Min > max
        List<TickGenerator.Tick> ticks2 = generator.generateTimeTicks(2000.0, 1000.0);
        assertTrue(ticks2.isEmpty());
    }

    @Test
    void testTimeTicksLabelFormats() {
        // Test hour labels
        LocalDateTime start = LocalDateTime.of(2025, 1, 1, 0, 0);
        LocalDateTime end = LocalDateTime.of(2025, 1, 1, 6, 0);

        long startTime = start.atZone(ZoneId.systemDefault()).toEpochSecond();
        long endTime = end.atZone(ZoneId.systemDefault()).toEpochSecond();

        List<TickGenerator.Tick> ticks = generator.generateTimeTicks(startTime, endTime);

        // Labels should contain time info
        for (TickGenerator.Tick tick : ticks) {
            assertNotNull(tick.getLabel());
            assertFalse(tick.getLabel().isEmpty());
            // Hour format should contain ":" (e.g., "10:00")
            assertTrue(tick.getLabel().matches(".*\\d+.*"));
        }
    }
}
