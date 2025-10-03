# Test-Driven Development Plan - Gnuplot Demo Suite

## Overview

This plan shifts to a **test-driven approach** using the official Gnuplot demo scripts (`gnuplot-c/demo/*.dem`) as our test oracle. We will systematically execute each demo, compare outputs, identify gaps, and implement missing features.

## Approach

### 1. Test Execution Framework

**Goal**: Create automated comparison between C Gnuplot output and Java implementation

**Components**:
- **Test Runner**: Execute demo scripts in both C and Java versions
- **Output Comparator**: Compare SVG outputs (visual regression testing)
- **Gap Analyzer**: Identify missing commands, features, and rendering differences
- **Progress Tracker**: Track which demos pass/fail

### 2. Demo Suite Structure

The `all.dem` file contains **100+ demo scripts** organized by complexity:

**Tier 1: Basic (Easy)** - Foundation features
- `simple.dem` - Basic plotting with trig functions
- `controls.dem` - Control flow
- `using.dem` - Data file usage
- `fillstyle.dem` - Fill styles
- `errorbars.dem` - Error bars (already partially implemented)
- `scatter.dem` - Scatter plots (already implemented)

**Tier 2: Intermediate** - Advanced 2D features
- `polar.dem` - Polar coordinates
- `multiplt.dem` / `layout.dem` - Multi-plot layouts (partially implemented)
- `histograms.dem` - Histogram styles
- `boxplot.dem` - Box plots
- `smooth.dem` / `spline.dem` - Data smoothing
- `contours.dem` - Contour plots

**Tier 3: Advanced** - Complex features
- `surface1.dem` / `surface2.dem` - 3D surfaces
- `pm3d.dem` - PM3D colored surfaces
- `fit.dem` - Curve fitting
- `dgrid3d.dem` - 3D gridding
- `image.dem` - Image plotting
- `heatmaps.dem` - Heatmaps

**Tier 4: Expert** - Specialized features
- `hidden.dem` - Hidden line removal
- `projection.dem` - Map projections
- `animate.dem` - Animation
- `voxel.dem` - Voxel plots

## Implementation Plan

### Phase 1: Test Infrastructure (1 week) - ✅ COMPLETE

**Story TDD-1: Test Execution Framework** (8 SP) - ✅ COMPLETE
- ✅ Create `DemoTestRunner.java` to execute .dem files
- ✅ Implement script modification (set terminal, remove pauses)
- ✅ Create output capture mechanism (stdout, stderr, SVG files)
- ✅ Compare C gnuplot output vs Java output
- ✅ Create `TestResultRepository` for persistent storage
- ✅ Generate HTML reports with side-by-side comparison
- ✅ Timestamped test runs with "latest" symlink
- ✅ Store all artifacts: scripts, outputs, logs

**Completed**: 2025-10-03
**Tests**: 3 demos (simple.dem, scatter.dem, controls.dem)
**Infrastructure**: DemoTestRunner, TestResultRepository, HtmlReportGenerator, DemoTestSuite

**Story TDD-2: Visual Comparison System** (13 SP) - ✅ COMPLETE
- ✅ SVG diff tool (structural comparison)
- ✅ Pixel-based comparison (rasterize SVGs with Apache Batik)
- ✅ Difference highlighting (red pixels for differences)
- ✅ Acceptance threshold configuration
- ✅ SVG statistics (element counts, dimensions)
- ✅ Text content comparison

**Completed**: 2025-10-03
**Components**: SvgComparator, PixelComparator
**Tests**: 4 unit tests passing

**Story TDD-3: Gap Analysis Reporting** (5 SP) - ✅ COMPLETE
- ✅ Parse error messages with regex patterns
- ✅ Classify errors (command missing, parse error, feature missing, rendering, data)
- ✅ Gap priority analysis (P1: commands, P2: features, P3: parse errors)
- ✅ Summary reports with gap counts
- ✅ Extract missing commands and features

**Completed**: 2025-10-03
**Components**: GapAnalyzer
**Tests**: 8 unit tests passing

**Phase TDD Complete**: 26/26 SP (100%) ✅

### Phase 2: Tier 1 - Basic Demos (2-3 weeks) - 🟡 IN PROGRESS

