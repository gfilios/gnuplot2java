# Gnuplot Render Quick Start

This guide shows you how to create simple plots using the gnuplot-render module.

## Basic Example

Here's the simplest way to create a plot:

```java
import com.gnuplot.render.*;
import com.gnuplot.render.elements.LinePlot;
import com.gnuplot.render.svg.SvgRenderer;
import java.io.FileOutputStream;

public class QuickPlot {
    public static void main(String[] args) throws Exception {
        // 1. Create a line plot
        LinePlot plot = LinePlot.builder()
                .id("myplot")
                .addPoint(0.0, 0.0)
                .addPoint(1.0, 1.0)
                .addPoint(2.0, 4.0)
                .addPoint(3.0, 9.0)
                .color("#0000FF")
                .build();

        // 2. Create a scene
        Scene scene = Scene.builder()
                .dimensions(800, 600)
                .title("My Plot")
                .viewport(Viewport.of2D(0.0, 3.0, 0.0, 9.0))
                .addElement(plot)
                .build();

        // 3. Render to SVG
        SvgRenderer renderer = new SvgRenderer();
        try (FileOutputStream out = new FileOutputStream("plot.svg")) {
            renderer.render(scene, out);
        }
    }
}
```

## Running the Examples

The module includes complete examples in `SimplePlotExample.java`:

```bash
# From the gnuplot-java directory (parent directory)
cd gnuplot-java
mvn test-compile exec:java \
  -Dexec.mainClass="com.gnuplot.render.examples.SimplePlotExample" \
  -Dexec.classpathScope=test \
  -pl gnuplot-render
```

This will create three SVG files:
- `plot.svg` - Minimal plot
- `sine_wave.svg` - Complete plot with axes and legend
- `multi_line.svg` - Multiple lines on one plot

## Key Concepts

### 1. Scene Elements

Build your plot from these elements:

- **LinePlot**: Line graphs with points and styles
- **Axis**: X and Y axes with labels
- **Legend**: Plot legend with entries

### 2. Scene

The scene is a container for all elements:

```java
Scene scene = Scene.builder()
    .dimensions(800, 600)           // Size in pixels
    .title("My Plot")               // Optional title
    .viewport(Viewport.of2D(...))   // Data coordinate range
    .addElement(plot)               // Add your elements
    .addElement(xAxis)
    .addElement(legend)
    .build();
```

### 3. Viewport

Maps data coordinates to screen coordinates:

```java
// For data ranging from x=[0,10], y=[-5,5]
Viewport viewport = Viewport.of2D(0.0, 10.0, -5.0, 5.0);
```

### 4. Rendering

Currently supports SVG output:

```java
SvgRenderer renderer = new SvgRenderer();
renderer.render(scene, outputStream);
```

## Line Styles

Available line styles:
- `LineStyle.SOLID` - Solid line (default)
- `LineStyle.DASHED` - Dashed line
- `LineStyle.DOTTED` - Dotted line
- `LineStyle.DASH_DOT` - Dash-dot pattern
- `LineStyle.NONE` - No line (points only)

## Legend Positions

Position your legend:
- `Position.TOP_LEFT`
- `Position.TOP_RIGHT` (default)
- `Position.BOTTOM_LEFT`
- `Position.BOTTOM_RIGHT`
- `Position.TOP_CENTER`
- `Position.BOTTOM_CENTER`
- `Position.LEFT_CENTER`
- `Position.RIGHT_CENTER`
- `Position.CENTER`

## Axis Types

Available axis types:
- `AxisType.X_AXIS` - Primary X axis
- `AxisType.Y_AXIS` - Primary Y axis
- `AxisType.X2_AXIS` - Secondary X axis (top)
- `AxisType.Y2_AXIS` - Secondary Y axis (right)
- `AxisType.Z_AXIS` - Z axis (for 3D plots)

## Scale Types

Axis scale types:
- `ScaleType.LINEAR` - Linear scale (default)
- `ScaleType.LOGARITHMIC` - Logarithmic scale
- `ScaleType.TIME` - Time-based scale

## Complete Example

```java
// Create multiple data series
LinePlot line1 = LinePlot.builder()
    .id("series1")
    .addPoint(0, 0)
    .addPoint(1, 1)
    .addPoint(2, 4)
    .color("#FF0000")
    .lineStyle(LinePlot.LineStyle.SOLID)
    .build();

LinePlot line2 = LinePlot.builder()
    .id("series2")
    .addPoint(0, 0)
    .addPoint(1, 2)
    .addPoint(2, 3)
    .color("#0000FF")
    .lineStyle(LinePlot.LineStyle.DASHED)
    .build();

// Create axes
Axis xAxis = Axis.builder()
    .id("x")
    .axisType(Axis.AxisType.X_AXIS)
    .range(0, 2)
    .label("X Axis")
    .showGrid(true)
    .build();

Axis yAxis = Axis.builder()
    .id("y")
    .axisType(Axis.AxisType.Y_AXIS)
    .range(0, 5)
    .label("Y Axis")
    .showGrid(true)
    .build();

// Create legend
Legend legend = Legend.builder()
    .id("legend")
    .position(Legend.Position.TOP_RIGHT)
    .addEntry("Linear", "#FF0000", LinePlot.LineStyle.SOLID)
    .addEntry("Exponential", "#0000FF", LinePlot.LineStyle.DASHED)
    .showBorder(true)
    .build();

// Assemble scene
Scene scene = Scene.builder()
    .dimensions(800, 600)
    .title("My Data")
    .viewport(Viewport.of2D(0, 2, 0, 5))
    .addElement(xAxis)
    .addElement(yAxis)
    .addElement(line1)
    .addElement(line2)
    .addElement(legend)
    .build();

// Render
SvgRenderer renderer = new SvgRenderer();
try (FileOutputStream out = new FileOutputStream("output.svg")) {
    renderer.render(scene, out);
}
```

## Rendering Hints

Customize rendering with hints:

```java
RenderingHints hints = RenderingHints.builder()
    .backgroundColor("#F5F5F5")
    .antialiasing(true)
    .dpi(300)
    .build();

Scene scene = Scene.builder()
    .dimensions(800, 600)
    .hints(hints)
    // ... rest of scene
    .build();
```

## Next Steps

- Check `SimplePlotExample.java` for more examples
- See the test classes for advanced usage
- Explore rendering capabilities with `RendererCapabilities`

## Getting Help

- View tests: `src/test/java/com/gnuplot/render/`
- Read JavaDocs: `mvn javadoc:javadoc`
- Run examples: See "Running the Examples" above
