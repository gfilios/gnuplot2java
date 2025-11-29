# 3D Axis Rendering Issue Analysis

## Problem Summary

The point cloud positioning is now correct (1px X, 5px Y difference), proving the transformation matrix and data projection are working properly. However, the 3D axis box has issues:

1. **Axes extend beyond canvas boundaries** (go out of bounds)
2. **Axis positioning doesn't match C Gnuplot**
3. **3D box corners/edges are incorrectly scaled or positioned**
4. **The axis box geometry looks different from C**

## Context

- **plotBounds**: plotLeft=19, plotRight=781, plotTop=39, plotBottom=566
- **Canvas**: 800x600 pixels
- **Point rendering**: NOW CORRECT (matches C perfectly)
- **Transformation matrix**: CORRECT (points prove this)

## Root Cause Analysis

### Issue 1: Axis Corner Coordinates Are Using Wrong Values

**Java Implementation** (SvgRenderer.java:362-384):
```java
int quadrant = (int)(surfaceRotZ / 90);  // surfaceRotZ = 30.0, quadrant = 0

// Determine X coordinates (from quadrant Z-rotation)
if (((quadrant + 1) & 2) != 0) {  // (0+1) & 2 = 1 & 2 = 0 (FALSE)
    zaxisX = 1;   // X_AXIS.max
    rightX = -1;  // X_AXIS.min
} else {
    zaxisX = -1;  // X_AXIS.min  <-- USED (WRONG!)
    rightX = 1;   // X_AXIS.max
}

// Determine Y coordinates (from quadrant Z-rotation)
if ((quadrant & 2) != 0) {  // 0 & 2 = 0 (FALSE)
    zaxisY = 1;   // Y_AXIS.max
    rightY = -1;  // Y_AXIS.min
} else {
    zaxisY = -1;  // Y_AXIS.min  <-- USED (WRONG!)
    rightY = 1;   // Y_AXIS.max
}

// Result:
// origin = (-1, -1, -1)  <-- WRONG!
// xEnd = (1, -1, -1)
// yEnd = (-1, 1, -1)
// zEnd = (-1, -1, 1)
```

**C Gnuplot Implementation** (graph3d.c:2400-2426):
```c
int quadrant = surface_rot_z / 90;  // surface_rot_z = 30, quadrant = 0

if ((quadrant + 1) & 2) {  // (0+1) & 2 = 1 & 2 = 0 (FALSE)
    zaxis_x = X_AXIS.max;
    right_x = X_AXIS.min;
    back_y  = Y_AXIS.min;
    front_y  = Y_AXIS.max;
} else {
    zaxis_x = X_AXIS.min;  <-- USED: -1
    right_x = X_AXIS.max;  <-- USED: 1
    back_y  = Y_AXIS.max;  <-- USED: 1
    front_y  = Y_AXIS.min; <-- USED: -1
}

if (quadrant & 2) {  // 0 & 2 = 0 (FALSE)
    zaxis_y = Y_AXIS.max;
    right_y = Y_AXIS.min;
    back_x  = X_AXIS.max;
    front_x  = X_AXIS.min;
} else {
    zaxis_y = Y_AXIS.min;  <-- USED: -1
    right_y = Y_AXIS.max;  <-- USED: 1
    back_x  = X_AXIS.min;  <-- USED: -1
    front_x  = X_AXIS.max; <-- USED: 1
}

// Result from C gnuplot:
// zaxis_x = -1, zaxis_y = -1
// right_x = 1, right_y = 1
// back_x = -1, back_y = 1
// front_x = 1, front_y = -1
```

**WAIT - The Java and C implementations are producing THE SAME VALUES!**

So the corner selection is actually correct. The issue must be elsewhere.

### Issue 2: 3D Box Corners Are Using Hardcoded [-1, 1] Range

