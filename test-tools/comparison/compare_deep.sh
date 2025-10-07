#!/bin/bash
# Deep Element-by-Element Comparison for Gnuplot SVG outputs
# Analyzes ALL visual elements: axes, ticks, labels, legend, title, borders, plot styles

# Removed set -e to allow script to continue on errors
# set -e

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

# Extract numeric values for font size comparison
C_FONT_NUM=$(echo "$C_TITLE_FONT" | grep -o '[0-9.]*' | head -1)
JAVA_FONT_NUM=$(echo "$JAVA_TITLE_FONT" | grep -o '[0-9.]*' | head -1)

# Compare as numbers (with tolerance of 0.1)
if [ -n "$C_FONT_NUM" ] && [ -n "$JAVA_FONT_NUM" ]; then
    # Use bc for floating point comparison
    DIFF=$(echo "$C_FONT_NUM - $JAVA_FONT_NUM" | bc -l | sed 's/-//')
    IS_DIFFERENT=$(echo "$DIFF > 0.1" | bc -l)
    if [ "$IS_DIFFERENT" -eq 1 ]; then
        echo "  ❌ Font size mismatch (difference: $DIFF)"
    fi
fi

# ============================================================================
# 2. PLOT BORDER/FRAME ANALYSIS
# ============================================================================
echo ""
echo "═══════════════════════════════════════════════════════════════════"
echo "2. PLOT BORDER/FRAME COMPARISON"
echo "═══════════════════════════════════════════════════════════════════"

# Extract border path (should be a rectangle around plot area)
# C: Uses <path d='M54.53,66.01 L54.53,564.00 L774.82,564.00 L774.82,66.01 L54.53,66.01 Z'/>
C_BORDER=$(grep -o "M54[^Z]*Z" "$C_SVG" | tail -1)
# Java: Uses <path d="M 54.00 66.00 L 775.00 66.00 L 775.00 564.00 L 54.00 564.00 Z" stroke="#000000" ... fill="none"/>
JAVA_BORDER=$(grep -o 'path d="M [0-9.]*.*Z"' "$JAVA_SVG" | grep -o 'M [0-9. L]*Z' || \
              grep -o '<path d="M [0-9.]*.*Z".*fill="none"' "$JAVA_SVG" | grep -o 'd="[^"]*"' | sed 's/d="//;s/"//' || echo "")

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
# C: Detect tick marks as short horizontal lines in left margin area (x < 100)
# Pattern: horizontal path with small x-delta, excluding the main axis line itself
C_Y_TICKS=$(grep -o 'M[0-9.]*,[0-9.]* L[0-9.]*,[0-9.]*' "$C_SVG" | \
           awk -F'[M,L ]' '{
             x1=$2; y1=$3; x2=$5; y2=$6;
             if (x1 < 100 && y1 == y2 && (x2-x1) > 5 && (x2-x1) < 20) print $0
           }' | wc -l | tr -d ' ')

# Java: Short horizontal lines in left margin (x < 100), small x-delta
# Tick marks are short (< 10px) horizontal lines, not the main axis
JAVA_Y_TICKS=$(grep -o '<line x1="[0-9.]*" y1="[0-9.]*" x2="[0-9.]*" y2="[0-9.]*"' "$JAVA_SVG" | \
              awk -F'"' '{
                x1=$2; y1=$4; x2=$6; y2=$8;
                xdelta = (x1 > x2) ? x1-x2 : x2-x1;
                if (x1 < 100 && y1 == y2 && y1 > 65 && y1 < 565 && xdelta < 10) print $0
              }' | wc -l | tr -d ' ')

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
# Dynamically detect X-axis ticks by finding vertical lines in the bottom area
# C: Uses <path d='M54.53,564.00 L54.53,555.00'/> for ticks (vertical lines)
# Look for vertical lines (x1 == x2) in the bottom 100px of the plot area
C_X_TICKS=$(grep -o 'M[0-9.]*,[0-9.]* L[0-9.]*,[0-9.]*' "$C_SVG" | \
           awk -F'[M,L ]' '{
             x1=$2; y1=$3; x2=$5; y2=$6;
             ydelta = (y1>y2) ? y1-y2 : y2-y1;
             # Vertical lines (x1 == x2) with small y-delta (5-20px) in bottom area (y > 400)
             if (x1 == x2 && ydelta > 5 && ydelta < 20 && y1 > 400) print $0
           }' | wc -l | tr -d ' ')

