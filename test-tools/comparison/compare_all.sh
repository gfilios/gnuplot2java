#!/bin/bash
# Comprehensive comparison wrapper - runs all comparison tools
# Usage: ./compare_all.sh <c_file.svg> <java_file.svg>

set -e

if [ "$#" -ne 2 ]; then
    echo "Usage: $0 <c_file.svg> <java_file.svg>"
    echo ""
    echo "Example:"
    echo "  $0 test-results/latest/outputs/simple_c.svg test-results/latest/outputs/simple_java.svg"
    exit 1
fi

C_SVG="$1"
JAVA_SVG="$2"
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"

echo "╔════════════════════════════════════════════════════════════╗"
echo "║    COMPREHENSIVE GNUPLOT COMPARISON SUITE                  ║"
echo "╠════════════════════════════════════════════════════════════╣"
echo "║ This tool runs multiple comparison analyses:              ║"
echo "║  1. Deep Element Analysis (all Gnuplot elements)          ║"
echo "║  2. SVG Code Analysis (structure, elements, colors)       ║"
echo "║  3. Visual Image Comparison (pixel-level differences)     ║"
echo "╚════════════════════════════════════════════════════════════╝"
echo ""

# Verify files exist
if [ ! -f "$C_SVG" ]; then
    echo "❌ Error: C file not found: $C_SVG"
    exit 1
fi

if [ ! -f "$JAVA_SVG" ]; then
    echo "❌ Error: Java file not found: $JAVA_SVG"
    exit 1
fi

echo "Files to compare:"
echo "  C SVG:    $C_SVG"
echo "  Java SVG: $JAVA_SVG"
echo ""
echo "═══════════════════════════════════════════════════════════════"
echo ""

# Check which tools are available
HAS_DEEP_COMPARE=false
HAS_SVG_COMPARE=false
HAS_VISUAL_COMPARE=false
HAS_IMAGE_COMPARE=false

if [ -x "$SCRIPT_DIR/compare_deep.sh" ]; then
    HAS_DEEP_COMPARE=true
fi

if [ -x "$SCRIPT_DIR/compare_svg.sh" ]; then
    HAS_SVG_COMPARE=true
fi

if [ -x "$SCRIPT_DIR/compare_visual.sh" ]; then
    HAS_VISUAL_COMPARE=true
fi

if [ -x "$SCRIPT_DIR/compare_images.py" ]; then
    if python3 -c "import cairosvg, PIL, numpy" 2>/dev/null; then
        HAS_IMAGE_COMPARE=true
    fi
fi

# Run DEEP element-by-element comparison (NEW!)
if [ "$HAS_DEEP_COMPARE" = true ]; then
    echo "╔════════════════════════════════════════════════════════════╗"
    echo "║          PART 1: DEEP ELEMENT-BY-ELEMENT ANALYSIS          ║"
    echo "╚════════════════════════════════════════════════════════════╝"
    echo ""
    "$SCRIPT_DIR/compare_deep.sh" "$C_SVG" "$JAVA_SVG" || true
    echo ""
    echo "═══════════════════════════════════════════════════════════════"
    echo ""
else
    echo "⚠️  Deep comparison tool not found, skipping..."
    echo ""
fi

# Run SVG-level comparison
if [ "$HAS_SVG_COMPARE" = true ]; then
    echo "╔════════════════════════════════════════════════════════════╗"
    echo "║            PART 2: SVG CODE STRUCTURE ANALYSIS             ║"
    echo "╚════════════════════════════════════════════════════════════╝"
    echo ""
    "$SCRIPT_DIR/compare_svg.sh" "$C_SVG" "$JAVA_SVG" || true
    echo ""
    echo "═══════════════════════════════════════════════════════════════"
    echo ""
else
    echo "⚠️  SVG comparison tool not found, skipping..."
    echo ""
fi

# Run visual comparison
if [ "$HAS_VISUAL_COMPARE" = true ]; then
    echo "╔════════════════════════════════════════════════════════════╗"
    echo "║           PART 3: VISUAL IMAGE COMPARISON                  ║"
    echo "╚════════════════════════════════════════════════════════════╝"
    echo ""
    "$SCRIPT_DIR/compare_visual.sh" "$C_SVG" "$JAVA_SVG" 2>/dev/null || {
        echo "⚠️  ImageMagick not available, trying Python alternative..."
        echo ""
        if [ "$HAS_IMAGE_COMPARE" = true ]; then
            python3 "$SCRIPT_DIR/compare_images.py" "$C_SVG" "$JAVA_SVG" || true
        else
            echo "⚠️  Python image comparison dependencies not available"
            echo "   Install with: pip3 install cairosvg Pillow numpy"
        fi
    }
    echo ""
else
    echo "⚠️  Visual comparison tool not found, skipping..."
    echo ""
fi

# Summary
echo ""
echo "╔════════════════════════════════════════════════════════════╗"
echo "║                   COMPARISON SUMMARY                       ║"
echo "╚════════════════════════════════════════════════════════════╝"
echo ""
echo "Analysis complete! Key findings:"
echo ""
echo "To view visual differences:"
echo "  • Side-by-side: open /tmp/gnuplot_visual_comparison/overlay_*.png"
echo "  • Diff map:     open /tmp/gnuplot_visual_comparison/diff_*.png"
echo ""
echo "For detailed analysis, see:"
echo "  • VISUAL_COMPARISON_APPROACH.md"
echo ""
