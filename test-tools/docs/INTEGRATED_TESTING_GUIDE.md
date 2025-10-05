# Integrated Testing with Automatic Visual Comparison

**Updated:** 2025-10-05

## Overview

The DemoTestSuite now **automatically runs comprehensive visual comparison** after each test execution. No manual steps required!

---

## How It Works

### Before (Manual Process)
```bash
# 1. Run tests
mvn test -Dtest=DemoTestSuite -q

# 2. Manually run comparison (easy to forget!)
./compare_all.sh test-results/latest/outputs/simple_c.svg \
                  test-results/latest/outputs/simple_java.svg

# 3. Check output files
open /tmp/gnuplot_visual_comparison/overlay_*.png
```

### Now (Automatic)
```bash
# Just run the test - comparison happens automatically!
mvn test -Dtest=DemoTestSuite

# Results automatically saved to:
#  - test-results/latest/comparison_simple.dem.txt (full analysis)
#  - /tmp/gnuplot_visual_comparison/ (visual diff images)
```

---

## What Gets Compared Automatically

When you run `mvn test -Dtest=DemoTestSuite`, for each demo file:

1. ✅ **Executes C Gnuplot** (reference implementation)
2. ✅ **Executes Java Gnuplot** (our implementation)
3. ✅ **Runs Deep Element Comparison** (`compare_deep.sh`)
   - Title position, font size
   - Border/frame coordinates
   - Axis positions and tick marks
   - Legend position and style
   - Plot style (lines vs points)
   - Font rendering
4. ✅ **Runs SVG Code Analysis** (`compare_svg.sh`)
   - Plot style detection
   - Color palette verification
   - Element counts
5. ✅ **Runs Visual Image Comparison** (`compare_visual.sh`)
   - Pixel-level differences
   - Edge pixel analysis
   - Unique color count
   - Structural differences
6. ✅ **Extracts and Reports Critical Issues**
7. ✅ **Saves Full Comparison Log**

---

## Example Output

```
╔═══════════════════════════════════════════════════════════╗
║                  simple.dem Test Results                  ║
╚═══════════════════════════════════════════════════════════╝
C Gnuplot Success:    true
Java Gnuplot Success: true

🔍 Running comprehensive comparison analysis...

⚠️  CRITICAL ISSUES FOUND:
   ❌ CRITICAL: Plot style mismatch (C uses LINES, Java uses POINTS)
   ❌ Title font size mismatch: font-size="20.00" vs font-size="16"
   ❌ Legend position differs by 118px horizontally
   ❌ Y-axis tick count differs: 7 vs 9
   ❌ X-axis tick count differs: 5 vs 0

📊 Comparison Metrics:
   Plot Style:
     C:    Lines=3, Points=7
     Java: Lines=0, Points=157
   Edge Pixels:
     C:    3882
     Java: 11701
     Diff: 201.4%
   Unique Colors:
     C:    255
     Java: 690

📄 Full comparison log: test-results/latest/comparison_simple.dem.txt
🖼️  Visual diff images: /tmp/gnuplot_visual_comparison/
```

---

## Integration Details

### New Components

1. **`ComparisonRunner.java`**
   - Executes comparison scripts
   - Parses output and extracts metrics
   - Identifies critical issues automatically

2. **Updated `DemoTestSuite.java`**
   - Creates `ComparisonRunner` instance
   - Runs comparison after each test
   - Prints summary in console
   - Saves full log to repository

### File Locations

| File | Purpose |
|------|---------|
| `test-results/latest/comparison_*.txt` | Full comparison logs (all tools combined) |
| `/tmp/gnuplot_visual_comparison/*.png` | Visual diff images |
| `test-results/latest/outputs/*.svg` | Original C and Java outputs |
| `test-results/latest/index.html` | HTML test report |

---

## Running Tests

### Run All Tests with Comparison
```bash
cd gnuplot-java/gnuplot-cli
mvn test -Dtest=DemoTestSuite
```

### Run Single Test
```bash
mvn test -Dtest=DemoTestSuite#testSimpleDem
```

### Quiet Mode (Less Maven Output)
```bash
mvn test -Dtest=DemoTestSuite -q
```

---

## Viewing Results

### 1. Console Output
Critical issues and metrics are printed directly in the console during test execution.

### 2. Comparison Log File
```bash
# View full comparison analysis
cat test-results/latest/comparison_simple.dem.txt

# Or use less for scrolling
less test-results/latest/comparison_simple.dem.txt
```

### 3. Visual Diff Images
```bash
# Side-by-side comparison
open /tmp/gnuplot_visual_comparison/overlay_*.png

# Difference map (highlights mismatches)
open /tmp/gnuplot_visual_comparison/diff_*.png

# Individual PNGs
open /tmp/gnuplot_visual_comparison/simple_c.png
open /tmp/gnuplot_visual_comparison/simple_java.png
```

