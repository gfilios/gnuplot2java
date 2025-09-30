# Contributing to Gnuplot Modernization

Thank you for your interest in contributing to the Gnuplot Modernization Project! This document provides guidelines and instructions for contributing.

---

## 📋 Table of Contents

- [Code of Conduct](#code-of-conduct)
- [Getting Started](#getting-started)
- [Development Workflow](#development-workflow)
- [Coding Standards](#coding-standards)
- [Testing Guidelines](#testing-guidelines)
- [Commit Message Guidelines](#commit-message-guidelines)
- [Pull Request Process](#pull-request-process)
- [Project Structure](#project-structure)

---

## Code of Conduct

This project adheres to a code of professional conduct. By participating, you are expected to:

- Be respectful and inclusive
- Focus on constructive feedback
- Prioritize the project's best interests
- Help create a welcoming environment

---

## Getting Started

### Prerequisites

Before contributing, ensure you have:

1. **Java 21 LTS** - Download from [Adoptium](https://adoptium.net/)
2. **Maven 3.9+** - [Installation Guide](https://maven.apache.org/install.html)
3. **Git** - [Download](https://git-scm.com/downloads)
4. **IDE** - IntelliJ IDEA (recommended), Eclipse, or VS Code

📖 **See [SETUP.md](SETUP.md) for detailed setup instructions**

### Fork and Clone

```bash
# Fork the repository on GitHub
# Then clone your fork
git clone https://github.com/YOUR_USERNAME/gnuplot-java.git
cd gnuplot-java

# Add upstream remote
git remote add upstream https://github.com/gnuplot/gnuplot-java.git
```

---

## Development Workflow

### 1. Create a Feature Branch

```bash
# Update your local main branch
git checkout main
git pull upstream main

# Create a feature branch
git checkout -b feature/expression-parser
# or
git checkout -b fix/null-pointer-rendering
```

### 2. Make Your Changes

```bash
# Navigate to Java project
cd gnuplot-java

# Make your changes
# Write tests
# Run tests locally
mvn clean test

# Run code quality checks
mvn checkstyle:check
```

### 3. Commit Your Changes

Follow our [commit message guidelines](#commit-message-guidelines):

```bash
git add .
git commit -m "feat(core): add expression parser for mathematical functions"
```

### 4. Push and Create Pull Request

```bash
git push origin feature/expression-parser

# Create Pull Request on GitHub
# Fill out the PR template
```

---

## Coding Standards

### Java Style Guide

We follow the **Google Java Style Guide** with minor modifications:

- **Line Length**: 120 characters maximum
- **Indentation**: 4 spaces (no tabs)
- **Braces**: K&R style (opening brace on same line)
- **Naming Conventions**:
  - Classes: `PascalCase`
  - Methods: `camelCase`
  - Constants: `UPPER_SNAKE_CASE`
  - Packages: `lowercase.dot.separated`

### Code Quality Tools

All code must pass:

```bash
# Checkstyle
mvn checkstyle:check

# Unit tests
mvn test

# Integration tests
mvn verify
```

### Documentation

- **Public APIs**: Must have JavaDoc comments
- **Complex Logic**: Add inline comments explaining "why", not "what"
- **Examples**: Include usage examples in JavaDoc for key classes

Example:

```java
/**
 * Evaluates a mathematical expression and returns the result.
 *
 * <p>This evaluator supports basic arithmetic operations, functions,
 * and variables. It uses ANTLR4 for parsing and maintains variable
 * scope for efficient evaluation.
 *
 * <p>Example usage:
 * <pre>{@code
 * ExpressionEvaluator evaluator = new ExpressionEvaluator();
 * double result = evaluator.evaluate("2 * sin(pi/4)");
 * // result = 1.414...
 * }</pre>
 *
 * @param expression the mathematical expression to evaluate
 * @return the numerical result of the evaluation
 * @throws ParseException if the expression syntax is invalid
 */
public double evaluate(String expression) throws ParseException {
    // implementation
}
```

---

## Testing Guidelines

### Test Coverage

- **Minimum Coverage**: 70% line coverage (enforced by JaCoCo)
- **Critical Paths**: 90%+ coverage for core mathematical functions
- **Edge Cases**: Always test boundary conditions

### Test Structure

```java
@DisplayName("ExpressionEvaluator")
class ExpressionEvaluatorTest {

    private ExpressionEvaluator evaluator;

    @BeforeEach
    void setUp() {
        evaluator = new ExpressionEvaluator();
    }

    @Test
    @DisplayName("should evaluate simple addition")
    void shouldEvaluateSimpleAddition() {
        // Given
        String expression = "2 + 3";

        // When
        double result = evaluator.evaluate(expression);

        // Then
        assertThat(result).isEqualTo(5.0);
    }

    @Test
    @DisplayName("should throw exception for invalid syntax")
    void shouldThrowExceptionForInvalidSyntax() {
        // Given
        String expression = "2 +* 3";

        // When/Then
        assertThatThrownBy(() -> evaluator.evaluate(expression))
            .isInstanceOf(ParseException.class)
            .hasMessageContaining("Unexpected token");
    }
}
```

### Running Tests

```bash
# Unit tests only
mvn test

# Integration tests only
mvn verify -DskipUnitTests

# All tests with coverage
mvn clean verify jacoco:report

# View coverage report
open gnuplot-core/target/site/jacoco/index.html
```

---

## Commit Message Guidelines

We follow **Conventional Commits** specification.

### Format

```
<type>(<scope>): <subject>

<body>

<footer>
```

### Types

- `feat`: New feature
- `fix`: Bug fix
- `docs`: Documentation only changes
- `style`: Code style changes (formatting, missing semicolons, etc.)
- `refactor`: Code refactoring without changing behavior
- `perf`: Performance improvements
- `test`: Adding or updating tests
- `build`: Changes to build system or dependencies
- `ci`: Changes to CI configuration
- `chore`: Other changes that don't modify src or test files

### Scopes

- `core`: gnuplot-core module
- `render`: gnuplot-render module
- `server`: gnuplot-server module
- `cli`: gnuplot-cli module
- `build`: Build system
- `ci`: CI/CD pipeline

### Examples

```bash
# Feature
feat(core): add trigonometric functions to expression evaluator

# Bug fix
fix(render): correct SVG coordinate transformation for 3D plots

# Documentation
docs(api): update REST API documentation for plot endpoints

# Refactoring
refactor(core): extract parser initialization into factory method

# Performance
perf(render): optimize pixel buffer allocation in PNG renderer
```

---

## Pull Request Process

### Before Submitting

1. ✅ All tests pass: `mvn clean verify`
2. ✅ Code style checks pass: `mvn checkstyle:check`
3. ✅ Coverage meets minimum threshold
4. ✅ Documentation updated (if applicable)
5. ✅ Commits follow message guidelines
6. ✅ Branch is up-to-date with `main`

### PR Template

When creating a PR, include:

```markdown
## Summary
Brief description of changes

## Related Issue
Fixes #123

## Changes Made
- Added expression parser
- Implemented basic arithmetic operations
- Added unit tests with 85% coverage

## Testing
- [x] Unit tests added/updated
- [x] Integration tests added/updated
- [x] Manual testing performed

## Checklist
- [x] Code follows style guidelines
- [x] Tests pass locally
- [x] Documentation updated
- [x] Commit messages follow convention
```

### Review Process

1. **Automated Checks**: CI/CD pipeline runs automatically
2. **Code Review**: At least one maintainer review required
3. **Feedback**: Address review comments and push updates
4. **Approval**: Once approved, a maintainer will merge

---

## Project Structure

### Repository Layout

```
gnuplot-master/
├── gnuplot-c/                      # Original C implementation (reference)
│   └── ...
├── gnuplot-java/                   # New Java implementation
│   ├── gnuplot-core/               # Core mathematical engine
│   │   ├── src/main/java/
│   │   │   └── com/gnuplot/core/
│   │   │       ├── math/           # Expression parser & evaluator
│   │   │       ├── functions/      # Mathematical functions
│   │   │       ├── data/           # Data processing
│   │   │       └── geometry/       # Coordinate systems
│   │   └── src/test/java/          # Unit tests
│   ├── gnuplot-render/             # Rendering engine
│   ├── gnuplot-server/             # Spring Boot REST API
│   ├── gnuplot-cli/                # CLI interface
│   └── pom.xml                     # Maven parent POM
├── .github/workflows/              # CI/CD workflows
├── SETUP.md                        # Development setup guide
├── TESTING.md                      # Testing guide
└── CONTRIBUTING.md                 # This file
```

### Module Responsibilities

- **gnuplot-core**: Mathematical engine, parser, data processing
- **gnuplot-render**: 2D/3D rendering, output formats (SVG, PNG, PDF)
- **gnuplot-server**: REST API, WebSocket, authentication
- **gnuplot-cli**: Command-line interface, script execution

---

## Questions or Issues?

- **Documentation**: Check [SETUP.md](SETUP.md) and [TESTING.md](TESTING.md)
- **Bugs**: Open an [issue on GitHub](https://github.com/gnuplot/gnuplot-java/issues)
- **Discussions**: Use [GitHub Discussions](https://github.com/gnuplot/gnuplot-java/discussions)

---

## License

By contributing, you agree that your contributions will be licensed under the same license as the original Gnuplot project.

---

**Thank you for contributing to Gnuplot Modernization!** 🚀