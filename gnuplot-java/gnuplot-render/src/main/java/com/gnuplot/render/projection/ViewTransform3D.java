package com.gnuplot.render.projection;

import com.gnuplot.render.elements.Point3D;

/**
 * 3D view transformation for projecting 3D points to 2D screen coordinates.
 * Implements gnuplot's view transformation with rotation and perspective.
 *
 * @since 1.0
 */
public class ViewTransform3D {

    private final double rotX;  // Rotation around X-axis (vertical)
    private final double rotZ;  // Rotation around Z-axis (horizontal)
    private final double scale; // Overall scale factor
    private final double zscale; // Z-axis scale (ticslevel)

    /**
     * Creates a 3D view transformation.
     *
     * @param rotX rotation around X-axis in degrees (gnuplot view vertical angle)
     * @param rotZ rotation around Z-axis in degrees (gnuplot view horizontal angle)
     * @param scale overall scale factor
     * @param zscale Z-axis scale factor (ticslevel)
     */
    public ViewTransform3D(double rotX, double rotZ, double scale, double zscale) {
        this.rotX = Math.toRadians(rotX);
        this.rotZ = Math.toRadians(rotZ);
        this.scale = scale;
        this.zscale = zscale;
    }

    /**
     * Default gnuplot view (60, 30 degrees).
     * Matches C gnuplot's "set view 60,30" command with coordinate adjustments.
     * C gnuplot: rotX=60°, rotZ=30°
     * Java: rotX is negated in project() to compensate for SVG Y-axis direction.
     *       rotZ=30° is used directly.
     *       Axis corner positioning is handled by SvgRenderer.setup3DBoxCorners().
     * Scale adjusted to match C gnuplot visual output.
     * Additional viewport scaling is handled by SvgRenderer's mapProjectedX/Y.
     * Z-scale (ticslevel) set to 0.5 matching C gnuplot default.
     */
    public static ViewTransform3D gnuplotDefault() {
        return new ViewTransform3D(60, 30, 1.0, 0.5);
    }

    /**
     * Projects a 3D point to 2D screen coordinates.
     * Uses gnuplot's view transformation algorithm.
     *
     * @param point the 3D point to project
     * @return projected 2D point (x, y)
     */
    public Point2D project(Point3D point) {
        // Normalize coordinates to [-1, 1] range (assumes data is already normalized)
        double x = point.x();
        double y = point.y();
        double z = point.z() * zscale;

        // Apply rotation around Z-axis (horizontal rotation)
        double cosZ = Math.cos(rotZ);
        double sinZ = Math.sin(rotZ);
        double x1 = x * cosZ - y * sinZ;
        double y1 = x * sinZ + y * cosZ;
        double z1 = z;

        // Apply rotation around X-axis (vertical rotation)
        // Negate rotX to match SVG coordinate system (Y-axis inverted)
        double cosX = Math.cos(-rotX);
        double sinX = Math.sin(-rotX);
        double x2 = x1;
        double y2 = y1 * cosX - z1 * sinX;
        double z2 = y1 * sinX + z1 * cosX;

        // Orthographic projection (ignore z2 for depth)
        // In the future, we could add perspective by dividing by (1 + z2)
        double screenX = x2 * scale;
        double screenY = y2 * scale;

        return new Point2D(screenX, screenY, z2); // Keep z for depth sorting
    }

    /**
     * 2D projection result with depth information.
     */
    public record Point2D(double x, double y, double z) {
        /**
         * Checks if the projected point is finite.
         */
        public boolean isFinite() {
            return Double.isFinite(x) && Double.isFinite(y) && Double.isFinite(z);
        }
    }
}
