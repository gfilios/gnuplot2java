package com.gnuplot.cli.command;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents a parsed Gnuplot script containing multiple commands.
 */
public final class GnuplotScript {
    private final List<Command> commands;

    public GnuplotScript(List<Command> commands) {
        this.commands = Collections.unmodifiableList(new ArrayList<>(commands));
    }

    public List<Command> getCommands() {
        return commands;
    }

    @Override
    public String toString() {
        return String.format("GnuplotScript{commands=%d}", commands.size());
    }
}
