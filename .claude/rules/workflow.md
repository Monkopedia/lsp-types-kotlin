---
description: Design-first TDD workflow for all non-trivial features
globs: **/*
---

# Development Workflow

## Design Before Code

For any feature that touches more than 2-3 files or involves architectural decisions:

1. Use plan mode to explore and design before writing code
2. Present the plan with: what files change, what the interfaces look like, how it wires together
3. Wait for explicit approval — "sure", "go ahead", "looks good", etc.
4. Do NOT start writing code during the design phase

## Test-Driven Implementation

After design approval:

1. Write tests that define the expected behavior of the new code
2. Run the tests — they MUST fail (if they pass, the tests aren't testing anything new)
3. Implement the minimum code to make the tests pass
4. Do NOT modify tests to match your implementation — if tests fail, fix the implementation
5. Refactor only after tests are green

## When to Skip TDD

- Trivial changes (typos, renames, config tweaks)
- Build/CI configuration
- Documentation
- Codegen output (generated types are validated by Layer A round-trip tests)

## Post-Implementation Checklist

After completing any implementation work:

1. `./gradlew allTests` — all tests pass
2. `./gradlew ktlintCheck` — no lint issues
3. `./gradlew ktlintFormat` if formatting issues found
4. Commit with a clear message describing why, not just what
