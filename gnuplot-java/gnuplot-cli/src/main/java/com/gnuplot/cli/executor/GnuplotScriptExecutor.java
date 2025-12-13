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
import com.gnuplot.render.elements.Point3D;
import com.gnuplot.render.elements.SurfacePlot3D;
import com.gnuplot.render.elements.ContourPlot3D;
import com.gnuplot.render.color.Color;
import com.gnuplot.render.style.MarkerStyle;
import com.gnuplot.render.style.PointStyle;
import com.gnuplot.render.svg.SvgRenderer;
import com.gnuplot.core.grid.ContourExtractor;
import com.gnuplot.core.grid.ContourLine;
import com.gnuplot.core.grid.ContourParams;

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
    private String zlabel = "";
    private int samples = 100;
    private boolean grid = false;
    private boolean drawBorder = true; // Default: true (matching C Gnuplot's draw_border = 31)
    private String outputFile = "output.svg";
    private String scriptName = null; // Source script name for default output naming
    private boolean outputFileExplicitlySet = false; // Track if user set output file
    private final Map<String, Double> variables = new HashMap<>();

    // Legend/key state - split into vertical and horizontal components to match gnuplot's incremental behavior
    private String keyVerticalPosition = "top";  // top, bottom, center, tmargin, bmargin
    private String keyHorizontalPosition = "right";  // left, right, center (gnuplot default is right)
    private boolean keyShowBorder = true;
    private boolean keyHorizontal = false;

    // Style state
    private String styleDataValue = "lines"; // default: lines

    // 3D grid state (for dgrid3d)
    private boolean dgrid3dEnabled = false;
    private int dgrid3dRows = 10;
    private int dgrid3dCols = 10;
    private String dgrid3dMode = "qnorm";
    private int dgrid3dNorm = 1;

    // Contour state
    private boolean contourEnabled = false;
    private ContourParams contourParams = new ContourParams();

    // Current scene elements
    private final List<LinePlot> plots = new ArrayList<>();
    private final List<SurfacePlot3D> plots3D = new ArrayList<>();
    private final List<ContourPlot3D> contourPlots3D = new ArrayList<>();

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
            case "zlabel":
                if (value instanceof String) {
                    zlabel = (String) value;
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
                    outputFileExplicitlySet = true;
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
            case "dgrid3d":
                dgrid3dEnabled = true;
                if (value instanceof Map) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> dgridSettings = (Map<String, Object>) value;
                    if (dgridSettings.containsKey("rows")) {
                        dgrid3dRows = (Integer) dgridSettings.get("rows");
                    }
                    if (dgridSettings.containsKey("cols")) {
                        dgrid3dCols = (Integer) dgridSettings.get("cols");
                    }
                    if (dgridSettings.containsKey("mode")) {
                        dgrid3dMode = (String) dgridSettings.get("mode");
                    }
                    if (dgridSettings.containsKey("norm")) {
                        dgrid3dNorm = (Integer) dgridSettings.get("norm");
                    }
                }
                System.out.println("dgrid3d enabled: " + dgrid3dRows + "x" + dgrid3dCols + " " + dgrid3dMode + " " + dgrid3dNorm);
                break;
            case "contour":
                contourEnabled = true;
                // Parse contour place (base, surface, both)
                if (value instanceof String) {
                    String place = ((String) value).toLowerCase();
                    switch (place) {
                        case "base":
                            contourParams.setPlace(ContourParams.ContourPlace.BASE);
                            break;
                        case "surface":
                            contourParams.setPlace(ContourParams.ContourPlace.SURFACE);
                            break;
                        case "both":
                            contourParams.setPlace(ContourParams.ContourPlace.BOTH);
                            break;
                    }
                }
                System.out.println("contour enabled: " + contourParams.getPlace());
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
                        case "impulses" -> LinePlot.PlotStyle.IMPULSES;
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
    public void visitSplotCommand(SplotCommand command) {
        // Clear 3D plots from previous splot command
        plots3D.clear();
        contourPlots3D.clear();

        System.out.println("Processing SPLOT command with " + command.getPlotSpecs().size() + " plot spec(s)");

        int colorIndex = 0;  // Track color cycling for multi-plot commands
        // Process each plot specification
        for (PlotCommand.PlotSpec spec : command.getPlotSpecs()) {
            String expression = spec.getExpression();
            String plotTitle = spec.getTitle();
            String style = spec.getStyle();

            System.out.println("  Plot: " + expression);
            System.out.println("    Explicit style: " + (style != null ? style : "none"));
            System.out.println("    Style data setting: " + styleDataValue);
            System.out.println("    Title: " + (plotTitle != null ? plotTitle : expression));

            // Read 3D data file
            if (isDataFile(expression)) {
                Point3D[] points = read3DDataFile(expression);
                System.out.println("    Loaded " + points.length + " 3D points from " + expression);

                // Apply dgrid3d interpolation if enabled
                if (dgrid3dEnabled && points.length > 0) {
                    System.out.println("    Applying dgrid3d interpolation: " + dgrid3dRows + "x" + dgrid3dCols + " " + dgrid3dMode + " " + dgrid3dNorm);

                    // Convert to core Point3D for interpolation
                    List<com.gnuplot.core.grid.Point3D> corePoints = new ArrayList<>();
                    for (Point3D p : points) {
                        corePoints.add(new com.gnuplot.core.grid.Point3D(p.x(), p.y(), p.z()));
                    }

                    // Create dgrid3d interpolator
                    com.gnuplot.core.grid.Dgrid3D.InterpolationMode mode =
                        com.gnuplot.core.grid.Dgrid3D.InterpolationMode.QNORM; // TODO: parse mode from dgrid3dMode
                    com.gnuplot.core.grid.Dgrid3D dgrid = new com.gnuplot.core.grid.Dgrid3D(
                        dgrid3dRows, dgrid3dCols, mode, dgrid3dNorm
                    );

                    // Interpolate to grid
                    List<com.gnuplot.core.grid.Point3D> gridded = dgrid.interpolate(corePoints);

                    // Convert back to render Point3D
                    points = new Point3D[gridded.size()];
                    for (int i = 0; i < gridded.size(); i++) {
                        com.gnuplot.core.grid.Point3D gp = gridded.get(i);
                        points[i] = new Point3D(gp.x(), gp.y(), gp.z());
                    }

                    System.out.println("    Interpolated to " + points.length + " grid points");
                }

                if (points.length > 0) {
                    // Determine plot style
                    SurfacePlot3D.PlotStyle3D plotStyle;
                    if (style != null && !style.isEmpty()) {
                        plotStyle = switch (style.toLowerCase()) {
                            case "points" -> SurfacePlot3D.PlotStyle3D.POINTS;
                            case "lines" -> SurfacePlot3D.PlotStyle3D.LINES;
                            case "surface" -> SurfacePlot3D.PlotStyle3D.SURFACE;
                            case "dots" -> SurfacePlot3D.PlotStyle3D.DOTS;
                            default -> SurfacePlot3D.PlotStyle3D.POINTS;
                        };
                    } else if (!styleDataValue.isEmpty()) {
                        // Use "set style data" value
                        plotStyle = switch (styleDataValue) {
                            case "points" -> SurfacePlot3D.PlotStyle3D.POINTS;
                            case "lines" -> SurfacePlot3D.PlotStyle3D.LINES;
                            default -> SurfacePlot3D.PlotStyle3D.POINTS;
                        };
                    } else {
                        plotStyle = SurfacePlot3D.PlotStyle3D.POINTS;
                    }

                    // Create 3D surface plot
                    SurfacePlot3D.Builder builder = SurfacePlot3D.builder()
                        .id("splot_" + expression)
                        .plotStyle(plotStyle)
                        .color(DEFAULT_COLORS[colorIndex % DEFAULT_COLORS.length]);

                    // Pass grid dimensions if dgrid3d was applied
                    if (dgrid3dEnabled) {
                        builder.gridDimensions(dgrid3dRows, dgrid3dCols);
                    }

                    // Add all points
                    for (Point3D point : points) {
                        builder.addPoint(point.x(), point.y(), point.z());
                    }

                    // Set label
                    String label = (plotTitle != null && !plotTitle.isEmpty()) ? plotTitle : expression;
                    builder.label(label);

                    SurfacePlot3D surfacePlot = builder.build();
                    System.out.println("    Created SurfacePlot3D: " + surfacePlot);

                    // Add to 3D plots list
                    plots3D.add(surfacePlot);

                    // Generate contour lines if enabled and dgrid3d was applied
                    if (contourEnabled && dgrid3dEnabled) {
                        System.out.println("    Extracting contour lines...");

                        // Convert points to core Point3D array for contour extraction
                        com.gnuplot.core.geometry.Point3D[] gridPoints =
                            new com.gnuplot.core.geometry.Point3D[points.length];
                        double zMin = Double.MAX_VALUE;
                        double zMax = -Double.MAX_VALUE;
                        for (int i = 0; i < points.length; i++) {
                            gridPoints[i] = new com.gnuplot.core.geometry.Point3D(
                                points[i].x(), points[i].y(), points[i].z());
                            if (points[i].z() < zMin) zMin = points[i].z();
                            if (points[i].z() > zMax) zMax = points[i].z();
                        }

                        // Extract contours
                        ContourExtractor extractor = new ContourExtractor();
                        List<ContourLine> contours = extractor.extract(
                            gridPoints, dgrid3dRows, dgrid3dCols, contourParams);

                        System.out.println("    Extracted " + contours.size() + " contour lines");

                        if (!contours.isEmpty()) {
                            // Assign colors to contours based on z-level (like C gnuplot)
                            List<ContourLine> coloredContours = assignContourColors(contours);

                            // Calculate zMinVisual - the bottom of the 3D plot box
                            // This matches the ticslevel adjustment in buildScene()
                            double ticslevel = 0.5;  // TODO: get from command parser
                            double zRange = zMax - zMin;
                            double zMinVisual = zMin - (zRange * ticslevel);

                            // Create contour plot element
                            ContourPlot3D contourPlot = ContourPlot3D.builder()
                                .id("contour_" + expression)
                                .contourLines(coloredContours)
                                .place(contourParams.getPlace())
                                .color("#000000")  // Fallback color
                                .baseZ(zMinVisual)  // Use visual base, not data minimum
                                .showLabels(false)  // Labels not yet implemented in grammar
                                .build();

                            contourPlots3D.add(contourPlot);
                            System.out.println("    Created ContourPlot3D with " + coloredContours.size() + " lines");
                        }
                    }

                    colorIndex++;  // Next color for next plot
                } else {
                    System.err.println("    No valid points loaded from " + expression);
                }
            } else {
                System.err.println("    3D function plotting not yet supported: " + expression);
            }
        }

        // Create a 3D scene from current 3D plots and add to scenes list
        createAndAddScene3D();
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
            case "zlabel":
                zlabel = "";
                break;
            case "dgrid3d":
                dgrid3dEnabled = false;
                System.out.println("dgrid3d disabled");
                break;
            case "contour":
                contourEnabled = false;
                System.out.println("contour disabled");
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
        zlabel = "";
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
     * Creates a 3D scene from the current 3D plots and adds it to the scenes list.
     * Similar to createAndAddScene() but handles 3D data and rendering.
     */
    private void createAndAddScene3D() {
        if (plots3D.isEmpty()) {
            return;
        }

        // Calculate 3D bounding box from all points
        double xMin = Double.POSITIVE_INFINITY;
        double xMax = Double.NEGATIVE_INFINITY;
        double yMin = Double.POSITIVE_INFINITY;
        double yMax = Double.NEGATIVE_INFINITY;
        double zMin = Double.POSITIVE_INFINITY;
        double zMax = Double.NEGATIVE_INFINITY;

        for (SurfacePlot3D plot : plots3D) {
            for (Point3D point : plot.getPoints()) {
                if (point.isFinite()) {
                    xMin = Math.min(xMin, point.x());
                    xMax = Math.max(xMax, point.x());
                    yMin = Math.min(yMin, point.y());
                    yMax = Math.max(yMax, point.y());
                    zMin = Math.min(zMin, point.z());
                    zMax = Math.max(zMax, point.z());
                }
            }
        }

        // Fallback if no valid data
        if (!Double.isFinite(xMin)) {
            xMin = -1; xMax = 1;
            yMin = -1; yMax = 1;
            zMin = 0; zMax = 1;
        }

        // Round axis ranges to "nice" values like C gnuplot does
        // Uses C gnuplot's quantize_normal_tics and round_outward algorithm
        // Important: The tick step is calculated from the ORIGINAL data range, before rounding
        double[] xRange = computeAxisRange(xMin, xMax);
        double xTicStep = xRange[2];  // Save tick step before updating range
        xMin = xRange[0];
        xMax = xRange[1];

        double[] yRange = computeAxisRange(yMin, yMax);
        double yTicStep = yRange[2];  // Save tick step before updating range
        yMin = yRange[0];
        yMax = yRange[1];

        double[] zAxisRange = computeAxisRange(zMin, zMax);
        double zTicStep = zAxisRange[2];  // Save tick step before updating range
        zMin = zAxisRange[0];
        zMax = zAxisRange[1];
        double zDataMinRounded = zMin;  // Save the rounded data minimum for tick generation

        // Apply ticslevel to Z-axis range
        // ticslevel determines where the XY base plane sits relative to Z=0 data plane
        // With ticslevel=0.5, the visual Z range extends from -(zMax-zMin)*0.5 to zMax
        // This creates empty space below Z=0 for the base axes
        double ticslevel = 0.5;  // TODO: get from command parser
        double zRange = zMax - zMin;
        double zMinVisual = zMin - (zRange * ticslevel);

        // Use rounded data ranges for viewport, including tick steps calculated from original data
        // The tick steps are used by the renderer to generate ticks at correct intervals
        // Also pass the data Z minimum (before ticslevel adjustment) for correct tick placement
        Viewport viewport = Viewport.builder()
                .xRange(xMin, xMax)
                .yRange(yMin, yMax)
                .zRange(zMinVisual, zMax)
                .ticSteps(xTicStep, yTicStep, zTicStep)
                .zDataMin(zDataMinRounded)
                .build();

        // Build scene
        // Note: 3D plots don't use 2D borders - axes are part of the 3D scene
        Scene.Builder sceneBuilder = Scene.builder()
                .dimensions(800, 600)
                .viewport(viewport)
                .border(false);

        if (!title.isEmpty()) {
            sceneBuilder.title(title);
        }

        // Add axis labels (matching C gnuplot xlabel/ylabel/zlabel)
        if (!xlabel.isEmpty()) {
            sceneBuilder.xlabel(xlabel);
        }
        if (!ylabel.isEmpty()) {
            sceneBuilder.ylabel(ylabel);
        }
        if (!zlabel.isEmpty()) {
            sceneBuilder.zlabel(zlabel);
        }

        // Add all 3D plots
        for (SurfacePlot3D plot : plots3D) {
            sceneBuilder.addElement(plot);
        }

        // Add all contour plots
        for (ContourPlot3D contour : contourPlots3D) {
            sceneBuilder.addElement(contour);
        }

        // Create and add legend if any plot has a label
        boolean hasLabels = plots3D.stream().anyMatch(plot -> plot.getLabel() != null && !plot.getLabel().isEmpty());
        if (hasLabels) {
            Legend.Builder legendBuilder = Legend.builder()
                    .id("legend")
                    .position(combineKeyPosition(keyVerticalPosition, keyHorizontalPosition))
                    .showBorder(keyShowBorder)
                    .columns(keyHorizontal ? plots3D.size() : 1);

            // Add entry for each plot with a label
            for (SurfacePlot3D plot : plots3D) {
                if (plot.getLabel() != null && !plot.getLabel().isEmpty()) {
                    String color = plot.getColor() != null ? plot.getColor() : "#9400D3";
                    // Use marker for POINTS style, line for other styles (matching C gnuplot)
                    if (plot.getPlotStyle() == SurfacePlot3D.PlotStyle3D.POINTS) {
                        // Use PLUS marker (gpPt0) as default, matching C gnuplot
                        MarkerStyle markerStyle = MarkerStyle.unfilled(18.0,
                                Color.fromHexString(color), PointStyle.PLUS);
                        legendBuilder.addEntry(Legend.LegendEntry.withMarker(
                                plot.getLabel(), color, markerStyle));
                    } else {
                        legendBuilder.addEntry(plot.getLabel(), color, LinePlot.LineStyle.SOLID);
                    }
                }
            }

            sceneBuilder.addElement(legendBuilder.build());
        }

        scenes.add(sceneBuilder.build());
    }

    /**
     * Compute tic step size using C gnuplot's quantize_normal_tics algorithm.
     * @param range the axis range (max - min)
     * @param guide approximate number of tics wanted (default 20)
     * @return the tic step size
     */
    private double quantizeNormalTics(double range, int guide) {
        if (range == 0) return 1;

        // Order of magnitude of the range
        double power = Math.pow(10.0, Math.floor(Math.log10(range)));
        double xnorm = range / power;  // normalized range, expected 1-10
        double posns = guide / xnorm;  // approx number of tic positions per decade

        // Choose tic step based on number of positions
        double tics;
        if (posns > 40)
            tics = 0.05;
        else if (posns > 20)
            tics = 0.1;
        else if (posns > 10)
            tics = 0.2;
        else if (posns > 4)
            tics = 0.5;
        else if (posns > 2)
            tics = 1;
        else if (posns > 0.5)
            tics = 2;
        else
            tics = Math.ceil(xnorm);

        return tics * power;
    }

    /**
     * Round axis value outward to multiple of tic step.
     * Mimics C gnuplot's round_outward function.
     *
     * @param value the value to round
     * @param ticStep the tic step size
     * @param upwards true to round up (for max), false to round down (for min)
     * @return the rounded value
     */
    private double roundOutward(double value, double ticStep, boolean upwards) {
        if (ticStep == 0) return value;
        return ticStep * (upwards
            ? Math.ceil(value / ticStep)
            : Math.floor(value / ticStep));
    }

    /**
     * Compute axis range and tic step, then round axis endpoints outward.
     * This combines quantize_normal_tics and round_outward from C gnuplot.
     *
     * @param min axis minimum
     * @param max axis maximum
     * @return array of [roundedMin, roundedMax, ticStep]
     */
    private double[] computeAxisRange(double min, double max) {
        double range = Math.abs(max - min);
        double ticStep = quantizeNormalTics(range, 20);  // guide=20 like C gnuplot

        double roundedMin = roundOutward(min, ticStep, false);
        double roundedMax = roundOutward(max, ticStep, true);

        return new double[]{roundedMin, roundedMax, ticStep};
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
     * Set the source script name for default output file naming.
     * Should be called before execute() when running a script file.
     */
    public void setScriptName(String scriptName) {
        this.scriptName = scriptName;
        // If output file was not explicitly set via "set output", derive from script name
        if (!outputFileExplicitlySet && scriptName != null) {
            // Extract base name without path and extension
            String baseName = scriptName;
            int lastSlash = baseName.lastIndexOf('/');
            if (lastSlash >= 0) {
                baseName = baseName.substring(lastSlash + 1);
            }
            int lastDot = baseName.lastIndexOf('.');
            if (lastDot > 0) {
                baseName = baseName.substring(0, lastDot);
            }
            this.outputFile = baseName + ".svg";
        }
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
     * Reads a 3D data file in x y z format (one point per line).
     */
    private Point3D[] read3DDataFile(String filePath) {
        // Remove quotes from file path
        String cleanPath = filePath.substring(1, filePath.length() - 1);

        // Try multiple path resolution strategies
        Path dataFile = Paths.get(cleanPath);

        // If not found, try relative to gnuplot-c/demo directory (matching 2D behavior)
        if (!Files.exists(dataFile)) {
            dataFile = Paths.get("gnuplot-c/demo").resolve(cleanPath);
        }

        // If not found, try from project root (go up from gnuplot-cli to gnuplot-java to gnuplot-master)
        if (!Files.exists(dataFile)) {
            Path currentDir = Paths.get(System.getProperty("user.dir"));
            dataFile = currentDir.getParent().getParent().resolve("gnuplot-c/demo").resolve(cleanPath);
        }

        // If not found, try demos directory (for our new 3D demos)
        if (!Files.exists(dataFile)) {
            dataFile = Paths.get("demos").resolve(cleanPath);
        }

        // If not found, try from project root demos directory
        if (!Files.exists(dataFile)) {
            Path currentDir = Paths.get(System.getProperty("user.dir"));
            dataFile = currentDir.getParent().getParent().resolve("demos").resolve(cleanPath);
        }

        List<Point3D> pointsList = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(dataFile.toFile()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) {
                    continue; // Skip empty lines and comments
                }

                // Split by whitespace
                String[] parts = line.split("\\s+");
                if (parts.length >= 3) {
                    try {
                        double x = Double.parseDouble(parts[0]);
                        double y = Double.parseDouble(parts[1]);
                        double z = Double.parseDouble(parts[2]);
                        pointsList.add(new Point3D(x, y, z));
                    } catch (NumberFormatException e) {
                        // Skip malformed lines
                        System.err.println("Skipping malformed 3D line: " + line);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Failed to read 3D data file " + cleanPath + ": " + e.getMessage());
            return new Point3D[0];
        }

        return pointsList.toArray(new Point3D[0]);
    }

    /**
     * Assign colors to contour lines based on their z-level.
     * Each unique z-level gets a color from the DEFAULT_COLORS palette,
     * cycling through the palette if there are more levels than colors.
     * This matches C gnuplot's behavior of coloring contours by level.
     *
     * @param contours the list of contour lines to colorize
     * @return a new list of contour lines with colors assigned
     */
    private List<ContourLine> assignContourColors(List<ContourLine> contours) {
        if (contours.isEmpty()) {
            return contours;
        }

        // Get unique z-levels in sorted order
        java.util.Set<Double> uniqueLevels = new java.util.TreeSet<>();
        for (ContourLine contour : contours) {
            uniqueLevels.add(contour.zLevel());
        }

        // Map each z-level to a color index
        java.util.Map<Double, String> levelColorMap = new java.util.HashMap<>();
        int colorIdx = 0;
        for (Double level : uniqueLevels) {
            levelColorMap.put(level, DEFAULT_COLORS[colorIdx % DEFAULT_COLORS.length]);
            colorIdx++;
        }

        // Create new contour lines with assigned colors
        List<ContourLine> coloredContours = new java.util.ArrayList<>();
        for (ContourLine contour : contours) {
            String color = levelColorMap.get(contour.zLevel());
            coloredContours.add(contour.withColor(color));
        }

        return coloredContours;
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
