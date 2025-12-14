package com.gnuplot.core.ast;

/**
 * AST node representing a comma expression (sequence operator).
 *
 * <p>The comma operator evaluates its left operand, discards the result,
 * then evaluates and returns the right operand. This is used in gnuplot
 * for inline parameter assignments in plot commands.
 *
 * <p>Example: {@code s=.1,c(t)} evaluates s=.1 (assigns s), discards result,
 * then evaluates c(t) and returns that value.
 *
 * <p>For chained commas like {@code a,b,c}, this creates a left-associative
 * tree: {@code CommaExpression(CommaExpression(a, b), c)}.
 */
public record CommaExpression(
        ASTNode left,
        ASTNode right,
        SourceLocation location
) implements ASTNode {

    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visitCommaExpression(this);
    }

    @Override
    public SourceLocation getLocation() {
        return location;
    }

    @Override
    public String toString() {
        return String.format("(%s, %s)", left, right);
    }
}
