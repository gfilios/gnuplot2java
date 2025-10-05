# Testing Guide

This guide explains how to test the Gnuplot modernization project at various stages.

---

## Quick Test (Automated)

Run the automated setup verification script:

```bash
./test-setup.sh
```

This will check:
- ✅ Java 21+ installed
- ✅ Maven 3.9+ installed
- ✅ Git installed and configured
- ✅ Project structure is correct
- ✅ Maven can build the project
- ✅ All tests pass

---

## Demo Test Suite (Test-Driven Development)

The project uses a test-driven approach with the official Gnuplot demo scripts as test oracle.

### Running Demo Tests

```bash
cd gnuplot-java/gnuplot-cli
mvn test -Dtest=DemoTestSuite
```

### Available Demo Tests

- `testSimpleDem()` - Basic plotting with trig functions (8 plots) - ⚠️ **3/8 perfect, 5/8 minor issues**
  * **Latest (2025-10-05)**: Point markers 100% working, only tick count differences remain
  * Plots 1, 2, 3: ✅ Perfect match (no visual differences)
  * Plots 4, 5, 6, 7, 8: ⚠️ Minor tick count differences only
- `testScatterDem()` - Scatter plots (comparison not yet run)
- `testControlsDem()` - Control flow (expected to fail until control flow implemented)

### Viewing Test Results

After running tests, open the HTML report:

```bash
open ../../test-results/latest/index.html
```

The HTML report includes:
- ✅ Side-by-side comparison of C vs Java outputs
- ✅ Per-plot visual comparison analysis
- ✅ Collapsible comparison sections for each plot
- ✅ Detailed element-by-element analysis (fonts, colors, positions, etc.)

### Comparison Tools

The test suite uses three comparison tools (located in `test-tools/comparison/`):

1. **compare_deep.sh** - Element-by-element analysis
   - Title, border, axes, legend, plot style, fonts, coordinates

2. **compare_svg.sh** - SVG code structure analysis
   - Plot style detection, color palette, file size

3. **compare_visual.sh** - Pixel-level image comparison
   - Uses ImageMagick to detect visual differences
   - Edge detection for structural analysis
   - Color analysis

### Test Results Structure

```
test-results/
├── latest -> run_2025-10-05_09-20-04/
├── run_2025-10-05_09-20-04/
│   ├── index.html              # Main HTML report
│   ├── summary.txt             # Text summary
│   ├── scripts/                # Test scripts
│   │   ├── simple.dem          # Original script
│   │   └── simple_modified.dem # Modified script
│   ├── outputs/                # SVG outputs
│   │   ├── simple_c.svg        # C output plot 1
│   │   ├── simple_c_002.svg    # C output plot 2
│   │   ├── simple_java.svg     # Java output plot 1
│   │   └── simple_java_002.svg # Java output plot 2
│   ├── logs/                   # Stdout/stderr logs
│   │   ├── simple_c.stdout
│   │   ├── simple_c.stderr
│   │   ├── simple_java.stdout
│   │   └── simple_java.stderr
│   ├── comparison_simple.dem.txt       # Plot 1 comparison
│   ├── comparison_simple.dem_plot2.txt # Plot 2 comparison
│   └── ...                     # Additional plot comparisons
```

### Understanding Comparison Reports

Each comparison file contains:

1. **Deep Element Analysis**
   - Title: text, position, font size
   - Border: coordinates, dimensions
   - Axes: tick marks, labels, positions
   - Legend: position, entries, colors
   - Plot style: LINES vs POINTS vs LINESPOINTS

2. **SVG Code Analysis**
   - Element counts (paths, markers)
   - Color palette
   - File size comparison

3. **Visual Image Analysis**
   - Pixel differences
   - Edge detection (structural differences)
   - Color palette differences

### Documentation

- **Test-Driven Plan**: [TEST_DRIVEN_PLAN.md](TEST_DRIVEN_PLAN.md)
- **Comparison Tools**: [test-tools/README.md](test-tools/README.md)
- **Implementation Backlog**: [IMPLEMENTATION_BACKLOG.md](IMPLEMENTATION_BACKLOG.md)

---

## Manual Testing Steps

### 1. Verify Prerequisites

#### Check Java Version
```bash
java -version
```
**Expected Output:**
```
openjdk version "21.0.x" 2024-xx-xx
OpenJDK Runtime Environment (build 21.0.x+xx)
OpenJDK 64-Bit Server VM (build 21.0.x+xx, mixed mode)
```

**✅ Success**: Version shows 21 or higher
**❌ Failure**: Version is lower than 21 → Install JDK 21 (see [SETUP.md](SETUP.md))

---

#### Check Maven Version
```bash
mvn -version
```
**Expected Output:**
```
Apache Maven 3.9.x (...)
Maven home: /path/to/maven
Java version: 21.0.x, vendor: ...
Default locale: en_US, platform encoding: UTF-8
```

