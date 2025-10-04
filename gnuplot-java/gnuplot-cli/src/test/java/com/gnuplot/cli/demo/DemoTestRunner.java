package com.gnuplot.cli.demo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Test runner for executing Gnuplot demo scripts in both C and Java implementations.
 * Compares outputs to validate compatibility.
 */
public class DemoTestRunner {

    private final Path demoDirectory;
    private final Path outputDirectory;
    private final Path gnuplotExecutable;
    private final Path javaCliJar;
    private final long timeoutSeconds;

    /**
     * Result of running a demo script.
     */
    public static class DemoResult {
        private final String demoName;
        private final boolean cExecutionSuccess;
        private final boolean javaExecutionSuccess;
        private final String cOutput;
        private final String javaOutput;
        private final String cError;
        private final String javaError;
        private final Path cOutputFile;
        private final Path javaOutputFile;
        private final List<Path> cAdditionalOutputFiles;
        private final List<Path> javaAdditionalOutputFiles;
        private final Path modifiedScript;

        public DemoResult(String demoName, boolean cExecutionSuccess, boolean javaExecutionSuccess,
                         String cOutput, String javaOutput, String cError, String javaError,
                         Path cOutputFile, Path javaOutputFile, Path modifiedScript) {
            this(demoName, cExecutionSuccess, javaExecutionSuccess, cOutput, javaOutput, cError, javaError,
                 cOutputFile, javaOutputFile, new ArrayList<>(), new ArrayList<>(), modifiedScript);
        }

        public DemoResult(String demoName, boolean cExecutionSuccess, boolean javaExecutionSuccess,
                         String cOutput, String javaOutput, String cError, String javaError,
                         Path cOutputFile, Path javaOutputFile,
                         List<Path> cAdditionalOutputFiles, List<Path> javaAdditionalOutputFiles,
                         Path modifiedScript) {
            this.demoName = demoName;
            this.cExecutionSuccess = cExecutionSuccess;
            this.javaExecutionSuccess = javaExecutionSuccess;
            this.cOutput = cOutput;
            this.javaOutput = javaOutput;
            this.cError = cError;
            this.javaError = javaError;
            this.cOutputFile = cOutputFile;
            this.javaOutputFile = javaOutputFile;
            this.cAdditionalOutputFiles = cAdditionalOutputFiles;
            this.javaAdditionalOutputFiles = javaAdditionalOutputFiles;
            this.modifiedScript = modifiedScript;
        }

        public String getDemoName() { return demoName; }
        public boolean isCExecutionSuccess() { return cExecutionSuccess; }
        public boolean isJavaExecutionSuccess() { return javaExecutionSuccess; }
        public String getCOutput() { return cOutput; }
        public String getJavaOutput() { return javaOutput; }
        public String getCError() { return cError; }
        public String getJavaError() { return javaError; }
        public Path getCOutputFile() { return cOutputFile; }
        public Path getJavaOutputFile() { return javaOutputFile; }
        public List<Path> getCAdditionalOutputFiles() { return cAdditionalOutputFiles; }
        public List<Path> getJavaAdditionalOutputFiles() { return javaAdditionalOutputFiles; }
        public Path getModifiedScript() { return modifiedScript; }

        public boolean isPassing() {
            return cExecutionSuccess && javaExecutionSuccess
                   && cOutputFile != null && javaOutputFile != null
                   && Files.exists(cOutputFile) && Files.exists(javaOutputFile);
        }
    }

    /**
     * Builder for DemoTestRunner.
     */
    public static class Builder {
        private Path demoDirectory;
        private Path outputDirectory;
        private Path gnuplotExecutable = Paths.get("/opt/homebrew/bin/gnuplot");
        private Path javaCliJar;
        private long timeoutSeconds = 30;

        public Builder demoDirectory(Path path) {
            this.demoDirectory = path;
            return this;
        }

        public Builder outputDirectory(Path path) {
            this.outputDirectory = path;
            return this;
        }

        public Builder gnuplotExecutable(Path path) {
            this.gnuplotExecutable = path;
            return this;
        }

        public Builder javaCliJar(Path path) {
            this.javaCliJar = path;
            return this;
        }

        public Builder timeoutSeconds(long seconds) {
            this.timeoutSeconds = seconds;
            return this;
        }

        public DemoTestRunner build() throws IOException {
            if (demoDirectory == null) {
                throw new IllegalStateException("Demo directory must be set");
            }
            if (outputDirectory == null) {
                // Default to temp directory
                outputDirectory = Files.createTempDirectory("gnuplot-demo-test");
            }
            if (!Files.exists(demoDirectory)) {
                throw new IllegalStateException("Demo directory does not exist: " + demoDirectory);
            }
            Files.createDirectories(outputDirectory);

            return new DemoTestRunner(demoDirectory, outputDirectory, gnuplotExecutable,
                                     javaCliJar, timeoutSeconds);
        }
    }

