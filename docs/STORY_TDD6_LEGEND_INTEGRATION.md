# Story TDD-6: Color Assignment & Legend Integration for simple.dem

**Status**: PLANNED
**Story Points**: 13 SP (3 SP color + 2+2+3+1+2 SP legend)
**Priority**: CRITICAL (Required for simple.dem to pass visual comparison)

## Goal

Implement automatic color assignment for multi-plot commands AND integrate Legend rendering to match C Gnuplot output. These two features work together to make plots distinguishable.

## Gap Analysis

After comparing C vs Java SVG outputs for simple.dem:

### Missing Features (Priority Order):

1. **Plot Color Assignment** 🔴 **MOST CRITICAL** - NEW FINDING!
   - C Output: Uses distinct colors for each plot line
     * Plot 1: Purple #9400D3, Green #009E73, Blue #56B4E9 (3 lines)
     * Plot 2: Purple, Green (2 lines)
     * etc.
   - Java Output: **ALL PLOTS ARE BLACK #000000**
   - Impact: **All lines look identical - impossible to distinguish!**
   - Root cause: `LinePlot.Builder` defaults to black, no color assignment in executor
   - **This must be fixed BEFORE legend** - legend without colors is useless!

2. **Legend/Key Display** ⭐ CRITICAL
   - C Output: Shows legend box with plot labels ("sin(x)", "atan(x)", "cos(atan(x)"))
   - Java Output: No legend visible
   - Impact: Makes it impossible to identify which line is which function
   - Gnuplot command: `set key left box`, `set key right nobox`, `set key bmargin center horizontal`
   - **Depends on #1** - Legend needs colors to display correctly

3. **Plot Area Border/Frame**
   - C Output: Black border around plot area
   - Java Output: No border
   - Impact: Less defined plot region

4. **Point Markers** (for `set style data points`)
   - C Output: 15 point types (circles, squares, triangles)
   - Java Output: Only renders lines
   - Impact: Cannot show data as points

4. **Font Styling**
   - C Output: Arial font
   - Java Output: Basic font
   - Impact: Minor visual difference

## Architectural Check

✅ **Infrastructure Ready**:
- Legend class exists (Story 3.5.2 - COMPLETE, 8 SP)
- Legend.Builder with full features: position, showBorder, entries, columns, fonts
- SvgRenderer.visitLegend() fully implemented
- Grammar supports "set key" commands (keyPosition, BOX/NOBOX, HORIZONTAL/VERTICAL)

❌ **Missing**:
- SetKeyContext proper parsing (currently just stores raw text)
- GnuplotScriptExecutor doesn't create Legend
- No integration tests for legend rendering

## simple.dem Key Commands

From simple.dem, we need to support:
```gnuplot
set key left box              # Plot 1: Legend at left with border
set key right nobox           # Plot 2: Legend at right without border
set key left box              # Plot 3: Legend at left with border
# (Plot 4: implicit - uses last setting)
# (Plot 5: implicit)
set key bmargin center horizontal  # Plot 6: Legend at bottom margin, centered
set key left box              # Plot 7: Legend at left with border
# (Plot 8: implicit - uses last setting with data files)
```

## Implementation Plan (TDD Approach)

### Phase 0: Plot Color Assignment (3 SP) 🔴 **DO THIS FIRST!**

**Task**: Assign distinct colors to plots in multi-plot commands

**Why first**: Legend without colors is useless - all lines look identical. This is the #1 visual gap.

**Changes to GnuplotScriptExecutor.java**:
```java
// Add color palette field (Gnuplot default colors)
private static final String[] DEFAULT_COLORS = {
    "#9400D3",  // Purple
    "#009E73",  // Green
    "#56B4E9",  // Blue
    "#E69F00",  // Orange
    "#F0E442",  // Yellow
    "#0072B2",  // Dark Blue
    "#D55E00",  // Red-Orange
    "#CC79A7"   // Pink
};

@Override
public void visitPlotCommand(PlotCommand command) {
    plots.clear();
    plotTitles.clear();

    int colorIndex = 0;  // Track color cycling
    for (PlotCommand.PlotSpec spec : command.getPlotSpecs()) {
        // ... existing code to generate points ...

        LinePlot.Builder plotBuilder = LinePlot.builder()
                .id("plot_" + plots.size())
                .points(List.of(points))
                .color(DEFAULT_COLORS[colorIndex % DEFAULT_COLORS.length]);  // ← ADD THIS!

        if (plotTitle != null && !plotTitle.isEmpty()) {
            plotBuilder.label(plotTitle);
        }

        plots.add(plotBuilder.build());
        colorIndex++;  // Next color for next plot
    }

    createAndAddScene();
}
```