# Java: Uses <line x1="54.00" y1="564.00" x2="54.00" y2="570.00"/> for ticks
# Look for vertical lines (x1 == x2) in the bottom area
JAVA_X_TICKS=$(grep -o '<line x1="[0-9.]*" y1="[0-9.]*" x2="[0-9.]*" y2="[0-9.]*"' "$JAVA_SVG" | \
              sed 's/[<>"]//g' | \
              awk '{
                gsub("line x1=",""); gsub(" y1="," "); gsub(" x2="," "); gsub(" y2="," ");
                x1=$1; y1=$2; x2=$3; y2=$4;
                ydelta = (y1>y2) ? y1-y2 : y2-y1;
                # Vertical lines (x1 == x2) with small y-delta (< 20px) in bottom area (y > 400)
                if (x1 == x2 && ydelta > 0 && ydelta < 20 && y1 > 400) print $0
              }' | wc -l | tr -d ' ')

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
# C gnuplot draws legend as a closed path (rectangle with 5 points forming Z)
# Find all rectangular paths and filter for the smaller one (legend, not plot border)
# Plot border is typically larger (e.g., ~500px height), legend is smaller (e.g., 18-100px)
C_LEGEND_BOX=$(grep -oE "M[0-9.]+,[0-9.]+ L[0-9.]+,[0-9.]+ L[0-9.]+,[0-9.]+ L[0-9.]+,[0-9.]+ L[0-9.]+,[0-9.]+ Z" "$C_SVG" | \
    while read path; do
        # Extract y coordinates to calculate height
        y1=$(echo "$path" | grep -oE 'M[^,]+,([0-9.]+)' | sed 's/.*,//')
        y2=$(echo "$path" | grep -oE 'L[^,]+,([0-9.]+)' | head -1 | sed 's/.*,//')
        height=$(awk "BEGIN {h=$y1-$y2; if(h<0) h=-h; print h}")
        # Legend typically has height < 200, plot border > 400
        if awk "BEGIN {exit !($height > 10 && $height < 200)}"; then
            echo "$path"
            break
        fi
    done)
JAVA_LEGEND_BOX=$(grep -oE '<rect x="[0-9]+" y="[0-9]+" width="[0-9]+" height="[0-9]+".*(fill="#FFFFFF"|stroke="#000000")' "$JAVA_SVG" | grep 'stroke="#000000"' | head -1 || echo "")

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

# Legend box height comparison
echo ""
echo "Legend Box Height:"
if [ -n "$C_LEGEND_BOX" ]; then
    # C gnuplot uses path coordinates: extract y values from path
    # Path format: M62.92,129.01 L62.92,75.01 ... (top-left to bottom-left)
    C_LEG_Y_TOP=$(echo "$C_LEGEND_BOX" | grep -oE 'L[0-9.]+,[0-9.]+' | head -1 | cut -d',' -f2)
    C_LEG_Y_BOTTOM=$(echo "$C_LEGEND_BOX" | grep -oE 'M[0-9.]+,[0-9.]+' | head -1 | cut -d',' -f2)
    C_LEG_HEIGHT=$(awk "BEGIN {print $C_LEG_Y_BOTTOM - $C_LEG_Y_TOP}")
    echo "  C:    ${C_LEG_HEIGHT}px (from path coordinates)"
fi

if [ -n "$JAVA_LEGEND_BOX" ]; then
    JAVA_LEG_HEIGHT=$(echo "$JAVA_LEGEND_BOX" | grep -oE 'height="[0-9]+"' | sed 's/height="//;s/"//')
    echo "  Java: ${JAVA_LEG_HEIGHT}px (from rect height)"

    # Compare heights if both available
    if [ -n "$C_LEG_HEIGHT" ] && [ -n "$JAVA_LEG_HEIGHT" ]; then
        HEIGHT_DIFF=$(awk "BEGIN {print $JAVA_LEG_HEIGHT - $C_LEG_HEIGHT}")
        if [ "${HEIGHT_DIFF%.*}" != "0" ]; then
            echo "  ⚠️  Height difference: ${HEIGHT_DIFF}px"
            if awk "BEGIN {exit !($HEIGHT_DIFF > 5 || $HEIGHT_DIFF < -5)}"; then
                echo "  ❌ CRITICAL: Legend box height mismatch (>5px difference)"
            fi
        else
            echo "  ✅ Heights match"
        fi
    fi
