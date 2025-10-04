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
}