**Java Implementation** (SvgRenderer.java:440-447):
```java
// Define all 8 corners of the 3D box
Point3D[] corners = new Point3D[8];
corners[0] = new Point3D(-1, -1, -1);  // origin
corners[1] = new Point3D(1, -1, -1);   // x-end
corners[2] = new Point3D(-1, 1, -1);   // y-end
corners[3] = new Point3D(1, 1, -1);    // x+y corner (bottom)
corners[4] = new Point3D(-1, -1, 1);   // z-end (from origin)
corners[5] = new Point3D(1, -1, 1);    // x+z corner
corners[6] = new Point3D(-1, 1, 1);    // y+z corner
corners[7] = new Point3D(1, 1, 1);     // top corner
```

**Problem**: These corners are in the NORMALIZED space [-1, 1] which is correct for the ViewTransform3D.project() method. The projection is working correctly (as proven by the point cloud).

### Issue 3: The Real Problem - Wrong Box Edge Drawing Logic

Looking at the C gnuplot code (graph3d.c:2451-2550), I see that C gnuplot draws the box base using:
```c
// the four corners of the base plane
vertex bl, bb, br, bf;

// map to normalized view coordinates the corners of the baseplane:
map3d_xyz(zaxis_x, zaxis_y, base_z, &bl);  // back-left
map3d_xyz(back_x , back_y , base_z, &bb);  // back-back
map3d_xyz(right_x, right_y, base_z, &br);  // back-right
map3d_xyz(front_x, front_y, base_z, &bf);  // back-front
```

With the corner values:
- zaxis_x = -1, zaxis_y = -1
- back_x = -1, back_y = 1
- right_x = 1, right_y = 1
- front_x = 1, front_y = -1

This creates:
- bl = map3d_xyz(-1, -1, base_z) = origin corner
- bb = map3d_xyz(-1, 1, base_z) = y-axis end corner
- br = map3d_xyz(1, 1, base_z) = x+y diagonal corner
- bf = map3d_xyz(1, -1, base_z) = x-axis end corner

Then C gnuplot draws edges:
```c
if (BACKGRID != whichgrid) {
    // Draw front part of base grid, right to front corner:
    if (draw_border & 4)
        draw3d_line(&br, &bf, &border_lp);  // (1,1,base_z) to (1,-1,base_z)
    // ... and left to front:
    if (draw_border & 1)
        draw3d_line(&bl, &bf, &border_lp);  // (-1,-1,base_z) to (1,-1,base_z)
}
if (FRONTGRID != whichgrid) {
    // Draw back part of base grid: left to back corner:
    if (draw_border & 2)
        draw3d_line(&bl, &bb, &border_lp);  // (-1,-1,base_z) to (-1,1,base_z)
    // ... and right to back:
    if (draw_border & 8)
        draw3d_line(&br, &bb, &border_lp);  // (1,1,base_z) to (-1,1,base_z)
}
```

**Java is drawing**:
```java
// X-axis: from origin (corner 0) to x-end (corner 1)
// (-1,-1,-1) to (1,-1,-1)

// Y-axis: from origin (corner 0) to y-end (corner 2)
// (-1,-1,-1) to (-1,1,-1)

// Z-axis: from origin (corner 0) to z-end (corner 4)
// (-1,-1,-1) to (-1,-1,1)

// Additional edge 1: from x-end (corner 1) to x+y corner (corner 3)
// (1,-1,-1) to (1,1,-1)

// Additional edge 2: from y-end (corner 2) to x+y corner (corner 3)
// (-1,1,-1) to (1,1,-1)
```

This is drawing a complete base rectangle! But the problem is that C gnuplot is using **base_z** (not -1) and **floor_z/ceiling_z** for the vertical edges.

### Issue 4: Missing base_z, floor_z, ceiling_z Handling

**C gnuplot defines**:
- `base_z`: The Z coordinate of the XY base plane (where axes start)
- `floor_z`: The minimum Z coordinate (bottom of the box)
- `ceiling_z`: The maximum Z coordinate (top of the box)

