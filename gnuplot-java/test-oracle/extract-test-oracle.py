#!/usr/bin/env python3
"""
Extract mathematical function test data from gnuplot to use as test oracle.
This generates JSON files with expected results from the reference C implementation.
"""

import subprocess
import json
import os
from pathlib import Path

# Test cases organized by category
TEST_CASES = {
    "basic_arithmetic": [
        "2 + 3",
        "10 - 4",
        "5 * 6",
        "20 / 4",
        "2 ** 8",
        "17 % 5",
        "2 + 3 * 4",
        "(2 + 3) * 4",
        "-5",
        "+7",
        "3.14159",
        "1e10",
        "1.5e-5",
    ],
    "trigonometric": [
        "sin(0)",
        "sin(pi/2)",
        "sin(pi)",
        "sin(3*pi/2)",
        "sin(2*pi)",
        "cos(0)",
        "cos(pi/2)",
        "cos(pi)",
        "tan(0)",
        "tan(pi/4)",
        "tan(pi/6)",
        "asin(0)",
        "asin(0.5)",
        "asin(1)",
        "acos(0)",
        "acos(0.5)",
        "acos(1)",
        "atan(0)",
        "atan(1)",
        "atan(sqrt(3))",
        "atan2(1, 1)",
        "atan2(1, 0)",
        "atan2(0, 1)",
    ],
    "exponential_logarithmic": [
        "exp(0)",
        "exp(1)",
        "exp(2)",
        "exp(-1)",
        "log(1)",
        "log(exp(1))",
        "log(10)",
        "log(100)",
        "log10(1)",
        "log10(10)",
        "log10(100)",
        "log10(1000)",
        "sqrt(0)",
        "sqrt(1)",
        "sqrt(4)",
        "sqrt(2)",
        "sqrt(9)",
    ],
    "hyperbolic": [
        "sinh(0)",
        "sinh(1)",
        "sinh(-1)",
        "cosh(0)",
        "cosh(1)",
        "cosh(-1)",
        "tanh(0)",
        "tanh(1)",
        "tanh(-1)",
    ],
    "special_functions": [
        "abs(-5)",
        "abs(0)",
        "abs(3.14)",
        "ceil(3.14)",
        "ceil(-3.14)",
        "ceil(5)",
        "floor(3.14)",
        "floor(-3.14)",
        "floor(5)",
        "int(3.14)",
        "int(-3.14)",
        "sgn(-5)",
        "sgn(0)",
        "sgn(5)",
    ],
    "constants": [
        "pi",
        "2*pi",
        "pi/2",
        "pi/4",
        "pi/6",
    ],
    "complex_expressions": [
        "sin(pi/4) ** 2 + cos(pi/4) ** 2",
        "exp(log(10))",
        "log(exp(5))",
        "sqrt(4 ** 2)",
        "abs(sin(pi))",
        "2 * sin(pi/6)",
        "sqrt(3) / 2",
        "1 / sqrt(2)",
    ],
}


def evaluate_expression(expression):
    """Evaluate an expression using gnuplot and return the result."""
    try:
        # Create a gnuplot command that prints the result
        # Use 'set print "-"' to ensure output goes to stdout
        gnuplot_commands = f"set print \"-\"; print {expression}"

        result = subprocess.run(
            ["gnuplot", "-e", gnuplot_commands],
            capture_output=True,
            text=True,
            timeout=5
        )

        if result.returncode != 0:
            stderr = result.stderr.strip()
            if stderr:
                return {"error": stderr}
            # Sometimes gnuplot doesn't set return code but fails
            return {"error": "evaluation failed"}

        # Parse the output - gnuplot prints to stdout
        output = result.stdout.strip()

        if not output:
            return {"error": "no output"}

        try:
            # Try to convert to float
            value = float(output)
            return value
        except ValueError:
            # If it fails, check if it's undefined or error
            if "undefined" in output.lower() or "error" in output.lower():
                return {"error": output}
            # Otherwise return as string
            return output

    except subprocess.TimeoutExpired:
        return {"error": "timeout"}
    except Exception as e:
        return {"error": str(e)}


def extract_test_data(category, expressions):
    """Extract test data for a category of expressions."""
    print(f"Extracting: {category}")

    tests = []
    for expr in expressions:
        result = evaluate_expression(expr)
        test_case = {
            "expression": expr,
            "result": result
        }

        # Add error flag if result is an error
        if isinstance(result, dict) and "error" in result:
            test_case["error"] = True

        tests.append(test_case)

    return {
        "function": category,
        "gnuplot_version": get_gnuplot_version(),
        "tests": tests
    }


def get_gnuplot_version():
    """Get the gnuplot version string."""
    try:
        result = subprocess.run(
            ["gnuplot", "--version"],
            capture_output=True,
            text=True,
            timeout=5
        )
        return result.stdout.strip()
    except:
        return "unknown"


def main():
    """Main extraction process."""
    print("=== Gnuplot Test Oracle Data Extraction ===")
    print(f"Gnuplot version: {get_gnuplot_version()}")
    print()

    # Create output directory
    output_dir = Path(__file__).parent / "data"
    output_dir.mkdir(exist_ok=True)

    # Extract test data for each category
    for category, expressions in TEST_CASES.items():
        test_data = extract_test_data(category, expressions)

        # Write to JSON file
        output_file = output_dir / f"{category}.json"
        with open(output_file, 'w') as f:
            json.dump(test_data, f, indent=2)

        print(f"  ✓ Generated: {output_file} ({len(expressions)} tests)")

    print()
    print("=== Extraction Complete ===")
    print(f"Test oracle data files generated in: {output_dir}")
    print(f"Total categories: {len(TEST_CASES)}")
    print(f"Total test cases: {sum(len(tests) for tests in TEST_CASES.values())}")


if __name__ == "__main__":
    main()