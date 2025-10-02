# Gnuplot Rendering Engine Architecture

**Version**: 1.0.0
**Status**: In Development
**Last Updated**: 2025-10-01

---

## Overview

The Gnuplot Rendering Engine is a modular, format-agnostic rendering system designed to support multiple output formats (SVG, PNG, PDF) and plot types (2D, 3D). The architecture follows a scene graph pattern with a visitor-based rendering approach.

## Design Principles

1. **Format Agnostic**: Core rendering logic is independent of output format
2. **Extensible**: New renderers and scene elements can be added without modifying existing code
3. **Type-Safe**: Rendering hints use type-safe keys to prevent runtime errors
4. **Testable**: Clear separation of concerns enables comprehensive unit testing
5. **Performance**: Capabilities-based feature detection avoids unnecessary operations

---

## Architecture Diagram

```
┌─────────────────────────────────────────────────────────────┐
│                        Client Code                           │
└────────────────────┬────────────────────────────────────────┘
                     │
                     ▼
┌─────────────────────────────────────────────────────────────┐
│                      Scene (Container)                       │
│  - viewport: Viewport                                        │
│  - elements: List<SceneElement>                             │
│  - hints: RenderingHints                                     │
└────────────────────┬────────────────────────────────────────┘
                     │
        ┌────────────┴────────────┐
        ▼                         ▼
┌──────────────────┐    ┌──────────────────┐
│    Viewport      │    │  SceneElement    │
│  - bounds        │    │  (interface)     │
│  - projection    │    └─────────┬────────┘
└──────────────────┘              │
                                  │
        ┌─────────────────────────┼─────────────────────┐
        ▼                         ▼                     ▼
┌──────────────┐        ┌──────────────┐    ┌──────────────┐
│     Axis     │        │   LinePlot   │    │    Legend    │
│ - range      │        │ - points     │    │ - entries    │
│ - scale      │        │ - style      │    │ - position   │
│ - ticks      │        └──────────────┘    └──────────────┘
└──────────────┘
        │
        ▼
┌──────────────────┐
│  TickGenerator   │
│ - generateTicks()│
└──────────────────┘
                                  │
                                  ▼
                     ┌────────────────────────┐
                     │  SceneElementVisitor   │
                     │     (interface)        │
                     └───────────┬────────────┘
                                 │
                ┌────────────────┼────────────────┐
                ▼                ▼                ▼
        ┌──────────────┐  ┌──────────────┐  ┌──────────────┐
        │ SVGRenderer  │  │ PNGRenderer  │  │ PDFRenderer  │
        │              │  │              │  │              │
        └──────────────┘  └──────────────┘  └──────────────┘
```

---

## Core Components

### 1. Renderer Interface

The `Renderer` interface defines the contract for all rendering implementations.

```java
public interface Renderer {
    void render(Scene scene, OutputStream output) throws RenderException;
    RendererCapabilities getCapabilities();
    String getFormat();
}
```

**Key Methods**:
- `render()`: Main rendering method that accepts a scene and outputs to a stream
- `getCapabilities()`: Returns capabilities for feature detection
- `getFormat()`: Returns the output format identifier (e.g., "SVG", "PNG")

### 2. Scene Graph

The scene graph is a hierarchical structure representing the plot to be rendered.

#### Scene
Container for all rendering elements:
- **Viewport**: Defines the coordinate system and projection
- **Elements**: List of scene elements (axes, plots, legends, etc.)
- **Hints**: Rendering hints for renderer-specific customization

#### SceneElement
Base interface for all renderable elements:
- `getType()`: Returns the element type
- `accept(SceneElementVisitor)`: Visitor pattern for type-safe rendering

**Concrete Elements**:
- **Axis**: Coordinate axis with tick generation
- **LinePlot**: Line plot with points and styling
- **Legend**: Plot legend with entries
- **Grid**: Background grid
- **Label**: Text label

### 3. Viewport

Defines the coordinate system and viewing transformation:

```java
public class Viewport {
    private final Bounds bounds;
    private final ProjectionType projectionType;
    // ...
}
```

**Projection Types**:
- `CARTESIAN_2D`: Standard 2D Cartesian coordinates
- `CARTESIAN_3D`: 3D Cartesian coordinates
- `POLAR`: Polar coordinates (r, θ)
- `CYLINDRICAL`: Cylindrical coordinates (r, θ, z)
- `SPHERICAL`: Spherical coordinates (ρ, θ, φ)

### 4. RenderingHints

Type-safe hints system for renderer customization:

```java
RenderingHints hints = new RenderingHints();
hints.put(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
hints.put(RenderingHints.KEY_RESOLUTION, 300);
```

**Standard Hint Keys**:
- `KEY_ANTIALIASING`: Enable/disable antialiasing
- `KEY_RESOLUTION`: DPI for raster formats
- `KEY_LINE_WIDTH`: Default line width
- `KEY_FONT_SIZE`: Default font size
- `KEY_BACKGROUND_COLOR`: Background color
- `KEY_GRID_STYLE`: Grid line style
- `KEY_COMPRESSION`: Compression level
- `KEY_TRANSPARENCY`: Enable/disable transparency
- `KEY_EMBEDDED_FONTS`: Embed fonts in output
- `KEY_OUTPUT_SIZE`: Output dimensions
- `KEY_QUALITY`: Quality level (0.0-1.0)
- `KEY_ANIMATION_DURATION`: Animation duration (ms)

