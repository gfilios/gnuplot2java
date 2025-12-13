/*
 * Gnuplot Java - Contour Extractor
 * Port of gnuplot-c/src/contour.c
 *
 * Original Authors:
 *   Gershon Elber (original software)
 *   Hans-Martin Keller, 1995, 1997 (numerical improvements)
 */
package com.gnuplot.core.grid;

import com.gnuplot.core.geometry.Point3D;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Extracts contour lines from 3D gridded surface data.
 * <p>
 * This implementation uses triangulation-based edge detection, ported from
 * gnuplot-c/src/contour.c. The algorithm:
 * <ol>
 *   <li>Triangulates the grid (2 triangles per cell)</li>
 *   <li>Marks edges that cross the contour level</li>
 *   <li>Traces contours by following active edges through triangles</li>
 *   <li>Optionally applies smoothing (cubic spline or B-spline)</li>
 * </ol>
 * </p>
 *
 * @see <a href="file:///gnuplot-c/src/contour.c">contour.c</a>
 */
public class ContourExtractor {

    private static final double EPSILON = 1.0e-5;

    /** Position of edge in mesh */
    private enum EdgePosition {
        INNER_MESH,
        BOUNDARY,
        DIAGONAL
    }

    /** Edge structure - each edge belongs to up to 2 polygons */
    private static class Edge {
        Polygon[] poly = new Polygon[2];
        Point3D[] vertex = new Point3D[2];
        Edge next;
        boolean isActive;
        EdgePosition position = EdgePosition.INNER_MESH;
    }

    /** Polygon (triangle) structure */
    private static class Polygon {
        Edge[] edge = new Edge[3];
        Polygon next;
    }

    /** Temporary contour point during tracing */
    private static class CntrPoint {
        double x, y;
        CntrPoint next;

        CntrPoint(double x, double y) {
            this.x = x;
            this.y = y;
        }
    }

    // State during extraction
    private double zMin, zMax;
    private double unitX, unitY;
    private Edge edgesHead;
    private Polygon polysHead;

    /**
     * Extract contour lines from gridded 3D data.
     *
     * @param grid   Array of Point3D in row-major order (row * cols + col)
     * @param rows   Number of rows in the grid
     * @param cols   Number of columns in the grid
     * @param params Contour parameters (levels, interpolation, etc.)
     * @return List of contour lines
     */
    public List<ContourLine> extract(Point3D[] grid, int rows, int cols, ContourParams params) {
        if (grid == null || grid.length == 0 || rows < 2 || cols < 2) {
            return Collections.emptyList();
        }

        List<ContourLine> result = new ArrayList<>();

        // Calculate min/max values
        calcMinMax(grid, rows, cols);

        // Generate triangulation
        generateTriangles(grid, rows, cols);

        // Calculate contour levels
        double[] levels = calculateLevels(params);

        if (levels.length == 0) {
            return result;
        }

        // Sort levels high-to-low if requested
        if (params.isSortLevels()) {
            Arrays.sort(levels);
            // Reverse for high-to-low
            for (int i = 0; i < levels.length / 2; i++) {
                double temp = levels[i];
                levels[i] = levels[levels.length - 1 - i];
                levels[levels.length - 1 - i] = temp;
            }
        }

        // Generate contours for each level
        for (double zLevel : levels) {
            List<ContourLine> levelContours = generateContoursAtLevel(zLevel, params);
            result.addAll(levelContours);
        }

        // Clean up
        edgesHead = null;
        polysHead = null;

        return result;
    }

    /**
     * Calculate contour levels based on parameters.
     */
    private double[] calculateLevels(ContourParams params) {
        switch (params.getLevelsKind()) {
            case AUTO:
                return calculateAutoLevels(zMin, zMax, params.getLevels());
            case INCREMENTAL:
                return calculateIncrementalLevels(params);
            case DISCRETE:
                return params.getDiscreteLevels() != null ?
                        params.getDiscreteLevels().clone() : new double[0];
            default:
                return calculateAutoLevels(zMin, zMax, params.getLevels());
        }
    }