**Goal**: Pass all basic demos (simple, controls, using, fillstyle, errorbars, scatter)

**Story TDD-4: simple.dem Compliance** (21 SP) - 🟡 IN PROGRESS (8/21 SP = 38%)
- **Phase 1**: Grammar fixes (8 SP) - ✅ COMPLETE
  * ✅ Terminal size: `set term svg size 800,600` - Added SIZE token
  * ✅ Font spec: `set title "text" font ",20"` - Added FONT token, updated visitor
  * ✅ Key positions: `set key bmargin center` - Compound position support
  * ✅ Single quotes: `'1.dat'` - Already supported in QUOTED_STRING
  * ✅ Plot ranges: `plot [-30:20] expr, [0:*] expr` - Global + per-plot ranges
- **Phase 2**: Output file path (3 SP) - 🔴 NOT STARTED
  * `set output` parsed but executor writes to "output.svg" instead
- **Phase 3**: Plot styles (5 SP) - 🔴 NOT STARTED
  * `with impulses` not implemented
- **Phase 4**: Data file reading (5 SP) - 🔴 NOT STARTED
  * `.dat` file reading not implemented

**Roadmap**: See [STORY_TDD4_ROADMAP.md](../docs/STORY_TDD4_ROADMAP.md)

**Completed**:
- ✅ All grammar parse errors fixed - simple.dem parses completely
- ✅ 5 grammar enhancements (SIZE, FONT, key positions, ranges)
- ✅ CommandBuilderVisitor updated for new grammar rules

**Remaining blockers**:
- Output file path: Executor ignores `set output` path
- Plot styles: `with impulses` style not implemented
- Data file reading: `.dat` file reading not implemented

**Story TDD-5: controls.dem Compliance** (13 SP)
- Implement: `if/else`, `do/while`, `for` loops
- Variable assignments
- Command-line argument handling

**Story TDD-6: using.dem Compliance** (21 SP)
- Data file reading: CSV, whitespace-separated
- `using` column specifications: `using 1:2`, `using 1:($2*2)`
- Column expressions and transformations
- Header row handling

**Story TDD-7: fillstyle.dem Compliance** (13 SP)
- Fill styles: solid, pattern, transparent
- Fill color specifications
- Border styles

### Phase 3: Tier 2 - Intermediate Demos (4-5 weeks)

**Story TDD-8: polar.dem Compliance** (13 SP)
- Polar coordinate system
- `set polar` command
- Polar grid rendering

**Story TDD-9: Histogram Demos Compliance** (21 SP)
- Clustered histograms
- Stacked histograms (already have some)
- Gap/overlap control
- Row-stacked histograms

**Story TDD-10: boxplot.dem Compliance** (13 SP)
- Box-and-whisker plots
- Outlier detection
- Median/quartile calculation
- Notched boxes

**Story TDD-11: smooth/spline Demos** (21 SP)
- Spline interpolation (have cubic spline)
- Bezier curves
- Frequency smoothing
- Monotonic splines

**Story TDD-12: contours.dem Compliance** (21 SP)
- Contour line calculation
- Contour labels
- Custom contour levels
- Filled contours

### Phase 4: Tier 3 - Advanced Demos (6-8 weeks)

**Story TDD-13: 3D Surface Rendering** (34 SP)
- `splot` command
- 3D coordinate system
- Surface mesh generation
- Hidden line removal (basic)

**Story TDD-14: PM3D Implementation** (34 SP)
- Colored surface patches
- Lighting models
- Depth cueing
- Surface interpolation

**Story TDD-15: Data Fitting** (21 SP)
- `fit` command
- Levenberg-Marquardt algorithm
- Parameter estimation
- Fit statistics

### Phase 5: Continuous Validation

**Process**: For each new feature
1. Run relevant demo script
2. Compare output with C gnuplot
3. If differences:
   - Identify missing command/feature
   - Implement it
   - Add unit test
   - Re-run demo
4. Move to next demo

## Test Execution Workflow

