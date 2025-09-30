package com.gnuplot.core.parser;

import com.gnuplot.core.ast.ASTNode;

/**
 * Result of parsing an expression.
 *
 * <p>This class represents the outcome of parsing an expression, which can either
 * be successful (containing an AST) or failed (containing an error message).
 *
 * <p>This design allows for error handling without exceptions, which can be more
 * convenient in many scenarios:
 *
 * <pre>{@code
 * ParseResult result = parser.parse("x + 1");
 * if (result.isSuccess()) {
 *     processAst(result.getAst());
 * } else {
 *     logError(result.getError());
 * }
 * }</pre>
 */
public final class ParseResult {
    private final ASTNode ast;
    private final String error;
    private final boolean success;

    private ParseResult(ASTNode ast, String error, boolean success) {
        this.ast = ast;
        this.error = error;
        this.success = success;
    }

    /**
     * Creates a successful parse result.
     *
     * @param ast the parsed AST
     * @return a successful ParseResult
     */
    public static ParseResult success(ASTNode ast) {
        if (ast == null) {
            throw new IllegalArgumentException("AST cannot be null for successful parse");
        }
        return new ParseResult(ast, null, true);
    }

    /**
     * Creates a failed parse result.
     *
     * @param error the error message
     * @return a failed ParseResult
     */
    public static ParseResult failure(String error) {
        if (error == null || error.trim().isEmpty()) {
            throw new IllegalArgumentException("Error message cannot be null or empty");
        }
        return new ParseResult(null, error, false);
    }

    /**
     * Returns whether the parse was successful.
     *
     * @return true if parsing succeeded, false otherwise
     */
    public boolean isSuccess() {
        return success;
    }

    /**
     * Returns whether the parse failed.
     *
     * @return true if parsing failed, false otherwise
     */
    public boolean isFailure() {
        return !success;
    }

    /**
     * Gets the parsed AST.
     *
     * @return the AST if parsing was successful
     * @throws IllegalStateException if parsing failed
     */
    public ASTNode getAst() {
        if (!success) {
            throw new IllegalStateException("Cannot get AST from failed parse result");
        }
        return ast;
    }

    /**
     * Gets the error message.
     *
     * @return the error message if parsing failed
     * @throws IllegalStateException if parsing succeeded
     */
    public String getError() {
        if (success) {
            throw new IllegalStateException("Cannot get error from successful parse result");
        }
        return error;
    }

    @Override
    public String toString() {
        if (success) {
            return "ParseResult{success=true, ast=" + ast + "}";
        } else {
            return "ParseResult{success=false, error='" + error + "'}";
        }
    }
}