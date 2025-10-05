#!/bin/bash
# compare_svg.sh - Systematic SVG comparison between C and Java Gnuplot outputs
# Usage: ./compare_svg.sh <c_file.svg> <java_file.svg>

if [ "$#" -ne 2 ]; then
    echo "Usage: $0 <c_file.svg> <java_file.svg>"
    exit 1
fi

C_FILE=$1
JAVA_FILE=$2

if [ ! -f "$C_FILE" ]; then
    echo "Error: C file not found: $C_FILE"
    exit 1
fi

if [ ! -f "$JAVA_FILE" ]; then
    echo "Error: Java file not found: $JAVA_FILE"
    exit 1
fi

echo "╔════════════════════════════════════════════════════════════╗"
echo "║        SVG Visual Comparison: C vs Java Gnuplot           ║"
echo "╠════════════════════════════════════════════════════════════╣"
echo "║ C File:    $(basename "$C_FILE")"
echo "║ Java File: $(basename "$JAVA_FILE")"
echo "╚════════════════════════════════════════════════════════════╝"

echo ""
echo "=== 1. PLOT STYLE COMPARISON ==="
C_LINES=$(grep -c "<path stroke='rgb" "$C_FILE" 2>/dev/null || echo "0")
C_POINTS=$(grep -c "<use xlink:href='#gpPt" "$C_FILE" 2>/dev/null || echo "0")
JAVA_LINES=$(grep -c "<path stroke='rgb" "$JAVA_FILE" 2>/dev/null || echo "0")
JAVA_POINTS=$(grep -c "<use xlink:href='#gpPt" "$JAVA_FILE" 2>/dev/null || echo "0")

echo "C Gnuplot:"
echo "  - Line paths (<path>): $C_LINES"
echo "  - Point markers (<use>): $C_POINTS"
echo "  - Style: $([ "$C_LINES" -gt 0 ] && echo "LINES" || echo "POINTS")"
echo ""
echo "Java Gnuplot:"
echo "  - Line paths (<path>): $JAVA_LINES"
echo "  - Point markers (<use>): $JAVA_POINTS"
echo "  - Style: $([ "$JAVA_LINES" -gt 0 ] && echo "LINES" || echo "POINTS")"
echo ""
if [ "$C_LINES" -gt 0 ] && [ "$JAVA_POINTS" -gt 0 ]; then
    echo "❌ MISMATCH: C uses LINES, Java uses POINTS"
elif [ "$C_POINTS" -gt 0 ] && [ "$JAVA_LINES" -gt 0 ]; then
    echo "❌ MISMATCH: C uses POINTS, Java uses LINES"
else
    echo "✅ MATCH: Both use same plot style"
fi

echo ""
echo "=== 2. AXIS POSITION COMPARISON ==="
C_Y_AXIS=$(grep -o "M[0-9.]*,[0-9.]* L[0-9.]*,[0-9.]*" "$C_FILE" | head -1 | grep -o "M[0-9.]*" | sed 's/M//')
JAVA_Y_AXIS=$(grep -o 'x1="[0-9.]*".*y1="[0-9.]*".*x2="[0-9.]*".*y2="[0-9.]*"' "$JAVA_FILE" | head -1 | grep -o 'x1="[0-9.]*"' | sed 's/x1="//;s/"//')

echo "C Gnuplot Y-axis x-position: ${C_Y_AXIS:-'not found'}"
echo "Java Gnuplot Y-axis x-position: ${JAVA_Y_AXIS:-'not found'}"

if [ -n "$C_Y_AXIS" ] && [ -n "$JAVA_Y_AXIS" ]; then
    # Simple comparison (within 5 pixels tolerance)
    DIFF=$(echo "$C_Y_AXIS - $JAVA_Y_AXIS" | bc -l | sed 's/-//')
    if (( $(echo "$DIFF < 5" | bc -l) )); then
        echo "✅ MATCH: Axes aligned (within 5px tolerance)"
    else
        echo "❌ MISMATCH: Axis positions differ by ${DIFF}px"
    fi
fi

echo ""
echo "=== 3. PLOT BORDER COMPARISON ==="
C_BORDER=$(grep -c "L54.53,66.01 Z" "$C_FILE" 2>/dev/null || echo "0")
JAVA_BORDER=$(grep -c '<path d="M.*Z" stroke="#000000".*fill="none"' "$JAVA_FILE" 2>/dev/null || echo "0")

echo "C Gnuplot border: $([ "$C_BORDER" -gt 0 ] && echo "✅ Present" || echo "❌ Missing")"
echo "Java Gnuplot border: $([ "$JAVA_BORDER" -gt 0 ] && echo "✅ Present" || echo "❌ Missing")"

echo ""
echo "=== 4. COLOR PALETTE COMPARISON ==="
echo "C Gnuplot colors:"
grep -o "stroke='rgb([^']*)" "$C_FILE" | sed "s/stroke='rgb(/  /;s/)//" | sort -u | while read -r color; do
    # Convert rgb(r,g,b) to hex
    R=$(echo "$color" | awk '{print $1}' | sed 's/,//')
    G=$(echo "$color" | awk '{print $2}' | sed 's/,//')
    B=$(echo "$color" | awk '{print $3}')
    printf "  rgb(%3d,%3d,%3d) = #%02X%02X%02X\n" "$R" "$G" "$B" "$R" "$G" "$B"
done

echo ""
echo "Java Gnuplot colors:"
grep -o "color='#[A-F0-9]*'" "$JAVA_FILE" | sed "s/color='//;s/'//" | sort -u | while read -r hex; do
    echo "  $hex"
done

echo ""
echo "=== 5. LEGEND COMPARISON ==="
C_LEGEND=$(grep -c "sin(x)" "$C_FILE" 2>/dev/null || echo "0")
JAVA_LEGEND=$(grep -c "sin(x)" "$JAVA_FILE" 2>/dev/null || echo "0")

echo "C Gnuplot legend: $([ "$C_LEGEND" -gt 0 ] && echo "✅ Present" || echo "❌ Missing")"
echo "Java Gnuplot legend: $([ "$JAVA_LEGEND" -gt 0 ] && echo "✅ Present" || echo "❌ Missing")"

echo ""
echo "=== 6. FILE SIZE COMPARISON ==="
C_SIZE=$(wc -c < "$C_FILE")
JAVA_SIZE=$(wc -c < "$JAVA_FILE")
RATIO=$(echo "scale=1; $JAVA_SIZE * 100 / $C_SIZE" | bc)

echo "C Gnuplot:    $(numfmt --to=iec-i --suffix=B $C_SIZE)"
echo "Java Gnuplot: $(numfmt --to=iec-i --suffix=B $JAVA_SIZE)"
echo "Java/C ratio: ${RATIO}%"

echo ""
echo "╔════════════════════════════════════════════════════════════╗"
echo "║                    COMPARISON COMPLETE                     ║"
echo "╚════════════════════════════════════════════════════════════╝"
