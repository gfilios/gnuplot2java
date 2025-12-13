package com.gnuplot.cli.demo;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Repository for persisting demo test results across test runs.
 * Stores scripts, outputs, errors, and metadata for historical tracking.
 */
public class TestResultRepository {

    private final Path repositoryRoot;
    private final Path currentRunDir;
    private final Path latestSymlink;
    private final List<DemoTestRecord> records;

    private static final DateTimeFormatter TIMESTAMP_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");

    /**
     * Record of a single demo test execution.
     */
    public static class DemoTestRecord {
        private final String demoName;
        private final LocalDateTime timestamp;
        private final boolean cSuccess;
        private final boolean javaSuccess;
        private final Path originalScript;
        private final Path modifiedScript;
        private final Path cSvgOutput;
        private final Path javaSvgOutput;
        private final Path cStdout;
        private final Path cStderr;
        private final Path javaStdout;
        private final Path javaStderr;
        private final boolean passing;

        public DemoTestRecord(String demoName, LocalDateTime timestamp,
                            boolean cSuccess, boolean javaSuccess,
                            Path originalScript, Path modifiedScript,
                            Path cSvgOutput, Path javaSvgOutput,
                            Path cStdout, Path cStderr,
                            Path javaStdout, Path javaStderr,
                            boolean passing) {
            this.demoName = demoName;
            this.timestamp = timestamp;
            this.cSuccess = cSuccess;
            this.javaSuccess = javaSuccess;
            this.originalScript = originalScript;
            this.modifiedScript = modifiedScript;
            this.cSvgOutput = cSvgOutput;
            this.javaSvgOutput = javaSvgOutput;
            this.cStdout = cStdout;
            this.cStderr = cStderr;
            this.javaStdout = javaStdout;
            this.javaStderr = javaStderr;
            this.passing = passing;
        }

        public String getDemoName() { return demoName; }
        public LocalDateTime getTimestamp() { return timestamp; }
        public boolean isCSuccess() { return cSuccess; }
        public boolean isJavaSuccess() { return javaSuccess; }
        public Path getOriginalScript() { return originalScript; }
        public Path getModifiedScript() { return modifiedScript; }
        public Path getCSvgOutput() { return cSvgOutput; }
        public Path getJavaSvgOutput() { return javaSvgOutput; }
        public Path getCStdout() { return cStdout; }
        public Path getCStderr() { return cStderr; }
        public Path getJavaStdout() { return javaStdout; }
        public Path getJavaStderr() { return javaStderr; }
        public boolean isPassing() { return passing; }
    }

    /**
     * Create a new test result repository.
     */
    public TestResultRepository(Path repositoryRoot) throws IOException {
        this.repositoryRoot = repositoryRoot;
        this.records = new ArrayList<>();

        // Create repository structure
        Files.createDirectories(repositoryRoot);

        // Use "latest" directory directly (not as a symlink)
        this.currentRunDir = repositoryRoot.resolve("latest");
        this.latestSymlink = null; // Not used anymore

        // Clear and recreate the latest directory
        if (Files.exists(currentRunDir)) {
            deleteDirectoryRecursively(currentRunDir);
        }
        Files.createDirectories(currentRunDir);

        // Create subdirectories
        Files.createDirectories(currentRunDir.resolve("scripts"));
        Files.createDirectories(currentRunDir.resolve("outputs"));
        Files.createDirectories(currentRunDir.resolve("logs"));
    }

    /**
     * Recursively delete a directory and all its contents.
     */
    private void deleteDirectoryRecursively(Path directory) throws IOException {
        if (Files.exists(directory)) {
            Files.walk(directory)
                .sorted((a, b) -> b.compareTo(a)) // Reverse order to delete files before directories
                .forEach(path -> {
                    try {
                        Files.delete(path);
                    } catch (IOException e) {
                        // Ignore errors during cleanup
                    }
                });
        }
    }

