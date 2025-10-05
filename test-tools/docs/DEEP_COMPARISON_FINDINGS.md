# Deep Element-by-Element Comparison Findings

**Date:** 2025-10-05
**Test File:** simple.dem
**Tool:** `compare_deep.sh`

---

## Summary of ALL Differences Found

### ❌ CRITICAL ISSUES

| Element | C Gnuplot | Java Gnuplot | Impact |
|---------|-----------|--------------|--------|
| **Plot Style** | LINES (3 paths) | POINTS (157 markers) | ❌ **CRITICAL** - Completely different rendering |
| **Title Font Size** | 20.00 | 16 | ❌ Text appears smaller |
| **Title Position** | translate(414.67,33.51) | x="400" y="21" | ❌ Different positioning system |
| **Legend Y Position** | y=129.01 (top) | y=247 (middle) | ❌ **118px lower!** |
| **Border Path** | Complete rectangle | NOT FOUND | ❌ Border missing in search |
| **Y-Axis Ticks** | 7 ticks | 9 ticks | ⚠️ Different tick count |
| **X-Axis Ticks** | 5 ticks | 0 ticks | ❌ **X-axis ticks missing!** |
| **Axis Label Positions** | Left side (x=46) | Center (x=404) | ❌ **Axes in wrong location** |

---

## Detailed Analysis by Element

### 1. Title (Heading)

**Text:** ✅ Both show "Simple Plots"
**Font Family:** ✅ Both use Arial
**Font Size:** ❌ C=20.00, Java=16
**Position:** ❌ Different coordinate systems
- C: `transform="translate(414.67,33.51)"`
- Java: `x="400" y="21"`

**Impact:** Title appears 20% smaller in Java version

---

### 2. Plot Border/Frame

**C Gnuplot:**
```
M54.53,66.01 L54.53,564.00 L774.82,564.00 L774.82,66.01 L54.53,66.01 Z
```
- Complete rectangle: (54.53,66.01) → (774.82,564.00)
- Forms proper border around plot area

**Java Gnuplot:**
- Border path NOT detected by search pattern
- Need to verify if border exists with different pattern
- Possible issue with SVG structure

**Impact:** Plot may not have visible border frame

---

### 3. Axes - Y-Axis

**Tick Marks:**
- C: 7 ticks at x=54.53 (left side)
- Java: 9 ticks at x=414.50 (center!)

**Label Positions:**
- C: `translate(46.14,567.90)` - left of plot area
- Java: `x="404.50" y="527.49"` - center of canvas!

**Values:** Both show -1.5, -1, -0.5, 0, 0.5, 1, 1.5

**Impact:** ❌ **CRITICAL** - Java Y-axis is centered instead of on left edge

---

### 4. Axes - X-Axis

**Tick Marks:**
- C: 5 ticks at y=564.00 (bottom)
- Java: 0 ticks detected at y=294.50

**Label Positions:**
- C: `translate(54.53,585.90)` - below plot area
- Java: `x="54.00" y="312.50"` - middle of canvas!

**Values:** -10, -5, 0, 5, 10

**Impact:** ❌ **CRITICAL** - Java X-axis is centered vertically instead of at bottom

---

### 5. Legend (Key)

**Box Position:**
- C: x=62.92, y=129.01 (top-left corner)
- Java: x=59, y=247 (middle-left area)
- **Δy = 118 pixels lower in Java!**

**Box Style:**
- C: `M62.92,129.01 L62.92,75.01 L222.94,75.01 L222.94,129.01` (path)
- Java: `<rect x="59" y="247" width="170" height="95"...>` (rect)
- Both have black stroke ✅

**Entries:**
- ✅ Both have 3 entries: sin(x), atan(x), cos(atan(x))
- ❌ Positions differ due to different legend location

**Impact:** Legend appears in different location, disrupting layout

---

### 6. Plot Lines/Points

