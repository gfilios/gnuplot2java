package com.gnuplot.cli;

import com.gnuplot.cli.command.GnuplotScript;
import com.gnuplot.cli.executor.GnuplotScriptExecutor;
import com.gnuplot.cli.parser.GnuplotCommandParser;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.UserInterruptException;
import org.jline.reader.EndOfFileException;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.Callable;

/**
 * Main CLI entry point for Gnuplot Java implementation.
 *
 * Supports three modes:
 * 1. Interactive mode: gnuplot-cli (starts REPL shell)
 * 2. Batch mode: gnuplot-cli script.gp (executes script file)
 * 3. Pipe mode: echo "plot sin(x)" | gnuplot-cli (reads from stdin)
 */
@Command(
    name = "gnuplot-cli",
    mixinStandardHelpOptions = true,
    version = "gnuplot-cli 1.0.0-SNAPSHOT",
    description = "Gnuplot Java CLI - A Java implementation of gnuplot"
)
public class GnuplotCli implements Callable<Integer> {

    @Parameters(
        index = "0",
        arity = "0..1",
        paramLabel = "FILE",
        description = "Script file to execute (optional, if omitted starts interactive mode)"
    )
    private Path scriptFile;

    @Option(
        names = {"-c", "--command"},
        description = "Execute a single command and exit"
    )
    private String command;

    @Option(
        names = {"-e", "--execute"},
        description = "Execute commands (can be used multiple times)",
        arity = "1..*"
    )
    private String[] commands;

    @Option(
        names = {"-p", "--persist"},
        description = "Keep output window open after script execution"
    )
    private boolean persist;

    private final GnuplotCommandParser parser = new GnuplotCommandParser();
    private final GnuplotScriptExecutor executor = new GnuplotScriptExecutor();

    public static void main(String[] args) {
        int exitCode = new CommandLine(new GnuplotCli()).execute(args);
        System.exit(exitCode);
    }

    @Override
    public Integer call() throws Exception {
        // Mode 1: Execute single command
        if (command != null) {
            executeCommand(command);
            return 0;
        }

        // Mode 2: Execute multiple commands
        if (commands != null && commands.length > 0) {
            for (String cmd : commands) {
                executeCommand(cmd);
            }
            return 0;
        }

        // Mode 3: Execute script file
        if (scriptFile != null) {
            return executeBatchMode(scriptFile);
        }

        // Mode 4: Check if stdin is piped
        if (System.console() == null || isPiped()) {
            return executePipeMode();
        }

        // Mode 5: Interactive mode (default)
        return executeInteractiveMode();
    }

    /**
     * Execute a single command string.
     */
    private void executeCommand(String commandText) {
        try {
            GnuplotScript script = parser.parse(commandText);
            executor.execute(script);
        } catch (Exception e) {
            System.err.println("Error executing command: " + e.getMessage());
        }
    }

    /**
     * Execute a script file (batch mode).
     */
    private int executeBatchMode(Path scriptPath) {
        System.out.println("Executing script: " + scriptPath);

        try {
            String scriptContent = Files.readString(scriptPath);
            GnuplotScript script = parser.parse(scriptContent);
            executor.execute(script);
            System.out.println("Script executed successfully.");
            return 0;
        } catch (IOException e) {
            System.err.println("Error reading script file: " + e.getMessage());
            return 1;
        } catch (Exception e) {
            System.err.println("Error executing script: " + e.getMessage());
            return 1;
        }
    }

    /**
     * Read from stdin pipe and execute.
     */
    private int executePipeMode() {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(System.in))) {
            StringBuilder scriptContent = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                scriptContent.append(line).append("\n");
            }

            if (scriptContent.length() > 0) {
                GnuplotScript script = parser.parse(scriptContent.toString());
                executor.execute(script);
            }

            return 0;
        } catch (IOException e) {
            System.err.println("Error reading from stdin: " + e.getMessage());
            return 1;
        } catch (Exception e) {
            System.err.println("Error executing piped input: " + e.getMessage());
            return 1;
        }
    }

    /**
     * Start interactive REPL shell.
     */
    private int executeInteractiveMode() {
        System.out.println("Gnuplot Java Interactive Shell");
        System.out.println("Version 1.0.0-SNAPSHOT");
        System.out.println("Type 'help' for help, 'quit' or 'exit' to exit");
        System.out.println();

        try (Terminal terminal = TerminalBuilder.builder()
                .system(true)
                .dumb(true)  // Allow dumb terminal fallback
                .build()) {
            LineReader lineReader = LineReaderBuilder.builder()
                    .terminal(terminal)
                    .build();

            StringBuilder multiLineCommand = new StringBuilder();

            while (true) {
                try {
                    String prompt = multiLineCommand.length() > 0 ? "> " : "gnuplot> ";
                    String line = lineReader.readLine(prompt);

                    // Handle empty lines
                    if (line == null || line.trim().isEmpty()) {
                        continue;
                    }

                    // Handle exit commands
                    if (line.trim().equalsIgnoreCase("quit") ||
                        line.trim().equalsIgnoreCase("exit")) {
                        System.out.println("Goodbye!");
                        break;
                    }

                    // Handle help
                    if (line.trim().equalsIgnoreCase("help")) {
                        printHelp();
                        continue;
                    }

                    // Accumulate multi-line commands
                    multiLineCommand.append(line).append("\n");

                    // Check if command is complete (ends with newline, not continuation)
                    // For now, execute each line immediately
                    try {
                        GnuplotScript script = parser.parse(multiLineCommand.toString());
                        executor.execute(script);
                        multiLineCommand.setLength(0); // Clear for next command
                    } catch (Exception e) {
                        System.err.println("Error: " + e.getMessage());
                        multiLineCommand.setLength(0); // Clear on error
                    }

                } catch (UserInterruptException e) {
                    // Ctrl+C pressed
                    multiLineCommand.setLength(0);
                    System.out.println("\nInterrupted.");
                } catch (EndOfFileException e) {
                    // Ctrl+D pressed
                    System.out.println("\nGoodbye!");
                    break;
                }
            }

            return 0;
        } catch (IOException e) {
            System.err.println("Terminal error: " + e.getMessage());
            return 1;
        }
    }

    /**
     * Print help message.
     */
    private void printHelp() {
        System.out.println("Gnuplot Java Interactive Shell - Help");
        System.out.println("=====================================");
        System.out.println();
        System.out.println("Commands:");
        System.out.println("  set title \"My Plot\"    - Set plot title");
        System.out.println("  set xlabel \"X Axis\"    - Set X axis label");
        System.out.println("  set ylabel \"Y Axis\"    - Set Y axis label");
        System.out.println("  set samples 100        - Set number of samples");
        System.out.println("  set grid               - Enable grid");
        System.out.println("  plot sin(x)            - Plot sin(x)");
        System.out.println("  plot sin(x), cos(x)    - Plot multiple functions");
        System.out.println("  reset                  - Reset all settings");
        System.out.println("  help                   - Show this help");
        System.out.println("  quit/exit              - Exit the shell");
        System.out.println();
        System.out.println("Examples:");
        System.out.println("  gnuplot> set title \"Sine Wave\"");
        System.out.println("  gnuplot> plot sin(x)");
        System.out.println();
    }

    /**
     * Check if stdin is piped.
     */
    private boolean isPiped() {
        try {
            return System.in.available() > 0;
        } catch (IOException e) {
            return false;
        }
    }
}
