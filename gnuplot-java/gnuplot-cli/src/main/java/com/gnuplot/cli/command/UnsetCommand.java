package com.gnuplot.cli.command;

import java.util.Objects;

/**
 * Represents an UNSET command in Gnuplot.
 */
public final class UnsetCommand implements Command {
    private final String option;

    public UnsetCommand(String option) {
        this.option = Objects.requireNonNull(option, "option cannot be null");
    }

    public String getOption() {
        return option;
    }

    @Override
    public void accept(CommandVisitor visitor) {
        visitor.visitUnsetCommand(this);
    }

    @Override
    public String toString() {
        return String.format("UNSET %s", option);
    }
}
