package com.gnuplot.core.types;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.*;

class ComplexNumberTest {

    @Test
    void testConstants() {
        assertThat(ComplexNumber.ZERO).isEqualTo(new ComplexNumber(0, 0));
        assertThat(ComplexNumber.ONE).isEqualTo(new ComplexNumber(1, 0));
        assertThat(ComplexNumber.I).isEqualTo(new ComplexNumber(0, 1));
    }

    @Test
    void testFromReal() {
        ComplexNumber z = ComplexNumber.fromReal(3.14);
        assertThat(z.real()).isEqualTo(3.14);
        assertThat(z.imag()).isEqualTo(0.0);
        assertThat(z.isReal()).isTrue();
    }

    @Test
    void testIsReal() {
        assertThat(new ComplexNumber(5, 0).isReal()).isTrue();
        assertThat(new ComplexNumber(5, 0.0).isReal()).isTrue();
        assertThat(new ComplexNumber(5, 1e-10).isReal()).isFalse();
    }

    @Test
    void testIsImaginary() {
        assertThat(new ComplexNumber(0, 5).isImaginary()).isTrue();
        assertThat(new ComplexNumber(0.0, 5).isImaginary()).isTrue();
        assertThat(new ComplexNumber(1e-10, 5).isImaginary()).isFalse();
    }

    @ParameterizedTest
    @CsvSource({
            "3, 4, 5.0",
            "0, 0, 0.0",
            "1, 0, 1.0",
            "0, 1, 1.0",
            "-3, -4, 5.0"
    })
    void testAbs(double real, double imag, double expected) {
        ComplexNumber z = new ComplexNumber(real, imag);
        assertThat(z.abs()).isCloseTo(expected, within(1e-10));
    }

    @Test
    void testArg() {
        assertThat(new ComplexNumber(1, 0).arg()).isCloseTo(0, within(1e-10));
        assertThat(new ComplexNumber(0, 1).arg()).isCloseTo(Math.PI / 2, within(1e-10));
        assertThat(new ComplexNumber(-1, 0).arg()).isCloseTo(Math.PI, within(1e-10));
        assertThat(new ComplexNumber(0, -1).arg()).isCloseTo(-Math.PI / 2, within(1e-10));
    }

    @Test
    void testAdd() {
        ComplexNumber z1 = new ComplexNumber(1, 2);
        ComplexNumber z2 = new ComplexNumber(3, 4);
        ComplexNumber result = z1.add(z2);

        assertThat(result.real()).isEqualTo(4);
        assertThat(result.imag()).isEqualTo(6);
    }

    @Test
    void testSubtract() {
        ComplexNumber z1 = new ComplexNumber(5, 7);
        ComplexNumber z2 = new ComplexNumber(2, 3);
        ComplexNumber result = z1.subtract(z2);

        assertThat(result.real()).isEqualTo(3);
        assertThat(result.imag()).isEqualTo(4);
    }

    @Test
    void testMultiply() {
        ComplexNumber z1 = new ComplexNumber(1, 2);
        ComplexNumber z2 = new ComplexNumber(3, 4);
        ComplexNumber result = z1.multiply(z2);

        // (1+2i)(3+4i) = 3+4i+6i+8i^2 = 3+10i-8 = -5+10i
        assertThat(result.real()).isCloseTo(-5, within(1e-10));
        assertThat(result.imag()).isCloseTo(10, within(1e-10));
    }

    @Test
    void testMultiplyByI() {
        ComplexNumber z = new ComplexNumber(3, 4);
        ComplexNumber result = z.multiply(ComplexNumber.I);

        // (3+4i)*i = 3i+4i^2 = 3i-4 = -4+3i
        assertThat(result.real()).isCloseTo(-4, within(1e-10));
        assertThat(result.imag()).isCloseTo(3, within(1e-10));
    }

    @Test
    void testDivide() {
        ComplexNumber z1 = new ComplexNumber(1, 2);
        ComplexNumber z2 = new ComplexNumber(3, 4);
        ComplexNumber result = z1.divide(z2);

        // (1+2i)/(3+4i) = (1+2i)(3-4i)/(9+16) = (3-4i+6i-8i^2)/25 = (11+2i)/25
        assertThat(result.real()).isCloseTo(11.0 / 25, within(1e-10));
        assertThat(result.imag()).isCloseTo(2.0 / 25, within(1e-10));
    }

    @Test
    void testDivideByZero() {
        ComplexNumber z = new ComplexNumber(1, 2);
        assertThatThrownBy(() -> z.divide(ComplexNumber.ZERO))
                .isInstanceOf(ArithmeticException.class)
                .hasMessageContaining("Division by zero");
    }

