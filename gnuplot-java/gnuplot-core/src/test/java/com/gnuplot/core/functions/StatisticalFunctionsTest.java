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

class StatisticalFunctionsTest {

    private ExpressionParser parser;
    private Evaluator evaluator;
    private EvaluationContext context;

    @BeforeEach
    void setUp() {
        parser = new ExpressionParser();
        context = new EvaluationContext();
        StatisticalFunctions.registerAll(context);
        evaluator = new Evaluator(context);
    }

    @ParameterizedTest
    @CsvSource({
            "norm(0), 0.5",
            "norm(1), 0.8413",
            "norm(2), 0.9772",
            "norm(-1), 0.1587",
            "norm(3), 0.9987"
    })
    void testNormalCDF(String expression, double expected) {
        var ast = parser.parseOrThrow(expression);
        var result = ast.accept(evaluator);
        assertThat(result).isCloseTo(expected, within(1e-3));
    }

    @ParameterizedTest
    @CsvSource({
            "invnorm(0.5), 0",
            "invnorm(0.8413), 1",
            "invnorm(0.9772), 2",
            "invnorm(0.1587), -1"
    })
    void testInverseNormalCDF(String expression, double expected) {
        var ast = parser.parseOrThrow(expression);
        var result = ast.accept(evaluator);
        assertThat(result).isCloseTo(expected, within(1e-2));
    }

    @Test
    void testNormAndInvnormAreInverses() {
        // invnorm(norm(x)) = x
        var normAst = parser.parseOrThrow("norm(1.5)");
        var normResult = normAst.accept(evaluator);
        context.setVariable("normVal", normResult);

        var invnormAst = parser.parseOrThrow("invnorm(normVal)");
        var result = invnormAst.accept(evaluator);

        assertThat(result).isCloseTo(1.5, within(1e-10));
    }

    @Test
    void testNormProperties() {
        // norm(0) should be 0.5 (median)
        var ast1 = parser.parseOrThrow("norm(0)");
        assertThat(ast1.accept(evaluator)).isCloseTo(0.5, within(1e-10));

        // norm(-x) + norm(x) should be 1 (symmetry)
        var normPos = parser.parseOrThrow("norm(1)");
        var normNeg = parser.parseOrThrow("norm(-1)");
        double posResult = normPos.accept(evaluator);
        double negResult = normNeg.accept(evaluator);
        assertThat(posResult + negResult).isCloseTo(1.0, within(1e-10));
    }

    @Test
    void testStatisticalFunctionsAgainstTestOracle() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        try (InputStream is = getClass().getClassLoader()
                .getResourceAsStream("test-oracle/statistical_functions.json")) {

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
                        .isCloseTo(expected, within(1e-6));
            }
        }
    }
}