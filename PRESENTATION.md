---
marp: true
theme: default
style: |
  section {
    font-size: 18px; /* Adjust base font size */
    overflow: hidden; /* Hides overflow */
  }
  /* Or use a class for specific sections */
  .content-section {
    font-size: 20px;
    max-height: 80vh; /* Limit height */
    overflow-y: auto; /* Scroll if needed, but might not work well for PPT */
  }
---
s
# Modernizing with Claude
## A Journey from 134,000 Lines of C to Test-Driven Java

---

# The Mission

## "Modernize Gnuplot - A 40-Year-Old C Codebase"

**Gnuplot**: The scientific plotting tool used by researchers worldwide since **1986**

| The Beast | |
|-----------|---------|
| Lines of C Code | **134,000+** |
| Source Files | **65+** |
| Terminal Drivers | **60+** |
| Years of Evolution | **40** |

*"How hard could it be?"*

---

# The Problem

## Why This Seemed Impossible

```c
// set.c - 6,665 lines of this:
if (almost_equals(c_token, "xra$nge")) {
    // 200 lines of nested if-else...
}
```

### The Ugly Truth

| Challenge | Reality |
|-----------|---------|
| Architecture | C procedural → Java OOP |
| Technical Debt | 40 years accumulated |
| Platform Code | VMS, OS/2, BeOS, DOS |
| Documentation | Sparse, outdated |

**Traditional estimate: 2-3 years with a team of 5**

---

# Enter Claude

## September 30, 2025 - Day 1

I gave Claude one instruction:

> "Analyze this C codebase and create a modernization plan."

### Claude's Response (after 2 hours):

```
After analyzing the codebase (134K lines of C
across 65+ files, complex mathematical operations,
60+ terminal drivers), I recommend a
PROGRESSIVE REWRITE rather than direct conversion.
```

**Claude didn't just analyze - it started building.**

---

# The First Day

## 39 Commits Before Midnight

```
09:00  ░░░░░░░░░░  Initial commit + strategy
10:00  ██░░░░░░░░  200+ user stories created
12:00  ████░░░░░░  Maven project + CI/CD
14:00  ██████░░░░  ANTLR4 expression parser
16:00  ████████░░  AST builder + evaluator
18:00  ██████████  Math functions library
21:00  ██████████  Phase 1 COMPLETE
```

### What Claude Built in ONE Session:
- Complete project structure
- Expression parser with ANTLR4
- 30+ mathematical functions
- Complex number support
- Full test suite

---

# The Plan

## Claude's 5-Phase Strategy

| Phase | Estimated | Focus |
|-------|-----------|-------|
| 1 | 3-4 months | Core Math Engine |
| 2 | 2-3 months | Data Processing |
| 3 | 3-4 months | Rendering Engine |
| 4 | 3-4 months | Web Frontend |
| 5 | 2-3 months | CLI Compatibility |

**Total Estimate: 12-18 months**

*But something wasn't right...*

---

# The Crisis

## Week 1: Feature Creep

We were building features... but were they the *right* features?

```
Day 2:  24 commits - More functions
Day 3:   7 commits - Slowing down...
```

### The Questions:
- How do we know if our output is correct?
- Are we building what users actually need?
- How do we validate against the original?

**We were coding blind.**

---

# The Pivot

## October 3, 2025 - The Breakthrough

Claude suggested something radical:

> "What if we use the original C gnuplot as our **oracle**?"

### The Insight

```
gnuplot-c/demo/
├── simple.dem      # 231 official demo files
├── scatter.dem     # Real-world test cases
├── controls.dem    # Already written for us!
└── ... (228 more)
```

**The test suite already existed. We just hadn't seen it.**

---

# The Oracle Pattern

## C Output = Ground Truth

```
C Gnuplot (Oracle)          Java Gnuplot (Test)
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

**If the pixels match, the code is correct.**

---

# The New Workflow

## Test-Driven Everything

```bash
# BEFORE writing any code:
mvn test -Dtest=DemoTestSuite    # Run demos

# See what fails:
# "C tick count: 7, Java: 1" → Need to fix!

# Find the C algorithm:
grep -rn "quantize_normal_tics" gnuplot-c/src/

# Port it to Java, then:
mvn test -Dtest=DemoTestSuite    # Verify fix
```

**Every feature justified by a failing test.**

---

# The Rules

## What Claude Learned NOT To Do

### ❌ NEVER Hardcode

```java
// BAD - Magic number!
private static final double AXIS_START = 54.53;

// GOOD - Calculate it!
double axisStart = calculateAxisStart(viewport, margins);
```

### ❌ NEVER Guess

```java
// BAD - Random guess!
int tickCount = (int) (range / 10);

// GOOD - Port the real algorithm!
// From gnuplot-c/src/graphics.c:quantize_normal_tics()
int tickCount = quantizeNormalTics(range, maxTics);
```

---

# The Grind

## Weeks 2-10: The Long March

```
Oct 03:  ██████████████████████████  26 commits (TDD pivot!)
Oct 04:  ███████████████████████     23 commits
Oct 05:  ██████████████████████      22 commits
Oct 06:  ██████                       6 commits
Oct 07:  ██████████                  10 commits
Oct 08:  ██████████████              14 commits
   ...
Nov 04:  ███████                      7 commits
   ...
