package com.gnuplot.core.evaluator;

/**
 * Represents a complex number with real and imaginary parts.
 * Provides operations needed for gnuplot mathematical functions.
 */
public final class Complex {
    private final double real;
    private final double imag;

    public static final Complex ZERO = new Complex(0, 0);
    public static final Complex ONE = new Complex(1, 0);
    public static final Complex I = new Complex(0, 1);

    public Complex(double real, double imag) {
        this.real = real;
        this.imag = imag;
    }

    public Complex(double real) {
        this(real, 0);
    }

    public double real() {
        return real;
    }

    public double imag() {
        return imag;
    }

    public boolean isReal() {
        return Math.abs(imag) < 1e-15;
    }

    public double abs() {
        return Math.hypot(real, imag);
    }

    public double arg() {
        return Math.atan2(imag, real);
    }

    public Complex conjugate() {
        return new Complex(real, -imag);
    }

    public Complex negate() {
        return new Complex(-real, -imag);
    }

    // Arithmetic operations
    public Complex add(Complex other) {
        return new Complex(real + other.real, imag + other.imag);
    }

    public Complex subtract(Complex other) {
        return new Complex(real - other.real, imag - other.imag);
    }

    public Complex multiply(Complex other) {
        return new Complex(
            real * other.real - imag * other.imag,
            real * other.imag + imag * other.real
        );
    }

    public Complex divide(Complex other) {
        double denom = other.real * other.real + other.imag * other.imag;
        if (denom == 0) {
            return new Complex(Double.NaN, Double.NaN);
        }
        return new Complex(
            (real * other.real + imag * other.imag) / denom,
            (imag * other.real - real * other.imag) / denom
        );
    }

    // Mathematical functions
    public static Complex sqrt(Complex z) {
        if (z.imag == 0 && z.real >= 0) {
            return new Complex(Math.sqrt(z.real), 0);
        }
        if (z.imag == 0 && z.real < 0) {
            // sqrt of negative real number
            return new Complex(0, Math.sqrt(-z.real));
        }
        double r = z.abs();
        double theta = z.arg();
        return new Complex(
            Math.sqrt(r) * Math.cos(theta / 2),
            Math.sqrt(r) * Math.sin(theta / 2)
        );
    }

    public static Complex exp(Complex z) {
        double expReal = Math.exp(z.real);
        return new Complex(
            expReal * Math.cos(z.imag),
            expReal * Math.sin(z.imag)
        );
    }

    public static Complex log(Complex z) {
        return new Complex(Math.log(z.abs()), z.arg());
    }

    public static Complex sin(Complex z) {
        // sin(a + bi) = sin(a)cosh(b) + i*cos(a)sinh(b)
        return new Complex(
            Math.sin(z.real) * Math.cosh(z.imag),
            Math.cos(z.real) * Math.sinh(z.imag)
        );
    }

    public static Complex cos(Complex z) {
        // cos(a + bi) = cos(a)cosh(b) - i*sin(a)sinh(b)
        return new Complex(
            Math.cos(z.real) * Math.cosh(z.imag),
            -Math.sin(z.real) * Math.sinh(z.imag)
        );
    }

    public static Complex tan(Complex z) {
        return sin(z).divide(cos(z));
    }

    public static Complex atan(Complex z) {
        // atan(z) = (i/2) * ln((i+z)/(i-z))
        Complex i = I;
        Complex numerator = i.add(z);
        Complex denominator = i.subtract(z);
        Complex logArg = numerator.divide(denominator);
        Complex logResult = log(logArg);
        return i.divide(new Complex(2, 0)).multiply(logResult);
    }

    public static Complex pow(Complex base, Complex exponent) {
        if (base.real == 0 && base.imag == 0) {
            if (exponent.real > 0) {
                return ZERO;
            } else {
                return new Complex(Double.NaN, Double.NaN);
            }
        }
        // z^w = exp(w * ln(z))
        return exp(exponent.multiply(log(base)));
    }

    public static Complex pow(Complex base, double exponent) {
        return pow(base, new Complex(exponent, 0));
    }

    @Override
    public String toString() {
        if (imag == 0) {
            return String.valueOf(real);
        } else if (real == 0) {
            return imag + "i";
        } else if (imag > 0) {
            return real + " + " + imag + "i";
        } else {
            return real + " - " + (-imag) + "i";
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Complex)) return false;
        Complex other = (Complex) obj;
        return Double.compare(real, other.real) == 0
            && Double.compare(imag, other.imag) == 0;
    }

    @Override
    public int hashCode() {
        return Double.hashCode(real) * 31 + Double.hashCode(imag);
    }
}
