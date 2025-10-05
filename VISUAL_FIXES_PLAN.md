# Visual Differences Fix Plan

**Created**: 2025-10-05
**Status**: In Progress
**Goal**: Achieve pixel-perfect visual match between C and Java gnuplot outputs

---

## Issues Identified

### 1. ❌ Axis Label Decimal Formatting
**Problem**: Java shows unnecessary decimal places (e.g., `-1.0` instead of `-1`)

**Evidence**:
```
C:    -1.5, -1, -0.5, 0, 0.5, 1, 1.5
Java: -1.5, -1.0, -0.5, 0, 0.5, 1.0, 1.5
```

**Root Cause**: `TickGenerator.formatTickLabel()` uses different precision logic than C

**C Reference**: `gnuplot-c/src/graphics.c:gprintf()`

**Fix Location**:
- File: `gnuplot-java/gnuplot-render/src/main/java/com/gnuplot/render/axis/TickGenerator.java`
- Method: `formatTickLabel(double value, double tickStep)` (lines 625-648)

**Fix Strategy**:
1. If value is integer (Math.abs(value - Math.round(value)) < 1e-9), format as integer
2. Otherwise use minimum decimal places needed
3. Never show unnecessary trailing zeros

**Priority**: HIGH (affects all plots)
**Estimated Effort**: 1 SP

---

### 2. ❌ Top Border Tick Marks Missing
**Problem**: Java doesn't render tick marks on top and right borders

**Evidence**:
```
C:    Has tick marks on all 4 sides of plot border
Java: Only has tick marks on bottom and left
```

**Root Cause**: Axis rendering only draws ticks on primary sides

**C Reference**: `gnuplot-c/src/graphics.c:axis_output_tics()` with mirror ticks

**Fix Location**:
- File: `gnuplot-java/gnuplot-render/src/main/java/com/gnuplot/render/svg/SvgRenderer.java`
- Method: `renderAxis()` or `Axis.java:generateTicks()`

**Fix Strategy**:
1. Add `mirrorTicks` parameter to Axis
2. When rendering X-axis, also draw ticks at top border (y=yMin)
3. When rendering Y-axis, also draw ticks at right border (x=xMax)
4. Mirror ticks should not have labels

**Priority**: MEDIUM (visual polish)
**Estimated Effort**: 2 SP

---

### 3. ❌ Plot 3: Second Graph Missing Markers
**Problem**: `acos(x)` plot should have point markers but doesn't

**Evidence**:
```
C:    First plot (asin) uses gpPt5 (circle), second (acos) uses gpPt7 (triangle)
Java: No markers visible on either plot
```

**Root Cause**: Plot style determination logic or marker rendering

**C Reference**: Plot command parsing for implicit point markers

**Fix Location**:
- File: `gnuplot-java/gnuplot-cli/src/main/java/com/gnuplot/cli/executor/GnuplotScriptExecutor.java`
- Method: `visitPlotCommand()` - plot style logic (lines 192-213)

**Fix Strategy**:
1. Check simple.dem: `plot [-3:5] asin(x),acos(x)`
2. Verify if C uses default markers for function plots
3. May need to parse plot index and apply different styles per plot
4. Or check if "set style function" affects markers

**Priority**: HIGH (functional difference)
**Estimated Effort**: 3 SP

---

### 4. ❌ Plot 5: Legend Box Too Small
**Problem**: Legend box width doesn't accommodate long text

**Evidence**:
```
C:    width=243.92, height=18
Java: width=160, height=38
```

**Root Cause**: Legend width calculation doesn't measure text properly

**C Reference**: `gnuplot-c/src/graphics.c:do_key_layout()`

**Fix Location**:
- File: `gnuplot-java/gnuplot-render/src/main/java/com/gnuplot/render/elements/Legend.java`
- Method: Constructor or box dimension calculation

**Fix Strategy**:
1. Measure actual text width using font metrics
2. Add padding (typically 2-4 characters worth)
3. Ensure minimum width based on longest entry
4. Check C algorithm for box sizing

**Priority**: HIGH (text clipping issue)
**Estimated Effort**: 2 SP

---

### 5. ❌ Plots Extend Outside Border Box
**Problem**: Graph lines are not clipped to plot area boundary

**Evidence**:
```
C:    All plots clipped at border edges
Java: Lines extend beyond plot border
```

**Root Cause**: Missing SVG clip-path or incorrect viewport bounds

**C Reference**: SVG clip-path generation in terminal drivers

**Fix Location**:
- File: `gnuplot-java/gnuplot-render/src/main/java/com/gnuplot/render/svg/SvgRenderer.java`
- Method: Plot rendering with clip-path

**Fix Strategy**:
1. Define `<clipPath id="plotClip">` in SVG defs
2. Set clip region to exact plot area (border interior)
3. Apply `clip-path="url(#plotClip)"` to all plot elements
4. Ensure viewport coordinates match border coordinates

