package com.gnuplot.core.oracle;

/**
 * Represents a single test case from the test oracle.
 * Contains an expression and its expected result from the C gnuplot implementation.
 */
public record TestCase(
        String expression,
        Double result,
        Boolean error
) {
    /**
     * Creates a test case with a successful result.
     */
    public TestCase(String expression, Double result) {
        this(expression, result, false);
    }

    /**
     * Creates a test case that represents an error condition.
     */
    public static TestCase error(String expression) {
        return new TestCase(expression, null, true);
    }

    /**
     * Checks if this test case represents an error condition.
     */
    public boolean isError() {
        return error != null && error;
    }

    /**
     * Gets the result, or throws if this is an error case.
     */
    public double getResult() {
        if (isError()) {
            throw new IllegalStateException("Test case is an error case: " + expression);
        }
        return result;
    }
}