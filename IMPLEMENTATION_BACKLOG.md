# Gnuplot Modernization - Implementation Backlog

**Project**: Gnuplot Java Modernization
**Approach**: Progressive Rewrite
**Timeline**: 12-18 months to MVP
**Last Updated**: 2025-09-30

---

## Backlog Organization

- **Epic**: Major feature area (Phase-level)
- **Story**: User-facing functionality
- **Task**: Technical implementation work
- **Spike**: Research/investigation work

**Priority Levels**:
- 🔴 P0: Blocker - Must have for MVP
- 🟠 P1: Critical - Important for MVP
- 🟡 P2: High - Nice to have for MVP
- 🟢 P3: Medium - Post-MVP
- 🔵 P4: Low - Future consideration

**Story Points**: Fibonacci scale (1, 2, 3, 5, 8, 13, 21)

---

# PHASE 0: PROJECT SETUP (Weeks 1-4)

## Epic 0.1: Infrastructure Setup

### Story 0.1.1: Development Environment Setup 🔴 P0
**As a** developer
**I want** a standardized development environment
**So that** everyone can build and run the project consistently

**Acceptance Criteria**:
- [ ] JDK 21 LTS installed and configured
- [ ] Maven 3.9+ installed
- [ ] IDE setup guide (IntelliJ IDEA / Eclipse / VS Code)
- [ ] Git hooks configured (pre-commit, pre-push)
- [ ] Environment variables documented

**Tasks**:
- [ ] Task 0.1.1.1: Document JDK installation (all platforms) - 1 SP
- [ ] Task 0.1.1.2: Create Maven wrapper configuration - 1 SP
- [ ] Task 0.1.1.3: Write IDE setup guide with screenshots - 2 SP
- [ ] Task 0.1.1.4: Configure Husky/pre-commit hooks - 2 SP
- [ ] Task 0.1.1.5: Create .env.example file - 1 SP

**Story Points**: 5

---

### Story 0.1.2: Multi-Module Maven Project Structure 🔴 P0
**As a** developer
**I want** a well-organized multi-module project
**So that** code is properly separated and maintainable

**Acceptance Criteria**:
- [ ] Parent POM with dependency management
- [ ] Module structure created (core, render, server, cli, web)
- [ ] Build executes successfully
- [ ] Inter-module dependencies configured

**Tasks**:
- [ ] Task 0.1.2.1: Create parent POM with version management - 3 SP
- [ ] Task 0.1.2.2: Create gnuplot-core module - 2 SP
- [ ] Task 0.1.2.3: Create gnuplot-render module - 2 SP
- [ ] Task 0.1.2.4: Create gnuplot-server module - 2 SP
- [ ] Task 0.1.2.5: Create gnuplot-cli module - 1 SP
- [ ] Task 0.1.2.6: Create gnuplot-web module (placeholder) - 1 SP
- [ ] Task 0.1.2.7: Configure module dependencies - 2 SP

**Story Points**: 13

---

### Story 0.1.3: CI/CD Pipeline Setup 🔴 P0
**As a** developer
**I want** automated build and test pipeline
**So that** code quality is maintained automatically

**Acceptance Criteria**:
- [ ] GitHub Actions workflow configured
- [ ] Build runs on every PR
- [ ] Tests run automatically
- [ ] Code coverage reports generated
- [ ] Artifacts published on merge to main

**Tasks**:
- [ ] Task 0.1.3.1: Create GitHub Actions workflow file - 3 SP
- [ ] Task 0.1.3.2: Configure Maven build step - 1 SP
- [ ] Task 0.1.3.3: Configure test execution - 2 SP
- [ ] Task 0.1.3.4: Set up JaCoCo for code coverage - 2 SP
- [ ] Task 0.1.3.5: Configure artifact publishing - 2 SP
- [ ] Task 0.1.3.6: Add status badges to README - 1 SP

**Story Points**: 8

---

### Story 0.1.4: Code Quality Tools 🟠 P1
**As a** developer
**I want** automated code quality checks
**So that** code standards are enforced

**Acceptance Criteria**:
- [ ] Checkstyle configured with rules
- [ ] SpotBugs configured
- [ ] SonarQube integration
- [ ] Quality gates block bad code

**Tasks**:
- [ ] Task 0.1.4.1: Configure Checkstyle plugin - 2 SP
- [ ] Task 0.1.4.2: Create custom Checkstyle rules - 3 SP
- [ ] Task 0.1.4.3: Set up SpotBugs - 2 SP
- [ ] Task 0.1.4.4: Configure SonarQube Cloud - 3 SP
- [ ] Task 0.1.4.5: Define quality gates - 2 SP

**Story Points**: 8

---

### Story 0.1.5: Documentation Framework 🟡 P2
**As a** developer
**I want** comprehensive documentation system
**So that** APIs and usage are well documented

**Acceptance Criteria**:
- [ ] JavaDoc configuration
- [ ] API documentation generation
- [ ] GitHub Pages setup for docs
- [ ] README with getting started guide

**Tasks**:
- [ ] Task 0.1.5.1: Configure JavaDoc Maven plugin - 2 SP
- [ ] Task 0.1.5.2: Set up GitHub Pages - 2 SP
- [ ] Task 0.1.5.3: Write comprehensive README - 3 SP
- [ ] Task 0.1.5.4: Create CONTRIBUTING.md - 2 SP
- [ ] Task 0.1.5.5: Create architecture documentation - 3 SP

**Story Points**: 8

---

## Epic 0.2: Test Infrastructure

### Story 0.2.1: Test Framework Setup 🔴 P0
**As a** developer
**I want** a robust test framework
**So that** I can write effective unit and integration tests

**Acceptance Criteria**:
- [ ] JUnit 5 configured
- [ ] AssertJ for assertions
- [ ] Mockito for mocking
- [ ] Test utilities created

**Tasks**:
- [ ] Task 0.2.1.1: Add JUnit 5 dependencies - 1 SP
- [ ] Task 0.2.1.2: Add AssertJ dependencies - 1 SP
- [ ] Task 0.2.1.3: Add Mockito dependencies - 1 SP
- [ ] Task 0.2.1.4: Create test utilities package - 2 SP
- [ ] Task 0.2.1.5: Write example tests - 2 SP

**Story Points**: 5

---

### Story 0.2.2: Test Data Extraction from C Gnuplot 🔴 P0
**As a** developer
**I want** reference test data from C implementation
**So that** I can verify correctness of Java rewrite

**Acceptance Criteria**:
- [ ] Script to compile C gnuplot
- [ ] Script to generate test outputs
- [ ] Test data organized by module
- [ ] At least 100 test cases extracted

**Tasks**:
- [ ] Task 0.2.2.1: Write build script for C gnuplot - 3 SP
- [ ] Task 0.2.2.2: Create test input generator - 5 SP
- [ ] Task 0.2.2.3: Extract mathematical function outputs - 5 SP
- [ ] Task 0.2.2.4: Extract plot coordinate data - 5 SP
- [ ] Task 0.2.2.5: Organize test data files - 2 SP
- [ ] Task 0.2.2.6: Document test data format - 2 SP

**Story Points**: 21

---

### Story 0.2.3: Visual Regression Test Framework 🟠 P1
**As a** developer
**I want** automated visual regression testing
**So that** plot rendering accuracy is maintained

**Acceptance Criteria**:
- [ ] Framework to compare images
- [ ] Baseline images from C gnuplot
- [ ] Automated pixel comparison
- [ ] Diff image generation

**Tasks**:
- [ ] Spike 0.2.3.1: Research image comparison libraries - 3 SP
- [ ] Task 0.2.3.2: Implement image comparison utility - 5 SP
- [ ] Task 0.2.3.3: Generate baseline images from C - 5 SP
- [ ] Task 0.2.3.4: Create test harness - 3 SP
- [ ] Task 0.2.3.5: Document usage - 2 SP

**Story Points**: 13

---

# PHASE 1: CORE MATHEMATICAL ENGINE (Weeks 5-20)

## Epic 1.1: Expression Parser

### Story 1.1.1: ANTLR4 Grammar Definition 🔴 P0
**As a** developer
**I want** a formal grammar for mathematical expressions
**So that** parsing is robust and maintainable

**Acceptance Criteria**:
- [ ] ANTLR4 grammar file created
- [ ] Supports basic arithmetic (+, -, *, /, ^)
- [ ] Supports parentheses and operator precedence
- [ ] Supports function calls
- [ ] Supports variables

**Tasks**:
- [ ] Spike 1.1.1.1: Study C scanner.c and parse.c - 5 SP
- [ ] Task 1.1.1.2: Define lexer rules - 5 SP
- [ ] Task 1.1.1.3: Define parser rules for expressions - 8 SP
- [ ] Task 1.1.1.4: Define parser rules for statements - 5 SP
- [ ] Task 1.1.1.5: Add error recovery rules - 3 SP
- [ ] Task 1.1.1.6: Generate and test parser - 3 SP

**Story Points**: 21

---

### Story 1.1.2: Abstract Syntax Tree (AST) Builder 🔴 P0
**As a** developer
**I want** an AST representation of expressions
**So that** expressions can be analyzed and evaluated

**Acceptance Criteria**:
- [ ] AST node classes defined
- [ ] Visitor pattern implemented
- [ ] ANTLR parse tree converted to AST
- [ ] Unit tests for AST construction

**Tasks**:
- [ ] Task 1.1.2.1: Design AST node hierarchy - 5 SP
- [ ] Task 1.1.2.2: Implement node classes - 8 SP
- [ ] Task 1.1.2.3: Implement ANTLR visitor - 5 SP
- [ ] Task 1.1.2.4: Write AST builder - 5 SP
- [ ] Task 1.1.2.5: Unit tests for AST - 5 SP

