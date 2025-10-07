package com.gnuplot.cli.executor;

import com.gnuplot.cli.command.*;
import com.gnuplot.core.evaluator.EvaluationContext;
import com.gnuplot.core.evaluator.Evaluator;
import com.gnuplot.core.parser.ExpressionParser;
import com.gnuplot.core.parser.ParseResult;
import com.gnuplot.render.Scene;
import com.gnuplot.render.Viewport;
import com.gnuplot.render.axis.TickGenerator;
import com.gnuplot.render.elements.Axis;
import com.gnuplot.render.elements.Legend;
import com.gnuplot.render.elements.LinePlot;
import com.gnuplot.render.svg.SvgRenderer;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Executes parsed Gnuplot commands by translating them to Java API calls.
 */
public class GnuplotScriptExecutor implements CommandVisitor {

    // Gnuplot default color palette
    private static final String[] DEFAULT_COLORS = {
        "#9400D3",  // Purple
        "#009E73",  // Green
        "#56B4E9",  // Blue
        "#E69F00",  // Orange
        "#F0E442",  // Yellow
        "#0072B2",  // Dark Blue
        "#D55E00",  // Red-Orange
        "#CC79A7"   // Pink
    };

    private final ExpressionParser expressionParser = new ExpressionParser();
    private final EvaluationContext evaluationContext = new EvaluationContext();
    private final Evaluator evaluator = new Evaluator(evaluationContext);
    private final SvgRenderer renderer = new SvgRenderer();

    // Current plot state
    private String title = "";
    private String xlabel = "";
    private String ylabel = "";
    private int samples = 100;
    private boolean grid = false;
    private boolean drawBorder = true; // Default: true (matching C Gnuplot's draw_border = 31)
    private String outputFile = "output.svg";
    private final Map<String, Double> variables = new HashMap<>();

    // Legend/key state - split into vertical and horizontal components to match gnuplot's incremental behavior
    private String keyVerticalPosition = "top";  // top, bottom, center, tmargin, bmargin
    private String keyHorizontalPosition = "left";  // left, right, center
    private boolean keyShowBorder = true;
    private boolean keyHorizontal = false;

    // Style state
    private String styleDataValue = "lines"; // default: lines

    // Current scene elements
    private final List<LinePlot> plots = new ArrayList<>();

    // Accumulated scenes (for multi-page rendering)
    private final List<Scene> scenes = new ArrayList<>();

    // Generated output files (for test result tracking)
    private final List<String> generatedOutputFiles = new ArrayList<>();