    /**
     * Calculate automatic contour levels using quantize_normal_tics algorithm.
     * Port of gnuplot-c/src/axis.c:quantize_normal_tics()
     *
     * @param zMin      Minimum z value
     * @param zMax      Maximum z value
     * @param numLevels Desired number of levels
     * @return Array of z-levels for contours
     */
    public double[] calculateAutoLevels(double zMin, double zMax, int numLevels) {
        double dz = Math.abs(zMax - zMin);
        if (dz == 0) {
            return new double[0];
        }

        // Find a tic step that will generate approximately the desired number
        // of contour levels. The "* 2" is historical from C gnuplot.
        dz = quantizeNormalTics(dz, (numLevels + 1) * 2);

        double z0 = Math.floor(zMin / dz) * dz;
        int actualLevels = (int) Math.floor((zMax - z0) / dz);

        if (actualLevels <= 0) {
            return new double[0];
        }

        double[] levels = new double[actualLevels];
        for (int i = 0; i < actualLevels; i++) {
            double z = z0 + (i + 1) * dz;
            // Check for near-zero values (like C gnuplot's CheckZero)
            if (Math.abs(z) < dz * 1e-10) {
                z = 0;
            }
            levels[i] = z;
        }

        return levels;
    }

    /**
     * Quantize to nice round values (2, 5, or 10 multiples).
     * Port of gnuplot-c/src/axis.c:quantize_normal_tics()
     */
    private double quantizeNormalTics(double tic, int guide) {
        double power = Math.pow(10.0, Math.floor(Math.log10(tic / guide)));
        double xnorm = tic / guide / power;

        if (xnorm <= 2.0) {
            xnorm = 2.0;
        } else if (xnorm <= 5.0) {
            xnorm = 5.0;
        } else {
            xnorm = 10.0;
        }

        return xnorm * power;
    }

    /**
     * Calculate incremental contour levels.
     */
    private double[] calculateIncrementalLevels(ContourParams params) {
        double start = params.getIncrementStart();
        double step = params.getIncrementStep();
        int numLevels = params.getLevels();

        if (step == 0 || numLevels <= 0) {
            return new double[0];
        }

        double[] levels = new double[numLevels];
        for (int i = 0; i < numLevels; i++) {
            levels[i] = start + i * step;
        }
        return levels;
    }

    /**
     * Calculate min/max values from grid.
     * Port of gnuplot-c/src/contour.c:calc_min_max()
     */
    private void calcMinMax(Point3D[] grid, int rows, int cols) {
        double xMin = Double.MAX_VALUE, xMax = -Double.MAX_VALUE;
        double yMin = Double.MAX_VALUE, yMax = -Double.MAX_VALUE;
        zMin = Double.MAX_VALUE;
        zMax = -Double.MAX_VALUE;

        for (Point3D p : grid) {
            if (p != null && Double.isFinite(p.getX()) && Double.isFinite(p.getY()) && Double.isFinite(p.getZ())) {
                if (p.getX() > xMax) xMax = p.getX();
                if (p.getX() < xMin) xMin = p.getX();
                if (p.getY() > yMax) yMax = p.getY();
                if (p.getY() < yMin) yMin = p.getY();
                if (p.getZ() > zMax) zMax = p.getZ();
                if (p.getZ() < zMin) zMin = p.getZ();
            }
        }

        // Width and height used as unit length for fuzzy comparison
        unitX = xMax - xMin;
        unitY = yMax - yMin;
        if (unitX <= 0) unitX = 1.0;
        if (unitY <= 0) unitY = 1.0;
    }

