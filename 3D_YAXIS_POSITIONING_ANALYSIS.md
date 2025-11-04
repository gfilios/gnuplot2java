# 3D Y-Axis Positioning Analysis

## Issue Summary
The Java implementation's 3D plots appear **vertically offset** (too low on screen) compared to C gnuplot, despite having correct tick marks and labels.

## Current Status
- ✅ All 3 demo tests passing (100%)
- ✅ 3D axes rendering with tick marks and labels
- ✅ Correct number of ticks (11 per axis)
- ❌ Vertical positioning differs from C gnuplot (plot appears too low)
- ❌ Axis positions differ by 735px (comparison report)

## Coordinate Analysis

### Test Environment
- Canvas: 800x600 pixels
- Plot area margins:
  - Left: 54px
  - Right: 25px
  - Top: 66px
  - Bottom: 36px
- Effective plot area: (54, 66) to (775, 564)

### Java Implementation Origin Coordinates

**With scale=0.7** (current):
- Origin point: (69.78, 358.58)
- X position: 69.78 (near left edge, ~16px from plotLeft=54)
- Y position: 358.58 (vertically centered at ~315, offset by 43.58px)

**With scale=1.0** (tested):
- Origin point: (-77.95, 377.25)
- X position: -77.95 ⚠️ **NEGATIVE - plot overflows viewport!**
- Y position: 377.25 (moved down 19px from scale=0.7)

### C Gnuplot Origin Coordinates

From SVG analysis:
- X-axis labels positioned at Y ~467-536
- This suggests origin Y should be around Y=450-500 range
- Java current: origin Y = 358.58
- **Difference: ~90-140px too high (plot too low on screen)**

## Mathematical Analysis

### Current Mapping (SvgRenderer.java:1637-1643)

```java
private double mapProjectedY(double y) {
    // Map normalized y [-1, 1] to screen coordinates
    // Note: SVG y-axis is inverted (top=0, bottom=height)
    double center = (plotTop + plotBottom) / 2.0;  // = (66 + 564) / 2 = 315
    double height = (plotBottom - plotTop) / 2.0;  // = (564 - 66) / 2 = 249
    return center - y * height;  // Invert y-axis
}
```

### Projection with scale=0.7

For origin at (-1, -1, -1):
1. ViewTransform3D.project() with rotations (60°, -60°) and scale=0.7
2. Returns approximately: (x≈-0.7, y≈-0.7) (not exactly -0.7 due to rotation)
3. mapProjectedY(-0.7) = 315 - (-0.7) * 249 = 315 + 174.3 = **489.3**

But actual Java origin Y = 358.58, not 489.3!

This suggests the ViewTransform3D is returning different values than expected after rotation.

### Why scale=1.0 Makes It Worse

With scale=1.0:
- Projected coordinates fill [-1, 1] range more completely
- X-axis: 414.5 + (-1) * 360.5 = 54 (correct, but rotation pushes it negative)
- Y-axis: 315 - (-1) * 249 = 564 (bottom of viewport)
- The rotation matrix amplifies the overflow

**Result**: Plot becomes too large and extends beyond viewport boundaries.

## Root Cause Analysis

The issue is **not** the scale factor. The issue is a combination of:

1. **Rotation angles (-60° horizontal)**: Creates an asymmetric projection
2. **Viewport centering**: Assumes symmetric projection in X and Y
3. **Missing offset compensation**: C gnuplot likely applies additional offsets to position the rotated 3D box within the viewport

### C Gnuplot Behavior

Looking at C gnuplot's scatter.dem output:
- X-axis labels: Y positions ~467-536 (near bottom of viewport)
- Y-axis labels: spread across the plot
- The 3D box appears positioned lower on screen (Y ~450-500 for origin)

Java positioning (origin Y=358.58) is **~100-140px too high**, making the plot appear lower than it should.

## Root Cause Discovery: C Gnuplot Uses 4/7 Scaling Ratio

After examining C gnuplot source code (graph3d.c:534-545), the **actual issue** is identified:

### C Gnuplot Scaling Algorithm

```c
xmiddle = (plot_bounds.xright + plot_bounds.xleft) / 2;
ymiddle = (plot_bounds.ytop + plot_bounds.ybot) / 2;

/* HBB: Magic number alert! */
xscaler = ((plot_bounds.xright - plot_bounds.xleft) * 4L) / 7L;  // 4/7 ≈ 0.571
yscaler = ((plot_bounds.ytop - plot_bounds.ybot) * 4L) / 7L;    // 4/7 ≈ 0.571

/* If margins explicitly set, use surface_scale instead */
if (tmargin.scalex == screen && bmargin.scalex == screen)
    yscaler = (plot_bounds.ytop - plot_bounds.ybot) / surface_scale;
if (rmargin.scalex == screen && lmargin.scalex == screen)
    xscaler = (plot_bounds.xright - plot_bounds.xleft) / surface_scale;
```

**Key Discovery**: C gnuplot uses **4/7 ≈ 0.571** as the viewport scaler, NOT 0.5 (half) or 1.0 (full)!

### Java Current Implementation

