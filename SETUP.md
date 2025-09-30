# Development Environment Setup

This guide will help you set up your development environment for the Gnuplot Java Modernization project.

---

## Prerequisites

### Required Software

#### 1. Java Development Kit (JDK) 21 LTS

**Why JDK 21?**
- Long Term Support (LTS) release
- Modern Java features (Records, Pattern Matching, Virtual Threads)
- Stable and production-ready

**Installation:**

**macOS:**
```bash
# Using Homebrew
brew install openjdk@21

# Add to PATH (add to ~/.zshrc or ~/.bash_profile)
export PATH="/opt/homebrew/opt/openjdk@21/bin:$PATH"
export JAVA_HOME="/opt/homebrew/opt/openjdk@21"
```

**Linux (Ubuntu/Debian):**
```bash
# Using apt
sudo apt update
sudo apt install openjdk-21-jdk

# Verify installation
java -version
```

**Windows:**
1. Download from [Adoptium](https://adoptium.net/) (Temurin JDK 21)
2. Run the installer
3. Set JAVA_HOME environment variable:
   - Open System Properties → Environment Variables
   - Add JAVA_HOME: `C:\Program Files\Eclipse Adoptium\jdk-21.0.x`
   - Add to PATH: `%JAVA_HOME%\bin`

**Verify Installation:**
```bash
java -version
# Should show: openjdk version "21.0.x"

javac -version
# Should show: javac 21.0.x
```

---

#### 2. Apache Maven 3.9+

**Why Maven?**
- Excellent dependency management
- Standard Java build tool
- Large ecosystem of plugins
- Better for library-heavy projects

**Installation:**

**macOS:**
```bash
brew install maven
```

**Linux:**
```bash
sudo apt install maven
```

**Windows:**
1. Download from [Maven Downloads](https://maven.apache.org/download.cgi)
2. Extract to `C:\Program Files\Apache\maven`
3. Add to PATH: `C:\Program Files\Apache\maven\bin`

**Verify Installation:**
```bash
mvn -version
# Should show: Apache Maven 3.9.x
# Should use Java version 21
```

---

#### 3. Git

**Installation:**

**macOS:**
```bash
brew install git
```

**Linux:**
```bash
sudo apt install git
```

**Windows:**
Download from [git-scm.com](https://git-scm.com/download/win)

**Configure Git:**
```bash
git config --global user.name "Your Name"
git config --global user.email "your.email@example.com"
```

---

### Recommended IDE

#### IntelliJ IDEA (Recommended)

**Download:**
- Community Edition (Free): [Download](https://www.jetbrains.com/idea/download/)
- Ultimate Edition (Paid, better Spring support)

**Initial Setup:**
1. Install IntelliJ IDEA
2. Open the project: `File → Open → Select gnuplot-master directory`
3. IntelliJ will auto-detect Maven and import the project
4. Wait for Maven to download dependencies

**Plugins to Install:**
- Lombok (if we use it)
- SonarLint (code quality)
- Docker (for deployment)
- Rainbow Brackets (readability)

**Configure Code Style:**
1. `File → Settings → Editor → Code Style`
2. Import our `intellij-codestyle.xml` (to be created)

---

#### Eclipse (Alternative)

**Download:**
- Eclipse IDE for Java Developers: [Download](https://www.eclipse.org/downloads/)

**Initial Setup:**
1. Install Eclipse
2. `File → Import → Existing Maven Projects`
3. Select the gnuplot-master directory
4. Eclipse will import all modules

---

#### VS Code (Lightweight Alternative)

**Required Extensions:**
- Language Support for Java (Red Hat)
- Debugger for Java
- Maven for Java
- Spring Boot Extension Pack

**Setup:**
1. Install VS Code
2. Install Java Extension Pack
3. Open folder: `gnuplot-master`
4. VS Code will detect Maven projects

---

## Project Setup

### 1. Clone the Repository

```bash
git clone <repository-url>
cd gnuplot-master
```

### 2. Build the Project

```bash
# Clean build
mvn clean install

# Skip tests (faster)
mvn clean install -DskipTests

# Build specific module
mvn clean install -pl gnuplot-core
```

### 3. Run Tests

```bash
# Run all tests
mvn test

# Run tests for specific module
mvn test -pl gnuplot-core

# Run specific test class
mvn test -Dtest=ExpressionParserTest

# Run with coverage
mvn clean test jacoco:report
```

### 4. IDE Import

**IntelliJ IDEA:**
```
File → Open → Select pom.xml in gnuplot-master
Select "Open as Project"
```

**Eclipse:**
```
File → Import → Maven → Existing Maven Projects
Browse to gnuplot-master directory
Select all modules
```

---

## Development Workflow

### Git Workflow

We follow the **Git Flow** branching model:

**Branch Types:**
- `main` - Production-ready code
- `develop` - Integration branch for features
- `feature/*` - New features
- `bugfix/*` - Bug fixes
- `hotfix/*` - Emergency fixes for production
- `release/*` - Release preparation

**Creating a Feature Branch:**
```bash
# Start from develop
git checkout develop
git pull origin develop

# Create feature branch
git checkout -b feature/expression-parser

# Make changes, commit
git add .
git commit -m "Implement expression parser"

# Push to remote
git push origin feature/expression-parser

# Create Pull Request on GitHub
```

**Commit Message Format:**
```
<type>(<scope>): <subject>

<body>

<footer>
```

**Types:**
- `feat`: New feature
- `fix`: Bug fix
- `docs`: Documentation
- `style`: Formatting
- `refactor`: Code restructuring
- `test`: Adding tests
- `chore`: Maintenance

**Example:**
```
feat(core): Add ANTLR4 expression parser

Implemented ANTLR4 grammar for mathematical expressions.
Supports arithmetic operators, functions, and variables.

Closes #42
```

---

## Git Hooks

We use pre-commit hooks to ensure code quality.

### Install Pre-commit Hooks

```bash
# Copy hooks to .git/hooks/
cp scripts/git-hooks/pre-commit .git/hooks/
chmod +x .git/hooks/pre-commit
```

**Pre-commit Hook Checks:**
- ✅ Code compiles
- ✅ Tests pass
- ✅ Checkstyle passes
- ✅ No debug statements (System.out.println)

---

## Environment Variables

Create a `.env` file in the project root (will be gitignored):

```bash
# Development Database
DB_HOST=localhost
DB_PORT=5432
DB_NAME=gnuplot_dev
DB_USER=gnuplot
DB_PASSWORD=development

# Redis Cache
REDIS_HOST=localhost
REDIS_PORT=6379

# Application Settings
APP_ENV=development
LOG_LEVEL=DEBUG

# AWS (if using S3 for file storage)
AWS_ACCESS_KEY_ID=your_key
AWS_SECRET_ACCESS_KEY=your_secret
AWS_REGION=us-east-1
AWS_S3_BUCKET=gnuplot-data-dev
```

**Load Environment Variables:**

**macOS/Linux:**
```bash
export $(cat .env | xargs)
```

**Windows (PowerShell):**
```powershell
Get-Content .env | ForEach-Object {
    $name, $value = $_.split('=')
    Set-Content -Path "env:$name" -Value $value
}
```

---

## Docker Setup (Optional)

For local development with PostgreSQL and Redis:

### Install Docker

**macOS:**
```bash
brew install --cask docker
```

**Linux:**
```bash
curl -fsSL https://get.docker.com -o get-docker.sh
sudo sh get-docker.sh
```

**Windows:**
Download [Docker Desktop](https://www.docker.com/products/docker-desktop/)

### Start Development Services

```bash
# Start all services (PostgreSQL + Redis)
docker-compose up -d

# Stop services
docker-compose down

# View logs
docker-compose logs -f
```

**docker-compose.yml** (to be created):
```yaml
version: '3.8'

services:
  postgres:
    image: postgres:16
    environment:
      POSTGRES_DB: gnuplot_dev
      POSTGRES_USER: gnuplot
      POSTGRES_PASSWORD: development
    ports:
      - "5432:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data

  redis:
    image: redis:7
    ports:
      - "6379:6379"

volumes:
  postgres_data:
```

---

## Troubleshooting

### Issue: Maven can't find JDK 21

**Solution:**
```bash
# Check JAVA_HOME
echo $JAVA_HOME

# Set JAVA_HOME
export JAVA_HOME=$(/usr/libexec/java_home -v 21)

# Or add to ~/.zshrc or ~/.bash_profile
```

### Issue: Port already in use (8080, 5432, 6379)

**Solution:**
```bash
# Check what's using the port
lsof -i :8080

# Kill the process
kill -9 <PID>

# Or change port in application.properties
```

### Issue: Tests failing with "Cannot find symbol"

**Solution:**
```bash
# Rebuild project
mvn clean install -DskipTests

# Refresh IDE
# IntelliJ: File → Invalidate Caches / Restart
# Eclipse: Project → Clean
```

### Issue: Out of memory during build

**Solution:**
```bash
# Increase Maven memory
export MAVEN_OPTS="-Xmx2048m -XX:MaxPermSize=512m"

# Add to ~/.zshrc or ~/.bash_profile
```

---

## Code Style Guide

### Java Code Style

**Naming Conventions:**
- Classes: `PascalCase` (e.g., `ExpressionParser`)
- Methods: `camelCase` (e.g., `parseExpression()`)
- Constants: `UPPER_SNAKE_CASE` (e.g., `MAX_DEPTH`)
- Packages: `lowercase` (e.g., `com.gnuplot.core.math`)

**Formatting:**
- Indentation: 4 spaces (no tabs)
- Line length: 120 characters max
- Braces: K&R style (opening brace on same line)

**Example:**
```java
public class ExpressionEvaluator {
    private static final int MAX_STACK_DEPTH = 1000;

    public double evaluate(Expression expr) {
        if (expr == null) {
            throw new IllegalArgumentException("Expression cannot be null");
        }
        return evaluateInternal(expr);
    }

    private double evaluateInternal(Expression expr) {
        // Implementation
    }
}
```

---

## Testing Guidelines

### Unit Tests

- Place in `src/test/java` mirroring `src/main/java` structure
- Name: `<ClassName>Test.java`
- Use JUnit 5 annotations: `@Test`, `@BeforeEach`, `@AfterEach`

**Example:**
```java
class ExpressionParserTest {

    private ExpressionParser parser;

    @BeforeEach
    void setUp() {
        parser = new ExpressionParser();
    }

    @Test
    void shouldParseSimpleAddition() {
        Expression result = parser.parse("2 + 3");
        assertThat(result).isNotNull();
        assertThat(result.evaluate()).isEqualTo(5.0);
    }
}
```

### Integration Tests

- Place in `src/test/java` in separate package `integration`
- Use `@SpringBootTest` for Spring components
- Name: `<Feature>IntegrationTest.java`

---

## Performance Profiling

### JMH Benchmarks

For performance-critical code, use JMH benchmarks:

```java
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Thread)
public class ExpressionEvaluatorBenchmark {

    private ExpressionEvaluator evaluator;

    @Setup
    public void setup() {
        evaluator = new ExpressionEvaluator();
    }

    @Benchmark
    public double benchmarkSimpleExpression() {
        return evaluator.evaluate("2 + 3 * 4");
    }
}
```

Run benchmarks:
```bash
mvn clean install
java -jar target/benchmarks.jar
```

---

## Getting Help

### Resources

- **Project Documentation**: [MODERNIZATION_STRATEGY.md](MODERNIZATION_STRATEGY.md)
- **Implementation Backlog**: [IMPLEMENTATION_BACKLOG.md](IMPLEMENTATION_BACKLOG.md)
- **Original Gnuplot**: C source code in `src/` directory

### Communication

- **Issues**: GitHub Issues for bugs and feature requests
- **Discussions**: GitHub Discussions for questions
- **Pull Requests**: For code contributions

---

## Next Steps

After setting up your environment:

1. ✅ Build the project: `mvn clean install`
2. ✅ Run tests: `mvn test`
3. ✅ Open in IDE
4. ✅ Read [IMPLEMENTATION_BACKLOG.md](IMPLEMENTATION_BACKLOG.md)
5. ✅ Pick a task from Phase 0
6. ✅ Create a feature branch
7. ✅ Start coding!

---

**Last Updated**: 2025-09-30
**Maintainer**: Development Team