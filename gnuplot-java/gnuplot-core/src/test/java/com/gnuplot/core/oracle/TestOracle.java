package com.gnuplot.core.oracle;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Test oracle that loads expected results from the C gnuplot implementation.
 * This serves as the "source of truth" for mathematical function evaluation.
 */
public class TestOracle {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static TestOracle instance;

    private final Map<String, FunctionTestData> testData = new HashMap<>();

    private TestOracle() {
        loadAllTestData();
    }

    /**
     * Gets the singleton instance of the test oracle.
     */
    public static synchronized TestOracle getInstance() {
        if (instance == null) {
            instance = new TestOracle();
        }
        return instance;
    }

    /**
     * Loads all test oracle data from JSON files.
     */
    private void loadAllTestData() {
        String[] categories = {
                "basic_arithmetic",
                "trigonometric",
                "exponential_logarithmic",
                "hyperbolic",
                "special_functions",
                "constants",
                "complex_expressions"
        };

        for (String category : categories) {
            try {
                FunctionTestData data = loadTestData(category);
                testData.put(category, data);
            } catch (IOException e) {
                throw new RuntimeException("Failed to load test oracle data for: " + category, e);
            }
        }
    }

    /**
     * Loads test data for a specific function category.
     */
    private FunctionTestData loadTestData(String category) throws IOException {
        String resourcePath = "/oracle-data/" + category + ".json";
        InputStream is = getClass().getResourceAsStream(resourcePath);

        if (is == null) {
            throw new IOException("Test oracle file not found: " + resourcePath);
        }

        JsonNode root = OBJECT_MAPPER.readTree(is);
        String functionName = root.get("function").asText();
        String gnuplotVersion = root.get("gnuplot_version").asText();

        List<TestCase> testCases = new ArrayList<>();
        JsonNode testsNode = root.get("tests");

        for (JsonNode testNode : testsNode) {
            String expression = testNode.get("expression").asText();
            JsonNode resultNode = testNode.get("result");
            JsonNode errorNode = testNode.get("error");

            TestCase testCase;
            if (errorNode != null && errorNode.asBoolean()) {
                testCase = TestCase.error(expression);
            } else if (resultNode != null && !resultNode.isNull()) {
                Double result = resultNode.asDouble();
                testCase = new TestCase(expression, result);
            } else {
                testCase = TestCase.error(expression);
            }

            testCases.add(testCase);
        }

        return new FunctionTestData(functionName, gnuplotVersion, testCases);
    }

    /**
     * Gets test data for a specific function category.
     */
    public FunctionTestData getTestData(String category) {
        FunctionTestData data = testData.get(category);
        if (data == null) {
            throw new IllegalArgumentException("Unknown category: " + category);
        }
        return data;
    }

    /**
     * Gets all available categories.
     */
    public List<String> getCategories() {
        return new ArrayList<>(testData.keySet());
    }

    /**
     * Gets the total number of test cases across all categories.
     */
    public int getTotalTestCount() {
        return testData.values().stream()
                .mapToInt(data -> data.tests().size())
                .sum();
    }

    /**
     * Container for function test data from the oracle.
     */
    public record FunctionTestData(
            String functionName,
            String gnuplotVersion,
            List<TestCase> tests
    ) {
    }
}