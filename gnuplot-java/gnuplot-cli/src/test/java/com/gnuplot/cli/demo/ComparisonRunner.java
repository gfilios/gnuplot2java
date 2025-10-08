package com.gnuplot.cli.demo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Runs comprehensive comparison tools on C vs Java SVG outputs.
 * Integrates with DemoTestSuite to automatically analyze visual differences.
 */
public class ComparisonRunner {

    private final Path repositoryRoot;
    private final Path compareDeepScript;
    private final Path compareSvgScript;
    private final Path compareVisualScript;
    private final Path compareAllScript;

    public ComparisonRunner(Path repositoryRoot) {
        this.repositoryRoot = repositoryRoot;

        // Locate comparison scripts in test-tools/comparison directory
        Path repoRoot = repositoryRoot.getParent();
        Path comparisonDir = repoRoot.resolve("test-tools/comparison");
        this.compareDeepScript = comparisonDir.resolve("compare_deep.sh");
        this.compareSvgScript = comparisonDir.resolve("compare_svg.sh");
        this.compareVisualScript = comparisonDir.resolve("compare_visual.sh");
        this.compareAllScript = comparisonDir.resolve("compare_all.sh");
    }

    /**
     * Result of running comparison analysis.
     */
    public static class ComparisonResult {
        private final boolean success;
        private final String output;
        private final List<String> criticalIssues;
        private final ComparisonMetrics metrics;

        public ComparisonResult(boolean success, String output,
                              List<String> criticalIssues, ComparisonMetrics metrics) {
            this.success = success;
            this.output = output;
            this.criticalIssues = criticalIssues;
            this.metrics = metrics;
        }

        public boolean isSuccess() { return success; }
        public String getOutput() { return output; }
        public List<String> getCriticalIssues() { return criticalIssues; }
        public ComparisonMetrics getMetrics() { return metrics; }
        public boolean hasIssues() { return !criticalIssues.isEmpty(); }
    }

    /**
     * Metrics extracted from comparison tools.
     */
    public static class ComparisonMetrics {
        private int cLineCount = 0;
        private int javaLineCount = 0;
        private int cPointCount = 0;
        private int javaPointCount = 0;
        private int cEdgePixels = 0;
        private int javaEdgePixels = 0;
        private int cUniqueColors = 0;
        private int javaUniqueColors = 0;
        private double pixelDifferencePercent = 0.0;

        // Getters
        public int getCLineCount() { return cLineCount; }
        public int getJavaLineCount() { return javaLineCount; }
        public int getCPointCount() { return cPointCount; }
        public int getJavaPointCount() { return javaPointCount; }
        public int getCEdgePixels() { return cEdgePixels; }
        public int getJavaEdgePixels() { return javaEdgePixels; }
        public int getCUniqueColors() { return cUniqueColors; }
        public int getJavaUniqueColors() { return javaUniqueColors; }
        public double getPixelDifferencePercent() { return pixelDifferencePercent; }

        // Setters
        public void setCLineCount(int count) { this.cLineCount = count; }
        public void setJavaLineCount(int count) { this.javaLineCount = count; }
        public void setCPointCount(int count) { this.cPointCount = count; }
        public void setJavaPointCount(int count) { this.javaPointCount = count; }
        public void setCEdgePixels(int count) { this.cEdgePixels = count; }
        public void setJavaEdgePixels(int count) { this.javaEdgePixels = count; }
        public void setCUniqueColors(int count) { this.cUniqueColors = count; }
        public void setJavaUniqueColors(int count) { this.javaUniqueColors = count; }
        public void setPixelDifferencePercent(double percent) { this.pixelDifferencePercent = percent; }
    }

    /**
     * Run comprehensive comparison on C vs Java outputs.
     */
    public ComparisonResult runComparison(Path cSvgOutput, Path javaSvgOutput) {
        // Check if both files exist
        if (!Files.exists(cSvgOutput) || !Files.exists(javaSvgOutput)) {
            return new ComparisonResult(false, "Missing SVG files",
                                      List.of("One or both SVG files not found"),
                                      new ComparisonMetrics());
        }

        StringBuilder fullOutput = new StringBuilder();
        List<String> criticalIssues = new ArrayList<>();
        ComparisonMetrics metrics = new ComparisonMetrics();

        try {
            // Run deep comparison if available
            if (Files.exists(compareDeepScript) && Files.isExecutable(compareDeepScript)) {
                String deepOutput = runScript(compareDeepScript, cSvgOutput, javaSvgOutput);
                fullOutput.append("=== DEEP ELEMENT-BY-ELEMENT COMPARISON ===\n");
                fullOutput.append(deepOutput);
                fullOutput.append("\n\n");

                // Extract issues from deep comparison
                extractDeepIssues(deepOutput, criticalIssues);
            }

            // Run SVG comparison if available
            if (Files.exists(compareSvgScript) && Files.isExecutable(compareSvgScript)) {
                String svgOutput = runScript(compareSvgScript, cSvgOutput, javaSvgOutput);
                fullOutput.append("=== SVG CODE COMPARISON ===\n");
                fullOutput.append(svgOutput);
                fullOutput.append("\n\n");

                // Extract metrics from SVG comparison
                extractSvgMetrics(svgOutput, metrics);
            }

            // Run visual comparison if available
            if (Files.exists(compareVisualScript) && Files.isExecutable(compareVisualScript)) {
                String visualOutput = runScript(compareVisualScript, cSvgOutput, javaSvgOutput);
                fullOutput.append("=== VISUAL IMAGE COMPARISON ===\n");
                fullOutput.append(visualOutput);
                fullOutput.append("\n\n");

                // Extract metrics from visual comparison
                extractVisualMetrics(visualOutput, metrics);

                // Extract visual issues
                extractVisualIssues(visualOutput, criticalIssues);
            }

            return new ComparisonResult(true, fullOutput.toString(),
                                      criticalIssues, metrics);

        } catch (IOException | InterruptedException e) {
            return new ComparisonResult(false,
                                      "Error running comparison: " + e.getMessage(),
                                      List.of("Comparison execution failed: " + e.getMessage()),
                                      metrics);
        }
    }