    private DemoTestRunner(Path demoDirectory, Path outputDirectory, Path gnuplotExecutable,
                          Path javaCliJar, long timeoutSeconds) {
        this.demoDirectory = demoDirectory;
        this.outputDirectory = outputDirectory;
        this.gnuplotExecutable = gnuplotExecutable;
        this.javaCliJar = javaCliJar;
        this.timeoutSeconds = timeoutSeconds;
    }

    /**
     * Run a single demo script and compare C vs Java output.
     */
    public DemoResult runDemo(String demoFileName) throws IOException {
        Path demoFile = demoDirectory.resolve(demoFileName);
        if (!Files.exists(demoFile)) {
            throw new IllegalArgumentException("Demo file does not exist: " + demoFile);
        }

        String baseName = demoFileName.replace(".dem", "");

        // Run C gnuplot
        ExecutionResult cResult = executeCGnuplot(demoFile, baseName);

        // Run Java gnuplot
        ExecutionResult javaResult = executeJavaGnuplot(demoFile, baseName);

        // Keep reference to modified script
        Path modifiedScript = javaResult.modifiedScript != null ? javaResult.modifiedScript : cResult.modifiedScript;

        return new DemoResult(
            demoFileName,
            cResult.exitCode == 0,
            javaResult.exitCode == 0,
            cResult.stdout,
            javaResult.stdout,
            cResult.stderr,
            javaResult.stderr,
            cResult.outputFile,
            javaResult.outputFile,
            cResult.additionalOutputFiles,
            javaResult.additionalOutputFiles,
            modifiedScript
        );
    }

    /**
     * Execute C gnuplot with the demo script.
     */
    private ExecutionResult executeCGnuplot(Path demoFile, String baseName) throws IOException {
        Path outputFile = outputDirectory.resolve(baseName + "_c.svg");

        // Create a modified script that sets SVG terminal and output file
        Path modifiedScript = createModifiedScript(demoFile, outputFile, baseName + "_c");

        List<String> command = new ArrayList<>();
        command.add(gnuplotExecutable.toString());
        command.add(modifiedScript.toString());

        ExecutionResult result = executeProcess(command, demoDirectory, outputFile);
        return new ExecutionResult(result.exitCode, result.stdout, result.stderr, result.outputFile, result.additionalOutputFiles, modifiedScript);
    }

    /**
     * Execute Java gnuplot CLI with the demo script.
     */
    private ExecutionResult executeJavaGnuplot(Path demoFile, String baseName) throws IOException {
        Path outputFile = outputDirectory.resolve(baseName + "_java.svg");

        // Create a modified script that sets SVG terminal and output file
        Path modifiedScript = createModifiedScript(demoFile, outputFile, baseName + "_java");

        List<String> command = new ArrayList<>();

        // Use Maven exec:java to run with proper classpath
        Path cliDir = Paths.get(System.getProperty("user.dir"));
        command.add("mvn");
        command.add("exec:java");
        command.add("-q");
        command.add("-Dexec.mainClass=com.gnuplot.cli.GnuplotCli");
        command.add("-Dexec.args=" + modifiedScript.toString());

        ExecutionResult result = executeProcess(command, cliDir, outputFile);
        return new ExecutionResult(result.exitCode, result.stdout, result.stderr, result.outputFile, result.additionalOutputFiles, modifiedScript);
    }

    /**
     * Create a modified version of the demo script that:
     * 1. Sets SVG terminal
     * 2. Sets output file
     * 3. Removes interactive pauses
     */
    private Path createModifiedScript(Path originalScript, Path outputFile, String baseName)
            throws IOException {
        List<String> lines = Files.readAllLines(originalScript);
        List<String> modifiedLines = new ArrayList<>();

        // Add SVG terminal and output settings at the beginning
        modifiedLines.add("set term svg size 800,600");
        modifiedLines.add("set output '" + outputFile.toString() + "'");

        // Process original lines
        for (String line : lines) {
            String trimmed = line.trim();

            // Skip pause commands (they require user interaction)
            if (trimmed.startsWith("pause") && trimmed.contains("-1")) {
                continue;
            }

            // Skip set term/output commands from original script
            if (trimmed.startsWith("set term") || trimmed.startsWith("set output")) {
                continue;
            }

            modifiedLines.add(line);
        }

        Path modifiedScript = outputDirectory.resolve(baseName + "_modified.dem");
        Files.write(modifiedScript, modifiedLines);

        return modifiedScript;
    }

    /**
     * Execute a process and capture output.
     */
    private ExecutionResult executeProcess(List<String> command, Path workingDir, Path expectedOutputFile)
            throws IOException {
        ProcessBuilder pb = new ProcessBuilder(command);
        pb.directory(workingDir.toFile());
        pb.redirectErrorStream(false);

        Process process = pb.start();

        StringBuilder stdout = new StringBuilder();
        StringBuilder stderr = new StringBuilder();

        try {
            // Read stdout
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    stdout.append(line).append("\n");
                }
            }

