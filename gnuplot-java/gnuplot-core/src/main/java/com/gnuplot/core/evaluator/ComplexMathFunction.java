package com.gnuplot.core.evaluator;

/**
 * Functional interface for mathematical functions that work with complex numbers.
 */
@FunctionalInterface
public interface ComplexMathFunction {

    /**
     * Calls the function with the given complex arguments.
     *
     * @param args the function arguments as complex numbers
     * @return the function result as a complex number
     * @throws IllegalArgumentException if arguments are invalid
     */
    Complex call(Complex... args);

    /**
     * Creates a function that validates argument count.
     *
     * @param expectedCount the expected number of arguments
     * @param function the function implementation
     * @return a wrapped function that validates argument count
     */
    static ComplexMathFunction withArgCount(int expectedCount, ComplexMathFunction function) {
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
    static ComplexMathFunction withMinArgCount(int minCount, ComplexMathFunction function) {
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