**Story Points**: 21

---

### Story 1.1.3: Expression Parser API 🔴 P0
**As a** user of the core library
**I want** a simple API to parse expressions
**So that** I can easily integrate parsing

**Acceptance Criteria**:
- [ ] Parser class with simple parse() method
- [ ] Handles syntax errors gracefully
- [ ] Returns AST or error details
- [ ] Good error messages with line/column

**Tasks**:
- [ ] Task 1.1.3.1: Design Parser API - 3 SP
- [ ] Task 1.1.3.2: Implement Parser class - 5 SP
- [ ] Task 1.1.3.3: Implement error handling - 5 SP
- [ ] Task 1.1.3.4: Write comprehensive tests - 5 SP
- [ ] Task 1.1.3.5: Document API with examples - 3 SP

**Story Points**: 13

---

## Epic 1.2: Expression Evaluator

### Story 1.2.1: AST Interpreter for Basic Arithmetic 🔴 P0
**As a** user
**I want** to evaluate arithmetic expressions
**So that** I can compute mathematical results

**Acceptance Criteria**:
- [ ] Supports +, -, *, /, ^ operators
- [ ] Correct operator precedence
- [ ] Handles integers and doubles
- [ ] Returns computed result

**Tasks**:
- [ ] Task 1.2.1.1: Design evaluator architecture - 3 SP
- [ ] Task 1.2.1.2: Implement arithmetic operators - 5 SP
- [ ] Task 1.2.1.3: Implement power operator - 2 SP
- [ ] Task 1.2.1.4: Handle type coercion - 3 SP
- [ ] Task 1.2.1.5: Unit tests (100+ test cases) - 8 SP

**Story Points**: 13

---

### Story 1.2.2: Variable Support 🔴 P0
**As a** user
**I want** to use variables in expressions
**So that** I can create reusable formulas

**Acceptance Criteria**:
- [ ] Variable assignment (x = 5)
- [ ] Variable reference in expressions
- [ ] Scoped variable context
- [ ] Error on undefined variables

**Tasks**:
- [ ] Task 1.2.2.1: Design variable context - 3 SP
- [ ] Task 1.2.2.2: Implement variable storage - 3 SP
- [ ] Task 1.2.2.3: Implement variable lookup - 2 SP
- [ ] Task 1.2.2.4: Implement assignment - 3 SP
- [ ] Task 1.2.2.5: Unit tests - 5 SP

**Story Points**: 13

---

### Story 1.2.3: Function Call Framework 🔴 P0
**As a** developer
**I want** a framework for mathematical functions
**So that** I can easily add new functions

**Acceptance Criteria**:
- [ ] Function registry
- [ ] Function signature validation
- [ ] Argument type checking
- [ ] Function execution

**Tasks**:
- [ ] Task 1.2.3.1: Design function interface - 3 SP
- [ ] Task 1.2.3.2: Implement function registry - 5 SP
- [ ] Task 1.2.3.3: Implement argument validation - 3 SP
- [ ] Task 1.2.3.4: Implement function calls in evaluator - 3 SP
- [ ] Task 1.2.3.5: Unit tests - 3 SP

**Story Points**: 13

---

### Story 1.2.4: Complex Number Support 🔴 P0
**As a** user
**I want** to work with complex numbers
**So that** I can perform scientific computations

**Acceptance Criteria**:
- [ ] Complex number data type
- [ ] Arithmetic operations on complex
- [ ] Complex-aware functions
- [ ] Imaginary unit support (i or {0,1})

**Tasks**:
- [ ] Task 1.2.4.1: Design Complex class - 3 SP
- [ ] Task 1.2.4.2: Implement Complex arithmetic - 5 SP
- [ ] Task 1.2.4.3: Integrate with evaluator - 3 SP
- [ ] Task 1.2.4.4: Add complex literals - 2 SP
- [ ] Task 1.2.4.5: Unit tests vs C outputs - 5 SP

**Story Points**: 13

---

## Epic 1.3: Mathematical Functions Library

### Story 1.3.1: Standard Math Functions 🔴 P0
**As a** user
**I want** standard mathematical functions
**So that** I can perform common calculations

**Functions**: sin, cos, tan, asin, acos, atan, atan2, sinh, cosh, tanh, exp, log, log10, sqrt, abs, ceil, floor, round

**Acceptance Criteria**:
- [ ] All functions implemented
- [ ] Support real and complex inputs
- [ ] Results match C gnuplot within 1e-12
- [ ] Comprehensive tests

**Tasks**:
- [ ] Task 1.3.1.1: Implement trigonometric functions - 5 SP
- [ ] Task 1.3.1.2: Implement hyperbolic functions - 3 SP
- [ ] Task 1.3.1.3: Implement logarithmic/exp functions - 3 SP
- [ ] Task 1.3.1.4: Implement basic math functions - 3 SP
- [ ] Task 1.3.1.5: Add complex support - 5 SP
- [ ] Task 1.3.1.6: Test against C outputs - 8 SP

**Story Points**: 21

---

### Story 1.3.2: Special Functions (Gamma, Beta) 🔴 P0
**As a** user
**I want** special mathematical functions
**So that** I can perform advanced statistical computations

**Functions**: gamma, lgamma, igamma, beta, ibeta

**Acceptance Criteria**:
- [ ] All functions implemented
- [ ] Use Apache Commons Math where available
- [ ] Port from C where necessary
- [ ] Match C accuracy

**Tasks**:
- [ ] Spike 1.3.2.1: Evaluate Apache Commons Math coverage - 2 SP
- [ ] Task 1.3.2.2: Implement gamma functions - 5 SP
- [ ] Task 1.3.2.3: Implement beta functions - 5 SP
- [ ] Task 1.3.2.4: Implement incomplete gamma/beta - 8 SP
- [ ] Task 1.3.2.5: Test against C outputs - 5 SP

**Story Points**: 21

---

### Story 1.3.3: Bessel Functions 🔴 P0
**As a** user
**I want** Bessel functions of the first and second kind
**So that** I can solve physics and engineering problems

**Functions**: besj0, besj1, besjn, besy0, besy1, besyn, besi0, besi1, besin

**Acceptance Criteria**:
- [ ] All Bessel functions implemented
- [ ] Use Apache Commons Math
- [ ] Match C accuracy within 1e-10
- [ ] Handle edge cases

**Tasks**:
- [ ] Task 1.3.3.1: Implement Bessel J functions - 5 SP
- [ ] Task 1.3.3.2: Implement Bessel Y functions - 5 SP
- [ ] Task 1.3.3.3: Implement modified Bessel I - 5 SP
- [ ] Task 1.3.3.4: Test against C outputs - 5 SP

**Story Points**: 13

---

### Story 1.3.4: Error Functions and Complementary 🔴 P0
**As a** user
**I want** error functions
**So that** I can work with probability distributions

**Functions**: erf, erfc, inverf, inverfc, voigt, faddeeva

**Acceptance Criteria**:
- [ ] All error functions implemented
- [ ] Complex error functions (cerf)
- [ ] Voigt profile function
- [ ] Match C accuracy

**Tasks**:
- [ ] Task 1.3.4.1: Implement erf/erfc - 3 SP
- [ ] Task 1.3.4.2: Implement inverse error functions - 5 SP
- [ ] Task 1.3.4.3: Implement complex error functions - 8 SP
- [ ] Task 1.3.4.4: Implement Voigt profile - 5 SP
- [ ] Task 1.3.4.5: Test against C outputs - 5 SP

**Story Points**: 21

---

### Story 1.3.5: Elliptic Integrals 🟠 P1
**As a** user
**I want** elliptic integral functions
**So that** I can solve certain differential equations

**Functions**: EllipticK, EllipticE, EllipticPi

**Acceptance Criteria**:
- [ ] Complete and incomplete elliptic integrals
- [ ] Match C accuracy
- [ ] Handle edge cases

**Tasks**:
- [ ] Task 1.3.5.1: Research implementation approach - 3 SP
- [ ] Task 1.3.5.2: Implement EllipticK - 5 SP
- [ ] Task 1.3.5.3: Implement EllipticE - 5 SP
- [ ] Task 1.3.5.4: Implement EllipticPi - 5 SP
- [ ] Task 1.3.5.5: Test against C outputs - 3 SP

**Story Points**: 13

---

### Story 1.3.6: Airy Functions 🟠 P1
**As a** user
**I want** Airy functions
**So that** I can solve wave equation problems

**Functions**: Ai, Bi, Ai', Bi'

**Acceptance Criteria**:
- [ ] Airy functions and derivatives
- [ ] Complex arguments supported
- [ ] Match C accuracy

**Tasks**:
- [ ] Task 1.3.6.1: Study C AMOS library port - 3 SP
- [ ] Task 1.3.6.2: Implement Airy Ai - 5 SP
- [ ] Task 1.3.6.3: Implement Airy Bi - 5 SP
- [ ] Task 1.3.6.4: Implement derivatives - 3 SP
- [ ] Task 1.3.6.5: Test against C outputs - 3 SP

**Story Points**: 13

---

### Story 1.3.7: Statistical Functions 🟠 P1
**As a** user
**I want** statistical distribution functions
**So that** I can perform statistical analysis

**Functions**: norm, invnorm, chisquare, students_t, f_dist

**Acceptance Criteria**:
- [ ] PDF, CDF, and inverse CDF
- [ ] Common distributions
- [ ] Use Apache Commons Math