Dec 14:  ███████                      7 commits
```

**207 commits. Each one tested. Each one validated.**

---

# The Challenges

## What We Had to Solve

| Problem | Solution |
|---------|----------|
| 3D Projection | Port `graph3d.c` ViewTransform3D |
| Tick Generation | Port `quantize_normal_tics()` |
| dgrid3d Interpolation | Implement qnorm weighted average |
| Complex Numbers | `{0,1}` notation support |
| Contour Lines | Multi-level extraction algorithm |
| Point Markers | `<use>` SVG optimization |

**Each fix: Read C → Understand → Port → Test → Verify**

---

# The Climax

## December 18, 2025 - Today

```bash
$ mvn test -pl gnuplot-cli -Dtest=DemoTestSuite

[INFO] Running com.gnuplot.cli.demo.DemoTestSuite
[INFO] Tests run: 3, Failures: 0, Errors: 0, Skipped: 0

BUILD SUCCESS
```

---

# The Results

## What We Achieved

| Metric | Value |
|--------|-------|
| Duration | **2.5 months** (not 18!) |
| Commits | **207** |
| Java Files | **213** |
| Lines of Code | **50,000+** |
| Unit Tests | **1,005** |
| Demo Tests | **3/3 passing** |

### Visual Accuracy
- simple.dem: **~98%**
- scatter.dem: **~95%**
- controls.dem: **~95%**

---

# The Proof

## Side-by-Side Comparison

```
┌─────────────────────┐  ┌─────────────────────┐
│   C Gnuplot (SVG)   │  │  Java Gnuplot (SVG) │
│                     │  │                     │
│    [sine wave]      │  │    [sine wave]      │
│         ≈           │  │         ≈           │
│    IDENTICAL        │  │    IDENTICAL        │
│                     │  │                     │
└─────────────────────┘  └─────────────────────┘
         │                        │
         └────── 95-98% ──────────┘
              pixel match
```

**The pixels don't lie.**

---

# Bonus: Performance Win

## Java Beat C (In File Size!)

| Metric | C Gnuplot | Java | Win |
|--------|-----------|------|-----|
| SVG Size | 50 KB | 25 KB | **50% smaller** |
| Path Elements | 257 | 8 + refs | **75% fewer** |

### How?

```xml
<!-- C: 257 separate paths -->
<path d="M-1,0 h2 M0,-1 v2" transform="..."/>
<path d="M-1,0 h2 M0,-1 v2" transform="..."/>
...

<!-- Java: 1 definition, 249 references -->
<defs><path id='gpPt0' d='M-1,0 h2 M0,-1 v2'/></defs>
<use xlink:href='#gpPt0' transform='translate(x,y)'/>
```

---

# The Lessons

## What I Learned About AI-Assisted Development

### 1. Let Claude Read First
Don't start coding. Let it understand the codebase.

### 2. Tests Are Everything
The demo suite saved us. Build test infrastructure early.

### 3. Reference the Source
Every algorithm links back to C source. Future-proof.

### 4. Trust But Verify
Claude is fast, but always run the tests.

---

# The Secret

## Why This Worked

```
┌─────────────────────────────────────────┐
│  Claude reads session startup guide     │
│  (Project rules, anti-patterns, C refs) │
└─────────────────┬───────────────────────┘
                  ▼
┌─────────────────────────────────────────┐
│  Run tests BEFORE coding                │
└─────────────────┬───────────────────────┘
                  ▼
┌─────────────────────────────────────────┐
│  Find C implementation                  │
│  grep -rn "feature" gnuplot-c/src/      │
└─────────────────┬───────────────────────┘
                  ▼
┌─────────────────────────────────────────┐
│  Port algorithm (with C source ref)     │
└─────────────────┬───────────────────────┘
                  ▼
┌─────────────────────────────────────────┐
│  Run tests AFTER coding                 │
└─────────────────────────────────────────┘
```

**Discipline + AI = Results**

---

# What's Next?

## The Road Ahead

| Current | Goal |
|---------|------|
| 3 demos passing | 231 demos |
| CLI only | Web UI |
| SVG output | PNG, PDF, interactive |

### Prioritized Backlog
1. `discrete.dem` - Contour levels
2. `candlesticks.dem` - Financial charts
3. `histograms.dem` - Bar chart styles
4. `surface1.dem` - 3D surfaces
5. `pm3d.dem` - Colored surfaces

**The framework is ready. Adding features is now easy.**

---

# Summary

## From Impossible to Done

| | Before | After |
|--|--------|-------|
| Estimate | 18 months | **2.5 months** |
| Team | 5 developers | **1 + Claude** |
| Approach | Feature-first | **Test-first** |
| Validation | Manual | **Automated** |
| Confidence | Low | **High** |

---

# The Takeaway

## AI Changes Everything - If You Do It Right

1. **Give AI Context** - Session guides, documentation, rules
2. **Use Real Tests** - Not invented ones, real use cases
3. **Trust the Oracle** - Original system = ground truth
4. **Never Hardcode** - Calculate everything
5. **Always Verify** - Tests before AND after

---

# Thank You

## Questions?

```bash
# Try it yourself:
cd gnuplot-java
mvn clean install -DskipTests
mvn test -pl gnuplot-cli -Dtest=DemoTestSuite

# Compare outputs:
open test-results/latest/outputs/simple_c.svg
open test-results/latest/outputs/simple_java.svg
```

### Resources
- Project: `gnuplot-master/`
- Docs: `CLAUDE_DEVELOPMENT_GUIDE.md`
- Tests: `test-results/latest/`

**207 commits. 1,005 tests. 3 demos. 2.5 months. 1 Claude.**
