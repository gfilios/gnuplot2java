package com.gnuplot.core.evaluator;

import com.gnuplot.core.ast.*;

/**
 * Evaluates an Abstract Syntax Tree (AST) to compute mathematical results.
 *
 * <p>This class implements the visitor pattern to traverse AST nodes and
 * compute their numeric values. It supports basic arithmetic operations,
 * function calls, and variable lookups.
 *
 * <h2>Usage Example:</h2>
 * <pre>{@code
 * ExpressionParser parser = new ExpressionParser();
 * ASTNode ast = parser.parseOrThrow("2 + 3 * 4");
 *
 * Evaluator evaluator = new Evaluator();
 * double result = evaluator.evaluate(ast);
 * // result = 14.0
 * }</pre>
 *
 * <p>The evaluator supports:
 * <ul>
 *   <li>Arithmetic: +, -, *, /, %, **</li>
 *   <li>Comparison: &lt;, &lt;=, &gt;, &gt;=, ==, !=</li>
 *   <li>Logical: &amp;&amp;, ||, !</li>
 *   <li>Bitwise: &amp;, |, ^, ~</li>
 *   <li>Ternary conditional: condition ? trueValue : falseValue</li>
 * </ul>
 *
 * @see ASTNode
 * @see EvaluationContext
 */
public class Evaluator implements ASTVisitor<Double> {

    private final EvaluationContext context;

    /**
     * Creates a new evaluator with an empty context.
     */
    public Evaluator() {
        this(new EvaluationContext());
    }

    /**
     * Creates a new evaluator with the given context.
     *
     * @param context the evaluation context containing variables and functions
     */
    public Evaluator(EvaluationContext context) {
        this.context = context;
    }

    /**
     * Evaluates an AST and returns the result.
     *
     * @param node the AST node to evaluate
     * @return the computed numeric result
     * @throws EvaluationException if evaluation fails
     */
    public double evaluate(ASTNode node) {
        if (node == null) {
            throw new EvaluationException("Cannot evaluate null AST node");
        }
        return node.accept(this);
    }

    @Override
    public Double visitNumberLiteral(NumberLiteral node) {
        return node.value();
    }

    @Override
    public Double visitVariable(Variable node) {
        if (!context.hasVariable(node.name())) {
            throw new EvaluationException(
                    String.format("Undefined variable: %s at line %d, column %d",
                            node.name(),
                            node.getLocation().line(),
                            node.getLocation().column())
            );
        }
        return context.getVariable(node.name());
    }

    @Override
    public Double visitBinaryOperation(BinaryOperation node) {
        double left = node.left().accept(this);
        double right = node.right().accept(this);

        return switch (node.operator()) {
            // Arithmetic operators
            case ADD -> left + right;
            case SUBTRACT -> left - right;
            case MULTIPLY -> left * right;
            case DIVIDE -> {
                if (right == 0.0) {
                    throw new EvaluationException(
                            String.format("Division by zero at line %d, column %d",
                                    node.getLocation().line(),
                                    node.getLocation().column())
                    );
                }
                yield left / right;
            }
            case MODULO -> {
                if (right == 0.0) {
                    throw new EvaluationException(
                            String.format("Modulo by zero at line %d, column %d",
                                    node.getLocation().line(),
                                    node.getLocation().column())
                    );
                }
                yield left % right;
            }
            case POWER -> Math.pow(left, right);

            // Comparison operators (return 1.0 for true, 0.0 for false like C/gnuplot)
            case LESS_THAN -> left < right ? 1.0 : 0.0;
            case LESS_EQUAL -> left <= right ? 1.0 : 0.0;
            case GREATER_THAN -> left > right ? 1.0 : 0.0;
            case GREATER_EQUAL -> left >= right ? 1.0 : 0.0;
            case EQUAL -> left == right ? 1.0 : 0.0;
            case NOT_EQUAL -> left != right ? 1.0 : 0.0;

            // Logical operators (treat 0.0 as false, non-zero as true)
            case LOGICAL_AND -> (left != 0.0 && right != 0.0) ? 1.0 : 0.0;
            case LOGICAL_OR -> (left != 0.0 || right != 0.0) ? 1.0 : 0.0;

            // Bitwise operators (convert to int, operate, convert back)
            case BITWISE_AND -> (double) ((long) left & (long) right);
            case BITWISE_OR -> (double) ((long) left | (long) right);
            case BITWISE_XOR -> (double) ((long) left ^ (long) right);
        };
    }

    @Override
    public Double visitUnaryOperation(UnaryOperation node) {
        double operand = node.operand().accept(this);

        return switch (node.operator()) {
            case NEGATE -> -operand;
            case PLUS -> operand;
            case LOGICAL_NOT -> operand == 0.0 ? 1.0 : 0.0;
            case BITWISE_NOT -> (double) ~((long) operand);
        };
    }

    @Override
    public Double visitFunctionCall(FunctionCall node) {
        // Evaluate all arguments
        double[] args = node.arguments().stream()
                .mapToDouble(arg -> arg.accept(this))
                .toArray();

        // Look up and call the function
        if (!context.hasFunction(node.functionName())) {
            throw new EvaluationException(
                    String.format("Undefined function: %s at line %d, column %d",
                            node.functionName(),
                            node.getLocation().line(),
                            node.getLocation().column())
            );
        }

        try {
            return context.getFunction(node.functionName()).call(args);
        } catch (Exception e) {
            throw new EvaluationException(
                    String.format("Error calling function %s at line %d, column %d: %s",
                            node.functionName(),
                            node.getLocation().line(),
                            node.getLocation().column(),
                            e.getMessage()),
                    e
            );
        }
    }

    @Override
    public Double visitTernaryConditional(TernaryConditional node) {
        double condition = node.condition().accept(this);
        // Treat 0.0 as false, non-zero as true (like C/gnuplot)
        if (condition != 0.0) {
            return node.trueExpression().accept(this);
        } else {
            return node.falseExpression().accept(this);
        }
    }

    /**
     * Gets the evaluation context.
     *
     * @return the evaluation context
     */
    public EvaluationContext getContext() {
        return context;
    }
}