**Tasks**:
- [ ] Task 1.3.7.1: Implement normal distribution - 3 SP
- [ ] Task 1.3.7.2: Implement chi-square - 3 SP
- [ ] Task 1.3.7.3: Implement Student's t - 3 SP
- [ ] Task 1.3.7.4: Implement F distribution - 3 SP
- [ ] Task 1.3.7.5: Test against C outputs - 3 SP

**Story Points**: 13

---

### Story 1.3.8: Random Number Functions 🟡 P2
**As a** user
**I want** random number generation
**So that** I can create stochastic simulations

**Functions**: rand, random, sgrand (set seed)

**Acceptance Criteria**:
- [ ] Uniform random [0,1]
- [ ] Seeded randomness
- [ ] Thread-safe

**Tasks**:
- [ ] Task 1.3.8.1: Implement random functions - 3 SP
- [ ] Task 1.3.8.2: Implement seeding - 2 SP
- [ ] Task 1.3.8.3: Make thread-safe - 2 SP
- [ ] Task 1.3.8.4: Unit tests - 2 SP

**Story Points**: 8

---

### Story 1.3.9: String Functions 🟡 P2
**As a** user
**I want** string manipulation functions
**So that** I can work with text data

**Functions**: strlen, substr, sprintf, strstrt, trim, word, words

**Acceptance Criteria**:
- [ ] All string functions working
- [ ] Unicode support
- [ ] Match C behavior

**Tasks**:
- [ ] Task 1.3.9.1: Implement strlen, substr - 2 SP
- [ ] Task 1.3.9.2: Implement sprintf - 5 SP
- [ ] Task 1.3.9.3: Implement string search - 2 SP
- [ ] Task 1.3.9.4: Implement trim, word - 3 SP
- [ ] Task 1.3.9.5: Test with Unicode - 3 SP

**Story Points**: 13

---

## Epic 1.4: Core Engine Integration

### Story 1.4.1: Performance Optimization 🟠 P1
**As a** developer
**I want** optimized expression evaluation
**So that** performance is acceptable

**Acceptance Criteria**:
- [ ] Benchmark suite created
- [ ] Performance within 2x of C
- [ ] Hot paths optimized
- [ ] JMH benchmarks

**Tasks**:
- [ ] Task 1.4.1.1: Create JMH benchmark suite - 5 SP
- [ ] Task 1.4.1.2: Profile evaluator - 3 SP
- [ ] Task 1.4.1.3: Optimize hot paths - 8 SP
- [ ] Task 1.4.1.4: Add caching where appropriate - 5 SP
- [ ] Task 1.4.1.5: Document performance - 2 SP

**Story Points**: 21

---

### Story 1.4.2: Error Handling and Messages 🔴 P0
**As a** user
**I want** clear error messages
**So that** I can fix problems in my expressions

**Acceptance Criteria**:
- [ ] Helpful error messages
- [ ] Line and column numbers
- [ ] Suggestions for common mistakes
- [ ] Exception hierarchy

**Tasks**:
- [ ] Task 1.4.2.1: Design exception hierarchy - 3 SP
- [ ] Task 1.4.2.2: Implement error messages - 5 SP
- [ ] Task 1.4.2.3: Add error recovery - 3 SP
- [ ] Task 1.4.2.4: Add suggestions - 3 SP
- [ ] Task 1.4.2.5: Test error scenarios - 3 SP

**Story Points**: 13

---

### Story 1.4.3: Core Module Documentation 🟠 P1
**As a** developer using the core module
**I want** comprehensive documentation
**So that** I can integrate it easily

**Acceptance Criteria**:
- [ ] All public APIs documented
- [ ] Usage examples provided
- [ ] API reference generated
- [ ] Tutorial/guide written

**Tasks**:
- [ ] Task 1.4.3.1: JavaDoc all public APIs - 8 SP
- [ ] Task 1.4.3.2: Write usage examples - 5 SP
- [ ] Task 1.4.3.3: Write tutorial guide - 5 SP
- [ ] Task 1.4.3.4: Generate API docs - 2 SP

**Story Points**: 13

---

# PHASE 2: DATA PROCESSING LAYER (Weeks 21-32)

## Epic 2.1: Data Import

### Story 2.1.1: CSV File Reader 🔴 P0
**As a** user
**I want** to import CSV data
**So that** I can plot tabular data

**Acceptance Criteria**:
- [ ] Reads standard CSV files
- [ ] Handles quoted fields
- [ ] Configurable delimiter
- [ ] Header row support
- [ ] Streaming for large files

**Tasks**:
- [ ] Task 2.1.1.1: Design CSV reader API - 3 SP
- [ ] Task 2.1.1.2: Implement CSV parser - 5 SP
- [ ] Task 2.1.1.3: Add streaming support - 5 SP
- [ ] Task 2.1.1.4: Handle edge cases - 3 SP
- [ ] Task 2.1.1.5: Unit tests - 5 SP

**Story Points**: 13

---

### Story 2.1.2: JSON Data Reader 🔴 P0
**As a** user
**I want** to import JSON data
**So that** I can plot structured data

**Acceptance Criteria**:
- [ ] Reads JSON files
- [ ] JSONPath for data extraction
- [ ] Nested object support
- [ ] Array handling

**Tasks**:
- [ ] Task 2.1.2.1: Choose JSON library (Jackson) - 1 SP
- [ ] Task 2.1.2.2: Implement JSON reader - 3 SP
- [ ] Task 2.1.2.3: Implement JSONPath extraction - 5 SP
- [ ] Task 2.1.2.4: Unit tests - 3 SP

**Story Points**: 8

---

### Story 2.1.3: Binary Data Reader 🟡 P2
**As a** user
**I want** to import binary data files
**So that** I can plot scientific instrument data

**Acceptance Criteria**:
- [ ] Configurable binary format
- [ ] Endianness support
- [ ] Multiple data types
- [ ] Match C gnuplot binary format

**Tasks**:
- [ ] Spike 2.1.3.1: Study C binary format spec - 3 SP
- [ ] Task 2.1.3.2: Design binary reader - 5 SP
- [ ] Task 2.1.3.3: Implement reader - 8 SP
- [ ] Task 2.1.3.4: Test with C-generated files - 5 SP

**Story Points**: 13

---

### Story 2.1.4: Excel File Support 🟡 P2
**As a** user
**I want** to import Excel files
**So that** I don't need to export to CSV first

**Acceptance Criteria**:
- [ ] Read .xlsx files
- [ ] Multiple sheets support
- [ ] Cell range selection
- [ ] Formula evaluation optional

**Tasks**:
- [ ] Task 2.1.4.1: Add Apache POI dependency - 1 SP
- [ ] Task 2.1.4.2: Implement Excel reader - 5 SP
- [ ] Task 2.1.4.3: Add sheet selection - 3 SP
- [ ] Task 2.1.4.4: Unit tests - 3 SP

**Story Points**: 8

---

### Story 2.1.5: Data Source Abstraction 🔴 P0
**As a** developer
**I want** a unified data source interface
**So that** all readers work the same way

**Acceptance Criteria**:
- [ ] DataSource interface defined
- [ ] All readers implement interface
- [ ] Factory for reader creation
- [ ] Pluggable reader system

**Tasks**:
- [ ] Task 2.1.5.1: Design DataSource interface - 3 SP
- [ ] Task 2.1.5.2: Refactor readers to interface - 5 SP
- [ ] Task 2.1.5.3: Implement factory - 3 SP
- [ ] Task 2.1.5.4: Document extension points - 2 SP

**Story Points**: 8

---

## Epic 2.2: Data Transformation

### Story 2.2.1: Data Filtering and Selection 🔴 P0
**As a** user
**I want** to filter and select data
**So that** I can plot subsets of data

**Acceptance Criteria**:
- [ ] Row filtering by condition
- [ ] Column selection
- [ ] Row range selection
- [ ] Expression-based filtering

**Tasks**:
- [ ] Task 2.2.1.1: Design filter API - 3 SP
- [ ] Task 2.2.1.2: Implement row filters - 5 SP
- [ ] Task 2.2.1.3: Implement column selection - 3 SP
- [ ] Task 2.2.1.4: Implement expression filters - 5 SP
- [ ] Task 2.2.1.5: Unit tests - 5 SP

**Story Points**: 13

---

### Story 2.2.2: Linear Interpolation 🔴 P0
**As a** user
**I want** linear interpolation of data
**So that** I can smooth curves

**Acceptance Criteria**:
- [ ] Linear interpolation implemented
- [ ] Handles missing data
- [ ] Configurable sampling
- [ ] Match C behavior

**Tasks**:
- [ ] Task 2.2.2.1: Study C interpol.c - 3 SP
- [ ] Task 2.2.2.2: Implement linear interpolation - 5 SP
- [ ] Task 2.2.2.3: Handle edge cases - 3 SP
- [ ] Task 2.2.2.4: Test vs C outputs - 5 SP

**Story Points**: 13

---

### Story 2.2.3: Spline Interpolation 🔴 P0
**As a** user
**I want** spline interpolation
**So that** I can create smooth curves

**Acceptance Criteria**:
- [ ] Cubic spline interpolation
- [ ] Natural, clamped, and periodic boundary conditions
- [ ] Akima spline
- [ ] Bezier spline

**Tasks**:
- [ ] Task 2.2.3.1: Study C spline algorithms - 5 SP
- [ ] Task 2.2.3.2: Implement cubic spline - 8 SP
- [ ] Task 2.2.3.3: Implement Akima spline - 5 SP
- [ ] Task 2.2.3.4: Implement Bezier spline - 5 SP
- [ ] Task 2.2.3.5: Test vs C outputs - 8 SP

**Story Points**: 21

---

### Story 2.2.4: Smoothing Algorithms 🟠 P1
**As a** user
**I want** data smoothing
**So that** I can reduce noise