**✅ Success**: Maven 3.9+ and Java 21 shown
**❌ Failure**: Maven not found or wrong version → Install Maven (see [SETUP.md](SETUP.md))

---

#### Check Git
```bash
git --version
```
**Expected Output:**
```
git version 2.x.x
```

Check git configuration:
```bash
git config user.name
git config user.email
```

**✅ Success**: Both commands return values
**❌ Failure**: Empty output → Configure git:
```bash
git config --global user.name "Your Name"
git config --global user.email "your.email@example.com"
```

---

### 2. Verify Project Structure

Check that all POMs exist:

```bash
ls -la pom.xml
ls -la gnuplot-*/pom.xml
```

**Expected Output:**
```
pom.xml
gnuplot-cli/pom.xml
gnuplot-core/pom.xml
gnuplot-render/pom.xml
gnuplot-server/pom.xml
```

**✅ Success**: All 5 POMs found
**❌ Failure**: Missing POMs → Project structure incomplete

---

### 3. Test Maven Build (Without Tests)

```bash
mvn clean compile -DskipTests
```

**What This Does:**
- Downloads all dependencies (first time takes 5-10 minutes)
- Compiles all Java code
- Skips running tests

**Expected Output:**
```
[INFO] Reactor Summary:
[INFO]
[INFO] Gnuplot Modernization .............................. SUCCESS
[INFO] Gnuplot Core Engine ................................ SUCCESS
[INFO] Gnuplot Rendering Engine ........................... SUCCESS
[INFO] Gnuplot Server ..................................... SUCCESS
[INFO] Gnuplot CLI ........................................ SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
```

**✅ Success**: BUILD SUCCESS at the end
**❌ Failure**: BUILD FAILURE → Check error messages

**Common Issues:**

**Issue**: "No compiler is provided in this environment"
```
Solution: Ensure JAVA_HOME is set to JDK (not JRE)
export JAVA_HOME=$(/usr/libexec/java_home -v 21)
```

**Issue**: "Failed to execute goal ... compiler"
```
Solution: Check Java version, must be 21+
```

**Issue**: "Could not resolve dependencies"
```
Solution: Check internet connection, Maven downloads from Central
```

---

### 4. Run Tests

```bash
mvn test
```

**What This Does:**
- Compiles code
- Runs all unit tests
- Generates test reports

**Expected Output:**
```
[INFO] -------------------------------------------------------
[INFO]  T E S T S
[INFO] -------------------------------------------------------
[INFO] Running com.gnuplot.core.PlaceholderTest
[INFO] Tests run: 2, Failures: 0, Errors: 0, Skipped: 0
[INFO]
[INFO] Results:
[INFO]
[INFO] Tests run: 2, Failures: 0, Errors: 0, Skipped: 0
[INFO]
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
```

**✅ Success**: Tests run: X, Failures: 0, Errors: 0
**❌ Failure**: Failures or Errors > 0 → Check test output

---

### 5. Verify Test Coverage

```bash
mvn clean test jacoco:report
```

**What This Does:**
- Runs tests with coverage instrumentation
- Generates HTML coverage report

**View Report:**
```bash
# Open in browser (macOS)
open gnuplot-core/target/site/jacoco/index.html

# Open in browser (Linux)
xdg-open gnuplot-core/target/site/jacoco/index.html

# Open in browser (Windows)
start gnuplot-core/target/site/jacoco/index.html
```

**Expected:** Coverage report shows test execution

---

### 6. Run Code Quality Checks

#### Checkstyle
```bash
mvn checkstyle:check
```

**Note:** This will initially fail because `checkstyle.xml` doesn't exist yet. This is expected in Phase 0.

#### SpotBugs
```bash
mvn spotbugs:check
```

**Expected:** Should pass with no bugs found (minimal code so far)

---

### 7. Test Full Build with All Checks

```bash
mvn clean verify
```

**What This Does:**
- Cleans previous builds
- Compiles code
- Runs unit tests
- Runs integration tests
- Runs code quality checks
- Packages artifacts

**Expected:** BUILD SUCCESS

**Time:** 2-5 minutes (depending on hardware)

---

### 8. Test IDE Integration

#### IntelliJ IDEA

1. Open IntelliJ IDEA
2. **File → Open**
3. Select the `gnuplot-master` directory
4. Click **OK**
5. Wait for Maven import (watch bottom right progress bar)
6. Check Project Structure: **File → Project Structure**
   - SDK should be Java 21
   - Language level should be 21
7. Run tests: Right-click on `PlaceholderTest` → **Run 'PlaceholderTest'**

**✅ Success**: Tests run in IDE and pass
**❌ Failure**:
- Check SDK is set to Java 21
- Check Maven auto-import is enabled
- Try: **File → Invalidate Caches / Restart**

---

#### Eclipse

1. Open Eclipse
2. **File → Import → Maven → Existing Maven Projects**
3. Browse to `gnuplot-master` directory
4. Select all projects
5. Click **Finish**
6. Wait for workspace build
7. Right-click on `PlaceholderTest` → **Run As → JUnit Test**

