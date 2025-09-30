#!/bin/bash
# Extract mathematical function test data from gnuplot
# This script generates reference output from the installed gnuplot to use as test oracle data

set -e

OUTPUT_DIR="$(dirname "$0")/data"
mkdir -p "$OUTPUT_DIR"

echo "=== Gnuplot Test Oracle Data Extraction ==="
echo "Output directory: $OUTPUT_DIR"
echo "Gnuplot version: $(gnuplot --version)"
echo ""

# Function to extract function results
extract_function_results() {
    local func_name=$1
    local test_file="$OUTPUT_DIR/${func_name}.json"

    echo "Extracting: $func_name"

    gnuplot <<EOF > "$test_file"
set print "-"
set table
# Generate JSON format
print "{"
print "  \"function\": \"$func_name\","
print "  \"tests\": ["
EOF

    case "$func_name" in
        "basic_arithmetic")
            gnuplot <<'EOF' >> "$test_file"
print sprintf("    {\"expression\": \"2 + 3\", \"result\": %.17g},", 2 + 3)
print sprintf("    {\"expression\": \"10 - 4\", \"result\": %.17g},", 10 - 4)
print sprintf("    {\"expression\": \"5 * 6\", \"result\": %.17g},", 5 * 6)
print sprintf("    {\"expression\": \"20 / 4\", \"result\": %.17g},", 20 / 4)
print sprintf("    {\"expression\": \"2 ** 8\", \"result\": %.17g},", 2 ** 8)
print sprintf("    {\"expression\": \"17 % 5\", \"result\": %.17g},", 17 % 5)
print sprintf("    {\"expression\": \"2 + 3 * 4\", \"result\": %.17g},", 2 + 3 * 4)
print sprintf("    {\"expression\": \"(2 + 3) * 4\", \"result\": %.17g},", (2 + 3) * 4)
print sprintf("    {\"expression\": \"-5\", \"result\": %.17g},", -5)
print sprintf("    {\"expression\": \"3.14159\", \"result\": %.17g}", 3.14159)
EOF
            ;;
        "trigonometric")
            gnuplot <<'EOF' >> "$test_file"
print sprintf("    {\"expression\": \"sin(0)\", \"result\": %.17g},", sin(0))
print sprintf("    {\"expression\": \"sin(pi/2)\", \"result\": %.17g},", sin(pi/2))
print sprintf("    {\"expression\": \"sin(pi)\", \"result\": %.17g},", sin(pi))
print sprintf("    {\"expression\": \"cos(0)\", \"result\": %.17g},", cos(0))
print sprintf("    {\"expression\": \"cos(pi/2)\", \"result\": %.17g},", cos(pi/2))
print sprintf("    {\"expression\": \"cos(pi)\", \"result\": %.17g},", cos(pi))
print sprintf("    {\"expression\": \"tan(0)\", \"result\": %.17g},", tan(0))
print sprintf("    {\"expression\": \"tan(pi/4)\", \"result\": %.17g},", tan(pi/4))
print sprintf("    {\"expression\": \"asin(0)\", \"result\": %.17g},", asin(0))
print sprintf("    {\"expression\": \"asin(1)\", \"result\": %.17g},", asin(1))
print sprintf("    {\"expression\": \"acos(0)\", \"result\": %.17g},", acos(0))
print sprintf("    {\"expression\": \"acos(1)\", \"result\": %.17g},", acos(1))
print sprintf("    {\"expression\": \"atan(0)\", \"result\": %.17g},", atan(0))
print sprintf("    {\"expression\": \"atan(1)\", \"result\": %.17g},", atan(1))
print sprintf("    {\"expression\": \"atan2(1,1)\", \"result\": %.17g}", atan2(1,1))
EOF
            ;;
        "exponential_logarithmic")
            gnuplot <<'EOF' >> "$test_file"