**Acceptance Criteria**:
- [ ] Moving average
- [ ] Gaussian smoothing
- [ ] Savitzky-Golay filter
- [ ] Configurable window size

**Tasks**:
- [ ] Task 2.2.4.1: Implement moving average - 3 SP
- [ ] Task 2.2.4.2: Implement Gaussian smoothing - 5 SP
- [ ] Task 2.2.4.3: Implement Savitzky-Golay - 8 SP
- [ ] Task 2.2.4.4: Test vs C outputs - 5 SP

**Story Points**: 13

---

### Story 2.2.5: Data Binning and Histograms 🟠 P1
**As a** user
**I want** to bin data into histograms
**So that** I can visualize distributions

**Acceptance Criteria**:
- [ ] Automatic bin size calculation
- [ ] Manual bin specification
- [ ] Cumulative histograms
- [ ] Normalized histograms

**Tasks**:
- [ ] Task 2.2.5.1: Implement binning algorithm - 5 SP
- [ ] Task 2.2.5.2: Implement histogram calculation - 5 SP
- [ ] Task 2.2.5.3: Add cumulative option - 3 SP
- [ ] Task 2.2.5.4: Add normalization - 2 SP
- [ ] Task 2.2.5.5: Unit tests - 3 SP

**Story Points**: 13

---

## Epic 2.3: Statistical Analysis

### Story 2.3.1: Descriptive Statistics 🔴 P0
**As a** user
**I want** to calculate statistics on data
**So that** I can understand my data

**Acceptance Criteria**:
- [ ] Mean, median, mode
- [ ] Standard deviation, variance
- [ ] Min, max, quartiles
- [ ] Correlation coefficient

**Tasks**:
- [ ] Task 2.3.1.1: Implement basic statistics - 5 SP
- [ ] Task 2.3.1.2: Implement variance/stddev - 3 SP
- [ ] Task 2.3.1.3: Implement quartiles - 3 SP
- [ ] Task 2.3.1.4: Implement correlation - 3 SP
- [ ] Task 2.3.1.5: Test vs C stats - 5 SP

**Story Points**: 13

---

### Story 2.3.2: Linear Regression 🟠 P1
**As a** user
**I want** to fit linear models
**So that** I can analyze trends

**Acceptance Criteria**:
- [ ] Simple linear regression
- [ ] R-squared calculation
- [ ] Confidence intervals
- [ ] Residual analysis

**Tasks**:
- [ ] Task 2.3.2.1: Implement least squares - 5 SP
- [ ] Task 2.3.2.2: Calculate R-squared - 2 SP
- [ ] Task 2.3.2.3: Calculate confidence intervals - 5 SP
- [ ] Task 2.3.2.4: Unit tests - 3 SP

**Story Points**: 13

---

### Story 2.3.3: Non-Linear Fitting 🟡 P2
**As a** user
**I want** to fit non-linear models
**So that** I can analyze complex relationships

**Acceptance Criteria**:
- [ ] Levenberg-Marquardt algorithm
- [ ] Custom function fitting
- [ ] Parameter bounds
- [ ] Convergence criteria

**Tasks**:
- [ ] Spike 2.3.3.1: Study C fit.c (2,449 lines) - 8 SP
- [ ] Task 2.3.3.2: Implement LM algorithm - 13 SP
- [ ] Task 2.3.3.3: Add parameter constraints - 5 SP
- [ ] Task 2.3.3.4: Test vs C fit results - 8 SP

**Story Points**: 21

---

## Epic 2.4: Coordinate Systems

### Story 2.4.1: Cartesian Coordinates 🔴 P0
**As a** developer
**I want** Cartesian coordinate system support
**So that** standard plots work

**Acceptance Criteria**:
- [ ] 2D Cartesian coordinates
- [ ] 3D Cartesian coordinates
- [ ] Coordinate transformation
- [ ] Axis mapping

**Tasks**:
- [ ] Task 2.4.1.1: Design coordinate system API - 5 SP
- [ ] Task 2.4.1.2: Implement 2D Cartesian - 3 SP
- [ ] Task 2.4.1.3: Implement 3D Cartesian - 5 SP
- [ ] Task 2.4.1.4: Unit tests - 5 SP

**Story Points**: 13

---

### Story 2.4.2: Polar Coordinates 🔴 P0
**As a** user
**I want** polar coordinate plotting
**So that** I can create polar plots

**Acceptance Criteria**:
- [ ] Polar to Cartesian conversion
- [ ] Angle units (degrees/radians)
- [ ] Radial scaling
- [ ] Angular wrapping

**Tasks**:
- [ ] Task 2.4.2.1: Implement polar coordinates - 5 SP
- [ ] Task 2.4.2.2: Add unit conversion - 2 SP
- [ ] Task 2.4.2.3: Handle angle wrapping - 3 SP
- [ ] Task 2.4.2.4: Unit tests - 3 SP

**Story Points**: 8

---

### Story 2.4.3: Cylindrical and Spherical Coordinates 🟡 P2
**As a** user
**I want** cylindrical and spherical coordinates
**So that** I can plot 3D data naturally

**Acceptance Criteria**:
- [ ] Cylindrical coordinate system
- [ ] Spherical coordinate system
- [ ] Conversion to Cartesian
- [ ] Proper axis labels

**Tasks**:
- [ ] Task 2.4.3.1: Implement cylindrical - 5 SP
- [ ] Task 2.4.3.2: Implement spherical - 5 SP
- [ ] Task 2.4.3.3: Test conversions - 3 SP

**Story Points**: 8

---

### Story 2.4.4: Map Projections 🟢 P3
**As a** user
**I want** geographic map projections
**So that** I can plot geographic data

**Acceptance Criteria**:
- [ ] Mercator projection
- [ ] Equirectangular projection
- [ ] Coordinate transformation
- [ ] Lat/long support

**Tasks**:
- [ ] Task 2.4.4.1: Research projection libraries - 3 SP
- [ ] Task 2.4.4.2: Implement Mercator - 5 SP
- [ ] Task 2.4.4.3: Implement equirectangular - 3 SP
- [ ] Task 2.4.4.4: Test with real data - 3 SP

**Story Points**: 13

---

# PHASE 3: RENDERING ENGINE (Weeks 33-48)

## Epic 3.1: Rendering Infrastructure

### Story 3.1.1: Rendering Pipeline Architecture 🔴 P0
**As a** developer
**I want** a modular rendering pipeline
**So that** multiple output formats are supported

**Acceptance Criteria**:
- [ ] Renderer interface defined
- [ ] Scene graph representation
- [ ] Format-agnostic intermediate representation
- [ ] Pluggable renderer system

**Tasks**:
- [ ] Task 3.1.1.1: Design rendering architecture - 8 SP
- [ ] Task 3.1.1.2: Define renderer interfaces - 5 SP
- [ ] Task 3.1.1.3: Implement scene graph - 8 SP
- [ ] Task 3.1.1.4: Document architecture - 3 SP

**Story Points**: 21

---

### Story 3.1.2: Axis Rendering System 🔴 P0
**As a** developer
**I want** a flexible axis rendering system
**So that** all plot types can have proper axes

**Acceptance Criteria**:
- [ ] Linear axes
- [ ] Logarithmic axes
- [ ] Time axes
- [ ] Tick mark generation
- [ ] Label formatting

**Tasks**:
- [ ] Task 3.1.2.1: Study C axis.c (2,999 lines) - 5 SP
- [ ] Task 3.1.2.2: Design axis API - 5 SP
- [ ] Task 3.1.2.3: Implement tick generation - 8 SP
- [ ] Task 3.1.2.4: Implement label formatting - 5 SP
- [ ] Task 3.1.2.5: Test against C outputs - 5 SP

**Story Points**: 21

---

### Story 3.1.3: Color Palette System 🔴 P0
**As a** user
**I want** flexible color palettes
**So that** plots are visually appealing

**Acceptance Criteria**:
- [ ] Named color palettes
- [ ] Custom color palettes
- [ ] Gradient generation
- [ ] Colormap functions
- [ ] Alpha channel support

**Tasks**:
- [ ] Task 3.1.3.1: Study C color.c - 3 SP
- [ ] Task 3.1.3.2: Implement color system - 5 SP
- [ ] Task 3.1.3.3: Add named palettes - 3 SP
- [ ] Task 3.1.3.4: Implement gradients - 3 SP
- [ ] Task 3.1.3.5: Test color accuracy - 3 SP

**Story Points**: 13

---

### Story 3.1.4: Text Rendering and Fonts 🔴 P0
**As a** developer
**I want** proper text rendering
**So that** labels and titles display correctly

**Acceptance Criteria**:
- [ ] Font loading and management
- [ ] Text measurement
- [ ] Text rotation
- [ ] Unicode support
- [ ] Math notation support

**Tasks**:
- [ ] Task 3.1.4.1: Implement font system - 5 SP
- [ ] Task 3.1.4.2: Add text rendering - 5 SP
- [ ] Task 3.1.4.3: Support rotation - 3 SP
- [ ] Task 3.1.4.4: Add Unicode support - 5 SP
- [ ] Task 3.1.4.5: Add math notation (LaTeX subset) - 8 SP

**Story Points**: 21

---

## Epic 3.2: 2D Plot Rendering

### Story 3.2.1: Line Plot Renderer 🔴 P0
**As a** user
**I want** to create line plots
**So that** I can visualize continuous data

**Acceptance Criteria**:
- [ ] Draw polylines
- [ ] Line styles (solid, dashed, dotted)
- [ ] Line width control
- [ ] Color support
- [ ] Clipping to plot area

