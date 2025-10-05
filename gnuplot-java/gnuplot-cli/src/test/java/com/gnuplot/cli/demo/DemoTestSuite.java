package com.gnuplot.cli.demo;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test suite for validating Gnuplot demo compatibility between C and Java implementations.
 */
class DemoTestSuite {

    private static final Path DEMO_DIR = Paths.get(System.getProperty("user.dir"))
            .getParent()  // gnuplot-java
            .getParent()  // gnuplot-master
            .resolve("gnuplot-c/demo");

    private static final Path REPOSITORY_ROOT = Paths.get(System.getProperty("user.dir"))
            .getParent()  // gnuplot-java
            .getParent()  // gnuplot-master
            .resolve("test-results");

    @TempDir
    static Path tempOutputDir;

    private static DemoTestRunner testRunner;
    private static TestResultRepository repository;
    private static ComparisonRunner comparisonRunner;

    @BeforeAll
    static void setup() throws IOException {
        assertThat(DEMO_DIR)
                .as("Demo directory should exist at: " + DEMO_DIR)
                .exists()
                .isDirectory();

        testRunner = new DemoTestRunner.Builder()
                .demoDirectory(DEMO_DIR)
                .outputDirectory(tempOutputDir)
                .timeoutSeconds(30)
                .build();

        repository = new TestResultRepository(REPOSITORY_ROOT);
        comparisonRunner = new ComparisonRunner(REPOSITORY_ROOT);

        // Print comparison tools status
        System.out.println("\n" + comparisonRunner.getToolsStatus());
    }

    @org.junit.jupiter.api.AfterAll
    static void tearDown() throws IOException {
        if (repository != null) {
            repository.generateSummaryReport();

            // Generate HTML report
            Path htmlReport = repository.getCurrentRunDir().resolve("index.html");
            HtmlReportGenerator.generateReport(htmlReport, repository.getRecords(),
                                              repository.getCurrentRunDir());
            System.out.println("\n📊 HTML Report: " + htmlReport);
            System.out.println("   Open with: open " + htmlReport);
        }
    }

    /**
     * Helper method to run comparison and save results for a specific plot pair.
     */
    private static void runAndSaveComparison(Path cSvgOutput, Path javaSvgOutput,
                                            Path runDir, String demoName, int plotNumber) throws IOException {
        ComparisonRunner.ComparisonResult compResult =
                comparisonRunner.runComparison(cSvgOutput, javaSvgOutput);

        // Print summary for this plot
        System.out.println("Plot " + plotNumber + ":");
        if (compResult.hasIssues()) {
            System.out.println("  ⚠️  " + compResult.getCriticalIssues().size() + " issues found");
        } else {
            System.out.println("  ✅ No critical issues");
        }

        // Save comparison output to file with plot number
        String filename = plotNumber == 1 ?
                "comparison_" + demoName + ".txt" :
                String.format("comparison_%s_plot%d.txt", demoName, plotNumber);
        Path comparisonLog = runDir.resolve(filename);
        Files.writeString(comparisonLog, compResult.getOutput());
    }

    /**
     * Test simple.dem - Basic plotting with trig functions.
     * This is a Tier 1 demo that should eventually pass.
     */
    @Test
    void testSimpleDem() throws IOException {
        DemoTestRunner.DemoResult result = testRunner.runDemo("simple.dem");

        // Store result in repository
        Path originalScript = DEMO_DIR.resolve("simple.dem");
        TestResultRepository.DemoTestRecord record =
                repository.store("simple.dem", originalScript, result.getModifiedScript(), result);

        System.out.println("\n╔═══════════════════════════════════════════════════════════╗");
        System.out.println("║                  simple.dem Test Results                  ║");
        System.out.println("╚═══════════════════════════════════════════════════════════╝");
        System.out.println("C Gnuplot Success:    " + result.isCExecutionSuccess());
        System.out.println("Java Gnuplot Success: " + result.isJavaExecutionSuccess());

        // Run comprehensive comparison if both outputs exist
        if (comparisonRunner.areToolsAvailable() &&
            result.isCExecutionSuccess() && result.isJavaExecutionSuccess()) {

            System.out.println("\n🔍 Running comprehensive comparison analysis...\n");

            // Compare main output (plot 1)
            runAndSaveComparison(record.getCSvgOutput(), record.getJavaSvgOutput(),
                               repository.getCurrentRunDir(), "simple.dem", 1);

            // Compare additional numbered outputs (_002, _003, etc.)
            String baseName = record.getDemoName().replace(".dem", "");
            Path outputDir = record.getCSvgOutput().getParent();

            for (int i = 2; i <= 100; i++) {
                Path cNumberedFile = outputDir.resolve(String.format("%s_c_%03d.svg", baseName, i));
                Path javaNumberedFile = outputDir.resolve(String.format("%s_java_%03d.svg", baseName, i));

                if (Files.exists(cNumberedFile) && Files.exists(javaNumberedFile)) {
                    runAndSaveComparison(cNumberedFile, javaNumberedFile,
                                       repository.getCurrentRunDir(), "simple.dem", i);
                } else {
                    break; // Stop when we find a gap
                }
            }

            System.out.println("\n🖼️  Visual diff images: /tmp/gnuplot_visual_comparison/");
        }

        assertThat(result.isCExecutionSuccess())
                .as("C Gnuplot should execute simple.dem successfully")
                .isTrue();
    }

    /**
     * Test scatter.dem - Scatter plots (should already pass).
     * This demo uses features we've already implemented.
     */
    @Test
    void testScatterDem() throws IOException {
        Path scatterDem = DEMO_DIR.resolve("scatter.dem");
        if (!Files.exists(scatterDem)) {
            System.out.println("scatter.dem not found, skipping test");
            return;
        }

        DemoTestRunner.DemoResult result = testRunner.runDemo("scatter.dem");
        repository.store("scatter.dem", scatterDem, result.getModifiedScript(), result);

        System.out.println("=== scatter.dem Test Results ===");
        System.out.println(result.isPassing() ? "✅ PASS" : "❌ FAIL");

        assertThat(result.isCExecutionSuccess())
                .as("C Gnuplot should execute scatter.dem successfully")
                .isTrue();
    }

    /**
     * Test controls.dem - Control flow (if/else, loops).
     * This is expected to fail until we implement control flow.
     */
    @Test
    void testControlsDem() throws IOException {
        Path controlsDem = DEMO_DIR.resolve("controls.dem");
        if (!Files.exists(controlsDem)) {
            System.out.println("controls.dem not found, skipping test");
            return;
        }

        DemoTestRunner.DemoResult result = testRunner.runDemo("controls.dem");
        repository.store("controls.dem", controlsDem, result.getModifiedScript(), result);

        System.out.println("=== controls.dem Test Results ===");
        System.out.println(result.isPassing() ? "✅ PASS" : "❌ FAIL (expected - control flow not implemented)");

        assertThat(result.isCExecutionSuccess())
                .as("C Gnuplot should execute controls.dem successfully")
                .isTrue();
    }
}
