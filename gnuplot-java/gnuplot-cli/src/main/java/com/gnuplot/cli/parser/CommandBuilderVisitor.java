package com.gnuplot.cli.parser;

import com.gnuplot.cli.GnuplotCommandBaseVisitor;
import com.gnuplot.cli.GnuplotCommandParser;
import com.gnuplot.cli.command.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Visitor that builds Command objects from the ANTLR parse tree.
 */
public class CommandBuilderVisitor extends GnuplotCommandBaseVisitor<List<Command>> {

    private List<Command> commands = new ArrayList<>();

    @Override
    public List<Command> visitScript(GnuplotCommandParser.ScriptContext ctx) {
        commands.clear();

        for (GnuplotCommandParser.StatementContext stmt : ctx.statement()) {
            if (stmt.command() != null) {
                visit(stmt.command());
            }
        }

        return new ArrayList<>(commands);
    }

    @Override
    public List<Command> visitSetCommand(GnuplotCommandParser.SetCommandContext ctx) {
        if (ctx.setOption() != null) {
            // Parse set options
            GnuplotCommandParser.SetOptionContext optCtx = ctx.setOption();

            if (optCtx instanceof GnuplotCommandParser.SetTitleContext) {
                GnuplotCommandParser.SetTitleContext titleCtx = (GnuplotCommandParser.SetTitleContext) optCtx;
                String title = extractString(titleCtx.string(0));
                commands.add(new SetCommand("title", title));
            } else if (optCtx instanceof GnuplotCommandParser.SetXLabelContext) {
                GnuplotCommandParser.SetXLabelContext xlabelCtx = (GnuplotCommandParser.SetXLabelContext) optCtx;
                String xlabel = extractString(xlabelCtx.string(0));
                commands.add(new SetCommand("xlabel", xlabel));
            } else if (optCtx instanceof GnuplotCommandParser.SetYLabelContext) {
                GnuplotCommandParser.SetYLabelContext ylabelCtx = (GnuplotCommandParser.SetYLabelContext) optCtx;
                String ylabel = extractString(ylabelCtx.string(0));
                commands.add(new SetCommand("ylabel", ylabel));
            } else if (optCtx instanceof GnuplotCommandParser.SetZLabelContext) {
                GnuplotCommandParser.SetZLabelContext zlabelCtx = (GnuplotCommandParser.SetZLabelContext) optCtx;
                String zlabel = extractString(zlabelCtx.string(0));
                commands.add(new SetCommand("zlabel", zlabel));
            } else if (optCtx instanceof GnuplotCommandParser.SetOutputContext) {
                GnuplotCommandParser.SetOutputContext outputCtx = (GnuplotCommandParser.SetOutputContext) optCtx;
                String output = extractString(outputCtx.string());
                commands.add(new SetCommand("output", output));
            } else if (optCtx instanceof GnuplotCommandParser.SetSamplesContext) {
                GnuplotCommandParser.SetSamplesContext samplesCtx = (GnuplotCommandParser.SetSamplesContext) optCtx;
                int samples = Integer.parseInt(samplesCtx.NUMBER().getText());
                commands.add(new SetCommand("samples", samples));
            } else if (optCtx instanceof GnuplotCommandParser.SetKeyContext) {
                GnuplotCommandParser.SetKeyContext keyCtx = (GnuplotCommandParser.SetKeyContext) optCtx;

                // Extract position components and options from keySpec list
                Map<String, String> positionComponents = new HashMap<>();
                Boolean showBorder = null; // null means not specified
                Boolean horizontal = null; // null means not specified

                for (GnuplotCommandParser.KeySpecContext spec : keyCtx.keySpec()) {
                    if (spec.keyPosition() != null) {
                        // Extract position components
                        positionComponents = parseKeyPositionComponents(spec.keyPosition());
                    } else if (spec.keyOptions() != null) {
                        // Extract options (BOX/NOBOX, HORIZONTAL/VERTICAL)
                        GnuplotCommandParser.KeyOptionsContext opt = spec.keyOptions();
                        if (opt.BOX() != null) showBorder = true;
                        if (opt.NOBOX() != null) showBorder = false;
                        if (opt.HORIZONTAL() != null) horizontal = true;
                        if (opt.VERTICAL() != null) horizontal = false;
                    }
                }

                // Create structured command with separate vertical/horizontal components
                Map<String, Object> keySettings = new HashMap<>();
                // Only include components that were actually specified
                if (positionComponents.containsKey("vertical")) {
                    keySettings.put("vertical", positionComponents.get("vertical"));
                }
                if (positionComponents.containsKey("horizontal")) {
                    keySettings.put("horizontal", positionComponents.get("horizontal"));
                }
                if (showBorder != null) {
                    keySettings.put("showBorder", showBorder);
                }
                if (horizontal != null) {
                    keySettings.put("layout", horizontal);  // Renamed to avoid conflict with position component
                }

                commands.add(new SetCommand("key", keySettings));
            } else if (optCtx instanceof GnuplotCommandParser.SetGridContext) {
                commands.add(new SetCommand("grid", true));
            } else if (optCtx instanceof GnuplotCommandParser.SetBorderContext) {
                commands.add(new SetCommand("border", true));
            } else if (optCtx instanceof GnuplotCommandParser.SetAutoscaleContext) {
                commands.add(new SetCommand("autoscale", true));
            } else if (optCtx instanceof GnuplotCommandParser.SetDgrid3DContext) {
                GnuplotCommandParser.SetDgrid3DContext dgridCtx = (GnuplotCommandParser.SetDgrid3DContext) optCtx;
                Map<String, Object> dgridSettings = new HashMap<>();

                // Parse dgrid3d options: [rows,cols] [qnorm N]
                // Grammar: (NUMBER COMMA NUMBER)? (IDENTIFIER NUMBER?)?
                // Examples:
                //   "10,10 qnorm 4" -> rows=10, cols=10, mode=qnorm, norm=4
                //   "qnorm 4"       -> mode=qnorm, norm=4 (without rows/cols)
                //   "10,10"         -> rows=10, cols=10 (default mode)
                if (dgridCtx.dgridOptions() != null) {
                    GnuplotCommandParser.DgridOptionsContext opts = dgridCtx.dgridOptions();
                    List<org.antlr.v4.runtime.tree.TerminalNode> numbers = opts.NUMBER();

                    // Check if we have the COMMA token to determine if first two numbers are rows,cols
                    boolean hasRowsCols = opts.COMMA() != null;

                    if (hasRowsCols && numbers.size() >= 2) {
                        // Format: rows,cols [mode [norm]]
                        dgridSettings.put("rows", Integer.parseInt(numbers.get(0).getText()));
                        dgridSettings.put("cols", Integer.parseInt(numbers.get(1).getText()));
                        if (opts.IDENTIFIER() != null) {
                            dgridSettings.put("mode", opts.IDENTIFIER().getText());
                            if (numbers.size() >= 3) {
                                dgridSettings.put("norm", Integer.parseInt(numbers.get(2).getText()));
                            }
                        }
                    } else if (opts.IDENTIFIER() != null) {
                        // Format: mode [norm] (without rows,cols)
                        dgridSettings.put("mode", opts.IDENTIFIER().getText());
                        if (numbers.size() >= 1) {
                            dgridSettings.put("norm", Integer.parseInt(numbers.get(0).getText()));
                        }
                    }
                }
                commands.add(new SetCommand("dgrid3d", dgridSettings));
            } else if (optCtx instanceof GnuplotCommandParser.SetStyleContext) {
                GnuplotCommandParser.SetStyleContext styleCtx = (GnuplotCommandParser.SetStyleContext) optCtx;

                // Parse style type (data, line, arrow, fill)
                String styleType = styleCtx.styleType().getText();

                // Parse style options (points, lines, linespoints, etc.)
                String styleValue = null;
                if (!styleCtx.styleOptions().isEmpty()) {
                    styleValue = styleCtx.styleOptions(0).getText().toLowerCase();
                }

                // Create structured command for style settings
                Map<String, Object> styleSettings = new HashMap<>();
                styleSettings.put("type", styleType);
                styleSettings.put("value", styleValue);

                commands.add(new SetCommand("style", styleSettings));
            } else if (optCtx instanceof GnuplotCommandParser.SetContourContext) {
                // Enable contour with default place (base)
                // TODO: Parse "set contour base|surface|both" when grammar is extended
                commands.add(new SetCommand("contour", "base"));
            }
        }

        return commands;
    }

