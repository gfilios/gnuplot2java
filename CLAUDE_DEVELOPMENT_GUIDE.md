# Claude Development Guide for Gnuplot Modernization

**CRITICAL**: This guide must be read and followed in EVERY Claude session working on this project.

---

## 🎯 Primary Working Principle

**TEST-DRIVEN DEVELOPMENT IS MANDATORY**

Every development session MUST:
1. Start by running the test-tools comparison suite
2. Identify differences between C and Java implementations
3. Fix issues based on test results
4. Re-run tests to verify fixes
5. Never proceed to new features without validating existing ones

---

## 🔧 Test-Tools Approach

### Always Use Test-Tools for Validation

**Before starting ANY coding work:**

```bash
# Run the full test suite
cd gnuplot-java
mvn clean test

# Run demo comparison tests
cd ../test-tools
./run_demo_tests.sh simple

# Check latest test results
cat test-results/latest/summary.txt
cat test-results/latest/comparison_*.txt
```

### Test-Tools Location and Scripts

```
test-tools/
├── comparison/
│   ├── compare_all.sh      # Run all comparison types
│   ├── compare_deep.sh     # Deep element-by-element comparison
│   ├── compare_svg.sh      # SVG structure comparison
│   └── compare_visual.sh   # Visual/pixel comparison
├── run_demo_tests.sh       # Main test runner
└── docs/
    └── README.md           # Test tools documentation
```

**Key comparison tools detect:**
- ✅ Title positions and fonts
- ✅ Border/frame presence
- ✅ Axis tick counts and positions
- ✅ Legend positions and entries
- ✅ Line rendering modes
- ✅ Color palettes
- ✅ Plot styles
- ✅ Visual pixel differences

---

## 🚫 Anti-Patterns: What NOT to Do

### ❌ NEVER Hardcode Values

**BAD** (Hardcoded):
```java
// DON'T DO THIS!
private static final double AXIS_START = 54.53;
private static final double AXIS_END = 774.82;
private static final int TICK_COUNT = 7;
```

**GOOD** (Algorithm-driven):
```java
// DO THIS - Calculate dynamically
double axisStart = calculateAxisStart(viewport, margins);
double axisEnd = calculateAxisEnd(viewport, margins);
int tickCount = calculateOptimalTicks(dataRange, viewport);
```

### ❌ NEVER Guess or Approximate Algorithms

**BAD** (Guessing):
```java
// DON'T DO THIS!
int tickCount = (int) (range / 10);  // Random guess
```

**GOOD** (Study C implementation):
```java
// DO THIS - Port the C algorithm
// From gnuplot-c/src/graphics.c:quantize_normal_tics()
int tickCount = quantizeNormalTics(range, maxTics);
```

---

## 📚 Algorithm Implementation Process

### Step 1: Locate C Implementation

**Always start by finding the C code:**

```bash
# Search for the feature in C codebase
cd gnuplot-c
grep -r "function_name" src/
grep -r "set key" src/
grep -r "border" src/
```

### Step 2: Understand the C Algorithm

**Read and analyze the C code:**
- Understand the logic flow
- Identify key calculations
- Note edge cases and special handling
- Document any magic numbers (and find their source)

### Step 3: Port to Java (Not Copy!)

**Convert C idioms to Java best practices:**

**C Code Example** (gnuplot-c/src/graphics.c):
```c
void quantize_normal_tics(double *tic, int guide) {
    int power = (int) floor(log10(*tic));
    double xnorm = (*tic) / pow(10.0, power);

    if (xnorm <= 2)
        xnorm = 2;
    else if (xnorm <= 5)
        xnorm = 5;
    else
        xnorm = 10;

    *tic = xnorm * pow(10.0, power);
}
```

**Java Port**:
```java
/**
 * Quantizes tick spacing to nice values (2, 5, or 10 multiples).
 * Ported from gnuplot-c/src/graphics.c:quantize_normal_tics()
 *
 * @param tic the proposed tick spacing
 * @return the quantized tick spacing
 */
public double quantizeNormalTics(double tic) {
    int power = (int) Math.floor(Math.log10(tic));
    double xnorm = tic / Math.pow(10.0, power);

    if (xnorm <= 2.0) {
        xnorm = 2.0;
    } else if (xnorm <= 5.0) {
        xnorm = 5.0;
    } else {
        xnorm = 10.0;
    }

    return xnorm * Math.pow(10.0, power);
}
```