    /**
     * Generate triangulation from grid.
     * Port of gnuplot-c/src/contour.c:gen_triangle()
     */
    private void generateTriangles(Point3D[] grid, int rows, int cols) {
        edgesHead = null;
        polysHead = null;
        Edge edgesTail = null;
        Polygon polysTail = null;

        // Generate edges of first row
        Edge rowEdgesHead = null;
        Edge rowEdgesTail = null;
        for (int j = 0; j < cols - 1; j++) {
            Point3D p0 = grid[j];
            Point3D p1 = grid[j + 1];
            Edge e = addEdge(p0, p1);
            if (e != null) {
                if (rowEdgesHead == null) {
                    rowEdgesHead = e;
                } else {
                    rowEdgesTail.next = e;
                }
                rowEdgesTail = e;
            }
        }
        edgesHead = rowEdgesHead;
        edgesTail = rowEdgesTail;

        // Process remaining rows
        for (int i = 1; i < rows; i++) {
            int rowOffset = i * cols;
            int prevRowOffset = (i - 1) * cols;

            Edge newRowEdgesHead = null;
            Edge newRowEdgesTail = null;
            Edge prevRowEdge = rowEdgesHead;

            // Generate first vertical edge
            Edge vertEdge = addEdge(grid[prevRowOffset], grid[rowOffset]);
            if (vertEdge != null) {
                if (edgesTail != null) {
                    edgesTail.next = vertEdge;
                } else {
                    edgesHead = vertEdge;
                }
                edgesTail = vertEdge;
            }

            for (int j = 0; j < cols - 1; j++) {
                Point3D p0 = grid[prevRowOffset + j];
                Point3D p1 = grid[prevRowOffset + j + 1];
                Point3D p2 = grid[rowOffset + j];
                Point3D p3 = grid[rowOffset + j + 1];

                // Edge0 = previous vertical edge
                Edge edge0 = vertEdge;

                // Edge2 = bottom horizontal edge (from previous row)
                Edge edge2 = prevRowEdge;
                if (prevRowEdge != null) {
                    prevRowEdge = prevRowEdge.next;
                }

                // Generate diagonal edge (p1 to p2)
                Edge edge1 = addEdge(p1, p2);
                if (edge1 != null) {
                    edge1.position = EdgePosition.DIAGONAL;
                    if (edgesTail != null) {
                        edgesTail.next = edge1;
                    } else {
                        edgesHead = edge1;
                    }
                    edgesTail = edge1;
                }

                // Generate lower triangle
                addPolygon(edge0, edge1, edge2);

                // Copy diagonal for upper triangle
                edge0 = edge1;

                // Generate upper horizontal edge
                edge1 = addEdge(p2, p3);
                if (edge1 != null) {
                    if (newRowEdgesHead == null) {
                        newRowEdgesHead = edge1;
                    } else {
                        newRowEdgesTail.next = edge1;
                    }
                    newRowEdgesTail = edge1;
                }

                // Generate next vertical edge
                vertEdge = addEdge(p1, p3);
                if (vertEdge != null) {
                    if (edgesTail != null) {
                        edgesTail.next = vertEdge;
                    } else {
                        edgesHead = vertEdge;
                    }
                    edgesTail = vertEdge;
                }

                // Generate upper triangle
                addPolygon(edge0, edge1, vertEdge);
            }

            // Chain new row edges to main list
            if (newRowEdgesHead != null) {
                if (edgesTail != null) {
                    edgesTail.next = newRowEdgesHead;
                } else {
                    edgesHead = newRowEdgesHead;
                }
                edgesTail = newRowEdgesTail;
            }

            rowEdgesHead = newRowEdgesHead;
            rowEdgesTail = newRowEdgesTail;
        }

        // Update boundary flags
        Edge e = edgesHead;
        while (e != null) {
            if (e.poly[0] == null || e.poly[1] == null) {
                if (e.position != EdgePosition.DIAGONAL) {
                    e.position = EdgePosition.BOUNDARY;
                }
            }
            e = e.next;
        }
    }