    @Override
    public List<Command> visitPlotCommand(GnuplotCommandParser.PlotCommandContext ctx) {
        List<PlotCommand.PlotSpec> plotSpecs = new ArrayList<>();

        // Extract X and Y ranges from plot command
        PlotCommand.Range xRange = null;
        PlotCommand.Range yRange = null;

        List<GnuplotCommandParser.RangeContext> ranges = ctx.range();
        if (!ranges.isEmpty()) {
            xRange = parseRange(ranges.get(0));
            if (ranges.size() > 1) {
                yRange = parseRange(ranges.get(1));
            }
        }

        for (GnuplotCommandParser.PlotSpecContext specCtx : ctx.plotSpec()) {
            String expression;
            String title = null;
            String style = null;  // No default - let executor decide based on data type
            PlotCommand.Range plotSpecRange = null;

            // Extract per-plot range if specified (e.g., [0:*] in "plot [0:*] sin(x)")
            if (specCtx.range() != null) {
                plotSpecRange = parseRange(specCtx.range());
            }

            // Extract expression or data source
            if (specCtx.expression() != null) {
                expression = specCtx.expression().getText();
            } else if (specCtx.dataSource() != null) {
                expression = specCtx.dataSource().getText();
            } else {
                expression = "";
            }

            // Extract modifiers
            for (GnuplotCommandParser.PlotModifiersContext modCtx : specCtx.plotModifiers()) {
                if (modCtx.WITH() != null && modCtx.plotStyle() != null) {
                    style = modCtx.plotStyle().getText();
                }
                if (modCtx.TITLE() != null && modCtx.string() != null) {
                    title = extractString(modCtx.string());
                }
                if (modCtx.NOTITLE() != null) {
                    title = "";
                }
            }

            plotSpecs.add(new PlotCommand.PlotSpec(expression, title, style, plotSpecRange));
        }

        commands.add(new PlotCommand(plotSpecs, xRange, yRange));
        return commands;
    }

