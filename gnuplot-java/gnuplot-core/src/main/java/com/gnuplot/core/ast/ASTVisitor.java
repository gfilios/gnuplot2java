package com.gnuplot.core.ast;

/**
 * Visitor interface for traversing and processing AST nodes.
 *
 * <p>This interface implements the Visitor pattern, allowing different
 * operations to be performed on the AST without modifying the node classes.
 *
 * @param <T> the return type of visitor methods
 */
public interface ASTVisitor<T> {

    // Literals
    T visitNumberLiteral(NumberLiteral node);
    T visitVariable(Variable node);

    // Binary operations
    T visitBinaryOperation(BinaryOperation node);

    // Unary operations
    T visitUnaryOperation(UnaryOperation node);

    // Function calls
    T visitFunctionCall(FunctionCall node);

    // Ternary conditional
    T visitTernaryConditional(TernaryConditional node);

    // Assignment expression (x = value, returns assigned value)
    T visitAssignmentExpression(AssignmentExpression node);

    // Comma expression (a, b - evaluates both, returns right)
    T visitCommaExpression(CommaExpression node);
}