**Tasks**:
- [ ] Task 3.2.1.1: Implement line drawing - 5 SP
- [ ] Task 3.2.1.2: Add line styles - 3 SP
- [ ] Task 3.2.1.3: Implement clipping - 5 SP
- [ ] Task 3.2.1.4: Visual regression tests - 5 SP

**Story Points**: 13

---

### Story 3.2.2: Scatter Plot Renderer 🔴 P0
**As a** user
**I want** to create scatter plots
**So that** I can visualize discrete data points

**Acceptance Criteria**:
- [ ] Multiple point styles
- [ ] Variable point size
- [ ] Point colors
- [ ] Filled/unfilled points

**Tasks**:
- [ ] Task 3.2.2.1: Implement point drawing - 5 SP
- [ ] Task 3.2.2.2: Add point styles (circle, square, etc.) - 5 SP
- [ ] Task 3.2.2.3: Add variable sizing - 3 SP
- [ ] Task 3.2.2.4: Visual tests - 3 SP

**Story Points**: 13

---

### Story 3.2.3: Bar Chart Renderer 🔴 P0
**As a** user
**I want** to create bar charts
**So that** I can compare categorical data

**Acceptance Criteria**:
- [ ] Vertical and horizontal bars
- [ ] Grouped bars
- [ ] Stacked bars
- [ ] Bar width control
- [ ] Error bars

**Tasks**:
- [ ] Task 3.2.3.1: Implement basic bars - 5 SP
- [ ] Task 3.2.3.2: Add grouping - 5 SP
- [ ] Task 3.2.3.3: Add stacking - 5 SP
- [ ] Task 3.2.3.4: Add error bars - 3 SP
- [ ] Task 3.2.3.5: Visual tests - 3 SP

**Story Points**: 13

---

### Story 3.2.4: Area/Fill Renderer 🟠 P1
**As a** user
**I want** filled area plots
**So that** I can show regions and distributions

**Acceptance Criteria**:
- [ ] Fill between curves
- [ ] Fill to axis
- [ ] Fill patterns
- [ ] Transparency support

**Tasks**:
- [ ] Task 3.2.4.1: Implement fill between - 5 SP
- [ ] Task 3.2.4.2: Implement fill to axis - 3 SP
- [ ] Task 3.2.4.3: Add patterns - 5 SP
- [ ] Task 3.2.4.4: Visual tests - 3 SP

**Story Points**: 13

---

### Story 3.2.5: Heatmap Renderer 🟠 P1
**As a** user
**I want** to create heatmaps
**So that** I can visualize 2D data

**Acceptance Criteria**:
- [ ] Color mapping for values
- [ ] Interpolation options
- [ ] Colorbar legend
- [ ] Missing data handling

**Tasks**:
- [ ] Task 3.2.5.1: Implement heatmap rendering - 8 SP
- [ ] Task 3.2.5.2: Add interpolation - 5 SP
- [ ] Task 3.2.5.3: Add colorbar - 5 SP
- [ ] Task 3.2.5.4: Visual tests - 3 SP

**Story Points**: 13

---

### Story 3.2.6: Contour Plot Renderer 🟠 P1
**As a** user
**I want** contour plots
**So that** I can visualize 3D data in 2D

**Acceptance Criteria**:
- [ ] Contour line generation
- [ ] Filled contours
- [ ] Contour labels
- [ ] Multiple contour levels

**Tasks**:
- [ ] Task 3.2.6.1: Study C contour.c algorithms - 5 SP
- [ ] Task 3.2.6.2: Implement marching squares - 8 SP
- [ ] Task 3.2.6.3: Add contour lines - 5 SP
- [ ] Task 3.2.6.4: Add filled contours - 5 SP
- [ ] Task 3.2.6.5: Add labels - 3 SP
- [ ] Task 3.2.6.6: Visual tests - 5 SP

**Story Points**: 21

---

### Story 3.2.7: Box Plot Renderer 🟡 P2
**As a** user
**I want** box plots
**So that** I can visualize statistical distributions

**Acceptance Criteria**:
- [ ] Box with quartiles
- [ ] Whiskers
- [ ] Outliers
- [ ] Notched boxes option

**Tasks**:
- [ ] Task 3.2.7.1: Implement box rendering - 5 SP
- [ ] Task 3.2.7.2: Add whiskers and outliers - 3 SP
- [ ] Task 3.2.7.3: Add notches - 2 SP
- [ ] Task 3.2.7.4: Visual tests - 2 SP

**Story Points**: 8

---

### Story 3.2.8: Violin Plot Renderer 🟡 P2
**As a** user
**I want** violin plots
**So that** I can show full distributions

**Acceptance Criteria**:
- [ ] KDE for distribution shape
- [ ] Box plot overlay option
- [ ] Symmetry options

**Tasks**:
- [ ] Task 3.2.8.1: Implement KDE - 8 SP
- [ ] Task 3.2.8.2: Render violin shape - 5 SP
- [ ] Task 3.2.8.3: Add box overlay - 2 SP
- [ ] Task 3.2.8.4: Visual tests - 2 SP

**Story Points**: 13

---

## Epic 3.3: 3D Plot Rendering

### Story 3.3.1: 3D Rendering Setup (JOGL/JavaFX) 🔴 P0
**As a** developer
**I want** 3D rendering capability
**So that** 3D plots can be created

**Acceptance Criteria**:
- [ ] JOGL or JavaFX 3D configured
- [ ] Camera system
- [ ] Lighting system
- [ ] Basic primitives (lines, triangles)

**Tasks**:
- [ ] Spike 3.3.1.1: Evaluate JOGL vs JavaFX 3D - 5 SP
- [ ] Task 3.3.1.2: Set up 3D framework - 8 SP
- [ ] Task 3.3.1.3: Implement camera - 5 SP
- [ ] Task 3.3.1.4: Implement lighting - 5 SP
- [ ] Task 3.3.1.5: Test basic rendering - 3 SP

**Story Points**: 21

---

### Story 3.3.2: 3D Surface Plot Renderer 🔴 P0
**As a** user
**I want** 3D surface plots
**So that** I can visualize functions of two variables

**Acceptance Criteria**:
- [ ] Mesh surface rendering
- [ ] Color mapping
- [ ] Hidden line removal
- [ ] Transparency support

**Tasks**:
- [ ] Task 3.3.2.1: Study C graph3d.c (4,591 lines) - 8 SP
- [ ] Task 3.3.2.2: Implement mesh generation - 8 SP
- [ ] Task 3.3.2.3: Add color mapping - 5 SP
- [ ] Task 3.3.2.4: Implement hidden surface - 8 SP
- [ ] Task 3.3.2.5: Visual tests - 5 SP

**Story Points**: 21

---

### Story 3.3.3: 3D Line/Scatter Plot Renderer 🔴 P0
**As a** user
**I want** 3D line and scatter plots
**So that** I can visualize 3D trajectories

**Acceptance Criteria**:
- [ ] 3D polylines
- [ ] 3D point clouds
- [ ] Depth sorting
- [ ] Proper perspective

**Tasks**:
- [ ] Task 3.3.3.1: Implement 3D lines - 5 SP
- [ ] Task 3.3.3.2: Implement 3D points - 3 SP
- [ ] Task 3.3.3.3: Add depth sorting - 5 SP
- [ ] Task 3.3.3.4: Visual tests - 3 SP

**Story Points**: 13

---

### Story 3.3.4: Isosurface Rendering 🟠 P1
**As a** user
**I want** isosurface plots
**So that** I can visualize 3D volumetric data

**Acceptance Criteria**:
- [ ] Marching cubes algorithm
- [ ] Multiple isosurfaces
- [ ] Transparency
- [ ] Smooth shading

**Tasks**:
- [ ] Task 3.3.4.1: Study C marching_cubes.h - 5 SP
- [ ] Task 3.3.4.2: Implement marching cubes - 13 SP
- [ ] Task 3.3.4.3: Add smooth shading - 5 SP
- [ ] Task 3.3.4.4: Visual tests - 5 SP

**Story Points**: 21

---

### Story 3.3.5: Voxel Grid Rendering 🟡 P2
**As a** user
**I want** voxel visualization
**So that** I can display 3D gridded data

**Acceptance Criteria**:
- [ ] Voxel grid structure
- [ ] Color per voxel
- [ ] Transparency
- [ ] Efficient rendering

**Tasks**:
- [ ] Task 3.3.5.1: Study C voxelgrid.c - 5 SP
- [ ] Task 3.3.5.2: Implement voxel structure - 5 SP
- [ ] Task 3.3.5.3: Implement rendering - 8 SP
- [ ] Task 3.3.5.4: Optimize performance - 5 SP
- [ ] Task 3.3.5.5: Visual tests - 3 SP

**Story Points**: 21

---

## Epic 3.4: Output Formats

### Story 3.4.1: SVG Export 🔴 P0
**As a** user
**I want** to export plots as SVG
**So that** I have vector graphics

**Acceptance Criteria**:
- [ ] 2D plots to SVG
- [ ] Text preserved as text
- [ ] Proper styling
- [ ] Clean SVG output

**Tasks**:
- [ ] Task 3.4.1.1: Implement SVG renderer - 8 SP
- [ ] Task 3.4.1.2: Handle text rendering - 3 SP
- [ ] Task 3.4.1.3: Optimize output size - 3 SP
- [ ] Task 3.4.1.4: Test with various plots - 5 SP

**Story Points**: 13

---

### Story 3.4.2: PNG Export 🔴 P0
**As a** user
**I want** to export plots as PNG
**So that** I can embed in documents

**Acceptance Criteria**:
- [ ] Configurable resolution
- [ ] Anti-aliasing
- [ ] Transparent background option
- [ ] DPI settings

