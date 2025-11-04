# Documentation Index

Complete guide to all documentation in the Gnuplot Java modernization project.

---

## 🚀 Start Here

**New to the project?** Read these in order:

1. **[CLAUDE_SESSION_START.md](CLAUDE_SESSION_START.md)** ⭐ **READ FIRST EVERY SESSION**
   - Current status (3/3 demos passing)
   - Recent changes and known issues
   - Development workflow checklist
   - Quick reference for common tasks
   - ~10,000 tokens, fits in single read

2. **[README.md](README.md)** - Project overview
   - What is this project?
   - Quick start guide
   - Architecture overview
   - Current capabilities

3. **[QUICK_START.md](QUICK_START.md)** - Get started in 5 minutes
   - Installation
   - First build
   - First demo run
   - Verify setup

---

## 📋 Essential Documentation (Read for Every Session)

### Development Guidelines

- **[CLAUDE_DEVELOPMENT_GUIDE.md](CLAUDE_DEVELOPMENT_GUIDE.md)** ⚠️ **MANDATORY**
  - Test-driven development workflow
  - Anti-patterns to avoid (no hardcoding!)
  - C code reference process
  - Algorithm porting guidelines
  - Session startup checklist

- **[CONTRIBUTING.md](CONTRIBUTING.md)** - Contribution guidelines
  - Coding standards
  - Test-first workflow
  - Commit message format
  - Pull request process

### Current Status

- **[IMPLEMENTATION_STATUS.md](IMPLEMENTATION_STATUS.md)** - Feature matrix
  - Demo test results (3/3 passing)
  - Implemented features (2D, 3D plotting)
  - Algorithm comparisons (C vs Java)
  - Performance metrics
  - Known issues and limitations

- **[CHANGELOG.md](CHANGELOG.md)** - Recent changes
  - Point marker visibility fix (2025-10-07)
  - simple.dem progress (2025-10-05)
  - Phase 7 completion (2025-10-01)

---

## 🎯 Strategy & Architecture

### Project Strategy

- **[MODERNIZATION_STRATEGY.md](MODERNIZATION_STRATEGY.md)** - Why progressive rewrite
  - Rationale for approach
  - Comparison: rewrite vs conversion
  - Risk mitigation
  - Long-term vision

- **[MODERNIZATION_PROPOSAL.md](MODERNIZATION_PROPOSAL.md)** - Original proposal
  - Microservices architecture
  - Technology stack choices
  - Module design
  - Timeline estimates

- **[TEST_DRIVEN_PLAN.md](TEST_DRIVEN_PLAN.md)** - TDD methodology
  - Demo suite as test oracle
  - Validation approach
  - Comparison tools
  - Success metrics

### Architecture

- **[gnuplot-java/README.md](gnuplot-java/README.md)** - Java modules overview
  - Module structure
  - Technology stack
  - Dependencies
  - Quick start

- **[gnuplot-render/ARCHITECTURE.md](gnuplot-java/gnuplot-render/ARCHITECTURE.md)** - Rendering engine
  - Scene graph design
  - Visitor pattern
  - Rendering pipeline
  - SVG generation

- **[gnuplot-cli/README.md](gnuplot-java/gnuplot-cli/README.md)** - CLI interface
  - 5 execution modes
  - Command parsing
  - Usage examples

---

## 🧪 Testing & Validation

### Testing Guides

- **[TESTING.md](TESTING.md)** ⭐ **PRIMARY TESTING GUIDE**
  - Demo test suite
  - Running tests with Maven
  - Viewing HTML results
  - Visual comparison tools
  - Integration testing

- **[test-tools/README.md](test-tools/README.md)** - Test tools overview
  - Running demo tests
  - Comparison tools (deep, SVG, visual)
  - Test result interpretation
  - Automated comparison

### Comparison Methodology

- **[test-tools/docs/INTEGRATED_TESTING_GUIDE.md](test-tools/docs/INTEGRATED_TESTING_GUIDE.md)** - Automated testing
  - Integration with Maven
  - Continuous validation
  - HTML report generation

