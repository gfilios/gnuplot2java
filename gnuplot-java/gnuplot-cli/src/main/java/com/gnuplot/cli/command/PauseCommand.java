package com.gnuplot.cli.command;

/**
 * Represents a PAUSE command in Gnuplot.
 */
public final class PauseCommand implements Command {
    private final double seconds;
    private final String message;

    public PauseCommand(double seconds, String message) {
        this.seconds = seconds;
        this.message = message;
    }

    public double getSeconds() {
        return seconds;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public void accept(CommandVisitor visitor) {
        visitor.visitPauseCommand(this);
    }

    @Override
    public String toString() {
        return String.format("PAUSE %f %s", seconds, message != null ? "\"" + message + "\"" : "");
    }
}