**Tasks**:
- [ ] Task 3.4.2.1: Implement PNG renderer - 5 SP
- [ ] Task 3.4.2.2: Add resolution control - 2 SP
- [ ] Task 3.4.2.3: Add anti-aliasing - 3 SP
- [ ] Task 3.4.2.4: Test quality - 3 SP

**Story Points**: 8

---

### Story 3.4.3: PDF Export 🔴 P0
**As a** user
**I want** to export plots as PDF
**So that** I can include in publications

**Acceptance Criteria**:
- [ ] Vector PDF output
- [ ] Embedded fonts
- [ ] Multi-page support
- [ ] PDF/A compliance option

**Tasks**:
- [ ] Task 3.4.3.1: Choose PDF library (iText/PDFBox) - 2 SP
- [ ] Task 3.4.3.2: Implement PDF renderer - 8 SP
- [ ] Task 3.4.3.3: Handle fonts - 5 SP
- [ ] Task 3.4.3.4: Test output quality - 3 SP

**Story Points**: 13

---

### Story 3.4.4: Interactive HTML Export 🟡 P2
**As a** user
**I want** interactive HTML plots
**So that** I can share on the web

**Acceptance Criteria**:
- [ ] Self-contained HTML file
- [ ] Zoom/pan functionality
- [ ] Data tooltips
- [ ] Responsive design

**Tasks**:
- [ ] Task 3.4.4.1: Choose JS library (Plotly/D3) - 2 SP
- [ ] Task 3.4.4.2: Implement HTML export - 8 SP
- [ ] Task 3.4.4.3: Add interactivity - 5 SP
- [ ] Task 3.4.4.4: Test in browsers - 3 SP

**Story Points**: 13

---

## Epic 3.5: Layout and Composition

### Story 3.5.1: Multi-Plot Layouts 🔴 P0
**As a** user
**I want** multiple plots in one figure
**So that** I can create dashboards

**Acceptance Criteria**:
- [ ] Grid layout
- [ ] Custom positioning
- [ ] Shared axes
- [ ] Independent axes

**Tasks**:
- [ ] Task 3.5.1.1: Design layout system - 5 SP
- [ ] Task 3.5.1.2: Implement grid layout - 5 SP
- [ ] Task 3.5.1.3: Implement custom positioning - 5 SP
- [ ] Task 3.5.1.4: Handle axis sharing - 5 SP
- [ ] Task 3.5.1.5: Visual tests - 3 SP

**Story Points**: 21

---

### Story 3.5.2: Legend System 🔴 P0
**As a** user
**I want** configurable legends
**So that** plot elements are identified

**Acceptance Criteria**:
- [ ] Automatic legend generation
- [ ] Custom positioning
- [ ] Multi-column legends
- [ ] Legend styling

**Tasks**:
- [ ] Task 3.5.2.1: Implement legend generation - 5 SP
- [ ] Task 3.5.2.2: Add positioning options - 3 SP
- [ ] Task 3.5.2.3: Add styling options - 3 SP
- [ ] Task 3.5.2.4: Visual tests - 2 SP

**Story Points**: 8

---

### Story 3.5.3: Annotations and Shapes 🟠 P1
**As a** user
**I want** to add annotations to plots
**So that** I can highlight features

**Acceptance Criteria**:
- [ ] Text annotations
- [ ] Arrows
- [ ] Shapes (rectangles, circles, polygons)
- [ ] Lines and curves

**Tasks**:
- [ ] Task 3.5.3.1: Implement text annotations - 3 SP
- [ ] Task 3.5.3.2: Implement arrows - 3 SP
- [ ] Task 3.5.3.3: Implement shapes - 5 SP
- [ ] Task 3.5.3.4: Visual tests - 2 SP

**Story Points**: 8

---

# PHASE 4: BACKEND SERVER (Weeks 33-48, parallel with Phase 3)

## Epic 4.1: Spring Boot Application

### Story 4.1.1: Spring Boot Project Setup 🔴 P0
**As a** developer
**I want** a Spring Boot application
**So that** I can build REST APIs

**Acceptance Criteria**:
- [ ] Spring Boot 3.2 project created
- [ ] Security configured
- [ ] Database configured
- [ ] Application runs

**Tasks**:
- [ ] Task 4.1.1.1: Create Spring Boot project - 2 SP
- [ ] Task 4.1.1.2: Configure Spring Security - 3 SP
- [ ] Task 4.1.1.3: Configure PostgreSQL - 3 SP
- [ ] Task 4.1.1.4: Configure Redis cache - 2 SP
- [ ] Task 4.1.1.5: Add Swagger/OpenAPI - 2 SP

**Story Points**: 8

---

### Story 4.1.2: User Authentication and Authorization 🔴 P0
**As a** user
**I want** to authenticate securely
**So that** my plots are private

**Acceptance Criteria**:
- [ ] User registration
- [ ] User login (JWT)
- [ ] Password hashing
- [ ] Role-based access control

**Tasks**:
- [ ] Task 4.1.2.1: Implement user entity - 2 SP
- [ ] Task 4.1.2.2: Implement authentication - 5 SP
- [ ] Task 4.1.2.3: Implement JWT tokens - 3 SP
- [ ] Task 4.1.2.4: Implement RBAC - 3 SP
- [ ] Task 4.1.2.5: Security tests - 3 SP

**Story Points**: 13

---

### Story 4.1.3: Plot CRUD API 🔴 P0
**As a** user
**I want** to save and manage plots
**So that** I can reuse them

**Acceptance Criteria**:
- [ ] Create plot
- [ ] Read plot
- [ ] Update plot
- [ ] Delete plot
- [ ] List user plots

**Tasks**:
- [ ] Task 4.1.3.1: Design plot entity - 3 SP
- [ ] Task 4.1.3.2: Implement repository - 2 SP
- [ ] Task 4.1.3.3: Implement service layer - 3 SP
- [ ] Task 4.1.3.4: Implement REST controller - 5 SP
- [ ] Task 4.1.3.5: Write integration tests - 5 SP

**Story Points**: 13

---

### Story 4.1.4: Data Upload API 🔴 P0
**As a** user
**I want** to upload data files
**So that** I can plot my data

**Acceptance Criteria**:
- [ ] File upload endpoint
- [ ] Size limits enforced
- [ ] File type validation
- [ ] Preview data

**Tasks**:
- [ ] Task 4.1.4.1: Implement file upload - 5 SP
- [ ] Task 4.1.4.2: Add validation - 3 SP
- [ ] Task 4.1.4.3: Store files (S3 or local) - 5 SP
- [ ] Task 4.1.4.4: Implement preview - 3 SP
- [ ] Task 4.1.4.5: Integration tests - 3 SP

**Story Points**: 13

---

### Story 4.1.5: Plot Rendering API 🔴 P0
**As a** user
**I want** to render plots via API
**So that** I can generate images programmatically

**Acceptance Criteria**:
- [ ] Render plot to PNG
- [ ] Render plot to SVG
- [ ] Render plot to PDF
- [ ] Async rendering for large plots

**Tasks**:
- [ ] Task 4.1.5.1: Implement render service - 5 SP
- [ ] Task 4.1.5.2: Add format support - 5 SP
- [ ] Task 4.1.5.3: Implement async rendering - 5 SP
- [ ] Task 4.1.5.4: Add progress tracking - 3 SP
- [ ] Task 4.1.5.5: Integration tests - 3 SP

**Story Points**: 13

---

### Story 4.1.6: WebSocket for Real-time Updates 🟠 P1
**As a** user
**I want** real-time plot updates
**So that** I see changes immediately

**Acceptance Criteria**:
- [ ] WebSocket connection
- [ ] Plot update messages
- [ ] Render progress updates
- [ ] Error notifications

**Tasks**:
- [ ] Task 4.1.6.1: Configure STOMP WebSocket - 3 SP
- [ ] Task 4.1.6.2: Implement plot updates - 5 SP
- [ ] Task 4.1.6.3: Implement progress updates - 3 SP
- [ ] Task 4.1.6.4: Test WebSocket - 3 SP

**Story Points**: 8

---

### Story 4.1.7: Template Management API 🟡 P2
**As a** user
**I want** plot templates
**So that** I can quickly create common plots

**Acceptance Criteria**:
- [ ] List templates
- [ ] Create plot from template
- [ ] Save custom template
- [ ] Share templates

**Tasks**:
- [ ] Task 4.1.7.1: Design template entity - 3 SP
- [ ] Task 4.1.7.2: Implement template CRUD - 5 SP
- [ ] Task 4.1.7.3: Add sharing functionality - 3 SP
- [ ] Task 4.1.7.4: Integration tests - 2 SP

**Story Points**: 8

---

## Epic 4.2: API Documentation and Testing

### Story 4.2.1: OpenAPI/Swagger Documentation 🔴 P0
**As a** API consumer
**I want** comprehensive API documentation
**So that** I can integrate easily

**Acceptance Criteria**:
- [ ] All endpoints documented
- [ ] Request/response examples
- [ ] Interactive API testing
- [ ] Auto-generated docs

**Tasks**:
- [ ] Task 4.2.1.1: Configure Springdoc OpenAPI - 2 SP
- [ ] Task 4.2.1.2: Add API annotations - 5 SP
- [ ] Task 4.2.1.3: Add examples - 3 SP
- [ ] Task 4.2.1.4: Configure UI - 2 SP

**Story Points**: 8

---

### Story 4.2.2: Integration Test Suite 🔴 P0
**As a** developer
**I want** comprehensive integration tests
**So that** API reliability is ensured

**Acceptance Criteria**:
- [ ] Test all endpoints
- [ ] Test authentication
- [ ] Test error scenarios
- [ ] >80% code coverage