**Test** (ScriptExecutionTest.java):
```java
@Test
void executeMultiPlotCommandWithDifferentColors() throws IOException {
    String script = """
            plot sin(x), cos(x), tan(x)
            """;

    GnuplotScript gnuplotScript = parser.parse(script);
    executor.execute(gnuplotScript);

    Path outputFile = Path.of("output.svg");
    String svgContent = Files.readString(outputFile);

    // Verify each plot has different color
    assertThat(svgContent).contains("#9400D3");  // Purple - first plot
    assertThat(svgContent).contains("#009E73");  // Green - second plot
    assertThat(svgContent).contains("#56B4E9");  // Blue - third plot

    // Verify NO black lines (default)
    assertThat(svgContent).doesNotContain("stroke=\"#000000\"");

    Files.deleteIfExists(outputFile);
}
```

**Expected improvements**:
- simple.dem plots now have distinct colors matching C Gnuplot
- Visual differentiation even without legend
- File sizes unchanged (just color attribute changes)

---

### Phase 1: Parser Enhancement (2 SP)

**Task**: Properly parse SetKeyContext to extract structured data

**Changes to CommandBuilderVisitor.java**:
```java
} else if (optCtx instanceof GnuplotCommandParser.SetKeyContext) {
    GnuplotCommandParser.SetKeyContext keyCtx = (GnuplotCommandParser.SetKeyContext) optCtx;

    // Extract position
    String position = parseKeyPosition(keyCtx.keyPosition());

    // Extract options (BOX/NOBOX, HORIZONTAL/VERTICAL)
    boolean showBorder = true; // default
    boolean horizontal = false; // default

    for (GnuplotCommandParser.KeyOptionsContext opt : keyCtx.keyOptions()) {
        if (opt.BOX() != null) showBorder = true;
        if (opt.NOBOX() != null) showBorder = false;
        if (opt.HORIZONTAL() != null) horizontal = true;
        if (opt.VERTICAL() != null) horizontal = false;
    }

    // Create structured command
    Map<String, Object> keySettings = new HashMap<>();
    keySettings.put("position", position);
    keySettings.put("showBorder", showBorder);
    keySettings.put("horizontal", horizontal);

    commands.add(new SetCommand("key", keySettings));
}

private String parseKeyPosition(GnuplotCommandParser.KeyPositionContext ctx) {
    if (ctx.LEFT() != null) return "LEFT";
    if (ctx.RIGHT() != null) return "RIGHT";
    if (ctx.TOP() != null) return "TOP";
    if (ctx.BOTTOM() != null) return "BOTTOM";
    if (ctx.CENTER() != null) return "CENTER";
    if (ctx.BMARGIN() != null) {
        if (ctx.LEFT() != null) return "BOTTOM_LEFT";
        if (ctx.RIGHT() != null) return "BOTTOM_RIGHT";
        if (ctx.CENTER() != null) return "BOTTOM_CENTER";
        return "BOTTOM_CENTER";
    }
    if (ctx.TMARGIN() != null) return "TOP_CENTER";
    return "TOP_RIGHT"; // default
}
```

**Test** (GnuplotCommandParserTest.java):
```java
@Test
void parseSetKeyLeftBox() {
    String script = "set key left box";
    GnuplotScript result = parser.parse(script);

    assertThat(result.getCommands()).hasSize(1);
    Command cmd = result.getCommands().get(0);
    assertThat(cmd).isInstanceOf(SetCommand.class);

    SetCommand setCmd = (SetCommand) cmd;
    assertThat(setCmd.getOption()).isEqualTo("key");

    Map<String, Object> settings = (Map<String, Object>) setCmd.getValue();
    assertThat(settings.get("position")).isEqualTo("LEFT");
    assertThat(settings.get("showBorder")).isEqualTo(true);
}

@Test
void parseSetKeyRightNoBox() {
    String script = "set key right nobox";
    GnuplotScript result = parser.parse(script);

    SetCommand setCmd = (SetCommand) result.getCommands().get(0);
    Map<String, Object> settings = (Map<String, Object>) setCmd.getValue();
    assertThat(settings.get("position")).isEqualTo("RIGHT");
    assertThat(settings.get("showBorder")).isEqualTo(false);
}

@Test
void parseSetKeyBmarginCenterHorizontal() {
    String script = "set key bmargin center horizontal";
    GnuplotScript result = parser.parse(script);

    SetCommand setCmd = (SetCommand) result.getCommands().get(0);
    Map<String, Object> settings = (Map<String, Object>) setCmd.getValue();
    assertThat(settings.get("position")).isEqualTo("BOTTOM_CENTER");
    assertThat(settings.get("horizontal")).isEqualTo(true);
}
```

### Phase 2: Executor State Management (2 SP)

**Task**: Store key settings in GnuplotScriptExecutor

