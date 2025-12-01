/*
 * Gnuplot Java - Contour Parameters
 * Port of gnuplot-c/src/contour.h:contour_params
 */
package com.gnuplot.core.grid;

/**
 * Configuration parameters for contour line generation.
 * <p>
 * This class encapsulates all settings that control how contour lines
 * are generated and rendered, matching the C gnuplot contour_params structure.
 * </p>
 *
 * @see <a href="file:///gnuplot-c/src/contour.h">contour.h</a>
 */
public class ContourParams {

    /**
     * Method of drawing/smoothing the contour lines.
     * Port of t_contour_kind from contour.h
     */
    public enum ContourKind {
        /** Raw traced points, no smoothing */
        LINEAR,
        /** C2 continuous cubic spline interpolation */
        CUBIC_SPLINE,
        /** B-spline approximation curve */
        BSPLINE
    }

    /**
     * How contour levels are determined.
     * Port of t_contour_levels_kind from contour.h
     */
    public enum LevelsKind {
        /** Automatically calculate nice round levels */
        AUTO,
        /** User-specified start value and increment */
        INCREMENTAL,
        /** User-specified discrete z values */
        DISCRETE
    }

    /**
     * Where to draw contour lines.
     */
    public enum ContourPlace {
        /** Draw contours projected on the base plane (z=base_z) */
        BASE,
        /** Draw contours on the 3D surface at their actual z-level */
        SURFACE,
        /** Draw contours on both base and surface */
        BOTH
    }

    // Defaults matching C gnuplot (contour.h)
    private static final int DEFAULT_CONTOUR_LEVELS = 5;
    private static final int DEFAULT_NUM_APPROX_PTS = 5;
    private static final int DEFAULT_CONTOUR_ORDER = 4;
    private static final int MAX_BSPLINE_ORDER = 10;

    private ContourKind kind = ContourKind.LINEAR;
    private LevelsKind levelsKind = LevelsKind.AUTO;
    private ContourPlace place = ContourPlace.BASE;
    private int levels = DEFAULT_CONTOUR_LEVELS;
    private int order = DEFAULT_CONTOUR_ORDER;
    private int npoints = DEFAULT_NUM_APPROX_PTS;
    private boolean sortLevels = true;
    private String format = "%g";
    private double[] discreteLevels;
    private double incrementStart;
    private double incrementStep;
    private int firstLineType = 0;

    public ContourParams() {
    }

    /**
     * Copy constructor for creating modified versions.
     */
    public ContourParams(ContourParams other) {
        this.kind = other.kind;
        this.levelsKind = other.levelsKind;
        this.place = other.place;
        this.levels = other.levels;
        this.order = other.order;
        this.npoints = other.npoints;
        this.sortLevels = other.sortLevels;
        this.format = other.format;
        this.discreteLevels = other.discreteLevels != null ? other.discreteLevels.clone() : null;
        this.incrementStart = other.incrementStart;
        this.incrementStep = other.incrementStep;
        this.firstLineType = other.firstLineType;
    }

    // Getters and setters

    public ContourKind getKind() {
        return kind;
    }

    public void setKind(ContourKind kind) {
        this.kind = kind;
    }

    public LevelsKind getLevelsKind() {
        return levelsKind;
    }

    public void setLevelsKind(LevelsKind levelsKind) {
        this.levelsKind = levelsKind;
    }

    public ContourPlace getPlace() {
        return place;
    }

    public void setPlace(ContourPlace place) {
        this.place = place;
    }

    public int getLevels() {
        return levels;
    }

    public void setLevels(int levels) {
        this.levels = Math.max(1, levels);
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = Math.min(MAX_BSPLINE_ORDER, Math.max(2, order));
    }

    public int getNpoints() {
        return npoints;
    }

    public void setNpoints(int npoints) {
        this.npoints = Math.max(1, npoints);
    }

    public boolean isSortLevels() {
        return sortLevels;
    }

    public void setSortLevels(boolean sortLevels) {
        this.sortLevels = sortLevels;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format != null ? format : "%g";
    }

    public double[] getDiscreteLevels() {
        return discreteLevels;
    }

    public void setDiscreteLevels(double[] discreteLevels) {
        this.discreteLevels = discreteLevels;
        if (discreteLevels != null) {
            this.levelsKind = LevelsKind.DISCRETE;
            this.levels = discreteLevels.length;
        }
    }

    public double getIncrementStart() {
        return incrementStart;
    }

    public double getIncrementStep() {
        return incrementStep;
    }

    public void setIncremental(double start, double step) {
        this.incrementStart = start;
        this.incrementStep = step;
        this.levelsKind = LevelsKind.INCREMENTAL;
    }

    public int getFirstLineType() {
        return firstLineType;
    }

    public void setFirstLineType(int firstLineType) {
        this.firstLineType = firstLineType;
    }

    /**
     * Reset to default values.
     */
    public void reset() {
        this.kind = ContourKind.LINEAR;
        this.levelsKind = LevelsKind.AUTO;
        this.place = ContourPlace.BASE;
        this.levels = DEFAULT_CONTOUR_LEVELS;
        this.order = DEFAULT_CONTOUR_ORDER;
        this.npoints = DEFAULT_NUM_APPROX_PTS;
        this.sortLevels = true;
        this.format = "%g";
        this.discreteLevels = null;
        this.incrementStart = 0;
        this.incrementStep = 0;
        this.firstLineType = 0;
    }

    @Override
    public String toString() {
        return String.format("ContourParams{kind=%s, levelsKind=%s, place=%s, levels=%d, order=%d, npoints=%d}",
                kind, levelsKind, place, levels, order, npoints);
    }
}
