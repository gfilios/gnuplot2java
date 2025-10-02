# Gnuplot Modernization Project

![Java Version](https://img.shields.io/badge/Java-21-blue.svg)
![Maven](https://img.shields.io/badge/Maven-3.9+-blue.svg)
![License](https://img.shields.io/badge/License-Gnuplot-green.svg)
![Status](https://img.shields.io/badge/Status-In%20Development-yellow.svg)

Modern Java implementation of the Gnuplot plotting utility with a contemporary web-based frontend.

---

## 📁 Repository Structure

This repository contains both the **original C implementation** (for reference) and the **new Java implementation**:

```
gnuplot-master/
├── gnuplot-c/                      # Original C implementation (v6.1.0)
│   ├── src/                        # C source code
│   ├── term/                       # Terminal drivers
│   ├── demo/                       # Demo plots
│   ├── docs/                       # Original documentation
│   └── ...                         # C build files
│
├── gnuplot-java/                   # New Java implementation
│   ├── gnuplot-core/               # Mathematical engine
│   ├── gnuplot-render/             # Rendering engine
│   ├── gnuplot-server/             # Spring Boot API
│   ├── gnuplot-cli/                # CLI interface
│   └── pom.xml                     # Maven parent POM
│
├── MODERNIZATION_STRATEGY.md      # Modernization approach
├── IMPLEMENTATION_BACKLOG.md      # Complete backlog
├── SETUP.md                        # Dev environment setup
├── TESTING.md                      # Testing guide
└── README.md                       # This file
```

---

## 📋 Project Overview

This project is a complete modernization of [Gnuplot](http://gnuplot.sourceforge.net/) from C to Java, featuring:

- ✅ **Modern Architecture**: Clean, modular design using Java 21 and Spring Boot
- ✅ **Web-Based UI**: Interactive React frontend for plot creation
- ✅ **REST API**: Comprehensive API for programmatic access
- ✅ **Multiple Output Formats**: PNG, SVG, PDF, interactive HTML
- ✅ **2D & 3D Plotting**: Full support for scientific visualization
- ✅ **Mathematical Engine**: 100+ built-in functions with Apache Commons Math
- ✅ **Backward Compatibility**: Optional Gnuplot script compatibility layer

**Approach**: Progressive rewrite using C code as reference, not direct conversion.

📖 **See [MODERNIZATION_STRATEGY.md](MODERNIZATION_STRATEGY.md) for detailed rationale**

---

## 🏗️ Architecture

### New Java Implementation

```
gnuplot-java/
├── gnuplot-core/         # Mathematical engine (parser, evaluator, functions)
├── gnuplot-render/       # 2D/3D rendering engine (SVG, PNG, PDF, OpenGL)
├── gnuplot-server/       # Spring Boot REST API
├── gnuplot-cli/          # Command-line interface
└── gnuplot-web/          # React frontend (planned)
```

**Technology Stack:**
- **Backend**: Java 21, Spring Boot 3.2, ANTLR4, Apache Commons Math
- **Frontend**: React 18, TypeScript, Plotly.js, Material-UI
- **Build**: Maven 3.9+
- **Database**: PostgreSQL 16+, Redis 7+
- **Testing**: JUnit 5, Mockito, AssertJ, Testcontainers

---

## 🚀 Quick Start

### For Java Development (New Implementation)

```bash
# Clone repository
git clone <repository-url>
cd gnuplot-master

# Navigate to Java project
cd gnuplot-java

# Build all modules
mvn clean install

# Run tests
mvn test
```

📖 **See [gnuplot-java/README.md](gnuplot-java/README.md) for detailed Java documentation**

### For C Reference (Original Implementation)

```bash
cd gnuplot-c

# Original build
./configure
make
```

📖 **See [gnuplot-c/README](gnuplot-c/README) for original documentation**

---

## 📚 Documentation

| Document | Description |
|----------|-------------|
| [SETUP.md](../SETUP.md) | Development environment setup guide |
| [MODERNIZATION_STRATEGY.md](../MODERNIZATION_STRATEGY.md) | Detailed modernization approach and rationale |
| [IMPLEMENTATION_BACKLOG.md](../IMPLEMENTATION_BACKLOG.md) | Complete backlog with 200+ user stories |
| [MODERNIZATION_PROPOSAL.md](../MODERNIZATION_PROPOSAL.md) | Original architecture proposal |
| [CONTRIBUTING.md](../CONTRIBUTING.md) | Contributing guidelines and workflow |
| [TESTING.md](../TESTING.md) | Testing strategy and best practices |

---

## 📦 Modules

### gnuplot-core ✅ (MVP Complete)
Core mathematical engine providing:
- ✅ Expression parser (ANTLR4-based, 14 precedence levels)
- ✅ Mathematical function library (38+ functions)
- ✅ Data processing (CSV, JSON readers)
- ✅ Statistical analysis (descriptive statistics)
- ✅ Coordinate system transformations (Cartesian 2D/3D, Polar 2D)
- ✅ Interpolation (linear, cubic spline)

### gnuplot-render 🟡 (In Progress - 30%)
Rendering engine supporting:
- ✅ Rendering pipeline architecture (Scene, Viewport, RenderingHints)
- ✅ Scene elements (Axis, LinePlot, Legend, Grid, Label)
- ✅ Axis tick generation (linear, logarithmic, time-based scales)
- ✅ Color palette system (RGB formulas, gradients, viridis)
- ✅ Text rendering and fonts (measurement, rotation, Unicode)
- 🟡 SVG renderer (in progress)
- 🔵 PNG renderer (planned)
- 🔵 2D plots (line, scatter, bar, histogram, heatmap, contour) - planned
- 🔵 3D plots (surface, isosurface, voxel) - planned
- 🔵 OpenGL-accelerated 3D rendering - planned

### gnuplot-server 🔵 (Planned)
Spring Boot REST API providing:
- 🔵 Plot creation and management
- 🔵 Data upload and processing
- 🔵 User authentication
- 🔵 Real-time updates via WebSocket

### gnuplot-cli 🔵 (Planned)
Command-line interface for:
- 🔵 Interactive shell
- 🔵 Script execution
- 🔵 Batch processing
- 🔵 Pipe support

---

## 🛠️ Development

### Project Structure

```
gnuplot-core/
├── src/
│   ├── main/
│   │   ├── java/com/gnuplot/core/
│   │   │   ├── math/        # Expression parser & evaluator
│   │   │   ├── functions/   # Mathematical functions
│   │   │   ├── data/        # Data processing
│   │   │   └── geometry/    # Coordinate systems
│   │   └── antlr4/          # ANTLR4 grammar files
│   └── test/
│       └── java/            # Unit tests
└── pom.xml
```

### Building

```bash
# Full build with tests
mvn clean install

# Skip tests (faster)
mvn clean install -DskipTests

# Build specific module
mvn clean install -pl gnuplot-core

# Run with specific profile
mvn clean install -P ci
```

### Code Quality

```bash
# Run Checkstyle
mvn checkstyle:check

# Run SpotBugs
mvn spotbugs:check

# Generate all reports
mvn site
```

---

## 🧪 Testing

### Test Structure

- **Unit Tests**: `*Test.java` in `src/test/java`
- **Integration Tests**: `*IntegrationTest.java` or `*IT.java`
- **Test Coverage**: JaCoCo reports in `target/site/jacoco/`

### Running Tests

```bash
# Unit tests only
mvn test

# Integration tests only
mvn verify -DskipUnitTests

# All tests with coverage
mvn clean verify jacoco:report

# Specific test class
mvn test -Dtest=ExpressionParserTest

# Specific test method
mvn test -Dtest=ExpressionParserTest#shouldParseAddition
```

---

## 📈 Project Status

### Current Phase: **Phase 3 - Rendering Engine** 🟡

| Phase | Status | Progress | Tests |
|-------|--------|----------|-------|
| Phase 0: Setup | ✅ Complete | 100% | - |
| Phase 1: Core Math Engine | ✅ Complete (MVP) | 66% | 335 passing |
| Phase 2: Data Processing | ✅ Complete (MVP) | 100% P0 | 238 passing |
| Phase 3: Rendering Engine | 🟡 In Progress | 30% | 202 passing |
| Phase 4: Backend Server | 🔵 Planned | 0% | - |
| Phase 5: Web Frontend | 🔵 Planned | 0% | - |

**Total Tests**: 775 passing ✅ (335 Phase 1 + 238 Phase 2 + 202 Phase 3)
**Timeline**: 12-18 months to MVP

**Latest Achievement**: Completed text rendering and fonts system! Font management, accurate text measurement using AWT, rotation support, Unicode validation, and SVG escaping. 38 tests validate all text rendering functionality. Epic 3.1 (Rendering Infrastructure) MVP now complete!

### Recent Milestones
- ✅ **Phase 1 Complete (66%)**: Expression parser, evaluator, 38+ math functions
  - ANTLR4 grammar with 14 precedence levels
  - Variable support and function call framework
  - Standard math, special, Bessel, error, statistical, and random functions
  - Complex number foundation
  - Context-aware error handling with source location tracking

- ✅ **Phase 2 Complete (100% P0)**: Data processing layer MVP ready
  - CSV, JSON readers with factory pattern
  - Row/column filtering with expression support
  - Linear and cubic spline interpolation
  - Descriptive statistics (mean, median, variance, correlation)
  - Cartesian 2D/3D and Polar 2D coordinate systems
  - Point3D geometry with vector operations

- 🟡 **Phase 3 In Progress (30%)**: Rendering Engine
  - ✅ **Epic 3.1 MVP Complete!** Rendering Infrastructure (88 SP, 219 tests)
    - Rendering pipeline architecture (Scene, Viewport, RenderingHints)
    - Scene elements (Axis, LinePlot, Legend, Grid, Label)
    - TickGenerator with gnuplot's quantize_normal_tics algorithm
    - Axis tick generation (linear, logarithmic, time-based scales)
    - Color palette system (RGB formulas, gradients, viridis, cubehelix)
    - Text rendering and fonts (measurement, rotation, Unicode)
  - 🔵 Next: Epic 3.2 - Line and Scatter Plot Renderers

---

## 🤝 Contributing

We welcome contributions! Please see our [Contributing Guide](../CONTRIBUTING.md) for details.

### Development Workflow

1. Fork the repository
2. Create a feature branch: `git checkout -b feature/expression-parser`
3. Make your changes
4. Run tests: `mvn test`
5. Commit with conventional commits: `feat(core): add expression parser`
6. Push and create a Pull Request

### Code Style

- **Java**: Follow Google Java Style Guide
- **Indentation**: 4 spaces
- **Line Length**: 120 characters max
- **Naming**: PascalCase for classes, camelCase for methods

---

## 📄 License

This project maintains the same license as the original Gnuplot. See [Copyright](Copyright) for details.

---

## 🔗 Links

- **Original Gnuplot**: [gnuplot.sourceforge.net](http://gnuplot.sourceforge.net/)
- **Documentation**: [Gnuplot Manual](http://www.gnuplot.info/documentation.html)
- **GitHub Issues**: Report bugs and request features

---

## 🙏 Acknowledgments

This project is based on the original [Gnuplot](http://gnuplot.sourceforge.net/) by Thomas Williams and Colin Kelley, maintained by many contributors over 40+ years.

---

## 📧 Contact

- **Issues**: GitHub Issues
- **Discussions**: GitHub Discussions
- **Email**: dev@gnuplot.com

---

**Last Updated**: 2025-10-02

Epic 3.1 (Rendering Infrastructure) is complete! ✅