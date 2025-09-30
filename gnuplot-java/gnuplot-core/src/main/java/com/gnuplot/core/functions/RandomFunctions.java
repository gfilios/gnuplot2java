package com.gnuplot.core.functions;

import com.gnuplot.core.evaluator.EvaluationContext;
import com.gnuplot.core.evaluator.MathFunction;

import java.util.Random;
import java.util.WeakHashMap;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Random number generation functions.
 *
 * <p>Provides implementations for:
 * <ul>
 *   <li>rand(x) - Uniform random number in [0,1), with optional seed</li>
 *   <li>sgrand(seed) - Set the random seed (stored in context)</li>
 * </ul>
 *
 * <p>Thread-safe implementation using ThreadLocalRandom for rand(0),
 * and per-context Random instances for seeded generators.
 */
public final class RandomFunctions {

    private static final String RANDOM_SEED_KEY = "__random_seed__";

    // Store Random instances per context (weak references to allow GC)
    private static final WeakHashMap<EvaluationContext, Random> randomGenerators = new WeakHashMap<>();

    private RandomFunctions() {
        // Utility class
    }

    /**
     * Registers all random functions with the given evaluation context.
     *
     * @param context the evaluation context to register functions with
     */
    public static void registerAll(EvaluationContext context) {
        // rand(x) - Returns uniform random in [0,1)
        // If x != 0, uses x as seed and creates deterministic sequence
        // If x == 0, uses ThreadLocalRandom (thread-safe, non-reproducible)
        context.registerFunction("rand", MathFunction.withArgCount(1, args -> {
            double seed = args[0];

            if (seed == 0.0) {
                // Use ThreadLocalRandom for non-seeded random (thread-safe)
                return ThreadLocalRandom.current().nextDouble();
            } else {
                // Use seeded Random for reproducible sequences
                Random rng = getOrCreateRandom(context, (long) seed);
                return rng.nextDouble();
            }
        }));

        // sgrand(seed) - Set random seed, returns previous seed
        // This affects subsequent rand() calls in this context
        context.registerFunction("sgrand", MathFunction.withArgCount(1, args -> {
            long newSeed = (long) args[0];

            // Get previous seed (or 0 if not set)
            double previousSeed = 0.0;
            try {
                previousSeed = context.getVariable(RANDOM_SEED_KEY);
            } catch (Exception e) {
                // Variable doesn't exist yet, use 0
            }

            // Create new Random with new seed
            synchronized (randomGenerators) {
                Random rng = new Random(newSeed);
                randomGenerators.put(context, rng);
                context.setVariable(RANDOM_SEED_KEY, (double) newSeed);
            }

            return previousSeed;
        }));
    }

    /**
     * Gets or creates a Random instance for the given seed.
     * If a Random already exists with the same seed, reuses it.
     * Otherwise creates a new one.
     */
    private static Random getOrCreateRandom(EvaluationContext context, long seed) {
        synchronized (randomGenerators) {
            // Check if we have a seeded generator
            try {
                double currentSeed = context.getVariable(RANDOM_SEED_KEY);
                if ((long) currentSeed == seed) {
                    // Same seed, reuse existing generator
                    Random existing = randomGenerators.get(context);
                    if (existing != null) {
                        return existing;
                    }
                }
            } catch (Exception e) {
                // Variable doesn't exist yet, create new
            }

            // Different seed or no seed, create new generator
            Random rng = new Random(seed);
            randomGenerators.put(context, rng);
            context.setVariable(RANDOM_SEED_KEY, (double) seed);
            return rng;
        }
    }
}