**Key principles:**
- ✅ Preserve the algorithm logic exactly
- ✅ Use Java naming conventions (camelCase)
- ✅ Add JavaDoc with C source reference
- ✅ Use Java idioms (Math.log10 vs log10, etc.)
- ✅ Keep the same edge case handling

---

## 🧪 Test-First Development Cycle

### Mandatory Testing Cycle

**EVERY fix must follow this cycle:**

```bash
# 1. Run tests BEFORE making changes
./test-tools/run_demo_tests.sh simple
cat test-results/latest/comparison_simple.dem.txt

# 2. Identify specific issues
#    Example: "Y-AXIS TICKS: C=7, Java=1"
#    Example: "Border Path: Java='NOT FOUND'"

# 3. Locate C implementation
cd gnuplot-c
grep -r "border" src/graphics.c

# 4. Implement Java port
cd ../gnuplot-java
# Write code...

# 5. Run tests AFTER changes
mvn clean test
cd ../test-tools
./run_demo_tests.sh simple

# 6. Verify fix
cat test-results/latest/comparison_simple.dem.txt
# Confirm: "Y-AXIS TICKS: C=7, Java=7" ✓
```

**Never skip step 1 or step 5!**

---

## 📝 Documentation Requirements

### Code Must Reference C Source

**Always document which C code you ported:**

```java
/**
 * Renders the plot border/frame.
 *
 * <p>This implementation follows the C gnuplot border rendering logic
 * from gnuplot-c/src/graphics.c:plot_border(), which draws a rectangle
 * around the plot area using the border coordinates.
 *
 * <p>The border is drawn as a closed path (M...Z) with stroke color
 * matching the configured border style.
 *
 * @param renderer the SVG renderer
 * @param viewport the plot viewport with border coordinates
 * @see <a href="file:///gnuplot-c/src/graphics.c#L1234">graphics.c:plot_border()</a>
 */
public void renderBorder(SvgRenderer renderer, Viewport viewport) {
    // Implementation...
}
```

### Session Notes

**At the end of each session, update session notes:**

```bash
# Create/update session log
echo "Session $(date +%Y-%m-%d): Fixed border rendering" >> SESSION_NOTES.md
echo "- Issue: Java missing plot border" >> SESSION_NOTES.md
echo "- C Source: gnuplot-c/src/graphics.c:plot_border()" >> SESSION_NOTES.md
echo "- Fix: Added border path rendering to SvgRenderer" >> SESSION_NOTES.md
echo "- Tests: Y-axis ticks 7→7 ✓, Border present ✓" >> SESSION_NOTES.md
```

---

## 🔍 Common Issues and C Code References

### Border/Frame Rendering
- **C Source**: `gnuplot-c/src/graphics.c:plot_border()`
- **Java Target**: `SvgRenderer.renderBorder()`

### Axis Tick Generation
- **C Source**: `gnuplot-c/src/graphics.c:quantize_normal_tics()`
- **Java Target**: `TickGenerator.quantizeNormalTics()`

### Legend Positioning
- **C Source**: `gnuplot-c/src/graphics.c:do_key_sample()`
- **Java Target**: `LegendRenderer.calculatePosition()`

### Title Positioning
- **C Source**: `gnuplot-c/src/graphics.c:do_plot_title()`
- **Java Target**: `TitleRenderer.renderTitle()`

### Axis Label Positioning
- **C Source**: `gnuplot-c/src/graphics.c:axis_output_tics()`
- **Java Target**: `AxisRenderer.renderTicks()`

---

## 🎓 Learning from Test Results

### Reading Test Output

**Test comparison files contain:**

```
=== DEEP ELEMENT-BY-ELEMENT COMPARISON ===
...
3. AXIS POSITIONING & TICK MARKS
Y-AXIS TICKS:
  C tick count:    7
  Java tick count: 1
  ❌ Mismatch detected
```