    /**
     * Add an edge to the mesh.
     */
    private Edge addEdge(Point3D p0, Point3D p1) {
        if (p0 == null || p1 == null ||
            !Double.isFinite(p0.getZ()) || !Double.isFinite(p1.getZ())) {
            return null;
        }

        Edge e = new Edge();
        e.poly[0] = null;
        e.poly[1] = null;
        e.vertex[0] = p0;
        e.vertex[1] = p1;
        e.next = null;
        e.isActive = false;
        e.position = EdgePosition.INNER_MESH;
        return e;
    }

    /**
     * Add a polygon (triangle) to the mesh.
     */
    private void addPolygon(Edge e0, Edge e1, Edge e2) {
        if (e0 == null || e1 == null || e2 == null) {
            return;
        }

        Polygon p = new Polygon();
        p.edge[0] = e0;
        p.edge[1] = e1;
        p.edge[2] = e2;
        p.next = polysHead;
        polysHead = p;

        // Update edge->polygon links
        linkEdgeToPoly(e0, p);
        linkEdgeToPoly(e1, p);
        linkEdgeToPoly(e2, p);
    }

    private void linkEdgeToPoly(Edge e, Polygon p) {
        if (e.poly[0] == null) {
            e.poly[0] = p;
        } else if (e.poly[1] == null) {
            e.poly[1] = p;
        }
    }

    /**
     * Generate contours at a specific z-level.
     * Port of gnuplot-c/src/contour.c:gen_contours()
     */
    private List<ContourLine> generateContoursAtLevel(double zLevel, ContourParams params) {
        List<ContourLine> contours = new ArrayList<>();

        // Pass 1: Mark active edges
        int numActive = updateAllEdges(zLevel);

        // Pass 2: Trace contours
        boolean lookForClosed = false;  // Start looking on boundaries

        while (numActive > 0) {
            TraceResult result = generateOneContour(zLevel, lookForClosed);
            if (result == null) {
                break;
            }

            numActive = result.numActiveRemaining;
            lookForClosed = result.nextLookForClosed;

            if (result.points != null && result.points.size() >= 2) {
                // Apply smoothing if requested
                List<Point3D> finalPoints = applySmoothing(result.points, result.isClosed, params);

                String label = formatLabel(zLevel, params.getFormat());
                ContourLine line = new ContourLine(zLevel, finalPoints, result.isClosed, label);
                contours.add(line);
            }
        }

        return contours;
    }

    private static class TraceResult {
        List<Point3D> points;
        boolean isClosed;
        int numActiveRemaining;
        boolean nextLookForClosed;
    }

    /**
     * Mark edges that cross the z-level.
     * Port of gnuplot-c/src/contour.c:update_all_edges()
     */
    private int updateAllEdges(double zLevel) {
        int count = 0;
        Edge e = edgesHead;
        while (e != null) {
            // Use same comparison at both vertices to avoid roundoff errors
            boolean v0Above = e.vertex[0].getZ() >= zLevel;
            boolean v1Above = e.vertex[1].getZ() >= zLevel;
            if (v0Above != v1Above) {
                e.isActive = true;
                count++;
            } else {
                e.isActive = false;
            }
            e = e.next;
        }
        return count;
    }

    /**
     * Generate one contour.
     * Port of gnuplot-c/src/contour.c:gen_one_contour()
     */
    private TraceResult generateOneContour(double zLevel, boolean lookForClosed) {
        TraceResult result = new TraceResult();
        result.nextLookForClosed = lookForClosed;

        Edge startEdge = null;

        if (!lookForClosed) {
            // Look for boundary edge to start
            Edge e = edgesHead;
            while (e != null) {
                if (e.isActive && e.position == EdgePosition.BOUNDARY) {
                    startEdge = e;
                    break;
                }
                e = e.next;
            }
            if (startEdge == null) {
                result.nextLookForClosed = true;
            }
        }

        if (result.nextLookForClosed && startEdge == null) {
            // Look for any active edge (interior)
            Edge e = edgesHead;
            while (e != null) {
                if (e.isActive && e.position != EdgePosition.BOUNDARY) {
                    startEdge = e;
                    break;
                }
                e = e.next;
            }
        }

        if (startEdge == null) {
            result.numActiveRemaining = 0;
            return result;
        }

        // Trace the contour
        return traceContour(startEdge, zLevel, result.nextLookForClosed);
    }

