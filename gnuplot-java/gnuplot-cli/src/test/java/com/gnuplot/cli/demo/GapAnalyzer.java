package com.gnuplot.cli.demo;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Analyzes error messages and execution output to identify missing features
 * and compatibility gaps between C Gnuplot and Java Gnuplot.
 */
public class GapAnalyzer {

    /**
     * Type of gap/issue identified.
     */
    public enum GapType {
        MISSING_COMMAND,     // Command not recognized
        PARSE_ERROR,         // Syntax/parsing error
        MISSING_FEATURE,     // Feature not implemented
        RENDERING_ERROR,     // Error during rendering
        DATA_ERROR,          // Data file or input error
        UNKNOWN              // Unclassified error
    }

    /**
     * An identified gap or issue.
     */
    public static class Gap {
        private final GapType type;
        private final String description;
        private final String errorMessage;
        private final int lineNumber;

        public Gap(GapType type, String description, String errorMessage, int lineNumber) {
            this.type = type;
            this.description = description;
            this.errorMessage = errorMessage;
            this.lineNumber = lineNumber;
        }

        public GapType getType() { return type; }
        public String getDescription() { return description; }
        public String getErrorMessage() { return errorMessage; }
        public int getLineNumber() { return lineNumber; }

        @Override
        public String toString() {
            return String.format("[%s] %s (line %d)", type, description, lineNumber);
        }
    }

    /**
     * Analysis result with all identified gaps.
     */
    public static class AnalysisResult {
        private final List<Gap> gaps;
        private final Map<GapType, Integer> gapCounts;
        private final Set<String> missingCommands;
        private final Set<String> missingFeatures;

        public AnalysisResult(List<Gap> gaps) {
            this.gaps = new ArrayList<>(gaps);
            this.gapCounts = new EnumMap<>(GapType.class);
            this.missingCommands = new TreeSet<>();
            this.missingFeatures = new TreeSet<>();

            // Count gaps by type
            for (Gap gap : gaps) {
                gapCounts.merge(gap.getType(), 1, Integer::sum);

                if (gap.getType() == GapType.MISSING_COMMAND) {
                    missingCommands.add(gap.getDescription());
                } else if (gap.getType() == GapType.MISSING_FEATURE) {
                    missingFeatures.add(gap.getDescription());
                }
            }
        }

        public List<Gap> getGaps() { return new ArrayList<>(gaps); }
        public Map<GapType, Integer> getGapCounts() { return new EnumMap<>(gapCounts); }
        public Set<String> getMissingCommands() { return new TreeSet<>(missingCommands); }
        public Set<String> getMissingFeatures() { return new TreeSet<>(missingFeatures); }

        public int getTotalGaps() { return gaps.size(); }

        public boolean hasGaps() { return !gaps.isEmpty(); }

        public String getSummary() {
            StringBuilder sb = new StringBuilder();
            sb.append(String.format("Total gaps: %d\n", getTotalGaps()));

            for (Map.Entry<GapType, Integer> entry : gapCounts.entrySet()) {
                sb.append(String.format("  %s: %d\n", entry.getKey(), entry.getValue()));
            }

            if (!missingCommands.isEmpty()) {
                sb.append("\nMissing commands:\n");
                for (String cmd : missingCommands) {
                    sb.append("  - ").append(cmd).append("\n");
                }
            }

            if (!missingFeatures.isEmpty()) {
                sb.append("\nMissing features:\n");
                for (String feature : missingFeatures) {
                    sb.append("  - ").append(feature).append("\n");
                }
            }

            return sb.toString();
        }
    }

    // Common error patterns
    private static final Pattern PARSE_ERROR_PATTERN =
        Pattern.compile("line (\\d+):(\\d+) (.+)");

    private static final Pattern MISSING_COMMAND_PATTERN =
        Pattern.compile("(?i)(unrecognized|unknown|invalid) command[:]? ['\"]?([^'\"\\s]+)");

    private static final Pattern SYNTAX_ERROR_PATTERN =
        Pattern.compile("(?i)syntax error|parse error|mismatched input|extraneous input|missing");

    private static final Pattern TOKEN_ERROR_PATTERN =
        Pattern.compile("(?i)token recognition error|unexpected token");

    /**
     * Analyze error output from Java Gnuplot execution.
     */
    public AnalysisResult analyze(String errorOutput) {
        if (errorOutput == null || errorOutput.trim().isEmpty()) {
            return new AnalysisResult(List.of());
        }

        List<Gap> gaps = new ArrayList<>();
        String[] lines = errorOutput.split("\n");

        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.isEmpty()) {
                continue;
            }