**✅ Success**: Tests run and pass
**❌ Failure**: Check Java Compiler level in project properties

---

#### VS Code

1. Open VS Code
2. **File → Open Folder**
3. Select `gnuplot-master` directory
4. Install extensions if prompted:
   - Language Support for Java (Red Hat)
   - Debugger for Java
   - Maven for Java
5. Wait for Java extension to load
6. Open `PlaceholderTest.java`
7. Click **Run Test** above test method

**✅ Success**: Tests run and pass

---

## Testing Individual Modules

### Test Core Module Only
```bash
mvn test -pl gnuplot-core
```

### Test Render Module Only
```bash
mvn test -pl gnuplot-render
```

### Test Server Module Only
```bash
mvn test -pl gnuplot-server
```

### Test CLI Module Only
```bash
mvn test -pl gnuplot-cli
```

---

## Advanced Testing

### Run Tests in Parallel
```bash
mvn -T 4 test
```
Uses 4 threads to run tests faster

### Run Specific Test Class
```bash
mvn test -Dtest=PlaceholderTest
```

### Run Specific Test Method
```bash
mvn test -Dtest=PlaceholderTest#testInfrastructure
```

### Skip Checkstyle (During Development)
```bash
mvn test -Dcheckstyle.skip=true
```

### Run with Debug Output
```bash
mvn test -X
```
Shows detailed debug information

### Run with Specific Profile
```bash
mvn test -P ci
```
Uses CI profile with strict checks

---

## Continuous Testing (Watch Mode)

For continuous testing during development, you can use Maven with watch:

```bash
# Install Maven wrapper if not present
mvn -N io.takari:maven:wrapper

# Run tests on file change (requires external tool)
# Option 1: Use IntelliJ IDEA's built-in "Toggle auto-test"
# Option 2: Use nodemon (requires Node.js)
npm install -g nodemon
nodemon -w src -e java -x "mvn test"
```

---

## Expected Test Results (Current State)

Since we're in Phase 0 with minimal code:

### gnuplot-core
- **Tests**: 2 (PlaceholderTest)
- **Expected**: ✅ All pass
- **Coverage**: ~100% (only placeholder code)

### gnuplot-render
- **Tests**: 0 (no tests yet)
- **Expected**: ⚠️ Build succeeds, no tests

### gnuplot-server
- **Tests**: 0 (no tests yet)
- **Expected**: ⚠️ Build succeeds, no tests

### gnuplot-cli
- **Tests**: 0 (no tests yet)
- **Expected**: ⚠️ Build succeeds, no tests

---

## Troubleshooting

### Problem: Maven downloads are very slow
**Solution:**
- Configure Maven mirror (e.g., Maven Central mirror)
- Edit `~/.m2/settings.xml`
- Add mirror closer to your location

### Problem: "Test compilation failed"
**Solution:**
- Run `mvn clean` first
- Check that test dependencies are correct in POM
- Verify Java version is 21+

### Problem: OutOfMemoryError during tests
**Solution:**
```bash
export MAVEN_OPTS="-Xmx2048m -XX:MaxPermSize=512m"
mvn test
```

### Problem: Tests pass locally but fail in CI
**Solution:**
- Check for timing-dependent tests
- Check for file system path issues
- Review CI environment configuration

---

## Performance Benchmarks

### Expected Build Times (on modern hardware)

| Command | First Run | Subsequent Runs |
|---------|-----------|----------------|
| `mvn compile` | 2-3 min | 5-10 sec |
| `mvn test` | 2-3 min | 10-15 sec |
| `mvn verify` | 3-5 min | 15-30 sec |
| `mvn clean install` | 3-5 min | 20-40 sec |

**Note:** First run is slow due to dependency downloads

---

## Next Steps After Testing

If all tests pass:

1. ✅ **Create a feature branch**
   ```bash
   git checkout -b feature/your-feature
   ```

2. ✅ **Pick a story from backlog**
   - See [IMPLEMENTATION_BACKLOG.md](IMPLEMENTATION_BACKLOG.md)
   - Start with Phase 1 stories

3. ✅ **Write tests first (TDD)**
   - Create test class
   - Write failing tests
   - Implement feature
   - Tests pass ✅

4. ✅ **Commit and push**
   ```bash
   git add .
   git commit -m "feat(core): implement feature X"
   git push origin feature/your-feature
   ```

5. ✅ **Create Pull Request**

---

## CI/CD Testing (Future)

Once GitHub Actions is set up (Story 0.1.3):

```yaml
# .github/workflows/build.yml
on: [push, pull_request]
jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - uses: actions/setup-java@v3
        with:
          java-version: 21
      - run: mvn verify
```

---

## Contact

If you encounter issues not covered here:
- Check [SETUP.md](SETUP.md) for installation help
- Create an issue on GitHub
- Check discussions for similar problems

---

**Last Updated**: 2025-09-30