# Gnuplot Modernization Project

![Java Version](https://img.shields.io/badge/Java-21-blue.svg)
![Maven](https://img.shields.io/badge/Maven-3.9+-blue.svg)
![License](https://img.shields.io/badge/License-Gnuplot-green.svg)
![Status](https://img.shields.io/badge/Status-In%20Development-yellow.svg)

Modern Java implementation of the Gnuplot plotting utility with a contemporary web-based frontend.

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

---

## 🏗️ Architecture

```
gnuplot-modern/
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

### Prerequisites

- **JDK 21** LTS or higher
- **Maven 3.9+**
- **Git**

### Clone and Build

```bash
# Clone repository
git clone <repository-url>
cd gnuplot-master

# Build all modules
mvn clean install

# Run tests
mvn test
```

### Run Tests

```bash
# All tests
mvn test

# Specific module
mvn test -pl gnuplot-core

# With coverage report
mvn clean test jacoco:report
```

---

## 📚 Documentation

| Document | Description |
|----------|-------------|
| [SETUP.md](SETUP.md) | Development environment setup guide |
| [MODERNIZATION_STRATEGY.md](MODERNIZATION_STRATEGY.md) | Detailed modernization approach and rationale |
| [IMPLEMENTATION_BACKLOG.md](IMPLEMENTATION_BACKLOG.md) | Complete backlog with 200+ user stories |
| [MODERNIZATION_PROPOSAL.md](MODERNIZATION_PROPOSAL.md) | Original architecture proposal |

---

## 📦 Modules

### gnuplot-core
Core mathematical engine providing:
- Expression parser (ANTLR4-based)
- Mathematical function library (100+ functions)
- Data processing and statistics
- Coordinate system transformations

### gnuplot-render
Rendering engine supporting:
- 2D plots (line, scatter, bar, histogram, heatmap, contour)
- 3D plots (surface, isosurface, voxel)
- Multiple output formats (SVG, PNG, PDF)
- OpenGL-accelerated 3D rendering

### gnuplot-server
Spring Boot REST API providing:
- Plot creation and management
- Data upload and processing
- User authentication
- Real-time updates via WebSocket

### gnuplot-cli
Command-line interface for:
- Interactive shell
- Script execution
- Batch processing
- Pipe support

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

### Current Phase: **Phase 0 - Project Setup** ✅

| Phase | Status | Progress |
|-------|--------|----------|
| Phase 0: Setup | 🟢 In Progress | 40% |
| Phase 1: Core Math Engine | 🔵 Planned | 0% |
| Phase 2: Data Processing | 🔵 Planned | 0% |
| Phase 3: Rendering Engine | 🔵 Planned | 0% |
| Phase 4: Backend Server | 🔵 Planned | 0% |
| Phase 5: Web Frontend | 🔵 Planned | 0% |

**Timeline**: 12-18 months to MVP

---

## 🤝 Contributing

We welcome contributions! Please see our [Contributing Guide](CONTRIBUTING.md) (to be created) for details.

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

**Last Updated**: 2025-09-30