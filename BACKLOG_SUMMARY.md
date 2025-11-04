# Gnuplot Modernization - Backlog Summary

**Project**: Gnuplot Java Modernization
**Approach**: Test-Driven Development using Official Gnuplot Demo Suite
**Timeline**: 12-18 months to MVP
**Last Updated**: 2025-11-04

**🎯 NEW APPROACH**: Shifted to test-driven development using `gnuplot-c/demo/*.dem` scripts as test oracle. See [TEST_DRIVEN_PLAN.md](TEST_DRIVEN_PLAN.md) for details.

---

## Quick Navigation

This backlog has been split into manageable files for easier reading:

- **[BACKLOG_SUMMARY.md](BACKLOG_SUMMARY.md)** (this file) - Progress overview, priorities, summary statistics
- **[BACKLOG_PHASE_0_SETUP.md](BACKLOG_PHASE_0_SETUP.md)** - Phase 0: Project Setup (COMPLETE)
- **[BACKLOG_PHASE_1_MATH.md](BACKLOG_PHASE_1_MATH.md)** - Phase 1: Core Mathematical Engine (COMPLETE)
- **[BACKLOG_PHASE_2_DATA.md](BACKLOG_PHASE_2_DATA.md)** - Phase 2: Data Processing Layer (COMPLETE)
- **[BACKLOG_PHASE_3_RENDER.md](BACKLOG_PHASE_3_RENDER.md)** - Phase 3: Rendering Engine (IN PROGRESS)
- **[BACKLOG_PHASE_4_BACKEND.md](BACKLOG_PHASE_4_BACKEND.md)** - Phase 4: Backend Services
- **[BACKLOG_PHASE_5_FRONTEND.md](BACKLOG_PHASE_5_FRONTEND.md)** - Phase 5: Web Frontend
- **[BACKLOG_PHASE_6_INTEGRATION.md](BACKLOG_PHASE_6_INTEGRATION.md)** - Phase 6: Integration & Testing
- **[BACKLOG_PHASE_7_CLI.md](BACKLOG_PHASE_7_CLI.md)** - Phase 7: CLI & Script Compatibility (COMPLETE)

> **Note**: The original [IMPLEMENTATION_BACKLOG.md](IMPLEMENTATION_BACKLOG.md) (3,346 lines, 39K tokens) has been preserved for reference but is too large to read in one go. Use the split files above instead.

---

## 📊 Overall Progress Summary

### Completed Phases ✅

| Phase | Status | Story Points | Completion |
|-------|--------|--------------|------------|
| **Phase 0: Setup** | 🟢 COMPLETE | 61/61 | 100% |
| **Phase 1: Core Math** | 🟢 COMPLETE (MVP) | 197/300 | 66% (MVP ready) |
| **Phase 2: Data Processing** | 🟢 COMPLETE (MVP) | 173/200 | 100% P0 |
| **Phase 7: CLI** | 🟢 COMPLETE | 80/80 | 100% |

### Active Phase 🟡

| Phase | Status | Story Points | Completion |
|-------|--------|--------------|------------|
| **Phase 3: Rendering** | 🟡 IN PROGRESS | ~50/300 | ~17% (3/3 demos) |

### Future Phases ⚪

| Phase | Status | Story Points | Weeks |
|-------|--------|--------------|-------|
| **Phase 4: Backend** | ⚪ PLANNED | 120 | 8-10 |
| **Phase 5: Frontend** | ⚪ PLANNED | 250 | 16 |
| **Phase 6: Integration** | ⚪ PLANNED | 115 | 8 |

---

## 🎯 Current Sprint Priorities

### Active Stories (Week of 2025-11-04)

**Priority 1: Polish Current Demos (Quick Wins)**

