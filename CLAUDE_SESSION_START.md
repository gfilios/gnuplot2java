# Claude Session Startup Guide

**READ THIS FIRST** at the start of every Claude Code session working on the Gnuplot Java modernization project.

---

## Modernization Approach

This project is a **progressive rewrite** of gnuplot from C to Java. The original C implementation is preserved in `gnuplot-c/` as the reference ("oracle"), while the new Java implementation lives in `gnuplot-java/`.

**Key Principle:** The C gnuplot output is the ground truth. Java must produce visually identical results.

### Why Progressive Rewrite (Not Conversion)

1. **C code is not directly convertible** - gnuplot's C codebase uses patterns that don't translate well to Java
2. **Algorithm extraction** - We study C algorithms and reimplement them using Java idioms
3. **Test-driven validation** - Official demo files (`gnuplot-c/demo/*.dem`) serve as test oracles
4. **Incremental progress** - Each demo file validated = more features working

### The Oracle Pattern

```
C Gnuplot (Oracle)          Java Gnuplot (Implementation)
       │                              │
       ▼                              ▼
   demo.dem ──────────────────► demo.dem
       │                              │
       ▼                              ▼
   output.svg                    output.svg
       │                              │
       └──────── COMPARE ─────────────┘
                    │
                    ▼
            Pixel Similarity ≥80%
```

---

## Mandatory Development Workflow

### TEST-DRIVEN DEVELOPMENT IS MANDATORY

**Every session MUST follow this cycle:**

```bash
# 1. START: Run ALL tests BEFORE coding
cd gnuplot-java
mvn test                                    # 1005 unit tests
mvn test -pl gnuplot-cli -Dtest=DemoTestSuite  # 3 demo comparison tests

# 2. IDENTIFY: Find specific issues in test output
# Look for: "❌ Mismatch detected" or "⚠️ Warning"
# Check pixel similarity percentages (must be ≥80%)

# 3. LOCATE: Find C gnuplot implementation
cd ../gnuplot-c
grep -rn "feature_name" src/

# 4. IMPLEMENT: Port algorithm to Java
cd ../gnuplot-java
# Write code following C algorithm...

# 5. VERIFY: Run tests AFTER coding
mvn test                                    # Must pass 1005 tests
mvn test -pl gnuplot-cli -Dtest=DemoTestSuite  # Must pass 3 demos

# 6. CONFIRM: Check all tests pass
# Demo tests now assert:
#   - C execution success
#   - Java execution success
#   - Pixel similarity ≥80%
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
- `DemoTestSuite` - Automated demo testing (3 demos with pixel similarity assertions)
- Unit Tests - 1005 tests across core, render, and cli modules
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

## Essential Documentation Links

### Must-Read Documents

1. **[CLAUDE_DEVELOPMENT_GUIDE.md](CLAUDE_DEVELOPMENT_GUIDE.md)** - Complete workflow, anti-patterns, C code reference process
2. **[IMPLEMENTATION_STATUS.md](IMPLEMENTATION_STATUS.md)** - Detailed feature matrix, algorithm comparisons
3. **[README.md](README.md)** - Project overview, quick start, architecture
4. **[CONTRIBUTING.md](CONTRIBUTING.md)** - Contribution guidelines, coding standards

### Strategy & Planning

- [MODERNIZATION_STRATEGY.md](MODERNIZATION_STRATEGY.md) - Why progressive rewrite vs conversion
- [TEST_DRIVEN_PLAN.md](TEST_DRIVEN_PLAN.md) - TDD methodology using demo suite as oracle

### Testing

- [TESTING.md](TESTING.md) - How to run tests, test infrastructure
- [test-tools/README.md](test-tools/README.md) - Visual comparison tools

### Setup

- [SETUP.md](SETUP.md) - Development environment (JDK 21, Maven 3.9+)

---

## Command Cheat Sheet

### Common Development Tasks

```bash
# Build everything
cd gnuplot-java
mvn clean install -Djacoco.skip=true

# Run all unit tests (1005 tests)
mvn test

# Run demo comparison tests (requires C gnuplot)
mvn test -pl gnuplot-cli -Dtest=DemoTestSuite

# Run ALL tests together
mvn test && mvn test -pl gnuplot-cli -Dtest=DemoTestSuite

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

**Remember:** Test-driven development is mandatory. Always run tests before and after changes. Never hardcode values. Always reference C source code.
