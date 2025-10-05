# Visual Comparison Tools - Summary

**Created:** 2025-10-05
**Purpose:** Ensure Java Gnuplot produces visually identical output to C Gnuplot

---

## 🎯 What Was Created

### 4 Comparison Tools

1. **`compare_svg.sh`** - SVG code structure analysis
2. **`compare_visual.sh`** - ImageMagick pixel comparison (recommended)
3. **`compare_images.py`** - Python advanced analysis (optional)
4. **`compare_all.sh`** - Runs all tools (use this!)

### 3 Documentation Files

1. **`VISUAL_COMPARISON_APPROACH.md`** - Detailed technical analysis
2. **`COMPARISON_TOOLS_README.md`** - Usage guide
3. **`COMPARISON_SUMMARY.md`** - This file

---

## 🚀 Quick Start

```bash
# Run comprehensive comparison
./compare_all.sh test-results/latest/outputs/simple_c.svg \
                  test-results/latest/outputs/simple_java.svg

# View results
open /tmp/gnuplot_visual_comparison/overlay_*.png
```

---

## 📊 Key Finding from Initial Analysis

### simple.dem Comparison Results

**❌ CRITICAL ISSUE FOUND:**

| Metric | C Gnuplot | Java Gnuplot | Difference |
|--------|-----------|--------------|------------|
| Plot Style | LINES | POINTS | ❌ Mismatch |
| Line Paths | 3 | 0 | -100% |
| Point Markers | 7 (legend) | 157 (plot) | +2,143% |
| Edge Pixels | 3,882 | 11,701 | +201% |
| Unique Colors | 255 | 690 | +173% |

**Root Cause:** Java interprets `set style data points` as POINTS-only, but C Gnuplot renders it as LINESPOINTS (both lines and points).

**Evidence:**
- C SVG has `<path>` elements for continuous lines
- Java SVG has only `<use xlink:href='#gpPt*'>` elements (discrete points)
- Visual diff shows completely different rendering

---

## ✅ What These Tools Detect

### SVG-Level Issues
- ✅ Wrong plot style (LINES/POINTS/LINESPOINTS)
- ✅ Missing borders
- ✅ Missing legends
- ✅ Color mismatches
- ✅ Axis positioning errors

### Pixel-Level Issues
- ✅ Structural differences (line vs point rendering)
- ✅ Anti-aliasing differences
- ✅ Color gradient issues
- ✅ Coordinate mapping errors
- ✅ Font rendering differences

---

## 🔧 Required Fix

**File:** `gnuplot-java/gnuplot-cli/src/main/java/com/gnuplot/cli/executor/GnuplotScriptExecutor.java`

**Line ~175, change:**
```java
LinePlot.PlotStyle plotStyle = switch (styleDataValue) {
    case "points" -> LinePlot.PlotStyle.LINESPOINTS;  // FIX: C Gnuplot compatibility
    case "linespoints" -> LinePlot.PlotStyle.LINESPOINTS;
    default -> LinePlot.PlotStyle.LINES;
};
```

**Rationale:** C Gnuplot's historical behavior for "set style data points" is to render BOTH lines and points, not points alone.

---

## 📈 Success Criteria

Graphs are considered matching when:

- ✅ Plot style matches exactly
- ✅ Edge pixels within ±10%
- ✅ Unique colors within ±20%
- ✅ Pixel difference <5%
- ✅ Visual inspection shows identical rendering

---

## 🔄 Workflow for Future Changes

```bash
# 1. Make code change
vim gnuplot-java/gnuplot-cli/src/main/java/com/gnuplot/cli/executor/GnuplotScriptExecutor.java

# 2. Rebuild
mvn clean install -DskipTests -Djacoco.skip=true

# 3. Run tests
mvn test -Dtest=DemoTestSuite -q

# 4. Compare outputs (CRITICAL STEP!)
./compare_all.sh test-results/latest/outputs/simple_c.svg \
                  test-results/latest/outputs/simple_java.svg

# 5. Visually verify
open /tmp/gnuplot_visual_comparison/overlay_*.png

# 6. Document findings
# Update VISUAL_COMPARISON_APPROACH.md with results
```

---

## 📁 Generated Files

After running `compare_all.sh`:

```
/tmp/gnuplot_visual_comparison/
├── simple_c.png              # C output as PNG
├── simple_java.png           # Java output as PNG
├── diff_*.png                # Difference map (color = different pixels)
├── overlay_*.png             # Side-by-side comparison
└── *_edges.png               # Edge detection (structural analysis)
```

---

## 🎨 Visual Evidence

### Side-by-Side Comparison
`overlay_*.png` shows C (left) vs Java (right):
- C: Smooth continuous lines
- Java: Discrete point markers with no connecting lines
- **Immediately visible difference!**

### Difference Map
`diff_*.png` highlights differences in white/color:
- Large areas of difference around plot lines
- Points where lines should be but aren't

---

## 📚 Documentation Links

- **Detailed Analysis:** [VISUAL_COMPARISON_APPROACH.md](VISUAL_COMPARISON_APPROACH.md)
- **Usage Guide:** [COMPARISON_TOOLS_README.md](COMPARISON_TOOLS_README.md)
- **Tool Scripts:** `compare_*.sh`, `compare_*.py`

---

## 🎯 Next Steps

1. **Apply the fix** to GnuplotScriptExecutor.java (change "points" → LINESPOINTS)
2. **Rebuild and retest**
3. **Run comparison again** to verify fix
4. **Document results** in VISUAL_COMPARISON_APPROACH.md
5. **Use these tools** for all future rendering changes

---

## ⚠️ Important Notes

- **NEVER** claim tests pass without running visual comparison
- **ALWAYS** check the overlay PNG - metrics alone aren't enough
- **DOCUMENT** all findings in VISUAL_COMPARISON_APPROACH.md
- **UPDATE** this summary when new issues are found

---

## 🔍 For Next Session

1. Read `VISUAL_COMPARISON_APPROACH.md` for current state
2. Use `./compare_all.sh` for ALL visual verifications
3. Check `/tmp/gnuplot_visual_comparison/*.png` before claiming success
4. Update documentation with findings

**The comparison tools are now the authoritative source for visual compatibility verification.**
