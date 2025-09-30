package com.gnuplot.core.types;

/**
 * Represents a complex number with real and imaginary parts.
 *
 * <p>Immutable value type for complex number arithmetic.
 * Follows gnuplot's complex number representation: {real, imag}
 *
 * <p>Supports standard complex operations:
 * <ul>
 *   <li>Arithmetic: +, -, *, /</li>
 *   <li>Properties: abs, arg, real, imag</li>
 *   <li>Functions: conj, sqrt, exp, log, sin, cos, etc.</li>
 * </ul>
 */
public record ComplexNumber(double real, double imag) {

    /**
     * Real unit: 1 + 0i
     */
    public static final ComplexNumber ONE = new ComplexNumber(1, 0);

    /**
     * Zero: 0 + 0i
     */
    public static final ComplexNumber ZERO = new ComplexNumber(0, 0);

    /**
     * Imaginary unit: 0 + 1i
     */
    public static final ComplexNumber I = new ComplexNumber(0, 1);

    /**
     * Creates a complex number from a real value.
     *
     * @param real the real part
     * @return complex number with zero imaginary part
     */
    public static ComplexNumber fromReal(double real) {
        return new ComplexNumber(real, 0);
    }

    /**
     * Checks if this complex number is purely real (imaginary part is zero).
     *
     * @return true if imaginary part is zero
     */
    public boolean isReal() {
        return imag == 0.0;
    }

    /**
     * Checks if this complex number is purely imaginary (real part is zero).
     *
     * @return true if real part is zero
     */
    public boolean isImaginary() {
        return real == 0.0;
    }

    /**
     * Returns the magnitude (absolute value) of this complex number.
     * |z| = sqrt(real^2 + imag^2)
     *
     * @return the magnitude
     */
    public double abs() {
        return Math.hypot(real, imag);
    }

    /**
     * Returns the argument (phase angle) of this complex number in radians.
     * arg(z) = atan2(imag, real)
     *
     * @return the argument in radians, range [-π, π]
     */
    public double arg() {
        return Math.atan2(imag, real);
    }

    /**
     * Adds another complex number to this one.
     *
     * @param other the complex number to add
     * @return the sum
     */
    public ComplexNumber add(ComplexNumber other) {
        return new ComplexNumber(real + other.real, imag + other.imag);
    }

    /**
     * Subtracts another complex number from this one.
     *
     * @param other the complex number to subtract
     * @return the difference
     */
    public ComplexNumber subtract(ComplexNumber other) {
        return new ComplexNumber(real - other.real, imag - other.imag);
    }

    /**
     * Multiplies this complex number by another.
     * (a+bi)(c+di) = (ac-bd) + (ad+bc)i
     *
     * @param other the complex number to multiply by
     * @return the product
     */
    public ComplexNumber multiply(ComplexNumber other) {
        double newReal = real * other.real - imag * other.imag;
        double newImag = real * other.imag + imag * other.real;
        return new ComplexNumber(newReal, newImag);
    }

    /**
     * Divides this complex number by another.
     * (a+bi)/(c+di) = [(ac+bd) + (bc-ad)i] / (c^2+d^2)
     *
     * @param other the complex number to divide by
     * @return the quotient
     * @throws ArithmeticException if dividing by zero
     */
    public ComplexNumber divide(ComplexNumber other) {
        double denominator = other.real * other.real + other.imag * other.imag;
        if (denominator == 0.0) {
            throw new ArithmeticException("Division by zero complex number");
        }
        double newReal = (real * other.real + imag * other.imag) / denominator;
        double newImag = (imag * other.real - real * other.imag) / denominator;
        return new ComplexNumber(newReal, newImag);
    }

    /**
     * Returns the negation of this complex number.
     *
     * @return -this
     */
    public ComplexNumber negate() {
        return new ComplexNumber(-real, -imag);
    }

    /**
     * Returns the complex conjugate.
     * conj(a+bi) = a-bi
     *
     * @return the conjugate
     */
    public ComplexNumber conjugate() {
        return new ComplexNumber(real, -imag);
    }

    /**
     * Raises this complex number to a real power.
     *
     * @param exponent the exponent
     * @return this^exponent
     */
    public ComplexNumber pow(double exponent) {
        if (isReal() && real >= 0) {
            // Optimization for positive real numbers
            return fromReal(Math.pow(real, exponent));
        }
        // Use polar form: z^n = r^n * e^(i*n*θ)
        double r = abs();
        double theta = arg();
        double newR = Math.pow(r, exponent);
        double newTheta = exponent * theta;
        return new ComplexNumber(
                newR * Math.cos(newTheta),
                newR * Math.sin(newTheta)
        );
    }

    /**
     * Returns the square root of this complex number.
     *
     * @return sqrt(this)
     */
    public ComplexNumber sqrt() {
        double r = abs();
        double theta = arg();
        double sqrtR = Math.sqrt(r);
        double halfTheta = theta / 2;
        return new ComplexNumber(
                sqrtR * Math.cos(halfTheta),
                sqrtR * Math.sin(halfTheta)
        );
    }

    /**
     * Returns the exponential of this complex number.
     * exp(a+bi) = e^a * (cos(b) + i*sin(b))
     *
     * @return e^this
     */
    public ComplexNumber exp() {
        double expReal = Math.exp(real);
        return new ComplexNumber(
                expReal * Math.cos(imag),
                expReal * Math.sin(imag)
        );
    }

    /**
     * Returns the natural logarithm of this complex number.
     * log(a+bi) = log(|z|) + i*arg(z)
     *
     * @return ln(this)
     */
    public ComplexNumber log() {
        return new ComplexNumber(Math.log(abs()), arg());
    }

    /**
     * Converts to a double, throwing an exception if there's an imaginary part.
     *
     * @return the real part as a double
     * @throws ArithmeticException if the imaginary part is non-zero
     */
    public double toReal() {
        if (!isReal()) {
            throw new ArithmeticException("Cannot convert complex number with imaginary part to real: " + this);
        }
        return real;
    }

    @Override
    public String toString() {
        if (isReal()) {
            return String.valueOf(real);
        }
        if (isImaginary()) {
            return imag + "i";
        }
        if (imag < 0) {
            return String.format("{%g, %g}", real, imag);
        }
        return String.format("{%g, %g}", real, imag);
    }
}
