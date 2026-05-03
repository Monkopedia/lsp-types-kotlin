# Agent preamble

Read this once when launched. Apply to every agent task in this repo.

## Tooling

- Use `Read`, `Grep`, `Glob`, `Edit`, `Write` over their `bash` equivalents (`cat`, `grep`, `find`, `sed`).
- Spawn sub-agents with model `opus` for any code work.
- Run agents in the background unless the user asks otherwise.

## What to NOT touch

- Anything under `.claude/` (rules, agent-preamble, project memory): triggers approval prompts and blocks autonomous work. If you genuinely need a rule change, surface it to the user first.
- Build outputs (`build/`, `.gradle/`, etc).

## Workflow

For any non-trivial change: design → approve → test → implement → verify. See `.claude/rules/workflow.md`.

## Build & verify

`JAVA_HOME=/usr/lib/jvm/java-21-openjdk` for every Gradle command.

Always before committing:
- `./gradlew ktlintFormat` (formats Kotlin)
- `./gradlew allTests ktlintCheck` for the modules you touched

Match commit-message style — `git log --oneline -10` for recent examples.