### 4. HTML Report
```bash
# Open the generated HTML report
open test-results/latest/index.html
```

---

## Comparison Tools Status

When tests start, you'll see:

```
Comparison Tools Status:
  Deep comparison:   ✅ Available
  SVG comparison:    ✅ Available
  Visual comparison: ✅ Available
  All-in-one:        ✅ Available
```

If any tool shows ❌ or ⚠️:
- Check that scripts exist in repository root
- Verify scripts are executable: `chmod +x compare_*.sh`
- For visual comparison, install ImageMagick: `brew install imagemagick`

---

## Understanding Metrics

### Plot Style
- **Lines**: Number of `<path>` elements (continuous lines)
- **Points**: Number of `<use>` elements (point markers)
- **Match when**: C and Java have same ratio

### Edge Pixels
- **What it means**: Number of edge transitions in the image
- **201% more**: Java using points (many edges) vs lines (few edges)
- **Match when**: Within ±10%

### Unique Colors
- **What it means**: Number of distinct colors in rendered image
- **173% more**: Point marker anti-aliasing creates color gradients
- **Match when**: Within ±20%

### Critical Issues
- Automatically extracted from comparison output
- Lines starting with ❌ in deep comparison
- Examples:
  - "Plot style mismatch"
  - "Font size mismatch"
  - "Axis positioning errors"

---

## Workflow

### Development Cycle
```bash
# 1. Make code changes
vim gnuplot-java/gnuplot-cli/src/main/java/...

# 2. Rebuild
mvn clean install -DskipTests -Djacoco.skip=true

# 3. Run tests (comparison runs automatically!)
mvn test -Dtest=DemoTestSuite

# 4. Check console for critical issues

# 5. View visual diffs
open /tmp/gnuplot_visual_comparison/overlay_*.png

# 6. Read full analysis if needed
less test-results/latest/comparison_simple.dem.txt

# 7. Fix issues and repeat
```

### Before Committing
```bash
# Ensure no critical issues
mvn test -Dtest=DemoTestSuite

# Check that metrics are improving:
# - Edge pixels getting closer
# - Unique colors converging
# - Critical issues list shrinking
```

---

## Troubleshooting

### "Comparison Tools Status: ❌ Not found"
```bash
# Navigate to repository root
cd /Users/gfilios/develop/modernization/gnuplot-master

# Verify scripts exist
ls -la compare_*.sh

# Make executable
chmod +x compare_*.sh
```

### "ImageMagick not available"
```bash
# Install ImageMagick
brew install imagemagick

# Verify installation
magick --version
```

### "No comparison files generated"
- Check that both C and Java executions succeeded
- Verify SVG outputs exist in `test-results/latest/outputs/`
- Ensure comparison scripts are executable

### "Cannot find symbol: ComparisonRunner"
```bash
# Recompile test sources
mvn test-compile
```

---

## Benefits

### Before Integration
- ❌ Easy to forget to run comparisons
- ❌ Manual steps required
- ❌ Inconsistent analysis
- ❌ Hard to track metrics over time

### After Integration
- ✅ **Automatic** - Runs every test
- ✅ **Consistent** - Same analysis every time
- ✅ **Comprehensive** - All comparison tools
- ✅ **Traceable** - Logs saved per test run
- ✅ **Visible** - Issues printed in console
- ✅ **Evidence** - Visual diffs automatically generated

---

## Next Steps

### Adding More Demos
When adding new demo tests to `DemoTestSuite`, comparison runs automatically:

```java
@Test
void testNewDemo() throws IOException {
    DemoTestRunner.DemoResult result = testRunner.runDemo("new.dem");

    TestResultRepository.DemoTestRecord record =
            repository.store("new.dem", originalScript, result.getModifiedScript(), result);

    // Comparison runs automatically here!
    if (comparisonRunner.areToolsAvailable() &&
        result.isCExecutionSuccess() && result.isJavaExecutionSuccess()) {

        ComparisonRunner.ComparisonResult compResult =
                comparisonRunner.runComparison(record.getCSvgOutput(), record.getJavaSvgOutput());

        // Issues and metrics automatically printed
    }

    assertThat(result.isCExecutionSuccess()).isTrue();
}
```

### Customizing Comparison
To modify what gets compared, edit:
- `compare_deep.sh` - Element-by-element analysis
- `compare_svg.sh` - SVG code analysis
- `compare_visual.sh` - Image comparison
- `ComparisonRunner.java` - Metric extraction logic

---

## Summary

**The comparison tools are now an integral part of the test suite.**

Every test run automatically:
1. Executes C and Java implementations
2. Runs comprehensive visual comparison
3. Identifies and reports critical issues
4. Saves detailed analysis logs
5. Generates visual diff images

**No manual steps required. No forgetting to compare. Consistent analysis every time.**