fi

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

# Check for impulses (vertical lines from baseline)
# Impulses have same x-coordinate at start and end of line
# C gnuplot: single path with multiple "M x,y L x,y2" segments
# Java: separate path elements for each impulse
echo ""
echo "Impulses Detection:"
C_IMPULSES=$(grep -oE "M[0-9.]+,[0-9.]+ L[0-9.]+,[0-9.]+" "$C_SVG" | \
    awk '{gsub(/[ML,]/, " "); if ($1 == $3) count++} END {print count+0}')
JAVA_IMPULSES=$(grep -oE 'd="M [0-9.]+ [0-9.]+ L [0-9.]+ [0-9.]+"' "$JAVA_SVG" | \
    awk '{gsub(/[dML"=]/, " "); if ($1 == $3) count++} END {print count+0}')

echo "  C:    $C_IMPULSES impulse lines detected"
echo "  Java: $JAVA_IMPULSES impulse lines detected"

if [ "$C_IMPULSES" -gt 0 ] && [ "$JAVA_IMPULSES" -eq 0 ]; then
    echo "  ❌ CRITICAL: C has impulses but Java doesn't"
elif [ "$C_IMPULSES" -eq 0 ] && [ "$JAVA_IMPULSES" -gt 0 ]; then
    echo "  ❌ CRITICAL: Java has impulses but C doesn't"
elif [ "$C_IMPULSES" -gt 0 ] && [ "$JAVA_IMPULSES" -gt 0 ]; then
    IMPULSE_DIFF=$((C_IMPULSES - JAVA_IMPULSES))
    if [ "$IMPULSE_DIFF" -ne 0 ]; then
        echo "  ⚠️  Warning: Impulse count differs by $IMPULSE_DIFF"
    else
        echo "  ✅ Impulse counts match"
    fi
fi

# Check point marker scale
echo ""
echo "Point Marker Scale:"
C_POINT_SCALE=$(grep -o "scale([0-9.]*)" "$C_SVG" | head -1 | sed 's/scale(//;s/)//')
JAVA_POINT_SCALE=$(grep -o "scale([0-9.]*)" "$JAVA_SVG" | head -1 | sed 's/scale(//;s/)//')

if [ -n "$C_POINT_SCALE" ] && [ -n "$JAVA_POINT_SCALE" ]; then
    echo "  C:    scale($C_POINT_SCALE)"
    echo "  Java: scale($JAVA_POINT_SCALE)"

    # Compare scales (allowing 0.01 tolerance)
    SCALE_MATCH=$(awk "BEGIN {diff=$C_POINT_SCALE-$JAVA_POINT_SCALE; if(diff<0) diff=-diff; print (diff<0.01)?1:0}")
    if [ "$SCALE_MATCH" -eq 1 ]; then
        echo "  ✅ Point marker scales match"
    else
        echo "  ❌ CRITICAL: Point marker scale mismatch"
    fi
elif [ -n "$C_POINT_SCALE" ]; then
    echo "  C:    scale($C_POINT_SCALE)"
    echo "  Java: No point markers found"
elif [ -n "$JAVA_POINT_SCALE" ]; then
    echo "  C:    No point markers found"
    echo "  Java: scale($JAVA_POINT_SCALE)"
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
    # Use bc for floating point arithmetic
    LEG_X_DIFF=$(echo "$C_LEG_X - $JAVA_LEG_X" | bc -l | sed 's/-//')
    # Convert to integer for comparison
    LEG_X_DIFF_INT=$(echo "$LEG_X_DIFF" | awk '{print int($1)}')
    if [ "$LEG_X_DIFF_INT" -gt 10 ]; then
        echo "❌ Legend position differs by ${LEG_X_DIFF}px horizontally"
        ISSUES=$((ISSUES + 1))
    fi
