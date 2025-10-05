#!/bin/bash
# Visual comparison tool for SVG outputs using ImageMagick
# Usage: ./compare_visual.sh <c_file.svg> <java_file.svg>

set -e

if [ "$#" -ne 2 ]; then
    echo "Usage: $0 <c_file.svg> <java_file.svg>"
    exit 1
fi

C_SVG="$1"
JAVA_SVG="$2"

if [ ! -f "$C_SVG" ]; then
    echo "Error: C file not found: $C_SVG"
    exit 1
fi

if [ ! -f "$JAVA_SVG" ]; then
    echo "Error: Java file not found: $JAVA_SVG"
    exit 1
fi

# Check for required tools
CONVERT_CMD=""
COMPARE_CMD=""

if command -v magick &> /dev/null; then
    # ImageMagick 7.x
    CONVERT_CMD="magick"
    COMPARE_CMD="magick compare"
elif command -v convert &> /dev/null; then
    # ImageMagick 6.x or compatible
    CONVERT_CMD="convert"
    COMPARE_CMD="compare"
else
    echo "╔════════════════════════════════════════════════════════════╗"
    echo "║     Visual Image Comparison Tool                          ║"
    echo "╠════════════════════════════════════════════════════════════╣"
    echo "║ ❌ ImageMagick not found                                   ║"
    echo "║                                                            ║"
    echo "║ Install with: brew install imagemagick                    ║"
    echo "║                                                            ║"
    echo "║ Alternative: Use compare_images.py (requires Python libs) ║"
    echo "╚════════════════════════════════════════════════════════════╝"
    echo ""
    echo "Falling back to basic SVG text comparison..."
    echo ""

    # Do basic SVG comparison without ImageMagick
    exec "$0-fallback" "$C_SVG" "$JAVA_SVG"
fi

# Create temp directory
TEMP_DIR="/tmp/gnuplot_visual_comparison"
mkdir -p "$TEMP_DIR"

C_BASE=$(basename "$C_SVG" .svg)
JAVA_BASE=$(basename "$JAVA_SVG" .svg)

C_PNG="$TEMP_DIR/${C_BASE}.png"
JAVA_PNG="$TEMP_DIR/${JAVA_BASE}.png"
DIFF_PNG="$TEMP_DIR/diff_${C_BASE}_vs_${JAVA_BASE}.png"
OVERLAY_PNG="$TEMP_DIR/overlay_${C_BASE}_vs_${JAVA_BASE}.png"

echo "╔════════════════════════════════════════════════════════════╗"
echo "║     Visual Image Comparison: C vs Java Gnuplot            ║"
echo "╠════════════════════════════════════════════════════════════╣"
echo "║ C SVG:    $(basename "$C_SVG")"
echo "║ Java SVG: $(basename "$JAVA_SVG")"
echo "╚════════════════════════════════════════════════════════════╝"
echo ""

echo "=== 1. CONVERTING SVG TO PNG ==="
echo "Converting C SVG to PNG..."
$CONVERT_CMD -background white -density 150 "$C_SVG" "$C_PNG" 2>/dev/null
echo "✅ Created: $C_PNG"

echo "Converting Java SVG to PNG..."
$CONVERT_CMD -background white -density 150 "$JAVA_SVG" "$JAVA_PNG" 2>/dev/null
echo "✅ Created: $JAVA_PNG"

echo ""
echo "=== 2. IMAGE DIMENSIONS ==="
C_DIM=$($CONVERT_CMD identify -format "%wx%h" "$C_PNG" 2>/dev/null)
JAVA_DIM=$($CONVERT_CMD identify -format "%wx%h" "$JAVA_PNG" 2>/dev/null)

echo "C Gnuplot:    $C_DIM"
echo "Java Gnuplot: $JAVA_DIM"

if [ "$C_DIM" != "$JAVA_DIM" ]; then
    echo "⚠️  Dimension mismatch detected"
fi

echo ""
echo "=== 3. PIXEL DIFFERENCE ANALYSIS ==="

# Use ImageMagick's compare to generate metrics
if $COMPARE_CMD -metric AE -fuzz 5% "$C_PNG" "$JAVA_PNG" "$DIFF_PNG" 2>&1 | grep -q "compare: image widths or heights differ"; then
    echo "❌ Cannot compare: Images have different dimensions"
