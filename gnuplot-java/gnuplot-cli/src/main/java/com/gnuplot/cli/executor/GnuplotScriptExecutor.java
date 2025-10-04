package com.gnuplot.cli.executor;

import com.gnuplot.cli.command.*;
import com.gnuplot.core.evaluator.EvaluationContext;
import com.gnuplot.core.evaluator.Evaluator;
import com.gnuplot.core.parser.ExpressionParser;
import com.gnuplot.core.parser.ParseResult;
import com.gnuplot.render.Scene;
import com.gnuplot.render.Viewport;
import com.gnuplot.render.elements.Axis;
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
    private String outputFile = "output.svg";
    private final Map<String, Double> variables = new HashMap<>();

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
            case "output":
                if (value instanceof String) {
                    outputFile = (String) value;
                }
                break;
        }
    }

    @Override
    public void visitPlotCommand(PlotCommand command) {
        // Clear plots from previous plot command
        plots.clear();

        int colorIndex = 0;  // Track color cycling for multi-plot commands
        for (PlotCommand.PlotSpec spec : command.getPlotSpecs()) {
            String expression = spec.getExpression();
            String plotTitle = spec.getTitle();
            String style = spec.getStyle();

            // Generate points from data file or expression
            LinePlot.Point2D[] points;
            if (isDataFile(expression)) {
                points = readDataFile(expression);
            } else {
                points = generatePoints(expression);
            }

            if (points.length > 0) {
                LinePlot.Builder plotBuilder = LinePlot.builder()
                        .id("plot_" + plots.size())
                        .points(List.of(points))
                        .color(DEFAULT_COLORS[colorIndex % DEFAULT_COLORS.length]);

                if (plotTitle != null && !plotTitle.isEmpty()) {
                    plotBuilder.label(plotTitle);
                }

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

    /**
     * Generate points by evaluating the expression for x in a range.
     */
    private LinePlot.Point2D[] generatePoints(String expression) {
        LinePlot.Point2D[] points = new LinePlot.Point2D[samples];

        // Default range: -10 to 10
        double xMin = -10.0;
        double xMax = 10.0;
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

        // Create viewport (auto-calculate from data or use defaults)
        Viewport viewport = Viewport.of2D(-10, 10, -10, 10);

        // Build scene
        Scene.Builder sceneBuilder = Scene.builder()
                .dimensions(800, 600)
                .viewport(viewport);

        if (!title.isEmpty()) {
            sceneBuilder.title(title);
        }

        // Create and add X axis
        Axis xAxis = Axis.builder()
                .id("xaxis")
                .axisType(Axis.AxisType.X_AXIS)
                .range(-10.0, 10.0)
                .showTicks(true)
                .showGrid(grid)
                .label(xlabel.isEmpty() ? null : xlabel)
                .build();

        sceneBuilder.addElement(xAxis);

        // Create and add Y axis
        Axis yAxis = Axis.builder()
                .id("yaxis")
                .axisType(Axis.AxisType.Y_AXIS)
                .range(-10.0, 10.0)
                .showTicks(true)
                .showGrid(grid)
                .label(ylabel.isEmpty() ? null : ylabel)
                .build();

        sceneBuilder.addElement(yAxis);

        // Add all plots
        for (LinePlot plot : plots) {
            sceneBuilder.addElement(plot);
        }

        scenes.add(sceneBuilder.build());
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
