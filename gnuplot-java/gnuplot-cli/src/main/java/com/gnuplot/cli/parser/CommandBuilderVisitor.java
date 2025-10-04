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

                // Extract position
                String position = parseKeyPosition(keyCtx.keyPosition());

                // Extract options (BOX/NOBOX, HORIZONTAL/VERTICAL)
                boolean showBorder = true; // default
                boolean horizontal = false; // default

                for (GnuplotCommandParser.KeyOptionsContext opt : keyCtx.keyOptions()) {
                    if (opt.BOX() != null) showBorder = true;
                    if (opt.NOBOX() != null) showBorder = false;
                    if (opt.HORIZONTAL() != null) horizontal = true;
                    if (opt.VERTICAL() != null) horizontal = false;
                }

                // Create structured command
                Map<String, Object> keySettings = new HashMap<>();
                keySettings.put("position", position);
                keySettings.put("showBorder", showBorder);
                keySettings.put("horizontal", horizontal);

                commands.add(new SetCommand("key", keySettings));
            } else if (optCtx instanceof GnuplotCommandParser.SetGridContext) {
                commands.add(new SetCommand("grid", true));
            } else if (optCtx instanceof GnuplotCommandParser.SetAutoscaleContext) {
                commands.add(new SetCommand("autoscale", true));
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
            String style = "lines";

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

            plotSpecs.add(new PlotCommand.PlotSpec(expression, title, style));
        }

        commands.add(new PlotCommand(plotSpecs, xRange, yRange));
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

    private String parseKeyPosition(GnuplotCommandParser.KeyPositionContext ctx) {
        // Check margin-based positions first (they can include LEFT/RIGHT/CENTER as modifiers)
        if (ctx.BMARGIN() != null) {
            if (ctx.LEFT() != null) return "BOTTOM_LEFT";
            if (ctx.RIGHT() != null) return "BOTTOM_RIGHT";
            if (ctx.CENTER() != null) return "BOTTOM_CENTER";
            return "BOTTOM_CENTER";
        }
        if (ctx.TMARGIN() != null) {
            if (ctx.LEFT() != null) return "TOP_LEFT";
            if (ctx.RIGHT() != null) return "TOP_RIGHT";
            if (ctx.CENTER() != null) return "TOP_CENTER";
            return "TOP_CENTER";
        }
        if (ctx.LMARGIN() != null) return "LEFT";
        if (ctx.RMARGIN() != null) return "RIGHT";

        // Then check simple positions
        if (ctx.LEFT() != null) return "LEFT";
        if (ctx.RIGHT() != null) return "RIGHT";
        if (ctx.TOP() != null) return "TOP";
        if (ctx.BOTTOM() != null) return "BOTTOM";
        if (ctx.CENTER() != null) return "CENTER";

        return "TOP_RIGHT"; // default
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
