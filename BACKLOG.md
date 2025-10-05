# Gnuplot Java Implementation - Backlog

## Summary of Current Status

### ✅ Recently Completed (2025-10-05)
- **Fixed: Default plot styles for data files** - Data files without explicit `with` clause now use `set style data` defaults
- **Fixed: Per-plot range support** - Plot specs can now override global range (e.g., `[0:*] expression`)
- **Fixed: Mirror tick directions** - Top/right border ticks now point inward correctly
- **Fixed: HTML report generation** - Handles null outputs gracefully
- **Fixed: Comparison script** - Better point marker detection

### 🎯 Current Test Results - simple.dem (8 plots)

| Plot | Description | Status | Issues |
|------|-------------|--------|--------|
| 1 | Basic trig functions | ✅ PASS | No critical issues |
| 2 | Multiple functions | ✅ PASS | No critical issues |
| 3 | Arc functions | ✅ PASS | No critical issues |
| 4 | Bessel + per-plot range | ⚠️ MINOR | Y-axis ticks: 9 vs 8 |
| 5 | Complex function | ⚠️ MINOR | Y-axis ticks: 12 vs 9 |
| 6 | Bottom-center legend | ⚠️ MINOR | X-axis ticks: 0 vs 7 |
| 7 | High-resolution plot | ⚠️ MINOR | Y-axis: 9 vs 7, X-axis: 0 vs 6 |
| 8 | Data files (mixed styles) | ⚠️ MINOR | Y-axis: 11 vs 9, X-axis: 0 vs 7 |

**Point Markers:** ✅ All working perfectly!
- Plot 4: 200 markers (C: 201) - diff=1
- Plot 8: 47 markers (C: 47) - exact match!

---

## Priority 1: Critical Rendering Issues

### None! 🎉
All critical rendering issues have been resolved. The remaining issues are minor tick count differences.

---

## Priority 2: Tick Generation & Axis Scaling

### Issue: Tick Count Differences
**Affected Plots:** 4, 5, 6, 7, 8
**Impact:** Minor visual difference, not functionally broken

**Root Cause:**
The Java implementation generates different numbers of axis ticks compared to C gnuplot. This is likely due to:
1. Different tick spacing algorithm
2. Different rounding/precision in tick label generation
3. Different handling of auto-ranging

**Tasks:**
- [ ] Investigate C gnuplot's tick generation algorithm (`src/graphics.c`)
- [ ] Compare tick spacing calculations between C and Java
- [ ] Implement matching tick generation logic in Java
- [ ] Add tests for tick count accuracy

**Expected Effort:** Medium (2-3 days)

**Files to Modify:**
- `SvgRenderer.java` - Tick generation logic
- Add tick generation utility class if needed

---

## Priority 3: Legend Position Fine-tuning

### Issue: Legend Position Variations
**Affected Plots:** 6, 7, 8 (plots with non-default legend positions)
**Status:** Partially working, but may need position adjustments

**Tasks:**
- [ ] Review legend positioning for `set key bmargin center horizontal` (Plot 6)
- [ ] Verify legend positioning matches C output exactly
- [ ] Add tests for all legend position combinations

**Expected Effort:** Small (1 day)

**Files to Modify:**
- `SvgRenderer.java` - Legend rendering

---

## Priority 4: Additional Demo Scripts

### Expand Test Coverage
**Goal:** Test more gnuplot demo scripts to find edge cases

**Tasks:**
- [ ] Test `scatter.dem` - Already runs, needs comparison analysis
- [ ] Test `controls.dem` - Expected to fail (control flow not implemented)
- [ ] Test `fillbetween.dem` - Fill styles
- [ ] Test `histograms.dem` - Histogram plot types
- [ ] Test `multiplot.dem` - Multiple plots on one canvas

**Expected Effort:** Small per demo (0.5 day each)

---

## Priority 5: Advanced Plot Styles

### Missing Plot Styles
**Status:** Not yet implemented

**Tasks:**
- [ ] Implement `impulses` as actual impulse lines (currently treated as regular lines)
- [ ] Implement `boxes` plot style
- [ ] Implement `steps` plot style
- [ ] Implement `fsteps` plot style
- [ ] Implement `histeps` plot style
- [ ] Implement `errorbars` plot style
- [ ] Implement `financebars` plot style

**Expected Effort:** Medium-Large (1-2 weeks total)

**Files to Modify:**
- `PlotCommand.java` - Add new PlotStyle enum values
- `GnuplotScriptExecutor.java` - Handle new styles
- `SvgRenderer.java` - Render new styles

---

## Priority 6: Advanced Features

### 6.1 Parametric Plots
**Status:** Not implemented

**Tasks:**
- [ ] Support `set parametric` mode
- [ ] Support parametric function syntax: `plot [t=0:2*pi] cos(t), sin(t)`
- [ ] Handle 3D parametric plots: `splot [u=0:2*pi][v=0:pi] cos(u)*sin(v), sin(u)*sin(v), cos(v)`