    @Override
    public void visitSetCommand(SetCommand command) {
        String option = command.getOption();
        Object value = command.getValue();

        switch (option) {
            case "title":
                if (value instanceof String) {
                    title = (String) value;
                }
                break;
            case "xlabel":
                if (value instanceof String) {
                    xlabel = (String) value;
                }
                break;
            case "ylabel":
                if (value instanceof String) {
                    ylabel = (String) value;
                }
                break;
            case "samples":
                if (value instanceof Integer) {
                    samples = (Integer) value;
                }
                break;
            case "grid":
                grid = (Boolean) value;
                break;
            case "border":
                drawBorder = true; // "set border" enables border
                break;
            case "output":
                if (value instanceof String) {
                    outputFile = (String) value;
                }
                break;
            case "key":
                if (value instanceof Map) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> keySettings = (Map<String, Object>) value;

                    // Extract vertical and horizontal components separately
                    // This allows gnuplot's incremental behavior: "set key bmargin center" then "set key left"
                    // should result in bottom-left, not top-left
                    String vertical = (String) keySettings.get("vertical");
                    String horizontal = (String) keySettings.get("horizontal");

                    // Only update if present (null means not specified in this command)
                    if (vertical != null) {
                        keyVerticalPosition = vertical;
                    }
                    if (horizontal != null) {
                        keyHorizontalPosition = horizontal;
                    }

                    // Update border and layout orientation if specified
                    if (keySettings.containsKey("showBorder")) {
                        keyShowBorder = (Boolean) keySettings.get("showBorder");
                    }
                    if (keySettings.containsKey("layout")) {
                        keyHorizontal = (Boolean) keySettings.get("layout");
                    }
                }
                break;
            case "style":
                if (value instanceof Map) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> styleSettings = (Map<String, Object>) value;
                    String styleType = (String) styleSettings.get("type");
                    String styleValue = (String) styleSettings.get("value");

                    // Handle "set style data {points|lines|linespoints}"
                    if ("data".equalsIgnoreCase(styleType) && styleValue != null) {
                        styleDataValue = styleValue.toLowerCase();
                    }
                }
                break;
        }
    }

    @Override
    public void visitPlotCommand(PlotCommand command) {
        // Clear plots from previous plot command
        plots.clear();

        // Update X range from plot command
        if (command.getXRange() != null) {
            PlotCommand.Range xRange = command.getXRange();
            currentXMin = xRange.getMin() != null ? xRange.getMin() : -10.0;
            currentXMax = xRange.getMax() != null ? xRange.getMax() : 10.0;
        } else {
            // Reset to default if no range specified
            currentXMin = -10.0;
            currentXMax = 10.0;
        }

        // Update Y range from plot command
        if (command.getYRange() != null) {
            PlotCommand.Range yRange = command.getYRange();
            currentYMin = yRange.getMin();
            currentYMax = yRange.getMax();
        } else {
            // Reset to auto-scale if no range specified
            currentYMin = null;
            currentYMax = null;
        }

        int colorIndex = 0;  // Track color cycling for multi-plot commands
        for (PlotCommand.PlotSpec spec : command.getPlotSpecs()) {
            String expression = spec.getExpression();
            String plotTitle = spec.getTitle();
            String style = spec.getStyle();
            PlotCommand.Range plotSpecRange = spec.getRange();

            // Use per-plot range if specified, otherwise use command-level range
            double xMin = currentXMin;
            double xMax = currentXMax;
            if (plotSpecRange != null) {
                xMin = plotSpecRange.getMin() != null ? plotSpecRange.getMin() : currentXMin;
                xMax = plotSpecRange.getMax() != null ? plotSpecRange.getMax() : currentXMax;
            }

            // Generate points from data file or expression
            LinePlot.Point2D[] points;
            if (isDataFile(expression)) {
                points = readDataFile(expression);
            } else {
                points = generatePoints(expression, xMin, xMax);
            }

            if (points.length > 0) {
                LinePlot.Builder plotBuilder = LinePlot.builder()
                        .id("plot_" + plots.size())
                        .points(List.of(points))
                        .color(DEFAULT_COLORS[colorIndex % DEFAULT_COLORS.length]);

                // Use explicit title if provided, otherwise use expression as default label (Gnuplot behavior)
                String label = (plotTitle != null && !plotTitle.isEmpty()) ? plotTitle : expression;
                plotBuilder.label(label);

                // Determine plot style:
                // 1. If explicit "with <style>" is specified, use that
                // 2. If data file, use "set style data" setting
                // 3. If function (expression), default to "lines" (gnuplot behavior)
                LinePlot.PlotStyle plotStyle;

                if (style != null && !style.isEmpty()) {
                    // Explicit "with" clause takes precedence
                    plotStyle = switch (style.toLowerCase()) {
                        case "points" -> LinePlot.PlotStyle.POINTS;
                        case "lines" -> LinePlot.PlotStyle.LINES;
                        case "linespoints" -> LinePlot.PlotStyle.LINESPOINTS;
                        case "impulses" -> LinePlot.PlotStyle.LINES; // treat impulses as lines for now
                        default -> LinePlot.PlotStyle.LINES;
                    };
                } else if (isDataFile(expression)) {
                    // Data files use "set style data" setting
                    plotStyle = switch (styleDataValue) {
                        case "points" -> LinePlot.PlotStyle.POINTS;
                        case "linespoints" -> LinePlot.PlotStyle.LINESPOINTS;
                        default -> LinePlot.PlotStyle.LINES;
                    };
                } else {
                    // Functions always default to LINES (gnuplot behavior)
                    plotStyle = LinePlot.PlotStyle.LINES;
                }

                plotBuilder.plotStyle(plotStyle);

                plots.add(plotBuilder.build());
                colorIndex++;  // Next color for next plot
            }
        }

        // Create a scene from current plots and add to scenes list
        createAndAddScene();
    }

    @Override
    public void visitUnsetCommand(UnsetCommand command) {
        String option = command.getOption();

        switch (option) {
            case "grid":
                grid = false;
                break;
            case "border":
                drawBorder = false; // "unset border" disables border
                break;
            case "title":
                title = "";
                break;
            case "xlabel":
                xlabel = "";
                break;
            case "ylabel":
                ylabel = "";
                break;
        }
    }

    @Override
    public void visitPauseCommand(PauseCommand command) {
        // In a real implementation, this would pause for the specified time
        // For now, we'll just log it
        System.out.println("Pause: " + command.getSeconds() + "s - " + command.getMessage());
    }

    @Override
    public void visitResetCommand(ResetCommand command) {
        title = "";
        xlabel = "";
        ylabel = "";
        samples = 100;
        grid = false;
        plots.clear();
        variables.clear();
    }

    // Current plot ranges (updated from plot command)
    private double currentXMin = -10.0;
    private double currentXMax = 10.0;
    private Double currentYMin = null;  // null means auto-scale
    private Double currentYMax = null;  // null means auto-scale

    /**
     * Generate points by evaluating the expression for x in a range.
     */
    private LinePlot.Point2D[] generatePoints(String expression, double xMin, double xMax) {
        LinePlot.Point2D[] points = new LinePlot.Point2D[samples];

        // Use provided X range (can be per-plot range or command-level range)
        double step = (xMax - xMin) / (samples - 1);

        // Parse expression once
        ParseResult parseResult = expressionParser.parse(expression);
        if (!parseResult.isSuccess()) {
            // If parsing fails, return NaN points
            for (int i = 0; i < samples; i++) {
                double x = xMin + i * step;
                points[i] = new LinePlot.Point2D(x, Double.NaN);
            }
            System.err.println("Parse error: " + parseResult.getError());
            return points;
        }

        for (int i = 0; i < samples; i++) {
            double x = xMin + i * step;

            // Set x variable in evaluation context
            evaluationContext.setVariable("x", x);

            try {
                // Evaluate the expression
                double y = evaluator.evaluate(parseResult.getAst());

                points[i] = new LinePlot.Point2D(x, y);
            } catch (Exception e) {
                // If evaluation fails, use NaN
                points[i] = new LinePlot.Point2D(x, Double.NaN);
            }
        }

        return points;
    }

    /**
     * Create a scene from current plots and settings, and add to scenes list.
     */
    private void createAndAddScene() {
        if (plots.isEmpty()) {
            return;
        }

        // Determine Y range: use explicit range if specified, otherwise auto-calculate
        double yMin;
        double yMax;

        if (currentYMin != null && currentYMax != null) {
            // Use explicit Y range from plot command
            yMin = currentYMin;
            yMax = currentYMax;
        } else {
            // Auto-calculate Y range from plot data
            yMin = Double.POSITIVE_INFINITY;
            yMax = Double.NEGATIVE_INFINITY;

            for (LinePlot plot : plots) {
                for (LinePlot.Point2D point : plot.getPoints()) {
                    double y = point.getY();
                    if (Double.isFinite(y)) {
                        yMin = Math.min(yMin, y);
                        yMax = Math.max(yMax, y);
                    }
                }
            }

            // Apply gnuplot's auto-range extension algorithm
            // Ported from gnuplot-c/src/axis.c:axis_checked_extend_empty_range()
            if (Double.isFinite(yMin) && Double.isFinite(yMax)) {
                // Check if range is empty (min == max)
                if (yMax - yMin == 0.0) {
                    // Widen empty range
                    // If max is zero, widen by absolute amount (1.0)
                    // Otherwise, widen by 1% of the value
                    double widen = (yMax == 0.0) ? 1.0 : 0.01 * Math.abs(yMax);
                    yMin -= widen;
                    yMax += widen;
                }
                // For non-empty ranges, C gnuplot does NOT add padding here.
                // Instead, it extends to tick boundaries in setup_tics().

                // Extend range to next tick boundary (gnuplot's round_outward behavior)
                // Ported from gnuplot-c/src/axis.c:round_outward() and setup_tics()
                // C gnuplot uses guide=20 for setup_tics()
                TickGenerator tickGenerator = new TickGenerator();
                double yTickStep = tickGenerator.calculateTickStep(yMin, yMax, 20);
                double[] extendedYRange = tickGenerator.extendRangeToTicks(yMin, yMax, yTickStep);
                yMin = extendedYRange[0];
                yMax = extendedYRange[1];
            } else {
                // Fallback if no valid data
                yMin = -10;
                yMax = 10;
            }
        }

        // X range is always explicitly set from plot command, so don't extend it
        // Only autoscaled ranges are extended to tick boundaries in C gnuplot
        // (see axis.c:setup_tics() lines 908-913: autoextend only if autoscaled)
        double xMin = currentXMin;
        double xMax = currentXMax;

        // Create viewport: X range from plot command, Y range autoscaled and extended
        Viewport viewport = Viewport.of2D(xMin, xMax, yMin, yMax);

        // Build scene
        Scene.Builder sceneBuilder = Scene.builder()
                .dimensions(800, 600)
                .viewport(viewport)
                .border(drawBorder); // Apply border setting

        if (!title.isEmpty()) {
            sceneBuilder.title(title);
        }

        // Create and add X axis with extended range
        Axis xAxis = Axis.builder()
                .id("xaxis")
                .axisType(Axis.AxisType.X_AXIS)
                .range(xMin, xMax)
                .showTicks(true)
                .showGrid(grid)
                .label(xlabel.isEmpty() ? null : xlabel)
                .build();

        sceneBuilder.addElement(xAxis);

        // Create and add Y axis with calculated/specified range
        Axis yAxis = Axis.builder()
                .id("yaxis")
                .axisType(Axis.AxisType.Y_AXIS)
                .range(yMin, yMax)
                .showTicks(true)
                .showGrid(grid)
                .label(ylabel.isEmpty() ? null : ylabel)
                .build();

        sceneBuilder.addElement(yAxis);

        // Add all plots
        for (LinePlot plot : plots) {
            sceneBuilder.addElement(plot);
        }

        // Create and add legend if any plot has a label
        boolean hasLabels = plots.stream().anyMatch(plot -> plot.getLabel() != null && !plot.getLabel().isEmpty());
        if (hasLabels) {
            Legend.Builder legendBuilder = Legend.builder()
                    .id("legend")
                    .position(combineKeyPosition(keyVerticalPosition, keyHorizontalPosition))
                    .showBorder(keyShowBorder)
                    .columns(keyHorizontal ? plots.size() : 1);

            // Add entry for each plot with a label
            for (LinePlot plot : plots) {
                if (plot.getLabel() != null && !plot.getLabel().isEmpty()) {
                    legendBuilder.addEntry(plot.getLabel(), plot.getColor(), plot.getLineStyle());
                }
            }

            sceneBuilder.addElement(legendBuilder.build());
        }

        scenes.add(sceneBuilder.build());
    }

    /**
     * Combine vertical and horizontal key position components into a Legend.Position enum.
     * This matches gnuplot's incremental behavior where "set key bmargin center" followed by
     * "set key left" results in bottom-left position (preserving bmargin but updating horizontal).
     */
    private Legend.Position combineKeyPosition(String vertical, String horizontal) {
        // Normalize to lowercase for consistent matching
        String v = vertical.toLowerCase();
        String h = horizontal.toLowerCase();

        // Handle margin-based vertical positions (bmargin/tmargin) - these place legend OUTSIDE plot
        if ("bmargin".equals(v)) {
            return switch (h) {
                case "left" -> Legend.Position.BMARGIN_LEFT;
                case "right" -> Legend.Position.BMARGIN_RIGHT;
                default -> Legend.Position.BMARGIN_CENTER;  // center or unspecified
            };
        }
        if ("tmargin".equals(v)) {
            return switch (h) {
                case "left" -> Legend.Position.TMARGIN_LEFT;
                case "right" -> Legend.Position.TMARGIN_RIGHT;
                default -> Legend.Position.TMARGIN_CENTER;
            };
        }

        // Handle standard vertical positions
        if ("top".equals(v)) {
            return switch (h) {
                case "left" -> Legend.Position.TOP_LEFT;
                case "right" -> Legend.Position.TOP_RIGHT;
                default -> Legend.Position.TOP_CENTER;
            };
        }
        if ("bottom".equals(v)) {
            return switch (h) {
                case "left" -> Legend.Position.BOTTOM_LEFT;
                case "right" -> Legend.Position.BOTTOM_RIGHT;
                default -> Legend.Position.BOTTOM_CENTER;
            };
        }
        if ("center".equals(v)) {
            return switch (h) {
                case "left" -> Legend.Position.LEFT_CENTER;
                case "right" -> Legend.Position.RIGHT_CENTER;
                default -> Legend.Position.CENTER;
            };
        }

        // Default: top-left (gnuplot's default for "set key left")
        return Legend.Position.TOP_LEFT;
    }

    /**
     * Render all accumulated scenes to separate numbered output files.
     */
    private void renderAllScenes() {
        if (scenes.isEmpty()) {
            return;
        }

        // Generate base filename (remove .svg extension if present)
        String baseName = outputFile;
        if (baseName.endsWith(".svg")) {
            baseName = baseName.substring(0, baseName.length() - 4);
        }

        // Render each scene to a separate file
        for (int i = 0; i < scenes.size(); i++) {
            String filename;
            if (i == 0) {
                // First file uses original name
                filename = outputFile;
            } else {
                // Subsequent files use numbered suffix: output_002.svg, output_003.svg, etc.
                filename = String.format("%s_%03d.svg", baseName, i + 1);
            }

            try (FileOutputStream out = new FileOutputStream(filename)) {
                renderer.render(scenes.get(i), out);
                generatedOutputFiles.add(filename);
                System.out.println("Rendered scene " + (i + 1) + " to: " + filename);
            } catch (Exception e) {
                System.err.println("Failed to render scene " + (i + 1) + ": " + e.getMessage());
            }
        }

        System.out.println("Total: " + scenes.size() + " scenes rendered");
    }

    /**
     * Get list of generated output files (for test result tracking).
     */
    public List<String> getGeneratedOutputFiles() {
        return new ArrayList<>(generatedOutputFiles);
    }

    /**
     * Check if expression is a data file path.
     */
    private boolean isDataFile(String expression) {
        // Data files are quoted strings (single or double quotes)
        return (expression.startsWith("'") && expression.endsWith("'")) ||
               (expression.startsWith("\"") && expression.endsWith("\""));
    }

    /**
     * Read data file and return points.
     */
    private LinePlot.Point2D[] readDataFile(String filePath) {
        // Remove quotes from file path
        String cleanPath = filePath.substring(1, filePath.length() - 1);

        // Try multiple path resolution strategies
        Path dataFile = Paths.get(cleanPath);

        // If not found, try relative to current directory
        if (!Files.exists(dataFile)) {
            dataFile = Paths.get("gnuplot-c/demo").resolve(cleanPath);
        }

        // If not found, try from project root (go up from gnuplot-cli to gnuplot-java to gnuplot-master)
        if (!Files.exists(dataFile)) {
            Path currentDir = Paths.get(System.getProperty("user.dir"));
            dataFile = currentDir.getParent().getParent().resolve("gnuplot-c/demo").resolve(cleanPath);
        }

        List<LinePlot.Point2D> pointsList = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(dataFile.toFile()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) {
                    continue; // Skip empty lines and comments
                }

                // Split by whitespace
                String[] parts = line.split("\\s+");
                if (parts.length >= 2) {
                    try {
                        double x = Double.parseDouble(parts[0]);
                        double y = Double.parseDouble(parts[1]);
                        pointsList.add(new LinePlot.Point2D(x, y));
                    } catch (NumberFormatException e) {
                        // Skip malformed lines
                        System.err.println("Skipping malformed line: " + line);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Failed to read data file " + cleanPath + ": " + e.getMessage());
            return new LinePlot.Point2D[0];
        }

        return pointsList.toArray(new LinePlot.Point2D[0]);
    }

    /**
     * Execute a parsed script.
     */
    public void execute(GnuplotScript script) {
        for (Command command : script.getCommands()) {
            command.accept(this);
        }

        // After executing all commands, render all accumulated scenes
        renderAllScenes();
    }
}
