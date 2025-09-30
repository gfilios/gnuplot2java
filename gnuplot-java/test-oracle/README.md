# Test Oracle - Reference Data from C Gnuplot

This directory contains test oracle data extracted from the reference C implementation of gnuplot. This data serves as the "source of truth" for validating the Java implementation's mathematical accuracy.

---

## 📋 Overview

The test oracle approach ensures that our Java implementation produces the same results as the proven C implementation:

1. **Extract**: Run mathematical expressions through C gnuplot
2. **Store**: Save results as JSON files
3. **Compare**: Use as expected values in Java unit tests

This follows the test-driven development approach outlined in the modernization strategy.

---

## 📁 Directory Structure

```
test-oracle/
├── README.md                    # This file
├── extract-test-oracle.py       # Python script to extract test data
├── extract-math-functions.sh    # (Legacy) Bash script
└── data/                        # Generated JSON test data
    ├── basic_arithmetic.json
    ├── trigonometric.json
    ├── exponential_logarithmic.json
    ├── hyperbolic.json
    ├── special_functions.json
    ├── constants.json
    └── complex_expressions.json
```

---

## 🚀 Extracting Test Data

### Prerequisites

- gnuplot 6.0.3+ installed and available on PATH
- Python 3.6+ (for extraction script)

### Running the Extraction

```bash
cd gnuplot-java/test-oracle

# Run the Python extraction script
python3 extract-test-oracle.py
```

### What Gets Extracted

The script extracts results for **89 test cases** across **7 categories**:

| Category | Test Cases | Description |
|----------|------------|-------------|
| `basic_arithmetic` | 13 | Addition, subtraction, multiplication, division, power, modulo |
| `trigonometric` | 23 | sin, cos, tan, asin, acos, atan, atan2 |
| `exponential_logarithmic` | 17 | exp, log, log10, sqrt |
| `hyperbolic` | 9 | sinh, cosh, tanh |
| `special_functions` | 14 | abs, ceil, floor, int, sgn |
| `constants` | 5 | pi and related values |
| `complex_expressions` | 8 | Combined operations testing precedence and accuracy |

---

## 📄 JSON Format

Each JSON file follows this structure:

```json
{
  "function": "trigonometric",
  "gnuplot_version": "gnuplot 6.0.3 patchlevel 3",
  "tests": [
    {
      "expression": "sin(pi/2)",
      "result": 1.0
    },
    {
      "expression": "cos(0)",
      "result": 1.0
    }
  ]
}
```

### Fields

- `function`: Category name
- `gnuplot_version`: Version of gnuplot used for extraction
- `tests`: Array of test cases
  - `expression`: The mathematical expression
  - `result`: Expected numerical result from C gnuplot
  - `error` (optional): Boolean flag if expression should produce an error

---

## 🔧 Using Test Oracle in Java Tests

The test oracle data is automatically loaded by the Java test framework:

```java
import com.gnuplot.core.oracle.TestOracle;
import com.gnuplot.core.oracle.FunctionTestData;
import com.gnuplot.core.oracle.TestCase;

// Get the test oracle instance
TestOracle oracle = TestOracle.getInstance();

// Get test data for a specific category
FunctionTestData trigTests = oracle.getTestData("trigonometric");

// Iterate through test cases
for (TestCase testCase : trigTests.tests()) {
    String expression = testCase.expression();
    double expected = testCase.getResult();

    // Your evaluation code here
    double actual = evaluator.evaluate(expression);

    // Assert with appropriate tolerance for floating point
    assertEquals(expected, actual, 1e-10);
}
```

---

## 🔄 When to Regenerate Test Data

Regenerate the test oracle data when:

1. **Gnuplot Version Update**: New version of C gnuplot is installed
2. **New Test Cases Added**: You add expressions to `extract-test-oracle.py`
3. **Bug Fix Verification**: Confirming C gnuplot behavior for edge cases

### Steps to Regenerate

```bash
# 1. Ensure latest gnuplot is installed
gnuplot --version

# 2. Run extraction script
cd gnuplot-java/test-oracle
python3 extract-test-oracle.py

# 3. Copy to test resources
cp data/*.json ../gnuplot-core/src/test/resources/oracle-data/

# 4. Commit the updated files
git add data/*.json
git add ../gnuplot-core/src/test/resources/oracle-data/*.json
git commit -m "chore(test): Update test oracle data"
```

---

## 📝 Adding New Test Cases

To add new test cases, edit `extract-test-oracle.py`:

```python
TEST_CASES = {
    "basic_arithmetic": [
        "2 + 3",
        "10 - 4",
        # Add your new test cases here
        "42 * 1.5",
    ],
    # ... other categories
}
```

Then run the extraction script to regenerate the JSON files.

---

## 🎯 Test Strategy

### Floating Point Tolerance

When comparing results, use appropriate tolerance for floating point arithmetic:

```java
// For most functions
assertEquals(expected, actual, 1e-10);

// For trigonometric functions near zero
assertTrue(Math.abs(expected - actual) < 1e-15);

// For large numbers
assertTrue(Math.abs(expected - actual) / Math.abs(expected) < 1e-10);
```

### Known Limitations

- **Floating Point Precision**: Some expressions may have tiny differences due to different IEEE 754 implementations
- **Special Values**: NaN, Infinity cases need special handling
- **Round-off Errors**: Functions like `sin(pi)` return very small numbers instead of exactly 0

---

## 📊 Coverage

Current test oracle coverage:

- ✅ Basic arithmetic operators (+, -, *, /, **, %)
- ✅ Trigonometric functions (sin, cos, tan, asin, acos, atan, atan2)
- ✅ Exponential and logarithmic (exp, log, log10, sqrt)
- ✅ Hyperbolic functions (sinh, cosh, tanh)
- ✅ Special functions (abs, ceil, floor, int, sgn)
- ✅ Mathematical constants (pi)
- ✅ Complex expressions with operator precedence

### Planned Additions

- ⏳ Statistical functions (mean, median, stddev)
- ⏳ Rounding and modulo variants
- ⏳ Complex number operations
- ⏳ Date/time functions
- ⏳ String manipulation functions

---

## 🐛 Troubleshooting

### "gnuplot command not found"

Ensure gnuplot is installed and on your PATH:

```bash
# macOS
brew install gnuplot

# Ubuntu/Debian
sudo apt-get install gnuplot

# Check installation
gnuplot --version
```

### "Test oracle file not found" in Java tests

Ensure JSON files are copied to test resources:

```bash
cp test-oracle/data/*.json gnuplot-core/src/test/resources/oracle-data/
```

### Results don't match C implementation

1. Check gnuplot version matches
2. Verify JSON file is not corrupted
3. Consider floating point tolerance needs adjustment

---

## 📚 References

- [Gnuplot Official Documentation](http://gnuplot.sourceforge.net/docs_6.0/gnuplot.pdf)
- [Modernization Strategy](../../MODERNIZATION_STRATEGY.md) - Test-driven approach section
- [Implementation Backlog](../../IMPLEMENTATION_BACKLOG.md) - Story 0.2.2

---

**Last Updated**: 2025-09-30
**Gnuplot Version**: 6.0.3 patchlevel 3
**Total Test Cases**: 89