else
    # Get difference count
    DIFF_PIXELS=$($COMPARE_CMD -metric AE -fuzz 0% "$C_PNG" "$JAVA_PNG" "$DIFF_PNG" 2>&1 || true)

    # Get total pixels
    TOTAL_PIXELS=$($CONVERT_CMD identify -format "%[fx:w*h]" "$C_PNG" 2>/dev/null)

    # Calculate percentage
    if [ -n "$DIFF_PIXELS" ] && [ -n "$TOTAL_PIXELS" ] && [ "$TOTAL_PIXELS" -gt 0 ]; then
        PERCENT=$(echo "scale=2; ($DIFF_PIXELS * 100) / $TOTAL_PIXELS" | bc -l 2>/dev/null || echo "N/A")

        echo "Total pixels:      $TOTAL_PIXELS"
        echo "Different pixels:  $DIFF_PIXELS"
        echo "Difference:        $PERCENT%"

        # Interpret results
        if [ -n "$PERCENT" ] && [ "$PERCENT" != "N/A" ]; then
            PERCENT_INT=$(echo "$PERCENT / 1" | bc 2>/dev/null || echo "100")
            if [ "$PERCENT_INT" -lt 1 ]; then
                echo "✅ IMAGES ARE NEARLY IDENTICAL (<1% difference)"
            elif [ "$PERCENT_INT" -lt 10 ]; then
                echo "⚠️  IMAGES HAVE MINOR DIFFERENCES ($PERCENT% difference)"
            else
                echo "❌ IMAGES ARE SIGNIFICANTLY DIFFERENT ($PERCENT% difference)"
            fi
        fi
    else
        echo "ℹ️  Difference metrics: N/A"
    fi

    echo "✅ Difference map saved: $DIFF_PNG"
fi

echo ""
echo "=== 4. CREATING OVERLAY COMPARISON ==="

# Create side-by-side comparison
$CONVERT_CMD "$C_PNG" "$JAVA_PNG" +append "$OVERLAY_PNG" 2>/dev/null
echo "✅ Side-by-side comparison: $OVERLAY_PNG"

echo ""
echo "=== 5. COLOR ANALYSIS ==="

# Get unique colors
C_COLORS=$($CONVERT_CMD "$C_PNG" -unique-colors -format "%c" histogram:info:- 2>/dev/null | wc -l | tr -d ' ')
JAVA_COLORS=$($CONVERT_CMD "$JAVA_PNG" -unique-colors -format "%c" histogram:info:- 2>/dev/null | wc -l | tr -d ' ')

echo "C Gnuplot unique colors:    $C_COLORS"
echo "Java Gnuplot unique colors: $JAVA_COLORS"

if [ "$C_COLORS" != "$JAVA_COLORS" ]; then
    COLOR_DIFF=$((C_COLORS - JAVA_COLORS))
    COLOR_DIFF=${COLOR_DIFF#-}  # absolute value
    echo "⚠️  Color palette differs by $COLOR_DIFF colors"
fi

echo ""
echo "=== 6. VISUAL STRUCTURE ANALYSIS ==="

# Analyze edge detection to see structural differences
C_EDGES="$TEMP_DIR/${C_BASE}_edges.png"
JAVA_EDGES="$TEMP_DIR/${JAVA_BASE}_edges.png"

$CONVERT_CMD "$C_PNG" -edge 1 -threshold 50% "$C_EDGES" 2>/dev/null
$CONVERT_CMD "$JAVA_PNG" -edge 1 -threshold 50% "$JAVA_EDGES" 2>/dev/null

C_EDGE_PIXELS=$($CONVERT_CMD "$C_EDGES" -format "%[fx:mean*w*h]" info: 2>/dev/null | cut -d. -f1)
JAVA_EDGE_PIXELS=$($CONVERT_CMD "$JAVA_EDGES" -format "%[fx:mean*w*h]" info: 2>/dev/null | cut -d. -f1)

echo "C Gnuplot edge pixels:    $C_EDGE_PIXELS"
echo "Java Gnuplot edge pixels: $JAVA_EDGE_PIXELS"

if [ -n "$C_EDGE_PIXELS" ] && [ -n "$JAVA_EDGE_PIXELS" ]; then
    EDGE_DIFF=$((C_EDGE_PIXELS - JAVA_EDGE_PIXELS))
    EDGE_DIFF=${EDGE_DIFF#-}  # absolute value
    EDGE_PERCENT=$(echo "scale=1; ($EDGE_DIFF * 100) / $C_EDGE_PIXELS" | bc -l 2>/dev/null || echo "N/A")

    if [ -n "$EDGE_PERCENT" ] && [ "$EDGE_PERCENT" != "N/A" ]; then
        EDGE_PERCENT_INT=$(echo "$EDGE_PERCENT / 1" | bc 2>/dev/null || echo "100")
        if [ "$EDGE_PERCENT_INT" -gt 20 ]; then
            echo "⚠️  Structural difference: ${EDGE_PERCENT}% more/fewer edges"
            echo "   (This indicates different line/point rendering)"
        fi
    fi
fi

echo ""
echo "╔════════════════════════════════════════════════════════════╗"
echo "║                  COMPARISON COMPLETE                       ║"
echo "╠════════════════════════════════════════════════════════════╣"
echo "║ Output files:                                              ║"
echo "║  • PNG files:     $TEMP_DIR"
echo "║  • Diff map:      $(basename "$DIFF_PNG")"
echo "║  • Side-by-side:  $(basename "$OVERLAY_PNG")"
echo "╚════════════════════════════════════════════════════════════╝"

# Also run the SVG-level comparison
echo ""
echo "Running SVG-level comparison..."
echo "─────────────────────────────────────────────────────────────"
exec "$(dirname "$0")/compare_svg.sh" "$C_SVG" "$JAVA_SVG"
