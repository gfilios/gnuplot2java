# Story TDD-4: simple.dem Compliance Roadmap

## Goal
Make simple.dem pass by implementing missing grammar rules and features identified by gap analysis.

**Story Points**: 21 SP
**Current Status**: 🔴 Not Started
**Demo Pass Rate**: 0/8 plots in simple.dem

## Gap Analysis Results

Based on error analysis from test run:

```
Line 1:21 - mismatched input ',' in: set term svg size 800,600
Line 8:25 - missing '\n' at 'font' in: set title "Simple Plots" font ",20"
Line 28:16 - mismatched 'center' in: set key bmargin center
Line 29:18-44 - range syntax errors in: plot [-30:20] expression
Multiple - token recognition error at: ''' (single quotes for file paths)
```

## Implementation Plan

### Phase 1: Grammar Fixes (8 SP)

**Task 1.1: Terminal Size Specification** (2 SP)
- Fix: `set term svg size 800,600`
- Update grammar rule: `terminalOptions : SIZE NUMBER COMMA NUMBER`
- Test: Terminal size parsing

**Task 1.2: Font Specification** (2 SP)
- Fix: `set title "text" font ",20"`
- Update grammar: `TITLE string (FONT string)?`
- Support font shorthand: `",20"` means default font, size 20

**Task 1.3: Key Position Modifiers** (2 SP)
- Fix: `set key bmargin center`
- Update grammar: `keyPosition : (BMARGIN|TMARGIN|LMARGIN|RMARGIN) (CENTER|LEFT|RIGHT)?`
- Support horizontal/vertical alignment

**Task 1.4: Single-Quoted Strings** (1 SP)
- Fix: `'1.dat'` file paths
- Add STRING token for single quotes: `STRING : '\'' ~[']* '\''`
- Update string rule to accept both double and single quotes

**Task 1.5: Plot Range Syntax** (1 SP)
- Fix: `plot [-30:20] expression`
- Update plotCommand: `PLOT range? plotSpec`
- Support inline range before expression

### Phase 2: Set Output File Path (3 SP)

**Task 2.1: Output File Path Handling** (2 SP)
- Current: Executor has outputFile variable but always writes to "output.svg"
- Fix: Pass outputFile to renderer.renderToFile(outputFile)
- Test: Multiple plots write to different files

**Task 2.2: Working Directory Support** (1 SP)
- Handle relative vs absolute paths
- Test: Output to temp directories

### Phase 3: Plot Styles (5 SP)

**Task 3.1: Impulses Style** (3 SP)
- Implement: `with impulses`
- Add ImpulsePlot renderer (vertical lines from x-axis to point)
- Update executor to recognize "impulses" style
- Test: Impulse plot rendering

**Task 3.2: Set Style Data** (2 SP)
- Implement: `set style data points|lines|impulses`
- Store default plot style in executor state
- Apply to plots without explicit 'with' clause
- Test: Default style application

### Phase 4: Data File Reading (5 SP)

**Task 4.1: File Path Parsing** (1 SP)
- Update grammar: `dataSource : string (USING usingSpec)?`
- Support: `'1.dat'`, `"data.txt"`, etc.

**Task 4.2: Data File Reader** (2 SP)
- Implement: DataFileReader class
- Support whitespace-separated columns
- Handle comments (#)
- Test: Read demo data files

**Task 4.3: Plot Integration** (2 SP)
- Read data file and create points
- Support column selection (using 1:2)
- Test: Plot from file data

## Test Strategy

### Unit Tests
1. GrammarTest: Terminal size, font, key position, quotes, ranges
2. ExecutorTest: Output file path, style data, impulses
3. DataFileReaderTest: Read files, parse columns

### Integration Tests
1. simple.dem plot 1: `plot sin(x)` → SVG output
2. simple.dem plot 2: `plot cos(x)` with key position
3. simple.dem plot 3: `plot besj0(x) with impulses`
4. simple.dem plot 4: `plot '1.dat' with impulses`

### Acceptance Criteria
- ✅ All 8 plots in simple.dem execute without errors
- ✅ Java produces SVG output files
- ✅ SVG structural similarity > 80% vs C Gnuplot
- ✅ No parse errors in test output
- ✅ Gap analysis shows 0 P1 issues

## Dependencies

**Required from existing modules:**
- ✅ gnuplot-core: Expression parser, evaluator (already complete)
- ✅ gnuplot-render: SVG renderer, LinePlot (already complete)
- 🔴 gnuplot-render: ImpulsePlot (needs implementation)
- 🔴 gnuplot-cli: Enhanced grammar (needs updates)

**New modules needed:**
- DataFileReader (in gnuplot-cli or gnuplot-core)

## Risk Assessment

**Low Risk:**
- Grammar fixes (well-understood, incremental)
- Output file path (simple change)

**Medium Risk:**
- Data file reading (new functionality, needs testing)
- Plot styles (requires new renderer)

**High Risk:**
- None identified

## Timeline Estimate

- Week 1: Grammar fixes + output file path (11 SP)
- Week 2: Plot styles + data file reading (10 SP)
- Total: 2 weeks for 21 SP

## Success Metrics

**Before:**
- Demo pass rate: 0/231 (0%)
- simple.dem: 0/8 plots working
- Java output: 0 SVG files generated

**After (Target):**
- Demo pass rate: 1/231 (0.4%)
- simple.dem: 8/8 plots working (100%)
- Java output: Matches C Gnuplot structure
- Test infrastructure: Validates all changes

## Next Steps

1. Create feature branch: `feature/tdd-4-simple-dem`
2. Start with Task 1.1 (terminal size grammar)
3. Test incrementally with DemoTestSuite
4. Use gap analysis to verify fixes
5. Commit each task independently
6. Final: Full simple.dem integration test
