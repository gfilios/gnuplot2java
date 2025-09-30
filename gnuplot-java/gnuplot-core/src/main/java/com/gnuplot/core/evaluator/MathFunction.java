package com.gnuplot.core.evaluator;

/**
 * Functional interface for mathematical functions.
 *
 * <p>This interface represents a mathematical function that can be called
 * with a variable number of numeric arguments and returns a numeric result.
 *
 * <h2>Usage Example:</h2>
 * <pre>{@code
 * // Register a custom function
 * context.registerFunction("square", args -> {
 *     if (args.length != 1) {
 *         throw new IllegalArgumentException("square() requires 1 argument");
 *     }
 *     return args[0] * args[0];
 * });
 *
 * // Use in expression
 * double result = evaluator.evaluate(parser.parseOrThrow("square(5)"));
 * // result = 25.0
 * }</pre>
 */
@FunctionalInterface
public interface MathFunction {

    /**
     * Calls the function with the given arguments.
     *
     * @param args the function arguments
     * @return the function result
     * @throws IllegalArgumentException if arguments are invalid
     */
    double call(double... args);

    /**
     * Creates a function that validates argument count.
     *
     * @param expectedCount the expected number of arguments
     * @param function the function implementation
     * @return a wrapped function that validates argument count
     */
    static MathFunction withArgCount(int expectedCount, MathFunction function) {
        return args -> {
            if (args.length != expectedCount) {
                throw new IllegalArgumentException(
                        String.format("Expected %d arguments but got %d", expectedCount, args.length)
                );
            }
            return function.call(args);
        };
    }

    /**
     * Creates a function that validates minimum argument count.
     *
     * @param minCount the minimum number of arguments
     * @param function the function implementation
     * @return a wrapped function that validates argument count
     */
    static MathFunction withMinArgCount(int minCount, MathFunction function) {
        return args -> {
            if (args.length < minCount) {
                throw new IllegalArgumentException(
                        String.format("Expected at least %d arguments but got %d", minCount, args.length)
                );
            }
            return function.call(args);
        };
    }
}