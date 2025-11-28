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
    private static PixelComparator pixelComparator;
    private static SvgStructuralComparator structuralComparator;

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
        pixelComparator = new PixelComparator();
        structuralComparator = new SvgStructuralComparator();
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
        // Run structural comparison (primary comparison method)
        SvgStructuralComparator.StructuralComparisonResult structResult = null;
        try {
            structResult = structuralComparator.compare(cSvgOutput, javaSvgOutput);
        } catch (Exception e) {
            System.out.println("  ⚠️  Structural comparison failed: " + e.getMessage());
        }

        // Run pixel comparison with amplified diff (secondary/visual comparison)
        PixelComparator.PixelComparisonResult pixelResult = null;
        try {
            pixelResult = pixelComparator.compareWithAmplifiedDiff(cSvgOutput, javaSvgOutput);
        } catch (Exception e) {
            System.out.println("  ⚠️  Pixel comparison failed: " + e.getMessage());
        }

        // Print summary for this plot
        System.out.println("Plot " + plotNumber + ":");

        // Print structural comparison results (primary)
        if (structResult != null) {
            if (structResult.isStructurallyEquivalent()) {
                System.out.println("  ✅ Structurally equivalent");
            } else {
                System.out.println("  🔴 Structural differences found:");
                for (String diff : structResult.getCriticalDifferences()) {
                    System.out.println("      • " + diff);
                }
            }
            if (!structResult.getMinorDifferences().isEmpty()) {
                System.out.println("  🟡 Minor differences:");
                for (String diff : structResult.getMinorDifferences()) {
                    System.out.println("      - " + diff);
                }
            }
        }

        // Print pixel comparison results
        if (pixelResult != null) {
            String similarityColor = pixelResult.getSimilarity() > 0.95 ? "🟢" :
                                    pixelResult.getSimilarity() > 0.80 ? "🟡" : "🔴";
            System.out.printf("  %s Pixel similarity: %.2f%% (%,d pixels differ)%n",
                    similarityColor,
                    pixelResult.getSimilarity() * 100,
                    pixelResult.getDifferentPixels());

            // Save diff image
            String diffFilename = plotNumber == 1 ?
                    "diff_" + demoName.replace(".dem", "") + ".png" :
                    String.format("diff_%s_plot%d.png", demoName.replace(".dem", ""), plotNumber);
            Path diffImagePath = runDir.resolve("outputs").resolve(diffFilename);
            pixelComparator.saveDiffImage(pixelResult, diffImagePath);

            // Save metrics to a JSON-like file for HTML report (use Locale.US for decimal point)
            String metricsFilename = plotNumber == 1 ?
                    "metrics_" + demoName.replace(".dem", "") + ".txt" :
                    String.format("metrics_%s_plot%d.txt", demoName.replace(".dem", ""), plotNumber);
            Path metricsPath = runDir.resolve(metricsFilename);
            String metrics = String.format(java.util.Locale.US,
                    "similarity=%.4f%ndifferentPixels=%d%ntotalPixels=%d%n",
                    pixelResult.getSimilarity(),
                    pixelResult.getDifferentPixels(),
                    pixelResult.getTotalPixels());
            Files.writeString(metricsPath, metrics);
        }

        // Save structural comparison to file
        if (structResult != null) {
            String structFilename = plotNumber == 1 ?
                    "structural_" + demoName.replace(".dem", "") + ".txt" :
                    String.format("structural_%s_plot%d.txt", demoName.replace(".dem", ""), plotNumber);
            Path structPath = runDir.resolve(structFilename);
            Files.writeString(structPath, structResult.toDetailedReport());

            // Save structural metrics in key=value format for HTML report
            String structMetricsFilename = plotNumber == 1 ?
                    "structural_metrics_" + demoName.replace(".dem", "") + ".txt" :
                    String.format("structural_metrics_%s_plot%d.txt", demoName.replace(".dem", ""), plotNumber);
            Path structMetricsPath = runDir.resolve(structMetricsFilename);
            StringBuilder structMetrics = new StringBuilder();
            structMetrics.append("structurallyEquivalent=").append(structResult.isStructurallyEquivalent()).append("\n");
            structMetrics.append("criticalDifferenceCount=").append(structResult.getCriticalDifferences().size()).append("\n");
            structMetrics.append("minorDifferenceCount=").append(structResult.getMinorDifferences().size()).append("\n");

            SvgStructuralComparator.SvgMetrics cMetrics = structResult.getCMetrics();
            SvgStructuralComparator.SvgMetrics javaMetrics = structResult.getJavaMetrics();
            structMetrics.append("cXAxisTicks=").append(cMetrics.getXAxisTickCount()).append("\n");
            structMetrics.append("javaXAxisTicks=").append(javaMetrics.getXAxisTickCount()).append("\n");
            structMetrics.append("cYAxisTicks=").append(cMetrics.getYAxisTickCount()).append("\n");
            structMetrics.append("javaYAxisTicks=").append(javaMetrics.getYAxisTickCount()).append("\n");
            structMetrics.append("cDataSeries=").append(cMetrics.getDataPointCounts().size()).append("\n");
            structMetrics.append("javaDataSeries=").append(javaMetrics.getDataPointCounts().size()).append("\n");
            structMetrics.append("cTotalDataPoints=").append(cMetrics.getTotalDataPoints()).append("\n");
            structMetrics.append("javaTotalDataPoints=").append(javaMetrics.getTotalDataPoints()).append("\n");
            structMetrics.append("cTextCount=").append(cMetrics.getTextCount()).append("\n");
            structMetrics.append("javaTextCount=").append(javaMetrics.getTextCount()).append("\n");

            // Save critical differences as numbered entries
            int i = 0;
            for (String diff : structResult.getCriticalDifferences()) {
                structMetrics.append("criticalDiff").append(i++).append("=").append(diff).append("\n");
            }
            i = 0;
            for (String diff : structResult.getMinorDifferences()) {
                structMetrics.append("minorDiff").append(i++).append("=").append(diff).append("\n");
            }

            Files.writeString(structMetricsPath, structMetrics.toString());
        }

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
        if (result.isCExecutionSuccess() && result.isJavaExecutionSuccess()) {

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
        TestResultRepository.DemoTestRecord record =
                repository.store("scatter.dem", scatterDem, result.getModifiedScript(), result);

        System.out.println("\n╔═══════════════════════════════════════════════════════════╗");
        System.out.println("║                 scatter.dem Test Results                  ║");
        System.out.println("╚═══════════════════════════════════════════════════════════╝");
        System.out.println("C Gnuplot Success:    " + result.isCExecutionSuccess());
        System.out.println("Java Gnuplot Success: " + result.isJavaExecutionSuccess());

        // Run comprehensive comparison if both outputs exist
        if (result.isCExecutionSuccess() && result.isJavaExecutionSuccess()) {

            System.out.println("\n🔍 Running comprehensive comparison analysis...\n");

            // Compare main output (plot 1)
            runAndSaveComparison(record.getCSvgOutput(), record.getJavaSvgOutput(),
                               repository.getCurrentRunDir(), "scatter.dem", 1);

            // Compare additional numbered outputs (_002, _003, etc.)
            String baseName = record.getDemoName().replace(".dem", "");
            Path outputDir = record.getCSvgOutput().getParent();

            for (int i = 2; i <= 100; i++) {
                Path cNumberedFile = outputDir.resolve(String.format("%s_c_%03d.svg", baseName, i));
                Path javaNumberedFile = outputDir.resolve(String.format("%s_java_%03d.svg", baseName, i));

                if (Files.exists(cNumberedFile) && Files.exists(javaNumberedFile)) {
                    runAndSaveComparison(cNumberedFile, javaNumberedFile,
                                       repository.getCurrentRunDir(), "scatter.dem", i);
                } else {
                    break; // Stop when we find a gap
                }
            }

            System.out.println("\n🖼️  Visual diff images: /tmp/gnuplot_visual_comparison/");
        }

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
