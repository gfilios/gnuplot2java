package com.gnuplot.core.ast;

/**
 * AST node representing an assignment expression.
 *
 * <p>In gnuplot, assignment is an expression that returns the assigned value.
 * This allows syntax like: {@code plot s=.1,c(t)} where s is assigned before
 * evaluating c(t).
 *
 * <p>Example: {@code x = 5} assigns 5 to x and returns 5.
 */
public record AssignmentExpression(
        String variableName,
        ASTNode valueExpression,
        SourceLocation location
) implements ASTNode {

    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visitAssignmentExpression(this);
    }

    @Override
    public SourceLocation getLocation() {
        return location;
    }

    @Override
    public String toString() {
        return String.format("(%s = %s)", variableName, valueExpression);
    }
}