print sprintf("    {\"expression\": \"exp(0)\", \"result\": %.17g},", exp(0))
print sprintf("    {\"expression\": \"exp(1)\", \"result\": %.17g},", exp(1))
print sprintf("    {\"expression\": \"exp(2)\", \"result\": %.17g},", exp(2))
print sprintf("    {\"expression\": \"log(1)\", \"result\": %.17g},", log(1))
print sprintf("    {\"expression\": \"log(exp(1))\", \"result\": %.17g},", log(exp(1)))
print sprintf("    {\"expression\": \"log(10)\", \"result\": %.17g},", log(10))
print sprintf("    {\"expression\": \"log10(1)\", \"result\": %.17g},", log10(1))
print sprintf("    {\"expression\": \"log10(10)\", \"result\": %.17g},", log10(10))
print sprintf("    {\"expression\": \"log10(100)\", \"result\": %.17g},", log10(100))
print sprintf("    {\"expression\": \"sqrt(0)\", \"result\": %.17g},", sqrt(0))
print sprintf("    {\"expression\": \"sqrt(4)\", \"result\": %.17g},", sqrt(4))
print sprintf("    {\"expression\": \"sqrt(2)\", \"result\": %.17g}", sqrt(2))
EOF
            ;;
        "hyperbolic")
            gnuplot <<'EOF' >> "$test_file"
print sprintf("    {\"expression\": \"sinh(0)\", \"result\": %.17g},", sinh(0))
print sprintf("    {\"expression\": \"sinh(1)\", \"result\": %.17g},", sinh(1))
print sprintf("    {\"expression\": \"cosh(0)\", \"result\": %.17g},", cosh(0))
print sprintf("    {\"expression\": \"cosh(1)\", \"result\": %.17g},", cosh(1))
print sprintf("    {\"expression\": \"tanh(0)\", \"result\": %.17g},", tanh(0))
print sprintf("    {\"expression\": \"tanh(1)\", \"result\": %.17g}", tanh(1))
EOF
            ;;
        "special_functions")
            gnuplot <<'EOF' >> "$test_file"
print sprintf("    {\"expression\": \"abs(-5)\", \"result\": %.17g},", abs(-5))
print sprintf("    {\"expression\": \"abs(3.14)\", \"result\": %.17g},", abs(3.14))
print sprintf("    {\"expression\": \"ceil(3.14)\", \"result\": %.17g},", ceil(3.14))
print sprintf("    {\"expression\": \"ceil(-3.14)\", \"result\": %.17g},", ceil(-3.14))
print sprintf("    {\"expression\": \"floor(3.14)\", \"result\": %.17g},", floor(3.14))
print sprintf("    {\"expression\": \"floor(-3.14)\", \"result\": %.17g},", floor(-3.14))
print sprintf("    {\"expression\": \"int(3.14)\", \"result\": %.17g},", int(3.14))
print sprintf("    {\"expression\": \"int(-3.14)\", \"result\": %.17g},", int(-3.14))
print sprintf("    {\"expression\": \"sgn(-5)\", \"result\": %.17g},", sgn(-5))
print sprintf("    {\"expression\": \"sgn(0)\", \"result\": %.17g},", sgn(0))
print sprintf("    {\"expression\": \"sgn(5)\", \"result\": %.17g}", sgn(5))
EOF
            ;;
        "constants")
            gnuplot <<'EOF' >> "$test_file"
print sprintf("    {\"expression\": \"pi\", \"result\": %.17g},", pi)
print sprintf("    {\"expression\": \"2*pi\", \"result\": %.17g},", 2*pi)
print sprintf("    {\"expression\": \"pi/2\", \"result\": %.17g},", pi/2)
print sprintf("    {\"expression\": \"pi/4\", \"result\": %.17g}", pi/4)
EOF
            ;;
    esac

    # Close JSON
    gnuplot <<'EOF' >> "$test_file"
print "  ]"
print "}"
EOF

    echo "  ✓ Generated: $test_file"
}

# Extract test data for various function categories
extract_function_results "basic_arithmetic"
extract_function_results "trigonometric"
extract_function_results "exponential_logarithmic"
extract_function_results "hyperbolic"
extract_function_results "special_functions"
extract_function_results "constants"

echo ""
echo "=== Extraction Complete ==="
echo "Test oracle data files generated in: $OUTPUT_DIR"
echo ""
echo "Files created:"
ls -lh "$OUTPUT_DIR"/*.json