From graph3d.c:
```c
map3d_xyz(zaxis_x, zaxis_y, floor_z, &fl);
map3d_xyz(back_x , back_y , floor_z, &fb);
map3d_xyz(right_x, right_y, floor_z, &fr);
map3d_xyz(front_x, front_y, floor_z, &ff);

map3d_xyz(zaxis_x, zaxis_y, ceiling_z, &tl);
map3d_xyz(back_x , back_y , ceiling_z, &tb);
map3d_xyz(right_x, right_y, ceiling_z, &tr);
map3d_xyz(front_x, front_y, ceiling_z, &tf);
```

**Java is using hardcoded -1 and 1** instead of actual data range values!

### Issue 5: Clipping and Viewport Bounds

The C gnuplot code sets:
```c
clip_area = &canvas;
```

And uses `clip_move()` and `clip_vector()` which clip lines to the canvas bounds.

**Java is NOT clipping** - it's drawing the full lines even if they go outside the plotBounds.

## The Real Issues

1. **Java is using normalized [-1, 1] coordinates for box corners** instead of using the actual axis ranges transformed through the same pipeline that works for data points

2. **Java is not using base_z, floor_z, ceiling_z** - it's hardcoding -1 and 1 for Z coordinates

3. **Java is not clipping** the axis lines to the canvas bounds

4. **Java is drawing ALL 12 edges** but C gnuplot only draws the visible edges based on whichgrid (BACKGRID, FRONTGRID, ALLGRID)

## Solution

### Fix 1: Use Actual Axis Range Values (Not Hardcoded [-1, 1])

The 3D box corners should use the actual viewport axis ranges:
```java
double xMin = viewport.getXMin();  // e.g., -1
double xMax = viewport.getXMax();  // e.g., 1
double yMin = viewport.getYMin();  // e.g., -1
double yMax = viewport.getYMax();  // e.g., 1
double zMin = viewport.getZMin();  // e.g., -0.5 (with ticslevel)
double zMax = viewport.getZMax();  // e.g., 1
```

Then define corners using these values:
```java
Point3D[] corners = new Point3D[8];
corners[0] = new Point3D(xMin, yMin, zMin);  // origin
corners[1] = new Point3D(xMax, yMin, zMin);  // x-end
corners[2] = new Point3D(xMin, yMax, zMin);  // y-end
corners[3] = new Point3D(xMax, yMax, zMin);  // x+y corner (bottom)
corners[4] = new Point3D(xMin, yMin, zMax);  // z-end (from origin)
corners[5] = new Point3D(xMax, yMin, zMax);  // x+z corner
corners[6] = new Point3D(xMin, yMax, zMax);  // y+z corner
corners[7] = new Point3D(xMax, yMax, zMax);  // top corner
```

**BUT WAIT**: This won't work! Because the ViewTransform3D.project() method expects ALREADY NORMALIZED coordinates in [-1, 1] range!

Looking at ViewTransform3D.java:56:
```java
public Point2D project(Point3D point) {
    // Normalize coordinates to [-1, 1] range (assumes data is already normalized)
    double x = point.x();
    double y = point.y();
    double z = point.z() * zscale;
```

The comment says "assumes data is already normalized"!

So the issue is NOT in the box corner definition. The corners ARE correct at [-1, 1].

### The REAL Issue: mapProjectedX/Y Scaling

Let me recalculate what the corners should map to in screen space:

**Viewport**:
- plotLeft = 19
- plotRight = 781
- plotTop = 39
- plotBottom = 566

**Current mapping (4/7 scaling)**:
```java
double xscaler = (plotRight - plotLeft) * 4.0 / 7.0;  // (781-19) * 4/7 = 762 * 4/7 = 435.4
double xmiddle = (plotLeft + plotRight) / 2.0;        // (19+781)/2 = 400

double yscaler = (plotBottom - plotTop) * 4.0 / 7.0;  // (566-39) * 4/7 = 527 * 4/7 = 301.1
double ymiddle = (plotTop + plotBottom) / 2.0;        // (39+566)/2 = 302.5
```

For corner at x=-1:
```
screenX = -1 * 435.4 + 400 = 400 - 435.4 = -35.4  <-- NEGATIVE! Outside canvas!
```

