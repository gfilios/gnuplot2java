package com.gnuplot.cli.command;

/**
 * Visitor interface for executing Gnuplot commands.
 */
public interface CommandVisitor {
    void visitSetCommand(SetCommand command);
    void visitPlotCommand(PlotCommand command);
    void visitUnsetCommand(UnsetCommand command);
    void visitPauseCommand(PauseCommand command);
    void visitResetCommand(ResetCommand command);
}
