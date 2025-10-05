# Test Tools for Gnuplot C vs Java Comparison

This directory contains all testing infrastructure for validating the Java implementation against the C reference.

## Quick Start

### Run Tests with Automatic Comparison
```bash
cd gnuplot-java/gnuplot-cli
mvn test -Dtest=DemoTestSuite
```

Comparison runs automatically and results are saved to `test-results/latest/`.

### Manual Comparison
```bash
# Run comprehensive comparison on specific files
./comparison/compare_all.sh test-results/latest/outputs/simple_c.svg \
                            test-results/latest/outputs/simple_java.svg
```

---

## Directory Structure

```
test-tools/
├── comparison/              # Comparison scripts
│   ├── compare_all.sh      # ⭐ Master script (runs all comparisons)
│   ├── compare_deep.sh     # Element-by-element analysis
│   ├── compare_svg.sh      # SVG code analysis
│   ├── compare_visual.sh   # Visual image comparison
│   └── compare_images.py   # Python advanced analysis (optional)
│
├── docs/                    # Documentation
│   ├── README.md           # Detailed comparison tools guide
│   ├── VISUAL_COMPARISON_APPROACH.md
│   ├── DEEP_COMPARISON_FINDINGS.md
│   ├── COMPARISON_SUMMARY.md
│   └── INTEGRATED_TESTING_GUIDE.md
│
└── README.md               # This file
```

---

## Comparison Tools

### 1. Deep Element-by-Element Analysis
**Script:** `comparison/compare_deep.sh`
**Analyzes:** Title, border, axes, legend, plot style, fonts, coordinates

```bash
./comparison/compare_deep.sh file_c.svg file_java.svg
```

**Output:**
- Title position and font size
- Border coordinates
- Axis tick counts and positions
- Legend box position and style
- Plot style (lines vs points)
- Critical issues summary

### 2. SVG Code Analysis
**Script:** `comparison/compare_svg.sh`
**Analyzes:** SVG structure, element counts, colors

```bash
./comparison/compare_svg.sh file_c.svg file_java.svg
```

**Output:**
- Plot style comparison
- Color palette verification
- Border/legend presence
- File size comparison

### 3. Visual Image Comparison
**Script:** `comparison/compare_visual.sh`
**Requirements:** ImageMagick (`brew install imagemagick`)
**Analyzes:** Pixel-level differences, edge detection, structural analysis

```bash
./comparison/compare_visual.sh file_c.svg file_java.svg
```

**Output:**
- Pixel difference percentage
- Edge pixel count
- Unique color count
- PNG diff images in `/tmp/gnuplot_visual_comparison/`

### 4. Python Advanced Analysis (Optional)
**Script:** `comparison/compare_images.py`
**Requirements:** `pip3 install cairosvg Pillow numpy`
**Analyzes:** Regional differences, per-channel analysis

```bash
./comparison/compare_images.py file_c.svg file_java.svg
```

**Output:**
- 9-zone regional analysis
- RGB channel differences
- Amplified difference maps

### 5. Comprehensive Suite (Recommended)
**Script:** `comparison/compare_all.sh`
**Runs:** All available comparison tools

```bash
./comparison/compare_all.sh file_c.svg file_java.svg
```

---

## Documentation

### Quick References
- **[Comparison Tools Guide](docs/README.md)** - Complete usage guide
- **[Integrated Testing Guide](docs/INTEGRATED_TESTING_GUIDE.md)** - How tests auto-run comparison
- **[Comparison Summary](docs/COMPARISON_SUMMARY.md)** - Quick reference

### Detailed Analysis
- **[Visual Comparison Approach](docs/VISUAL_COMPARISON_APPROACH.md)** - Methodology and findings
- **[Deep Comparison Findings](docs/DEEP_COMPARISON_FINDINGS.md)** - Element-by-element results

---

## Integration with Test Suite

The comparison tools are automatically integrated with `DemoTestSuite`:

1. **Test execution** - C and Java implementations run
2. **Automatic comparison** - All tools run on outputs
3. **Issue detection** - Critical issues extracted
4. **Results saved** - Logs and images saved to `test-results/`
5. **Console output** - Metrics printed during test

See [Integrated Testing Guide](docs/INTEGRATED_TESTING_GUIDE.md) for details.

---

## Output Locations

| Output | Location |
|--------|----------|
| Comparison logs | `../test-results/latest/comparison_*.txt` |
| Visual diff images | `/tmp/gnuplot_visual_comparison/*.png` |
| Test SVG outputs | `../test-results/latest/outputs/*.svg` |
| HTML reports | `../test-results/latest/index.html` |

---

## What Gets Compared

### Visual Elements
- ✅ Title (position, font size, text)
- ✅ Plot border/frame
- ✅ Axes (X, Y, Z)
  - Tick marks (count, position)
  - Labels (text, position, font)
  - Position (left/bottom vs centered)
- ✅ Legend/Key
  - Box position and size
  - Border style
  - Entry positions
- ✅ Plot style
  - Lines vs Points vs Linespoints
  - Colors (RGB values)
  - Stroke widths

### Image Metrics
- ✅ Pixel differences
- ✅ Edge pixel count
- ✅ Unique color count
- ✅ Structural analysis
- ✅ Regional differences

---

## Adding New Comparisons

To add a new comparison check:

1. **Update `comparison/compare_deep.sh`** for element analysis
2. **Update `ComparisonRunner.java`** to extract new metrics
3. **Update documentation** with new findings

---

## Troubleshooting

### Scripts not found
```bash
# Ensure you're in repository root
cd /path/to/gnuplot-master

# Scripts should be at:
ls test-tools/comparison/*.sh
```

### Scripts not executable
```bash
chmod +x test-tools/comparison/*.sh
```

### ImageMagick not installed
```bash
brew install imagemagick
```

### Python dependencies missing
```bash
pip3 install cairosvg Pillow numpy
```

---

## Maintenance

### Adding New Tools
Place new comparison scripts in `comparison/` directory.

### Updating Documentation
Update files in `docs/` directory.

### Version Control
All test tools are tracked in git:
```bash
git add test-tools/
git commit -m "Update test tools"
```

---

## Related

- **Test Results:** `../test-results/` - Historical test outputs
- **Java Tests:** `../gnuplot-java/gnuplot-cli/src/test/java/` - Test source code
- **C Reference:** `../gnuplot-c/` - Reference implementation

---

**For detailed usage and examples, see [docs/README.md](docs/README.md)**
