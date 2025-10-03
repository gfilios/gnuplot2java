package com.gnuplot.cli.command;

/**
 * Represents a RESET command in Gnuplot.
 */
public final class ResetCommand implements Command {

    @Override
    public void accept(CommandVisitor visitor) {
        visitor.visitResetCommand(this);
    }

    @Override
    public String toString() {
        return "RESET";
    }
}
