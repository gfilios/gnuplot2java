# Gnuplot C Implementation (Reference)

This directory contains the **original Gnuplot v6.1.0 C implementation**.

## ⚠️ Purpose

This C code is preserved in the repository for **reference purposes only** during the Java modernization:

- ✅ **Algorithm Reference**: Study mathematical implementations
- ✅ **Test Oracle**: Generate expected outputs for Java tests
- ✅ **Domain Knowledge**: Understand 40+ years of plotting expertise
- ✅ **Historical Context**: Learn from original design decisions

## 🚫 Not for Active Development

The C code in this directory is **NOT being actively developed** as part of this modernization project.

For active development, see:
- **Java Implementation**: `../gnuplot-java/`
- **Project Documentation**: `../README.md`

## 📖 Original Documentation

This directory contains the original Gnuplot documentation:

- [README](README) - Original Gnuplot README
- [Copyright](Copyright) - License information
- [INSTALL](INSTALL) - Original build instructions
- [NEWS](NEWS) - Changelog and version history
- [docs/](docs/) - Complete manual and documentation

## 🔨 Building Original Gnuplot

If you want to build the original C version (for reference or testing):

```bash
cd gnuplot-c

# Configure
./configure

# Build
make

# Test
make test

# Install (optional)
sudo make install
```

See [INSTALL](INSTALL) for detailed build instructions.

## 📊 Comparing C vs Java

### Use Cases for Original C Code

1. **Generate Test Data**
   ```bash
   # Generate expected output for Java tests
   cd gnuplot-c
   make
   echo "plot sin(x)" | ./gnuplot > output.txt
   ```

2. **Study Algorithms**
   - Look at `src/specfun.c` for special function implementations
   - Study `src/interpol.c` for interpolation algorithms
   - Review `src/graph3d.c` for 3D rendering approach

3. **Understand Data Formats**
   - Check binary data readers in `src/datafile.c`
   - Study plot styles in `src/plot2d.c` and `src/plot3d.c`

## 📁 Directory Structure

```
gnuplot-c/
├── src/               # C source code (~134K lines)
│   ├── *.c           # Core implementation files
│   └── *.h           # Header files
├── term/              # Terminal/output drivers (60+)
├── demo/              # Example plots and scripts
├── docs/              # Documentation
├── config/            # Platform-specific configs
├── m4/                # Autoconf macros
└── configure.ac       # Build configuration
```

## 🔗 Key Files for Reference

| File | Lines | Purpose | Java Equivalent |
|------|-------|---------|----------------|
| `src/eval.c` | ~4000 | Expression evaluator | `gnuplot-java/core/math/` |
| `src/specfun.c` | ~5000 | Special functions | `gnuplot-java/core/functions/` |
| `src/graphics.c` | ~6000 | 2D rendering | `gnuplot-java/render/renderer2d/` |
| `src/graph3d.c` | ~4500 | 3D rendering | `gnuplot-java/render/renderer3d/` |
| `src/datafile.c` | ~5900 | Data reading | `gnuplot-java/core/data/` |
| `src/interpol.c` | ~1500 | Interpolation | `gnuplot-java/core/data/` |

## ⚙️ Dependencies

The original C code has platform-specific dependencies. You may need:

**Linux/macOS:**
- GCC or Clang compiler
- X11 libraries (for X11 terminal)
- Cairo libraries (for cairo terminal)
- Qt/wxWidgets (for GUI terminals)
- LaTeX (for LaTeX terminals)

**See [INSTALL](INSTALL) for complete dependency list**

## 🐛 Known Issues

This is the original code with all its historical quirks:
- Contains platform-specific code for obsolete systems (VMS, OS/2, BeOS)
- Manual memory management with potential leaks
- Global state and complex interdependencies
- Limited type safety

**These are why we're modernizing!** The Java version addresses these issues.

## 📜 License

Same license as original Gnuplot. See [Copyright](Copyright) for details.

## 🔙 Back to Modernization

- **Java Implementation**: [../gnuplot-java/](../gnuplot-java/)
- **Main README**: [../README.md](../README.md)
- **Implementation Plan**: [../IMPLEMENTATION_BACKLOG.md](../IMPLEMENTATION_BACKLOG.md)

---

**Original Authors**: Thomas Williams, Colin Kelley, and many contributors over 40+ years

**Preserved Version**: 6.1.0 (Development, 2023)