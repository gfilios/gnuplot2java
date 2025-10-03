# Gnuplot Java - Quick Start Guide

This guide will help you get started with the Gnuplot Java implementation quickly.

## Prerequisites

- Java 21 or higher
- Maven 3.9+
- Git

## Installation

```bash
# Clone the repository
git clone https://github.com/gfilios/gnuplot2java.git
cd gnuplot2java/gnuplot-java

# Build all modules
mvn clean install

# Verify build (run tests)
mvn test
```

You should see output indicating all tests pass:
```
Tests run: 989, Failures: 0, Errors: 0, Skipped: 0
```

## Using the CLI (Recommended for Getting Started)

The CLI is the easiest way to start creating plots.

### Interactive Mode

Start the interactive shell:

```bash
cd gnuplot-cli
mvn exec:java -Dexec.mainClass="com.gnuplot.cli.GnuplotCli"
```

Try these commands:

```gnuplot
gnuplot> set title "My First Plot"
gnuplot> set xlabel "X Axis"
gnuplot> set ylabel "Y Axis"
gnuplot> set samples 100
gnuplot> plot sin(x)
Rendered to: output.svg
gnuplot> quit
```

Open `output.svg` in a browser to see your plot!

### Script Mode

Create a file `demo.gp`:

```gnuplot
# Demo script
set title "Trigonometric Functions"
set xlabel "X (radians)"
set ylabel "Y"
set samples 100
set grid

# Plot multiple functions
plot sin(x), cos(x)
```

Execute it:

```bash
mvn exec:java -Dexec.mainClass="com.gnuplot.cli.GnuplotCli" -Dexec.args="demo.gp"
```

### Quick Commands

```bash
# Single command
mvn exec:java -Dexec.mainClass="com.gnuplot.cli.GnuplotCli" -Dexec.args='-c "plot sin(x)"'

# Multiple commands
mvn exec:java -Dexec.mainClass="com.gnuplot.cli.GnuplotCli" \
  -Dexec.args='-e "set title \"Test\"" -e "plot sin(x)"'

# Pipe mode
echo "plot sin(x)" | mvn exec:java -Dexec.mainClass="com.gnuplot.cli.GnuplotCli"
```

## Using the Java API

### Core Module (Expression Evaluation)

```java
import com.gnuplot.core.parser.ExpressionParser;
import com.gnuplot.core.evaluator.Evaluator;
import com.gnuplot.core.evaluator.EvaluationContext;

public class Example {
    public static void main(String[] args) {
        // Parse an expression
        ExpressionParser parser = new ExpressionParser();
        var result = parser.parse("sin(pi/2) + cos(0)");

        // Evaluate it
        EvaluationContext context = new EvaluationContext();
        Evaluator evaluator = new Evaluator(context);
        double value = evaluator.evaluate(result.getAst());

        System.out.println(value);  // 2.0
    }
}
```

### Render Module (Creating Plots)

```java
import com.gnuplot.render.*;
import com.gnuplot.render.elements.LinePlot;
import com.gnuplot.render.svg.SvgRenderer;
import java.io.FileOutputStream;

public class PlotExample {
    public static void main(String[] args) throws Exception {
        // Create data points
        LinePlot plot = LinePlot.builder()
                .id("sine")
                .addPoint(0.0, 0.0)
                .addPoint(1.0, 0.84)
                .addPoint(2.0, 0.91)
                .addPoint(3.0, 0.14)
                .color("#0000FF")
                .build();

        // Create scene
        Scene scene = Scene.builder()
                .dimensions(800, 600)
                .title("Sine Wave")
                .viewport(Viewport.of2D(0.0, 3.0, 0.0, 1.0))
                .addElement(plot)
                .build();

        // Render to SVG
        SvgRenderer renderer = new SvgRenderer();
        try (FileOutputStream out = new FileOutputStream("sine.svg")) {
            renderer.render(scene, out);
        }
    }
}
```

### CLI Module (Programmatic Script Execution)

```java
import com.gnuplot.cli.command.GnuplotScript;
import com.gnuplot.cli.executor.GnuplotScriptExecutor;
import com.gnuplot.cli.parser.GnuplotCommandParser;

public class ScriptExample {
    public static void main(String[] args) {
        String script = """
            set title "My Plot"
            set xlabel "X"
            set ylabel "Y"
            plot sin(x)
            """;

        // Parse the script
        GnuplotCommandParser parser = new GnuplotCommandParser();
        GnuplotScript gnuplotScript = parser.parse(script);

        // Execute it
        GnuplotScriptExecutor executor = new GnuplotScriptExecutor();
        executor.execute(gnuplotScript);

        // Creates output.svg
    }
}
```

