#!/bin/bash
# Deep Element-by-Element Comparison for Gnuplot SVG outputs
# Analyzes ALL visual elements: axes, ticks, labels, legend, title, borders, plot styles

set -e

if [ "$#" -ne 2 ]; then
    echo "Usage: $0 <c_file.svg> <java_file.svg>"
    exit 1
fi

C_SVG="$1"
JAVA_SVG="$2"

echo "╔═══════════════════════════════════════════════════════════════════╗"
echo "║     DEEP ELEMENT-BY-ELEMENT COMPARISON: C vs Java Gnuplot        ║"
echo "╠═══════════════════════════════════════════════════════════════════╣"
echo "║ C SVG:    $(basename "$C_SVG")"
echo "║ Java SVG: $(basename "$JAVA_SVG")"
echo "╚═══════════════════════════════════════════════════════════════════╝"
echo ""

# ============================================================================
# 1. TITLE ANALYSIS
# ============================================================================
echo "═══════════════════════════════════════════════════════════════════"
echo "1. TITLE COMPARISON"
echo "═══════════════════════════════════════════════════════════════════"

# Extract title text
C_TITLE=$(grep -o '<text[^>]*>Simple Plots</text>' "$C_SVG" | head -1 || echo "")
JAVA_TITLE=$(grep -o '<text[^>]*>Simple Plots</text>' "$JAVA_SVG" | head -1 || echo "")

# Extract title position
C_TITLE_POS=$(grep -B1 '>Simple Plots<' "$C_SVG" | grep 'transform="translate' | grep -o 'translate([^)]*)' || echo "")
JAVA_TITLE_POS=$(grep -B1 '>Simple Plots<' "$JAVA_SVG" | grep 'transform="translate' | grep -o 'translate([^)]*)' || echo "")

# Extract title font size
C_TITLE_FONT=$(grep -B1 '>Simple Plots<' "$C_SVG" | grep 'font-size' | grep -o 'font-size="[^"]*"' || echo "")
JAVA_TITLE_FONT=$(grep '>Simple Plots<' "$JAVA_SVG" | grep -o 'font-size="[^"]*"' || echo "")

echo "Text:"
echo "  C:    ${C_TITLE:-'NOT FOUND'}"
echo "  Java: ${JAVA_TITLE:-'NOT FOUND'}"

echo "Position:"
echo "  C:    ${C_TITLE_POS:-'NOT FOUND'}"
echo "  Java: ${JAVA_TITLE_POS:-'NOT FOUND'}"

echo "Font Size:"
echo "  C:    ${C_TITLE_FONT:-'NOT FOUND'}"
echo "  Java: ${JAVA_TITLE_FONT:-'NOT FOUND'}"

if [ "$C_TITLE_POS" != "$JAVA_TITLE_POS" ] && [ -n "$C_TITLE_POS" ] && [ -n "$JAVA_TITLE_POS" ]; then
    echo "  ❌ Position mismatch"
fi

if [ "$C_TITLE_FONT" != "$JAVA_TITLE_FONT" ] && [ -n "$C_TITLE_FONT" ] && [ -n "$JAVA_TITLE_FONT" ]; then
    echo "  ❌ Font size mismatch"
fi

# ============================================================================
# 2. PLOT BORDER/FRAME ANALYSIS
# ============================================================================
echo ""
echo "═══════════════════════════════════════════════════════════════════"
echo "2. PLOT BORDER/FRAME COMPARISON"
echo "═══════════════════════════════════════════════════════════════════"

# Extract border path (should be a rectangle around plot area)
C_BORDER=$(grep -o "M54[^Z]*Z" "$C_SVG" | tail -1)
JAVA_BORDER=$(grep -o "M [0-9.]*.*Z" "$JAVA_SVG" | grep "stroke=\"#000000\"" | head -1 || echo "")

echo "C Border Path:"
echo "  ${C_BORDER:-'NOT FOUND'}"
echo ""
echo "Java Border Path:"
echo "  ${JAVA_BORDER:-'NOT FOUND'}"

# Extract border coordinates
C_BORDER_COORDS=$(echo "$C_BORDER" | grep -o '[0-9.]*,[0-9.]*' | head -4)
JAVA_BORDER_COORDS=$(echo "$JAVA_BORDER" | grep -o '[0-9.]*' | paste -sd',' - | sed 's/,/ /g' | xargs -n2 | paste -sd',' -)

echo ""
echo "Border Coordinates:"
echo "C corners:    $(echo "$C_BORDER_COORDS" | tr '\n' ' ')"
echo "Java corners: $(echo "$JAVA_BORDER_COORDS" | tr '\n' ' ')"