**Tasks**:
- [ ] Task 4.2.2.1: Set up test database - 2 SP
- [ ] Task 4.2.2.2: Write controller tests - 8 SP
- [ ] Task 4.2.2.3: Write security tests - 5 SP
- [ ] Task 4.2.2.4: Write error tests - 3 SP

**Story Points**: 13

---

# PHASE 5: WEB FRONTEND (Weeks 49-64)

## Epic 5.1: Frontend Setup

### Story 5.1.1: React Project Setup 🔴 P0
**As a** frontend developer
**I want** a modern React project
**So that** I can build the UI

**Acceptance Criteria**:
- [ ] Vite + React + TypeScript
- [ ] Routing configured (React Router)
- [ ] State management (Zustand)
- [ ] UI library (Material-UI)

**Tasks**:
- [ ] Task 5.1.1.1: Create Vite project - 1 SP
- [ ] Task 5.1.1.2: Configure TypeScript - 2 SP
- [ ] Task 5.1.1.3: Add React Router - 2 SP
- [ ] Task 5.1.1.4: Add Zustand - 2 SP
- [ ] Task 5.1.1.5: Add Material-UI - 2 SP
- [ ] Task 5.1.1.6: Configure ESLint/Prettier - 2 SP

**Story Points**: 8

---

### Story 5.1.2: API Client Setup 🔴 P0
**As a** frontend developer
**I want** a configured API client
**So that** I can call backend services

**Acceptance Criteria**:
- [ ] Axios configured
- [ ] React Query configured
- [ ] Authentication interceptor
- [ ] Error handling

**Tasks**:
- [ ] Task 5.1.2.1: Configure Axios - 2 SP
- [ ] Task 5.1.2.2: Add React Query - 2 SP
- [ ] Task 5.1.2.3: Implement auth interceptor - 3 SP
- [ ] Task 5.1.2.4: Implement error handling - 3 SP

**Story Points**: 8

---

### Story 5.1.3: Design System and Components 🔴 P0
**As a** designer/developer
**I want** a consistent design system
**So that** the UI is cohesive

**Acceptance Criteria**:
- [ ] Theme configuration
- [ ] Common components library
- [ ] Style guide documented
- [ ] Responsive design

**Tasks**:
- [ ] Task 5.1.3.1: Create theme - 3 SP
- [ ] Task 5.1.3.2: Create button components - 2 SP
- [ ] Task 5.1.3.3: Create input components - 3 SP
- [ ] Task 5.1.3.4: Create layout components - 3 SP
- [ ] Task 5.1.3.5: Document components - 2 SP

**Story Points**: 8

---

## Epic 5.2: Core Pages

### Story 5.2.1: Landing Page 🔴 P0
**As a** visitor
**I want** an attractive landing page
**So that** I understand what the app does

**Acceptance Criteria**:
- [ ] Hero section
- [ ] Feature highlights
- [ ] Example plots
- [ ] Call to action (Sign up)

**Tasks**:
- [ ] Task 5.2.1.1: Design landing page - 5 SP
- [ ] Task 5.2.1.2: Implement hero section - 3 SP
- [ ] Task 5.2.1.3: Add feature section - 3 SP
- [ ] Task 5.2.1.4: Add examples gallery - 5 SP

**Story Points**: 13

---

### Story 5.2.2: Authentication Pages 🔴 P0
**As a** user
**I want** to register and login
**So that** I can use the application

**Acceptance Criteria**:
- [ ] Registration form
- [ ] Login form
- [ ] Password reset
- [ ] Form validation

**Tasks**:
- [ ] Task 5.2.2.1: Design auth pages - 3 SP
- [ ] Task 5.2.2.2: Implement registration - 5 SP
- [ ] Task 5.2.2.3: Implement login - 3 SP
- [ ] Task 5.2.2.4: Implement password reset - 5 SP
- [ ] Task 5.2.2.5: Add validation - 3 SP

**Story Points**: 13

---

### Story 5.2.3: Dashboard/Home Page 🔴 P0
**As a** user
**I want** a dashboard
**So that** I can see my plots and start new ones

**Acceptance Criteria**:
- [ ] List of user plots
- [ ] Recent plots
- [ ] Quick actions
- [ ] Search and filter

**Tasks**:
- [ ] Task 5.2.3.1: Design dashboard - 5 SP
- [ ] Task 5.2.3.2: Implement plot list - 5 SP
- [ ] Task 5.2.3.3: Add search/filter - 5 SP
- [ ] Task 5.2.3.4: Add quick actions - 3 SP

**Story Points**: 13

---

## Epic 5.3: Plot Creation Interface

### Story 5.3.1: Data Upload Component 🔴 P0
**As a** user
**I want** to upload my data
**So that** I can create plots

**Acceptance Criteria**:
- [ ] Drag-and-drop upload
- [ ] File type validation
- [ ] Upload progress
- [ ] Data preview

**Tasks**:
- [ ] Task 5.3.1.1: Implement file upload - 5 SP
- [ ] Task 5.3.1.2: Add drag-and-drop - 3 SP
- [ ] Task 5.3.1.3: Add progress bar - 2 SP
- [ ] Task 5.3.1.4: Implement data preview - 5 SP

**Story Points**: 13

---

### Story 5.3.2: Plot Type Selector 🔴 P0
**As a** user
**I want** to choose plot type
**So that** I create the right visualization

**Acceptance Criteria**:
- [ ] Visual plot type gallery
- [ ] Plot type descriptions
- [ ] Template preview
- [ ] Quick start

**Tasks**:
- [ ] Task 5.3.2.1: Design selector UI - 5 SP
- [ ] Task 5.3.2.2: Implement gallery - 5 SP
- [ ] Task 5.3.2.3: Add thumbnails - 3 SP
- [ ] Task 5.3.2.4: Add descriptions - 2 SP

**Story Points**: 13

---

### Story 5.3.3: Data Mapping Interface 🔴 P0
**As a** user
**I want** to map data columns to axes
**So that** the plot displays correctly

**Acceptance Criteria**:
- [ ] Column selection for X, Y, Z
- [ ] Visual mapping interface
- [ ] Auto-detect option
- [ ] Preview updates

**Tasks**:
- [ ] Task 5.3.3.1: Design mapping UI - 5 SP
- [ ] Task 5.3.3.2: Implement column selection - 5 SP
- [ ] Task 5.3.3.3: Add auto-detection - 5 SP
- [ ] Task 5.3.3.4: Link to preview - 3 SP

**Story Points**: 13

---

### Story 5.3.4: Plot Configuration Panel 🔴 P0
**As a** user
**I want** to configure plot appearance
**So that** it looks professional

**Acceptance Criteria**:
- [ ] Title and labels editor
- [ ] Color picker
- [ ] Style options
- [ ] Live preview

**Tasks**:
- [ ] Task 5.3.4.1: Design config panel - 5 SP
- [ ] Task 5.3.4.2: Implement title/labels - 3 SP
- [ ] Task 5.3.4.3: Implement color picker - 3 SP
- [ ] Task 5.3.4.4: Implement style options - 5 SP
- [ ] Task 5.3.4.5: Connect to preview - 3 SP

**Story Points**: 13

---

### Story 5.3.5: Live Plot Preview 🔴 P0
**As a** user
**I want** a live preview of my plot
**So that** I see changes immediately

**Acceptance Criteria**:
- [ ] Plotly.js integration
- [ ] Real-time updates
- [ ] Interactive controls (zoom, pan)
- [ ] Responsive sizing

**Tasks**:
- [ ] Task 5.3.5.1: Integrate Plotly.js - 5 SP
- [ ] Task 5.3.5.2: Implement preview component - 5 SP
- [ ] Task 5.3.5.3: Add real-time updates - 5 SP
- [ ] Task 5.3.5.4: Add interactivity - 5 SP

**Story Points**: 13

---

### Story 5.3.6: Code Editor for Advanced Users 🟡 P2
**As an** advanced user
**I want** to edit plot configuration as code
**So that** I have full control

**Acceptance Criteria**:
- [ ] Monaco editor integration
- [ ] Syntax highlighting
- [ ] Auto-completion
- [ ] Error highlighting

**Tasks**:
- [ ] Task 5.3.6.1: Integrate Monaco - 5 SP
- [ ] Task 5.3.6.2: Configure syntax highlighting - 3 SP
- [ ] Task 5.3.6.3: Add auto-completion - 5 SP
- [ ] Task 5.3.6.4: Add error checking - 3 SP

**Story Points**: 13

---

## Epic 5.4: Export and Sharing

### Story 5.4.1: Export Functionality 🔴 P0
**As a** user
**I want** to export my plots
**So that** I can use them elsewhere

**Acceptance Criteria**:
- [ ] Export to PNG
- [ ] Export to SVG
- [ ] Export to PDF
- [ ] Resolution/quality settings

**Tasks**:
- [ ] Task 5.4.1.1: Implement export dialog - 5 SP
- [ ] Task 5.4.1.2: Connect to backend export API - 3 SP
- [ ] Task 5.4.1.3: Add format options - 3 SP
- [ ] Task 5.4.1.4: Handle download - 2 SP

**Story Points**: 8

---

### Story 5.4.2: Share Plot Link 🟡 P2
**As a** user
**I want** to share a link to my plot
**So that** others can view it

**Acceptance Criteria**:
- [ ] Generate shareable link
- [ ] Public/private toggle
- [ ] Embed code generation
- [ ] View count tracking

**Tasks**:
- [ ] Task 5.4.2.1: Implement link generation - 5 SP
- [ ] Task 5.4.2.2: Add privacy controls - 3 SP
- [ ] Task 5.4.2.3: Generate embed code - 3 SP
- [ ] Task 5.4.2.4: Add view tracking - 2 SP