    /**
     * Trace a contour from a starting edge.
     * Port of gnuplot-c/src/contour.c:trace_contour()
     */
    private TraceResult traceContour(Edge startEdge, double zLevel, boolean isClosed) {
        TraceResult result = new TraceResult();
        result.nextLookForClosed = isClosed;

        List<CntrPoint> points = new ArrayList<>();
        Edge currentEdge = startEdge;
        Polygon lastPoly = null;

        // Mark start edge as inactive - applies to both open and closed contours
        // This prevents the algorithm from re-tracing the same contour
        startEdge.isActive = false;

        // Check if we have polygons to trace
        if (currentEdge.poly[0] == null && currentEdge.poly[1] == null) {
            result.points = null;
            result.numActiveRemaining = countActiveEdges();
            return result;
        }

        // Add first point
        CntrPoint firstPoint = updateCntrPt(startEdge, zLevel);
        points.add(firstPoint);

        do {
            // Find polygon to continue (not where we came from)
            Polygon nextPoly;
            if (currentEdge.poly[0] == lastPoly) {
                nextPoly = currentEdge.poly[1];
            } else {
                nextPoly = currentEdge.poly[0];
            }

            if (nextPoly == null) {
                break;
            }

            // Find next active edge in polygon
            Edge nextEdge = null;
            for (int i = 0; i < 3; i++) {
                Edge e = nextPoly.edge[i];
                if (e != currentEdge && e.isActive) {
                    nextEdge = e;
                    break;
                }
            }

            if (nextEdge == null) {
                break;
            }

            currentEdge = nextEdge;
            lastPoly = nextPoly;
            currentEdge.isActive = false;

            // Don't add points on diagonal edges
            if (currentEdge.position != EdgePosition.DIAGONAL) {
                CntrPoint newPoint = updateCntrPt(currentEdge, zLevel);

                // Remove nearby points (fuzzy equal check)
                if (!points.isEmpty()) {
                    CntrPoint lastPoint = points.get(points.size() - 1);
                    if (!fuzzyEqual(lastPoint, newPoint)) {
                        points.add(newPoint);
                    }
                } else {
                    points.add(newPoint);
                }
            }

        } while (currentEdge != startEdge && currentEdge.position != EdgePosition.BOUNDARY);

        // Determine if contour is closed
        result.isClosed = (currentEdge == startEdge);

        // For closed contour, make first and last point equal
        if (result.isClosed && points.size() >= 2) {
            CntrPoint last = points.get(points.size() - 1);
            CntrPoint first = points.get(0);
            first.x = last.x;
            first.y = last.y;
        }

        // Convert to Point3D list
        List<Point3D> point3DList = new ArrayList<>();
        for (CntrPoint cp : points) {
            point3DList.add(new Point3D(cp.x, cp.y, zLevel));
        }

        result.points = point3DList;
        result.numActiveRemaining = countActiveEdges();
        return result;
    }

    /**
     * Interpolate contour point on an edge.
     * Port of gnuplot-c/src/contour.c:update_cntr_pt()
     */
    private CntrPoint updateCntrPt(Edge edge, double zLevel) {
        double z0 = edge.vertex[0].getZ();
        double z1 = edge.vertex[1].getZ();

        double t = (zLevel - z0) / (z1 - z0);

        // Clamp to [0, 1]
        t = Math.max(0.0, Math.min(1.0, t));

        double x = edge.vertex[1].getX() * t + edge.vertex[0].getX() * (1 - t);
        double y = edge.vertex[1].getY() * t + edge.vertex[0].getY() * (1 - t);

        return new CntrPoint(x, y);
    }

