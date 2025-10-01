package com.gnuplot.core.oracle;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for the Test Oracle data loading.
 */
@DisplayName("TestOracle")
class TestOracleTest {

    @Test
    @DisplayName("should load all test oracle categories")
    void shouldLoadAllCategories() {
        // Given
        TestOracle oracle = TestOracle.getInstance();

        // When
        var categories = oracle.getCategories();

        // Then
        assertThat(categories).containsExactlyInAnyOrder(
                "basic_arithmetic",
                "trigonometric",
                "exponential_logarithmic",
                "hyperbolic",
                "special_functions",
                "constants",
                "complex_expressions"
        );
    }

    @Test
    @DisplayName("should load 89 total test cases")
    void shouldLoad89TestCases() {
        // Given
        TestOracle oracle = TestOracle.getInstance();

        // When
        int totalTests = oracle.getTotalTestCount();

        // Then
        assertThat(totalTests).isEqualTo(89);
    }

    @Test
    @DisplayName("should load basic arithmetic test cases")
    void shouldLoadBasicArithmetic() {
        // Given
        TestOracle oracle = TestOracle.getInstance();

        // When
        TestOracle.FunctionTestData data = oracle.getTestData("basic_arithmetic");

        // Then
        assertThat(data.functionName()).isEqualTo("basic_arithmetic");
        assertThat(data.gnuplotVersion()).contains("gnuplot 6.0.3");
        assertThat(data.tests()).hasSizeGreaterThan(10);
    }

    @Test
    @DisplayName("should provide correct test case data")
    void shouldProvideCorrectTestCaseData() {
        // Given
        TestOracle oracle = TestOracle.getInstance();
        TestOracle.FunctionTestData data = oracle.getTestData("basic_arithmetic");

        // When
        TestCase firstTest = data.tests().get(0);

        // Then
        assertThat(firstTest.expression()).isEqualTo("2 + 3");
        assertThat(firstTest.getResult()).isEqualTo(5.0);
        assertThat(firstTest.isError()).isFalse();
    }

    @Test
    @DisplayName("should load trigonometric test cases with correct values")
    void shouldLoadTrigonometricWithCorrectValues() {
        // Given
        TestOracle oracle = TestOracle.getInstance();
        TestOracle.FunctionTestData data = oracle.getTestData("trigonometric");

        // When
        TestCase sinZero = data.tests().stream()
                .filter(tc -> tc.expression().equals("sin(0)"))
                .findFirst()
                .orElseThrow();

        // Then
        assertThat(sinZero.getResult()).isEqualTo(0.0);
    }

    @Test
    @DisplayName("should load pi constant with correct value")
    void shouldLoadPiWithCorrectValue() {
        // Given
        TestOracle oracle = TestOracle.getInstance();
        TestOracle.FunctionTestData data = oracle.getTestData("constants");

        // When
        TestCase piTest = data.tests().stream()
                .filter(tc -> tc.expression().equals("pi"))
                .findFirst()
                .orElseThrow();

        // Then
        assertThat(piTest.getResult()).isCloseTo(Math.PI, org.assertj.core.data.Offset.offset(1e-10));
    }
}