**Expected Effort:** Medium (3-5 days)

---

### 6.2 3D Plotting (splot)
**Status:** Not implemented

**Tasks:**
- [ ] Parse `splot` command
- [ ] Implement 3D coordinate system
- [ ] Implement 3D surface rendering
- [ ] Implement 3D contour lines
- [ ] Implement hidden line removal
- [ ] Implement view rotation/perspective

**Expected Effort:** Large (2-3 weeks)

---

### 6.3 Multiplot
**Status:** Not implemented

**Tasks:**
- [ ] Parse `set multiplot` command
- [ ] Support multiplot layout: `set multiplot layout 2,3`
- [ ] Support manual positioning: `set origin`, `set size`
- [ ] Handle multiple scenes in one SVG

**Expected Effort:** Medium (1 week)

---

### 6.4 Fill Styles
**Status:** Not implemented

**Tasks:**
- [ ] Parse `set style fill` command
- [ ] Implement solid fill
- [ ] Implement pattern fills
- [ ] Implement transparency
- [ ] Support `fillbetween` style

**Expected Effort:** Medium (1 week)

---

## Priority 7: Code Quality & Testing

### 7.1 Test Coverage
**Current:** ~380 tests passing

**Tasks:**
- [ ] Add tests for all plot styles
- [ ] Add tests for all `set` commands
- [ ] Add tests for edge cases (empty data, NaN, infinity)
- [ ] Add regression tests for fixed bugs
- [ ] Achieve >90% code coverage

**Expected Effort:** Ongoing

---

### 7.2 Performance Optimization
**Status:** Not yet measured

**Tasks:**
- [ ] Profile rendering performance
- [ ] Optimize point generation for large datasets
- [ ] Optimize SVG generation
- [ ] Add benchmarks for common operations

**Expected Effort:** Medium (1 week)

---

### 7.3 Code Documentation
**Current:** Basic JavaDoc

**Tasks:**
- [ ] Complete JavaDoc for all public APIs
- [ ] Add architecture documentation
- [ ] Add developer guide
- [ ] Add contribution guidelines

**Expected Effort:** Small-Medium (2-3 days)

---

## Priority 8: Error Handling & User Experience

### 8.1 Better Error Messages
**Current:** Basic error messages

**Tasks:**
- [ ] Add line numbers to parse errors
- [ ] Add helpful suggestions for common mistakes
- [ ] Add validation for data ranges
- [ ] Add validation for color specifications

**Expected Effort:** Small-Medium (2-3 days)

---

### 8.2 Interactive Mode
**Status:** Basic implementation exists

**Tasks:**
- [ ] Improve REPL user experience
- [ ] Add command history
- [ ] Add tab completion
- [ ] Add syntax highlighting

**Expected Effort:** Medium (1 week)

---

## Future Considerations

### Terminal Support
Currently only SVG output is supported. Consider adding:
- [ ] PNG output (via SVG conversion)
- [ ] PDF output
- [ ] Canvas (HTML5) output
- [ ] Qt terminal for interactive plots

### Advanced Math Functions
- [ ] Add more special functions (gamma, beta, etc.)
- [ ] Add statistics functions
- [ ] Add fitting capabilities

### Data Processing
- [ ] Support data manipulation (smoothing, binning, etc.)
- [ ] Support database queries as data source
- [ ] Support CSV/JSON data formats

---

## Metrics & Goals

### Current Progress
- **Core Functionality:** ~70% complete
- **Demo Compatibility:** 3/8 perfect, 5/8 minor issues
- **Test Coverage:** 380 tests, all passing
- **Code Quality:** Clean, maintainable architecture

### Next Milestone Goals
**Target Date:** 2025-10-12 (1 week)

- [ ] Fix all tick count differences (Plots 4-8)
- [ ] Verify legend positions are pixel-perfect
- [ ] Add 2 more demo script tests
- [ ] Achieve 5/8 perfect plots in simple.dem

### Long-term Goals
**Target Date:** 2025-11-05 (1 month)

- [ ] All 8 plots in simple.dem render perfectly
- [ ] 5+ demo scripts fully compatible
- [ ] Implement 3D plotting (splot)
- [ ] Implement multiplot
- [ ] >90% code coverage

---

## Notes

### Architecture Strengths
- Clean separation: Parser → Executor → Renderer
- ANTLR grammar makes parsing robust
- SVG rendering is clean and maintainable
- Good test infrastructure with comparison tools

### Areas for Improvement
- Tick generation algorithm needs refinement
- Need more comprehensive demo test suite
- Documentation could be more detailed
- Performance not yet optimized

---

**Last Updated:** 2025-10-05
**Contributors:** gfilios, Claude
