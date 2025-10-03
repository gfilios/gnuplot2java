package com.gnuplot.cli.parser;

import com.gnuplot.cli.GnuplotCommandBaseVisitor;
import com.gnuplot.cli.GnuplotCommandParser;
import com.gnuplot.cli.command.*;

import java.util.ArrayList;
import java.util.List;

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
            } else if (optCtx instanceof GnuplotCommandParser.SetSamplesContext) {
                GnuplotCommandParser.SetSamplesContext samplesCtx = (GnuplotCommandParser.SetSamplesContext) optCtx;
                int samples = Integer.parseInt(samplesCtx.NUMBER().getText());
                commands.add(new SetCommand("samples", samples));
            } else if (optCtx instanceof GnuplotCommandParser.SetKeyContext) {
                commands.add(new SetCommand("key", optCtx.getText()));
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

        commands.add(new PlotCommand(plotSpecs));
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
}
