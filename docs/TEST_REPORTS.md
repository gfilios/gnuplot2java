# Test Reports

## Overview

The test infrastructure generates HTML reports comparing C Gnuplot and Java Gnuplot demo execution.

## Location

Test results are stored locally in `test-results/` (gitignored):

```
test-results/
├── latest -> run_2025-10-03_21-14-09
└── run_2025-10-03_21-14-09/
    ├── index.html         # Main HTML report
    ├── summary.txt        # Text summary
    ├── scripts/           # Demo scripts
    ├── outputs/           # SVG outputs
    └── logs/              # Execution logs
```

## Running Tests

```bash
cd gnuplot-java/gnuplot-cli
mvn test -Dtest=DemoTestSuite
```

Output will show:
```
Gnuplot Demo Test Results
=========================
Test Run: run_2025-10-03_21-14-09
Total Tests: 3
Passing: 0 (0.0%)
C Gnuplot Success: 3/3
Java Gnuplot Success: 3/3

📊 HTML Report: /path/to/test-results/latest/index.html
   Open with: open /path/to/test-results/latest/index.html
```

## Report Features

### Summary Dashboard
- Total tests run
- Pass/fail rate (%)
- C Gnuplot success rate
- Java Gnuplot success rate

### Individual Test Results
- ✅/❌ Status indicator
- Collapsible details
- Original demo script
- Side-by-side SVG comparison (C vs Java)
- Error/warning logs with syntax highlighting

### Example Screenshot

*Note: Actual reports are generated locally in `test-results/` directory*

```
┌─────────────────────────────────────────────┐
│  🧪 Gnuplot Demo Test Results               │
│  Test Run: run_2025-10-03_21-14-09          │
└─────────────────────────────────────────────┘

📊 Summary
┌────────────┬────────────┬────────────┬────────────┐
│ Total      │ Passing    │ C Success  │ Java       │
│ Tests      │            │            │ Success    │
├────────────┼────────────┼────────────┼────────────┤
│     3      │   0 (0%)   │    3/3     │    3/3     │
└────────────┴────────────┴────────────┴────────────┘

Test Results
────────────────────────────────────────────────────

▶ simple.dem                              ❌ FAIL
  C: ✓ | Java: ✓

▶ scatter.dem                             ❌ FAIL
  C: ✓ | Java: ✓

▶ controls.dem                            ❌ FAIL
  C: ✓ | Java: ✓
```

## CI/CD Integration

Test results are **NOT committed** to the repository.

For CI/CD:
1. **GitHub Actions**: Upload reports as artifacts
   ```yaml
   - uses: actions/upload-artifact@v3
     with:
       name: test-reports
       path: test-results/latest/
   ```

2. **Keep last N runs**: Implement cleanup script
   ```bash
   # Keep only last 10 test runs
   cd test-results
   ls -t | tail -n +11 | xargs rm -rf
   ```

## Historical Tracking

**Recommended approach:**
- Store summary metrics in a database or file
- Track pass rate over time
- Generate charts from metrics

**Example metrics file** (commit this):
```json
{
  "runs": [
    {
      "timestamp": "2025-10-03T21:14:09",
      "total": 3,
      "passing": 0,
      "c_success": 3,
      "java_success": 3
    }
  ]
}
```

## Local Development

Each developer gets their own `test-results/` directory locally.
Reports are regenerated on every test run.

To share results:
- Take screenshots of HTML report
- Copy specific SVG comparisons to `docs/examples/`
- Share the summary.txt via PR comments
