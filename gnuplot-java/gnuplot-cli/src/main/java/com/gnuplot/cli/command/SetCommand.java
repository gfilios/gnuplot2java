package com.gnuplot.cli.command;

import java.util.Objects;

/**
 * Represents a SET command in Gnuplot.
 */
public final class SetCommand implements Command {
    private final String option;
    private final Object value;

    public SetCommand(String option, Object value) {
        this.option = Objects.requireNonNull(option, "option cannot be null");
        this.value = value;
    }

    public String getOption() {
        return option;
    }

    public Object getValue() {
        return value;
    }

    @Override
    public void accept(CommandVisitor visitor) {
        visitor.visitSetCommand(this);
    }

    @Override
    public String toString() {
        return String.format("SET %s %s", option, value);
    }
}
