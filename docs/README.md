# Gnuplot Modernization Documentation

Welcome to the Gnuplot Modernization documentation. This directory contains comprehensive documentation for developers and users.

---

## 📚 Documentation Structure

```
docs/
├── README.md                       # This file
├── api/                            # API documentation
│   └── (JavaDoc HTML - generated)
├── guides/                         # Developer guides
│   ├── architecture.md             # Architecture overview
│   ├── expression-parser.md        # Expression parser guide
│   └── rendering.md                # Rendering engine guide
└── images/                         # Documentation images
```

---

## 🚀 Quick Links

### For Users

- **[Getting Started](../README.md#quick-start)** - Installation and first steps
- **[User Guide](../README.md)** - Complete user documentation
- **REST API Documentation** - Coming soon

### For Developers

- **[Development Setup](../SETUP.md)** - Set up your development environment
- **[Contributing Guide](../CONTRIBUTING.md)** - How to contribute
- **[Testing Guide](../TESTING.md)** - Testing practices
- **[Architecture Overview](guides/architecture.md)** - System architecture
- **[API Documentation](api/index.html)** - JavaDoc reference

### Project Planning

- **[Modernization Strategy](../MODERNIZATION_STRATEGY.md)** - Detailed modernization approach
- **[Implementation Backlog](../IMPLEMENTATION_BACKLOG.md)** - 200+ user stories
- **[Original Proposal](../MODERNIZATION_PROPOSAL.md)** - Initial architecture proposal

---

## 📖 API Documentation

JavaDoc documentation for all Java modules is available at:

- **[gnuplot-core](api/gnuplot-core/index.html)** - Core mathematical engine
- **[gnuplot-render](api/gnuplot-render/index.html)** - Rendering engine
- **[gnuplot-server](api/gnuplot-server/index.html)** - REST API server
- **[gnuplot-cli](api/gnuplot-cli/index.html)** - Command-line interface

### Generating JavaDoc

```bash
cd gnuplot-java

# Generate JavaDoc for all modules
mvn javadoc:aggregate

# Generated docs will be in target/site/apidocs/
```

---

## 🏗️ Architecture Documentation

### System Overview

The Gnuplot Modernization project follows a modular architecture:

```
┌─────────────────────────────────────────────────────────────┐
│                       Client Layer                          │
│  ┌─────────────┐  ┌──────────────┐  ┌──────────────────┐  │
│  │ React Web UI│  │  CLI Client  │  │  External Apps   │  │
│  └─────────────┘  └──────────────┘  └──────────────────┘  │
└──────────────────────────┬──────────────────────────────────┘
                           │
                           ▼
┌─────────────────────────────────────────────────────────────┐
│                     API Layer (REST)                        │
│  ┌─────────────────────────────────────────────────────┐   │
│  │        gnuplot-server (Spring Boot)                 │   │
│  │  - REST Endpoints    - WebSocket                    │   │
│  │  - Authentication    - Data Upload                  │   │
│  └─────────────────────────────────────────────────────┘   │
└──────────────────────────┬──────────────────────────────────┘
                           │
                           ▼
┌─────────────────────────────────────────────────────────────┐
│                     Core Services                           │
│  ┌────────────────┐         ┌───────────────────────┐      │
│  │  gnuplot-core  │────────▶│   gnuplot-render      │      │
│  │                │         │                       │      │
│  │  - Parser      │         │  - 2D Rendering       │      │
│  │  - Evaluator   │         │  - 3D Rendering       │      │
│  │  - Functions   │         │  - SVG/PNG/PDF Output │      │
│  │  - Data        │         │  - OpenGL Acceleration│      │
│  └────────────────┘         └───────────────────────┘      │
└─────────────────────────────────────────────────────────────┘
```

---

## 🛠️ Development Guides

### Core Module (gnuplot-core)

The core module provides:
- Mathematical expression parser (ANTLR4-based)
- Expression evaluator with variable support
- 100+ mathematical functions (via Apache Commons Math)
- Data processing and statistics
- Coordinate system transformations

**[Read More](guides/architecture.md)**

### Render Module (gnuplot-render)

The render module handles:
- 2D plot generation (line, scatter, bar, histogram, heatmap, contour)
- 3D surface and volume rendering
- Multiple output formats (SVG, PNG, PDF, HTML)
- OpenGL-accelerated 3D rendering

### Server Module (gnuplot-server)

The server module provides:
- RESTful API for plot creation
- WebSocket for real-time updates
- User authentication and authorization
- Data upload and processing
- Plot management (CRUD operations)

### CLI Module (gnuplot-cli)

The CLI module offers:
- Interactive shell with command history
- Script execution and batch processing
- Pipe support for data streaming
- Backward compatibility layer for Gnuplot scripts

---

## 📝 Code Examples

### Creating a Simple Plot

```java
// Core API usage
ExpressionEvaluator evaluator = new ExpressionEvaluator();
double result = evaluator.evaluate("sin(x)", Map.of("x", Math.PI / 2));
// result = 1.0

// Rendering API usage
Plot2D plot = new Plot2D();
plot.setTitle("Sine Function");
plot.addSeries("y = sin(x)", x -> Math.sin(x), 0, 2 * Math.PI);

SVGRenderer renderer = new SVGRenderer();
renderer.render(plot, Path.of("output.svg"));
```

### REST API Usage

```bash
# Create a new plot
curl -X POST http://localhost:8080/api/plots \
  -H "Content-Type: application/json" \
  -d '{
    "title": "My Plot",
    "type": "line",
    "data": [1, 2, 3, 4, 5]
  }'

# Get plot as PNG
curl http://localhost:8080/api/plots/123/render?format=png \
  --output plot.png
```

---

## 🧪 Testing

Comprehensive testing documentation is available in [TESTING.md](../TESTING.md).

```bash
# Run all tests
mvn clean verify

# Run tests with coverage report
mvn clean verify jacoco:report

# View coverage report
open gnuplot-core/target/site/jacoco/index.html
```

---

## 📊 Project Status

See [IMPLEMENTATION_BACKLOG.md](../IMPLEMENTATION_BACKLOG.md) for:
- Current phase progress
- Completed user stories
- Remaining backlog items
- Story point estimates

---

## 🤝 Contributing

We welcome contributions! Please read:

1. **[CONTRIBUTING.md](../CONTRIBUTING.md)** - Contribution guidelines
2. **[Code of Conduct](#code-of-conduct)** - Community standards
3. **[Development Workflow](../CONTRIBUTING.md#development-workflow)** - How to contribute code

---

## 📄 License

This project maintains the same license as the original Gnuplot. See [Copyright](../Copyright) for details.

---

## 📧 Support

- **GitHub Issues**: [Report bugs](https://github.com/gnuplot/gnuplot-java/issues)
- **GitHub Discussions**: [Ask questions](https://github.com/gnuplot/gnuplot-java/discussions)
- **Email**: dev@gnuplot.com

---

**Last Updated**: 2025-09-30