    @Test
    void testNegate() {
        ComplexNumber z = new ComplexNumber(3, -4);
        ComplexNumber result = z.negate();

        assertThat(result.real()).isEqualTo(-3);
        assertThat(result.imag()).isEqualTo(4);
    }

    @Test
    void testConjugate() {
        ComplexNumber z = new ComplexNumber(3, 4);
        ComplexNumber result = z.conjugate();

        assertThat(result.real()).isEqualTo(3);
        assertThat(result.imag()).isEqualTo(-4);
    }

    @Test
    void testConjugateProperty() {
        // z * conj(z) = |z|^2
        ComplexNumber z = new ComplexNumber(3, 4);
        ComplexNumber product = z.multiply(z.conjugate());

        assertThat(product.real()).isCloseTo(25, within(1e-10)); // 3^2 + 4^2
        assertThat(product.imag()).isCloseTo(0, within(1e-10));
    }

    @Test
    void testPow() {
        ComplexNumber z = new ComplexNumber(2, 0);
        ComplexNumber result = z.pow(3);

        assertThat(result.real()).isCloseTo(8, within(1e-10));
        assertThat(result.imag()).isCloseTo(0, within(1e-10));
    }

    @Test
    void testPowComplex() {
        ComplexNumber z = new ComplexNumber(1, 1);
        ComplexNumber result = z.pow(2);

        // (1+i)^2 = 1+2i+i^2 = 1+2i-1 = 2i
        assertThat(result.real()).isCloseTo(0, within(1e-10));
        assertThat(result.imag()).isCloseTo(2, within(1e-10));
    }

    @Test
    void testSqrt() {
        ComplexNumber z = new ComplexNumber(0, 4);
        ComplexNumber result = z.sqrt();

        // sqrt(4i) should give us a number that when squared equals 4i
        ComplexNumber squared = result.multiply(result);
        assertThat(squared.real()).isCloseTo(0, within(1e-10));
        assertThat(squared.imag()).isCloseTo(4, within(1e-10));
    }

    @Test
    void testSqrtOfNegativeOne() {
        ComplexNumber z = new ComplexNumber(-1, 0);
        ComplexNumber result = z.sqrt();

        // sqrt(-1) = i
        assertThat(result.real()).isCloseTo(0, within(1e-10));
        assertThat(result.imag()).isCloseTo(1, within(1e-10));
    }

    @Test
    void testExp() {
        ComplexNumber z = new ComplexNumber(0, Math.PI);
        ComplexNumber result = z.exp();

        // e^(iπ) = -1
        assertThat(result.real()).isCloseTo(-1, within(1e-10));
        assertThat(result.imag()).isCloseTo(0, within(1e-10));
    }

    @Test
    void testExpReal() {
        ComplexNumber z = new ComplexNumber(1, 0);
        ComplexNumber result = z.exp();

        assertThat(result.real()).isCloseTo(Math.E, within(1e-10));
        assertThat(result.imag()).isCloseTo(0, within(1e-10));
    }

    @Test
    void testLog() {
        ComplexNumber z = new ComplexNumber(Math.E, 0);
        ComplexNumber result = z.log();

        assertThat(result.real()).isCloseTo(1, within(1e-10));
        assertThat(result.imag()).isCloseTo(0, within(1e-10));
    }

    @Test
    void testLogOfI() {
        ComplexNumber result = ComplexNumber.I.log();

        // log(i) = log(e^(iπ/2)) = iπ/2
        assertThat(result.real()).isCloseTo(0, within(1e-10));
        assertThat(result.imag()).isCloseTo(Math.PI / 2, within(1e-10));
    }

    @Test
    void testToReal() {
        ComplexNumber z = new ComplexNumber(3.14, 0);
        assertThat(z.toReal()).isEqualTo(3.14);
    }

    @Test
    void testToRealThrowsOnImaginary() {
        ComplexNumber z = new ComplexNumber(3, 4);
        assertThatThrownBy(z::toReal)
                .isInstanceOf(ArithmeticException.class)
                .hasMessageContaining("Cannot convert complex number with imaginary part to real");
    }

    @Test
    void testToString() {
        assertThat(new ComplexNumber(3, 0).toString()).contains("3");
        assertThat(new ComplexNumber(0, 4).toString()).contains("4").contains("i");
        assertThat(new ComplexNumber(3, 4).toString()).contains("3").contains("4");
    }

    @Test
    void testEulerIdentity() {
        // e^(iπ) + 1 = 0
        ComplexNumber eiPi = new ComplexNumber(0, Math.PI).exp();
        ComplexNumber result = eiPi.add(ComplexNumber.ONE);

        assertThat(result.real()).isCloseTo(0, within(1e-10));
        assertThat(result.imag()).isCloseTo(0, within(1e-10));
    }
}