**Rendering Mode:**
- C: Lines=3, Points=7 (LINESPOINTS in legend)
- Java: Lines=19 (?), Points=157 (POINTS-only rendering)

**Stroke Widths:**
- C: "1.00", "2.00" (decimal format)
- Java: "1", "1.0", "2" (mixed format)

**Colors:** ✅ Identical
- #9400D3 (purple)
- #009E73 (green)
- #56B4E9 (blue)

**Impact:** ❌ **CRITICAL** - Completely different visual appearance

---

### 7. Fonts

**Font Family:** ✅ Both use Arial

**Font Sizes Used:**
- C: 12.00 (labels), 20.00 (title)
- Java: 10 (labels), 16 (title)

**Impact:** All text 16-20% smaller in Java

---

### 8. Coordinate System

**ViewBox:** ✅ Both use `viewBox="0 0 800 600"`
**SVG Dimensions:** ✅ Both are 800×600

**Plot Area (from border):**
- C: 54.53 to 774.82 (width=720.29), 66.01 to 564.00 (height=497.99)
- Java: Need to extract from actual border path

---

## Root Causes Identified

### 1. **Axis Coordinate System Bug**
Java is rendering axes in the CENTER of the canvas instead of at plot boundaries:
- Y-axis should be at x=54 (left), but renders at x=414 (center)
- X-axis should be at y=564 (bottom), but renders at y=294 (center)

### 2. **Plot Style Misinterpretation**
- `set style data points` renders as POINTS-only in Java
- Should render as LINESPOINTS (both lines and points) like C

### 3. **Font Scaling Issue**
- All fonts 16-20% smaller in Java
- Title: 20→16, Labels: 12→10

### 4. **Legend Positioning**
- Java legend 118px lower than C version
- Different coordinate calculation system

---

## Fix Priority

### PRIORITY 1 (Blocking)
1. ✅ **Fix axis positioning** - Axes must be at plot boundaries, not centered
2. ✅ **Fix plot style interpretation** - "points" → LINESPOINTS
3. ✅ **Fix X-axis tick marks** - Currently showing 0, should show 5

### PRIORITY 2 (High)
4. ✅ **Fix font sizes** - Match C Gnuplot sizing (20 for title, 12 for labels)
5. ✅ **Fix legend position** - Should be at y=129, not y=247

### PRIORITY 3 (Medium)
6. ✅ **Verify border rendering** - Ensure border path is correct
7. ✅ **Standardize stroke widths** - Use consistent decimal format

---

## Verification Checklist

After fixes, verify using `compare_deep.sh`:

- [ ] Title font-size="20.00" (not 16)
- [ ] Title at y≈33 (not y=21)
- [ ] Y-axis at x≈54 (not x=414)
- [ ] X-axis at y≈564 (not y=294)
- [ ] Y-axis tick count = 7 (not 9)
- [ ] X-axis tick count = 5 (not 0)
- [ ] Legend at y≈129 (not y=247)
- [ ] Plot style = LINES (3 paths, not 157 points)
- [ ] Border rectangle present and correct

---

## Combined with Image Analysis

**From compare_visual.sh:**
- Edge pixels: C=3,882 vs Java=11,701 (+201%)
- Unique colors: C=255 vs Java=690 (+173%)

These metrics **confirm** the plot style issue:
- 201% more edges = individual point markers vs continuous lines
- 173% more colors = point anti-aliasing creating color gradients

---

## Next Steps

1. **Fix axis rendering system** - Review SvgRenderer axis drawing logic
2. **Fix "points" style mapping** - Change to LINESPOINTS in GnuplotScriptExecutor
3. **Fix font sizes** - Review font rendering constants
4. **Fix legend positioning** - Review legend placement calculation
5. **Re-run all comparisons** to verify fixes

Use:
```bash
./compare_all.sh test-results/latest/outputs/simple_c.svg \
                  test-results/latest/outputs/simple_java.svg
```
