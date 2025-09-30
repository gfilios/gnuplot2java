package com.gnuplot.core.ast;

/**
 * AST node representing a variable reference.
 *
 * <p>Examples: x, y, pi, myVariable
 */
public record Variable(
        String name,
        SourceLocation location
) implements ASTNode {

    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visitVariable(this);
    }

    @Override
    public SourceLocation getLocation() {
        return location;
    }

    @Override
    public String toString() {
        return name;
    }
}