    /**
     * Store a demo test result.
     */
    public DemoTestRecord store(String demoName,
                               Path originalScriptSource,
                               Path modifiedScriptSource,
                               DemoTestRunner.DemoResult result) throws IOException {

        String baseName = demoName.replace(".dem", "");
        LocalDateTime timestamp = LocalDateTime.now();

        // Copy original script
        Path originalScript = currentRunDir.resolve("scripts").resolve(demoName);
        if (originalScriptSource != null && Files.exists(originalScriptSource)) {
            Files.copy(originalScriptSource, originalScript, StandardCopyOption.REPLACE_EXISTING);
        }

        // Copy modified script
        Path modifiedScript = currentRunDir.resolve("scripts")
                .resolve(baseName + "_modified.dem");
        if (modifiedScriptSource != null && Files.exists(modifiedScriptSource)) {
            Files.copy(modifiedScriptSource, modifiedScript, StandardCopyOption.REPLACE_EXISTING);
        }

        // Copy C SVG output
        Path cSvgOutput = null;
        if (result.getCOutputFile() != null && Files.exists(result.getCOutputFile())) {
            cSvgOutput = currentRunDir.resolve("outputs").resolve(baseName + "_c.svg");
            Files.copy(result.getCOutputFile(), cSvgOutput, StandardCopyOption.REPLACE_EXISTING);
        }

        // Copy C additional SVG outputs (e.g., _002.svg, _003.svg)
        for (int i = 0; i < result.getCAdditionalOutputFiles().size(); i++) {
            Path additionalFile = result.getCAdditionalOutputFiles().get(i);
            if (Files.exists(additionalFile)) {
                Path dest = currentRunDir.resolve("outputs")
                        .resolve(String.format("%s_c_%03d.svg", baseName, i + 2));
                Files.copy(additionalFile, dest, StandardCopyOption.REPLACE_EXISTING);
            }
        }

        // Copy Java SVG output
        Path javaSvgOutput = null;
        if (result.getJavaOutputFile() != null && Files.exists(result.getJavaOutputFile())) {
            javaSvgOutput = currentRunDir.resolve("outputs").resolve(baseName + "_java.svg");
            Files.copy(result.getJavaOutputFile(), javaSvgOutput, StandardCopyOption.REPLACE_EXISTING);
        }

        // Copy Java additional SVG outputs (e.g., _002.svg, _003.svg)
        for (int i = 0; i < result.getJavaAdditionalOutputFiles().size(); i++) {
            Path additionalFile = result.getJavaAdditionalOutputFiles().get(i);
            if (Files.exists(additionalFile)) {
                Path dest = currentRunDir.resolve("outputs")
                        .resolve(String.format("%s_java_%03d.svg", baseName, i + 2));
                Files.copy(additionalFile, dest, StandardCopyOption.REPLACE_EXISTING);
            }
        }

        // Store C stdout
        Path cStdout = currentRunDir.resolve("logs").resolve(baseName + "_c.stdout");
        Files.writeString(cStdout, result.getCOutput() != null ? result.getCOutput() : "");

        // Store C stderr
        Path cStderr = currentRunDir.resolve("logs").resolve(baseName + "_c.stderr");
        Files.writeString(cStderr, result.getCError() != null ? result.getCError() : "");

        // Store Java stdout
        Path javaStdout = currentRunDir.resolve("logs").resolve(baseName + "_java.stdout");
        Files.writeString(javaStdout, result.getJavaOutput() != null ? result.getJavaOutput() : "");

        // Store Java stderr
        Path javaStderr = currentRunDir.resolve("logs").resolve(baseName + "_java.stderr");
        Files.writeString(javaStderr, result.getJavaError() != null ? result.getJavaError() : "");

        DemoTestRecord record = new DemoTestRecord(
            demoName, timestamp,
            result.isCExecutionSuccess(), result.isJavaExecutionSuccess(),
            originalScript, modifiedScript,
            cSvgOutput, javaSvgOutput,
            cStdout, cStderr,
            javaStdout, javaStderr,
            result.isPassing()
        );

        records.add(record);
        return record;
    }

    /**
     * Generate summary report for this test run.
     */
    public void generateSummaryReport() throws IOException {
        Path summaryFile = currentRunDir.resolve("summary.txt");

        // Generate timestamp for the report
        String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMAT);

        StringBuilder sb = new StringBuilder();
        sb.append("Gnuplot Demo Test Results\n");
        sb.append("=========================\n");
        sb.append("Test Run: ").append(timestamp).append("\n");
        sb.append("Total Tests: ").append(records.size()).append("\n");

        long passing = records.stream().filter(DemoTestRecord::isPassing).count();
        long cSucceeded = records.stream().filter(DemoTestRecord::isCSuccess).count();
        long javaSucceeded = records.stream().filter(DemoTestRecord::isJavaSuccess).count();

        sb.append("Passing: ").append(passing).append(" (")
          .append(String.format("%.1f%%", 100.0 * passing / records.size())).append(")\n");
        sb.append("C Gnuplot Success: ").append(cSucceeded).append("/").append(records.size()).append("\n");
        sb.append("Java Gnuplot Success: ").append(javaSucceeded).append("/").append(records.size()).append("\n");
        sb.append("\n");

        sb.append("Individual Results:\n");
        sb.append("-------------------\n");
        for (DemoTestRecord record : records) {
            String status = record.isPassing() ? "✅ PASS" : "❌ FAIL";
            sb.append(String.format("%-15s %s  (C:%s Java:%s)\n",
                record.getDemoName(),
                status,
                record.isCSuccess() ? "✓" : "✗",
                record.isJavaSuccess() ? "✓" : "✗"));
        }

        Files.writeString(summaryFile, sb.toString());

        // Print to console
        System.out.println("\n" + sb.toString());
        System.out.println("Full results saved to: " + currentRunDir);
        System.out.println("Summary: " + summaryFile);
    }

    public Path getCurrentRunDir() {
        return currentRunDir;
    }

    public List<DemoTestRecord> getRecords() {
        return new ArrayList<>(records);
    }
}