```
┌─────────────────────────────────────────────┐
│ 1. Execute demo in C gnuplot               │
│    $ gnuplot simple.dem                     │
│    Output: simple_c.svg                     │
└──────────────────┬──────────────────────────┘
                   │
                   ▼
┌─────────────────────────────────────────────┐
│ 2. Execute demo in Java gnuplot            │
│    $ ./gnuplot simple.dem                   │
│    Output: simple_java.svg                  │
└──────────────────┬──────────────────────────┘
                   │
                   ▼
┌─────────────────────────────────────────────┐
│ 3. Compare outputs                          │
│    - SVG structure comparison               │
│    - Visual pixel diff                      │
│    - Parse error logs                       │
└──────────────────┬──────────────────────────┘
                   │
                   ▼
┌─────────────────────────────────────────────┐
│ 4. Identify gaps                            │
│    - Missing commands: set key, set style   │
│    - Wrong rendering: impulses style        │
│    - Data file errors: column parsing      │
└──────────────────┬──────────────────────────┘
                   │
                   ▼
┌─────────────────────────────────────────────┐
│ 5. Implement missing features               │
│    - Add grammar rules                      │
│    - Implement executor logic               │
│    - Add rendering support                  │
│    - Write unit tests                       │
└──────────────────┬──────────────────────────┘
                   │
                   ▼
┌─────────────────────────────────────────────┐
│ 6. Re-test until passing                    │
│    Repeat steps 2-5 until output matches    │
└─────────────────────────────────────────────┘
```

## Metrics & Tracking

### Progress Metrics
- **Demo Pass Rate**: X/100+ demos passing
- **Command Coverage**: Y/Z commands implemented
- **Visual Similarity**: Average pixel diff < 5%
- **Test Coverage**: Keep 95%+ unit test coverage

### Report Format
```
Demo Test Results - Run 2025-10-03
=====================================

Tier 1 (Basic): 4/6 passing (67%)
  ✅ scatter.dem - PASS (100% match)
  ✅ errorbars.dem - PASS (98% match)
  ❌ simple.dem - FAIL
     Missing: set key left box
     Missing: 'data.dat' with impulses
     Rendering: Wrong impulses thickness
  ❌ controls.dem - FAIL
     Missing: if/else statements
     Missing: for loops
  ⏸️  using.dem - SKIP (data files not implemented)
  ⏸️  fillstyle.dem - SKIP (fill patterns not implemented)

Tier 2 (Intermediate): 0/15 passing (0%)
  ⏸️  All skipped - dependencies not met

Overall: 4/100 demos passing (4%)
```

## Integration with Existing Work

### Already Implemented (Leverage These)
- ✅ Expression parser (sin, cos, tan, etc.)
- ✅ Basic plotting (plot sin(x))
- ✅ Line styles (7 types)
- ✅ Scatter plots (10 marker types)
- ✅ Bar charts (vertical, horizontal, grouped, stacked)
- ✅ Error bars (symmetric, asymmetric)
- ✅ Multi-plot layouts (grid, custom)
- ✅ Legend system
- ✅ Color palettes
- ✅ Axis rendering (linear, log, time)

### Gaps to Fill (Priority Order)
1. **Data file reading** - Required by 80% of demos
2. **Plot styles** - `with impulses`, `with boxes`, `with steps`
3. **Set commands** - `set key`, `set style`, `set border`
4. **Control flow** - `if/else`, `for`, `while`
5. **Polar coordinates** - `set polar`
6. **3D plotting** - `splot` command
7. **Data transformations** - `smooth`, `fit`

## Success Criteria

**Milestone 1** (End of Month 1): Pass all Tier 1 demos (6/6)
**Milestone 2** (End of Month 2): Pass 50% of Tier 2 demos (8/15)
**Milestone 3** (End of Month 3): Pass all Tier 2 demos + 30% Tier 3 (15+3/~30)

**Final Goal**: Pass 80%+ of all demos (80/100+)

## Backlog Integration

This test-driven approach will:
1. **Replace** speculative feature development
2. **Prioritize** features based on demo requirements
3. **Validate** each feature immediately with real Gnuplot scripts
4. **Ensure** compatibility with C gnuplot behavior

Each story will be tracked in IMPLEMENTATION_BACKLOG.md with:
- Demo file(s) being targeted
- Expected vs actual output
- Gap analysis results
- Implementation plan
