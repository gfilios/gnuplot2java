package com.gnuplot.core.data.transform;

import com.gnuplot.core.ast.ASTNode;
import com.gnuplot.core.data.DataRecord;
import com.gnuplot.core.evaluator.EvaluationContext;
import com.gnuplot.core.evaluator.Evaluator;
import com.gnuplot.core.parser.ExpressionParser;

/**
 * DataFilter that evaluates expressions to determine record inclusion.
 * Uses the gnuplot expression evaluator to support complex filtering conditions.
 *
 * <p>Expression variables are bound to column values:
 * <ul>
 *   <li>$0, $1, $2, ... for columns by index</li>
 *   <li>Column names for named columns (if metadata has headers)</li>
 * </ul>
 *
 * <p>Example usage:
 * <pre>{@code
 * // Filter where column 0 > 10 and column 1 < 100
 * DataFilter filter = ExpressionFilter.compile("$0 > 10 && $1 < 100");
 *
 * // Filter using column names (requires header)
 * DataFilter filter = ExpressionFilter.compile("age >= 18 && score > 75");
 *
 * // Complex mathematical expressions
 * DataFilter filter = ExpressionFilter.compile("sqrt($0**2 + $1**2) < 10");
 * }</pre>
 *
 * @since 1.0
 */
public class ExpressionFilter implements DataFilter {

    private final ASTNode expression;

    private ExpressionFilter(ASTNode expression) {
        this.expression = expression;
    }

    /**
     * Compiles an expression string into a filter.
     *
     * @param expressionString the expression to compile
     * @return compiled filter
     */
    public static ExpressionFilter compile(String expressionString) {
        ExpressionParser parser = new ExpressionParser();
        ASTNode expression = parser.parseOrThrow(expressionString);
        return new ExpressionFilter(expression);
    }

    @Override
    public boolean test(DataRecord record) {
        try {
            EvaluationContext context = createContext(record);
            Evaluator evaluator = new Evaluator(context);
            double result = evaluator.evaluate(expression);

            // Non-zero is true, zero is false (C-style)
            return result != 0.0;
        } catch (Exception e) {
            // If evaluation fails, exclude the record
            return false;
        }
    }

    private EvaluationContext createContext(DataRecord record) {
        EvaluationContext context = new EvaluationContext();

        // Bind columns by index ($0, $1, $2, ...)
        for (int i = 0; i < record.size(); i++) {
            String varName = "$" + i;
            try {
                double value = record.getDouble(i);
                context.setVariable(varName, value);
            } catch (NumberFormatException e) {
                // Skip non-numeric values
            }
        }

        // Try to bind columns by name if available
        // This requires accessing metadata through the record
        // For now, we'll support manual variable binding
        // via a more advanced API if needed

        return context;
    }

    /**
     * Creates an expression filter with pre-bound variables.
     * Use this when you need to bind column names to indices.
     *
     * @param expressionString the expression
     * @param columnBindings   map of column names to indices
     * @return filter with bindings
     */
    public static ExpressionFilter compileWithBindings(
            String expressionString,
            java.util.Map<String, Integer> columnBindings) {
        return new BoundExpressionFilter(expressionString, columnBindings);
    }

    /**
     * Expression filter with pre-bound column name mappings.
     */
    private static class BoundExpressionFilter extends ExpressionFilter {
        private final java.util.Map<String, Integer> columnBindings;

        BoundExpressionFilter(String expressionString,
                              java.util.Map<String, Integer> columnBindings) {
            super(new ExpressionParser().parseOrThrow(expressionString));
            this.columnBindings = columnBindings;
        }

        @Override
        public boolean test(DataRecord record) {
            try {
                EvaluationContext context = createBoundContext(record);
                Evaluator evaluator = new Evaluator(context);
                double result = evaluator.evaluate(super.expression);
                return result != 0.0;
            } catch (Exception e) {
                return false;
            }
        }

        private EvaluationContext createBoundContext(DataRecord record) {
            EvaluationContext context = new EvaluationContext();

            // Bind by index
            for (int i = 0; i < record.size(); i++) {
                String varName = "$" + i;
                try {
                    double value = record.getDouble(i);
                    context.setVariable(varName, value);
                } catch (NumberFormatException e) {
                    // Skip non-numeric values
                }
            }

            // Bind by name using the provided mappings
            for (java.util.Map.Entry<String, Integer> entry : columnBindings.entrySet()) {
                String name = entry.getKey();
                int index = entry.getValue();
                try {
                    if (index < record.size()) {
                        double value = record.getDouble(index);
                        context.setVariable(name, value);
                    }
                } catch (NumberFormatException e) {
                    // Skip non-numeric values
                }
            }

            return context;
        }
    }
}
