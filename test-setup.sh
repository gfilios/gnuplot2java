#!/bin/bash

# Color codes for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo "=================================================="
echo "  Gnuplot Modernization - Setup Verification"
echo "=================================================="
echo ""

# Function to check command existence
check_command() {
    if command -v $1 &> /dev/null; then
        echo -e "${GREEN}✓${NC} $1 is installed"
        return 0
    else
        echo -e "${RED}✗${NC} $1 is NOT installed"
        return 1
    fi
}

# Function to check version
check_version() {
    echo -e "${BLUE}Checking $1 version...${NC}"
    $2
    echo ""
}

# Track overall status
ALL_GOOD=true

# 1. Check Java
echo -e "${YELLOW}1. Checking Java...${NC}"
if check_command java; then
    check_version "Java" "java -version"
    JAVA_VERSION=$(java -version 2>&1 | head -n 1 | cut -d'"' -f2 | cut -d'.' -f1)
    if [ "$JAVA_VERSION" -ge 21 ]; then
        echo -e "${GREEN}✓ Java version $JAVA_VERSION is sufficient (need 21+)${NC}"
    else
        echo -e "${RED}✗ Java version $JAVA_VERSION is too old (need 21+)${NC}"
        ALL_GOOD=false
    fi
else
    ALL_GOOD=false
    echo -e "${RED}Please install JDK 21 or higher${NC}"
fi
echo ""

# 2. Check Maven
echo -e "${YELLOW}2. Checking Maven...${NC}"
if check_command mvn; then
    check_version "Maven" "mvn -version"
else
    ALL_GOOD=false
    echo -e "${RED}Please install Maven 3.9 or higher${NC}"
fi
echo ""

# 3. Check Git
echo -e "${YELLOW}3. Checking Git...${NC}"
if check_command git; then
    check_version "Git" "git --version"

    # Check git config
    GIT_USER=$(git config user.name)
    GIT_EMAIL=$(git config user.email)

    if [ -z "$GIT_USER" ] || [ -z "$GIT_EMAIL" ]; then
        echo -e "${YELLOW}⚠ Git user not configured${NC}"
        echo "  Run: git config --global user.name \"Your Name\""
        echo "  Run: git config --global user.email \"your.email@example.com\""
    else
        echo -e "${GREEN}✓ Git configured as: $GIT_USER <$GIT_EMAIL>${NC}"
    fi
else
    ALL_GOOD=false
    echo -e "${RED}Please install Git${NC}"
fi
echo ""

# 4. Check project structure
echo -e "${YELLOW}4. Checking Project Structure...${NC}"
PROJECT_FILES=(
    "gnuplot-java/pom.xml"
    "gnuplot-java/gnuplot-core/pom.xml"
    "gnuplot-java/gnuplot-render/pom.xml"
    "gnuplot-java/gnuplot-server/pom.xml"
    "gnuplot-java/gnuplot-cli/pom.xml"
    "gnuplot-c/README"
    "README.md"
    "SETUP.md"
)

for file in "${PROJECT_FILES[@]}"; do
    if [ -f "$file" ]; then
        echo -e "${GREEN}✓${NC} $file exists"
    else
        echo -e "${RED}✗${NC} $file is missing"
        ALL_GOOD=false
    fi
done
echo ""

# 5. Try to build the project
if [ "$ALL_GOOD" = true ]; then
    echo -e "${YELLOW}5. Testing Maven Build...${NC}"
    echo "Running: cd gnuplot-java && mvn clean compile -DskipTests"
    echo ""

    cd gnuplot-java
    if mvn clean compile -DskipTests; then
        echo ""
        echo -e "${GREEN}✓ Maven build successful!${NC}"
    else
        echo ""
        echo -e "${RED}✗ Maven build failed${NC}"
        ALL_GOOD=false
    fi
    echo ""

    # 6. Try to run tests
    echo -e "${YELLOW}6. Running Tests...${NC}"
    echo "Running: mvn test"
    echo ""

    if mvn test; then
        echo ""
        echo -e "${GREEN}✓ Tests passed!${NC}"
    else
        echo ""
        echo -e "${RED}✗ Tests failed${NC}"
        ALL_GOOD=false
    fi
    cd ..
    echo ""
else
    echo -e "${YELLOW}Skipping build tests due to missing prerequisites${NC}"
    echo ""
fi

# 7. Check optional tools
echo -e "${YELLOW}7. Checking Optional Tools...${NC}"
check_command docker && echo "  Docker available for local database setup"
check_command docker-compose && echo "  Docker Compose available"
echo ""

# Final summary
echo "=================================================="
if [ "$ALL_GOOD" = true ]; then
    echo -e "${GREEN}✓ All checks passed! You're ready to develop.${NC}"
    echo ""
    echo "Next steps:"
    echo "  1. cd gnuplot-java"
    echo "  2. Open the project in your IDE (IntelliJ IDEA recommended)"
    echo "  3. Wait for Maven to download dependencies"
    echo "  4. Start implementing features from ../IMPLEMENTATION_BACKLOG.md"
    echo "  5. Run tests with: mvn test"
else
    echo -e "${RED}✗ Some checks failed. Please fix the issues above.${NC}"
    echo ""
    echo "See SETUP.md for detailed installation instructions."
fi
echo "=================================================="