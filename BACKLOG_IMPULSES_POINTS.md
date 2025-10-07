# Backlog Story: Implement "with impulses" and fix "with points" rendering

## Problem Statement
The Java gnuplot implementation currently does not properly support the "with impulses" plot style and has incorrect point marker scaling for "with points" style.

### Current Behavior
1. **Impulses**: Rendered as continuous lines (using PlotStyle.LINES)
2. **Points**: Point markers use scale factor 1.00 instead of 4.50

### Expected Behavior (from C gnuplot)
1. **Impulses**: Vertical lines from the baseline (y-axis zero point) to each data point
2. **Points**: Point markers scaled to 4.50 (larger, more visible markers)

## Analysis

### C Gnuplot Implementation

#### Impulses (graphics.c:1213-1260)
```c
plot_impulses(struct curve_points *plot, int yaxis_x, int xaxis_y)
{
    for (i = 0; i < plot->p_count; i++) {
        x = map_x(plot->points[i].x);
        y = map_y(plot->points[i].y);

        if (polar)
            draw_clip_line(yaxis_x, xaxis_y, x, y);
        else
            draw_clip_line(x, xaxis_y, x, y);  // Vertical line from xaxis_y to y
    }
}
```

Key insight: Draws vertical line from `(x, xaxis_y)` to `(x, y)` where `xaxis_y` is the y-coordinate of the x-axis (baseline at y=0).

#### Points (graphics.c:2746-2880)
```c
p_width = t->h_tic * plot->lp_properties.p_size;
p_height = t->v_tic * plot->lp_properties.p_size;
```

Default point size multiplier appears to be around 4.50 based on SVG output analysis.

### Java Implementation Gaps

1. **Missing PlotStyle enum value**: No IMPULSES in LinePlot.PlotStyle (lines 91-98)
2. **Parser maps incorrectly**: GnuplotScriptExecutor line 230 maps "impulses" to LINES
3. **SVG Renderer missing impulses logic**: No code to render vertical lines to baseline
4. **Point marker scale**: SvgRenderer uses scale(1.00) but should use scale(4.50)

## Technical Design

### 1. Add IMPULSES to PlotStyle enum

**File**: `gnuplot-java/gnuplot-render/src/main/java/com/gnuplot/render/elements/LinePlot.java`

```java
public enum PlotStyle {
    /** Render as connected lines only */
    LINES,
    /** Render as point markers only */
    POINTS,
    /** Render as both lines and point markers */
    LINESPOINTS,
    /** Render as vertical lines from baseline to each point */
    IMPULSES
}
```

### 2. Update Parser to recognize "with impulses"

**File**: `gnuplot-java/gnuplot-cli/src/main/java/com/gnuplot/cli/executor/GnuplotScriptExecutor.java`

Line 230: Change from `case "impulses" -> LinePlot.PlotStyle.LINES;`
To: `case "impulses" -> LinePlot.PlotStyle.IMPULSES;`

### 3. Implement IMPULSES rendering in SVG Renderer

**File**: `gnuplot-java/gnuplot-render/src/main/java/com/gnuplot/render/svg/SvgRenderer.java`

Add new rendering logic that:
1. Detects PlotStyle.IMPULSES
2. For each point, draws vertical line from `(x, baselineY)` to `(x, y)`
3. `baselineY` should be the y-coordinate where the y-axis crosses zero (typically `plotBottom` when y-range includes 0)

### 4. Fix point marker scale

**File**: `gnuplot-java/gnuplot-render/src/main/java/com/gnuplot/render/svg/SvgRenderer.java`

Change point marker scale from 1.00 to 4.50 to match C gnuplot output.

## Test Cases

### Validation
Use Plot 4 from simple.dem:
```gnuplot
plot [-30:20] besj0(x)*0.12e1 with impulses, [0:*] (x**besj0(x))-2.5 with points
```

Expected SVG output (from C gnuplot):
1. First plot: Vertical lines (path elements) from y=252.76 (baseline) to each computed y-value
2. Second plot: Point markers (use elements) with `scale(4.50)`

### Comparison Checks
Extend `test-tools/comparison/compare_deep.sh` to:
1. Detect presence of impulses (vertical line segments with same x-coordinate)
2. Check point marker scale attribute
3. Flag mismatches as CRITICAL

## Acceptance Criteria

1. ✅ PlotStyle.IMPULSES enum added
2. ✅ Parser correctly maps "with impulses" to PlotStyle.IMPULSES
3. ✅ SVG renderer draws vertical lines from baseline for impulses
4. ✅ Point markers use scale(4.50) instead of scale(1.00)
5. ✅ Plot 4 Java output visually matches C gnuplot output
6. ✅ Comparison tools detect and flag impulses/points rendering issues
7. ✅ All existing tests continue to pass

## References

- C gnuplot source: `/Users/gfilios/develop/modernization/gnuplot-master/gnuplot-c/src/graphics.c`
- Test script: `/Users/gfilios/develop/modernization/gnuplot-master/gnuplot-c/demo/simple.dem`
- C output: `/Users/gfilios/develop/modernization/gnuplot-master/test-results/latest/outputs/simple_c_004.svg`
- Java output: `/Users/gfilios/develop/modernization/gnuplot-master/test-results/latest/outputs/simple_java_004.svg`
