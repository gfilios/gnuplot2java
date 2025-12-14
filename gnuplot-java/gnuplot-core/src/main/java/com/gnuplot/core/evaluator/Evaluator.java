package com.gnuplot.core.evaluator;

import com.gnuplot.core.ast.*;
import com.gnuplot.core.parser.ExpressionParser;
import com.gnuplot.core.parser.ParseResult;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    private final ExpressionParser parser;

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
        this.parser = new ExpressionParser();
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
        String functionName = node.functionName();

        // Evaluate all arguments
        double[] args = node.arguments().stream()
                .mapToDouble(arg -> arg.accept(this))
                .toArray();

        // Check for user-defined functions first
        EvaluationContext.UserFunction userFunc = context.getUserFunction(functionName);
        if (userFunc != null) {
            return evaluateUserFunction(userFunc, args, node);
        }

        // Fall back to built-in functions
        if (!context.hasFunction(functionName)) {
            throw new EvaluationException(
                    String.format("Undefined function: %s at line %d, column %d",
                            functionName,
                            node.getLocation().line(),
                            node.getLocation().column())
            );
        }

        try {
            return context.getFunction(functionName).call(args);
        } catch (Exception e) {
            throw new EvaluationException(
                    String.format("Error calling function %s at line %d, column %d: %s",
                            functionName,
                            node.getLocation().line(),
                            node.getLocation().column(),
                            e.getMessage()),
                    e
            );
        }
    }

    /**
     * Evaluates a user-defined function by binding arguments to parameters
     * and evaluating the body expression.
     *
     * @param userFunc the user function definition
     * @param args the evaluated argument values
     * @param node the function call node (for error reporting)
     * @return the result of evaluating the function body
     */
    private Double evaluateUserFunction(EvaluationContext.UserFunction userFunc, double[] args, FunctionCall node) {
        List<String> parameters = userFunc.parameters();

        // Validate argument count
        if (args.length != parameters.size()) {
            throw new EvaluationException(
                    String.format("Function %s expects %d argument(s) but got %d at line %d, column %d",
                            node.functionName(),
                            parameters.size(),
                            args.length,
                            node.getLocation().line(),
                            node.getLocation().column())
            );
        }

        // Save current variable values for parameters (to restore later)
        Map<String, Double> savedValues = new HashMap<>();
        for (String param : parameters) {
            if (context.hasVariable(param)) {
                savedValues.put(param, context.getVariable(param));
            }
        }

        try {
            // Bind arguments to parameters
            for (int i = 0; i < parameters.size(); i++) {
                context.setVariable(parameters.get(i), args[i]);
            }

            // Parse and evaluate the function body
            ParseResult parseResult = parser.parse(userFunc.bodyExpression());
            if (!parseResult.isSuccess()) {
                throw new EvaluationException(
                        String.format("Error parsing function %s body: %s",
                                node.functionName(),
                                parseResult.getError())
                );
            }

            return evaluate(parseResult.getAst());
        } finally {
            // Restore previous variable values
            for (String param : parameters) {
                if (savedValues.containsKey(param)) {
                    context.setVariable(param, savedValues.get(param));
                } else {
                    context.removeVariable(param);
                }
            }
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

    @Override
    public Double visitAssignmentExpression(AssignmentExpression node) {
        // Evaluate the value expression
        double value = node.valueExpression().accept(this);
        // Assign to the variable
        context.setVariable(node.variableName(), value);
        // Return the assigned value (assignment is an expression in gnuplot, like C)
        return value;
    }

    @Override
    public Double visitCommaExpression(CommaExpression node) {
        // Evaluate left expression and discard the result
        node.left().accept(this);
        // Evaluate right expression and return the result
        return node.right().accept(this);
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