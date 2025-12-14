package com.gnuplot.cli.command;

import java.util.List;
import java.util.Objects;

/**
 * Represents a user-defined function definition in Gnuplot.
 *
 * <p>Examples:
 * <ul>
 *   <li>{@code f(x) = x**2}</li>
 *   <li>{@code damp(t) = exp(-s*wn*t)/sqrt(1.0-s*s)}</li>
 *   <li>{@code c(t) = 1-damp(t)*per(t)}</li>
 * </ul>
 */
public final class FunctionDefinitionCommand implements Command {
    private final String functionName;
    private final List<String> parameters;
    private final String bodyExpression;

    public FunctionDefinitionCommand(String functionName, List<String> parameters, String bodyExpression) {
        this.functionName = Objects.requireNonNull(functionName, "functionName cannot be null");
        this.parameters = List.copyOf(Objects.requireNonNull(parameters, "parameters cannot be null"));
        this.bodyExpression = Objects.requireNonNull(bodyExpression, "bodyExpression cannot be null");
    }

    public String getFunctionName() {
        return functionName;
    }

    public List<String> getParameters() {
        return parameters;
    }

    public String getBodyExpression() {
        return bodyExpression;
    }

    @Override
    public void accept(CommandVisitor visitor) {
        visitor.visitFunctionDefinitionCommand(this);
    }

    @Override
    public String toString() {
        return String.format("%s(%s) = %s", functionName, String.join(", ", parameters), bodyExpression);
    }
}
