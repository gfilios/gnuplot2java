# Gnuplot Demo Test Infrastructure

This directory contains the test-driven development (TDD) infrastructure for validating Gnuplot compatibility using the official demo suite.

## Overview

The test infrastructure executes Gnuplot demo scripts (`.dem` files) in both C Gnuplot and Java Gnuplot, captures outputs, and generates detailed comparison reports.

## Components

### 1. [DemoTestRunner.java](DemoTestRunner.java)
- Executes demo scripts in both implementations
- Modifies scripts to set SVG terminal and remove interactive pauses
- Captures stdout, stderr, and SVG outputs
- Provides `DemoResult` objects with execution details

### 2. [TestResultRepository.java](TestResultRepository.java)
- Persists test results across test runs
- Organizes results in timestamped directories
- Stores scripts, outputs, logs, and metadata
- Creates `latest` symlink for easy access

### 3. [HtmlReportGenerator.java](HtmlReportGenerator.java)
- Generates beautiful HTML reports with side-by-side comparisons
- Shows original scripts, SVG outputs, and error logs
- Collapsible sections for each demo test
- Color-coded status indicators

### 4. [DemoTestSuite.java](DemoTestSuite.java)
- JUnit 5 test suite with demo tests
- Currently tests: simple.dem, scatter.dem, controls.dem
- Automatically generates reports after test run

### 5. [SvgComparator.java](SvgComparator.java) - ✅ NEW
- Structural SVG comparison (element counts, dimensions)
- Text content comparison
- Similarity metrics with configurable thresholds
- SVG statistics for reporting

### 6. [PixelComparator.java](PixelComparator.java) - ✅ NEW
- Pixel-based comparison using Apache Batik
- Rasterizes SVGs to BufferedImages
- Generates visual diff images (red pixels for differences)
- Color distance calculation with tolerance
- Similarity percentage calculation

## Usage

### Run All Demo Tests

```bash
cd gnuplot-java/gnuplot-cli
mvn test -Dtest=DemoTestSuite
```

### Run Single Demo Test

```bash
mvn test -Dtest=DemoTestSuite#testSimpleDem
```

### View Results

After running tests, results are stored in:
```
gnuplot-master/test-results/
├── latest -> run_2025-10-03_21-14-09  (symlink to latest run)
└── run_2025-10-03_21-14-09/
    ├── index.html              # HTML report (open this!)
    ├── summary.txt             # Text summary
    ├── scripts/                # Original and modified scripts
    │   ├── simple.dem
    │   ├── simple_modified.dem
    │   ├── scatter.dem
    │   └── ...
    ├── outputs/                # SVG outputs
    │   ├── simple_c.svg
    │   ├── simple_java.svg
    │   └── ...
    └── logs/                   # Execution logs
        ├── simple_c.stdout
        ├── simple_c.stderr
        ├── simple_java.stdout
        ├── simple_java.stderr
        └── ...
```

### Open HTML Report

```bash
open test-results/latest/index.html
```

Or use the path printed at the end of the test run.

## Report Features

The HTML report includes:

- **Summary Dashboard**: Test counts, pass rate, C vs Java success rates
- **Individual Test Results**: Expandable sections for each demo
- **Script Display**: Original demo script with syntax highlighting
- **Side-by-Side Comparison**: C Gnuplot output vs Java Gnuplot output
- **SVG Previews**: Inline rendering of generated plots
- **Error Analysis**: Color-coded error and warning messages
- **File Sizes**: Output file size comparison

## Directory Structure

```
gnuplot-master/
├── test-results/               # Persistent test results
│   ├── latest -> run_...       # Symlink to latest run
│   ├── run_2025-10-03_14-30/   # Timestamped test run
│   ├── run_2025-10-03_15-45/
│   └── run_2025-10-03_21-14/
│
└── gnuplot-java/gnuplot-cli/src/test/java/com/gnuplot/cli/demo/
    ├── DemoTestRunner.java         # Test execution engine
    ├── TestResultRepository.java   # Result persistence
    ├── HtmlReportGenerator.java    # HTML report generator
    ├── DemoTestSuite.java          # JUnit test suite
    └── README.md                   # This file
```

## Test Results Format

Each test produces a `DemoTestRecord` with:
- Demo name (e.g., "simple.dem")
- Timestamp
- C Gnuplot success status
- Java Gnuplot success status
- Paths to original and modified scripts
- Paths to C and Java SVG outputs
- Paths to stdout and stderr logs
- Overall pass/fail status

## Current Test Status

As of the initial implementation:

- **Total Demos Available**: 231 (in gnuplot-c/demo/)
- **Currently Tested**: 3 (simple.dem, scatter.dem, controls.dem)
- **Pass Rate**: 0% (Java doesn't respect `set output` yet)
- **C Gnuplot Success**: 100% (3/3)
- **Java Gnuplot Success**: 100% execution (3/3), but 0% output matching

## Next Steps

1. **Implement `set output` command** in Java CLI executor
2. **Add more demo tests** to cover all 231 demos
3. **Implement visual comparison** (SVG diff, pixel comparison)
4. **Add gap analysis** to identify missing features
5. **Track progress over time** with historical charts

## Example Output

```
Gnuplot Demo Test Results
=========================
Test Run: run_2025-10-03_21-14-09
Total Tests: 3
Passing: 0 (0.0%)
C Gnuplot Success: 3/3
Java Gnuplot Success: 3/3

Individual Results:
-------------------
simple.dem      ❌ FAIL  (C:✓ Java:✓)
scatter.dem     ❌ FAIL  (C:✓ Java:✓)
controls.dem    ❌ FAIL  (C:✓ Java:✓)

Full results saved to: test-results/run_2025-10-03_21-14-09
Summary: test-results/run_2025-10-03_21-14-09/summary.txt

📊 HTML Report: test-results/run_2025-10-03_21-14-09/index.html
   Open with: open test-results/run_2025-10-03_21-14-09/index.html
```

## Contributing

To add a new demo test:

1. Add a test method in `DemoTestSuite.java`:
```java
@Test
void testMyDem() throws IOException {
    Path myDem = DEMO_DIR.resolve("my.dem");
    if (!Files.exists(myDem)) {
        return; // Skip if not available
    }

    DemoTestRunner.DemoResult result = testRunner.runDemo("my.dem");
    repository.store("my.dem", myDem, result.getModifiedScript(), result);

    System.out.println("=== my.dem Test Results ===");
    System.out.println(result.isPassing() ? "✅ PASS" : "❌ FAIL");

    assertThat(result.isCExecutionSuccess()).isTrue();
}
```

2. Run the test suite
3. View the HTML report to see results

## Benefits

✅ **Automated Validation**: Compare C and Java outputs automatically
✅ **Historical Tracking**: Keep all test runs for progress tracking
✅ **Visual Inspection**: See plots side-by-side in HTML report
✅ **Gap Analysis**: Identify missing features from error logs
✅ **Reproducible**: All test data persisted for debugging
✅ **CI/CD Ready**: Maven-based, can run in CI pipelines
