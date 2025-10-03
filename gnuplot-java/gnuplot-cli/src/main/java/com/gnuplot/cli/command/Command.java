package com.gnuplot.cli.command;

/**
 * Base interface for all Gnuplot commands.
 */
public interface Command {
    /**
     * Accept a visitor to execute this command.
     */
    void accept(CommandVisitor visitor);
}
