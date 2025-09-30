package com.gnuplot.core.ast;

import java.util.List;

/**
 * AST node representing a function call.
 *
 * <p>Examples: sin(x), atan2(y, x), sqrt(4), log(exp(1))
 */
public record FunctionCall(
        String functionName,
        List<ASTNode> arguments,
        SourceLocation location
) implements ASTNode {

    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visitFunctionCall(this);
    }

    @Override
    public SourceLocation getLocation() {
        return location;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(functionName).append("(");
        for (int i = 0; i < arguments.size(); i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append(arguments.get(i));
        }
        sb.append(")");
        return sb.toString();
    }
}