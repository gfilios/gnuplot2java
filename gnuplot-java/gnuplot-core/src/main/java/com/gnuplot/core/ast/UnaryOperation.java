package com.gnuplot.core.ast;

/**
 * AST node representing a unary operation.
 *
 * <p>Examples: -x, +5, !flag, ~bits
 */
public record UnaryOperation(
        Operator operator,
        ASTNode operand,
        SourceLocation location
) implements ASTNode {

    /**
     * Unary operators supported in gnuplot expressions.
     */
    public enum Operator {
        NEGATE("-"),
        PLUS("+"),
        LOGICAL_NOT("!"),
        BITWISE_NOT("~");

        private final String symbol;

        Operator(String symbol) {
            this.symbol = symbol;
        }

        public String getSymbol() {
            return symbol;
        }

        @Override
        public String toString() {
            return symbol;
        }
    }

    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visitUnaryOperation(this);
    }

    @Override
    public SourceLocation getLocation() {
        return location;
    }

    @Override
    public String toString() {
        return String.format("(%s%s)", operator.getSymbol(), operand);
    }
}