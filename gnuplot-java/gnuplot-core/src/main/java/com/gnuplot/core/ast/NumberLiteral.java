package com.gnuplot.core.ast;

/**
 * AST node representing a numeric literal.
 *
 * <p>Examples: 42, 3.14159, 1.5e-5
 */
public record NumberLiteral(
        double value,
        SourceLocation location
) implements ASTNode {

    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visitNumberLiteral(this);
    }

    @Override
    public SourceLocation getLocation() {
        return location;
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }
}