**Priority**: HIGH (visual correctness)
**Estimated Effort**: 2 SP

---

### 6. ❌ Plot 7 & 8: Legend Position Wrong
**Problem**: Legend appears at wrong location

**Evidence**:
```
Plot 7:
  C:    Legend at specific position (need to check)
  Java: Legend at default TOP_LEFT

Plot 8:
  C:    Legend position (need to check)
  Java: Legend at default TOP_LEFT
```

**Root Cause**: Plot-specific legend positioning commands not parsed

**C Reference**: `set key <position>` command parsing

**Fix Location**:
- File: `gnuplot-java/gnuplot-cli/src/main/java/com/gnuplot/cli/executor/GnuplotScriptExecutor.java`
- Method: `visitSetCommand()` - key position handling (lines 109-119)

**Fix Strategy**:
1. Check simple.dem for plot 7 & 8 specific commands
2. Verify if position is set differently per plot
3. May need to reset legend position between plots
4. Check if `set key default` is used

**Priority**: MEDIUM (layout correctness)
**Estimated Effort**: 1 SP

---

## Implementation Order

### Phase 1: Critical Fixes (6 SP)
1. **Issue #1**: Axis label decimal formatting (1 SP) ⭐ Quick win
2. **Issue #3**: Plot 3 markers (3 SP) ⭐ Functional
3. **Issue #4**: Legend box size (2 SP) ⭐ Text clipping

### Phase 2: Visual Polish (4 SP)
4. **Issue #5**: Plot clipping (2 SP) ⭐ Visual correctness
5. **Issue #2**: Top border ticks (2 SP)

### Phase 3: Layout Fixes (1 SP)
6. **Issue #6**: Legend positions plot 7 & 8 (1 SP)

**Total Effort**: 11 Story Points

---

## Comparison Script Updates

Add detection for these issues in `compare_deep.sh`:

```bash
# Section 9: ADDITIONAL VISUAL CHECKS

# Check for unnecessary decimals in labels
echo "AXIS LABEL FORMATTING:"
C_DECIMAL_LABELS=$(grep -o '>-\?[0-9]*\.[0-9]<' "$C_SVG" | grep '\.0<' | wc -l)
JAVA_DECIMAL_LABELS=$(grep -o '>-\?[0-9]*\.[0-9]<' "$JAVA_SVG" | grep '\.0<' | wc -l)
if [ "$JAVA_DECIMAL_LABELS" -gt "$C_DECIMAL_LABELS" ]; then
    echo "⚠️  Java shows unnecessary decimals: $JAVA_DECIMAL_LABELS vs $C_DECIMAL_LABELS"
fi

# Check for top/right border ticks
C_TOP_TICKS=$(grep -o "M[0-9.]*,66\.0[0-9] L[0-9.]*,75\.0[0-9]" "$C_SVG" | wc -l)
JAVA_TOP_TICKS=$(grep -o '<line.*y1="66[^"]*".*y2="[0-9.]*"' "$JAVA_SVG" | wc -l)
if [ "$C_TOP_TICKS" -gt 0 ] && [ "$JAVA_TOP_TICKS" -eq 0 ]; then
    echo "❌ Java missing top border ticks: $C_TOP_TICKS expected"
fi

# Check for point markers
C_MARKERS=$(grep -o "use xlink:href='#gpPt[0-9]" "$C_SVG" | wc -l)
JAVA_MARKERS=$(grep -o 'use xlink:href="#gpPt[0-9]' "$JAVA_SVG" | wc -l)
if [ "$C_MARKERS" -gt 0 ] && [ "$JAVA_MARKERS" -eq 0 ]; then
    echo "❌ Java missing point markers: $C_MARKERS expected"
fi

# Check for clipping
JAVA_HAS_CLIP=$(grep -o 'clip-path="url(#plotClip)"' "$JAVA_SVG" | wc -l)
if [ "$JAVA_HAS_CLIP" -eq 0 ]; then
    echo "⚠️  Java may be missing clip-path for plots"
fi
```

---

## Testing Strategy

For each fix:
1. Make change in Java code
2. Run: `mvn test -Dtest=DemoTestSuite#testSimpleDem`
3. Compare specific SVG output
4. Verify fix in comparison report
5. Check no regression in other plots

---

## Success Criteria

All 8 plots in simple.dem should show:
- ✅ Matching axis label formats (no unnecessary decimals)
- ✅ Tick marks on all 4 border sides
- ✅ Correct marker styles per plot
- ✅ Legend boxes sized to fit text
- ✅ All graphs clipped within border
- ✅ Correct legend positions

**Target**: "✅ No critical issues found" for all 8 plots

---

## References

- C gnuplot source: `gnuplot-c/src/graphics.c`, `gnuplot-c/src/axis.c`
- Java implementation: `gnuplot-java/gnuplot-render/src/main/java/`
- Test oracle: `test-results/latest/outputs/simple_*.svg`
- Comparison: `test-results/latest/comparison_simple*.txt`