            // Read stderr
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getErrorStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    stderr.append(line).append("\n");
                }
            }

            boolean finished = process.waitFor(timeoutSeconds, TimeUnit.SECONDS);
            int exitCode = finished ? process.exitValue() : -1;

            if (!finished) {
                process.destroyForcibly();
                stderr.append("Process timed out after ").append(timeoutSeconds).append(" seconds\n");
            }

            // Check for expected output file
            Path outputFile = Files.exists(expectedOutputFile) ? expectedOutputFile : null;

            // Also check for numbered output files (output_002.svg, output_003.svg, etc.)
            // These are created when a script has multiple plot commands
            List<Path> additionalFiles = new ArrayList<>();
            if (outputFile != null) {
                String baseName = expectedOutputFile.getFileName().toString().replace(".svg", "");
                Path parent = expectedOutputFile.getParent();

                // Check if this is a multi-page SVG (C gnuplot creates these)
                // If so, split it into separate files
                List<Path> splitFiles = splitMultiPageSvg(outputFile, parent, baseName);
                if (!splitFiles.isEmpty()) {
                    // Multi-page SVG was split successfully
                    additionalFiles.addAll(splitFiles);
                } else {
                    // Single-page SVG - scan for pre-existing numbered files (Java gnuplot creates these)
                    for (int i = 2; i <= 100; i++) { // Check up to 100 plots
                        Path numberedFile = parent.resolve(String.format("%s_%03d.svg", baseName, i));
                        if (Files.exists(numberedFile)) {
                            additionalFiles.add(numberedFile);
                        } else {
                            break; // Stop when we find a gap
                        }
                    }
                }
            }

            return new ExecutionResult(exitCode, stdout.toString(), stderr.toString(),
                                      outputFile, additionalFiles);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Process execution interrupted", e);
        }
    }

    /**
     * Split a multi-page SVG file (with multiple <svg> elements) into separate files.
     * Returns list of additional SVG files created (excludes the first page which stays in original file).
     */
    private List<Path> splitMultiPageSvg(Path svgFile, Path outputDir, String baseName) throws IOException {
        List<Path> additionalFiles = new ArrayList<>();

        // Read the SVG file
        String content = Files.readString(svgFile);

        // Check if this is a multi-page SVG by counting <svg> tags
        int svgCount = 0;
        int pos = 0;
        while ((pos = content.indexOf("<svg", pos)) != -1) {
            svgCount++;
            pos += 4;
        }

        // If only 1 <svg> element, no splitting needed
        if (svgCount <= 1) {
            return additionalFiles;
        }

        // Extract individual SVG elements
        List<String> svgElements = new ArrayList<>();
        int startPos = 0;
        while (true) {
            int svgStart = content.indexOf("<svg", startPos);
            if (svgStart == -1) break;

            int svgEnd = content.indexOf("</svg>", svgStart);
            if (svgEnd == -1) break;
            svgEnd += 6; // Include </svg>

            String svgElement = content.substring(svgStart, svgEnd);
            svgElements.add(svgElement);
            startPos = svgEnd;
        }

        // Write each SVG to a separate file
        for (int i = 0; i < svgElements.size(); i++) {
            String svgContent = "<?xml version=\"1.0\" encoding=\"utf-8\"  standalone=\"no\"?>\n" + svgElements.get(i);

            if (i == 0) {
                // Overwrite the original file with just the first page
                Files.writeString(svgFile, svgContent);
            } else {
                // Create numbered files for additional pages
                Path numberedFile = outputDir.resolve(String.format("%s_%03d.svg", baseName, i + 1));
                // Only write if file doesn't exist (avoid recursion)
                if (!Files.exists(numberedFile)) {
                    Files.writeString(numberedFile, svgContent);
                }
                additionalFiles.add(numberedFile);
            }
        }

        return additionalFiles;
    }

    /**
     * Get classpath for direct Java execution.
     * This is a placeholder - in practice we'll use the assembled JAR.
     */
    private String getClasspath() {
        // TODO: Build proper classpath or use assembled JAR
        return "target/classes:target/test-classes";
    }

    /**
     * Result of executing a process.
     */
    private static class ExecutionResult {
        final int exitCode;
        final String stdout;
        final String stderr;
        final Path outputFile;
        final List<Path> additionalOutputFiles;
        final Path modifiedScript;

        ExecutionResult(int exitCode, String stdout, String stderr, Path outputFile) {
            this(exitCode, stdout, stderr, outputFile, new ArrayList<>());
        }

        ExecutionResult(int exitCode, String stdout, String stderr, Path outputFile, List<Path> additionalOutputFiles) {
            this(exitCode, stdout, stderr, outputFile, additionalOutputFiles, null);
        }

        ExecutionResult(int exitCode, String stdout, String stderr, Path outputFile,
                       List<Path> additionalOutputFiles, Path modifiedScript) {
            this.exitCode = exitCode;
            this.stdout = stdout;
            this.stderr = stderr;
            this.outputFile = outputFile;
            this.additionalOutputFiles = additionalOutputFiles != null ? additionalOutputFiles : new ArrayList<>();
            this.modifiedScript = modifiedScript;
        }
    }
}