    /**
     * Check if two contour points are effectively equal.
     * Port of gnuplot-c/src/contour.c:fuzzy_equal()
     */
    private boolean fuzzyEqual(CntrPoint p1, CntrPoint p2) {
        double x1 = p1.x / unitX;
        double x2 = p2.x / unitX;
        double y1 = p1.y / unitY;
        double y2 = p2.y / unitY;

        return Math.abs(x1 - x2) < EPSILON && Math.abs(y1 - y2) < EPSILON;
    }

    /**
     * Count remaining active edges.
     */
    private int countActiveEdges() {
        int count = 0;
        Edge e = edgesHead;
        while (e != null) {
            if (e.isActive) count++;
            e = e.next;
        }
        return count;
    }

    /**
     * Apply smoothing to contour points.
     */
    private List<Point3D> applySmoothing(List<Point3D> points, boolean isClosed, ContourParams params) {
        switch (params.getKind()) {
            case CUBIC_SPLINE:
                return applyCubicSpline(points, isClosed, params.getNpoints());
            case BSPLINE:
                return applyBSpline(points, isClosed, params.getOrder(), params.getNpoints());
            case LINEAR:
            default:
                return points;
        }
    }

    /**
     * Apply cubic spline interpolation.
     * Port of gnuplot-c/src/contour.c:put_contour_cubic()
     */
    private List<Point3D> applyCubicSpline(List<Point3D> points, boolean isClosed, int numInterp) {
        int n = points.size();
        if (n < 3) return points;

        // Calculate spline coefficients
        double[] d2x = new double[n];
        double[] d2y = new double[n];
        double[] deltaT = new double[n];

        // Calculate delta_t (arc length parameters)
        deltaT[0] = 0;
        for (int i = 1; i < n; i++) {
            double dx = points.get(i).getX() - points.get(i - 1).getX();
            double dy = points.get(i).getY() - points.get(i - 1).getY();
            deltaT[i] = Math.sqrt(dx * dx + dy * dy);
        }

        // Solve for second derivatives
        if (!solveTridiagonal(points, d2x, d2y, deltaT, isClosed)) {
            return points;
        }

        // Generate interpolated points
        List<Point3D> result = new ArrayList<>();
        double z = points.get(0).getZ();

        for (int i = 0; i < n - 1; i++) {
            double dt = deltaT[i + 1];
            for (int j = 0; j < numInterp; j++) {
                double t = j * dt / numInterp;
                double[] xy = evalCubicSpline(points, d2x, d2y, deltaT, i, t);
                result.add(new Point3D(xy[0], xy[1], z));
            }
        }
        // Add last point
        result.add(points.get(n - 1));

        return result;
    }

    /**
     * Solve tridiagonal system for cubic spline.
     */
    private boolean solveTridiagonal(List<Point3D> points, double[] d2x, double[] d2y,
                                      double[] deltaT, boolean isClosed) {
        int n = points.size();
        double[][] m = new double[n][3]; // Tridiagonal matrix

        // Set up tridiagonal system
        for (int i = 1; i < n - 1; i++) {
            m[i][0] = deltaT[i];
            m[i][1] = 2 * (deltaT[i] + deltaT[i + 1]);
            m[i][2] = deltaT[i + 1];
        }

        // Natural spline boundary conditions
        m[0][1] = 1;
        m[0][2] = 0;
        m[n - 1][0] = 0;
        m[n - 1][1] = 1;

        // Right-hand side for x
        double[] rhsX = new double[n];
        double[] rhsY = new double[n];
        rhsX[0] = 0;
        rhsY[0] = 0;
        rhsX[n - 1] = 0;
        rhsY[n - 1] = 0;

        for (int i = 1; i < n - 1; i++) {
            double dx1 = (points.get(i).getX() - points.get(i - 1).getX()) / deltaT[i];
            double dx2 = (points.get(i + 1).getX() - points.get(i).getX()) / deltaT[i + 1];
            rhsX[i] = 6 * (dx2 - dx1);

            double dy1 = (points.get(i).getY() - points.get(i - 1).getY()) / deltaT[i];
            double dy2 = (points.get(i + 1).getY() - points.get(i).getY()) / deltaT[i + 1];
            rhsY[i] = 6 * (dy2 - dy1);
        }

        // Solve using Thomas algorithm
        solveThomas(m, rhsX, d2x, n);
        solveThomas(m, rhsY, d2y, n);

        return true;
    }

