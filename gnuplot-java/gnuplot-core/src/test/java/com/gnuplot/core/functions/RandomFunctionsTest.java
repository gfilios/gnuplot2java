package com.gnuplot.core.functions;

import com.gnuplot.core.evaluator.EvaluationContext;
import com.gnuplot.core.evaluator.Evaluator;
import com.gnuplot.core.parser.ExpressionParser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

class RandomFunctionsTest {

    private ExpressionParser parser;
    private Evaluator evaluator;
    private EvaluationContext context;

    @BeforeEach
    void setUp() {
        parser = new ExpressionParser();
        context = new EvaluationContext();
        RandomFunctions.registerAll(context);
        evaluator = new Evaluator(context);
    }

    @Test
    void testRandReturnsValueInRange() {
        var ast = parser.parseOrThrow("rand(0)");

        // Test multiple calls to ensure range
        for (int i = 0; i < 100; i++) {
            double result = ast.accept(evaluator);
            assertThat(result).isBetween(0.0, 1.0);
        }
    }

    @Test
    void testRandWithZeroIsNonDeterministic() {
        var ast = parser.parseOrThrow("rand(0)");

        // Get 100 random values
        Set<Double> values = new HashSet<>();
        for (int i = 0; i < 100; i++) {
            double result = ast.accept(evaluator);
            values.add(result);
        }

        // Should have many different values (not deterministic)
        assertThat(values.size()).isGreaterThan(90);
    }

    @Test
    void testRandWithSeedIsDeterministic() {
        var ast = parser.parseOrThrow("rand(42)");

        // First sequence
        double first1 = ast.accept(evaluator);
        double first2 = ast.accept(evaluator);
        double first3 = ast.accept(evaluator);

        // Reset context and use same seed
        context = new EvaluationContext();
        RandomFunctions.registerAll(context);
        evaluator = new Evaluator(context);
        ast = parser.parseOrThrow("rand(42)");

        // Second sequence with same seed
        double second1 = ast.accept(evaluator);
        double second2 = ast.accept(evaluator);
        double second3 = ast.accept(evaluator);

        // Should produce same sequence
        assertThat(first1).isEqualTo(second1);
        assertThat(first2).isEqualTo(second2);
        assertThat(first3).isEqualTo(second3);
    }

    @Test
    void testRandWithDifferentSeedsProducesDifferentValues() {
        var ast1 = parser.parseOrThrow("rand(42)");
        var ast2 = parser.parseOrThrow("rand(123)");

        double value1 = ast1.accept(evaluator);
        double value2 = ast2.accept(evaluator);

        // Different seeds should produce different values
        assertThat(value1).isNotEqualTo(value2);
    }

    @Test
    void testSgrandSetsSeed() {
        var sgrandAst = parser.parseOrThrow("sgrand(42)");
        var randAst = parser.parseOrThrow("rand(42)");

        // Set seed using sgrand
        sgrandAst.accept(evaluator);

        // Get first value after sgrand
        double afterSgrand = randAst.accept(evaluator);

        // Reset and get value directly with rand(42)
        context = new EvaluationContext();
        RandomFunctions.registerAll(context);
        evaluator = new Evaluator(context);
        randAst = parser.parseOrThrow("rand(42)");

        double directRand = randAst.accept(evaluator);

        // Should produce same first value
        assertThat(afterSgrand).isEqualTo(directRand);
    }

    @Test
    void testSgrandReturnsPreviousSeed() {
        var ast = parser.parseOrThrow("sgrand(42)");

        // First sgrand should return 0 (no previous seed)
        double result1 = ast.accept(evaluator);
        assertThat(result1).isEqualTo(0.0);

        // Second sgrand should return previous seed
        ast = parser.parseOrThrow("sgrand(123)");
        double result2 = ast.accept(evaluator);
        assertThat(result2).isEqualTo(42.0);

        // Third sgrand should return second seed
        ast = parser.parseOrThrow("sgrand(999)");
        double result3 = ast.accept(evaluator);
        assertThat(result3).isEqualTo(123.0);
    }

    @Test
    void testRandDistribution() {
        var ast = parser.parseOrThrow("rand(0)");

        int belowHalf = 0;
        int aboveHalf = 0;

        // Generate many values
        for (int i = 0; i < 1000; i++) {
            double value = ast.accept(evaluator);
            if (value < 0.5) {
                belowHalf++;
            } else {
                aboveHalf++;
            }
        }

        // Should be roughly uniform distribution
        // Allow 40-60% in each half (very loose bounds for randomness)
        assertThat(belowHalf).isBetween(400, 600);
        assertThat(aboveHalf).isBetween(400, 600);
    }

    @Test
    void testRandNeverReturnsOne() {
        var ast = parser.parseOrThrow("rand(0)");

        // Test many values - should never return exactly 1.0
        for (int i = 0; i < 1000; i++) {
            double result = ast.accept(evaluator);
            assertThat(result).isLessThan(1.0);
        }
    }

    @Test
    void testRandSeededSequence() {
        var ast = parser.parseOrThrow("rand(1)");

        // Get a sequence of values
        double[] sequence = new double[5];
        for (int i = 0; i < 5; i++) {
            sequence[i] = ast.accept(evaluator);
        }

        // All values should be different (extremely unlikely to be same)
        Set<Double> uniqueValues = new HashSet<>();
        for (double value : sequence) {
            uniqueValues.add(value);
        }
        assertThat(uniqueValues.size()).isEqualTo(5);
    }

    @Test
    void testThreadSafety() throws InterruptedException {
        // Test that rand(0) is thread-safe
        var ast = parser.parseOrThrow("rand(0)");

        Thread[] threads = new Thread[10];
        Set<Double>[] results = new Set[10];

        for (int i = 0; i < 10; i++) {
            final int index = i;
            results[i] = new HashSet<>();
            threads[i] = new Thread(() -> {
                for (int j = 0; j < 100; j++) {
                    double value = ast.accept(evaluator);
                    results[index].add(value);
                }
            });
        }

        // Start all threads
        for (Thread thread : threads) {
            thread.start();
        }

        // Wait for all threads
        for (Thread thread : threads) {
            thread.join();
        }

        // Each thread should have gotten different values
        for (Set<Double> result : results) {
            assertThat(result.size()).isGreaterThan(90);
        }
    }

    @Test
    void testSgrandAffectsSubsequentRandCalls() {
        // Set seed
        var sgrandAst = parser.parseOrThrow("sgrand(42)");
        sgrandAst.accept(evaluator);

        // Now rand(42) should continue the same sequence
        var randAst = parser.parseOrThrow("rand(42)");
        double value1 = randAst.accept(evaluator);
        double value2 = randAst.accept(evaluator);

        // Reset and verify same sequence
        context = new EvaluationContext();
        RandomFunctions.registerAll(context);
        evaluator = new Evaluator(context);

        sgrandAst = parser.parseOrThrow("sgrand(42)");
        sgrandAst.accept(evaluator);

        randAst = parser.parseOrThrow("rand(42)");
        double newValue1 = randAst.accept(evaluator);
        double newValue2 = randAst.accept(evaluator);

        assertThat(value1).isEqualTo(newValue1);
        assertThat(value2).isEqualTo(newValue2);
    }
}