1. **3D Y-Axis Positioning Fix** ⚡ READY TO IMPLEMENT
   - Change viewport scaling from 1/2 to 4/7 (matches C gnuplot)
   - File: [SvgRenderer.java:1637-1643](gnuplot-java/gnuplot-render/src/main/java/com/gnuplot/render/svg/SvgRenderer.java#L1637-L1643)
   - Effort: 30 minutes
   - Impact: Fixes vertical positioning in scatter.dem

2. **Y/Z Axis Tick Labels**
   - Port from C `graphics.c:axis_output_tics()`
   - Add to `render3DAxes()` in SvgRenderer
   - Effort: 1-2 hours
   - Impact: Completes scatter.dem 3D axes

3. **Impulse Lines Rendering**
   - Implement "with impulses" style
   - See [BACKLOG_IMPULSES_POINTS.md](BACKLOG_IMPULSES_POINTS.md)
   - Effort: 2-3 hours
   - Impact: Completes simple.dem plot 4

**Priority 2: Next Demo (TDD Approach)**

4. **arrows.dem** - Arrow rendering
5. **boxes.dem** - Box plots
6. **surface1.dem** - 3D surface rendering

---

## 📈 Key Metrics

### Test Results (Current)

```
Total Tests: 989 passing
├── Parser: 69 tests
├── Evaluator: 74 tests
├── Complex Numbers: 31 tests
├── Math Functions: 135 tests
├── Error Handling: 18 tests
├── Data Processing: 238 tests
├── CLI: 31 tests
├── Rendering: 354 tests
└── Oracle Validation: 39 tests

Demo Suite: 3/3 passing (100%)
├── simple.dem: ✅ 8 plots (~95% visual accuracy)
├── scatter.dem: ✅ 8 plots (~90% visual accuracy)
└── controls.dem: ✅ (not analyzed)

Available Demos: 231 total
Implemented: 3 (1.3%)
```

### Code Quality

- **Test Coverage**: >95% maintained
- **Build Status**: ✅ Passing
- **Code Style**: Enforced with Checkstyle
- **Static Analysis**: SpotBugs active

### Performance

- **SVG File Size**: 50% smaller than C gnuplot (25KB vs 50KB)
- **Rendering Speed**: ~3-4 sec per demo (vs C: 2-3 sec)
- **Memory**: Efficient with `<use>` references (75% reduction)

---

## 🎓 Implementation Philosophy

### Test-Driven Development (TDD)

Every feature follows this cycle:

1. **Run Tests** - Execute demo comparison before changes
2. **Identify Gaps** - Find differences between C and Java output
3. **Locate C Code** - Find algorithm in `gnuplot-c/src/`
4. **Port to Java** - Implement using Java idioms
5. **Verify** - Re-run tests to confirm fix
6. **Document** - Add JavaDoc with C source references

### Never Hardcode, Always Calculate

- ❌ **Bad**: `private static final double AXIS_START = 54.53;`
- ✅ **Good**: `double axisStart = calculateAxisStart(viewport, margins);`

### Progressive Rewrite, Not Conversion

We're building a **modern Java implementation**, not translating C code line-by-line:

- Use Java design patterns (scene graph, visitor pattern)
- Leverage Java ecosystem (ANTLR4, Apache Commons Math)
- Modern architecture (modules, clean interfaces)
- But preserve C gnuplot's **algorithms** and **rendering behavior**

---

## 📦 Delivered Features (Phase 0-2, 7)

### Phase 0: Infrastructure ✅

- Multi-module Maven project (core, render, CLI, server, web)
- CI/CD pipeline with GitHub Actions
- Code quality tools (Checkstyle, SpotBugs, JaCoCo)
- Test oracle with 89 reference test cases
- Documentation framework

### Phase 1: Math Engine ✅ (MVP)

- **Expression Parser** (ANTLR4, 14 precedence levels, 69 tests)
- **Evaluator** (arithmetic, variables, functions, 74 tests)
- **Complex Numbers** (foundation, 31 tests)
- **Math Functions** (38+ functions, 135 tests):
  - Trigonometric (sin, cos, tan, asin, acos, atan, atan2)
  - Hyperbolic (sinh, cosh, tanh, asinh, acosh, atanh)
  - Exponential (exp, log, log10, sqrt, cbrt, pow)
  - Special (gamma, lgamma, beta, lambertw)
  - Bessel (besj0, besj1, besy0, besy1, jn, yn)
  - Error functions (erf, erfc, inverf)
  - Statistical (norm, invnorm)
  - Random (rand, random_normal, random_gamma)
- **Error Handling** (context-aware, source location, 18 tests)

**Validation**: All functions ≤1e-10 precision vs C gnuplot 6.0.3

### Phase 2: Data Processing ✅ (MVP)

- **Data Readers** (CSV, JSON, 52 tests)
- **Data Transformation** (filter, interpolation, 86 tests)
  - Linear interpolation
  - Cubic spline interpolation
- **Statistical Analysis** (descriptive stats, 21 tests)
- **Coordinate Systems** (Cartesian, polar, spherical, cylindrical, 79 tests)

### Phase 7: CLI ✅

- **Command Parser** (ANTLR4 grammar, full gnuplot syntax)
- **Script Execution** (5 modes: file, inline, interactive, stdin, demo)
- **Expression Integration** (evaluator, function calls)
- **SVG Output** (primary format)
- **Tests**: 31 CLI tests passing

---

## 🚧 In Progress (Phase 3: Rendering)

### Currently Working

**3D Rendering** (scatter.dem focus):
- ✅ Point cloud rendering (249 points)
- ✅ Wireframe rendering (LINES style)
- ✅ 3D coordinate axes with tick marks
- ✅ ViewTransform3D projection (60°, 30°)
- ✅ dgrid3d qnorm interpolation
- ✅ Legend positioning (top-right)
- 🟡 Y/Z axis tick labels (in progress)
- 🟡 Vertical positioning adjustment (4/7 scaling)
- ❌ Impulse guide lines (not started)

**2D Rendering** (simple.dem focus):
- ✅ Function evaluation plots
- ✅ Point markers (8 types, optimized)
- ✅ Line plots (continuous)
- ✅ Legend rendering (92% accurate)
- ✅ Plot border/frame
- ✅ 2D axes with ticks
- ✅ Grid lines
- ✅ Titles and labels
- 🟡 Impulse lines (in progress)
- ❌ Boxes, steps, histograms

---

## 📅 Roadmap (Next 6 Months)

### Q1 2026: Expand Demo Coverage

**Goal**: 10/231 demos passing (~4% coverage)

**Target Demos**:
1. ✅ simple.dem (DONE)
2. ✅ scatter.dem (DONE)
3. ✅ controls.dem (DONE)
4. arrows.dem - Arrow rendering
5. boxes.dem - Box plots
6. surface1.dem - 3D surfaces
7. fillstyle.dem - Fill patterns
8. histograms.dem - Histogram plots
9. polar.dem - Polar coordinates
10. parametric.dem - Parametric curves

**Estimated Effort**: 60-80 SP (8-10 weeks)

### Q2 2026: Advanced 3D Features

**Goal**: 3D surface rendering, pm3d coloring

**Features**:
- Surface plots (splot with pm3d)
- Contour lines
- Color palettes
- Hidden line removal
- Lighting/shading

**Estimated Effort**: 100-120 SP (12-16 weeks)

### Q3 2026: Backend API (Optional)

**Goal**: REST API for plot generation

**Features**:
- Spring Boot application
- REST endpoints (POST /plot, GET /status)
- Async job processing
- File storage (S3/local)
- API documentation (OpenAPI)

**Estimated Effort**: 120 SP (8-10 weeks)

---

## 📊 Story Points Summary

### Total Story Points by Phase

| Phase | Story Points | Status | Completion |
|-------|--------------|--------|------------|
| **Phase 0** (Setup) | 100 SP | ✅ COMPLETE | 100% (61 SP) |
| **Phase 1** (Core Math) | 300 SP | ✅ MVP READY | 66% (197 SP) |
| **Phase 2** (Data) | 200 SP | ✅ MVP READY | 87% (173 SP) |
| **Phase 3** (Rendering) | 300 SP | 🟡 IN PROGRESS | ~17% (~50 SP) |
| **Phase 4** (Backend) | 120 SP | ⚪ PLANNED | 0% |
| **Phase 5** (Frontend) | 250 SP | ⚪ PLANNED | 0% |
| **Phase 6** (Integration) | 115 SP | ⚪ PLANNED | 0% |
| **Phase 7** (CLI) | 80 SP | ✅ COMPLETE | 100% (80 SP) |

**Total for MVP (Phases 0-6)**: ~1,385 SP
**Completed**: ~561 SP (40.5%)
**Remaining**: ~824 SP (59.5%)

### Timeline Estimate

**Assumptions**:
- Team velocity: 25-30 SP/week
- Current pace: ~20 SP/week (solo developer)

**Projected Timeline**:
- **Phase 3 Rendering**: 10-12 weeks (250 SP remaining)
- **Phase 4 Backend**: 5-6 weeks (120 SP) [optional, parallel]
- **Phase 5 Frontend**: 10-12 weeks (250 SP) [optional]
- **Phase 6 Integration**: 4-6 weeks (115 SP)

**Total Remaining**: 24-36 weeks (6-9 months) to MVP

---

## 🔗 Related Documentation

### Essential Reading

- **[CLAUDE_SESSION_START.md](CLAUDE_SESSION_START.md)** - Start here every session
- **[CLAUDE_DEVELOPMENT_GUIDE.md](CLAUDE_DEVELOPMENT_GUIDE.md)** - TDD workflow, anti-patterns
- **[IMPLEMENTATION_STATUS.md](IMPLEMENTATION_STATUS.md)** - Current feature matrix
- **[TEST_DRIVEN_PLAN.md](TEST_DRIVEN_PLAN.md)** - TDD methodology

### Strategy & Architecture

- **[MODERNIZATION_STRATEGY.md](MODERNIZATION_STRATEGY.md)** - Progressive rewrite rationale
- **[MODERNIZATION_PROPOSAL.md](MODERNIZATION_PROPOSAL.md)** - Original architecture proposal
- **[gnuplot-render/ARCHITECTURE.md](gnuplot-java/gnuplot-render/ARCHITECTURE.md)** - Scene graph design

### Testing & Validation

- **[TESTING.md](TESTING.md)** - Test infrastructure guide
- **[test-tools/README.md](test-tools/README.md)** - Visual comparison tools
- **[test-tools/docs/INTEGRATED_TESTING_GUIDE.md](test-tools/docs/INTEGRATED_TESTING_GUIDE.md)** - Automated comparison

### Active Stories

- **[3D_YAXIS_POSITIONING_ANALYSIS.md](3D_YAXIS_POSITIONING_ANALYSIS.md)** - 3D positioning fix (ready to implement)
- **[BACKLOG_IMPULSES_POINTS.md](BACKLOG_IMPULSES_POINTS.md)** - Impulses and point markers
- **[docs/STORY_TDD4_ROADMAP.md](docs/STORY_TDD4_ROADMAP.md)** - simple.dem roadmap
- **[docs/STORY_TDD6_LEGEND_INTEGRATION.md](docs/STORY_TDD6_LEGEND_INTEGRATION.md)** - Legend integration

---

## 🎯 Key Takeaways

1. **Current Focus**: Polish 3 passing demos to 100% visual accuracy
2. **Next Goal**: Expand to 10 passing demos (4% coverage)
3. **Approach**: Test-driven using C gnuplot demo suite as oracle
4. **Quality**: 989 tests passing, >95% coverage maintained
5. **Performance**: 50% smaller SVG files than C gnuplot

**Remember**: Test-first development is mandatory. Always run `test-tools/run_demo_tests.sh` before and after changes!

---

**Last Updated**: 2025-11-04
**Document Version**: 2.0 (split from IMPLEMENTATION_BACKLOG.md)