- **[test-tools/docs/VISUAL_COMPARISON_APPROACH.md](test-tools/docs/VISUAL_COMPARISON_APPROACH.md)** - Comparison methodology
  - Systematic approach
  - Tool chain
  - Validation levels

- **[test-tools/docs/DEEP_COMPARISON_FINDINGS.md](test-tools/docs/DEEP_COMPARISON_FINDINGS.md)** - Detailed findings
  - Element-by-element analysis
  - Known differences
  - Validation results

- **[test-tools/docs/COMPARISON_SUMMARY.md](test-tools/docs/COMPARISON_SUMMARY.md)** - Quick reference
  - Summary of comparison results
  - Quick lookup

- **[gnuplot-java/test-oracle/README.md](gnuplot-java/test-oracle/README.md)** - Test oracle
  - Extracting reference data from C gnuplot
  - Validation approach

---

## 📦 Backlog & Planning

### Backlog Organization

> **Note:** The original IMPLEMENTATION_BACKLOG.md (3,346 lines, 39K tokens) was too large to read at once.
> It has been split into manageable phase-specific files below.

- **[BACKLOG_SUMMARY.md](BACKLOG_SUMMARY.md)** ⭐ **START HERE**
  - Progress overview
  - Current sprint priorities
  - Key metrics
  - Story points summary
  - Quick navigation to phase files

### Phase-Specific Backlogs

- **[BACKLOG_PHASE_0_SETUP.md](BACKLOG_PHASE_0_SETUP.md)** - Phase 0: Setup ✅ COMPLETE
  - Infrastructure setup (100%)
  - 61/61 story points

- **[BACKLOG_PHASE_1_MATH.md](BACKLOG_PHASE_1_MATH.md)** - Phase 1: Math Engine ✅ MVP COMPLETE
  - Expression parser, evaluator, functions (66%)
  - 197/300 story points

- **[BACKLOG_PHASE_2_DATA.md](BACKLOG_PHASE_2_DATA.md)** - Phase 2: Data Processing ✅ MVP COMPLETE
  - CSV, JSON, interpolation, coordinates (87%)
  - 173/200 story points

- **[BACKLOG_PHASE_3_RENDER.md](BACKLOG_PHASE_3_RENDER.md)** - Phase 3: Rendering 🟡 IN PROGRESS
  - 2D and 3D plotting (~17%)
  - ~50/300 story points

- **[BACKLOG_PHASE_4_BACKEND.md](BACKLOG_PHASE_4_BACKEND.md)** - Phase 4: Backend ⚪ PLANNED
  - Spring Boot API
  - 120 story points

- **[BACKLOG_PHASE_5_FRONTEND.md](BACKLOG_PHASE_5_FRONTEND.md)** - Phase 5: Frontend ⚪ PLANNED
  - React + TypeScript
  - 250 story points

- **[BACKLOG_PHASE_6_INTEGRATION.md](BACKLOG_PHASE_6_INTEGRATION.md)** - Phase 6: Integration ⚪ PLANNED
  - E2E testing, deployment
  - 115 story points

- **[BACKLOG_PHASE_7_CLI.md](BACKLOG_PHASE_7_CLI.md)** - Phase 7: CLI ✅ COMPLETE
  - Script compatibility (100%)
  - 80/80 story points

### Active Stories

- **[3D_YAXIS_POSITIONING_ANALYSIS.md](3D_YAXIS_POSITIONING_ANALYSIS.md)** ⚡ **READY TO IMPLEMENT**
  - 3D vertical positioning issue
  - Root cause: 4/7 scaling ratio vs 1/2
  - Solution: Update mapProjectedX/Y()
  - Estimated effort: 30 minutes

- **[BACKLOG_IMPULSES_POINTS.md](BACKLOG_IMPULSES_POINTS.md)** - Impulses & points
  - Implement "with impulses" style
  - Point marker fixes