# ============================================================================
# 3. AXIS ANALYSIS
# ============================================================================
echo ""
echo "═══════════════════════════════════════════════════════════════════"
echo "3. AXIS POSITIONING & TICK MARKS"
echo "═══════════════════════════════════════════════════════════════════"

# Y-axis ticks and labels
echo "Y-AXIS TICKS:"
C_Y_TICKS=$(grep -o "M54\.53,[0-9.]* L63\.53,[0-9.]*" "$C_SVG" | wc -l | tr -d ' ')
JAVA_Y_TICKS=$(grep -o 'x1="414[^"]*".*y1="[^"]*".*x2="[^"]*".*y2="[^"]*"' "$JAVA_SVG" | wc -l | tr -d ' ')

echo "  C tick count:    $C_Y_TICKS"
echo "  Java tick count: $JAVA_Y_TICKS"

# Extract Y-axis label positions
echo ""
echo "Y-AXIS LABELS:"
C_Y_LABELS=$(grep 'translate(46.14' "$C_SVG" | grep -o 'translate([^)]*)' | head -3)
JAVA_Y_LABELS=$(grep 'text-anchor="end"' "$JAVA_SVG" | grep -o 'x="[^"]*" y="[^"]*"' | head -3)

echo "C positions (sample):"
echo "$C_Y_LABELS" | head -3 | sed 's/^/  /'

echo "Java positions (sample):"
echo "$JAVA_Y_LABELS" | head -3 | sed 's/^/  /'

# X-axis ticks and labels
echo ""
echo "X-AXIS TICKS:"
C_X_TICKS=$(grep -o "M[0-9.]*,564\.00 L[0-9.]*,555\.00" "$C_SVG" | wc -l | tr -d ' ')
JAVA_X_TICKS=$(grep -o 'y1="294[^"]*".*x1="[^"]*".*y2="[^"]*".*x2="[^"]*"' "$JAVA_SVG" | wc -l | tr -d ' ')

echo "  C tick count:    $C_X_TICKS"
echo "  Java tick count: $JAVA_X_TICKS"

# Extract X-axis label positions
echo ""
echo "X-AXIS LABELS:"
C_X_LABELS=$(grep 'translate([0-9.]*,585.90)' "$C_SVG" | grep -o 'translate([^)]*)' | head -3)
JAVA_X_LABELS=$(grep 'text-anchor="middle".*>[0-9-]*<' "$JAVA_SVG" | grep -o 'x="[^"]*" y="[^"]*"' | head -3)

echo "C positions (sample):"
echo "$C_X_LABELS" | head -3 | sed 's/^/  /'

echo "Java positions (sample):"
echo "$JAVA_X_LABELS" | head -3 | sed 's/^/  /'

# ============================================================================
# 4. LEGEND/KEY ANALYSIS
# ============================================================================
echo ""
echo "═══════════════════════════════════════════════════════════════════"
echo "4. LEGEND (KEY) COMPARISON"
echo "═══════════════════════════════════════════════════════════════════"

# Legend box position and size
C_LEGEND_BOX=$(grep -o 'M62\.92,129\.01 L62\.92,75\.01 L222\.94,75\.01 L222\.94,129\.01' "$C_SVG" || echo "")
JAVA_LEGEND_BOX=$(grep -o '<rect x="[0-9]*" y="[0-9]*" width="[0-9]*" height="[0-9]*".*stroke="#000000"' "$JAVA_SVG" | head -1 || echo "")

echo "Legend Box:"
echo "  C:    ${C_LEGEND_BOX:-'NOT FOUND'}"
echo "  Java: ${JAVA_LEGEND_BOX:-'NOT FOUND'}"

# Extract legend position
if [ -n "$C_LEGEND_BOX" ]; then
    C_LEG_X=$(echo "$C_LEGEND_BOX" | grep -o 'M[0-9.]*' | sed 's/M//')
    C_LEG_Y=$(echo "$C_LEGEND_BOX" | grep -o 'M[^,]*,\([0-9.]*\)' | cut -d',' -f2)
    echo ""
    echo "  C position:    x=$C_LEG_X, y=$C_LEG_Y"
fi

if [ -n "$JAVA_LEGEND_BOX" ]; then
    JAVA_LEG_X=$(echo "$JAVA_LEGEND_BOX" | grep -o 'x="[0-9]*"' | sed 's/x="//;s/"//')
    JAVA_LEG_Y=$(echo "$JAVA_LEGEND_BOX" | grep -o 'y="[0-9]*"' | sed 's/y="//;s/"//')
    echo "  Java position: x=$JAVA_LEG_X, y=$JAVA_LEG_Y"
fi

