package com.gnuplot.core.ast;

/**
 * Base interface for all Abstract Syntax Tree (AST) nodes.
 *
 * <p>AST nodes represent the structure of parsed expressions in a form
 * that can be easily analyzed, optimized, and evaluated. Each node type
 * corresponds to a different kind of expression or operation.
 *
 * <p>This design uses the Visitor pattern to allow operations on the AST
 * without modifying the node classes themselves.
 */
public interface ASTNode {

    /**
     * Accept a visitor for double-dispatch pattern.
     *
     * @param <T> the return type of the visitor
     * @param visitor the visitor to accept
     * @return the result of visiting this node
     */
    <T> T accept(ASTVisitor<T> visitor);

    /**
     * Get the source location of this node (for error reporting).
     *
     * @return the source location, or null if not available
     */
    SourceLocation getLocation();
}