package com.gnuplot.core.ast;

/**
 * AST node representing a binary operation.
 *
 * <p>Examples: x + y, 2 * 3, a && b, x < y
 */
public record BinaryOperation(
        Operator operator,
        ASTNode left,
        ASTNode right,
        SourceLocation location
) implements ASTNode {

    /**
     * Binary operators supported in gnuplot expressions.
     */
    public enum Operator {
        // Arithmetic
        ADD("+"),
        SUBTRACT("-"),
        MULTIPLY("*"),
        DIVIDE("/"),
        MODULO("%"),
        POWER("**"),

        // Comparison
        LESS_THAN("<"),
        LESS_EQUAL("<="),
        GREATER_THAN(">"),
        GREATER_EQUAL(">="),
        EQUAL("=="),
        NOT_EQUAL("!="),

        // Logical
        LOGICAL_AND("&&"),
        LOGICAL_OR("||"),

        // Bitwise
        BITWISE_AND("&"),
        BITWISE_OR("|"),
        BITWISE_XOR("^");

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
        return visitor.visitBinaryOperation(this);
    }

    @Override
    public SourceLocation getLocation() {
        return location;
    }

    @Override
    public String toString() {
        return String.format("(%s %s %s)", left, operator.getSymbol(), right);
    }
}