# Legend entries (plot labels)
echo ""
echo "Legend Entries:"
C_LEG_ENTRIES=$(grep -o '<text>sin(x)</text>\|<text>atan(x)</text>\|<text>cos(atan(x))</text>' "$C_SVG" | wc -l | tr -d ' ')
JAVA_LEG_ENTRIES=$(grep -o '>sin(x)<\|>atan(x)<\|>cos(atan(x))<' "$JAVA_SVG" | wc -l | tr -d ' ')

echo "  C count:    $C_LEG_ENTRIES"
echo "  Java count: $JAVA_LEG_ENTRIES"

# Legend entry positions
C_LEG_POS_1=$(grep -B1 '>sin(x)<' "$C_SVG" | grep 'translate' | grep -o 'translate([^)]*)' || echo "")
JAVA_LEG_POS_1=$(grep '>sin(x)<' "$JAVA_SVG" | grep -o 'x="[^"]*" y="[^"]*"' || echo "")

echo ""
echo "First entry position:"
echo "  C:    ${C_LEG_POS_1:-'NOT FOUND'}"
echo "  Java: ${JAVA_LEG_POS_1:-'NOT FOUND'}"

# Legend border style
C_LEG_STROKE=$(echo "$C_LEGEND_BOX" | grep -o 'stroke="[^"]*"' || echo "implicit black")
JAVA_LEG_STROKE=$(echo "$JAVA_LEGEND_BOX" | grep -o 'stroke="[^"]*"' || echo "")

echo ""
echo "Legend border stroke:"
echo "  C:    $C_LEG_STROKE"
echo "  Java: $JAVA_LEG_STROKE"

# ============================================================================
# 5. PLOT LINE/POINT STYLE ANALYSIS
# ============================================================================
echo ""
echo "═══════════════════════════════════════════════════════════════════"
echo "5. PLOT STYLE & LINE ATTRIBUTES"
echo "═══════════════════════════════════════════════════════════════════"

# Count plot lines vs points
C_PLOT_LINES=$(grep -c "stroke='rgb" "$C_SVG" || echo "0")
C_PLOT_POINTS=$(grep -c "use xlink:href='#gpPt" "$C_SVG" || echo "0")
JAVA_PLOT_LINES=$(grep -c "stroke='rgb\|stroke=\"#" "$JAVA_SVG" | head -1 || echo "0")
JAVA_PLOT_POINTS=$(grep -c "use xlink:href='#gpPt" "$JAVA_SVG" || echo "0")

echo "Rendering Mode:"
echo "  C:    Lines=$C_PLOT_LINES, Points=$C_PLOT_POINTS"
echo "  Java: Lines=$JAVA_PLOT_LINES, Points=$JAVA_PLOT_POINTS"

if [ "$C_PLOT_LINES" -gt 0 ] && [ "$JAVA_PLOT_POINTS" -gt "$C_PLOT_POINTS" ]; then
    echo "  ❌ CRITICAL: C uses lines, Java uses points"
fi

# Extract stroke widths
echo ""
echo "Stroke Widths:"
C_STROKE_WIDTHS=$(grep -o 'stroke-width="[^"]*"' "$C_SVG" | sort -u | head -5)
JAVA_STROKE_WIDTHS=$(grep -o 'stroke-width="[^"]*"' "$JAVA_SVG" | sort -u | head -5)

echo "C:"
echo "$C_STROKE_WIDTHS" | sed 's/^/  /'
echo "Java:"
echo "$JAVA_STROKE_WIDTHS" | sed 's/^/  /'

# Extract plot colors
echo ""
echo "Plot Colors:"
C_COLORS=$(grep -o "stroke='rgb([^']*)" "$C_SVG" | sed "s/stroke='rgb(//" | sed 's/)//' | sort -u)
JAVA_COLORS=$(grep -o "color='#[A-F0-9]*'" "$JAVA_SVG" | sed "s/color='//" | sed "s/'//" | sort -u)

echo "C (RGB):"
echo "$C_COLORS" | while read -r rgb; do
    if [ -n "$rgb" ]; then
        R=$(echo "$rgb" | awk '{print $1}' | sed 's/,//')
        G=$(echo "$rgb" | awk '{print $2}' | sed 's/,//')
        B=$(echo "$rgb" | awk '{print $3}')
        printf "  rgb(%3d,%3d,%3d) = #%02X%02X%02X\n" "$R" "$G" "$B" "$R" "$G" "$B"
    fi
done

echo "Java (Hex):"
echo "$JAVA_COLORS" | sed 's/^/  /'

# ============================================================================
# 6. FONT ANALYSIS
# ============================================================================
echo ""
echo "═══════════════════════════════════════════════════════════════════"
echo "6. FONT & TEXT RENDERING"
echo "═══════════════════════════════════════════════════════════════════"

