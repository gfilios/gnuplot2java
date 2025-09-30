package com.gnuplot.core.functions;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gnuplot.core.evaluator.EvaluationContext;
import com.gnuplot.core.evaluator.Evaluator;
import com.gnuplot.core.parser.ExpressionParser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.io.IOException;
import java.io.InputStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

class ErrorFunctionsTest {

    private ExpressionParser parser;
    private Evaluator evaluator;
    private EvaluationContext context;

    @BeforeEach
    void setUp() {
        parser = new ExpressionParser();
        context = new EvaluationContext();
        ErrorFunctions.registerAll(context);
        evaluator = new Evaluator(context);
    }

    @ParameterizedTest
    @CsvSource({
            "erf(0), 0",
            "erf(0.5), 0.5205",
            "erf(1), 0.8427",
            "erf(2), 0.9953",
            "erf(-1), -0.8427"
    })
    void testErrorFunction(String expression, double expected) {
        var ast = parser.parseOrThrow(expression);
        var result = ast.accept(evaluator);
        assertThat(result).isCloseTo(expected, within(1e-3));
    }

    @ParameterizedTest
    @CsvSource({
            "erfc(0), 1",
            "erfc(0.5), 0.4795",
            "erfc(1), 0.1573",
            "erfc(2), 0.0047"
    })
    void testComplementaryErrorFunction(String expression, double expected) {
        var ast = parser.parseOrThrow(expression);
        var result = ast.accept(evaluator);
        assertThat(result).isCloseTo(expected, within(1e-3));
    }

    @ParameterizedTest
    @CsvSource({
            "inverf(0), 0",
            "inverf(0.5), 0.4769",
            "inverf(0.9), 1.1631",
            "inverf(-0.5), -0.4769"
    })
    void testInverseErrorFunction(String expression, double expected) {
        var ast = parser.parseOrThrow(expression);
        var result = ast.accept(evaluator);
        assertThat(result).isCloseTo(expected, within(1e-3));
    }

    @Test
    void testErrorFunctionProperties() {
        // erf(x) + erfc(x) = 1
        var erfAst = parser.parseOrThrow("erf(1.5)");
        var erfcAst = parser.parseOrThrow("erfc(1.5)");
        var erfResult = erfAst.accept(evaluator);
        var erfcResult = erfcAst.accept(evaluator);
        assertThat(erfResult + erfcResult).isCloseTo(1.0, within(1e-10));

        // erf(-x) = -erf(x) (odd function)
        var erfPosAst = parser.parseOrThrow("erf(1.5)");
        var erfNegAst = parser.parseOrThrow("erf(-1.5)");
        var erfPosResult = erfPosAst.accept(evaluator);
        var erfNegResult = erfNegAst.accept(evaluator);
        assertThat(erfNegResult).isCloseTo(-erfPosResult, within(1e-10));
    }

    @Test
    void testInverseProperties() {
        // inverf(erf(x)) = x
        var erfAst = parser.parseOrThrow("erf(0.5)");
        var erfResult = erfAst.accept(evaluator);
        context.setVariable("erfVal", erfResult);
        var inverfAst = parser.parseOrThrow("inverf(erfVal)");
        var result = inverfAst.accept(evaluator);
        assertThat(result).isCloseTo(0.5, within(1e-10));
    }

    @Test
    void testErrorFunctionsAgainstTestOracle() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        try (InputStream is = getClass().getClassLoader()
                .getResourceAsStream("test-oracle/error_functions.json")) {

            assertThat(is).isNotNull();
            JsonNode root = mapper.readTree(is);
            JsonNode tests = root.get("tests");

            for (JsonNode test : tests) {
                String expression = test.get("expression").asText();
                double expected = test.get("result").asDouble();

                var ast = parser.parseOrThrow(expression);
                var result = ast.accept(evaluator);

                assertThat(result)
                        .as("Expression: %s", expression)
                        .isCloseTo(expected, within(1e-10));
            }
        }
    }
}