- **[docs/STORY_TDD4_ROADMAP.md](docs/STORY_TDD4_ROADMAP.md)** - simple.dem roadmap
  - Implementation phases
  - Feature breakdown

- **[docs/STORY_TDD6_LEGEND_INTEGRATION.md](docs/STORY_TDD6_LEGEND_INTEGRATION.md)** - Legend system
  - Legend rendering integration

---

## 🛠️ Setup & Configuration

- **[SETUP.md](SETUP.md)** - Development environment
  - JDK 21 installation
  - Maven 3.9+ setup
  - IDE configuration (IntelliJ, Eclipse, VS Code)
  - Troubleshooting

---

## 📚 Reference Documentation

### C Gnuplot Reference

- **[gnuplot-c/README_MODERNIZATION.md](gnuplot-c/README_MODERNIZATION.md)** - C code purpose
  - Why preserve C code
  - Algorithm references
  - How to use as reference

### Additional Docs

- **[docs/README.md](docs/README.md)** - Docs directory index
  - Documentation overview
  - JavaDoc links
  - Architecture diagrams

---

## 📁 Documentation by Category

### 🎓 For Learning the Project

**Read in this order:**

1. [CLAUDE_SESSION_START.md](CLAUDE_SESSION_START.md) - Current status, workflow
2. [README.md](README.md) - Project overview
3. [QUICK_START.md](QUICK_START.md) - Get started guide
4. [MODERNIZATION_STRATEGY.md](MODERNIZATION_STRATEGY.md) - Why this approach
5. [gnuplot-render/ARCHITECTURE.md](gnuplot-java/gnuplot-render/ARCHITECTURE.md) - Architecture details

### 👨‍💻 For Active Development

**Essential reading every session:**

1. [CLAUDE_SESSION_START.md](CLAUDE_SESSION_START.md) - Session startup ⭐
2. [CLAUDE_DEVELOPMENT_GUIDE.md](CLAUDE_DEVELOPMENT_GUIDE.md) - Workflow ⚠️ MANDATORY
3. [IMPLEMENTATION_STATUS.md](IMPLEMENTATION_STATUS.md) - Current state
4. [test-results/latest/](test-results/latest/) - Latest test results

**As needed:**

- [TESTING.md](TESTING.md) - How to run tests
- [CONTRIBUTING.md](CONTRIBUTING.md) - Coding standards
- [3D_YAXIS_POSITIONING_ANALYSIS.md](3D_YAXIS_POSITIONING_ANALYSIS.md) - Active issues

### 📊 For Planning & Tracking

**Progress tracking:**

1. [BACKLOG_SUMMARY.md](BACKLOG_SUMMARY.md) - Overall progress ⭐
2. [IMPLEMENTATION_STATUS.md](IMPLEMENTATION_STATUS.md) - Feature matrix
3. [CHANGELOG.md](CHANGELOG.md) - Recent changes

**Detailed planning:**

- Phase-specific backlog files (BACKLOG_PHASE_*.md)
- [TEST_DRIVEN_PLAN.md](TEST_DRIVEN_PLAN.md) - TDD methodology

### 🧪 For Testing & Validation

**Quick testing:**

1. [TESTING.md](TESTING.md) - Main testing guide ⭐
2. [test-tools/README.md](test-tools/README.md) - Comparison tools

**Deep dive:**

- [test-tools/docs/INTEGRATED_TESTING_GUIDE.md](test-tools/docs/INTEGRATED_TESTING_GUIDE.md) - Automated testing
- [test-tools/docs/VISUAL_COMPARISON_APPROACH.md](test-tools/docs/VISUAL_COMPARISON_APPROACH.md) - Methodology
- [test-tools/docs/DEEP_COMPARISON_FINDINGS.md](test-tools/docs/DEEP_COMPARISON_FINDINGS.md) - Detailed analysis

### 🏗️ For Architecture Understanding

**Core architecture:**

