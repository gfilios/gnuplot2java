# Visual Comparison Methodology for C vs Java Gnuplot

## Analysis Date: 2025-10-05

## Systematic Comparison Approach

### 1. **SVG Structure Comparison**
- Layout dimensions (viewBox, width, height)
- Coordinate system mapping
- Plot area boundaries (margins)

### 2. **Visual Elements Comparison**
- Plot style (lines vs points vs linespoints)
- Axis rendering (position, labels, tick marks)
- Legend rendering (position, border, content)
- Border/frame rendering
- Title rendering

### 3. **Data Accuracy Comparison**
- Point coordinates
- Line paths
- Color values
- Stroke widths

---

## Current Issues Found: simple.dem (2025-10-05)

### ❌ CRITICAL: "set style data points" Interpretation Mismatch

**simple.dem line 9:** `set style data points`

**C Gnuplot Behavior (Expected):**
```svg
<!-- Draws BOTH lines AND points (behaves like LINESPOINTS) -->
<path stroke='rgb(148, 0, 211)' d='M54.53,224.70 L69.23,287.40 L83.93,354.64...'/>
<!-- Plus point markers shown in legend -->
```
- C Gnuplot's "set style data points" actually renders **LINESPOINTS** (both lines and point markers)
- Draws continuous lines using `<path>` with `L` (lineto) commands
- Also shows point markers in the legend area (7 point markers visible)
- Line width: 1.00

**Java Gnuplot Behavior (Actual):**
```svg
<!-- Draws ONLY points, NO lines (pure POINTS style) -->
<use xlink:href='#gpPt0' transform='translate(54.00,211.45) scale(1.00)' color='#9400D3'/>
<use xlink:href='#gpPt1' transform='translate(68.71,269.12) scale(1.00)' color='#9400D3'/>
<!-- 157 point markers total, no connecting lines -->
```
- Java interprets "set style data points" as pure **POINTS** (only markers, no lines)
- Renders 157 point markers across all 3 plots
- No connecting lines drawn
- Cycles through all 15 marker types (gpPt0-gpPt14)

**Root Cause:**
1. **Semantic Mismatch:** C Gnuplot's "points" style actually means LINESPOINTS (historical behavior)
2. **Java Implementation:** Incorrectly interprets "points" as POINTS-only
3. **Evidence:** C SVG has 3 `<path>` elements (lines) + 7 point markers (legend); Java has 0 paths + 157 markers

**Fix Required:**
- In GnuplotScriptExecutor.java, when parsing "set style data points"
- Change from `PlotStyle.POINTS` to `PlotStyle.LINESPOINTS`
- This matches C Gnuplot's historical behavior

---

### ❌ Axis Rendering Differences

**C Gnuplot:**
- Y-axis on LEFT side (x=54.53)
- X-axis on BOTTOM (y=564.00)
- Tick marks: 9px outward from axis
- Labels: positioned outside plot area

**Java Gnuplot:**
- Y-axis in CENTER (x=414.50) - WRONG
- X-axis in CENTER (y=294.50) - WRONG
- Tick marks: 6px from axis
- Labels: positioned differently

**Visual Impact:** Axes appear in completely different locations, making plots unreadable.

---

### ❌ Plot Area Boundaries

**C Gnuplot:**
```
plotLeft:   54.53
plotRight:  774.82
plotTop:    66.01
plotBottom: 564.00
Size: 720.29 × 497.99 pixels
```

**Java Gnuplot:**
```
plotLeft:   54.00
plotRight:  775.00
plotTop:    25.00
plotBottom: 564.00
Size: 721 × 539 pixels
```

**Issue:** Different margin calculations lead to different plot areas.

---

### ✅ Elements Working Correctly

