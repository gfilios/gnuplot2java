# Gnuplot CLI

Command-line interface for the Gnuplot Java implementation.

## Features

### Execution Modes

1. **Interactive Mode** (REPL)
   ```bash
   gnuplot-cli
   ```
   Starts an interactive shell with line editing and command history.

2. **Batch Mode** (Script Execution)
   ```bash
   gnuplot-cli script.gp
   ```
   Executes a Gnuplot script file.

3. **Pipe Mode** (stdin)
   ```bash
   echo "plot sin(x)" | gnuplot-cli
   ```
   Reads commands from standard input.

4. **Single Command**
   ```bash
   gnuplot-cli -c "plot sin(x)"
   ```
   Executes a single command and exits.

5. **Multiple Commands**
   ```bash
   gnuplot-cli -e "set title \"Test\"" -e "plot sin(x)"
   ```
   Executes multiple commands in sequence.

## Usage Examples

### Interactive Session
```bash
$ gnuplot-cli
Gnuplot Java Interactive Shell
Version 1.0.0-SNAPSHOT
Type 'help' for help, 'quit' or 'exit' to exit

gnuplot> set title "Sine Wave"
gnuplot> set xlabel "X Axis"
gnuplot> set ylabel "Y Axis"
gnuplot> set samples 100
gnuplot> plot sin(x)
Rendered to: output.svg
gnuplot> quit
Goodbye!
```

### Script Execution
Create a file `demo.gp`:
```gnuplot
set title "Demo Plot"
set xlabel "X"
set ylabel "Y"
set samples 50
plot sin(x), cos(x)
```

Execute it:
```bash
$ gnuplot-cli demo.gp
Executing script: demo.gp
Rendered to: output.svg
Script executed successfully.
```

### Pipe Mode
```bash
$ cat <<EOF | gnuplot-cli
set title "Piped Plot"
plot sin(x)
EOF
Rendered to: output.svg
```

### Command-Line Options
```bash
# Show help
gnuplot-cli --help

# Show version
gnuplot-cli --version

# Execute single command
gnuplot-cli -c "plot sin(x)"

# Execute multiple commands
gnuplot-cli -e "set title \"Test\"" -e "plot sin(x)"
```

## Supported Commands

### SET Commands
- `set title "Title Text"` - Set plot title
- `set xlabel "X Label"` - Set X axis label
- `set ylabel "Y Label"` - Set Y axis label
- `set samples N` - Set number of sampling points
- `set grid` - Enable grid display
- `set autoscale` - Enable autoscaling

### PLOT Commands
- `plot <expression>` - Plot a mathematical expression
- `plot <expr1>, <expr2>, ...` - Plot multiple expressions
- `plot <expression> title "Label"` - Plot with custom label
- `plot <expression> with <style>` - Plot with specific style (lines, points, etc.)

### Other Commands
- `unset <option>` - Remove a setting
- `reset` - Reset all settings to defaults
- `pause <seconds> "message"` - Pause execution
- `help` - Show help (interactive mode only)
- `quit` / `exit` - Exit the shell (interactive mode only)

## Expressions

Supports full mathematical expressions:
- Arithmetic: `+`, `-`, `*`, `/`, `%`, `**` (power)
- Functions: `sin(x)`, `cos(x)`, `tan(x)`, `exp(x)`, `log(x)`, `sqrt(x)`, etc.
- Variables: `x` (automatically set based on range)
- Complex expressions: `x**2 + 2*x + 1`, `sin(x)*cos(x)`, etc.

## Building

```bash
mvn clean package
```

This creates an executable JAR with all dependencies:
```
target/gnuplot-cli-1.0.0-SNAPSHOT-jar-with-dependencies.jar
```

## Running

### With Maven
```bash
mvn exec:java -Dexec.mainClass="com.gnuplot.cli.GnuplotCli"
```

### With JAR
```bash
java -jar target/gnuplot-cli-1.0.0-SNAPSHOT-jar-with-dependencies.jar
```

## Architecture

### Components

1. **GnuplotCli** - Main entry point with Picocli integration
   - Command-line argument parsing
   - Mode detection and routing
   - JLine integration for interactive mode

2. **GnuplotCommandParser** - ANTLR4-based parser
   - Parses Gnuplot command syntax
   - Builds command AST
   - Error reporting

3. **GnuplotScriptExecutor** - Command executor
   - Translates commands to Java API calls
   - Integrates with gnuplot-core (expression evaluation)
   - Integrates with gnuplot-render (visualization)
   - Generates SVG output

### Dependencies

- **Picocli** - CLI framework for argument parsing and help generation
- **JLine** - Terminal support for interactive shell (line editing, history)
- **ANTLR4** - Parser generator for Gnuplot command grammar
- **gnuplot-core** - Expression parsing and evaluation
- **gnuplot-render** - Scene graph and SVG rendering

## Testing

```bash
mvn test
```

Test coverage:
- 8 CLI tests (modes, options, error handling)
- 17 parser tests (command parsing)
- 4 integration tests (end-to-end script execution)
- 2 debug tests (grammar validation)

Total: 31 tests

## Output

By default, plots are rendered to `output.svg` in the current directory.

## Future Enhancements

- [ ] Configurable output file path
- [ ] PNG/PDF output formats (requires additional rendering backends)
- [ ] Terminal-based plotting (ASCII art, sixel, etc.)
- [ ] Multi-line command support in interactive mode
- [ ] Command history persistence
- [ ] Tab completion for commands
- [ ] Variable inspection (`show` commands)
- [ ] Interactive plotting (hover, zoom, pan)