For corner at x=1:
```
screenX = 1 * 435.4 + 400 = 835.4  <-- Beyond plotRight=781!
```

**THIS IS THE PROBLEM!**

The 4/7 scaling is making the box TOO LARGE for the plot bounds!

Let me check what C gnuplot would calculate with the same plot_bounds:

C gnuplot (graph3d.c:538-539):
```c
xscaler = ((plot_bounds.xright - plot_bounds.xleft) * 4L) / 7L;
yscaler = ((plot_bounds.ytop - plot_bounds.ybot) * 4L) / 7L;
```

With:
- plot_bounds.xleft = 54 (from C gnuplot, not Java's 19!)
- plot_bounds.xright = 775
- plot_bounds.ybot = 36
- plot_bounds.ytop = 564

C gnuplot:
```
xscaler = (775 - 54) * 4 / 7 = 721 * 4/7 = 412.0
yscaler = (564 - 36) * 4 / 7 = 528 * 4/7 = 301.7
```

**The plot bounds are different between Java and C!**

Java: plotLeft=19, plotRight=781 (width=762)
C: plotLeft=54, plotRight=775 (width=721)

**Root cause**: The Java margin calculation is different from C gnuplot!

## Solution

The real fix is to ensure the margins match C gnuplot. Looking at the Java code (SvgRenderer.java:74-76):
```java
this.plotLeft = h_char * 2 + h_tic;  // ≈ 19
this.plotRight = scene.getWidth() - h_char * 2 - h_tic;  // ≈ 781
this.plotTop = v_char * (titleLines + 2);  // ≈ 39 with title, 26 without
```

And the C gnuplot code sets margins based on axis labels, tics, etc. The margins need to match!

**But wait** - the points are rendering correctly with these margins. So the margins themselves are not the issue.

The issue is that **the 3D box should fit WITHIN the plot bounds**, but with 4/7 scaling of the plot bounds, the box extends beyond them.

## The Actual Fix

The ViewTransform3D should be scaling the 3D box to fit within the plot bounds. Currently it's using scale=1.0:

```java
public static ViewTransform3D gnuplotDefault() {
    return new ViewTransform3D(60, 30, 1.0, 0.5);
}
```

With the 4/7 viewport scaling, this makes the box too large. The scale should be adjusted so that:

```
max_projected_coordinate * xscaler <= plot_width / 2
1.0 * xscaler <= (plotRight - plotLeft) / 2

xscaler = (plotRight - plotLeft) * 4 / 7
1.0 * (plotRight - plotLeft) * 4 / 7 <= (plotRight - plotLeft) / 2
4/7 <= 1/2
0.571 <= 0.5  <-- FALSE!
```

**The 4/7 scaling makes the box TOO LARGE!**

To fit within the plot bounds:
```
required_scale * 4/7 <= 1/2
required_scale <= (1/2) / (4/7) = (1/2) * (7/4) = 7/8 = 0.875
```

So the ViewTransform3D scale should be 0.875 or less!

**BUT** - the points are rendering correctly with scale=1.0. This means the points are using a different code path that works.

Let me trace through the point rendering...

Actually, looking at the modified SvgRenderer.java, I see that the point rendering now uses a different method that normalizes the data points through the full C gnuplot pipeline. But the axis rendering is still using the simple ViewTransform3D.project() method.

## Final Solution

**Option 1**: Scale down the ViewTransform3D to 7/8 = 0.875
**Option 2**: Use the same normalization pipeline for axis corners as for data points
**Option 3**: Adjust mapProjectedX/Y to use a different scaling factor

The correct solution is **Option 2**: The axis corners should go through the same transformation pipeline as the data points. This means using map3d_xyz_double() instead of ViewTransform3D.project().

Looking at SvgRenderer.java, I see there's already a map3d_xyz_double() method! It's just not being used for the axis corners.

The fix:
1. Replace ViewTransform3D.project() calls with map3d_xyz_double() for all axis corners
2. This will ensure axes use the same transformation as data points
3. The axes will then fit correctly within the plot bounds
