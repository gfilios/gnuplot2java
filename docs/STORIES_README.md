# Story Documentation Organization

This document explains how story and backlog documentation is organized in this project.

---

## Organization Structure

### Root Directory - Active & High-Priority Stories

Files in the root directory are **active stories** or **critical references**:

- **[BACKLOG_IMPULSES_POINTS.md](../BACKLOG_IMPULSES_POINTS.md)** - Active: Implement impulses and fix point markers
- **[3D_YAXIS_POSITIONING_ANALYSIS.md](../3D_YAXIS_POSITIONING_ANALYSIS.md)** - Active: 3D positioning fix (ready to implement)

**Why root?** These need immediate visibility for Claude Code session startup.

### docs/ Directory - Detailed Story Documentation

Files in `docs/` are **detailed roadmaps** and **epic documentation**:

- **[STORY_TDD4_ROADMAP.md](STORY_TDD4_ROADMAP.md)** - simple.dem implementation roadmap
- **[STORY_TDD6_LEGEND_INTEGRATION.md](STORY_TDD6_LEGEND_INTEGRATION.md)** - Legend system integration (epic)

**Why docs/?** These are comprehensive, multi-phase implementation plans.

### Backlog Organization

**Summary & Navigation:**
- **[BACKLOG_SUMMARY.md](../BACKLOG_SUMMARY.md)** - Start here for backlog overview

**Phase-Specific Details:**
- **[BACKLOG_PHASE_0_SETUP.md](../BACKLOG_PHASE_0_SETUP.md)** - Phase 0 stories (complete)
- **[BACKLOG_PHASE_1_MATH.md](../BACKLOG_PHASE_1_MATH.md)** - Phase 1 stories (complete)
- **[BACKLOG_PHASE_2_DATA.md](../BACKLOG_PHASE_2_DATA.md)** - Phase 2 stories (complete)
- **[BACKLOG_PHASE_3_RENDER.md](../BACKLOG_PHASE_3_RENDER.md)** - Phase 3 stories (in progress)
- **[BACKLOG_PHASE_4_BACKEND.md](../BACKLOG_PHASE_4_BACKEND.md)** - Phase 4 stories (planned)
- **[BACKLOG_PHASE_5_FRONTEND.md](../BACKLOG_PHASE_5_FRONTEND.md)** - Phase 5 stories (planned)
- **[BACKLOG_PHASE_6_INTEGRATION.md](../BACKLOG_PHASE_6_INTEGRATION.md)** - Phase 6 stories (planned)
- **[BACKLOG_PHASE_7_CLI.md](../BACKLOG_PHASE_7_CLI.md)** - Phase 7 stories (complete)

---

## Story Lifecycle

### 1. Idea → Backlog Phase File

New stories start in the appropriate phase backlog file:
- Added to the phase-specific BACKLOG_PHASE_*.md file
- Status: ⚪ PLANNED

### 2. Planning → Detailed Story Doc

When a story needs detailed planning:
- Create `docs/STORY_<NAME>.md` with roadmap
- Status: 🟡 IN PROGRESS (planning)

### 3. Active Work → Root Directory

When a story becomes actively worked on:
- Optionally create root-level `BACKLOG_<NAME>.md` for immediate visibility
- Update [CLAUDE_SESSION_START.md](../CLAUDE_SESSION_START.md) to reference it
- Status: 🟡 IN PROGRESS (implementation)

### 4. Completion → Archive

When a story is complete:
- Update phase backlog file with ✅
- Keep detailed docs in place for reference
- Remove from active references in CLAUDE_SESSION_START.md
- Status: 🟢 COMPLETE

---

## Current Active Stories

**Note**: All previously tracked stories have been completed and archived.

### Status (2025-12-16)
- **simple.dem** ✅ COMPLETE - 8/8 plots working
- **scatter.dem** ✅ COMPLETE - 3D with contours
- **controls.dem** ✅ COMPLETE - Complex number support
- **3D Axis Fixes** ✅ COMPLETE - All positioning fixed
- **Impulses & Points** ✅ COMPLETE - Fully implemented

### Future Work
See **[IMPLEMENTATION_STATUS.md](../IMPLEMENTATION_STATUS.md)** for current issues and next steps.
See **[FUTURE_ROADMAP.md](../FUTURE_ROADMAP.md)** for planned features (Backend, Frontend, Integration).

---

## Best Practices

### When to Create a Story Doc

**Create in root** (BACKLOG_<NAME>.md):
- Story needs immediate attention (this week)
- Story is blocking other work
- Story has critical bugs
- Estimated effort: < 1 week

**Create in docs/** (STORY_<NAME>.md):
- Story is a multi-week epic
- Story needs detailed roadmap
- Story has multiple phases
- Estimated effort: > 1 week

**Keep in backlog file** (BACKLOG_PHASE_*.md):
- Story is planned but not active
- Story is part of larger phase
- Story has simple requirements

### Naming Conventions

- **BACKLOG_<NAME>.md** - Active backlog items (root)
- **STORY_<NAME>.md** - Detailed epic documentation (docs/)
- **BACKLOG_PHASE_<N>_<NAME>.md** - Phase-specific backlogs (root)
- **BACKLOG_SUMMARY.md** - Overall backlog summary (root)

### Story Documentation Template

When creating a new story doc, include:

```markdown
# Story: <Name>

**Priority**: P0/P1/P2
**Effort**: <X> Story Points / <Y> hours
**Status**: 🟡 IN PROGRESS / ⚪ PLANNED / 🟢 COMPLETE

## Overview
Brief description of the story.

## Acceptance Criteria
- [ ] Criterion 1
- [ ] Criterion 2

## Implementation Plan
### Phase 1: <Name>
- Task 1
- Task 2

## C Code References
- File: gnuplot-c/src/<file>.c
- Function: <function_name>

## Tests
- Test 1
- Test 2

## Related Documentation
- [Link 1](path)
```

---

## Finding Stories

### By Status

```bash
# Find all active stories (in progress)
grep -r "🟡 IN PROGRESS" *.md docs/*.md

# Find completed stories
grep -r "🟢 COMPLETE" BACKLOG_*.md

# Find planned stories
grep -r "⚪ PLANNED" BACKLOG_*.md
```

### By Priority

```bash
# Find P0 (critical) stories
grep -r "P0" BACKLOG_*.md docs/*.md

# Find P1 (high priority) stories
grep -r "P1" BACKLOG_*.md docs/*.md
```

### By Phase

```bash
# List all Phase 3 stories
cat BACKLOG_PHASE_3_RENDER.md

# Search within phase
grep "Epic" BACKLOG_PHASE_3_RENDER.md
```

---

## Related Documentation

- **[BACKLOG_SUMMARY.md](../BACKLOG_SUMMARY.md)** - Overall backlog overview
- **[DOCUMENTATION_INDEX.md](../DOCUMENTATION_INDEX.md)** - Complete docs index
- **[CLAUDE_SESSION_START.md](../CLAUDE_SESSION_START.md)** - Session startup guide (references active stories)

---

**Last Updated:** 2025-11-04
**Purpose:** Explain story documentation organization
**Version:** 1.0
