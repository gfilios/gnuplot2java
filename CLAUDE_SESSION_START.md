# Claude Session Startup Guide

**READ THIS FIRST** at the start of every Claude Code session working on the Gnuplot Java modernization project.

---

## Current Project Status (Updated: 2025-11-04)

### Test Results: 3/3 Demos Passing (100%)

| Demo | Status | Visual Accuracy | Issues |
|------|--------|----------------|--------|
| **simple.dem** | ✅ PASS | ~95% | Missing impulse lines (plot 4) |
| **scatter.dem** | ✅ PASS | ~90% | Legend +45px offset, Y/Z tick labels missing |
| **controls.dem** | ✅ PASS | Not analyzed | None reported |

**Total Available Demos:** 231
**Implemented:** 3 (1.3%)

### Recent Changes

**Latest Fix (2025-10-07):** Point marker visibility (CRITICAL)
- **Issue:** Point markers invisible in SVG despite correct structure
- **Root Cause:** SVG `clip-path` incompatibility with `<path>` transforms
- **Solution:** Wrap paths in `<g>` element, apply clip-path to parent
- **Impact:** All point marker types now visible (Cross, Plus, Circle, etc.)
- **Files:** [SvgRenderer.java:469-504](gnuplot-java/gnuplot-render/src/main/java/com/gnuplot/render/svg/SvgRenderer.java#L469-L504)

**Previous Milestones:**
- 2025-10-05: Default plot styles, impulses rendering, legend positioning
- 2025-10-01: CLI interface complete (5 execution modes), 31 CLI tests passing

### Known Issues & Active Work

**Priority 1 - Missing Features:**
1. **Impulse Lines** - Not implemented for 2D/3D (affects simple.dem plot 4)
   - See [BACKLOG_IMPULSES_POINTS.md](BACKLOG_IMPULSES_POINTS.md)
   - Estimated: 2-3 hours

**Priority 2 - Minor Cosmetic Issues:**
2. **Legend Positioning** - 45px horizontal offset in 3D plots (scatter.dem)
   - Cosmetic only, does not affect functionality
   - Estimated: 30-60 minutes

**Recently Completed:**
- ✅ **3D Y-Axis Positioning** (Fixed 2025-11-03, commit 0759a997)
  - Implemented 4/7 scaling ratio matching C gnuplot
  - [SvgRenderer.java:1637-1647](gnuplot-java/gnuplot-render/src/main/java/com/gnuplot/render/svg/SvgRenderer.java#L1637-L1647)
- ✅ **Y/Z Tick Labels** (Already implemented)
  - All 3D axes have tick marks and labels
  - 33 labels rendered correctly

---

## Mandatory Development Workflow

### TEST-DRIVEN DEVELOPMENT IS MANDATORY

**Every session MUST follow this cycle:**

```bash
# 1. START: Run tests BEFORE coding
cd test-tools
./run_demo_tests.sh simple
cat test-results/latest/comparison_simple.dem.txt

# 2. IDENTIFY: Find specific issues in test output
# Look for: "❌ Mismatch detected" or "⚠️ Warning"

# 3. LOCATE: Find C gnuplot implementation
cd ../gnuplot-c
grep -rn "feature_name" src/

# 4. IMPLEMENT: Port algorithm to Java
cd ../gnuplot-java
# Write code following C algorithm...

# 5. VERIFY: Run tests AFTER coding
mvn clean test
cd ../test-tools
./run_demo_tests.sh simple

# 6. CONFIRM: Check test results improved
cat test-results/latest/comparison_simple.dem.txt
```

### Session Startup Checklist

At the start of **EVERY** Claude session:

- [ ] Read this file (`CLAUDE_SESSION_START.md`)
- [ ] Read [CLAUDE_DEVELOPMENT_GUIDE.md](CLAUDE_DEVELOPMENT_GUIDE.md) for detailed workflow
- [ ] Check latest test results: `test-results/latest/summary.txt`
- [ ] Review comparison files: `test-results/latest/comparison_*.txt`
- [ ] Identify top priority issues from test results
- [ ] Locate C source code for the issue
- [ ] Create TodoWrite list for the session
- [ ] Start fixing with test-first approach

---

## Critical Anti-Patterns (What NOT to Do)

### ❌ NEVER Hardcode Values

**BAD:**
```java
private static final double AXIS_START = 54.53;  // DON'T!
```

**GOOD:**
```java
double axisStart = calculateAxisStart(viewport, margins);
```

### ❌ NEVER Guess Algorithms

**BAD:**
```java
int tickCount = (int) (range / 10);  // Random guess!
```

**GOOD:**
```java
// From gnuplot-c/src/graphics.c:quantize_normal_tics()
int tickCount = quantizeNormalTics(range, maxTics);
```

### ❌ NEVER Skip Testing

- Always run tests BEFORE and AFTER changes
- Use `test-tools/run_demo_tests.sh` for validation
- Never commit without verifying tests still pass

---

## Quick Reference: C Code Locations

### Common Features & Their C Sources

| Feature | C Source File | Java Target |
|---------|---------------|-------------|
| Border/Frame | `graphics.c:plot_border()` | `SvgRenderer.renderBorder()` |
| Axis Ticks | `graphics.c:quantize_normal_tics()` | `TickGenerator.quantizeNormalTics()` |
| Legend | `graphics.c:do_key_sample()` | `LegendRenderer.calculatePosition()` |
| Title | `graphics.c:do_plot_title()` | `TitleRenderer.renderTitle()` |
| 3D Projection | `graph3d.c:map3d_xyz()` | `ViewTransform3D.project()` |
| 3D Scaling | `graph3d.c:534-545` (4/7 ratio) | `SvgRenderer.mapProjectedX/Y()` |

### How to Search C Codebase

```bash
cd gnuplot-c/src

# Search by function name
grep -rn "plot_border" .

# Search by feature keyword
grep -rn "legend" . | head -20

# Search by setting
grep -rn "set key" .

# Find specific calculations
grep -rn "xscaler" .
```

---

## Architecture Quick Reference

### Module Structure

```
gnuplot-java/
├── gnuplot-core/          # Parsing, AST, expression evaluation
│   ├── parser/            # ANTLR4 grammar
│   └── grid/              # dgrid3d interpolation
├── gnuplot-render/        # Rendering engine
│   ├── elements/          # Scene graph elements
│   ├── projection/        # 3D transformations (ViewTransform3D)
│   └── svg/               # SVG renderer
└── gnuplot-cli/           # Command-line interface
    ├── executor/          # Script execution
    └── demo/              # Test infrastructure (DemoTestSuite)
```

### Key Classes to Know

**3D Rendering:**
- `ViewTransform3D` - 3D to 2D projection (rotation, scaling)
- `SurfacePlot3D` - 3D plot scene element
- `Dgrid3D` - Grid interpolation algorithm (qnorm)

**2D Rendering:**
- `LinePlot` - 2D line plots
- `ScatterPlot` - 2D point plots
- `Legend` - Legend rendering
- `SvgRenderer` - Main SVG output generator

**Testing:**
- `DemoTestSuite` - Automated demo testing (989 tests)
- `ComparisonRunner` - Visual comparison tools (test-tools/)

---

## Test Tools Overview

### Available Comparison Tools

Located in `test-tools/`:

```bash
# Run all comparisons for a demo
./run_demo_tests.sh simple

# Individual comparison types
./comparison/compare_deep.sh     # Element-by-element analysis
./comparison/compare_svg.sh      # SVG structure comparison
./comparison/compare_visual.sh   # Pixel-level PNG diff
```

### Reading Test Output

Test results in `test-results/latest/`:

```
comparison_simple.dem.txt        # Detailed comparison report
  ├── Title positioning
  ├── Border/frame presence
  ├── Axis tick counts (C vs Java)
  ├── Legend position/entries
  ├── Line rendering modes
  └── Visual differences

summary.txt                      # Overall pass/fail summary
outputs/                         # Generated SVG/PNG files
  ├── simple_c.svg              # C gnuplot reference
  ├── simple_java.svg           # Java implementation
  └── simple_diff.png           # Visual diff (red=differences)
```

**What tests detect:**
- ✅ Tick count mismatches (e.g., "C=7, Java=1")
- ✅ Missing elements (e.g., "Border: NOT FOUND")
- ✅ Position differences (e.g., "+45px offset")
- ✅ Color/style differences
- ✅ Pixel-level visual differences

---

## Implementation Features Matrix

### What's Working ✅

**2D Plotting:**
- Function evaluation (sin, cos, atan, bessel)
- Point markers (8 types, optimized with `<use>` refs)
- Line plots (continuous lines)
- Legend (92% accurate positioning)
- Axes with tick marks
- Grid lines
- Titles and labels
- Variable sampling (50-400 samples)

**3D Plotting:**
- Point cloud scatter plots
- Wireframe rendering (LINES style)
- 3D coordinate axes
- ViewTransform3D projection (60°, 30° rotation)
- dgrid3d qnorm interpolation (weighted)
- Legend positioning (top-right)

**Rendering & Optimization:**
- SVG output (primary format)
- Point marker optimization (75% size reduction vs inline paths)
- File size 50% smaller than C gnuplot

### What's Missing ❌

**2D Features:**
- Impulses (vertical bars from baseline)
- Boxes, steps, histograms
- More advanced plot styles

**3D Features:**
- Y/Z axis tick labels
- Impulse guide lines
- Surface plots (pm3d)
- Contour lines

---

## Performance Metrics

### File Size Optimization

| Metric | C Gnuplot | Java (Optimized) | Improvement |
|--------|-----------|------------------|-------------|
| scatter.dem SVG | 50 KB | 25 KB | **50% smaller** |
| Line count | 600 | 282 | **53% reduction** |
| Point markers | 257 inline | 249 `<use>` refs | **Optimized** |

### Test Execution

| Demo | Plots | C Runtime | Java Runtime |
|------|-------|-----------|--------------|
| simple.dem | 8 | ~2 sec | ~3 sec |
| scatter.dem | 8 | ~3 sec | ~4 sec |

**Total Tests:** 989 passing (31 CLI + 958 core)

---

## Next Priorities (Suggested)

### To Fix Current Issues (Quick Wins)

1. **Impulse Lines** (2-3 hours) ⭐ **TOP PRIORITY**
   - Implement "with impulses" style for 2D and 3D
   - Vertical lines from baseline to data point
   - Affects simple.dem plot 4 and scatter.dem
   - See [BACKLOG_IMPULSES_POINTS.md](BACKLOG_IMPULSES_POINTS.md)
   - C code reference: `graphics.c` and `graph3d.c`

2. **Legend Positioning Fine-tuning** (30-60 min)
   - Fix 45px horizontal offset in 3D plots
   - Cosmetic improvement for scatter.dem
   - File: [SvgRenderer.java](gnuplot-java/gnuplot-render/src/main/java/com/gnuplot/render/svg/SvgRenderer.java)

3. **Number Format Matching** (30 min)
   - Match C gnuplot's number formatting (`-1` vs `-1.0`)
   - Low priority, cosmetic only

### To Add New Demos

**Next suggested demos:**
- `arrows.dem` - Arrow rendering
- `boxes.dem` - Box plots
- `surface1.dem` - 3D surfaces

---

## Essential Documentation Links

### Must-Read Documents

1. **[CLAUDE_DEVELOPMENT_GUIDE.md](CLAUDE_DEVELOPMENT_GUIDE.md)** - Complete workflow, anti-patterns, C code reference process
2. **[IMPLEMENTATION_STATUS.md](IMPLEMENTATION_STATUS.md)** - Detailed feature matrix, algorithm comparisons
3. **[README.md](README.md)** - Project overview, quick start, architecture
4. **[CONTRIBUTING.md](CONTRIBUTING.md)** - Contribution guidelines, coding standards

### Deep Dive Documentation

**Strategy & Planning:**
- [MODERNIZATION_STRATEGY.md](MODERNIZATION_STRATEGY.md) - Why progressive rewrite vs conversion
- [TEST_DRIVEN_PLAN.md](TEST_DRIVEN_PLAN.md) - TDD methodology using demo suite as oracle
- [MODERNIZATION_PROPOSAL.md](MODERNIZATION_PROPOSAL.md) - Original architecture proposal

**Testing:**
- [TESTING.md](TESTING.md) - How to run tests, test infrastructure
- [test-tools/README.md](test-tools/README.md) - Visual comparison tools
- [test-tools/docs/INTEGRATED_TESTING_GUIDE.md](test-tools/docs/INTEGRATED_TESTING_GUIDE.md) - Automated comparison

**Implementation Details:**
- [3D_YAXIS_POSITIONING_ANALYSIS.md](3D_YAXIS_POSITIONING_ANALYSIS.md) - 3D positioning issue analysis
- [gnuplot-render/ARCHITECTURE.md](gnuplot-java/gnuplot-render/ARCHITECTURE.md) - Scene graph, visitor pattern
- [docs/STORY_TDD4_ROADMAP.md](docs/STORY_TDD4_ROADMAP.md) - simple.dem implementation roadmap

**Current Stories:**
- [BACKLOG_IMPULSES_POINTS.md](BACKLOG_IMPULSES_POINTS.md) - Impulses and point marker fixes
- [docs/STORY_TDD6_LEGEND_INTEGRATION.md](docs/STORY_TDD6_LEGEND_INTEGRATION.md) - Legend system integration

### Reference Documentation

**Setup:**
- [SETUP.md](SETUP.md) - Development environment (JDK 21, Maven 3.9+)
- [QUICK_START.md](QUICK_START.md) - Get started in 5 minutes

**Backlog:**
- [IMPLEMENTATION_BACKLOG.md](IMPLEMENTATION_BACKLOG.md) - Complete backlog (200+ stories, 39K tokens - too large to read in one go, use search)

**C Reference:**
- [gnuplot-c/README_MODERNIZATION.md](gnuplot-c/README_MODERNIZATION.md) - Purpose of preserved C code

---

## Command Cheat Sheet

### Common Development Tasks

```bash
# Build everything
cd gnuplot-java
mvn clean install

# Run all tests
mvn test

# Run specific test
mvn test -Dtest=DemoTestSuite

# Run demo comparison
cd ../test-tools
./run_demo_tests.sh simple

# View test results
cat test-results/latest/summary.txt
cat test-results/latest/comparison_simple.dem.txt

# Generate SVG from .dem file
cd gnuplot-java
java -jar gnuplot-cli/target/gnuplot-cli-1.0.0-SNAPSHOT-jar-with-dependencies.jar /path/to/file.dem

# View generated SVG
open /tmp/output.svg

# Search C code
cd gnuplot-c/src
grep -rn "feature_name" .

# Check git status
git status
git diff
```

### Test Tools Commands

```bash
cd test-tools

# Run specific demo
./run_demo_tests.sh simple
./run_demo_tests.sh scatter

# Run specific comparison type
./comparison/compare_deep.sh simple.dem
./comparison/compare_svg.sh simple.dem
./comparison/compare_visual.sh simple.dem

# View comparison results
ls -lh test-results/latest/
cat test-results/latest/comparison_*.txt
```

---

## Key Success Metrics

### You're On Track If:

- ✅ Test comparison shows decreasing differences
- ✅ C and Java tick counts match
- ✅ Border/frame rendering matches
- ✅ Visual pixel differences decreasing
- ✅ Code has C source references in JavaDoc
- ✅ No hardcoded magic numbers

### Warning Signs:

- ❌ Adding features without running tests
- ❌ Hardcoding values to "make tests pass"
- ❌ Guessing algorithms instead of reading C code
- ❌ Skipping test-tools validation
- ❌ Not documenting C source references

---

## Emergency Reference

### If Tests Are Failing

1. **Check recent changes:**
   ```bash
   git diff
   git log -3 --oneline
   ```

2. **Compare test outputs:**
   ```bash
   diff test-results/latest/outputs/simple_c.svg \
        test-results/latest/outputs/simple_java.svg
   ```

3. **Run tests in isolation:**
   ```bash
   cd gnuplot-java
   mvn clean test -Dtest=DemoTestSuite#testSimpleDem
   ```

4. **Check build errors:**
   ```bash
   mvn clean install 2>&1 | grep ERROR
   ```

### If You Can't Find C Code

**Common C source files:**
- `gnuplot-c/src/graphics.c` - Main 2D rendering
- `gnuplot-c/src/graph3d.c` - 3D rendering
- `gnuplot-c/src/term.c` - Terminal drivers (SVG output)
- `gnuplot-c/src/axis.c` - Axis calculations
- `gnuplot-c/src/set.c` - Setting handlers

**Search strategies:**
```bash
# By command name
grep -rn "set key" gnuplot-c/src/

# By function pattern
grep -rn "border" gnuplot-c/src/graphics.c

# By algorithm keyword
grep -rn "ticslevel\|xscaler\|quantize" gnuplot-c/src/
```

---

## Document History

- **Created:** 2025-11-04
- **Purpose:** Consolidated session startup guide for Claude Code
- **Replaces:** Reading 5-7 separate documents at session start
- **Estimated Read Time:** 5-7 minutes
- **Token Count:** ~10,000 tokens (fits in single read)

---

**Remember:** Test-driven development is mandatory. Always run tests before and after changes. Never hardcode values. Always reference C source code. Document everything.

Happy coding!