```java
private double mapProjectedX(double x) {
    double center = (plotLeft + plotRight) / 2.0;
    double width = (plotRight - plotLeft) / 2.0;  // Uses 1/2 = 0.5
    return center + x * width;
}

private double mapProjectedY(double y) {
    double center = (plotTop + plotBottom) / 2.0;
    double height = (plotBottom - plotTop) / 2.0;  // Uses 1/2 = 0.5
    return center - y * height;
}
```

**Problem**: Java uses **1/2 = 0.5** for viewport scaling, but C gnuplot uses **4/7 ≈ 0.571**

Combined with ViewTransform3D scale=0.7:
- **Java effective scale**: 0.7 * 0.5 = 0.35
- **C effective scale**: surface_scale * (4/7) ≈ varies by mode

### Why This Matters

The difference in scaling ratios affects:
1. **Plot size**: 4/7 makes plot slightly larger than 1/2
2. **Vertical positioning**: Different aspect ratios position the plot differently
3. **Viewport utilization**: C uses ~57% of available space vs Java's 50%

## Proposed Solutions

### Option 1: Match C Gnuplot's 4/7 Ratio (RECOMMENDED)

Modify `mapProjectedX()` and `mapProjectedY()` to use 4/7 scaling:

```java
private double mapProjectedX(double x) {
    double center = (plotLeft + plotRight) / 2.0;
    double width = ((plotRight - plotLeft) * 4.0) / 7.0;  // Match C: 4/7
    return center + x * width;
}

private double mapProjectedY(double y) {
    double center = (plotTop + plotBottom) / 2.0;
    double height = ((plotBottom - plotTop) * 4.0) / 7.0;  // Match C: 4/7
    return center - y * height;
}
```

**Pros**:
- Directly matches C gnuplot algorithm
- Simple, mathematically correct
- No magic offsets or empirical tuning
- Works for both 2D and 3D plots

**Cons**:
- Changes viewport utilization (minor)
- May affect 2D plot positioning (needs testing)

### Option 2: Use 4/7 Scaling Only for 3D Plots

Apply 4/7 scaling conditionally:

```java
private double mapProjectedX(double x) {
    double center = (plotLeft + plotRight) / 2.0;
    double scale = has3DPlots ? 4.0/7.0 : 0.5;
    double width = (plotRight - plotLeft) * scale;
    return center + x * width;
}
```

**Pros**: Preserves 2D plot behavior
**Cons**: Requires detecting 3D vs 2D plots

### Option 3: Adjust ViewTransform3D Scale to Compensate

Change ViewTransform3D scale from 0.7 to match expected ratio:

```java
// Adjust scale to compensate for 0.5 vs 4/7 difference
return new ViewTransform3D(60, -60, 0.7 * (4.0/7.0) / 0.5, 0.5);
// = 0.7 * 0.571 / 0.5 = 0.7 * 1.142 = 0.8
```

**Pros**: Isolated change
**Cons**: Less clear; mixing concerns; doesn't address root cause

### ~~Option 4: Apply Post-Projection Offset~~ (OBSOLETE)

This approach is **NOT analogous** to C gnuplot. C doesn't use post-projection offsets; it uses different scaling ratios in the projection itself.

## Testing Strategy

For each solution:
1. Measure origin Y coordinate after fix
2. Target: origin Y ≈ 450-500 (matching C gnuplot)
3. Verify all 3 demos still pass
4. Check scatter.dem visual comparison
5. Ensure no clipping or overflow

## Recommendation

**Implement Option 1** (4/7 scaling ratio):
- **Correct solution**: Matches C gnuplot's exact algorithm
- No empirical tuning required
- Mathematically sound
- Clean implementation

This is the proper fix discovered by analyzing C gnuplot source code (graph3d.c:538-539).

## Current Implementation State

Files modified:
- [ViewTransform3D.java:40](gnuplot-java/gnuplot-render/src/main/java/com/gnuplot/render/projection/ViewTransform3D.java#L40)
  - Scale set to 0.7 (working but plot positioned too low)

- [SvgRenderer.java:312-525](gnuplot-java/gnuplot-render/src/main/java/com/gnuplot/render/svg/SvgRenderer.java#L312-L525)
  - render3DAxes() with complete tick marks and labels
  - Uses mapProjectedX/Y for coordinate mapping

- [GnuplotScriptExecutor.java:660-661](gnuplot-java/gnuplot-cli/src/main/java/com/gnuplot/cli/executor/GnuplotScriptExecutor.java#L660-L661)
  - Viewport set to 3D normalized ranges [-1, 1]

## Next Steps

1. ✅ Document analysis (this file)
2. ✅ Discover C gnuplot uses 4/7 scaling ratio
3. ✅ Update analysis with correct solution
4. 🔜 Implement Option 1 (4/7 scaling in mapProjectedX/Y)
5. 🔜 Test with all demos
6. 🔜 Validate with visual comparison
7. 🔜 Measure Y-axis positioning improvement

## References

- Test results: `test-results/latest/comparison_scatter.dem.txt`
- Java SVG output: `test-results/latest/outputs/scatter_java.svg`
- C SVG output: `test-results/latest/outputs/scatter_c.svg`
- Implementation status: `IMPLEMENTATION_STATUS.md`

---
**Date**: 2025-11-03
**Status**: Analysis Complete, Awaiting Implementation
**Priority**: Medium (cosmetic issue, not functional)