**Changes to GnuplotScriptExecutor.java**:
```java
// Add fields
private String keyPosition = "TOP_RIGHT";  // default
private boolean keyShowBorder = true;      // default
private boolean keyHorizontal = false;     // default

@Override
public void visitSetCommand(SetCommand command) {
    String option = command.getOption();
    Object value = command.getValue();

    switch (option) {
        // ... existing cases ...

        case "key":
            if (value instanceof Map) {
                Map<String, Object> settings = (Map<String, Object>) value;
                keyPosition = (String) settings.getOrDefault("position", "TOP_RIGHT");
                keyShowBorder = (Boolean) settings.getOrDefault("showBorder", true);
                keyHorizontal = (Boolean) settings.getOrDefault("horizontal", false);
            }
            break;
    }
}
```

### Phase 3: Legend Creation in Scene (3 SP)

**Task**: Create and add Legend to scene with plot titles

**Changes to GnuplotScriptExecutor.java**:
```java
private void createAndAddScene() {
    if (plots.isEmpty()) {
        return;
    }

    // ... existing viewport and scene builder code ...

    // Create and add X axis
    // ... existing code ...

    // Create and add Y axis
    // ... existing code ...

    // Create Legend if we have plot titles
    if (hasPlotTitles()) {
        Legend.Builder legendBuilder = Legend.builder()
                .id("legend")
                .position(mapKeyPosition(keyPosition))
                .showBorder(keyShowBorder)
                .columns(keyHorizontal ? plots.size() : 1);  // Horizontal: 1 row, N columns

        // Add entries for each plot
        for (int i = 0; i < plots.size(); i++) {
            LinePlot plot = plots.get(i);
            String plotTitle = plotTitles.get(i);  // Store titles from plot specs

            Legend.LegendEntry entry = Legend.LegendEntry.builder()
                    .label(plotTitle)
                    .lineColor(plot.getStrokeStyle().getColor())
                    .lineWidth(plot.getStrokeStyle().getWidth())
                    .build();

            legendBuilder.addEntry(entry);
        }

        sceneBuilder.addElement(legendBuilder.build());
    }

    // Add all plots
    for (LinePlot plot : plots) {
        sceneBuilder.addElement(plot);
    }

    scenes.add(sceneBuilder.build());
}

private boolean hasPlotTitles() {
    return plotTitles.stream().anyMatch(t -> t != null && !t.isEmpty());
}

private Legend.Position mapKeyPosition(String pos) {
    switch (pos) {
        case "LEFT": return Legend.Position.TOP_LEFT;
        case "RIGHT": return Legend.Position.TOP_RIGHT;
        case "TOP": return Legend.Position.TOP_CENTER;
        case "BOTTOM": return Legend.Position.BOTTOM_CENTER;
        case "BOTTOM_LEFT": return Legend.Position.BOTTOM_LEFT;
        case "BOTTOM_RIGHT": return Legend.Position.BOTTOM_RIGHT;
        case "BOTTOM_CENTER": return Legend.Position.BOTTOM_CENTER;
        case "TOP_CENTER": return Legend.Position.TOP_CENTER;
        default: return Legend.Position.TOP_RIGHT;
    }
}
```

**Note**: Need to also store plot titles from PlotCommand.PlotSpec:
```java
private List<String> plotTitles = new ArrayList<>();

@Override
public void visitPlotCommand(PlotCommand command) {
    plots.clear();
    plotTitles.clear();  // Clear titles too

    for (PlotCommand.PlotSpec spec : command.getPlotSpecs()) {
        // ... existing plot creation code ...

        plotTitles.add(spec.getTitle() != null ? spec.getTitle() : spec.getExpression());
    }

    createAndAddScene();
}
```

### Phase 4: Integration Test (1 SP)

**Test** (ScriptExecutionTest.java):
```java
@Test
void executeScriptWithLegendRendering() throws IOException {
    String script = """
            set title "Plot with Legend"
            set key left box
            plot sin(x) title "Sine", cos(x) title "Cosine"
            """;

    GnuplotScript gnuplotScript = parser.parse(script);
    executor.execute(gnuplotScript);

    // Verify output file was created
    Path outputFile = Path.of("output.svg");
    assertThat(outputFile).exists();

    // Read SVG content
    String svgContent = Files.readString(outputFile);

    // Verify legend box is rendered (should have <rect> for border)
    assertThat(svgContent).contains("<rect");
    int rectCount = svgContent.split("<rect").length - 1;
    assertThat(rectCount).as("Should render legend border").isGreaterThan(1);

    // Verify legend labels are rendered
    assertThat(svgContent).as("Should render 'Sine' label").contains("Sine");
    assertThat(svgContent).as("Should render 'Cosine' label").contains("Cosine");

    // Clean up
    Files.deleteIfExists(outputFile);
}

@Test
void executeScriptWithLegendNoBox() throws IOException {
    String script = """
            set key right nobox
            plot sin(x), cos(x)
            """;

    GnuplotScript gnuplotScript = parser.parse(script);
    executor.execute(gnuplotScript);

    Path outputFile = Path.of("output.svg");
    String svgContent = Files.readString(outputFile);

    // Should still have legend labels but no border rect
    // (Background rect will still exist for the whole scene)
    assertThat(svgContent).contains("sin(x)");
    assertThat(svgContent).contains("cos(x)");

    Files.deleteIfExists(outputFile);
}
```