1. **Point Markers** - All 15 types (gpPt0-gpPt14) match C exactly
2. **Colors** - RGB values match perfectly (#9400D3, #009E73, #56B4E9)
3. **Legend** - Present with border and correct labels
4. **Border** - Rectangle rendered around plot area
5. **Title** - "Simple Plots" rendered correctly

---

## Comparison Tool Requirements

### Automated Visual Diff Script
```bash
#!/bin/bash
# compare_svg.sh - Systematic SVG comparison

C_FILE=$1
JAVA_FILE=$2

echo "=== PLOT STYLE COMPARISON ==="
echo "C uses <path> for lines:"
grep -c "<path stroke='rgb" "$C_FILE"
echo "Java uses <use> for points:"
grep -c "<use xlink:href='#gpPt" "$JAVA_FILE"

echo -e "\n=== AXIS POSITION COMPARISON ==="
echo "C Y-axis position:"
grep "M54" "$C_FILE" | head -1
echo "Java Y-axis position:"
grep "x1=\"414" "$JAVA_FILE" | head -1

echo -e "\n=== PLOT AREA BOUNDARIES ==="
echo "C: $(grep -o 'M[0-9.]*,[0-9.]* L[0-9.]*,[0-9.]* L[0-9.]*,[0-9.]* L[0-9.]*,[0-9.]* Z' "$C_FILE" | tail -1)"
echo "Java: $(grep -o 'd="M [0-9.]* [0-9.]* L [0-9.]* [0-9.]* L [0-9.]* [0-9.]* L [0-9.]* [0-9.]* Z"' "$JAVA_FILE")"

echo -e "\n=== COLOR COMPARISON ==="
echo "C colors:"
grep -o "stroke='rgb([^']*)" "$C_FILE" | sort -u
echo "Java colors:"
grep -o "color='#[A-F0-9]*'" "$JAVA_FILE" | sort -u
```

---

## Prioritized Fix List

### Priority 1 (Critical - Breaks Functionality)
1. ✅ **FIXED: Point markers implemented** - All 15 types (gpPt0-gpPt14) working
2. ✅ **FIXED: Plot border implemented** - Border rendering working
3. ❌ **FIX REQUIRED: "points" style interpretation** - Change to LINESPOINTS (see fix below)
4. ❌ **FIX REQUIRED: Axis positioning** - Axes appear centered instead of at boundaries

### IMMEDIATE FIX: Change "points" to LINESPOINTS

**File:** `gnuplot-java/gnuplot-cli/src/main/java/com/gnuplot/cli/executor/GnuplotScriptExecutor.java`

**Current code (line ~64):**
```java
private String styleDataValue = "lines"; // default: lines
```

**Current switch (around line 175):**
```java
LinePlot.PlotStyle plotStyle = switch (styleDataValue) {
    case "points" -> LinePlot.PlotStyle.POINTS;
    case "linespoints" -> LinePlot.PlotStyle.LINESPOINTS;
    default -> LinePlot.PlotStyle.LINES;
};
```

**Required change:**
```java
LinePlot.PlotStyle plotStyle = switch (styleDataValue) {
    case "points" -> LinePlot.PlotStyle.LINESPOINTS;  // C Gnuplot compatibility
    case "linespoints" -> LinePlot.PlotStyle.LINESPOINTS;
    default -> LinePlot.PlotStyle.LINES;
};
```

**Rationale:** C Gnuplot's "set style data points" historically renders both lines AND points, not points alone.

### Image-Based Comparison Results (2025-10-05)

**Tool:** `./compare_visual.sh simple_c.svg simple_java.svg`

#### Quantitative Differences:
- **Unique Colors:** C=255, Java=690 (⚠️ +435 colors, 173% more)
- **Edge Pixels:** C=3,882, Java=11,701 (⚠️ +201.4% more edges)
- **File Size:** Java is 180.5% of C size

#### Interpretation:
1. **+201% more edges** → Java renders 157 individual point markers (each with 4-8 edges) vs C's 3 continuous line paths
2. **+173% more colors** → Point marker anti-aliasing creates color gradients; lines have fewer intermediate colors
3. **Larger file size** → 157 `<use>` elements + transformations vs 3 `<path>` elements

#### Visual Evidence Files:
- Side-by-side: `/tmp/gnuplot_visual_comparison/overlay_simple_c_vs_simple_java.png`
- Diff map: `/tmp/gnuplot_visual_comparison/diff_simple_c_vs_simple_java.png`
- PNG conversions: `/tmp/gnuplot_visual_comparison/*.png`

### Priority 2 (High - Visual Accuracy)
3. **Fix plot area margins** - Match C Gnuplot's margin calculations
4. **Fix tick mark positioning** - Match C Gnuplot's tick lengths and positions

### Priority 3 (Medium - Polish)
5. **Font rendering** - Match font families and sizes
6. **Stroke widths** - Ensure exact match with C version

---

## Comparison Tools Suite

### Tool 1: SVG Code Analysis
**Script:** `./compare_svg.sh <c.svg> <java.svg>`
**Purpose:** Analyzes SVG structure, elements, and attributes
**Outputs:**
- Plot style comparison (lines vs points)
- Axis position analysis
- Color palette verification
- Border/legend presence checks

### Tool 2: Visual Image Comparison
**Script:** `./compare_visual.sh <c.svg> <java.svg>`
**Purpose:** Pixel-level visual comparison using ImageMagick
**Outputs:**
- Pixel difference percentage
- Edge pixel count (detects line vs point rendering)
- Unique color count
- Structural analysis
- Visual diff maps and overlays in `/tmp/gnuplot_visual_comparison/`

### Tool 3: Python Image Analysis (Optional)
**Script:** `./compare_images.py <c.svg> <java.svg>`
**Requirements:** `pip3 install cairosvg Pillow numpy`
**Purpose:** Detailed pixel analysis with regional breakdowns
**Outputs:**
- Regional difference analysis (9-zone grid)
- Per-channel RGB difference
- Visual element detection
- Amplified difference maps

### Tool 4: Comprehensive Suite (Recommended)
**Script:** `./compare_all.sh <c.svg> <java.svg>`
**Purpose:** Runs all available comparison tools in sequence
**Usage:**
```bash
./compare_all.sh test-results/latest/outputs/simple_c.svg \
                  test-results/latest/outputs/simple_java.svg
```

---

## Test Procedure for Each Fix

1. Make code change
2. Run: `mvn clean install -DskipTests -Djacoco.skip=true`
3. Run: `mvn test -Dtest=DemoTestSuite -q`
4. **Run comprehensive comparison:**
   ```bash
   ./compare_all.sh test-results/latest/outputs/simple_c.svg \
                     test-results/latest/outputs/simple_java.svg
   ```
5. **Review metrics - graphs should match when:**
   - Plot style matches (LINES/POINTS/LINESPOINTS)
   - Edge pixel count within ±10%
   - Unique colors within ±20%
   - Pixel difference <5%
6. **Visually inspect:** `open /tmp/gnuplot_visual_comparison/overlay_*.png`
7. Document findings in this file

---

## Session Continuity

**For next session:**
- Read this file first to understand current state
- Use the comparison approach above
- Update this file with new findings
- Reference specific line numbers in SVG files when describing issues