### 5. RendererCapabilities

Capabilities-based feature detection:

```java
public class RendererCapabilities {
    private final boolean supportsTransparency;
    private final boolean supports3D;
    private final boolean supportsAnimation;
    private final boolean supportsInteractivity;
    private final boolean isVectorGraphics;
    // ...
}
```

This allows renderers to declare their capabilities and clients to query them before using advanced features.

---

## Visitor Pattern

The architecture uses the Visitor pattern for type-safe, double-dispatch rendering:

```java
public interface SceneElementVisitor {
    void visitAxis(Axis axis);
    void visitLinePlot(LinePlot plot);
    void visitLegend(Legend legend);
    void visitGrid(Grid grid);
    void visitLabel(Label label);
}
```

**Benefits**:
- Type safety: No casting required
- Extensibility: New element types can be added easily
- Separation: Rendering logic is separate from scene element logic

---

## Axis Tick Generation

Axes use the `TickGenerator` to automatically generate tick marks:

```java
public class Axis implements SceneElement {
    public List<TickGenerator.Tick> generateTicks() {
        switch (scaleType) {
            case LINEAR:
                return tickGenerator.generateTicks(min, max, 20, minorTicsCount);
            case LOGARITHMIC:
                return tickGenerator.generateLogTicks(min, max, 10.0, minorTicsCount > 0);
            case TIME:
                // Time-based tick generation
                return tickGenerator.generateTicks(min, max, 20, minorTicsCount);
        }
    }
}
```

**Tick Generation Algorithm**:
- **Linear**: Uses gnuplot's `quantize_normal_tics` algorithm for "nice" spacing
- **Logarithmic**: Places ticks at powers of the base (typically 10)
- **Time**: Time-based intervals (planned)

**Tick Types**:
- **MAJOR**: Main tick marks with labels
- **MINOR**: Subdivision tick marks without labels

---

## Rendering Flow

1. **Client creates Scene**:
   ```java
   Scene scene = Scene.builder()
       .viewport(viewport)
       .addElement(axis)
       .addElement(plot)
       .hints(hints)
       .build();
   ```

2. **Client selects Renderer**:
   ```java
   Renderer renderer = new SVGRenderer();
   ```

3. **Renderer processes Scene**:
   ```java
   renderer.render(scene, outputStream);
   ```

4. **Visitor traverses Scene**:
   - Renderer implements `SceneElementVisitor`
   - Each element accepts the visitor
   - Visitor performs format-specific rendering

---

## Error Handling

The architecture uses `RenderException` for all rendering errors:

```java
public class RenderException extends Exception {
    private final ErrorCode errorCode;
    private final String context;
    // ...
}
```

**Error Codes**:
- `INVALID_SCENE`: Scene validation failed
- `UNSUPPORTED_FEATURE`: Feature not supported by renderer
- `IO_ERROR`: I/O operation failed
- `RENDERING_ERROR`: General rendering error

---

## Extension Points

### Adding a New Renderer

1. Implement `Renderer` interface
2. Implement `SceneElementVisitor` interface
3. Define `RendererCapabilities`
4. Implement format-specific rendering logic

Example:
```java
public class CustomRenderer implements Renderer, SceneElementVisitor {
    @Override
    public void render(Scene scene, OutputStream output) throws RenderException {
        // Initialize format-specific context
        for (SceneElement element : scene.getElements()) {
            element.accept(this);  // Visitor pattern
        }
        // Write output
    }

    @Override
    public void visitAxis(Axis axis) {
        // Render axis in custom format
    }
    // ... implement other visit methods
}
```

### Adding a New Scene Element

1. Implement `SceneElement` interface
2. Add visit method to `SceneElementVisitor`
3. Implement visitor method in all renderers

---

## Testing Strategy

### Unit Tests
- Individual components tested in isolation
- Mock dependencies for focused testing
- 104 tests covering all components

### Integration Tests
- End-to-end rendering tests
- Visual regression tests
- Format-specific validation

### Test Coverage
- **Renderer Interface**: 58 tests
- **Axis & TickGenerator**: 46 tests
- **Total**: 104 tests (all passing)

---

## Performance Considerations

1. **Lazy Generation**: Ticks are generated on demand
2. **Immutable Elements**: Scene elements are immutable for thread safety
3. **Streaming Output**: Renderers write directly to output streams
4. **Capabilities Check**: Avoid attempting unsupported features

---

## Future Enhancements

1. **GPU Acceleration**: OpenGL-based 3D rendering
2. **Caching**: Cache generated ticks and transformations
3. **Parallel Rendering**: Multi-threaded rendering for large scenes
4. **Interactive Rendering**: WebGL/Canvas for interactive plots
5. **Animation**: Support for animated plots

---

## References

- Gnuplot C source code (axis.c, term.c)
- Scene Graph design patterns
- Visitor pattern implementation
- Rendering pipeline best practices

---

## Revision History

| Version | Date       | Changes                           |
|---------|------------|-----------------------------------|
| 1.0.0   | 2025-10-01 | Initial architecture document     |