    @Override
    public List<Command> visitSplotCommand(GnuplotCommandParser.SplotCommandContext ctx) {
        List<PlotCommand.PlotSpec> plotSpecs = new ArrayList<>();

        // Extract X, Y, and Z ranges from splot command
        PlotCommand.Range xRange = null;
        PlotCommand.Range yRange = null;
        PlotCommand.Range zRange = null;

        List<GnuplotCommandParser.RangeContext> ranges = ctx.range();
        if (!ranges.isEmpty()) {
            xRange = parseRange(ranges.get(0));
            if (ranges.size() > 1) {
                yRange = parseRange(ranges.get(1));
                if (ranges.size() > 2) {
                    zRange = parseRange(ranges.get(2));
                }
            }
        }

        for (GnuplotCommandParser.PlotSpecContext specCtx : ctx.plotSpec()) {
            String expression;
            String title = null;
            String style = null;  // No default - let executor decide based on data type
            PlotCommand.Range plotSpecRange = null;

            // Extract per-plot range if specified
            if (specCtx.range() != null) {
                plotSpecRange = parseRange(specCtx.range());
            }

            // Extract expression or data source
            if (specCtx.expression() != null) {
                expression = specCtx.expression().getText();
            } else if (specCtx.dataSource() != null) {
                expression = specCtx.dataSource().getText();
            } else {
                expression = "";
            }

            // Extract modifiers
            for (GnuplotCommandParser.PlotModifiersContext modCtx : specCtx.plotModifiers()) {
                if (modCtx.WITH() != null && modCtx.plotStyle() != null) {
                    style = modCtx.plotStyle().getText();
                }
                if (modCtx.TITLE() != null && modCtx.string() != null) {
                    title = extractString(modCtx.string());
                }
                if (modCtx.NOTITLE() != null) {
                    title = "";
                }
            }

            plotSpecs.add(new PlotCommand.PlotSpec(expression, title, style, plotSpecRange));
        }

        commands.add(new SplotCommand(plotSpecs, xRange, yRange, zRange));
        return commands;
    }

    @Override
    public List<Command> visitUnsetCommand(GnuplotCommandParser.UnsetCommandContext ctx) {
        if (ctx.unsetOption() != null) {
            commands.add(new UnsetCommand(ctx.unsetOption().getText()));
        }
        return commands;
    }

    @Override
    public List<Command> visitPauseCommand(GnuplotCommandParser.PauseCommandContext ctx) {
        double seconds = -1;
        String message = null;

        if (ctx.NUMBER() != null) {
            seconds = Double.parseDouble(ctx.NUMBER().getText());
        }
        if (ctx.string() != null) {
            message = extractString(ctx.string());
        }

        commands.add(new PauseCommand(seconds, message));
        return commands;
    }

    @Override
    public List<Command> visitResetCommand(GnuplotCommandParser.ResetCommandContext ctx) {
        commands.add(new ResetCommand());
        return commands;
    }

    /**
     * Extract string value from string context (handles quotes).
     */
    private String extractString(GnuplotCommandParser.StringContext ctx) {
        if (ctx.QUOTED_STRING() != null) {
            String quoted = ctx.QUOTED_STRING().getText();
            // Remove surrounding quotes
            return quoted.substring(1, quoted.length() - 1);
        } else if (ctx.IDENTIFIER() != null) {
            return ctx.IDENTIFIER().getText();
        }
        return "";
    }

