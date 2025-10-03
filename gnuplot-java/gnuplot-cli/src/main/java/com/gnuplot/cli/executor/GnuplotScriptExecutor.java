package com.gnuplot.cli.executor;

import com.gnuplot.cli.command.*;
import com.gnuplot.core.evaluator.EvaluationContext;
import com.gnuplot.core.evaluator.Evaluator;
import com.gnuplot.core.parser.ExpressionParser;
import com.gnuplot.core.parser.ParseResult;
import com.gnuplot.render.Scene;
import com.gnuplot.render.Viewport;
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
        // Don't clear - accumulate all plots in one scene
        // plots.clear();

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
                        .points(List.of(points));

                if (plotTitle != null && !plotTitle.isEmpty()) {
                    plotBuilder.label(plotTitle);
                }

                plots.add(plotBuilder.build());
            }
        }

        // Render after each plot command
        renderCurrentScene();
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
     * Render the current scene to the output file.
     */
    private void renderCurrentScene() {
        // Create viewport (auto-calculate from data or use defaults)
        Viewport viewport = Viewport.of2D(-10, 10, -10, 10);

        // Build scene
        Scene.Builder sceneBuilder = Scene.builder()
                .dimensions(800, 600)
                .viewport(viewport);

        if (!title.isEmpty()) {
            sceneBuilder.title(title);
        }

        // Add all plots
        for (LinePlot plot : plots) {
            sceneBuilder.addElement(plot);
        }

        Scene scene = sceneBuilder.build();

        // Render to file
        try (FileOutputStream out = new FileOutputStream(outputFile)) {
            renderer.render(scene, out);
            System.out.println("Rendered to: " + outputFile);
        } catch (Exception e) {
            System.err.println("Failed to render: " + e.getMessage());
        }
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
    }
}
