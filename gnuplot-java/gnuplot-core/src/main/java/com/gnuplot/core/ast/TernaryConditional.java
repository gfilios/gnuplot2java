package com.gnuplot.core.ast;

/**
 * AST node representing a ternary conditional expression.
 *
 * <p>Example: x > 0 ? 1 : -1
 */
public record TernaryConditional(
        ASTNode condition,
        ASTNode trueExpression,
        ASTNode falseExpression,
        SourceLocation location
) implements ASTNode {

    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visitTernaryConditional(this);
    }

    @Override
    public SourceLocation getLocation() {
        return location;
    }

    @Override
    public String toString() {
        return String.format("(%s ? %s : %s)", condition, trueExpression, falseExpression);
    }
}