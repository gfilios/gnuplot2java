#!/bin/bash
set -e  # Exit on any error

echo "=== Clean Build Script for gnuplot-java ==="
echo ""

# Step 1: Clean everything
echo "Step 1: Cleaning all targets and removing duplicate ANTLR classes..."
mvn clean
rm -f gnuplot-core/target/classes/*.class 2>/dev/null || true
rm -f gnuplot-core/target/classes/*.tokens 2>/dev/null || true

# Step 2: Build and install gnuplot-core (with ANTLR generation)
echo ""
echo "Step 2: Building gnuplot-core..."
mvn clean install -pl gnuplot-core -DskipTests

# Step 3: Remove duplicate root-level ANTLR classes after compilation
echo ""
echo "Step 3: Removing duplicate root-level ANTLR classes..."
rm -f gnuplot-core/target/classes/*.class 2>/dev/null || true
rm -f gnuplot-core/target/classes/*.tokens 2>/dev/null || true

# Step 4: Repackage gnuplot-core without duplicates
echo ""
echo "Step 4: Repackaging gnuplot-core..."
mvn jar:jar install:install -pl gnuplot-core -DskipTests

# Step 5: Build remaining modules
echo ""
echo "Step 5: Building gnuplot-render..."
mvn install -pl gnuplot-render -DskipTests

echo ""
echo "Step 6: Building gnuplot-server..."
mvn install -pl gnuplot-server -DskipTests

echo ""
echo "Step 7: Building gnuplot-cli with fat jar..."
mvn install -pl gnuplot-cli -DskipTests

echo ""
echo "=== Build Complete ==="
echo "Fat JAR: gnuplot-cli/target/gnuplot-cli-1.0.0-SNAPSHOT-jar-with-dependencies.jar"