# Extract all font families
C_FONTS=$(grep -o 'font-family="[^"]*"' "$C_SVG" | sort -u)
JAVA_FONTS=$(grep -o 'font-family="[^"]*"' "$JAVA_SVG" | sort -u)

echo "Font Families:"
echo "C:"
echo "$C_FONTS" | sed 's/^/  /'
echo "Java:"
echo "$JAVA_FONTS" | sed 's/^/  /'

# Extract all font sizes
echo ""
echo "Font Sizes:"
C_FONT_SIZES=$(grep -o 'font-size="[^"]*"' "$C_SVG" | sort -u)
JAVA_FONT_SIZES=$(grep -o 'font-size="[^"]*"' "$JAVA_SVG" | sort -u)

echo "C:"
echo "$C_FONT_SIZES" | sed 's/^/  /'
echo "Java:"
echo "$JAVA_FONT_SIZES" | sed 's/^/  /'

# ============================================================================
# 7. COORDINATE SYSTEM & VIEWPORT
# ============================================================================
echo ""
echo "═══════════════════════════════════════════════════════════════════"
echo "7. COORDINATE SYSTEM & VIEWPORT"
echo "═══════════════════════════════════════════════════════════════════"

# Extract viewBox
C_VIEWBOX=$(grep 'viewBox' "$C_SVG" | grep -o 'viewBox="[^"]*"')
JAVA_VIEWBOX=$(grep 'viewBox' "$JAVA_SVG" | grep -o 'viewBox="[^"]*"')

echo "ViewBox:"
echo "  C:    ${C_VIEWBOX:-'NOT FOUND'}"
echo "  Java: ${JAVA_VIEWBOX:-'NOT FOUND'}"

# Extract SVG dimensions
C_DIMS=$(grep '<svg' "$C_SVG" | grep -o 'width="[^"]*" height="[^"]*"')
JAVA_DIMS=$(grep '<svg' "$JAVA_SVG" | grep -o 'width="[^"]*" height="[^"]*"')

echo ""
echo "SVG Dimensions:"
echo "  C:    $C_DIMS"
echo "  Java: $JAVA_DIMS"

# ============================================================================
# 8. SUMMARY & CRITICAL ISSUES
# ============================================================================
echo ""
echo "═══════════════════════════════════════════════════════════════════"
echo "8. CRITICAL ISSUES SUMMARY"
echo "═══════════════════════════════════════════════════════════════════"

ISSUES=0

# Check plot style
if [ "$C_PLOT_LINES" -gt 0 ] && [ "$JAVA_PLOT_LINES" -eq 0 ]; then
    echo "❌ CRITICAL: Plot style mismatch (C uses LINES, Java uses POINTS)"
    ISSUES=$((ISSUES + 1))
fi

# Check legend position
if [ -n "$C_LEG_X" ] && [ -n "$JAVA_LEG_X" ]; then
    LEG_X_DIFF=$((C_LEG_X - JAVA_LEG_X))
    LEG_X_DIFF=${LEG_X_DIFF#-}
    if [ "$LEG_X_DIFF" -gt 10 ]; then
        echo "❌ Legend position differs by ${LEG_X_DIFF}px horizontally"
        ISSUES=$((ISSUES + 1))
    fi
fi

# Check title font size
if [ "$C_TITLE_FONT" != "$JAVA_TITLE_FONT" ] && [ -n "$C_TITLE_FONT" ] && [ -n "$JAVA_TITLE_FONT" ]; then
    echo "❌ Title font size mismatch: $C_TITLE_FONT vs $JAVA_TITLE_FONT"
    ISSUES=$((ISSUES + 1))
fi

# Check axis tick counts
if [ "$C_Y_TICKS" != "$JAVA_Y_TICKS" ]; then
    echo "⚠️  Y-axis tick count differs: $C_Y_TICKS vs $JAVA_Y_TICKS"
    ISSUES=$((ISSUES + 1))
fi

if [ "$C_X_TICKS" != "$JAVA_X_TICKS" ]; then
    echo "⚠️  X-axis tick count differs: $C_X_TICKS vs $JAVA_X_TICKS"
    ISSUES=$((ISSUES + 1))
fi

echo ""
if [ "$ISSUES" -eq 0 ]; then
    echo "✅ No critical issues found"
else
    echo "Total issues found: $ISSUES"
fi

echo ""
echo "╔═══════════════════════════════════════════════════════════════════╗"
echo "║                  DEEP COMPARISON COMPLETE                         ║"
echo "╚═══════════════════════════════════════════════════════════════════╝"