- [MODERNIZATION_STRATEGY.md](MODERNIZATION_STRATEGY.md) - Overall strategy
- [MODERNIZATION_PROPOSAL.md](MODERNIZATION_PROPOSAL.md) - Original design
- [gnuplot-render/ARCHITECTURE.md](gnuplot-java/gnuplot-render/ARCHITECTURE.md) - Rendering engine

**Module-specific:**

- [gnuplot-java/README.md](gnuplot-java/README.md) - Modules overview
- [gnuplot-cli/README.md](gnuplot-java/gnuplot-cli/README.md) - CLI module
- [gnuplot-java/test-oracle/README.md](gnuplot-java/test-oracle/README.md) - Test oracle

---

## 🔍 Finding Documentation

### By File Type

**Markdown files (.md):**
```bash
# List all markdown files
find . -name "*.md" -not -path "*/node_modules/*" -not -path "*/target/*"

# Search within markdown files
grep -r "keyword" --include="*.md" .
```

### By Topic

| Topic | Key Documents |
|-------|---------------|
| **Getting Started** | README.md, QUICK_START.md, SETUP.md |
| **Development** | CLAUDE_SESSION_START.md, CLAUDE_DEVELOPMENT_GUIDE.md, CONTRIBUTING.md |
| **Testing** | TESTING.md, test-tools/README.md, test-tools/docs/* |
| **Status** | IMPLEMENTATION_STATUS.md, CHANGELOG.md, BACKLOG_SUMMARY.md |
| **Strategy** | MODERNIZATION_STRATEGY.md, MODERNIZATION_PROPOSAL.md, TEST_DRIVEN_PLAN.md |
| **Architecture** | gnuplot-render/ARCHITECTURE.md, gnuplot-java/README.md |
| **Backlog** | BACKLOG_SUMMARY.md, BACKLOG_PHASE_*.md |
| **Active Work** | 3D_YAXIS_POSITIONING_ANALYSIS.md, BACKLOG_IMPULSES_POINTS.md |

---

## 📝 Documentation Standards

### File Naming Conventions

- **UPPERCASE.md** - Root-level project documentation
- **lowercase.md** - Module-specific or detailed documentation
- **BACKLOG_*.md** - Backlog and planning documents
- **STORY_*.md** - Individual story/epic documentation

### Linking Guidelines

- Use relative paths for all links
- Link to specific line numbers when referencing code: `[file.java:42](path/to/file.java#L42)`
- Use markdown link syntax for files: `[FILENAME.md](path/to/FILENAME.md)`

### Update Frequency

- **[CLAUDE_SESSION_START.md](CLAUDE_SESSION_START.md)** - Updated with every significant change
- **[IMPLEMENTATION_STATUS.md](IMPLEMENTATION_STATUS.md)** - Updated when demo status changes
- **[CHANGELOG.md](CHANGELOG.md)** - Updated with every significant fix or feature
- **[BACKLOG_SUMMARY.md](BACKLOG_SUMMARY.md)** - Updated weekly or when priorities change

---

## 🤝 Contributing to Documentation

When adding or updating documentation:

1. **Keep CLAUDE_SESSION_START.md current** - This is the most important file
2. **Update this index** when adding new documentation files
3. **Link from multiple places** - Make docs discoverable
4. **Use consistent formatting** - Follow existing patterns
5. **Keep it concise** - Link to details rather than duplicating

---

## 📊 Documentation Statistics

**Total Documentation:**
- ~33 markdown files
- ~195,000 tokens total
- ~25,000 tokens essential reading (CLAUDE_SESSION_START.md + guides)

**Most Important Files (Read Every Session):**
1. CLAUDE_SESSION_START.md (~10K tokens)
2. CLAUDE_DEVELOPMENT_GUIDE.md (~4K tokens)
3. IMPLEMENTATION_STATUS.md (~3K tokens)
4. BACKLOG_SUMMARY.md (~8K tokens)

**Total Essential: ~25K tokens** (fits in single context window)

---

**Last Updated:** 2025-11-04
**Maintainer:** Claude Code Documentation Team
**Version:** 1.0