    private void solveThomas(double[][] m, double[] rhs, double[] result, int n) {
        // Forward elimination
        for (int i = 1; i < n; i++) {
            double factor = m[i][0] / m[i - 1][1];
            m[i][1] -= factor * m[i - 1][2];
            rhs[i] -= factor * rhs[i - 1];
        }

        // Back substitution
        result[n - 1] = rhs[n - 1] / m[n - 1][1];
        for (int i = n - 2; i >= 0; i--) {
            result[i] = (rhs[i] - m[i][2] * result[i + 1]) / m[i][1];
        }
    }

    private double[] evalCubicSpline(List<Point3D> points, double[] d2x, double[] d2y,
                                      double[] deltaT, int i, double t) {
        double h = deltaT[i + 1];
        double a = (h - t) / h;
        double b = t / h;

        double x = a * points.get(i).getX() + b * points.get(i + 1).getX() +
                   ((a * a * a - a) * d2x[i] + (b * b * b - b) * d2x[i + 1]) * h * h / 6;
        double y = a * points.get(i).getY() + b * points.get(i + 1).getY() +
                   ((a * a * a - a) * d2y[i] + (b * b * b - b) * d2y[i + 1]) * h * h / 6;

        return new double[]{x, y};
    }

    /**
     * Apply B-spline approximation.
     * Port of gnuplot-c/src/contour.c:put_contour_bspline()
     */
    private List<Point3D> applyBSpline(List<Point3D> points, boolean isClosed,
                                        int order, int numPoints) {
        int n = points.size();
        if (n < order) return points;

        List<Point3D> result = new ArrayList<>();
        double z = points.get(0).getZ();

        int numSegments = isClosed ? n : n - order + 1;
        double step = 1.0 / numPoints;

        for (int j = 0; j < numSegments; j++) {
            for (int i = 0; i < numPoints; i++) {
                double t = j + i * step;
                double[] xy = evalBSpline(points, order, t, isClosed);
                result.add(new Point3D(xy[0], xy[1], z));
            }
        }

        // Add final point for open curves
        if (!isClosed) {
            result.add(points.get(n - 1));
        }

        return result;
    }

    private double[] evalBSpline(List<Point3D> points, int order, double t,
                                  boolean isClosed) {
        int n = points.size();
        int j = (int) t;
        t = t - j;

        double x = 0, y = 0;

        for (int i = 0; i < order; i++) {
            double basis = bSplineBasis(order, i, t, n, isClosed);
            int idx = (j + i) % n;
            if (!isClosed && j + i >= n) idx = n - 1;
            x += basis * points.get(idx).getX();
            y += basis * points.get(idx).getY();
        }

        return new double[]{x, y};
    }

    private double bSplineBasis(int order, int i, double t, int n, boolean isClosed) {
        // Simplified B-spline basis using de Boor recursion
        if (order == 1) {
            return (i == 0) ? 1.0 : 0.0;
        }

        double[] N = new double[order];
        N[0] = 1.0;

        for (int k = 1; k < order; k++) {
            double[] newN = new double[order];
            for (int j = 0; j <= k; j++) {
                double left = (j > 0) ? N[j - 1] * t / k : 0;
                double right = (j < k) ? N[j] * (k - t) / k : 0;
                newN[j] = left + right;
            }
            N = newN;
        }

        return (i < order) ? N[i] : 0.0;
    }

    /**
     * Format a contour label.
     */
    private String formatLabel(double z, String format) {
        try {
            return String.format(format, z);
        } catch (Exception e) {
            return String.format("%.3g", z);
        }
    }
}
