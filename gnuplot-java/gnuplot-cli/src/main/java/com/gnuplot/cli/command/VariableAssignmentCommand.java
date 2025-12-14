package com.gnuplot.cli.command;

import java.util.Objects;

/**
 * Represents a variable assignment in Gnuplot.
 *
 * <p>Examples:
 * <ul>
 *   <li>{@code wn = 1.0}</li>
 *   <li>{@code s = 0.5}</li>
 *   <li>{@code a = b + c}</li>
 * </ul>
 */
public final class VariableAssignmentCommand implements Command {
    private final String variableName;
    private final String expression;

    public VariableAssignmentCommand(String variableName, String expression) {
        this.variableName = Objects.requireNonNull(variableName, "variableName cannot be null");
        this.expression = Objects.requireNonNull(expression, "expression cannot be null");
    }

    public String getVariableName() {
        return variableName;
    }

    public String getExpression() {
        return expression;
    }

    @Override
    public void accept(CommandVisitor visitor) {
        visitor.visitVariableAssignmentCommand(this);
    }

    @Override
    public String toString() {
        return String.format("%s = %s", variableName, expression);
    }
}
