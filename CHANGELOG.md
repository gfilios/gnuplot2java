# Changelog

All notable changes to the Gnuplot Java Implementation will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/).

## [Unreleased]

### Fixed - 2025-10-07

#### Point Marker Visibility in SVG Output (CRITICAL FIX)
**Issue**: Point markers (with points) were not visible in generated SVG files, despite being structurally correct.

**Root Cause**: SVG `clip-path` attribute does not work correctly when applied directly to `<path>` elements that have a `transform` attribute. This is a browser compatibility issue where the transform is not properly applied before clipping.

**Solution**:
- Wrap all point marker `<path>` elements in a `<g>` (group) element
- Apply `clip-path` to the parent `<g>` element instead of individual paths
- Use `stroke-width="2.0"` for clear visibility (not scaled by transform)
- Render point markers directly as inline `<path>` elements instead of using `<use>` references

**Technical Details**:
```svg
<!-- BEFORE (not working) -->
<path d="..." transform="translate(...) scale(...)"
      clip-path="url(#plotClip)" stroke="..." stroke-width="..."/>

<!-- AFTER (working) -->
<g clip-path="url(#plotClip)">
  <path d="..." transform="translate(...) scale(...)"
        stroke="..." stroke-width="2.0"/>
</g>
```

**Files Changed**:
- `gnuplot-java/gnuplot-render/src/main/java/com/gnuplot/render/svg/SvgRenderer.java`
  - Lines 469-504: Point marker rendering logic
  - Added `getGpPtPathData()` method to provide path geometry for each marker type
  - Modified rendering to wrap paths in `<g>` element with clip-path

**Impact**:
- ✅ All point marker types now visible (Cross, Plus, Circle, Square, Diamond, Triangles, Star, Pentagon)
- ✅ simple.dem Plot 4 (with points) now renders correctly
- ✅ Point marker count matches C gnuplot (Java: 200, C: 201)
- ✅ Tests passing: 3/3 (100%)

**Testing**:
```bash
# Verify point markers are visible
cat > /tmp/test.dem << 'EOF'
set terminal svg size 800,600
set output '/tmp/test.svg'
plot [-10:10] sin(x) with points lc rgb "#FF0000"
EOF
java -jar gnuplot-cli/target/gnuplot-cli-1.0.0-SNAPSHOT-jar-with-dependencies.jar /tmp/test.dem
open /tmp/test.svg  # Red X markers should be clearly visible
```

**Related Issues**: Fixes rendering of all Gnuplot demos using `with points` or `with linespoints`

---

## [2025-10-05] - simple.dem Major Progress

### Added
- Default plot styles (lines, points, impulses)
- Per-plot range support
- Mirror ticks on all axes
- Impulses rendering (vertical bars from baseline)

### Fixed
- Plot style detection and application
- Legend positioning defaults
- Axis positioning to match C gnuplot

### Status
- 3/8 plots perfect match with C gnuplot
- 5/8 plots with minor tick count differences
- Point markers structurally correct but visibility issue discovered

---

## [2025-10-01] - Phase 7 Epic 7.1 Complete

### Added
- ✅ Gnuplot Command Parser (ANTLR4)
- ✅ CLI Interface with 5 execution modes
- ✅ Full script compatibility
- ✅ Expression evaluation integration
- ✅ SVG output generation

### Tests
- 31 CLI tests passing
- 989 total tests passing

---

## Previous Releases

See [README.md](README.md) for complete project history.