    /**
     * Parse key position into separate vertical and horizontal components.
     * Returns a map with "vertical" and/or "horizontal" keys, depending on what was specified.
     * This allows incremental position updates: "set key bmargin center" followed by "set key left"
     * should preserve vertical=bmargin but update horizontal=left.
     */
    private Map<String, String> parseKeyPositionComponents(GnuplotCommandParser.KeyPositionContext ctx) {
        Map<String, String> components = new HashMap<>();

        // Check margin-based positions first (they set vertical position)
        if (ctx.BMARGIN() != null) {
            components.put("vertical", "bmargin");
            // Margin positions can include horizontal modifiers
            if (ctx.LEFT() != null) components.put("horizontal", "left");
            if (ctx.RIGHT() != null) components.put("horizontal", "right");
            if (ctx.CENTER() != null) components.put("horizontal", "center");
        } else if (ctx.TMARGIN() != null) {
            components.put("vertical", "tmargin");
            if (ctx.LEFT() != null) components.put("horizontal", "left");
            if (ctx.RIGHT() != null) components.put("horizontal", "right");
            if (ctx.CENTER() != null) components.put("horizontal", "center");
        } else if (ctx.LMARGIN() != null) {
            components.put("horizontal", "left");
            // lmargin doesn't specify vertical, defaults to center
        } else if (ctx.RMARGIN() != null) {
            components.put("horizontal", "right");
            // rmargin doesn't specify vertical, defaults to center
        } else {
            // Simple positions
            if (ctx.LEFT() != null) components.put("horizontal", "left");
            if (ctx.RIGHT() != null) components.put("horizontal", "right");
            if (ctx.TOP() != null) components.put("vertical", "top");
            if (ctx.BOTTOM() != null) components.put("vertical", "bottom");
            if (ctx.CENTER() != null) {
                // CENTER can be both vertical and horizontal
                // In gnuplot, just "center" typically means centered both ways
                components.put("vertical", "center");
                components.put("horizontal", "center");
            }
        }

        return components;
    }

    /**
     * Parse a range specification [min:max].
     * Returns null for auto (*) values.
     */
    private PlotCommand.Range parseRange(GnuplotCommandParser.RangeContext ctx) {
        if (ctx == null || ctx.rangeSpec() == null) {
            return null;
        }

        GnuplotCommandParser.RangeSpecContext spec = ctx.rangeSpec();
        Double min = null;
        Double max = null;

        // Parse min expression
        if (spec.expression() != null && !spec.expression().isEmpty()) {
            try {
                min = evaluateExpression(spec.expression(0));
            } catch (Exception e) {
                // If evaluation fails, leave as null (auto)
            }
        }

        // Parse max expression
        if (spec.expression() != null && spec.expression().size() > 1) {
            try {
                max = evaluateExpression(spec.expression(1));
            } catch (Exception e) {
                // If evaluation fails, leave as null (auto)
            }
        }

        // Handle explicit STAR tokens
        if (spec.STAR() != null) {
            // [*:expr] or [expr:*] or [*:*]
            // Already handled by leaving min/max as null
        }

        return new PlotCommand.Range(min, max);
    }

    /**
     * Evaluate a simple expression to a double value.
     * Supports numbers, pi, and basic arithmetic.
     */
    private Double evaluateExpression(GnuplotCommandParser.ExpressionContext ctx) {
        if (ctx == null) {
            return null;
        }

        String text = ctx.getText();

        // Handle special constants
        if (text.equals("pi")) {
            return Math.PI;
        }

        // Try to parse as number
        try {
            return Double.parseDouble(text);
        } catch (NumberFormatException e) {
            // Not a simple number
        }

        // Handle simple expressions like -10, -pi, 5*pi, -5*pi
        if (text.contains("*")) {
            String[] parts = text.split("\\*");
            if (parts.length == 2) {
                try {
                    double left = evaluateSimpleValue(parts[0]);
                    double right = evaluateSimpleValue(parts[1]);
                    return left * right;
                } catch (Exception e) {
                    // Fall through
                }
            }
        }

        if (text.contains("/")) {
            String[] parts = text.split("/");
            if (parts.length == 2) {
                try {
                    double left = evaluateSimpleValue(parts[0]);
                    double right = evaluateSimpleValue(parts[1]);
                    return left / right;
                } catch (Exception e) {
                    // Fall through
                }
            }
        }

        // Try as simple value
        return evaluateSimpleValue(text);
    }

    private double evaluateSimpleValue(String text) {
        text = text.trim();
        if (text.equals("pi")) {
            return Math.PI;
        }
        if (text.startsWith("-pi")) {
            return -Math.PI;
        }
        return Double.parseDouble(text);
    }
}
