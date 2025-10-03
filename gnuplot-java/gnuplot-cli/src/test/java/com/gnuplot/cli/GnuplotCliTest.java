package com.gnuplot.cli;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import picocli.CommandLine;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for GnuplotCli command-line interface.
 */
class GnuplotCliTest {

    @TempDir
    Path tempDir;

    @Test
    void testHelpOption() {
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(outContent));

        try {
            int exitCode = new CommandLine(new GnuplotCli()).execute("--help");
            assertThat(exitCode).isEqualTo(0);
            assertThat(outContent.toString()).contains("gnuplot-cli");
            assertThat(outContent.toString()).contains("Usage:");
        } finally {
            System.setOut(originalOut);
        }
    }

    @Test
    void testVersionOption() {
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(outContent));

        try {
            int exitCode = new CommandLine(new GnuplotCli()).execute("--version");
            assertThat(exitCode).isEqualTo(0);
            assertThat(outContent.toString()).contains("1.0.0-SNAPSHOT");
        } finally {
            System.setOut(originalOut);
        }
    }

    @Test
    void testBatchModeWithValidScript() throws Exception {
        Path scriptFile = tempDir.resolve("test.gp");
        Files.writeString(scriptFile, """
                set title "Test"
                plot sin(x)
                """);

        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(outContent));

        try {
            int exitCode = new CommandLine(new GnuplotCli()).execute(scriptFile.toString());
            assertThat(exitCode).isEqualTo(0);
            assertThat(outContent.toString()).contains("Executing script:");
            assertThat(outContent.toString()).contains("successfully");
        } finally {
            System.setOut(originalOut);
        }
    }

    @Test
    void testBatchModeWithNonexistentScript() {
        ByteArrayOutputStream errContent = new ByteArrayOutputStream();
        PrintStream originalErr = System.err;
        System.setErr(new PrintStream(errContent));

        try {
            int exitCode = new CommandLine(new GnuplotCli())
                    .execute("/nonexistent/script.gp");
            assertThat(exitCode).isEqualTo(1);
            assertThat(errContent.toString()).contains("Error reading script file");
        } finally {
            System.setErr(originalErr);
        }
    }

    @Test
    void testSingleCommandExecution() {
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(outContent));

        try {
            int exitCode = new CommandLine(new GnuplotCli())
                    .execute("-c", "plot sin(x)");
            assertThat(exitCode).isEqualTo(0);
            // Should execute without error
        } finally {
            System.setOut(originalOut);
        }
    }

    @Test
    void testMultipleCommandExecution() {
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(outContent));

        try {
            int exitCode = new CommandLine(new GnuplotCli())
                    .execute("-e", "set title \"Test\"", "-e", "plot sin(x)");
            assertThat(exitCode).isEqualTo(0);
            // Should execute without error
        } finally {
            System.setOut(originalOut);
        }
    }

    @Test
    void testPipeModeSimulation() {
        // Simulate piped input
        String input = "plot sin(x)\n";
        InputStream originalIn = System.in;
        ByteArrayInputStream inContent = new ByteArrayInputStream(input.getBytes());
        System.setIn(inContent);

        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(outContent));

        try {
            int exitCode = new CommandLine(new GnuplotCli()).execute();
            assertThat(exitCode).isEqualTo(0);
            // Should execute without error
        } finally {
            System.setIn(originalIn);
            System.setOut(originalOut);
        }
    }

    @Test
    void testInvalidCommand() {
        ByteArrayOutputStream errContent = new ByteArrayOutputStream();
        PrintStream originalErr = System.err;
        System.setErr(new PrintStream(errContent));

        try {
            new CommandLine(new GnuplotCli()).execute("-c", "invalid syntax here");
            // Should handle error gracefully - error should be printed but not crash
        } finally {
            System.setErr(originalErr);
        }
    }
}
