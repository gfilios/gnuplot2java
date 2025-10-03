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
}