## Supported Commands

### SET Commands
- `set title "Title"` - Set plot title
- `set xlabel "X Label"` - Set X axis label
- `set ylabel "Y Label"` - Set Y axis label
- `set samples N` - Set number of sample points (default: 100)
- `set grid` - Enable grid
- `set autoscale` - Enable autoscaling

### PLOT Commands
- `plot <expr>` - Plot mathematical expression
- `plot <expr1>, <expr2>, ...` - Plot multiple expressions
- `plot <expr> title "Label"` - Plot with custom label
- `plot <expr> with <style>` - Plot with style (lines, points, etc.)

### Other Commands
- `unset <option>` - Remove setting
- `reset` - Reset all settings
- `pause <seconds> "message"` - Pause execution
- `quit` / `exit` - Exit shell (interactive mode)

## Supported Functions

Mathematical functions available in expressions:

**Trigonometric**: `sin`, `cos`, `tan`, `asin`, `acos`, `atan`, `atan2`, `sinh`, `cosh`, `tanh`

**Exponential/Logarithmic**: `exp`, `log`, `log10`, `sqrt`, `cbrt`

**Special Functions**: `gamma`, `lgamma`, `erf`, `erfc`, `besj0`, `besj1`, `besy0`, `besy1`

**Statistical**: `norm`, `invnorm`

**Random**: `rand`, `randint`

**Utility**: `abs`, `ceil`, `floor`, `int`, `sgn`, `real`, `imag`

**Constants**: `pi`, `e`

## Examples Gallery

Check out the demo files for inspiration:

```bash
# Line plots with different styles
cd gnuplot-render
mvn test -Dtest=LineStyleDemo

# Scatter plots with different markers
mvn test -Dtest=ScatterPlotDemo

# Bar charts (vertical, horizontal, grouped, stacked)
mvn test -Dtest=BarChartDemo

# Multi-plot layouts
mvn test -Dtest=MultiPlotDemo
```

Generated SVG files will be in the module directory.

## Next Steps

1. **Read the documentation**:
   - [gnuplot-cli/README.md](gnuplot-java/gnuplot-cli/README.md) - Complete CLI guide
   - [gnuplot-render/QUICK_START.md](gnuplot-java/gnuplot-render/QUICK_START.md) - Render API guide
   - [IMPLEMENTATION_BACKLOG.md](IMPLEMENTATION_BACKLOG.md) - Full project roadmap

2. **Try more complex examples**:
   - Multi-function plots: `plot sin(x), cos(x), tan(x)`
   - Complex expressions: `plot x**2 + 2*x + 1`
   - Custom ranges: `plot [0:10] sin(x)`

3. **Explore the modules**:
   - `gnuplot-core` - Expression parsing and evaluation (583 tests)
   - `gnuplot-render` - Scene graph and rendering (375 tests)
   - `gnuplot-cli` - Command-line interface (31 tests)

4. **Run the test suite**:
   ```bash
   mvn test
   ```
   All 989 tests should pass!

## Getting Help

- View CLI help: `gnuplot-cli --help`
- Interactive help: Type `help` in the REPL
- Issues: https://github.com/gfilios/gnuplot2java/issues

## What's Working

✅ **Full Gnuplot Script Compatibility**:
- Script parsing with ANTLR4
- Expression evaluation with 38+ functions
- SVG rendering
- Interactive REPL, batch mode, pipe mode

✅ **Production Ready Modules**:
- gnuplot-core: Mathematical engine (583 tests)
- gnuplot-render: Rendering engine (375 tests)
- gnuplot-cli: CLI interface (31 tests)

✅ **Output Formats**:
- SVG (fully supported)
- PNG, PDF (planned)

## Known Limitations

- Output is currently SVG only (PNG/PDF coming soon)
- 3D plotting not yet implemented
- Some advanced Gnuplot features pending (fit, splot, complex data files)
- Terminal/ASCII output not available

See [IMPLEMENTATION_BACKLOG.md](IMPLEMENTATION_BACKLOG.md) for the complete roadmap.
