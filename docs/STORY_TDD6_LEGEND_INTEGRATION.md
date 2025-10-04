# Story TDD-6: Legend Integration for simple.dem

**Status**: PLANNED
**Story Points**: 8 SP
**Priority**: HIGH (Required for simple.dem to pass visual comparison)

## Goal

Integrate Legend rendering into CLI to match C Gnuplot output. This addresses the primary visual gap in simple.dem where Java output has no legend/key display.

## Gap Analysis

After comparing C vs Java SVG outputs for simple.dem:

### Missing Features (Priority Order):

1. **Legend/Key Display** ⭐ CRITICAL
   - C Output: Shows legend box with plot labels ("sin(x)", "atan(x)", "cos(atan(x))")
   - Java Output: No legend visible
   - Impact: Makes it impossible to identify which line is which
   - Gnuplot command: `set key left box`, `set key right nobox`, `set key bmargin center horizontal`

2. **Plot Area Border/Frame**
   - C Output: Black border around plot area
   - Java Output: No border
   - Impact: Less defined plot region

3. **Point Markers** (for `set style data points`)
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

1. **Red**: Write parser test expecting structured key settings ❌
2. **Green**: Implement SetKeyContext parsing ✅
3. **Refactor**: Clean up parseKeyPosition method
4. **Red**: Write executor test expecting legend in scene ❌
5. **Green**: Implement key state management in executor ✅
6. **Green**: Implement legend creation in createAndAddScene ✅
7. **Refactor**: Extract legend creation to helper method
8. **Red**: Write integration test expecting legend in SVG ❌
9. **Green**: Run test, verify it passes ✅
10. **Test**: Run simple.dem, open HTML report, verify legends match C output

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