**What this tells you:**
1. **What's wrong**: Java only generates 1 tick instead of 7
2. **Where to look**: Axis tick generation algorithm
3. **What to search**: `grep -r "tic" gnuplot-c/src/graphics.c`
4. **What to fix**: Port the C tick calculation algorithm

### Test-Driven Debugging

**Use tests to guide debugging:**

```bash
# 1. See what's different
diff test-results/latest/outputs/simple_c.svg \
     test-results/latest/outputs/simple_java.svg

# 2. Extract specific elements
grep "tick" test-results/latest/outputs/simple_c.svg
grep "tick" test-results/latest/outputs/simple_java.svg

# 3. Count occurrences
grep -c "M54.53" test-results/latest/outputs/simple_c.svg
grep -c "M54" test-results/latest/outputs/simple_java.svg
```

---

## 🚀 Session Startup Checklist

**At the start of EVERY Claude session:**

- [ ] Read this file (`CLAUDE_DEVELOPMENT_GUIDE.md`)
- [ ] Check latest test results: `cat test-results/latest/summary.txt`
- [ ] Review comparison files: `ls test-results/latest/comparison_*.txt`
- [ ] Identify top priority issues from test results
- [ ] Locate C source code for the issue
- [ ] Create TodoWrite list for the session
- [ ] Start fixing with test-first approach

**Example session start:**
```bash
# 1. Read this guide
cat CLAUDE_DEVELOPMENT_GUIDE.md

# 2. Check test results
cat test-results/latest/summary.txt
cat test-results/latest/comparison_simple.dem.txt | grep "❌\|⚠️"

# 3. Identify issues
# Output: "❌ Border path: Java='NOT FOUND'"
# Output: "⚠️ Y-axis tick count differs: 7 vs 1"

# 4. Search C code
cd gnuplot-c
grep -rn "border" src/graphics.c | head -20

# 5. Begin fixing
cd ../gnuplot-java
# Start coding...
```

---

## 📊 Success Metrics

### How to Know You're on Track

**Good indicators:**
- ✅ Test comparison shows decreasing differences
- ✅ C and Java tick counts match
- ✅ Border/frame rendering matches
- ✅ Visual pixel differences decreasing
- ✅ Code has C source references in JavaDoc
- ✅ No hardcoded magic numbers

**Bad indicators:**
- ❌ Adding features without running tests
- ❌ Hardcoding values to "make tests pass"
- ❌ Guessing at algorithms instead of reading C code
- ❌ Skipping test-tools validation
- ❌ Not documenting C source references

---

## 🔄 Iterative Improvement

### Each Issue Should Follow This Pattern

```
1. Test reveals issue: "Border missing"
   ↓
2. Find C implementation: graphics.c:plot_border()
   ↓
3. Understand algorithm: Draws M...Z path with border coords
   ↓
4. Port to Java: SvgRenderer.renderBorder()
   ↓
5. Re-test: Border now present ✓
   ↓
6. Document: Add JavaDoc with C reference
   ↓
7. Commit: "fix(render): add plot border rendering per graphics.c:plot_border()"
```

**Repeat for every issue identified by tests.**

---

## 🎯 Key Takeaways

1. **TEST-FIRST**: Always run test-tools before and after changes
2. **NO HARDCODING**: Calculate values using algorithms from C code
3. **READ C CODE**: Don't guess - find the actual C implementation
4. **PORT, DON'T COPY**: Use Java idioms while preserving C logic
5. **DOCUMENT SOURCES**: Always reference which C code you ported
6. **VALIDATE EVERYTHING**: Use test-tools to verify every fix

---

## 📖 Additional Resources

- [CONTRIBUTING.md](CONTRIBUTING.md) - Contribution guidelines
- [test-tools/README.md](test-tools/README.md) - Test tools documentation
- [gnuplot-c/src/graphics.c](gnuplot-c/src/graphics.c) - Main C rendering code
- [gnuplot-c/src/term.c](gnuplot-c/src/term.c) - Terminal drivers
- [Test Results](test-results/latest/) - Latest test comparison outputs

---

**Last Updated**: 2025-10-05
**Version**: 1.0
**Status**: MANDATORY for all Claude sessions
