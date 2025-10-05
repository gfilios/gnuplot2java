# Claude Session Startup Instructions

## 🚨 START HERE - READ THIS FIRST 🚨

**Before doing ANYTHING in this project, you MUST:**

1. **Read**: [CLAUDE_DEVELOPMENT_GUIDE.md](../CLAUDE_DEVELOPMENT_GUIDE.md) - MANDATORY
2. **Run tests**: Check current state before making changes
3. **Review**: Latest test results in `test-results/latest/`

---

## Quick Start Commands

```bash
# 1. Check latest test results
cat test-results/latest/summary.txt
cat test-results/latest/comparison_simple.dem.txt

# 2. Run tests
cd test-tools
./run_demo_tests.sh simple

# 3. Build and test Java code
cd ../gnuplot-java
mvn clean test
```

---

## Core Principles (Never Forget!)

1. **TEST-FIRST**: Run tests before and after changes
2. **NO HARDCODING**: Use C algorithms, not magic numbers
3. **READ C CODE**: Find the C implementation first
4. **PORT, DON'T GUESS**: Implement same logic, Java way
5. **VALIDATE**: Use test-tools to verify everything

---

## Current Known Issues (from test-results)

Based on latest test run:

### Plot 1 (simple.dem)
- ❌ **Border**: Missing in Java (C has border frame)
- ❌ **Y-axis ticks**: Java=1, C=7 (missing 6 ticks)
- ❌ **X-axis ticks**: Java=0, C=5 (missing all ticks)
- ⚠️ **Legend position**: Off by ~4px
- ⚠️ **Line rendering**: Java=22 paths, C=3 paths
- ⚠️ **Visual edges**: 207% more edges (11,931 vs 3,882)

### Priorities
1. Fix axis tick generation (see: gnuplot-c/src/graphics.c)
2. Add border rendering
3. Fix line segmentation

---

## Test-Tools Location

```
test-tools/
├── comparison/
│   ├── compare_deep.sh      # Element comparison
│   ├── compare_svg.sh       # Structure comparison
│   └── compare_visual.sh    # Pixel comparison
└── run_demo_tests.sh        # Main runner
```

---

## C Code Reference Locations

Common rendering code locations:

- **Border**: `gnuplot-c/src/graphics.c:plot_border()`
- **Ticks**: `gnuplot-c/src/graphics.c:quantize_normal_tics()`
- **Legend**: `gnuplot-c/src/graphics.c:do_key_sample()`
- **Title**: `gnuplot-c/src/graphics.c:do_plot_title()`

---

## Documentation Files

- [CLAUDE_DEVELOPMENT_GUIDE.md](../CLAUDE_DEVELOPMENT_GUIDE.md) - **START HERE**
- [CONTRIBUTING.md](../CONTRIBUTING.md) - Contribution guidelines
- [README.md](../README.md) - Project overview
- [test-tools/README.md](../test-tools/README.md) - Test tools docs

---

**Remember**: The test-tools will tell you exactly what's different. Let them guide your work!