**Story Points**: 8

---

## Epic 5.5: Polish and User Experience

### Story 5.5.1: Responsive Mobile Design 🟠 P1
**As a** mobile user
**I want** the app to work on my phone
**So that** I can create plots anywhere

**Acceptance Criteria**:
- [ ] Mobile-friendly layout
- [ ] Touch-friendly controls
- [ ] Responsive plot preview
- [ ] Mobile navigation

**Tasks**:
- [ ] Task 5.5.1.1: Design mobile layouts - 5 SP
- [ ] Task 5.5.1.2: Implement responsive styles - 8 SP
- [ ] Task 5.5.1.3: Test on devices - 3 SP

**Story Points**: 13

---

### Story 5.5.2: Keyboard Shortcuts 🟡 P2
**As a** power user
**I want** keyboard shortcuts
**So that** I can work faster

**Acceptance Criteria**:
- [ ] Save (Ctrl+S)
- [ ] Export (Ctrl+E)
- [ ] Undo/Redo
- [ ] Help overlay

**Tasks**:
- [ ] Task 5.5.2.1: Implement shortcut system - 5 SP
- [ ] Task 5.5.2.2: Add common shortcuts - 3 SP
- [ ] Task 5.5.2.3: Create help overlay - 3 SP

**Story Points**: 8

---

### Story 5.5.3: Onboarding Tutorial 🟡 P2
**As a** new user
**I want** a tutorial
**So that** I learn how to use the app

**Acceptance Criteria**:
- [ ] Interactive walkthrough
- [ ] Step-by-step guide
- [ ] Skip option
- [ ] Example creation

**Tasks**:
- [ ] Task 5.5.3.1: Design tutorial flow - 5 SP
- [ ] Task 5.5.3.2: Implement walkthrough - 8 SP
- [ ] Task 5.5.3.3: Add example data - 2 SP

**Story Points**: 13

---

### Story 5.5.4: Loading States and Feedback 🔴 P0
**As a** user
**I want** clear feedback on operations
**So that** I know what's happening

**Acceptance Criteria**:
- [ ] Loading spinners
- [ ] Progress bars
- [ ] Success/error messages
- [ ] Toast notifications

**Tasks**:
- [ ] Task 5.5.4.1: Implement loading states - 3 SP
- [ ] Task 5.5.4.2: Add progress tracking - 3 SP
- [ ] Task 5.5.4.3: Implement toast system - 3 SP
- [ ] Task 5.5.4.4: Add error messages - 2 SP

**Story Points**: 8

---

# PHASE 6: INTEGRATION AND POLISH (Weeks 65-72)

## Epic 6.1: End-to-End Integration

### Story 6.1.1: Full User Journey Testing 🔴 P0
**As a** QA engineer
**I want** E2E tests for complete workflows
**So that** the entire system works together

**Acceptance Criteria**:
- [ ] Registration to plot creation
- [ ] Data upload to export
- [ ] All plot types tested
- [ ] Cross-browser testing

**Tasks**:
- [ ] Task 6.1.1.1: Set up Playwright/Cypress - 3 SP
- [ ] Task 6.1.1.2: Write E2E test scenarios - 13 SP
- [ ] Task 6.1.1.3: Run on CI/CD - 3 SP

**Story Points**: 13

---

### Story 6.1.2: Performance Testing and Optimization 🔴 P0
**As a** developer
**I want** to ensure good performance
**So that** users have a smooth experience

**Acceptance Criteria**:
- [ ] Load time < 2s
- [ ] Plot rendering < 200ms
- [ ] API response < 100ms
- [ ] 1000+ concurrent users

**Tasks**:
- [ ] Task 6.1.2.1: Set up performance testing (JMeter) - 3 SP
- [ ] Task 6.1.2.2: Run load tests - 5 SP
- [ ] Task 6.1.2.3: Identify bottlenecks - 5 SP
- [ ] Task 6.1.2.4: Optimize - 8 SP

**Story Points**: 13

---

### Story 6.1.3: Security Audit and Hardening 🔴 P0
**As a** security engineer
**I want** to audit security
**So that** the application is secure

**Acceptance Criteria**:
- [ ] Penetration testing
- [ ] Dependency scanning
- [ ] OWASP top 10 coverage
- [ ] Security fixes

**Tasks**:
- [ ] Task 6.1.3.1: Run security scan (OWASP ZAP) - 3 SP
- [ ] Task 6.1.3.2: Fix vulnerabilities - 8 SP
- [ ] Task 6.1.3.3: Add security headers - 2 SP
- [ ] Task 6.1.3.4: Update dependencies - 2 SP

**Story Points**: 13

---

## Epic 6.2: Documentation and Deployment

### Story 6.2.1: User Documentation 🔴 P0
**As a** user
**I want** comprehensive documentation
**So that** I can learn to use all features

**Acceptance Criteria**:
- [ ] User guide
- [ ] Video tutorials
- [ ] FAQ section
- [ ] Troubleshooting guide

**Tasks**:
- [ ] Task 6.2.1.1: Write user guide - 13 SP
- [ ] Task 6.2.1.2: Create video tutorials - 8 SP
- [ ] Task 6.2.1.3: Write FAQ - 3 SP

**Story Points**: 21

---

### Story 6.2.2: Developer Documentation 🔴 P0
**As a** developer
**I want** API and architecture documentation
**So that** I can extend the system

**Acceptance Criteria**:
- [ ] API documentation
- [ ] Architecture guide
- [ ] Contributing guide
- [ ] Code examples

**Tasks**:
- [ ] Task 6.2.2.1: Complete API docs - 8 SP
- [ ] Task 6.2.2.2: Write architecture guide - 8 SP
- [ ] Task 6.2.2.3: Write contributing guide - 3 SP
- [ ] Task 6.2.2.4: Add code examples - 5 SP

**Story Points**: 21

---

### Story 6.2.3: Production Deployment 🔴 P0
**As a** DevOps engineer
**I want** to deploy to production
**So that** users can access the application

**Acceptance Criteria**:
- [ ] Kubernetes deployment
- [ ] CI/CD pipeline to production
- [ ] Monitoring configured
- [ ] Backup strategy

**Tasks**:
- [ ] Task 6.2.3.1: Create Kubernetes manifests - 5 SP
- [ ] Task 6.2.3.2: Configure production pipeline - 5 SP
- [ ] Task 6.2.3.3: Set up monitoring (Prometheus/Grafana) - 5 SP
- [ ] Task 6.2.3.4: Configure backups - 3 SP

**Story Points**: 13

---

### Story 6.2.4: Beta Launch 🔴 P0
**As a** product manager
**I want** to launch beta
**So that** we get user feedback

**Acceptance Criteria**:
- [ ] Beta program set up
- [ ] Feedback mechanism
- [ ] Usage analytics
- [ ] Bug tracking

**Tasks**:
- [ ] Task 6.2.4.1: Set up beta program - 3 SP
- [ ] Task 6.2.4.2: Add analytics (Google Analytics) - 3 SP
- [ ] Task 6.2.4.3: Set up feedback form - 2 SP
- [ ] Task 6.2.4.4: Launch communication - 2 SP

**Story Points**: 8

---

# OPTIONAL PHASE 7: GNUPLOT COMPATIBILITY (Post-MVP)

## Epic 7.1: Script Compatibility

### Story 7.1.1: Gnuplot Command Parser 🟢 P3
**As a** Gnuplot user
**I want** to run my existing scripts
**So that** I can migrate easily

**Acceptance Criteria**:
- [ ] Parse basic Gnuplot commands
- [ ] Translate to Java API
- [ ] Support common plot types
- [ ] Error messages for unsupported features

**Tasks**:
- [ ] Spike 7.1.1.1: Study C command.c - 8 SP
- [ ] Task 7.1.1.2: Create Gnuplot grammar - 13 SP
- [ ] Task 7.1.1.3: Implement command translator - 21 SP
- [ ] Task 7.1.1.4: Test with demo scripts - 8 SP

**Story Points**: 34

---

### Story 7.1.2: CLI Interface 🟢 P3
**As a** command-line user
**I want** a CLI tool
**So that** I can use gnuplot from terminal

**Acceptance Criteria**:
- [ ] Interactive shell
- [ ] Script execution
- [ ] Pipe support
- [ ] Output to terminal/file

**Tasks**:
- [ ] Task 7.1.2.1: Design CLI architecture - 5 SP
- [ ] Task 7.1.2.2: Implement interactive mode - 8 SP
- [ ] Task 7.1.2.3: Implement batch mode - 5 SP
- [ ] Task 7.1.2.4: Add pipe support - 5 SP

**Story Points**: 21

---

# SUMMARY STATISTICS

## Total Story Points by Phase:
- **Phase 0 (Setup)**: ~100 SP (4 weeks)
- **Phase 1 (Core Math)**: ~300 SP (16 weeks)
- **Phase 2 (Data)**: ~200 SP (12 weeks)
- **Phase 3 (Rendering)**: ~300 SP (16 weeks)
- **Phase 4 (Backend)**: ~120 SP (parallel with Phase 3)
- **Phase 5 (Frontend)**: ~250 SP (16 weeks)
- **Phase 6 (Integration)**: ~115 SP (8 weeks)
- **Phase 7 (Optional)**: ~80 SP (post-MVP)

## Total: ~1,385 Story Points for MVP (Phases 0-6)

## Timeline Estimate (assuming 25-30 SP per week for team):
- **Optimistic**: 46 weeks (~11 months)
- **Realistic**: 58 weeks (~14 months)
- **Conservative**: 72 weeks (~18 months)

---

**Next Action**: Begin Sprint Planning for Phase 0