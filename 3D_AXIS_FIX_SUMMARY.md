# 3D Axis Rendering Fix - Summary

## Problem

The 3D point cloud is correctly positioned (1px X, 5px Y difference from C gnuplot), but the 3D axis box has issues:

1. **Axes extend beyond canvas boundaries** (go out of bounds)
2. **Axis positioning doesn't match C Gnuplot**
3. **3D box corners/edges are incorrectly scaled or positioned**

## Root Cause

The Java implementation has **TWO DIFFERENT PROJECTION PIPELINES**:

### Pipeline 1: Points (CORRECT - Recently Fixed)
```
Data Point → map3d_to_screen() → Screen Coordinates
```

The `map3d_to_screen()` method (SvgRenderer.java:1610-1692) implements the complete C gnuplot pipeline:
1. Normalize data to [-1, 1] using axis ranges
2. Apply transformation matrix (rotation + scale)
3. Apply TERMCOORD mapping with 4/7 viewport scaling
4. Result: Points fit perfectly within plot bounds

### Pipeline 2: Axes (BROKEN - Current Issue)
```
Normalized Corner [-1,1] → ViewTransform3D.project() → mapProjectedX/Y() → Screen Coordinates
```

The axes use:
1. **ViewTransform3D.project()** - Simple rotation matrix (no data normalization)
2. **mapProjectedX/Y()** - Applies 4/7 viewport scaling

**The Problem**: The axes skip the data normalization step that points use!

## Mathematical Analysis

With plotLeft=19, plotRight=781, the axis corners at normalized coordinates [-1, 1] map to:

```
xscaler = (781 - 19) * 4/7 = 762 * 4/7 = 435.4
xmiddle = (19 + 781) / 2 = 400

For x = -1:
  screenX = -1 * 435.4 + 400 = -35.4  ← NEGATIVE! Outside canvas!

For x = 1:
  screenX = 1 * 435.4 + 400 = 835.4  ← Beyond plotRight=781!
```

**The axes are 14% too large** because they're using the full viewport width (762px) with 4/7 scaling, resulting in 435px range from center, but the plot area is only 381px from center (762/2).

## Why Points Work But Axes Don't

Points work because `map3d_to_screen()` applies the complete transformation pipeline including data normalization. The transformation matrix includes a `surface_scale / 2.0` factor that compensates for the 4/7 viewport scaling:

From compute_transformation_matrix():
```java
double scale_factor = surface_scale / 2.0;  // Divides by 2!
mat_scale(scale_factor, scale_factor, scale_factor, mat);
```

This `/2.0` factor compensates for the 4/7 viewport scaling:
- Without compensation: range would be ±(4/7) = ±0.571 of viewport
- With `/2.0` compensation: range becomes ±(4/7)/2 = ±0.286 of viewport
- This keeps points within plot bounds

But the axes bypass this normalization and use raw [-1, 1] coordinates!

## Solution

**Replace ViewTransform3D.project() with map3d_to_screen() for all axis corners.**

### Current Code (BROKEN):
```java
// Define 3D axis endpoints in normalized coordinates [-1, 1]
Point3D origin = new Point3D(zaxisX, zaxisY, -1);
Point3D xEnd = new Point3D(rightX, zaxisY, -1);
Point3D yEnd = new Point3D(zaxisX, rightY, -1);
Point3D zEnd = new Point3D(zaxisX, zaxisY, 1);

// Project to 2D using ViewTransform3D
ViewTransform3D.Point2D originProj = viewTransform.project(origin);
ViewTransform3D.Point2D xProj = viewTransform.project(xEnd);
ViewTransform3D.Point2D yProj = viewTransform.project(yEnd);
ViewTransform3D.Point2D zProj = viewTransform.project(zEnd);

// Map to screen coordinates
double ox = mapProjectedX(originProj.x());
double oy = mapProjectedY(originProj.y());
```

### Fixed Code:
```java
// Get axis ranges from viewport
double xMin = viewport.getXMin();
double xMax = viewport.getXMax();
double yMin = viewport.getYMin();
double yMax = viewport.getYMax();
double zMin = viewport.getZMin();  // Includes ticslevel adjustment
double zMax = viewport.getZMax();

// Define 3D axis endpoints in DATA coordinates (NOT normalized)
// Use actual axis ranges, not [-1, 1]

// For axis corners, use the setup_3d_box_corners logic:
// zaxisX, zaxisY, rightX, rightY are in normalized [-1, 1] space
// Convert to data coordinates:
double originX = (zaxisX < 0) ? xMin : xMax;
double originY = (zaxisY < 0) ? yMin : yMax;
double xEndX = (rightX < 0) ? xMin : xMax;
double xEndY = (zaxisY < 0) ? yMin : yMax;
double yEndX = (zaxisX < 0) ? xMin : xMax;
double yEndY = (rightY < 0) ? yMin : yMax;

// Project to screen using the SAME pipeline as points
double[] originScreen = map3d_to_screen(originX, originY, zMin, viewport);
double[] xEndScreen = map3d_to_screen(xEndX, xEndY, zMin, viewport);
double[] yEndScreen = map3d_to_screen(yEndX, yEndY, zMin, viewport);
double[] zEndScreen = map3d_to_screen(originX, originY, zMax, viewport);

double ox = originScreen[0];
double oy = originScreen[1];
double xx = xEndScreen[0];
double xy = xEndScreen[1];
// ... etc
```

## Additional Issues to Fix

1. **8 Box Corners**: All 8 corners should use `map3d_to_screen()` instead of `ViewTransform3D.project()`

2. **Tick Marks**: The tick positions along axes should also use `map3d_to_screen()` for consistency

3. **Z Coordinate**: Currently using hardcoded -1 and 1, should use zMin and zMax from viewport

## Expected Result

After the fix:
- Axes will fit within plot bounds
- Axes will match C gnuplot positioning exactly
- No clipping needed (axes won't extend beyond canvas)
- Box edges will align with data point rendering

## Files to Modify

1. **SvgRenderer.java** - render3DAxes() method (lines 341-600)
   - Replace ViewTransform3D.project() calls with map3d_to_screen()
   - Use viewport axis ranges instead of hardcoded [-1, 1]
   - Apply setup_3d_box_corners logic to data coordinates

## Implementation Steps

1. Extract axis ranges from viewport
2. Convert normalized corner coordinates [-1, 1] to data coordinates using axis ranges
3. Replace all ViewTransform3D.project() calls with map3d_to_screen()
4. Remove mapProjectedX/mapProjectedY calls (map3d_to_screen already does this)
5. Test with scatter.dem to verify axes match C gnuplot

## References

- **Working code**: map3d_to_screen() in SvgRenderer.java:1610-1692
- **C gnuplot**: graph3d.c:setup_3d_box_corners() and draw_3d_graphbox()
- **C gnuplot**: util3d.c:map3d_xyz() and util3d.h:TERMCOORD macro
