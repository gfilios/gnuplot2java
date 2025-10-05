# Visual Comparison Tools for Gnuplot C vs Java

This directory contains comprehensive comparison tools to ensure the Java implementation produces visually identical output to the C reference implementation.

## Quick Start

```bash
# Run all comparisons at once (recommended)
./compare_all.sh test-results/latest/outputs/simple_c.svg \
                  test-results/latest/outputs/simple_java.svg
```

## Individual Tools

### 1. SVG Code Analysis (`compare_svg.sh`)
Analyzes the SVG markup structure and elements.

```bash
./compare_svg.sh <c_file.svg> <java_file.svg>
```

**Checks:**
- ✅ Plot style (LINES/POINTS/LINESPOINTS)
- ✅ Axis positioning
- ✅ Border presence
- ✅ Color palette
- ✅ Legend presence
- ✅ File size ratio

**Example Output:**
```
❌ MISMATCH: C uses LINES (3 paths), Java uses POINTS (157 markers)
✅ MATCH: Both have border present
✅ Colors match: #9400D3, #009E73, #56B4E9
```

---

### 2. Visual Image Comparison (`compare_visual.sh`)
Converts SVG to PNG and performs pixel-level analysis using ImageMagick.

```bash
./compare_visual.sh <c_file.svg> <java_file.svg>
```

**Requirements:** ImageMagick (`brew install imagemagick`)

**Checks:**
- 🖼️ Pixel-by-pixel differences
- 🎨 Unique color count (anti-aliasing detection)
- 📐 Edge pixel analysis (detects point vs line rendering)
- 📊 Structural differences

**Outputs:**
- `/tmp/gnuplot_visual_comparison/simple_c.png` - C rendered image
- `/tmp/gnuplot_visual_comparison/simple_java.png` - Java rendered image
- `/tmp/gnuplot_visual_comparison/diff_*.png` - Difference map
- `/tmp/gnuplot_visual_comparison/overlay_*.png` - Side-by-side comparison

**Example Output:**
```
C Gnuplot edge pixels:    3,882
Java Gnuplot edge pixels: 11,701
⚠️  Structural difference: 201.4% more/fewer edges
   (This indicates different line/point rendering)
```

**Key Metrics:**
- **Edge pixels** should be within ±10% for similar rendering
- **Unique colors** should be within ±20%
- **201% more edges** = Java using points instead of lines!

---

### 3. Python Image Analysis (`compare_images.py`)
Advanced pixel analysis with regional breakdowns.

```bash
./compare_images.py <c_file.svg> <java_file.svg>
```

**Requirements:**
```bash
pip3 install cairosvg Pillow numpy
```

**Features:**
- 🗺️ Regional analysis (9-zone grid)
- 📊 Per-channel RGB differences
- 🔍 Visual element detection
- 🎨 Amplified difference maps (10x visibility)

**Use When:**
- Need to identify which region of the plot has issues
- Debugging color mismatches
- Detailed statistical analysis required

---

### 4. Comprehensive Suite (`compare_all.sh`)
Runs all available comparison tools in sequence.

```bash
./compare_all.sh <c_file.svg> <java_file.svg>
```

**Automatically:**
- Checks which tools are available
- Runs SVG code analysis
- Runs visual image comparison (if ImageMagick available)
- Falls back to Python analysis if ImageMagick unavailable
- Generates all diff images
- Provides summary of findings

---

## Interpreting Results

### ✅ Graphs Match When:

| Metric | Expected Range | What It Means |
|--------|---------------|---------------|
| Plot Style | Exact match | C=LINES → Java=LINES |
| Edge Pixels | ±10% | Similar line/point complexity |
| Unique Colors | ±20% | Similar anti-aliasing |
| Pixel Diff | <5% | Visually identical |

### ❌ Common Issues:

| Symptom | Root Cause | Fix |
|---------|-----------|-----|
| +200% edges | Points instead of lines | Change POINTS → LINESPOINTS |
| +170% colors | Point marker anti-aliasing | Fix plot style |
| Axes displaced | Wrong coordinate mapping | Review axis rendering |
| Missing border | Border not rendered | Check Scene.showBorder |

---

## Current Known Issues (2025-10-05)

### Issue #1: "points" Style Interpretation
**Status:** ❌ Open
**Severity:** Critical
**Evidence:**
- C: 3 line paths + 7 point markers
- Java: 0 line paths + 157 point markers
- Edge pixels: C=3,882 vs Java=11,701 (+201%)

**Fix:** In `GnuplotScriptExecutor.java`, change:
```java
case "points" -> LinePlot.PlotStyle.LINESPOINTS;  // C Gnuplot compatibility
```

**Reference:** See `VISUAL_COMPARISON_APPROACH.md` lines 163-190

---

## Viewing Visual Differences

After running comparisons, view the generated images:

```bash
# Side-by-side comparison (C on left, Java on right)
open /tmp/gnuplot_visual_comparison/overlay_*.png

# Difference map (white = same, colored = different)
open /tmp/gnuplot_visual_comparison/diff_*.png

# Individual PNGs
open /tmp/gnuplot_visual_comparison/simple_c.png
open /tmp/gnuplot_visual_comparison/simple_java.png
```

---

## Integration with Test Suite

After each code change:

```bash
# 1. Rebuild
mvn clean install -DskipTests -Djacoco.skip=true

# 2. Run demo tests
mvn test -Dtest=DemoTestSuite -q

# 3. Compare outputs
./compare_all.sh test-results/latest/outputs/simple_c.svg \
                  test-results/latest/outputs/simple_java.svg

# 4. Inspect visuals
open /tmp/gnuplot_visual_comparison/overlay_*.png
```

---

## Troubleshooting

### "ImageMagick not found"
```bash
# macOS
brew install imagemagick

# Ubuntu/Debian
sudo apt-get install imagemagick

# Verify
magick --version
```

### "Python dependencies missing"
```bash
# Install with pip
pip3 install cairosvg Pillow numpy

# Or use system packages (macOS)
brew install cairo
pip3 install --break-system-packages cairosvg Pillow numpy
```

### "No such file or directory"
Make sure you:
1. Run tests first: `mvn test -Dtest=DemoTestSuite -q`
2. Use correct path: `test-results/latest/outputs/*.svg`
3. Scripts are executable: `chmod +x compare_*.sh`

---

## Files Overview

| File | Purpose | Dependencies |
|------|---------|-------------|
| `compare_svg.sh` | SVG code analysis | bash, grep, bc |
| `compare_visual.sh` | Visual image comparison | ImageMagick |
| `compare_images.py` | Python pixel analysis | Python 3, cairosvg, PIL, numpy |
| `compare_all.sh` | Comprehensive wrapper | All of the above (optional) |
| `VISUAL_COMPARISON_APPROACH.md` | Detailed documentation | None |
| `COMPARISON_TOOLS_README.md` | This file | None |

---

## For Future Sessions

Always run comprehensive comparison **before** marking a test as passing:

```bash
# After any rendering change
./compare_all.sh test-results/latest/outputs/simple_c.svg \
                  test-results/latest/outputs/simple_java.svg
```

If any metric shows >10% deviation, investigate further before claiming compatibility.

---

## Contributing

When adding new comparison checks:

1. Add to appropriate script (`compare_svg.sh` or `compare_visual.sh`)
2. Document in `VISUAL_COMPARISON_APPROACH.md`
3. Update this README with new metrics/interpretations
4. Test on multiple demo files (simple.dem, scatter.dem, controls.dem)