fi

# Check title font size (compare numeric values, not string format)
if [ -n "$C_FONT_NUM" ] && [ -n "$JAVA_FONT_NUM" ]; then
    # Already compared above, only report if significant difference (> 0.1)
    if [ "$(echo "$DIFF > 0.1" | bc -l)" -eq 1 ] 2>/dev/null; then
        echo "❌ Title font size mismatch: $C_TITLE_FONT vs $JAVA_TITLE_FONT (difference: $DIFF)"
        ISSUES=$((ISSUES + 1))
    fi
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

# ============================================================================
# 9. ADDITIONAL VISUAL QUALITY CHECKS
# ============================================================================
echo ""
echo "═══════════════════════════════════════════════════════════════════"
echo "9. ADDITIONAL VISUAL QUALITY CHECKS"
echo "═══════════════════════════════════════════════════════════════════"

# Check for unnecessary decimal places in axis labels (e.g., -1.0 vs -1)
C_DECIMAL_LABELS=$(grep -o '>-\?[0-9]*\.0<' "$C_SVG" | wc -l | tr -d ' ')
JAVA_DECIMAL_LABELS=$(grep -o '>-\?[0-9]*\.0<' "$JAVA_SVG" | wc -l | tr -d ' ')
if [ "$JAVA_DECIMAL_LABELS" -gt "$C_DECIMAL_LABELS" ]; then
    echo "⚠️  Java shows unnecessary decimal places: $JAVA_DECIMAL_LABELS labels with .0 (C has $C_DECIMAL_LABELS)"
    ISSUES=$((ISSUES + 1))
fi

# Check for top border tick marks (mirror ticks)
C_TOP_TICKS=$(grep -o "M[0-9.]*,66\.[0-9]* L[0-9.]*,75\.[0-9]*" "$C_SVG" | wc -l | tr -d ' ')
JAVA_TOP_TICKS=$(grep -o '<line.*y1="66[^"]*".*y2="[67][0-9][^"]*"' "$JAVA_SVG" | wc -l | tr -d ' ')
if [ "$C_TOP_TICKS" -gt 0 ] && [ "$JAVA_TOP_TICKS" -eq 0 ]; then
    echo "⚠️  Java missing top border ticks: $C_TOP_TICKS expected, 0 found"
    ISSUES=$((ISSUES + 1))
fi

# Check for point markers on plots (exclude definitions with id= attribute)
C_MARKERS=$(grep "use xlink:href='#gpPt" "$C_SVG" | grep -v " id=" | wc -l | tr -d ' ')
JAVA_MARKERS=$(grep "use xlink:href='#gpPt" "$JAVA_SVG" | grep -v ' id=' | wc -l | tr -d ' ')
if [ "$C_MARKERS" -gt 0 ] && [ "$JAVA_MARKERS" -eq 0 ]; then
    echo "❌ Java missing point markers: $C_MARKERS expected, 0 found"
    ISSUES=$((ISSUES + 1))
elif [ "$C_MARKERS" -gt 0 ] && [ "$JAVA_MARKERS" -ne "$C_MARKERS" ]; then
    DIFF=$((C_MARKERS - JAVA_MARKERS))
    if [ $DIFF -lt 0 ]; then DIFF=$((-DIFF)); fi
    if [ $DIFF -le 2 ]; then
        echo "✅ Point marker count close: Java=$JAVA_MARKERS, C=$C_MARKERS (diff=$DIFF)"
    else
        echo "⚠️  Point marker count differs: Java=$JAVA_MARKERS, C=$C_MARKERS (diff=$DIFF)"
    fi
fi

# Check for plot clipping
JAVA_HAS_CLIP=$(grep -o 'clip-path="url(#plotClip)"' "$JAVA_SVG" | wc -l | tr -d ' ')
if [ "$JAVA_HAS_CLIP" -eq 0 ]; then
    echo "⚠️  Java may be missing clip-path for plot area"
    # Don't count as issue since this needs visual verification
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