            // Try to identify gap type
            Gap gap = classifyError(trimmed);
            if (gap != null) {
                gaps.add(gap);
            }
        }

        return new AnalysisResult(gaps);
    }

    /**
     * Classify a single error line.
     */
    private Gap classifyError(String errorLine) {
        // Extract line number if present
        int lineNumber = -1;
        Matcher parseErrorMatcher = PARSE_ERROR_PATTERN.matcher(errorLine);
        if (parseErrorMatcher.find()) {
            try {
                lineNumber = Integer.parseInt(parseErrorMatcher.group(1));
            } catch (NumberFormatException e) {
                // Ignore
            }
        }

        // Check for missing command
        Matcher missingCmdMatcher = MISSING_COMMAND_PATTERN.matcher(errorLine);
        if (missingCmdMatcher.find()) {
            String command = missingCmdMatcher.group(2);
            return new Gap(GapType.MISSING_COMMAND, command, errorLine, lineNumber);
        }

        // Check for parse/syntax errors
        if (SYNTAX_ERROR_PATTERN.matcher(errorLine).find()) {
            String description = extractErrorDescription(errorLine);
            return new Gap(GapType.PARSE_ERROR, description, errorLine, lineNumber);
        }

        // Check for token errors
        if (TOKEN_ERROR_PATTERN.matcher(errorLine).find()) {
            String description = extractErrorDescription(errorLine);
            return new Gap(GapType.PARSE_ERROR, description, errorLine, lineNumber);
        }

        // Check for specific missing features
        if (errorLine.contains("set output") || errorLine.contains("set term")) {
            return new Gap(GapType.MISSING_FEATURE, "set output/term command", errorLine, lineNumber);
        }

        if (errorLine.contains("data file") || errorLine.contains(".dat")) {
            return new Gap(GapType.DATA_ERROR, "data file reading", errorLine, lineNumber);
        }

        if (errorLine.contains("set key")) {
            return new Gap(GapType.MISSING_FEATURE, "set key command", errorLine, lineNumber);
        }

        if (errorLine.contains("set style")) {
            return new Gap(GapType.MISSING_FEATURE, "set style command", errorLine, lineNumber);
        }

        // Generic error if contains "error" but we can't classify
        if (errorLine.toLowerCase().contains("error") ||
            errorLine.toLowerCase().contains("fail")) {
            return new Gap(GapType.UNKNOWN, extractErrorDescription(errorLine),
                          errorLine, lineNumber);
        }

        return null; // Not an error we care about
    }

    /**
     * Extract a concise description from error message.
     */
    private String extractErrorDescription(String errorLine) {
        // Try to extract the key part of the error
        if (errorLine.length() > 80) {
            // Truncate long errors
            return errorLine.substring(0, 77) + "...";
        }
        return errorLine;
    }

    /**
     * Compare two demo results and identify compatibility gaps.
     */
    public AnalysisResult compareResults(DemoTestRunner.DemoResult cResult,
                                         DemoTestRunner.DemoResult javaResult) {
        List<Gap> gaps = new ArrayList<>();

        // Analyze Java errors
        if (!javaResult.isJavaExecutionSuccess() || !javaResult.getJavaError().isEmpty()) {
            AnalysisResult javaAnalysis = analyze(javaResult.getJavaError());
            gaps.addAll(javaAnalysis.getGaps());
        }

        // Check for output mismatches
        if (cResult.isCExecutionSuccess() && javaResult.isJavaExecutionSuccess()) {
            if (cResult.getCOutputFile() != null && javaResult.getJavaOutputFile() == null) {
                gaps.add(new Gap(GapType.RENDERING_ERROR,
                    "Java produced no SVG output",
                    "C produced output but Java did not",
                    -1));
            }
        }

        return new AnalysisResult(gaps);
    }

    /**
     * Get prioritized list of issues to fix.
     */
    public List<String> getPriorityIssues(AnalysisResult analysis) {
        List<String> issues = new ArrayList<>();

        // Priority 1: Missing commands (blocks functionality)
        for (String cmd : analysis.getMissingCommands()) {
            issues.add("P1: Implement command: " + cmd);
        }

        // Priority 2: Missing features
        for (String feature : analysis.getMissingFeatures()) {
            issues.add("P2: Implement feature: " + feature);
        }

        // Priority 3: Parse errors (may indicate grammar issues)
        int parseErrors = analysis.getGapCounts().getOrDefault(GapType.PARSE_ERROR, 0);
        if (parseErrors > 0) {
            issues.add(String.format("P3: Fix %d parse errors in grammar", parseErrors));
        }

        return issues;
    }
}
