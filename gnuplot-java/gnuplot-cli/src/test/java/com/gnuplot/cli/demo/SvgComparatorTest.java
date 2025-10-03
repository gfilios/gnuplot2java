package com.gnuplot.cli.demo;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test for SVG comparison functionality.
 */
class SvgComparatorTest {

    @TempDir
    Path tempDir;

    @Test
    void shouldDetectIdenticalSvgs() throws IOException {
        String svg = """
            <svg width="100" height="100">
                <rect x="10" y="10" width="80" height="80" fill="blue"/>
            </svg>
            """;

        Path svg1 = tempDir.resolve("test1.svg");
        Path svg2 = tempDir.resolve("test2.svg");
        Files.writeString(svg1, svg);
        Files.writeString(svg2, svg);

        SvgComparator comparator = new SvgComparator();
        SvgComparator.ComparisonResult result = comparator.compare(svg1, svg2);

        assertThat(result.isIdentical()).isTrue();
        assertThat(result.getSimilarity()).isEqualTo(1.0);
        assertThat(result.getDifferences()).isEmpty();
    }

    @Test
    void shouldDetectDifferentElementCounts() throws IOException {
        String svg1Content = """
            <svg width="100" height="100">
                <rect x="10" y="10" width="80" height="80" fill="blue"/>
            </svg>
            """;

        String svg2Content = """
            <svg width="100" height="100">
                <rect x="10" y="10" width="80" height="80" fill="blue"/>
                <circle cx="50" cy="50" r="20" fill="red"/>
            </svg>
            """;

        Path svg1 = tempDir.resolve("test1.svg");
        Path svg2 = tempDir.resolve("test2.svg");
        Files.writeString(svg1, svg1Content);
        Files.writeString(svg2, svg2Content);

        SvgComparator comparator = new SvgComparator();
        SvgComparator.ComparisonResult result = comparator.compare(svg1, svg2);

        assertThat(result.isIdentical()).isFalse();
        assertThat(result.getSimilarity()).isLessThan(1.0);
        assertThat(result.getDifferences()).isNotEmpty();
        assertThat(result.getDifferences()).anyMatch(d -> d.contains("circle"));
    }

    @Test
    void shouldGetSvgStats() throws IOException {
        String svg = """
            <svg width="800" height="600">
                <rect x="0" y="0" width="100" height="100"/>
                <circle cx="50" cy="50" r="25"/>
                <line x1="0" y1="0" x2="100" y2="100"/>
                <text x="10" y="20">Hello</text>
            </svg>
            """;

        Path svgFile = tempDir.resolve("test.svg");
        Files.writeString(svgFile, svg);

        SvgComparator comparator = new SvgComparator();
        SvgComparator.SvgStats stats = comparator.getStats(svgFile);

        assertThat(stats.width).isEqualTo("800");
        assertThat(stats.height).isEqualTo("600");
        assertThat(stats.rectangles).isEqualTo(1);
        assertThat(stats.circles).isEqualTo(1);
        assertThat(stats.lines).isEqualTo(1);
        assertThat(stats.texts).isEqualTo(1);
    }

    @Test
    void shouldCompareTextContent() throws IOException {
        String svg1 = """
            <svg width="100" height="100">
                <text x="10" y="20">Hello World</text>
            </svg>
            """;

        String svg2 = """
            <svg width="100" height="100">
                <text x="10" y="20">Hello Java</text>
            </svg>
            """;

        Path svg1Path = tempDir.resolve("test1.svg");
        Path svg2Path = tempDir.resolve("test2.svg");
        Files.writeString(svg1Path, svg1);
        Files.writeString(svg2Path, svg2);

        SvgComparator comparator = new SvgComparator();
        var textDiffs = comparator.compareTextContent(svg1Path, svg2Path);

        assertThat(textDiffs).isNotEmpty();
        assertThat(textDiffs).anyMatch(d -> d.contains("Hello World") && d.contains("Hello Java"));
    }
}
