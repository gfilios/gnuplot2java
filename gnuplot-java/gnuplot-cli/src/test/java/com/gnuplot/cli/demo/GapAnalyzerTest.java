package com.gnuplot.cli.demo;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test for gap analysis functionality.
 */
class GapAnalyzerTest {

    @Test
    void shouldIdentifyParseErrors() {
        String errorOutput = """
            line 1:21 mismatched input ',' expecting {NUMBER, QUOTED_STRING}
            line 8:25 missing '\\n' at 'font'
            """;

        GapAnalyzer analyzer = new GapAnalyzer();
        GapAnalyzer.AnalysisResult result = analyzer.analyze(errorOutput);

        assertThat(result.hasGaps()).isTrue();
        assertThat(result.getTotalGaps()).isEqualTo(2);
        assertThat(result.getGapCounts().get(GapAnalyzer.GapType.PARSE_ERROR)).isEqualTo(2);
    }

    @Test
    void shouldIdentifyMissingCommands() {
        String errorOutput = """
            Unrecognized command: 'set key left box'
            Unknown command 'plot_style'
            """;

        GapAnalyzer analyzer = new GapAnalyzer();
        GapAnalyzer.AnalysisResult result = analyzer.analyze(errorOutput);

        assertThat(result.hasGaps()).isTrue();
        assertThat(result.getMissingCommands()).hasSize(2);
        assertThat(result.getGapCounts().get(GapAnalyzer.GapType.MISSING_COMMAND))
            .isEqualTo(2);
    }

    @Test
    void shouldIdentifyMissingFeatures() {
        String errorOutput = """
            Error: set output not implemented
            Warning: set key feature missing
            set style command not supported
            """;

        GapAnalyzer analyzer = new GapAnalyzer();
        GapAnalyzer.AnalysisResult result = analyzer.analyze(errorOutput);

        assertThat(result.hasGaps()).isTrue();
        assertThat(result.getMissingFeatures()).isNotEmpty();
    }

    @Test
    void shouldHandleEmptyInput() {
        GapAnalyzer analyzer = new GapAnalyzer();
        GapAnalyzer.AnalysisResult result = analyzer.analyze("");

        assertThat(result.hasGaps()).isFalse();
        assertThat(result.getTotalGaps()).isZero();
    }

    @Test
    void shouldExtractLineNumbers() {
        String errorOutput = "line 42:15 syntax error at token 'xyz'";

        GapAnalyzer analyzer = new GapAnalyzer();
        GapAnalyzer.AnalysisResult result = analyzer.analyze(errorOutput);

        assertThat(result.getGaps()).hasSize(1);
        assertThat(result.getGaps().get(0).getLineNumber()).isEqualTo(42);
    }

    @Test
    void shouldGenerateSummary() {
        String errorOutput = """
            line 1:21 parse error
            Unrecognized command: 'foo'
            set output not implemented
            """;

        GapAnalyzer analyzer = new GapAnalyzer();
        GapAnalyzer.AnalysisResult result = analyzer.analyze(errorOutput);

        String summary = result.getSummary();

        assertThat(summary).contains("Total gaps: 3");
        assertThat(summary).contains("PARSE_ERROR");
        assertThat(summary).contains("MISSING_COMMAND");
        assertThat(summary).contains("MISSING_FEATURE");
    }

    @Test
    void shouldPrioritizeIssues() {
        String errorOutput = """
            Unrecognized command: 'set key'
            set output not implemented
            line 5:10 parse error
            """;

        GapAnalyzer analyzer = new GapAnalyzer();
        GapAnalyzer.AnalysisResult result = analyzer.analyze(errorOutput);

        var priorities = analyzer.getPriorityIssues(result);

        assertThat(priorities).isNotEmpty();
        assertThat(priorities.get(0)).startsWith("P1:"); // Commands are P1
        assertThat(priorities).anyMatch(p -> p.startsWith("P2:")); // Features are P2
        assertThat(priorities).anyMatch(p -> p.startsWith("P3:")); // Parse errors are P3
    }

    @Test
    void shouldIdentifyDataFileIssues() {
        String errorOutput = """
            Error reading data file: 1.dat
            Cannot open file: test.dat
            """;

        GapAnalyzer analyzer = new GapAnalyzer();
        GapAnalyzer.AnalysisResult result = analyzer.analyze(errorOutput);

        assertThat(result.hasGaps()).isTrue();
        assertThat(result.getGapCounts()).containsKey(GapAnalyzer.GapType.DATA_ERROR);
    }
}