### Phase 5: Visual Verification with simple.dem

**Run demo test**:
```bash
mvn test -Dtest=DemoTestSuite -q
```

**Expected improvements**:
- Java SVG files increase from 4-13KB to closer to C's 12-37KB
- Legend boxes visible in plots 1, 3, 7
- Legend without box in plot 2
- Bottom-center legend in plot 6
- Plot labels: "sin(x)", "atan(x)", "cos(atan(x)", etc.

**Visual comparison checklist**:
- [ ] Plot 1: Legend left with box, 3 entries (sin, atan, cos(atan))
- [ ] Plot 2: Legend right without box, 2 entries
- [ ] Plot 3: Legend left with box, 2 entries (asin, acos)
- [ ] Plot 4: Legend with besj0 and x**besj0 entries
- [ ] Plot 5: Legend with real(sin) entry
- [ ] Plot 6: Legend at bottom center, horizontal, 2 entries
- [ ] Plot 7: Legend left with box, 1 entry
- [ ] Plot 8: Legend with data file entries

## Implementation Order (TDD Red-Green-Refactor)

**PHASE 0 (COLOR) - DO FIRST:**
1. **Red**: Write test expecting different colors for multi-plot ❌
2. **Green**: Add DEFAULT_COLORS array to executor ✅
3. **Green**: Update visitPlotCommand to assign colors ✅
4. **Refactor**: Verify color cycling works correctly
5. **Test**: Run simple.dem, verify colors match C Gnuplot (purple, green, blue)

**PHASE 1-4 (LEGEND):**
6. **Red**: Write parser test expecting structured key settings ❌
7. **Green**: Implement SetKeyContext parsing ✅
8. **Refactor**: Clean up parseKeyPosition method
9. **Red**: Write executor test expecting legend in scene ❌
10. **Green**: Implement key state management in executor ✅
11. **Green**: Implement legend creation in createAndAddScene ✅
12. **Refactor**: Extract legend creation to helper method
13. **Red**: Write integration test expecting legend in SVG ❌
14. **Green**: Run test, verify it passes ✅
15. **Test**: Run simple.dem, open HTML report, verify legends match C output

## File Size Impact

**Before** (current):
- simple_java.svg: 4.6K
- simple_java_002.svg: 5.1K
- simple_java_003.svg: 2.9K
- simple_java_004.svg: 6.5K

**After** (estimated with legends):
- simple_java.svg: ~8-10K (legend adds ~3-5K for box + entries)
- simple_java_002.svg: ~7-9K (legend without box adds ~2-4K)
- simple_java_003.svg: ~5-7K
- simple_java_004.svg: ~9-11K

**Target** (C Gnuplot):
- simple_c.svg: 12K
- simple_c_002.svg: 14K
- simple_c_003.svg: 14K
- simple_c_004.svg: 37K (outlier - has many data points)

## Success Criteria

✅ All parser tests pass (3 new tests)
✅ All executor tests pass (integration test)
✅ simple.dem passes in demo test suite
✅ Visual comparison shows legends matching C Gnuplot:
   - Correct position (left, right, bottom)
   - Border shown/hidden correctly
   - Horizontal vs vertical layout
   - All plot labels visible
✅ File sizes closer to C Gnuplot (within 20-40%)
✅ No regressions in existing tests (377 render + 45 CLI tests)

## Related Stories

- ✅ Story 3.5.2: Legend System (COMPLETE) - Infrastructure
- ✅ Story TDD-4: simple.dem Grammar (COMPLETE) - Parser foundation
- ✅ Story TDD-5: Axis Integration (COMPLETE) - Scene building pattern
- 🔲 Story TDD-7: Point Markers (FUTURE) - `set style data points`
- 🔲 Story TDD-8: Border/Frame (FUTURE) - Plot area border

## Notes

- Legend is the #1 missing feature based on visual gap analysis
- All infrastructure exists - just needs integration
- Estimated 8 SP (2+2+3+1) for complete implementation
- Following same TDD pattern as Story TDD-5 (Axis Integration)
- Should significantly improve visual parity with C Gnuplot
