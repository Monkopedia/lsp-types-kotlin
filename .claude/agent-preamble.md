# Agent preamble

Read this once when launched. Apply to every agent task in this repo.

## Tooling

- Use `Read`, `Grep`, `Glob`, `Edit`, `Write` over their `bash` equivalents (`cat`, `grep`, `find`, `sed`).
- Spawn sub-agents with model `opus` for any code work.
- Run agents in the background unless the user asks otherwise.

## What to NOT touch

- **⛔ GENERATED SOURCE — 49 files carrying `// Auto-generated from LSP metaModel.json`.** Editing one **silently loses your work**: the next `:lsp-codegen:generate` overwrites it with no error, no conflict, and nothing to notice in a diff. Fix `:lsp-codegen`, never the output.
  **It is NOT "`:lsp` is generated, `:lsp-ksrpc` is hand-written."** The generator writes into **both**. `:lsp-ksrpc/src/commonMain/kotlin/com/monkopedia/lsp/` contains five generated files —
  `DefaultLanguageClient.kt`, `DefaultLanguageServer.kt`, `KsrpcLanguageClient.kt`, `KsrpcLanguageServer.kt`, `LifecycleTrackingLanguageServer.kt` —
  sitting beside hand-written wiring and reading exactly like it. **Grep for the header before editing any file under `:lsp-ksrpc`'s `commonMain`.**
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