    /**
     * Run a comparison script and capture output.
     */
    private String runScript(Path script, Path cSvg, Path javaSvg)
            throws IOException, InterruptedException {
        ProcessBuilder pb = new ProcessBuilder(
            script.toString(),
            cSvg.toString(),
            javaSvg.toString()
        );

        pb.redirectErrorStream(true);
        Process process = pb.start();

        StringBuilder output = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
        }

        process.waitFor();
        return output.toString();
    }

    /**
     * Extract critical issues from deep comparison output.
     */
    private void extractDeepIssues(String output, List<String> issues) {
        String[] lines = output.split("\n");
        for (String line : lines) {
            if (line.contains("❌ CRITICAL:") || line.contains("❌")) {
                String issue = line.replaceAll("^.*❌\\s*", "").trim();
                if (!issue.isEmpty() && !issue.equals("CRITICAL:")) {
                    issues.add(issue);
                }
            }
        }
    }

    /**
     * Extract metrics from SVG comparison output.
     */
    private void extractSvgMetrics(String output, ComparisonMetrics metrics) {
        String[] lines = output.split("\n");
        for (String line : lines) {
            // Extract line/point counts
            if (line.contains("Line paths (<path>):")) {
                String[] parts = line.split(":");
                if (parts.length > 1) {
                    try {
                        int count = Integer.parseInt(parts[1].trim());
                        if (line.contains("C Gnuplot")) {
                            metrics.setCLineCount(count);
                        } else if (line.contains("Java Gnuplot")) {
                            metrics.setJavaLineCount(count);
                        }
                    } catch (NumberFormatException ignored) {}
                }
            }

            if (line.contains("Point markers (<use>):")) {
                String[] parts = line.split(":");
                if (parts.length > 1) {
                    try {
                        int count = Integer.parseInt(parts[1].trim());
                        if (line.contains("C Gnuplot")) {
                            metrics.setCPointCount(count);
                        } else if (line.contains("Java Gnuplot")) {
                            metrics.setJavaPointCount(count);
                        }
                    } catch (NumberFormatException ignored) {}
                }
            }
        }
    }

    /**
     * Extract metrics from visual comparison output.
     */
    private void extractVisualMetrics(String output, ComparisonMetrics metrics) {
        String[] lines = output.split("\n");
        for (String line : lines) {
            // Extract edge pixels
            if (line.contains("edge pixels:")) {
                try {
                    String[] parts = line.split(":");
                    if (parts.length > 1) {
                        int count = Integer.parseInt(parts[1].trim().replaceAll("[^0-9]", ""));
                        if (line.contains("C Gnuplot")) {
                            metrics.setCEdgePixels(count);
                        } else if (line.contains("Java Gnuplot")) {
                            metrics.setJavaEdgePixels(count);
                        }
                    }
                } catch (NumberFormatException ignored) {}
            }

            // Extract unique colors
            if (line.contains("unique colors:")) {
                try {
                    String[] parts = line.split(":");
                    if (parts.length > 1) {
                        int count = Integer.parseInt(parts[1].trim().replaceAll("[^0-9]", ""));
                        if (line.contains("C Gnuplot")) {
                            metrics.setCUniqueColors(count);
                        } else if (line.contains("Java Gnuplot")) {
                            metrics.setJavaUniqueColors(count);
                        }
                    }
                } catch (NumberFormatException ignored) {}
            }

            // Extract pixel difference percentage
            if (line.contains("Difference:") && line.contains("%")) {
                try {
                    String percentStr = line.replaceAll(".*Difference:\\s*", "")
                                          .replaceAll("%.*", "").trim();
                    metrics.setPixelDifferencePercent(Double.parseDouble(percentStr));
                } catch (NumberFormatException ignored) {}
            }
        }
    }

    /**
     * Extract issues from visual comparison output.
     */
    private void extractVisualIssues(String output, List<String> issues) {
        String[] lines = output.split("\n");
        for (String line : lines) {
            // Check for significant visual differences
            if (line.contains("❌ IMAGES ARE SIGNIFICANTLY DIFFERENT")) {
                String issue = line.replaceAll("❌\\s*", "").trim();
                issues.add("Visual: " + issue);
            }
            // Check for structural differences
            if (line.contains("⚠️") && line.contains("Structural difference")) {
                String issue = line.replaceAll("⚠️\\s*", "").trim();
                issues.add("Visual: " + issue);
            }
        }
    }

    /**
     * Check if comparison tools are available.
     */
    public boolean areToolsAvailable() {
        return Files.exists(compareDeepScript) && Files.isExecutable(compareDeepScript);
    }

    /**
     * Get comparison tool status message.
     */
    public String getToolsStatus() {
        StringBuilder status = new StringBuilder();
        status.append("Comparison Tools Status:\n");
        status.append("  Deep comparison:   ").append(checkScript(compareDeepScript)).append("\n");
        status.append("  SVG comparison:    ").append(checkScript(compareSvgScript)).append("\n");
        status.append("  Visual comparison: ").append(checkScript(compareVisualScript)).append("\n");
        status.append("  All-in-one:        ").append(checkScript(compareAllScript)).append("\n");
        return status.toString();
    }

    private String checkScript(Path script) {
        if (!Files.exists(script)) return "❌ Not found";
        if (!Files.isExecutable(script)) return "⚠️  Not executable";
        return "✅ Available";
    }
}
