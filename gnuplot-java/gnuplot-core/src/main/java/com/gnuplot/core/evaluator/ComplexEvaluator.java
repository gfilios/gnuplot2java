package com.gnuplot.core.evaluator;

import com.gnuplot.core.ast.*;
import com.gnuplot.core.parser.ExpressionParser;
import com.gnuplot.core.parser.ParseResult;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Evaluates an AST using complex numbers internally.
 * This allows proper handling of operations like sqrt(-1).
 *
 * <p>All operations are performed with complex numbers, and the result
 * can be retrieved as either a Complex number or as the real part only.
 */
public class ComplexEvaluator implements ASTVisitor<Complex> {

    private final EvaluationContext context;
    private final ExpressionParser parser;

    /**
     * Creates a new complex evaluator with an empty context.
     */
    public ComplexEvaluator() {
        this(new EvaluationContext());
    }

    /**
     * Creates a new complex evaluator with the given context.
     *
     * @param context the evaluation context containing variables and functions
     */
    public ComplexEvaluator(EvaluationContext context) {
        this.context = context;
        this.parser = new ExpressionParser();
    }

    /**
     * Evaluates an AST and returns the complex result.
     *
     * @param node the AST node to evaluate
     * @return the computed complex result
     * @throws EvaluationException if evaluation fails
     */
    public Complex evaluate(ASTNode node) {
        if (node == null) {
            throw new EvaluationException("Cannot evaluate null AST node");
        }
        return node.accept(this);
    }

    /**
     * Evaluates an AST and returns the real part of the result.
     *
     * @param node the AST node to evaluate
     * @return the real part of the computed result
     * @throws EvaluationException if evaluation fails
     */
    public double evaluateReal(ASTNode node) {
        Complex result = evaluate(node);
        return result.real();
    }

    @Override
    public Complex visitNumberLiteral(NumberLiteral node) {
        return new Complex(node.value());
    }

    @Override
    public Complex visitVariable(Variable node) {
        if (!context.hasVariable(node.name())) {
            throw new EvaluationException(
                    String.format("Undefined variable: %s at line %d, column %d",
                            node.name(),
                            node.getLocation().line(),
                            node.getLocation().column())
            );
        }
        return new Complex(context.getVariable(node.name()));
    }

    @Override
    public Complex visitBinaryOperation(BinaryOperation node) {
        Complex left = node.left().accept(this);
        Complex right = node.right().accept(this);

        return switch (node.operator()) {
            // Arithmetic operators
            case ADD -> left.add(right);
            case SUBTRACT -> left.subtract(right);
            case MULTIPLY -> left.multiply(right);
            case DIVIDE -> {
                if (right.real() == 0.0 && right.imag() == 0.0) {
                    // Division by zero returns NaN (matching gnuplot behavior)
                    yield new Complex(Double.NaN, Double.NaN);
                }
                yield left.divide(right);
            }
            case MODULO -> {
                // Modulo only makes sense for real numbers
                if (right.real() == 0.0) {
                    throw new EvaluationException(
                            String.format("Modulo by zero at line %d, column %d",
                                    node.getLocation().line(),
                                    node.getLocation().column())
                    );
                }
                yield new Complex(left.real() % right.real());
            }
            case POWER -> Complex.pow(left, right);

            // Comparison operators (return 1.0 for true, 0.0 for false)
            // Compare real parts for complex numbers
            case LESS_THAN -> new Complex(left.real() < right.real() ? 1.0 : 0.0);
            case LESS_EQUAL -> new Complex(left.real() <= right.real() ? 1.0 : 0.0);
            case GREATER_THAN -> new Complex(left.real() > right.real() ? 1.0 : 0.0);
            case GREATER_EQUAL -> new Complex(left.real() >= right.real() ? 1.0 : 0.0);
            case EQUAL -> new Complex(left.equals(right) ? 1.0 : 0.0);
            case NOT_EQUAL -> new Complex(!left.equals(right) ? 1.0 : 0.0);

            // Logical operators (treat 0.0 as false, non-zero as true)
            case LOGICAL_AND -> new Complex((left.real() != 0.0 && right.real() != 0.0) ? 1.0 : 0.0);
            case LOGICAL_OR -> new Complex((left.real() != 0.0 || right.real() != 0.0) ? 1.0 : 0.0);

            // Bitwise operators (convert to int, operate, convert back)
            case BITWISE_AND -> new Complex((long) left.real() & (long) right.real());
            case BITWISE_OR -> new Complex((long) left.real() | (long) right.real());
            case BITWISE_XOR -> new Complex((long) left.real() ^ (long) right.real());
        };
    }

    @Override
    public Complex visitUnaryOperation(UnaryOperation node) {
        Complex operand = node.operand().accept(this);

        return switch (node.operator()) {
            case NEGATE -> operand.negate();
            case PLUS -> operand;
            case LOGICAL_NOT -> new Complex(operand.real() == 0.0 && operand.imag() == 0.0 ? 1.0 : 0.0);
            case BITWISE_NOT -> new Complex(~((long) operand.real()));
        };
    }

    @Override
    public Complex visitFunctionCall(FunctionCall node) {
        String functionName = node.functionName();

        // Evaluate all arguments as complex numbers
        Complex[] args = node.arguments().stream()
                .map(arg -> arg.accept(this))
                .toArray(Complex[]::new);

        // Check for user-defined functions first
        EvaluationContext.UserFunction userFunc = context.getUserFunction(functionName);
        if (userFunc != null) {
            return evaluateUserFunction(userFunc, args, node);
        }

        // Check for complex-aware functions
        if (context.hasComplexFunction(functionName)) {
            try {
                return context.getComplexFunction(functionName).call(args);
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

        // Fall back to real-valued built-in functions
        if (!context.hasFunction(functionName)) {
            throw new EvaluationException(
                    String.format("Undefined function: %s at line %d, column %d",
                            functionName,
                            node.getLocation().line(),
                            node.getLocation().column())
            );
        }

        try {
            // Convert complex args to real for real-valued functions
            double[] realArgs = new double[args.length];
            for (int i = 0; i < args.length; i++) {
                realArgs[i] = args[i].real();
            }
            double result = context.getFunction(functionName).call(realArgs);
            return new Complex(result);
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
     * Evaluates a user-defined function using complex arithmetic.
     */
    private Complex evaluateUserFunction(EvaluationContext.UserFunction userFunc, Complex[] args, FunctionCall node) {
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
            // Bind arguments to parameters (use real part for variable storage)
            for (int i = 0; i < parameters.size(); i++) {
                context.setVariable(parameters.get(i), args[i].real());
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
    public Complex visitTernaryConditional(TernaryConditional node) {
        Complex condition = node.condition().accept(this);
        // Treat 0.0 as false, non-zero as true
        if (condition.real() != 0.0 || condition.imag() != 0.0) {
            return node.trueExpression().accept(this);
        } else {
            return node.falseExpression().accept(this);
        }
    }

    @Override
    public Complex visitAssignmentExpression(AssignmentExpression node) {
        // Evaluate the value expression
        Complex value = node.valueExpression().accept(this);
        // Assign real part to the variable
        context.setVariable(node.variableName(), value.real());
        // Return the assigned value
        return value;
    }

    @Override
    public Complex visitCommaExpression(CommaExpression node) {
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
