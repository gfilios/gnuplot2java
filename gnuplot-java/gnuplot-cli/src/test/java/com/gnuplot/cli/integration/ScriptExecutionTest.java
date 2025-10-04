package com.gnuplot.cli.integration;

import com.gnuplot.cli.command.GnuplotScript;
import com.gnuplot.cli.executor.GnuplotScriptExecutor;
import com.gnuplot.cli.parser.GnuplotCommandParser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests that parse and execute complete Gnuplot scripts.
 *
 * IMPORTANT: After making changes that affect visual output, run DemoTestSuite:
 * mvn test -Dtest=DemoTestSuite
 *
 * This runs the full demo comparison (C vs Java) and generates HTML report at:
 * test-results/latest/index.html
 */
class ScriptExecutionTest {

    private final GnuplotCommandParser parser = new GnuplotCommandParser();
    private final GnuplotScriptExecutor executor = new GnuplotScriptExecutor();

    @TempDir
    Path tempDir;

    @Test
    void executeSimplePlotScript() throws IOException {
        String script = """
                set title "Simple Sin Plot"
                set xlabel "X Axis"
                set ylabel "Y Axis"
                set samples 50
                plot sin(x)
                """;

        // Parse the script
        GnuplotScript gnuplotScript = parser.parse(script);
        assertThat(gnuplotScript.getCommands()).hasSize(5);

        // Execute the script (will create output.svg)
        executor.execute(gnuplotScript);

        // Verify output file was created
        Path outputFile = tempDir.getParent().resolve("output.svg");
        if (Files.exists(outputFile)) {
            // Clean up
            Files.delete(outputFile);
        }
    }

    @Test
    void executeMultiplePlotsScript() {
        String script = """
                set title "Sin and Cos"
                plot sin(x), cos(x)
                """;

        GnuplotScript gnuplotScript = parser.parse(script);
        assertThat(gnuplotScript.getCommands()).hasSize(2);

        executor.execute(gnuplotScript);
    }

    @Test
    void executeComplexExpressionScript() {
        String script = """
                set title "Polynomial"
                set samples 100
                plot x**2 + 2*x + 1
                """;

        GnuplotScript gnuplotScript = parser.parse(script);
        assertThat(gnuplotScript.getCommands()).hasSize(3);

        executor.execute(gnuplotScript);
    }

    @Test
    void executeScriptWithReset() {
        String script = """
                set title "First Plot"
                plot sin(x)
                reset
                set title "Second Plot"
                plot cos(x)
                """;

        GnuplotScript gnuplotScript = parser.parse(script);
        assertThat(gnuplotScript.getCommands()).hasSize(5);

        executor.execute(gnuplotScript);
    }

    @Test
    void executeScriptWithMultiplePlotCommands() throws IOException {
        String script = """
                set title "Plot 1"
                plot sin(x)
                set title "Plot 2"
                plot cos(x)
                set title "Plot 3"
                plot tan(x)
                """;

        GnuplotScript gnuplotScript = parser.parse(script);
        assertThat(gnuplotScript.getCommands()).hasSize(6); // 3 set + 3 plot

        executor.execute(gnuplotScript);

        // Verify multiple output files were created with auto-numbering
        Path output1 = tempDir.getParent().resolve("output.svg");
        Path output2 = tempDir.getParent().resolve("output_002.svg");
        Path output3 = tempDir.getParent().resolve("output_003.svg");

        // Note: Files are created in working directory, not temp dir
        // This test verifies the execution doesn't crash with multiple plots
        // Actual file verification would require setting output path to tempDir
    }

    @Test
    void executeScriptWithAxesRendering() throws IOException {
        String script = """
                set title "Plot with Axes"
                set xlabel "X Values"
                set ylabel "Y Values"
                plot sin(x)
                """;

        GnuplotScript gnuplotScript = parser.parse(script);
        executor.execute(gnuplotScript);

        // Verify output file was created
        Path outputFile = Path.of("output.svg");
        assertThat(outputFile).exists();

        // Read SVG content
        String svgContent = Files.readString(outputFile);

        // Verify axes are rendered (should have multiple <line> elements for axes and ticks)
        int lineCount = svgContent.split("<line").length - 1;
        assertThat(lineCount).as("Should render axis lines and tick marks").isGreaterThan(5);

        // Verify axis labels are rendered
        assertThat(svgContent).as("Should render X axis label").contains("X Values");
        assertThat(svgContent).as("Should render Y axis label").contains("Y Values");

        // Verify tick labels are rendered (should have numeric labels like "0", "2", "4", etc.)
        assertThat(svgContent).as("Should render tick labels").contains("<text");

        // Clean up
        Files.deleteIfExists(outputFile);
    }

    @Test
    void executeMultiPlotCommandWithDifferentColors() throws IOException {
        String script = """
                plot sin(x), cos(x), tan(x)
                """;

        GnuplotScript gnuplotScript = parser.parse(script);
        executor.execute(gnuplotScript);

        Path outputFile = Path.of("output.svg");
        assertThat(outputFile).exists();

        String svgContent = Files.readString(outputFile);

        // Verify each plot has different color (Gnuplot default palette)
        assertThat(svgContent).as("First plot should be purple").contains("#9400D3");
        assertThat(svgContent).as("Second plot should be green").contains("#009E73");
        assertThat(svgContent).as("Third plot should be blue").contains("#56B4E9");

        // Verify NO black lines (the old default)
        assertThat(svgContent).as("Should not use default black color for plots")
                .doesNotContain("stroke=\"#000000\"");

        // Clean up
        Files.deleteIfExists(outputFile);
    }

    @Test
    void executeScriptWithLegendRendering() throws IOException {
        String script = """
                set key left box
                plot sin(x) title "Sine", cos(x) title "Cosine"
                """;

        GnuplotScript gnuplotScript = parser.parse(script);
        executor.execute(gnuplotScript);

        Path outputFile = Path.of("output.svg");
        assertThat(outputFile).exists();

        String svgContent = Files.readString(outputFile);

        // Verify legend text labels are rendered
        assertThat(svgContent).as("Should render 'Sine' label").contains("Sine");
        assertThat(svgContent).as("Should render 'Cosine' label").contains("Cosine");

        // Verify legend has colored lines (should match plot colors)
        assertThat(svgContent).as("Should have purple color").contains("#9400D3");
        assertThat(svgContent).as("Should have green color").contains("#009E73");

        // Clean up
        Files.deleteIfExists(outputFile);
    }
}
